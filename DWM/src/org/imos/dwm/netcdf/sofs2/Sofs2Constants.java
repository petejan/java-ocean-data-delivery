/*
 * This code is copyright Peter Wiley 1998-2008
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 *
 */

package org.imos.dwm.netcdf.sofs2;

import org.imos.dwm.netcdf.BaseNetCDFConstants;

/**
 *
 * @author peter
 */
public class Sofs2Constants extends BaseNetCDFConstants
{

    public static String time_coverage_start = "2011-11-25 00:00:00Z" ;
    public static String time_coverage_end = "2012-07-21 00:00:00Z" ;
    public static Double geospatial_lat_min = -46.77 ;
    public static Double geospatial_lat_max = -46.77 ;
    public static Double geospatial_lon_min = 141.99 ;
    public static Double geospatial_lon_max = 141.99 ;
    public static Double geospatial_vertical_min = 0.0 ;
    public static Double geospatial_vertical_max = 500.0 ;
    
    public static String level = "1" ;
    public static String field_trip_id = "N/A" ;
    public static String field_trip_description = "N/A" ;
    public static String site_code = "SOTS" ;
    public static String platform_code = "SOFS" ;
    public static String deployment_code = "SOFS-2-2011" ;     // year of deployment not recovery

    public static String title = "SOFS 2 Mooring Data" ;
    
    public static String Abstract = "The SOFS 2 mooring was deployed from November 2011 to July 2012 at Lat -46.77, Lon 141.99. "
                                + "Moored instruments are deployed by the IMOS Australian Bluewater Observing System (ABOS) Southern Ocean Time Series sub-facility for "
                                + "time-series observations of physical, biological, and chemical properties, in the Sub-Antarctic Zone southwest of Tasmania, with yearly servicing. "
                                + "The Southern Ocean Time Series (SOTS) Sub-Facility is responsible for the deployment of SOFS moorings. "
                                + "These time-series observations are crucial to resolving ecosystem processes that affect carbon cycling, "
                                + "ocean productivity and marine responses to climate variability and change, ocean acidification and other stresses. "
                                ;

    public static String keywords = "Oceans->Ocean Chemistry->Biogeochemical Cycles, Oceans->Ocean Chemistry->Carbon, Oceans->Ocean Chemistry->Nitrate,"
                                + "Oceans->Ocean Chemistry->Water Temperature, Oceans->Ocean Optics->Turbidity, Oceans->Salinity/Depth->Salinity"
                                ;
}
