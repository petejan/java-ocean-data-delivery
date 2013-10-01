select mooring_id, make, model, serial_number, data_timestamp AT time zone 'UTC', parameter_code, parameter_value, units
	FROM processed_instrument_data 
		JOIN instrument USING (instrument_id) 
		JOIN parameters ON (parameter_code = code) 
	WHERE data_timestamp in (select data_timestamp from raw_instrument_data where parameter_code = 'RAS_SAMPLE') 
		AND depth = 30 order by data_timestamp, parameter_code, instrument_id;