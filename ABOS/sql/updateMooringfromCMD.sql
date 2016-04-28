INSERT INTO mooring
(
	SELECT 	cmdddname as mooring_id, 
		cmdddname as shortdescription, 
		cmddddateinposition as timestamp_in, 
		cmddddateoutposition as timestamp_out, 
		cmdddlatitude as latitude_in, 
		cmdddlongitude as longitude_in,
		cmdddlatitude as latitude_out, 
		cmdddlongitude as longitude_out,
		cmdfsinstitution as facility
	FROM cmd_site_location_deployment 
	  WHERE length(cmdddname) > 0 AND
		cmddd_cbid = 2 AND
		NOT EXISTS (select 1 FROM mooring WHERE mooring_id = cmdddname) AND
		NOT (cmddddateinposition ISNULL)
	ORDER BY 1
  );
  
UPDATE mooring SET (short_description, timestamp_in, timestamp_out, latitude_in, longitude_in, latitude_out, longitude_out, facility) = 
						(cmdddname, cmddddateinposition, cmddddateoutposition, cmdddlatitude, cmdddlongitude, cmdddlatitude, cmdddlongitude, cmdfsinstitution) 
		FROM cmd_site_location_deployment WHERE mooring_id = cmdddname;