/*
 * Neonatal Screening Software Project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.netcdf.pulse7;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.wiley.core.Common;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.imos.abos.netcdf.BaseNetCDFCreator;

/**
 *
 * @author peter
 */
public class Pulse7NetCDFCreator extends BaseNetCDFCreator
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
            PropertyConfigurator.configure($HOME + "/ABOS/log4j.properties");
            Common.build($HOME + "/ABOS/ABOS.conf");
        }

        Pulse7NetCDFCreator cdf = new Pulse7NetCDFCreator();

        cdf.createTimeArray("PULSE_7");
        cdf.createDepthArray("PULSE_7");
        cdf.createCDFFile();
    }

    @Override
    protected String getFileName()
    {
        SimpleDateFormat nameFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        nameFormatter.setTimeZone(tz);

        String filename = System.getProperty("user.home")
                        + "/"
                        + Pulse7Constants.PREFIX
                        + Pulse7Constants.FACILITY_CODE
                        + Pulse7Constants.DATA_CODE
                        + Pulse7Constants.START_DATE
                        + nameFormatter.format(startTime)
                        + Pulse7Constants.PLATFORM_CODE
                        + Pulse7Constants.FILE_VERSION
                        + Pulse7Constants.PRODUCT_TYPE
                        + Pulse7Constants.END_DATE
                        + nameFormatter.format(endTime)
                        + Pulse7Constants.CREATION_DATE
                        + nameFormatter.format(System.currentTimeMillis())
                        + Pulse7Constants.SUFFIX
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
            Pulse7Constants.geospatial_lat_min = currentMooring.getLatitudeIn();
            Pulse7Constants.geospatial_lat_max = currentMooring.getLatitudeOut();
            Pulse7Constants.geospatial_lon_min = currentMooring.getLongitudeIn();
            Pulse7Constants.geospatial_lon_max = currentMooring.getLongitudeOut();
            if (Pulse7Constants.geospatial_lat_max == null)
            {
                Pulse7Constants.geospatial_lat_max = Pulse7Constants.geospatial_lat_min;
            }
            if (Pulse7Constants.geospatial_lon_max == null)
            {
                Pulse7Constants.geospatial_lon_max = Pulse7Constants.geospatial_lon_min;
            }
        }
        
        dataFile.addGlobalAttribute("level", Pulse7Constants.level);
        
        dataFile.addGlobalAttribute("field_trip_id", Pulse7Constants.field_trip_id);
        dataFile.addGlobalAttribute("field_trip_description", Pulse7Constants.field_trip_description);
        
        dataFile.addGlobalAttribute("site_code", Pulse7Constants.site_code);
        dataFile.addGlobalAttribute("platform_code", Pulse7Constants.platform_code);

        dataFile.addGlobalAttribute("title", Pulse7Constants.title);
        dataFile.addGlobalAttribute("abstract", Pulse7Constants.Abstract);
        dataFile.addGlobalAttribute("keywords", Pulse7Constants.keywords);

        dataFile.addGlobalAttribute("geospatial_vertical_max", Pulse7Constants.geospatial_vertical_max);
    }
}
