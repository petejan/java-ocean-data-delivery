/*
 * IMOS data delivery project
 * Written by Peter Jansen
 * This code is copyright (c) Peter Jansen 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.parsers;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;

import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.dbms.Array2DInstrumentData;
import org.imos.abos.dbms.ArrayInstrumentData;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.dbms.RawInstrumentData;
import org.imos.abos.netcdf.NetCDFfile;
import org.wiley.core.Common;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.Index2D;
import ucar.ma2.Index3D;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import org.apache.log4j.Logger;


/**
 *
 * @author peter
 */
public class TriAXYSDataParser extends AbstractDataParser
{
	private static Logger log = Logger.getLogger(TriAXYSDataParser.class);
	private static long tOffset = 24 * 3600 * 1000;

    String mooring = "SOFS-2-2011";
    boolean raw = true;

    public TriAXYSDataParser()
    {
    	super();
    }
    
    private TriAXYSDataParser(String string)
    {
        super();
        mooring = string;
    }
    
    TriAXYSParser p = null;
    
    int header = 0;
    enum Type {NONE, SUMMARY, DIRSPEC, NONDIRSPEC, MEANDIR, RAW, WAVE, UNKNOWN};
    boolean commit = true;
    
    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy MMM dd HH:mm");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    Timestamp ts;
    
    public class Sample
    {
        Date ts;
        Double spectralDensity[] = null;
        Double meanWaveDir = null;
        Double meanSpreadWidth = null;
        Double[] spectralDensity_MEANDIR = null;
        Double meanDirection[] = null;
        Double spreadWidth[] = null;  
        Double dirSpectrum[][] = null;
        Double zeroCrossings;
        Double averageHt;
        Double Tz;
        Double maxHt;
        Double sigHt;
        Double sigPer;
        Double Tp;
        Double Tp5;
        Double HM0;
        Double meanTheta;
        Double sigmaTheta;  
        Double[] compass = null;
        Double[][] accel = null;
        Double[][] gyro = null;        
    }
    
    TreeMap<Timestamp,Sample> samples = new TreeMap<Timestamp,Sample>();
    

    private class SummaryParser extends TriAXYSParser
    {        
        Double zeroCrossings;
        Double averageHt;
        Double Tz;
        Double maxHt;
        Double sigHt;
        Double sigPer;
        Double Tp;
        Double Tp5;
        Double HM0;
        Double meanTheta;
        Double sigmaTheta;
        SimpleDateFormat df;        

        public SummaryParser()
        {
            headerLength = 1;
            type = Type.SUMMARY;
            log.debug("SummaryParser " + headerLength);  
            df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        }

        @Override
        protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
        {            
        }

        @Override
        protected void parseData(String dataLine) throws ParseException, NoSuchElementException
        {
            // Date	Year	Julian Date	Zero Crossings	Ave. Ht.	Ave. Per.	Max Ht.	Sig. Wave	 Sig. Per.	Peak Per.(Tp)	Peak Per.(Tp5)	HM0	Mean Theta	Sigma Theta            
            String[] split = dataLine.split("\t");       
            if (split.length >= 14)
            {
                ts = new Timestamp(df.parse(split[0]).getTime() + tOffset);
                
                zeroCrossings = Double.parseDouble(split[3]); // Number of waves detected by zero-crossing analysis of the wave elevation record.                
                averageHt = Double.parseDouble(split[4]); // Average zero down-crossing wave height (m)
                Tz = Double.parseDouble(split[5]); // Average zero down-crossing wave period (s)
                maxHt = Double.parseDouble(split[6]); // Maximum zero down-crossing wave height (trough topeak) (m)
                sigHt = Double.parseDouble(split[7]); // sig wave height, height of 1/3 of waves fro zero crossing analysis
                sigPer = Double.parseDouble(split[8]); // Average period of the significant zero down-crossing waves (s).
                Tp = Double.parseDouble(split[9]); // Peak wave period Tp in seconds.
                Tp5 = Double.parseDouble(split[10]); // Peak wave period in seconds as computed by the Read method. Tp5 has less statistical variability than Tp because it is based on spectral moments.
                HM0 = Double.parseDouble(split[11]); // sig wave height as 4 x sqrt(m0)
                meanTheta = Double.parseDouble(split[12]); // Overall mean wave direction in degrees obtained by averaging the mean wave angle q over all frequencies with weighting function S(f). q is calculated by the KVH method.
                sigmaTheta = Double.parseDouble(split[13]); // Overall directional spreading width in degrees obtained by averaging the spreading width sigma theta, sq, over all frequencies with weighting function S(f). sq is calculated by the KVH method.
                
                log.debug("Summary parse " + ts + " SWH " + sigHt);

                //if (ts.after(df.parse("2011/11/20 23:59")) & ts.before(df.parse("2012/07/31 23:00")))
                {
                    Sample s = samples.get(ts);
                    if (s == null)
                    {
                        s = new Sample();
                        samples.put(ts, s);
                        s.ts = (Timestamp)ts.clone();
                    }
                    s.zeroCrossings = zeroCrossings;
                    s.averageHt = averageHt;
                    s.Tz = Tz;
                    s.maxHt = maxHt;
                    s.sigHt = sigHt;    
                    s.sigPer = sigPer;
                    s.Tp = Tp;
                    s.Tp5 = Tp5;
                    s.HM0 = HM0;
                    s.meanTheta = meanTheta;
                    s.sigmaTheta = sigmaTheta;
                }
            }
        }

        @Override
        protected void commitData(RawInstrumentData raw)
        {
            if (currentFile != null)
            {            
                boolean ok = false;

                raw.setParameterCode("ZERO_CROSS");
                raw.setParameterValue(zeroCrossings);
                ok = raw.insert();
                raw.setParameterCode("AVG_WAVE_HEIGHT");
                raw.setParameterValue(averageHt);
                ok = raw.insert();
                raw.setParameterCode("AVG_PERIOD");
                raw.setParameterValue(Tz);
                ok = raw.insert();
                raw.setParameterCode("MAX_WAVE_HEIGHT");
                raw.setParameterValue(maxHt);
                ok = raw.insert();
                raw.setParameterCode("SIG_WAVE_HEIGHT");
                raw.setParameterValue(sigHt);
                ok = raw.insert();
                raw.setParameterCode("SIG_WAVE_PERIOD");
                raw.setParameterValue(sigPer);
                ok = raw.insert();
                raw.setParameterCode("TP");
                raw.setParameterValue(Tp);
                ok = raw.insert();
                raw.setParameterCode("TP5");
                raw.setParameterValue(Tp5);
                ok = raw.insert();
//                raw.setParameterCode("SIG_WAVE_HEIGHT");
//                raw.setParameterValue(HM0);
//                ok = raw.insert();
                raw.setParameterCode("MEAN_THETA");
                raw.setParameterValue(meanTheta);
                ok = raw.insert();
                raw.setParameterCode("SIGMA_THETA");
                raw.setParameterValue(sigmaTheta);
                ok = raw.insert();
                
                log.info("commit " + ok);
            }
            
        }
    }
    public abstract class TriAXYSParser
    {
        int headerLength = -1;
        Type type = Type.NONE;
        int line = 0;                

        protected abstract void parseHeader(String dataLine) throws ParseException, NoSuchElementException;
        protected abstract void parseData(String dataLine) throws ParseException, NoSuchElementException;
        protected abstract void commitData(RawInstrumentData raw);
    }
    
    public class NonDirParser extends TriAXYSParser
    {
        Double spectralDensity[] = null;
        ArrayInstrumentData array = new ArrayInstrumentData();
                
        public NonDirParser()
        {
            headerLength = 8;
            type = Type.NONDIRSPEC;
            log.debug("NonDirParser " + headerLength);
        }
        @Override
        protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
        {
        }

        @Override
        protected void parseData(String dataLine) throws ParseException, NoSuchElementException
        {
            if (spectralDensity == null)
            {
                spectralDensity = new Double[nFreqPoints];
                for(int i=0;i<nFreqPoints;i++)
                {
                    spectralDensity[i] = Double.NaN;
                }                                
            }
            double f = Double.parseDouble(dataLineTokens.nextToken());
            int i = 0;
            for(i = 0;i<frequency.length;i++)
            {
                if (frequency[i] >= f)
                {
                    break;
                }
            }
//            log.debug("f " + frequency[i]);
            Double v = new Double(dataLineTokens.nextToken());
            if (v.doubleValue() != 0.0)
            {
                spectralDensity[i] = v;
            }
            line++;
            if (line == nFreq)
            {
            	commit = true;
            }
        }  

        @Override
        protected void commitData(RawInstrumentData raw)
        {            

            Sample s = samples.get(ts);
            if (s == null)
            {
                s = new Sample();
                samples.put(ts, s);
                s.ts = (Timestamp) ts.clone();
            }
            s.spectralDensity = spectralDensity;

            log.debug("Set NONDIR spectral density " + ts + " " + spectralDensity[5]);
            
            if (currentFile != null)
            {
                boolean ok = false;

                array.setDataTimestamp(ts);
                array.setDepth(instrumentDepth);
                array.setInstrumentID(currentInstrument.getInstrumentID());
                array.setLatitude(currentMooring.getLatitudeIn());
                array.setLongitude(currentMooring.getLongitudeIn());
                array.setMooringID(currentMooring.getMooringID());
                array.setSourceFileID(currentFile.getDataFilePrimaryKey());

                array.setParameterCode("SPECTRAL_DENSITY");            
                array.setParameterValue(spectralDensity);

                array.setQualityCode("RAW");

                ok |= array.insert();
            }
        }
        
    }
    public class MeanDirParser extends TriAXYSParser
    {
        Double meanWaveDir = null;
        Double meanSpreadWidth = null;
        Double spectralDensity[] = null;
        Double meanDirection[] = null;
        Double spreadWidth[] = null;
        ArrayInstrumentData array = new ArrayInstrumentData();
                
        public MeanDirParser()
        {
            headerLength = 13;
            type = Type.MEANDIR;
            log.debug("MeanDirParser " + headerLength);
        }
        @Override
        protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
        {
            String[] split = dataLine.split("=");
            String param = split[0].trim();
            String value = split[1].trim();
            
            if (param.startsWith("S(f) WEIGHTED MEAN WAVE DIRECTION"))
            {
                meanWaveDir = Double.parseDouble(value);
            }
            else if (param.startsWith("S(f) WEIGHTED MEAN SPREAD WIDTH"))
            {
                meanSpreadWidth = Double.parseDouble(value);
            }           
            fn = 0;
        }

        int fn = 0;
        @Override
        protected void parseData(String dataLine) throws ParseException, NoSuchElementException
        {
            if (spectralDensity == null)
            {
                spectralDensity = new Double[nFreqPoints];
                meanDirection = new Double[nFreqPoints];
                spreadWidth = new Double[nFreqPoints];
                for(int i=0;i<nFreqPoints;i++)
                {
                    spectralDensity[i] = Double.NaN;
                    meanDirection[i] = Double.NaN;
                    spreadWidth[i] = Double.NaN;
                }                
            }
            double f = Double.parseDouble(dataLineTokens.nextToken());
            int i = 0;
            for(i = 0;i<frequency.length;i++)
            {
                if (frequency[i] >= f)
                {
                    break;
                }
            }
            Double v = new Double(dataLineTokens.nextToken());
            if (v.doubleValue() != 0.0)
            {
                spectralDensity[i] = v;
                meanDirection[i] = new Double(dataLineTokens.nextToken());
                spreadWidth[i] = new Double(dataLineTokens.nextToken());
            }
            //log.debug("MEAN " + fn + " " + f + " " + v);
            line++;
            fn++;
            if (fn == nFreq)
            	commit = true;
        }  

        @Override
        protected void commitData(RawInstrumentData raw)
        {
            Sample s = samples.get(ts);
            if (s == null)
            {
                s = new Sample();
                samples.put(ts, s);
                s.ts = (Timestamp) ts.clone();
            }
            s.spectralDensity_MEANDIR = spectralDensity;
            s.meanDirection = meanDirection;
            s.spreadWidth = spreadWidth;

            log.debug("Set MEANDIR " + ts + " " + meanDirection[5]);
            
            if (currentFile != null)
            {            
                boolean ok = false;

                raw.setParameterCode("MEAN_DIR");
                raw.setParameterValue(meanWaveDir);
                ok = raw.insert();

                raw.setParameterCode("MEAN_SPREAD_WIDTH");
                raw.setParameterValue(meanSpreadWidth);
                ok = raw.insert();

                array.setDataTimestamp(ts);
                array.setDepth(instrumentDepth);
                array.setInstrumentID(currentInstrument.getInstrumentID());
                array.setLatitude(currentMooring.getLatitudeIn());
                array.setLongitude(currentMooring.getLongitudeIn());
                array.setMooringID(currentMooring.getMooringID());
                array.setSourceFileID(currentFile.getDataFilePrimaryKey());
                array.setQualityCode("RAW");

                array.setParameterCode("SPECTRAL_DENSITY");            
                array.setParameterValue(spectralDensity);

                ok |= array.insert();

                array.setParameterCode("MEAN_WAVE_DIR");            
                array.setParameterValue(meanDirection);

                ok |= array.insert();

                array.setParameterCode("SPREAD_WIDTH");            
                array.setParameterValue(spreadWidth);

                ok |= array.insert();
            }
        }
        
    }

    int nDir = 0;
    double dirStep = 0;
    public class DirParser extends TriAXYSParser
    {
        Double dirSpectrum[][] = null;
        Array2DInstrumentData aid2D = new Array2DInstrumentData();

        public DirParser()
        {
            headerLength = 11;
            type = Type.DIRSPEC;
            log.debug("DirParser " + headerLength);    
        }
        @Override
        protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
        {
            String[] split = dataLine.split("=");
            String param = split[0].trim();
            String value = split[1].trim();
            
            if (param.startsWith("NUMBER OF DIRECTIONS"))
            {
                nDir = Integer.parseInt(value);
            }            
            else if (param.startsWith("DIRECTION SPACING (DEG)"))
            {
                dirStep = Double.parseDouble(value);
            }            
        }

        int f = 0;
        @Override
        protected void parseData(String dataLine) throws ParseException, NoSuchElementException
        {
            if (dirSpectrum == null)
            {
                dirSpectrum = new Double[nFreqPoints][nDir];
                for(int i=0;i<nFreqPoints;i++)
                {
                    for(int j=0;j<nDir;j++)
                    {
                        dirSpectrum[i][j] = Double.NaN;
                    }                    
                }
            }
            if (f < nFreq)
            {
                int i = 0;
                for(i = 0;i<frequency.length;i++)
                {
                    if (frequency[i] >= initialF)
                    {
                        break;
                    }
                }
                int j = 0;
                while(dataLineTokens.hasMoreElements())
                {
                    Double v = new Double(dataLineTokens.nextToken());
                    if (v.doubleValue() != 0.0)
                    {
                        dirSpectrum[i+line][j] = v;
                    }
                    j++;
                }
            	f++;
            }  
            
            line++;
            if (f == nFreq)
            	commit = true;
        }        

        @Override
        protected void commitData(RawInstrumentData raw)
        {
            Sample s = samples.get(ts);
            if (s == null)
            {
                s = new Sample();
                
                samples.put(ts, s);
                s.ts = (Timestamp) ts.clone();
            }
            s.dirSpectrum = dirSpectrum;

            log.debug("Set DIR spectrum " + ts + " " + dirSpectrum[5][2]);
            
            if (currentFile != null)
            {                        
                boolean ok = false;

                aid2D.setDataTimestamp(ts);
                aid2D.setDepth(instrumentDepth);
                aid2D.setInstrumentID(currentInstrument.getInstrumentID());
                aid2D.setLatitude(currentMooring.getLatitudeIn());
                aid2D.setLongitude(currentMooring.getLongitudeIn());
                aid2D.setMooringID(currentMooring.getMooringID());
                aid2D.setSourceFileID(currentFile.getDataFilePrimaryKey());
                aid2D.setQualityCode("RAW");

                aid2D.setParameterCode("DIR_SPECTRUM");            
                aid2D.setParameterValue(dirSpectrum);

                ok |= aid2D.insert();
            }
        }
    }
    
    public class WaveParser extends TriAXYSParser
	{
        Double zeroCrossings;
        Double averageHt;
        Double Tz;
        Double maxHt;
        Double sigHt;
        Double sigPer;
        Double Tp;
        Double Tp5;
        Double HM0;
        Double meanTheta;
        Double sigmaTheta;
        SimpleDateFormat df;        
    	
    	public WaveParser()
    	{
            headerLength = 4;

            type = Type.WAVE;
            log.debug("WaveParser " + headerLength);
            commit = false;
    	}
    	
		@Override
		protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
		{
			// TODO Auto-generated method stub

		}

		@Override
		protected void parseData(String dataLine) throws ParseException, NoSuchElementException
		{
            String[] split = dataLine.split("=");
            if (split.length < 2)
            	return;
            
            String param = split[0].trim();
            String value = split[1].trim();
            
            if (param.startsWith("Number of Zero Crossings"))
            {
            	zeroCrossings = Double.parseDouble(value);
            }
            else if (param.startsWith("Average Wave Height (Havg)"))
            {
            	averageHt = Double.parseDouble(value);
            }            
            else if (param.startsWith("Tz"))
            {
            	Tz = Double.parseDouble(value);
            }            
            else if (param.startsWith("Max Wave Height (Hmax)"))
            {
            	maxHt = Double.parseDouble(value);
            }            
            else if (param.startsWith("Significant Wave Height (Hsig)"))
            {
            	sigHt = Double.parseDouble(value);
            }            
            else if (param.startsWith("Significant Wave Period (Tsig)"))
            {
            	sigPer = Double.parseDouble(value);
            }            
            else if (param.startsWith("Mean Period"))
            {
            	Tp = Double.parseDouble(value);
            }            
            else if (param.startsWith("Tp5"))
            {
            	Tp5 = Double.parseDouble(value);
            }            
            else if (param.startsWith("Hm0"))
            {
            	HM0 = Double.parseDouble(value);
            }            
            else if (param.startsWith("Mean Magnetic Direction"))
            {
            	meanTheta = Double.parseDouble(value);
            }            
            else if (param.startsWith("Mean Spread"))
            {
            	sigmaTheta = Double.parseDouble(value);
            }            
            else if (param.startsWith("Wave Steepness"))
            {
            	commit = true;
            }            
		}

		@Override
		protected void commitData(RawInstrumentData raw)
		{
            Sample s = samples.get(ts);
            if (s == null)
            {
                s = new Sample();
                samples.put(ts, s);
                
                s.ts = (Timestamp) ts.clone();
            }
            s.zeroCrossings = zeroCrossings;
            s.averageHt = averageHt;
            s.Tz = Tz;
            s.maxHt = maxHt;
            s.sigHt = sigHt;    
            s.sigPer = sigPer;
            s.Tp = Tp;
            s.Tp5 = Tp5;
            s.HM0 = HM0;
            s.meanTheta = meanTheta;
            s.sigmaTheta = sigmaTheta;    
            
            if (currentFile != null)
            {            
                boolean ok = false;

                raw.setParameterCode("ZERO_CROSS");
                raw.setParameterValue(zeroCrossings);
                ok = raw.insert();
                raw.setParameterCode("AVG_WAVE_HEIGHT");
                raw.setParameterValue(averageHt);
                ok = raw.insert();
                raw.setParameterCode("AVG_PERIOD");
                raw.setParameterValue(Tz);
                ok = raw.insert();
                raw.setParameterCode("MAX_WAVE_HEIGHT");
                raw.setParameterValue(maxHt);
                ok = raw.insert();
                raw.setParameterCode("SIG_WAVE_HEIGHT");
                raw.setParameterValue(sigHt);
                ok = raw.insert();
                raw.setParameterCode("SIG_WAVE_PERIOD");
                raw.setParameterValue(sigPer);
                ok = raw.insert();
                raw.setParameterCode("TP");
                raw.setParameterValue(Tp);
                ok = raw.insert();
                raw.setParameterCode("TP5");
                raw.setParameterValue(Tp5);
                ok = raw.insert();
//                raw.setParameterCode("SIG_WAVE_HEIGHT");
//                raw.setParameterValue(HM0);
//                ok = raw.insert();
                raw.setParameterCode("MEAN_THETA");
                raw.setParameterValue(meanTheta);
                ok = raw.insert();
                raw.setParameterCode("SIGMA_THETA");
                raw.setParameterValue(sigmaTheta);
                ok = raw.insert();
            }
            
		}

	}
    
    public class UnknownParser extends TriAXYSParser
    {
        public UnknownParser()
        {
            headerLength = 12;
            type = Type.UNKNOWN;
            log.debug("UnknownParser " + headerLength);            
        }

        @Override
        protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
        {
        }

        @Override
        protected void parseData(String dataLine) throws ParseException, NoSuchElementException
        {
        }

        @Override
        protected void commitData(RawInstrumentData raw)
        {
        }
    
    }
    public class RawParser extends TriAXYSParser
    {
    	int sampleNo = 0;
        Double[] compass = null;
        Double[][] accel = null;
        Double[][] gyro = null; 
        SimpleDateFormat df = null;

    	public RawParser()
    	{
            headerLength = 1;
            type = Type.RAW;
            log.debug("RAW " + headerLength + " file " + processingFile);

            compass = new Double[4800];
            accel = new Double[4800][3];
            gyro = new Double[4800][3];
            
            sampleNo = 0;
            df = new SimpleDateFormat("yyyyMMddHHmm");
            try
			{
            	String tString = processingFile.substring(processingFile.lastIndexOf("/")+1);
				ts = new Timestamp(df.parse(tString).getTime() + tOffset);
			}
			catch (ParseException e)
			{
				// TODO Auto-generated catch block
				log.debug(e);
			}
            
            log.debug("RAW Timestamp " + ts);
            
            commit = false;
            
            s = 0;
    	}
    	
    	@Override
		protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
		{
			// TODO Auto-generated method stub
			
		}

    	int s = 0;
		@Override
		protected void parseData(String dataLine) throws ParseException, NoSuchElementException
		{
            dataLineTokens = new StringTokenizer(dataLine,",");

            //log.debug("DataLines tokens " + dataLineTokens.countTokens());
            
            if ((s < 4800) && (dataLineTokens.countTokens() == 8))
            {
		        int fsample = Integer.parseInt(dataLineTokens.nextToken());
		        
		        compass[s] = Double.parseDouble(dataLineTokens.nextToken());
		        accel[s][0] = Double.parseDouble(dataLineTokens.nextToken());
		        accel[s][1] = Double.parseDouble(dataLineTokens.nextToken());
		        accel[s][2] = Double.parseDouble(dataLineTokens.nextToken());
		        gyro[s][0] = Double.parseDouble(dataLineTokens.nextToken());
		        gyro[s][1] = Double.parseDouble(dataLineTokens.nextToken());
		        gyro[s][2] = Double.parseDouble(dataLineTokens.nextToken());		
		        
		        //log.debug("sample " + s + " compass " + compass[s]);
		        
		        s++;
		        commit = false;
            }
            else // 1502150400,SOFS-5,+000.0,,13.74,4.0,4800,120,120,1,04811,1.01.0687,,53,0,0.000,+00.0
            {
	        	commit = true;
            }
		}

		@Override
		protected void commitData(RawInstrumentData raw)
		{
            Sample s = samples.get(ts);
            if (s == null)
            {
                s = new Sample();
                samples.put(ts, s);
                s.ts = (Timestamp) ts.clone();
            }
            s.compass = compass;
            s.accel = accel;
            s.gyro = gyro;

            log.debug("Set RAW " + ts + " " + compass[5]);
		}
    }
    
    int nFreq = -1;
    int nFreqPoints = 129;
    double initialF = Double.NaN;
    double fSpacing = Double.NaN;
    double frequency[] = null;
    
    @Override
    protected boolean isHeader(String dataLine)
    {
        if (dataLine.startsWith("TRIAXYS BUOY DATA REPORT") || dataLine.startsWith("Sample,Comp(deg)"))
        {
            p = null;
            return true;
        }
        if (p == null)
        {
            return true;
        }
        
        return header < p.headerLength;
    }

    @Override
    protected void parseHeader(String headerLine) throws ParseException, NoSuchElementException
    {
        TimeZone.setDefault(tz);
        sdf1.setTimeZone(tz);
        sdf2.setTimeZone(tz);
     
        commit = true;

        log.debug("Header " + header + " : " + headerLine);
        if (headerLine.startsWith("TRIAXYS BUOY DATA REPORT"))
        {
            header = 0;
            p = null;
            frequency = null;
            
            return;
        }
        else if (headerLine.startsWith("Date\tYear\tJulian Date"))
        {
            header = 0;
            p = new SummaryParser();
        }
        else if (headerLine.startsWith("Sample,Comp(deg)"))
        {
        	if (raw)
        	{
            header = 0;
            p = new RawParser();
        	}
        	else
        		p = new UnknownParser();
        }
        
        header++;
        
        if (p != null)
        {
            p.parseHeader(headerLine);            
        }
        if (p instanceof SummaryParser)
        {
            return;
        }
        if (p instanceof RawParser)
        {
            return;
        }
        if (p instanceof UnknownParser)
        {
            return;
        }
        
        String[] split = headerLine.split("=");
        if (split.length < 2)
        	return;
        
        String param = split[0].trim();
        String value = split[1].trim();
        if (param.startsWith("TYPE"))
        {
            if (value.startsWith("DIRECTIONAL SPECTRUM")) 
            {
                p = new DirParser();
            }
            else if (value.startsWith("NON-DIRECTIONAL SPECTRUM")) 
            {
                p = new NonDirParser();
            }
            else if (value.startsWith("MEAN DIRECTION")) 
            {
                p = new MeanDirParser();
            }
            else if (value.startsWith("WAVE STATISTICS")) 
            {
                p = new WaveParser();
            }
            else 
            {
                p = new UnknownParser();
            }                
        }
        else if (param.startsWith("DATE"))
        {
        	Date t;
        	try
        	{
        		t = sdf1.parse(value);
        	}
        	catch (ParseException pe)
        	{
        		t = sdf2.parse(value);        		
        	}
            ts = new Timestamp(t.getTime() + tOffset);                
        }
        else if (param.startsWith("NUMBER OF FREQUENCIES"))
        {
            nFreq = Integer.parseInt(value);
        }
        else if (param.startsWith("INITIAL FREQUENCY (Hz)"))
        {
            initialF = Double.parseDouble(value);
        }
        else if (param.startsWith("FREQUENCY SPACING (Hz)"))
        {
            fSpacing = Double.parseDouble(value);
        }
        else if (param.startsWith("COLUMN"))
        {
            // ignore these lines
        }
    }
    
    StringTokenizer dataLineTokens;
    
    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        if (frequency == null)
        {
            frequency = new double[nFreqPoints];
            for(int i=0;i<nFreqPoints;i++)
            {
                frequency[i] = i * 0.01;
            }
        }
        dataLineTokens = new StringTokenizer(dataLine," ");
        
        RawInstrumentData row = null;
        if (currentFile != null)
        {
            row = new RawInstrumentData();

            row.setDataTimestamp(ts);
            row.setDepth(instrumentDepth);
            row.setInstrumentID(currentInstrument.getInstrumentID());
            row.setLatitude(currentMooring.getLatitudeIn());
            row.setLongitude(currentMooring.getLongitudeIn());
            row.setMooringID(currentMooring.getMooringID());
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");
        }
        
        // log.debug("Data line " + p.line + " " + dataLine);
        
        p.parseData(dataLine);
        
        //if (commit)
        {
            log.debug("Data Record " + ts);
            
            p.commitData(row);
        }
    }
    
    public void createNetCDF() throws ParseException
    {
        log.debug("Data Start " + samples.firstKey() + " end " + samples.lastKey());

        Mooring m = Mooring.selectByMooringID(mooring);
        
        // Create the file.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");        
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        NetCDFfile ndf = new NetCDFfile();
        ndf.setMooring(m);
        ndf.setAuthority("IMOS");
        ndf.setFacility("ABOS-ASFS");
        ndf.setMultiPart(true);
        
        ArrayList<Instrument> insts = Instrument.selectInstrumentsForMooring(m.getMooringID()); 
        Instrument inst = Instrument.selectByInstrumentID(1620);
        for(Instrument ix : insts)
        {
    		log.debug("Instrument " + ix);
    		
        	if (ix.getModel().contains("TriAxys"))
        	{
        		inst = ix;        
        		break;
        	}
        }
		log.info("Instrument " + inst);
		
        Timestamp dataStartTime = m.getTimestampIn(); // TODO: probably should come from data, esp for part files
        Timestamp dataEndTime = m.getTimestampOut();        

        String filename = ndf.getFileName(inst, dataStartTime, dataEndTime, "raw", "RW", null);
        
        try
        {
            // Create new netcdf-3 file with the given filename
            ndf.createFile(filename);
            ndf.writeGlobalAttributes();
            ndf.addGroupAttribute(null, new Attribute("time_coverage_start", sdf.format(samples.firstKey())));
            ndf.addGroupAttribute(null, new Attribute("time_coverage_end", sdf.format(samples.lastKey())));
            
            ndf.addGroupAttribute(null, new Attribute("time_deployment_start", sdf.format(m.getTimestampIn())));
            ndf.addGroupAttribute(null, new Attribute("time_deployment_end", sdf.format(m.getTimestampOut())));
            ndf.addGroupAttribute(null, new Attribute("file_version", "Level 0 - RAW data"));
            //ndf.dataFile.addGroupAttribute(null, new Attribute("instrument", "AXYS Technologies Inc, TRIAXYS OEM Directional Wave Sensor, TAS04811"));
            ndf.addGroupAttribute(null, new Attribute("instrument", "AXYS Technologies Inc, TRIAXYS OEM Directional Wave Sensor , " + inst.getSerialNumber()));
            ndf.addGroupAttribute(null, new Attribute("title", "SOFS TriAXYS Wave height, direction, and spectra"));
            ndf.addGroupAttribute(null, new Attribute("site_code", "SOTS"));
            ndf.addGroupAttribute(null, new Attribute("deployment_code", m.getMooringID()));
            //ndf.dataFile.addGroupAttribute(null, new Attribute("abstract", "SOFS is an observing platform in the Sub-Antarctic Zone, approximately 350 nautical miles southwest of Tasmania.\nIt obtains frequent measurements of the surface and deep ocean properties that control the transfer of heat, moisture, energy and CO2 between the atmosphere and the upper ocean\nto improve understanding of climate and carbon processes.\nThe mooring was deployed in May 2013 at (-46.7S, 142E) and recovered in October 2013\nThis wave data was collected using a AXYS technologies TriAXYS OEM wave sensor deployed on a 3m WHOI surface float"));
            ndf.addGroupAttribute(null, new Attribute("abstract", "SOFS is an observing platform in the Sub-Antarctic Zone, approximately 350 nautical miles southwest of Tasmania.\nIt obtains frequent measurements of the surface and deep ocean properties that control the transfer of heat, moisture, energy and CO2 between the atmosphere and the upper ocean\nto improve understanding of climate and carbon processes.\nThe mooring was deployed in November 2011 at (-46.7S, 142E) and recovered in July 2012\nThis wave data was collected using a AXYS technologies TriAXYS OEM wave sensor deployed on a 3m WHOI surface float"));
            ndf.addGroupAttribute(null, new Attribute("keywords", "OCEANS > OCEAN WAVES > SIGNIFICANT WAVE HEIGHT\nOCEANS > OCEAN WAVES > WAVE HEIGHT\nOCEANS > OCEAN WAVES > WAVE SPECTRA\nOCEANS > OCEAN WAVES > WAVE FREQUENCY\nOCEANS > OCEAN WAVES > WAVE PERIOD"));
            ndf.addGroupAttribute(null, new Attribute("geospatial_vertical_max", new Double(0)));
            ndf.addGroupAttribute(null, new Attribute("geospatial_vertical_min", new Double(0)));
            ndf.addGroupAttribute(null, new Attribute("comment", "RAW heave, horizontal displacement and velocities are also avaliable upon request"));

            
            ndf.createCoordinateVariables(samples.size());

            ndf.writeCoordinateVariables(new ArrayList(samples.keySet()));
            ndf.writeCoordinateVariableAttributes();
            if (mooring.compareTo("SOFS-5-2015") == 0)
            {
            	ndf.vTime.addAttribute(new Attribute ("comment", "time adjustument, added 1 day as incorrect time setting"));
            }
            
//            Dimension timeDim = ndf.dataFile.addDimension(null, "TIME", Samples.size());
            Dimension frequencyDim = null;
            Dimension dirDim = null;
            List<Dimension> dimSpec = null;
            List<Dimension> dimSpecDir = null;
            if (nFreq > 0)
            {
                frequencyDim = ndf.dataFile.addDimension(null, "frequency", nFreq);
                dimSpec = new ArrayList<Dimension>();
                dimSpec.add(ndf.timeDim);
                dimSpec.add(frequencyDim);			
                dirDim = ndf.dataFile.addDimension(null, "direction", nDir);
                dimSpecDir = new ArrayList<Dimension>();
                dimSpecDir.add(ndf.timeDim);
                dimSpecDir.add(frequencyDim);			
                dimSpecDir.add(dirDim);			
            }
            
            List<Dimension> dimSample = null;
            List<Dimension> dimVector = null;
            if (raw)
            {
	            Dimension sampleDim = ndf.dataFile.addDimension(null, "sample", 4800);
	            Dimension vectorDim = ndf.dataFile.addDimension(null, "vector", 3);
	            dimSample = new ArrayList<Dimension>();
	            dimSample.add(ndf.timeDim);
	            dimSample.add(sampleDim);			
	            dimVector = new ArrayList<Dimension>();
	            dimVector.add(ndf.timeDim);
	            dimVector.add(sampleDim);			
	            dimVector.add(vectorDim);
            }
            
            Variable vFrequency = null;
            Variable vDir = null;
            
            if (nFreq > 0)
            {
                vFrequency = ndf.dataFile.addVariable(null, "frequency", DataType.FLOAT, "frequency");
                vFrequency.addAttribute(new Attribute("long_name", "spectral_frequency"));
                vFrequency.addAttribute(new Attribute("units", "Hz"));   

                vDir = ndf.dataFile.addVariable(null, "direction", DataType.FLOAT, "direction");
                vDir.addAttribute(new Attribute("long_name", "spectral_direction"));
                vDir.addAttribute(new Attribute("units", "degrees_true"));   
            }

//            Variable vTime = ndf.dataFile.addVariable(null, "TIME", DataType.INT, "TIME");
            // Define units attributes for coordinate vars. This attaches a
            // text attribute to each of the coordinate variables, containing
            // the units.
//            vTime.addAttribute(new Attribute("units", "seconds since 2000-01-01T00:00:00Z"));
//            vTime.addAttribute(new Attribute("axis", "T"));
//            vTime.addAttribute(new Attribute("calendar", "gregorian"));

            
//            Variable vSWH = ndf.dataFile.addVariable(null, "H13", DataType.FLOAT, "TIME");
//            vSWH.addAttribute(new Attribute("units", "metre"));
//            vSWH.addAttribute(new Attribute("standard_name", "sea_surface_wave_significant_height"));
//            vSWH.addAttribute(new Attribute("_FillValue", Float.NaN));
            
            Variable vSWPer = ndf.dataFile.addVariable(null, "T13", DataType.FLOAT, "TIME");
            vSWPer.addAttribute(new Attribute("units", "sec"));
            vSWPer.addAttribute(new Attribute("standard_name", "sea_surface_wind_wave_period"));
            vSWPer.addAttribute(new Attribute("_FillValue", Float.NaN));
            
            Variable vAvgH = ndf.dataFile.addVariable(null, "HAV", DataType.FLOAT, "TIME");
            vAvgH.addAttribute(new Attribute("units", "metre"));
            vAvgH.addAttribute(new Attribute("long_name", "average_wave_height"));
            vAvgH.addAttribute(new Attribute("_FillValue", Float.NaN));
            
            Variable vAvgPer = ndf.dataFile.addVariable(null, "TZ", DataType.FLOAT, "TIME");
            vAvgPer.addAttribute(new Attribute("units", "sec"));
            vAvgPer.addAttribute(new Attribute("long_name", "average_wave_period"));
            vAvgPer.addAttribute(new Attribute("_FillValue", Float.NaN));
            vAvgPer.addAttribute(new Attribute("comment", "Estimated period from spectral moments m0 and m2, where Tz = SQRT(m0/m2)"));

            Variable vMaxH = ndf.dataFile.addVariable(null, "HMAX", DataType.FLOAT, "TIME");
            vMaxH.addAttribute(new Attribute("long_name", "max_wave_height"));
            vMaxH.addAttribute(new Attribute("units", "metre"));
            vMaxH.addAttribute(new Attribute("_FillValue", Float.NaN));
            vMaxH.addAttribute(new Attribute("comment", "Maximum zero down-crossing wave height (trough to peak)"));
            
            Variable vTp = ndf.dataFile.addVariable(null, "TP", DataType.FLOAT, "TIME");
            vTp.addAttribute(new Attribute("units", "sec"));
            vTp.addAttribute(new Attribute("long_name", "peak_wave_period"));
            vTp.addAttribute(new Attribute("_FillValue", Float.NaN));
            
            Variable vHM0 = ndf.dataFile.addVariable(null, "VAVH", DataType.FLOAT, "TIME");
            vHM0.addAttribute(new Attribute("units", "m"));
            vHM0.addAttribute(new Attribute("standard_name", "sea_surface_wave_significant_height"));
            vHM0.addAttribute(new Attribute("_FillValue", Float.NaN));

            Variable vTp5 = ndf.dataFile.addVariable(null, "TP5", DataType.FLOAT, "TIME");
            vTp5.addAttribute(new Attribute("units", "sec"));
            vTp5.addAttribute(new Attribute("long_name", "peak_wave_period"));
            vTp5.addAttribute(new Attribute("comment", "Peak wave period in seconds as computed by the Read method"));
            vTp5.addAttribute(new Attribute("_FillValue", Float.NaN));
            
            Variable vZC = ndf.dataFile.addVariable(null, "VAVT", DataType.FLOAT, "TIME");
            vZC.addAttribute(new Attribute("units", "count"));
            vZC.addAttribute(new Attribute("standard_name", "sea_surface_wave_zero_upcrossing_period"));
            vZC.addAttribute(new Attribute("_FillValue", Float.NaN));
            
            Variable vMeanTheta = ndf.dataFile.addVariable(null, "WAV_DIR", DataType.FLOAT, "TIME");
            vMeanTheta.addAttribute(new Attribute("units", "degrees_true"));
            vMeanTheta.addAttribute(new Attribute("_FillValue", Float.NaN));
            vMeanTheta.addAttribute(new Attribute("long_name", "mean_wave_direction"));
            vMeanTheta.addAttribute(new Attribute("comment", "Overall mean wave direction in degrees obtained by averaging the mean wave angle q over all frequencies with weighting function S(f). q is calculated by the KVH method"));

            Variable vSigmaTheta = ndf.dataFile.addVariable(null, "SSDS", DataType.FLOAT, "TIME");
            vSigmaTheta.addAttribute(new Attribute("long_name", "mean_wave_direction_spread"));
            vSigmaTheta.addAttribute(new Attribute("units", "degrees"));
            vSigmaTheta.addAttribute(new Attribute("_FillValue", Float.NaN));
            vSigmaTheta.addAttribute(new Attribute("comment", "Overall directional spreading width in degrees obtained by averaging the spreading width sigma theta, sq, over all frequencies with weighting function S(f). sq is calculated by the KVH method."));

            Variable vSpectralDensity = null;
            Variable vMeanWaveDir = null;
            Variable vSpreadWidth = null;
            Variable vDirSpectrum = null;
            if (nFreq > 0)
            {
                vSpectralDensity = ndf.dataFile.addVariable(null, "spectral_density", DataType.FLOAT, dimSpec);
                vSpectralDensity.addAttribute(new Attribute("long_name", "wave_spectral_density"));
                vSpectralDensity.addAttribute(new Attribute("units", "m^2/Hz"));
                vSpectralDensity.addAttribute(new Attribute("_FillValue", Float.NaN));
                
                vMeanWaveDir = ndf.dataFile.addVariable(null, "mean_wave_dir", DataType.FLOAT, dimSpec);
                vMeanWaveDir.addAttribute(new Attribute("long_name", "mean_wave_direction"));
                vMeanWaveDir.addAttribute(new Attribute("units", "degrees_true"));
                vMeanWaveDir.addAttribute(new Attribute("_FillValue", Float.NaN));

                vSpreadWidth = ndf.dataFile.addVariable(null, "mean_direction_spread", DataType.FLOAT, dimSpec);
                vSpreadWidth.addAttribute(new Attribute("long_name", "mean_direction_spread"));
                vSpreadWidth.addAttribute(new Attribute("units", "degrees"));
                vSpreadWidth.addAttribute(new Attribute("_FillValue", Float.NaN));

                vDirSpectrum = ndf.dataFile.addVariable(null, "directional_sprectrum", DataType.FLOAT, dimSpecDir);
                vDirSpectrum.addAttribute(new Attribute("long_name", "directional_sprectrum"));
                vDirSpectrum.addAttribute(new Attribute("units", "degrees_true"));
                vDirSpectrum.addAttribute(new Attribute("_FillValue", Float.NaN));
            }
            Variable vCompass = null;
            Variable vAccel = null;
            Variable vGyro = null;
            if (raw)
            {
	            vCompass = ndf.dataFile.addVariable(null, "compass", DataType.FLOAT, dimSample);
	            vCompass.addAttribute(new Attribute("long_name", "compass_direction"));
	            vCompass.addAttribute(new Attribute("units", "degrees"));
	            vCompass.addAttribute(new Attribute("_FillValue", Float.NaN));
	            vAccel = ndf.dataFile.addVariable(null, "acceleration", DataType.FLOAT, dimVector);
	            vAccel.addAttribute(new Attribute("long_name", "acceleration_vector"));
	            vAccel.addAttribute(new Attribute("units", "m/s/s"));
	            vAccel.addAttribute(new Attribute("_FillValue", Float.NaN));
	            vGyro = ndf.dataFile.addVariable(null, "gyro", DataType.FLOAT, dimVector);
	            vGyro.addAttribute(new Attribute("long_name", "rotation_speed"));
	            vGyro.addAttribute(new Attribute("units", "rad/s"));
	            vGyro.addAttribute(new Attribute("_FillValue", Float.NaN));
            }

//            Array dataTime = Array.factory(DataType.INT, new int[] { timeDim.getLength() });

//            Array dataSWH = Array.factory(DataType.FLOAT, new int[] { ndf.timeDim.getLength() });
            Array dataSWP = Array.factory(DataType.FLOAT, new int[] { ndf.timeDim.getLength() });
            Array dataAvgH = Array.factory(DataType.FLOAT, new int[] { ndf.timeDim.getLength() });
            Array dataAvgPer = Array.factory(DataType.FLOAT, new int[] { ndf.timeDim.getLength() });
            Array dataMaxH = Array.factory(DataType.FLOAT, new int[] { ndf.timeDim.getLength() });
            Array dataTp = Array.factory(DataType.FLOAT, new int[] { ndf.timeDim.getLength() });
            Array dataHM0 = Array.factory(DataType.FLOAT, new int[] { ndf.timeDim.getLength() });
            Array dataTp5 = Array.factory(DataType.FLOAT, new int[] { ndf.timeDim.getLength() });
            Array dataZC = Array.factory(DataType.FLOAT, new int[] { ndf.timeDim.getLength() });
            Array dataMeanTheta = Array.factory(DataType.FLOAT, new int[] { ndf.timeDim.getLength() });
            Array dataSigmaTheta = Array.factory(DataType.FLOAT, new int[] { ndf.timeDim.getLength() });

            Array dataFrequency = null;
            Array dataDirection = null;
            Index frequencyIndex = null;
            Index frequencyDirIndex = null;

            Array dataSpectralDensity = null;
            Array dataMeanWaveDir = null;
            Array dataSpreadWidth = null;
            Array dataDirSpectrum = null;
            
            if (nFreq > 0)
            {
                dataFrequency = Array.factory(DataType.FLOAT, new int[] { frequencyDim.getLength() });
                int[] specDim = new int[] { ndf.timeDim.getLength(), frequencyDim.getLength()};

                for(int i=0;i<frequencyDim.getLength();i++)
                {
                    dataFrequency.setFloat(i, (float)(i * fSpacing));                    
                }
                frequencyIndex = new Index2D(specDim);
                dataSpectralDensity = Array.factory(DataType.FLOAT, specDim );
                dataMeanWaveDir = Array.factory(DataType.FLOAT, specDim );
                dataSpreadWidth = Array.factory(DataType.FLOAT, specDim );
                
                dataDirection = Array.factory(DataType.FLOAT, new int[] { dirDim.getLength() });
                int[] specDirDim = new int[] { ndf.timeDim.getLength(), frequencyDim.getLength(), dirDim.getLength()};
                for(int i=0;i<dirDim.getLength();i++)
                {
                    dataDirection.setFloat(i, (float)(i * dirStep));                    
                }
                dataDirSpectrum = Array.factory(DataType.FLOAT, specDirDim );    
                frequencyDirIndex = dataDirSpectrum.getIndex();
            }
            Array dataCompass = null;
            Array dataAccel = null;
            Array dataGyro = null;
            if (raw)
            {
            	dataCompass = Array.factory(DataType.FLOAT, new int[] { ndf.timeDim.getLength(), 4800} );
            	dataAccel = Array.factory(DataType.FLOAT, new int[] { ndf.timeDim.getLength(), 4800, 3} );
            	dataGyro = Array.factory(DataType.FLOAT, new int[] { ndf.timeDim.getLength(), 4800, 3} );
            }

            // Write the coordinate variable data. 
            ndf.create();
            
            //long tz = sdf.parse("2000-01-01T000000").getTime();

            //Date ts = null;

            int i = 0;
            Index vectorIndex = null;
            Index sampleIndex = null;
            if (raw)
            {
            	vectorIndex = dataAccel.getIndex();
            	sampleIndex = dataCompass.getIndex();
            }

            for (Sample s : samples.values())
            {                
//                dataTime.setInt(i, (int) ((s.ts.getTime() - tz) / 1000));

//                dataSWH.setFloat(i, s.sigHt.floatValue());
                dataSWP.setFloat(i, s.sigPer.floatValue());
                dataAvgH.setFloat(i, s.averageHt.floatValue());
                dataAvgPer.setFloat(i, s.Tz.floatValue());
                dataMaxH.setFloat(i, s.maxHt.floatValue());
                dataTp.setFloat(i, s.Tp.floatValue());
                dataHM0.setFloat(i, s.HM0.floatValue());
                dataTp5.setFloat(i, s.Tp5.floatValue());
                dataZC.setFloat(i, s.zeroCrossings.floatValue());
                dataMeanTheta.setFloat(i, s.meanTheta.floatValue());
                dataSigmaTheta.setFloat(i, s.sigmaTheta.floatValue());
                if (nFreq > 0)
                {
                    for(int j=0;j<nFreq;j++)
                    {
                        frequencyIndex.set(i, j);
                        if (s.spectralDensity != null)
                        {
                            dataSpectralDensity.setFloat(frequencyIndex, s.spectralDensity[j].floatValue());
                            dataMeanWaveDir.setFloat(frequencyIndex, s.meanDirection[j].floatValue());
                            dataSpreadWidth.setFloat(frequencyIndex, s.spreadWidth[j].floatValue());
                            for(int k=0;k<nDir;k++)
                            {
                                frequencyDirIndex.set(i, j, k);
                            	//log.debug("Freq Dir Index " + frequencyDirIndex);
                                dataDirSpectrum.setFloat(frequencyDirIndex, s.dirSpectrum[j][k].floatValue());                                
                            }
                        }
                        else
                        {
                            dataSpectralDensity.setFloat(frequencyIndex, Float.NaN);                            
                        }
                    }
                }
                if (raw)
                {
	                if (s.accel != null)
	                {
	                	for (int j = 0;j<s.accel.length;j++)
	                	{
	                		sampleIndex.set(i, j);
	                		dataCompass.setFloat(sampleIndex, s.compass[j].floatValue());
	                		for (int k=0;k<3;k++)
	                		{
		                		vectorIndex.set(i, j, k);
		                		//log.debug("Sample Index " + vectorIndex);
		                		dataAccel.setFloat(vectorIndex, s.accel[j][k].floatValue());
		                		dataGyro.setFloat(vectorIndex, s.gyro[j][k].floatValue());
	                		}
	                	}
	                }
                }
                
                i++;
            }

            ndf.dataFile.write(ndf.vTime, ndf.times);
            ndf.writePosition(m.getLatitudeIn(), m.getLongitudeIn());
            if (nFreq > 0)
            {
                ndf.dataFile.write(vFrequency, dataFrequency);
                ndf.dataFile.write(vDir, dataDirection);
            }
            
//            ndf.dataFile.write(vSWH, dataSWH);
            ndf.dataFile.write(vSWPer, dataSWP);
            ndf.dataFile.write(vAvgH, dataAvgH);
            ndf.dataFile.write(vAvgPer, dataAvgPer);
            ndf.dataFile.write(vMaxH, dataMaxH);
            ndf.dataFile.write(vTp, dataTp);
            ndf.dataFile.write(vHM0, dataHM0);
            ndf.dataFile.write(vTp5, dataTp5);
            ndf.dataFile.write(vZC, dataZC);
            ndf.dataFile.write(vMeanTheta, dataMeanTheta);
            ndf.dataFile.write(vSigmaTheta, dataSigmaTheta);
            
            if (raw)
            {
	            ndf.dataFile.write(vCompass, dataCompass);
	            ndf.dataFile.write(vAccel, dataAccel);
	            ndf.dataFile.write(vGyro, dataGyro);
            }

            if (nFreq > 0)
            {
                ndf.dataFile.write(vSpectralDensity, dataSpectralDensity);
                ndf.dataFile.write(vMeanWaveDir, dataMeanWaveDir);
                ndf.dataFile.write(vSpreadWidth, dataSpreadWidth);
                ndf.dataFile.write(vDirSpectrum, dataDirSpectrum);
            }

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
            if (null != ndf.dataFile)
            {
                try
                {
                    ndf.dataFile.close();
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }
        }
        log.debug("*** SUCCESS writing file " + filename);        
    }
    
    public static void main(String args[])
    {
        String $HOME = System.getProperty("user.home");
        PropertyConfigurator.configure("log4j.properties");
        Common.build("ABOS.properties");
        
        TriAXYSDataParser p = new TriAXYSDataParser(args[0]);
        
        int i = 1;
        
        for(;i<args.length;i++)
        {
            String s = args[i];
            p.processFile(new File(s)); 
        }
        try
        {
            p.createNetCDF();
        }
        catch (ParseException ex)
        {
            log.error(ex);
        }
        
    }

}
