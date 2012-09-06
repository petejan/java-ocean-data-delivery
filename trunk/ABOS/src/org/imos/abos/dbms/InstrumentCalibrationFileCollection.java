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
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;
import org.imos.abos.forms.InstrumentCalibrationFileForm;
import org.wiley.core.Common;
import org.wiley.table.EditableTableInterface;
import org.wiley.table.ExtendedTableMetaData;

public class InstrumentCalibrationFileCollection extends AbstractTableModel
        implements EditableTableInterface, ExtendedTableMetaData
{
    private static Logger logger = Logger.getLogger(InstrumentCalibrationFileCollection.class.getName());

    private Instrument parentInstrument = null;
    private Mooring parentMooring = null;

    private javax.swing.JFrame parentFrame = null;
    protected ArrayList<InstrumentCalibrationFile> rows = null;

    // save the original of the focused row both to compare values and to undo edits
    private InstrumentCalibrationFile savedRow = null;


    public void setInstrument(Instrument ins)
    {
        parentInstrument = ins;
        loadDataFilesForInstrument();
    }

    public void setMooring(Mooring m)
    {
        parentMooring = m;
        rows = InstrumentCalibrationFile.selectCalibrationFilesForMooring(parentMooring.getMooringID());
    }

    public void loadFromPersistentStore()
    {
        rows = InstrumentCalibrationFile.selectAll();
    }

    public void loadDataFilesForInstrument()
    {
        if(parentInstrument != null)
            rows = InstrumentCalibrationFile.selectCalibrationFilesForInstrument(parentInstrument.getInstrumentID());
    }

    public int getRowCount()
    {
        if(rows != null)
            return rows.size();
        else
            return 0;
    }

    @Override
    public String getColumnName(int column)
    {
        return InstrumentCalibrationFile.getColumnName( column );
    }


    @Override
    public Class getColumnClass(int column)
    {
        return InstrumentCalibrationFile.getColumnClass( column);
    }

    public int getColumnCount()
    {
        return InstrumentCalibrationFile.getColumnCount();
    }

    /**
     * this method needs to always return true so the keys behave nicely. the
     * real test of whether the cell is editable is done from the editors and they use
     * isCellReallyEditable(...) which is a kludge, but works
     * @param row
     * @param column
     * @return true always
     */
    @Override
    public boolean isCellEditable(int row, int column)
    {
        return true;
    }

     /**
     * ask the class for the appropriate value. it's up to the class to map a column
     * reference to a value
     *
     * @param aRow
     * @param aColumn
     * @return
     */
    public Object getValueAt(int aRow, int aColumn)
    {
        InstrumentCalibrationFile row = rows.get(aRow);
        return row.getColumn(aColumn);
    }

    /**
     * do not use this method!
     * @param value
     * @param aRow
     * @param aColumn
     */
    @Override
    public void setValueAt(Object value, int aRow, int aColumn)
    {
        //
        //  not used; cell updates not permitted via this method
        //
        throw new UnsupportedOperationException("You cannot use setValueAt(...) method!");
    }

    public boolean addRow(int row)
    {
        InstrumentCalibrationFile newRow = new InstrumentCalibrationFile();
        newRow.setNew(true);

        InstrumentCalibrationFileForm form = new InstrumentCalibrationFileForm();
        form.setParent(parentFrame);
        form.setLocationRelativeTo(parentFrame);
        form.setRunStatus(false);
        form.setInstrument(parentInstrument);
        form.setNew(newRow);
        form.initialise();
        form.setVisible(true);

        if(rows == null)
            rows = new ArrayList();

        rows.add(row, newRow);

        fireTableRowsInserted(row,row);
        fireTableDataChanged();

        return true;
    }

    public boolean saveEditedRow(int row)
    {
        try
        {
            InstrumentCalibrationFile editingRow = rows.get( row );
            boolean ok = editingRow.update();

            if( ! ok)
            {
                String message = editingRow.getLastErrorMessage();
                if(message == null || message.trim().isEmpty())
                    message = "SQL Error - no further details available; check command window.";

                Common.showMessage(parentFrame, "Insert/Update Error",message);
            }
            return ok;
        }
        catch(ArrayIndexOutOfBoundsException aix)
        {
            //
            // this happens when you delete the last row in the table and someday
            // someone should handle this more gracefully but for now - we don't.
            //
            return true;
        }
    }

    public boolean deleteRow(int row)
    {
        InstrumentCalibrationFile editingRow = rows.get( row );
        boolean OK = editingRow.delete();
        if(OK)
        {
            rows.remove(row);
            super.fireTableRowsDeleted(row, row);
            super.fireTableDataChanged();
            return true;
        }
        else
        {
            String errorMessage = editingRow.getLastErrorMessage();
            if(errorMessage == null || errorMessage.trim().isEmpty())
                errorMessage = "Deletion failed, no further information available.";

            Common.showMessage(parentFrame, "Deletion Failure",errorMessage);
            return false;
        }
    }

    public boolean undoRowEdits(int row)
    {
        //
        // need to set the specified row back to its default values
        // but we haven't assigned anything as yet - need either a row
        // entry trigger or to delegate the saved state to the class
        // instance itself - which would be preferable
        //
        rows.set(row, savedRow);
        this.fireTableRowsUpdated(row, row);
        return true;
    }

    public void storeRowBeforeEditing(int row)
    {
        try
        {
            savedRow =  (InstrumentCalibrationFile) ((InstrumentCalibrationFile)rows.get(row)).clone();
        }
        catch (CloneNotSupportedException ex)
        {
            logger.error(ex);
        }
    }

    public boolean updateCell(Object value, int aRow, int aColumn)
    {
        InstrumentCalibrationFile row = rows.get(aRow);
        boolean ok = row.setColumn(aColumn, value);
        if(ok)
        {
            fireTableCellUpdated(aRow, aColumn);
        }
        return ok;
    }

    public boolean isCellReallyEditable(int row, int column)
    {
        return false;
    }

    /**
     * tell this class what its parent is, so that it can pass the information on to
     * dialog boxes and the like.
     *
     * @param parent
     */
    public void setParentFrame( javax.swing.JFrame parent )
    {
        parentFrame = parent;
    }

    public int[] getColumnWidths()
    {
        return InstrumentCalibrationFile.getColumnWidths();
    }

    public Object getSelectedRow(int row)
    {
        if(row < rows.size())
            return rows.get(row);
        else
            return null;
    }

    protected void updateRow(int intValue)
    {
        InstrumentCalibrationFile row = rows.get(intValue);

        InstrumentCalibrationFileForm form = new InstrumentCalibrationFileForm();
        form.setParent(parentFrame);
        form.setLocationRelativeTo(parentFrame);
        form.setRunStatus(false);
        form.setInstrument(parentInstrument);
        form.setInstrumentCalibrationFile(row);
        form.initialise();
        form.setVisible(true);

        //throw new UnsupportedOperationException("Not yet implemented");
    }

}
