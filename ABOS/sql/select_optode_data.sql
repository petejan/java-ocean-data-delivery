
begin work;

drop table if exists dphase;
drop table if exists optode_temp;
drop table if exists sbe16_temperature;
drop table if exists sbe16_pressure;
drop table if exists sbe16_conductivity;
drop table if exists set1;
drop table if exists set2;
drop table if exists set3;

create temp table dphase as
select distinct on (date_trunc('hour',data_timestamp)) 
date_trunc('hour',data_timestamp) as obs_time, depth, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and parameter_code = 'OPTODE_DPHASE'
;

create temp table optode_temp as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and parameter_code = 'OPTODE_TEMP'
;

create temp table sbe16_temperature as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value
from raw_instrument_data
where mooring_id = 'PULSE_7'
and instrument_id = 4
and parameter_code = 'WATER_TEMP'
;


create temp table sbe16_pressure as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and instrument_id = 4
and parameter_code = 'WATER_PRESSURE'
;

create temp table sbe16_conductivity as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and instrument_id = 4
and parameter_code = 'CONDUCTIVITY'
;

create temp table set1 as
select 
	dphase.obs_time,
	dphase.depth,
	dphase.parameter_value as optode_dphase,
	optode_temp.parameter_value as optode_temperature
from dphase full join optode_temp on 
(
dphase.obs_time = optode_temp.obs_time
AND
dphase.depth = optode_temp.depth
)
;

create temp table set2 as
select 
	set1.obs_time,
	set1.depth,
	set1.optode_dphase,
	set1.optode_temperature,
	sbe16_temperature.parameter_value as sbe16_temperature
from set1 full join sbe16_temperature on
(
set1.obs_time = sbe16_temperature.obs_time
AND
set1.depth = sbe16_temperature.depth
)
;

create temp table set3 as
select
	set2.obs_time,
	set2.depth,
	set2.optode_dphase,
	set2.optode_temperature,
        set2.sbe16_temperature,
	sbe16_pressure.parameter_value as sbe16_pressure
from set2 full join sbe16_pressure on
(
set2.obs_time = sbe16_pressure.obs_time
AND
set2.depth = sbe16_pressure.depth
)
;

create temp table set4 as
select 
	set3.obs_time,
	set3.depth,
	set3.optode_dphase,
	set3.optode_temperature,
        set3.sbe16_temperature,
	set3.sbe16_pressure,
	sbe16_conductivity.parameter_value as sbe16_conductivity
from set3 full join sbe16_conductivity on
(
set3.obs_time = sbe16_conductivity.obs_time
AND
set3.depth = sbe16_conductivity.depth
)
;

select * from set4 order by 1;