/*
 * Neonatal Screening Software Project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.log4j.Logger;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentDataFile;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.forms.DataFileProcessorForm;
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

    protected Integer parseFailureLimit = new Integer(100);

    protected Double instrumentDepth;
    protected boolean instrumentDepthSet = false;

    protected ArrayList<String> headers = new ArrayList();

    abstract protected boolean isHeader(String dataLine);
    abstract protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException;
    abstract protected void parseData(String dataLine) throws ParseException, NoSuchElementException;

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
        int parseFailures = 0;
        String $HOME = System.getProperty("user.home");
        TextFileLogger errorlogger = new TextFileLogger($HOME + "/" + fileName.trim() + "_errors", "csv");
        try
        {
            errorlogger.open();
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
                        errorlogger.receive(ex.getMessage() + "|" + dataLine);
                        continue;
                    }
                    catch (NoSuchElementException nex)
                    {
                        errorlogger.receive(nex.getMessage() + "|" + dataLine);
                        continue;
                    }
                }
                try
                {
                    parseData(dataLine.trim());
                }
                catch (ParseException ex)
                {
                    errorlogger.receive(ex.getMessage() + "|" + dataLine);
                    parseFailures++;
                    if (parseFailures > parseFailureLimit)
                    {
                        AbstractDataParser.logger.error("Too many parse failures - quitting.");
                        break;
                    }
                }
                catch (NoSuchElementException nse)
                {
                    nse.printStackTrace();
                    errorlogger.receive("Insufficient data elements - " + nse.getMessage() + "|" + dataLine);
                    parseFailures++;
                    if (parseFailures > parseFailureLimit)
                    {
                        AbstractDataParser.logger.error("Too many parse failures - quitting.");
                        if(parentForm != null)
                            parentForm.updateMessageArea("Too many parse failures - quitting.\n");
                        break;
                    }
                }
            }
            if (parseFailures > 0)
            {
                AbstractDataParser.logger.debug("Wrote errors to file " + errorlogger.getFullName());
                if(parentForm != null)
                    parentForm.updateMessageArea("Wrote errors to file " + errorlogger.getFullName() + "\n");
            }
            errorlogger.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        AbstractDataParser.logger.debug("Finished processing file, read " + rowCount + " rows, found " + parseFailures + " bad rows.");
        if(parentForm != null)
            parentForm.updateMessageArea("Finished processing file, read " + rowCount + " rows, found " + parseFailures + " bad rows.");
    }

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
                            in = zf.getInputStream(ze);
                            BufferedReader input = new BufferedReader(new InputStreamReader(in));
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
                //
                // straight ASCII file
                //
                BufferedReader input = new BufferedReader(new FileReader(dataFile));
                decodeFile(input, dataFile.getName());
            }
        }
        catch (IOException e)
        {
            AbstractDataParser.logger.error(e);
        }
    }


}
