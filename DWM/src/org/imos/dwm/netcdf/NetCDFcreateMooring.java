package org.imos.dwm.netcdf;

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
import org.imos.dwm.dbms.Instrument;
import org.imos.dwm.dbms.InstrumentDataFile;
import org.imos.dwm.dbms.Mooring;
import org.imos.dwm.forms.NetCDFcreateForm;
import org.imos.dwm.netcdf.NetCDFfile.InstanceCoord;
import org.wiley.core.Common;
import org.wiley.util.StringUtilities;

import ucar.nc2.Attribute;

public class NetCDFcreateMooring extends NetCDFcreateForm
{
  private static Logger logger = Logger.getLogger(NetCDFcreateMooring.class.getName());

  public NetCDFcreateMooring()
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

    String SQL = "SELECT parameter_code, imos_data_code, array_agg(instrument_id) AS instruments, array_agg(source) AS source, array_agg(depth) AS depths FROM"
    		+"(SELECT CAST(parameter_code AS varchar(20)), imos_data_code , d.mooring_id, d.instrument_id, s.instrument_id AS source, CAST(avg(depth) AS numeric(8,3)) AS depth" 
    		+"		   FROM  processed_instrument_data AS d JOIN instrument_data_files AS s ON (source_file_id = datafile_pk)"
    		+"			JOIN instrument ON (d.instrument_id = instrument.instrument_id)"
    		+"			JOIN parameters ON (d.parameter_code = parameters.code)"
    		+"		  WHERE d.mooring_id = 'SOFS-7.5-2018' AND quality_code not in ('BAD')"
    		+"		  GROUP BY parameter_code, imos_data_code, d.mooring_id, d.instrument_id, s.instrument_id, make, model, serial_number ORDER BY 1, 2, depth, make, model, serial_number" 
    		+"		) AS a"
    		+"		GROUP BY parameter_code, imos_data_code ORDER BY depths, parameter_code";

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

        Array instruments = (Array)row.get(2);
        logger.debug("instruments " + instruments);
        Array source = (Array)row.get(3);
        logger.debug("source_instrument " + source);
        Array depths = (Array)row.get(4);
        logger.debug("depths " + depths);

        InstanceCoord dc = f.new InstanceCoord();

        Integer[] insts = null;
        Integer[] src = null;
        BigDecimal[] d = null;
        try
        {
        	insts = (Integer[])instruments.getArray();
        	src = (Integer[])source.getArray();
            d = (BigDecimal[])depths.getArray();
        }
        catch (SQLException ex)
        {
          java.util.logging.Logger.getLogger(NetCDFcreateForm.class.getName()).log(Level.SEVERE, null, ex);
        }

        for( int j = 0; j< insts.length; j++)
        {
        	dc.createParam(param, imos_data_code);

        	Integer[] x = new Integer[1];
        	x[0] = insts[j];
        	dc.instruments = x;
        	x[0] = src[j];
        	dc.source = x;                

        	if (insts.length > 1)
        	{
        		// TODO: check for duplicate variables
        		dc.varName = dc.params + "_" + insts[j];
        	}
        	else
        	{
        		dc.varName = dc.params;	        	
        	}

        	BigDecimal[] y = new BigDecimal[1];
        	y[0] = d[j];

        	dc.createDepths(y);
        	for (int i1=0;i1<d.length;i1++)
        	{
        		allDepths.add(y[0]);
        	}
        	logger.debug("data coord " + dc);

        	instanceCoords.add(dc);
        }
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
      + " FROM processed_instrument_data AS d "
      + " WHERE mooring_id = " + StringUtilities.quoteString(selectedMooring.getMooringID())      
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

    logger.debug("Finished generating time array, last timestamp was " + currentTimestamp + " Total Elements: " + timeArray.size());

  }

  public void NetCDFCreateSetMooring(String mooring)
  {
    table = "raw_instrument_data";  
    selectedMooring = Mooring.selectByMooringID(mooring);

    logger.debug("NetCDFCreateSetInstrument:: mooring " + mooring + " " + selectedMooring);

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

    NetCDFcreateMooring cs = new NetCDFcreateMooring();
  	cs.NetCDFCreateSetMooring(args[0]);    	

    cs.cleanup();

    // TODO Auto-generated method stub

  }

}
