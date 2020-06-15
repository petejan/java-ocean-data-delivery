-- TRUNCATE TABLE instrument CASCADE;
INSERT INTO instrument
(
	SELECT 	cmdidid AS instrument_id,
		LEFT(cmdidbrand,50) AS brand,
		LEFT(cmdidmodel,50) AS model,
		LEFT(cmdidserialnumber,50) AS serial_number,
		cmdidassetid AS asset_code
	FROM cmditemdetail
	WHERE cmdidid NOT IN (SELECT instrument_id FROM instrument)
)