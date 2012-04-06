/*
 * Neonatal Screening Software Project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.netcdf;

/**
 *
 * @author peter
 */
public class BaseNetCDFConstants
{
    //
    // file naming codes
    //
    public static String PREFIX = "IMOS_";
    public static String FACILITY_CODE = "SOTS_";
    public static String DATA_CODE = "";
    public static String START_DATE = "";
    public static String PLATFORM_CODE = "_PULSE_";
    public static String FILE_VERSION = "FV01_";
    public static String PRODUCT_TYPE = "";
    public static String END_DATE = "END-";
    public static String CREATION_DATE = "_C-";
    public static String SUFFIX = ".nc";




    public static String instrument_make = "" ;
    public static String instrument_model = "" ;
    public static String instrument_serial_no = "" ;
    public static String level = "1." ;
    public static String date_created = null ;
    public static String field_trip_id = "" ;
    public static String field_trip_description = "" ;
    public static String project = "Integrated Marine Observing System (IMOS)" ;
    public static String conventions = "IMOS version 1.2" ;
    public static String title = "Pulse 7 Mooring Temperature & Pressure Data" ;
    public static String institution = "ANMN-NRS" ;
    public static String source = "Wetlabs WQM" ;
    public static String netcdf_version = "3.6" ;
    public static String quality_control_set = "1." ;
    public static String site_code = "" ;
    public static String platform_code = "NRSMAI" ;
    public static String naming_authority = "IMOS" ;
    public static String product_type = "" ;
    public static Double geospatial_lat_min = 0.0 ;
    public static Double geospatial_lat_max = 0.0 ;
    public static Double geospatial_lon_min = 0.0 ;
    public static Double geospatial_lon_max = 0.0 ;
    public static Double geospatial_vertical_min = 0.0 ;
    public static Double geospatial_vertical_max = 0.0 ;
    public static String time_coverage_start = "" ;
    public static String time_coverage_end = "" ;
    public static String local_time_zone = "UTC/GMT" ;
    public static String data_centre = "eMarine Information Infrastructure (eMII)" ;
    public static String data_centre_email = "info@emii.org.au" ;
    public static String author_email = "peter.jansen@csiro.au" ;
    public static String author = "Peter Jansen" ;
    public static String principal_investigator = "Tom Trull" ;
    public static String principal_investigator_email = "tom.trull@utas.edu.au" ;
    public static String acknowledgement = "Data was sourced from the Integrated Marine Observing System (IMOS) - an initiative of the Australian Government being conducted as part of the National Collaborative Research Infrastructure Strategy." ;
    public static String raw_data_file = "" ;
    public static String index = "1." ;
}