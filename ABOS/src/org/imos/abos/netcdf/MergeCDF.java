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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.Index;
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
 * this is not a general netCDF reader, but intended only to Merge the generated NetCDF 
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
    
    public class InputData
    {
        List<Dimension> dimensions;
        List<Attribute> globalAttributes;
        List<Variable> vars = new ArrayList<Variable>();
        Date startDate;
        Date endDate;
        Date deploymentStart = null;
        Date deploymentEnd = null;
        NetcdfDataset ncd;
        long tStep;
        double depth;
        
        long samplingInterval;   
        final SimpleDateFormat sdfT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");      
        
        public InputData(NetcdfDataset newNcd)
        {
            ncd = newNcd;
            
            System.out.println("InputData:: " + ncd.getLocation());
            
            Variable varT = ncd.findVariable("TIME");
            if (varT == null)
            {
                System.out.println("InputData::No TIME variable, giving up");

                return;
            }
            VariableDS varTds = new VariableDS(null, varT, false);
            globalAttributes = ncd.getGlobalAttributes();
            vars = ncd.getVariables();
            
            // find deployment start/end
            for (Attribute a : globalAttributes)
            {
                String name = a.getShortName().trim();
                try
                {
                    if (name.contains("time_deployment_start"))
                    {
                        System.out.println("InputData::deployment start " + a.getStringValue());
                        deploymentStart = sdfT.parse(a.getStringValue());
                    }
                    else if (name.contains("time_deployment_end"))
                    {
                        System.out.println("InputData::deployment end   " + a.getStringValue());
                        deploymentEnd = sdfT.parse(a.getStringValue());
                    }
                    else if (name.contains("instrument_nominal_depth"))
                    {
                        depth = a.getNumericValue().doubleValue();
                        
                        System.out.println("InputData::instrument_nominal_depth   " + depth);
                    }
                }
                catch (ParseException ex)
                {
                    Logger.getLogger(MergeCDF.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            CoordinateAxis1DTime timeAxis;
            try
            {
                timeAxis = CoordinateAxis1DTime.factory(ncd, varTds, null);
                List<CalendarDate> dates = timeAxis.getCalendarDates();
                int tSize = dates.size();
                // System.out.println("time range size " + tSize);
                System.out.println("InputData::deployment start " + sdf.format(deploymentStart) + " end " + sdf.format(deploymentEnd));

                startDate = new Date(dates.get(0).getMillis());
                endDate = new Date(dates.get(tSize-1).getMillis());
                System.out.println("InputData:: time start " + startDate + " end " + endDate);

                int tMid = tSize/2;
                double t1 = dates.get(tMid).getMillis();
                double t2 = dates.get(tMid+1).getMillis();

                t1 = Math.round(t1/1000.0)*1000;
                t2 = Math.round(t2/1000.0)*1000;
                tStep = (long) (t2 - t1);

                double tSampleAt = t1 % tStep;
                tSampleAt = Math.round(tSampleAt);
                
                System.out.println("InputData:: time mid " + dates.get(tMid) + " interval " + tStep + " sample at " + tSampleAt);
            }
            catch (IOException ex)
            {
                Logger.getLogger(MergeCDF.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            System.out.println();
        }
    }
    
    public void ingestFile(NetcdfDataset ncd) throws IOException, Exception
    {
        InputData id = new InputData(ncd);
        
        for (Attribute a : id.globalAttributes)
        {
            String name = a.getShortName().trim();
            
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
                //System.out.println("ingest::globalAttribute " + name + " " + a.getValue(0));
                
                GlobalAttribute newOne = new GlobalAttribute();
                newOne.name = name;
                newOne.values = a.getValues().copy();
                gAtts.add(newOne);
            }
            else
            {
                //System.out.println("ingest::globalAttribute duplicate name " + name);
                //System.out.println("ingest::globalAttribute duplicate value " + a.getStringValue() + " " + found.values.getObject(0));
                // TODO: merge attributes, when there different
                if (a.isString())
                {
                    if (a.getStringValue().compareTo((String)found.values.getObject(0)) != 0)
                    {
                        System.out.println("ingest::globalAttribute nonduplicate values " + name);                       
                    }
                 }
            }
        }
        
        if (start != null)
        {
            start = new Date(Math.min(start.getTime(), id.startDate.getTime()));
        }
        else
        {
            start = id.startDate;
        }
        if (end != null)
        {
            end = new Date(Math.max(end.getTime(), id.endDate.getTime()));
        }
        else
        {
            end = id.endDate;
        }
        tStep = id.tStep;

        inputDataSets.add(id);
        
        System.out.println();
    }
    
    Date start = null;
    Date end = null;
    double tStep;
    List <InputData> inputDataSets = new ArrayList<InputData>();

    List <Variable> newVars = new ArrayList<Variable>();

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
        
        System.out.println("write::sRound " + sRound);

        long e = end.getTime();
        e = (long) (e / ((long)tStep)) * (long)(tStep);
        Date eRound = new Date(e);
        
        System.out.println("write::eRound " + eRound);
        
        int count = (int) ((e - s) / (tStep)) + 1;
        
        System.out.println("write::Count " + count);
        
        Dimension timeDim = dataFile.addDimension(null, "TIME", count);

        // create a new INSTANCE dimension
        Dimension instanceDim = dataFile.addDimension(null, "DEPTH", inputDataSets.size());
        
        // TODO: could have different dimensions as well
        NetcdfDataset ncd = inputDataSets.get(0).ncd;
        
        for (Dimension dim : ncd.getDimensions())
        {
            if (!dim.getShortName().startsWith("TIME"))
            {
                dataFile.addDimension(null, dim.getShortName(), dim.getLength());
            }
        }      
        
        for(GlobalAttribute a : gAtts)
        {
            System.out.println("write::Adding global " + a.name);
            
            dataFile.addGroupAttribute(null, new Attribute(a.name, a.values));            
        }
                
        ArrayDouble.D1 times = new ArrayDouble.D1(timeDim.getLength());
        
        Variable vTime = dataFile.addVariable(null, "TIME", DataType.DOUBLE, "TIME");
        
        // TODO: there is a much more general way of doing time....
        
        vTime.addAttribute(new Attribute("name", "time"));
        vTime.addAttribute(new Attribute("long_name", "time"));
        vTime.addAttribute(new Attribute("units", "days since 1950-01-01T00:00:00 UTC"));
        vTime.addAttribute(new Attribute("axis", "T"));
        vTime.addAttribute(new Attribute("valid_min", 0.0));
        vTime.addAttribute(new Attribute("valid_max", 999999999));
        vTime.addAttribute(new Attribute("calendar", "gregorian"));

        Collections.sort(inputDataSets, new Comparator<InputData>()
                {
                    public int compare(InputData s1, InputData s2) 
                    {
                        return (int)(s1.depth - s2.depth);
                    }
                });

        ArrayFloat.D1 depths = new ArrayFloat.D1(instanceDim.getLength());
        
        Variable vDepth = dataFile.addVariable(null, "DEPTH", DataType.FLOAT, "DEPTH");
        
        vDepth.addAttribute(new Attribute("name", "nominal_depth"));
        vDepth.addAttribute(new Attribute("long_name", "nominal depth of each sensor"));
        vDepth.addAttribute(new Attribute("units", "meters"));
        vDepth.addAttribute(new Attribute("positive", "down"));
        vDepth.addAttribute(new Attribute("axis", "Z"));
        vDepth.addAttribute(new Attribute("comment", "These are nominal values. Use PRES to derive time-varying depths of instruments, as the mooring may tilt in ambient currents."));
        vDepth.addAttribute(new Attribute("reference", "sea_level"));
        vDepth.addAttribute(new Attribute("valid_min", 0.0f));
        vDepth.addAttribute(new Attribute("valid_max", 4000.0f));
        
        // Att the new variable dimensions, variables and variable attributes
        int instance = 0;
        for (InputData id : inputDataSets)
        {
            System.out.println("write:: processing file " + id.ncd.getLocation());
            depths.set(instance, (float)id.depth);
            
            for (Variable var : id.vars)
            {
                System.out.println("write::Var " + var.getShortName() + " rank " + var.getRank() );

                Variable newOne = null;
                if ((var.getShortName().compareTo("TIME") != 0) 
                        && (var.getShortName().compareTo("TIME_quality_control") != 0) 
                        && (var.getShortName().compareTo("DEPTH") != 0) 
                        && (var.getShortName().compareTo("DEPTH_quality_control") != 0)) 
                {
                    for (Variable nv : newVars)
                    {
                        if (nv.getShortName().compareTo(var.getShortName()) == 0)
                        {
                            newOne = nv;
                            break;
                        }
                    }
                    if (newOne == null)
                    {
                        System.out.println("write::New Variable : " + var.getShortName());

                        List<Dimension> vDim = new ArrayList<Dimension>(var.getDimensions());
                        if (vDim.get(0).getShortName().compareTo("TIME") == 0)
                        {   
                            vDim.set(0, timeDim);
                            vDim.add(1, instanceDim);
                        }

                        newOne = dataFile.addVariable(null, var.getShortName(), var.getDataType(), vDim);

                        for(Attribute att : var.getAttributes())
                        {
                            newOne.addAttribute(att);
                        }
                        
                        newVars.add(newOne);
                    }
                    else
                    {
                        for(Attribute att: newOne.getAttributes())
                        {
                            Attribute newAtt = var.findAttribute(att.getShortName());
                            if (newAtt == null)
                            {
                                newOne.addAttribute(att);
                                System.out.println("write::Adding Variable Attribute " + att.getShortName() + " value " + att.getStringValue());
                            }
                            else
                            {
                                if (newAtt.isString())
                                {
                                    if (newAtt.getStringValue().compareTo(att.getStringValue()) != 0)
                                    {
                                        Attribute newAttComp = new Attribute(att.getShortName(), newAtt.getStringValue() + "; " + att.getStringValue());
                                        newOne.addAttribute(newAttComp);
                                        System.out.println("write::Adding Variable Attribute Comb " + newAttComp.getShortName() + " value " + newAttComp.getStringValue());
                                    }
                                }
                            }
                        }

                    }
                }
            }
            instance++;
            System.out.println();
        }

        dataFile.create();

        // Create new time variable
        // TODO: just copy from input file
        
        long d = (sRound.getTime() - anchorTime);
        for(int i=0;i<count;i++)
        {            
            times.set(i, ((double)d) / 1000 / 60 / 60 / 24);
            d += tStep;
        }

        dataFile.write(vTime, times);
        dataFile.write(vDepth, depths);
        
        // find index of new timestamp in input file
        
        // write out all variables to new file
        // now copy data        
        
        for (Variable var : newVars)
        {
            System.out.println("write::Writing " + var.getShortName());
            System.out.println("write::new var " + var.getNameAndDimensions());
        
            int[] newShape = var.getShape();
            for (int c = 0; c < newShape.length; c++)
            {
                System.out.println("write:: Shape " + c + " " + newShape[c]);
            }
            System.out.println("write::Type " + var.getDataType().toString() + " " + var.getDataType().getClassType().getName());
            
            Array varArray = Array.factory(var.getDataType().getClassType(), newShape);
            //ArrayDouble v = new ArrayDouble(newShape);

            Index idx = varArray.getIndex();
            int stride = 1;
            for (int i=2;i<idx.getRank();i++)
            {
                stride = stride * idx.getShape(i);
            }
            System.out.println("write:: stride " + stride);
            
            instance = 0;
            for (InputData id : inputDataSets)
            {
                for (int j = 0; j < id.vars.size(); j++)
                {
                    Variable oldVar = id.vars.get(j);

                    if (oldVar.getShortName().compareTo(var.getShortName()) == 0)
                    {
                        System.out.println("write::old var   " + oldVar.getNameAndDimensions() + " depth " + id.depth);

                        Array oldA = oldVar.read();
                        Index oldIdx = oldA.getIndex();

                        System.out.println("write::new index " + idx.toStringDebug());
                        System.out.println("write::old index " + oldIdx.toStringDebug());

                        if (var.getDimensions().get(0).getShortName().compareTo("TIME") == 0)
                        {
                            int firstIndex = 0;
                            while (times.getFloat(firstIndex) < oldA.getFloat(0))
                            {
                                firstIndex++;
                            }
                            System.out.println("write::First Index " + firstIndex);

                            idx.set1(instance);

                            // loop down the TIME dimension                            
                            for (int k = 0; k < var.getShape(0); k++)
                            {
                                idx.set0(k);
                                idx.set2(0);
                                idx.set3(0);
                                idx.setCurrentCounter((k * inputDataSets.size() + instance) * stride);
                                
                                if (((k + firstIndex) >= 0) && ((k + firstIndex) < oldIdx.getShape(0)))
                                {
                                    // System.out.println("i " + i);
                                    oldIdx.set0(k + firstIndex);
                                    oldIdx.set1(0);
                                    oldIdx.set2(0);
                                    oldIdx.setCurrentCounter(k + firstIndex);
                                    
                                    //System.out.println("old " + oldIdx.toStringDebug());
                                    for(int y = 0;y<stride;y++)
                                    {
                                        varArray.setObject(idx, oldA.getObject(oldIdx));
                                        idx.incr();
                                        oldIdx.incr();
                                        
                                        //System.out.println("write:: current " + idx.currentElement() + " " + oldIdx.currentElement());
                                    }
                                }
                                else
                                {
                                    for(int y = 0;y<stride;y++)
                                    {
                                        varArray.setObject(idx, Float.NaN);
                                        idx.incr();
                                        oldIdx.incr();
                                    }                                    
                                }
                            }
                        }
                        else
                        {
                            System.out.println("write::Non time dimension variable ");

                            //System.out.println("Shape [" + r + "] " + newShape[r]);
                            oldIdx.setCurrentCounter(0);
                            idx.setCurrentCounter(0);
                            for(int y = 0;y<oldIdx.getSize();y++)
                            {
                                varArray.setObject(idx, oldA.getObject(oldIdx));
                                idx.incr();
                                oldIdx.incr();
                            }
                        }
                    }
                }
                instance++;
                // System.out.println("write:: " + var + " " + varArray);
                dataFile.write(var, varArray);
            }
        }
        
        System.out.println("write::close");
        
        dataFile.close();
        
        for(InputData ds : inputDataSets)
        {
            ds.ncd.close();
        }
    }
    
    public void read(String filename)
    {
        NetcdfDataset ncd = null;
        try
        {
            System.out.println("read::file : " + filename);
            ncd = NetcdfDataset.openDataset(filename);

            ingestFile(ncd);
        
            // create new time array which spans input time, with same step as input file
            
        }
        catch (IOException ioe)
        {
            System.out.println("read::trying to open " + filename + " " + ioe);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static public void main(String[] args)
    {
        MergeCDF r = new MergeCDF();
        String outFile = "output.nc";
        
        for (int i=0;i<args.length;i++)
        {
            if (args[i].startsWith("-o"))
            {
                outFile = args[++i];
            }
            else
            {
                r.read(args[i]);
            }
        }

        try
        {
            r.write(outFile);
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
