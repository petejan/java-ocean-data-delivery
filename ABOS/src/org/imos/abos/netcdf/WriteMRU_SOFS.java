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
import ucar.ma2.InvalidRangeException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.util.Vector;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.dbms.InstrumentCalibrationValue;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.mooring.MRU.WaveCalculator;
import org.imos.abos.mooring.MRU.decode;
import org.imos.abos.mooring.MRU.waveSpectra;
import org.imos.abos.mooring.MRU.decode.mruRecord;
import org.imos.abos.mooring.MRU.decode.mruStabQ;
import org.wiley.core.Common;
import ucar.ma2.ArrayDouble;

public class WriteMRU_SOFS
{
    public static class ListFiles
    {
        ArrayList endAll = new ArrayList();
        
        public ArrayList listFilez(File tree, String shapeName) throws IOException
        {            
            File[] files = tree.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                String fileName = files[i].getName();
                if (files[i].isFile())
                {
                    if (fileName.endsWith(shapeName))
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
    
    public static void main(String args[]) throws Exception
    {
        String $HOME = System.getProperty("user.home");
        PropertyConfigurator.configure("log4j.properties");
        Common.build($HOME + "/ABOS/ABOS.properties");
        
        decode d = new decode();
        final int len = 35;
        d.setMsgs(0);

        final int NTIME = args.length;
        final int NSAMPLE = 3072;
        final int NSPEC = 256;

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Mooring m = Mooring.selectByMooringID(args[0]);

        // Create the file.
        String filename = m.getMooringID() + "-MRU.nc";
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        ArrayList<InstrumentCalibrationValue> v = InstrumentCalibrationValue.selectByInstrumentAndMooring(2248, m.getMooringID()); // load cell
        double slope = Double.NaN;
        double offset = Double.NaN;
        for(InstrumentCalibrationValue i : v)
        {
            if (i.getParameterCode().compareTo("OFFSET") == 0)
                offset = Double.parseDouble(i.getParameterValue());
            if (i.getParameterCode().compareTo("SLOPE") == 0)
                slope = Double.parseDouble(i.getParameterValue());
        }
        System.out.println("Calibration Slope " + slope + " offset " + offset);
        
        /*
         // global attributes:
         :project = "Integrated Marine Observing System" ;
         :conventions = "IMOS version 1.3" ;
         :title = "Heat and radiative flux data from Southern Ocean Flux Station" ;
         :institution = "Australian Bureau of Meteorology" ;
         :date_created = "2012-07-03T04:12:57Z" ;
         :abstract = "" ;
         :comment = "COARE Bulk Flux Algorithm version 3.0b (Fairall et \n",
         "al.,2003: J.Climate,16,571-591). Net heat flux does not include flux due to a rainfall (H_RAIN)." ;
         :source = "Mooring observation" ;
         :keywords = "Oceans>Ocean Temperature>Sea Surface \n",
         "Temperature,Oceans>Ocean Winds>Surface Winds,Atmosphere>Atmospheric \n",
         "Pressure>Atmospheric \n",
         "Pressure,Atmosphere>Precipitation>Precipitation Rate,Atmosphere>Atmospheric \n",
         "Water Vapor>Humidity,Atmosphere>Atmospheric Winds>Surface \n",
         "Winds,Atmosphere>Atmospheric Temperature>Air Temperature,Atmosphere>Atmospheric \n",
         "Radiation>Shortwave Radiation,Atmosphere>Atmospheric Radiation>Longwave \n",
         "Radiation,Atmosphere>Atmospheric Radiation>Net Radiation,Atmosphere>Atmospheric \n",
         "Radiation>Radiative Flux,Oceans>Ocean Heat Budget>Heat Flux,Oceans>Ocean \n",
         "Heat Budget>Longwave Radiation,Oceans>Ocean Heat Budget>Shortwave Radiation" ;
         :references = "http://www.imos.org.au" ;
         :netcdf_version = "3.6.1" ;
         :platform_code = "SOFS" ;
         :naming_authority = "IMOS" ;
         :cdm_data_type = "Trajectory" ;
         :geospatial_lat_min = "        -46.75780" ;
         :geospatial_lat_max = "        -46.69600" ;
         :geospatial_lon_min = "        141.98100" ;
         :geospatial_lon_max = "        142.10300" ;
         :geospatial_vertical_min = "0.0" ;
         :geospatial_vertical_max = "0.0" ;
         :time_coverage_start = "2010-03-17T11:59:00Z" ;
         :time_coverage_end = "2011-03-13T06:00:00Z" ;
         :data_centre = "eMII eMarine Information Infrastructure" ;
         :data_center_email = "info@emii.org.au" ;
         :author_email = "r.verein@bom.gov.au" ;
         :author = "Ruslan Verein" ;
         :principal_investigator = "Eric Schulz" ;
         :citation = "Citation to be used in publications should follow \n",
         "the format: \'IMOS.[year-of-data-download],[Title],[Data access URL],accessed \n",
         "[date-of access]\'" ;
         :acknowledgement = "Data was sourced from Integrated Marine \n",
         "Observing System (IMOS) - an initiative of the Australian Government being \n",
         "conducted as part of the National Calloborative Research Infrastructure \n",
         "Strategy." ;
         :distribution_statement = "ABOS data may be re-used, provided \n",
         "that related metadata explaining the data has been reviewed by the user, and the \n",
         "data is appropriately acknowledged. Data, products and services from IMOS are \n",
         "provided \'as is\' without any warranty as to fitness for a particular purpose." ;
         :file_version = "Level 2 - Derived product" ;
         :file_version_quality_control = "All data in this file has been \n",
         "through the BOM quality control procedure (Reference Table F) and was classed Z \n",
         "(passes all tests)" ;
         :site = "SOUTHERN OCEAN FLUX STATION" ;
         :WMO = "58450" ;
        
         Sensor Example: VWND
         long_name: northward component of wind speed (5180)
         units: meter second-1
         instrument: Gill Instruments Wind Observer II (s/n 213)
         observation_type: measured
         sensor_height: 3.52
         _FillValue: -9999.0
         standard_name: northward_wind
         ancillary_variables: VWND_quality_control

         */
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        ListFiles lf = new ListFiles();
        
        ArrayList listOfFiles = lf.listFilez(new File(args[1]), ".BIN");
        
        System.out.println("Files to process " + listOfFiles.size());
        NetCDFfile ndf = new NetCDFfile();
        
        try
        {
            ByteBuffer b = ByteBuffer.allocate(len);
            byte[] barray = new byte[len];
            b.order(ByteOrder.BIG_ENDIAN);

            // Create new netcdf-4 file with the given filename
            ndf.createFile(filename);
            ndf.setMooring(m);
            ndf.setAuthority("IMOS");
            ndf.setFacility("ABOS-ASFS");

            ndf.writeGlobalAttributes();
            ndf.createCoordinateVariables(listOfFiles.size());      
            
            Variable vLon = ndf.dataFile.addVariable(null, "XPOS", DataType.DOUBLE, "TIME");
            Variable vLat = ndf.dataFile.addVariable(null, "YPOS", DataType.DOUBLE, "TIME");
            vLat.addAttribute(new Attribute("standard_name", "latitude"));
            vLat.addAttribute(new Attribute("long_name", "latitude of float"));
            vLat.addAttribute(new Attribute("units", "degrees_north"));
            vLat.addAttribute(new Attribute("axis", "Y"));
            vLat.addAttribute(new Attribute("valid_min", -90.0));
            vLat.addAttribute(new Attribute("valid_max", 90.0));
            vLat.addAttribute(new Attribute("reference", "WGS84"));
            vLat.addAttribute(new Attribute("coordinate_reference_frame", "urn:ogc:crs:EPSG::4326"));

            vLon.addAttribute(new Attribute("standard_name", "longitude"));
            vLon.addAttribute(new Attribute("long_name", "longitude of float"));
            vLon.addAttribute(new Attribute("units", "degrees_east"));
            vLon.addAttribute(new Attribute("axis", "X"));
            vLon.addAttribute(new Attribute("valid_min", -180.0));
            vLon.addAttribute(new Attribute("valid_max", 180.0));
            vLon.addAttribute(new Attribute("reference", "WGS84"));
            vLon.addAttribute(new Attribute("coordinate_reference_frame", "urn:ogc:crs:EPSG::4326"));
            
            ArrayDouble.D1 lat = new ArrayDouble.D1(listOfFiles.size());
            ArrayDouble.D1 lon = new ArrayDouble.D1(listOfFiles.size());
            
            Dimension sampleDim = ndf.dataFile.addDimension(null, "sample", NSAMPLE);
            Dimension specDim = ndf.dataFile.addDimension(null, "spectrum", NSPEC);
            Dimension vectorDim = ndf.dataFile.addDimension(null, "vector", 3);

            List<Dimension> vdims = new ArrayList<Dimension>();
            vdims.add(ndf.timeDim);
            vdims.add(sampleDim);
            vdims.add(vectorDim);
            
            Variable vSpecFreq = ndf.dataFile.addVariable(null, "frequency", DataType.FLOAT, "spectrum");
            Variable vSampleTime = ndf.dataFile.addVariable(null, "sample_time", DataType.FLOAT, "sample");
            vSpecFreq.addAttribute(new Attribute("units", "Hz"));
            vSampleTime.addAttribute(new Attribute("units", "s"));

            Variable vAccel = ndf.dataFile.addVariable(null, "Acceleration", DataType.FLOAT, vdims);
            Variable vMag = ndf.dataFile.addVariable(null, "Magnetic", DataType.FLOAT, vdims);
            Variable vAttitude = ndf.dataFile.addVariable(null, "Attitude", DataType.FLOAT, vdims);

            List<Dimension> dims = new ArrayList<Dimension>();
            dims.add(ndf.timeDim);
            dims.add(sampleDim);

            Variable vLoad = ndf.dataFile.addVariable(null, "load", DataType.FLOAT, dims);

            List<Dimension> dimSpec = new ArrayList<Dimension>();
            dimSpec.add(ndf.timeDim);
            dimSpec.add(specDim);

            Variable vSpec = ndf.dataFile.addVariable(null, "wave_spectra", DataType.FLOAT, dimSpec);
            Variable vSWH = ndf.dataFile.addVariable(null, "significant_wave_height", DataType.FLOAT, "TIME");

            // Define units attributes for variables.
            vAccel.addAttribute(new Attribute("units", "m/s/s"));
            vLoad.addAttribute(new Attribute("units", "kg"));
            vMag.addAttribute(new Attribute("units", "Guass"));
            vAttitude.addAttribute(new Attribute("units", "degrees"));

            int[] vDim = new int[]
            {
                ndf.timeDim.getLength(), sampleDim.getLength(), 3
            };
            Array dataAccel = Array.factory(DataType.FLOAT, vDim);
            Array dataMag = Array.factory(DataType.FLOAT, vDim);
            Array dataAttitude = Array.factory(DataType.FLOAT, vDim);

            int[] iDim = new int[]
            {
                ndf.timeDim.getLength(), sampleDim.getLength()
            };
            Array dataLoad = Array.factory(DataType.FLOAT, iDim);

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

            for (int fileNo = 0; fileNo < listOfFiles.size(); fileNo++)
            {
                int i;

                File f = (File)listOfFiles.get(fileNo);
                System.out.println("Process file : " + f);

                FileInputStream is = new FileInputStream(f);
                int crlfCount = 0;
                int c;
                String s = "";
                while (crlfCount < 3)
                {
                    c = is.read();
                    if (c == 0x0a)
                    {
                        crlfCount++;
                    }
                    s += (char) c;
                }

                // System.out.println("line  " + s);
                ts = sdf.parse(s.substring(2, 22));
                dataTime.add(new Timestamp(ts.getTime()));

                // Read the MRU file
                Index3D vidx = new Index3D(vDim);
                Index2D idx = new Index2D(iDim);

                boolean eof = false;
                int sample = 0;
                mruRecord mru;
                double[] zAccel = new double[sampleDim.getLength()];

                loadStats.clear();
                while ((sample < NSAMPLE) && (!eof))
                {
                    int r = is.read(barray);
                    if (r < 0)
                    {
                        eof = true;
                    }
                    else
                    {
                        idx.set(fileNo, sample);

                        b.position(0);
                        b.put(barray);
                        b.position(0);
                        mru = d.read(b);

                        if (mru instanceof mruStabQ)
                        {
                            mruStabQ stab = (mruStabQ) mru;
                            zAccel[sample] = stab.accelWorld.z;

                            vidx.set(fileNo, sample, 0);
                            dataAccel.setFloat(vidx, (float) stab.accelWorld.x);
                            dataAttitude.setFloat(vidx, (float) stab.pry.x);
                            dataMag.setFloat(vidx, (float) stab.mag.x);

                            vidx.set(fileNo, sample, 1);
                            dataAccel.setFloat(vidx, (float) stab.accelWorld.y);
                            dataAttitude.setFloat(vidx, (float) stab.pry.y);
                            dataMag.setFloat(vidx, (float) stab.mag.y);

                            vidx.set(fileNo, sample, 2);
                            dataAccel.setFloat(vidx, (float) stab.accelWorld.z);
                            dataAttitude.setFloat(vidx, (float) stab.pry.z);
                            dataMag.setFloat(vidx, (float) stab.mag.z);
                        }

                        if (len > 31)
                        {
                            //dataLoad.setFloat(idx, b.getFloat(31));
                            double loadVolt = b.getFloat(31)/1000.0; // loadCell recorded in mV
                            double dLoad = ((loadVolt - offset) * slope);
                            loadStats.addValue(dLoad);
                            if (dLoad > 0)
                            {
                                dataLoad.setFloat(idx, (float) dLoad);
                            }
                            else
                            {
                                dataLoad.setFloat(idx, Float.NaN);
                            }
                            // System.out.println("load " + load + " = " + dLoad + " kg");
                        }
                        sample++;
                    }
                }

                is.close();
                double waveHeight = Double.NaN;
                if (sample >= NSAMPLE)
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
                        // System.out.println((i * 5.0/512) + " spec " + spec[i]);
                        logSpec[i] = Math.log10(spec[i]);
                    }

                    waveHeight = wd.calculate(WaveCalculator.DF, logSpec);
                    dataSWH.setFloat(fileNo, (float) waveHeight);
                }

                System.out.println("FILE " + f + " time " + sdf.format(ts) + " wave height " + waveHeight + " load av " + loadStats.getMean() + " load max " + loadStats.getMax());
            }

            ndf.writeCoordinateVariables(dataTime);
            ndf.writeCoordinateVariableAttributes();

            // Write the coordinate variable data. 
            ndf.dataFile.create();
            
            ndf.writePosition(m.getLatitudeIn(), m.getLongitudeIn());            
            
            //lat.set(0, latitudeIn);
            //lon.set(0, longitudeOut);

            ndf.dataFile.write(vLat, lat);
            ndf.dataFile.write(vLon, lon);

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

            ndf.dataFile.write(vLoad, dataLoad);

            System.out.println("SUCCESS writing file " + filename);
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
