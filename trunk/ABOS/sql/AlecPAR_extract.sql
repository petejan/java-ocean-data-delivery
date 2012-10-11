/*
select distinct instrument_id from raw_instrument_data
where parameter_code = 'PAR'
*/
/*

drop table if exists junk;

create temp table junk as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, instrument_id, depth, parameter_value 
from raw_instrument_data
where parameter_code = 'PAR'
and instrument_id = 624
UNION
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time,instrument_id, depth, parameter_value 
from raw_instrument_data
where parameter_code = 'PAR'
and instrument_id = 626
UNION
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time,instrument_id, depth, parameter_value 
from raw_instrument_data
where parameter_code = 'PAR'
and instrument_id = 627
;
*/
select junk.obs_time, instrument.serial_number, junk.instrument_id, junk.depth, junk.parameter_value
 from junk, instrument
 where junk.instrument_id = instrument.instrument_id
order by obs_time, depth
limit 200
;