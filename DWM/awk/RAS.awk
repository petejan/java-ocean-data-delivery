/Event/ { 	split( $6, date, "/");
			instrument = 2;
			mooring = "Pulse-8-2011";
			file = 200580;
			depth = 31.1;
			printf("WITH m AS (SELECT * FROM mooring WHERE mooring_id = '%s') INSERT INTO raw_instrument_data VALUES (%s, %s, mooring_id, '%s-%s-%s %s+00', latitude_in, longitude_in, %s, 'WATER_SAMPLE', %d, 'RAW');\n", mooring, file, instrument, date[3], date[1], date[2], $7, depth, $2) 
		}