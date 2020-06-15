SELECT mooring_id, stats.instrument_id, parameter_code, to_char(avg, '999990.00') AS avg, stddev, max, min, count, units, make || '-' || model || '-' || serial_number AS instrument, 
	depth, attribute_value AS uncertenty, time_range.first, time_range.last, to_char(age(time_range.last, time_range.first), 'DDD "days" HH24 "hours"') AS duration FROM 
  (
    select mooring_id, instrument_id, parameter_code,  
	avg(parameter_value)::float, 
	stddev(parameter_value)::float, 
	max(parameter_value)::float, 
	min(parameter_value)::float, 
	avg(depth)::float AS depth,
	count(*), 
	min(data_timestamp) AS first, max(data_timestamp) AS last
	from raw_instrument_data JOIN mooring USING (mooring_id)
                    where mooring_id in ('Pulse-9-2012', 'SAZ47-15-2012', 'SOFS-3-2012') and quality_code != 'BAD'
			AND data_timestamp between timestamp_in and timestamp_out
			group by mooring_id, instrument_id, depth, parameter_code
			order by mooring_id, depth, instrument_id, parameter_code
  ) AS stats 
  JOIN
  (
    select mooring_id, instrument_id, parameter_code,  
	(min(data_timestamp) at time zone 'utc')::timestamp without time zone AS first, (max(data_timestamp) at time zone 'utc')::timestamp without time zone AS last
	from raw_instrument_data 
                    where mooring_id in ('Pulse-9-2012', 'SAZ47-15-2012', 'SOFS-3-2012')
			group by mooring_id, instrument_id, depth, parameter_code
  ) AS time_range USING (mooring_id, instrument_id, parameter_code) 
  JOIN instrument USING (instrument_id) 
	     left join netcdf_attributes on (stats.instrument_id = netcdf_attributes.instrument_id AND attribute_name = 'uncertainty' AND parameter_code = parameter)
	     JOIN parameters ON (parameter_code = code)
	ORDER BY mooring_id, depth, parameter_code
                    
