pg_dump -a -v -C --inserts -t cmditemdetail -t cmditemlink -t cmd_site_location_deployment -h cmar-cmd-cdc.it.csiro.au -U csiro IMOS-DEPLOY > IMOS-DEPLOY.sql
pg_dump -s -v --inserts -t cmditemdetail -t cmditemlink -t cmd_site_location_deployment -h cmar-cmd-cdc.it.csiro.au -U csiro IMOS-DEPLOY > IMOS-DEPLOY-schem.sql


psql -c "TRUNCATE table cmditemdetail, cmditemlink, cmd_site_location_deployment" ABOS

psql -f IMOS-DEPLOY.sql ABOS

psql -f sql\updateInstrumentsfromCMD.sql ABOS

psql -f sql\updateMooringfromCMD.sql ABOS
psql -f sql\updateMooringInstrumentsfromCMD.sql ABOS



