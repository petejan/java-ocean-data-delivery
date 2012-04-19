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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;
import org.imos.abos.dbms.RawInstrumentData;
import org.wiley.util.DateUtilities;

/**
 *
 * @author peter
 */
public class ISUSDataParser extends AbstractDataParser
{

    @Override
    protected boolean isHeader(String dataLine)
    {
        if(dataLine.startsWith("SATNHR"))
            return true;
        else
            return false;
    }

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
    }

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        if( ! dataLine.startsWith("SATNLF"))
            return;

        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        
        SimpleDateFormat dateParser = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String headerString = null;
        String dateString  = null;
        String timeString = null;
        String NTR_CONCString  = null;
        String AUX1String  = null;
        String AUX2String  = null;
        String AUX3String  = null;
        String RMS_ERRORString  = null;
        String T_INTString  = null;
        String T_SPECString  = null;
        String T_LAMPString  = null;
        String LAMP_TIMEString  = null;
        String HUMIDITYString  = null;
        String VOLT_12String  = null;
        String VOLT_5String  = null;
        String VOLT_MAINString  = null;
        String REF_AVGString  = null;
        String REF_STDString  = null;
        String SW_DARKString  = null;
        String SPEC_AVGString  = null;

        String[] CHANNELString = new String[256];

        String CHECKSUMString  = null;
        String TERMINATORString  = null;

        Double NTR_CONC = null;
        Timestamp dataTimestamp = null;

        StringTokenizer st = new StringTokenizer(dataLine,",");
        
        int tokenCount = st.countTokens();
        if(tokenCount < 277)
        {
            logger.error("Short data line - skipping.");
            return;
        }
        headerString = st.nextToken();
        dateString  = st.nextToken();
        timeString = st.nextToken();
        NTR_CONCString  = st.nextToken();
        AUX1String  = st.nextToken();
        AUX2String  = st.nextToken();
        AUX3String  = st.nextToken();
        RMS_ERRORString  = st.nextToken();
        T_INTString  = st.nextToken();
        T_SPECString  = st.nextToken();
        T_LAMPString  = st.nextToken();
        LAMP_TIMEString  = st.nextToken();
        HUMIDITYString  = st.nextToken();
        VOLT_12String  = st.nextToken();
        VOLT_5String  = st.nextToken();
        VOLT_MAINString  = st.nextToken();
        REF_AVGString  = st.nextToken();
        REF_STDString  = st.nextToken();
        SW_DARKString  = st.nextToken();
        SPEC_AVGString  = st.nextToken();

        for(int i = 0; i < 256; i++)
        {
            CHANNELString[i]  = st.nextToken();
        }
        CHECKSUMString  = st.nextToken();
        //TERMINATORString  = st.nextToken();

        //logger.debug("Success parsing all tokens from string.");
        //
        // insert the only field we're actually interested in
        //

        NTR_CONC = new Double(NTR_CONCString);

        GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
        cal.setTimeZone(tz);

        Integer year = new Integer(dateString.substring(0,4));
        Integer days = new Integer(dateString.substring(4));
        Double dayFraction = new Double(timeString);

        Integer millisecs = (int) (DateUtilities.MILLIS_PER_HOUR * dayFraction);

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_YEAR, days);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        dataTimestamp = new Timestamp(cal.getTimeInMillis() + millisecs);
        
        RawInstrumentData row = new RawInstrumentData();

        row.setDataTimestamp(dataTimestamp);
        row.setDepth(instrumentDepth);
        row.setInstrumentID(currentInstrument.getInstrumentID());
        row.setLatitude(currentMooring.getLatitudeIn());
        row.setLongitude(currentMooring.getLongitudeIn());
        row.setMooringID(currentMooring.getMooringID());
        row.setParameterCode("NTR_CONC");
        row.setParameterValue(NTR_CONC);
        row.setSourceFileID(currentFile.getDataFilePrimaryKey());
        row.setQualityCode("RAW");

        boolean ok = row.insert();
    }

}
