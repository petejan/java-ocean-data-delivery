/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.netcdf;

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
import java.util.TimeZone;
import org.imos.abos.dbms.Mooring;
import ucar.ma2.ArrayInt;

/**
 *
 * @author peter
 */
public class TestCDF3
{
    private static SQLWrapper query    = new SQLWrapper();
    private static Logger logger = Logger.getLogger(TestCDF3.class.getName());

    private Vector dataSet = null;

    public TestCDF3()
    {
        super();
    }

    public static void main(String args[])
    {
        String $HOME = System.getProperty("user.home");

        if(args.length == 0)
        {
            PropertyConfigurator.configure("log4j.properties");
            Common.build($HOME + "/ABOS/ABOS.properties");
        }

        TestCDF3 cdf = new TestCDF3();
        cdf.getData();
        cdf.createCDFFile();
    }

    private void getData()
    {
        String setupSQL = "drop table if exists foobar"
                    + ";\n"
                    + "drop table if exists foobar2"
                    + ";\n"
                    + " create temp table foobar as "
                    + " select distinct date_trunc('hour',data_timestamp) as Obs_Time,"
                    + " latitude, longitude, parameter_value as TEMP"
                    + " from raw_instrument_data"
                    + " where instrument_id = 4"
                    + " and (parameter_code = 'TEMP')"
                    + " ;\n"
                    + "create temp table foobar2 as "
                    + " select distinct date_trunc('hour',data_timestamp) as Obs_Time,"
                    + " latitude, longitude, parameter_value as PRES"
                    + " from raw_instrument_data"
                    + " where instrument_id = 4"
                    + " and (parameter_code = 'PRES')"
                    + " ;\n"
                    ;

        String sql = "select  "
                    + " foobar.Obs_Time,"
                    + " foobar.latitude,"
                    + " foobar.longitude,"
                    + " foobar.TEMP,"
                    + " foobar2.PRES"
                    + " from foobar, foobar2"
                    + " where foobar.Obs_Time = foobar2.Obs_Time"
                    + " order by 1\n"
                    ;

        Common.executeSQL(setupSQL);
        query.setConnection( Common.getConnection() );
        query.executeQuery( sql );

        dataSet = query.getData();
    }

    private void createCDFFile()
    {

        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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


        if(dataSet == null || dataSet.isEmpty())
        {
            logger.error("No data, cannot create netCDF file!");
            return;
        }

        logger.debug("Got " + dataSet.size() + " records.");

        Mooring currentMooring = Mooring.selectByMooringID("PULSE_7");

        String filename = "/Users/peter/SBE16_tempV6.nc";

        int NLVL = 1;
        int NLAT = 1;
        int NLON = 1;
        int RECORD_COUNT = dataSet.size();

        float START_LAT = 25.0f;
        float START_LON = -125.0f;
        float START_DEPTH = -100;

        NetcdfFileWriteable dataFile = null;

        try
        {
            // Create new netcdf-3 file with the given filename
            dataFile = NetcdfFileWriteable.createNew(filename, false);

            if(currentMooring != null)
            {
                dataFile.addGlobalAttribute("Mooring", currentMooring.getShortDescription());
                dataFile.addGlobalAttribute("Latitude", currentMooring.getLatitudeIn());
                dataFile.addGlobalAttribute("Longitude", currentMooring.getLongitudeIn());

                START_LAT = currentMooring.getLatitudeIn().floatValue();
                START_LON = currentMooring.getLongitudeIn().floatValue();
            }


            //add dimensions  where time dimension is unlimit
            Dimension lvlDim = dataFile.addDimension("level", NLVL);
            Dimension latDim = dataFile.addDimension("latitude", NLAT);
            Dimension lonDim = dataFile.addDimension("longitude", NLON);
            //Dimension timeDim = dataFile.addUnlimitedDimension("TIME");
            Dimension timeDim = dataFile.addDimension("TIME",RECORD_COUNT);

            ArrayList dims = null;

            // Define the coordinate variables.
            dataFile.addVariable("latitude", DataType.FLOAT, new Dimension[]
                    {
                        latDim
                    });
            dataFile.addVariable("longitude", DataType.FLOAT, new Dimension[]
                    {
                        lonDim
                    });
            dataFile.addVariable("level", DataType.FLOAT, new Dimension[]
                    {
                        lvlDim
                    });
            dataFile.addVariable("TIME", DataType.INT, new Dimension[]
                    {
                        timeDim
                    });


            // Define units attributes for data variables.
            dataFile.addVariableAttribute("latitude", "units", "degrees_south");
            dataFile.addVariableAttribute("longitude", "units", "degrees_east");
            dataFile.addVariableAttribute("level", "units", "metres below surface (nominal)");
            dataFile.addVariableAttribute("TIME", "units","hours since 1950-01-01T00:00:00Z");

            // Define the netCDF variables for the pressure and temperature
            // data.
            dims = new ArrayList();
            dims.add(timeDim);
            dims.add(lvlDim);
            dims.add(latDim);
            dims.add(lonDim);
            dataFile.addVariable("pressure", DataType.FLOAT, dims);
            dataFile.addVariable("temperature", DataType.FLOAT, dims);

            // Define units attributes for data variables.
            dataFile.addVariableAttribute("pressure", "units", "dbar");
            dataFile.addVariableAttribute("pressure", "standard_name", "sea_PRES");
            dataFile.addVariableAttribute("temperature", "units", "celsius");




            // Create some pretend data. If this wasn't an example program, we
            // would have some real data to write for example, model output.
            ArrayFloat.D1 lats = new ArrayFloat.D1(latDim.getLength());
            ArrayFloat.D1 lons = new ArrayFloat.D1(lonDim.getLength());
            ArrayFloat.D1 depths = new ArrayFloat.D1(lvlDim.getLength());

            ArrayInt.D1 times = new ArrayInt.D1(timeDim.getLength());

            for (int i = 0; i < latDim.getLength(); i++)
            {
                lats.set(i, START_LAT);
            }

            for (int j = 0; j < lonDim.getLength(); j++)
            {
                lons.set(j, START_LON);
            }

            for (int j = 0; j < lvlDim.getLength(); j++)
            {
                depths.set(j, START_DEPTH);
            }

            Vector v = (Vector) dataSet.get(0);
            Timestamp ts0 = ((Timestamp)v.get(0));
            logger.debug("Start timestamp is " + ts0);

            TimeZone tz = TimeZone.getTimeZone("GMT");
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(tz);

            for(int i = 0; i < timeDim.getLength(); i++)
            {
                Vector row = (Vector) dataSet.get(i);

                Timestamp ts = ((Timestamp)row.get(0));
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
            Vector v0 = (Vector) dataSet.get(timeDim.getLength() - 1);
            Timestamp ts1 = ((Timestamp)v0.get(0));
            logger.debug("End timestamp is " + ts1);


            // Create the data.
            
            ArrayFloat.D4 dataTemp = new ArrayFloat.D4(RECORD_COUNT, lvlDim.getLength(), latDim.getLength(), lonDim.getLength());
            ArrayFloat.D4 dataPres = new ArrayFloat.D4(RECORD_COUNT, lvlDim.getLength(), latDim.getLength(), lonDim.getLength());

            for (int record = 0; record < RECORD_COUNT; record++)
            {
                Vector row = (Vector) dataSet.get(record);

                Double SST = ((Number)row.get(3)).doubleValue();
                Double pressure = ((Number)row.get(4)).doubleValue();

                int i = 0;
                for (int lvl = 0; lvl < NLVL; lvl++)
                {
                    for (int lat = 0; lat < NLAT; lat++)
                    {
                        for (int lon = 0; lon < NLON; lon++)
                        {

                            dataPres.set(record, lvl, lat, lon, pressure.floatValue());
                            dataTemp.set(record, lvl, lat, lon, SST.floatValue());
                        }
                    }
                }
            }

            //Create the file. At this point the (empty) file will be written to disk
            dataFile.create();

            // A newly created Java integer array to be initialized to zeros.
            int[] origin = new int[4];

            dataFile.write("latitude", lats);
            dataFile.write("longitude", lons);
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
        System.out.println("*** SUCCESS writing example file " + filename);
    }
}
