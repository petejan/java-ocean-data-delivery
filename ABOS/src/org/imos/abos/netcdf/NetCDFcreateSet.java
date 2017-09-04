package org.imos.abos.netcdf;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentDataFile;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.forms.NetCDFcreateForm;
import org.imos.abos.netcdf.NetCDFfile.InstanceCoord;
import org.wiley.core.Common;
import org.wiley.util.StringUtilities;

public class NetCDFcreateSet extends NetCDFcreateForm
{
    private static Logger logger = Logger.getLogger(NetCDFcreateSet.class.getName());
	
	int source_file;
	
	public NetCDFcreateSet()
	{
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        netcdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        
        netcdfDate.setTimeZone(tz);
  	}
	
    protected void createDepthArray(String mooringID)
    {
        logger.debug("dataStart " + dataStartTime + " dataEnd " + dataEndTime);
        if (dataStartTime == null)
        {
        	logger.info("no data exiting");
        	
        	return;
        }
        
        // Based on soucefile_id
        
//        String SQL = "SELECT parameter_code, imos_data_code, array_agg(instrument_id) AS instruments, array_agg(source) AS source, array_agg(depth) AS depths FROM "       
//        			+ " (SELECT CAST(parameter_code AS varchar(20)), imos_data_code , d.mooring_id, d.instrument_id, s.instrument_id AS source, CAST(att.depth AS numeric(8,3)) AS depth" 
//        			+ "  FROM  " + table + " AS d JOIN instrument_data_files AS s ON (source_file_id = datafile_pk)"                         
//        			+ "  JOIN instrument ON (d.instrument_id = instrument.instrument_id)"                         
//        			+ "  JOIN parameters ON (d.parameter_code = parameters.code)"
//        			+ "  JOIN mooring_attached_instruments as att ON (d.mooring_id = att.mooring_id AND d.instrument_id = att.instrument_id)" 
//        			+ " WHERE d.mooring_id = " + StringUtilities.quoteString(selectedMooring.getMooringID())
//        			+ "       AND d.quality_code not in ('INTERPOLATED', 'PBAD', 'BAD', 'PGOOD')"
//        			+ "       AND datafile_pk = " + sourceFile.getDataFilePrimaryKey() 
//                    + "       AND data_timestamp BETWEEN " + StringUtilities.quoteString(Common.getRawSQLTimestamp(dataStartTime)) + " AND " + StringUtilities.quoteString(Common.getRawSQLTimestamp(dataEndTime))
//        			+ "   GROUP BY parameter_code, imos_data_code, d.mooring_id, d.instrument_id, s.instrument_id, att.depth, make, model, serial_number" 
//        			+ "   ORDER BY 1, 2, depth, make, model, serial_number"       
//        			+ " ) AS a    GROUP BY parameter_code, imos_data_code ORDER BY depths, parameter_code";

        String SQL = "SELECT CAST(parameter_code AS varchar(20)), imos_data_code , ARRAY[d.instrument_id] AS instrument_id, ARRAY[s.instrument_id] AS source, ARRAY[CAST(att.depth AS numeric(8,3))] AS depth" 
    			+ "  FROM  " + table + " AS d JOIN instrument_data_files AS s ON (source_file_id = datafile_pk)"                         
    			+ "  JOIN instrument ON (d.instrument_id = instrument.instrument_id)"                         
    			+ "  JOIN parameters ON (d.parameter_code = parameters.code)"
    			+ "  JOIN mooring_attached_instruments as att ON (d.mooring_id = att.mooring_id AND d.instrument_id = att.instrument_id)" 
    			+ " WHERE d.mooring_id = " + StringUtilities.quoteString(selectedMooring.getMooringID())
    			+ "       AND d.quality_code not in ('INTERPOLATED', 'PBAD', 'BAD', 'PGOOD')"
    			+ "       AND datafile_pk = " + sourceFile.getDataFilePrimaryKey() 
                + "       AND data_timestamp BETWEEN " + StringUtilities.quoteString(Common.getRawSQLTimestamp(dataStartTime)) + " AND " + StringUtilities.quoteString(Common.getRawSQLTimestamp(dataEndTime))
    			+ "   GROUP BY parameter_code, imos_data_code, d.mooring_id, d.instrument_id, s.instrument_id, att.depth, make, model, serial_number" 
    			+ "   ORDER BY 1, 2, depth, make, model, serial_number"; 
        
        instanceCoords = new ArrayList<InstanceCoord>();
        logger.debug(SQL);
        query.setConnection(Common.getConnection());
        query.executeQuery(SQL);
        Vector depthSet = query.getData();
        
        if (depthSet != null && depthSet.size() > 0)
        {
            for (int i = 0; i < depthSet.size(); i++)
            {
                Vector row = (Vector) depthSet.get(i);
                String param = (String)row.get(0);
                String imos_data_code = (String)row.get(1);
                logger.debug("param " + param + " data_code " + imos_data_code);
                
                InstanceCoord dc = f.new InstanceCoord();
                dc.createParam(param, imos_data_code);
                
                Array instruments = (Array)row.get(2);
                logger.debug("instruments " + instruments);
                Array source = (Array)row.get(3);
                logger.debug("source_instrument " + source);
                
                try
                {
                    dc.instruments = (Integer[])instruments.getArray();
                    dc.source = (Integer[])source.getArray();                
                }
                catch (SQLException ex)
                {
                    java.util.logging.Logger.getLogger(NetCDFcreateForm.class.getName()).log(Level.SEVERE, null, ex);
                }
                                
                Array depths = (Array)row.get(4);
                logger.debug("depths " + depths);
                
                try
                {
                    BigDecimal[] d = (BigDecimal[])depths.getArray();

                    dc.createDepths(d);
                    for (int i1=0;i1<d.length;i1++)
                    {
                    	allDepths.add(d[i1]);
                    }
                }
                catch (SQLException ex)
                {
                    java.util.logging.Logger.getLogger(NetCDFcreateForm.class.getName()).log(Level.SEVERE, null, ex);
                }
                instanceCoords.add(dc);
            }
            HashMap<String, Integer> nameAndCount = new HashMap<String, Integer>();
            
            for (InstanceCoord ic : instanceCoords)
            {
            	Integer count = nameAndCount.get(ic.params);
            	if (count == null)
            		nameAndCount.put(ic.params, 0);
            	else
            		nameAndCount.put(ic.params, 1);
            }
            int n = 0;
            for (InstanceCoord ic : instanceCoords)
            {
            	Integer count = nameAndCount.get(ic.params);
            	if (count > 0)
            	{            		
            		ic.varName = ic.params + "_" + count++;
            		nameAndCount.put(ic.params, count);            		
            	}
            	else
            		ic.varName = ic.params;

            	logger.debug("dup Param " + ic.params + " count " + count + " name " + ic.varName);
            }
        }
        logger.debug("Finished generating depth array, number of params is " + instanceCoords.size() + " n depths " + allDepths.size());
    }
	
    protected void createTimeArray(String mooringID)
    {
        timeArray = new ArrayList<Timestamp>();
                
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        selectedMooring = Mooring.selectByMooringID(mooringID);
        
        mooringInWaterTime = selectedMooring.getTimestampIn();
        mooringOutWaterTime = selectedMooring.getTimestampOut();
        
        Connection conn = Common.getConnection();
        Statement proc = null;
        
        String SQL = "SELECT min(data_timestamp), max(data_timestamp) FROM " + table + " AS d WHERE mooring_id = " + StringUtilities.quoteString(selectedMooring.getMooringID())
        			+ " AND source_file_id = " + source_file;

        try
        {
            proc = conn.createStatement();
            conn.setAutoCommit(false);
            logger.debug("SQL " + SQL);
            proc.execute(SQL);  
            ResultSet results = (ResultSet) proc.getResultSet();
            results.next();
            dataStartTime = results.getTimestamp(1);
            dataEndTime = results.getTimestamp(2);            
        }
        catch (SQLException sqex)
        {
            logger.warn(sqex);
        }

        logger.debug("DataStart " + dataStartTime + " End " + dataEndTime);
        
        Timestamp currentTimestamp = null;
        
        SQL = "SELECT DISTINCT(data_timestamp)" 
                + " FROM " + table + " AS d "
                + " WHERE mooring_id = " + StringUtilities.quoteString(selectedMooring.getMooringID())      
        		+ " AND source_file_id = " + source_file        
        		+ " ORDER BY 1";
        
        try
        {
            proc.execute(SQL);  
            logger.debug("Time Array SQL : " + SQL);
            ResultSet results = (ResultSet) proc.getResultSet();
            while (results.next())
            {
                currentTimestamp = results.getTimestamp(1);
                timeArray.add(currentTimestamp);
            }
        }
        catch (SQLException ex)
        {
            logger.warn(ex);
        }

        dataEndTime = currentTimestamp;
        
        logger.debug("Finished generating time array, last timestamp was " + currentTimestamp + "\nTotal Elements: " + timeArray.size());
    }
	
	public void NetCDFCreateSetRun()
	{
        table = "raw_instrument_data";  
        InstrumentDataFile idf = InstrumentDataFile.selectByInstrumentDataFileID(source_file);
        selectedMooring = Mooring.selectByMooringID(idf.getMooringID());
        sourceFile = idf;
        
        int insId = idf.getInstrumentID();
        sourceInstrument = Instrument.selectByInstrumentID(insId);
                
        //appendInstrument = sourceInstrument.getModel() + '-' + sourceInstrument.getSerialNumber();
        
        f = new NetCDFfile();
        
        createTimeArray(selectedMooring.getMooringID());
        if (dataStartTime == null)
        {
        	logger.info("No data, exiting");
        	
        	return;
        }
        createDepthArray(selectedMooring.getMooringID());
        createCDFFile();                        
		
	}

	public static void main(String[] args)
	{
        PropertyConfigurator.configure("log4j.properties");
        Common.build("ABOS.properties");

		NetCDFcreateSet cs = new NetCDFcreateSet();
		
		for (int i=0;i<args.length;i++)
		{
			cs.source_file = Integer.parseInt(args[i]);
					
			cs.NetCDFCreateSetRun();
		}
		
		cs.cleanup();
		
		// TODO Auto-generated method stub

	}

}
