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
import org.imos.abos.parsers.RawAWCPdata;
import org.wiley.core.Common;

import sun.util.logging.resources.logging;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayShort;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

public class WriteNortek
{
    private static Logger log = Logger.getLogger(NortekParse.class);

    public static void main(String args[]) throws Exception
    {
        System.out.println("Memory " + Runtime.getRuntime().totalMemory() + " free " + Runtime.getRuntime().freeMemory());

        String $HOME = System.getProperty("user.home");
        PropertyConfigurator.configure("log4j.properties");
        Common.build($HOME + "/ABOS/ABOS.properties");
        
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
                
        String xmlFile = null;
        String mooring_id = "SOFS-5-2015";

        // run with something like
        //
        // java -Djna.library.path=/usr/lib64 -cp dist/ABOS.jar org.imos.abos.netcdf.WriteAWCP -x ~/ABOS/AWCP/SOFS-4-2014/201303/13032423.XML -m SOFS-4-2013 ~/ABOS/AWCP/SOFS-4-2014/201305
        
        Date tsStart = null;
        Date tsEnd = null;
        
        NortekParse nortek = new NortekParse();
        nortek.open(new File(args[0]));
		while(nortek.read() != -1) {}
		nortek.close();
        
		System.out.println("Memory " + Runtime.getRuntime().totalMemory() + " free " + Runtime.getRuntime().freeMemory());

		log.info("TS Size : " + nortek.ts.size() + " ts start " + nortek.ts.get(0));
		log.info("Pressure Size " + nortek.pressureData.size());
		log.info("IMU Size " + nortek.stabAccelData.size() + " data 0,0 " + ((float[][])nortek.stabAccelData.get(0))[0][0]);
		log.info("System Data " + nortek.sdTS.size() + " start " + nortek.sdTS.get(0));
        
        tsStart = nortek.ts.get(0);
        tsEnd = nortek.ts.get(nortek.ts.size()-1);
        
        Mooring m = Mooring.selectByMooringID(mooring_id);
        ArrayList<Instrument> insts = Instrument.selectInstrumentsForMooring(mooring_id); // Nortek Vector
        Instrument inst = Instrument.selectByInstrumentID(2410);
        for(Instrument ix : insts)
        {
    		log.debug("Instrument " + ix);
    		
        	if (ix.getSerialNumber().contains("8224"))
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
            ndf.setFacility("ABOS-ASFS");
            ndf.setMultiPart(true);
            
        	filename = ndf.getFileName(inst, dataStartTime, dataEndTime, "raw_instrument_data", "RVT");
        	
            // Create new netcdf-4 file with the given filename
            ndf.createFile(filename);

            ndf.writeGlobalAttributes();
            ndf.createCoordinateVariables(nortek.ts.size());  
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
            ndf.addGroupAttribute(null, new Attribute("history", ndf.netcdfDate.format(new Date()) + " File Created"));
            
            Dimension sdTimeDim = ndf.dataFile.addDimension(null, "TIME_SYSTEM_DATA", nortek.sdTS.size());
            
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

            ArrayDouble.D1 sdTimes = new ArrayDouble.D1(sdTimeDim.getLength());
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(ndf.tz);
            for (int i = 0; i < sdTimeDim.getLength(); i++)
            {
                Timestamp ts = nortek.sdTS.get(i);
                long offsetTime = (ts.getTime() - ndf.anchorTime) / 1000;
                double elapsedHours = ((double) offsetTime) / (3600 * 24);
                sdTimes.setDouble(i, elapsedHours);
            }

            // Battery voltage
            Variable vBattery = ndf.dataFile.addVariable(null, "BAT", DataType.FLOAT, sdDims);
            vBattery.addAttribute(new Attribute("units", "volt"));
            vBattery.addAttribute(new Attribute("long_name", "instrument_battery_voltage"));
            vBattery.addAttribute(new Attribute("name", "instrument battery voltage"));
            vBattery.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vBattery.addAttribute(new Attribute("valid_min", 0f));
            vBattery.addAttribute(new Attribute("valid_max", 20f));            

            ArrayFloat.D1 dataBattery = new ArrayFloat.D1(nortek.sdBattery.size());
            for(int i=0;i<nortek.sdTS.size();i++)
            {
            	dataBattery.set(i, (float)nortek.sdBattery.get(i));
            }
            
            // Temperature
            Variable vTemp = ndf.dataFile.addVariable(null, "TEMP", DataType.FLOAT, sdDims);
            vTemp.addAttribute(new Attribute("units", "degree_Celsius"));
            vTemp.addAttribute(new Attribute("long_name", "instrument_temperature"));
            vTemp.addAttribute(new Attribute("name", "instrument temperature"));
            vTemp.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vTemp.addAttribute(new Attribute("valid_min", 0f));
            vTemp.addAttribute(new Attribute("valid_max", 40f));            

            ArrayFloat.D1 dataTemp = new ArrayFloat.D1(nortek.sdTemp.size());
            for(int i=0;i<nortek.sdTS.size();i++)
            {
            	dataTemp.set(i, (float)nortek.sdTemp.get(i));
            }
                        
//            // Heading, Pitch, Roll
//            Variable vHead = ndf.dataFile.addVariable(null, "HEAD", DataType.FLOAT, sdDims);
//            vHead.addAttribute(new Attribute("units", "degree"));
//            vHead.addAttribute(new Attribute("long_name", "instrument_heading"));
//            vHead.addAttribute(new Attribute("name", "instrument heading"));
//            vHead.addAttribute(new Attribute("_FillValue", Float.NaN));            
//            vHead.addAttribute(new Attribute("valid_min", 0f));
//            vHead.addAttribute(new Attribute("valid_max", 360f));            
//
//            ArrayFloat.D1 dataHead = new ArrayFloat.D1(nortek.sdTemp.size());
//            for(int i=0;i<nortek.sdTS.size();i++)
//            {
//            	dataHead.set(i, (float)nortek.sdHead.get(i));
//            }
//            Variable vPitch = ndf.dataFile.addVariable(null, "PITCH", DataType.FLOAT, sdDims);
//            vPitch.addAttribute(new Attribute("units", "degree"));
//            vPitch.addAttribute(new Attribute("long_name", "instrument_pitch"));
//            vPitch.addAttribute(new Attribute("name", "instrument pitch"));
//            vPitch.addAttribute(new Attribute("_FillValue", Float.NaN));            
//            vPitch.addAttribute(new Attribute("valid_min", 0f));
//            vPitch.addAttribute(new Attribute("valid_max", 360f));            
//
//            ArrayFloat.D1 dataPitch = new ArrayFloat.D1(nortek.sdPitch.size());
//            for(int i=0;i<nortek.sdTS.size();i++)
//            {
//            	dataPitch.set(i, (float)nortek.sdPitch.get(i));
//            }
//            
//            Variable vRoll = ndf.dataFile.addVariable(null, "ROLL", DataType.FLOAT, sdDims);
//            vRoll.addAttribute(new Attribute("units", "degree"));
//            vRoll.addAttribute(new Attribute("long_name", "instrument_pitch"));
//            vRoll.addAttribute(new Attribute("name", "instrument pitch"));
//            vRoll.addAttribute(new Attribute("_FillValue", Float.NaN));            
//            vRoll.addAttribute(new Attribute("valid_min", 0f));
//            vRoll.addAttribute(new Attribute("valid_max", 360f));            
//
//            ArrayFloat.D1 dataRoll = new ArrayFloat.D1(nortek.sdRoll.size());
//            for(int i=0;i<nortek.sdTS.size();i++)
//            {
//            	dataRoll.set(i, (float)nortek.sdRoll.get(i));
//            }
//                        
            System.out.println(" timeDim " + ndf.timeDim.toString() + " " + ndf.timeDim.getLength());

            ndf.writeCoordinateVariables(nortek.ts);
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

            ArrayFloat.D2 dataPressure = new ArrayFloat.D2(nortek.ts.size(), nortek.NRecords);
            for(int i=0;i<nortek.pressureData.size();i++)
            {
        		float pressure[] = (float[])nortek.pressureData.get(i);
            	for (int j=0;j<nortek.NRecords;j++)
            	{
            		dataPressure.set(i, j, pressure[j]);
            	}
            }
            ArrayFloat.D2 dataAna1 = new ArrayFloat.D2(nortek.ts.size(), nortek.NRecords);
            for(int i=0;i<nortek.ana1Data.size();i++)
            {
        		float ana1[] = (float[])nortek.ana1Data.get(i);
            	for (int j=0;j<nortek.NRecords;j++)
            	{
            		dataAna1.set(i, j, ana1[j]);
            	}
            }
            ArrayFloat.D2 dataAna2 = new ArrayFloat.D2(nortek.ts.size(), nortek.NRecords);
            for(int i=0;i<nortek.ana2Data.size();i++)
            {
        		float ana2[] = (float[])nortek.ana2Data.get(i);
            	for (int j=0;j<nortek.NRecords;j++)
            	{
            		dataAna2.set(i, j, ana2[j]);
            	}
            }
            Variable aVel = ndf.dataFile.addVariable(null, "VELOCITY", DataType.FLOAT, vectorDims);
            aVel.addAttribute(new Attribute("units", "mm/s"));
            aVel.addAttribute(new Attribute("long_name", "water_velocity_vector"));
            aVel.addAttribute(new Attribute("name", "water velocity vector (B1,B2,B3)"));
            aVel.addAttribute(new Attribute("_FillValue", Float.NaN));            
            aVel.addAttribute(new Attribute("valid_min", -32768f));
            aVel.addAttribute(new Attribute("valid_max", 32767f));

            Variable aAmp = ndf.dataFile.addVariable(null, "ABSI", DataType.SHORT, vectorDims);
            aAmp.addAttribute(new Attribute("units", "counts"));
            aAmp.addAttribute(new Attribute("long_name", "amplitude"));
            aAmp.addAttribute(new Attribute("name", "amplitude (B1,B2,B3)"));
            aAmp.addAttribute(new Attribute("_FillValue", (short)-1));            
            aAmp.addAttribute(new Attribute("valid_min", (short)0));
            aAmp.addAttribute(new Attribute("valid_max", (short)255));
            
            Variable aCor = ndf.dataFile.addVariable(null, "CORR_MAG", DataType.SHORT, vectorDims);
            aCor.addAttribute(new Attribute("units", "%"));
            aCor.addAttribute(new Attribute("long_name", "correlation"));
            aCor.addAttribute(new Attribute("name", "correlation (B1,B2,B3)"));
            aCor.addAttribute(new Attribute("_FillValue", (short)-1));            
            aCor.addAttribute(new Attribute("valid_min", (short)0));
            aCor.addAttribute(new Attribute("valid_max", (short)100));
            
            ArrayFloat.D3 dataVel = new ArrayFloat.D3(nortek.ts.size(), nortek.NRecords, 3);
            for(int i=0;i<nortek.ts.size();i++)
            {
        		float vel[][] = (float[][])nortek.velData.get(i);
            	for (int j=0;j<nortek.NRecords;j++)
            	{
            		for(int k=0;k<3;k++)
            		{
            			dataVel.set(i, j, k, vel[j][k]);
            		}
            	}
            }
            ArrayShort.D3 dataAmp = new ArrayShort.D3(nortek.ts.size(), nortek.NRecords, 3);
            for(int i=0;i<nortek.ts.size();i++)
            {
        		int amp[][] = (int[][])nortek.ampData.get(i);
            	for (int j=0;j<nortek.NRecords;j++)
            	{
            		for(int k=0;k<3;k++)
            		{
            			dataAmp.set(i, j, k, (short)amp[j][k]);
            		}
            	}
            }
            ArrayShort.D3 dataCor = new ArrayShort.D3(nortek.ts.size(), nortek.NRecords, 3);
            for(int i=0;i<nortek.ts.size();i++)
            {
        		int cor[][] = (int[][])nortek.corData.get(i);
            	for (int j=0;j<nortek.NRecords;j++)
            	{
            		for(int k=0;k<3;k++)
            		{
            			dataCor.set(i, j, k, (short)cor[j][k]);
            		}
            	}
            }
            Variable aAccel = ndf.dataFile.addVariable(null, "IMU_ACCEL", DataType.FLOAT, vectorDims);
            aAccel.addAttribute(new Attribute("units", "g"));
            aAccel.addAttribute(new Attribute("long_name", "acceleration"));
            aAccel.addAttribute(new Attribute("name", "acceleration vector (X,Y,Z)"));
            aAccel.addAttribute(new Attribute("_FillValue", Float.NaN));            
            aAccel.addAttribute(new Attribute("valid_min", -10f));
            aAccel.addAttribute(new Attribute("valid_max", 10f));
            
            Variable aAngRate = ndf.dataFile.addVariable(null, "IMU_ANGULAR_RATE", DataType.FLOAT, vectorDims);
            aAngRate.addAttribute(new Attribute("units", "rad/sec"));
            aAngRate.addAttribute(new Attribute("long_name", "angular_rate"));
            aAngRate.addAttribute(new Attribute("name", "angular rate vector (X,Y,Z)"));
            aAngRate.addAttribute(new Attribute("_FillValue", Float.NaN));            
            aAngRate.addAttribute(new Attribute("valid_min", -100f));
            aAngRate.addAttribute(new Attribute("valid_max", 100f));

            Variable aMag = ndf.dataFile.addVariable(null, "IMU_MAGNETIC", DataType.FLOAT, vectorDims);
            aAngRate.addAttribute(new Attribute("units", "Gauss"));
            aAngRate.addAttribute(new Attribute("long_name", "magnetic_field_strength"));
            aAngRate.addAttribute(new Attribute("name", "magnetic field vector (X,Y,Z)"));
            aAngRate.addAttribute(new Attribute("_FillValue", Float.NaN));            
            aAngRate.addAttribute(new Attribute("valid_min", -100000f));
            aAngRate.addAttribute(new Attribute("valid_max", 100000f));

            ArrayFloat.D3 dataAccel = new ArrayFloat.D3(nortek.ts.size(), nortek.NRecords, 3);
            for(int i=0;i<nortek.velData.size();i++)
            {
        		float accel[][] = (float[][])nortek.stabAccelData.get(i);
            	for (int j=0;j<nortek.NRecords;j++)
            	{
            		for(int k=0;k<3;k++)
            		{
            			dataAccel.set(i, j, k, accel[j][k]);
            		}
            	}
            }
            ArrayFloat.D3 dataAngRate = new ArrayFloat.D3(nortek.ts.size(), nortek.NRecords, 3);
            for(int i=0;i<nortek.velData.size();i++)
            {
        		float rate[][] = (float[][])nortek.angRateData.get(i);
            	for (int j=0;j<nortek.NRecords;j++)
            	{
            		for(int k=0;k<3;k++)
            		{
            			dataAngRate.set(i, j, k, rate[j][k]);
            		}
            	}
            }
            ArrayFloat.D3 dataMag = new ArrayFloat.D3(nortek.ts.size(), nortek.NRecords, 3);
            for(int i=0;i<nortek.velData.size();i++)
            {
        		float mag[][] = (float[][])nortek.stabMagData.get(i);
            	for (int j=0;j<nortek.NRecords;j++)
            	{
            		for(int k=0;k<3;k++)
            		{
            			dataMag.set(i, j, k, mag[j][k]);
            		}
            	}
            }

            // Write the coordinate variable data. 
            ndf.create();

            ndf.dataFile.write(ndf.vTime, ndf.times);
            ndf.dataFile.write(vsdTime, sdTimes);

            ndf.writePosition(m.getLatitudeIn(), m.getLongitudeIn());            
            
            ndf.dataFile.write(vBattery, dataBattery);
            ndf.dataFile.write(vTemp, dataTemp);
            
//            ndf.dataFile.write(vHead, dataHead);
//            ndf.dataFile.write(vPitch, dataPitch);
//            ndf.dataFile.write(vRoll, dataRoll);
            
            ndf.dataFile.write(vPressure, dataPressure);
            ndf.dataFile.write(vAna1, dataAna1);
            ndf.dataFile.write(vAna2, dataAna2);

            ndf.dataFile.write(aVel, dataVel);
            ndf.dataFile.write(aAmp, dataAmp);
            ndf.dataFile.write(aCor, dataCor);
            ndf.dataFile.write(aAccel, dataAccel);
            ndf.dataFile.write(aAngRate, dataAngRate);
            ndf.dataFile.write(aMag, dataMag);
            
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
