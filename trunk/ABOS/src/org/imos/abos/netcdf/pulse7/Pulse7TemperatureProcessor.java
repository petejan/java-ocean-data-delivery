/*
 * IMOS data delivery project
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
public class Pulse7TemperatureProcessor
{
    private static SQLWrapper query    = new SQLWrapper();
    private static Logger logger = Logger.getLogger(Pulse7TemperatureProcessor.class.getName());

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
            PropertyConfigurator.configure("log4j.properties");
            Common.build("ABOS.conf");
        }

        Pulse7TemperatureProcessor cdf = new Pulse7TemperatureProcessor();
        
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
        String SQL = "select distinct depth from raw_instrument_data"
                    + " where mooring_id = 'PULSE_7'"
                    + " and parameter_code = 'WATER_TEMP'"
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
                        + "_" + Pulse7Constants.platform_code + "_"
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
            Dimension timeDim = dataFile.addDimension("TIME",RECORD_COUNT);

            ArrayList dims = null;

            // Define the coordinate variables.
            
            dataFile.addVariable("level", DataType.FLOAT, new Dimension[]
                    {
                        lvlDim
                    });
            dataFile.addVariable("TIME", DataType.INT, new Dimension[]
                    {
                        timeDim
                    });


            

            // Define the netCDF variables for the pressure and temperature
            // data.
            dims = new ArrayList();
            dims.add(timeDim);
            dims.add(lvlDim);
            dataFile.addVariable("pressure", DataType.FLOAT, dims);
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

            //logger.debug("Start timestamp is " + timeArray.get(0));

            //TimeZone tz = TimeZone.getTimeZone("GMT");
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(tz);

            for(int i = 0; i < timeDim.getLength(); i++)
            {
                Timestamp ts = timeArray.get(i);
                //logger.debug("Timestamp " + i + " is " + ts);
                //
                // get the number of seconds from the base time.
                // need to divide by 1000 as getTime() returns the number of millisecs
                //
                long offsetTime = (ts.getTime() - anchorTime)/1000;

                int elapsedHours = (int) offsetTime/3600;
                //logger.debug("Time in hours is " + elapsedHours);
                //times.set(i, (int) ts.getTime());
                times.set(i, elapsedHours);
                //times.set(i, (int) offsetTime);
                //times.set(i, i);
            }

            //logger.debug("End timestamp is " + timeArray.get(timeArray.size() - 1));


            // Create the data.

            ArrayFloat.D2 dataTemp = new ArrayFloat.D2(RECORD_COUNT, lvlDim.getLength());
            ArrayFloat.D2 dataPres = new ArrayFloat.D2(RECORD_COUNT, lvlDim.getLength());

            //for (int lvl = 0; lvl < 1; lvl++)
            for (int lvl = 0; lvl < depthArray.size(); lvl++)
            {
                int tempIterator = 0;
                int pressureIterator = 0;

                ArrayList<Datum> temperatureSet = getTemperaturesForDepth(depthArray.get(lvl));
                ArrayList<Datum> pressureSet = getPressuresForDepth(depthArray.get(lvl));

                for (int record = 0; record < RECORD_COUNT; record++)
                {
                    Timestamp currentTime = timeArray.get(record);
                    
                    Double SST = Double.NaN;
                    Double pressure = Double.NaN;

                    if(temperatureSet != null && temperatureSet.size() > 0)
                    {
                        for(int i = tempIterator; i < temperatureSet.size(); i++)
                        {
                            Datum d = temperatureSet.get(i);
                            if(d.ts.equals(currentTime))
                            {
                                SST = d.val;
                                tempIterator = i;
                                break;
                            }
                            if(d.ts.after(currentTime))
                            {
                                //
                                // safest to reset to 0
                                //
                                tempIterator = 0;
                                break;
                            }
                        }
                    }

                    if(pressureSet != null && pressureSet.size() > 0)
                    {
                        for(int i = pressureIterator; i < pressureSet.size(); i++)
                        {
                            Datum d = pressureSet.get(i);
                            if(d.ts.equals(currentTime))
                            {
                                pressure = d.val;
                                pressureIterator = i;
                                break;
                            }
                            if(d.ts.after(currentTime))
                            {
                                //
                                // safest to reset to 0
                                //
                                pressureIterator = 0;
                                break;
                            }
                        }
                    }

                    dataPres.set(record, lvl, pressure.floatValue());
                    dataTemp.set(record, lvl, SST.floatValue());
                }
            }

            //Create the file. At this point the (empty) file will be written to disk
            dataFile.create();

            // A newly created Java integer array to be initialized to zeros.
            int[] origin = new int[4];

            dataFile.write("level", depths);
            dataFile.write("TIME", times);
            dataFile.write("pressure", origin, dataPres);
            dataFile.write("temperature", origin, dataTemp);


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

    private ArrayList<Datum> getTemperaturesForDepth(Double depth)
    {
        logger.debug("Searching for temperature data for depth " + depth);

        String SQL = " select distinct on (date_trunc('hour',data_timestamp))"
                    + " date_trunc('hour',data_timestamp) as Obs_Time,"
                    + " parameter_value as Water_Temp"
                    + " from raw_instrument_data"
                    + " where mooring_id = 'PULSE_7'"
                    + " and (parameter_code = 'WATER_TEMP')"
                    + " and depth = "
                    + depth
                    + " and data_timestamp between "
                    + StringUtilities.quoteString(Common.getRawSQLTimestamp(startTime))
                    + " and "
                    + StringUtilities.quoteString(Common.getRawSQLTimestamp(endTime))
                    + " order by date_trunc('hour',data_timestamp)"
                    ;
        
        logger.debug(SQL);

        query.setConnection( Common.getConnection() );
        query.executeQuery( SQL );

        Vector dataSet = query.getData();
        if(dataSet != null && dataSet.size() > 0)
        {
            ArrayList<Datum> foo = new ArrayList();
            for(int i = 0; i < dataSet.size(); i++)
            {
                Vector row = (Vector) dataSet.get(i);
                Timestamp t = (Timestamp) row.get(0);
                Double d = ((Number)row.get(1)).doubleValue();

                Datum dd = new Datum(t, d);
                foo.add(dd);
            }
            logger.debug("Found " + foo.size() + " records for temperature data for depth " + depth);
            return foo;
        }
        logger.debug("Found 0 records for temperature data for depth " + depth);
        return null;
    }

    private ArrayList<Datum> getPressuresForDepth(Double depth)
    {
        logger.debug("Searching for pressure data for depth " + depth);

        String SQL = " select distinct on (date_trunc('hour',data_timestamp))"
                    + " date_trunc('hour',data_timestamp) as Obs_Time,"
                    + " parameter_value as Water_pressure"
                    + " from raw_instrument_data"
                    + " where mooring_id = 'PULSE_7'"
                    + " and (parameter_code = 'WATER_PRESSURE')"
                    + " and depth = "
                    + depth
                    + " and data_timestamp between "
                    + StringUtilities.quoteString(Common.getRawSQLTimestamp(startTime))
                    + " and "
                    + StringUtilities.quoteString(Common.getRawSQLTimestamp(endTime))
                    + " order by date_trunc('hour',data_timestamp)"
                    ;

        //logger.debug(SQL);

        query.setConnection( Common.getConnection() );
        query.executeQuery( SQL );

        Vector dataSet = query.getData();
        if(dataSet != null && dataSet.size() > 0)
        {
            ArrayList<Datum> foo = new ArrayList();
            for(int i = 0; i < dataSet.size(); i++)
            {
                Vector row = (Vector) dataSet.get(i);
                Timestamp t = (Timestamp) row.get(0);
                Double d = ((Number)row.get(1)).doubleValue();

                Datum dd = new Datum(t, d);
                foo.add(dd);
            }
            logger.debug("Found " + foo.size() + " records for pressure data for depth " + depth);
            return foo;
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



        dataFile.addVariableAttribute("TIME", "name","TIME");
        dataFile.addVariableAttribute("TIME", "standard_name","TIME");
        dataFile.addVariableAttribute("TIME", "units","hours since 1950-01-01T00:00:00Z");
        dataFile.addVariableAttribute("TIME", "axis","T");
        dataFile.addVariableAttribute("TIME", "valid_min",0.0);
        dataFile.addVariableAttribute("TIME", "valid_max",999999999);
        dataFile.addVariableAttribute("TIME", "calendar","gregorian");
        dataFile.addVariableAttribute("TIME", "quality_control_set",1.0);

        dataFile.addVariableAttribute("level", "units", "metres below surface (nominal)");

        // Define units attributes for data variables.
        dataFile.addVariableAttribute("pressure", "name", "PRES");
        dataFile.addVariableAttribute("pressure", "units", "dbar");
        dataFile.addVariableAttribute("pressure", "standard_name", "sea_water_pressure");
        dataFile.addVariableAttribute("pressure", "valid_min", -999999);
        dataFile.addVariableAttribute("pressure", "valid_max", 999999);
        dataFile.addVariableAttribute("pressure", "quality_control_set", 1.0);


        dataFile.addVariableAttribute("temperature", "name", "TEMP");
        dataFile.addVariableAttribute("temperature", "standard_name", "sea_water_temperature");
        dataFile.addVariableAttribute("temperature", "units", "Celsius");
        dataFile.addVariableAttribute("temperature", "valid_min", 0.0);
        dataFile.addVariableAttribute("temperature", "valid_max", 99.9);
        dataFile.addVariableAttribute("temperature", "quality_control_set", 1.0);
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

}
