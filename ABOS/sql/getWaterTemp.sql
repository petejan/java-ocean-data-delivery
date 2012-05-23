SELECT p || '-' || TRIM(to_char(d, '9999')) FROM
(	SELECT DISTINCT parameter_code AS p, depth AS d
		FROM raw_instrument_data 
		WHERE mooring_id = 'PULSE_7' AND (parameter_code = 'WATER_TEMP') 
		ORDER BY 2, 1
) As t;
		
SELECT * from CROSSTAB('SELECT DATE_TRUNC(''hour'', data_timestamp) AT time zone ''UTC'', parameter_code || ''-'' || TRIM(to_char(depth, ''9999'')), AVG(parameter_value) 
					           FROM raw_instrument_data 
							   WHERE mooring_id = ''PULSE_7'' AND (parameter_code = ''WATER_TEMP'')
							   GROUP BY DATE_TRUNC(''hour'', data_timestamp), parameter_code || ''-'' || TRIM(to_char(depth, ''9999'')) 
						       ORDER BY 1, 2',
					   'SELECT p || ''-'' || TRIM(to_char(d, ''9999'')) FROM
(
	SELECT DISTINCT parameter_code AS p, depth AS d
		FROM raw_instrument_data 
		WHERE mooring_id = ''PULSE_7'' AND (parameter_code = ''WATER_TEMP'') 
		ORDER BY 2, 1
) AS t') 
	    AS ct( t timestamp, 
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
			   
