package org.imos.abos.netcdf;

/* Write MRU data from binary packet to NetCDF file */
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index2D;
import ucar.ma2.Index3D;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.util.Vector;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentCalibrationValue;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.mooring.MRU.Parse3DMGX1File;
import org.imos.abos.mooring.MRU.Parse3DMGX1File.MruStabQ;
import org.imos.abos.mooring.MRU.WaveCalculator;
import org.imos.abos.mooring.MRU.decode;
import org.imos.abos.mooring.MRU.waveSpectra;
import org.imos.abos.mooring.MRU.decode.mruRecord;
import org.imos.abos.mooring.MRU.decode.mruStabQ;
import org.imos.abos.parsers.NortekParse;
import org.wiley.core.Common;
import ucar.ma2.ArrayDouble;

public class WriteMRU_SOFS
{
    private static Logger log = Logger.getLogger(WriteMRU_SOFS.class);

    public static class ListFiles
    {
        ArrayList endAll = new ArrayList();
        
        public ArrayList listFilez(File tree, String shapeName) throws IOException
        {            
            File[] files = tree.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                String fileName = files[i].getName().toLowerCase();
                if (files[i].isFile())
                {
                    if (fileName.endsWith(shapeName.toLowerCase()))
                    {
                        endAll.add(files[i]);
                    }
                }
                if (files[i].isDirectory())
                {
                    listFilez(files[i], shapeName);
                }
            }
            return endAll;
        }
    }
    
    // for SOFS-1 only
    static HashMap<Integer, Date> dataTs;

    public static void readDataFile(String file) throws IOException
    {
        String thisLine;
        String token;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        int i;
        int n = -1;
        Date ts = null;
        dataTs = new HashMap<Integer, Date>();

        File f = new File(file);

        FileReader is = new FileReader(f);

        BufferedReader br = new BufferedReader(is);

        while ((thisLine = br.readLine()) != null)
        {
            //System.out.println(thisLine);

            String[] st = thisLine.split(",");
            try
            {
	            n = Integer.parseInt(st[0]);
	            ts = sdf.parse(st[1]);            
            }
            catch (ParseException pe)
            {
                // ignore
                //pe.printStackTrace();
            }
            catch (NumberFormatException ne)
            {
                // ignore
                //ne.printStackTrace();
            }
            if (n > 0)
            {
                dataTs.put(n, ts);

                log.debug("SOFS-1 n " + n + " ts " + sdf.format(ts));
            }
        }

        br.close();
    }

    
    public static void main(String args[]) throws Exception
    {
        final TimeZone tz = TimeZone.getTimeZone("UTC");

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat netcdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        
        netcdfDate.setTimeZone(tz);
        
        String $HOME = System.getProperty("user.home");
        PropertyConfigurator.configure("log4j.properties");
        Common.build("ABOS.properties");
        
        decode d = new decode();
        final int len = 35;
        d.setMsgs(1);

        final int NTIME = args.length;
        final int NSAMPLE = 3072;
        final int NSPEC = 256;

        Mooring m = Mooring.selectByMooringID(args[0]);

        // Create the file.
        String filename = m.getMooringID() + "-MRU.nc";
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<Instrument> insts = Instrument.selectInstrumentsForMooring(m.getMooringID()); 
        Instrument instLoad = null;
        for(Instrument ix : insts)
        {
    		log.debug("Instrument " + ix);
    		
        	if (ix.getModel().contains("Load Cell"))
        	{
        		instLoad = ix;        
        		break;
        	}
        }
		log.info("load cell instrument " + instLoad);
        
        double slope = Double.NaN;
        double offset = Double.NaN;
		if (instLoad != null)
		{
	        ArrayList<InstrumentCalibrationValue> v = InstrumentCalibrationValue.selectByInstrumentAndMooring(instLoad.getInstrumentID(), m.getMooringID()); // load cell
	        
	        for(InstrumentCalibrationValue i : v)
	        {
	            if (i.getParameterCode().compareTo("OFFSET") == 0)
	                offset = Double.parseDouble(i.getParameterValue());
	            if (i.getParameterCode().compareTo("SLOPE") == 0)
	                slope = Double.parseDouble(i.getParameterValue());
	        }
	        log.info("Calibration Slope " + slope + " offset " + offset);
		}
        
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        int fileArg = 1;
        if (m.getMooringID().startsWith("SOFS-1-2010"))
        {
        	readDataFile(args[1]);
        	fileArg++;
        }
        ListFiles lf = new ListFiles();
        
        ArrayList listOfFiles = lf.listFilez(new File(args[fileArg]), ".BIN");
        
        log.info("Files to process " + listOfFiles.size());
        NetCDFfile ndf = new NetCDFfile();
        ndf.setMooring(m);
        ndf.setAuthority("IMOS");
        ndf.setFacility(m.getFacility());
        
        Instrument inst = Instrument.selectByInstrumentID(1620);
        for(Instrument ix : insts)
        {
    		log.trace("Instrument " + ix);
    		
        	if (ix.getModel().contains("3DM"))
        	{
        		inst = ix;        
        		break;
        	}
        }
		log.info("Instrument " + inst);
		
        Timestamp dataStartTime = m.getTimestampIn(); // TODO: probably should come from data, esp for part files
        Timestamp dataEndTime = m.getTimestampOut();        

        filename = ndf.getFileName(inst, dataStartTime, dataEndTime, "raw", "RW", null);
        
        boolean haveLoad = false;
        try
        {
            // Create new netcdf-4 file with the given filename
            ndf.createFile(filename);

            ndf.writeGlobalAttributes();
            ndf.createCoordinateVariables(listOfFiles.size());      
            ndf.addGroupAttribute(null, new Attribute("serial_number", inst.getSerialNumber()));
            ndf.addGroupAttribute(null, new Attribute("featureType", "timeSeries"));
            ndf.addGroupAttribute(null, new Attribute("cdm_data_type", "Station"));
            ndf.addGroupAttribute(null, new Attribute("geospatial_vertical_min", 0f));
            ndf.addGroupAttribute(null, new Attribute("geospatial_vertical_max", 0f));
            ndf.addGroupAttribute(null, new Attribute("geospatial_vertical_positive", "down"));
            ndf.addGroupAttribute(null, new Attribute("instrument_nominal_depth", 0f));
            ndf.addGroupAttribute(null, new Attribute("instrument", (inst.getMake() + " " + inst.getModel())));
            ndf.addGroupAttribute(null, new Attribute("instrument_serial_number", inst.getSerialNumber()));
            ndf.addGroupAttribute(null, new Attribute("file_version", "Level 0 – Raw data"));
            ndf.addGroupAttribute(null, new Attribute("history", ndf.netcdfDate.format(new Date()) + " File Created"));
            
            ndf.addGroupAttribute(null, new Attribute("time_deployment_start", netcdfDate.format(m.getTimestampIn())));
            ndf.addGroupAttribute(null, new Attribute("time_deployment_end", netcdfDate.format(m.getTimestampOut())));            
            
//            Variable vLon = ndf.dataFile.addVariable(null, "XPOS", DataType.DOUBLE, "TIME");
//            Variable vLat = ndf.dataFile.addVariable(null, "YPOS", DataType.DOUBLE, "TIME");
//            vLat.addAttribute(new Attribute("standard_name", "latitude"));
//            vLat.addAttribute(new Attribute("long_name", "latitude of float"));
//            vLat.addAttribute(new Attribute("units", "degrees_north"));
//            vLat.addAttribute(new Attribute("axis", "Y"));
//            vLat.addAttribute(new Attribute("valid_min", -90.0));
//            vLat.addAttribute(new Attribute("valid_max", 90.0));
//            vLat.addAttribute(new Attribute("reference", "WGS84"));
//            vLat.addAttribute(new Attribute("coordinate_reference_frame", "urn:ogc:crs:EPSG::4326"));
//
//            vLon.addAttribute(new Attribute("standard_name", "longitude"));
//            vLon.addAttribute(new Attribute("long_name", "longitude of float"));
//            vLon.addAttribute(new Attribute("units", "degrees_east"));
//            vLon.addAttribute(new Attribute("axis", "X"));
//            vLon.addAttribute(new Attribute("valid_min", -180.0));
//            vLon.addAttribute(new Attribute("valid_max", 180.0));
//            vLon.addAttribute(new Attribute("reference", "WGS84"));
//            vLon.addAttribute(new Attribute("coordinate_reference_frame", "urn:ogc:crs:EPSG::4326"));
//            
//            ArrayDouble.D1 lat = new ArrayDouble.D1(listOfFiles.size());
//            ArrayDouble.D1 lon = new ArrayDouble.D1(listOfFiles.size());
            
            Dimension sampleDim = ndf.dataFile.addDimension(null, "sample", NSAMPLE);
            Dimension specDim = ndf.dataFile.addDimension(null, "spectrum", NSPEC);
            Dimension vectorDim = ndf.dataFile.addDimension(null, "vector", 3);
            Dimension quetDim = ndf.dataFile.addDimension(null, "quaternion", 4);

            List<Dimension> vdims = new ArrayList<Dimension>();
            vdims.add(ndf.timeDim);
            vdims.add(sampleDim);
            vdims.add(vectorDim);

            List<Dimension> qdims = new ArrayList<Dimension>();
            vdims.add(ndf.timeDim);
            vdims.add(sampleDim);
            vdims.add(quetDim);
            
            Variable vSpecFreq = ndf.dataFile.addVariable(null, "frequency", DataType.FLOAT, "spectrum");
            vSpecFreq.addAttribute(new Attribute("long_name", "spectral_frequency"));
            vSpecFreq.addAttribute(new Attribute("units", "Hz"));   
            vSpecFreq.addAttribute(new Attribute("_FillValue", Float.NaN));

            Variable vSampleTime = ndf.dataFile.addVariable(null, "sample_time", DataType.FLOAT, "sample");
            vSampleTime.addAttribute(new Attribute("long_name", "time_of_sample"));
            vSampleTime.addAttribute(new Attribute("units", "s"));
            vSampleTime.addAttribute(new Attribute("_FillValue", Float.NaN));

            Variable vAccel = ndf.dataFile.addVariable(null, "acceleration", DataType.FLOAT, vdims);
            vAccel.addAttribute(new Attribute("long_name", "acceleration_vector_XYZ"));
            vAccel.addAttribute(new Attribute("units", "m/s/s"));
            vAccel.addAttribute(new Attribute("_FillValue", Float.NaN));
            
            Variable vMag = ndf.dataFile.addVariable(null, "magnetic", DataType.FLOAT, vdims);
            vMag.addAttribute(new Attribute("long_name", "magnetic_direction_XYZ"));
            vMag.addAttribute(new Attribute("units", "Guass"));
            vMag.addAttribute(new Attribute("_FillValue", Float.NaN));
            
            Variable vAttitude = ndf.dataFile.addVariable(null, "attitude", DataType.FLOAT, vdims);
            vAttitude.addAttribute(new Attribute("long_name", "float_attitude_vector_HPR"));
            vAttitude.addAttribute(new Attribute("units", "degrees"));
            vAttitude.addAttribute(new Attribute("_FillValue", Float.NaN));

            Variable vVelocity = ndf.dataFile.addVariable(null, "rotational_velocity", DataType.FLOAT, vdims);
            vVelocity.addAttribute(new Attribute("long_name", "float_rotational_velocity"));
            vVelocity.addAttribute(new Attribute("units", "deg/sec"));
            vVelocity.addAttribute(new Attribute("_FillValue", Float.NaN));

            Variable vQuet = ndf.dataFile.addVariable(null, "quaternion", DataType.FLOAT, vdims);
            vQuet.addAttribute(new Attribute("long_name", "float_orientation_quaternion"));
            vQuet.addAttribute(new Attribute("units", "1"));
            vQuet.addAttribute(new Attribute("_FillValue", Float.NaN));

            List<Dimension> dims = new ArrayList<Dimension>();
            dims.add(ndf.timeDim);
            dims.add(sampleDim);

            Variable vLoad = null;            		
            if (instLoad != null)
            {
	            vLoad = ndf.dataFile.addVariable(null, "load", DataType.FLOAT, dims);
	            vLoad.addAttribute(new Attribute("long_name", "mooring_wire_load"));
	            vLoad.addAttribute(new Attribute("units", "kg"));
	            vLoad.addAttribute(new Attribute("_FillValue", Float.NaN));
            }

            List<Dimension> dimSpec = new ArrayList<Dimension>();
            dimSpec.add(ndf.timeDim);
            dimSpec.add(specDim);

            Variable vSpec = ndf.dataFile.addVariable(null, "wave_spectra", DataType.FLOAT, dimSpec);
            vSpec.addAttribute(new Attribute("long_name", "wave_spectral_density"));
            vSpec.addAttribute(new Attribute("units", "m^2/Hz"));
            vSpec.addAttribute(new Attribute("_FillValue", Float.NaN));

            Variable vSWH = ndf.dataFile.addVariable(null, "SWH", DataType.FLOAT, "TIME");
            vSWH.addAttribute(new Attribute("units", "metre"));
            vSWH.addAttribute(new Attribute("standard_name", "sea_surface_wave_significant_height"));
            vSWH.addAttribute(new Attribute("_FillValue", Float.NaN));

            int[] vDim = new int[]
            {
                ndf.timeDim.getLength(), sampleDim.getLength(), 3
            };
            int[] qDim = new int[]
            {
                ndf.timeDim.getLength(), sampleDim.getLength(), 4
            };
            Array dataAccel = Array.factory(DataType.FLOAT, vDim);
            Array dataMag = Array.factory(DataType.FLOAT, vDim);
            Array dataAttitude = Array.factory(DataType.FLOAT, vDim);
            Array dataVelocity = Array.factory(DataType.FLOAT, vDim);
            Array dataQuet = Array.factory(DataType.FLOAT, qDim);
            IndexIterator iter = dataAccel.getIndexIterator();
            while (iter.hasNext()) 
            {
            	iter.setFloatNext(Float.NaN);
            }
            iter = dataMag.getIndexIterator();
            while (iter.hasNext()) 
            {
            	iter.setFloatNext(Float.NaN);
            }
            iter = dataAttitude.getIndexIterator();
            while (iter.hasNext()) 
            {
            	iter.setFloatNext(Float.NaN);
            }
            iter = dataVelocity.getIndexIterator();
            while (iter.hasNext()) 
            {
            	iter.setFloatNext(Float.NaN);
            }
            iter = dataQuet.getIndexIterator();
            while (iter.hasNext()) 
            {
            	iter.setFloatNext(Float.NaN);
            }

            int[] iDim = new int[]
            {
                ndf.timeDim.getLength(), sampleDim.getLength()
            };
            Array dataLoad = Array.factory(DataType.FLOAT, iDim);
            iter = dataLoad.getIndexIterator();
            while (iter.hasNext()) 
            {
            	iter.setFloatNext(Float.NaN);
            }
            
            int[] specDims = new int[]
            {
                ndf.timeDim.getLength(), specDim.getLength()
            };
            Array dataSpec = Array.factory(DataType.FLOAT, specDims);

            Array dataSWH = Array.factory(DataType.FLOAT, new int[]
            {
                ndf.timeDim.getLength()
            });

            ArrayList<Timestamp> dataTime = new ArrayList<Timestamp>();
            SummaryStatistics loadStats = new SummaryStatistics();

            Date ts = null;
            // Read the MRU file
            double[] zAccel = new double[sampleDim.getLength()];
            for(int k=0;k<sampleDim.getLength();k++)
            {
            	zAccel[k] = -9.81;
            }

            Index3D qidx = new Index3D(vDim);
            Index3D vidx = new Index3D(vDim);
            Index2D idx = new Index2D(iDim);

            Parse3DMGX1File pf = new Parse3DMGX1File();
            if (instLoad != null)
            {
            	pf.readLoad = true;
            }
            int mode = 3;
            if (m.getMooringID().startsWith("SOFS-1"))
            {
            	mode = 1;
            	pf.sofs1 = true;
            }
            else if (m.getMooringID().startsWith("SOFS-2"))
            {
            	mode = 2;
            }

            for (int fileNo = 0; fileNo < listOfFiles.size(); fileNo++)
            {
                int i;

                File f = (File)listOfFiles.get(fileNo);

                log.info("Process file : " + f);
                
                // SOFS-1 files
                if (mode == 1)
                {
	                int fNo = Integer.parseInt(f.getName().substring(4, 8));
	
	                ts = dataTs.get(fNo);  
	                dataTime.add(new Timestamp(ts.getTime()));
                }
                
                if (mode == 2)
                {
	                // SOFS-2 file names
	                SimpleDateFormat fndf = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss");
	                try
	                {
	                	ts = fndf.parse(f.getName());
	                	dataTime.add(new Timestamp(ts.getTime()));
	                }
	                catch (ParseException pe)
	                {
	                	
	                }
                }
                
                loadStats.clear();
                
                int sample = 0;

                pf.open(f);
                Object r;
                while ((r = pf.read()) != null)
				{
                	if (r instanceof String)
                	{
                		String rs = (String)r;
                		if (rs.contains("START RAW MRU DATA") && (mode >= 3))
                		{
                			ts = sdf.parse((String)r);
                			if (ts.after(sdf.parse("2020-01-01 00:00:00"))) // Extream grudge to fix Pulse-11-2015 MRU timestamps
                			{
                				ts = new Timestamp(ts.getTime() - 810989523000l);
                			}
                            dataTime.add(new Timestamp(ts.getTime()));
                            
                            log.info("MRU file data " + ts);
                		}        
                	}
                	else if ((r instanceof Date) && (mode >= 3))
                	{
                		ts = (Date)r;
                		
                		dataTime.add(new Timestamp(ts.getTime()));

                		log.info("MRU file data timestamp " + ts);
                	}
                	else if (r instanceof MruStabQ)
                	{
                		MruStabQ stab = (MruStabQ) r;
                        zAccel[sample] = stab.accelWorld.z;

                        vidx.set(fileNo, sample, 0);
                        idx.set(fileNo, sample);
                        
                        log.trace("MRUstabQ sample " + sample + " idx " + idx);

                        dataAccel.setFloat(vidx, (float) stab.accel.x);
                        dataAttitude.setFloat(vidx, (float) stab.pry.x);
                        dataVelocity.setFloat(vidx, (float) stab.pry.x);
                        dataMag.setFloat(vidx, (float) stab.mag.x);

                        vidx.set(fileNo, sample, 1);
                        dataAccel.setFloat(vidx, (float) stab.accel.y);
                        dataVelocity.setFloat(vidx, (float) stab.pry.y);
                        dataAttitude.setFloat(vidx, (float) stab.pry.y);
                       
                        dataMag.setFloat(vidx, (float) stab.mag.y);

                        vidx.set(fileNo, sample, 2);
                        dataAccel.setFloat(vidx, (float) stab.accel.z);
                        dataAttitude.setFloat(vidx, (float) stab.pry.z);
                        dataVelocity.setFloat(vidx, (float) stab.pry.z);
                        dataMag.setFloat(vidx, (float) stab.mag.z);

                        qidx.set(fileNo, sample, 0);
                        dataQuet.setFloat(qidx, (float) stab.stab.Q1);                        
                        qidx.set(fileNo, sample, 1);
                        dataQuet.setFloat(qidx, (float) stab.stab.Q2);                        
                        qidx.set(fileNo, sample, 2);
                        dataQuet.setFloat(qidx, (float) stab.stab.Q3);                        
                        qidx.set(fileNo, sample, 3);
                        dataQuet.setFloat(qidx, (float) stab.stab.Q4);                        
                        
                        sample++;             		
                	}
                	else if (r instanceof Float)
                	{
                		haveLoad = true;
                        double loadVolt = (Float)r; // loadCell recorded in mV
                        double dLoad = ((loadVolt/1000.0 - offset) * slope);
                        
                        log.trace("Load = " + dLoad);
                        log.trace("load sample " + sample + " idx " + idx + " load " + dLoad);
                        
                        //if (dLoad > 0)
                        {
                            dataLoad.setFloat(idx, (float) dLoad);
                            loadStats.addValue(dLoad);
                        }
                        //else
                        {
                            //dataLoad.setFloat(idx, Float.NaN);
                        }                		
                	}					
				}
                
                pf.close();
                
                double waveHeight = Double.NaN;
                //if (sample >= NSAMPLE)
                {
                    waveSpectra ws = new waveSpectra();
                    double spec[] = ws.computeSpec(zAccel, true);

                    Index2D idxSpec = new Index2D(specDims);
                    for (i = 0; i < spec.length; i++)
                    {
                        idxSpec.set(fileNo, i);
                        dataSpec.setFloat(idxSpec, (float) spec[i]);
                    }

                    WaveCalculator wd = new WaveCalculator();
                    double[] logSpec = new double[spec.length];
                    for (i = 0; i < spec.length; i++)
                    {
                        // log.info((i * 5.0/512) + " spec " + spec[i]);
                        logSpec[i] = Math.log10(spec[i]);
                    }

                    waveHeight = wd.calculate(WaveCalculator.DF, logSpec);
                    dataSWH.setFloat(fileNo, (float) waveHeight);
                }

                
                log.info("FILE " + f);
                log.info("time " + sdf.format(ts) + " wave height " + String.format("%5.2f", waveHeight) + 
                		" load av " + String.format("%6.1f", loadStats.getMean()) + 
                		" load max " + String.format("%4.1f", loadStats.getMax()) + 
                		" load min " + String.format("%6.1f", loadStats.getMin()) + 
                		" count " + loadStats.getN());
            }
            
            ndf.addGroupAttribute(null, new Attribute("time_coverage_start", ndf.netcdfDate.format(dataTime.get(0))));
            ndf.addGroupAttribute(null, new Attribute("time_coverage_end", ndf.netcdfDate.format(dataTime.get(dataTime.size()-1))));

            ndf.writeCoordinateVariables(dataTime);
            ndf.writeCoordinateVariableAttributes();

            // Write the coordinate variable data. 
            ndf.create();
            
            ndf.writePosition(m.getLatitudeIn(), m.getLongitudeIn());            
            
            //lat.set(0, latitudeIn);
            //lon.set(0, longitudeOut);

//            ndf.dataFile.write(vLat, lat);
//            ndf.dataFile.write(vLon, lon);

            ndf.dataFile.write(ndf.vTime, ndf.times);

            Array dataSpecFreq = Array.factory(DataType.FLOAT, new int[]
            {
                NSPEC
            });
            for (int i = 0; i < 256; i++)
            {
                dataSpecFreq.setFloat(i, (float) (i * WaveCalculator.DF));
            }

            ndf.dataFile.write(vSWH, dataSWH);
            ndf.dataFile.write(vSpecFreq, dataSpecFreq);
            ndf.dataFile.write(vSpec, dataSpec);

            Array dataSampleTime = Array.factory(DataType.FLOAT, new int[]
            {
                NSAMPLE
            });
            for (int i = 0; i < NSAMPLE; i++)
            {
                dataSampleTime.setFloat(i, (float) (i * 0.2)); // 200ms sample time
            }

            ndf.dataFile.write(vSampleTime, dataSampleTime);
            ndf.dataFile.write(vAccel, dataAccel);
            ndf.dataFile.write(vMag, dataMag);
            ndf.dataFile.write(vAttitude, dataAttitude);
            ndf.dataFile.write(vVelocity, dataVelocity);
            ndf.dataFile.write(vQuet, dataQuet);

            if (instLoad != null)            	
            	ndf.dataFile.write(vLoad, dataLoad);

            log.info("SUCCESS writing file " + filename);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InvalidRangeException e)
        {
            e.printStackTrace();
        }
//        finally
//        {
//            if (null != ndf.dataFile)
//            {
//                try
//                {
//                    ndf.dataFile.close();
//                }
//                catch (IOException ioe)
//                {
//                    ioe.printStackTrace();
//                }
//            }
//        }
    }
}
