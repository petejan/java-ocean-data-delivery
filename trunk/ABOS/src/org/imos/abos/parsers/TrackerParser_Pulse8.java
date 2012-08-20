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
public class TrackerParser_Pulse8 extends TrackerParser
{

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String dateString;
        String modemIDString;
        String latitudeString;
        String longitudeString;

        Timestamp dataTimestamp = null;
        Double latitude = null;
        Double longitude = null;

        String constructTimestamp;

        StringTokenizer st = new StringTokenizer(dataLine,",");
        try
        {
            dateString = st.nextToken();
            
            modemIDString = st.nextToken();
            
            latitudeString = st.nextToken();
            longitudeString  = st.nextToken();

            constructTimestamp = dateString.trim();
            try
            {
                java.util.Date d = dateParser.parse(constructTimestamp);
                dataTimestamp = new Timestamp(d.getTime());
            }
            catch(ParseException pex)
            {
                throw new ParseException("Timestamp parse failed for text '" + constructTimestamp + "'",0);
            }

            int latCutoff = latitudeString.lastIndexOf("=") + 1;
            
            try
            {
                
                latitude = new Double(latitudeString.substring(latCutoff));
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(latitudeString.substring(latCutoff));
                    latitude = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + latitudeString.substring(latCutoff) + "'",0);
                }
            }

            int lonCutoff = longitudeString.lastIndexOf("=") + 1;
            
            try
            {
                
                longitude = new Double(longitudeString.substring(lonCutoff));
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(longitudeString.substring(lonCutoff));
                    longitude = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + longitudeString.substring(lonCutoff) + "'",0);
                }
            }

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
            row.setParameterCode("LATITUDE");
            row.setParameterValue(latitude);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            boolean ok = row.insert();

            row.setParameterCode("LONGITUDE");
            row.setParameterValue(longitude);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            ok = row.insert();

        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }
    
    
}
