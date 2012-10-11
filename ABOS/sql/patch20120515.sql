
begin work;

alter table instrument_calibration_values
drop constraint instrument_calibration_values_pkey
;

alter table instrument_calibration_values
add constraint instrument_calibration_values_pkey PRIMARY KEY (mooring_id, datafile_pk, param_code);

commit work;
