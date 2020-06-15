with upd AS (
	SELECT 
	source_file_id, 857 as instrument_id, mooring_id, data_timestamp, latitude, longitude, depth, parameter_code, parameter_value, quality_code
	FROM raw_instrument_data WHERE mooring_id = 'SOFS-6-2017' and parameter_code IN ('OPTODE_BPHASE', 'OPTODE_TEMP') order by data_timestamp, parameter_code
	)
	insert into raw_instrument_data SELECT * from upd;
