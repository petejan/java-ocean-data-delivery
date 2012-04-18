/*
 * Neonatal Screening Software Project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.netcdf.pulse7;

import java.util.Vector;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.wiley.core.Common;
import org.wiley.util.SQLWrapper;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Dimension;
import ucar.ma2.DataType;
import ucar.ma2.ArrayFloat;
import ucar.ma2.InvalidRangeException;

import java.util.ArrayList;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.imos.abos.dbms.Mooring;
import org.wiley.util.StringUtilities;
import ucar.ma2.ArrayInt;

/**
 *
 * @author peter
 */
public class Pulse7NetCDFCreator
{
    private static SQLWrapper query    = new SQLWrapper();
    private static Logger logger = Logger.getLogger(Pulse7NetCDFCreator.class.getName());

    private Mooring currentMooring = null;
    private Timestamp startTime = null;
    private Timestamp endTime = null;

    private ArrayList<Timestamp> timeArray = new ArrayList();
    private ArrayList<Double> depthArray = new ArrayList();

    private NetcdfFileWriteable dataFile = null;

    private TimeZone tz = TimeZone.getTimeZone("GMT");

    public static void main(String args[])
    {
        String $HOME = System.getProperty("user.home");

        if(args.length == 0)
        {
            PropertyConfigurator.configure($HOME + "/ABOS/log4j.properties");
            Common.build($HOME + "/ABOS/ABOS.conf");
        }

        Pulse7NetCDFCreator cdf = new Pulse7NetCDFCreator();

        cdf.createTimeArray();
        cdf.createDepthArray();
        cdf.createCDFFile();
    }

    private void createTimeArray()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        currentMooring = Mooring.selectByMooringID("PULSE_7");

        startTime = currentMooring.getTimestampIn();
        endTime = currentMooring.getTimestampOut();

        tz = TimeZone.getTimeZone("GMT");
        GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
        cal.setTimeZone(tz);
        cal.setTimeInMillis(startTime.getTime());

        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long baseMillis = cal.getTimeInMillis();
        Timestamp current = new Timestamp(baseMillis);

        logger.debug("Starting timestamp is " + current);

        while(current.before(endTime))
        {
            timeArray.add(new Timestamp(baseMillis));
            baseMillis += 3600000;
            current.setTime(baseMillis);
            //logger.debug("Current timestamp is " + current);
        }

        timeArray.add(current);
        logger.debug("Finished generating time array, last timestamp was " + current + "\nTotal Elements: " + timeArray.size());
    }

    private void createDepthArray()
    {
        String SQL = "select distinct depth from processed_instrument_data"
                    + " where mooring_id = 'PULSE_7'"
                    + " order by depth"
                    ;
        query.setConnection( Common.getConnection() );
        query.executeQuery( SQL );

        Vector depthSet = query.getData();
        if(depthSet != null && depthSet.size() > 0)
        {
            for(int i = 0; i < depthSet.size(); i++)
            {
                Vector row = (Vector) depthSet.get(i);
                Double d = new Double( ((Number) row.get(0)).doubleValue());
                depthArray.add(d);
            }
        }
        logger.debug("Finished generating depth array, number of depths is " + depthArray.size());
    }

    private void createCDFFile()
    {

        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat nameFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        nameFormatter.setTimeZone(tz);

        long anchorTime = 0;
        try
        {
            Date ts = df.parse("1950-01-01 00:00:00");
            anchorTime = ts.getTime();
        }
        catch(ParseException pex)
        {
            logger.error(pex);
        }

        //String filename = "/Users/peter/Pulse7Temperature.nc";

        String filename = System.getProperty("user.home")
                        + "/"
                        + Pulse7Constants.PREFIX
                        + Pulse7Constants.FACILITY_CODE
                        + Pulse7Constants.DATA_CODE
                        + Pulse7Constants.START_DATE
                        + nameFormatter.format(startTime)
                        + Pulse7Constants.PLATFORM_CODE
                        + Pulse7Constants.FILE_VERSION
                        + Pulse7Constants.PRODUCT_TYPE
                        + Pulse7Constants.END_DATE
                        + nameFormatter.format(endTime)
                        + Pulse7Constants.CREATION_DATE
                        + nameFormatter.format(System.currentTimeMillis())
                        + Pulse7Constants.SUFFIX
                        ;

        int RECORD_COUNT = timeArray.size();

        try
        {
            // Create new netcdf-3 file with the given filename
            dataFile = NetcdfFileWriteable.createNew(filename, false);

            writeGlobalAttributes();

            //add dimensions
            Dimension lvlDim = dataFile.addDimension("level", depthArray.size());
            Dimension timeDim = dataFile.addDimension("time",RECORD_COUNT);

            ArrayList dims = null;

            // Define the coordinate variables.

            dataFile.addVariable("level", DataType.FLOAT, new Dimension[]
                    {
                        lvlDim
                    });
            dataFile.addVariable("time", DataType.INT, new Dimension[]
                    {
                        timeDim
                    });




            // Define the netCDF variables for the pressure and temperature
            // data.
            dims = new ArrayList();
            dims.add(timeDim);
            //dims.add(lvlDim);
            dataFile.addVariable("temperature", DataType.FLOAT, dims);

            //
            // got to add the variables before you can write their attributes
            //
            writeVariableAttributes();

            ArrayFloat.D1 depths = new ArrayFloat.D1(lvlDim.getLength());
            ArrayInt.D1 times = new ArrayInt.D1(timeDim.getLength());

            for (int j = 0; j < lvlDim.getLength(); j++)
            {
                Double currentDepth = depthArray.get(j);
                depths.set(j, currentDepth.floatValue());
            }

            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(tz);

            for(int i = 0; i < timeDim.getLength(); i++)
            {
                Timestamp ts = timeArray.get(i);
                long offsetTime = (ts.getTime() - anchorTime)/1000;
                int elapsedHours = (int) offsetTime/3600;
                times.set(i, elapsedHours);
            }

            // Create the data.

            ArrayList<String> varNames = new ArrayList();
            ArrayList<ArrayFloat.D1> stuff = new ArrayList();

            

            for (int lvl = 0; lvl < depthArray.size(); lvl++)
            {
                int rowIterator = 0;

                ArrayList<ArrayList> masterSet = getDataForDepth(depthArray.get(lvl));

                if(masterSet != null && masterSet.size() > 0)
                {
                    for(int setSize = 0; setSize < masterSet.size(); setSize++)
                    {
                        ArrayFloat.D1 dataTemp = new ArrayFloat.D1(RECORD_COUNT);

                        ArrayList<ParamDatum> dataSet = masterSet.get(setSize);

                        ParamDatum d = dataSet.get(0);
                        logger.debug("Processing instrument/parameter "
                                    + d.instrumentID
                                    + "/"
                                    + d.paramCode
                                    + " for depth "
                                    + depthArray.get(lvl))
                                    ;

                        String varName = d.instrumentID 
                                        + "_"
                                        + d.paramCode
                                        + "_DEPTH_"
                                        + depthArray.get(lvl).intValue();

                        varNames.add(varName);
                        dataFile.addVariable(varName, DataType.FLOAT, dims);

                        for (int record = 0; record < RECORD_COUNT; record++)
                        {
                            Timestamp currentTime = timeArray.get(record);

                            Double SST = Double.NaN;

                            for(int i = rowIterator; i < dataSet.size(); i++)
                            {
                                ParamDatum currentValue = dataSet.get(i);

                                if(currentValue.ts.equals(currentTime))
                                {
                                    SST = currentValue.val;
                                    rowIterator = i;
                                    break;
                                }
                                if(currentValue.ts.after(currentTime))
                                {
                                    //
                                    // safest to reset to 0
                                    //
                                    rowIterator = 0;
                                    break;
                                }
                            }

                            dataTemp.set(record, SST.floatValue());
                        }

                        stuff.add(dataTemp);
                    }
                }
            }

            //Create the file. At this point the (empty) file will be written to disk
            dataFile.create();

            // A newly created Java integer array to be initialized to zeros.
            int[] origin = new int[varNames.size() + 2];

            dataFile.write("level", depths);
            dataFile.write("time", times);
            for(int i = 0; i < varNames.size(); i++)
            {
                dataFile.write(varNames.get(i), origin, stuff.get(i));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
        }
        catch (InvalidRangeException e)
        {
            e.printStackTrace(System.err);
        }
        finally
        {
            if (dataFile != null)
            {
                try
                {
                    dataFile.close();
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }
        }
        System.out.println("*** SUCCESS writing file " + filename);
    }

    private ArrayList<ArrayList> getDataForDepth(Double depth)
    {
        ArrayList<ArrayList> set = new ArrayList();

        logger.debug("Searching for data for depth " + depth);

        String SQL = " select data_timestamp,"
                    + " instrument_id,"
                    + " parameter_code,"
                    + " parameter_value"
                    + " from processed_instrument_data"
                    + " where mooring_id = 'PULSE_7'"
                    + " and depth = "
                    + depth
                    + " and data_timestamp between "
                    + StringUtilities.quoteString(Common.getRawSQLTimestamp(startTime))
                    + " and "
                    + StringUtilities.quoteString(Common.getRawSQLTimestamp(endTime))
                    + " order by instrument_id, parameter_code, data_timestamp"
                    ;

        //logger.debug(SQL);

        query.setConnection( Common.getConnection() );
        query.executeQuery( SQL );

        String currentParam = "";
        Integer currentInstrument = 0;

        Vector dataSet = query.getData();
        if(dataSet != null && dataSet.size() > 0)
        {
            ArrayList<ParamDatum> foo = new ArrayList();
            for(int i = 0; i < dataSet.size(); i++)
            {
                Vector row = (Vector) dataSet.get(i);
                Timestamp t = (Timestamp) row.get(0);
                Integer ix = ((Number)row.get(1)).intValue();
                String p = (String) row.get(2);
                Double d = ((Number)row.get(3)).doubleValue();

                ParamDatum dd = new ParamDatum(t, ix, p, d);
                if(i == 0)
                {
                    currentParam = dd.paramCode;
                    currentInstrument = dd.instrumentID;
                }
                else
                {
                    if( (!dd.paramCode.equalsIgnoreCase(currentParam)) || (! dd.instrumentID.equals(currentInstrument)))
                    {
                        currentParam = dd.paramCode;
                        currentInstrument = dd.instrumentID;
                        set.add(foo);
                        foo = new ArrayList();
                    }
                }
                foo.add(dd);
            }

            set.add(foo);

            logger.debug("Found " + dataSet.size() + " records for depth " + depth);
            logger.debug("Created " + set.size() + " sets of parameter/instrument for depth " + depth);
            return set;
        }
        logger.debug("Found 0 records for pressure data for depth " + depth);
        return null;
    }

    private void writeGlobalAttributes()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);

        if(currentMooring != null)
        {
            dataFile.addGlobalAttribute("Mooring", currentMooring.getShortDescription());
            dataFile.addGlobalAttribute("Latitude", currentMooring.getLatitudeIn());
            dataFile.addGlobalAttribute("Longitude", currentMooring.getLongitudeIn());

            Pulse7Constants.geospatial_lat_min = currentMooring.getLatitudeIn();
            Pulse7Constants.geospatial_lat_max = currentMooring.getLatitudeOut();
            Pulse7Constants.geospatial_lon_min = currentMooring.getLongitudeIn();
            Pulse7Constants.geospatial_lon_max = currentMooring.getLongitudeOut();

            if(Pulse7Constants.geospatial_lat_max == null)
                Pulse7Constants.geospatial_lat_max = Pulse7Constants.geospatial_lat_min;

            if(Pulse7Constants.geospatial_lon_max == null)
                Pulse7Constants.geospatial_lon_max = Pulse7Constants.geospatial_lon_min;
        }

        Pulse7Constants.date_created = df.format(Common.today());
        Pulse7Constants.time_coverage_start = df.format(startTime);
        Pulse7Constants.time_coverage_end = df.format(endTime);

        //dataFile.addGlobalAttribute("instrument_make",Pulse7Constants.instrument_make) ;
        //dataFile.addGlobalAttribute("instrument_model",Pulse7Constants.instrument_model) ;
        //dataFile.addGlobalAttribute("instrument_serial_no",Pulse7Constants.instrument_serial_no) ;
        dataFile.addGlobalAttribute("level",Pulse7Constants.level) ;
        dataFile.addGlobalAttribute("date_created",Pulse7Constants.date_created) ;
        dataFile.addGlobalAttribute("field_trip_id",Pulse7Constants.field_trip_id) ;
        dataFile.addGlobalAttribute("field_trip_description",Pulse7Constants.field_trip_description) ;
        dataFile.addGlobalAttribute("project",Pulse7Constants.project) ;
        dataFile.addGlobalAttribute("conventions",Pulse7Constants.conventions) ;
        dataFile.addGlobalAttribute("title",Pulse7Constants.title) ;
        //dataFile.addGlobalAttribute("institution",Pulse7Constants.institution) ;
        //dataFile.addGlobalAttribute("source",Pulse7Constants.source) ;
        dataFile.addGlobalAttribute("netcdf_version",Pulse7Constants.netcdf_version) ;
        //dataFile.addGlobalAttribute("quality_control_set",Pulse7Constants.quality_control_set) ;
        dataFile.addGlobalAttribute("site_code",Pulse7Constants.site_code) ;
        dataFile.addGlobalAttribute("platform_code",Pulse7Constants.platform_code) ;
        dataFile.addGlobalAttribute("naming_authority",Pulse7Constants.naming_authority) ;
        dataFile.addGlobalAttribute("product_type",Pulse7Constants.product_type) ;
        dataFile.addGlobalAttribute("geospatial_lat_min",Pulse7Constants.geospatial_lat_min) ;
        dataFile.addGlobalAttribute("geospatial_lat_max",Pulse7Constants.geospatial_lat_max) ;
        dataFile.addGlobalAttribute("geospatial_lon_min",Pulse7Constants.geospatial_lon_min) ;
        dataFile.addGlobalAttribute("geospatial_lon_max",Pulse7Constants.geospatial_lon_max) ;
        dataFile.addGlobalAttribute("geospatial_vertical_min",Pulse7Constants.geospatial_vertical_min) ;
        dataFile.addGlobalAttribute("geospatial_vertical_max",Pulse7Constants.geospatial_vertical_max) ;
        dataFile.addGlobalAttribute("time_coverage_start",Pulse7Constants.time_coverage_start) ;
        dataFile.addGlobalAttribute("time_coverage_end",Pulse7Constants.time_coverage_end) ;
        dataFile.addGlobalAttribute("local_time_zone",Pulse7Constants.local_time_zone) ;
        dataFile.addGlobalAttribute("data_centre",Pulse7Constants.data_centre) ;
        dataFile.addGlobalAttribute("data_centre_email",Pulse7Constants.data_centre_email) ;
        dataFile.addGlobalAttribute("author_email",Pulse7Constants.author_email) ;
        dataFile.addGlobalAttribute("author",Pulse7Constants.author) ;
        dataFile.addGlobalAttribute("principal_investigator",Pulse7Constants.principal_investigator) ;
        dataFile.addGlobalAttribute("principal_investigator_email",Pulse7Constants.principal_investigator_email) ;
        dataFile.addGlobalAttribute("acknowledgement_ = ",Pulse7Constants.acknowledgement) ;
        dataFile.addGlobalAttribute("raw_data_file",Pulse7Constants.raw_data_file) ;
        dataFile.addGlobalAttribute("index",Pulse7Constants.index) ;
    }

    private void writeVariableAttributes()
    {

        dataFile.addVariableAttribute("time", "name","TIME");
        dataFile.addVariableAttribute("time", "standard_name","time");
        dataFile.addVariableAttribute("time", "units","hours since 1950-01-01T00:00:00Z");
        dataFile.addVariableAttribute("time", "axis","T");
        dataFile.addVariableAttribute("time", "valid_min",0.0);
        dataFile.addVariableAttribute("time", "valid_max",999999999);
        dataFile.addVariableAttribute("time", "calendar","gregorian");
        dataFile.addVariableAttribute("time", "quality_control_set",1.0);

//        dataFile.addVariableAttribute("level", "units", "metres below surface (nominal)");
//
//        // Define units attributes for data variables.
//        dataFile.addVariableAttribute("pressure", "name", "PRES");
//        dataFile.addVariableAttribute("pressure", "units", "dbar");
//        dataFile.addVariableAttribute("pressure", "standard_name", "sea_water_pressure");
//        dataFile.addVariableAttribute("pressure", "valid_min", -999999);
//        dataFile.addVariableAttribute("pressure", "valid_max", 999999);
//        dataFile.addVariableAttribute("pressure", "quality_control_set", 1.0);
//
//
//        dataFile.addVariableAttribute("temperature", "name", "TEMP");
//        dataFile.addVariableAttribute("temperature", "standard_name", "sea_water_temperature");
//        dataFile.addVariableAttribute("temperature", "units", "Celsius");
//        dataFile.addVariableAttribute("temperature", "valid_min", 0.0);
//        dataFile.addVariableAttribute("temperature", "valid_max", 99.9);
//        dataFile.addVariableAttribute("temperature", "quality_control_set", 1.0);
    }

    private class Datum
    {
        public Timestamp ts;
        public Double val;

        public Datum()
        {
            super();
        }

        public Datum(Timestamp t, Double d)
        {
            super();
            ts = t;
            val = d;
        }
    }

    private class ParamDatum
    {
        public Timestamp ts;
        public Integer instrumentID;
        public String paramCode;
        public Double val;

        public ParamDatum()
        {
            super();
        }

        public ParamDatum(Timestamp t, Integer i, String p, Double d)
        {
            super();
            ts = t;
            instrumentID = i;
            paramCode = p.trim();
            val = d;
        }
    }

}
