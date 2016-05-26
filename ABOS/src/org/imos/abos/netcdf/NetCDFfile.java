/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imos.abos.netcdf;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentCalibrationValue;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.dbms.ParameterCodes;
import org.jfree.util.Log;
import org.wiley.core.Common;
import org.wiley.util.SQLWrapper;
import org.wiley.util.StringUtilities;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayString;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

/**
 *
 * @author jan079
 */
public class NetCDFfile
{
    protected TimeZone tz = TimeZone.getTimeZone("UTC");
    private static Logger logger = Logger.getLogger(NetCDFfile.class.getName());
    public NetcdfFileWriter dataFile;
    protected static SQLWrapper query = new SQLWrapper();
    private String authority = "OS";
    private String facility = "ABOS-SOTS";
    private String mooringString = null;
    private String deployment = null;

    private Mooring mooring = null;
    
    public boolean fileOrderTimeDepth = true;

    public Variable vTime;
//    public Variable vPos;
    public Variable vLat;
    public Variable vLon;
    public Dimension timeDim;
    public Dimension name_strlenDim;
    public Variable vStationName;
    public final boolean timeIsDoubleDays = true;
    public SimpleDateFormat netcdfDate;
    long anchorTime;

    public NetCDFfile() 
    {        
        TimeZone.setDefault(tz);
        netcdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        anchorTime = 0;
        try
        {
            java.util.Date ts = df.parse("1950-01-01 00:00:00");
            anchorTime = ts.getTime();
        }
        catch (ParseException pex)
        {
            logger.error(pex);
        }
    }
    
    boolean multiPart = false;
    public boolean setMultiPart(boolean b)
    {
    	multiPart = b;
    	
    	return multiPart;
    }
    public String getFileName(Instrument sourceInstrument, Timestamp dataStartTime, Timestamp dataEndTime, String table)
    {
    	return getFileName(sourceInstrument, dataStartTime, dataEndTime, table, "RTSCP");
    }
    
    public String getFileName(Instrument sourceInstrument, Timestamp dataStartTime, Timestamp dataEndTime, String table, String dataType)
    {
        SimpleDateFormat nameFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        nameFormatter.setTimeZone(tz);

        String filename = "ABOS_NetCDF.nc";
        String deployment = mooring.getMooringID();
        String mooring = deployment.substring(0, deployment.indexOf("-"));
        if (sourceInstrument != null)
        {
        	String sn = sourceInstrument.getSerialNumber().replaceAll("[()_]", "").trim();
            deployment += "_" + sourceInstrument.getModel().trim() + "-" + sn;
        }
        if (authority.equals("IMOS"))
        {
            // IMOS_<Facility-Code>_<Data-Code>_<Start-date>_<Platform-Code>_FV<File-Version>_ <Product-Type>_END-<End-date>_C-<Creation_date>_<PARTX>.nc
            
            // IMOS_ABOS-SOTS_20110803T115900Z_PULSE_FV01_PULSE-8-2011_END-20120719T214600Z_C-20130724T051434Z.nc
            filename = //System.getProperty("user.home")
                            //+ "/"
                            authority 
                            + "_" + facility + "_" 
                            + dataType + "_"
                            + nameFormatter.format(dataStartTime)
                            + "_" + mooring;
                    
            if (table.startsWith("raw"))
            {
                filename        += "_FV00";
            }
            else
            {
                filename        += "_FV01";                
            }
            filename        += "_" + deployment
                            + "_END-"
                            + nameFormatter.format(dataEndTime)
                            + "_C-"               
                            + nameFormatter.format(System.currentTimeMillis());
            
            if (multiPart)
            	filename	+= "_PART01";
            
            filename        += ".nc"
                            ;
        }
        else if (authority.equals("OS"))
        {
            filename = "OS"
                        + "_" + facility
                        + "_" + deployment
                        + "_D"
                        + ".nc"
                        ;
        }

        filename = filename.replaceAll("\\s+", "-"); // replace any spaces with a - character
        
        // hacks to get the next filename in a mulitpart set
        final String filter = authority 
                            + "_" + facility + "_" 
                            + dataType + "_"
                            + "(\\d{8}T\\d{6}Z)"
                            + "_" + mooring
                            + "_FV00"
                            + "_" + deployment
                            + "_END-"
                            + "(\\d{8}T\\d{6}Z)"
                            + "_C-"               
                            + "\\d{8}T\\d{6}Z"
                            + "_(PART\\d+)"
                            + "\\.nc";
        
        final Pattern pattern = Pattern.compile(filter);
        File fl = new File(".");
        File[] files = fl.listFiles(new FilenameFilter()
                        {
                            @Override
                            public boolean accept(File dir, String name)
                            {
//                                System.out.println("name " + name + " " + name.matches(filter));
                                return pattern.matcher(name).matches();
                            }
                        });
        
        int fileno = 1;
        for (File datfile : files)
        {
            filename = datfile.getName();
            System.out.println("Filtered files " + datfile);
            fileno++;
        }        
        Matcher mat = pattern.matcher(filename);
        if (mat.find())
        {
	        filename = filename.replaceAll(mat.group(1), nameFormatter.format(dataStartTime));
	        filename = filename.replaceAll(mat.group(2), nameFormatter.format(dataEndTime));

	        filename = filename.replaceAll(mat.group(3), String.format("PART%02d", fileno));
        }
        
        System.out.println("Next filename " + filename);        
                
        return filename;
    }    
    
    
    public void createFile(String filename) throws IOException
    {
        //dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, filename);
        dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, filename);       
    }

    public void setMooring(Mooring m)
    {
        mooring = m;
        deployment = mooring.getMooringID();
        mooringString = deployment.substring(0, deployment.indexOf("-"));
    }

    public void setAuthority(String a)
    {
        authority = a;
    }

    public void setFacility(String s)
    {
        facility = s;
    }

    public String getMooringName()
    {
        return mooringString;
    }

    public String getDeployment()
    {
        return deployment;
    }

    List <Attribute> groupAttributeList = new Vector<Attribute>();
    
    public void create() throws IOException
    {
    	Comparator comparator = new Comparator<Attribute>() 
    	{
    	    public int compare(Attribute o1, Attribute o2) 
    	    {
    	        return o1.getFullName().toLowerCase().compareTo(o2.getFullName().toLowerCase());
    	    }
    	};
    	
    	Collections.sort(groupAttributeList, comparator);
    	
    	Attribute lastAtt = null;
    	for(Attribute a : groupAttributeList)
    	{
    		//logger.debug("Sorted Attributes "  + a);
    		if (lastAtt == null)
    		{
    			lastAtt = a;
    		}
    		if (lastAtt.getFullName().compareTo(a.getFullName()) != 0)
    		{
        		logger.trace("Add Attributes "  + lastAtt + " = " + lastAtt.getStringValue());
    			dataFile.addGroupAttribute(null, lastAtt);
    			lastAtt = a;
    		}
    		else
    		{
    			if (lastAtt.isString() && a.isString())
    			{
            		logger.trace("Add Attributes "  + lastAtt + " = " + lastAtt.getStringValue() + " duplicate " + a.getStringValue());
	    			if (!lastAtt.getStringValue().contains(a.getStringValue()))
	    			{
	    				lastAtt = new Attribute(lastAtt.getFullName(), lastAtt.getStringValue() + "; " + a.getStringValue());
	    			}
    			}
    		}    		
    	}
		dataFile.addGroupAttribute(null, lastAtt);
    	dataFile.create();
    }
    
    public void addGroupAttribute(Group gp, Attribute at)
    {
    	groupAttributeList.add(at);    	
    }
    
    private void addGlobal(String from, Vector attributeSet)
    {
        if (attributeSet != null && attributeSet.size() > 0)
        {
            for (int i = 0; i < attributeSet.size(); i++)
            {
                Vector row = (Vector) attributeSet.get(i);
                String name = (String) (row.get(0));
                String type = (String) (row.get(1));
                String value = (String) (row.get(2));

                logger.debug(from + " : " + name + " = " + value);

                if (type.startsWith("NUMBER"))
                {
                    addGroupAttribute(null, new Attribute(name.trim(), new Double(value.trim())));
                }
                else
                {
                    addGroupAttribute(null, new Attribute(name.trim(), value.replaceAll("\\\\n", "\n").trim()));
                }
            }
        }
    }

    public void writeGlobalAttributes()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'");
        df.setTimeZone(tz);

        query.setConnection(Common.getConnection());

        String SQL = "SELECT attribute_name, attribute_type, attribute_value FROM netcdf_attributes "
                + " WHERE naming_authority = '*'"
                + " AND facility = '*' AND mooring = '*' AND deployment = '*' AND instrument_id ISNULL AND parameter = '*'"
                + " ORDER BY attribute_name";

        query.executeQuery(SQL);
        Vector attributeSet = query.getData();
        addGlobal("GLOBAL", attributeSet);

        SQL = "SELECT attribute_name, attribute_type, attribute_value FROM netcdf_attributes "
                + " WHERE naming_authority = " + StringUtilities.quoteString(authority)
                + " AND facility = '*' AND mooring = '*' AND deployment = '*' AND instrument_id ISNULL AND parameter = '*'"
                + " ORDER BY attribute_name";

        query.executeQuery(SQL);
        attributeSet = query.getData();
        addGlobal("AUTHORITY", attributeSet);

        SQL = "SELECT attribute_name, attribute_type, attribute_value FROM netcdf_attributes "
                + " WHERE (naming_authority = " + StringUtilities.quoteString(authority) + " OR naming_authority = '*')"
                + " AND facility = " + StringUtilities.quoteString(facility)
                + " AND mooring = '*' AND deployment = '*' AND instrument_id ISNULL AND parameter = '*'"
                + " ORDER BY attribute_name";

        query.executeQuery(SQL);
        attributeSet = query.getData();
        addGlobal("FACILITY", attributeSet);

        SQL = "SELECT attribute_name, attribute_type, attribute_value FROM netcdf_attributes "
                + " WHERE (naming_authority = " + StringUtilities.quoteString(authority) + " OR naming_authority = '*')"
                + " AND (facility = " + StringUtilities.quoteString(facility) + " OR facility = '*')"
                + " AND mooring = " + StringUtilities.quoteString(mooringString)
                + " AND deployment = '*' AND instrument_id ISNULL AND parameter = '*'"
                + " ORDER BY attribute_name";

        query.executeQuery(SQL);
        attributeSet = query.getData();
        addGlobal("MOORING", attributeSet);

        SQL = "SELECT attribute_name, attribute_type, attribute_value FROM netcdf_attributes "
                + " WHERE (naming_authority = " + StringUtilities.quoteString(authority) + " OR naming_authority = '*')"
                + " AND (facility = " + StringUtilities.quoteString(facility) + " OR facility = '*')"
                + " AND (mooring = " + StringUtilities.quoteString(mooringString) + " OR mooring = '*')"
                + " AND deployment = " + StringUtilities.quoteString(deployment)
                + " AND instrument_id ISNULL AND parameter = '*'"
                + " ORDER BY attribute_name";

        query.executeQuery(SQL);
        attributeSet = query.getData();
        addGlobal("DEPLOYMENT", attributeSet);

        if (authority.equals("IMOS"))
        {
            addGroupAttribute(null, new Attribute("date_update", df.format(Calendar.getInstance().getTime())));
        }
        else
        {
            addGroupAttribute(null, new Attribute("date_created", df.format(Calendar.getInstance().getTime())));
        }
    }

    public void writeCoordinateVariableAttributes()
    {
        vTime.addAttribute(new Attribute("name", "time"));
        vTime.addAttribute(new Attribute("standard_name", "time"));
        vTime.addAttribute(new Attribute("long_name", "time of measurement"));
        if (timeIsDoubleDays)
        {
            vTime.addAttribute(new Attribute("units", "days since 1950-01-01T00:00:00 UTC"));
        }
        else
        {
            vTime.addAttribute(new Attribute("units", "hours since 1950-01-01T00:00:00 UTC"));
        }
        vTime.addAttribute(new Attribute("axis", "T"));
        vTime.addAttribute(new Attribute("valid_min", 10957));
        vTime.addAttribute(new Attribute("valid_max", 54787));
        vTime.addAttribute(new Attribute("calendar", "gregorian"));

        //vPos.addAttribute(new Attribute("long_name", "deployment_location_position_index"));

        vStationName.addAttribute(new Attribute("long_name", "instance station name"));
        vStationName.addAttribute(new Attribute("cf_role", "timeseries_id"));
        
        vLat.addAttribute(new Attribute("standard_name", "latitude"));
        vLat.addAttribute(new Attribute("long_name", "latitude of anchor"));
        vLat.addAttribute(new Attribute("units", "degrees_north"));
        vLat.addAttribute(new Attribute("axis", "Y"));
        vLat.addAttribute(new Attribute("valid_min", -90.0));
        vLat.addAttribute(new Attribute("valid_max", 90.0));
        vLat.addAttribute(new Attribute("reference", "WGS84"));
        vLat.addAttribute(new Attribute("coordinate_reference_frame", "urn:ogc:crs:EPSG::4326"));
        vLat.addAttribute(new Attribute("comment", "Anchor Location"));

        vLon.addAttribute(new Attribute("standard_name", "longitude"));
        vLon.addAttribute(new Attribute("long_name", "longitude of anchor"));
        vLon.addAttribute(new Attribute("units", "degrees_east"));
        vLon.addAttribute(new Attribute("axis", "X"));
        vLon.addAttribute(new Attribute("valid_min", -180.0));
        vLon.addAttribute(new Attribute("valid_max", 180.0));
        vLon.addAttribute(new Attribute("reference", "WGS84"));
        vLon.addAttribute(new Attribute("coordinate_reference_frame", "urn:ogc:crs:EPSG::4326"));
        vLon.addAttribute(new Attribute("comment", "Anchor Location"));
    }

    public void writePosition(Double latitudeIn, Double longitudeOut) throws IOException, InvalidRangeException
    {
//        ArrayDouble.D1 lat = new ArrayDouble.D1(1);
//        ArrayDouble.D1 lon = new ArrayDouble.D1(1);
//        ArrayInt.D1 pos = new ArrayInt.D1(1);

        ArrayChar.D1 stationNameData = new ArrayChar.D1(name_strlenDim.getLength());

        ArrayDouble.D0 lat = new ArrayDouble.D0();
        ArrayDouble.D0 lon = new ArrayDouble.D0();

        lat.set(latitudeIn);
        lon.set(longitudeOut);
//        pos.set(0, 1);
        stationNameData.setString(mooringString + "-" + deployment);

        dataFile.write(vStationName, stationNameData);
//        dataFile.write(vPos, pos);
        dataFile.write(vLat, lat);
        dataFile.write(vLon, lon);
    }

    public ucar.ma2.Array times;

    public void createCoordinateVariables(int RECORD_COUNT)
    {
        timeDim = dataFile.addDimension(null, "TIME", RECORD_COUNT);
        name_strlenDim = dataFile.addDimension(null, "name_strlen", 40);
        vStationName = dataFile.addVariable(null, "station_name", DataType.CHAR, "name_strlen");

//        posDim = dataFile.addDimension(null, "POSITION", 1);
//        vPos = dataFile.addVariable(null, "POSITION", DataType.INT, "POSITION");

//        vLat = dataFile.addVariable(null, "LATITUDE", DataType.DOUBLE, "POSITION");
//        vLon = dataFile.addVariable(null, "LONGITUDE", DataType.DOUBLE, "POSITION");
        vLat = dataFile.addVariable(null, "LATITUDE", DataType.DOUBLE, new ArrayList());
        vLon = dataFile.addVariable(null, "LONGITUDE", DataType.DOUBLE, new ArrayList());
    }

    public void writeCoordinateVariables(ArrayList<Timestamp> timeArray)
    {
        if (timeIsDoubleDays)
        {
            times = new ArrayDouble.D1(timeDim.getLength());
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(tz);
            for (int i = 0; i < timeDim.getLength(); i++)
            {
                Timestamp ts = timeArray.get(i);
                long offsetTime = (ts.getTime() - anchorTime) / 1000;
                double elapsedHours = ((double) offsetTime) / (3600 * 24);
                times.setDouble(i, elapsedHours);
            }
        }
        else
        {
            times = new ArrayInt.D1(timeDim.getLength());
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(tz);
            for (int i = 0; i < timeDim.getLength(); i++)
            {
                Timestamp ts = timeArray.get(i);
                long offsetTime = (ts.getTime() - anchorTime) / 1000;
                int elapsedHours = (int) offsetTime / 3600;
                times.setInt(i, elapsedHours);
            }
        }

        ArrayList<Dimension> tdlist = new ArrayList<Dimension>();
        tdlist.add(timeDim);
        if (timeIsDoubleDays)
        {
            vTime = dataFile.addVariable(null, "TIME", DataType.DOUBLE, tdlist);
        }
        else
        {
            vTime = dataFile.addVariable(null, "TIME", DataType.INT, tdlist);
        }
    }

    public class InstanceCoord
    {
        public String params;
        public Double[] depths;
        public Integer[] instruments;

        public Dimension dim;
        public Variable dimVar;
        public ArrayFloat.D1 dimData;
        public ArrayList<Dimension> timeAndDim;
        public String dimensionName;

        public String varName;
        public String varNameQC;
        public ArrayFloat.D2 dataVar;
        public ArrayByte.D2 dataVarQC;
        public Variable var;
        public Variable varQC;
        public boolean useHeight = false;

        public Integer[] source;
        
        public InstanceCoord()
        {
            
        }

        public void createParam(String string)
        {
            params = string;

            varName = new String();
            varNameQC = new String();
        }

        private String getDimensionName()
        {
            return dimensionName;
        }

        private String getDepthsString()
        {
            String ds = "";
            for (int d = 0; d < depths.length; d++)
            {
                ds += depths[d];
                if (d < depths.length - 1)
                {
                    ds += "; ";
                }
            }

            return ds;
        }

        private String getHeightsString()
        {
            String ds = "";
            for (int d = 0; d < depths.length; d++)
            {
                float dh = depths[d].floatValue();
                dh = dh * -1;
                if (Math.abs(dh) < 0.01)
                    dh = 0.0f;
                ds += dh;
                if (d < depths.length - 1)
                {
                    ds += "; ";
                }
            }

            return ds;
        }

        public void createDepths(BigDecimal[] bigDecimal)
        {
            
            int l = bigDecimal.length;
            Double[] dDepths = new Double[l];
            int j = 0;
            float depth = 0;
            for (int d = 0; d < l; d++)
            {
                BigDecimal s = bigDecimal[d];
                dDepths[j++] = s.doubleValue();
                depth += s.floatValue();
            }
            depths = dDepths;

            if (depth <= 0)
                useHeight = true;                        
        }

        public void createVariables()
        {
        }

        protected void addVariableAttributes(InstanceCoord dc)
        {
            ParameterCodes param = ParameterCodes.selectByID(dc.params);
            Variable variable = dc.var;

            String sensor = "";
            String serialNo = "";

            boolean differentSource = false;
            String sourceSensor = "";
            String sourceSerialNo = "";

            for (int i = 0; i < dc.instruments.length; i++)
            {
                Instrument sIns = Instrument.selectByInstrumentID(dc.source[i]);
                Instrument ins = Instrument.selectByInstrumentID(dc.instruments[i]);
                if (ins != null)
                {
                    sensor += ins.getMake() + "-" + ins.getModel();
                    if (i < dc.instruments.length - 1)
                    {
                        sensor += "; ";
                    }
                    serialNo += ins.getSerialNumber();
                    if (i < dc.instruments.length - 1)
                    {
                        serialNo += "; ";
                    }

                    sourceSensor += sIns.getMake() + "-" + sIns.getModel();
                    if (i < dc.instruments.length - 1)
                    {
                        sourceSensor += "; ";
                    }
                    sourceSerialNo += sIns.getSerialNumber();
                    if (i < dc.instruments.length - 1)
                    {
                        sourceSerialNo += "; ";
                    }

                    ArrayList<InstrumentCalibrationValue> values = InstrumentCalibrationValue.selectByInstrumentAndMooring(ins.getInstrumentID(), mooring.getMooringID());

                    for (InstrumentCalibrationValue v : values)
                    {
                        // System.out.println("Calibration Value " + v.getParameterCode() + " " + v.getParameterValue());
                        if (v.getDataType().contains("NUMBER"))
                        {
                            variable.addAttribute(new Attribute("calibration_" + ins.getSerialNumber() + "_" + v.getParameterCode(), Double.parseDouble(v.getParameterValue())));
                        }
                        else
                        {
                            variable.addAttribute(new Attribute("calibration_" + ins.getSerialNumber() + "_" + v.getParameterCode(), v.getParameterValue()));
                        }
                    }
                }
                if ((dc.instruments[i] - dc.source[i]) != 0)
                {
                    logger.info("DIFFERENT SOURCE::INSTRUMENT " + dc.instruments[i] + " " + dc.source[i]);
                    differentSource = true;
                }
            }
            variable.addAttribute(new Attribute("sensor_name", sensor));
            variable.addAttribute(new Attribute("sensor_serial_number", serialNo));
            
            groupAttributeList.add(new Attribute("instrument", sensor));
            groupAttributeList.add(new Attribute("instrument_serial_number", serialNo));
            
            if (differentSource)
            {
                variable.addAttribute(new Attribute("sensor_source_name", sourceSensor));
                variable.addAttribute(new Attribute("sensor_source_serial_number", sourceSerialNo));
            }

            if (param != null)
            {
                variable.addAttribute(new Attribute("name", param.getDescription()));
                if (param.getUnits() != null)
                {
                    variable.addAttribute(new Attribute("units", param.getUnits()));
                }

                if (param.getNetCDFStandardName() != null && !(param.getNetCDFStandardName().trim().isEmpty()))
                {
                    variable.addAttribute(new Attribute("standard_name", param.getNetCDFStandardName()));
                }
                else if (param.getNetCDFStandardName() != null)
                {
                    variable.addAttribute(new Attribute("long_name", param.getNetCDFLongName()));
                }

                if (param.getMinimumValidValue() != null)
                {
                    variable.addAttribute(new Attribute("valid_min", param.getMinimumValidValue().floatValue()));
                }

                if (param.getMaximumValidValue() != null)
                {
                    variable.addAttribute(new Attribute("valid_max", param.getMaximumValidValue().floatValue()));
                }

                variable.addAttribute(new Attribute("_FillValue", Float.NaN));
                //dataFile.addVariableAttribute(variable, "csiro_instrument_id", instrumentID);
            }

            // Select non instrument specific attributes
            String SQL = "SELECT attribute_name, attribute_type, attribute_value FROM netcdf_attributes "
                    + " WHERE (deployment = " + StringUtilities.quoteString(getDeployment()) + " OR deployment = '*')"
                    + " AND (naming_authority = " + StringUtilities.quoteString(authority) + " OR naming_authority = '*')"
                    + " AND instrument_id ISNULL"
                    + " AND parameter = " + StringUtilities.quoteString(dc.varName.trim()) + " ORDER BY attribute_name";

            query.setConnection(Common.getConnection());
            query.executeQuery(SQL);
            Vector attributeSet = query.getData();
            if (attributeSet != null && attributeSet.size() > 0)
            {
                for (int i = 0; i < attributeSet.size(); i++)
                {
                    Vector row = (Vector) attributeSet.get(i);
                    String name = (String) (row.get(0));
                    String type = (String) (row.get(1));
                    String value = (String) (row.get(2));

                    logger.debug("PARAMETER: " + name + " " + value);

                    if (type.startsWith("NUMBER"))
                    {
                        variable.addAttribute(new Attribute(name.trim(), new Double(value.trim())));
                    }
                    else
                    {
                        variable.addAttribute(new Attribute(name.trim(), value.replaceAll("\\\\n", "\n").trim()));
                    }

                }
            }

            String instruments = StringUtils.join(dc.instruments, ",");

            logger.debug("Instruments " + instruments);
            SQL = "SELECT DISTINCT(attribute_name) FROM netcdf_attributes "
                    + " WHERE (deployment = " + StringUtilities.quoteString(getDeployment()) + " OR deployment = '*')"
                    + " AND (instrument_id IS NOT NULL AND instrument_id IN ( " + instruments + ")) "
                    + " AND (parameter = " + StringUtilities.quoteString(dc.varName.trim()) + " OR parameter = '*') ORDER BY attribute_name";

            query.setConnection(Common.getConnection());
            query.executeQuery(SQL);
            Vector attributeName = query.getData();
            String type = null;
            for (int attribN = 0; (attributeName != null) && (attribN < attributeName.size()); attribN++)
            {
                Vector aRow = (Vector) attributeName.get(attribN);
                String name = (String) (aRow.get(0));

                int hasInstrumentAttribute = 0;
                ArrayDouble.D1 values = new ArrayDouble.D1(dc.instruments.length);
                ArrayString.D1 strings = new ArrayString.D1(dc.instruments.length);
                for (int i = 0; i < dc.instruments.length; i++)
                {
                    values.set(i, Double.NaN);
                    strings.set(i, new String(""));
                }
                for (int i = 0; i < dc.instruments.length; i++)
                {
                    SQL = "SELECT attribute_name, attribute_type, attribute_value FROM netcdf_attributes "
                            + " WHERE (deployment = " + StringUtilities.quoteString(getDeployment()) + " OR deployment = '*')"
                            + " AND instrument_id = " + dc.instruments[i]
                            + " AND (parameter = " + StringUtilities.quoteString(dc.varName.trim()) + " OR parameter = '*') ORDER BY attribute_name";

                    query.setConnection(Common.getConnection());
                    query.executeQuery(SQL);
                    attributeSet = query.getData();
                    if (attributeSet != null && attributeSet.size() > 0)
                    {
                        for (int j = 0; j < attributeSet.size(); j++)
                        {
                            Vector row = (Vector) attributeSet.get(j);
                            name = (String) (row.get(0));
                            type = (String) (row.get(1));
                            String value = (String) (row.get(2));

                            logger.debug("INSTRUMENT PARAMETER: " + name + " " + value);

                            hasInstrumentAttribute++;

                            if (type.startsWith("NUMBER"))
                            {
                                values.set(i, new Double(value.trim()));
                            }
                            else
                            {
                                strings.set(i, value.replaceAll("\\\\n", "\n").trim());
                            }

                        }
                    }

                }
                if (hasInstrumentAttribute > 0)
                {
                	if (type.startsWith("NUMBER"))                		
                		variable.addAttribute(new Attribute(name.trim(), values));
                	else
                	{
                		String aggString = "";
                		for(int i=0;i<strings.getSize();i++)
                		{
                			aggString += strings.get(i);
                			if ((i+1) < strings.getSize())
                				aggString += ";";
                		}
                		variable.addAttribute(new Attribute(name.trim(), aggString));
                	}
                		
                }
            }
        }

        public String createParams(int RECORD_COUNT)
        {
            String pt = params.trim();
            ArrayFloat.D2 dataTemp;
            ArrayByte.D2 dataTempQC;
            if (fileOrderTimeDepth)
            {
            	dataTemp = new ArrayFloat.D2(RECORD_COUNT, depths.length);
            	dataTempQC = new ArrayByte.D2(RECORD_COUNT, depths.length);            	
            }
            else
            {
            	dataTemp = new ArrayFloat.D2(depths.length, RECORD_COUNT);
            	dataTempQC = new ArrayByte.D2(depths.length, RECORD_COUNT);
            }
            byte b = 9; // missing value
            for (int i = 0; i < RECORD_COUNT; i++)
            {
                for (int j = 0; j < depths.length; j++)
                {
                	if (fileOrderTimeDepth)
                	{
                		dataTemp.set(i, j, Float.NaN);
                		dataTempQC.set(i, j, b);
                	}
                	else
                	{
                		dataTemp.set(j, i, Float.NaN);
                		dataTempQC.set(j, i, b);
                	}
                }
            }
            varName = pt;
            dataVar = dataTemp;
            dataVarQC = dataTempQC;
            var = dataFile.addVariable(null, pt, DataType.FLOAT, timeAndDim);
            
            if (useHeight)
            {
                var.addAttribute(new Attribute("sensor_height", getHeightsString()));
            }
            else
            {
                var.addAttribute(new Attribute("sensor_depth", getDepthsString()));
            }
            addVariableAttributes(this);

            String qc = pt + "_QC";
            var.addAttribute(new Attribute("ancillary_variables", qc));
            var.addAttribute(new Attribute("coordinates", "TIME " + getDimensionName() + " LATITUDE LONGITUDE"));

            varNameQC = qc;
            varQC = dataFile.addVariable(null, qc, DataType.BYTE, timeAndDim);
            varQC.addAttribute(new Attribute("long_name", "quality flag for " + varName));
            if (authority.equals("IMOS"))
            {
                varQC.addAttribute(new Attribute("quality_control_conventions", "IMOS standard set using the IODE flags"));
            }
            else
            {
                varQC.addAttribute(new Attribute("conventions", "OceanSITES reference table 2"));
            }

            varQC.addAttribute(new Attribute("quality_control_set", (double) 1.0));

            b = -128;
            varQC.addAttribute(new Attribute("_FillValue", b));
            b = 0;
            varQC.addAttribute(new Attribute("valid_min", b));
            b = 9;
            varQC.addAttribute(new Attribute("valid_max", b));

            ArrayByte.D1 qcValues = new ArrayByte.D1(4);
            b = 0;
            qcValues.set(0, b);
            b = 1;
            qcValues.set(1, b);
            b = 4;
            qcValues.set(2, b);
            b = 9;
            qcValues.set(3, b);
            varQC.addAttribute(new Attribute("flag_values", qcValues));
            varQC.addAttribute(new Attribute("flag_meanings", "unknown good_data bad_data missing_value"));

            return pt;
        }

        private void createDimension()
        {
        }

    }
}
