\a
\t on
\f ','
SELECT p || '-' || TRIM(to_char(d, '9999')) || '-' || make || '-' || model || '-' || serial_number FROM
(	SELECT DISTINCT parameter_code AS p, depth AS d, instrument_id, make, model, serial_number
		FROM raw_instrument_data JOIN instrument USING (instrument_id)
		WHERE mooring_id = 'PULSE_7' AND (parameter_code = 'WATER_TEMP') 
		ORDER BY 2, 1, 3 
) AS t;
		
SELECT * from CROSSTAB('SELECT DATE_TRUNC(''hour'', data_timestamp) AT time zone ''utc'', parameter_code || ''-'' || TRIM(to_char(depth, ''9999'')), AVG(parameter_value) 
					           FROM raw_instrument_data 
							   WHERE mooring_id = ''PULSE_7'' AND (parameter_code = ''WATER_TEMP'')
							   GROUP BY DATE_TRUNC(''hour'', data_timestamp), parameter_code || ''-'' || TRIM(to_char(depth, ''9999'')) 
						       ORDER BY 1, 2',
					   'SELECT p || ''-'' || TRIM(to_char(d, ''9999'')) || ''-'' || TRIM(to_char(i, ''999999'')) FROM
						(
							SELECT DISTINCT parameter_code AS p, depth AS d, instrument_id AS i
								FROM raw_instrument_data 
								WHERE mooring_id = ''PULSE_7'' AND (parameter_code = ''WATER_TEMP'') 
								ORDER BY 2, 1, 3
						) AS t') 
	    AS ct( t timestamp without time zone, 
			   t1 numeric, 
			   t2 numeric, 
			   t3 numeric, 
			   t4 numeric, 
			   t5 numeric, 
			   t6 numeric, 
			   t7 numeric, 
			   t8 numeric, 
			   t9 numeric, 
			   t10 numeric, 
			   t11 numeric, 
			   t12 numeric, 
			   t13 numeric);
			   