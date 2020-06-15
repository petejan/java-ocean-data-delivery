select instrument_id, data_timestamp, depth, parameter_code, parameter_value
from processed_instrument_data
where mooring_id = 'PULSE_7'
order by depth, instrument_id, parameter_code
