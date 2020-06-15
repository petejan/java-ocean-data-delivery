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
public class GPSParser_Pulse9 extends TrackerParser_Pulse8
{

    
@Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        /*
         * data sample
         * 
         2012-07-20 00:11:54 ,len,26 ,site,20 ,datatype,GPS ,retryCount,1 ,CSQ,3 ,filesToTx,18 ,numModems,0 ,filePointer,19 ,lat,-46.842315 ,lon,142.398303 ,fixQ,1 ,sats,6 ,HDOP,1
         2012-07-20 01:11:54 ,len,26 ,site,20 ,datatype,GPS ,retryCount,1 ,CSQ,5 ,filesToTx,1 ,numModems,0 ,filePointer,2 ,lat,-46.844373 ,lon,142.393792 ,fixQ,1 ,sats,5 ,HDOP,2
         2012-07-20 02:11:54 ,len,26 ,site,20 ,datatype,GPS ,retryCount,3 ,CSQ,1 ,filesToTx,1 ,numModems,0 ,filePointer,4 ,lat,-46.846957 ,lon,142.389600 ,fixQ,1 ,sats,5 ,HDOP,2
         */
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String dateString;
        String spacer01;    // len
        String spacer02;    // 26
        String spacer03;    // site
        String spacer04;    // 20
        String spacer05;    //datatype
        String spacer06;    // GPS
        String spacer07;    // retrycount
        String spacer08;    // 1
        String spacer09;    // CSQ
        String spacer10;    // 3
        String spacer11;    // filesToTx
        String spacer12;    // 18
        String spacer13;    // numModems
        String spacer14;    // 0
        String spacer15;    // filePointer
        String spacer16;    // 19
        String latitudeLabel;   
        String longitudeLabel;
        
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
            
            spacer01 = st.nextToken();
            spacer02 = st.nextToken();
            spacer03 = st.nextToken();
            spacer04 = st.nextToken();
            spacer05 = st.nextToken();
            spacer06 = st.nextToken();
            spacer07 = st.nextToken();
            spacer08 = st.nextToken();
            spacer09 = st.nextToken();
            spacer10 = st.nextToken();
            spacer11 = st.nextToken();
            spacer12 = st.nextToken();
            spacer13 = st.nextToken();
            spacer14 = st.nextToken();
            spacer15 = st.nextToken();
            spacer16 = st.nextToken();
            
            latitudeLabel = st.nextToken();
            latitudeString = st.nextToken();
            
            longitudeLabel = st.nextToken();
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

            try
            {
                latitude = new Double(latitudeString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(latitudeString.trim());
                    latitude = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + latitudeString.trim() + "'",0);
                }
            }

            try
            {
                longitude = new Double(longitudeString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(longitudeString.trim());
                    longitude = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + longitudeString.trim() + "'",0);
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
