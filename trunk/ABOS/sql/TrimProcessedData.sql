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

