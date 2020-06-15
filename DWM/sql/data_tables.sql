begin work;

CREATE TABLE parameters
(
  parameter_code character(20) NOT NULL,
  description character(60),
  display_code character(10) DEFAULT 'Y'::bpchar,
  CONSTRAINT parameters_pkey PRIMARY KEY (parameter_code)
)
WITH (OIDS=FALSE);

create table raw_instrument_data
(
source_file_id integer not null references instrument_data_files,
instrument_id integer NOT NULL references instrument,
mooring_id character(20) NOT NULL references mooring,
data_timestamp timestamp not null,
latitude decimal(16,4) not null,
longitude decimal(16,4) not null,
depth decimal(16,4) not null default 0,
parameter_code character(20) not null references parameters,
parameter_value decimal(16,4) not null,
quality_code character(20) not null default 'N/A',
primary key(instrument_id, data_timestamp, parameter_code)
);

create table corrected_instrument_data
(
source_file_id integer not null references instrument_data_files,
calibration_file_id integer not null references instrument_calibration_files,
instrument_id integer NOT NULL references instrument,
mooring_id character(20) NOT NULL references mooring,
data_timestamp timestamp not null,
latitude decimal(16,4) not null,
longitude decimal(16,4) not null,
depth decimal(16,4) not null default 0,
parameter_code character(20) not null references parameters,
parameter_value decimal(16,4) not null,
quality_code character(20) not null default 'N/A',
primary key(instrument_id, data_timestamp, parameter_code)
);

commit work;