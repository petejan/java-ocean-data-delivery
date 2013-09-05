/*
 * This code is copyright Peter Wiley 1998-2008
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 *
 */

package org.imos.abos.parsers;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.imos.abos.dbms.RawInstrumentData;

/**
 *
 * @author peter
 */
public class AanderraRCM5Parser extends AbstractDataParser
{
    
    protected SimpleDateFormat dateParser = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    
    public AanderraRCM5Parser()
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
        
        String averageSpeedString;
        String pressureString;
        String temperatureString;
        String CNDCString;
        
        Timestamp currentDataTimestamp = null;
        Timestamp pressureDataTimestamp = null;
        
        Double speedValue = null;
        
        Double averageSpeedValue = null;
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
            //directionString  = st.nextToken();
            //eastString = st.nextToken();
            //northString = st.nextToken();
            
            pressureDecimalTimeString = st.nextToken();
            pressureDayString = st.nextToken();
            pressureMonthString = st.nextToken();
            pressureYearString = st.nextToken();
            pressureHourString = st.nextToken();
            pressureMinuteString = st.nextToken();
            pressureSecondString = st.nextToken();
           
            averageSpeedString  = st.nextToken();
            directionString  = st.nextToken();
            eastString = st.nextToken();
            northString = st.nextToken();
            
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

            try
            {
                speedValue = new Double(speedString.trim());
            }
            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(speedString.trim());
                    speedValue = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + speedString + "'",0);
                }
            }
            
            try
            {
                averageSpeedValue = new Double(averageSpeedString.trim());
            }
            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(averageSpeedString.trim());
                    averageSpeedValue = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + averageSpeedString + "'",0);
                }
            }
            
            try
            {
                directionValue = new Double(directionString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(directionString.trim());
                    directionValue = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + directionString + "'",0);
                }
            }
            
            try
            {
                eastValue = new Double(eastString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(eastString.trim());
                    eastValue = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + eastString + "'",0);
                }
            }
            
            try
            {
                northValue = new Double(northString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(northString.trim());
                    northValue = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + northString + "'",0);
                }
            }

            try
            {
                pressureValue = new Double(pressureString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(pressureString.trim());
                    pressureValue = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + pressureString + "'",0);
                }
            }
            
            try
            {
                CNDCValue = new Double(CNDCString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(CNDCString.trim());
                    CNDCValue = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + CNDCString + "'",0);
                }
            }
            
            try
            {
                temperatureValue = new Double(temperatureString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(temperatureString.trim());
                    temperatureValue = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + temperatureString + "'",0);
                }
            }
            
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
            row.setParameterCode("SPEED_VALUE");
            row.setParameterValue(speedValue);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            boolean ok = row.insert();

            //
            // now for the pressure/temperature/CNDC data
            // note this has a different timestamp and they are NOT aligned
            // with the current spped/direction data!!!!
            //
            
            row.setDataTimestamp(pressureDataTimestamp);
            
            row.setParameterCode("AVG_SPEED_VALUE");
            row.setParameterValue(averageSpeedValue);
            
            ok = row.insert();
            
            row.setParameterCode("DIRECTION_VALUE");
            row.setParameterValue(directionValue);
            
            ok = row.insert();
            
            row.setParameterCode("EAST_VALUE");
            row.setParameterValue(eastValue);
            
            ok = row.insert();
            
            row.setParameterCode("NORTH_VALUE");
            row.setParameterValue(northValue);
            
            ok = row.insert();
            
            row.setParameterCode("PRES");
            row.setParameterValue(pressureValue);
            
            ok = row.insert();
            
            row.setParameterCode("TEMP");
            row.setParameterValue(temperatureValue);
            
            ok = row.insert();
            
            row.setParameterCode("CNDC");
            row.setParameterValue(CNDCValue);
            
            ok = row.insert();
        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

}
