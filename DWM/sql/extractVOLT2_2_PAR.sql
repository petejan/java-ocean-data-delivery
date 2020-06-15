insert into raw_instrument_data 
  (select source_file_id, 660, mooring_id, data_timestamp, latitude, longitude, depth, 'PAR_VOLT', parameter_value, 'EXTRACTED' 
    from raw_instrument_data 
    where mooring_id = 'Pulse-11-2015' and instrument_id = 740 and parameter_code = 'VOLT2'
  );
