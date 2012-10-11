drop table if exists foobar;
drop table if exists foobar2;

create temp table foobar as
select distinct date_trunc('hour',data_timestamp) as Obs_Time, latitude, longitude,  parameter_value as Water_Temp
from raw_instrument_data
where instrument_id = 4
and (parameter_code = 'WATER_TEMP')
;

create temp table foobar2 as
select distinct date_trunc('hour',data_timestamp) as Obs_Time, latitude, longitude, parameter_value as Water_Pressure
from raw_instrument_data
where instrument_id = 4
and (parameter_code = 'WATER_PRESSURE')
;

--select * from foobar2;
--/*
select  
foobar.Obs_Time,
foobar.latitude,
foobar.longitude,
foobar.Water_Temp,
foobar2.Water_Pressure
from foobar, foobar2
where foobar.Obs_Time = foobar2.Obs_Time
order by 1
;
--*/