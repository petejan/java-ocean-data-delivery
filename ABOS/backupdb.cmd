#!/bin/sh

pg_dump -Fc -v -T raw_instrument_data_fluxpulse1 -T raw_instrument_data_sofs5 -T raw_instrument_data_sofs4 -T raw_instrument_data_sofs3 -T raw_instrument_data_sofs1 -T raw_instrument_data_sofs2 -T raw_instrument_data_sofs1 -T raw_instrument_data_pulse -f %1\ABOS-part-2017-05-19.backup.z ABOS
pg_dump -Fc -v -t raw_instrument_data_sofs1 -f %1\ABOS-part-sofs1-2017-05-19.backup.zm ABOS
pg_dump -Fc -v -t raw_instrument_data_sofs2 -f %1\ABOS-part-sofs2-2017-05-19.backup.z ABOS
pg_dump -Fc -v -t raw_instrument_data_sofs3 -f %1\ABOS-part-sofs3-2017-05-19.backup.z ABOS
pg_dump -Fc -v -t raw_instrument_data_sofs4 -f %1\ABOS-part-sofs4-2017-05-19.backup.z ABOS
pg_dump -Fc -v -t raw_instrument_data_sofs5 -f %1\ABOS-part-sofs5-2017-05-19.backup.z ABOS
pg_dump -Fc -v -t raw_instrument_data_pulse -f %1\ABOS-part-pulse-2017-05-19.backup.z ABOS
pg_dump -Fc -v -t raw_instrument_data_fluxpulse1 -f %1\ABOS-part-fluxpulse-2017-05-19.backup.z ABOS
