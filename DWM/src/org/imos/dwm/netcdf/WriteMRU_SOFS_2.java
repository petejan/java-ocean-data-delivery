package org.imos.dwm.netcdf;

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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.imos.dwm.mooring.MRU.WaveCalculator;
import org.imos.dwm.mooring.MRU.decode;
import org.imos.dwm.mooring.MRU.waveSpectra;
import org.imos.dwm.mooring.MRU.decode.mruRecord;
import org.imos.dwm.mooring.MRU.decode.mruStabQ;

public class WriteMRU_SOFS_2
{
    public static void main(String args[]) throws Exception
    {
        decode d = new decode();
        final int len = 35;
        d.setMsgs(0);

        final int NTIME = args.length;
        final int NSAMPLE = 3072;
        final int NSPEC = 256;

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // Create the file.
        File f = new File(args[0]);
        String filename = "SOFS-2-" + f.getName() + ".nc";
        NetcdfFileWriter dataFile = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        SimpleDateFormat sdfFile = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss");

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
        sdfFile.setTimeZone(TimeZone.getTimeZone("UTC"));
        try
        {
            ByteBuffer b = ByteBuffer.allocate(len);
            byte[] barray = new byte[len];
            b.order(ByteOrder.BIG_ENDIAN);

            // Create new netcdf-3 file with the given filename
            dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, filename);

            SimpleDateFormat netcdfDate = new SimpleDateFormat("yyyy-MM-DD'T'hh:mm:ss'Z'");
            netcdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));

            dataFile.addGroupAttribute(null, new Attribute("platform_code", "SOFS"));
            dataFile.addGroupAttribute(null, new Attribute("site", "SOUTHERN OCEAN FLUX STATION"));
            dataFile.addGroupAttribute(null, new Attribute("date_created", netcdfDate.format(new Date())));

            Dimension timeDim = dataFile.addDimension(null, "TIME", NTIME);
            Dimension sampleDim = dataFile.addDimension(null, "sample", NSAMPLE);
            Dimension specDim = dataFile.addDimension(null, "spectrum", NSPEC);
            Dimension vectorDim = dataFile.addDimension(null, "vector", 3);

            Variable vTime = dataFile.addVariable(null, "TIME", DataType.INT, "TIME");
            List<Dimension> vdims = new ArrayList<Dimension>();
            vdims.add(timeDim);
            vdims.add(sampleDim);
            vdims.add(vectorDim);
            Variable vSpecFreq = dataFile.addVariable(null, "frequency", DataType.FLOAT, "spectrum");
            Variable vSampleTime = dataFile.addVariable(null, "sampleT", DataType.FLOAT, "sample");
            vSpecFreq.addAttribute(new Attribute("units", "Hz"));
            vSampleTime.addAttribute(new Attribute("units", "s"));

            Variable vAccel = dataFile.addVariable(null, "Acceleration", DataType.FLOAT, vdims);
            Variable vMag = dataFile.addVariable(null, "Magnetic", DataType.FLOAT, vdims);
            Variable vAttitude = dataFile.addVariable(null, "Attitude", DataType.FLOAT, vdims);

            List<Dimension> dims = new ArrayList<Dimension>();
            dims.add(timeDim);
            dims.add(sampleDim);

            Variable vLoad = dataFile.addVariable(null, "load", DataType.FLOAT, dims);

            List<Dimension> dimSpec = new ArrayList<Dimension>();
            dimSpec.add(timeDim);
            dimSpec.add(specDim);

            Variable vSpec = dataFile.addVariable(null, "wave_spectra", DataType.FLOAT, dimSpec);
            Variable vSWH = dataFile.addVariable(null, "significant_wave_height", DataType.FLOAT, "TIME");

			// Define units attributes for coordinate vars. This attaches a
            // text attribute to each of the coordinate variables, containing
            // the units.
            vTime.addAttribute(new Attribute("units", "seconds since 2000-01-01T00:00:00Z"));
            vTime.addAttribute(new Attribute("axis", "T"));
            vTime.addAttribute(new Attribute("calendar", "gregorian"));

            // Define units attributes for variables.
            vAccel.addAttribute(new Attribute("units", "m/s/s"));
            vLoad.addAttribute(new Attribute("units", "kg"));
            vLoad.addAttribute(new Attribute("units", "Guass"));

            int[] vDim = new int[]
            {
                timeDim.getLength(), sampleDim.getLength(), 3
            };
            Array dataAccel = Array.factory(DataType.FLOAT, vDim);
            Array dataMag = Array.factory(DataType.FLOAT, vDim);
            Array dataAttitude = Array.factory(DataType.FLOAT, vDim);

            int[] iDim = new int[]
            {
                timeDim.getLength(), sampleDim.getLength()
            };
            Array dataLoad = Array.factory(DataType.FLOAT, iDim);

            int[] specDims = new int[]
            {
                timeDim.getLength(), specDim.getLength()
            };
            Array dataSpec = Array.factory(DataType.FLOAT, specDims);

            Array dataSWH = Array.factory(DataType.FLOAT, new int[]
            {
                timeDim.getLength()
            });

            // Write the coordinate variable data. 
            dataFile.create();
            Array dataTime = Array.factory(DataType.INT, new int[]
            {
                NTIME
            });
            long tz = sdf.parse("2000-01-01 00:00:00").getTime();
            SummaryStatistics loadStats = new SummaryStatistics();

            Date ts = null;

            for (int fileNo = 0; fileNo < args.length; fileNo++)
            {
                int i, j;

                f = new File(args[fileNo]);

                FileInputStream is = new FileInputStream(f);

                ts = sdfFile.parse(f.getName());
                dataTime.setInt(fileNo, (int) ((ts.getTime() - tz) / 1000));

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
                            float load = b.getFloat(31); // loadCell in mV
                            // loadcell 1827.894419 kg/V, 0.104 V offset = 190 kg
                            double dLoad = ((load * 1.8279) - 190);
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
                
                System.out.println("FILE " + args[fileNo] + " time " + ts + " wave height " + waveHeight + " load av " + loadStats.getMean());                
            }

            dataFile.write(vTime, dataTime);

            Array dataSpecFreq = Array.factory(DataType.FLOAT, new int[]
            {
                NSPEC
            });
            for (int i = 0; i < 256; i++)
            {
                dataSpecFreq.setFloat(i, (float) (i * WaveCalculator.DF));
            }

            dataFile.write(vSWH, dataSWH);
            dataFile.write(vSpecFreq, dataSpecFreq);
            dataFile.write(vSpec, dataSpec);

            Array dataSampleTime = Array.factory(DataType.FLOAT, new int[]
            {
                NSAMPLE
            });
            for (int i = 0; i < NSAMPLE; i++)
            {
                dataSampleTime.setFloat(i, (float) (i * 0.2)); // 200ms sample time
            }

            dataFile.write(vSampleTime, dataSampleTime);
            dataFile.write(vAccel, dataAccel);
            dataFile.write(vMag, dataMag);
            dataFile.write(vAttitude, dataAttitude);

            dataFile.write(vLoad, dataLoad);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InvalidRangeException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (null != dataFile)
            {
                try
                {
                    dataFile.close();
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }
        }
        System.out.println("SUCCESS writing file " + filename);
    }
}
