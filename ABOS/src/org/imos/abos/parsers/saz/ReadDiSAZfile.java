package org.imos.abos.parsers.saz;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.Mooring;
import org.wiley.core.Common;

/**
 *
 * @see #main
 * @author Peter Jansen
 */
public final class ReadDiSAZfile
{
	Workbook wb;

	int deploymentCol = -1;
	int sampleCol = -1;
	int depthCol = -1;
	int depthActualCol = -1;
	int sampleTimeCol = -1;
	int durationCol = -1;
	int totalMassFluxCol = -1;
	int sampleQcCol = -1;
	int totalMassFluxQCCol = -1;
	int PC_mol_fluxCol = -1;
	int PN_mol_fluxCol = -1;
	int POC_mol_fluxCol = -1;
	int PIC_mol_fluxCol = -1;
	int BSi_mol_fluxCol = -1;
	
	int sedMassCol = -1;
	int cupCol = -1;
	int pHCol = -1;
	int salCol = -1;
	int PICCol = -1;
	int POCCol = -1;
	int BSiCol = -1;
	int BSiO2Col = -1;
	int CaCO3Col = -1;
	int CCol = -1;
	int NCol = -1;
	
	// FIXME: this is really ugly, should be separate classes for the parsers of the main sheet and the netcdf sheet
	
	ArrayList <Integer> dataRows_main = new ArrayList<Integer>();
	ArrayList <Integer> dataRows_netcdf = new ArrayList<Integer>();

	FormulaEvaluator evaluator;
	Sheet netcdf_format_sheet;
	Sheet main_sheet;
	String filter = null;
	
	String deployment = "unknown";

    private static org.apache.log4j.Logger log = Logger.getLogger(ReadDiSAZfile.class);

	public ReadDiSAZfile()
	{

	}

	/**
	 * creates an {@link HSSFWorkbook} the specified OS filename.
	 * @throws InvalidFormatException 
	 */
	FileInputStream inputStream = null;
	
	public Workbook readFile(String filename) throws IOException, InvalidFormatException
	{
		inputStream = new FileInputStream(filename);
		wb = WorkbookFactory.create(inputStream);
		evaluator = wb.getCreationHelper().createFormulaEvaluator();
		
		main_sheet = wb.getSheet("main");		
		log.debug("Sheet " + main_sheet);
		
		netcdf_format_sheet = wb.getSheet("netcdf_format");		
		log.debug("Sheet " + netcdf_format_sheet);

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		return wb;		
	}
	public void parse(String filter)
	{
		parseSheet(main_sheet, null);
		parseSheet(netcdf_format_sheet, null);
	}

	public void parseSheet(Sheet sheet, String filter)
	{
		this.filter = filter;

		Row row0 = sheet.getRow(0);
		Row row1 = sheet.getRow(1);
		Row row2 = sheet.getRow(2);
		Row row3 = sheet.getRow(3);

		int cells = row0.getLastCellNum();
		
		Cell deploymentCell = row2.getCell(0);
		if (deploymentCell != null)
		{
			deployment = deploymentCell.getStringCellValue();
		}

		for (int c = 0; c < cells; c++)
		{
			Cell cell0 = row0.getCell(c);
			Cell cell1 = row1.getCell(c);

			if (cell0 != null)
			{
				if (cell0.getCellType() == Cell.CELL_TYPE_STRING)
				{
					if (cell0.getStringCellValue().matches("deployment year start"))
					{
						log.debug("parse::deployment col " + c);
						deploymentCol = c;						
					}
					if (cell0.getStringCellValue().matches("depth_nominal"))
					{
						log.debug("parse::depth col " + c);
						depthCol = c;						
					}
					if (cell0.getStringCellValue().matches("depth_actual"))
					{
						log.debug("parse::depth actual col " + c);
						depthActualCol = c;						
					}
					if (cell0.getStringCellValue().matches("sample"))
					{
						log.debug("parse::sample col " + c);
						sampleCol = c;						
					}
					if (cell0.getStringCellValue().matches("sample_qc"))
					{
						log.debug("parse::sample_qc col " + c);
						sampleQcCol = c;						
					}
					if (cell0.getStringCellValue().matches("mass_flux_qc"))
					{
						log.debug("parse::sample_qc col " + c);
						sampleQcCol = c;						
					}
					if (cell0.getStringCellValue().matches("sample mid-point"))
					{
						log.debug("parse::time open col " + c);
						sampleTimeCol = c;						
					}
					if (cell0.getStringCellValue().matches("sample_duration"))
					{
						log.debug("parse::duration open col " + c);
						durationCol = c;						
					}
					if (cell0.getStringCellValue().matches("mass_flux"))
					{
						log.debug("parse::total_mass_flux col " + c);
						totalMassFluxCol = c;						
					}
					if (cell0.getStringCellValue().matches("mass_flux_qc"))
					{
						log.debug("parse::mass_flux_qc_qc col " + c);
						totalMassFluxQCCol = c;						
					}
					if (cell0.getStringCellValue().matches("PC_mol_flux"))
					{
						log.debug("parse::PC_mol_flux proportion col " + c);
						PC_mol_fluxCol = c;						
					}
					if (cell0.getStringCellValue().matches("PN_mol_flux"))
					{
						log.debug("parse::PN_mol_flux proportion col " + c);
						PN_mol_fluxCol = c;						
					}
					if (cell0.getStringCellValue().matches("POC_mol_flux"))
					{
						log.debug("parse::POC_mol_flux proportion col " + c);
						POC_mol_fluxCol = c;						
					}
					if (cell0.getStringCellValue().matches("BSi_mol_flux"))
					{
						log.debug("parse::BSi_mol_flux proportion col " + c);
						PIC_mol_fluxCol = c;						
					}
					if (cell0.getStringCellValue().matches("BSi_mol_flux"))
					{
						log.debug("parse::BSi_mol_flux col " + c);
						BSi_mol_fluxCol = c;						
					}

					if (cell0.getStringCellValue().matches("sed mass"))
					{
						log.debug("parse::sed mass col " + c);
						sedMassCol = c + 1000;						
					}
					if (cell0.getStringCellValue().matches("Cup"))
					{
						log.debug("parse::Cup col " + c);
						cupCol = c + 1000;						
					}
					if (cell0.getStringCellValue().matches("pH"))
					{
						log.debug("parse::pH col " + c);
						pHCol = c + 1000;						
					}
					if (cell0.getStringCellValue().matches("Sal"))
					{
						log.debug("parse::Sal col " + c);
						salCol = c + 1000;						
					}
					if (cell0.getStringCellValue().matches("C%"))
					{
						log.debug("parse::C col " + c);
						CCol = c + 1000;						
					}
					if (cell0.getStringCellValue().matches("N%"))
					{
						log.debug("parse::N col " + c);
						NCol = c + 1000;						
					}
					if (cell0.getStringCellValue().matches("PIC"))
					{
						log.debug("parse::PIC col " + c);
						PICCol = c + 1000;						
					}
					if (cell0.getStringCellValue().matches("CaCO3"))
					{
						log.debug("parse::CaCO3 col " + c);
						CaCO3Col = c + 1000;						
					}
					if (cell1 != null)
					{
						if (cell1.getStringCellValue().matches("BSi"))
						{
							log.debug("parse::BSi col " + c);
							BSiCol = c + 1000;						
						}
						if (cell1.getStringCellValue().matches("BSiO2"))
						{
							log.debug("parse::BSiO2 col " + c);
							BSiO2Col = c + 1000;						
						}
					}
				
				}
			}
		}
		
		if (sheet == netcdf_format_sheet)
		{
			dataRows_netcdf = getRowList(sheet);
		}
		else
		{
			dataRows_main = getRowList(sheet);			
		}
	}

	public ArrayList<Double> getSample()
	{
		return getDataAt(sampleCol);
	}
	public ArrayList<Double> getSampleQc()
	{
		return getDataAt(sampleQcCol);
	}

	public ArrayList<Double> getDepths()
	{
		ArrayList<Double> depths = new ArrayList<Double>(new HashSet<Double>(getDataAt(depthCol)));

		return depths;
	}

	public ArrayList<Double> getDepthActual()
	{
		return getDataAt(depthActualCol);
	}
	public ArrayList<Double> getDuration()
	{
		return getDataAt(durationCol);
	}
	public ArrayList<Double> getMassFlux()
	{
		return getDataAt(totalMassFluxCol);
	}
	public ArrayList<Double> getPCflux()
	{
		return getDataAt(PC_mol_fluxCol);
	}
	public ArrayList<Double> getPNflux()
	{
		return getDataAt(PN_mol_fluxCol);
	}
	public ArrayList<Double> getPOCflux()
	{
		return getDataAt(POC_mol_fluxCol);
	}
	public ArrayList<Double> getPICflux()
	{
		return getDataAt(PIC_mol_fluxCol);
	}
	public ArrayList<Double> getBSiFlux()
	{
		return getDataAt(BSi_mol_fluxCol);
	}
	
	public ArrayList<Double> getSedMass()
	{
		return getDataAt(sedMassCol);
	}
	public ArrayList<Double> getC()
	{
		return getDataAt(CCol);
	}
	public ArrayList<Double> getN()
	{
		return getDataAt(NCol);
	}
	public ArrayList<Double> getCaCO3()
	{
		return getDataAt(CaCO3Col);
	}
	public ArrayList<Double> getBSi()
	{
		return getDataAt(BSiCol);
	}
	public ArrayList<Double> getBSiO2()
	{
		return getDataAt(BSiO2Col);
	}
	public ArrayList<Double> getpH()
	{
		return getDataAt(pHCol);
	}
	public ArrayList<Double> getSal()
	{
		return getDataAt(salCol);
	}
	
	public ArrayList<Date> getTime()
	{
		ArrayList <Date>d = getDateAt(netcdf_format_sheet, sampleTimeCol);

		return d;
	}
		
	public ArrayList<Integer> getRowList(Sheet sheet)
	{
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		for(int r=0;r<sheet.getPhysicalNumberOfRows();r++)
		{
			Row row = sheet.getRow(r);
			//log.debug("row " + r + " row " + row);
			
			if (row == null)
			{
				continue;
			}
			boolean ok = true;
			Cell c = null;
			if (sheet == netcdf_format_sheet)
			{
				c = row.getCell(sampleCol);
			}
			else
			{
				c = row.getCell(cupCol-1000);
			}
			if (c != null)
			{
				Double cellValue = null;
				
				if (c.getCellType() == Cell.CELL_TYPE_FORMULA)
				{
					CellValue v = evaluator.evaluate(c);
					if (v != null)
						cellValue = v.getNumberValue();
						
				}
				if (c.getCellType() == Cell.CELL_TYPE_NUMERIC)
				{
					cellValue = c.getNumericCellValue();
				}
				if (cellValue == null)
				{
					ok = false;
				}
				else
				{
					double sampleN = cellValue;
					
					if (sampleN > 21)
					{
						ok = false;
					}
					else if (sampleN < 1)
					{
						ok = false;
					}
				}
			}
			else
			{
				ok = false;
			}
			//log.debug("getData " + row.getCell(sampleCol) + " row " + r + " ok " + ok);
			if (filter != null)
			{
				Cell cell0 = row.getCell(0);
				Cell cell1 = row.getCell(1);

				String[] fn = filter.split(",");
				if (fn.length >= 1)
				{
					int type = cell0.getCellType();
					if (type == Cell.CELL_TYPE_NUMERIC)
					{
						if (cell0.getNumericCellValue() != Integer.parseInt(fn[0]))
						{
							ok = false;
						}
					}
					else if (type == Cell.CELL_TYPE_STRING)
					{
						if (cell0.getStringCellValue().matches(fn[0]))
						{
							ok = false;
						}						
					}
				}
				if (fn.length >= 2)
				{
					int type = cell1.getCellType();
					if (type == Cell.CELL_TYPE_NUMERIC)
					{
						if (cell1.getNumericCellValue() != Integer.parseInt(fn[1]))
						{
							ok = false;
						}
					}
					else if (type == Cell.CELL_TYPE_STRING)
					{
						if (!cell1.getStringCellValue().matches(fn[1]))
						{
							ok = false;
						}						
					}
				}
			}
			if (ok)
			{
				list.add(r);
			}

		}

		log.info("Data Rows " + list.size());
		
		return list;
	}
	public ArrayList<Double> getDataAt(int c)
	{
		if (c > 0)
		{
			if (c > 1000)
				return getDataAt(main_sheet, c - 1000);
			else			
				return getDataAt(netcdf_format_sheet, c);
		}
		else
			return null;
	}

	public ArrayList<Double> getDataAt(Sheet sheet, int c)
	{
		ArrayList <Double >v = new ArrayList<Double>();

		ArrayList<Integer> dataRows;
		if (sheet == netcdf_format_sheet)
			dataRows = dataRows_netcdf;
		else
			dataRows = dataRows_main;
					
		for(Integer r : dataRows)
		{
			Row row = sheet.getRow(r);
			Cell cell = row.getCell(c);

			if (cell != null)
			{	
				switch (cell.getCellType())
				{	
				case Cell.CELL_TYPE_FORMULA:
					CellValue cellValue = evaluator.evaluate(cell);
					if (cellValue.getCellType() == Cell.CELL_TYPE_NUMERIC)
						v.add(cellValue.getNumberValue());
					else
						v.add(Double.parseDouble(cellValue.getStringValue()));						
					break;		

				case Cell.CELL_TYPE_NUMERIC:
					v.add(cell.getNumericCellValue());
					break;

				default:
					v.add(Double.NaN);
					break;
				}
				CellStyle st = cell.getCellStyle();
				HSSFColor color = (HSSFColor) st.getFillForegroundColorColor();
				int colourInt = st.getFillForegroundColor();
				//log.trace("row " + r + " cell " + c + " Cell Color " + color.getHexString() + " int " + colourInt);				
			}
			else
			{
				//log.trace("cell is null");
				v.add(Double.NaN);
			}
		}

		return v;		
	}
	public ArrayList<Date> getDateAt(Sheet sheet, int c)
	{
		ArrayList <Date >v = new ArrayList<Date>();
		ArrayList<Integer> dataRows;
		if (sheet == netcdf_format_sheet)
			dataRows = dataRows_netcdf;
		else
			dataRows = dataRows_main;

		for(Integer r : dataRows)
		{
			Row row = sheet.getRow(r);
			Cell cell = row.getCell(c);
			log.trace("Date Row " + cell);

			if (cell != null)
			{	
				switch (cell.getCellType())
				{	
					case Cell.CELL_TYPE_FORMULA:
						CellValue cellValue = evaluator.evaluate(cell);
						
						Date d = DateUtil.getJavaDate(cellValue.getNumberValue());
						
						log.trace("Date FORMULA " + cellValue.getNumberValue() + " " + d);
						v.add(d);						
						break;		
						
					case Cell.CELL_TYPE_NUMERIC:
						v.add(cell.getDateCellValue());
						break;
	
					default:
						v.add(new Date(0));
						break;
				}
			}
			else
			{
				v.add(new Date(0));
			}
		}

		return v;		
	}

	public void dump()
	{
		log.debug("Data dump:\n");

		for (int k = 0; k < wb.getNumberOfSheets(); k++)
		{
			Sheet sheet = wb.getSheetAt(k);
			int rows = sheet.getPhysicalNumberOfRows();
			log.debug("Sheet " + k + " \"" + wb.getSheetName(k) + "\" has " + rows + " row(s).");
			for (int r = 0; r < rows; r++)
			{
				Row row = sheet.getRow(r);
				if (row == null)
				{
					continue;
				}

				int cells = row.getLastCellNum();
				log.debug("\nROW " + row.getRowNum() + " has " + cells + " cell(s). last cell " + row.getLastCellNum());
				for (int c = 0; c < cells; c++)
				{
					Cell cell = row.getCell(c);
					String value = null;
					if (cell != null)
					{	
						switch (cell.getCellType())
						{	
							case Cell.CELL_TYPE_FORMULA:
								value = "FORMULA value=" + cell.getCellFormula();
								CellValue cellValue = evaluator.evaluate(cell);
								value += " = (" + cellValue.getNumberValue() + ")";										
								break;		
	
							case Cell.CELL_TYPE_NUMERIC:
								value = "NUMERIC value=" + cell.getNumericCellValue();
								break;
	
							case Cell.CELL_TYPE_STRING:
								value = "STRING value=" + cell.getStringCellValue();
								break;
	
							default:
						}
					}
					log.debug("CELL col=" + c + " VALUE=" + value);
				}
			}
		}		
	}

	public void close()
	{
		if (inputStream != null)
		{
			try
			{
				inputStream.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
	
	/**
	 * Method main
	 *
	 * Given 1 argument takes that as the filename, inputs it and dumps the cell
	 * values/types out to sys.out.<br/>
	 *
	 */

	public static void main(String[] args)
	{
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

		try
		{
			sf.readFile(fileName);
			sf.parse(null);
			
			log.debug("deployment " + sf.deployment);;

			ArrayList <Date> time = sf.getTime();
			ArrayList <Double> sample = sf.getSample();
			ArrayList <Double> depths = sf.getDepths();
			ArrayList <Double> mf = sf.getMassFlux();
			ArrayList <Double> sedMass = sf.getSedMass();
			
			Double depth = depths.get(0);
			
			String mooringStr = sf.deployment; 
			Mooring m = Mooring.selectByMooringID(mooringStr);
			
			log.debug("Mooring " + m.getMooringID());
			
			for(int i=0;i<time.size();i++)
			{
				depth = sf.getDataAt(sf.depthCol).get(i);
				ArrayList<Instrument> inst = Instrument.selectInstrumentsAttachedToMooringAtDepth(m.getMooringID(), depth);
				
				if (inst != null)
					log.debug(sdf.format(time.get(i)) + " mooring " + mooringStr + " depth " + depth + " instrument " + inst.get(0) + " Sample " + sample.get(i) + " mass flux " + mf.get(i) + " sed mass " + sedMass.get(i));
			}
			//sf.dump();
			
			sf.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
