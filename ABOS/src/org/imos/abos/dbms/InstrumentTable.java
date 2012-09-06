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
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.TableColumnModel;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.wiley.LabMaster.Common;
import org.wiley.table.ComboBoxEditor;
import org.wiley.table.DateTimeField;
import org.wiley.table.EditableBaseTable;
import org.wiley.table.StringEditor;
import org.wiley.table.DateTimeEditor;
import org.wiley.util.SettableCaseJTextField;

public class InstrumentTable extends EditableBaseTable
{
    private static Logger logger = Logger.getLogger(InstrumentTable.class.getName());
    private Mooring currentMooring = null;

    public InstrumentTable()
    {
        super();
    }

    private InstrumentCollection collection = new InstrumentCollection();

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
            Common.build("ABOS.conf");
        }

        InstrumentTable table = new InstrumentTable();
        table.initialise();
    }

    @Override
    public void initialise()
    {
        super.initialise();

        if(currentMooring != null)
        {
            setTitle("List of Instruments for Mooring "
                    + currentMooring.getMooringID()
                    + " - "
                    + currentMooring.getShortDescription()
                    );

                collection.setMooring(currentMooring);
                if(collection.getRowCount() == 0)
                {
                    attachInstrumentsToMooring();
                    collection.setMooring(currentMooring);
                }
        }
        else
        {
            setTitle("List of Instruments" );
            collection.loadFromPersistentStore();
        }

        collection.setParentFrame(this);
        setDataStore( collection );

        InstrumentStatusBox statusSelector = new InstrumentStatusBox();
        ComboBoxEditor cbe0 = new ComboBoxEditor(statusSelector, collection);

        CompTypeBox typeSelector = new CompTypeBox();
        ComboBoxEditor cbe = new ComboBoxEditor(typeSelector, collection);

        StringEditor us = new StringEditor(new SettableCaseJTextField("UPPER"), collection);

        DateTimeEditor tsEdit = new DateTimeEditor(new DateTimeField(), collection);

        getTable().setDefaultEditor(Timestamp.class, tsEdit);
        TableColumnModel tm = getTable().getColumnModel();
        //tm.getColumn(1).setCellEditor(compEditor);
        tm.getColumn(7).setCellEditor(cbe);
        tm.getColumn(8).setCellEditor(cbe0);
        //
        // if we didn't load the size/location data from the database, set the default
        //
        if(! super.loadedFrameSizeFromStore)
        {
            this.setSize(850, 400 );
        }

        spareButton1.setText("Data Files");
        spareButton1.setMnemonic('f');
        spareButton1.setToolTipText("Maintain data files for instrument");
        spareButton1.setVisible(true);
        spareButton1.setEnabled(true);

        spareButton2.setText("Cal Files");
        spareButton2.setMnemonic('l');
        spareButton2.setToolTipText("Maintain calibration files for instrument");
        spareButton2.setVisible(true);
        spareButton2.setEnabled(true);
        
        if(currentMooring != null)
        {
            spareButton3.setText("Link Files");
            spareButton3.setMnemonic('l');
            spareButton3.setToolTipText("Link instruments to mooring");
            spareButton3.setVisible(true);
            spareButton3.setEnabled(true);
        }

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

            collection.setParentFrame( this );
            collection.deleteRow( currentRow );
        }
    }

    @Override
    protected void jbInit() throws Exception
    {
        super.jbInit();

        int maxSize = 105;
        int prefSize = 105;

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
        
        spareButton3.setMaximumSize(new Dimension(maxSize,27));
        spareButton3.setPreferredSize(new Dimension(prefSize, 27));

    }

    @Override
    public void runRightClick( int row, int column )
    {
        if(column == 2)
        {
            String s = JOptionPane.showInputDialog(this,"Enter instrument Model....");
            if(s != null)
            {
                collection.loadDataForSpecifiedModel(s);
            }
        }
        else if(column == 3)
        {
            String s = JOptionPane.showInputDialog(this,"Enter instrument S/num....");
            if(s != null)
            {
                collection.loadDataForSpecifiedSerialNumber(s);
            }
        }
        else
        {
            String s = JOptionPane.showInputDialog(this,"Enter instrument make....");
            if(s != null)
            {
                collection.loadDataForSpecifiedMake(s);
            }
        }
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
            Common.showMessage(this, "No Row Selection","There is no row selected, a search is not possible.");
            return;
        }
        Instrument row = (Instrument) collection.getSelectedRow(currentRow);
        //
        // check to see if we got a valid row
        //
        if(row == null)
        {
            Common.showMessage(this, "No Data","There is no row selected, a search is not possible.");
            return;
        }

        InstrumentDataFileTable form = new InstrumentDataFileTable();
        
        //form.setLocationRelativeTo(this);
        
        form.setLocation(this.getLocationOnScreen());
        
        form.setRunStatus(false);
        form.setInstrument(row);
        form.initialise();
        form.setVisible(true);
    }

    @Override
    public void spareButton2_actionPerformed(ActionEvent e)
    {
        Integer currentRow = getSelectedRowNumber();
        if(currentRow == null || currentRow < 0)
        {
            Common.showMessage(this, "No Row Selection","There is no row selected.");
            return;
        }
        Instrument selectedRow = (Instrument) collection.getSelectedRow(currentRow);
        //
        // check to see if we got a valid row
        //
        if(selectedRow == null)
        {
            Common.showMessage(this, "No Data","There is no row selected.");
            return;
        }

        InstrumentCalibrationFileTable form = new InstrumentCalibrationFileTable();
        form.setLocationRelativeTo(this);
        form.setRunStatus(false);
        form.setInstrument(selectedRow);
        form.initialise();
        form.setVisible(true);

        //Common.showMessage(this, "Not Implemented","This function is not yet implemented.");

    }
    
    @Override
    public void spareButton3_actionPerformed(ActionEvent e)
    {
        String s = JOptionPane.showInputDialog(this,"Enter instrument ID to link: ");
        if(s != null)
        {
            try
            {
                Integer ix = new Integer(s.trim());
                Instrument ins= Instrument.selectByInstrumentID(ix);
                
                if(ins != null)
                {
                    Mooring.assignAttachedInstrument(currentMooring.getMooringID(), ix);
                }
            }
            catch(NumberFormatException nex)
            {
                logger.error("Not an integer number - " + s);
            }
        }
            
        //Common.showMessage(this, "Not Implemented","This function is not yet implemented.");
    }
    
    private void attachInstrumentsToMooring()
    {
        ArrayList<InstrumentDataFile> linkedFiles = InstrumentDataFile.selectDataFilesForMooring(currentMooring.getMooringID());
        
        if(linkedFiles != null)
        {
            for(int i = 0; i < linkedFiles.size(); i++)
            {
                InstrumentDataFile row = linkedFiles.get(i);
                
                boolean OK = Mooring.assignAttachedInstrument(currentMooring.getMooringID(), row.getInstrumentID());
            }
        }
    }

    protected void setMooring(Mooring m)
    {
        currentMooring = m;
    }

    class CompTypeBox extends JComboBox
    {
        public CompTypeBox()
        {
            super();

            this.addItem("SIMPLE");
            this.addItem("COMPLEX");
        }

        @Override
        public void setSelectedItem( Object val )
        {
            //logger.debug("Setting selected item to " + val);
            if(val != null && val instanceof String)
                super.setSelectedItem( ((String)val).trim());
        }
    }

    class InstrumentStatusBox extends JComboBox
    {
        public InstrumentStatusBox()
        {
            super();

            this.addItem("AVAILABLE");
            this.addItem("ASSIGNED");
        }

        @Override
        public void setSelectedItem( Object val )
        {
            //logger.debug("Setting selected item to " + val);
            if(val != null && val instanceof String)
                super.setSelectedItem( ((String)val).trim());
        }
    }

//    class CompTypeBox extends JComboBox
//    {
//        public CompTypeBox()
//        {
//            super();
//            ArrayList<AbstractInstrument> codes = AbstractInstrument.selectAllActiveCodes();
//
//            if(codes != null && codes.size() > 0)
//            {
//                this.addItem("");
//
//                for(int i = 0; i < codes.size(); i++)
//                {
//                    AbstractInstrument ps = codes.get(i);
//                    this.addItem(ps.getInstrumentID());
//                }
//            }
//            else
//            {
//                this.addItem("");
//            }
//        }
//
//        @Override
//        public void setSelectedItem( Object val )
//        {
//            //logger.debug("Setting selected item to " + val);
//            if(val != null && val instanceof String)
//                super.setSelectedItem( ((String)val).trim());
//        }
//    }
}


