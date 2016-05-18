/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.parsers;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.log4j.Logger;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentDataFile;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.forms.DataFileProcessorForm;
import org.imos.abos.forms.MarkBadDataForMooringDeploymentForm;
import org.wiley.core.Common;
import org.wiley.util.StringUtilities;
import org.wiley.util.TextFileLogger;

/**
 *
 * @author peter
 */
public abstract class AbstractDataParser
{
    protected static Logger logger = Logger.getLogger(AbstractDataParser.class.getName());
    protected TimeZone tz = TimeZone.getTimeZone("GMT");

    protected DataFileProcessorForm parentForm = null;

    protected InstrumentDataFile currentFile;
    protected Instrument currentInstrument;
    protected Mooring currentMooring;

    public static Integer PARSE_FAILURE_LIMIT = new Integer(100);

    protected Double instrumentDepth;
    protected boolean instrumentDepthSet = false;

    protected ArrayList<String> headers = new ArrayList();
    
    //protected TextFileLogger errorlogger = null;
    protected int PARSE_FAILURES = 0;

    abstract protected boolean isHeader(String dataLine);
    abstract protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException;
    abstract protected void parseData(String dataLine) throws ParseException, NoSuchElementException;

    public void AbstractDataParser()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    protected DecimalFormat deciFormat = new DecimalFormat("-######.0#");

    protected Double getDouble(String s) throws ParseException
    {
        Double d;

        try
        {
            d = new Double(s.trim());
        }
        catch (NumberFormatException pex)
        {
            try
            {
                Number n = deciFormat.parse(s.trim());
                d = n.doubleValue();
            }
            catch (ParseException pexx)
            {
                throw new ParseException("parse failed for text '" + s + "'", 0);
            }
        }

        return d;
    }

    
    /**
     * get the number of parse failures
     * @return 
     */
    
    public int getParseFailureCount()
    {
        return PARSE_FAILURES;
    }
    
    /**
     * get the error log file if required
     * @return 
     */
    
    public TextFileLogger getErrorLogFile()
    {
        //return errorlogger;
    	return null;
    }
    
    /**
     * set the maximum allowed number of parse failures
     * @return 
     */
    
    public void setMaximumAllowedParseFailures(Integer failures)
    {
        PARSE_FAILURE_LIMIT = failures;
    }
    
    public Integer getMaximumAllowedParseFailures()
    {
        return PARSE_FAILURE_LIMIT;
    }
    
    public ArrayList<String> getHeaders()
    {
        return headers;
    }

    public void run()
    {
        if(currentFile == null)
        {
            logger.error("No instrument file object, cannot proceed!");
            return;
        }

        logger.debug("Extracting file from database");
        if (parentForm != null)
        {
            parentForm.updateMessageArea("Extracting file from database " + currentFile.getFileName() + "\n");
        }
        
        
        File f = currentFile.getInstrumentDataFile();
        if( f != null)
        {
            processFile(f);
        }
    }

    /**
     * set a value for the instrument depth
     * if set, this value takes precedence over one read from the data file
     * mainly intended to specify a depth value for instruments which don't
     * record their own depth in either header or data block.
     * 
     * @param d
     */
    public void setInstrumentDepth(Double d)
    {
        instrumentDepth = d;
        instrumentDepthSet = true;
    }

    public Double getInstrumentDepth()
    {
        return instrumentDepth;
    }

    public void setParentForm(DataFileProcessorForm form)
    {
        parentForm = form;
    }
    
    public void setInstrument(Instrument ins)
    {
        currentInstrument = ins;        
    }

    public void setMooring(Mooring m)
    {
        currentMooring = m;
    }

    public void setInstrumentDataFile(InstrumentDataFile ins)
    {
        currentFile = ins;
    }

    protected void decodeFile(BufferedReader input, String fileName)
    {
        String dataLine;
        int rowCount = 0;
        
        String $HOME = System.getProperty("user.home");
        //TextFileLogger errorlogger = new TextFileLogger($HOME + "/" + fileName.trim() + "_errors", "csv");
        String n = fileName.trim();
        n = n.substring(n.lastIndexOf("/")+1);
        //errorlogger = new TextFileLogger(n + "_errors", "csv");
        try
        {
            //errorlogger.open();
            while (true)
            {
                dataLine = input.readLine();
                if (dataLine == null)
                {
                    break;
                }
                rowCount++;
                if (rowCount % 1000 == 0)
                {
                    AbstractDataParser.logger.debug("Processed " + rowCount + " rows.");
                    if(parentForm != null)
                        parentForm.updateMessageArea("Processed " + rowCount + " rows.\n");
                }
                //
                // this file may have some blank lines in it so skip them
                //
                if(dataLine.trim().isEmpty())
                    continue;

                if( isHeader(dataLine.trim()) )
                {
                    //
                    // it is a header
                    //
                    headers.add(dataLine);
                    try
                    {
                        parseHeader(dataLine.trim());
                        continue;
                    }
                    catch (ParseException ex)
                    {
                        //errorlogger.receive("Row: " + (rowCount+1) + " - " + ex.getMessage() + "|" + dataLine);
                        continue;
                    }
                    catch (NoSuchElementException nex)
                    {
                        //errorlogger.receive("Row: " + (rowCount+1) + " - " + nex.getMessage() + "|" + dataLine);
                        continue;
                    }
                }
                try
                {
                    parseData(dataLine.trim());
                }
                catch (ParseException ex)
                {
                    // errorlogger.receive("Row: " + (rowCount+1) + " - " + ex.getMessage() + "|" + dataLine);
                    PARSE_FAILURES++;
                    if (PARSE_FAILURES > PARSE_FAILURE_LIMIT)
                    {
                        AbstractDataParser.logger.error("Too many parse failures - quitting.");
                        break;
                    }
                }
                catch (NoSuchElementException nse)
                {
                    nse.printStackTrace();
                    //errorlogger.receive("Row: " + (rowCount+1) + " - " +"Insufficient data elements - " + nse.getMessage() + "|" + dataLine);
                    PARSE_FAILURES++;
                    if (PARSE_FAILURES > PARSE_FAILURE_LIMIT)
                    {
                        AbstractDataParser.logger.error("Too many parse failures - quitting.");
                        if(parentForm != null)
                            parentForm.updateMessageArea("Too many parse failures - quitting.\n");
                        break;
                    }
                }
            }
            if (PARSE_FAILURES > 0)
            {
                //AbstractDataParser.logger.debug("Wrote errors to file " + errorlogger.getFullName());
                //if(parentForm != null)
                    //parentForm.updateMessageArea("Wrote errors to file " + errorlogger.getFullName() + "\n");
            }
            //errorlogger.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        AbstractDataParser.logger.debug("Finished processing file " 
                                        + fileName 
                                        + ", read " 
                                        + rowCount 
                                        + " rows, found " 
                                        + PARSE_FAILURES 
                                        + " bad rows.\n");
        if(parentForm != null)
            parentForm.updateMessageArea("Finished processing file " 
                                        + fileName 
                                        + ", read " 
                                        + rowCount 
                                        + " rows, found " 
                                        + PARSE_FAILURES 
                                        + " bad rows.\n");
    }

    protected String processingFile = null;
    protected void processFile(File dataFile)
    {
        try
        {
            if (dataFile.getName().toLowerCase().endsWith("zip"))
            {
                ///  zip file
                InputStream in = null;
                ZipFile zf = new ZipFile(dataFile);
                Enumeration e = zf.entries();
                while (e.hasMoreElements())
                {
                    Object o = e.nextElement();
                    if (o instanceof ZipEntry)
                    {
                        ZipEntry ze = (ZipEntry) o;
                        if (true)
                        {
                            System.out.println("Processing file " + ze.getName());
                            processingFile = ze.getName();
                            in = zf.getInputStream(ze);
                            BufferedReader input = new BufferedReader(new InputStreamReader(in));
                            if(parentForm != null)
                            {
                                // output number of lines, just so we know how long to wait.
                                int lines = 0;
                                while (input.readLine() != null) lines++;
                                input.close();
                                parentForm.updateMessageArea("lines " + lines + "\n");
                                input = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
                            }
                            
                            decodeFile(input, ze.getName());
                            input.close();
                        }
                    }
                    else
                    {
                        throw new RuntimeException("Not a Zip Entry");
                    }
                }
            }
            else
            {
                System.out.println("Processing file " + dataFile.getName());
                processingFile = dataFile.getName();
                //
                // straight ASCII file
                //
                BufferedReader input = new BufferedReader(new FileReader(dataFile));
                if(parentForm != null)
                {
                    // output number of lines, just so we know how long to wait.
                    int lines = 0;
                    while (input.readLine() != null) lines++;
                    input.close();
                    parentForm.updateMessageArea("lines " + lines + "\n");
                    input = new BufferedReader(new FileReader(dataFile));
                }
                                
                decodeFile(input, dataFile.getName());
            }
        }
        catch (IOException e)
        {
            AbstractDataParser.logger.error(e);
        }
    }

    public static void main(String args[])
    {
        if (args.length < 3)
        {
            System.err.println("Usage : parser.class <mooring> <instrument_id> <file....>");
            
            System.exit(-1);
        }
    }
}
