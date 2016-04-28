TRUNCATE TABLE mooring_attached_instruments;
INSERT INTO mooring_attached_instruments
(
	SELECT 	distinct(cmdddname) AS mooring, 
			cmdil_childidid AS instrument_id,
			cmdildepth AS depth
	FROM cmditemlink JOIN cmd_site_location_deployment ON (cmdil_ddid = cmdddid) 	  
	  WHERE length(cmdddname) > 0 AND 
	    cmddddateoutposition NOTNULL AND 
	    cmddddateoutposition < '2100-01-01' AND
		cmddd_cbid = 2
	ORDER BY 1
)
 