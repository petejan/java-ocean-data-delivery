package org.imos.abos.netcdf;

// make a single parameter netcdf file for all deployments

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.dbms.ParameterCodes;
import org.wiley.core.Common;
import org.wiley.util.SQLWrapper;
import org.wiley.util.StringUtilities;

import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

public class NetCDFparamDSG
{
    private static Logger log = Logger.getLogger(NetCDFparamDSG.class);
	protected static SQLWrapper query = new SQLWrapper();
    
    //Calendar cal = null;
    
    public NetCDFparamDSG()
    {
        final TimeZone tz = TimeZone.getTimeZone("UTC");

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat netcdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        
        netcdfDate.setTimeZone(tz);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		anchorTime = 0;
		try
		{
			java.util.Date ts = df.parse("1950-01-01 00:00:00");
			anchorTime = ts.getTime();
		}
		catch (ParseException pex)
		{
			log.error(pex);
		}
		
//		cal = Calendar.getInstance();
//		cal.setTimeZone(tz);

    }

	public static void main(String[] args)
	{
        PropertyConfigurator.configure("log4j.properties");
        Common.build("ABOS.properties");

		NetCDFparamDSG dsg = new NetCDFparamDSG();
		
		String parameter = "PAR";
		if (args.length >= 1)
			parameter = args[0];

		log.info("Parameter : " + parameter);
		try
		{
			dsg.createFile(parameter);
			dsg.loadData(parameter);
		}
		catch (IOException e)
		{
			log.warn(e);
		}
	}

	Variable vTime = null;
	Variable vLat = null;
	Variable vLon = null;
	ArrayList<Dimension> obsDims = null;
	ArrayList<Dimension> stationDims = null;
	Variable vStationIndex = null;
	Variable vDepth = null;
	NetCDFfile ndf = null;
	
    HashMap <String, StationInstance> stationMap = new HashMap<String, StationInstance>();

    private void createFile(String param) throws IOException
	{
        Connection conn = null;
        Statement proc = null;
        ResultSet results = null;
        
        Timestamp dataStartTime = new Timestamp(Long.MAX_VALUE);
        Timestamp dataEndTime = new Timestamp(Long.MIN_VALUE);
        
        String select;
        conn = Common.getConnection();
		try
		{
			proc = conn.createStatement();

			select = "select distinct instrument_id, mooring_id, make, model, serial_number, mooring_attached_instruments.depth, latitude_in, longitude_in, timestamp_in, timestamp_out from raw_instrument_data "
				   + " join instrument using (instrument_id) "
				   + " join mooring using (mooring_id) "
				   + " join mooring_attached_instruments using (mooring_id, instrument_id) "
				   + " where parameter_code = '"+param+"' order by timestamp_in, mooring_id";
	
			log.debug(select);
	    	
	        proc.execute(select);
	        results = (ResultSet) proc.getResultSet();
	
	        int stnN = 0;
	
	        while (results.next())
	        {
	        		String make = results.getString("make");
	        		String model = results.getString("model");
	        		String serial = results.getString("serial_number");
	        		String mooring = results.getString("mooring_id");
	        		Timestamp ts_in = results.getTimestamp("timestamp_in");
	        		Timestamp ts_out = results.getTimestamp("timestamp_out");
	        		
	        		if (ts_in.before(dataStartTime) )
	        			dataStartTime = ts_in;
	        		if (ts_out.after(dataEndTime) )
	        			dataEndTime = ts_out;
	        		
	        		double depth = results.getDouble("depth");
	        		double lat = results.getDouble("latitude_in");
	        		double lon = results.getDouble("longitude_in");
	        		int inst = results.getInt("instrument_id");
	
	        		String stationId = String.format("%s-%d", mooring, inst);
	
		    		StationInstance id = new StationInstance(stnN, mooring.trim(), depth, inst, lat, lon, make.trim(), model.trim(), serial.trim());
	            log.debug(" instrument " + id);  
	            id.setTimestampStart(ts_in);
	            id.setTimestampEnd(ts_out);

	            stationMap.put(stationId, id);
	    		
		    		stnN ++;
	        }

		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        Mooring m = Mooring.selectByMooringID("SOFS-1-2010");

		ndf = new NetCDFfile();
        ndf.setMooring(m);
        ndf.setAuthority("IMOS");
        ndf.setFacility("ABOS-SOTS");
        
        String filename = ndf.getFileName(null, dataStartTime, dataEndTime, "raw", "F", param.toUpperCase()+"-DiscreteGeometries");
        ndf.createFile(filename);
        ndf.writeGlobalAttributes();
        ndf.addGroupAttribute(null, new Attribute("featureType", "timeSeries"));

		Dimension obsdim = ndf.dataFile.addUnlimitedDimension("obs");
		
		obsDims = new ArrayList<Dimension>();
		obsDims.add(obsdim);
		
		vTime = ndf.dataFile.addVariable(null, "TIME", DataType.DOUBLE, obsDims);
		
		vTime.addAttribute(new Attribute("name", "time"));
		vTime.addAttribute(new Attribute("standard_name", "time"));
		vTime.addAttribute(new Attribute("long_name", "time of measurement"));
		vTime.addAttribute(new Attribute("units", "days since 1950-01-01T00:00:00 UTC"));
		vTime.addAttribute(new Attribute("axis", "T"));
		vTime.addAttribute(new Attribute("valid_min", 10957.0));
		vTime.addAttribute(new Attribute("valid_max", 54787.0));
		vTime.addAttribute(new Attribute("calendar", "gregorian"));
		
		vStationIndex = ndf.dataFile.addVariable(null, "stationIndex", DataType.INT, obsDims);
		vStationIndex.addAttribute(new Attribute("long_name", "which station this obs is for")); 
		vStationIndex.addAttribute(new Attribute("instance_dimension", "station")); 
	}
	
	long anchorTime;
	
	class StationInstance
	{
		@Override
		public String toString()
		{
			return "StationInstance [id=" + id + ", mooring=" + mooring + ", depth=" + depth + ", instrument_id=" + instrument_id + "]";
		}

		int id;
		String mooring;
		double depth;
		int instrument_id;
		double lat;
		double lon;
		String make;
		String model;
		String serial;
		Timestamp start = null;
		Timestamp end = null;
		
		public StationInstance(int id, String mooring, double depth, int instrument_id, double lat, double lon, String make, String model, String serial)
		{
			this.id = id;
			this.mooring = mooring;
			this.depth = depth;
			this.instrument_id = instrument_id;
			this.lat = lat;
			this.lon = lon;
			this.make = make;
			this.model = model;
			this.serial = serial;
		}
		public void setTimestampStart(Timestamp s)
		{
			start = s;
		}
		public void setTimestampEnd(Timestamp e)
		{
			end = e;
		}
	}
	
	private void loadData(String parameter)
	{
		ArrayDouble.D1 timeData = new ArrayDouble.D1(1);
		ArrayFloat.D1 obsData = new ArrayFloat.D1(1);
		ArrayByte.D1 qcData = new ArrayByte.D1(1);
		int[] obs_origin = new int[] {0};

		ArrayInt.D1 stationData = new ArrayInt.D1(1);
		
        Connection conn = null;
        Statement proc = null;
        ResultSet results = null;
        
		HashMap<String, Integer> qualityFlag = new HashMap<String, Integer>();
		qualityFlag.put("RAW", 1);
		qualityFlag.put("DERIVED", 1);
		qualityFlag.put("EXTRACTED", 1);
		qualityFlag.put("AVG", 1);
		qualityFlag.put("INTERPOLATED", 1);
		qualityFlag.put("GOOD", 1);
		qualityFlag.put("PGOOD", 2);
		qualityFlag.put("PBAD", 3);
		qualityFlag.put("BAD", 4);
		qualityFlag.put("OOR", 4);
		qualityFlag.put("OUT", 5);
		qualityFlag.put("MISSING", 9);


        String select;
        conn = Common.getConnection();
        try
        {
    	        int stnN = stationMap.size();
    	        
    			Dimension stationdim = ndf.dataFile.addDimension(null, "station", stnN);
    			stationDims = new ArrayList<Dimension>();
    			stationDims.add(stationdim);

    			vLat = ndf.dataFile.addVariable(null, "LATITUDE", DataType.DOUBLE, stationDims);

    			vLat.addAttribute(new Attribute("standard_name", "latitude"));
    			vLat.addAttribute(new Attribute("long_name", "latitude of anchor"));
    			vLat.addAttribute(new Attribute("units", "degrees_north"));
    			vLat.addAttribute(new Attribute("axis", "Y"));
    			vLat.addAttribute(new Attribute("valid_min", -90.0));
    			vLat.addAttribute(new Attribute("valid_max", 90.0));
    			vLat.addAttribute(new Attribute("reference_datum", "WGS84 coordinate reference system"));
    			vLat.addAttribute(new Attribute("coordinate_reference_frame", "urn:ogc:crs:EPSG::4326"));
    			vLat.addAttribute(new Attribute("comment", "Anchor Location"));
    			ArrayDouble.D1 dLat = new ArrayDouble.D1(stnN); 

    			vLon = ndf.dataFile.addVariable(null, "LONGITUDE", DataType.DOUBLE, stationDims);

    			vLon.addAttribute(new Attribute("standard_name", "longitude"));
    			vLon.addAttribute(new Attribute("long_name", "longitude of anchor"));
    			vLon.addAttribute(new Attribute("units", "degrees_east"));
    			vLon.addAttribute(new Attribute("axis", "X"));
    			vLon.addAttribute(new Attribute("valid_min", -180.0));
    			vLon.addAttribute(new Attribute("valid_max", 180.0));
    			vLon.addAttribute(new Attribute("reference_datum", "WGS84 coordinate reference system"));        
    			vLon.addAttribute(new Attribute("coordinate_reference_frame", "urn:ogc:crs:EPSG::4326"));
    			vLon.addAttribute(new Attribute("comment", "Anchor Location"));
    			ArrayDouble.D1 dLon = new ArrayDouble.D1(stnN); 

    			Dimension instrumentNamedim = ndf.dataFile.addDimension(null, "nameinst", 80);
    			ArrayList<Dimension> instNameDims = new ArrayList<Dimension>();
    			instNameDims.add(stationdim);
    			instNameDims.add(instrumentNamedim);
    			Variable vInstName = ndf.dataFile.addVariable(null, "station_name", DataType.CHAR, instNameDims);
    			vInstName.addAttribute(new Attribute("long_name","deployment : instrument"));
    			
    	        ArrayChar.D2 dInstName = new ArrayChar.D2(stnN, 80); 

    			vDepth = ndf.dataFile.addVariable(null, "NOMINAL_DEPTH", DataType.DOUBLE, stationDims);
    			ArrayDouble.D1 dDepth = new ArrayDouble.D1(stnN); 
    			vDepth.addAttribute(new Attribute("units", "meters"));
    			vDepth.addAttribute(new Attribute("standard_name", "depth"));
    			vDepth.addAttribute(new Attribute("long_name", "nominal depth of each sensor"));
    			vDepth.addAttribute(new Attribute("positive", "down"));
    			vDepth.addAttribute(new Attribute("axis", "Z"));
    			vDepth.addAttribute(new Attribute("valid_min", 0.0));
    			vDepth.addAttribute(new Attribute("valid_max", 12000.0));
    			vDepth.addAttribute(new Attribute("reference_datum", "Mean Sea Level (MSL)"));

    	        Variable vInst = ndf.dataFile.addVariable(null, "INSTRUMENT_ID", DataType.INT, stationDims);
    	        ArrayInt.D1 dInst = new ArrayInt.D1(stnN); 
    	        vInst.addAttribute(new Attribute("long_name", "internal instrument unique-identifier"));
    	        
    			// create a station instance for each station
    	        Collection<StationInstance> s = stationMap.values();
    	        String mooring_list = null;
    	        for (StationInstance st : s)
    	        {
    	        		dLat.set(st.id, st.lat);
    	        		dLon.set(st.id, st.lon);
    	        		dDepth.set(st.id, st.depth);
    	        		dInst.set(st.id, st.instrument_id);
    	        		if (mooring_list == null)
    	        			mooring_list = "'" + st.mooring + "'";
    	        		else
    	        			mooring_list += ",'" + st.mooring + "'";
    	        		
    	        		dInstName.setString(st.id, st.mooring + ":" + st.make + ":" + st.model + ":" + st.serial);
    	        }

    	        // create a variable for each parameter
    	        
	        select = "SELECT code AS parameter_code FROM parameters WHERE code in ('"+parameter+"')"; 

	        log.debug(select);
	        proc = conn.createStatement();
	        proc.execute(select);
	        results = (ResultSet) proc.getResultSet();
	
	        HashMap<String, Variable> params = new HashMap<String, Variable>();
	        HashMap<String, Variable> paramsQC = new HashMap<String, Variable>();
			
	        while (results.next())
	        {
	        		String p = results.getString("parameter_code").trim();
	            
	            log.debug(" param " + p);
	            
	            ParameterCodes param = ParameterCodes.selectByID(p);
	            
	            Variable vParam = ndf.dataFile.addVariable(null, p, DataType.FLOAT, obsDims);
	            Variable vParamQC = ndf.dataFile.addVariable(null, p + "_quality_code", DataType.BYTE, obsDims);

	            params.put(p, vParam);
	            paramsQC.put(p, vParamQC);
	            
	            // add attributes to variables
				if (param != null)
				{
					vParam.addAttribute(new Attribute("name", param.getDescription()));
					if (param.getUnits() != null)
					{
						vParam.addAttribute(new Attribute("units", param.getUnits()));
					}

					if (param.getNetCDFStandardName() != null && !(param.getNetCDFStandardName().trim().isEmpty()))
					{
						vParam.addAttribute(new Attribute("standard_name", param.getNetCDFStandardName()));
						//dc.stdNameSF = new Attribute("standard_name", param.getNetCDFStandardName() + " status_flag");
					}
					if (param.getNetCDFLongName() != null)
					{
						vParam.addAttribute(new Attribute("long_name", param.getNetCDFLongName()));
					}

					if (param.getMinimumValidValue() != null)
					{
						vParam.addAttribute(new Attribute("valid_min", param.getMinimumValidValue().floatValue()));
					}

					if (param.getMaximumValidValue() != null)
					{
						vParam.addAttribute(new Attribute("valid_max", param.getMaximumValidValue().floatValue()));
					}

					vParam.addAttribute(new Attribute("_FillValue", Float.NaN));
					
					vParamQC.addAttribute(new Attribute("long_name", "quality flag for " + p));

					vParamQC.addAttribute(new Attribute("quality_control_conventions", "IMOS standard flags"));

					vParamQC.addAttribute(new Attribute("quality_control_set", (double) 1.0));

					byte b = -128;
					vParamQC.addAttribute(new Attribute("_FillValue", b));
					b = 0;
					vParamQC.addAttribute(new Attribute("valid_min", b));
					b = 9;
					vParamQC.addAttribute(new Attribute("valid_max", b));

					ArrayByte.D1 qcValues = new ArrayByte.D1(7);
					b = 0;
					qcValues.set(0, b);
					b = 1;
					qcValues.set(1, b);
					b = 2;
					qcValues.set(2, b);
					b = 3;
					qcValues.set(3, b);
					b = 4;
					qcValues.set(4, b);
					b = 5;
					qcValues.set(5, b);
					b = 9;
					qcValues.set(6, b);
					vParamQC.addAttribute(new Attribute("flag_values", qcValues));
					vParamQC.addAttribute(new Attribute("flag_meanings", "unknown good_data probably_good_data probably_bad_data bad_data not_deployed missing_value"));
					
				}

				// Select non instrument specific attributes
				String SQL = "SELECT attribute_name, attribute_type, attribute_value FROM netcdf_attributes "
						+ " WHERE (deployment IN (" + mooring_list + ") OR deployment = '*')"
						+ " AND (naming_authority = " + StringUtilities.quoteString("IMOS") + " OR naming_authority = '*')"
						+ " AND instrument_id IS NULL"
						+ " AND parameter = " + StringUtilities.quoteString(p.trim()) + " ORDER BY attribute_name";

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

						log.debug("PARAMETER: " + name + " " + value);

						if (type.startsWith("NUMBER"))
						{
							vParam.addAttribute(new Attribute(name.trim(), new Double(value.trim())));
						}
						else
						{
							vParam.addAttribute(new Attribute(name.trim(), value.replaceAll("\\\\n", "\n").trim()));
						}

					}
				}
	        }
	        
	        results.close();
	        
	        ndf.dataFile.deleteGroupAttribute(null, "deployment_code");
	        	        
	        ndf.create();
	        
			ndf.dataFile.write(vLat, dLat);
			ndf.dataFile.write(vLon, dLon);
			ndf.dataFile.write(vDepth, dDepth);
			ndf.dataFile.write(vInst, dInst);
			ndf.dataFile.write(vInstName, dInstName);
	        
	        select = "SELECT data_timestamp, instrument_id, depth, mooring_id, parameter_code, parameter_value, quality_code"
	        			+ " FROM raw_instrument_data WHERE mooring_id in (" + mooring_list + ") AND parameter_code = '"+parameter+"'" 
	        			+ " ORDER BY data_timestamp, depth, mooring_id";
	
	        log.debug(select);
	
	        conn.setAutoCommit(false);
	        proc.execute(select);
	        proc.setFetchSize(50);
	        results = (ResultSet) proc.getResultSet();
	
	        conn.setAutoCommit(false);
	        results.setFetchSize(50);
	        
	        Timestamp ts;
	        Integer inst = null;
	        Double d = null;
	        String mooring = null;

	        String param = null;
	        Double value = null;
	        String quality = null;

	        int obs = 0;
	        Timestamp tsStart = null;
	        Timestamp tsEnd = null;
	        
	        // add the data into variables
	        while (results.next())
	        {
	            ts = results.getTimestamp("data_timestamp");
	            inst = results.getInt("instrument_id");
	            d = results.getDouble("depth");
	            mooring = results.getString("mooring_id");
	            
	            if (tsStart == null)
	            		tsStart = ts;
	            tsEnd = ts;
	            
	            String stationId = String.format("%s-%d", mooring, inst);
	            StationInstance id = stationMap.get(stationId);

	            param = results.getString("parameter_code").trim();
	            value = results.getDouble("parameter_value");
	            quality = results.getString("quality_code");
	            
	            //log.debug(id + " obs " + obs + " ts " + ts + " depth " + d + " param " + param + " value " + value + " quality " + quality);
	            
	            obs_origin[0] = obs;	            

	            // Time data
				long offsetTime = (ts.getTime() - anchorTime) / 1000;
				double elapsedDays = ((double) offsetTime) / (3600 * 24);
				
				timeData.set(0, elapsedDays);
	            ndf.dataFile.write(vTime, obs_origin, timeData);
	            
	            // station instance
	            stationData.set(0, id.id);
	            ndf.dataFile.write(vStationIndex, obs_origin, stationData);
	            
	            // Parameter data
            		Variable var = params.get(param);
            		if (var != null)
            		{
	            		obsData.set(0,  value.floatValue());
		            		
	    	            ndf.dataFile.write(var, obs_origin, obsData);
	
	            		Variable varQC = paramsQC.get(param);
	            		Integer qf = qualityFlag.get(quality.trim());
	            		if (ts.before(id.start))
	            			qf = 5;
	            		else if (ts.after(id.end))
	            			qf = 5;
	            		
	    	            qcData.set(0,  (byte)qf.byteValue());
	            		
	    	            ndf.dataFile.write(varQC, obs_origin, qcData);
		            
		            obs++;
            		}
            		else
            			log.warn("no variable for parameter " + param);
	        }
	        
	        ndf.dataFile.updateAttribute(null, new Attribute("time_coverage_start", ndf.netcdfDate.format(tsStart)));
	        ndf.dataFile.updateAttribute(null, new Attribute("time_coverage_end", ndf.netcdfDate.format(tsEnd)));
	        ndf.dataFile.updateAttribute(null, new Attribute("title", "SOTS "+parameter+" Mooring Data"));
	        
	        // close things
	        
	        results.close();
	        
	        conn.close();
	        
	        ndf.dataFile.close();
        }
		catch (SQLException e)
		{
			e.printStackTrace();
			log.warn(e);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			log.warn(e);
		}
		catch (InvalidRangeException e)
		{
			e.printStackTrace();
			log.warn(e);
		}
		
	}

}
