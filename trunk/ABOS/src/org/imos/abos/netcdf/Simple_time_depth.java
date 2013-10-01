package org.imos.abos.netcdf;

/* This is part of the netCDF package.
   Copyright 2006 University Corporation for Atmospheric Research/Unidata.
   See COPYRIGHT file for conditions of use.

   This example writes some surface pressure and temperatures. It is
   intended to illustrate the use of the netCDF Java API. The companion
   program sfc_pres_temp_rd.java shows how to read the netCDF data file
   created by this program.

   This example demonstrates the netCDF Java API.

   Full documentation of the netCDF Java API can be found at:
http://www.unidata.ucar.edu/software/netcdf-java/
*/

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index2D;
import ucar.ma2.InvalidRangeException;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import ucar.ma2.ArrayString;
import ucar.ma2.Index;
import ucar.ma2.Index1D;

public class Simple_time_depth
{

    public static void main(String args[]) throws Exception
    {
        final int TIME = 12;
        final int DEPTH = 1;
        final float SAMPLE_TEMP = 9.0f;

        // Create the file.
        String filename = "simple_time_depth.nc";
        NetcdfFileWriter dataFile = null;

        try
        {
            // Create new netcdf-3 file with the given filename
            dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, filename);

            Dimension timeDim = dataFile.addDimension(null, "TIME", TIME);
            Dimension depthDim = dataFile.addDimension(null, "DEPTH", DEPTH);
            List<Dimension> dims = new ArrayList<Dimension>();
            dims.add(timeDim);
            dims.add(depthDim);

            List<Dimension> dimTIME = new ArrayList<Dimension>();
            dimTIME.add(timeDim);
            
            Variable vTIME = dataFile.addVariable(null, "TIME", DataType.FLOAT, dimTIME);
            Variable vDEPTH = dataFile.addVariable(null, "DEPTH", DataType.FLOAT, dimTIME);

            Variable vTemp = dataFile.addVariable(null, "temperature", DataType.FLOAT, dims);
//            int[] snDim = {3};
//            ArrayString aSerialNumbers = new ArrayString(snDim);
//            Index idx1 = new Index1D(snDim);
//            idx1.set(0);
//            aSerialNumbers.set(idx1, "1234");
//            idx1.incr();
//            aSerialNumbers.set(idx1, "8967");
//            idx1.incr();
//            aSerialNumbers.set(idx1, "ABSC");
            ArrayList aSerialNumbers = new ArrayList();
            aSerialNumbers.add("1234; ");
            aSerialNumbers.add("8976; ");
            aSerialNumbers.add("ABSC");
            
            vTemp.addAttribute(new Attribute("SerialNumber", aSerialNumbers));

            // Define units attributes for coordinate vars. This attaches a
            // text attribute to each of the coordinate variables, containing
            // the units.

            // Define units attributes for variables.
            vTemp.addAttribute(new Attribute("units", "celsius"));

            // Write the coordinate variable data. This will put the latitudes
            // and longitudes of our data grid into the netCDF file.
            dataFile.create();

            Array dataTIME = Array.factory(DataType.FLOAT, new int[] { TIME });
            Array dataDEPTH = Array.factory(DataType.FLOAT, new int[] { TIME });

            // Create some pretend data. If this wasn't an example program, we
            // would have some real data to write, for example, model
            // output.
            int i, j;

            for (i = 0; i < timeDim.getLength(); i++)
            {
                dataTIME.setFloat(i, 0.1f * i);
            }

            for (j = 0; j < timeDim.getLength(); j++)
            {
                dataDEPTH.setFloat(j, -5.f * j);
            }

            dataFile.write(vTIME, dataTIME);
            dataFile.write(vDEPTH, dataDEPTH);

            // Create the pretend data. This will write our surface temperature data.

            int[] iDim = new int[] { timeDim.getLength(), depthDim.getLength() };
            Array dataTemp = Array.factory(DataType.FLOAT, iDim);

            Index2D idx = new Index2D(iDim);

            for (i = 0; i < timeDim.getLength(); i++)
            {
                for (j = 0; j < depthDim.getLength(); j++)
                {
                    idx.set(i, j);
                    dataTemp.setFloat(idx, SAMPLE_TEMP + .25f * (j * DEPTH + i));
                }
            }

            dataFile.write(vTemp, dataTemp);
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        } 
        catch (InvalidRangeException e)
        {
            e.printStackTrace();
        } 
        finally
        {
            if (null != dataFile)
            {
                try
                {
                    dataFile.close();
                    System.out.println("*** SUCCESS writing example file simple_time_depth.nc!");
                } 
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }
        }
        
    }
}
