package org.imos.abos.parsers.saz;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentDataFile;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.dbms.ProcessedInstrumentData;
import org.imos.abos.dbms.RawInstrumentData;
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
        

		try
		{
			sf.readFile(fileName);
			sf.parse(null);
			
			System.out.println("deployment " + sf.deployment);;

			ArrayList <Date> time = sf.getTime();
			ArrayList <Double> sample = sf.getSample();
			ArrayList <Double> actualDepth = sf.getDepthActual();
			ArrayList <Double> sampleQc = sf.getSampleQc();
			ArrayList <Double> duration = sf.getDuration();

			ArrayList <Double> massFlux = sf.getMassFlux();
			ArrayList <Double> PC = sf.getPCflux();
			ArrayList <Double> PN = sf.getPNflux();
			ArrayList <Double> POC = sf.getPOCflux();
			ArrayList <Double> PIC = sf.getPICflux();
			ArrayList <Double> BSIflux = sf.getBSiFlux();
			
			ArrayList <Double> C = sf.getC();
			ArrayList <Double> N = sf.getN();
			ArrayList <Double> CaCO3 = sf.getCaCO3();
			ArrayList <Double> BSi = sf.getBSi();
			ArrayList <Double> BSiO2 = sf.getBSiO2();
			ArrayList <Double> pH = sf.getpH();
			ArrayList <Double> sal = sf.getSal();
			ArrayList <Double> sedMass = sf.getSedMass();

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

            ProcessedInstrumentData row = new ProcessedInstrumentData();
            
            row.setLatitude(m.getLatitudeIn());
            row.setLongitude(m.getLongitudeIn());
            row.setMooringID(m.getMooringID());
            row.setQualityCode("RAW");

            int inst_id = -1;
            int loaded = 0;
            
			for(int i=0;i<time.size();i++)
			{
				double depth = sf.getDataAt(sf.depthCol).get(i);

				log.debug("looking for instrument on mooring " + m.getMooringID() + " at depth " + depth);
				ArrayList<Instrument> inst = Instrument.selectInstrumentsAttachedToMooringAtDepth(m.getMooringID(), depth);
				
				if (inst != null)
				{
					int new_inst_id = inst.get(0).getInstrumentID();
					
					log.debug(sdf.format(time.get(i)) + " mooring " + mooringStr + " depth " + depth + " instrument " + inst.get(0) + " Sample " + sample.get(i) + " cup mass " + massFlux.get(i) + " Q " + sampleQc.get(i));
	
					if (inst_id != new_inst_id)
					{
			            if (insert)
			            {
			            	log.info("new Instrument id " + new_inst_id);
			            	
			                idf.setDataFilePrimaryKey(InstrumentDataFile.getNextSequenceNumber());
			                idf.setInstrumentID(new_inst_id);
			                idf.setInstrumentDepth(depth);
			                idf.insert();
	
			                row.setSourceFileID(idf.getDataFilePrimaryKey());	            
			                raw.setSourceFileID(idf.getDataFilePrimaryKey());	            
			            }							
					}
					inst_id = new_inst_id;
					if (sampleQc.size() != 0)
					{
						log.debug("Sample QC " + sampleQc.get(i));
						switch (sampleQc.get(i).intValue())
						{
							case 1:
								row.setQualityCode("GOOD");
								break;
							case 2:
								row.setQualityCode("PGOOD");
								break;
							case 3:
								row.setQualityCode("PBAD");
								break;
							case 4:
								row.setQualityCode("BAD");
								break;
							default:
								row.setQualityCode("RAW");
								break;
						}
					}
					else
					{
						row.setQualityCode("RAW");						
					}
					
		            row.setDataTimestamp(new Timestamp(time.get(i).getTime()));
		            row.setDepth(depth);
		            row.setInstrumentID(inst.get(0).getInstrumentID());
		            
		            raw.setDataTimestamp(new Timestamp(time.get(i).getTime()));
		            raw.setDepth(depth);
		            raw.setInstrumentID(inst.get(0).getInstrumentID());
		            
		            row.setDepth(actualDepth.get(i));		            
		            row.setParameterCode("SAMPLE");
		            row.setParameterValue(sample.get(i));
		            
		            log.debug("ROW " + row.getDataTimestamp() + " Q " + row.getQualityCode());
	
		            boolean ok = row.insert();
		            loaded++;
		            				
		            row.setParameterCode("DURATION");
		            row.setParameterValue(duration.get(i));
	
		            ok = row.insert();
		            loaded++;
	
		            row.setParameterCode("MASS_FLUX");
		            row.setParameterValue(massFlux.get(i));
	
		            ok = row.insert();
		            loaded++;
	
		            row.setParameterCode("PC_FLUX");
		            row.setParameterValue(PC.get(i));
	
		            ok = row.insert();
		            loaded++;
	
		            row.setParameterCode("PN_FLUX");
		            row.setParameterValue(PN.get(i));
	
		            ok = row.insert();
		            loaded++;
	
		            row.setParameterCode("PIC_FLUX");
		            row.setParameterValue(PIC.get(i));
	
		            ok = row.insert();
		            loaded++;
	
		            row.setParameterCode("POC_FLUX");
		            row.setParameterValue(POC.get(i));
	
		            ok = row.insert();
		            loaded++;
	
		            row.setParameterCode("BSi_FLUX");
		            row.setParameterValue(BSIflux.get(i));
	
		            ok = row.insert();
		            loaded++;

		            raw.setParameterCode("SAMPLE");
		            raw.setParameterValue(sample.get(i));
		            ok = raw.insert();
		            loaded++;

		            raw.setParameterCode("DURATION");
		            raw.setParameterValue(duration.get(i));
	
		            ok = raw.insert();
		            loaded++;		            
		            
		            raw.setParameterCode("C");
		            raw.setParameterValue(C.get(i));
	
		            ok = raw.insert();
		            loaded++;
		            
		            raw.setParameterCode("N");
		            raw.setParameterValue(N.get(i));
	
		            ok = raw.insert();
		            loaded++;
		            
		            raw.setParameterCode("CaCO3");
		            raw.setParameterValue(CaCO3.get(i));
	
		            ok = raw.insert();
		            loaded++;

		            raw.setParameterCode("BSi");
		            raw.setParameterValue(BSi.get(i));
	
		            ok = raw.insert();
		            loaded++;

		            raw.setParameterCode("BSiO2");
		            raw.setParameterValue(BSiO2.get(i));
	
		            ok = raw.insert();
		            loaded++;
		            
		            raw.setParameterCode("PIC");
		            raw.setParameterValue(PIC.get(i));
	
		            ok = raw.insert();
		            loaded++;
		            
		            raw.setParameterCode("PH");
		            raw.setParameterValue(pH.get(i));
	
		            ok = raw.insert();
		            loaded++;
		            raw.setParameterCode("PSAL_REF");
		            raw.setParameterValue(sal.get(i));
	
		            ok = raw.insert();
		            loaded++;
		            raw.setParameterCode("SED_MASS");
		            raw.setParameterValue(sedMass.get(i));
	
		            ok = raw.insert();
		            loaded++;
				
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
