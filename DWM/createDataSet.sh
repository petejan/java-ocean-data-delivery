#!/bin/sh

mooring=$1

psql -c "select datafile_pk, mooring_id, file_name, processing_status from instrument_data_files where mooring_id = '$mooring' and processing_status like 'PROCESSED%' order by file_name" ABOS

files=$(psql -A -t -c "select datafile_pk from instrument_data_files where mooring_id = '$mooring' and processing_status like 'PROCESSED%'" ABOS)

java -cp dist/ABOS.jar org.imos.abos.netcdf.NetCDFcreateSet $files
