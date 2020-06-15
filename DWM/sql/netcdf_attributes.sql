--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

--
-- Name: netcdf_attribute_sequence; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE netcdf_attribute_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.netcdf_attribute_sequence OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: netcdf_attributes; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE netcdf_attributes (
    netcdf_attribute_id integer DEFAULT nextval('netcdf_attribute_sequence'::regclass) NOT NULL,
    naming_authority character varying(200),
    site character varying(30),
    mooring character varying(30),
    deployment character varying(40),
    instrument_id integer,
    parameter character varying(40),
    attribute_name character varying(40),
    attribute_type character varying(30),
    attribute_value character varying(2000)
);


ALTER TABLE public.netcdf_attributes OWNER TO postgres;

--
-- Name: netcdf_attribute_sequence; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('netcdf_attribute_sequence', 10096, true);


--
-- Data for Name: netcdf_attributes; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO netcdf_attributes VALUES (10022, NULL, NULL, NULL, NULL, NULL, NULL, 'institution_address', 'STRING', 'CSIRO Marine Laboratories, Castray Esp, Hobart, Tasmania 7001, Australia ');
INSERT INTO netcdf_attributes VALUES (10024, NULL, NULL, NULL, NULL, NULL, NULL, 'netcdf_version', 'STRING', '3.6 ');
INSERT INTO netcdf_attributes VALUES (10018, NULL, 'SOTS', NULL, NULL, NULL, NULL, 'citation', 'STRING', 'Integrated Marine Observing System. 2010, Southern Ocean Time Series (SOTS) data, http://imosmest.aodn.org.au/geonetwork/srv/en/metadata.show?id=11673&currTab=simple');
INSERT INTO netcdf_attributes VALUES (10042, NULL, 'SOTS', NULL, NULL, NULL, NULL, 'site_code', 'STRING', 'SOTS ');
INSERT INTO netcdf_attributes VALUES (10019, 'IMOS', NULL, NULL, NULL, NULL, NULL, 'distribution_statement', 'STRING', 'Data may be re-used, provided that related metadata explaining the data has been reviewed by the user, and the data is appropriately acknowledged. Data, products and services from IMOS are provided as is without any warranty as to fitness for a particular purpose. ');
INSERT INTO netcdf_attributes VALUES (10020, NULL, NULL, NULL, NULL, NULL, NULL, 'institution', 'STRING', 'CSIRO ');
INSERT INTO netcdf_attributes VALUES (10026, 'IMOS', NULL, NULL, NULL, NULL, NULL, 'naming_authority', 'STRING', 'IMOS ');
INSERT INTO netcdf_attributes VALUES (10036, NULL, NULL, 'PULSE', NULL, NULL, NULL, 'principal_investigator_email', 'STRING', 'tom.trull@utas.edu.au ');
INSERT INTO netcdf_attributes VALUES (10031, 'IMOS', NULL, NULL, NULL, NULL, NULL, 'data_centre', 'STRING', 'eMarine Information Infrastructure (eMII) ');
INSERT INTO netcdf_attributes VALUES (10043, NULL, NULL, 'PULSE', NULL, NULL, NULL, 'platform_code', 'STRING', 'PULSE ');
INSERT INTO netcdf_attributes VALUES (10032, 'IMOS', NULL, NULL, NULL, NULL, NULL, 'data_centre_email', 'STRING', 'info@emii.org.au ');
INSERT INTO netcdf_attributes VALUES (10037, NULL, NULL, NULL, NULL, NULL, NULL, 'acknowledgement', 'STRING', 'Any users of IMOS data are required to clearly acknowledge the source of the material in the format:Data was sourced from the Integrated Marine Observing System (IMOS) - an initiative of the Australian Government being conducted as part of the National Collaborative Research Infrastructure Strategy and and the Super Science Initiative.');
INSERT INTO netcdf_attributes VALUES (10047, NULL, NULL, 'PULSE', NULL, NULL, NULL, 'keywords', 'STRING', 'Oceans->Ocean Chemistry->Biogeochemical Cycles, Oceans->Ocean Chemistry->Carbon, Oceans->Ocean Chemistry->Nitrate,Oceans->Ocean Chemistry->Water Temperature, Oceans->Ocean Optics->Turbidity, Oceans->Salinity/Depth->Salinity ');
INSERT INTO netcdf_attributes VALUES (10038, NULL, NULL, NULL, NULL, NULL, NULL, 'file_version', 'STRING', 'Level 1 - Quality Controlled data ');
INSERT INTO netcdf_attributes VALUES (10025, NULL, NULL, NULL, NULL, NULL, NULL, 'quality_control_set', 'STRING', '1. ');
INSERT INTO netcdf_attributes VALUES (10051, 'OS', NULL, NULL, NULL, NULL, NULL, 'data_type', 'STRING', 'OceanSITES time-series data');
INSERT INTO netcdf_attributes VALUES (10052, 'OS', NULL, NULL, NULL, NULL, NULL, 'format_version', 'STRING', '1.2');
INSERT INTO netcdf_attributes VALUES (10053, 'OS', NULL, NULL, NULL, NULL, NULL, 'network', 'STRING', 'IMOS');
INSERT INTO netcdf_attributes VALUES (10035, NULL, NULL, 'PULSE', NULL, NULL, NULL, 'principal_investigator', 'STRING', 'Tom Trull ');
INSERT INTO netcdf_attributes VALUES (10054, 'OS', NULL, NULL, NULL, NULL, NULL, 'area', 'STRING', 'Southern Ocean');
INSERT INTO netcdf_attributes VALUES (10055, 'OS', NULL, NULL, NULL, NULL, NULL, 'source', 'STRING', 'Mooring observation');
INSERT INTO netcdf_attributes VALUES (10056, 'OS', NULL, NULL, NULL, NULL, NULL, 'naming_authority', 'STRING', 'OceanSITES');
INSERT INTO netcdf_attributes VALUES (10058, 'OS', NULL, NULL, NULL, NULL, NULL, 'data_mode', 'STRING', 'D');
INSERT INTO netcdf_attributes VALUES (10059, 'OS', NULL, NULL, NULL, NULL, NULL, 'title', 'STRING', 'Pulse 8 Mooring data');
INSERT INTO netcdf_attributes VALUES (10006, NULL, NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'Mooring', 'STRING', 'Pulse 8 mooring');
INSERT INTO netcdf_attributes VALUES (10013, NULL, NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'time_coverage_start', 'STRING', '2011-08-03T00:00:00Z ');
INSERT INTO netcdf_attributes VALUES (10014, NULL, NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'time_coverage_end', 'STRING', '2012-07-19T00:00:00Z ');
INSERT INTO netcdf_attributes VALUES (10016, NULL, NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'project', 'STRING', 'Integrated Marine Observing System (IMOS) ');
INSERT INTO netcdf_attributes VALUES (10028, NULL, NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'geospatial_lat_units', 'STRING', 'degrees_north ');
INSERT INTO netcdf_attributes VALUES (10029, NULL, NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'geospatial_lon_units', 'STRING', 'degrees_east ');
INSERT INTO netcdf_attributes VALUES (10030, NULL, NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'geospatial_vertical_units', 'STRING', 'metres ');
INSERT INTO netcdf_attributes VALUES (10044, NULL, NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'deployment_code', 'STRING', 'PULSE-8-2011 ');
INSERT INTO netcdf_attributes VALUES (10045, NULL, NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'title', 'STRING', 'Pulse 8 Mooring Data ');
INSERT INTO netcdf_attributes VALUES (10048, NULL, NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'geospatial_vertical_min', 'STRING', '0 ');
INSERT INTO netcdf_attributes VALUES (10009, 'IMOS', NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'geospatial_lat_min', 'STRING', '-46.9295 ');
INSERT INTO netcdf_attributes VALUES (10007, 'IMOS', NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'Latitude', 'STRING', '-46.9295 ');
INSERT INTO netcdf_attributes VALUES (10008, 'IMOS', NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'Longitude', 'STRING', '142.2147 ');
INSERT INTO netcdf_attributes VALUES (10010, 'IMOS', NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'geospatial_lon_min', 'STRING', '142.2147 ');
INSERT INTO netcdf_attributes VALUES (10011, 'IMOS', NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'geospatial_lat_max', 'STRING', '-46.9295 ');
INSERT INTO netcdf_attributes VALUES (10012, 'IMOS', NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'geospatial_lon_max', 'STRING', '142.2147 ');
INSERT INTO netcdf_attributes VALUES (10063, NULL, NULL, NULL, NULL, NULL, 'TEMP', 'reference_scale', 'STRING', 'ITS-90');
INSERT INTO netcdf_attributes VALUES (10060, NULL, NULL, NULL, 'Pulse-7-2010', NULL, NULL, 'Mooring', 'STRING', 'Pulse 7 mooring');
INSERT INTO netcdf_attributes VALUES (10040, 'IMOS', NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'field_trip_id', 'STRING', 'N/A ');
INSERT INTO netcdf_attributes VALUES (10049, NULL, NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'geospatial_vertical_max', 'STRING', '664');
INSERT INTO netcdf_attributes VALUES (10027, 'IMOS', NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'product_type', 'STRING', ' ');
INSERT INTO netcdf_attributes VALUES (10062, NULL, NULL, NULL, NULL, NULL, 'PSAL', 'comment', 'STRING', 'Derived from Pressure, Temperature (IPTS68) and Conductivity using PSS 1978 equations');
INSERT INTO netcdf_attributes VALUES (10064, NULL, NULL, NULL, NULL, NULL, 'PSAL', 'reference_scale', 'STRING', 'IPTS-68');
INSERT INTO netcdf_attributes VALUES (10065, NULL, NULL, NULL, NULL, NULL, 'GTD_PRESSURE', 'standard_name', 'STRING', 'total_dissolved_air_pressure');
INSERT INTO netcdf_attributes VALUES (10066, NULL, NULL, NULL, NULL, NULL, 'NTR_CONC', 'standard_name', 'STRING', 'moles_of_nitrate_per_unit_mass_in_sea_water');
INSERT INTO netcdf_attributes VALUES (10067, NULL, NULL, NULL, NULL, NULL, 'PAR', 'standard_name', 'STRING', 'downwelling_photosynthetic_photon_spherical_irradiance_in_sea_water');
INSERT INTO netcdf_attributes VALUES (10068, NULL, NULL, NULL, NULL, NULL, 'PSAL', 'standard_name', 'STRING', 'sea_water_practical_salinity');
INSERT INTO netcdf_attributes VALUES (10072, NULL, NULL, NULL, NULL, NULL, 'CNDC', 'standard_name', 'STRING', 'sea_water_electrical_conductivity');
INSERT INTO netcdf_attributes VALUES (10074, NULL, NULL, NULL, NULL, NULL, 'CPHL', 'standard_name', 'STRING', 'mass_concentration_of_chlorophyll_in_sea_water');
INSERT INTO netcdf_attributes VALUES (10041, 'IMOS', NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'field_trip_description', 'STRING', 'N/A ');
INSERT INTO netcdf_attributes VALUES (10023, 'IMOS', NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'source', 'STRING', 'Moorings ');
INSERT INTO netcdf_attributes VALUES (10017, 'IMOS', NULL, NULL, NULL, NULL, NULL, 'Conventions', 'STRING', 'CF-1.6;IMOS-1.3 ');
INSERT INTO netcdf_attributes VALUES (10034, 'IMOS', NULL, NULL, NULL, NULL, NULL, 'author', 'STRING', 'Peter Jansen ');
INSERT INTO netcdf_attributes VALUES (10033, 'IMOS', NULL, NULL, NULL, NULL, NULL, 'author_email', 'STRING', 'peter.jansen@csiro.au ');
INSERT INTO netcdf_attributes VALUES (10057, 'OS', NULL, NULL, NULL, NULL, NULL, 'data_assembly_center', 'STRING', 'IMOS');
INSERT INTO netcdf_attributes VALUES (10071, NULL, NULL, NULL, NULL, NULL, 'TEMP', 'standard_name', 'STRING', 'sea_water_temperature');
INSERT INTO netcdf_attributes VALUES (10021, NULL, 'SOTS', NULL, NULL, NULL, NULL, 'cdm_data_type', 'STRING', 'Station');
INSERT INTO netcdf_attributes VALUES (10069, NULL, NULL, NULL, NULL, NULL, 'PRES', 'standard_name', 'STRING', 'sea_water_pressure');
INSERT INTO netcdf_attributes VALUES (10075, NULL, NULL, NULL, NULL, NULL, 'DOX2', 'standard_name', 'STRING', 'moles_of_oxygen_per_unit_mass_in_sea_water');
INSERT INTO netcdf_attributes VALUES (10076, NULL, NULL, NULL, NULL, NULL, 'TURB', 'long_name', 'STRING', 'turbidity');
INSERT INTO netcdf_attributes VALUES (10078, NULL, NULL, NULL, NULL, NULL, 'DISSOLVED_AIR_PRESS', 'long_name', 'STRING', 'total_dissolved_air_pressure');
INSERT INTO netcdf_attributes VALUES (10079, NULL, NULL, NULL, NULL, NULL, 'LATITUDE', 'long_name', 'STRING', 'latitude');
INSERT INTO netcdf_attributes VALUES (10080, NULL, NULL, NULL, NULL, NULL, 'LONGITUDE', 'long_name', 'STRING', 'longitude');
INSERT INTO netcdf_attributes VALUES (10081, NULL, NULL, NULL, NULL, NULL, 'OPTODE_BPHASE', 'long_name', 'STRING', 'optode_bphase_value');
INSERT INTO netcdf_attributes VALUES (10082, NULL, NULL, NULL, NULL, NULL, 'OPTODE_TEMP', 'long_name', 'STRING', 'optode_temperature_value');
INSERT INTO netcdf_attributes VALUES (10077, NULL, NULL, NULL, NULL, NULL, 'DEPTH', 'long_name', 'STRING', 'water_depth_in_metres_from_surface');
INSERT INTO netcdf_attributes VALUES (10089, 'OS', NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'summary', 'STRING', 'The Pulse 8 mooring was deployed from August 2011 to July 2012 at Lat -46.93, Lon 142.26. Moored instruments are deployed by the IMOS Australia Bluewater Observing System (ABOS) Southern Ocean Time Series sub-facility for time-series observations of physical, biological, and chemical properties, in the Sub-Antarctic Zone southwest of Tasmania, with yearly servicing. The Southern Ocean Time Series (SOTS) Sub-Facility is responsible for the deployment of Pulse moorings. These time-series observations are crucial to resolving ecosystem processes that affect carbon cycling, ocean productivity and marine responses to climate variability and change, ocean acidification and other stresses.  ');
INSERT INTO netcdf_attributes VALUES (10046, 'IMOS', NULL, NULL, 'Pulse-8-2011', NULL, NULL, 'abstract', 'STRING', 'The Pulse 8 mooring was deployed from August 2011 to July 2012 at Lat -46.93, Lon 142.26. Moored instruments are deployed by the IMOS Australia Bluewater Observing System (ABOS) Southern Ocean Time Series sub-facility for time-series observations of physical, biological, and chemical properties, in the Sub-Antarctic Zone southwest of Tasmania, with yearly servicing. The Southern Ocean Time Series (SOTS) Sub-Facility is responsible for the deployment of Pulse moorings. These time-series observations are crucial to resolving ecosystem processes that affect carbon cycling, ocean productivity and marine responses to climate variability and change, ocean acidification and other stresses.  ');
INSERT INTO netcdf_attributes VALUES (10090, NULL, NULL, NULL, NULL, NULL, NULL, 'area', 'STRING', 'Southern Ocean');
INSERT INTO netcdf_attributes VALUES (10091, 'OS', NULL, NULL, NULL, NULL, NULL, 'Conventions', 'STRING', 'CF-1.6,OceanSITES 1.3');
INSERT INTO netcdf_attributes VALUES (10092, 'OS', NULL, NULL, NULL, NULL, NULL, 'creator_name', 'STRING', 'Peter Jansen ');
INSERT INTO netcdf_attributes VALUES (10093, 'OS', NULL, NULL, NULL, NULL, NULL, 'creator_email', 'STRING', 'peter.jansen@csiro.au ');
INSERT INTO netcdf_attributes VALUES (10094, 'OS', NULL, NULL, NULL, NULL, NULL, 'update_interval', 'STRING', 'void');
INSERT INTO netcdf_attributes VALUES (10095, 'OS', NULL, NULL, NULL, NULL, NULL, 'license', 'STRING', 'Follows CLIVAR (Climate Varibility and Predictability) standards, cf. http://www.clivar.org/data /data_policy.php. Data available free of charge. User assumes all risk for use of data. User must display citation in any publication or product using data. User must contact PI prior to any commercial use of data.');
INSERT INTO netcdf_attributes VALUES (10096, 'OS', NULL, NULL, NULL, NULL, NULL, 'processing_level', 'STRING', 'No QC performed');
INSERT INTO netcdf_attributes VALUES (10061, NULL, NULL, NULL, 'Pulse-8-2011', NULL, 'DOX2', 'comment', 'STRING', 'Salinity, Water Temperature and Pressure from SBE16 SN6330');


--
-- Name: netcdf_attribute_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY netcdf_attributes
    ADD CONSTRAINT netcdf_attribute_pk PRIMARY KEY (netcdf_attribute_id);


--
-- Name: netcdf_attributes_name_unique; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY netcdf_attributes
    ADD CONSTRAINT netcdf_attributes_name_unique UNIQUE (naming_authority, attribute_name);


--
-- PostgreSQL database dump complete
--

