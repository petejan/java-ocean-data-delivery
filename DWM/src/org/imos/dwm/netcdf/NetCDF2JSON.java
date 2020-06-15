package org.imos.dwm.netcdf;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class NetCDF2JSON 
{
    private static Logger log = Logger.getLogger(NetCDF2JSON.class);


	public static void main(String args[])
	{ 
		PropertyConfigurator.configure("log4j.properties");
		
		NetCDF2JSON n2j = new NetCDF2JSON();
		
		n2j.open(args[0]);
	}

	private void open(String filename)
	{
		NetcdfFile ncfile = null;
		
		try 
		{
		    ncfile = NetcdfDataset.openFile(filename, null);
		    
		    process(ncfile);
		  
		} 
		catch (IOException ioe) 
		{
		    log.warn("trying to open " + filename, ioe);		 
		} 
		finally 
		{ 
		    if (null != ncfile) try 
		    {
		      ncfile.close();
		    } 
		    catch (IOException ioe) 
		    {
		      log.warn("trying to close " + filename, ioe);
		    }
		  }		
	}

	private void process(NetcdfFile ncfile)
	{
		List<Dimension> dims = ncfile.getDimensions();
		List<Variable> vars = ncfile.getVariables();
		List<Attribute> attrs = ncfile.getGlobalAttributes();
		
		System.out.println("{");

		boolean first = true;
		System.out.println("dimensions:{");
		for (Dimension d : dims)
		{
			//log.info("Dimension " + d);
			if (!first)
				System.out.print(",");
			System.out.print(" {" + d.getShortName() + ":" + d.getLength() + "}");
			first = false;
		}
		System.out.println();
		System.out.println("}");
		
		System.out.println("global_attributes:{");
		first = true;
		for (Attribute a : attrs)
		{
			//log.info("Global Attrs " + a);
			if (!first)
				System.out.print(",");
			System.out.print(" {" + a.getShortName() + ":{type:" + a.getDataType() + ",value:");
			if (a.isString())
			{
				String s = a.getStringValue();
				System.out.print("\"" + s.replaceAll("\n", "\\\\n") + "\"");
			}
			else
			{
				System.out.print(a.getValues());				
			}
			System.out.println("}}");
			first = false;
		}
		System.out.println("}");

		for (Variable v : vars)
		{
			log.info("Variables " + v);
			List <Attribute> vattrs = v.getAttributes();
			for (Attribute a : vattrs)
			{
				log.info("Attrs " + a);
			}
		}
		System.out.println("}");
	}
}
