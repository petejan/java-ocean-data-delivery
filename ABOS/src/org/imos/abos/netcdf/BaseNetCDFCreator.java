/*
 * Neonatal Screening Software Project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.netcdf;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.log4j.Logger;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.dbms.ParameterCodes;
import org.imos.abos.netcdf.pulse7.Pulse7NetCDFCreator;
import org.wiley.core.Common;
import org.wiley.util.SQLWrapper;
import org.wiley.util.StringUtilities;
import ucar.ma2.*;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

/**
 *
 * @author peter
 */
public abstract class BaseNetCDFCreator
{
    private static Logger logger = Logger.getLogger(BaseNetCDFCreator.class.getName());

    protected static SQLWrapper query = new SQLWrapper();
    protected Mooring currentMooring = null;
    protected NetcdfFileWriteable dataFile = null;
    protected TimeZone tz = TimeZone.getTimeZone("GMT");
    protected ArrayList<Double> depthArray = new ArrayList();
    protected Timestamp endTime = null;
    protected Timestamp startTime = null;
    protected ArrayList<Timestamp> timeArray = new ArrayList();

    protected void createDepthArray(String mooringID)
    {
        String SQL = "select distinct depth from processed_instrument_data" + " where mooring_id = " + StringUtilities.quoteString(mooringID) + " order by depth";
        query.setConnection(Common.getConnection());
        query.executeQuery(SQL);
        Vector depthSet = Pulse7NetCDFCreator.query.getData();
        if (depthSet != null && depthSet.size() > 0)
        {
            for (int i = 0; i < depthSet.size(); i++)
            {
                Vector row = (Vector) depthSet.get(i);
                Double d = new Double(((Number) row.get(0)).doubleValue());
                depthArray.add(d);
            }
        }
        logger.debug("Finished generating depth array, number of depths is " + depthArray.size());
    }

    protected void createTimeArray(String mooringID)
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        currentMooring = Mooring.selectByMooringID(mooringID);
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
        while (current.before(endTime))
        {
            timeArray.add(new Timestamp(baseMillis));
            baseMillis += 3600000;
            current.setTime(baseMillis);
        }
        timeArray.add(current);
        logger.debug("Finished generating time array, last timestamp was " + current + "\nTotal Elements: " + timeArray.size());
    }

    protected ArrayList<ArrayList> getDataForDepth(Double depth)
    {
        ArrayList<ArrayList> set = new ArrayList();
        logger.debug("Searching for data for depth " + depth);
        String SQL = " select data_timestamp," 
                + " instrument_id,"
                + " parameter_code,"
                + " parameter_value"
                + " from processed_instrument_data"
                + " where mooring_id = "
                + StringUtilities.quoteString(currentMooring.getMooringID())
                + " and depth = "
                + depth
                + " and data_timestamp between "
                + StringUtilities.quoteString(Common.getRawSQLTimestamp(startTime))
                + " and "
                + StringUtilities.quoteString(Common.getRawSQLTimestamp(endTime))
                + " order by instrument_id, parameter_code, data_timestamp"
                ;
        //logger.debug(SQL);
        BaseNetCDFCreator.query.setConnection(Common.getConnection());
        BaseNetCDFCreator.query.executeQuery(SQL);
        String currentParam = "";
        Integer currentInstrument = 0;
        Vector dataSet = BaseNetCDFCreator.query.getData();
        if (dataSet != null && dataSet.size() > 0)
        {
            ArrayList<ParamDatum> foo = new ArrayList();
            for (int i = 0; i < dataSet.size(); i++)
            {
                Vector row = (Vector) dataSet.get(i);
                Timestamp t = (Timestamp) row.get(0);
                Integer ix = ((Number) row.get(1)).intValue();
                String p = (String) row.get(2);
                Double d = ((Number) row.get(3)).doubleValue();
                ParamDatum dd = new ParamDatum(t, ix, p, d);
                if (i == 0)
                {
                    currentParam = dd.paramCode;
                    currentInstrument = dd.instrumentID;
                }
                else
                {
                    if ((!dd.paramCode.equalsIgnoreCase(currentParam)) || (!dd.instrumentID.equals(currentInstrument)))
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

    protected void writeBaseVariableAttributes()
    {
        dataFile.addVariableAttribute("TIME", "name", "TIME");
        dataFile.addVariableAttribute("TIME", "standard_name", "TIME");
        dataFile.addVariableAttribute("TIME", "units", "days since 1950-01-01T00:00:00Z");
        dataFile.addVariableAttribute("TIME", "axis", "T");
        dataFile.addVariableAttribute("TIME", "valid_min", 0.0);
        dataFile.addVariableAttribute("TIME", "valid_max", 999999999);
        dataFile.addVariableAttribute("TIME", "calendar", "gregorian");
        dataFile.addVariableAttribute("TIME", "quality_control_set", 1.0);
    }

    protected void writeGlobalAttributes()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'");
        df.setTimeZone(tz);
        
        BaseNetCDFConstants.date_created = df.format(Common.today());
        BaseNetCDFConstants.time_coverage_start = df.format(startTime);
        BaseNetCDFConstants.time_coverage_end = df.format(endTime);

      
        dataFile.addGlobalAttribute("date_created", BaseNetCDFConstants.date_created);
        
        dataFile.addGlobalAttribute("project", BaseNetCDFConstants.project);
        dataFile.addGlobalAttribute("conventions", BaseNetCDFConstants.conventions);
        
        dataFile.addGlobalAttribute("institution", BaseNetCDFConstants.institution);
        dataFile.addGlobalAttribute("institution_address", BaseNetCDFConstants.institution_address);
        dataFile.addGlobalAttribute("source", BaseNetCDFConstants.source);
        dataFile.addGlobalAttribute("netcdf_version", BaseNetCDFConstants.netcdf_version);
        dataFile.addGlobalAttribute("quality_control_set", BaseNetCDFConstants.quality_control_set);
        
        dataFile.addGlobalAttribute("naming_authority", BaseNetCDFConstants.naming_authority);
        dataFile.addGlobalAttribute("product_type", BaseNetCDFConstants.product_type);
        dataFile.addGlobalAttribute("geospatial_lat_units", BaseNetCDFConstants.geospatial_lat_units);
        dataFile.addGlobalAttribute("geospatial_lon_units", BaseNetCDFConstants.geospatial_lon_units);
        dataFile.addGlobalAttribute("geospatial_vertical_units", BaseNetCDFConstants.geospatial_vertical_units);
        
        dataFile.addGlobalAttribute("data_centre", BaseNetCDFConstants.data_centre);
        dataFile.addGlobalAttribute("data_centre_email", BaseNetCDFConstants.data_centre_email);
        dataFile.addGlobalAttribute("author_email", BaseNetCDFConstants.author_email);
        dataFile.addGlobalAttribute("author", BaseNetCDFConstants.author);
        dataFile.addGlobalAttribute("principal_investigator", BaseNetCDFConstants.principal_investigator);
        dataFile.addGlobalAttribute("principal_investigator_email", BaseNetCDFConstants.principal_investigator_email);
        dataFile.addGlobalAttribute("acknowledgement", BaseNetCDFConstants.acknowledgement);
        dataFile.addGlobalAttribute("file_version", BaseNetCDFConstants.file_version);
    }

    protected void addVariableAttributes(Integer instrumentID, String paramCode, String variable)
    {
        Instrument ins = Instrument.selectByInstrumentID(instrumentID);
        ParameterCodes param = ParameterCodes.selectByID(paramCode);
        if (ins != null)
        {
            dataFile.addVariableAttribute(variable, "sensor", ins.getMake() + "-" + ins.getModel());
            dataFile.addVariableAttribute(variable, "sensor_serial_number", ins.getSerialNumber());
        }
        dataFile.addVariableAttribute(variable, "name", param.getDescription());
        dataFile.addVariableAttribute(variable, "units", param.getUnits());
        dataFile.addVariableAttribute(variable, "standard_name", param.getNetCDFName());
        dataFile.addVariableAttribute(variable, "valid_min", -999999);
        dataFile.addVariableAttribute(variable, "valid_max", 999999);
        dataFile.addVariableAttribute(variable, "_FillValue", Double.NaN);
        dataFile.addVariableAttribute(variable, "quality_control_set", 1.0);
        dataFile.addVariableAttribute(variable, "csiro_instrument_id", instrumentID);
    }

    protected void createCDFFile()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long anchorTime = 0;
        try
        {
            Date ts = df.parse("1950-01-01 00:00:00");
            anchorTime = ts.getTime();
        }
        catch (ParseException pex)
        {
            logger.error(pex);
        }
        String filename = getFileName();
        int RECORD_COUNT = timeArray.size();
        try
        {
            // Create new netcdf-3 file with the given filename
            dataFile = NetcdfFileWriteable.createNew(filename, false);
            writeGlobalAttributes();
            writeMooringSpecificAttributes();
            //add dimensions
            Dimension lvlDim = dataFile.addDimension("level", depthArray.size());
            Dimension timeDim = dataFile.addDimension("TIME", RECORD_COUNT);
            ArrayList dims = null;
            // Define the coordinate variables.
            dataFile.addVariable("level", DataType.FLOAT, new Dimension[]{lvlDim});
            dataFile.addVariable("TIME", DataType.DOUBLE, new Dimension[]{timeDim});
            // Define the netCDF variables for the pressure and temperature
            // data.
            dims = new ArrayList();
            dims.add(timeDim);
            //dims.add(lvlDim);
            //
            // got to add the variables before you can write their attributes
            //
            writeBaseVariableAttributes();
            ArrayFloat.D1 depths = new ArrayFloat.D1(lvlDim.getLength());
            ArrayDouble.D1 times = new ArrayDouble.D1(timeDim.getLength());
            for (int j = 0; j < lvlDim.getLength(); j++)
            {
                Double currentDepth = depthArray.get(j);
                depths.set(j, currentDepth.floatValue());
            }
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(tz);
            for (int i = 0; i < timeDim.getLength(); i++)
            {
                Timestamp ts = timeArray.get(i);
                long offsetTime = (ts.getTime() - anchorTime) / 1000;
                double elapsedDays = (double) offsetTime / 3600 / 24;
                times.set(i, elapsedDays);
            }
            // Create the data.
            ArrayList<String> varNames = new ArrayList();
            ArrayList<ArrayFloat.D1> stuff = new ArrayList();
            HashSet varNameList = new HashSet();
            
            for (int lvl = 0; lvl < depthArray.size(); lvl++)
            {
                ArrayList<ArrayList> masterSet = getDataForDepth(depthArray.get(lvl));
                if (masterSet != null && masterSet.size() > 0)
                {
                    for (int setSize = 0; setSize < masterSet.size(); setSize++)
                    {
                        int matchedElements = 0;
                        int rowIterator = 0;
                        ArrayFloat.D1 dataTemp = new ArrayFloat.D1(RECORD_COUNT);
                        ArrayList<ParamDatum> dataSet = masterSet.get(setSize);
                        ParamDatum d = dataSet.get(0);
                        String varName = d.paramCode + "_" + depthArray.get(lvl).intValue();
                        int varSeqNo = 1;
                        while (!varNameList.add(varName + "_" + varSeqNo))
                        {
                            varSeqNo++;
                        }
                        varName += "_" + varSeqNo;
                        
                        logger.debug("Processing instrument/parameter " + varName);
                        varNames.add(varName);
                        dataFile.addVariable(varName, DataType.FLOAT, dims);
                        addVariableAttributes(d.instrumentID, d.paramCode, varName);
                        dataFile.addVariableAttribute(varName, "sensor_depth", depthArray.get(lvl).intValue());

                        for (int record = 0; record < RECORD_COUNT; record++)
                        {
                            Timestamp currentTime = timeArray.get(record);
                            Double SST = Double.NaN;
                            for (int i = rowIterator; i < dataSet.size(); i++)
                            {
                                ParamDatum currentValue = dataSet.get(i);
                                if (currentValue.ts.equals(currentTime))
                                {
                                    matchedElements++;
                                    SST = currentValue.val;
                                    rowIterator = i;
                                    break;
                                }
                                if (currentValue.ts.after(currentTime))
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
                        logger.debug("Matched " + matchedElements + " records for " + varName);
                        stuff.add(dataTemp);
                    }
                }
            }
            //Create the file. At this point the (empty) file will be written to disk
            dataFile.create();
            // A newly created Java integer array to be initialized to zeros.
            int[] origin = new int[varNames.size() + 2];
            dataFile.write("level", depths);
            dataFile.write("TIME", times);
            for (int i = 0; i < varNames.size(); i++)
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

    protected abstract String getFileName();
    
    protected abstract void writeMooringSpecificAttributes();
    
    protected class ParamDatum
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
