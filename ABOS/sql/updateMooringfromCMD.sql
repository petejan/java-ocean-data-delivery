INSERT INTO mooring
(
	SELECT 	cmdddname as mooring_id, 
		cmdddname as shortdescription, 
		cmddddateinposition as timestamp_in, 
		cmddddateoutposition as timestamp_out, 
		cmdddlatitude as latitude_in, 
		cmdddlongitude as longitude_in,
		cmdddlatitude as latitude_out, 
		cmdddlongitude as longitude_out 
	FROM cmddeploymentdetails 
	  WHERE length(cmdddname) > 0 AND
		cmddd_cbid = 2 AND
		cmdddname NOT IN (SELECT mooring_id FROM mooring) AND
		NOT (cmddddateinposition ISNULL)
	ORDER BY 1
  );
  
UPDATE mooring SET (short_description, timestamp_in, timestamp_out, latitude_in, longitude_in, latitude_out, longitude_out) = 
						(cmdddname, cmddddateinposition, cmddddateoutposition, cmdddlatitude, cmdddlongitude, cmdddlatitude, cmdddlongitude) 
		FROM cmddeploymentdetails WHERE mooring_id = cmdddname;