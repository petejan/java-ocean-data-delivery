UPDATE raw_instrument_data SET parameter_code = 'OPTODE_VOLT' WHERE parameter_code = 'OPTODE_BPHASE_VOLT' AND mooring_id = 'Pulse-6-2009';
DELETE from raw_instrument_data WHERE parameter_code = 'OPTODE_BPHASE' AND mooring_id = 'Pulse-6-2009';
