/*
 * This code is copyright Peter Wiley 1998-2008
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 *
 */

package org.imos.abos.netcdf.pulse8;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.netcdf.BaseNetCDFCreator;
import org.imos.abos.netcdf.pulse7.Pulse7Constants;
import org.imos.abos.netcdf.pulse7.Pulse7NetCDFCreator;
import org.wiley.core.Common;

/**
 *
 * @author peter
 */
public class Pulse8NetCDFCreator extends BaseNetCDFCreator
{
    private static Logger logger = Logger.getLogger(Pulse7NetCDFCreator.class.getName());

    public void Pulse7NetCDFCreator()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }

    public static void main(String args[])
    {
        String $HOME = System.getProperty("user.home");

        if(args.length == 0)
        {
            PropertyConfigurator.configure("log4j.properties");
            Common.build("ABOS.conf");
        }

        Pulse8NetCDFCreator cdf = new Pulse8NetCDFCreator();

        cdf.createTimeArray("PULSE_8");
        cdf.createDepthArray("PULSE_8");
        cdf.createCDFFile();
    }

    @Override
    protected String getFileName()
    {
        SimpleDateFormat nameFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        nameFormatter.setTimeZone(tz);

        String filename = //System.getProperty("user.home")
                        //+ "/"
                        Pulse8Constants.PREFIX
                        + Pulse8Constants.FACILITY_CODE
                        + Pulse8Constants.DATA_CODE
                        + Pulse8Constants.START_DATE
                        + nameFormatter.format(startTime)
                        + "_" + Pulse8Constants.platform_code + "_"
                        + Pulse8Constants.FILE_VERSION
                        + Pulse8Constants.deployment_code
                        + "_"
                        + Pulse8Constants.PRODUCT_TYPE
                        + Pulse8Constants.END_DATE
                        + nameFormatter.format(endTime)
                        + Pulse8Constants.CREATION_DATE
                        + nameFormatter.format(System.currentTimeMillis())
                        + Pulse8Constants.SUFFIX
                        ;

        return filename;
    }

    @Override
    protected void writeMooringSpecificAttributes()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'");
        df.setTimeZone(tz);
                
        dataFile.addGlobalAttribute("level", Pulse8Constants.level);
        
        dataFile.addGlobalAttribute("field_trip_id", Pulse8Constants.field_trip_id);
        dataFile.addGlobalAttribute("field_trip_description", Pulse8Constants.field_trip_description);
        
        dataFile.addGlobalAttribute("site_code", Pulse8Constants.site_code);
        dataFile.addGlobalAttribute("platform_code", Pulse8Constants.platform_code);
        dataFile.addGlobalAttribute("deployment_code", Pulse8Constants.deployment_code);

        dataFile.addGlobalAttribute("title", Pulse8Constants.title);
        dataFile.addGlobalAttribute("abstract", Pulse8Constants.Abstract);
        dataFile.addGlobalAttribute("keywords", Pulse8Constants.keywords);

        dataFile.addGlobalAttribute("geospatial_vertical_min", 0);
        dataFile.addGlobalAttribute("geospatial_vertical_max", Pulse8Constants.geospatial_vertical_max);
        
        dataFile.addGlobalAttribute("comment", "Aanderra optode data have been omitted from this data set due to an apparent inconsistency with Seabird SBE43 data.");
    }
}
