package org.imos.abos.mooring.MRU;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import org.imos.abos.mooring.MRU.WaveCalculator;
import org.imos.abos.mooring.MRU.decode;
import org.imos.abos.mooring.MRU.waveSpectra;
import org.imos.abos.mooring.MRU.decode.mruRecord;
import org.imos.abos.mooring.MRU.decode.mruStabQ;

public class DecodeOpenLogFile
{
    public static void main(String[] args)
    {
        DecodeOpenLogFile d = new DecodeOpenLogFile();

        try
        {
            int i = 0;
            while (i < args.length)
            {
                d.readDataFile(args[i++]);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
    SimpleDateFormat sdfin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat sdffile = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss");

    public DecodeOpenLogFile()
    {
        sdfin.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdffile.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private void readDataFile(String filename) throws IOException
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar ts = new GregorianCalendar();

        File f = new File(filename);

        FileChannel roChannel = new RandomAccessFile(f, "r").getChannel();
        long fileSize = roChannel.size();
        ByteBuffer readOnlyBuffer = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) fileSize);

        System.err.println("read File " + filename + " file size " + fileSize);

        SummaryStatistics ssLoad = new SummaryStatistics();

        decode mruDecode = new decode();
        mruDecode.setMsgs(0);
        waveSpectra ws = new waveSpectra();

        int dataSize = -1;
        byte b;
        String line;
        int crlf = 0;
        int startOfLine = 0;
        Date startDate;
        double zAccel[] = new double[3072];
        boolean abort = false;
        try
        {
            while (readOnlyBuffer.hasRemaining() && !abort)
            {
                b = readOnlyBuffer.get();
                //System.out.println("byte " + readOnlyBuffer.position() + " " + b);
                if (b == 0x0d)
                {
                    crlf = 1;
                }
                else if (b == 0x0c) // start of MRU message
                {
                    readOnlyBuffer.position(0);
                    
                    dataSize = (int)fileSize;
                    startDate = sdffile.parse(f.getName());
                    ts.setTime(startDate);

                    int i = 0;
                    while (readOnlyBuffer.position() < (dataSize + 30) && (i < 3072))
                    {
                        //System.out.print(sdf.format(ts.getTime()) + " ," + i + " ,");
                        ts.add(Calendar.MILLISECOND, 200);

                        mruRecord mr = mruDecode.read(readOnlyBuffer);
                        if (mr != null)
                        {
                            mruStabQ v = (mruStabQ) mr;
                            zAccel[i++] = v.accelWorld.z;
                        }
                        else
                        {
                            abort = true;
                            break;
                        }
                        double load = readOnlyBuffer.getFloat();
                        ssLoad.addValue(load);

                        //System.out.println(" Load " + load);
                    }
                    //System.out.println("record count " + i);
                    
                    if (i > 3000)
                    {
                        double spec[] = ws.computeSpec(zAccel, false);
                        double max = -20;
                        double min = Double.MAX_VALUE;
                        double logSpec[] = new double[spec.length];
                        for (i = 0; i < spec.length; i++)
                        {
                            logSpec[i] = Math.log10(spec[i]);
                            max = Math.max(max, logSpec[i]);
                            min = Math.min(min, logSpec[i]);
                        }
                        double spec0 = min;
                        double specRes = 255 / (max - min);
                        int v[] = new int[spec.length];
                        //System.out.println("Spec Min " + min + " Max " + max);
                        for (i = 0; i < spec.length; i++)
                        {
                            v[i] = (int) Math.round((logSpec[i] - spec0) * specRes);
                            //System.out.println(i * WaveCalculator.DF + " ,Spec " + i + " spec " + spec[i] + " log " + logSpec[i] + " scaled " + v[i]);							
                        }
                        ByteBuffer bb = ByteBuffer.allocate(284);
                        bb.clear();
                        bb.put((byte) 0x20); // site code
                        bb.put((byte) 0x40); // data type
                        bb.position(16);
                        bb.putFloat((float) spec0);
                        bb.putFloat((float) specRes);
                        for (i = 0; i < spec.length; i++)
                        {
                            bb.put((byte) v[i]);
                        }
                        bb.putFloat((float) 0.0);
                        WaveCalculator wd = new WaveCalculator();
                        wd.decode(bb);
                        System.out.println(sdf.format(startDate) + ",swh=" + wd.calculate(WaveCalculator.DF, logSpec) + " ,minLoad=" + ssLoad.getMin()+ " ,meanLoad=" + ssLoad.getMean()+ " ,maxLoad=" + ssLoad.getMax());
                    }
                    //break;
                    
                }
                else if ((crlf == 1) && (b == 0xa))
                {
                    int endOfLine = readOnlyBuffer.position();
                    int lLen = endOfLine - startOfLine - 2;

                    byte[] ba = new byte[lLen];
                    readOnlyBuffer.position(startOfLine);
                    //System.out.println("start, end " + startOfLine + " , " + endOfLine + " len " + (endOfLine - startOfLine - 2));
                    readOnlyBuffer.get(ba, 0, lLen);
                    readOnlyBuffer.position(endOfLine);
                    line = new String(ba).trim();
                    //System.out.println("line " + startOfLine + " : '" + line + "'");
                    crlf = 0;
                    startOfLine = endOfLine;
                    if (line.indexOf("****** START RAW MRU DATA ******") > 0)
                    {
                        startDate = sdf.parse(line);
                        ts.setTime(startDate);
                        //System.err.println("file size " + fileSize + " " + dataSize + " pos " + readOnlyBuffer.position());
                        if (fileSize > dataSize)
                        {
                            int i = 0;
                            while (readOnlyBuffer.position() < (dataSize + endOfLine))
                            {
                                //System.out.print(sdf.format(ts.getTime()) + " ,");
                                ts.add(Calendar.MILLISECOND, 200);

                                mruRecord mr = mruDecode.read(readOnlyBuffer);
                                mruStabQ v = (mruStabQ) mr;
                                zAccel[i++] = v.accelWorld.z;
                                double load = readOnlyBuffer.getFloat();
                                ssLoad.addValue(load);

                                //System.out.println(" Load " + load);
                            }

                            double spec[] = ws.computeSpec(zAccel, false);
                            double max = -20;
                            double min = Double.MAX_VALUE;
                            double logSpec[] = new double[spec.length];
                            for (i = 0; i < spec.length; i++)
                            {
                                logSpec[i] = Math.log10(spec[i]);
                                max = Math.max(max, logSpec[i]);
                                min = Math.min(min, logSpec[i]);
                            }
                            double spec0 = min;
                            double specRes = 255 / (max - min);
                            int v[] = new int[spec.length];
                            //System.out.println("Spec Min " + min + " Max " + max);
                            for (i = 0; i < spec.length; i++)
                            {
                                v[i] = (int) Math.round((logSpec[i] - spec0) * specRes);
                                //System.out.println(i * WaveCalculator.DF + " ,Spec " + i + " spec " + spec[i] + " log " + logSpec[i] + " scaled " + v[i]);							
                            }
                            ByteBuffer bb = ByteBuffer.allocate(284);
                            bb.clear();
                            bb.put((byte) 0x20); // site code
                            bb.put((byte) 0x40); // data type
                            bb.position(16);
                            bb.putFloat((float) spec0);
                            bb.putFloat((float) specRes);
                            for (i = 0; i < spec.length; i++)
                            {
                                bb.put((byte) v[i]);
                            }
                            bb.putFloat((float) 0.0);
                            WaveCalculator wd = new WaveCalculator();
                            wd.decode(bb);
                            System.out.println(sdf.format(startDate) + ",swh=" + wd.calculate(WaveCalculator.DF, logSpec) + " ,minLoad=" + ssLoad.getMin()+ " ,meanLoad=" + ssLoad.getMean()+ " ,maxLoad=" + ssLoad.getMax());

                            readOnlyBuffer.get();
                            readOnlyBuffer.get();
                            startOfLine = readOnlyBuffer.position();
                        }
                    }
                    else if (line.indexOf("INFO: WAVE RAW DATA File Length =") > 0)
                    {
                        //System.out.println("size : " + line.substring(line.indexOf('=')+1).trim());
                        dataSize = Integer.parseInt(line.substring(line.indexOf('=') + 1).trim());
                        //System.out.println("dataSize " + dataSize);
                    }
                }
            }
            roChannel.close();
        }
        catch (ParseException pe)
        {
            pe.printStackTrace();
        }
    }

}
