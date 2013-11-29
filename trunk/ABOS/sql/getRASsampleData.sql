SELECT mooring_id, make, model, serial_number, data_timestamp at time zone 'utc', description, parameter_code, parameter_value, units
	from raw_instrument_data join instrument using (instrument_id) join parameters on (parameter_code = code)
	where mooring_id = 'Pulse-7-2010' and depth = 31.1 and quality_code != 'INTERPOLATED' and
	date_trunc('hour', data_timestamp) in (SELECT data_timestamp FROM raw_instrument_data WHERE mooring_id = 'Pulse-7-2010' AND parameter_code = 'WATER_SAMPLE') 
  order by data_timestamp, parameter_code;