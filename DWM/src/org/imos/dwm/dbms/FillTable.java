package org.imos.dwm.dbms;

import java.sql.*;
import java.util.*;
import javax.swing.table.*;

public class FillTable extends AbstractTableModel
{

	/**
	*
	*/
	private ResultSet rs;
	private int rowCount;
	private int columnCount;
	private ArrayList data = new ArrayList();

	public FillTable(ResultSet _rs) throws Exception
	{
		setRS(_rs);
	}

	public void setRS(ResultSet _rs) throws Exception
	{
		
		this.rs = _rs;
		ResultSetMetaData metaData = _rs.getMetaData();
		rowCount = 0;
		columnCount = metaData.getColumnCount();
		while (_rs.next())
		{
			Object[] row = new Object[columnCount];
			for (int j = 0; j < columnCount; j++)
			{
				row[j] = _rs.getObject(j + 1);
			}
			data.add(row);
			rowCount++;
		}
	}

	public int getColumnCount()
	{
		return columnCount;
	}

	public int getRowCount()
	{
		return rowCount;
	}

	public Object getValueAt(int rowIndex, int columnIndex)
	{
		Object[] row = (Object[]) data.get(rowIndex);
		
		return row[columnIndex];
	}

	public String getColumnName(int columnIndex)
	{
		try
		{
			ResultSetMetaData metaData = rs.getMetaData();
			
			return metaData.getColumnName(columnIndex + 1);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
