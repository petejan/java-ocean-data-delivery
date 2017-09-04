@rem
set mooring=%1

psql -c "SELECT file_name, datafile_pk, processing_status FROM instrument_data_files WHERE mooring_id = '%mooring%' ORDER BY file_name" ABOS

psql -A -t --record-separator=" "  -c "SELECT datafile_pk FROM instrument_data_files WHERE mooring_id = '%mooring%' ORDER BY file_name" ABOS > tmpfile.txt

set /P source_file_id=<tmpfile.txt

echo %source_file_id%

java -cp dist/ABOS.jar org.imos.abos.netcdf.NetCDFcreateSet %source_file_id%

del tmpfile.txt