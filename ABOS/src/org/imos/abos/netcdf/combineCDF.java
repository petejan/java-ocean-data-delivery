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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayObject;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author peter
 * 
 * this is not a general netCDF reader, but limited to IMOS ANMN data files
 * 
 */
public class combineCDF
{
    private static Logger log = Logger.getLogger(combineCDF.class);

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat netcdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	NetcdfFileWriter datafile;

	class FileToProcess
	{
		public NetcdfDataset ncd;
		public Variable timeVar = null;
		public List<Attribute> globalAttributes;
		public List<Variable> variableList;
		public String filename;
	}
	
	ArrayList<FileToProcess> files = new ArrayList<FileToProcess>();
	
	public combineCDF()
	{
		TimeZone tz = TimeZone.getTimeZone("UTC");
		TimeZone.setDefault(tz);
		
		sdf.setTimeZone(tz);
		netcdfDate.setTimeZone(tz);
	}

	public FileToProcess process(NetcdfDataset ncd) throws IOException, Exception
	{
		FileToProcess file = new FileToProcess();
		file.ncd = ncd;
		file.variableList = ncd.getVariables();
		file.globalAttributes = ncd.getGlobalAttributes();
		
		// find the time variable
		file.timeVar = ncd.findVariableByAttribute(null, "standard_name", "time");

//		for(Variable v: ncd.getVariables())
//		{
//			Attribute stdName = v.findAttribute("standard_name");
//			if (stdName != null)
//			{
//				if (stdName.getStringValue().contains("time"))
//				{
//					file.timeVar = v;
//				}
//			}	
//		}

		// skip file if no time variable found
		if (file.timeVar == null)
			return null;
		
		System.err.println("Time type " + file.timeVar.getDataType());

		Attribute tunit = file.timeVar.findAttribute("units");

		System.err.println("Time type " + tunit.getDataType() + " value " + tunit.toString());

		files.add(file);
		
		return file;
	}
	
	public Variable findVariableByShortName(ArrayList<Variable> list, String name)
	{
		Variable v = null;
		
		for(Variable x : list)
		{
			if (x.getShortName().contentEquals(name))
			{
				v = x;
				break;
			}
		}
		return v;
	}
	public Dimension findDimensionByShortName(ArrayList<Dimension> list, String name)
	{
		Dimension v = null;
		
		for(Dimension x : list)
		{
			if (x.getShortName().contentEquals(name))
			{
				v = x;
				break;
			}
		}
		return v;
	}
	
	public class ArrayIndexComparator implements Comparator<Integer>
	{
	    private final Double[] array;

	    public ArrayIndexComparator(Double[] array)
	    {
	        this.array = array;
	    }

	    public Integer[] createIndexArray()
	    {
	    	Integer[] indexes = new Integer[array.length];
	        for (int i = 0; i < array.length; i++)
	        {
	            indexes[i] = i; // Autoboxing
	        }
	        return indexes;
	    }

	    @Override
	    public int compare(Integer index1, Integer index2)
	    {
	         // Autounbox from Integer to int to use as array indexes
	        return array[index1].compareTo(array[index2]);
	    }
	}	
	
	public void output(ArrayList<String> vars)
	{
		String filename = "output.nc";
		
		try
		{
			datafile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, filename);
			Dimension obsDim = datafile.addUnlimitedDimension("OBS");
			Dimension stationDim = datafile.addDimension(null, "station", files.size());
			Dimension stationNameDim = datafile.addDimension(null, "stationName", 256);
			
			ArrayList<Variable> newVars = new ArrayList<Variable>();
			
			Variable timeX = null;

			for (FileToProcess f : files)
			{
				// add dimensions
				// need to treat time separatly
				// don't add what we already have
				
				for (Dimension d: f.ncd.getDimensions())
				{
					log.debug("dimension " + d.getShortName());
					if (!d.getShortName().contentEquals(f.timeVar.getShortName()))
					{
						if (!datafile.hasDimension(null, d.getShortName()))
						{
							datafile.addDimension(null, d.getShortName(), d.getLength());
						}
					}
				}
				// add global attributes
				// implement attribute black list
				for (Attribute a : f.globalAttributes)
				{
					log.debug("global " + a.getShortName());
					if (!a.getShortName().startsWith("_"))
						datafile.addGroupAttribute(null, a);
				}
				
				// add variables
				// implement variable black list
				for (Variable v: f.variableList)
				{
					log.debug("variable " + v.getShortName());
					Variable x = findVariableByShortName(newVars, v.getShortName());
					if (x == null)
					{
						log.debug("adding " + v.getDimensions().size());
						if (v.getDimensions().size() > 0)
							x = datafile.addVariable(null, v.getShortName(), v.getDataType(), "OBS");
						else
							x = datafile.addVariable(null, v.getShortName(), v.getDataType(), "station");
							
						newVars.add(x);
						if (x.getShortName().contentEquals("TIME"))
						{
							timeX = x;
						}
					}
					// implement variable black list/combining 
					x.addAll(v.getAttributes());
				}
				
			}
			// add instrumentIndex
			// add instrument_name variable
			ArrayList<Dimension>stationDims = new ArrayList<Dimension>();
			stationDims.add(stationDim);
			stationDims.add(stationNameDim);

			Variable stationVar = datafile.addVariable(null, "sourceFile", DataType.CHAR, stationDims);
			stationVar.addAttribute(new Attribute("long_name", "source file of data")); 
			
			Variable vStationIndex = datafile.addVariable(null, "stationIndex", DataType.BYTE, "OBS");
			vStationIndex.addAttribute(new Attribute("long_name", "which station this obs is for")); 
			vStationIndex.addAttribute(new Attribute("instance_dimension", "station")); 
			
			// create file
			datafile.create();
	        ArrayChar.D2 dInstName = new ArrayChar.D2(files.size(), 256); 

	        int i = 0;
	        ArrayList<Double> tArray = new ArrayList<Double>();
			for (FileToProcess f : files)
			{	
		        log.info("time::file " + f.filename + " origin " + tArray.size());
		        int size = f.timeVar.getShape()[0];
		        // deal with the time variable
		        Variable vfile = f.ncd.findVariableByAttribute(null, "standard_name", "time");
		        Array timeData = vfile.read();
		        
		        for(int j=0;j<size;j++)
		        {
		        	tArray.add(timeData.getDouble(j));
		        }
		        i++;
			}
	        
	        int size = tArray.size();
	        ArrayIndexComparator comparator = new ArrayIndexComparator(tArray.toArray(new Double[size]));
	        Integer[] indexes = comparator.createIndexArray();
	        Arrays.sort(indexes, comparator);

	        ArrayDouble.D1 dTime = new ArrayDouble.D1(size);
	        for(int j=0;j<size;j++)
	        {
	        	dTime.set(j, tArray.get(indexes[j]));
	        }
	        datafile.write(timeX, dTime);

	        // create the stationIndex and stationName list
	        ArrayByte.D1 dStationIndex = new ArrayByte.D1(size);
	        byte[] stationIndexB = new byte[size];
	        int j0 = 0;
	        i = 0;
	        for (FileToProcess f : files)
			{	
		        log.info("station::file " + f.filename + " origin " + j0);

		        // create the station name list
		        int s = f.timeVar.getShape()[0];
		        
		        for (int j=0;j<s;j++)
		        {
		        	//log.debug("index " + (j0 + j) + " -> " + indexes[j0+j]);
		        	
		        	stationIndexB[j+j0] = (byte)i;
		        }
		        j0 += s;
		              
		        // create the source file name list
		        dInstName.setString(i, f.filename);
		        
		        i++;
			}
	        datafile.write(stationVar, dInstName);	        
	        for(int j=0;j<size;j++)
	        {
	        	dStationIndex.set(j, stationIndexB[indexes[j]]);
	        }
	        datafile.write(vStationIndex, dStationIndex);
	        
			// add data
			for (Variable v: newVars)
			{
				if (!v.getShortName().contentEquals("TIME"))
				{
					log.info("variable " + v.getShortName());
					if (v.getShape().length == 0)
					{
						
					}
					else if (v.getDimension(0) != null)
						if (v.getDimension(0).getShortName().contentEquals(files.get(0).timeVar.getShortName()))
					{
						ArrayObject.D1 aData = new ArrayObject.D1(v.getDataType().getClassType(), size);
						Object[] data = new Object[size];
		
						for (FileToProcess f : files)
						{
					        log.info("file " + f.filename);
		
							Variable vfile = f.ncd.findVariable(null, v.getShortName());
							Array vd = vfile.read();
		
									int j1 = 0;
									for(int j=0;j<vd.getSize();j++)
									{
										data[j+j1] = vd.getObject(j);
									}
									j1 += size;
						}
				        for(int j=0;j<size;j++)
				        {
				        	aData.set(j, data[indexes[j]]);
				        }
						
						datafile.write(v,  aData);
					}
				}
			}
			datafile.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvalidRangeException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}


	public void read(String filename)
	{
		NetcdfDataset ncd = null;
		try
		{
			ncd = NetcdfDataset.openDataset(filename);

			FileToProcess ftp = process(ncd);
			
			if (ftp != null)
			{
				ftp.filename = filename;
			}
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
		combineCDF r = new combineCDF();
		ArrayList<String> var = new ArrayList<String>();

		for(int file=0;file<args.length;file++)
		{
			
			if (args[0].startsWith("-v"))
			{
				file ++;
				var.add(args[file]);
			}
			r.read(args[file]);
			
		}
		r.output(var);
	}
}
