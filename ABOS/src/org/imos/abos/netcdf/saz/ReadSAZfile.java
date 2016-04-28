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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
public final class ReadSAZfile
{
	Workbook wb;

	int deploymentCol = -1;
	int position = -1;
	int sampleTimeCol = -1;
	int durationCol = -1;
	int depthCol = -1;
	int totalMassFluxCol = -1;
	int NproportionxCol = -1;
	int NmassFluxCol = -1;
	int CACO3Col = -1;
	int PICCol = -1;
	int HmassCol = -1;
	int PCcol = -1;
	int POCcol = -1;
	int BSiO2Col = -1;
		
	int startDataRow = -1;
	int endDataRow = -1;
	FormulaEvaluator evaluator;
	Sheet sheet;
	String filter = null;
	
	public ReadSAZfile()
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
		sheet = wb.getSheetAt(0);

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		
		return wb;		
	}
	
	public void parse(String filter)
	{
		this.filter = filter;
		
		Row row0 = sheet.getRow(0);
		Row row1 = sheet.getRow(1);
		Row row3 = sheet.getRow(3);
		
		int cells = row0.getLastCellNum();
		
		for (int c = 0; c < cells; c++)
		{
			Cell cell0 = row0.getCell(c);
			Cell cell1 = row1.getCell(c);
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
						System.out.println("parse::osition col " + c);
						position = c;						
					}
					if (cell0.getStringCellValue().matches("Depth"))
					{
						System.out.println("parse::depth col " + c);
						depthCol = c;						
					}
					if (cell0.getStringCellValue().matches("Time") && cell1.getStringCellValue().matches("cup open"))
					{
						System.out.println("parse::time open col " + c);
						sampleTimeCol = c;						
					}
					if (cell0.getStringCellValue().matches("Time") && cell1.getStringCellValue().matches("cup duration"))
					{
						System.out.println("parse::duration open col " + c);
						durationCol = c;						
					}
					if (cell0.getStringCellValue().matches("Mass flux") && cell3.getStringCellValue().matches("g/m2/yr"))
					{
						System.out.println("parse::total_mass_flux open col " + c);
						totalMassFluxCol = c;						
					}
					if (cell0.getStringCellValue().matches("Total") && cell3.getStringCellValue().matches("g/m2/yr"))
					{
						System.out.println("parse::total_mass_flux open col " + c);
						totalMassFluxCol = c;						
					}
					if (cell0.getStringCellValue().matches("N") && cell1.getStringCellValue().matches("proportion"))
					{
						System.out.println("parse::nitrogen_proportion open col " + c);
						NproportionxCol = c;						
					}
					if (cell0.getStringCellValue().matches("N") && cell1.getStringCellValue().matches("mass flux"))
					{
						System.out.println("parse::nitrogen_mass_flux open col " + c);
						NmassFluxCol = c;						
					}
					if (cell0.getStringCellValue().matches("CaCO3") && cell1.getStringCellValue().matches("mass flux"))
					{
						System.out.println("parse::CACO3_mass_flux open col " + c);
						CACO3Col = c;						
					}
					if (cell0.getStringCellValue().matches("PIC") && cell1.getStringCellValue().matches("mass flux"))
					{
						System.out.println("parse::PICmass_flux open col " + c);
						PICCol = c;						
					}
					if (cell0.getStringCellValue().matches("H") && cell1.getStringCellValue().matches("mass flux"))
					{
						System.out.println("parse::H_mass_flux open col " + c);
						HmassCol = c;						
					}
					if (cell0.getStringCellValue().matches("PC") && cell1.getStringCellValue().matches("mass flux"))
					{
						System.out.println("parse::PC_mass_flux open col " + c);
						PCcol = c;						
					}
					if (cell0.getStringCellValue().matches("POC") && cell1.getStringCellValue().matches("mass flux"))
					{
						System.out.println("parse::POC_mass_flux open col " + c);
						POCcol = c;						
					}
					if (cell0.getStringCellValue().matches("BSiO2") && cell1.getStringCellValue().matches("mass flux"))
					{
						System.out.println("parse::BSiO2_mass_flux open col " + c);
						BSiO2Col = c;						
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
	
	public ArrayList<Double> getDepths()
	{
		ArrayList<Double> depths = new ArrayList<Double>(new HashSet<Double>(getDataAt(depthCol)));
		
		return depths;
	}
        
        public String getPosition()
        {
            Row row = sheet.getRow(startDataRow);
            Cell cell = row.getCell(position);
            
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
		ArrayList <Double>t = getDataAt(sampleTimeCol);
		ArrayList <Date>d = new ArrayList<Date>();
		
		Calendar c = new GregorianCalendar(1900,0,1);
		c.setTimeZone(TimeZone.getDefault());
		
		Date excelBase = c.getTime(); 
		
		long l;
		for(Double v : t)
		{
			l = ((Double)(v * 24 * 3600)).longValue() * 1000l;
			
			d.add(new Date(l + excelBase.getTime()));
		}
		
		return d;
	}
	
	public ArrayList<Double> getDataAt(int c)
	{
		ArrayList <Double >v = new ArrayList<Double>();
		
		for(int r=startDataRow;r<endDataRow;r++)
		{
			Row row = sheet.getRow(r);
			if (row == null)
			{
				continue;
			}
			boolean ok = true;
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
		
		ReadSAZfile sf = new ReadSAZfile();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try
		{
			sf.readFile(fileName);
			sf.parse(null);
			
			ArrayList <Double> mf = sf.getNmass();
			ArrayList <Date> time = sf.getTime();
			for(int i=0;i<time.size();i++)
			{
				System.out.println(sdf.format(time.get(i)) + " N Mass Flux " + mf.get(i));
			}
			//sf.dump();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
