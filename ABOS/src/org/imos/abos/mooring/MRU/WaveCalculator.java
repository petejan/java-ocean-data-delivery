package org.imos.abos.mooring.MRU;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class WaveCalculator
{
	static public final double DF = 5.0 / 512; // --------------------------------------- was /256
	static private final double LOG2PI = Math.log10(2.0 * Math.PI);

	// decode the byte buffer into an array of log Spectrum values
	public double[] decode(ByteBuffer bb)
	{
		double logSpec[] = new double[256];

		bb.position(16); // skip header

		double spec0 = bb.getFloat();
		double specRes = bb.getFloat();

		// System.out.println("spec0 " + spec0 + " specRes " + specRes);

		byte[] buff = new byte[256];
		bb.get(buff);

		for (int i = 0; i < logSpec.length; i++)
		{
			int d = buff[i] & 0xff;

			logSpec[i] = spec0 + d / specRes;

			// System.out.printf("freq %2.3f amp %5.3f\n", (i * DF),
			// logSpec[i]);
		}

		return logSpec;
	}

	public double calculate(double fStep, double logSpec[])
	{
		double logDispSpec[] = new double[logSpec.length];
		double logFreq[] = new double[logSpec.length];

		// calculate the power spectral density
		for (int i = 1; i < logSpec.length; i++) // --------------------------------- was i = 0
		{
			logFreq[i] = Math.log10(i * DF); // ------------------------------- was i + 1
			logDispSpec[i] = logSpec[i] - 4.0 * (LOG2PI + logFreq[i]);
		}

		// calculate the low frequency minimum
		double lowFreqSpec = Double.MAX_VALUE;
		int lowFreqSpecIndex = 0;

		// for(int i=1;i<5;i++)
		// {
		// if (logSpec[i] < lowFreqSpec)
		// {
		// lowFreqSpec = logSpec[i];
		// lowFreqSpecIndex = i;
		// }
		// }
		lowFreqSpecIndex = 3;
		lowFreqSpec = logSpec[lowFreqSpecIndex];

		// System.out.println("lowFreqSpecIndex " + lowFreqSpecIndex +
		// " lowFreqSpec " + lowFreqSpec);

		// Filter the spectrum - remove noise line which is a linear fit in
		// log space of a line joining very low and very high frequency
		// (index 200 to 209) values
		double x0 = logFreq[lowFreqSpecIndex];
		double y0 = logDispSpec[lowFreqSpecIndex];
		double x1 = 0.0;
		double y1 = 0.0;
		for (int i = 200; i < 210; i++)
		{
			x1 += logFreq[i];
			y1 += logDispSpec[i];
		}

		x1 /= 10.0;
		y1 /= 10.0;

		// System.out.println("x0 " + x0 + " y0 " + y0 + " x1 " + x1 + " y1 " +
		// y1);

		double a1 = (y1 - y0) / (x1 - x0);
		double a0 = y1 - a1 * x1;
		double minSpec = Math.pow(10.0, a1 * logFreq[logSpec.length - 1] + a0);
		double meanSq = 0.0;
		double spec;

		int endFindex = 100;
		// System.out.println("Min Spec " + minSpec);

		// System.out.println("frequency " + lowFreqSpecIndex + " range " +
		// (lowFreqSpecIndex * DF) + " to " + ((endFindex-1) * DF));
		// System.out.println("remove noise start " + (a1 *
		// logFreq[lowFreqSpecIndex] + a0) + " end " + (a1 *
		// logFreq[endFindex-1] + a0) + " minSpec " + Math.log10(minSpec));

		// Accumulate spectral power in wave frequency zone
		for (int i = lowFreqSpecIndex; i < endFindex; i++)
		{
			spec = Math.pow(10.0, logDispSpec[i]);// - Math.pow(10.0, a1 *
													// logFreq[i] + a0);

			// spec = Math.max(spec, minSpec);

			// System.out.printf("%3d  %5.3f  spec %e   %e\n", i, i * DF,
			// Math.pow(10, logDispSpec[i]), spec[i]);

			meanSq += spec;
		}

		// Get significant wave height (from definition)
		double value = 4.0 * Math.sqrt(meanSq);

		// System.out.println("Significant wave height " + value);

		return value;
	}

	public double read(ByteBuffer bb)
	{
		System.out.println("read length " + bb.limit());

		double logSpec[] = decode(bb);

		return calculate(DF, logSpec);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		WaveCalculator wd = new WaveCalculator();

		FileInputStream is;
		try
		{
			is = new FileInputStream(args[0]);
			// Obtain a channel
			FileChannel channel = is.getChannel();

			ByteBuffer buf = channel.map(MapMode.READ_ONLY, 0, is.available());

			System.out.println("Significant Wave Height " + wd.read(buf));
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
