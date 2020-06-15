package org.imos.dwm.netcdf;

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
import org.imos.dwm.dbms.Mooring;
import org.imos.dwm.dbms.ParameterCodes;
import org.wiley.core.Common;
import org.wiley.util.SQLWrapper;
import org.wiley.util.StringUtilities;

import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

public class NetCDFdsg
{
    private static Logger log = Logger.getLogger(NetCDFdsg.class);
	protected static SQLWrapper query = new SQLWrapper();
    
    Calendar cal = null;
    
    public NetCDFdsg()
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
		
		cal = Calendar.getInstance();
		cal.setTimeZone(tz);

    }

	public static void main(String[] args)
	{
        PropertyConfigurator.configure("log4j.properties");
        Common.build("ABOS.properties");

		NetCDFdsg dsg = new NetCDFdsg();
		
		String parameter = "TEMP";

		try
		{
			dsg.createFile();
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
	
	private void createFile() throws IOException
	{
        Mooring m = Mooring.selectByMooringID("SAZ47-15-2012");

		ndf = new NetCDFfile();
        ndf.setMooring(m);
        ndf.setAuthority("IMOS");
        ndf.setFacility(m.getFacility());
        
        Timestamp dataStartTime = m.getTimestampIn(); // TODO: probably should come from data, esp for part files
        Timestamp dataEndTime = m.getTimestampOut();  

        String filename = ndf.getFileName(null, dataStartTime, dataEndTime, "raw", "KRSF", "SedimentTrap-DiscreteGeometries");
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
	}
	
	private void loadData(String parameter)
	{
		ArrayDouble.D1 timeData = new ArrayDouble.D1(1);
		ArrayFloat.D1 obsData = new ArrayFloat.D1(1);
		int[] obs_origin = new int[] {0};

		ArrayInt.D1 stationData = new ArrayInt.D1(1);
		
        Connection conn = null;
        Statement proc = null;
        ResultSet results = null;

        String select;
        conn = Common.getConnection();
        try
        {
        		proc = conn.createStatement();

        		select = "select instrument_id, make, model, serial_number, depth, mooring_id, lat, lon from instrument join (select mooring_id, instrument_id, min(depth) AS depth, min(latitude) AS lat, min(longitude) as lon from raw_instrument_data where mooring_id in ('SAZ47-15-2012', 'SAZ47-16-2013') group by mooring_id, instrument_id) AS a using ( instrument_id) ORDER BY mooring_id, depth";

        		log.debug(select);
    	    	
    	        proc.execute(select);
    	        results = (ResultSet) proc.getResultSet();
    	
    	        HashMap <String, StationInstance> stationMap = new HashMap<String, StationInstance>();
    	        int stnN = 0;
    	
    	        while (results.next())
    	        {
    	        		String make = results.getString("make");
    	        		String model = results.getString("model");
    	        		String serial = results.getString("serial_number");
    	        		String mooring = results.getString("mooring_id");
    	        		double depth = results.getDouble("depth");
    	        		double lat = results.getDouble("lat");
    	        		double lon = results.getDouble("lon");
    	        		int inst = results.getInt("instrument_id");

    	        		String stationId = String.format("%s-%d", mooring, inst);

    	            log.debug(" instrument " + inst);  
            		StationInstance id = new StationInstance(stnN, mooring.trim(), depth, inst, lat, lon, make.trim(), model.trim(), serial.trim());
            		stationMap.put(stationId, id);
            		
            		stnN ++;
    	        }
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
    	        for (StationInstance st : s)
    	        {
    	        		dLat.set(st.id, st.lat);
    	        		dLon.set(st.id, st.lon);
    	        		dDepth.set(st.id, st.depth);
    	        		dInst.set(st.id, st.instrument_id);
    	        		dInstName.setString(st.id, st.mooring + ":" + st.make + ":" + st.model + ":" + st.serial);
    	        }
    	        results.close();

    	        // create a variable for each parameter
    	        
	        select = "SELECT distinct(cast(parameter_code AS varchar)) "
        		 	   + " FROM raw_instrument_data WHERE mooring_id in ('SAZ47-15-2012', 'SAZ47-16-2013') "
        			   + " ORDER BY parameter_code";

	        log.debug(select);
	
	        proc.execute(select);
	        results = (ResultSet) proc.getResultSet();
	
	        HashMap<String, Variable> params = new HashMap<String, Variable>();
			
	        while (results.next())
	        {
	        		String p = results.getString("parameter_code");
	            
	            log.debug(" param " + p);
	            
	            ParameterCodes param = ParameterCodes.selectByID(p);
	            
	            Variable vParam = ndf.dataFile.addVariable(null, p, DataType.FLOAT, obsDims);

	            params.put(p, vParam);
	            
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
				}

				// Select non instrument specific attributes
				String SQL = "SELECT attribute_name, attribute_type, attribute_value FROM netcdf_attributes "
						+ " WHERE (deployment = " + StringUtilities.quoteString("SAZ47-15-2012") + " OR deployment = '*')"
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
	        
	        select = "SELECT data_timestamp, depth, mooring_id, instrument_id, array_agg(cast(parameter_code AS varchar)) AS code, array_agg(cast(parameter_value AS float)) AS value, array_agg(cast(quality_code AS varchar)) AS quality"
	        			+ " FROM raw_instrument_data WHERE mooring_id in ('SAZ47-15-2012', 'SAZ47-16-2013') "
	        			+ " GROUP BY data_timestamp, depth, instrument_id, mooring_id "
	        			+ " ORDER BY data_timestamp, depth, mooring_id";
	
	        log.debug(select);
	
	        proc.execute(select);
	        results = (ResultSet) proc.getResultSet();
	
	        conn.setAutoCommit(false);
	        results.setFetchSize(50);
	        
	        Timestamp ts;
	        Integer inst = null;
	        Double d = null;
	        String mooring = null;

	        String[] param = null;
	        Double[] value = null;
	        String[] quality = null;

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

	            param = (String [])results.getArray("code").getArray();
	            value = (Double[]) results.getArray("value").getArray();
	            quality = (String [])results.getArray("quality").getArray();
	            
	            log.debug(id + " obs " + obs + " ts " + ts + " depth " + d + " param " + param[0] + " value " + value[0] + " quality " + quality[0]);
	            
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
	            for(int i=0;i<param.length;i++)
	            {
	            		Variable var = params.get(param[i]);
	            		obsData.set(0,  value[i].floatValue());
	            		
	    	            ndf.dataFile.write(var, obs_origin, obsData);
	            }
	            
	            obs++;
	        }
	        
	        ndf.dataFile.updateAttribute(null, new Attribute("time_coverage_start", ndf.netcdfDate.format(tsStart)));
	        ndf.dataFile.updateAttribute(null, new Attribute("time_coverage_end", ndf.netcdfDate.format(tsEnd)));
	        ndf.dataFile.updateAttribute(null, new Attribute("title", "SAZ Mooring Data"));
	        
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
