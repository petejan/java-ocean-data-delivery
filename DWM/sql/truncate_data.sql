truncate instrument_datafile_headers, raw_instrument_data, array_instrument_data, processed_instrument_data;
truncate instrument_data_files CASCADE;
truncate instrument_data_processors;
select setval('datafile_sequence', 200001);
select setval('instrument_data_processor_sequence', 100001);
