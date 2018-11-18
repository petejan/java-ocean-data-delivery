package org.imos.abos.netcdf;

/* Write MRU data from binary packet to NetCDF file */
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.parsers.NortekParse;
import org.imos.abos.parsers.NortekParse.IMU;
import org.imos.abos.parsers.NortekParse.VectorSystemData;
import org.imos.abos.parsers.NortekParse.VectorVelocityData;
import org.imos.abos.parsers.NortekParse.VelocityDataHeader;
import org.imos.abos.parsers.RawAZFPdata;
import org.wiley.core.Common;

import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayShort;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.Index2D;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

public class WriteNortekPointCurrent
{
    private static Logger log = Logger.getLogger(NortekParse.class);

    public static void main(String args[]) throws Exception
    {
        System.out.println("Memory " + Runtime.getRuntime().totalMemory() + " free " + Runtime.getRuntime().freeMemory());

        String $HOME = System.getProperty("user.home");
        PropertyConfigurator.configure("log4j.properties");
        Common.build("ABOS.properties");
        
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
                
        String mooring_id = args[0];

        // run with something like
        //
        // java -Djna.library.path=/usr/lib64 -cp dist/ABOS.jar org.imos.abos.netcdf.WriteAZFP -x ~/ABOS/AZFP/SOFS-4-2014/201303/13032423.XML -m SOFS-4-2013 ~/ABOS/AZFP/SOFS-4-2014/201305
        
        Date tsStart = null;
        Date tsEnd = null;
        String srcFilename = args[1];
        
        NortekParse nortek = new NortekParse();
        nortek.open(new File(srcFilename));
        
        Object userConfig = null;
		while((userConfig = nortek.read()) != null) 
		{
			if (userConfig instanceof ArrayList)
			{
				ArrayList<?> al = (ArrayList<?>)userConfig;
				if (al.get(0) instanceof Attribute)
					break;
			}
		}
		log.info(userConfig);
		
		Object velHeader = null;
		while((velHeader = nortek.read()) != null) 
		{
			if (velHeader instanceof VelocityDataHeader)
			{
				break;
			}
		}
		log.info(velHeader);
		VelocityDataHeader vHeader = (VelocityDataHeader)velHeader;
		
		System.out.println("Memory " + Runtime.getRuntime().totalMemory() + " free " + Runtime.getRuntime().freeMemory());

        tsStart = nortek.clockDeploy;
        tsEnd = new Date();
        
        Mooring m = Mooring.selectByMooringID(mooring_id);
        ArrayList<Instrument> insts = Instrument.selectInstrumentsForMooring(mooring_id); // Nortek Vector
        log.debug("insts " + m + " " + insts + " looking for " + nortek.serialNo.substring(4, 8));
        Instrument inst = Instrument.selectByInstrumentID(2413);
        for(Instrument ix : insts)
        {
    		log.debug("Instrument " + ix);
    		
        	if (ix.getSerialNumber().contains(nortek.serialNo.substring(4, 8)) & ix.getMake().startsWith("Nortek"))
        	{
        		inst = ix;        
        		break;
        	}
        }
		log.info("Instrument " + inst);
        
        Timestamp dataStartTime = m.getTimestampIn(); // TODO: probably should come from data, esp for part files
        Timestamp dataEndTime = m.getTimestampOut();
        
        NetCDFfile ndf = new NetCDFfile();
        String filename = "NortekTest.nc";
        
        try
        {
            ndf.setMooring(m);
            ndf.setAuthority("IMOS");
            ndf.setFacility("ABOS-SOFS");
            ndf.setMultiPart(true);
            
        	filename = ndf.getFileName(inst, dataStartTime, dataEndTime, "raw_instrument_data", "RVT", null);
        	
            // Create new netcdf-4 file with the given filename
            ndf.createFile(filename);

            ndf.writeGlobalAttributes();
            ndf.createCoordinateUnlimitedVariables();  
            
            ndf.addGroupAttribute(null, new Attribute("serial_number", nortek.serialNo));
            ndf.addGroupAttribute(null, new Attribute("featureType", "timeSeriesProfile"));
            ndf.addGroupAttribute(null, new Attribute("cdm_data_type", "Profile"));
            ndf.addGroupAttribute(null, new Attribute("geospatial_vertical_min", 30f));
            ndf.addGroupAttribute(null, new Attribute("geospatial_vertical_max", 30f));
            ndf.addGroupAttribute(null, new Attribute("geospatial_vertical_positive", "down"));
            ndf.addGroupAttribute(null, new Attribute("time_coverage_start", ndf.netcdfDate.format(tsStart)));
            ndf.addGroupAttribute(null, new Attribute("time_coverage_end", ndf.netcdfDate.format(tsEnd)));
            ndf.addGroupAttribute(null, new Attribute("instrument_nominal_depth", 30f));
            ndf.addGroupAttribute(null, new Attribute("instrument", "Nortek Vector"));
            ndf.addGroupAttribute(null, new Attribute("instrument_serial_numbe", nortek.serialNo));
            ndf.addGroupAttribute(null, new Attribute("file_version", "Level 0 – Raw data"));
            ndf.addGroupAttribute(null, new Attribute("original_file", srcFilename));
            ndf.addGroupAttribute(null, new Attribute("history", ndf.netcdfDate.format(new Date()) + " File Created"));
            
            Dimension sdTimeDim = ndf.dataFile.addUnlimitedDimension("TIME_SYSTEM_DATA");
            
            for(Attribute a : nortek.attributes)
            {
            	ndf.addGroupAttribute(null, a);
            }

            List<Dimension> sdDims = new ArrayList<Dimension>();
            sdDims.add(sdTimeDim);
            
            Variable vsdTime = ndf.dataFile.addVariable(null, "TIME_SYSTEM_DATA", DataType.DOUBLE, sdDims);
            vsdTime.addAttribute(new Attribute("name", "time_system_data"));
            vsdTime.addAttribute(new Attribute("standard_name", "time"));
            vsdTime.addAttribute(new Attribute("long_name", "time of system data measurement"));
            vsdTime.addAttribute(new Attribute("units", "days since 1950-01-01T00:00:00 UTC"));
            vsdTime.addAttribute(new Attribute("axis", "T"));
            vsdTime.addAttribute(new Attribute("valid_min", 10957));
            vsdTime.addAttribute(new Attribute("valid_max", 54787));
            vsdTime.addAttribute(new Attribute("calendar", "gregorian"));

            // Battery voltage
            Variable vBattery = ndf.dataFile.addVariable(null, "BAT", DataType.FLOAT, sdDims);
            vBattery.addAttribute(new Attribute("units", "volt"));
            vBattery.addAttribute(new Attribute("long_name", "instrument_battery_voltage"));
            vBattery.addAttribute(new Attribute("name", "instrument battery voltage"));
            vBattery.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vBattery.addAttribute(new Attribute("valid_min", 0f));
            vBattery.addAttribute(new Attribute("valid_max", 20f));            

            // Temperature
            Variable vTemp = ndf.dataFile.addVariable(null, "TEMP", DataType.FLOAT, sdDims);
            vTemp.addAttribute(new Attribute("units", "degree_Celsius"));
            vTemp.addAttribute(new Attribute("long_name", "instrument_temperature"));
            vTemp.addAttribute(new Attribute("name", "instrument temperature"));
            vTemp.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vTemp.addAttribute(new Attribute("valid_min", 0f));
            vTemp.addAttribute(new Attribute("valid_max", 40f));            

            // Heading, Pitch, Roll
            Variable vHead = ndf.dataFile.addVariable(null, "HEAD", DataType.FLOAT, sdDims);
            vHead.addAttribute(new Attribute("units", "degree"));
            vHead.addAttribute(new Attribute("long_name", "instrument_heading"));
            vHead.addAttribute(new Attribute("name", "instrument heading"));
            vHead.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vHead.addAttribute(new Attribute("valid_min", 0f));
            vHead.addAttribute(new Attribute("valid_max", 360f));            

            Variable vPitch = ndf.dataFile.addVariable(null, "PITCH", DataType.FLOAT, sdDims);
            vPitch.addAttribute(new Attribute("units", "degree"));
            vPitch.addAttribute(new Attribute("long_name", "instrument_pitch"));
            vPitch.addAttribute(new Attribute("name", "instrument pitch"));
            vPitch.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vPitch.addAttribute(new Attribute("valid_min", 0f));
            vPitch.addAttribute(new Attribute("valid_max", 360f));            
            
            Variable vRoll = ndf.dataFile.addVariable(null, "ROLL", DataType.FLOAT, sdDims);
            vRoll.addAttribute(new Attribute("units", "degree"));
            vRoll.addAttribute(new Attribute("long_name", "instrument_pitch"));
            vRoll.addAttribute(new Attribute("name", "instrument pitch"));
            vRoll.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vRoll.addAttribute(new Attribute("valid_min", 0f));
            vRoll.addAttribute(new Attribute("valid_max", 360f));            

            Variable vanaIn = ndf.dataFile.addVariable(null, "ANALOG_IN", DataType.FLOAT, sdDims);
            vanaIn.addAttribute(new Attribute("units", "volts"));
            vanaIn.addAttribute(new Attribute("long_name", "analogue_in"));
            vanaIn.addAttribute(new Attribute("name", "analogue input"));
            vanaIn.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vanaIn.addAttribute(new Attribute("valid_min", 0f));
            vanaIn.addAttribute(new Attribute("valid_max", 5f));            

            System.out.println(" timeDim " + ndf.timeDim.toString() + " " + ndf.timeDim.getLength());

            ndf.vTime = ndf.dataFile.addVariable(null, "TIME", DataType.DOUBLE, "TIME");
            ndf.writeCoordinateVariableAttributes();

            // The main data
            Dimension sampleDim = ndf.dataFile.addDimension(null, "SAMPLE", nortek.NRecords);
            Dimension vectorDim = ndf.dataFile.addDimension(null, "VECTOR", 3);

            List<Dimension> dataDims = new ArrayList<Dimension>();
            dataDims.add(ndf.timeDim);
            dataDims.add(sampleDim);
            
            List<Dimension> vectorDims = new ArrayList<Dimension>();
            vectorDims.add(ndf.timeDim);
            vectorDims.add(sampleDim);
            vectorDims.add(vectorDim);

            Variable vPressure = ndf.dataFile.addVariable(null, "PRES", DataType.FLOAT, dataDims);
            vPressure.addAttribute(new Attribute("units", "dbar"));
            vPressure.addAttribute(new Attribute("long_name", "instrument_pressure"));
            vPressure.addAttribute(new Attribute("name", "instrument pressure"));
            vPressure.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vPressure.addAttribute(new Attribute("valid_min", 0f));
            vPressure.addAttribute(new Attribute("valid_max", 100f));
            
            Variable vAna1 = ndf.dataFile.addVariable(null, "ANALOG1", DataType.FLOAT, dataDims);
            vAna1.addAttribute(new Attribute("units", "counts"));
            vAna1.addAttribute(new Attribute("long_name", "analog_input_micro_temp"));
            vAna1.addAttribute(new Attribute("name", "analog input 1 temp"));
            vAna1.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vAna1.addAttribute(new Attribute("valid_min", -32768f));
            vAna1.addAttribute(new Attribute("valid_max", 32768f));

            Variable vAna2 = ndf.dataFile.addVariable(null, "ANALOG2", DataType.FLOAT, dataDims);
            vAna2.addAttribute(new Attribute("units", "counts"));
            vAna2.addAttribute(new Attribute("long_name", "analog_input_micro_cond"));
            vAna2.addAttribute(new Attribute("name", "analog input 2 conductivity"));
            vAna2.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vAna2.addAttribute(new Attribute("valid_min", -32768f));
            vAna2.addAttribute(new Attribute("valid_max", 32768f));

            Variable vVel = ndf.dataFile.addVariable(null, "VELOCITY", DataType.SHORT, vectorDims);
            vVel.addAttribute(new Attribute("units", "mm/s"));
            vVel.addAttribute(new Attribute("long_name", "water_velocity_vector"));
            vVel.addAttribute(new Attribute("name", "water velocity vector (B1,B2,B3)"));
            vVel.addAttribute(new Attribute("_FillValue", Short.MIN_VALUE));            
            vVel.addAttribute(new Attribute("valid_min", Short.MIN_VALUE));
            vVel.addAttribute(new Attribute("valid_max", Short.MAX_VALUE));

            Variable vAmp = ndf.dataFile.addVariable(null, "ABSI", DataType.BYTE, vectorDims);
            vAmp.addAttribute(new Attribute("units", "counts"));
            vAmp.addAttribute(new Attribute("long_name", "amplitude"));
            vAmp.addAttribute(new Attribute("name", "amplitude (B1,B2,B3)"));
            vAmp.addAttribute(new Attribute("_FillValue", (byte)0));            
            vAmp.addAttribute(new Attribute("valid_min", (byte)0));
            vAmp.addAttribute(new Attribute("valid_max", (byte)255));
            
            Variable vCor = ndf.dataFile.addVariable(null, "CORR_MAG", DataType.SHORT, vectorDims);
            vCor.addAttribute(new Attribute("units", "%"));
            vCor.addAttribute(new Attribute("long_name", "correlation"));
            vCor.addAttribute(new Attribute("name", "correlation (B1,B2,B3)"));
            vCor.addAttribute(new Attribute("_FillValue", (short)-1));            
            vCor.addAttribute(new Attribute("valid_min", (short)0));
            vCor.addAttribute(new Attribute("valid_max", (short)100));
            
            Variable vAccel = ndf.dataFile.addVariable(null, "IMU_ACCEL", DataType.FLOAT, vectorDims);
            vAccel.addAttribute(new Attribute("units", "g"));
            vAccel.addAttribute(new Attribute("long_name", "acceleration"));
            vAccel.addAttribute(new Attribute("name", "acceleration vector (X,Y,Z)"));
            vAccel.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vAccel.addAttribute(new Attribute("valid_min", -10f));
            vAccel.addAttribute(new Attribute("valid_max", 10f));
            
            Variable vAngRate = ndf.dataFile.addVariable(null, "IMU_ANGULAR_RATE", DataType.FLOAT, vectorDims);
            vAngRate.addAttribute(new Attribute("units", "rad/sec"));
            vAngRate.addAttribute(new Attribute("long_name", "angular_rate"));
            vAngRate.addAttribute(new Attribute("name", "angular rate vector (X,Y,Z)"));
            vAngRate.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vAngRate.addAttribute(new Attribute("valid_min", -100f));
            vAngRate.addAttribute(new Attribute("valid_max", 100f));

            Variable vMag = ndf.dataFile.addVariable(null, "IMU_MAGNETIC", DataType.FLOAT, vectorDims);
            vMag.addAttribute(new Attribute("units", "Gauss"));
            vMag.addAttribute(new Attribute("long_name", "magnetic_field_strength"));
            vMag.addAttribute(new Attribute("name", "magnetic field vector (X,Y,Z)"));
            vMag.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vMag.addAttribute(new Attribute("valid_min", -100000f));
            vMag.addAttribute(new Attribute("valid_max", 100000f));

            // Write the coordinate variable data. 
            ndf.create();
            ndf.writePosition(m.getLatitudeIn(), m.getLongitudeIn());            
            
            Object o = null;
            ArrayDouble.D1 times = new ArrayDouble.D1(1);
            
            // System data variables
            ArrayDouble.D1 sd_times = new ArrayDouble.D1(1);
            ArrayFloat.D1 aBat = new ArrayFloat.D1(1);
            ArrayFloat.D1 aTemp = new ArrayFloat.D1(1);
            ArrayFloat.D1 aHead = new ArrayFloat.D1(1);
            ArrayFloat.D1 aPitch = new ArrayFloat.D1(1);
            ArrayFloat.D1 aRoll = new ArrayFloat.D1(1);
            ArrayFloat.D1 aanaIn = new ArrayFloat.D1(1);

            // velocity data
            ArrayFloat.D2 aPressure = new ArrayFloat.D2(1, nortek.NRecords);
            ArrayFloat.D2 aAna1 = new ArrayFloat.D2(1, nortek.NRecords);
            ArrayFloat.D2 aAna2 = new ArrayFloat.D2(1, nortek.NRecords);
            ArrayShort.D3 aVel = new ArrayShort.D3(1, nortek.NRecords, 3);
            ArrayByte.D3 aAmp = new ArrayByte.D3(1, nortek.NRecords, 3);

            // IMU vars
            ArrayFloat.D3 aAccel = new ArrayFloat.D3(1, nortek.NRecords, 3);
            ArrayFloat.D3 aGyro = new ArrayFloat.D3(1, nortek.NRecords, 3);
            ArrayFloat.D3 aMag = new ArrayFloat.D3(1, nortek.NRecords, 3);

            Timestamp ts = vHeader.ts;
            long offsetTime = (ts.getTime() - ndf.anchorTime) / 1000;
            double elapsedHours = ((double) offsetTime) / (3600 * 24);
            times.setDouble(0, elapsedHours);

            int[] time_origin = new int[] { 0 };
            int[] sd_time_origin = new int[] { 0 };
            int[] data_origin = new int[] { 0, 0 };
            int[] vector_origin = new int[] { 0, 0, 0};
            
    		ndf.dataFile.write(ndf.vTime, time_origin, times);
    		time_origin[0]++;

    		tsStart = null;
    		tsEnd = null;
            while ((o = nortek.read()) != null)
            {
            	if (o instanceof VelocityDataHeader)
            	{
            		vHeader = (VelocityDataHeader)o;
            		
            		ts = vHeader.ts;
            		if (tsStart == null)
            		{
            			tsStart = ts;
            			log.info("First TS " + ndf.netcdfDate.format(tsStart));
            		}
            		tsEnd = ts;
            		
            		offsetTime = (ts.getTime() - ndf.anchorTime) / 1000;
            		elapsedHours = ((double) offsetTime) / (3600 * 24);
            		times.setDouble(0, elapsedHours);
            		
            		ndf.dataFile.write(ndf.vTime, time_origin, times);
            		
            		time_origin[0]++;
            	}
            	else if (o instanceof ArrayList)
            	{
            		ArrayList<?> al = (ArrayList<?>)o;
            		Object al1 = al.get(0);
            		if (al1 instanceof VectorVelocityData)
            		{
            			log.info("VectorVelocityData: " + al.size());
            			for(int i = 0;i<al.size();i++)
            			{
            				VectorVelocityData vvd = (VectorVelocityData)al.get(i);
            				aPressure.set(0, i, vvd.pressure);
            				aAna1.set(0, i, vvd.anaIn1);
            				aAna2.set(0, i, vvd.anaIn2);
            				
            				aVel.set(0,  i, 0, vvd.velB1);
            				aVel.set(0,  i, 1, vvd.velB2);
            				aVel.set(0,  i, 2, vvd.velB3);
            				
            				aAmp.set(0,  i, 0, vvd.ampB1);
            				aAmp.set(0,  i, 1, vvd.ampB2);
            				aAmp.set(0,  i, 2, vvd.ampB3);
            			}
                		ndf.dataFile.write(vPressure, data_origin, aPressure);
                		ndf.dataFile.write(vAna1, data_origin, aAna1);
                		ndf.dataFile.write(vAna2, data_origin, aAna2);
                		ndf.dataFile.write(vVel, vector_origin, aVel);
                		ndf.dataFile.write(vAmp, vector_origin, aAmp);

                		data_origin[0]++; // Should be the same as time index above
                		
                		if (data_origin[0] != time_origin[0])
                		{
                			log.error("data_origin and time_origin not the same, record count problem " + data_origin[0] + " " + time_origin[0]);
                		}
            		}
            		else if (al1 instanceof IMU)
            		{
            			log.info("IMU: count " + al.size());
            			for(int i = 0;i<al.size();i++)
            			{
            				IMU imu = (IMU)al.get(i);
            				//log.debug("IMU " + imu.AHRSId);
            				aAccel.set(0, i, 0, imu.accelX);
            				aAccel.set(0, i, 1, imu.accelY);
            				aAccel.set(0, i, 2, imu.accelZ);
            				aGyro.set(0, i, 0, imu.AngRateX);
            				aGyro.set(0, i, 1, imu.AngRateY);
            				aGyro.set(0, i, 2, imu.AngRateZ);
            				aMag.set(0, i, 0, imu.StabMagX);
            				aMag.set(0, i, 1, imu.StabMagY);
            				aMag.set(0, i, 2, imu.StabMagZ);
            			}
                		ndf.dataFile.write(vAccel, vector_origin, aAccel);
                		ndf.dataFile.write(vAngRate, vector_origin, aGyro);
                		ndf.dataFile.write(vMag, vector_origin, aMag);

                		vector_origin[0]++; // Should be the same as time index above
                		
                		if (vector_origin[0] != time_origin[0])
                		{
                			log.error("vector_origin and time_origin not the same, record count problem " + vector_origin[0] + " " + time_origin[0]);
                		}
            		}
            	}
            	else if (o instanceof VectorSystemData)
            	{
            		VectorSystemData vsd = (VectorSystemData)o;
            		
            		//log.info("Write System data " + vsd);
            		
            		ts = vsd.ts;
            		
            		offsetTime = (ts.getTime() - ndf.anchorTime) / 1000;
            		elapsedHours = ((double) offsetTime) / (3600 * 24);
            		sd_times.setDouble(0, elapsedHours);
            		
            		aBat.setFloat(0, vsd.battery);
            		aTemp.setFloat(0, vsd.temp);
            		aHead.setFloat(0, vsd.heading);
            		aPitch.setFloat(0, vsd.pitch);
            		aRoll.setFloat(0, vsd.roll);
            		aanaIn.setFloat(0, vsd.anaIn);
            		
            		//log.debug("sd_time_origin " + sd_time_origin[0] + " " + sd_times);
            		ndf.dataFile.write(vsdTime, sd_time_origin, sd_times);
            		ndf.dataFile.write(vBattery, sd_time_origin, aBat);
            		ndf.dataFile.write(vTemp, sd_time_origin, aTemp);
            		ndf.dataFile.write(vHead, sd_time_origin, aHead);
            		ndf.dataFile.write(vPitch, sd_time_origin, aPitch);
            		ndf.dataFile.write(vRoll, sd_time_origin, aRoll);
            		ndf.dataFile.write(vanaIn, sd_time_origin, aanaIn);

            		sd_time_origin[0]++;
            	}
            }
            
            log.info("Timestamp end " + ndf.netcdfDate.format(tsEnd));
    		ndf.dataFile.updateAttribute(null, new Attribute("time_coverage_start", ndf.netcdfDate.format(tsStart)));
    		ndf.dataFile.updateAttribute(null, new Attribute("time_coverage_end", ndf.netcdfDate.format(tsEnd)));
              		
            nortek.close();
            
            ndf.dataFile.close();
            
            System.out.println("SUCCESS writing file " + filename);
            System.out.println("Memory " + Runtime.getRuntime().totalMemory() + " free " + Runtime.getRuntime().freeMemory());
            
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InvalidRangeException e)
        {
            e.printStackTrace();
        }
    }
}
