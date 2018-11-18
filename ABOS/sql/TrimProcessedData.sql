---UPDATE PROCESSED_INSTRUMENT_DATA SET QUALITY_CODE = 'BAD' WHERE QUALITY_CODE != 'BAD' AND MOORING_ID = 'PULSE-6-2009' AND (DATA_TIMESTAMP < '2009-09-30' OR DATA_TIMESTAMP > '2010-03-18');
---UPDATE PROCESSED_INSTRUMENT_DATA SET QUALITY_CODE = 'BAD' WHERE QUALITY_CODE != 'BAD' AND MOORING_ID = 'PULSE-6-2009' AND (DATA_TIMESTAMP = '2010-03-08 03:00');
---UPDATE PROCESSED_INSTRUMENT_DATA SET QUALITY_CODE = 'BAD' WHERE QUALITY_CODE != 'BAD' AND PARAMETER_CODE = 'DOX2' AND PARAMETER_VALUE < 240;
---UPDATE PROCESSED_INSTRUMENT_DATA SET QUALITY_CODE = 'BAD' WHERE QUALITY_CODE != 'BAD' AND MOORING_ID = 'PULSE-7-2010' AND (DATA_TIMESTAMP < '2010-09-12' OR DATA_TIMESTAMP > '2011-04-17');

---UPDATE PROCESSED_INSTRUMENT_DATA SET QUALITY_CODE = 'BAD' WHERE QUALITY_CODE != 'BAD' AND MOORING_ID = 'PULSE-8-2011' AND (DATA_TIMESTAMP < '2011-08-04' OR DATA_TIMESTAMP >= '2012-07-19');
---UPDATE PROCESSED_INSTRUMENT_DATA SET QUALITY_CODE = 'BAD' WHERE QUALITY_CODE != 'BAD' AND MOORING_ID = 'PULSE-8-2011' AND (DATA_TIMESTAMP < '2011-08-04' OR DATA_TIMESTAMP >= '2012-01-30') AND SOURCE_FILE = 100219;

---UPDATE PROCESSED_INSTRUMENT_DATA SET QUALITY_CODE = 'BAD' WHERE QUALITY_CODE != 'BAD' AND MOORING_ID = 'PULSE-9-2012' AND (DATA_TIMESTAMP < '2012-07-18' OR DATA_TIMESTAMP > '2012-12-29');
---UPDATE PROCESSED_INSTRUMENT_DATA SET QUALITY_CODE = 'BAD' WHERE QUALITY_CODE != 'BAD' AND MOORING_ID = 'SOFS-1-2010' AND (DATA_TIMESTAMP < '2010-03-18' OR DATA_TIMESTAMP > '2011-04-20');
---UPDATE PROCESSED_INSTRUMENT_DATA SET QUALITY_CODE = 'BAD' WHERE QUALITY_CODE != 'BAD' AND MOORING_ID = 'SOFS-2-2011' AND (DATA_TIMESTAMP < '2011-11-25' OR DATA_TIMESTAMP > '2012-07-18');
---UPDATE PROCESSED_INSTRUMENT_DATA SET QUALITY_CODE = 'BAD' WHERE QUALITY_CODE != 'BAD' AND MOORING_ID = 'SOFS-3-2012' AND (DATA_TIMESTAMP < '2012-07-15' OR DATA_TIMESTAMP > '2013-01-01');

-- PULSE-6 OPTODE CONFIGURATION NOT KNOWN
DELETE FROM RAW_INSTRUMENT_DATA WHERE MOORING_ID = 'PULSE-6-2009' AND PARAMETER_CODE = 'OPTODE_BPHASE';
UPDATE RAW_INSTRUMENT_DATA SET PARAMETER_CODE = 'OPTODE_VOLT' WHERE MOORING_ID = 'PULSE-6-2009' AND PARAMETER_CODE = 'OPTODE_BPHASE_VOLT';
-- Pulse-6 dox2 out of range
UPDATE RAW_INSTRUMENT_DATA SET QUALITY_CODE = 'PBAD' WHERE QUALITY_CODE != 'BAD' AND MOORING_ID = 'PULSE-6-2009'
	AND PARAMETER_CODE = 'DOX2' AND PARAMETER_VALUE < 240 AND INSTRUMENT_ID = 621;
UPDATE RAW_INSTRUMENT_DATA SET QUALITY_CODE = 'PBAD' WHERE QUALITY_CODE != 'BAD' AND MOORING_ID = 'PULSE-6-2009'
	AND PARAMETER_CODE = 'DOX2' AND DATA_TIMESTAMP BETWEEN '2010-02-16' AND '2010-02-18' AND INSTRUMENT_ID = 621;
-- Pulse-6 PSAL spikes
UPDATE RAW_INSTRUMENT_DATA SET QUALITY_CODE = 'PBAD' WHERE QUALITY_CODE != 'BAD' AND MOORING_ID = 'PULSE-6-2009'
	AND data_timestamp in (SELECT data_timestamp from raw_instrument_data WHERE mooring_id = 'Pulse-6-2009'
		AND depth = 37.5 AND parameter_code = 'PSAL' AND parameter_value < 34 AND parameter_value > 2)
	AND depth = 37.5;


UPDATE PROCESSED_INSTRUMENT_DATA SET QUALITY_CODE = 'BAD' WHERE QUALITY_CODE != 'BAD' AND PARAMETER_CODE = 'DOX2' AND PARAMETER_VALUE < 240 and mooring_id = 'Pulse-7-2010';

UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-8-2011' AND source_file_id = 200087 AND (data_timestamp > '2012-01-30 00:00');
UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-8-2011' AND instrument_id = 5;

-- Pulse-9 SBE16 file is bad after 2012-12-29
UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-9-2012' AND source_file_id = 200125 AND (data_timestamp > '2012-12-29 11:30');
-- Pulse-9 ISUS is bad after 2012-09-05
UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-9-2012' AND instrument_id = 746 AND (data_timestamp > '2012-09-05 05:00');
---UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-9-2012' AND (data_timestamp < '2012-07-17 08:00' AND data_timestamp > '2013-05-05 01:00');

update raw_instrument_data SET depth=28.5 where mooring_id = 'Pulse-9-2012' and instrument_id = 746;
update raw_instrument_data SET depth=28.5 where mooring_id = 'Pulse-9-2012' and instrument_id = 748;
update raw_instrument_data SET depth=28.5 WHERE mooring_id = 'Pulse-9-2012' and depth = 38.5;

update raw_instrument_data set quality_code = 'BAD' where mooring_id = 'Pulse-9-2012' and data_timestamp > '2013-01-03 17:30' and instrument_id = 748;
update raw_instrument_data set quality_code = 'BAD' where mooring_id = 'Pulse-9-2012' and data_timestamp > '2012-12-29 12:00' and instrument_id = 4;

update raw_instrument_data set quality_code = 'BAD' where  mooring_id = 'Pulse-8-2011' and data_timestamp > '2012-01-30 06:00' and source_file_id = 200087;

update raw_instrument_data set quality_code = 'BAD' where  mooring_id = 'Pulse-8-2011' and data_timestamp > '2012-02-24 06:00' and source_file_id = 200123;

-- SOFS-3 ASIMET L22 CAPH Failed 2012-07-15 12:00
UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'SOFS-3-2012' AND parameter_code = 'CAPH' AND source_file_id in (300072,300069) AND (data_timestamp > '2012-07-15 12:00');
-- SOFS-3 RELH bad after 2012-07-16 00:00
UPDATE raw_instrument_data SET quality_code = 'PBAD' WHERE quality_code != 'PBAD' AND mooring_id = 'SOFS-3-2012' AND parameter_code = 'RELH' AND source_file_id in (300073,300070) AND (data_timestamp > '2012-07-16 00:00');
UPDATE raw_instrument_data SET quality_code = 'PBAD' WHERE quality_code != 'PBAD' AND mooring_id = 'SOFS-3-2012' AND parameter_code = 'CAPH' AND source_file_id in (300072,300069) AND (data_timestamp > '2012-07-15 12:00');
UPDATE raw_instrument_data SET quality_code = 'PBAD' WHERE quality_code != 'BAD' AND mooring_id = 'SOFS-3-2012' AND parameter_code = 'CAPH' AND parameter_value < 900;
UPDATE raw_instrument_data SET quality_code = 'PBAD' WHERE quality_code != 'BAD' AND mooring_id = 'SOFS-3-2012' AND parameter_code = 'CAPH' AND parameter_value < 1 AND source_file_id in (300071) and data_timestamp between '2012-06-22' AND '2012-06-23';
UPDATE raw_instrument_data SET quality_code = 'PBAD' WHERE quality_code != 'BAD' AND mooring_id = 'SOFS-3-2012' AND parameter_code = 'RAIT' AND parameter_value < 1 AND source_file_id in (300085, 300070) and data_timestamp between '2012-06-22' AND '2012-06-23';
UPDATE raw_instrument_data SET quality_code = 'PBAD' WHERE quality_code != 'BAD' AND mooring_id = 'SOFS-3-2012' AND parameter_code = 'RAIT' AND source_file_id in (300078, 300069) and data_timestamp > '2012-07-15 10:00';
-- wind speed fault
UPDATE raw_instrument_data SET quality_code = 'PBAD' WHERE quality_code != 'BAD' AND mooring_id = 'SOFS-3-2012' AND parameter_code IN ('WSPD','WSPD_MIN','WSPD_MAX') AND source_file_id in (300069) and parameter_value > 100;
UPDATE raw_instrument_data SET quality_code = 'PBAD' WHERE
	quality_code != 'BAD' AND
	mooring_id = 'SOFS-3-2012' AND
	parameter_code IN ('WSPD','WSPD_MIN','WSPD_MAX') AND
	source_file_id IN (300069, 300070) AND
	parameter_value < 1.0 AND
	MOD(date_part('epoch', data_timestamp)::numeric, (3600.0*24)::numeric) >= 21540 AND
	MOD(date_part('epoch', data_timestamp)::numeric, (3600.0*24)::numeric) <= 21720;


-- SAZ-15-2012 PSAL bad
update processed_instrument_data set quality_code = 'BAD' where mooring_id = 'SAZ-15-2012' and parameter_code = 'PSAL' and parameter_value < 34.7055 and quality_code != 'BAD';
update processed_instrument_data set quality_code = 'BAD' where
	mooring_id = 'SAZ-15-2012'
	and parameter_code = 'CNDC'
	and data_timestamp in (SELECT data_timestamp FROM processed_instrument_data WHERE mooring_id = 'SAZ-15-2012' and parameter_code = 'PSAL' and quality_code = 'BAD');

-- Pulse-10-2013 475m pressure failed
UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-10-2013' AND parameter_code = 'PRES' AND source_file_id = 200470 AND (data_timestamp > '2013-10-03 13:55:00');
-- Pulse-10-2013 oxygen @100m, 200m failed
UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-10-2013' AND parameter_code = 'DOX2' AND parameter_value < 230 ;

delete from raw_instrument_data  where mooring_id = 'Pulse-10-2013' and data_timestamp < '2013-04-01'

-- Pulse-11-2015 SWH spike

update processed_instrument_data set quality_code = 'BAD' where data_timestamp between '2016-02-27 07:00:00'  AND '2016-02-27 08:00:00' and instrument_id = 1638;
update raw_instrument_data set quality_code = 'BAD' where data_timestamp between '2016-02-27 07:00:00'  AND '2016-02-27 07:20:00' and instrument_id = 1638;

update raw_instrument_data set quality_code = 'BAD' where data_timestamp = '2015-10-26 19:30:36' and instrument_id = 2402 AND parameter_code not in ('TEMP');
update raw_instrument_data set quality_code = 'BAD' where data_timestamp = '2015-08-06 19:00:36' and instrument_id = 2218 AND parameter_code in ('DOX2');
update processed_instrument_data set quality_code = 'BAD' where data_timestamp = '2015-08-06 19:00:00' and instrument_id = 2218 AND parameter_code in ('DOX2');

update raw_instrument_data set quality_code = 'BAD' FROM
    (SELECT source_file_id AS s, instrument_id AS i, data_timestamp AS t FROM raw_instrument_data WHERE parameter_code = 'SIG_WAVE_HEIGHT' and parameter_value > 30) AS bad
    WHERE source_file_id = s and instrument_id = i and data_timestamp = t;


-- SOFS-5-2015

update raw_instrument_data set quality_code = 'BAD' where parameter_code = 'AIRT' and (parameter_value > 16 OR parameter_value < 2);
update raw_instrument_data set quality_code = 'BAD' where instrument_id = 1630 AND data_timestamp > '2015-11-26';
update raw_instrument_data set quality_code = 'BAD' where parameter_code IN ('WSPD', 'WSPD_MIN', 'WSPD_MAX') and (parameter_value > 50 OR parameter_value <= 0);
update raw_instrument_data set quality_code = 'BAD' where instrument_id = 1365 AND data_timestamp > '2015-07-08';
update raw_instrument_data set quality_code = 'BAD' where instrument_id = 1381
    AND parameter_code in ('WSPD', 'WSPD_MIN', 'WSPD_MAX', 'COMPASS', 'UWND', 'VWND', 'WDIR') AND data_timestamp > '2015-07-08';
update raw_instrument_data set quality_code = 'BAD' where parameter_code = 'RAIT' and parameter_value <= 0;

update processed_instrument_data set quality_code = 'BAD' where parameter_code = 'RAIT' and parameter_value <= 0;

update raw_instrument_data set parameter_value = -parameter_value where mooring_id = 'SOFS-5-2015' and parameter_code = 'XPOS' and parameter_value < 0;
update raw_instrument_data set parameter_value = -parameter_value where mooring_id = 'SOFS-5-2015' and parameter_code = 'YPOS' and parameter_value > 0;
update raw_instrument_data set quality_code = 'BAD' where mooring_id = 'SOFS-5-2015' and parameter_code in ('XPOS', 'YPOS') and (parameter_value > 180 or parameter_value < -180);
update raw_instrument_data set quality_code = 'BAD' where mooring_id = 'SOFS-5-2015' and parameter_code in ('XPOS') and (parameter_value > 143) AND data_timestamp between '2015-03-24' AND '2015-04-01';

-- SOFS-1-2010

update raw_instrument_data set quality_code = 'BAD' where mooring_id = 'SOFS-1-2010' and parameter_code in ('YPOS') and (parameter_value > -45.8 or parameter_value < -47) AND data_timestamp between '2010-05-01' AND '2011-03-01';
update raw_instrument_data set quality_code = 'BAD' where mooring_id = 'SOFS-1-2010' and parameter_code in ('XPOS') and (parameter_value > 142.10 or parameter_value < 141.80) AND data_timestamp between '2010-05-01' AND '2011-03-01';

update raw_instrument_data set data_timestamp = data_timestamp - interval '11 hours' where mooring_id = 'SOFS-1-2010' AND instrument_id = 1564;
update raw_instrument_data set quality_code = 'BAD' where mooring_id = 'SOFS-1-2010' and parameter_code in ('ECO_FLNTUS_CHL', 'ECO_FLNTUS_TURB') and parameter_value > 4000;

update raw_instrument_data set quality_code = 'BAD' WHERE mooring_id = 'SOFS-1-2010' AND (data_timestamp < '2010-02-18' OR data_timestamp > '2011-05-20');
update raw_instrument_data set quality_code = 'BAD' WHERE mooring_id = 'SOFS-1-2010' AND data_timestamp > '2010-04-28 00:58:00' AND instrument_id in (1381);

INSERT INTO netcdf_attributes (naming_authority, facility, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)
	VALUES ('*', '*', '*', 'SOFS-1-2010', 1564, '*', 'comment_timefix', 'STRING', 'instrument time in UTC+11, corrected back to UTC');

update raw_instrument_data set data_timestamp = data_timestamp - interval '18 hours' where mooring_id = 'SOFS-1-2010' AND instrument_id = 1358;
update raw_instrument_data set quality_code = 'BAD' where instrument_id in (1367, 1382) and parameter_code = 'RAIT' and mooring_id = 'SOFS-1-2010' AND data_timestamp > '2010-09-15 08:40';
update raw_instrument_data set quality_code = 'BAD' where instrument_id in (1358, 1381) and parameter_code = 'RAIT' and mooring_id = 'SOFS-1-2010' and data_timestamp > '2010-09-28 10:00';

INSERT INTO netcdf_attributes (naming_authority, facility, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)
	VALUES ('*', '*', '*', 'SOFS-1-2010', 1367, '*', 'comment_timefix', 'STRING', 'rain instrument failed after 2010-09-15 08:40');
INSERT INTO netcdf_attributes (naming_authority, facility, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value)
	VALUES ('*', '*', '*', 'SOFS-1-2010', 1358, '*', 'comment_timefix', 'STRING', 'rain instrument failed after 2010-09-28 10:00');


-- delete duplicates

delete from raw_instrument_data where mooring_id = 'SOFS-1-2010' and instrument_id = 1375 AND ctid in (SELECT min from (SELECT min(ctid), count(data_timestamp), data_timestamp from raw_instrument_data where mooring_id = 'SOFS-1-2010' AND parameter_code = 'YPOS' group by data_timestamp) AS a WHERE count > 1);

delete from raw_instrument_data where mooring_id = 'SOFS-1-2010' and instrument_id = 1559 AND ctid in
	(SELECT min from (SELECT min(ctid), count(data_timestamp), data_timestamp from raw_instrument_data where mooring_id = 'SOFS-1-2010' AND instrument_id = 1559 group by data_timestamp, parameter_code) AS a WHERE count > 1);


--update raw_instrument_data set instrument_id = 1564 where mooring_id = 'SOFS-1-2010' and instrument_id = 2375 and parameter_code in ('ECO_FLNTUS_CHL', 'ECO_FLNTUS_TURB');

-- FluxPulse-1-2016

-- duplcate FLNTUS data
delete from raw_instrument_data where mooring_id = 'FluxPulse-1-2016' and instrument_id = 622 AND ctid in
 (SELECT min from
  (SELECT min(ctid), count(data_timestamp), data_timestamp from raw_instrument_data where mooring_id = 'FluxPulse-1-2016' AND instrument_id = 622 group by data_timestamp, parameter_code) AS a WHERE count > 1);

delete from raw_instrument_data where mooring_id = 'FluxPulse-1-2016' and instrument_id = 1382 AND ctid in
 (SELECT min from
  (SELECT min(ctid), count(data_timestamp), data_timestamp from raw_instrument_data where mooring_id = 'FluxPulse-1-2016' AND instrument_id = 1382 group by data_timestamp, parameter_code) AS a WHERE count > 1);

DELETE FROM raw_instrument_data WHERE parameter_code in ('CPHL', 'TURB');

-- SOFS-6

delete from raw_instrument_data where mooring_id = 'SOFS-6-2017' and source_file_id = 2018058 and data_timestamp > '2018-01-01';
-- instruments set to wrong year, add 366 days (it was a leap year)
update raw_instrument_data_sofs6 set data_timestamp = data_timestamp + interval '31622400 seconds' where mooring_id = 'SOFS-6-2017' and source_file_id = 2018058;
update raw_instrument_data_sofs6 set data_timestamp = data_timestamp + interval '31622400 seconds' where mooring_id = 'SOFS-6-2017' and source_file_id = 2018059;
