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
import org.imos.abos.parsers.NortekParse.AwacWaveData;
import org.imos.abos.parsers.NortekParse.HRProfileVelocityData;
import org.imos.abos.parsers.NortekParse.IMU;
import org.imos.abos.parsers.NortekParse.ProfileVelocityData;
import org.imos.abos.parsers.NortekParse.VectorSystemData;
import org.imos.abos.parsers.NortekParse.VectorVelocityData;
import org.imos.abos.parsers.NortekParse.VelocityDataHeader;
import org.imos.abos.parsers.NortekParse.WaveDataHeader;
import org.imos.abos.parsers.RawAZFPdata;
import org.wiley.core.Common;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayShort;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.Index2D;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

public class WriteNortekProfiler
{
    private static Logger log = Logger.getLogger(NortekParse.class);

    public static void main(String args[]) throws Exception
    {
        System.out.println("Memory " + Runtime.getRuntime().totalMemory() + " free " + Runtime.getRuntime().freeMemory());

        String $HOME = System.getProperty("user.home");
        PropertyConfigurator.configure("log4j.properties");
        Common.build("ABOS.properties");
        
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
                
        String mooring_id = "SOFS-5-2015";

        // run with something like
        //
        // java -Djna.library.path=/usr/lib64 -cp dist/ABOS.jar org.imos.abos.netcdf.WriteAZFP -x ~/ABOS/AZFP/SOFS-4-2014/201303/13032423.XML -m SOFS-4-2013 ~/ABOS/AZFP/SOFS-4-2014/201305
        
        Date tsStart = null;
        Date tsEnd = null;
        String srcFilename = args[0];
        
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
		
		System.out.println("Memory " + Runtime.getRuntime().totalMemory() + " free " + Runtime.getRuntime().freeMemory());

        tsStart = nortek.clockDeploy;
        tsEnd = new Date();
        
        Mooring m = Mooring.selectByMooringID(mooring_id);
        
        // Find the instrument
        ArrayList<Instrument> insts = Instrument.selectInstrumentsForMooring(mooring_id); 
        Instrument inst = Instrument.selectByInstrumentID(2410);
        for(Instrument ix : insts)
        {
    		log.debug("Instrument " + ix);
    		
        	if (ix.getSerialNumber().contains("11011"))
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
        boolean waveData = false;
        
        try
        {
            ndf.setMooring(m);
            ndf.setAuthority("IMOS");
            ndf.setFacility("ABOS-ASFS");
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
            ndf.addGroupAttribute(null, new Attribute("instrument", "Nortek Profiler"));
            ndf.addGroupAttribute(null, new Attribute("instrument_serial_numbe", nortek.serialNo));
            ndf.addGroupAttribute(null, new Attribute("file_version", "Level 0 – Raw data"));
            ndf.addGroupAttribute(null, new Attribute("original_file", srcFilename));
            ndf.addGroupAttribute(null, new Attribute("history", ndf.netcdfDate.format(new Date()) + " File Created"));
            
            for(Attribute a : nortek.attributes)
            {
            	ndf.addGroupAttribute(null, a);
            }

            System.out.println(" timeDim " + ndf.timeDim.toString() + " " + ndf.timeDim.getLength());

            ndf.vTime = ndf.dataFile.addVariable(null, "TIME", DataType.DOUBLE, "TIME");
            ndf.writeCoordinateVariableAttributes();

            // The main data
            Dimension cellDim = ndf.dataFile.addDimension(null, "CELL", nortek.NBins); // need to get from data
            Dimension vectorDim = ndf.dataFile.addDimension(null, "VECTOR", 3);
            Dimension waveDim = null;
            Dimension headDim = null;
            Dimension waveTimeDim = null;
            if ((nortek.Mode & 0x02) == 0x02)
            {
            	waveData = true;
            	waveTimeDim = ndf.dataFile.addDimension(null, "TIME_WAVE", 0, true, true, false);
            	waveDim = ndf.dataFile.addDimension(null, "WAVE", nortek.NSamp);
                headDim = ndf.dataFile.addDimension(null, "HEAD", 4);
            }

            List<Dimension> dataDims = new ArrayList<Dimension>();
            dataDims.add(ndf.timeDim);
            
            List<Dimension> vectorDims = new ArrayList<Dimension>();
            vectorDims.add(ndf.timeDim);
            vectorDims.add(cellDim);
            vectorDims.add(vectorDim);

            List<Dimension> waveDims = new ArrayList<Dimension>();
            List<Dimension> wavePDims = new ArrayList<Dimension>();
            if (waveData)
            {
            	waveDims.add(waveTimeDim);
            	waveDims.add(waveDim);
            	waveDims.add(headDim);
            	
            	wavePDims.add(waveTimeDim);
            	wavePDims.add(waveDim);
            }

            // Battery voltage
            Variable vBattery = ndf.dataFile.addVariable(null, "BAT", DataType.FLOAT, dataDims);
            vBattery.addAttribute(new Attribute("units", "volt"));
            vBattery.addAttribute(new Attribute("long_name", "instrument_battery_voltage"));
            vBattery.addAttribute(new Attribute("name", "instrument battery voltage"));
            vBattery.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vBattery.addAttribute(new Attribute("valid_min", 0f));
            vBattery.addAttribute(new Attribute("valid_max", 20f));            

            // Temperature
            Variable vTemp = ndf.dataFile.addVariable(null, "TEMP", DataType.FLOAT, dataDims);
            vTemp.addAttribute(new Attribute("units", "degree_Celsius"));
            vTemp.addAttribute(new Attribute("long_name", "instrument_temperature"));
            vTemp.addAttribute(new Attribute("name", "instrument temperature"));
            vTemp.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vTemp.addAttribute(new Attribute("valid_min", 0f));
            vTemp.addAttribute(new Attribute("valid_max", 40f));            

            // Heading, Pitch, Roll
            Variable vHead = ndf.dataFile.addVariable(null, "HEAD", DataType.FLOAT, dataDims);
            vHead.addAttribute(new Attribute("units", "degree"));
            vHead.addAttribute(new Attribute("long_name", "instrument_heading"));
            vHead.addAttribute(new Attribute("name", "instrument heading"));
            vHead.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vHead.addAttribute(new Attribute("valid_min", 0f));
            vHead.addAttribute(new Attribute("valid_max", 360f));            

            Variable vPitch = ndf.dataFile.addVariable(null, "PITCH", DataType.FLOAT, dataDims);
            vPitch.addAttribute(new Attribute("units", "degree"));
            vPitch.addAttribute(new Attribute("long_name", "instrument_pitch"));
            vPitch.addAttribute(new Attribute("name", "instrument pitch"));
            vPitch.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vPitch.addAttribute(new Attribute("valid_min", 0f));
            vPitch.addAttribute(new Attribute("valid_max", 360f));            
            
            Variable vRoll = ndf.dataFile.addVariable(null, "ROLL", DataType.FLOAT, dataDims);
            vRoll.addAttribute(new Attribute("units", "degree"));
            vRoll.addAttribute(new Attribute("long_name", "instrument_pitch"));
            vRoll.addAttribute(new Attribute("name", "instrument pitch"));
            vRoll.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vRoll.addAttribute(new Attribute("valid_min", 0f));
            vRoll.addAttribute(new Attribute("valid_max", 360f));            

            Variable vanaIn = ndf.dataFile.addVariable(null, "ANALOG_IN", DataType.FLOAT, dataDims);
            vanaIn.addAttribute(new Attribute("units", "volts"));
            vanaIn.addAttribute(new Attribute("long_name", "analogue_in"));
            vanaIn.addAttribute(new Attribute("name", "analogue input"));
            vanaIn.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vanaIn.addAttribute(new Attribute("valid_min", 0f));
            vanaIn.addAttribute(new Attribute("valid_max", 5f));            
                       
            Variable vPressure = ndf.dataFile.addVariable(null, "PRES", DataType.FLOAT, dataDims);
            vPressure.addAttribute(new Attribute("units", "dbar"));
            vPressure.addAttribute(new Attribute("long_name", "instrument_pressure"));
            vPressure.addAttribute(new Attribute("name", "instrument pressure"));
            vPressure.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vPressure.addAttribute(new Attribute("valid_min", 0f));
            vPressure.addAttribute(new Attribute("valid_max", 100f));
            
            Variable vAna1 = ndf.dataFile.addVariable(null, "ANALOG1", DataType.FLOAT, dataDims);
            vAna1.addAttribute(new Attribute("units", "counts"));
            vAna1.addAttribute(new Attribute("long_name", "analog_input_1"));
            vAna1.addAttribute(new Attribute("name", "analog input 1"));
            vAna1.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vAna1.addAttribute(new Attribute("valid_min", -32768f));
            vAna1.addAttribute(new Attribute("valid_max", 32768f));

            Variable vAna2 = ndf.dataFile.addVariable(null, "ANALOG2", DataType.FLOAT, dataDims);
            vAna2.addAttribute(new Attribute("units", "counts"));
            vAna2.addAttribute(new Attribute("long_name", "analog_input_1"));
            vAna2.addAttribute(new Attribute("name", "analog input 2"));
            vAna2.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vAna2.addAttribute(new Attribute("valid_min", -32768f));
            vAna2.addAttribute(new Attribute("valid_max", 32768f));

            Variable vVel = ndf.dataFile.addVariable(null, "VELOCITY", DataType.FLOAT, vectorDims);
            vVel.addAttribute(new Attribute("units", "mm/s"));
            vVel.addAttribute(new Attribute("long_name", "water_velocity_vector"));
            vVel.addAttribute(new Attribute("name", "water velocity vector (B1,B2,B3)"));
            vVel.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vVel.addAttribute(new Attribute("valid_min", -32768f));
            vVel.addAttribute(new Attribute("valid_max", 32767f));

            Variable vAmp = ndf.dataFile.addVariable(null, "ABSI", DataType.SHORT, vectorDims);
            vAmp.addAttribute(new Attribute("units", "counts"));
            vAmp.addAttribute(new Attribute("long_name", "amplitude"));
            vAmp.addAttribute(new Attribute("name", "amplitude (B1,B2,B3)"));
            vAmp.addAttribute(new Attribute("_FillValue", (short)-1));            
            vAmp.addAttribute(new Attribute("valid_min", (short)0));
            vAmp.addAttribute(new Attribute("valid_max", (short)255));
            
            Variable vCor = ndf.dataFile.addVariable(null, "CORR_MAG", DataType.SHORT, vectorDims);
            vCor.addAttribute(new Attribute("units", "%"));
            vCor.addAttribute(new Attribute("long_name", "correlation"));
            vCor.addAttribute(new Attribute("name", "correlation (B1,B2,B3)"));
            vCor.addAttribute(new Attribute("_FillValue", (short)-1));            
            vCor.addAttribute(new Attribute("valid_min", (short)0));
            vCor.addAttribute(new Attribute("valid_max", (short)100));
            
            Variable vWaveVel = null;
            Variable vWavePres = null;
            Variable vWtime = null;
       		if (waveData)
       		{
       			vWtime = ndf.dataFile.addVariable(null, "TIME_WAVE", DataType.DOUBLE, "TIME_WAVE");
       			vWtime.addAttribute(new Attribute("name", "time_wave"));
       			//vWtime.addAttribute(new Attribute("standard_name", "time"));
       			vWtime.addAttribute(new Attribute("long_name", "time of wave measurement"));
   	        	vWtime.addAttribute(new Attribute("units", "days since 1950-01-01T00:00:00 UTC"));
   	        	vWtime.addAttribute(new Attribute("axis", "T"));
   	        	vWtime.addAttribute(new Attribute("valid_min", 10957.0));
   	        	vWtime.addAttribute(new Attribute("valid_max", 54787.0));
   	        	vWtime.addAttribute(new Attribute("calendar", "gregorian"));
       			
       			vWaveVel = ndf.dataFile.addVariable(null, "WAVE_VEL", DataType.FLOAT, waveDims);
       			vWaveVel.addAttribute(new Attribute("units", "mm/s"));
       			vWaveVel.addAttribute(new Attribute("long_name", "water_wave_velocity"));
       			vWaveVel.addAttribute(new Attribute("name", "water velocity vector (B1,B2,B3,B4)"));
       			vWaveVel.addAttribute(new Attribute("_FillValue", Float.NaN));            
       			vWaveVel.addAttribute(new Attribute("valid_min", -32768f));
       			vWaveVel.addAttribute(new Attribute("valid_max", 32767f));

       			vWavePres = ndf.dataFile.addVariable(null, "WAVE_PRES", DataType.FLOAT, wavePDims);
       			vWavePres.addAttribute(new Attribute("units", "dbar"));
       			vWavePres.addAttribute(new Attribute("long_name", "wave_pressure_samples"));
       			vWavePres.addAttribute(new Attribute("name", "wave pressure sample"));
       			vWavePres.addAttribute(new Attribute("_FillValue", Float.NaN));            
       			vWavePres.addAttribute(new Attribute("valid_min", -32768f));
       			vWavePres.addAttribute(new Attribute("valid_max", 32767f));
       		}

            // Write the coordinate variable data. 
            ndf.create();
            ndf.writePosition(m.getLatitudeIn(), m.getLongitudeIn());            
            
            Object o = null;
            ArrayDouble.D1 times = new ArrayDouble.D1(1);
            ArrayDouble.D1 wTimes = new ArrayDouble.D1(1);
            
            // data variables
            ArrayFloat.D1 aBat = new ArrayFloat.D1(1);
            ArrayFloat.D1 aTemp = new ArrayFloat.D1(1);
            ArrayFloat.D1 aHead = new ArrayFloat.D1(1);
            ArrayFloat.D1 aPitch = new ArrayFloat.D1(1);
            ArrayFloat.D1 aRoll = new ArrayFloat.D1(1);
            ArrayFloat.D1 aanaIn = new ArrayFloat.D1(1);

            ArrayFloat.D1 aPressure = new ArrayFloat.D1(1);
            ArrayFloat.D1 aAna1 = new ArrayFloat.D1(1);
            ArrayFloat.D1 aAna2 = new ArrayFloat.D1(1);

            ArrayFloat.D3 aVelocity = new ArrayFloat.D3(1, nortek.NBins, 3);
            ArrayFloat.D3 aWaveVel = new ArrayFloat.D3(1, nortek.NSamp, 4);
            ArrayFloat.D2 aWavePres = new ArrayFloat.D2(1, nortek.NSamp);

            int[] time_origin = new int[] { 0 };
            int[] wTime_origin = new int[] { 0 };
            int[] vector_origin = new int[] { 0, 0, 0 };
            int[] wave_origin = new int[] { 0, 0, 0 };
            
            Timestamp ts;
            long offsetTime;
            double elapsedHours;
            int waveSample = 0;
            
    		tsStart = null;
    		tsEnd = null;
            while ((o = nortek.read()) != null)
            {
            	if (o instanceof HRProfileVelocityData)
            	{
            		HRProfileVelocityData vsd = (HRProfileVelocityData)o;
            		
            		//log.info("HRProfileVelocityData " + vsd);
            		
            		ts = vsd.ts;
            		
            		// Save start/end times for later
            		if (tsStart == null)
            			tsStart = ts;
            		tsEnd = ts;
            		
            		offsetTime = (ts.getTime() - ndf.anchorTime) / 1000;
            		elapsedHours = ((double) offsetTime) / (3600 * 24);
            		times.setDouble(0, elapsedHours);
            		
            		aBat.setFloat(0, vsd.battery);
            		aTemp.setFloat(0, vsd.temp);
            		aHead.setFloat(0, vsd.heading);
            		aPitch.setFloat(0, vsd.pitch);
            		aRoll.setFloat(0, vsd.roll);
            		aanaIn.setFloat(0, vsd.anaIn);
            		aAna1.setFloat(0, vsd.anaIn1);
            		aAna2.setFloat(0, vsd.anaIn2);
            		aPressure.setFloat(0, vsd.pressure);
            		
            		//log.debug("sd_time_origin " + sd_time_origin[0] + " " + sd_times);
            		ndf.dataFile.write(ndf.vTime, time_origin, times);
            		ndf.dataFile.write(vBattery, time_origin, aBat);
            		ndf.dataFile.write(vTemp, time_origin, aTemp);
            		ndf.dataFile.write(vHead, time_origin, aHead);
            		ndf.dataFile.write(vPitch, time_origin, aPitch);
            		ndf.dataFile.write(vRoll, time_origin, aRoll);
            		ndf.dataFile.write(vanaIn, time_origin, aanaIn);

            		ndf.dataFile.write(vAna1, time_origin, aAna1);
            		ndf.dataFile.write(vAna2, time_origin, aAna2);
            		ndf.dataFile.write(vPressure, time_origin, aPressure);
            		
        			for(int i = 0;i<vsd.cells;i++)
        			{
            			for(int j = 0;j<vsd.beams;j++)
            			{
	        				aVelocity.set(0, i, j, vsd.velocity[j][i]);
            			}
        			}
            		ndf.dataFile.write(vVel, vector_origin, aVelocity);

            		vector_origin[0]++; // Should be the same as time index above

            		time_origin[0]++;
            	}
            	if (o instanceof ProfileVelocityData)
            	{
            		ProfileVelocityData vsd = (ProfileVelocityData)o;
            		
            		//log.info("HRProfileVelocityData " + vsd);
            		
            		ts = vsd.ts;
            		
            		// Save start/end times for later
            		if (tsStart == null)
            			tsStart = ts;
            		tsEnd = ts;
            		
            		offsetTime = (ts.getTime() - ndf.anchorTime) / 1000;
            		elapsedHours = ((double) offsetTime) / (3600 * 24);
            		times.setDouble(0, elapsedHours);
            		
            		aBat.setFloat(0, vsd.battery);
            		aTemp.setFloat(0, vsd.temp);
            		aHead.setFloat(0, vsd.heading);
            		aPitch.setFloat(0, vsd.pitch);
            		aRoll.setFloat(0, vsd.roll);
            		aanaIn.setFloat(0, vsd.anaIn);
            		aAna1.setFloat(0, vsd.anaIn);
            		aAna2.setFloat(0, vsd.anaIn2);
            		aPressure.setFloat(0, vsd.pressure);
            		
            		//log.debug("sd_time_origin " + sd_time_origin[0] + " " + sd_times);
            		ndf.dataFile.write(ndf.vTime, time_origin, times);
            		ndf.dataFile.write(vBattery, time_origin, aBat);
            		ndf.dataFile.write(vTemp, time_origin, aTemp);
            		ndf.dataFile.write(vHead, time_origin, aHead);
            		ndf.dataFile.write(vPitch, time_origin, aPitch);
            		ndf.dataFile.write(vRoll, time_origin, aRoll);
            		ndf.dataFile.write(vanaIn, time_origin, aanaIn);

            		ndf.dataFile.write(vAna1, time_origin, aAna1);
            		ndf.dataFile.write(vAna2, time_origin, aAna2);
            		ndf.dataFile.write(vPressure, time_origin, aPressure);
            		
        			for(int i = 0;i<nortek.NBins;i++)
        			{
        				aVelocity.set(0, i, 0, vsd.velocityA[i]);
        				aVelocity.set(0, i, 1, vsd.velocityB[i]);        				
        				aVelocity.set(0, i, 2, vsd.velocityC[i]);        				
        			}
            		ndf.dataFile.write(vVel, vector_origin, aVelocity);

            		vector_origin[0]++; // Should be the same as time index above

            		time_origin[0]++;
            	}
            	if (waveData)
            	{
	            	if (o instanceof WaveDataHeader)
	            	{
	            		WaveDataHeader vsd = (WaveDataHeader)o;
	            		ts = vsd.ts;
	            		offsetTime = (ts.getTime() - ndf.anchorTime) / 1000;
	            		elapsedHours = ((double) offsetTime) / (3600 * 24);
	            		wTimes.setDouble(0, elapsedHours);
	            		ndf.dataFile.write(vWtime, wTime_origin, wTimes);
	
	            		waveSample = 0;
	            		wTime_origin[0]++;
	            	}
	            	if (o instanceof AwacWaveData)
	            	{
	            		//log.debug("Wave Sample " + waveSample);
	            		if (waveSample < nortek.NSamp)
	            		{
		            		AwacWaveData vsd = (AwacWaveData)o;
	
		            		aWaveVel.set(0, waveSample, 0, vsd.vel1);
		            		aWaveVel.set(0, waveSample, 1, vsd.vel2);
		            		aWaveVel.set(0, waveSample, 2, vsd.vel3);
		            		aWaveVel.set(0, waveSample, 3, vsd.vel4);
	        				
	        				aWavePres.set(0, waveSample, vsd.pressure);
		            		
	        				//log.debug("Wave Sample " + waveSample);
		            		waveSample++;
		            		if (waveSample == (nortek.NSamp-1))
		            		{
		            			log.debug("write wave sample " + waveSample);
		            			
			            		ndf.dataFile.write(vWaveVel, wave_origin, aWaveVel);
			            		ndf.dataFile.write(vWavePres, wave_origin, aWavePres);
	
			            		wave_origin[0]++; // Should be the same as time index above
		            		}
	            		}
	            	}
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
