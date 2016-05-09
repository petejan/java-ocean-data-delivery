package org.imos.abos.dbms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.imos.abos.Main;
import org.imos.abos.forms.EditDialog;

public class SQLtable
{
	JTable table;
	ListSelectionModel listSelectionModel;
	boolean editable = false;

	private static org.apache.log4j.Logger log = Logger.getLogger(Main.class);

	public void resizeColumnWidth(JTable table)
	{
		final TableColumnModel columnModel = table.getColumnModel();
		for (int column = 0; column < table.getColumnCount(); column++)
		{
			int width = 50; // Min width
			for (int row = 0; row < table.getRowCount(); row++)
			{
				TableCellRenderer renderer = table.getCellRenderer(row, column);
				Component comp = table.prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width + 1, width);
			}
			columnModel.getColumn(column).setPreferredWidth(width);
		}
	}

	public SQLtable(String db, String select) throws Exception
	{
		table = new JTable(myModel(db, select));

		table.setDefaultRenderer(Object.class, new TableCellRenderer()
		{
			private DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
			{
				Component c = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (row % 10 < 5)
				{
					c.setBackground(Color.WHITE);
				} 
				else
				{
					c.setBackground(Color.getHSBColor(0.3f, 0.15f, 1f));
				}
				return c;
			}

		});

		table.setShowGrid(true);
		table.setGridColor(Color.LIGHT_GRAY);
		table.setAutoCreateRowSorter(true);
		table.getRowSorter().toggleSortOrder(0);
		table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setCellSelectionEnabled(false);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		table.setSelectionForeground(Color.RED);

		resizeColumnWidth(table);		
	}

	public void setEditable(boolean edit)
	{
		editable = edit;

		table.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent me)
			{
				JTable table = (JTable) me.getSource();
				Point p = me.getPoint();
				int row = table.rowAtPoint(p);
				if (me.getClickCount() == 2)
				{
					System.out.println("double click " + row);
					Vector<String> labels = new Vector<String>();
					Vector<Object> values = new Vector<Object>();
					for(int i=0;i<table.getColumnCount();i++)
					{
						labels.add(table.getColumnName(i));
						values.add(table.getValueAt(row, i));
					}

					EditDialog ed = new EditDialog(labels, values);

				}
			}
		});

		listSelectionModel = table.getSelectionModel();
		listSelectionModel.addListSelectionListener(new SharedListSelectionHandler());
		table.setSelectionModel(listSelectionModel);		
	}

	public JTable getTable()
	{
		return table;
	}

	static Connection con = null;

	public static FillTable myModel(String db, String select) throws Exception
	{
		try
		{
			Class.forName("org.postgresql.Driver");
		} 
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		if (con == null)
		{
			con = DriverManager.getConnection(db);
		}
		DatabaseMetaData dm = con.getMetaData( );
		ResultSet rs = dm.getPrimaryKeys( "" , "" , "mooring" );

		while( rs.next( ) ) 
		{    
			String pkey = rs.getString("COLUMN_NAME");

			log.debug("primary key = " + pkey);
		}

		Statement st = con.createStatement();
		ResultSet rs1 = st.executeQuery(select);

		FillTable model = new FillTable(rs1);

		return model;
	}

	class SharedListSelectionHandler implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent e)
		{
			ListSelectionModel lsm = (ListSelectionModel) e.getSource();

			int firstIndex = e.getFirstIndex();
			int lastIndex = e.getLastIndex();
			boolean isAdjusting = e.getValueIsAdjusting();

			System.out.print("Event for indexes " + firstIndex + " - " + lastIndex + "; isAdjusting is " + isAdjusting + "; selected indexes:");

			if (lsm.isSelectionEmpty())
			{
				System.out.println(" <none>");
			} 
			else
			{
				// Find out which indexes are selected.
				int minIndex = lsm.getMinSelectionIndex();
				int maxIndex = lsm.getMaxSelectionIndex();
				for (int i = minIndex; i <= maxIndex; i++)
				{
					if (lsm.isSelectedIndex(i))
					{
						System.out.print(" " + i);
					}
				}

				System.out.println();
			}
		}
	}
}
