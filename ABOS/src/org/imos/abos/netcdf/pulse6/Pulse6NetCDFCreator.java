/*
 * Neonatal Screening Software Project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.netcdf.pulse6;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.netcdf.BaseNetCDFCreator;
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
            PropertyConfigurator.configure($HOME + "/ABOS/log4j.properties");
            Common.build($HOME + "/ABOS/ABOS.conf");
        }

        Pulse6NetCDFCreator cdf = new Pulse6NetCDFCreator();

        cdf.createTimeArray("PULSE_6");
        cdf.createDepthArray("PULSE_6");
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
                        + Pulse6Constants.PLATFORM_CODE
                        + Pulse6Constants.FILE_VERSION
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
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'");
        df.setTimeZone(tz);
        if (currentMooring != null)
        {
            dataFile.addGlobalAttribute("Mooring", currentMooring.getShortDescription());
            dataFile.addGlobalAttribute("Latitude", currentMooring.getLatitudeIn());
            dataFile.addGlobalAttribute("Longitude", currentMooring.getLongitudeIn());
            Pulse6Constants.geospatial_lat_min = currentMooring.getLatitudeIn();
            Pulse6Constants.geospatial_lat_max = currentMooring.getLatitudeOut();
            Pulse6Constants.geospatial_lon_min = currentMooring.getLongitudeIn();
            Pulse6Constants.geospatial_lon_max = currentMooring.getLongitudeOut();
            if (Pulse6Constants.geospatial_lat_max == null)
            {
                Pulse6Constants.geospatial_lat_max = Pulse6Constants.geospatial_lat_min;
            }
            if (Pulse6Constants.geospatial_lon_max == null)
            {
                Pulse6Constants.geospatial_lon_max = Pulse6Constants.geospatial_lon_min;
            }
        }

        dataFile.addGlobalAttribute("level", Pulse6Constants.level);

        dataFile.addGlobalAttribute("field_trip_id", Pulse6Constants.field_trip_id);
        dataFile.addGlobalAttribute("field_trip_description", Pulse6Constants.field_trip_description);

        dataFile.addGlobalAttribute("site_code", Pulse6Constants.site_code);
        dataFile.addGlobalAttribute("platform_code", Pulse6Constants.platform_code);

        dataFile.addGlobalAttribute("title", Pulse6Constants.title);
        dataFile.addGlobalAttribute("abstract", Pulse6Constants.Abstract);
        dataFile.addGlobalAttribute("keywords", Pulse6Constants.keywords);

        dataFile.addGlobalAttribute("geospatial_vertical_max", Pulse6Constants.geospatial_vertical_max);
    }
}

