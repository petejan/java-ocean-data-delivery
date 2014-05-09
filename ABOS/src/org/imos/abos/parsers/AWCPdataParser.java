/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.parsers;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.dbms.ArrayInstrumentData;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentDataFile;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.dbms.RawInstrumentData;
import org.imos.abos.processors.AWCPdataProcessor;
import org.wiley.core.Common;
import org.wiley.util.DateUtilities;
import org.wiley.util.StringUtilities;

/**
 *
 * @author peter
 */
public class AWCPdataParser extends AbstractDataParser
{

    boolean insert = true;
    
    private void setInsert(boolean insert)
    {
        this.insert = insert;
    }
    
    public enum FileType {NONE, C_38, C_125, C_200, C_455, PITCH, ROLL};
    
    FileType fileType = FileType.NONE;
    
    @Override
    protected boolean isHeader(String dataLine)
    {
        return !dataLine.matches("\\d{4}-\\d{2}-\\d{2},\\d{2}:\\d{2}:\\d{2},.+");
    }

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
    }

    Timestamp lastTimestamp;
    RawInstrumentData row = null;
    ArrayInstrumentData array = null;
    AWCPdataProcessor dp = new AWCPdataProcessor();
    
    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        TimeZone.setDefault(tz);
        boolean haveRange = false;

        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss S");
        SimpleDateFormat sdfms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
        dateParser.setTimeZone(tz);
        sdfms.setTimeZone(tz);

        Timestamp dataTimestamp = null;
                
        String[] split = dataLine.split(",");

        String ts = split[0] + " " + split[1] + " " + split[2];
        
        dataTimestamp = new Timestamp(dateParser.parse(ts).getTime());
        //System.out.println("AWCPdataParser::parseData() ts " + ts + " " + sdfms.format(dataTimestamp) + " last " + sdfms.format(lastTimestamp));
        
        if (dataTimestamp.equals(lastTimestamp))
        {
            return;
        }
        lastTimestamp = dataTimestamp;
        
        boolean ok = false;
        
        if (fileType == FileType.ROLL || fileType == FileType.PITCH)
        {
            row.setDataTimestamp(dataTimestamp);

            row.setParameterValue(Double.parseDouble(split[3]));
            if (insert)
                ok = row.insert();
        }
        else
        {
            row.setDataTimestamp(dataTimestamp);

            if (!haveRange)
            {
                row.setParameterCode("RANGE_START");
                row.setParameterValue(Double.parseDouble(split[3]));
                if (insert)
                    ok = row.insert();
                row.setParameterCode("RANGE_END");
                row.setParameterValue(Double.parseDouble(split[4]));
                if (insert)
                    ok |= row.insert();
                haveRange = true;
            }
            
            array.setDataTimestamp(dataTimestamp);

            int n = Integer.parseInt(split[5]);
            Double[] sv = new Double[n];
            for (int i=0;i<n;i++)
            {   
                sv[i] = Double.parseDouble(split[6 + i]);
            }
            array.setParameterValue(sv);
            array.setSourceFileID(currentFile.getDataFilePrimaryKey());
            
            dp.sampleInit(dataTimestamp, fileType);
            dp.sample(sv, fileType);
            dp.output();

            //System.out.println("AWCPdataParser::parseData() " + sdfms.format(array.getDataTimestamp()) + " " + array.getParameterCode() + " length = " + n);
            if (insert)            
                ok |= array.insert();
        }
    }
    public void setInstrumentDataFile(InstrumentDataFile ins)
    {
        super.setInstrumentDataFile(ins);
        if (currentFile.getFileName().contains("C1_038"))
        {
            fileType = FileType.C_38;
        }
        else if (currentFile.getFileName().contains("C2_125"))
        {
            fileType = FileType.C_125;
        }
        else if (currentFile.getFileName().contains("C3_200"))
        {
            fileType = FileType.C_200;
        }
        else if (currentFile.getFileName().contains("C4_455"))
        {
            fileType = FileType.C_455;
        }
        else if (currentFile.getFileName().contains("roll"))
        {
            fileType = FileType.ROLL;
        }
        else if (currentFile.getFileName().contains("pitch"))
        {
            fileType = FileType.PITCH;
        }
        lastTimestamp = new Timestamp(0);
        
        row = new RawInstrumentData();

        row.setDepth(instrumentDepth);
        row.setInstrumentID(currentInstrument.getInstrumentID());
        row.setLatitude(currentMooring.getLatitudeIn());
        row.setLongitude(currentMooring.getLongitudeIn());
        row.setMooringID(currentMooring.getMooringID());
        row.setSourceFileID(currentFile.getDataFilePrimaryKey());
        row.setQualityCode("RAW");

        if (fileType == FileType.ROLL)
        {
            row.setParameterCode("ROLL");
        }
        else if (fileType == FileType.PITCH)
        {
            row.setParameterCode("PITCH");
        }
        
        array = new ArrayInstrumentData();
        array.setDepth(instrumentDepth);
        array.setInstrumentID(currentInstrument.getInstrumentID());
        array.setLatitude(currentMooring.getLatitudeIn());
        array.setLongitude(currentMooring.getLongitudeIn());
        array.setMooringID(currentMooring.getMooringID());
        array.setQualityCode("RAW");

        if (fileType == FileType.C_38)
        {
            array.setParameterCode("SV_038");
        }
        else if (fileType == FileType.C_125)
        {
            array.setParameterCode("SV_125");
        }
        else if (fileType == FileType.C_200)
        {
            array.setParameterCode("SV_200");
        }
        else if (fileType == FileType.C_455)
        {
            array.setParameterCode("SV_455");
        }
        
    }

    public static void main(String args[])
    {
        if (args.length < 3)
        {
            System.err.println("Usage : parser.class <mooring> <instrument_id> <file....>");
            
            System.exit(-1);
        }
        String $HOME = System.getProperty("user.home");
        PropertyConfigurator.configure("log4j.properties");
        Common.build($HOME + "/ABOS/ABOS.properties");        
        
        int argsi = 0;
        boolean insert = true;
        if (args[0].equals("-noinsert"))
        {
            insert = false;
            argsi++;
        }
        
        Mooring m = Mooring.selectByMooringID(args[argsi++]);
        Instrument inst = Instrument.selectByInstrumentID(Integer.parseInt(args[argsi++]));
                
        String SQL = "SELECT depth FROM mooring_attached_instruments WHERE mooring_id = "
                        + StringUtilities.quoteString(m.getMooringID())
                        + " AND instrument_id = " + inst.getInstrumentID();
        
        Connection conn = Common.getConnection();
        Statement proc;
        double d = Double.NaN;
        try
        {
            proc = conn.createStatement();
            proc.execute(SQL);  
            ResultSet results = (ResultSet) proc.getResultSet();
            results.next();
            
            Double depth = results.getBigDecimal(1).doubleValue();
            logger.info("Depth from database " + depth);
            
            d = depth;
    
            proc.close();
        }
        catch (SQLException ex)
        {
            java.util.logging.Logger.getLogger(AbstractDataParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        AWCPdataParser awcp = new AWCPdataParser();
                
        System.err.println("AWCPdataParser::main():: using mooring " + m + " instrument " + inst + " depth " + d);
        
        awcp.setMooring(m);
        awcp.setInstrument(inst);
        awcp.setInstrumentDepth(d);
        awcp.setInsert(insert);
        
        InstrumentDataFile idf = new InstrumentDataFile();
        
        for(int i=argsi;i<args.length;i++)
        {
            File f = new File(args[i]);
            idf.setFileName(f.getName());
            idf.setInstrumentID((Number)inst.getInstrumentID());
            idf.setMooringID(m.getMooringID());
            idf.setFilePath(f.getAbsolutePath());
            idf.setInstrumentDepth(d);
            idf.setProcessingStatus("UNPROCESSED");
            byte[] nullFile = new byte[0];
            idf.setFileData(nullFile);

            if (insert)
            {
                idf.setDataFilePrimaryKey(InstrumentDataFile.getNextSequenceNumber());
                idf.insert();
            }

            awcp.setInstrumentDataFile(idf);
            awcp.processFile(f);

            System.err.println("AWCPdataParser::main() data file id = " + idf.getDataFilePrimaryKey() + " " + idf.getLastErrorMessage());
        }
    }
    
}
