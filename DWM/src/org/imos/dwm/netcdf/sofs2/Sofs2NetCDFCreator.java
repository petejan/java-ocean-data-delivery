/*
 * This code is copyright Peter Wiley 1998-2008
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 *
 */

package org.imos.dwm.netcdf.sofs2;

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
public class Sofs2NetCDFCreator extends BaseNetCDFCreator
{
    private static Logger logger = Logger.getLogger(Sofs2NetCDFCreator.class.getName());

    public void Sofs2NetCDFCreator()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String args[])
    {
        String $HOME = System.getProperty("user.home");

        if(args.length == 0)
        {
            PropertyConfigurator.configure("log4j.properties");
            Common.build("ABOS.properties");
        }

        Sofs2NetCDFCreator cdf = new Sofs2NetCDFCreator();

        cdf.createTimeArray("SOFS-2");
        cdf.createDepthArray("SOFS-2");
        cdf.createCDFFile();
    }

    @Override
    protected String getFileName()
    {
        SimpleDateFormat nameFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        nameFormatter.setTimeZone(tz);

        String filename = //System.getProperty("user.home")
                        //+ "/"
                        Sofs2Constants.PREFIX
                        + Sofs2Constants.FACILITY_CODE
                        + Sofs2Constants.DATA_CODE
                        + Sofs2Constants.START_DATE
                        + nameFormatter.format(startTime)
                        + "_" + Sofs2Constants.platform_code + "_"
                        + Sofs2Constants.FILE_VERSION
                        + Sofs2Constants.deployment_code
                        + "_"
                        + Sofs2Constants.PRODUCT_TYPE
                        + Sofs2Constants.END_DATE
                        + nameFormatter.format(endTime)
                        + Sofs2Constants.CREATION_DATE
                        + nameFormatter.format(System.currentTimeMillis())
                        + Sofs2Constants.SUFFIX
                        ;

        return filename;
    }

    @Override
    protected void writeMooringSpecificAttributes()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'");
        df.setTimeZone(tz);
                
        dataFile.addGlobalAttribute("level", Sofs2Constants.level);
        
        dataFile.addGlobalAttribute("field_trip_id", Sofs2Constants.field_trip_id);
        dataFile.addGlobalAttribute("field_trip_description", Sofs2Constants.field_trip_description);
        
        dataFile.addGlobalAttribute("site_code", Sofs2Constants.site_code);
        dataFile.addGlobalAttribute("platform_code", Sofs2Constants.platform_code);
        dataFile.addGlobalAttribute("deployment_code", Sofs2Constants.deployment_code);

        dataFile.addGlobalAttribute("title", Sofs2Constants.title);
        dataFile.addGlobalAttribute("abstract", Sofs2Constants.Abstract);
        dataFile.addGlobalAttribute("keywords", Sofs2Constants.keywords);

        dataFile.addGlobalAttribute("geospatial_vertical_min", 0);
        dataFile.addGlobalAttribute("geospatial_vertical_max", Sofs2Constants.geospatial_vertical_max);        
    }
}
