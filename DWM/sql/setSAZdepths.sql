
update processed_instrument_data set depth=1001 where instrument_id = 2735 and mooring_id = 'SAZ47-15-2012';
update processed_instrument_data set depth=1988 where instrument_id = 2736 and mooring_id = 'SAZ47-15-2012';
update processed_instrument_data set depth=3921 where instrument_id = 2737 and mooring_id = 'SAZ47-15-2012';

update processed_instrument_data set depth=1046.3 where instrument_id = 2953 and mooring_id = 'SAZ47-16-2013';
update processed_instrument_data set depth=1095.7 where instrument_id = 3027 and mooring_id = 'SAZ47-16-2013';
update processed_instrument_data set depth=2029.0 where instrument_id = 2954 and mooring_id = 'SAZ47-16-2013';
update processed_instrument_data set depth=3921.9 where instrument_id = 2969 and mooring_id = 'SAZ47-16-2013';

-- 
-- update processed_instrument_data set quality_code = 'GOOD' where mooring_id = 'SAZ47-16-2013';
-- 
-- update processed_instrument_data set quality_code = 'PGOOD' 
-- 	where mooring_id = 'SAZ47-16-2013' and 
-- 	data_timestamp in 
-- 		(SELECT data_timestamp FROM processed_instrument_data 
-- 			WHERE mooring_id = 'SAZ47-16-2013' and parameter_code = 'SAMPLE' and parameter_value in (5,6) and instrument_id = 2953) and instrument_id = 2953;
-- 
-- update processed_instrument_data set quality_code = 'PGOOD' 
-- 	where mooring_id = 'SAZ47-16-2013' and 
-- 	data_timestamp in 
-- 		(SELECT data_timestamp FROM processed_instrument_data 
-- 			WHERE mooring_id = 'SAZ47-16-2013' and parameter_code = 'SAMPLE' and parameter_value in (1,2,4,5,6,7,8,9,10,11) and instrument_id = 3027) and instrument_id = 3027;			