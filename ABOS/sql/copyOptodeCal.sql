with upd AS (
	SELECT 
	857, 'SOFS-6-2017'::text AS mooring_id, 2018020 AS datafile_pk, param_code, description, data_type, data_value
	FROM instrument_calibration_values WHERE datafile_pk = 200601
	)
	insert into instrument_calibration_values SELECT * from upd;
