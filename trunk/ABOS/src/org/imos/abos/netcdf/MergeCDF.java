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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.Index1D;
import ucar.ma2.Index2D;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
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
public class MergeCDF
{
    SimpleDateFormat sdf;
    private NetcdfFileWriter dataFile;

    public MergeCDF()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
    
    List <GlobalAttribute> gAtts = new ArrayList<GlobalAttribute>();

    public class GlobalAttribute
    {
        String name;
        Array values;
        
        public String toString()
        {
            return name;
        }
    }
    
    public void ingestFile(NetcdfDataset ncd) throws IOException, Exception
    {
        Variable varT = ncd.findVariable("TIME");
        if (varT == null)
        {
            System.out.println("No TIME variable, giving up");

            return;
        }
        VariableDS varTds = new VariableDS(null, varT, false);

        //  System.out.println("Time type " + varT.getDataType());

        // Attribute tunit = varT.findAttribute("units");
        
        List <Attribute> filegAtts = ncd.getGlobalAttributes();
        for (Attribute a : filegAtts)
        {
            String name = a.getShortName().trim();
            
            if (name.contains("time_deployment_start"))
            {
                System.out.println("deployment start " + a.getStringValue());
            }
            if (name.contains("time_deployment_end"))
            {
                System.out.println("deployment end   " + a.getStringValue());
            }
                                
            GlobalAttribute found = null;
            for(GlobalAttribute g : gAtts)
            {
                if (g.name.contains(name))
                {
                    found = g;
                    break;
                }
            }
            if (found == null)
            {
                System.out.println("New Attribute " + name);
                
                GlobalAttribute newOne = new GlobalAttribute();
                newOne.name = name;
                newOne.values = a.getValues().copy();
                gAtts.add(newOne);
            }
            else
            {
                Object gArray = found.values.copyTo1DJavaArray();
                System.out.println("Array " + gArray.getClass().getName());
                
            }
        }
        
        CoordinateAxis1DTime timeAxis = CoordinateAxis1DTime.factory(ncd, varTds, null);
        List<CalendarDate> dates = timeAxis.getCalendarDates();
        int tSize = dates.size();
        // System.out.println("time range size " + tSize);
        System.out.println(" time start " + dates.get(0) + " end " + dates.get(tSize-1));
        
        Date fDate = new Date(dates.get(0).getMillis());
        startDate.add(fDate);
        if (start == null)
        {
            start = fDate;
        }
        else
        {
            if (start.after(fDate))
            {
                start = fDate;
            }
        }
        fDate = new Date(dates.get(tSize-1).getMillis());
        if (end == null)
        {
            end = fDate;            
        }
        else
        {
            if (end.before(fDate))
            {
                end = fDate;
            }
        }
        
        int tMid = tSize/2;
        double t1 = dates.get(tMid).getMillis();
        double t2 = dates.get(tMid+1).getMillis();

        t1 = Math.round(t1/1000.0)*1000;
        t2 = Math.round(t2/1000.0)*1000;
        tStep = t2 - t1;

        double tSampleAt = t1 % tStep;
        tSampleAt = Math.round(tSampleAt);
        System.out.println(" time mid " + dates.get(tMid) + " interval " + tStep + " sample at " + tSampleAt);

        for (Variable var : ncd.getVariables())
        {
            System.out.println("Var " + var.getShortName() + " rank " + var.getRank() );
            
            NewVar newOne = null;
            if (var.getShortName().compareTo("TIME") != 0)
            {
                for (NewVar nv : newVars)
                {
                    if (nv.name.compareTo(var.getShortName()) == 0)
                    {
                        newOne = nv;
                        nv.oldVars.add(var);
                        break;
                    }
                }
                if (newOne == null)
                {
                    System.out.println("New Variable");
                    
                    newOne = new NewVar();
                    newOne.name = var.getShortName();
                    newOne.oldVars.add(var);
                    
                    newVars.add(newOne);
                }
            }
        }
        inputDataSets.add(ncd);
    }
    
    Date start = null;
    Date end = null;
    double tStep;
    List <NetcdfDataset> inputDataSets = new ArrayList<NetcdfDataset>();
    List <Date> startDate = new ArrayList<Date>();

    List <NewVar> newVars = new ArrayList<NewVar>();

    public class NewVar
    {
        String name;
        Variable newVar;
        List<Variable> oldVars = new ArrayList<Variable>();
    }
    
    public void write(String filename) throws IOException, InvalidRangeException
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long anchorTime = 0;
        try
        {
            Date ts = df.parse("1950-01-01 00:00:00");
            anchorTime = ts.getTime();
        }
        catch (ParseException pex)
        {
            System.out.println(pex);
        }
        
        dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, filename);

        // create the output TIME array
        
        long s = start.getTime();
        s = (long) (s / ((long)tStep)) * (long)(tStep);
        Date sRound = new Date(s);
        
        System.out.println("sRound " + sRound);

        long e = end.getTime();
        e = (long) (e / ((long)tStep)) * (long)(tStep);
        Date eRound = new Date(e);
        
        System.out.println("eRound " + eRound);
        
        int count = (int) ((e - s) / (tStep)) + 1;
        
        System.out.println("Count " + count);
        
        Dimension timeDim = dataFile.addDimension(null, "TIME", count);

        // create a new INSTANCE dimension
        Dimension instanceDim = dataFile.addDimension(null, "INSTANCE", newVars.get(0).oldVars.size());
        
        // TODO: could have different dimensions as well
        NetcdfDataset ncd = inputDataSets.get(0);
        
        for (Dimension dim : ncd.getDimensions())
        {
            if (!dim.getShortName().startsWith("TIME"))
            {
                dataFile.addDimension(null, dim.getShortName(), dim.getLength());
            }
        }      
        
        for(GlobalAttribute a : gAtts)
        {
            System.out.println("Adding global " + a.name);
            
            dataFile.addGroupAttribute(null, new Attribute(a.name, a.values));            
        }
                
        ArrayDouble.D1 times = new ArrayDouble.D1(timeDim.getLength());
        
        Variable vTime = dataFile.addVariable(null, "TIME", DataType.DOUBLE, "TIME");
        
        // TODO: there is a much more general way of doing time....
        
        vTime.addAttribute(new Attribute("name", "time"));
        vTime.addAttribute(new Attribute("long_name", "time"));
        vTime.addAttribute(new Attribute("units", "days since 1950-01-01T00:00:00Z"));
        vTime.addAttribute(new Attribute("axis", "T"));
        vTime.addAttribute(new Attribute("valid_min", 0.0));
        vTime.addAttribute(new Attribute("valid_max", 999999999));
        vTime.addAttribute(new Attribute("calendar", "gregorian"));

        // Att the new variable dimensions, variables and variable attributes
        for (NewVar var : newVars)
        {
            List<Dimension> vDim = new ArrayList<Dimension>(var.oldVars.get(0).getDimensions());
            if (vDim.get(0).getShortName().compareTo("TIME") == 0)
            {   
                vDim.set(0, timeDim);
                vDim.add(1, instanceDim);
            }
            
            var.newVar = dataFile.addVariable(null, var.name, var.oldVars.get(0).getDataType(), vDim);     
            List <Attribute> atts = var.oldVars.get(0).getAttributes();
            
            for(Attribute att: atts)
            {
                var.newVar.addAttribute(att);
            }
        }
        
        dataFile.create();

        // Create new time variable
        
        long d = (s - anchorTime);
        for(int i=0;i<count;i++)
        {            
            times.set(i, ((double)d) / 1000 / 60 / 60 / 24);
            d += tStep;
        }

        dataFile.write(vTime, times);
        
        // find index of new timestamp in input file
        
        int firstIndex = (int) ((startDate.get(0).getTime() - s)/((long)tStep));
        System.out.println("First Index " + firstIndex);        

        // write out all variables to new file
        // now copy data        
        
        for (NewVar var : newVars)
        {
            System.out.println("Writing " + var.newVar.getShortName());
            System.out.println("new var " + var.newVar.getNameAndDimensions());
        
            int[] newShape = var.newVar.getShape();
            for (int c = 0; c < newShape.length; c++)
            {
                System.out.println("Shape " + c + " " + newShape[c]);
            }
            System.out.println("Type " + var.newVar.getDataType().toString() + " " + var.newVar.getDataType().getClassType().getName());
            
            //if (var.newVar.getDataType().isFloatingPoint())
            {
                Array v = Array.factory(var.newVar.getDataType().getClassType(), newShape);
                //ArrayDouble v = new ArrayDouble(newShape);
            
                int instance = 0;
                Index idx = v.getIndex();
                for (Variable oldVar : var.oldVars)
                {
                    System.out.println("old var   " + oldVar.getNameAndDimensions());            
                    
                    Array oldA = oldVar.read();
                    Index oldIdx = oldA.getIndex();

                    //System.out.println("new Shape " + idx.toStringDebug());
                    //System.out.println("old Shape " + oldIdx.toStringDebug());
                    
                    if (var.newVar.getDimensions().get(0).getShortName().compareTo("TIME") == 0)
                    {                    
                        idx.set1(instance);

                        for(int i=0;i<var.newVar.getShape(0);i++)
                        {
                            if (i < oldIdx.getShape(0))
                            {
                                //System.out.println("i " + i);
                                oldIdx.setDim(0, i);
                                //System.out.println("old " + oldIdx.toStringDebug());
                                idx.setDim(0, i + firstIndex);

                                for(int r = 2;r<newShape.length;r++) // only take dimensions past TIME, INSTANCE
                                {
                                    for (int y = 0;y<newShape[r];y++)
                                    {
                                        idx.setDim(r, y);
                                        oldIdx.setDim(r-1, y);
                                        
                                        //System.out.println("r " + r + " y " + y);
                                    }
                                }
                                
                                //Float o = oldA.getFloat(oldIdx);
                                //v.setFloat(idx, o);
                                
                                Object o = oldA.getObject(oldIdx);
                                v.setObject(idx, o);
                            }
                        }
                    }
                    else
                    {
                        System.out.println("Non time dimension variable ");

                        for (int r = 0; r < newShape.length; r++)
                        {
                            for (int y = 0; y < newShape[r]; y++)
                            {
                                // System.out.println(" r y " + r + " " + y);
                                idx.setDim(r, y);
                                oldIdx.setDim(r, y);
                            }
                        }
                        
                        v.setObject(idx, oldA.getObject(oldIdx));
                    }
                                        
                    instance++;
                }
                dataFile.write(var.newVar, v);
            }                
        }
        
        dataFile.close();
        
        for(NetcdfDataset ds : inputDataSets)
        {
            ds.close();
        }
    }
    
    public void read(String filename, boolean header)
    {
        NetcdfDataset ncd = null;
        try
        {
            System.out.println("file : " + filename);
            ncd = NetcdfDataset.openDataset(filename);
            if (header)
            {
                System.out.println(ncd);
            }

            ingestFile(ncd);
        
            // create new time array which spans input time, with same step as input file
            
        }
        catch (IOException ioe)
        {
            System.out.println("trying to open " + filename + " " + ioe);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static public void main(String[] args)
    {
        MergeCDF r = new MergeCDF();
        boolean header = false;
        int file = 0;

        if (args[0].startsWith("-h"))
        {
            header = true;
            file++;
        }
        for (int i = file; i < args.length; i++)
        {
            r.read(args[i], header);
        }
        try
        {
            r.write("output.nc");
        }
        catch (IOException ex)
        {
            Logger.getLogger(MergeCDF.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InvalidRangeException ex)
        {
            Logger.getLogger(MergeCDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
