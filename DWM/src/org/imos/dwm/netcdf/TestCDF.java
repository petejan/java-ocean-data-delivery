/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.dwm.netcdf;

/**
 *
 * @author peter
 */

import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.ma2.*;

import java.io.IOException;
import java.util.ArrayList;

public class TestCDF {


     public static void main(String args[])
    {
        // We are writing 2D data, a 6 x 12 grid.
       final int NX = 6;
       final int NY = 12;


       // Create the file.
       String filename = "/Users/peter/simple_xy.nc";
       NetcdfFileWriteable dataFile = null;

       try {
           dataFile = NetcdfFileWriteable.createNew(filename, false);

           // Create netCDF dimensions,
            Dimension xDim = dataFile.addDimension("x", NX );
            Dimension yDim = dataFile.addDimension("y", NY );

            ArrayList dims =  new ArrayList();

            // define dimensions
            dims.add( xDim);
            dims.add( yDim);


           // Define a netCDF variable. The type of the variable in this case
           // is ncInt (32-bit integer).
           dataFile.addVariable("data", DataType.INT, dims);

            // This is the data array we will write. It will just be filled
            // with a progression of numbers for this example.
           ArrayInt.D2 dataOut = new ArrayInt.D2( xDim.getLength(), yDim.getLength());

           // Create some pretend data. If this wasn't an example program, we
           // would have some real data to write, for example, model output.
           int i,j;

           for (i=0; i<xDim.getLength(); i++) {
                for (j=0; j<yDim.getLength(); j++) {
                    dataOut.set(i,j, i * NY + j);
                }
           }

           // create the file
           dataFile.create();


           // Write the pretend data to the file. Although netCDF supports
           // reading and writing subsets of data, in this case we write all
           // the data in one operation.
          dataFile.write("data", dataOut);


       } catch (IOException e) {
              e.printStackTrace();
       } catch (InvalidRangeException e) {
              e.printStackTrace();
       } finally {
            if (null != dataFile)
            try {
                dataFile.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
       }

        System.out.println( "*** SUCCESS writing example file simple_xy.nc!");
    }

}
