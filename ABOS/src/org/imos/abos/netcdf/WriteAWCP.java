package org.imos.abos.netcdf;

/* Write MRU data from binary packet to NetCDF file */
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.parsers.RawAWCPdata;
import org.wiley.core.Common;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

public class WriteAWCP
{
    public static void main(String args[]) throws Exception
    {
        String $HOME = System.getProperty("user.home");
        PropertyConfigurator.configure("log4j.properties");
        Common.build($HOME + "/ABOS/ABOS.properties");
        
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
                
        String xmlFile = null;
        String mooring_id = "SOFS-4-2013";

        // run with something like
        //
        // java -Djna.library.path=/usr/lib64 -cp dist/ABOS.jar org.imos.abos.netcdf.WriteAWCP -x ~/ABOS/AWCP/SOFS-4-2014/201303/13032423.XML -m SOFS-4-2013 ~/ABOS/AWCP/SOFS-4-2014/201305
        
        ArrayList<File> listOfFiles = new ArrayList<File>();
        try
        {
            for (int i=0;i<args.length;i++)
            {
                String arg = args[i];
                if (arg.startsWith("-x"))
                {
                    xmlFile = args[++i];
                }
                else if (arg.startsWith("-m"))
                {
                    mooring_id = args[++i];
                }
                else
                {                
                    File f = new File(arg);

                    if (f.isFile())
                    {
                        listOfFiles.add(f);
                    }
                    else
                    {
                        System.out.println("directory " + f);
                        File[] files = f.listFiles(new FilenameFilter()
                        {
                            @Override
                            public boolean accept(File dir, String name)
                            {
                                return name.matches(".*0\\dA$"); //.endsWith(".01A");
                            }
                        });

                        for (File datfile : files)
                        {
                            //System.out.println("File : " + datfile);
                            listOfFiles.add(datfile);
                        }
                    }
                }
            }
            Collections.sort(listOfFiles, new Comparator<File>() {
                    @Override
                    public int compare(File  f1, File  f2)
                    {

                        return  f1.getName().compareTo(f2.getName());
                    }
                });            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }        
        
        long len = 0;
        
        Date tsStart = null;
        Date tsEnd = null;
        
        RawAWCPdata an = new RawAWCPdata(xmlFile);
        Mooring m = Mooring.selectByMooringID(mooring_id);
        SimpleDateFormat nameFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        nameFormatter.setTimeZone(TimeZone.getDefault());
        String deployment = m.getMooringID();
        String mooring = deployment.substring(0, deployment.indexOf("-"));
        
        Date dataStartTime = m.getTimestampIn(); // TODO: probably should come from data, esp for part files
        Date dataEndTime = m.getTimestampOut();

        for(File datfile : listOfFiles)
        {
            len += an.open(datfile);
            if ((tsStart == null) || (tsStart.after(an.tsStart)))
                    tsStart = an.tsStart;
            if ((tsEnd == null) || (tsEnd.before(an.tsEnd)))
                    tsEnd = an.tsEnd;                        
            an.close();            
        }
                
        String authority = "IMOS";
        String facility = m.getFacility();
        String filename = authority 
                            + "_" + facility + "_" 
                            + "RA_"
                            + nameFormatter.format(tsStart)
                            + "_" + mooring.toUpperCase() ;
                    
                filename    += "_FV00";
                filename    += "_" + deployment.toUpperCase() + "-AZFP-"+an.serialNo
                            + "_END-"
                            + nameFormatter.format(tsEnd)
                            + "_C-"               
                            + nameFormatter.format(System.currentTimeMillis())
                            + "_PART1"
                            + ".nc"
                            ;
                
        // hacks to get the next filename in a mulitpart set
        final String filter = authority 
                            + "_" + facility + "_" 
                            + "RA_"
                            + "(\\d{8}T\\d{6}Z)"
                            + "_" + mooring.toUpperCase() 
                            + "_FV00"
                            + "_" + deployment.toUpperCase() + "-AZFP-"+an.serialNo
                            + "_END-"
                            + "(\\d{8}T\\d{6}Z)"
                            + "_C-"               
                            + "\\d{8}T\\d{6}Z"
                            + "_(PART\\d)"
                            + "\\.nc";
        final Pattern pattern = Pattern.compile(filter);
        File fl = new File(".");
        File[] files = fl.listFiles(new FilenameFilter()
                        {
                            @Override
                            public boolean accept(File dir, String name)
                            {
//                                System.out.println("name " + name + " " + name.matches(filter));
                                return pattern.matcher(name).matches();
                            }
                        });
        
        int fileno = 1;
        for (File datfile : files)
        {
            filename = datfile.getName();
            System.out.println("Filtered files " + datfile);
            fileno++;
        }        
        Matcher mat = pattern.matcher(filename);
        mat.find();
        filename = filename.replaceAll(mat.group(1), nameFormatter.format(tsStart));
        filename = filename.replaceAll(mat.group(2), nameFormatter.format(tsEnd));
        filename = filename.replaceAll(mat.group(3), "PART"+fileno);
        System.out.println("Next filename " + filename);        
                
        //filename = "AWCP-NetCDF.nc";
        System.out.println("total records " + len + " tsStart " + tsStart + " tsEnd " + tsEnd);
        System.out.println(an.toString());
        
        NetCDFfile ndf = new NetCDFfile();
        
        try
        {
            // Create new netcdf-4 file with the given filename
            ndf.createFile(filename);
            ndf.setMooring(m);
            ndf.setAuthority("IMOS");
            ndf.setFacility("ABOS-ASFS");

            ndf.writeGlobalAttributes();
            ndf.createCoordinateVariables((int)len);  
            ndf.addGroupAttribute(null, new Attribute("serial_number", String.format("%d", an.serialNo)));
            ndf.addGroupAttribute(null, new Attribute("featureType", "timeSeriesProfile"));
            ndf.addGroupAttribute(null, new Attribute("cdm_data_type", "Profile"));
            ndf.addGroupAttribute(null, new Attribute("geospatial_vertical_min", 30f));
            ndf.addGroupAttribute(null, new Attribute("geospatial_vertical_max", 220f));
            ndf.addGroupAttribute(null, new Attribute("geospatial_vertical_positive", "down"));
            ndf.addGroupAttribute(null, new Attribute("time_coverage_start", ndf.netcdfDate.format(tsStart)));
            ndf.addGroupAttribute(null, new Attribute("time_coverage_end", ndf.netcdfDate.format(tsEnd)));
            ndf.addGroupAttribute(null, new Attribute("instrument_nominal_depth", 30f));
            ndf.addGroupAttribute(null, new Attribute("instrument", "ASL AZFP"));
            ndf.addGroupAttribute(null, new Attribute("instrument_serial_numbe", String.format("%d", an.serialNo)));
            ndf.addGroupAttribute(null, new Attribute("file_version", "Level 0 – Raw data"));
            ndf.addGroupAttribute(null, new Attribute("history", ndf.netcdfDate.format(new Date()) + " File Created"));
            
            List<Dimension> dims = new ArrayList<Dimension>();
            dims.add(ndf.timeDim);

            Variable vTemperature = ndf.dataFile.addVariable(null, "TEMP", DataType.FLOAT, dims);
            //Variable vPressure = ndf.dataFile.addVariable(null, "PRES", DataType.FLOAT, dims);
            Variable vBattery = ndf.dataFile.addVariable(null, "BAT", DataType.FLOAT, dims);
            Variable vTiltX = ndf.dataFile.addVariable(null, "TILTX", DataType.FLOAT, dims);
            Variable vTiltY = ndf.dataFile.addVariable(null, "TILTY", DataType.FLOAT, dims);

            vTemperature.addAttribute(new Attribute("units", "celsius"));
            vTemperature.addAttribute(new Attribute("long_name", "instrument_temperature"));
            vTemperature.addAttribute(new Attribute("name", "instrument temperature"));
            vTemperature.addAttribute(new Attribute("_FillValue", Float.NaN));
            vTemperature.addAttribute(new Attribute("valid_min", -10f));
            vTemperature.addAttribute(new Attribute("valid_max", 30f));
//            vTemperature.addAttribute(new Attribute("sensor_depth", 30f));
//            vTemperature.addAttribute(new Attribute("sensor_name", "ASL AZFP"));
//            vTemperature.addAttribute(new Attribute("sensor_serial_number", String.format("%d", an.serialNo)));
            
            //vPressure.addAttribute(new Attribute("units", "dbar"));
            vBattery.addAttribute(new Attribute("units", "volt"));
            vBattery.addAttribute(new Attribute("long_name", "instrument_battery_voltage"));
            vBattery.addAttribute(new Attribute("name", "instrument battery voltage"));
            vBattery.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vBattery.addAttribute(new Attribute("valid_min", 0f));
            vBattery.addAttribute(new Attribute("valid_max", 20f));
//            vBattery.addAttribute(new Attribute("sensor_depth", 30f));
//            vBattery.addAttribute(new Attribute("sensor_name", "ASL AZFP"));
//            vBattery.addAttribute(new Attribute("sensor_serial_number", String.format("%d", an.serialNo)));
            
            vTiltX.addAttribute(new Attribute("units", "degrees"));
            vTiltX.addAttribute(new Attribute("long_name", "instrument_tilt_x"));
            vTiltX.addAttribute(new Attribute("name", "instrument tilt X direction"));
            vTiltX.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vTiltX.addAttribute(new Attribute("valid_min", -180f));
            vTiltX.addAttribute(new Attribute("valid_max", 180f));
//            vTiltX.addAttribute(new Attribute("sensor_depth", 30f));
//            vTiltX.addAttribute(new Attribute("sensor_name", "ASL AZFP"));
//            vTiltX.addAttribute(new Attribute("sensor_serial_number", String.format("%d", an.serialNo)));
            
            vTiltY.addAttribute(new Attribute("units", "degrees"));
            vTiltY.addAttribute(new Attribute("long_name", "instrument_tilt_y"));
            vTiltY.addAttribute(new Attribute("name", "instrument tilt Y direction"));
            vTiltY.addAttribute(new Attribute("_FillValue", Float.NaN));            
            vTiltY.addAttribute(new Attribute("valid_min", -180f));
            vTiltY.addAttribute(new Attribute("valid_max", 180f));
//            vTiltY.addAttribute(new Attribute("sensor_depth", 30f));
//            vTiltY.addAttribute(new Attribute("sensor_name", "ASL AZFP"));
//            vTiltY.addAttribute(new Attribute("sensor_serial_number", String.format("%d", an.serialNo)));

            int[] iDim = new int[]
            {
                ndf.timeDim.getLength()
            };

            ArrayList<Timestamp> dataTime = new ArrayList<Timestamp>();
            Array dataTemperature = Array.factory(DataType.FLOAT, iDim);
            //Array dataPressure = Array.factory(DataType.FLOAT, iDim);
            Array dataBattery = Array.factory(DataType.FLOAT, iDim);
            Array dataTiltX = Array.factory(DataType.FLOAT, iDim);
            Array dataTiltY = Array.factory(DataType.FLOAT, iDim);
            
            Array dataABSI[] = new Array[4];
            Variable vABSI[] = new Variable[4];

            Variable distance[] = new Variable[4];
            Array dataDistance[] = new Array[4];
            
            for(int i=0;i<4;i++)
            {
                Dimension sampleDim1 = ndf.dataFile.addDimension(null, "depth_"+an.freq[i], an.bins[i]);
                
                List<Dimension> dimsd = new ArrayList<Dimension>();
                dimsd.add(sampleDim1);

                distance[i] = ndf.dataFile.addVariable(null, "depth_"+an.freq[i], DataType.FLOAT, dimsd);
                distance[i].addAttribute(new Attribute("sample_rate_sps", (float)an.rate[i]));            
                distance[i].addAttribute(new Attribute("frequency_kHz", (float)an.freq[i]));            
                distance[i].addAttribute(new Attribute("pulse_length_us", (float)an.pulseLen[i]));      
                
                distance[i].addAttribute(new Attribute("units", "m"));
                distance[i].addAttribute(new Attribute("standard_name", "depth"));
                distance[i].addAttribute(new Attribute("comment", "distance from instrument plus instrument depth"));
                distance[i].addAttribute(new Attribute("axis", "Z"));
                distance[i].addAttribute(new Attribute("positive", "down"));
                distance[i].addAttribute(new Attribute("_FillValue", Float.NaN));                
                distance[i].addAttribute(new Attribute("valid_min", 0f));
                distance[i].addAttribute(new Attribute("valid_max", 300f));
//                distance[i].addAttribute(new Attribute("sensor_depth", 30f));
//                distance[i].addAttribute(new Attribute("sensor_name", "ASL AZFP"));
//                distance[i].addAttribute(new Attribute("sensor_serial_number", String.format("%d", an.serialNo)));

                int[] iDimd = new int[]
                {
                    sampleDim1.getLength()
                };
                dataDistance[i] = Array.factory(DataType.FLOAT, iDimd);
                for(int j=0;j<sampleDim1.getLength();j++)
                {
                    dataDistance[i].setFloat(j, (float)(30.0 + an.sos * (j + 1) / an.rate[i] / 2));
                }
                
                List<Dimension> dims1 = new ArrayList<Dimension>();
                dims1.add(ndf.timeDim);
                dims1.add(sampleDim1);

                System.out.println("Creating " + "ABSI_"+i + " memory " + Runtime.getRuntime().totalMemory() + " free " + Runtime.getRuntime().freeMemory());
                
                vABSI[i] = ndf.dataFile.addVariable(null, "ABSI_"+an.freq[i], DataType.FLOAT, dims1);
                vABSI[i].addAttribute(new Attribute("sample_rate_sps", (float)an.rate[i]));            
                vABSI[i].addAttribute(new Attribute("frequency_kHz", (float)an.freq[i]));            
                vABSI[i].addAttribute(new Attribute("pulse_length_us", (float)an.pulseLen[i]));   
                vABSI[i].addAttribute(new Attribute("alpha_dBpm", (double)an.alpha[i]));          
                vABSI[i].addAttribute(new Attribute("units", "counts"));
                vABSI[i].addAttribute(new Attribute("long_name", "acoustic_return_counts"));
                vABSI[i].addAttribute(new Attribute("name", "acoustic return signal counts"));
                vABSI[i].addAttribute(new Attribute("coordinates", "TIME LATITUDE LONGITUDE depth_"+an.freq[i]));
                vABSI[i].addAttribute(new Attribute("_FillValue", Float.NaN));                                
                vABSI[i].addAttribute(new Attribute("valid_min", 0f));
                vABSI[i].addAttribute(new Attribute("valid_max", 65536f));

                vABSI[i].addAttribute(new Attribute("sv_calculation", "Sv = ELmax –2.5/ds + N/(26214·ds) – TVR – 20·logVTX + 20·logR + 2·α·R – 10log(1⁄2c·τ·Ψ)"));

                vABSI[i].addAttribute(new Attribute("tvr_transmit_voltage_response", (double)an.getTvr()[i]));      
                vABSI[i].addAttribute(new Attribute("vtx_transmit_voltage", (double)an.getVtx()[i]));      
                vABSI[i].addAttribute(new Attribute("bp_beam_pattern_factor", (double)an.getBp()[i]));      
                vABSI[i].addAttribute(new Attribute("ds_detector_slope", (double)an.getDs()[i]));      
                vABSI[i].addAttribute(new Attribute("el_echo_level_max", (double)an.getEl()[i]));      
                                                               
//                vABSI[i].addAttribute(new Attribute("sensor_depth", 30f));
//                vABSI[i].addAttribute(new Attribute("sensor_name", "ASL AZFP"));
//                vABSI[i].addAttribute(new Attribute("sensor_serial_number", String.format("%d", an.serialNo)));

                int[] iDim1 = new int[]
                {
                    ndf.timeDim.getLength(), sampleDim1.getLength()
                };
                dataABSI[i] = Array.factory(DataType.FLOAT, iDim1);
            }
            
            int i = 0;

            for (int fileNo = 0; fileNo < listOfFiles.size(); fileNo++)
            {
                File f = (File)listOfFiles.get(fileNo);
                //System.out.println("Process file : " + f);

                an.open(f);
                while(an.read() != 0)
                {
                    //System.out.println(an.toString());

                    dataTime.add(new Timestamp(an.ts.getTime()));
                    dataTemperature.setFloat(i, (float)(an.getTemp()));
                    //dataPressure.setFloat(i, (float)(an.pressure));
                    dataBattery.setFloat(i, (float)(an.getBattery()));
                    dataTiltX.setFloat(i, (float)(an.getTiltX()));
                    dataTiltY.setFloat(i, (float)(an.getTiltY()));
                    for(int k=0;k<4;k++)
                    {
                        Index idx = dataABSI[k].getIndex();
                        
                        double[] sv = an.getData(k);
                        for(int j=0;j<an.bins[k];j++)
                        {
                            idx.set(i, j);
                            dataABSI[k].setFloat(idx, (float)sv[j]);
                        }
                    }

                    i++;
                }
            }

            System.out.println("records " + i + " timeDim " + ndf.timeDim.toString() + " " + ndf.timeDim.getLength());

            ndf.writeCoordinateVariables(dataTime);
            ndf.writeCoordinateVariableAttributes();

            // Write the coordinate variable data. 
            ndf.create();

            ndf.dataFile.write(ndf.vTime, ndf.times);

            ndf.writePosition(m.getLatitudeIn(), m.getLongitudeIn());            
            
            ndf.dataFile.write(vTemperature, dataTemperature);
            //ndf.dataFile.write(vPressure, dataPressure);
            ndf.dataFile.write(vBattery, dataBattery);
            ndf.dataFile.write(vTiltX, dataTiltX);
            ndf.dataFile.write(vTiltY, dataTiltY);
            for(int k=0;k<4;k++)
            {
                ndf.dataFile.write(distance[k], dataDistance[k]);
                ndf.dataFile.write(vABSI[k], dataABSI[k]);
            }

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
