select instrument_id, parameter_code, depth, count(*) 
from processed_instrument_data
group by instrument_id, parameter_code, depth
order by depth, instrument_id, parameter_code