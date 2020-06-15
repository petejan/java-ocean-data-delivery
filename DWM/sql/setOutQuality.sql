update raw_instrument_data set quality_code = 'OUT' from mooring 
	where mooring.mooring_id = raw_instrument_data.mooring_id and (data_timestamp > timestamp_out or data_timestamp < timestamp_in)