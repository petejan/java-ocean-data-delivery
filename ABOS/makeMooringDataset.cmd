@rem
set mooring=%1

psql -c "SELECT file_name, datafile_pk, processing_status, depth, model, serial_number FROM instrument_data_files JOIN instrument USING (instrument_id) JOIN mooring_attached_instruments USING (instrument_id, mooring_id) WHERE mooring_id = '%mooring%' ORDER BY depth, model, serial_number, file_name" ABOS

pause

psql -A -t --record-separator=" "  -c "SELECT datafile_pk FROM instrument_data_files WHERE mooring_id = '%mooring%' ORDER BY file_name" ABOS > tmpfile.txt

set /P source_file_id=<tmpfile.txt

echo %source_file_id%

java -cp dist/ABOS.jar org.imos.abos.netcdf.NetCDFcreateSet %source_file_id%

del tmpfile.txt