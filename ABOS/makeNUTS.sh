#!/bin/sh

mooring=$1

#psql -c "SELECT file_name, datafile_pk, processing_status, depth, model, serial_number FROM instrument_data_files JOIN instrument USING (instrument_id) JOIN mooring_attached_instruments USING (instrument_id, mooring_id) WHERE mooring_id = '$mooring' ORDER BY depth, model, serial_number, file_name" ABOS

#source_file_id=$(psql -A -t --record-separator=" "  -c "SELECT datafile_pk FROM instrument_data_files WHERE mooring_id = '$mooring' AND processing_status = 'PROCESSED' ORDER BY file_name" ABOS)
#source_file_id=$(psql -A -t --record-separator=" "  -c "SELECT datafile_pk FROM instrument_data_files WHERE mooring_id = '$mooring' ORDER BY file_name" ABOS)

source_file_id=2017245 2017246 2017247 2017248 2017249 2017250 2017251 2017252 2017253 2017254 2017255 2017256 2017257 2017258 2017259

java -cp dist/ABOS.jar org.imos.abos.netcdf.NetCDFcreateSet $source_file_id

