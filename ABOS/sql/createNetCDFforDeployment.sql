--INSERT INTO netcdf_attributes (naming_authority, site, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)   
--  SELECT naming_authority, site, 'SAZ'::text, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value FROM netcdf_attributes WHERE mooring = 'Pulse';

INSERT INTO netcdf_attributes (naming_authority, site, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)   
  SELECT naming_authority, site, mooring, 'Pulse-10-2013'::text, instrument_id, parameter, attribute_name, attribute_type, attribute_value FROM netcdf_attributes WHERE deployment = 'Pulse-9-2012';


DELETE FROM netcdf_attributes WHERE deployment = 'Pulse-10-2013' AND attribute_name = 'Latitude';
INSERT INTO netcdf_attributes (naming_authority, site, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)   
  SELECT 'IMOS'::text AS naming_authority, '*'::text AS site, '*'::text AS mooring, mooring_id AS deployment, null::integer AS instrument_id, '*'::text AS parameter, 'Latitude'::text AS attribute_name, 'NUMBER'::text AS attribute_type, latitude_in AS attribute_value FROM mooring WHERE mooring_id = 'Pulse-10-2013';
DELETE FROM netcdf_attributes WHERE deployment = 'Pulse-10-2013' AND attribute_name = 'Longitude';
INSERT INTO netcdf_attributes (naming_authority, site, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)   
  SELECT 'IMOS'::text AS naming_authority, '*'::text AS site, '*'::text AS mooring, mooring_id AS deployment, null::integer AS instrument_id, '*'::text AS parameter, 'Longitude'::text AS attribute_name, 'NUMBER'::text AS attribute_type, longitude_in AS attribute_value FROM mooring WHERE mooring_id = 'Pulse-10-2013';

DELETE FROM netcdf_attributes WHERE deployment = 'Pulse-10-2013' AND attribute_name = 'geospatial_lat_max';
DELETE FROM netcdf_attributes WHERE deployment = 'Pulse-10-2013' AND attribute_name = 'geospatial_lat_min';
DELETE FROM netcdf_attributes WHERE deployment = 'Pulse-10-2013' AND attribute_name = 'geospatial_lon_max';
DELETE FROM netcdf_attributes WHERE deployment = 'Pulse-10-2013' AND attribute_name = 'geospatial_lon_min';
INSERT INTO netcdf_attributes (naming_authority, site, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)   
  SELECT 'IMOS'::text AS naming_authority, '*'::text AS site, '*'::text AS mooring, mooring_id AS deployment, null::integer AS instrument_id, '*'::text AS parameter, 'geospatial_lat_max'::text AS attribute_name, 'NUMBER'::text AS attribute_type, latitude_in AS attribute_value FROM mooring WHERE mooring_id = 'Pulse-10-2013';
INSERT INTO netcdf_attributes (naming_authority, site, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)   
  SELECT 'OS'::text AS naming_authority, '*'::text AS site, '*'::text AS mooring, mooring_id AS deployment, null::integer AS instrument_id, '*'::text AS parameter, 'geospatial_lat_max'::text AS attribute_name, 'STRING'::text AS attribute_type, latitude_in AS attribute_value FROM mooring WHERE mooring_id = 'Pulse-10-2013';
INSERT INTO netcdf_attributes (naming_authority, site, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)   
  SELECT 'IMOS'::text AS naming_authority, '*'::text AS site, '*'::text AS mooring, mooring_id AS deployment, null::integer AS instrument_id, '*'::text AS parameter, 'geospatial_lat_min'::text AS attribute_name, 'NUMBER'::text AS attribute_type, latitude_in AS attribute_value FROM mooring WHERE mooring_id = 'Pulse-10-2013';
INSERT INTO netcdf_attributes (naming_authority, site, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)   
  SELECT 'OS'::text AS naming_authority, '*'::text AS site, '*'::text AS mooring, mooring_id AS deployment, null::integer AS instrument_id, '*'::text AS parameter, 'geospatial_lat_min'::text AS attribute_name, 'STRING'::text AS attribute_type, latitude_in AS attribute_value FROM mooring WHERE mooring_id = 'Pulse-10-2013';
INSERT INTO netcdf_attributes (naming_authority, site, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)   
  SELECT 'IMOS'::text AS naming_authority, '*'::text AS site, '*'::text AS mooring, mooring_id AS deployment, null::integer AS instrument_id, '*'::text AS parameter, 'geospatial_lon_max'::text AS attribute_name, 'NUMBER'::text AS attribute_type, longitude_in AS attribute_value FROM mooring WHERE mooring_id = 'Pulse-10-2013';
INSERT INTO netcdf_attributes (naming_authority, site, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)   
  SELECT 'OS'::text AS naming_authority, '*'::text AS site, '*'::text AS mooring, mooring_id AS deployment, null::integer AS instrument_id, '*'::text AS parameter, 'geospatial_lon_max'::text AS attribute_name, 'STRING'::text AS attribute_type, longitude_in AS attribute_value FROM mooring WHERE mooring_id = 'Pulse-10-2013';
INSERT INTO netcdf_attributes (naming_authority, site, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)   
  SELECT 'IMOS'::text AS naming_authority, '*'::text AS site, '*'::text AS mooring, mooring_id AS deployment, null::integer AS instrument_id, '*'::text AS parameter, 'geospatial_lon_min'::text AS attribute_name, 'NUMBER'::text AS attribute_type, longitude_in AS attribute_value FROM mooring WHERE mooring_id = 'Pulse-10-2013';
INSERT INTO netcdf_attributes (naming_authority, site, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)   
  SELECT 'OS'::text AS naming_authority, '*'::text AS site, '*'::text AS mooring, mooring_id AS deployment, null::integer AS instrument_id, '*'::text AS parameter, 'geospatial_lon_min'::text AS attribute_name, 'STRING'::text AS attribute_type, longitude_in AS attribute_value FROM mooring WHERE mooring_id = 'Pulse-10-2013';

DELETE FROM netcdf_attributes WHERE deployment = 'Pulse-10-2013' AND attribute_name = 'time_coverage_start';
DELETE FROM netcdf_attributes WHERE deployment = 'Pulse-10-2013' AND attribute_name = 'time_coverage_end';
INSERT INTO netcdf_attributes (naming_authority, site, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)   
  SELECT '*'::text AS naming_authority, '*'::text AS site, '*'::text AS mooring, mooring_id AS deployment, null::integer AS instrument_id, '*'::text AS parameter, 'time_coverage_start'::text AS attribute_name, 'STRING'::text AS attribute_type, to_char(timestamp_in, 'yyyy-MM-DD"T"HH24:MI:SSZ') AS attribute_value FROM mooring WHERE mooring_id = 'Pulse-10-2013';
INSERT INTO netcdf_attributes (naming_authority, site, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)   
  SELECT '*'::text AS naming_authority, '*'::text AS site, '*'::text AS mooring, mooring_id AS deployment, null::integer AS instrument_id, '*'::text AS parameter, 'time_coverage_end'::text AS attribute_name, 'STRING'::text AS attribute_type, to_char(timestamp_out, 'yyyy-MM-DD"T"HH24:MI:SSZ') AS attribute_value FROM mooring WHERE mooring_id = 'Pulse-10-2013';
