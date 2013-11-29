pg_dump -c --inserts -t cmditemdetail -t cmditemlink -t cmddeploymentdetails IMOS-DEPLOY > IMOS-DEPLOY-items.sql
psql -f IMOS-DEPLOY-items.sql ABOS
rem psql -f sql\updateInstrumentsfromCMD.sql ABOS 
rem psql -f sql\updateMooringInstrumentsfromCMD.sql ABOS
