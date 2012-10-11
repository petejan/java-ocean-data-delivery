begin work;

drop table instrument_calibration_files;
drop table instrument_data_files;
drop table instrument_notes;
drop table instrument;

drop table mooring_notes;
drop table mooring;

create table mooring
(
mooring_id character(20) not null primary key,
short_description character(80) not null,
timestamp_in timestamp,
timestamp_out timestamp,
latitude_in decimal(16,4),
longitude_in decimal(16,4),
latitude_out decimal(16,4),
longitude_out decimal(16,4)
);

create table mooring_notes
(
mooring_id character(20) not null references mooring,
recording_timestamp timestamp not null,
notes_data varchar(4096) not null,
primary key(mooring_id, recording_timestamp)
)
;

CREATE TABLE instrument
(
  instrument_id integer NOT NULL,
  make character(50) NOT NULL,
  model character(50) NOT NULL,
  serial_number character(50),
  asset_code character(50),
  date_acquired timestamp without time zone NOT NULL default '2000-01-01 00:00:00',
  date_disposed timestamp without time zone,
  instrument_type char(20) not null default 'SIMPLE',
  instrument_status char(20) not null default 'AVAILABLE',
  CONSTRAINT instrument_pkey PRIMARY KEY (instrument_id)
)
WITH (OIDS=FALSE);

create table instrument_notes
(
instrument_id integer references instrument,
recording_timestamp timestamp not null,
notes_data varchar(4096) not null,
primary key(instrument_id, recording_timestamp)
)
;
create table instrument_data_files
(
instrument_id integer NOT NULL references instrument,
mooring_id character(20) not null references mooring,
file_path varchar(255) not null,
file_name varchar(255) not null,
file_data bytea not null,
primary key(instrument_id, mooring_id, file_path, file_name)
);

create table instrument_calibration_files
(
instrument_id integer NOT NULL references instrument,
file_path varchar(255) not null,
file_name varchar(255) not null,
validity_start timestamp not null,
validity_end timestamp not null default '2099-12-31 23:59:59',
file_data bytea not null,
primary key(instrument_id, file_path, file_name, validity_start)
);

commit work;