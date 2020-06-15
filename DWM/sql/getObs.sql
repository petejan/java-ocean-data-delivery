SELECT make, model, serial_number, p || '-' || TRIM(to_char(d, '9999')) FROM
(	SELECT DISTINCT parameter_code AS p, depth AS d, instrument_id AS i
		FROM raw_instrument_data 
		WHERE mooring_id = 'PULSE_7'
		ORDER BY 2, 1
) As t JOIN instrument ON (instrument_id = i);
	
