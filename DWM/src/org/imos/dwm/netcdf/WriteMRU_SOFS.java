package org.imos.dwm.netcdf;

/* Write MRU data from binary packet to NetCDF file */
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
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
import org.imos.dwm.dbms.Instrument;
import org.imos.dwm.dbms.InstrumentCalibrationValue;
import org.imos.dwm.dbms.Mooring;
import org.imos.dwm.mooring.MRU.Parse3DMGX1File;
import org.imos.dwm.mooring.MRU.WaveCalculator;
import org.imos.dwm.mooring.MRU.decode;
import org.imos.dwm.mooring.MRU.waveSpectra;
import org.imos.dwm.mooring.MRU.Parse3DMGX1File.MruStabQ;
import org.imos.dwm.mooring.MRU.decode.mruRecord;
import org.imos.dwm.mooring.MRU.decode.mruStabQ;
import org.imos.dwm.parsers.NortekParse;
import org.wiley.core.Common;
import ucar.ma2.ArrayDouble;

public class WriteMRU_SOFS
{
    private static Logger log = Logger.getLogger(WriteMRU_SOFS.class);

    public static class ListFiles
    {
        ArrayList<File> endAll = new ArrayList<File>();
        
        public ArrayList<File> listFilez(File tree, String shapeName) throws IOException
        {            
        	if (tree.isFile())
        	{
        		endAll.add(tree.getCanonicalFile());
        	}
        	else
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
        
        boolean raw = false;
        
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

        double slope = Double.NaN;
        double offset = Double.NaN;
		if (instLoad != null)
		{
		log.info("load cell Instrument (" + instLoad.getInstrumentID() + ") " + instLoad);
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

        ArrayList<File> listOfFiles = new ArrayList<File>();
        for(int i=fileArg;i<args.length;i++)
        {
        	ArrayList <File> lfs = lf.listFilez(new File(args[i]), ".BIN"); 

        }
        for(File f : lf.endAll)
        {
        	log.debug("file : " + f );
        }
    	listOfFiles.addAll(lf.endAll);
        
        
        log.info("Files to process " + listOfFiles.size());
        NetCDFfile ndf = new NetCDFfile();
        ndf.setMooring(m);
        ndf.setAuthority("IMOS");
        ndf.setFacility(m.getFacility());
        
        Instrument inst = Instrument.selectByInstrumentID(2375);
        for(Instrument ix : insts)
        {
    		log.trace("Instrument " + ix);
    		
        	if (ix.getModel().contains("CR1000"))
        	{
        		inst = ix;        
        		break;
        	}
        }
		log.info("Instrument (" + inst.getInstrumentID() + ") " + inst);
		
        Instrument inst3DM = Instrument.selectByInstrumentID(1620);
        for(Instrument ix : insts)
        {
    		log.trace("Instrument " + ix);
    		
        	if (ix.getModel().contains("3DM"))
        	{
        		inst3DM = ix;        
        		break;
        	}
        }
		log.info("Instrument MRU (" + inst3DM.getInstrumentID() + ") " + inst3DM);
		
        Timestamp dataStartTime = m.getTimestampIn(); // TODO: probably should come from data, esp for part files
        Timestamp dataEndTime = m.getTimestampOut();        

        if (raw)
        {
        	filename = ndf.getFileName(inst3DM, dataStartTime, dataEndTime, "raw", "RW", null);
        }
        else
        {
        	filename = ndf.getFileName(inst3DM, dataStartTime, dataEndTime, "raw", "W", null);        	
        }
        
        boolean haveLoad = false;
        try
        {
            // Create new netcdf-4 file with the given filename
            ndf.createFile(filename);

            ndf.writeGlobalAttributes();
            ndf.createCoordinateVariables(listOfFiles.size());      
            ndf.addGroupAttribute(null, new Attribute("serial_number", inst3DM.getSerialNumber()));
            ndf.addGroupAttribute(null, new Attribute("featureType", "timeSeries"));
            ndf.addGroupAttribute(null, new Attribute("cdm_data_type", "Station"));
            ndf.addGroupAttribute(null, new Attribute("geospatial_vertical_min", 0.0));
            ndf.addGroupAttribute(null, new Attribute("geospatial_vertical_max", 0.0));
            ndf.addGroupAttribute(null, new Attribute("geospatial_vertical_positive", "down"));
            ndf.addGroupAttribute(null, new Attribute("instrument_nominal_depth", 0));
            ndf.addGroupAttribute(null, new Attribute("instrument", (inst3DM.getMake() + " ; " + inst3DM.getModel())));
            ndf.addGroupAttribute(null, new Attribute("instrument_serial_number", inst3DM.getSerialNumber()));
            ndf.addGroupAttribute(null, new Attribute("logger", (inst.getMake() + " " + inst.getModel())));
            ndf.addGroupAttribute(null, new Attribute("logger_serial_number", inst.getSerialNumber()));
            ndf.addGroupAttribute(null, new Attribute("file_version", "Level 0 - Raw data"));
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
                        
            Variable vNOM_D = ndf.dataFile.addVariable(null, "NOMINAL_DEPTH", DataType.DOUBLE, new ArrayList<Dimension>());
            vNOM_D.addAttribute(new Attribute("standard_name", "depth"));
            vNOM_D.addAttribute(new Attribute("long_name", "depth"));
            vNOM_D.addAttribute(new Attribute("description", "nominal depth of each sensor"));
            vNOM_D.addAttribute(new Attribute("units", "meters"));
            //vNOM_D.addAttribute(new Attribute("_FillValue", Float.NaN));
            vNOM_D.addAttribute(new Attribute("reference_datum", "Mean Sea Level (MSL)"));
            vNOM_D.addAttribute(new Attribute("axis", "Z"));
            vNOM_D.addAttribute(new Attribute("valid_min", 0.0));
            vNOM_D.addAttribute(new Attribute("valid_max", 5000.0));
            vNOM_D.addAttribute(new Attribute("positive", "down"));
       
            Dimension specDim = ndf.dataFile.addDimension(null, "FREQ", NSPEC);

            Dimension sampleDim = null;
            Dimension vectorDim = null;
            Dimension quetDim = null;
            List<Dimension> vdims = null;
            List<Dimension> qdims = null;
            if (raw)
            {
	            sampleDim = ndf.dataFile.addDimension(null, "sample_time", NSAMPLE);
	            vectorDim = ndf.dataFile.addDimension(null, "vector", 3);
	            quetDim = ndf.dataFile.addDimension(null, "quaternion", 4);

	            vdims = new ArrayList<Dimension>();
	            vdims.add(ndf.timeDim);
	            vdims.add(sampleDim);
	            vdims.add(vectorDim);
	
	            qdims = new ArrayList<Dimension>();
	            qdims.add(ndf.timeDim);
	            qdims.add(sampleDim);
	            qdims.add(quetDim);
            }
            
            Variable vSpecFreq = ndf.dataFile.addVariable(null, "FREQ", DataType.FLOAT, "FREQ");
            vSpecFreq.addAttribute(new Attribute("long_name", "spectral_frequency"));
            vSpecFreq.addAttribute(new Attribute("units", "Hz"));   
            //vSpecFreq.addAttribute(new Attribute("_FillValue", Float.NaN));

            Variable vSampleTime = null;
            Variable vAccel = null;
            Variable vMag = null;
            Variable vAttitude = null;
            Variable vVelocity = null;
            Variable vQuet = null;
            if (raw)
            {
	            vSampleTime = ndf.dataFile.addVariable(null, "sample_time", DataType.FLOAT, "sample_time");
	            vSampleTime.addAttribute(new Attribute("long_name", "time_of_sample"));
	            vSampleTime.addAttribute(new Attribute("units", "s"));
	            //vSampleTime.addAttribute(new Attribute("_FillValue", Float.NaN));
	
	            vAccel = ndf.dataFile.addVariable(null, "acceleration", DataType.FLOAT, vdims);
	            vAccel.addAttribute(new Attribute("long_name", "acceleration_vector_XYZ"));
	            vAccel.addAttribute(new Attribute("units", "m/s/s"));
	            vAccel.addAttribute(new Attribute("_FillValue", Float.NaN));
	            vAccel.addAttribute(new Attribute("coordinates", "TIME NOMINAL_DEPTH LATITUDE LONGITUDE"));
	            
	            vMag = ndf.dataFile.addVariable(null, "magnetic", DataType.FLOAT, vdims);
	            vMag.addAttribute(new Attribute("long_name", "magnetic_direction_XYZ"));
	            vMag.addAttribute(new Attribute("units", "Guass"));
	            vMag.addAttribute(new Attribute("_FillValue", Float.NaN));
	            vMag.addAttribute(new Attribute("coordinates", "TIME NOMINAL_DEPTH LATITUDE LONGITUDE"));
	            
	            vAttitude = ndf.dataFile.addVariable(null, "attitude", DataType.FLOAT, vdims);
	            vAttitude.addAttribute(new Attribute("long_name", "float_attitude_vector_HPR"));
	            vAttitude.addAttribute(new Attribute("units", "degrees"));
	            vAttitude.addAttribute(new Attribute("_FillValue", Float.NaN));
	            vAttitude.addAttribute(new Attribute("coordinates", "TIME NOMINAL_DEPTH LATITUDE LONGITUDE"));
	
	            vVelocity = ndf.dataFile.addVariable(null, "rotational_velocity", DataType.FLOAT, vdims);
	            vVelocity.addAttribute(new Attribute("long_name", "float_rotational_velocity"));
	            vVelocity.addAttribute(new Attribute("units", "deg/sec"));
	            vVelocity.addAttribute(new Attribute("_FillValue", Float.NaN));
	            vVelocity.addAttribute(new Attribute("coordinates", "TIME NOMINAL_DEPTH LATITUDE LONGITUDE"));
	
	            vQuet = ndf.dataFile.addVariable(null, "quaternion", DataType.FLOAT, qdims);
	            vQuet.addAttribute(new Attribute("long_name", "float_orientation_quaternion"));
	            vQuet.addAttribute(new Attribute("units", "1"));
	            vQuet.addAttribute(new Attribute("instrument", (inst3DM.getMake() + " " + inst3DM.getModel())));
	            vQuet.addAttribute(new Attribute("instrument_serial_number", inst3DM.getSerialNumber()));
	            vQuet.addAttribute(new Attribute("coordinates", "TIME NOMINAL_DEPTH LATITUDE LONGITUDE"));
	
	            vQuet.addAttribute(new Attribute("_FillValue", Float.NaN));
            }

            List<Dimension> dims = new ArrayList<Dimension>();
            dims.add(ndf.timeDim);
            dims.add(sampleDim);

            Variable vLoad = null;            		
            if ((instLoad != null) & (raw))
            {
	            vLoad = ndf.dataFile.addVariable(null, "load", DataType.FLOAT, dims);
	            vLoad.addAttribute(new Attribute("long_name", "mooring_wire_load"));
	            vLoad.addAttribute(new Attribute("units", "kg"));
	            vLoad.addAttribute(new Attribute("_FillValue", Float.NaN));
	            vLoad.addAttribute(new Attribute("coordinates", "TIME NOMINAL_DEPTH LATITUDE LONGITUDE"));

	            vLoad.addAttribute(new Attribute("instrument", (instLoad.getMake() + " " + instLoad.getModel())));
	            vLoad.addAttribute(new Attribute("instrument_serial_number", instLoad.getSerialNumber()));
            }

            List<Dimension> dimSpec = new ArrayList<Dimension>();
            dimSpec.add(ndf.timeDim);
            dimSpec.add(specDim);

            Variable vSpec = ndf.dataFile.addVariable(null, "NON_DIR_SPEC", DataType.FLOAT, dimSpec);
            vSpec.addAttribute(new Attribute("standard_name", "sea_surface_wave_variance_spectral_density"));
            vSpec.addAttribute(new Attribute("long_name", "sea_surface_wave_variance_spectral_density"));
            vSpec.addAttribute(new Attribute("units", "m^2/Hz"));
            vSpec.addAttribute(new Attribute("comment", "displacement (heave) spectra"));
            vSpec.addAttribute(new Attribute("_FillValue", Float.NaN));
            vSpec.addAttribute(new Attribute("coordinates", "TIME NOMINAL_DEPTH LATITUDE LONGITUDE"));

            Variable vSWH = ndf.dataFile.addVariable(null, "WAVE_HEIGHT_SIG", DataType.FLOAT, "TIME");
            vSWH.addAttribute(new Attribute("units", "metre"));
            vSWH.addAttribute(new Attribute("standard_name", "sea_surface_wave_significant_height"));
            vSWH.addAttribute(new Attribute("long_name", "sea_surface_wave_significant_height"));
            vSWH.addAttribute(new Attribute("_FillValue", Float.NaN));
            vSWH.addAttribute(new Attribute("ancillary_variables", "WAVE_HEIGHT_SIG_quality_control"));
            vSWH.addAttribute(new Attribute("coordinates", "TIME NOMINAL_DEPTH LATITUDE LONGITUDE"));

            Variable vCount = ndf.dataFile.addVariable(null, "count", DataType.INT, "TIME");
            vCount.addAttribute(new Attribute("units", "1"));
            vCount.addAttribute(new Attribute("long_name", "count of data points"));

            // add a QC variable, 0 = unknown, 7 = not deployed
            Variable vSWHqC = ndf.dataFile.addVariable(null, "WAVE_HEIGHT_SIG_quality_control", DataType.BYTE, "TIME");
            vSWHqC.addAttribute(new Attribute("standard_name", "sea_surface_wave_significant_height status_flag"));
            vSWHqC.addAttribute(new Attribute("long_name", "quality flag for sea_surface_wave_significant_height"));
            vSWHqC.addAttribute(new Attribute("_FillValue", (byte)-128));
            vSWHqC.addAttribute(new Attribute("quality_control_conventions", "IMOS standard flags"));
            vSWHqC.addAttribute(new Attribute("quality_control_set", 1.0));
            vSWHqC.addAttribute(new Attribute("flag_meanings", "unknown good_data probably_good_data probably_bad_data bad_data not_deployed missing_value"));
            
            
            ArrayDouble.D0 nDepth = new ArrayDouble.D0();
            nDepth.setDouble(0, 0.0);
            
            byte b;
            ArrayByte.D1 qcValues = new ArrayByte.D1(7);
			b = 0;
			qcValues.set(0, b);
			b = 1;
			qcValues.set(1, b);
			b = 2;
			qcValues.set(2, b);
			b = 3;
			qcValues.set(3, b);
			b = 4;
			qcValues.set(4, b);
			b = 7;
			qcValues.set(5, b);
			b = 9;
			qcValues.set(6, b);
			vSWHqC.addAttribute(new Attribute("flag_values", qcValues));
            
			int[] vDim = null;
			int[] qDim = null;
			int[] iDim = null;
			Array dataAccel = null;
			Array dataMag = null;
			Array dataAttitude = null;
			Array dataVelocity = null;
			Array dataQuet = null;
			Array dataLoad = null;
			
			if (raw)
			{
	            vDim = new int[]
	            {
	                ndf.timeDim.getLength(), sampleDim.getLength(), 3
	            };
	            qDim = new int[]
	            {
	                ndf.timeDim.getLength(), sampleDim.getLength(), 4
	            };
	            dataAccel = Array.factory(DataType.FLOAT, vDim);
	            dataMag = Array.factory(DataType.FLOAT, vDim);
	            dataAttitude = Array.factory(DataType.FLOAT, vDim);
	            dataVelocity = Array.factory(DataType.FLOAT, vDim);
	            dataQuet = Array.factory(DataType.FLOAT, qDim);
	            
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
	
	            iDim = new int[]
	            {
	                ndf.timeDim.getLength(), sampleDim.getLength()
	            };
	            dataLoad = Array.factory(DataType.FLOAT, iDim);
	            iter = dataLoad.getIndexIterator();
	            while (iter.hasNext()) 
	            {
	            	iter.setFloatNext(Float.NaN);
	            }
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
            Array dataCount = Array.factory(DataType.INT, new int[]
            {
                ndf.timeDim.getLength()
            });
            Array dataSWHqC = Array.factory(DataType.BYTE, new int[]
            {
                ndf.timeDim.getLength()
            });

            ArrayList<Timestamp> dataTime = new ArrayList<Timestamp>();
            SummaryStatistics loadStats = new SummaryStatistics();
            SummaryStatistics accelStats = new SummaryStatistics();

            Date ts = null;
            // Read the MRU file
            double[] zAccel = new double[NSAMPLE];

            Index3D qidx = null;
            Index3D vidx = null;
            Index2D idx = null;
            
            
            if (raw)
            {
	            qidx = new Index3D(qDim);
	            vidx = new Index3D(vDim);
	            idx = new Index2D(iDim);
            }

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
                
                int sample = 0;
                int firstTick = -1;
                for(int k=0;k<NSAMPLE;k++)
                {
                	zAccel[k] = -9.81;
                }


                pf.open(f);
                Object r;
                while ((r = pf.read()) != null)
				{
                	if (r instanceof String)
                	{
                		String rs = (String)r;
                		if (rs.contains("START RAW MRU DATA") && (mode >= 2))
                		{
                			ts = sdf.parse(rs);
                			if (ts.after(sdf.parse("2020-01-01 00:00:00"))) // Extream grudge to fix Pulse-11-2015 MRU timestamps
                			{
                				ts = new Timestamp(ts.getTime() - 810989523000l);
                			}
                            dataTime.add(new Timestamp(ts.getTime()));
                            
                            log.info("MRU file data " + ts);
                            
                            firstTick = -1;
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
                		if (firstTick == -1)
                		{
                			firstTick = stab.ticks;
                			sample = 0;
                            loadStats.clear();
                            accelStats.clear();
                		}
                		else
                		{
                			if (mode == 1)
                				sample++;
                			else
                				sample = stab.ticks - firstTick;
                		}
                		
                        zAccel[sample] = stab.accelWorld.z;
                        accelStats.addValue(stab.accelWorld.z);
                        
                		log.trace("MRU tick " + stab.ticks + " sample " + sample + " " + zAccel[sample]);                		

                        if (raw)
                        {
	                        vidx.set(fileNo, sample, 0);
	                        idx.set(fileNo, sample);
	                        
	                        log.trace("MRUstabQ sample " + sample + " idx " + idx);
	
	                        dataAccel.setFloat(vidx, (float) stab.accel.x);
	                        dataAttitude.setFloat(vidx, (float) stab.pry.x);
	                        dataVelocity.setFloat(vidx, (float) stab.gyro.x);
	                        dataMag.setFloat(vidx, (float) stab.mag.x);
	
	                        vidx.set(fileNo, sample, 1);
	                        dataAccel.setFloat(vidx, (float) stab.accel.y);
	                        dataAttitude.setFloat(vidx, (float) stab.pry.y);
	                        dataVelocity.setFloat(vidx, (float) stab.gyro.y);
	                        dataMag.setFloat(vidx, (float) stab.mag.y);
	
	                        vidx.set(fileNo, sample, 2);
	                        dataAccel.setFloat(vidx, (float) stab.accel.z);
	                        dataAttitude.setFloat(vidx, (float) stab.pry.z);
	                        dataVelocity.setFloat(vidx, (float) stab.gyro.z);
	                        dataMag.setFloat(vidx, (float) stab.mag.z);
	
	                        qidx.set(fileNo, sample, 0);
	                        dataQuet.setFloat(qidx, (float) stab.stab.Q1);                        
	                        qidx.set(fileNo, sample, 1);
	                        dataQuet.setFloat(qidx, (float) stab.stab.Q2);                        
	                        qidx.set(fileNo, sample, 2);
	                        dataQuet.setFloat(qidx, (float) stab.stab.Q3);                        
	                        qidx.set(fileNo, sample, 3);
	                        dataQuet.setFloat(qidx, (float) stab.stab.Q4);
                        }
                        
                        //sample++;             		
                	}
                	else if (r instanceof Float)
                	{
                		haveLoad = true;
                        double loadVolt = (Float)r; // loadCell recorded in mV
                        double dLoad = ((loadVolt/1000.0 - offset) * slope);
                        
                        log.trace("Load = " + dLoad);
                        log.trace("load sample " + sample + " idx " + idx + " load " + dLoad);
                        
                        if ((instLoad != null) & raw)  	
                        {
                            dataLoad.setFloat(idx, (float) dLoad);
                            loadStats.addValue(dLoad);
                        }
                	}					
				}
                log.debug("Last Sample " + sample);
                log.debug(accelStats);
                
                pf.close();
                
                double waveHeight = Double.NaN;
                
//        		double df = 2.5/256;
//        		add("df", "#0.###E0", df);
//        		add("log[0]", (b[0] / scale) + offset);
//        		for (int j = 1; j < 256; j++)
//        		{
//        			d = (b[j] / scale) + offset;
//        			double f = j * 2.5 / 256.0;
//        			double wds = Math.pow(10, d) / Math.pow(2 * Math.PI * f , 4) / df;
//        			add("wds["+j+"]", "#0.###E0", wds);
//        		}
                
                
                //if (sample >= NSAMPLE)
                {
                    waveSpectra ws = new waveSpectra();
                    double spec[] = ws.computeSpec(zAccel, true);

                    Index2D idxSpec = new Index2D(specDims);
                    idxSpec.set(fileNo, 0);
                    dataSpec.setFloat(idxSpec, Float.NaN);
                    
                    double df = WaveCalculator.DF;
                    double wds[] = new double[spec.length];
                    for (i = 1; i < spec.length; i++)
                    {
                        idxSpec.set(fileNo, i);
                        double freq = i * df;
                        wds[i] = (spec[i]/Math.pow(2 * Math.PI * freq , 4) / df);
                        dataSpec.setFloat(idxSpec, (float)wds[i] );
                    }

                    WaveCalculator wd = new WaveCalculator();
                    double[] logSpec = new double[spec.length];
                    for (i = 0; i < spec.length; i++)
                    {
                        // log.info((i * 5.0/512) + " spec " + spec[i]);
                        logSpec[i] = Math.log10(spec[i]);
                    }

                    waveHeight = wd.calculate(df, logSpec);
                    dataSWH.setFloat(fileNo, (float) waveHeight);
                    dataCount.setInt(fileNo, sample);

                    if (ts.before(dataStartTime) | ts.after(dataEndTime))
                    	b = 7;
                    else if (sample < 3000)
                    	b = 9;
                    else if ((wds[4] < 0.001) | (wds[4] > 30)) // energy outside this something wrong with IMU (0.0391, 25 seconds) 
                    										   // NOAA wave limit is 0.0325 (30 seconds)
                    	b = 4;
                    else
                    	b = 1;
                    dataSWHqC.setByte(fileNo, b);
                }

                
                log.info("FILE " + f);
                log.info("time " + sdf.format(ts) + " wave height " + String.format("%5.2f", waveHeight) + 
                		" load av " + String.format("%6.1f", loadStats.getMean()) + 
                		" load max " + String.format("%4.1f", loadStats.getMax()) + 
                		" load min " + String.format("%6.1f", loadStats.getMin()) + 
                		" count " + loadStats.getN()+ " qc "+ b);
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
            ndf.dataFile.write(vSWHqC, dataSWHqC);
            ndf.dataFile.write(vCount, dataCount);
            ndf.dataFile.write(vSpecFreq, dataSpecFreq);
            ndf.dataFile.write(vSpec, dataSpec);
            ndf.dataFile.write(vNOM_D, nDepth);

            if (raw)
            {
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
            }

            if ((instLoad != null) & raw)        	
            	ndf.dataFile.write(vLoad, dataLoad);

            log.info("SUCCESS writing file " + filename);
        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
        catch (InvalidRangeException e)
        {
            e.printStackTrace();
        }
    }
}
