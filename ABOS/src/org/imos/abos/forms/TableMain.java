package org.imos.abos.forms;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.imos.abos.dbms.SQLtable;

public class TableMain
{
	public static void main(String[] argv) throws Exception
	{
		SQLtable tab = new SQLtable("jdbc:postgresql://pete@127.0.0.1/ABOS-2015", "SELECT * FROM mooring");
		
		JTable table = tab.getTable();
		
		final JFrame f = new JFrame();
		f.setSize(1500, 600);
		f.add(new JScrollPane(table));
		f.setLayout(new GridLayout(1,1));
		tab.setEditable(true);
		
		//Where the GUI is created:
		JMenuBar menuBar;
		JMenu menu, submenu;
		JMenuItem menuItem;

		//Create the menu bar.
		menuBar = new JMenuBar();

		//Build the first menu.
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		
		menuBar.add(menu);	
		
		menuItem = new JMenuItem("Quit");
		menu.setMnemonic(KeyEvent.VK_Q);
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				f.dispose();				
			}});
		
		f.setJMenuBar(menuBar);
		menu.add(menuItem);
		
		WindowListener wClose = new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		};
		
		f.addWindowListener(wClose);
		
		f.setLocationRelativeTo(null);  // *** this will center your app ***
		f.setVisible(true);
	}

}