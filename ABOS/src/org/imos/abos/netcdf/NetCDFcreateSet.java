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

import ucar.nc2.Attribute;

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
      + "       AND d.quality_code not in ('PBAD', 'BAD', 'PGOOD')" 
      //+ "       AND ((d.quality_code not in ('INTERPOLATED') AND datafile_pk = " + sourceFile.getDataFilePrimaryKey() + ") " 
      //+ "        OR  (d.instrument_id = " + dataForInstrument.getInstrumentID() + " AND d.quality_code = 'INTERPOLATED') )" 
      + "        AND  (d.instrument_id = " + dataForInstrument.getInstrumentID() + ")" 
      //                + "       AND data_timestamp BETWEEN " + StringUtilities.quoteString(Common.getRawSQLTimestamp(dataStartTime)) + " AND " + StringUtilities.quoteString(Common.getRawSQLTimestamp(dataEndTime))
      + "   GROUP BY parameter_code, imos_data_code, d.mooring_id, d.instrument_id, s.instrument_id, att.depth, make, model, serial_number" 
      + "   ORDER BY 1, 2, depth, make, model, serial_number"; 

    //selectLimited = " AND d.quality_code not in ('INTERPOLATED')";

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

      // for each parameter, count number of instances of the parameter
      HashMap<String, Integer> nameAndCount = new HashMap<String, Integer>();            
      for (InstanceCoord ic : instanceCoords)
      {
        Integer count = nameAndCount.get(ic.params);
        if (count == null)
          nameAndCount.put(ic.params, 0);
        else
          nameAndCount.put(ic.params, 1);
      }
      // create numbered variables for the duplicate parameters
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

    Timestamp currentTimestamp = null;
    dataStartTime = null;

    // fetch list of timestamps, not the INTERPOLATED ones for a source file as these ar interpolated to a new sensor with the same source file id
    String SQL = "SELECT DISTINCT(data_timestamp)" 
      + " FROM " + table + " AS d "
      + " WHERE mooring_id = " + StringUtilities.quoteString(selectedMooring.getMooringID())      
      + " AND source_file_id = " + source_file  
      + " AND quality_code not in ('INTERPOLATED')"
      + " ORDER BY 1";

    try
    {
      proc = conn.createStatement();
      conn.setAutoCommit(false); // we're not going to commit anything
      proc.execute(SQL);  

      logger.debug("Time Array SQL : " + SQL);
      ResultSet results = (ResultSet) proc.getResultSet();
      while (results.next())
      {
        currentTimestamp = results.getTimestamp(1);
        timeArray.add(currentTimestamp);
        if (dataStartTime == null)
          dataStartTime = currentTimestamp;
        dataEndTime = currentTimestamp;
      }
    }
    catch (SQLException ex)
    {
      logger.warn(ex);
    }

    logger.debug("DataStart " + dataStartTime + " End " + dataEndTime);

    logger.debug("Finished generating time array, last timestamp was " + currentTimestamp + "\nTotal Elements: " + timeArray.size());

    f.groupAttributeList.add(new Attribute("internal_source_file", sourceFile.getDataFilePrimaryKey()));

  }

  public void NetCDFCreateSetRun()
  {
    table = "raw_instrument_data";  
    InstrumentDataFile idf = InstrumentDataFile.selectByInstrumentDataFileID(source_file);
    selectedMooring = Mooring.selectByMooringID(idf.getMooringID());
    sourceFile = idf;

    int insId = idf.getInstrumentID();
    dataForInstrument = Instrument.selectByInstrumentID(insId);

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

  public void NetCDFCreateSetInstrument(String mooring, int instId)
  {
    table = "raw_instrument_data";  
    selectedMooring = Mooring.selectByMooringID(mooring);

    logger.debug("NetCDFCreateSetInstrument:: mooring " + mooring + " " + selectedMooring + " instrument " + instId);

    dataForInstrument = Instrument.selectByInstrumentID(instId);
    
    sourceFile = InstrumentDataFile.selectDataFilesForMooringInstrument(mooring, instId).get(0);
    source_file = sourceFile.getDataFilePrimaryKey();

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
    Integer source_file = null;
    Integer instrument = null;
    String mooring = null;

    for (int i=0;i<args.length;i++)
    {
      if (args[i].startsWith("-m"))
        mooring = args[++i];
      else if (args[i].startsWith("-i"))
        instrument = Integer.parseInt(args[++i]);
      else
      {
        cs.source_file = Integer.parseInt(args[i]);
        cs.NetCDFCreateSetRun();
      }
      if (instrument != null)
      {
        cs.NetCDFCreateSetInstrument(mooring, instrument);
      }
 
    }

    cs.cleanup();

    // TODO Auto-generated method stub

  }

}
