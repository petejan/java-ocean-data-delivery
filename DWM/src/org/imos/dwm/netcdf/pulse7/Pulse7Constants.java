/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.dwm.netcdf.pulse7;

import org.imos.dwm.netcdf.BaseNetCDFConstants;

/**
 *
 * @author peter
 */
public class Pulse7Constants extends BaseNetCDFConstants
{
    public static String time_coverage_start = "" ;
    public static String time_coverage_end = "" ;
    public static Double geospatial_lat_min = -46.93 ;
    public static Double geospatial_lat_max = -46.93 ;
    public static Double geospatial_lon_min = 142.26 ;
    public static Double geospatial_lon_max = 142.26 ;
    public static Double geospatial_vertical_min = 0.0 ;
    public static Double geospatial_vertical_max = 620.0 ;
    
    public static String level = "15" ;
    public static String field_trip_id = "" ;
    public static String field_trip_description = "SS-2011-V1" ;
    public static String site_code = "SOTS" ;
    public static String platform_code = "PULSE" ;
    public static String deployment_code = "PULSE-7-2010" ;     // year of deployment not recovery

    public static String title = "Pulse 7 Mooring Data" ;
    
    public static String Abstract = "The Pulse 7 mooring was deployed from September 2010 to April 2011 at Lat -46.93, Lon 142.26. "
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
