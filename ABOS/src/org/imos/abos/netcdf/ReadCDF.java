/*
 * IMOS data delivery project
 * Written by Peter Jansen
 * This code is copyright (c) Peter Jansen 2012
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */
package org.imos.abos.netcdf;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import ucar.ma2.Array;
import ucar.ma2.Index;
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
public class ReadCDF extends Component
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
                
                Index ix = v.getIndex();
                int shape[] = ix.getShape();
                int rank = ix.getRank();

                for(int s=0;s<rank;s++)
                {
                    ix.set(i, s);
                    System.out.print(v.getDouble(ix));
                    if (it.hasNext())
                    {
                        System.out.print(",");
                    }
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

        if (args.length == 0)
        {
            //Create a file chooser
            final JFileChooser fc = new JFileChooser();
            
            String $HOME = System.getProperty("user.home");
            
            File optionsFile = new File($HOME + "/ABOS/ABOS.properties");
            Properties p = new Properties();
            
            if (optionsFile.exists())
            {
                BufferedReader br;
                try
                {
                    br = new BufferedReader(new FileReader(optionsFile));
                    p.load(br);
                    
                    fc.setCurrentDirectory(new File(p.getProperty("dir")));
                }
                catch (FileNotFoundException ex)
                {
                    Logger.getLogger(ReadCDF.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (IOException ex)
                {
                    Logger.getLogger(ReadCDF.class.getName()).log(Level.SEVERE, null, ex);
                }                   
            }
            
            int returnVal = fc.showOpenDialog(r);
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
                File f = fc.getSelectedFile();
                try
                {
                    r.read(f.getCanonicalPath(), true);
                    
                    p.setProperty("cdf-dir", "" + f.getParent());
                    BufferedWriter br = new BufferedWriter(new FileWriter(optionsFile));
                    p.store(br, "ABOS user config");
                                        
                    r.csvOutput();
                }
                catch (IOException ex)
                {
                    Logger.getLogger(ReadCDF.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        else
        {
            if (args[0].startsWith("-h"))
            {
                header = true;
                file++;
            }
            r.read(args[file], header);
    
            r.csvOutput();
        }
        
    }
}
