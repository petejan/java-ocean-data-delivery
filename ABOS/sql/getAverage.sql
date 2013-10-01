SELECT 	 mooring_id, 
	 instrument_id, 
	 min(model) AS model, min(serial_number) AS sn, 
	 min(depth) AS depth, 
	 avg(parameter_value), count(*), 
	 min(data_timestamp AT TIME ZONE 'utc'), 
	 max(data_timestamp AT TIME ZONE 'utc') 
	FROM processed_instrument_data JOIN instrument USING (instrument_id) 
	WHERE parameter_code = 'TEMP' AND quality_code != 'BAD' 
	GROUP BY mooring_id, instrument_id 
	ORDER BY mooring_id, depth;