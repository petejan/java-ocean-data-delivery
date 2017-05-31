pg_dump -c --inserts -t cmdsitelocationsequence -t cmdfieldsitesequence -t cmdsitelocation -t cmdfieldsite -t cmdsitelocationdeployment -t cmddeploymentdetailssequence -t cmditemdetailsequence -t cmditemlinksequence -t cmditemdetail -t cmditemlink -t cmddeploymentdetails IMOS-DEPLOY > IMOS-DEPLOY-items.sql
psql -f IMOS-DEPLOY-items.sql ABOS-SOFS
rem psql -f sql\updateInstrumentsfromCMD.sql ABOS 
rem psql -f sql\updateMooringInstrumentsfromCMD.sql ABOS
