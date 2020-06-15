/*
create temp table tab1a as
select distinct on (date_trunc('hour',data_timestamp)) 
date_trunc('hour',data_timestamp) as obs_time, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and depth = 30
and parameter_code = 'WATER_PRESSURE'
;

create temp table tab2a as
select distinct on (date_trunc('hour',data_timestamp)) 
date_trunc('hour',data_timestamp) as obs_time, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and depth = 30
and parameter_code = 'GTD_PRESSURE'
;
*/
--create temp table set1 as
select 
	tab1a.obs_time,
	tab1a.parameter_value as SBE_PRESSURE,
	tab2a.parameter_value as GTD_PRESSURE,
	tab1a.parameter_value - tab2a.parameter_value as DIFF
from tab1a full join tab2a on 
(
tab1a.obs_time = tab2a.obs_time
)
;
