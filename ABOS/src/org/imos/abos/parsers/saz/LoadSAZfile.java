package org.imos.abos.parsers.saz;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentDataFile;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.dbms.ProcessedInstrumentData;
import org.imos.abos.dbms.RawInstrumentData;
import org.imos.abos.parsers.saz.ReadDiSAZfile.DataCol;
import org.imos.abos.parsers.saz.ReadDiSAZfile.Metadata;
import org.wiley.core.Common;

public class LoadSAZfile
{
    private static org.apache.log4j.Logger log = Logger.getLogger(ReadDiSAZfile.class);

	/**
	 * Method main
	 *
	 * Given 1 argument takes that as the filename, inputs it and dumps the cell
	 * values/types out to sys.out.<br/>
	 *
	 */

	public static void main(String[] args)
	{
		boolean insert = true;

		if (args.length < 1)
		{
			System.err.println("At least one argument expected");
			return;
		}

		String fileName = args[0];

        String $HOME = System.getProperty("user.home");
        PropertyConfigurator.configure("log4j.properties");
        Common.build("ABOS.properties");

		ReadDiSAZfile sf = new ReadDiSAZfile();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        InstrumentDataFile idf = new InstrumentDataFile();

        Connection conn = Common.getConnection();

		try
		{
			sf.readFile(fileName);
			sf.parse(null);

			System.out.println("deployment " + sf.deployment);;

			ArrayList <Date> time = sf.getTime();
			ArrayList <Double> actualDepth = sf.getDepthActual();

			String mooringStr = sf.deployment;
			Mooring m = Mooring.selectByMooringID(mooringStr);

			log.debug("Mooring " + m.getMooringID());

            File f = new File(fileName);
            idf.setFileName(f.getName());
            idf.setMooringID(m.getMooringID());
            idf.setFilePath(f.getAbsolutePath());
            idf.setProcessingStatus("UNPROCESSED");
            byte[] nullFile = new byte[0];
            idf.setFileData(nullFile);

            RawInstrumentData raw = new RawInstrumentData();

            raw.setLatitude(m.getLatitudeIn());
            raw.setLongitude(m.getLongitudeIn());
            raw.setMooringID(m.getMooringID());
            raw.setQualityCode("RAW");

            int inst_id = -1;
            int loaded = 0;

			for(int i=0;i<time.size();i++)
			{
				double depth = sf.getDataAt(sf.depthCol).get(i);

				log.debug("looking for instrument on mooring " + m.getMooringID() + " at depth " + depth);
				ArrayList<Instrument> insts = Instrument.selectInstrumentsAttachedToMooringAtDepth(m.getMooringID(), depth);
				Instrument in = null;
				for (Instrument inn : insts)
				{
					if (inn.getMake().startsWith("McLane"))
						in = inn;
					if (inn.getMake().startsWith("University of Washington"))
						in = inn;

				}

				if (in != null)
				{
					int new_inst_id = in.getInstrumentID();

					log.debug(sdf.format(time.get(i)) + " mooring " + mooringStr + " depth " + depth + " instrument " + in);

					if (inst_id != new_inst_id)
					{
			            if (insert)
			            {
			            	log.info("new Instrument id " + new_inst_id);

			                idf.setDataFilePrimaryKey(InstrumentDataFile.getNextSequenceNumber());
			                idf.setInstrumentID(new_inst_id);
			                idf.setInstrumentDepth(depth);
			                idf.insert();

			                raw.setSourceFileID(idf.getDataFilePrimaryKey());
			            }
					}
					inst_id = new_inst_id;

		            raw.setDataTimestamp(new Timestamp(time.get(i).getTime()));
		            raw.setDepth(depth);
		            raw.setInstrumentID(in.getInstrumentID());

					for (DataCol dc : sf.dataCols)
					{
						if (dc.parameter_code == null)
							continue;

						int sampleQc = 0;
						if (sf.getDataAt(dc.qcCol) != null)
							sampleQc = sf.getDataAt(dc.qcCol).get(i).intValue();

						log.trace("Sample QC " + sampleQc);
						switch (sampleQc)
						{
							case 1:
								raw.setQualityCode("GOOD");
								break;
							case 2:
								raw.setQualityCode("PGOOD");
								break;
							case 3:
								raw.setQualityCode("PBAD");
								break;
							case 4:
								raw.setQualityCode("BAD");
								break;
							case 9:
								raw.setQualityCode("MISSING");
								break;
							default:
								raw.setQualityCode("RAW");
								break;
						}

						log.debug("ROW " + raw.getDataTimestamp() + " Q " + raw.getQualityCode() + " value " + sampleQc);

			            //log.debug("data Col " + dc + " " + sf.getDataAt(dc.column));
						raw.setParameterCode(dc.parameter_code);
						raw.setParameterValue(sf.getDataAt(dc.column).get(i));

			            boolean ok = raw.insert();
			            loaded++;
					}

				}
			}

			// add the parameters
			PreparedStatement pc = conn.prepareStatement("INSERT INTO netcdf_attributes (naming_authority, facility, mooring, deployment, instrument_id, parameter, attribute_name, attribute_type, attribute_value) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			for (DataCol dc : sf.dataCols)
			{
				if (dc.parameter_code == null)
					continue;

				for (Metadata md : dc.metadata)
				{
					if (md.name.startsWith("long_name"))
						continue;
					if (md.name.startsWith("standard_name"))
						continue;
					if (md.name.startsWith("units"))
						continue;
					if (md.c.toString().isEmpty())
						continue;

					pc.setString(1,  "*"); // nameing_authority
					pc.setString(2,  "ABOS-SOTS"); // facility
					pc.setString(3,  "*"); // mooring
					pc.setString(4,  sf.deployment); // deployment
					pc.setNull(5,  java.sql.Types.INTEGER); // instrument_id
					pc.setString(6,  dc.parameter_code.trim()); // parameter
					pc.setString(7,  md.name.trim()); // attribute_name
					String value = md.c.toString();
					if (md.c.getCellType() == Cell.CELL_TYPE_FORMULA)
					{
						CellValue v = sf.evaluator.evaluate(md.c);
						if (v != null)
							value = Double.toString(v.getNumberValue());

					}
					else if (md.c.getCellType() == Cell.CELL_TYPE_NUMERIC)
					{
						pc.setString(8,  "NUMBER"); // attribute_type
					}
					else
					{
						pc.setString(8,  "STRING"); // attribute_type
					}
					pc.setString(9,  value); // attribute_value

					log.debug("add metadata " + pc);
					pc.executeUpdate();
				}
			}

			sf.close();

			log.info("Loaded " + loaded + " records");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
