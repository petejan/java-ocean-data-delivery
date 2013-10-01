INSERT INTO mooring
(
	SELECT 	cmdddname as mooringid, 
		cmdddname as shortdescription, 
		cmddddateinposition as timestamp_in, 
		cmddddateoutposition as timestamp_out, 
		cmdddlatitude as latitude_in, 
		cmdddlongitude as longitude_in 
	FROM cmddeploymentdetails 
	  WHERE length(cmdddname) > 0 AND 
	    cmddddateoutposition NOTNULL AND 
	    cmddddateoutposition < '2100-01-01' 
	ORDER BY 1
  );