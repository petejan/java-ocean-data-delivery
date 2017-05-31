-- SQL to update the ASIMET instrument heights

UPDATE raw_instrument_data SET depth=-2.610 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1365; -- SWND214
UPDATE raw_instrument_data SET depth=-2.615 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1362; -- SWND215

UPDATE raw_instrument_data SET depth=-2.230 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1360; -- BPR226
UPDATE raw_instrument_data SET depth=-2.230 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1369; -- BPR227
UPDATE raw_instrument_data SET depth=-2.350 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1366; -- HRH242
UPDATE raw_instrument_data SET depth=-2.400 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1361; -- HRH242
---UPDATE raw_instrument_data SET depth=-2.400 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 2235; -- HRH255

UPDATE raw_instrument_data SET depth=-2.640 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1371; -- LWR227
UPDATE raw_instrument_data SET depth=-2.640 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1356; -- LWR228
UPDATE raw_instrument_data SET depth=-2.640 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1357; -- SWR235
UPDATE raw_instrument_data SET depth=-2.640 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1370; -- SWR236

UPDATE raw_instrument_data SET depth=-2.485 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id IN (1359, 1358); -- PRC221
UPDATE raw_instrument_data SET depth=-2.485 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id IN (1368, 1367); -- PRC222

UPDATE raw_instrument_data SET depth=1.510 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1560; -- SBE37-1840
UPDATE raw_instrument_data SET depth=1.510 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1559; -- SBE37-7409

---UPDATE raw_instrument_data SET depth=-2.17, parameter_code = 'AIRT' WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 2236; -- SBE39-5282

-- L22
UPDATE raw_instrument_data SET depth=-2.610 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1382 AND parameter_code IN ('UWND', 'VWND', 'WSPD', 'WSPD_MAX', 'WSPD_MIN', 'WDIR', 'COMPASS', 'PITCH', 'ROLL');
UPDATE raw_instrument_data SET depth=-2.230 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1382 AND parameter_code IN ('CAPH');
UPDATE raw_instrument_data SET depth=-2.400 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1382 AND parameter_code IN ('AIRT', 'RELH');
UPDATE raw_instrument_data SET depth=-2.640 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1382 AND parameter_code IN ('SW', 'TDOME', 'TBODY', 'VPILE', 'LW');
UPDATE raw_instrument_data SET depth=-2.485 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1382 AND parameter_code IN ('RAIN', 'RAIT');
UPDATE raw_instrument_data SET depth=1.510 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1382 AND parameter_code IN ('TEMP', 'CNDC');

-- L23
UPDATE raw_instrument_data SET depth=-2.615 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1381 AND parameter_code IN ('UWND', 'VWND', 'WSPD', 'WSPD_MAX', 'WSPD_MIN', 'WDIR', 'COMPASS', 'PITCH', 'ROLL');
UPDATE raw_instrument_data SET depth=-2.230 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1381 AND parameter_code IN ('CAPH');
UPDATE raw_instrument_data SET depth=-2.400 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1381 AND parameter_code IN ('AIRT', 'RELH');
UPDATE raw_instrument_data SET depth=-2.640 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1381 AND parameter_code IN ('SW', 'TDOME', 'TBODY', 'VPILE', 'LW');
UPDATE raw_instrument_data SET depth=-2.485 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1381 AND parameter_code IN ('RAIN', 'RAIT');
UPDATE raw_instrument_data SET depth=1.510 WHERE mooring_id = 'SOFS-2-2011' AND instrument_id = 1381 AND parameter_code IN ('TEMP', 'CNDC');

