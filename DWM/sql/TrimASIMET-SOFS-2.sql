
UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'SOFS-2-2011' AND (data_timestamp < '2011-11-25' OR data_timestamp > '2012-07-18');

UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < -40 OR parameter_value > 60) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'AIRT';
UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value <= 0 OR parameter_value >= 100) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'RELH';

UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < 500 OR parameter_value > 1500) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'CAPH';

UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value <= 0 OR parameter_value > 20) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'CNDC';
UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < -5 OR parameter_value > 40) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'TEMP';

UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < 0 OR parameter_value > 360) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'COMPASS';

UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < 0 OR parameter_value > 51) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'PITCH';
UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < 0 OR parameter_value > 51) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'ROLL';

UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < 0 OR parameter_value > 55) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'RAIT';

UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < 0 OR parameter_value > 6553.5) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'LW';

UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < 0 OR parameter_value > 3000) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'SW';
UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value <= 0 OR parameter_value >= 655.35) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'TBODY';
UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value <= 0 OR parameter_value >= 655.35) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'TDOME';
UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < -500 OR parameter_value > 100) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'VPILE';

UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < -327.76 OR parameter_value > 327.67) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'UWND';
UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < -327.76 OR parameter_value > 327.67) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'VWND';
UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < 0 OR parameter_value > 655.35) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'WDIR';
UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < 0 OR parameter_value >= 51.0) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'WSPD';
UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < 0 OR parameter_value >= 51.0) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'WSPD_MAX';
UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < 0 OR parameter_value >= 51.0) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'WSPD_MIN';

UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value < 141.5) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'XPOS';
UPDATE raw_instrument_data SET quality_code = 'BAD' WHERE (parameter_value > -46.5) AND mooring_id = 'SOFS-2-2011' AND parameter_code = 'YPOS';
