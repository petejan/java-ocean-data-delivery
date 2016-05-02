---UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-6-2009' AND (data_timestamp < '2009-09-30' OR data_timestamp > '2010-03-18');
---UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-6-2009' AND (data_timestamp = '2010-03-08 03:00');
---UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND parameter_code = 'DOX2' AND parameter_value < 240;
---UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-7-2010' AND (data_timestamp < '2010-09-12' OR data_timestamp > '2011-04-17');

---UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-8-2011' AND (data_timestamp < '2011-08-04' OR data_timestamp >= '2012-07-19');
---UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-8-2011' AND (data_timestamp < '2011-08-04' OR data_timestamp >= '2012-01-30') AND source_file = 100219;

---UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-9-2012' AND (data_timestamp < '2012-07-18' OR data_timestamp > '2012-12-29');
---UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'SOFS-1-2010' AND (data_timestamp < '2010-03-18' OR data_timestamp > '2011-04-20');
---UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'SOFS-2-2011' AND (data_timestamp < '2011-11-25' OR data_timestamp > '2012-07-18');
---UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'SOFS-3-2012' AND (data_timestamp < '2012-07-15' OR data_timestamp > '2013-01-01');

-- Pulse-6 Optode configuration not known
delete from raw_instrument_data where mooring_id = 'Pulse-6-2009' and parameter_code = 'OPTODE_BPHASE';
update raw_instrument_data set parameter_code = 'OPTODE_VOLT' where mooring_id = 'Pulse-6-2009' and parameter_code = 'OPTODE_BPHASE_VOLT';

UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-8-2011' AND source_file_id = 200087 AND (data_timestamp > '2012-01-30 00:00');
UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-8-2011' AND instrument_id = 5;

-- Pulse-9 SBE16 file is bad after 2012-12-29
UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-9-2012' AND source_file_id = 200125 AND (data_timestamp > '2012-12-29 11:30');
-- Pulse-9 ISUS is bad after 2012-09-05
UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-9-2012' AND instrument_id = 746 AND (data_timestamp > '2012-09-05 05:00');
---UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'Pulse-9-2012' AND (data_timestamp < '2012-07-17 08:00' AND data_timestamp > '2013-05-05 01:00');

-- SOFS-3 ASIMET L22 CAPH Failed 2012-07-15 12:00
UPDATE processed_instrument_data SET quality_code = 'BAD' WHERE quality_code != 'BAD' AND mooring_id = 'SOFS-3-2012' AND parameter_code = 'CAPH' AND (data_timestamp > '2012-07-15 12:00');

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

-- Pulse-11-2015 SWH spike

update processed_instrument_data set quality_code = 'BAD' where data_timestamp between '2016-02-27 07:00:00'  AND '2016-02-27 08:00:00' and instrument_id = 1638;
update raw_instrument_data set quality_code = 'BAD' where data_timestamp between '2016-02-27 07:00:00'  AND '2016-02-27 07:20:00' and instrument_id = 1638;

update raw_instrument_data set quality_code = 'BAD' where data_timestamp = '2015-10-26 19:30:36' and instrument_id = 2402 AND parameter_code not in ('TEMP');
update raw_instrument_data set quality_code = 'BAD' where data_timestamp = '2015-08-06 19:00:36' and instrument_id = 2218 AND parameter_code in ('DOX2');
update processed_instrument_data set quality_code = 'BAD' where data_timestamp = '2015-08-06 19:00:00' and instrument_id = 2218 AND parameter_code in ('DOX2');
