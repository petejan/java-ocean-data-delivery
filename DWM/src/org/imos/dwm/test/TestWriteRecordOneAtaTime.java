package org.imos.dwm.test;

import java.io.IOException;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

public class TestWriteRecordOneAtaTime
{
	public void testWriteRecordOneAtaTime() throws IOException, InvalidRangeException
	{
		String filename = "testWriteRecord2.nc";
		NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, filename);

		// define dimensions, including unlimited
		Dimension latDim = writer.addDimension(null, "lat", 3);
		Dimension lonDim = writer.addDimension(null, "lon", 4);
		Dimension timeDim = writer.addUnlimitedDimension("time");
		
		Attribute n = new Attribute("number_of_records", 1);
		writer.addGroupAttribute(null, n);

		// define Variables
		Variable lat = writer.addVariable(null, "lat", DataType.FLOAT, "lat");
		lat.addAttribute(new Attribute("units", "degrees_north"));
		Variable lon = writer.addVariable(null, "lon", DataType.FLOAT, "lon");
		lon.addAttribute(new Attribute("units", "degrees_east"));
		Variable rh = writer.addVariable(null, "rh", DataType.INT, "time lat lon");
		rh.addAttribute(new Attribute("long_name", "relative humidity"));
		rh.addAttribute(new Attribute("units", "percent"));
		Variable t = writer.addVariable(null, "T", DataType.DOUBLE, "time lat lon");
		t.addAttribute(new Attribute("long_name", "surface temperature"));
		t.addAttribute(new Attribute("units", "degC"));
		Variable time = writer.addVariable(null, "time", DataType.INT, "time");
		time.addAttribute(new Attribute("units", "hours since 1990-01-01"));

		// create the file
		writer.create();

		// write out the non-record variables
		writer.write(lat, Array.factory(new float[] { 41, 40, 39 }));
		writer.write(lon, Array.factory(new float[] { -109, -107, -105, -103 }));

		//// heres where we write the record variables
		// different ways to create the data arrays.
		// Note the outer dimension has shape 1, since we will write one record
		//// at a time
		ArrayInt rhData = new ArrayInt.D3(1, latDim.getLength(), lonDim.getLength());
		ArrayDouble.D3 tempData = new ArrayDouble.D3(1, latDim.getLength(), lonDim.getLength());
		Array timeData = Array.factory(DataType.INT, new int[] { 1 });
		Index ima = rhData.getIndex();

		int[] origin = new int[] { 0, 0, 0 };
		int[] time_origin = new int[] { 0 };

		// loop over each record
		for (int timeIdx = 0; timeIdx < 10; timeIdx++)
		{
			// make up some data for this record, using different ways to fill
			// the data arrays.
			timeData.setInt(timeData.getIndex(), timeIdx * 12);

			for (int latIdx = 0; latIdx < latDim.getLength(); latIdx++)
			{
				for (int lonIdx = 0; lonIdx < lonDim.getLength(); lonIdx++)
				{
					rhData.setInt(ima.set(0, latIdx, lonIdx), timeIdx * latIdx * lonIdx);
					tempData.set(0, latIdx, lonIdx, timeIdx * latIdx * lonIdx / 3.14159);
				}
			}
			// write the data out for one record
			// set the origin here
			time_origin[0] = timeIdx;
			origin[0] = timeIdx;
			writer.write(rh, origin, rhData);
			writer.write(t, origin, tempData);
			writer.write(time, time_origin, timeData);
		} // loop over record

		
		timeDim.setLength(10);
		
		Attribute n2 = new Attribute("number_of_records", 10);
		Dimension td = time.getDimension(0);

		td.setLength(10);
		//td.setUnlimited(false);
		
		System.out.println("time dimension " + td);
		
		writer.updateAttribute(null, n2);
		
		// all done
		writer.close();
	}

	public static void main(String args[]) throws Exception
	{
		TestWriteRecordOneAtaTime tw = new TestWriteRecordOneAtaTime();
		tw.testWriteRecordOneAtaTime();
	}
}
