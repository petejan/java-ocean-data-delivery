package org.imos.abos.netcdf.saz;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.Mooring;
import org.jfree.util.Log;
import org.wiley.core.Common;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @see #main
 * @author Peter Jansen
 */
public final class ReadDiSAZfile
{
	Workbook wb;

	int deploymentCol = -1;
	int positionCol = -1;
	int sampleTimeCol = -1;
	int durationCol = -1;
	int depthCol = -1;
	int totalMassFluxCol = -1;
	int NproportionxCol = -1;
	int CproportionxCol = -1;
	int NmassFluxCol = -1;
	int CACO3Col = -1;
	int PICCol = -1;
	int HmassCol = -1;
	int PCcol = -1;
	int POCcol = -1;
	int BSiO2Col = -1;
	int BSiCol = -1;
	int PSALcol = -1;
	int sedMassCol = -1;
	int cupCol = -1;
	int PNPSiO2Col = -1;
	int TPCPSiO2Col = -1;
	int commentCol = -1;
	int areaCol = -1;
	int pHcol = -1;

	int startDataRow = -1;
	int endDataRow = -1;
	FormulaEvaluator evaluator;
	Sheet sheet;
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
	public Workbook readFile(String filename) throws IOException, InvalidFormatException
	{
		wb = WorkbookFactory.create(new FileInputStream(filename));
		evaluator = wb.getCreationHelper().createFormulaEvaluator();
		sheet = wb.getSheet("main");

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		return wb;		
	}

	public void parse(String filter)
	{
		this.filter = filter;

		Row row0 = sheet.getRow(0);
		Row row1 = sheet.getRow(1);
		Row row2 = sheet.getRow(2);
		Row row3 = sheet.getRow(3);

		int cells = row0.getLastCellNum();
		
		Cell deploymentCell = row1.getCell(0);
		if (deploymentCell != null)
		{
			deployment = deploymentCell.getStringCellValue();
		}

		for (int c = 0; c < cells; c++)
		{
			Cell cell0 = row0.getCell(c);
			Cell cell1 = row1.getCell(c);
			Cell cell2 = row2.getCell(c);
			Cell cell3 = row3.getCell(c);

			if (cell0 != null)
			{
				if (cell0.getCellType() == Cell.CELL_TYPE_STRING)
				{
					if (cell0.getStringCellValue().matches("Deployment"))
					{
						System.out.println("parse::deployment col " + c);
						deploymentCol = c;						
					}
					if (cell0.getStringCellValue().matches("Position"))
					{
						System.out.println("parse::position col " + c);
						positionCol = c;						
					}
//					if (cell0.getStringCellValue().matches("Depth"))
//					{
//						System.out.println("parse::depth col " + c);
//						depthCol = c;						
//					}
					if (cell0.getStringCellValue().matches("Cup"))
					{
						System.out.println("parse::cup col " + c);
						cupCol = c;						
					}
					if (cell0.getStringCellValue().matches("Time") && cell2.getStringCellValue().matches("Cup open"))
					{
						System.out.println("parse::time open col " + c);
						sampleTimeCol = c;						
					}
					if (cell0.getStringCellValue().matches("Time") && cell1.getStringCellValue().matches("open") && cell2.getStringCellValue().matches("cup"))
					{
						System.out.println("parse::duration open col " + c);
						durationCol = c;						
					}
					if (cell0.getStringCellValue().matches("sed mass"))
					{
						System.out.println("parse::sed mass col " + c);
						sedMassCol = c;						
					}
					if (cell0.getStringCellValue().matches("Mass flux") && cell3.getStringCellValue().matches("g/m2/yr"))
					{
						System.out.println("parse::total_mass_flux col " + c);
						totalMassFluxCol = c;						
					}
					if (cell0.getStringCellValue().matches("N%") && cell1.getStringCellValue().matches("% w/w"))
					{
						System.out.println("parse::nitrogen_proportion col " + c);
						NproportionxCol = c;						
					}
					if (cell0.getStringCellValue().matches("C%") && cell1.getStringCellValue().matches("% w/w"))
					{
						System.out.println("parse::carbon_proportion col " + c);
						CproportionxCol = c;						
					}
//					if (cell0.getStringCellValue().matches("N") && cell1.getStringCellValue().matches("mass flux"))
//					{
//						System.out.println("parse::nitrogen_mass_flux col " + c);
//						NmassFluxCol = c;						
//					}
//					if (cell0.getStringCellValue().matches("CaCO3") && cell1.getStringCellValue().matches("mass flux"))
//					{
//						System.out.println("parse::CACO3_mass_flux col " + c);
//						CACO3Col = c;						
//					}
//					if (cell0.getStringCellValue().matches("PIC") && cell1.getStringCellValue().matches("mass flux"))
//					{
//						System.out.println("parse::PICmass_flux col " + c);
//						PICCol = c;						
//					}
//					if (cell0.getStringCellValue().matches("H") && cell1.getStringCellValue().matches("mass flux"))
//					{
//						System.out.println("parse::H_mass_flux col " + c);
//						HmassCol = c;						
//					}
//					if (cell0.getStringCellValue().matches("PC") && cell1.getStringCellValue().matches("mass flux"))
//					{
//						System.out.println("parse::PC_mass_flux col " + c);
//						PCcol = c;						
//					}
//					if (cell0.getStringCellValue().matches("POC") && cell1.getStringCellValue().matches("mass flux"))
//					{
//						System.out.println("parse::POC_mass_flux col " + c);
//						POCcol = c;						
//					}
					if (cell0.getStringCellValue().matches("PN/PSiO2") && cell1.getStringCellValue().matches("mass ratio"))
					{
						System.out.println("parse::PN/PSiO2 proportion col " + c);
						PNPSiO2Col = c;						
					}
					if (cell0.getStringCellValue().matches("TPC/PSiO2") && cell1.getStringCellValue().matches("mass ratio"))
					{
						System.out.println("parse::TPC/PSiO2 proportion col " + c);
						TPCPSiO2Col = c;						
					}
					if (cell0.getStringCellValue().matches("proportion") && cell1.getStringCellValue().matches("BSiO2"))
					{
						System.out.println("parse::BSiO2 proportion col " + c);
						BSiO2Col = c;						
					}
					if (cell0.getStringCellValue().matches("proportion") && cell1.getStringCellValue().matches("BSi"))
					{
						System.out.println("parse::BSi proportion col " + c);
						BSiCol = c;						
					}
					if (cell0.getStringCellValue().matches("Sal"))
					{
						System.out.println("parse::Sal_mass_flux col " + c);
						PSALcol = c;						
					}
					if (cell0.getStringCellValue().matches("pH"))
					{
						System.out.println("parse::pH col " + c);
						pHcol = c;						
					}
					if (cell0.getStringCellValue().matches("area"))
					{
						System.out.println("parse::area col " + c);
						areaCol = c;						
					}
					if (cell0.getStringCellValue().matches("comments2"))
					{
						System.out.println("parse::comments2 col " + c);
						commentCol = c;						
					}
				}
			}
		}
		for(int r=4; r<sheet.getPhysicalNumberOfRows();r++)
		{
			Cell cell0 = sheet.getRow(r).getCell(0);
			if ((cell0 == null) || (cell0.getCellType() == Cell.CELL_TYPE_BLANK))
			{
				if  (startDataRow == -1)
				{
					startDataRow = r + 1;
				}
				else if (endDataRow == -1)
				{
					endDataRow = r-1; 
				}
				// System.out.println("Null first col " + r + " start " + startDataRow + " end " + endDataRow);
			}
		}
		if (endDataRow == -1)
		{
			endDataRow = sheet.getPhysicalNumberOfRows();
		}
		
		rows = getRowList();

		System.out.println("Data Start " + startDataRow + " end " + endDataRow + " size " + getTime().size());		
	}

	public int getDataRows()
	{
		return endDataRow - startDataRow;
	}

	public ArrayList<Double> getNmass()
	{
		return getDataAt(NmassFluxCol);
	}

	public ArrayList<Double> getCup()
	{
		return getDataAt(cupCol);
	}

	public ArrayList<Double> getSedMass()
	{
		return getDataAt(sedMassCol);
	}

	public ArrayList<Double> getDepths()
	{
		ArrayList<Double> depths = new ArrayList<Double>(new HashSet<Double>(getDataAt(depthCol)));

		return depths;
	}

	public String getPosition()
	{
		Row row = sheet.getRow(startDataRow);
		Cell cell = row.getCell(positionCol);

		return cell.getStringCellValue();
	}

	public ArrayList<Double> getDuration()
	{
		return getDataAt(durationCol);
	}
	public ArrayList<Double> getMassFlux()
	{
		return getDataAt(totalMassFluxCol);
	}
	public ArrayList<Double> getCACO3()
	{
		return getDataAt(CACO3Col);
	}
	public ArrayList<Double> getPIC()
	{
		return getDataAt(PICCol);
	}
	public ArrayList<Double> getHmass()
	{
		return getDataAt(HmassCol);
	}
	public ArrayList<Double> getPC()
	{
		return getDataAt(PCcol);
	}
	public ArrayList<Double> getPOC()
	{
		return getDataAt(POCcol);
	}
	public ArrayList<Double> getBSiO2()
	{
		return getDataAt(BSiO2Col);
	}

	public ArrayList<Date> getTime()
	{
		//ArrayList <Double>t = getDataAt(sampleTimeCol);
		//ArrayList <Date>d = new ArrayList<Date>();
		
		ArrayList <Date>d = getDateAt(sampleTimeCol);

//		Calendar c = new GregorianCalendar(1900,0,1);
//		c.setTimeZone(TimeZone.getDefault());
//
//		Date excelBase = c.getTime(); 
//
//		long l;
//		for(Double v : t)
//		{
//			l = ((Double)(v * 24 * 3600)).longValue() * 1000l;
//
//			d.add(new Date(l + excelBase.getTime() - (2 * 24 * 3600 * 1000l))); // 2 days out ?
//		}

		return d;
	}
	
	ArrayList<Integer> rows;
	
	public ArrayList<Integer> getRowList()
	{
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		for(int r=startDataRow;r<endDataRow;r++)
		{
			Row row = sheet.getRow(r);
			if (row == null)
			{
				continue;
			}
			boolean ok = true;
			if (row.getCell(cupCol) != null)
			{
				if (row.getCell(cupCol).getCellType() != Cell.CELL_TYPE_NUMERIC)
				{
					ok = false;
				}
				else if (row.getCell(cupCol).getNumericCellValue() > 21)
				{
					ok = false;
				}
				else if (row.getCell(cupCol).getNumericCellValue() < 1)
				{
					ok = false;
				}
				else if (Double.isNaN(row.getCell(cupCol).getNumericCellValue()))
				{
					ok = false;
				}
			}
			else
			{
				ok = false;
			}
			//log.debug("getData " + c + " row " + r + " ok " + ok);
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

		return list;
	}

	public ArrayList<Double> getDataAt(int c)
	{
		ArrayList <Double >v = new ArrayList<Double>();

		for(Integer r : rows)
		{
			Row row = sheet.getRow(r);
			Cell cell = row.getCell(c);

			if (cell != null)
			{	
				switch (cell.getCellType())
				{	
				case Cell.CELL_TYPE_FORMULA:
					CellValue cellValue = evaluator.evaluate(cell);
					v.add(cellValue.getNumberValue());						
					break;		

				case Cell.CELL_TYPE_NUMERIC:
					v.add(cell.getNumericCellValue());
					break;

				default:
					v.add(Double.NaN);
					break;
				}
			}
			else
			{
				v.add(Double.NaN);
			}
		}

		return v;		
	}
	public ArrayList<Date> getDateAt(int c)
	{
		ArrayList <Date >v = new ArrayList<Date>();

		for(Integer r : rows)
		{
			Row row = sheet.getRow(r);
			Cell cell = row.getCell(c);

			if (cell != null)
			{	
				switch (cell.getCellType())
				{	

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


	public ArrayList<String> getPositions()
	{
		ArrayList <String >v = new ArrayList<String>();

		for(Integer r : rows)
		{
			Row row = sheet.getRow(r);
			Cell cell = row.getCell(positionCol);

			if (cell != null)
			{	
				switch (cell.getCellType())
				{	
					case Cell.CELL_TYPE_FORMULA:
						CellValue cellValue = evaluator.evaluate(cell);
						v.add(cellValue.getStringValue());						
						break;		
		
					case Cell.CELL_TYPE_STRING:
						v.add(cell.getStringCellValue());
						break;
		
					default:
						v.add("");
						break;
				}
			}
			else
			{
				v.add("");
			}
		}

		return v;		
	}

	public void dump()
	{
		System.out.println("Data dump:\n");

		for (int k = 0; k < wb.getNumberOfSheets(); k++)
		{
			Sheet sheet = wb.getSheetAt(k);
			int rows = sheet.getPhysicalNumberOfRows();
			System.out.println("Sheet " + k + " \"" + wb.getSheetName(k) + "\" has " + rows + " row(s).");
			for (int r = 0; r < rows; r++)
			{
				Row row = sheet.getRow(r);
				if (row == null)
				{
					continue;
				}

				int cells = row.getLastCellNum();
				System.out.println("\nROW " + row.getRowNum() + " has " + cells + " cell(s). last cell " + row.getLastCellNum());
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
					System.out.println("CELL col=" + c + " VALUE=" + value);
				}
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
			
			System.out.println("deployment " + sf.deployment);;

			ArrayList <Double> mf = sf.getSedMass();
			ArrayList <Date> time = sf.getTime();
			ArrayList <Double> cup = sf.getCup();
			ArrayList <String> pos = sf.getPositions();
			
			String post = pos.get(0);
			
			SimpleDateFormat sdfyear = new SimpleDateFormat("yyyy");
			
			String mooringStr = "SAZ" + post.substring(0,2) + "-" + sf.deployment.substring(4, 6) + "-" + sdfyear.format(time.get(0)); 
			Mooring m = Mooring.selectByMooringID(mooringStr);
			
			log.debug("Mooring " + m.getMooringID());
			
			for(int i=0;i<time.size();i++)
			{
				post = pos.get(i);
				String depthStr = post.substring(3, 7);
				double depth = Integer.parseInt(depthStr);
				ArrayList<Instrument> inst = Instrument.selectInstrumentsAttachedToMooringAtDepth(m.getMooringID(), depth);
				
				System.out.println(sdf.format(time.get(i)) + " pos " + pos.get(i) + " mooring " + mooringStr + " depth " + depth + " instrument " + inst.get(0) + " Cup " + cup.get(i) + " cup mass " + mf.get(i));
			}
			//sf.dump();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}