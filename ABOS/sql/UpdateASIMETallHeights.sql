-- update all ASIMET data parameters to the same height
UPDATE raw_instrument_data SET depth=-2.610 WHERE parameter_code IN ('UWND', 'VWND', 'WSPD', 'WSPD_MAX', 'WSPD_MIN', 'WDIR', 'COMPASS', 'PITCH', 'ROLL');
UPDATE raw_instrument_data SET depth=-2.230 WHERE parameter_code IN ('CAPH');
UPDATE raw_instrument_data SET depth=-2.400 WHERE parameter_code IN ('AIRT', 'RELH');
UPDATE raw_instrument_data SET depth=-2.640 WHERE parameter_code IN ('SW', 'TDOME', 'TBODY', 'VPILE', 'LW');
UPDATE raw_instrument_data SET depth=-2.485 WHERE parameter_code IN ('RAIN', 'RAIT');
UPDATE raw_instrument_data SET depth=1.510 WHERE depth < 5 AND parameter_code IN ('TEMP', 'CNDC');

