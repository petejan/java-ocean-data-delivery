/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
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
public class AquadopCurrentMeterParser extends AbstractDataParser
{
    // Example file:
    //
    //ADV File  : C:\SAZ\2010-13-SAZ47\Saz1302 15082011.aqd
    //ADV PROBE : 1
    //ADV VELOCITIES (U, V, W):
    //09,07,2010,12, 0, 0,108.50,-11.20,-112.30
    //
    //
    // Another file
    // hdr:
    // 1   Month                            (1-12)
    // 2   Day                              (1-31)
    // 3   Year
    // 4   Hour                             (0-23)
    // 5   Minute                           (0-59)
    // 6   Second                           (0-59)
    // 7   Error code
    // 8   Status code
    // 9   Velocity (Beam1|X|East)          (m/s)
    //10   Velocity (Beam2|Y|North)         (m/s)
    //11   Velocity (Beam3|Z|Up)            (m/s)
    //12   Amplitude (Beam1)                (counts)
    //13   Amplitude (Beam2)                (counts)
    //14   Amplitude (Beam3)                (counts)
    //15   Battery voltage                  (V)
    //16   Soundspeed                       (m/s)
    //17   Soundspeed used                  (m/s)
    //18   Heading                          (degrees)
    //19   Pitch                            (degrees)
    //20   Roll                             (degrees)
    //21   Pressure                         (dbar)
    //22   Pressure                         (m)
    //23   Temperature                      (degrees C)
    //24   Analog input 1
    //25   Analog input 2
    //26   Speed                            (m/s)
    //27   Direction                        (degrees)
    //
    // DAT:
    //09 07 2010 12 00 00 00000000 00111100    1.088   -0.112   -1.126    30    26    25  13.0 1448.9 1452.9  41.7  53.5 -53.9   4.086   4.064  11.43     0 14489    1.094    95.89 
    //09 07 2010 12 30 00 00000000 00111100    1.065   -0.165   -1.139    30    26    25  13.0 1448.5 1452.5  42.7  53.5 -53.9   4.083   4.061  11.33     0 14485    1.078    98.83
    //09 07 2010 13 00 00 00000000 00111100    1.116   -0.169   -1.161    30    26    25  13.0 1448.3 1452.2  42.5  53.5 -53.9   4.149   4.126  11.25     0 14483    1.129    98.63


    protected SimpleDateFormat dateParser = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    
    public AquadopCurrentMeterParser()
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

        String yearString;
        String monthString;
        String dayString;
        String hourString;
        String minuteString;
        String secondString;
        
        Timestamp dataTimestamp = null;
        Double U_Value = null;
        Double V_Value = null;
        Double W_Value = null;

        String constructTimestamp;

        StringTokenizer st = new StringTokenizer(dataLine,", ");
        try
        {
            monthString = st.nextToken();
            dayString = st.nextToken();
            yearString = st.nextToken();
            hourString = st.nextToken();
            minuteString = st.nextToken();
            secondString = st.nextToken();
            
           
            constructTimestamp = yearString.trim() 
                                + "/" 
                                + monthString.trim()
                                + "/"
                                + dayString.trim()
                                + " "
                                + hourString.trim()
                                + ":"
                                + minuteString.trim()
                                + ":"
                                + secondString.trim()
                                ;

            try
            {
                java.util.Date d = dateParser.parse(constructTimestamp);
                dataTimestamp = new Timestamp(d.getTime());
            }
            catch(ParseException pex)
            {
                throw new ParseException("Timestamp parse failed for text '" + constructTimestamp + "'",0);
            }

            U_Value = getDouble(st.nextToken());
            V_Value = getDouble(st.nextToken());
            W_Value = getDouble(st.nextToken());
            
            //
            // ok, we have parsed out the values we need, can now construct the raw data class
            //
            RawInstrumentData row = new RawInstrumentData();

            row.setDataTimestamp(dataTimestamp);
            row.setDepth(instrumentDepth);
            row.setInstrumentID(currentInstrument.getInstrumentID());
            row.setLatitude(currentMooring.getLatitudeIn());
            row.setLongitude(currentMooring.getLongitudeIn());
            row.setMooringID(currentMooring.getMooringID());
            row.setParameterCode("UCUR");
            row.setParameterValue(U_Value);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            boolean ok = row.insert();

            row.setParameterCode("VCUR");
            row.setParameterValue(V_Value);
            
            ok = row.insert();
            
            row.setParameterCode("WCUR");
            row.setParameterValue(W_Value);
            
            ok = row.insert();

            

        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }
}
