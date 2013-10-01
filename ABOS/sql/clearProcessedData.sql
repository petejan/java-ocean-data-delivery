truncate processed_instrument_data;
truncate instrument_data_processors;
delete from raw_instrument_data where quality_code != 'RAW';
