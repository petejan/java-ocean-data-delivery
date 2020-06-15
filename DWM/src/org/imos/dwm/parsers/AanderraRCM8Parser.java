/*
 * This code is copyright Peter Wiley 1998-2008
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 *
 */

package org.imos.dwm.parsers;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.imos.dwm.dbms.RawInstrumentData;

/**
 *
 * @author peter
 */
public class AanderraRCM8Parser  extends AbstractDataParser
{
    // Example File:
    //
    //MOORING             : SAZ47-12                                                                                       
    //INSTRUMENT          : Aanderaa RCM8-07773                                                                            
    //POSITION            : 46 50.01 S  141 39.41 E                                                                        
    //INSTRUMENT DEPTH    : 1232 (m) (when mooring vertical)                                                               
    //INSTRUMENT PRESSURE : 1246.3 (dbar) (when mooring vertical)                                                          
    //BOTTOM DEPTH        : 4599 (m)                                                                                       
    //MAGNETIC DECLINATION APPLIED :  13.12 (deg)                                                                          
    //COMMENT :                                                                                                            
    //COMMENT :                                                                                                            
    //COMMENT :                                                                                                            
    //COMMENT :                                                                                                            
    //COMMENT :                                                                                                            
    //----time for speed,dir,u,v----                                 --------time for p,t,c--------                        
    //decimaltime date and time(UTC)  speed   dir  E comp  N comp    decimaltime date and time(UTC) press.   temp.   cond. 
    //   (UTC)   dd mm yyyy hh mm ss (cm/s) (degT) (cm/s)  (cm/s)       (UTC)   dd mm yyyy hh mm ss (dbar)  (degC)  (mS/cm)
    //4653.00015 28 09 2009 00 00 13 -999.00 -999 -999.00 -999.00    4653.01057 28 09 2009 00 15 13 -999.0 -999.00 -999.000
    //4653.75016 28 09 2009 18 00 14    4.88  286   -4.69    1.34    4653.76058 28 09 2009 18 15 14 1249.7    3.70 -999.000

    protected SimpleDateFormat dateParser = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    
    public AanderraRCM8Parser()
    {
        super();
        dateParser.setTimeZone(tz);
    }
    
    @Override
    protected boolean isHeader(String dataLine)
    {
        //
        // a valid data line has the format
        // 09,07,2010,12, 0, 0,108.50,-11.20,-112.30
        //
        // so anything else is a header
        //
        char c = dataLine.charAt(0);
        if(! Character.isDigit(c) )
        {
            //
            // it is a header
            //
            return true;
        }
        else
            return false;
    }

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        //
        // don't care about the headers
        //
    }

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String speedDecimalTimeString;
        String speedYearString;
        String speedMonthString;
        String speedDayString;
        String speedHourString;
        String speedMinuteString;
        String speedSecondString;
        
        String speedString;
        String directionString;
        String eastString;
        String northString;
        
        String pressureDecimalTimeString;
        String pressureYearString;
        String pressureMonthString;
        String pressureDayString;
        String pressureHourString;
        String pressureMinuteString;
        String pressureSecondString;
        
        String pressureString;
        String temperatureString;
        String CNDCString;
        
        Timestamp currentDataTimestamp = null;
        Timestamp pressureDataTimestamp = null;
        
        Double speedValue = null;
        Double directionValue = null;
        Double eastValue = null;
        Double northValue = null;
        
        Double pressureValue = null;
        Double temperatureValue = null;
        Double CNDCValue = null;
        
        String constructTimestamp;

        StringTokenizer st = new StringTokenizer(dataLine," ");
        try
        {
            speedDecimalTimeString = st.nextToken();
            speedDayString = st.nextToken();
            speedMonthString = st.nextToken();
            speedYearString = st.nextToken();
            speedHourString = st.nextToken();
            speedMinuteString = st.nextToken();
            speedSecondString = st.nextToken();
            
            speedString  = st.nextToken();
            directionString  = st.nextToken();
            eastString = st.nextToken();
            northString = st.nextToken();
            
            pressureDecimalTimeString = st.nextToken();
            pressureDayString = st.nextToken();
            pressureMonthString = st.nextToken();
            pressureYearString = st.nextToken();
            pressureHourString = st.nextToken();
            pressureMinuteString = st.nextToken();
            pressureSecondString = st.nextToken();
           
            pressureString  = st.nextToken();
            temperatureString = st.nextToken();
            CNDCString = st.nextToken();
            
            constructTimestamp = speedYearString.trim() 
                                + "/" 
                                + speedMonthString.trim()
                                + "/"
                                + speedDayString.trim()
                                + " "
                                + speedHourString.trim()
                                + ":"
                                + speedMinuteString.trim()
                                + ":"
                                + speedSecondString.trim()
                                ;

            try
            {
                java.util.Date d = dateParser.parse(constructTimestamp);
                currentDataTimestamp = new Timestamp(d.getTime());
            }
            catch(ParseException pex)
            {
                throw new ParseException("Timestamp parse failed for text '" + constructTimestamp + "'",0);
            }
            
            constructTimestamp = pressureYearString.trim() 
                                + "/" 
                                + pressureMonthString.trim()
                                + "/"
                                + pressureDayString.trim()
                                + " "
                                + pressureHourString.trim()
                                + ":"
                                + pressureMinuteString.trim()
                                + ":"
                                + pressureSecondString.trim()
                                ;

            try
            {
                java.util.Date d = dateParser.parse(constructTimestamp);
                pressureDataTimestamp = new Timestamp(d.getTime());
            }
            catch(ParseException pex)
            {
                throw new ParseException("Timestamp parse failed for text '" + constructTimestamp + "'",0);
            }

            speedValue = getDouble(speedString);
            directionValue = getDouble(directionString);
            eastValue = getDouble(eastString);
            northValue = getDouble(northString);
            pressureValue = getDouble(pressureString);
            CNDCValue = getDouble(CNDCString);
            temperatureValue = getDouble(temperatureString);
                        
            //
            // ok, we have parsed out the values we need, can now construct the raw data class
            //
            RawInstrumentData row = new RawInstrumentData();

            row.setDataTimestamp(currentDataTimestamp);
            row.setDepth(instrumentDepth);
            row.setInstrumentID(currentInstrument.getInstrumentID());
            row.setLatitude(currentMooring.getLatitudeIn());
            row.setLongitude(currentMooring.getLongitudeIn());
            row.setMooringID(currentMooring.getMooringID());
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            boolean ok = false;
            if (speedString.compareTo("-999.00") != 0)
            {
                row.setParameterCode("SPEED_VALUE");
                row.setParameterValue(speedValue * 0.01); // convert to m/s

                ok = row.insert();
            }
            if (directionString.compareTo("-999.00") != 0)
            {            
                row.setParameterCode("DIRECTION_VALUE");
                row.setParameterValue(directionValue);

                ok = row.insert();
            }
            if (eastString.compareTo("-999.00") != 0)
            {
                row.setParameterCode("UCUR");
                row.setParameterValue(eastValue * 0.01);

                ok = row.insert();
            }
            if (northString.compareTo("-9999.00") != 0)
            {            
                row.setParameterCode("VCUR");
                row.setParameterValue(northValue * 0.01);
            
                ok = row.insert();
            }
            
            //
            // now for the pressure/temperature/CNDC data
            // note this has a different timestamp and they are NOT aligned
            // with the current spped/direction data!!!!
            //
            
            row.setDataTimestamp(pressureDataTimestamp);
            
            if (pressureString.compareTo("-999.0") != 0)
            {
                row.setParameterCode("PRES");
                row.setParameterValue(pressureValue);

                ok = row.insert();
            }
            
            if (temperatureString.compareTo("-999.00") != 0)
            {
                row.setParameterCode("TEMP");
                row.setParameterValue(temperatureValue);

                ok = row.insert();
            }

            if (CNDCString.compareTo("-999.000") != 0)
            {
                row.setParameterCode("CNDC");
                row.setParameterValue(CNDCValue);

                ok = row.insert();
            }

        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

}
