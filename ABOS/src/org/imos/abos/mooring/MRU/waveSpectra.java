package org.imos.abos.mooring.MRU;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class waveSpectra 
{
    final int nFFT = 512;
    double spec[] = new double[nFFT];

    public waveSpectra()
    {

    }

    public double[] computeSpec(double[] data, boolean detrend)
    {
	int len = data.length;
	int i;
	int j;
	int nSegments = 0;
	double xInner[] = new double[nFFT];
	double xDetrend[] = new double[nFFT];
	Complex[] x = new Complex[nFFT];
	Complex[] xFFT;

	double window[] = getHamming();
	double specAccum[] = new double[nFFT/2];

	double avgData = 0;
	for(i=0;i<len;i++)
	{
	    avgData += data[i];
	}
	avgData /= len;
	//System.out.println("len " + len + " avgData " + avgData);
	if (detrend == false)
	{
	    for(i=0;i<len;i++)
	    {
		data[i] = data[i] - avgData;
	    }
	}
	
	for(i=0;i<nFFT/2;i++)
	{
	    specAccum[i] = 0;
	}
	for(i=0;i<(len-nFFT/2);i+=nFFT/2)
	{
	    for(j=0;j<nFFT;j++)
	    {
		xInner[j] = data[i+j];
	    }
	    if (detrend)
	    {
		xDetrend = detrend(xInner);
	    }
	    for(j=0;j<nFFT;j++)
	    {
		if (detrend)
		{
		    x[j] = new Complex(xDetrend[j] * window[j], 0);
		}
		else
		{
		    x[j] = new Complex(xInner[j] * window[j], 0);
		}
	    }
	    //FFT.show(x, "input");
	    xFFT = FFT.fft(x);
	    //FFT.show(xFFT, "FFT");
	    for(j=0;j<nFFT/2;j++)
	    {
		specAccum[j] += Math.pow(xFFT[j].abs(), 2);
	    }			
	    nSegments++;
	}
	for(j=0;j<nFFT/2;j++)
	{
	    specAccum[j] /= nSegments * (Math.pow(nFFT, 2)) * 0.3963873 * 0.5;
	}			

	return specAccum;
    }

    public double[] detrend(double[] data)
    {
	double[] dataOut = new double[data.length];
	int i;

	double accum1;
	double offset;

	double a0;
	double a1;

	// Get mean x0 and subtract
	offset = 0.0;

	for(i=0;i<data.length;i++)
	{
	    offset = offset + data[i];
	}
	offset = offset / data.length;

	//System.out.println("waveSprectra::detrend: accum2," + accum2);

	// Do linear regression
	accum1 = 0.0;

	for(i=0;i<data.length;i++)
	{
	    accum1 = accum1 + i * (data[i] - offset);
	}
	//System.out.println("waveSprectra::detrend: accum1," + accum1);

	a1 = accum1 / 11184768;			// WTF
	a0 = offset - a1 * 256.5;		// WTF (1 to 512)/2

	//System.out.println("waveSprectra::detrend: offset " + offset + " a1," + a1 + ", a0," + a0);
	
	// Remove linear background

	for(i=0;i<data.length;i++)
	{
	    dataOut[i] = data[i] - (a0 + a1 * i);
	    //dataOut[i] = data[i] - accum2;
	    //System.out.println("detrend," + data[i] + "," + dataOut[i]);
	}

	return dataOut;
    }

    public double[] getHamming()
    {
	double window[] = new double[nFFT];
	for(int i=0;i<nFFT;i++)
	{
	    window[i] = 0.53836 - 0.46164 * Math.cos(2 * Math.PI * i / nFFT);
	}

	return window;
    }


    /**
     * @param args
     */
    public static void main(String[] args) 
    {
	decode d = new decode();
	d.msgs = 0;
	final int len = 31;
	File f = new File(args[0]);
	int fLen = (int)f.length();
	System.out.println("file len " + fLen + " " + fLen/len);
	double zAccel[] = new double[fLen/len];
	int i;
	waveSpectra ws = new waveSpectra();

	try 
	{
	    FileInputStream is = new FileInputStream(f);

	    ByteBuffer b = ByteBuffer.allocate(len);
	    byte[] barray = new byte[len];

	    boolean eof = false;

	    i = 0;
	    while (!eof)
	    {
		int r = is.read(barray);
		if (r < 0)
		{
		    eof = true;
		}
		else
		{
		    b.position(0);
		    b.put(barray);

		    decode.mruStabQ v = (decode.mruStabQ)d.read(b);
		    zAccel[i] = v.accelWorld.z;
		    System.out.println("zAccel " + i + " " + zAccel[i]);
		    i++;
		}
	    }
	    double spec[] = ws.computeSpec(zAccel, false);
	    double max = -20;
	    double min = Double.MAX_VALUE;
	    double logSpec[] = new double[ws.nFFT/2];
	    for(i=0;i<ws.nFFT/2;i++)
	    {
		logSpec[i] = Math.log10(spec[i]);
		max = Math.max(max, logSpec[i]);
		min = Math.min(min, logSpec[i]);
	    }
	    double spec0 = min;
	    double specRes = 255 / (max - min);
	    int v[] = new int[ws.nFFT/2];
	    System.out.println("Spec Min " + min + " Max " + max);
	    for (i=0;i<ws.nFFT/2;i++)
	    {
		v[i] = (int)Math.round((logSpec[i] - spec0) * specRes);
		System.out.println("Spec " + i + " spec " + spec[i] + " log " + logSpec[i] + " scaled " + v[i]);
	    }
	    if (args.length > 1)
	    {
		System.out.println("Output to File " + args[1]);
		FileOutputStream out = new FileOutputStream(args[1]);
		ByteBuffer bb = ByteBuffer.allocate(284);
		bb.clear();
		bb.put((byte)0x20);
		bb.put((byte)0x40);
		bb.position(16);
		bb.putFloat((float)spec0);
		bb.putFloat((float)specRes);
		for(i =0;i<ws.nFFT/2;i++)
		{
		    bb.put((byte)v[i]);
		}
		bb.putFloat((float)0.0);
		System.out.println("bb pos " + bb.position());
		out.write(bb.array());
		out.close();
	    }
	} 
	catch (FileNotFoundException e) 
	{
	    e.printStackTrace();
	} 
	catch (IOException e) 
	{
	    e.printStackTrace();
	}
    }

}
