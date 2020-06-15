/*
 * IMOS Data Delivery Project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.dwm.netcdf.pulse6;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.dwm.netcdf.BaseNetCDFCreator;
import org.wiley.core.Common;

/**
 *
 * @author peter
 */
public class Pulse6NetCDFCreator extends BaseNetCDFCreator
{
    private static Logger logger = Logger.getLogger(Pulse6NetCDFCreator.class.getName());

    public void Pulse6NetCDFCreator()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }

    public static void main(String args[])
    {
        String $HOME = System.getProperty("user.home");

        if(args.length == 0)
        {
            PropertyConfigurator.configure("log4j.properties");
            Common.build("ABOS.properties");
        }

        Pulse6NetCDFCreator cdf = new Pulse6NetCDFCreator();

        cdf.createTimeArray("Pulse-6-2009");
        cdf.createDepthArray("Pulse-6-2009");
        cdf.createCDFFile();
    }

    @Override
    protected String getFileName()
    {
        SimpleDateFormat nameFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        nameFormatter.setTimeZone(tz);

        String filename = System.getProperty("user.home")
                        + "/"
                        + Pulse6Constants.PREFIX
                        + Pulse6Constants.FACILITY_CODE
                        + Pulse6Constants.DATA_CODE
                        + Pulse6Constants.START_DATE
                        + nameFormatter.format(startTime)
                        + "_" + Pulse6Constants.platform_code + "_"
                        + Pulse6Constants.FILE_VERSION
                        + Pulse6Constants.deployment_code.toUpperCase()
                        + "_"
                        + Pulse6Constants.PRODUCT_TYPE
                        + Pulse6Constants.END_DATE
                        + nameFormatter.format(endTime)
                        + Pulse6Constants.CREATION_DATE
                        + nameFormatter.format(System.currentTimeMillis())
                        + Pulse6Constants.SUFFIX
                        ;

        return filename;
    }

    @Override
    protected void writeMooringSpecificAttributes()
    { 
        // dataFile.addGlobalAttribute("level", Pulse6Constants.level);

        dataFile.addGlobalAttribute("field_trip_id", Pulse6Constants.field_trip_id);
        dataFile.addGlobalAttribute("field_trip_description", Pulse6Constants.field_trip_description);

        dataFile.addGlobalAttribute("site_code", Pulse6Constants.site_code);
        dataFile.addGlobalAttribute("platform_code", Pulse6Constants.platform_code);
        dataFile.addGlobalAttribute("deployment_code", Pulse6Constants.deployment_code);

        dataFile.addGlobalAttribute("title", Pulse6Constants.title);
        dataFile.addGlobalAttribute("abstract", Pulse6Constants.Abstract);
        dataFile.addGlobalAttribute("keywords", Pulse6Constants.keywords);

        dataFile.addGlobalAttribute("geospatial_vertical_min", 0);
        dataFile.addGlobalAttribute("geospatial_vertical_max", Pulse6Constants.geospatial_vertical_max);
        
        dataFile.addGlobalAttribute("comment", "Aanderra optode data have been omitted from this data set due to an apparent instrument malfunction.");
    }
}

