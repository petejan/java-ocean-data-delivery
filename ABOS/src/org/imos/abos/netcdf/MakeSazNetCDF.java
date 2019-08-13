package org.imos.abos.netcdf;
//
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.io.IOException;
//
//import ucar.ma2.*;
//import ucar.nc2.*;
//import ucar.nc2.NetcdfFile.*;
//import org.imos.abos.netcdf.saz.ReadSAZfile;
//
//public class MakeSazNetCDF
//{
//	NetcdfFileWriter ncfile;
//
//	/* dimension lengths */
//	int TIME_len = 21;
//	int DEPTH_len = 3;
////	final int LATITUDE_len = 1;
////	final int LONGITUDE_len = 1;
//	final int name_strlen = 20;
//
//	Dimension TIME_dim;
//	Dimension DEPTH_dim;
////	Dimension LATITUDE_dim;
////	Dimension LONGITUDE_dim;
//	Dimension timeSeries_dim;
//
//	Variable timeSeries;
//	Variable TIME;
//	Variable LATITUDE;
//	Variable LATITUDE_quality_control;
//	Variable LONGITUDE;
//	Variable LONGITUDE_quality_control;
//	Variable DEPTH;
//	Variable DURATION;
//	Variable MASS_FLUX;
//	Variable MASS_FLUX_quality_control;
//	Variable CACO3;
//	Variable CACO3_quality_control;
//	Variable PIC;
//	Variable PIC_quality_control;
//	Variable PC;
//	Variable PC_quality_control;
////	Variable H;
////	Variable H_quality_control;
//	Variable N;
//	Variable N_quality_control;
//	Variable POC;
//	Variable POC_quality_control;
//	Variable BSIO2;
//	Variable BSIO2_quality_control;
//
//	SimpleDateFormat sdf;
//	SimpleDateFormat sdf_netcdf;
//	SimpleDateFormat sdf_filename;
//	ReadSAZfile sf;
//	Date startTime;
//	Date endTime;
//	
//	String year;
//	
//	boolean IRS = false;
//	
//	public MakeSazNetCDF()
//	{
//		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		sdf_netcdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
//		sdf_filename = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
//		
//		IRS = false;
//	}
//	
//	public void generate(String fileName, String filter)
//	{
//		year = filter.split(",")[0];
//		try
//		{
//			sf = new ReadSAZfile();
//			
//			sf.readFile(fileName);
//			sf.parse(filter);
//                        
//                        System.out.println("Position " + sf.getPosition());
//                        if (sf.getPosition().matches(".*IRS"))
//                        {
//                            IRS = true;
//                            System.out.println("IRS TRAP DATA");
//                        }
//			
//			ArrayList <Date> time = sf.getTime();
//			startTime = time.get(0);
//			endTime = time.get(time.size()-1);
//			
//			DEPTH_len = sf.getDepths().size();
//			TIME_len = time.size()/DEPTH_len;
//
//			System.out.println("Time size " + time.size() + " rows " + sf.getDataRows() + " expected time_len : " + time.size()/DEPTH_len);
//			System.out.println("Depths " + sf.getDepths());
//			
//			// TODO: set TIME_len, DEPTH_len here
//			
////			ArrayList <Double> mf = sf.getNmass();
////			for(int i=0;i<time.size();i++)
////			{
////				System.out.println(sdf.format(time.get(i)) + " N Mass Flux " + mf.get(i));
////			}
//			//sf.dump();
//			
//			/* enter define mode */
//			String data = "Sediment-Trap-Data";
//			if (IRS)
//				data += "-IRS";
//			
//			String filename = "IMOS_ABOS-SOTS_RFK_"
//								+sdf_filename.format(startTime)
//								+"_SAZ47_FV01_SAZ47-"+year+data+"_"
//								+sdf_filename.format(endTime)
//								+"_C-"+sdf_filename.format(new Date())
//								+".nc";
//			
//			ncfile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, filename);
//			
//			System.out.println("Generating File : " + filename);
//			
//			defineDimensions();
//			defineVariables();
//			defineVariableAttributes();
//			defineGlobalAttributes();
//
//			ncfile.create();
//
//			variableData();
//
//			ncfile.close();
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//	}
//
//	/* define dimensions */
//	public void defineDimensions()
//	{
//		TIME_dim = ncfile.addDimension(null, "TIME", TIME_len);
//		DEPTH_dim = ncfile.addDimension(null, "DEPTH", DEPTH_len);
////		LATITUDE_dim = ncfile.addDimension(null, "LATITUDE", LATITUDE_len);
////		LONGITUDE_dim = ncfile.addDimension(null, "LONGITUDE", LONGITUDE_len);
//		timeSeries_dim = ncfile.addDimension(null, "name_strlen", name_strlen);
//	}
//
//	/* define variables */
//	public void defineVariables()
//	{
//		ArrayList name_dimlist = new ArrayList();
//		name_dimlist.add(timeSeries_dim);
//		timeSeries = ncfile.addVariable(null, "station_name", DataType.CHAR, name_dimlist);
//
//		ArrayList TIME_dimlist = new ArrayList();
//		TIME_dimlist.add(TIME_dim);
//		TIME = ncfile.addVariable(null, "TIME", DataType.DOUBLE, TIME_dimlist);
//
//		ArrayList LATITUDE_dimlist = new ArrayList();
//		//LATITUDE_dimlist.add(LATITUDE_dim);
//		LATITUDE = ncfile.addVariable(null, "LATITUDE", DataType.DOUBLE, LATITUDE_dimlist);
//
//		ArrayList LATITUDE_quality_control_dimlist = new ArrayList();
//		//LATITUDE_quality_control_dimlist.add(LATITUDE_dim);
//		LATITUDE_quality_control = ncfile.addVariable(null, "LATITUDE_quality_control", DataType.BYTE, LATITUDE_quality_control_dimlist);
//
//		ArrayList LONGITUDE_dimlist = new ArrayList();
//		//LONGITUDE_dimlist.add(LONGITUDE_dim);
//		LONGITUDE = ncfile.addVariable(null, "LONGITUDE", DataType.DOUBLE, LONGITUDE_dimlist);
//
//		ArrayList LONGITUDE_quality_control_dimlist = new ArrayList();
//		//LONGITUDE_quality_control_dimlist.add(LONGITUDE_dim);
//		LONGITUDE_quality_control = ncfile.addVariable(null, "LONGITUDE_quality_control", DataType.BYTE, LONGITUDE_quality_control_dimlist);
//
//		ArrayList DEPTH_dimlist = new ArrayList();
//		DEPTH_dimlist.add(DEPTH_dim);
//		DEPTH = ncfile.addVariable(null, "DEPTH", DataType.FLOAT, DEPTH_dimlist);
//
//		ArrayList dimlist = new ArrayList();
//		dimlist.add(TIME_dim);
//		dimlist.add(DEPTH_dim);
////		dimlist.add(LATITUDE_dim);
////		dimlist.add(LONGITUDE_dim);
//		
//		DURATION = ncfile.addVariable(null, "DURATION", DataType.FLOAT, dimlist);
//
//		MASS_FLUX = ncfile.addVariable(null, "MASS_FLUX", DataType.FLOAT, dimlist);
//		MASS_FLUX_quality_control = ncfile.addVariable(null, "MASS_FLUX_quality_control", DataType.BYTE, dimlist);
//
//		CACO3 = ncfile.addVariable(null, "CACO3", DataType.FLOAT, dimlist);
//		CACO3_quality_control = ncfile.addVariable(null, "CACO3_quality_control", DataType.BYTE, dimlist);
//
//		PIC = ncfile.addVariable(null, "PIC", DataType.FLOAT, dimlist);
//		PIC_quality_control = ncfile.addVariable(null, "PIC_quality_control", DataType.BYTE, dimlist);
//
//		PC = ncfile.addVariable(null, "PC", DataType.FLOAT, dimlist);
//		PC_quality_control = ncfile.addVariable(null, "PC_quality_control", DataType.BYTE, dimlist);
//
////		H = ncfile.addVariable(null, "H", DataType.FLOAT, dimlist);
////		H_quality_control = ncfile.addVariable(null, "H_quality_control", DataType.BYTE, dimlist);
//
//		N = ncfile.addVariable(null, "N", DataType.FLOAT, dimlist);
//		N_quality_control = ncfile.addVariable(null, "N_quality_control", DataType.BYTE, dimlist);
//
//		POC = ncfile.addVariable(null, "POC", DataType.FLOAT, dimlist);
//		POC_quality_control = ncfile.addVariable(null, "POC_quality_control", DataType.BYTE, dimlist);
//
//		BSIO2 = ncfile.addVariable(null, "BSIO2", DataType.FLOAT, dimlist);
//		BSIO2_quality_control = ncfile.addVariable(null, "BSIO2_quality_control", DataType.BYTE, dimlist);
//	}
//
//	/* assign global attributes */
//	public void defineGlobalAttributes()
//	{
//		/* attribute: file_version */
//		ncfile.addGroupAttribute(null, new Attribute("file_version", "Level 1 - Quality Controlled Data"));
//		/* attribute: file_version_quality_control */
//		ncfile.addGroupAttribute(null, new Attribute("file_version_quality_control", "Quality controlled data have passed quality assurance procedures such as automated or visual inspection and removal of obvious errors. The data are using standard SI metric units with calibration and other routine pre-processing applied, all time and location values are in absolute coordinates to agreed to standards and datum, metadata exists for the data or for the higher level dataset that the data belongs to. This is the standard IMOS data level and is what should be made available to eMII and to the IMOS community."));
//		/* attribute: project */
//		ncfile.addGroupAttribute(null, new Attribute("project", "Integrated Marine Observing System (IMOS)"));
//		/* attribute: Conventions */
//		ncfile.addGroupAttribute(null, new Attribute("Conventions", "CF-1.7,IMOS-1.3"));
//		/* attribute: Metadata_Conventions */
//		ncfile.addGroupAttribute(null, new Attribute("Metadata_Conventions", "Unidata Dataset Discovery v1.0"));
//		/* attribute: standard_name_vocabulary */
//		ncfile.addGroupAttribute(null, new Attribute("standard_name_vocabulary", "NetCDF Climate and Forecast (CF) Metadata Convention Standard Name Table 1.7"));
//		/* attribute: institution */
//		ncfile.addGroupAttribute(null, new Attribute("institution", "Antarctic Climate & Ecosystems Cooperative Research Centre"));
//		/* attribute: institution */
//		ncfile.addGroupAttribute(null, new Attribute("institution_address", "20 Castray Esplanade, Hobart Tasmania 7000, Australia"));
//		/* attribute: date_created */
//		ncfile.addGroupAttribute(null, new Attribute("date_created", sdf_netcdf.format(new Date())));
//		/* attribute: abstract */
//		ncfile.addGroupAttribute(null, new Attribute("abstract", "Southern Ocean Site at 46 South, with Pulse, SAZ, and SOFS moorings"));
//		/* attribute: comment */
//		if (IRS)
//			ncfile.addGroupAttribute(null, new Attribute("comment", "Sediment collected with IRS sediment trap at 1050m depth"));
//		else
//			ncfile.addGroupAttribute(null, new Attribute("comment", "Sediment collected with PARFLUX Mark78H-21 at three depths 1000, 2000, and 3900m depth"));
//			
//		/* attribute: source */
//		ncfile.addGroupAttribute(null, new Attribute("source", "Mooring"));
//		/* attribute: instrument */
//		if (IRS)
//			ncfile.addGroupAttribute(null, new Attribute("instrument", "Indented Rotating Sphere (IRS) Sediment Trap (Peterson et al 1993, 2005)"));
//		else
//			ncfile.addGroupAttribute(null, new Attribute("instrument", "PARFLUX Mark78H-21"));
//		/* attribute: keywords */
//		ncfile.addGroupAttribute(null, new Attribute("keywords", "Sediment Trap Data"));
//		/* attribute: references */
//		ncfile.addGroupAttribute(null, new Attribute("references", "http://www.imos.org.au"));
//		/* attribute: netcdf_version */
//		ncfile.addGroupAttribute(null, new Attribute("netcdf_version", "4.1.3"));
//		/* attribute: quality_control_set */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 1) });
//			ncfile.addGroupAttribute(null, new Attribute("quality_control_set", data));
//		}
//		/* attribute: site_code */
//		ncfile.addGroupAttribute(null, new Attribute("site_code", "SOTS"));
//		/* attribute: platform_code */
//		ncfile.addGroupAttribute(null, new Attribute("platform_code", "SAZ47"));
//		/* attribute: title */
//		ncfile.addGroupAttribute(null, new Attribute("title", "SAZ47-15-" + year + " Sediment trap mooring data"));
//		/* attribute: deployment_code */
//		ncfile.addGroupAttribute(null, new Attribute("deployment_code", "SAZ47-15-" + year));
//		/* attribute: featureType */
//		ncfile.addGroupAttribute(null, new Attribute("featureType", "timeSeries"));
//		/* attribute: cdm_data_type */
//		ncfile.addGroupAttribute(null, new Attribute("cdm_data_type", "Station"));		
//		/* attribute: naming_authority */
//		ncfile.addGroupAttribute(null, new Attribute("naming_authority", "IMOS"));
//		/* attribute: history */
//		ncfile.addGroupAttribute(null, new Attribute("history", sdf.format(new Date()) + " generated by Peter Jansen"));
//		/* attribute: geospatial_lat_min */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) -46.8304) });
//			ncfile.addGroupAttribute(null, new Attribute("geospatial_lat_min", data));
//		}
//		/* attribute: geospatial_lat_max */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) -46.8304) });
//			ncfile.addGroupAttribute(null, new Attribute("geospatial_lat_max", data));
//		}
//		/* attribute: geospatial_lon_min */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 141.6496833333) });
//			ncfile.addGroupAttribute(null, new Attribute("geospatial_lon_min", data));
//		}
//		/* attribute: geospatial_lon_max */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 141.6496833333) });
//			ncfile.addGroupAttribute(null, new Attribute("geospatial_lon_max", data));
//		}
//		/* attribute: site_nominal_depth */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 4580) });
//			ncfile.addGroupAttribute(null, new Attribute("site_nominal_depth", data));
//		}
//		/* attribute: geospatial_vertical_min */
//		/* attribute: geospatial_vertical_max */
//		{
//                        ArrayList<Double> depths = sf.getDepths();                            
//
//                        double dmin = depths.get(0);
//                        double dmax = depths.get(0);
//                        for(double d : depths)
//                        {
//                            if (dmin > d) dmin = d;
//                            if (dmax < d) dmax = d;
//                        }
//                                                                                    
//			ncfile.addGroupAttribute(null, new Attribute("geospatial_vertical_min", new Float(dmin)));
//			ncfile.addGroupAttribute(null, new Attribute("geospatial_vertical_max", new Float(dmax)));
//		}
//		/* attribute: geospatial_vertical_positive */
//		ncfile.addGroupAttribute(null, new Attribute("geospatial_vertical_positive", "down"));
//		/* attribute: time_deployment_start */
//		ncfile.addGroupAttribute(null, new Attribute("time_deployment_start", sdf_netcdf.format(startTime)));
//		/* attribute: time_deployment_end */
//		ncfile.addGroupAttribute(null, new Attribute("time_deployment_end", sdf_netcdf.format(endTime)));
//		/* attribute: time_coverage_start */
//		ncfile.addGroupAttribute(null, new Attribute("time_coverage_start", sdf_netcdf.format(startTime)));
//		/* attribute: time_coverage_end */
//		ncfile.addGroupAttribute(null, new Attribute("time_coverage_end", sdf_netcdf.format(endTime)));
//		/* attribute: time_deployment_end_origin */
//		ncfile.addGroupAttribute(null, new Attribute("data_centre", "eMarine Information Infrastructure (eMII)"));
//		/* attribute: data_centre_email */
//		ncfile.addGroupAttribute(null, new Attribute("data_centre_email", "info@emii.org.au"));
//		/* attribute: author_email */
//		ncfile.addGroupAttribute(null, new Attribute("author_email", "peter.jansen@csiro.au"));
//		/* attribute: author */
//		ncfile.addGroupAttribute(null, new Attribute("author", "Jansen, Peter"));
//		/* attribute: principal_investigator */
//		ncfile.addGroupAttribute(null, new Attribute("principal_investigator", "Trull, Tom"));
//		/* attribute: principal_investigator_email */
//		ncfile.addGroupAttribute(null, new Attribute("principal_investigator_email", "tom.trull@csiro.au"));
//		/* attribute: institution_references */
//		ncfile.addGroupAttribute(null, new Attribute("institution_references", "http://www.imos.org.au/emii.html"));
//		/* attribute: citation */
//		ncfile.addGroupAttribute(null, new Attribute("citation", "The citation in a list of references is: \"IMOS [year-of-data-download], [Title], [data-access-url], accessed [date-of-access]\""));
//		/* attribute: acknowledgement */
//		ncfile.addGroupAttribute(null, new Attribute("acknowledgement",	"Any users of IMOS data are required to clearly acknowledge the source of the material in the format: \"Data was sourced from the Integrated Marine Observing System (IMOS) - an initiative of the Australian Government being conducted as part of the National Collaborative Research Infrastructure Strategy and and the Super Science Initiative.\""));
//		/* attribute: distribution_statement */
//		ncfile.addGroupAttribute(null,	new Attribute("distribution_statement",	"Data may be re-used, provided that related metadata explaining the data has been reviewed by the user, and the data is appropriately acknowledged. Data, products and services from IMOS are provided \"as is\" without any warranty as to fitness for a particular purpose."));
//		/* attribute: project_acknowledgement */
//		ncfile.addGroupAttribute(null, new Attribute("project_acknowledgement", "The collection of this data was funded by IMOS"));
//		/* attribute: contributor_name */
//		ncfile.addGroupAttribute(null, new Attribute("contributor_name", "Stephen Bray"));
//		/* attribute: contributor_role */
//		ncfile.addGroupAttribute(null, new Attribute("contributor_role", "data_collection_sample_analysis"));
//	}
//
//	/* assign per-variable attributes */
//	public void defineVariableAttributes()
//	{
//		Array flag_values = Array.factory(byte.class, new int[] { 7 }, new byte[] { 0, 1, 2, 3, 4, 5, 9 });
//		Attribute flag_meanings = new Attribute("flag_meanings", "No_QC_performed Good_data Probably_good_data Bad_data_that_are_potentially_correctable Bad_data Value_changed Missing_value");
//				
//		Attribute sensor_name = new Attribute("sensor_name", "McLane PARFLUX Mark78H-21");
//		Attribute sensor_serial_number = new Attribute("sensor_serial_number", "11741-01; 11649-01; 11640-01");
//
//		if (IRS)
//		{
//			sensor_name = new Attribute("sensor_name", "Indented Rotating Sphere (IRS)");
//			sensor_serial_number = new Attribute("sensor_serial_number", "IRS");
//		}
//		
//		/* attribute: name */
//		timeSeries.addAttribute(new Attribute("long_name", "instance station name"));
//		/* attribute: standard_name */
//		timeSeries.addAttribute(new Attribute("cf_role", "timeseries_id"));
//
//		/* attribute: name */
//		TIME.addAttribute(new Attribute("name", "TIME"));
//		/* attribute: standard_name */
//		TIME.addAttribute(new Attribute("standard_name", "time"));
//		/* attribute: units */
//		TIME.addAttribute(new Attribute("units", "days since 1950-01-01 00:00:00 UTC"));
//		/* attribute: calendar */
//		TIME.addAttribute(new Attribute("calendar", "gregorian"));
//		/* attribute: axis */
//		TIME.addAttribute(new Attribute("axis", "T"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 10957) });
//			TIME.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 54787) });
//			TIME.addAttribute(new Attribute("valid_max", data));
//		}
////		/* attribute: _FillValue */
////		{
////			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 999999) });
////			TIME.addAttribute(new Attribute("_FillValue", data));
////		}
//		
//		/* attribute: name */
//		LATITUDE.addAttribute(new Attribute("name", "LATITUDE"));
//		/* attribute: standard_name */
//		LATITUDE.addAttribute(new Attribute("standard_name", "latitude"));
//		/* attribute: long_name */
//		LATITUDE.addAttribute(new Attribute("long_name", "anchor latitude"));
//		/* attribute: units */
//		LATITUDE.addAttribute(new Attribute("units", "degrees_north"));
//		/* attribute: axis */
//		LATITUDE.addAttribute(new Attribute("axis", "Y"));
//		/* attribute: reference_datum */
//		LATITUDE.addAttribute(new Attribute("reference_datum", "geographical coordinates, WGS84 projection"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) -90) });
//			LATITUDE.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 90) });
//			LATITUDE.addAttribute(new Attribute("valid_max", data));
//		}
////		/* attribute: _FillValue */
////		{
////			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 999999) });
////			LATITUDE.addAttribute(new Attribute("_FillValue", data));
////		}
//		/* attribute: ancillary_variables */
//		LATITUDE.addAttribute(new Attribute("ancillary_variables", "LATITUDE_quality_control"));
//		/* attribute: long_name */
//		LATITUDE_quality_control.addAttribute(new Attribute("long_name", "quality flag for latitude"));
//		/* attribute: standard_name */
//		LATITUDE_quality_control.addAttribute(new Attribute("standard_name", "latitude status_flag"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 0 });
//			LATITUDE_quality_control.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 9 });
//			LATITUDE_quality_control.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 99 });
//			LATITUDE_quality_control.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: quality_control_set */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 1) });
//			LATITUDE_quality_control.addAttribute(new Attribute("quality_control_set", data));
//		}
//		/* attribute: quality_control_conventions */
//		LATITUDE_quality_control.addAttribute(new Attribute("quality_control_conventions", "IMOS standard set using the IODE flags"));
//		/* attribute: flag_values */
//		{
//			LATITUDE_quality_control.addAttribute(new Attribute("flag_values", flag_values));
//		}
//		/* attribute: flag_meanings */
//		LATITUDE_quality_control.addAttribute(flag_meanings);
//		
//		/* attribute: name */
//		LONGITUDE.addAttribute(new Attribute("name", "LONGITUDE"));
//		/* attribute: standard_name */
//		LONGITUDE.addAttribute(new Attribute("standard_name", "longitude"));
//		/* attribute: units */
//		LONGITUDE.addAttribute(new Attribute("units", "degrees_east"));
//		/* attribute: axis */
//		LONGITUDE.addAttribute(new Attribute("axis", "X"));
//		/* attribute: reference_datum */
//		LONGITUDE.addAttribute(new Attribute("reference_datum", "geographical coordinates, WGS84 projection"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) -180) });
//			LONGITUDE.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 180) });
//			LONGITUDE.addAttribute(new Attribute("valid_max", data));
//		}
////		/* attribute: _FillValue */
////		{
////			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 999999) });
////			LONGITUDE.addAttribute(new Attribute("_FillValue", data));
////		}
//		/* attribute: ancillary_variables */
//		LONGITUDE.addAttribute(new Attribute("ancillary_variables", "LONGITUDE_quality_control"));
//		/* attribute: long_name */
//		LONGITUDE_quality_control.addAttribute(new Attribute("long_name", "quality flag for longitude"));
//		/* attribute: standard_name */
//		LONGITUDE_quality_control.addAttribute(new Attribute("standard_name", "longitude status_flag"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 0 });
//			LONGITUDE_quality_control.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 9 });
//			LONGITUDE_quality_control.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 99 });
//			LONGITUDE_quality_control.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: quality_control_set */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 1) });
//			LONGITUDE_quality_control.addAttribute(new Attribute("quality_control_set", data));
//		}
//		/* attribute: quality_control_conventions */
//		LONGITUDE_quality_control.addAttribute(new Attribute("quality_control_conventions", "IMOS standard set using the IODE flags"));
//		/* attribute: flag_values */
//		LONGITUDE_quality_control.addAttribute(new Attribute("flag_values", flag_values));
//		/* attribute: flag_meanings */
//		LONGITUDE_quality_control.addAttribute(flag_meanings);
//		
//		/* attribute: name */
//                
//		DEPTH.addAttribute(new Attribute("name", "nominal_depth"));
//		/* attribute: long_name */
//		DEPTH.addAttribute(new Attribute("long_name", "nominal depth of each sensor"));
//		/* attribute: units */
//		DEPTH.addAttribute(new Attribute("units", "meters"));
//		/* attribute: positive */
//		DEPTH.addAttribute(new Attribute("positive", "down"));
//		/* attribute: axis */
//		DEPTH.addAttribute(new Attribute("axis", "Z"));
//		/* attribute: comment */
//		DEPTH.addAttribute(new Attribute("comment", "These are nominal values. Use PRES to derive time-varying depths of instruments, as the mooring may tilt in ambient currents."));
//		/* attribute: reference */
//		DEPTH.addAttribute(new Attribute("reference", "sea_level"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 0) });
//			DEPTH.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 4000) });
//			DEPTH.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: name */
//		DURATION.addAttribute(new Attribute("name", "cup open time in days"));
//		/* attribute: long_name */
//		DURATION.addAttribute(new Attribute("long_name", "cup_open_time"));
//		/* attribute: units */
//		DURATION.addAttribute(new Attribute("units", "days"));
//		/* attribute: comment */
//		DURATION.addAttribute(new Attribute("comment", "TIME is cup open time"));
//		
//		/* attribute: name */
//		MASS_FLUX.addAttribute(new Attribute("name", "mass flux"));
//		/* attribute: long_name */
//		MASS_FLUX.addAttribute(new Attribute("long_name", "sinking_mass_flux_of_particulate_matter_in_sea_water"));
//		/* attribute: units */
//		MASS_FLUX.addAttribute(new Attribute("units", "g m-2 year-1"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 0) });
//			MASS_FLUX.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 100) });
//			MASS_FLUX.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { Float.NaN });
//			MASS_FLUX.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: ancillary_variables */
//		MASS_FLUX.addAttribute(new Attribute("ancillary_variables", "MASS_FLUX_quality_control"));
//		/* attribute: sensor_name */
//		MASS_FLUX.addAttribute(sensor_name);
//		/* attribute: sensor_serial_number */
//		MASS_FLUX.addAttribute(sensor_serial_number);
//		/* attribute: comment */
//		MASS_FLUX.addAttribute(new Attribute("comment", "flux of particles < 1 mm in size, this is flux that passed a 1mm sieve - there are many long skinny bits which can get though which have some dimension (eg length) >1mm. Zooplankton (swimmers) not separated"));
//		/* attribute: coordinates */
//		MASS_FLUX.addAttribute(new Attribute("coordinates", "TIME LATITUDE LONGITUDE DEPTH"));
//				
//		/* attribute: long_name */
//		MASS_FLUX_quality_control.addAttribute(new Attribute("long_name", "quality flag for sinking_mass_flux_of_particulate_matter_in_sea_water"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 0 });
//			MASS_FLUX_quality_control.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 9 });
//			MASS_FLUX_quality_control.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 99 });
//			MASS_FLUX_quality_control.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: quality_control_set */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 1) });
//			MASS_FLUX_quality_control.addAttribute(new Attribute("quality_control_set", data));
//		}
//		/* attribute: quality_control_conventions */
//		MASS_FLUX_quality_control.addAttribute(new Attribute("quality_control_conventions", "IMOS standard set using the IODE flags"));
//		/* attribute: flag_values */
//		MASS_FLUX_quality_control.addAttribute(new Attribute("flag_values", flag_values));
//		/* attribute: flag_meanings */
//		MASS_FLUX_quality_control.addAttribute(flag_meanings);
//		
//		/* attribute: name */
//		CACO3.addAttribute(new Attribute("name", "CACO3"));
//		/* attribute: long_name */
//		CACO3.addAttribute(new Attribute("long_name", "sinking_flux_of_particulate_calcium_carbonate_in_sea_water"));
//		/* attribute: units */
//		CACO3.addAttribute(new Attribute("units", "g m-2 year-1"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 0) });
//			CACO3.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 100) });
//			CACO3.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { Float.NaN });
//			CACO3.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: ancillary_variables */
//		CACO3.addAttribute(new Attribute("ancillary_variables", "CACO3_quality_control"));
//		/* attribute: sensor_name */
//		CACO3.addAttribute(sensor_name);
//		/* attribute: sensor_serial_number */
//		CACO3.addAttribute(sensor_serial_number);
//		/* attribute: comment */
//		CACO3.addAttribute(new Attribute("comment", "flux of particals < 1 mm in size"));
//		/* attribute: long_name */
//		CACO3_quality_control.addAttribute(new Attribute("long_name", "quality flag for sinking_flux_of_particulate_calcium_carbonate_in_sea_water"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 0 });
//			CACO3_quality_control.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 9 });
//			CACO3_quality_control.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 99 });
//			CACO3_quality_control.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: quality_control_set */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 1) });
//			CACO3_quality_control.addAttribute(new Attribute("quality_control_set", data));
//		}
//		/* attribute: quality_control_conventions */
//		CACO3_quality_control.addAttribute(new Attribute("quality_control_conventions", "IMOS standard set using the IODE flags"));
//		/* attribute: flag_values */
//		CACO3_quality_control.addAttribute(new Attribute("flag_values", flag_values));
//		/* attribute: flag_meanings */
//		CACO3_quality_control.addAttribute(flag_meanings);
//		
//		/* attribute: name */
//		PIC.addAttribute(new Attribute("name", "PIC"));
//		/* attribute: long_name */
//		PIC.addAttribute(new Attribute("long_name", "sinking_flux_of_particulate_inorganic_carbon_in_sea_water"));
//		/* attribute: units */
//		PIC.addAttribute(new Attribute("units", "g m-2 year-1"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 0) });
//			PIC.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 100) });
//			PIC.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { Float.NaN });
//			PIC.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: ancillary_variables */
//		PIC.addAttribute(new Attribute("ancillary_variables", "PIC_quality_control"));
//		/* attribute: sensor_name */
//		PIC.addAttribute(sensor_name);
//		/* attribute: sensor_serial_number */
//		PIC.addAttribute(sensor_serial_number);
//		/* attribute: comment */
//		PIC.addAttribute(new Attribute("comment", "flux of particals < 1 mm in size"));
//		/* attribute: long_name */
//		PIC_quality_control.addAttribute(new Attribute("long_name", "quality flag for sinking_flux_of_particulate_inorganic_carbon_in_sea_water"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 0 });
//			PIC_quality_control.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 9 });
//			PIC_quality_control.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 99 });
//			PIC_quality_control.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: quality_control_set */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 1) });
//			PIC_quality_control.addAttribute(new Attribute("quality_control_set", data));
//		}
//		/* attribute: quality_control_conventions */
//		PIC_quality_control.addAttribute(new Attribute("quality_control_conventions", "IMOS standard set using the IODE flags"));
//		/* attribute: flag_values */
//		PIC_quality_control.addAttribute(new Attribute("flag_values", flag_values));
//		/* attribute: flag_meanings */
//		PIC_quality_control.addAttribute(flag_meanings);
//		
//		/* attribute: name */
//		PC.addAttribute(new Attribute("name", "PC"));
//		/* attribute: long_name */
//		PC.addAttribute(new Attribute("long_name", "sinking_flux_of_particulate_carbon_in_sea_water"));
//		/* attribute: units */
//		PC.addAttribute(new Attribute("units", "g m-2 year-1"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 0) });
//			PC.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 100) });
//			PC.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { Float.NaN });
//			PC.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: ancillary_variables */
//		PC.addAttribute(new Attribute("ancillary_variables", "PC_quality_control"));
//		/* attribute: sensor_name */
//		PC.addAttribute(sensor_name);
//		/* attribute: sensor_serial_number */
//		PC.addAttribute(sensor_serial_number);
//		/* attribute: comment */
//		PC.addAttribute(new Attribute("comment", "flux of particals < 1 mm in size"));
//		/* attribute: long_name */
//		PC_quality_control.addAttribute(new Attribute("long_name", "quality flag for sinking_flux_of_particulate_carbon_in_sea_water"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 0 });
//			PC_quality_control.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 9 });
//			PC_quality_control.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 99 });
//			PC_quality_control.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: quality_control_set */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 1) });
//			PC_quality_control.addAttribute(new Attribute("quality_control_set", data));
//		}
//		/* attribute: quality_control_conventions */
//		PC_quality_control.addAttribute(new Attribute("quality_control_conventions", "IMOS standard set using the IODE flags"));
//		/* attribute: flag_values */
//		PC_quality_control.addAttribute(new Attribute("flag_values", flag_values));
//		/* attribute: flag_meanings */
//		PC_quality_control.addAttribute(flag_meanings);
//		
////		/* attribute: name */
////		H.addAttribute(new Attribute("name", "Hydrogen"));
////		/* attribute: long_name */
////		H.addAttribute(new Attribute("long_name", "sinking_flux_of_particulate_hydrogen_in_sea_water"));
////		/* attribute: units */
////		H.addAttribute(new Attribute("units", "g m-2 year-1"));
////		/* attribute: valid_min */
////		{
////			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 0) });
////			H.addAttribute(new Attribute("valid_min", data));
////		}
////		/* attribute: valid_max */
////		{
////			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 100) });
////			H.addAttribute(new Attribute("valid_max", data));
////		}
////		/* attribute: _FillValue */
////		{
////			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { Float.NaN });
////			H.addAttribute(new Attribute("_FillValue", data));
////		}
////		/* attribute: ancillary_variables */
////		H.addAttribute(new Attribute("ancillary_variables", "H_quality_control"));
////		/* attribute: sensor_name */
////		H.addAttribute(sensor_name);
////		/* attribute: sensor_serial_number */
////		H.addAttribute(sensor_serial_number);
////		/* attribute: comment */
////		H.addAttribute(new Attribute("comment", "flux of particals < 1 mm in size"));
////		/* attribute: long_name */
////		H_quality_control.addAttribute(new Attribute("long_name", "quality flag for sinking_flux_of_particulate_hydrogen_in_sea_water"));
////		/* attribute: valid_min */
////		{
////			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 0 });
////			H_quality_control.addAttribute(new Attribute("valid_min", data));
////		}
////		/* attribute: valid_max */
////		{
////			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 9 });
////			H_quality_control.addAttribute(new Attribute("valid_max", data));
////		}
////		/* attribute: _FillValue */
////		{
////			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 99 });
////			H_quality_control.addAttribute(new Attribute("_FillValue", data));
////		}
////		/* attribute: quality_control_set */
////		{
////			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 1) });
////			H_quality_control.addAttribute(new Attribute("quality_control_set", data));
////		}
////		/* attribute: quality_control_conventions */
////		H_quality_control.addAttribute(new Attribute("quality_control_conventions", "IMOS standard set using the IODE flags"));
////		/* attribute: flag_values */
////		H_quality_control.addAttribute(new Attribute("flag_values", flag_values));
////		/* attribute: flag_meanings */
////		H_quality_control.addAttribute(flag_meanings);
//		
//		/* attribute: name */
//		N.addAttribute(new Attribute("name", "Nitrogen"));
//		/* attribute: long_name */
//		N.addAttribute(new Attribute("long_name", "sinking_flux_of_particulate_nitrogen_in_sea_water"));
//		/* attribute: units */
//		N.addAttribute(new Attribute("units", "g m-2 year-1"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 0) });
//			N.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 100) });
//			N.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { Float.NaN });
//			N.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: ancillary_variables */
//		N.addAttribute(new Attribute("ancillary_variables", "N_quality_control"));
//		/* attribute: sensor_name */
//		N.addAttribute(sensor_name);
//		/* attribute: sensor_serial_number */
//		N.addAttribute(sensor_serial_number);
//		/* attribute: comment */
//		N.addAttribute(new Attribute("comment", "flux of particals < 1 mm in size"));
//		/* attribute: long_name */
//		N_quality_control.addAttribute(new Attribute("long_name", "quality flag for sinking_flux_of_particulate_nitrogen_in_sea_water"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 0 });
//			N_quality_control.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 9 });
//			N_quality_control.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 99 });
//			N_quality_control.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: quality_control_set */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 1) });
//			N_quality_control.addAttribute(new Attribute("quality_control_set", data));
//		}
//		/* attribute: quality_control_conventions */
//		N_quality_control.addAttribute(new Attribute("quality_control_conventions", "IMOS standard set using the IODE flags"));
//		/* attribute: flag_values */
//		N_quality_control.addAttribute(new Attribute("flag_values", flag_values));
//		/* attribute: flag_meanings */
//		N_quality_control.addAttribute(flag_meanings);
//		
//		/* attribute: name */
//		POC.addAttribute(new Attribute("name", "POC"));
//		/* attribute: long_name */
//		POC.addAttribute(new Attribute("long_name", "sinking_flux_of_particulate_organic_carbon_in_sea_water"));
//		/* attribute: units */
//		POC.addAttribute(new Attribute("units", "g m-2 year-1"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 0) });
//			POC.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 100) });
//			POC.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { Float.NaN });
//			POC.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: ancillary_variables */
//		POC.addAttribute(new Attribute("ancillary_variables", "POC_quality_control"));
//		/* attribute: sensor_name */
//		POC.addAttribute(sensor_name);
//		/* attribute: sensor_serial_number */
//		POC.addAttribute(sensor_serial_number);
//		/* attribute: comment */
//		POC.addAttribute(new Attribute("comment", "flux of particals < 1 mm in size"));
//		/* attribute: long_name */
//		POC_quality_control.addAttribute(new Attribute("long_name", "quality flag for sinking_flux_of_particulate_carbon_in_sea_water"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 0 });
//			POC_quality_control.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 9 });
//			POC_quality_control.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 99 });
//			POC_quality_control.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: quality_control_set */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 1) });
//			POC_quality_control.addAttribute(new Attribute("quality_control_set", data));
//		}
//		/* attribute: quality_control_conventions */
//		POC_quality_control.addAttribute(new Attribute("quality_control_conventions", "IMOS standard set using the IODE flags"));
//		/* attribute: flag_values */
//		POC_quality_control.addAttribute(new Attribute("flag_values", flag_values));
//		/* attribute: flag_meanings */
//		POC_quality_control.addAttribute(flag_meanings);
//		
//		/* attribute: name */
//		BSIO2.addAttribute(new Attribute("name", "BSIO2"));
//		/* attribute: long_name */
//		BSIO2.addAttribute(new Attribute("long_name", "sinking_flux_of_particulate_biogenic_silicate_in_sea_water"));
//		/* attribute: units */
//		BSIO2.addAttribute(new Attribute("units", "g m-2 year-1"));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 0) });
//			BSIO2.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { ((float) 100) });
//			BSIO2.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(float.class, new int[] { 1 }, new float[] { Float.NaN });
//			BSIO2.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: ancillary_variables */
//		BSIO2.addAttribute(new Attribute("ancillary_variables", "BSIO2_quality_control"));
//		/* attribute: sensor_name */
//		BSIO2.addAttribute(sensor_name);
//		/* attribute: sensor_serial_number */
//		BSIO2.addAttribute(sensor_serial_number);
//		/* attribute: comment */
//		BSIO2.addAttribute(new Attribute("comment", "flux of particals < 1 mm in size"));
//		/* attribute: long_name */
//		BSIO2_quality_control.addAttribute(new Attribute("long_name", "quality flag for sinking_flux_of_particulate_biogenic_silicate_in_sea_water  "));
//		/* attribute: valid_min */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 0 });
//			BSIO2_quality_control.addAttribute(new Attribute("valid_min", data));
//		}
//		/* attribute: valid_max */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 9 });
//			BSIO2_quality_control.addAttribute(new Attribute("valid_max", data));
//		}
//		/* attribute: _FillValue */
//		{
//			Array data = Array.factory(byte.class, new int[] { 1 }, new byte[] { 99 });
//			BSIO2_quality_control.addAttribute(new Attribute("_FillValue", data));
//		}
//		/* attribute: quality_control_set */
//		{
//			Array data = Array.factory(double.class, new int[] { 1 }, new double[] { ((double) 1) });
//			BSIO2_quality_control.addAttribute(new Attribute("quality_control_set", data));
//		}
//		/* attribute: quality_control_conventions */
//		BSIO2_quality_control.addAttribute(new Attribute("quality_control_conventions", "IMOS standard set using the IODE flags"));
//		/* attribute: flag_values */
//		BSIO2_quality_control.addAttribute(new Attribute("flag_values", flag_values));
//		/* attribute: flag_meanings */
//		BSIO2_quality_control.addAttribute(flag_meanings);
//	}
//
//	/* assign variable data */
//	public void variableData() throws IOException, InvalidRangeException
//	{
//		{
//			ArrayChar data = new ArrayChar(new int[] { name_strlen });
//			data.setString("ABOS-SOTS SAZ");
//			
//			ncfile.write(timeSeries, data);
//		}
//		{
//			ArrayList <Date>contents = sf.getTime();
//			Calendar c = new GregorianCalendar(1950, 0, 1);
//			c.setTimeZone(TimeZone.getDefault());
//			
//			Date netCDFBase = c.getTime(); 
//
//			ArrayDouble data = new ArrayDouble(new int[] { TIME_len });
//			IndexIterator iter = data.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//			{
//				iter.setDoubleNext((contents.get(count).getTime() - netCDFBase.getTime()) / 1000 / (24 * 3600));
//				count+=DEPTH_len;
//			}
//			
//			ncfile.write(TIME, data);
//		}
//
//		{
//			ArrayDouble.D0 data = new ArrayDouble.D0();
//			data.set(-46.8304);
//			
//			ncfile.write(LATITUDE, data);
//		}
//
//		{
//			ArrayByte.D0 data = new ArrayByte.D0();
//			data.set((byte)1);
//			
//			ncfile.write(LATITUDE_quality_control, data);
//		}
//
//		{
//			ArrayDouble.D0 data = new ArrayDouble.D0();
//			data.set(141.6496);
//			
//			ncfile.write(LONGITUDE, data);
//		}
//
//		{
//			ArrayByte.D0 data = new ArrayByte.D0();
//			data.set((byte)1);
//			
//			ncfile.write(LONGITUDE_quality_control, data);			
//		}
//
//		{
//			ArrayList<Double> contents = sf.getDepths();
//			ArrayFloat data = new ArrayFloat(new int[] { DEPTH_len });
//			IndexIterator iter = data.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//				iter.setFloatNext(contents.get(count++).floatValue());
//			
//			ncfile.write(DEPTH, data);
//		}
//
//		int[] dims = new int[] {TIME_len, DEPTH_len};
//		{
//			ArrayList <Double> contents = sf.getDuration();
//			ArrayFloat data = new ArrayFloat(dims);
//			IndexIterator iter = data.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//				iter.setFloatNext(contents.get(count++).floatValue());
//			ncfile.write(DURATION, data);
//		}
//
//		{
//			ArrayList <Double> contents = sf.getMassFlux();
//			
//			ArrayFloat data = new ArrayFloat(dims);
//			IndexIterator iter = data.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//				iter.setFloatNext(contents.get(count++).floatValue());
//			
//			ncfile.write(MASS_FLUX, data);
//		}
//
//		{
//			ArrayList <Double> contents = sf.getMassFlux();
//			ArrayByte data = new ArrayByte(dims);
//			IndexIterator iter = data.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//				iter.setByteNext(contents.get(count++).isNaN() ? (byte)3 : (byte)1);
//
//			ncfile.write(MASS_FLUX_quality_control, data);
//		}
//
//		{
//			ArrayList <Double> contents = sf.getCACO3();
//			ArrayFloat data = new ArrayFloat(dims);
//			IndexIterator iter = data.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//				iter.setFloatNext(contents.get(count++).floatValue());
//			
//			ncfile.write(CACO3, data);
//		}
//
//		{
//			ArrayList <Double> contents = sf.getCACO3();
//			ArrayByte data = new ArrayByte(dims);
//			IndexIterator iter = data.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//				iter.setByteNext(contents.get(count++).isNaN() ? (byte)3 : (byte)1);
//
//			ncfile.write(CACO3_quality_control, data);
//		}
//
//		{
//			ArrayList <Double> contents = sf.getPIC();
//			ArrayFloat data = new ArrayFloat(dims);
//			IndexIterator iter = data.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//				iter.setFloatNext(contents.get(count++).floatValue());
//			
//			int[] origin = new int[] { 0, 0, 0, 0 };
//			ncfile.write(PIC, origin, data);
//		}
//
//		{
//			ArrayList <Double> contents = sf.getPIC();
//			ArrayByte data = new ArrayByte(dims);
//			IndexIterator iter = data.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//				iter.setByteNext(contents.get(count++).isNaN() ? (byte)3 : (byte)1);
//
//			ncfile.write(PIC_quality_control, data);
//		}
//
//		{
//			ArrayList <Double> contents = sf.getPC();
//			ArrayFloat data = new ArrayFloat(dims);
//			IndexIterator iter = data.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//				iter.setFloatNext(contents.get(count++).floatValue());
//
//			ncfile.write(PC, data);
//		}
//
//		{
//			ArrayList <Double> contents = sf.getPC();
//			ArrayByte data = new ArrayByte(dims);
//			IndexIterator iter = data.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//				iter.setByteNext(contents.get(count++).isNaN() ? (byte)3 : (byte)1);
//			
//			ncfile.write(PC_quality_control, data);
//		}
//
////		{
////			ArrayList <Double> contents = sf.getHmass();
////			ArrayFloat data = new ArrayFloat(dims);
////			IndexIterator iter = data.getIndexIterator();
////			int count = 0;
////			while (iter.hasNext())
////				iter.setFloatNext(contents.get(count++).floatValue());
////
////			ncfile.write(H, data);
////		}
////
////		{
////			ArrayList <Double> contents = sf.getHmass();
////			ArrayByte data = new ArrayByte(dims);
////			IndexIterator iter = data.getIndexIterator();
////			int count = 0;
////			while (iter.hasNext())
////				iter.setByteNext(contents.get(count++).isNaN() ? (byte)3 : (byte)1);
////
////			ncfile.write(H_quality_control, data);
////		}
//
//		{
//			ArrayList <Double> contents = sf.getNmass();
//			ArrayFloat data = new ArrayFloat(dims);
//			IndexIterator iter = data.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//				iter.setFloatNext(contents.get(count++).floatValue());
//
//			ncfile.write(N, data);
//		}
//
//		{
//			ArrayList <Double> contents = sf.getNmass();
//			ArrayByte data = new ArrayByte(dims);
//			IndexIterator iter = data.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//				iter.setByteNext(contents.get(count++).isNaN() ? (byte)3 : (byte)1);
//
//			ncfile.write(N_quality_control, data);
//		}
//
//		{
//			ArrayList <Double> contents = sf.getPOC();
//			ArrayFloat data = new ArrayFloat(dims);
//			IndexIterator iter = data.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//				iter.setFloatNext(contents.get(count++).floatValue());
//
//			ncfile.write(POC, data);
//		}
//
//		{
//			ArrayList <Double> contents = sf.getPOC();
//			ArrayByte data = new ArrayByte(dims);
//			IndexIterator iter = data.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//				iter.setByteNext(contents.get(count++).isNaN() ? (byte)3 : (byte)1);
//
//			ncfile.write(POC_quality_control, data);
//		}
//
//		{
//			ArrayList <Double> contents = sf.getBSiO2();
//			ArrayFloat data = new ArrayFloat(dims);
//			IndexIterator iter = data.getIndexIterator();
//			ArrayByte data_QC = new ArrayByte(dims);
//			IndexIterator iter_QC = data_QC.getIndexIterator();
//			int count = 0;
//			while (iter.hasNext())
//			{
//				float f = contents.get(count++).floatValue();
//				iter.setFloatNext(f);
//				iter_QC.setByteNext(Float.isNaN(f) ? (byte)3 : (byte)1);
//			}
//
//			ncfile.write(BSIO2, data);
//			ncfile.write(BSIO2_quality_control, data_QC);
//		}
//	}
//
//	static public void main(String[] argv)
//	{
//		MakeSazNetCDF nc = new MakeSazNetCDF();
//		
//		nc.generate(argv[0], argv[1]);
//
//	}
//}
