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
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.time.CalendarDate;

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
    ArrayList<Variable> timeVars;
    List<CalendarDate> dates;
    ArrayList<String> auxVarNames;
    NetcdfDataset ncd;

    public ReadCDF()
    {
        timeVars = new ArrayList<Variable>();
        auxVarNames = new ArrayList<String>();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void process(NetcdfDataset ncd) throws IOException, Exception
    {
        this.ncd = ncd;
        
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

        CoordinateAxis1DTime tm = CoordinateAxis1DTime.factory(ncd, new VariableDS(null, varT, true), null);
        dates = tm.getCalendarDates();

        Date t0 = new Date(dates.get(0).getMillis());
        System.err.println(sdf.format(t0) + " CoordinateAxis1DTime " + tm);

        // iterate over all variables, extracting the time based ones
        for (Variable var : ncd.getVariables())
        {
            System.err.print("Var " + var.getShortName() + " (");
            if (!var.getShortName().startsWith(tvarString))
            {
                for (Dimension d : var.getDimensions())
                {
                    System.err.print(" " + d.getShortName());
                    if (d.getShortName().startsWith(tvarString))
                    {
                        timeVars.add(var);
                    }
                }
                Attribute aAv = var.findAttribute("ancillary_variables");
                System.err.println(")");
                if (aAv != null)
                {                    
                    auxVarNames.add(aAv.getStringValue());
                    System.err.println(" has AUX var : " + aAv.getStringValue());
                }                
            }
            else 
                System.err.println(")");
        }
    }
    
    public void csvOutput()
    {   
        // print header
        System.out.print("TIME");
        for (ListIterator<Variable> it = timeVars.listIterator(); it.hasNext();)
        {
            Variable v = it.next();
            
            if (!auxVarNames.contains(v.getShortName()))
            {
                Attribute aSn = v.findAttribute("standard_name");
                Attribute aLn = v.findAttribute("long_name");
                String name = v.getShortName();
                if (aSn != null)
                {
                    name = aSn.getStringValue();
                }
                else if (aLn != null)
                {
                    name = aLn.getStringValue();
                }
                Dimension dDepth = v.getDimension(1);
                //System.out.print("Dim : " + dDepth.getShortName());
                Variable vDepth = ncd.findVariable(dDepth.getShortName());
                ArrayFloat.D1 depths;
                try 
                {
                    depths = (ArrayFloat.D1)vDepth.read();
                    for(int i=0;i<depths.getSize();i++)
                    {
                        System.out.print(",");
                        System.out.print(name + "(" + depths.get(i) + ")");
                    }
                }
                catch (IOException ex) 
                {
                    ex.printStackTrace();
                }
            }
        }
        System.out.println();
        
        // print data
        Date t = new Date();
        float d;
        float nan = Float.NaN;
        
        // now finally for each time, output the data from each sensor at each depth
        for (int i = 0; i < dates.size() ; i++)
        {
            t.setTime(dates.get(i).getMillis());
            System.out.print(sdf.format(t));
            for (ListIterator<Variable> it = timeVars.listIterator(); it.hasNext();)
            {
                Variable v = it.next();
                if (!auxVarNames.contains(v.getShortName()))
                {
                    ArrayFloat.D2 data;
                    try
                    {
                        data = (ArrayFloat.D2) v.read();

                        int[] shape = data.getShape();
                        for(int j=0;j<shape[1];j++)
                        {
                            System.out.print(",");
                            d = data.get(i, j);
                            if (!Float.isNaN(d))
                                System.out.print(d);
                            else
                                System.out.print("");
                        }
                    }
                    catch (IOException ex)
                    {
                        Logger.getLogger(ReadCDF.class.getName()).log(Level.SEVERE, null, ex);
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
    
            csvOutput();            
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
        }
        
    }
}
