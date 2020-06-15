/*
create temp table raw_temperature as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp) as obs_time, depth, parameter_value 
from raw_instrument_data
where instrument_id = 4
and mooring_id = 'PULSE_7'
and parameter_code = 'WATER_TEMP'
;


create temp table processed_temperature as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp) as obs_time, depth, parameter_value 
from processed_instrument_data
where instrument_id = 4
and mooring_id = 'PULSE_7'
and parameter_code = 'WATER_TEMP'
;
*/
--create temp table set2 as
select 
	raw_temperature.obs_time,
	raw_temperature.depth,
	raw_temperature.parameter_value as raw_temp,
	processed_temperature.parameter_value as processed_temp,
	raw_temperature.parameter_value - processed_temperature.parameter_value as diff
from raw_temperature full join processed_temperature on 
(
raw_temperature.obs_time = processed_temperature.obs_time
)
;

