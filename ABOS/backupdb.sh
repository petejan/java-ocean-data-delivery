#!/bin/sh

date=$(date +%Y-%m-%d)

#pg_dump -Fc -v -T raw_instrument_data_fluxpulse1 -T raw_instrument_data_sofs5 -T raw_instrument_data_sofs4 -T raw_instrument_data_sofs3 -T raw_instrument_data_sofs2 -T raw_instrument_data_sofs1 -T raw_instrument_data_sofs6 -T raw_instrument_data_pulse ABOS -f $1/ABOS-part-${date}.backup.z
pg_dump -Fc -v -T 'raw_instrument_data_*' ABOS -f $1/ABOS-part-${date}.backup.z
pg_dump -Fc -v -t raw_instrument_data_saz47 ABOS -f $1/ABOS-part-saz-${date}.backup.z
pg_dump -Fc -v -t raw_instrument_data_pulse ABOS -f $1/ABOS-part-pulse-${date}.backup.z
pg_dump -Fc -v -t raw_instrument_data_sofs1 ABOS -f $1/ABOS-part-sofs1-${date}.backup.z
pg_dump -Fc -v -t raw_instrument_data_sofs2 ABOS -f $1/ABOS-part-sofs2-${date}.backup.z
pg_dump -Fc -v -t raw_instrument_data_sofs3 ABOS -f $1/ABOS-part-sofs3-${date}.backup.z
pg_dump -Fc -v -t raw_instrument_data_sofs4 ABOS -f $1/ABOS-part-sofs4-${date}.backup.z
pg_dump -Fc -v -t raw_instrument_data_sofs5 ABOS -f $1/ABOS-part-sofs5-${date}.backup.z
pg_dump -Fc -v -t raw_instrument_data_sofs6 ABOS -f $1/ABOS-part-sofs6-${date}.backup.z
pg_dump -Fc -v -t raw_instrument_data_sofs7 ABOS -f $1/ABOS-part-sofs7-${date}.backup.z
pg_dump -Fc -v -t raw_instrument_data_fluxpulse1 ABOS -f $1/ABOS-part-fluxpulse-${date}.backup.z
