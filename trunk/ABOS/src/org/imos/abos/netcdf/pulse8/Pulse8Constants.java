/*
 * This code is copyright Peter Wiley 1998-2008
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 *
 */

package org.imos.abos.netcdf.pulse8;

import org.imos.abos.netcdf.BaseNetCDFConstants;

/**
 *
 * @author peter
 */
public class Pulse8Constants extends BaseNetCDFConstants
{

    public static String time_coverage_start = "2011-08-03 00:00:00Z" ;
    public static String time_coverage_end = "2012-07-19 00:00:00Z" ;
    public static Double geospatial_lat_min = -46.93 ;
    public static Double geospatial_lat_max = -46.93 ;
    public static Double geospatial_lon_min = 142.26 ;
    public static Double geospatial_lon_max = 142.26 ;
    public static Double geospatial_vertical_min = -4500.0 ;
    public static Double geospatial_vertical_max = 0.0 ;
    
    public static String level = "15" ;
    public static String field_trip_id = "N/A" ;
    public static String field_trip_description = "N/A" ;
    public static String site_code = "SOTS" ;
    public static String platform_code = "PULSE" ;
    public static String deployment_code = "PULSE-8-2012" ;

    public static String title = "Pulse 8 Mooring Data" ;
    
    public static String Abstract = "The Pulse 8 mooring was deployed from August 2011 to July 2012 at Lat -46.93, Lon 142.26. "
                                + "Moored instruments are deployed by the IMOS Australian Bluewater Observing System (ABOS) Southern Ocean Time Series sub-facility for "
                                + "time-series observations of physical, biological, and chemical properties, in the Sub-Antarctic Zone southwest of Tasmania, with yearly servicing. "
                                + "The Southern Ocean Time Series (SOTS) Sub-Facility is responsible for the deployment of Pulse moorings. "
                                + "These time-series observations are crucial to resolving ecosystem processes that affect carbon cycling, "
                                + "ocean productivity and marine responses to climate variability and change, ocean acidification and other stresses. "
                                ;

    public static String keywords = "Oceans->Ocean Chemistry->Biogeochemical Cycles, Oceans->Ocean Chemistry->Carbon, Oceans->Ocean Chemistry->Nitrate,"
                                + "Oceans->Ocean Chemistry->Water Temperature, Oceans->Ocean Optics->Turbidity, Oceans->Salinity/Depth->Salinity"
                                ;
}
