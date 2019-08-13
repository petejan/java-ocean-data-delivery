package org.imos.abos.parsers.saz;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.Vector;

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
import org.imos.abos.forms.NetCDFcreateForm;
import org.wiley.core.Common;
import org.wiley.util.SQLWrapper;

import ucar.nc2.Attribute;

/**
 *
 * @see #main
 * @author Peter Jansen
 */
public final class ReadDiSAZfile
{
	Workbook wb;
	protected static SQLWrapper query = new SQLWrapper();

	int deploymentCol = -1;
	int depthCol = -1;
	int depthActualCol = -1;
	int sampleTimeCol = -1;
	int metadataCol = -1;

	class Metadata
	{
		public Metadata(String name2, Cell v)
		{
			name = name2;
			c = v;
		}
		String name;
		public String toString()
		{
			return "Metadata [name=" + name + ", cell=" + c + "]";
		}
		Cell c;
		Attribute a;
		public String getStringValue()
		{
			String value = null;
			
			if (c.getCellType() == Cell.CELL_TYPE_FORMULA)
			{
				CellValue v = evaluator.evaluate(c);
				if (v != null)
					value = Double.toString(v.getNumberValue());

			}
			else if (c.getCellType() == Cell.CELL_TYPE_NUMERIC)
			{
				value = Double.toString(c.getNumericCellValue());
			}
			else
			{
				value = c.getStringCellValue();
			}
			
			return value;
		}
	}
	class DataCol
	{
		@Override
		public String toString()
		{
			return "DataCol [column=" + column + ", name=" + name + ", long_name=" + long_name + ", standard_name=" + standard_name + ", qcCol=" + qcCol + "]";
		}
		int column = -1;
		String name = null;
		String long_name = null;
		String standard_name = null;
		int qcCol = -1;
		String parameter_code;
		ArrayList <Metadata> metadata = new ArrayList<Metadata>();
	}
	ArrayList <DataCol> dataCols = new ArrayList<DataCol>();
	class MetadataName
	{
		int row;
		String name;
		public MetadataName(int i, String n)
		{
			row = i;
			name = n;
		}
	}
	ArrayList <MetadataName> metadataList = new ArrayList<MetadataName>();
		
	ArrayList <Integer> dataRows_netcdf = new ArrayList<Integer>();

	FormulaEvaluator evaluator;
	Sheet netcdf_format_sheet;
	//Sheet main_sheet;
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
		
//		main_sheet = wb.getSheet("main");		
//		log.debug("Sheet " + main_sheet);
		
		netcdf_format_sheet = wb.getSheet("netcdf_format");		
		if (netcdf_format_sheet == null)
			netcdf_format_sheet = wb.getSheet("netcdf");		

		log.debug("Sheet " + netcdf_format_sheet);
			
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		return wb;		
	}
	public void parse(String filter)
	{
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
		
		Cell deploymentCell = row3.getCell(0);
		if (deploymentCell != null)
		{
			deployment = deploymentCell.getStringCellValue();
		}
		dataRows_netcdf = getRowList(sheet);

		// loop over all columns
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
					else if (cell0.getStringCellValue().matches("depth_nominal"))
					{
						log.debug("parse::depth col " + c);
						depthCol = c;						
					}
					else if (cell0.getStringCellValue().matches("time") || cell0.getStringCellValue().matches("sample time") || cell0.getStringCellValue().matches("start time") || cell0.getStringCellValue().matches("sample open"))
					{
						log.debug("parse::start time col " + c);
						sampleTimeCol = c;						
					}
					else if (cell0.getStringCellValue().matches("depth_actual"))
					{
						log.debug("parse::depth actual col " + c);
						depthActualCol = c;						
					}
					else if (cell0.getStringCellValue().matches("metadata"))
					{
						log.debug("parse::metadata col " + c);
						metadataCol = c;				
						// read down this until we find a blank
						for(int r=1;r<20;r++)
						{
							//log.debug("metedata row " + r);
							
							Row row = sheet.getRow(r);
							Cell cell = row.getCell(c);

							if (cell != null)
							{
								String md = cell.toString();
								if (md.length() > 0)
								{
									int i = md.indexOf(' ');
									if (i > 0)
										md = md.substring(0, md.indexOf(' '));
									log.debug("parse::metadata row " + r + " " + md);
									MetadataName mdN = new MetadataName(r, md);
									metadataList.add(mdN);
								}
							}
						}
					}
					else if (cell0.getStringCellValue().matches("site"))
					{
					}
					else if (cell0.getStringCellValue().matches("Remote Access Sampler"))
					{
					}
					else if (cell0.getStringCellValue().endsWith("_qc"))
					{
					}
					else
					{
						Row row = sheet.getRow(0);
						Cell cell = row.getCell(c);
						log.debug("parse::other col " + c + " " + cell.toString());
						DataCol dc = new DataCol();
						dc.column = c;
						dc.name = cell.toString();
						if (sheet.getRow(2).getCell(c) != null)
							dc.long_name = sheet.getRow(2).getCell(c).getStringCellValue();

						if (dc.long_name == null)
							continue;
						
						if (!dc.long_name.isEmpty())
						{
							Cell stdCell = sheet.getRow(1).getCell(c);
							if (stdCell != null)
							{
								if (!stdCell.toString().startsWith("none"))
									dc.standard_name = stdCell.toString();
							}
							
							// check if next column end in QC, use it as the QC column
							Cell cell_qc = row.getCell(c + 1);
							if (cell_qc.toString().endsWith("_qc"))
								dc.qcCol = c + 1;
							
							// metadata
							for (MetadataName mdn : metadataList)
							{
								Cell mdCell = sheet.getRow(mdn.row).getCell(c);
								log.debug("meta data row " + mdn.row + " name " + mdn.name + " cell " + mdCell);
								if (mdCell != null)
								{
									
									Metadata md = new Metadata(mdn.name, mdCell);
									
									dc.metadata.add(md);
								}
							}
	
							dataCols.add(dc);
							
							log.debug("data col " + dc.toString());
						}
						
					}
				
				}
			}
		}
		// get parameter codes for each long_name
		for(DataCol dc : dataCols)
		{
			log.debug("Looking up " + dc.long_name);
			
			String SQL = "SELECT code FROM parameters WHERE trim(from netcdf_long_name) LIKE '" + dc.long_name.trim() + "'";
			query.setConnection(Common.getConnection());
			query.executeQuery(SQL);
			Vector dataSet = query.getData();
			if (dataSet != null && dataSet.size() > 0)
			{
				for (int i = 0; i < dataSet.size(); i++)
				{
					String p = (String) ((Vector<String>)(dataSet.get(i))).get(0);
					log.debug("Found param " + p);
					dc.parameter_code = p;				
				}
			}

			
		}

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
			c = row.getCell(0);
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
					double year = cellValue;
					
					if (year < 1990)
					{
						ok = false;
					}
					else if (year > 2100)
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
//			if (c > 1000)
//				return getDataAt(main_sheet, c - 1000);
//			else			
				return getDataAt(netcdf_format_sheet, c);
		}
		else
			return null;
	}

	public ArrayList<Double> getDataAt(Sheet sheet, int c)
	{
		ArrayList <Double >v = new ArrayList<Double>();

		ArrayList<Integer> dataRows;
		dataRows = dataRows_netcdf;
					
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
		dataRows = dataRows_netcdf;

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
			
			Double depth = sf.getDepths().get(0);
			
			String mooringStr = sf.deployment; 
			Mooring m = Mooring.selectByMooringID(mooringStr);
			
			log.debug("Mooring " + m.getMooringID());

			for (DataCol dc : sf.dataCols)
			{
				log.debug("Metadata " + dc.metadata);
				//log.debug("data Col " + dc + " " + sf.getDataAt(dc.column));
			}
			
			for(int i=0;i<time.size();i++)
			{
				depth = sf.getDataAt(sf.depthCol).get(i);
				ArrayList<Instrument> insts = Instrument.selectInstrumentsAttachedToMooringAtDepth(m.getMooringID(), depth);
				Instrument in = null;
				for (Instrument inn : insts)
				{
					if (inn.getMake().startsWith("McLane"))
						in = inn;
				}
				
				if (in != null)
				{
					log.debug(sdf.format(time.get(i)) + " mooring " + mooringStr + " depth " + depth + " instrument " + in);
				}
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
