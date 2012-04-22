/*
 * IMOS data delivery project
 * Written by Peter Jansen
 * This code is copyright (c) Peter Jansen 2012
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.netcdf;

import java.io.IOException;
import ucar.ma2.Array;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author peter
 */
public class ReadCDF
{
    public ReadCDF()
    {
        
    }
    
    public void process(NetcdfDataset ncd) throws IOException
    {
        Variable v = ncd.findVariable("time");
        System.out.println("Time type " + v.getDataType());
        
        if (null == v) return;

        Array data = v.read();
        
        for(Variable vars : ncd.getVariables())
        {
            System.out.println("Var " + vars.getShortName());
            for (Dimension d : vars.getDimensions())
            {
                System.out.println("Dim " + d.getName());
            }
        }
        
    }

    public void read(String filename, boolean header)
    {
        NetcdfDataset ncd = null;
        try
        {
            ncd = NetcdfDataset.openDataset(filename);
            if (header)
            {
                System.out.println(ncd);
            }
            
            process(ncd);
        }
        catch (IOException ioe)
        {
            System.out.println("trying to open " + filename + " " + ioe);
        }
        finally
        {
            if (null != ncd)
            {
                try
                {
                    ncd.close();
                }
                catch (IOException ioe)
                {
                    System.out.println("trying to close " + filename + " " + ioe);
                }
            }
        }
    }

    static public void main(String[] args)
    {
        ReadCDF r = new ReadCDF();
        boolean header = false;
        int file = 0;
        
        if (args[0].startsWith("-h"))
        {
            header = true;
            file++;
        }
        r.read(args[file], header);
    }
    
}
