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
import java.text.SimpleDateFormat;
import java.util.*;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;

/**
 *
 * @author peter
 * 
 * this is not a general netCDF reader, but intended only to read the generated NetCDF 
 * 
 */
public class ReadCDF
{
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ArrayList timeArrays = new ArrayList();
    ArrayList headers = new ArrayList();
    Date times[];

    public ReadCDF()
    {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void process(NetcdfDataset ncd) throws IOException, Exception
    {
        Variable varT = ncd.findVariable("TIME");
        if (varT == null)
        {
            varT = ncd.findVariable("TIME");
        }
        if (varT == null)
        {
            System.err.println("No TIME variable, giving up");

            return;
        }
        String tvarString = varT.getShortName();

        System.err.println("Time type " + varT.getDataType());

        Attribute tunit = varT.findAttribute("units");

        System.err.println("Time type " + tunit.getDataType() + " value " + tunit.toString());

        Formatter fmt = new Formatter();
        fmt.format("CoordinateAxis1DTime %s: %n %s is %d %n %s is %f", fmt.getClass(), "Integer", 10, "Float", 10.4);
        CoordinateAxis1DTime tm = CoordinateAxis1DTime.factory(ncd, new VariableDS(null, varT, true), fmt);
        times = tm.getTimeDates();

        System.err.println(sdf.format(times[0]) + " CoordinateAxis1DTime " + tm);

        // iterate over all variables, extracting the time based ones
        for (Variable var : ncd.getVariables())
        {
            System.err.print("Var " + var.getShortName() + " (");
            if (!var.getShortName().startsWith(tvarString))
            {
                for (Dimension d : var.getDimensions())
                {
                    System.err.print(" " + d.getName());
                    if (d.getName().startsWith(tvarString))
                    {
                        headers.add(var.getShortName());
                        timeArrays.add(var.read());
                    }
                }
            }
            System.err.println(")");
        }
    }
    
    public void csvOutput()
    {   
        // print header
        System.out.print("TIME,");
        for (ListIterator<String> it = headers.listIterator(); it.hasNext();)
        {
            String v = it.next();
            System.out.print(v);
            if (it.hasNext())
            {
                System.out.print(",");
            }
        }
        System.out.println();
        
        // print data
        for(int i=0;i<times.length;i++)
        {
            System.out.print(sdf.format(times[i]) + ",");
            for (ListIterator<Array> it = timeArrays.listIterator(); it.hasNext();)
            {
                Array v = it.next();
                System.out.print(v.getDouble(i));
                if (it.hasNext())
                {
                    System.out.print(",");
                }
            }
            System.out.println();
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
        catch (Exception e)
        {
            e.printStackTrace();
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
        
        r.csvOutput();
    }
}
