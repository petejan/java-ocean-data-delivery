drop view abos_ts_timeseries_map;
drop view abos_ts_timeseries_data;
drop table measurement;
drop table timeseries;
drop table indexed_file;

--CREATE SEQUENCE indexed_file_id START 10001;
--CREATE SEQUENCE timeseries_id START 100001;
--CREATE SEQUENCE measurement_id START 1000001;

CREATE TABLE indexed_file
	(
	  id bigint NOT NULL,
	  file_name varchar(4096) NOT NULL,
	  url varchar(4096),
	  size bigint NOT NULL,
	  first_indexed  timestamp with time zone,
	  CONSTRAINT indexed_file_pk PRIMARY KEY (id)
	);

	CREATE TABLE timeseries
	(
	  id bigserial NOT NULL,
	  file_id bigint NOT NULL,
	  site_code text NOT NULL,
	  platform_code text NOT NULL,
	  deployment_code text NOT NULL,
	  "LATITUDE" double precision,
	  "LATITUDE_quality_control" character(1),
	  "LONGITUDE" double precision,
	  "LONGITUDE_quality_control" character(1),
	  geom geometry(Geometry,4326),
	  instrument_nominal_depth real,
	  site_depth_at_deployment real,
	  instrument text,
	  instrument_serial_number text,
	  time_coverage_start timestamp with time zone,
	  time_coverage_end timestamp with time zone,
	  time_deployment_start timestamp with time zone,
	  time_deployment_end timestamp with time zone,
	  comment text,
	  history text,
	  depth_b boolean,
	  sea_water_temperature_b boolean,
	  sea_water_electrical_conductivity_b boolean,
	  sea_water_salinity_b boolean,
	  sea_water_pressure_b boolean,
	  sea_water_pressure_due_to_sea_water_b boolean,
	  CONSTRAINT timeseries_pk PRIMARY KEY (id ),
	  CONSTRAINT timeseries_file_fk FOREIGN KEY (file_id)
	      REFERENCES indexed_file (id) MATCH SIMPLE
	      ON UPDATE CASCADE ON DELETE CASCADE,
	  CONSTRAINT timeseries_deployment_instrument_depth_uc UNIQUE (deployment_code, instrument, instrument_nominal_depth),
	  CONSTRAINT timeseries_deployment_serial_number_uc UNIQUE (deployment_code, instrument_serial_number),
	  CONSTRAINT timeseries_geom_check CHECK (st_isvalid(geom))
	);
	
	ALTER TABLE timeseries
	  ALTER COLUMN geom TYPE geometry(GEOMETRY, 4326)
	  USING ST_SetSRID(geom,4326);
	CREATE INDEX timeseries_gist_idx ON timeseries USING GIST (geom); 

	CREATE TABLE measurement
	(
	  ts_id bigint NOT NULL,
	  index bigint NOT NULL,
	  "TIME" timestamp with time zone NOT NULL,
	  "TIME_quality_control" character(1),
	  "DEPTH" real,
	  "DEPTH_quality_control" character(1),
	  "TEMP" real,
	  "TEMP_quality_control" character(1),
	  "CNDC" real,
	  "CNDC_quality_control" character(1),
	  "PSAL" real,
	  "PSAL_quality_control" character(1),
	  "PRES" real,
	  "PRES_quality_control" character(1),
	  "PRES_REL" real,
	  "PRES_REL_quality_control" character(1),
	  CONSTRAINT measurement_fk PRIMARY KEY (ts_id , index ),
	  CONSTRAINT measurement_ts_fk FOREIGN KEY (ts_id)
	      REFERENCES timeseries (id) MATCH SIMPLE
	      ON UPDATE CASCADE ON DELETE CASCADE
	  );	
	 CREATE INDEX measurement_TIME_idx ON measurement ("TIME"); 

CREATE OR REPLACE VIEW abos_ts_timeseries_map AS 
	  SELECT 
	    t.id AS timeseries_id,
	    f.url AS file_url,
	    f.size,
	    -- date(f.first_indexed AT TIME ZONE 'UTC') AS date_published,
	    t.site_code,
	    t.platform_code,
	    t.deployment_code,
	    t.geom,
	    t."LATITUDE",
	    t."LONGITUDE",
	    t.instrument_nominal_depth,
	    t.site_depth_at_deployment,
	    t.time_coverage_start AT TIME ZONE 'UTC' AS time_coverage_start,
	    t.time_coverage_end AT TIME ZONE 'UTC' AS time_coverage_end,
	    t.time_deployment_start AT TIME ZONE 'UTC' AS time_deployment_start,
	    t.time_deployment_end AT TIME ZONE 'UTC' AS time_deployment_end,
	    t.instrument,
	    t.instrument_serial_number,
	    t.comment,
	    t.history,
	    t.depth_b,
	    t.sea_water_temperature_b,
	    t.sea_water_electrical_conductivity_b,
	    t.sea_water_salinity_b,
	    t.sea_water_pressure_b,
	    t.sea_water_pressure_due_to_sea_water_b
	    FROM timeseries t JOIN indexed_file f ON t.file_id = f.id;	 

 	CREATE OR REPLACE VIEW abos_ts_timeseries_data AS 
	  SELECT 
	    m.ts_id AS timeseries_id,
	    m.index,
	    t.site_code,
	    t.platform_code,
	    t.deployment_code,
	    t.instrument_nominal_depth,
	    m."TIME" AT TIME ZONE 'UTC' AS "TIME",
	    m."TIME_quality_control",
	    t."LATITUDE",
	    t."LATITUDE_quality_control",
	    t."LONGITUDE",
	    t."LONGITUDE_quality_control",
	    m."DEPTH",
	    m."DEPTH_quality_control",
	    m."TEMP",
	    m."TEMP_quality_control",
	    m."CNDC",
	    m."CNDC_quality_control",
	    m."PSAL",
	    m."PSAL_quality_control",
	    m."PRES",
	    m."PRES_quality_control",
	    m."PRES_REL",
	    m."PRES_REL_quality_control",
	    t.geom,
	    t.depth_b,
	    t.sea_water_temperature_b,
	    t.sea_water_electrical_conductivity_b,
	    t.sea_water_salinity_b,
	    t.sea_water_pressure_b,
	    t.sea_water_pressure_due_to_sea_water_b
	    FROM  timeseries t  JOIN  measurement m  ON  t.id = m.ts_id ORDER BY m."TIME";
	    
ALTER TABLE indexed_file OWNER TO abos_ts;	    
ALTER TABLE timeseries OWNER TO abos_ts;	    
ALTER TABLE measurement OWNER TO abos_ts;	    
