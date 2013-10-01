"pg_dump -c --inserts -t cmditemdetail -t cmditemlink IMOS-DEPLOY > IMOS-DEPLOY-items.sql" 
"psql -f IMOS-DEPLOY-items.sql ABOS" 
"psql -f sql\updateInstrumentsfromCMD.sql" 
"psql -f sql\updateMooringInstrumentsfromCMD.sql ABOS" 
