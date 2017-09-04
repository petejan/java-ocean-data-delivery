#!/bin/sh

set date=2017-09-04

pg_dump -Fc -v -T raw_instrument_data_fluxpulse1 -T raw_instrument_data_sofs5 -T raw_instrument_data_sofs4 -T raw_instrument_data_sofs3 -T raw_instrument_data_sofs1 -T raw_instrument_data_sofs2 -T raw_instrument_data_sofs1 -T raw_instrument_data_pulse -f %1\ABOS-part-%date%.backup.z ABOS
pg_dump -Fc -v -t raw_instrument_data_sofs1 -f %1\ABOS-part-sofs1-%date%.backup.zm ABOS
pg_dump -Fc -v -t raw_instrument_data_sofs2 -f %1\ABOS-part-sofs2-%date%.backup.z ABOS
pg_dump -Fc -v -t raw_instrument_data_sofs3 -f %1\ABOS-part-sofs3-%date%.backup.z ABOS
pg_dump -Fc -v -t raw_instrument_data_sofs4 -f %1\ABOS-part-sofs4-%date%.backup.z ABOS
pg_dump -Fc -v -t raw_instrument_data_sofs5 -f %1\ABOS-part-sofs5-%date%.backup.z ABOS
pg_dump -Fc -v -t raw_instrument_data_pulse -f %1\ABOS-part-pulse-%date%.backup.z ABOS
pg_dump -Fc -v -t raw_instrument_data_fluxpulse1 -f %1\ABOS-part-fluxpulse-%date%.backup.z ABOS
