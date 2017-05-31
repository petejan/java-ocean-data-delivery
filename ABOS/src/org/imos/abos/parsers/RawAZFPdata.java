package org.imos.abos.parsers;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.xml.parsers.ParserConfigurationException;
import org.imos.abos.parsers.AZFP.ParseAZFPxml;
import org.jfree.util.Log;
import org.xml.sax.SAXException;

public class RawAZFPdata
{
    SimpleDateFormat sdf;
    Calendar c;
    String xmlFile;
    ParseAZFPxml cal;
    double calTiltX[] = new double[4];
    double calTiltY[] = new double[4];
    double calTemp[] = new double[3];
    double calTempK[] = new double[3];
    double tvr[] = new double[4];
    double vtx[] = new double[4];
    double bp[] = new double[4];
    double el[] = new double[4];
    double ds[] = new double[4];
    public double alpha[] = new double[4];
    public double sos = 1500; // m/s

    public RawAZFPdata(String xmlfile) throws ParserConfigurationException, SAXException, IOException
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
        c = new GregorianCalendar();
        
        cal = new ParseAZFPxml(xmlfile);
        calTiltX[0] = cal.getTiltXA();
        calTiltX[1] = cal.getTiltXB();
        calTiltX[2] = cal.getTiltXC();
        calTiltX[3] = cal.getTiltXD();
        calTiltY[0] = cal.getTiltYA();
        calTiltY[1] = cal.getTiltYB();
        calTiltY[2] = cal.getTiltYC();
        calTiltY[3] = cal.getTiltYD();
        calTemp[0] = cal.getAnalogTempA();
        calTemp[1] = cal.getAnalogTempB();
        calTemp[2] = cal.getAnalogTempC();
        calTempK[0] = cal.getAnalogTempKA();
        calTempK[1] = cal.getAnalogTempKB();
        calTempK[2] = cal.getAnalogTempKC();
        
        // setup the one-way water range attenuation in dB/m
        alpha[0] = 0.0101;
        alpha[1] = 0.0399;
        alpha[2] = 0.0548;
        alpha[3] = 0.1156;
        
        // hacked values based on 55046 data from SOFS-4 to make average constant with distance
        alpha[0] = -0.0101;
        alpha[1] = -0.025; 
        alpha[2] = -0.03; 
        alpha[3] = -0.065;

        // From http://resource.npl.co.uk/acoustics/techguides/seaabsorption/ t = 10C, depth = 0.05, sal = 35, ph=8, (Ainslie and McColm 1998)
        alpha[0] = 10.298/1000; 
        alpha[1] = 40.583/1000; 
        alpha[2] = 55.674/1000; 
        alpha[3] = 116.741/1000;
        
        for(int i=0;i<4;i++)
        {
            tvr[i] = cal.getFrequencyTVR(i);
            vtx[i] = cal.getFrequencyVTX(i);
            bp[i] = cal.getFrequencyBP(i);
            el[i] = cal.getFrequencyEL(i);
            ds[i] = cal.getFrequencyDS(i);
        }
    }

    public static void main(String args[])
    {
        int i = 0;
        
        try
        {
            RawAZFPdata an = new RawAZFPdata(args[i++]);

            for (;i<args.length;i++)
            {
                File f = new File(args[i]);

                if (f.isFile())
                {
                    an.open(f);
                    while(an.read() != 0)
                    {
                        System.out.println(an.toString());
                    }
                    an.close();
                }
                else
                {
                    System.out.println("directory " + f);
                    File[] files = f.listFiles(new FilenameFilter()
                    {
                        @Override
                        public boolean accept(File dir, String name)
                        {
                            return name.endsWith(".01A");
                        }
                    });

                    for (File datfile : files)
                    {
                        System.out.println("File : " + datfile);
                        an.open(datfile);
                        while(an.read() != 0)
                        {
                            System.out.println(an.toString());
                        }
                        an.close();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    int flag;
    int burst;
    public int serialNo;
    int status;
    int interval;

    int year;
    int month;
    int day;
    int hour;
    int min;
    int sec;
    int hsec;
    public Date ts;

    public int[] rate = new int[4];
    int[] lockout = new int[4];
    public int[] bins = new int[4];
    int[] samples = new int[4];
    int npings;
	int pings;
    int seconds;
    int firstPing;
    int lastPing;
    int dataType;
    int dataError;
    int phase;
    int overrun;
    int channels;
    int[] gain = new int[4];
    public int[] pulseLen = new int[4];
    int[] board = new int[4];
    public int[] freq = new int[4];
    int sensors;
    public int tiltx;
    public int tilty;
    public int battery;
    public int pressure;
    public int temperature;
    public int ad6;
    public int ad7;
    public int[][] data;

    public String toString()
    {
        System.out.printf("flag 0x%04x\n", flag);
        System.out.println("burst " + burst + " serial " + serialNo + " status " + status + " interval " + interval);
        System.out.printf("time %04d-%02d-%02d %02d:%02d:%02d.%03d\n", year, month, day, hour, min, sec, hsec);
        System.out.println("TS " + sdf.format(ts));

        for (int j = 0; j < 4; j++)
        {
            System.out.printf(j + " rate     %d", rate[j]);
            System.out.printf(" lockout  %d", lockout[j]);
            System.out.printf(" bins     %5d", bins[j]);
            System.out.printf(" samples  %d", samples[j]);
            System.out.printf(" gain     %3d", gain[j]);
            System.out.printf(" pulseLen %d", pulseLen[j]);
            System.out.printf(" board    %d", board[j]);
            System.out.printf(" freq     %3d\n", freq[j]);
        }
        System.out.println("pings " + pings + " npings " + npings + " seconds " + seconds + " first " + firstPing + " last " + lastPing);
        System.out.println("dataType " + dataType + " dataError " + dataError + " phase " + phase + " overrun " + overrun + " channels " + channels);

        System.out.println("sensors " + sensors + " Temp " + temperature + " pressure " + pressure);

        System.out.println("tilt " + getTiltX());
        System.out.println("temp " + getTemp());
        System.out.println("battery " + battery + " " + getBattery());
        
//        System.out.println("TVR " + tvr[0]);
//        System.out.println("VTX " + vtx[0]);
//        System.out.println("EL  " + el[0]);
//        System.out.println("BP  " + bp[0]);
//        System.out.println("DS  " + ds[0]);
//        System.out.println("al  " + alpha[0]);
//        
//        double[] sv = getSV(0);
//        System.out.print("data ");
//        for(int i=0;i<bins[0];i++)
//        {            
//            System.out.printf(",%4.2f", sv[i]);
//        }
//        System.out.println();

        return "serial no " + serialNo;
        
    }
    
    public double getVolts(int i)
    {
        return 2.5 * ((double)i) / 65535;
    }
    public double getBattery()
    {
        return 6.5 * getVolts(battery);
    }
    public double getTemp()
    {
        double v = getVolts(temperature);
        double r = (calTempK[0] + calTempK[1] * v ) / (calTempK[2] - v);
        double lnr = Math.log(r);
        return (1/(calTemp[0] + calTemp[1] * lnr + calTemp[2] * Math.pow(lnr, 3))) - 273.15;
    }
    
    public double getTiltX()
    {
        double v = tiltx; // manual says volts, but the AD counts gives the correct value (?)
        return calTiltX[0] + calTiltX[1] * v + calTiltX[2] * Math.pow(v, 2) + calTiltX[3] * Math.pow(v, 3);
    }
    public double getTiltY()
    {
        double v = tilty; // manual says volts, but the AD counts gives the correct value (?)
        return calTiltX[0] + calTiltY[1] * v + calTiltY[2] * Math.pow(v, 2) + calTiltY[3] * Math.pow(v, 3);
    }
    
    public double[] getSV(int ch)
    {
    	// Sv = ELmax –2.5/ds + N/(26214.ds) – TVR – 20.logVTX + 20.logR + 2.α.R – 10log(1/2c.t.Ψ)
        double[] sv = new double[bins[ch]];
        double r;
        
        double c = el[ch] - 2.5/ds[ch] - tvr[ch] - 20 * Math.log10(vtx[ch]) - 10 * Math.log10(bp[ch] * sos * (pulseLen[ch]*1.e-6) / 2.0);
        
        for(int i=0;i<bins[ch];i++)
        {
            r = sos * (i + 1) / rate[ch] / 2; // range, m
            // sv = const + data (dB) + range_spread + range_water_attenuation
            sv[i] = c + data[ch][i]/(26214 * ds[ch])  + 20 * Math.log10(r) + 2 * alpha[ch] * r ; // 26214 = 65536/2.5 
            
        }
        return sv;
    }
    public double[] getData(int ch)
    {
        double[] data = new double[bins[ch]];
        
        for(int i=0;i<bins[ch];i++)
        {
            data[i] = this.data[ch][i];
            
        }
        return data;
    }
    
    public double[] getTvr()
	{
		return tvr;
	}

	public double[] getVtx()
	{
		return vtx;
	}

	public double[] getBp()
	{
		return bp;
	}

	public double[] getEl()
	{
		return el;
	}

	public double[] getDs()
	{
		return ds;
	}

	FileInputStream fileInputStream;
    FileChannel in;
    MappedByteBuffer buffer;
    long recSize = -1;
    public Date tsStart = null;
    public Date tsEnd = null;    
    
    public long open(File filename) throws FileNotFoundException, IOException
    {
        fileInputStream = new FileInputStream(filename);
        in = fileInputStream.getChannel();

        Log.info("open " + filename);
        
        buffer = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());

        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.load();
        
        read();
        
        tsStart = (Date)ts.clone();
        
        buffer.position((int)((long)in.size() - (long)recSize));
        read();
        tsEnd = (Date)ts.clone();
        
        buffer.rewind() ; // rewind

        long records = (in.size()/recSize) - 1;

        //System.out.println("ts Start " + tsStart + " ts end " + tsEnd);
                
        //System.out.println("file size " + in.size() + " records " + records);
        
        return records;
    }
    
    public int read() throws IOException
    {
        int s = buffer.position();
        
        flag = buffer.getShort() & 0xffff;
        if (flag != 0xfd02)
        {
            return -1;
        }

        burst = buffer.getShort();
        serialNo = buffer.getShort() & 0xffff;
        status = buffer.getShort();
        interval = buffer.getInt();

        year = buffer.getShort();
        month = buffer.getShort();
        day = buffer.getShort();
        hour = buffer.getShort();
        min = buffer.getShort();
        sec = buffer.getShort();
        hsec = buffer.getShort();

        c.set(year, month - 1, day, hour, min, sec);
        c.set(Calendar.MILLISECOND, hsec * 10);
        ts = c.getTime();

        rate[0] = (buffer.getShort() & 0xffff);
        rate[1] = (buffer.getShort() & 0xffff);
        rate[2] = (buffer.getShort() & 0xffff);
        rate[3] = (buffer.getShort() & 0xffff);
        lockout[0] = buffer.getShort();
        lockout[1] = buffer.getShort();
        lockout[2] = buffer.getShort();
        lockout[3] = buffer.getShort();
        bins[0] = buffer.getShort();
        bins[1] = buffer.getShort();
        bins[2] = buffer.getShort();
        bins[3] = buffer.getShort();
        samples[0] = buffer.getShort();
        samples[1] = buffer.getShort();
        samples[2] = buffer.getShort();
        samples[3] = buffer.getShort();

        data = new int[4][bins[0]];

        pings = buffer.getShort();       // 16
        npings = buffer.getShort();
        seconds = buffer.getShort();
        firstPing = buffer.getShort() & 0xffff;
        lastPing = buffer.getShort() & 0xffff;

        dataType = buffer.getInt();       // 21
        dataError = buffer.getShort();
        phase = buffer.get();
        overrun = buffer.get();
        channels = buffer.get();

        gain[0] = buffer.get() & 0xff;
        gain[1] = buffer.get() & 0xff;
        gain[2] = buffer.get() & 0xff;
        gain[3] = buffer.get() & 0xff;
        buffer.get();
        buffer.get();
        buffer.get();
        pulseLen[0] = buffer.getShort();
        pulseLen[1] = buffer.getShort();
        pulseLen[2] = buffer.getShort();
        pulseLen[3] = buffer.getShort();
        board[0] = buffer.getShort();
        board[1] = buffer.getShort();
        board[2] = buffer.getShort();
        board[3] = buffer.getShort();
        freq[0] = buffer.getShort();
        freq[1] = buffer.getShort();
        freq[2] = buffer.getShort();
        freq[3] = buffer.getShort();

        sensors = buffer.getShort() & 0xffff; // 43
        tiltx = buffer.getShort() & 0xffff;
        tilty = buffer.getShort() & 0xffff;
        battery = buffer.getShort() & 0xffff;
        pressure = buffer.getShort() & 0xffff;
        temperature = buffer.getShort() & 0xffff;
        ad6 = buffer.getShort() & 0xffff;
        ad7 = buffer.getShort() & 0xffff;

        // EL = ELmax – 2.5/a + N/(26214·a)
        // Sv = ELmax – 2.5/a + N/(26214·a) – SL + 20·logR + 2·α·R – 10log(½c·τ·ψ)
        // SL = sound transmission level
        // α = absorption coefficient (dB/m)
        // τ = transmit pulse length (s)
        // c = sound speed (m/s)
        // ψ = equivalent solid angle of the transducer beam pattern (sr). 

        for (int k = 0; k < 4; k++)
        {
            for (int j = 0; j < bins[k]; j++)
            {
                data[k][j] = buffer.getShort() & 0xffff;
            }
        }

        recSize = buffer.position() - s;
        
        return (int)(in.size() - buffer.position());
    }
    
    public void close() throws IOException
    {
        buffer.clear();
        in.close();
        fileInputStream.close();
    }
}
