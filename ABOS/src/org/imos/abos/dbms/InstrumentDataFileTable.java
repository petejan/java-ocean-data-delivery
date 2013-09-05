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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.sql.Timestamp;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.TableColumnModel;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.forms.DataFileProcessorForm;
import org.wiley.LabMaster.Common;
import org.wiley.table.DateTimeEditor;
import org.wiley.table.DateTimeField;
import org.wiley.table.EditableBaseTable;
import org.wiley.table.StringEditor;
import org.wiley.util.SettableCaseJTextField;

public class InstrumentDataFileTable extends EditableBaseTable
{
    private Instrument parentInstrument = null;
    private Mooring currentMooring = null;

    public InstrumentDataFileTable()
    {
        super();
    }

    InstrumentDataFileCollection collection = new InstrumentDataFileCollection();

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

        InstrumentDataFileTable table = new InstrumentDataFileTable();
        table.initialise();
    }

    public void setInstrument(Instrument ins)
    {
        parentInstrument = ins;
    }

    @Override
    public void initialise()
    {
        super.initialise();

        if(parentInstrument == null && currentMooring == null)
        {
            setTitle("List of Data Files for Instruments" );
            collection.loadFromPersistentStore();
        }
        else
        {
            if(parentInstrument != null)
            {
                setTitle("List of Data Files for Instrument "
                    + parentInstrument.getMake()
                    + "/"
                    + parentInstrument.getModel()
                    + "/"
                    + parentInstrument.getSerialNumber()
                    );

                collection.setInstrument(parentInstrument);
            }
            else if(currentMooring != null)
            {
                setTitle("List of Data Files for Mooring "
                    + currentMooring.getMooringID()
                    + " - "
                    + currentMooring.getShortDescription()
                    );

                collection.setMooring(currentMooring);
            }
        }

        collection.setParentFrame(this);
        setDataStore( collection );

        StringEditor us = new StringEditor(new SettableCaseJTextField("MIXED"), collection);

        DateTimeEditor tsEdit = new DateTimeEditor(new DateTimeField(), collection);

        getTable().setDefaultEditor(Timestamp.class, tsEdit);        
        TableColumnModel tm = getTable().getColumnModel();
        //tm.getColumn(1).setCellEditor(compEditor);
        
        //
        // if we didn't load the size/location data from the database, set the default
        //
        if(! super.loadedFrameSizeFromStore)
        {
            this.setSize(850, 400 );
        }

        spareButton1.setText("Process");
        spareButton1.setMnemonic('p');
        spareButton1.setToolTipText("Process file contents into engineering data");
        spareButton1.setVisible(true);
        spareButton1.setEnabled(true);

        spareButton2.setText("Reprocess");
        spareButton2.setMnemonic('r');
        spareButton2.setToolTipText("Reprocess file contents into engineering data (will delete existing data");
        spareButton2.setVisible(true);
        spareButton2.setEnabled(true);

        this.setVisible( true );
    }

    @Override
    public void insertButton_actionPerformed(ActionEvent e)
    {
        if (currentMooring != null)
        {
            collection.setMooring(currentMooring);
        }
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

            collection.setParentFrame( this );
            collection.deleteRow( currentRow );
        }
    }

    @Override
    protected void jbInit() throws Exception
    {
        super.jbInit();

        int maxSize = 100;
        int prefSize = 100;

        insertButton.setMaximumSize(new Dimension(maxSize,27));
        insertButton.setPreferredSize(new Dimension(prefSize, 27));

        updateButton.setMaximumSize(new Dimension(maxSize,27));
        updateButton.setPreferredSize(new Dimension(prefSize, 27));

        deleteButton.setMaximumSize(new Dimension(maxSize,27));
        deleteButton.setPreferredSize(new Dimension(prefSize, 27));

        exportButton.setMaximumSize(new Dimension(maxSize,27));
        exportButton.setPreferredSize(new Dimension(prefSize, 27));

        quitButton.setMaximumSize(new Dimension(maxSize,27));
        quitButton.setPreferredSize(new Dimension(prefSize, 27));

        spareButton1.setMaximumSize(new Dimension(maxSize,27));
        spareButton1.setPreferredSize(new Dimension(prefSize, 27));

        spareButton2.setMaximumSize(new Dimension(maxSize,27));
        spareButton2.setPreferredSize(new Dimension(prefSize, 27));

    }

    @Override
    public void runRightClick( int row, int column )
    {

//        String s = JOptionPane.showInputDialog(this,"Enter instrument make....");
//        if(s != null)
//        {
//            collection.loadDataForSpecifiedMake(s);
//        }
    }

    @Override
    public void spareButton1_actionPerformed(ActionEvent e)
    {
        //
        // instrument data files
        //
        Integer currentRow = getSelectedRowNumber();
        if(currentRow == null || currentRow < 0)
        {
            Common.showMessage(this, "No Row Selection","There is no row selected, processing is not possible.");
            return;
        }
        InstrumentDataFile row = (InstrumentDataFile) collection.getSelectedRow(currentRow);
        //
        // check to see if we got a valid row
        //
        if(row == null)
        {
            Common.showMessage(this, "No Data","There is no row selected, processing is not possible.");
            return;
        }

        DataFileProcessorForm form = new DataFileProcessorForm();
        form.setParent(this);
        form.setRunStatus(false);
        form.setInstrumentDataFile(row);
        
        form.setLocation(this.getLocationOnScreen());
        //form.setLocationRelativeTo(this);
        
        form.initialise();
        form.setVisible(true);

        //Common.showMessage(this, "Not Implemented","This function is not yet implemented.");
    }

    @Override
    public void spareButton2_actionPerformed(ActionEvent e)
    {
        Integer currentRow = getSelectedRowNumber();
        if(currentRow == null || currentRow < 0)
        {
            Common.showMessage(this, "No Row Selection","There is no row selected, processing is not possible.");
            return;
        }
        InstrumentDataFile selectedRow = (InstrumentDataFile) collection.getSelectedRow(currentRow);
        //
        // check to see if we got a valid row
        //
        if(selectedRow == null)
        {
            Common.showMessage(this, "No Data","There is no row selected, processing is not possible.");
            return;
        }

        boolean yes = Common.askQuestion(this, "Reprocess Data", "This will result in ALL loaded data for this file being deleted. Proceed?");
        if(yes)
        {
            //
            // delete all data for the file
            //
            //RawInstrumentData.deleteDataForFile(selectedRow.getInstrumentID());
            RawInstrumentData.deleteDataForFile(selectedRow.getDataFilePrimaryKey());
            ArrayInstrumentData.deleteDataForFile(selectedRow.getDataFilePrimaryKey());
            //
            // now re-parse & load the data
            //
            DataFileProcessorForm form = new DataFileProcessorForm();
            form.setParent(this);
            form.setRunStatus(false);
            form.setInstrumentDataFile(selectedRow);
            form.setLocationRelativeTo(this);
            form.initialise();
            form.setVisible(true);
        }
    }

    protected void setMooring(Mooring mooring)
    {
        currentMooring = mooring;
    }
}


