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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.TableColumnModel;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.parsers.AbstractDataParser;
import org.wiley.JDBCAdapter;
import org.wiley.LabMaster.Common;
import org.wiley.table.*;
import org.wiley.util.SettableCaseJTextField;
import org.wiley.util.StringUtilities;

public class MooringTable extends EditableBaseTable
{

    private static Logger logger = Logger.getLogger(MooringTable.class.getName());
    public MooringTable()
    {
        super();
    }

    MooringCollection collection = new MooringCollection();

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

        MooringTable table = new MooringTable();
        table.initialise();
    }

    @Override
    public void initialise()
    {
        super.initialise();

        setTitle("List of Moorings" );

        collection.setParentFrame(this);
        collection.loadFromPersistentStore();

        setDataStore( collection );

        
        StringEditor ucEditor = new StringEditor(new SettableCaseJTextField("MIXED"), collection);
        DateTimeEditor tsEdit = new DateTimeEditor(new DateTimeField(), collection);

        getTable().setDefaultEditor(Timestamp.class, tsEdit);

        TableColumnModel tm = getTable().getColumnModel();
        tm.getColumn(0).setCellEditor(ucEditor);
        
        //
        // if we didn't load the size/location data from the database, set the default
        //
        if(! super.loadedFrameSizeFromStore)
        {
            this.setSize(850, 400 );
        }

        spareButton1.setText("Instruments");
        spareButton1.setMnemonic('i');
        spareButton1.setToolTipText("Maintain instruments for Mooring");
        spareButton1.setVisible(true);
        spareButton1.setEnabled(true);

        spareButton2.setText("Data Files");
        spareButton2.setMnemonic('f');
        spareButton2.setToolTipText("Maintain data files for Mooring");
        spareButton2.setVisible(true);
        spareButton2.setEnabled(true);

        spareButton3.setText("Reprocess");
        spareButton3.setMnemonic('r');
        spareButton3.setToolTipText("Reprocess all previously processed files for this mooring");
        spareButton3.setVisible(true);
        spareButton3.setEnabled(true);

        spareButton4.setText("Raw Data");
        spareButton4.setMnemonic('a');
        spareButton4.setToolTipText("Display raw data summary for this mooring");
        spareButton4.setVisible(true);
        spareButton4.setEnabled(true);

        spareButton5.setText("Proc. Data");
        spareButton5.setMnemonic('p');
        spareButton5.setToolTipText("Display processed data summary for this mooring");
        spareButton5.setVisible(true);
        spareButton5.setEnabled(true);

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

        spareButton3.setMaximumSize(new Dimension(maxSize,27));
        spareButton3.setPreferredSize(new Dimension(prefSize, 27));

        spareButton4.setMaximumSize(new Dimension(maxSize,27));
        spareButton4.setPreferredSize(new Dimension(prefSize, 27));

        spareButton5.setMaximumSize(new Dimension(maxSize,27));
        spareButton5.setPreferredSize(new Dimension(prefSize, 27));

    }

    @Override
    public void runRightClick( int row, int column )
    {

//        String s = JOptionPane.showInputDialog(this,"Enter Mooring make....");
//        if(s != null)
//        {
//            collection.loadDataForSpecifiedMake(s);
//        }
    }

    @Override
    public void spareButton1_actionPerformed(ActionEvent e)
    {
        Integer currentRow = getSelectedRowNumber();
        if(currentRow == null || currentRow < 0)
        {
            Common.showMessage(this, "No Row Selection","There is no row selected, a search is not possible.");
            return;
        }
        Mooring row = (Mooring) collection.getSelectedRow(currentRow);
        //
        // check to see if we got a valid row
        //
        if(row == null)
        {
            Common.showMessage(this, "No Data","There is no row selected, a search is not possible.");
            return;
        }

        InstrumentTable table = new InstrumentTable();
        table.setRunStatus(false);
        table.setMooring(row);
        table.setLocationRelativeTo(this);
        table.initialise();

        //Common.showMessage(this, "Not Implemented","This function is not yet implemented.");
    }

    @Override
    public void spareButton2_actionPerformed(ActionEvent e)
    {
        Integer currentRow = getSelectedRowNumber();
        if(currentRow == null || currentRow < 0)
        {
            Common.showMessage(this, "No Row Selection","There is no row selected, a search is not possible.");
            return;
        }
        Mooring row = (Mooring) collection.getSelectedRow(currentRow);
        //
        // check to see if we got a valid row
        //
        if(row == null)
        {
            Common.showMessage(this, "No Data","There is no row selected, a search is not possible.");
            return;
        }

        InstrumentDataFileTable table = new InstrumentDataFileTable();
        table.setRunStatus(false);
        table.setMooring(row);
        table.setLocationRelativeTo(this);
        table.initialise();

        //Common.showMessage(this, "Not Implemented","This function is not yet implemented.");
    }

    @Override
    public void spareButton3_actionPerformed(ActionEvent e)
    {
        Object component;

        Integer currentRow = getSelectedRowNumber();
        if(currentRow == null || currentRow < 0)
        {
            Common.showMessage(this, "No Row Selection","There is no row selected, a clone is not possible.");
            return;
        }
        Mooring selectedRow = (Mooring) collection.getSelectedRow(currentRow);
        //
        // check to see if we got a valid row
        //
        if(selectedRow == null)
        {
            Common.showMessage(this, "No Data","There is no row selected, a clone is not possible.");
            return;
        }

        boolean yes = Common.askQuestion(this, "Reprocess Data", "This will result in ALL loaded data for this mooring being deleted. Proceed?");
        if(yes)
        {
            ArrayList<InstrumentDataFile> files = InstrumentDataFile.selectDataFilesForMooring(selectedRow.getMooringID());

            for(int i = 0; i < files.size(); i++)
            {
                InstrumentDataFile currentFile = files.get(i);

                Instrument currentInstrument = Instrument.selectByInstrumentID(currentFile.getInstrumentID());
                Mooring currentMooring = Mooring.selectByMooringID(currentFile.getMooringID());
                //
                // delete all data for the file
                //
                RawInstrumentData.deleteDataForFile(currentFile.getDataFilePrimaryKey());
                //
                // now re-parse & load the data
                //
                final String className = currentFile.getProcessingClass();
                try
                {

                    component = Class.forName(className).newInstance();
                    //
                    // cast it to an AbstractDataParser as everything subclasses this
                    //
                    if(component instanceof AbstractDataParser)
                    {
                        AbstractDataParser parser = (AbstractDataParser) component;

                        parser.setInstrument(currentInstrument);
                        parser.setInstrumentDataFile(currentFile);
                        parser.setMooring(currentMooring);
                        parser.setInstrumentDepth(currentFile.getInstrumentDepth());

                        parser.run();

                        currentFile.setProcessingDate(Common.current());
                        currentFile.setProcessingStatus("PROCESSED");
                        currentFile.setProcessingClass(className);
                        currentFile.setInstrumentDepth(parser.getInstrumentDepth());
                        currentFile.setFileHeaders(parser.getHeaders());

                        currentFile.update();

                    }
                    else
                    {
                        //
                        // shouldn't happen - bitch so we know to fix it
                        //
                        Common.showMessage(this,
                                "Incorrect Inheritance",
                                "Class " + className + " does not inherit from AbstractDataParser, can't create!"
                                );
                    }
                }
                catch(LinkageError le)
                {
                    System.err.println("Link Error Failure creating " + className );
                    le.printStackTrace();
                }
                catch(ClassNotFoundException cnfe)
                {
                    Common.showMessage(this,
                                "Program Not Found",
                                "Class " + className + " is not in the classpath, can't create!\n"
                                +"(Check class name, path and spelling in the defining menu item)."
                                );
                    System.err.println("Class not found exception for " + className );
                    cnfe.printStackTrace();
                }
                catch(Exception exex)
                {
                    System.err.println("Failure creating " + className);
                    exex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void spareButton4_actionPerformed(ActionEvent e)
    {
        Integer currentRow = getSelectedRowNumber();
        if(currentRow == null || currentRow < 0)
        {
            Common.showMessage(this, "No Row Selection","There is no row selected, data display is not possible.");
            return;
        }
        Mooring selectedRow = (Mooring) collection.getSelectedRow(currentRow);
        //
        // check to see if we got a valid row
        //
        if(selectedRow == null)
        {
            Common.showMessage(this, "No Data","There is no row selected, data display is not possible.");
            return;
        }

        // TODO: fix underlying code so we don't have to cast to float
        
        String SQL = "select instrument_id, depth, parameter_code, avg(parameter_value)::float, max(parameter_value)::float, min(parameter_value)::float, count(*), min(data_timestamp) AS first, max(data_timestamp) AS last, to_char(age(max(data_timestamp), min(data_timestamp)), 'DDD \"days\" HH24 \"hours\"') AS duration"
                    + " from raw_instrument_data"
                    + " where mooring_id = "
                    + StringUtilities.quoteString(selectedRow.getMooringID())
                    + " group by instrument_id, depth, parameter_code"
                    + " order by depth, instrument_id, parameter_code"
                    ;

        JDBCAdapter dataSelector = new JDBCAdapter(Common.getConnection());
        dataSelector.executeQuery(SQL);
        DataDisplayTable table = new DataDisplayTable();
        table.setRunStatus(false);
        table.setDataTitle("Summary of Raw Data For Mooring " + selectedRow.getMooringID());
        table.setData(dataSelector);
        table.initialise();
    }

    @Override
    public void spareButton5_actionPerformed(ActionEvent e)
    {
        Integer currentRow = getSelectedRowNumber();
        if(currentRow == null || currentRow < 0)
        {
            Common.showMessage(this, "No Row Selection","There is no row selected, data display is not possible.");
            return;
        }
        Mooring selectedRow = (Mooring) collection.getSelectedRow(currentRow);
        //
        // check to see if we got a valid row
        //
        if(selectedRow == null)
        {
            Common.showMessage(this, "No Data","There is no row selected, data display is not possible.");
            return;
        }

        String SQL = " select pid.instrument_id, "
                    + " ins.make || '/' || ins.model as Make,"
                    + " depth, parameter_code,"
                    + " avg(parameter_value)::float as avg_value,"
                    + " max(parameter_value)::float as max_value,"
                    + " min(parameter_value)::float as min_value,"
                    + " count(*) as total_count, min(data_timestamp) AS first, max(data_timestamp) AS last, to_char(age(max(data_timestamp), min(data_timestamp)), 'DDD \"days\" HH24 \"hours\"') AS duration\n"
                    + " from processed_instrument_data pid, instrument ins\n"
                    + " where mooring_id = "
                    + StringUtilities.quoteString(selectedRow.getMooringID())
                    + "\n"
                    + "and ins.instrument_id = pid.instrument_id\n"
                    + "AND quality_code != 'BAD'"
                    + " group by 1,2,3,4"
                    + " order by depth, instrument_id, parameter_code\n"
                    + ";\n"
                    ;
        
        logger.debug(SQL);
        
        JDBCAdapter dataSelector = new JDBCAdapter(Common.getConnection());
        dataSelector.executeQuery(SQL);
        DataDisplayTable table = new DataDisplayTable();
        table.setRunStatus(false);
        table.setDataTitle("Summary of Processed Data For Mooring " + selectedRow.getMooringID());
        table.setData(dataSelector);
        table.initialise();       
    }

    private class DataDisplayTable extends BaseTable
    {
        private String title = "Data Display";
        private JDBCAdapter collection;

        public void setDataTitle(String text)
        {
            title = text;
        }

        public void setData(JDBCAdapter set)
        {
            collection = set;
        }

        @Override
        public void initialise()
        {
            super.initialise();

            setTitle( title );
            setDataStore( collection );

            deleteButton.setEnabled(false);
            insertButton.setEnabled(false);
            updateButton.setEnabled(false);

            this.setSize(400, 600 );
            this.setVisible( true );
        }
    }
}
