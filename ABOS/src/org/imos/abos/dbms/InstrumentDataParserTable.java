/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.dbms;

/**
 *
 * @author peter
 */
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.TableColumnModel;
import org.apache.log4j.PropertyConfigurator;
import org.wiley.LabMaster.Common;
import org.wiley.table.BooleanEditor;
import org.wiley.table.EditableBaseTable;

public class InstrumentDataParserTable extends EditableBaseTable
{
    public InstrumentDataParserTable()
    {
        super();
    }

    InstrumentDataParserCollection collection = new InstrumentDataParserCollection();

    public static void main(String[] args)
    {
        try
        {
	    // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel(
            UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException e)
        {
           // handle exception
        }
        catch (ClassNotFoundException e)
        {
           // handle exception
        }
        catch (InstantiationException e)
        {
           // handle exception
        }
        catch (IllegalAccessException e)
        {
           // handle exception
        }

        String $HOME = System.getProperty("user.home");

        if(args.length == 0)
        {
            PropertyConfigurator.configure("log4j.properties");
            Common.build($HOME + "/ABOS/ABOS.properties");
        }

        InstrumentDataParserTable table = new InstrumentDataParserTable();
        table.initialise();
    }

    @Override
    public void initialise()
    {
        super.initialise();

        setTitle("List of Data Parser Classes" );

        collection.setParentFrame(this);
        collection.loadFromPersistentStore();
        setDataStore( collection );

        //StringEditor us = new StringEditor(new SettableCaseJTextField("UPPER"), collection);
        BooleanEditor be = new BooleanEditor(new JCheckBox(), collection);

        TableColumnModel tm = getTable().getColumnModel();
        //tm.getColumn(0).setCellEditor(us);
        //
        // if we didn't load the size/location data from the database, set the default
        //
        if(! super.loadedFrameSizeFromStore)
        {
            this.setSize(700, 400 );
        }

        updateButton.setVisible(false);

        this.setVisible( true );
    }

    @Override
    public void insertButton_actionPerformed(ActionEvent e)
    {
        collection.setParentFrame( this );
        collection.addRow(collection.getRowCount());
    }

    @Override
    public void updateButton_actionPerformed(ActionEvent e)
    {
        Integer currentRow = getSelectedRowNumber();

        collection.setParentFrame( this );
        collection.updateRow( currentRow.intValue());
    }

    @Override
    public void deleteButton_actionPerformed(ActionEvent e)
    {
        if( super.confirmDelete() )
        {
            Integer currentRow = getSelectedRowNumber();
            collection.deleteRow( currentRow );
        }
    }
}

