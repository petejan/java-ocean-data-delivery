begin work;
delete from instrument;

insert into instrument
(
instrument_id,make,model,serial_number,asset_code
)
select cmdidid, 
cmdidbrand,
cmdidmodel,
cmdidserialnumber,
cmdidassetid
from cmditemdetail
;

commit work;