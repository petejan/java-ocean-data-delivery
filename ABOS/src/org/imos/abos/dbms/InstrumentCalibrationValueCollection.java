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
import org.wiley.core.Common;
import org.wiley.table.EditableTableInterface;
import org.wiley.table.ExtendedTableMetaData;

public class InstrumentCalibrationValueCollection extends AbstractTableModel
        implements EditableTableInterface, ExtendedTableMetaData
{
    private static Logger logger = Logger.getLogger(InstrumentCalibrationValueCollection.class.getName());

    private InstrumentCalibrationFile parentFile = null;
    private Instrument parentInstrument = null;
    private Mooring selectedMooring = null;

    private javax.swing.JFrame parentFrame = null;
    protected ArrayList<InstrumentCalibrationValue> rows = null;

    // save the original of the focused row both to compare values and to undo edits
    private InstrumentCalibrationValue savedRow = null;

    public void setSelectedMooring(Mooring m)
    {
        selectedMooring = m;
    }
    
    public void setParentCalibrationFile(InstrumentCalibrationFile p)
    {
        parentFile = p;
        parentInstrument = Instrument.selectByInstrumentID(parentFile.getInstrumentID());
    }

    public void loadFromPersistentStore()
    {
        if(selectedMooring != null && parentFile != null)
        {
            rows = InstrumentCalibrationValue.selectByCalibrationFileIDAndMooring(parentFile.getDataFilePrimaryKey(), selectedMooring.getMooringID());
        }
        else if(selectedMooring == null && parentFile != null)
        {
            rows = InstrumentCalibrationValue.selectByCalibrationFileID(parentFile.getDataFilePrimaryKey());
        }
        else
            rows = InstrumentCalibrationValue.selectAll();
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
        return InstrumentCalibrationValue.getDataEntryColumnName( column );
    }


    @Override
    public Class getColumnClass(int column)
    {
        return InstrumentCalibrationValue.getDataEntryColumnClass( column);
    }

    public int getColumnCount()
    {
        return InstrumentCalibrationValue.getDataEntryColumnCount();
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
        InstrumentCalibrationValue row = rows.get(aRow);
        return row.getDataEntryColumn(aColumn);
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

    public boolean saveEditedRow(int row)
    {
        try
        {
            InstrumentCalibrationValue editingRow = rows.get( row );
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
        InstrumentCalibrationValue editingRow = rows.get( row );
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
            savedRow =  (InstrumentCalibrationValue) ((InstrumentCalibrationValue)rows.get(row)).clone();
        }
        catch (CloneNotSupportedException ex)
        {
            logger.error(ex);
        }
    }

    public boolean updateCell(Object value, int aRow, int aColumn)
    {
        InstrumentCalibrationValue row = rows.get(aRow);
        boolean ok = row.setDataEntryColumn(aColumn, value);
        if(ok)
        {
            fireTableCellUpdated(aRow, aColumn);
        }
        return ok;
    }

    public boolean isCellReallyEditable(int row, int column)
    {
        return true;
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
        return InstrumentCalibrationValue.getDataEntryColumnWidths();
    }

    public Object getSelectedRow(int row)
    {
        if(row < rows.size())
            return rows.get(row);
        else
            return null;
    }

    public boolean addRow(int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addBlankRows(int rowcount)
    {
        if(rows == null)
            rows = new ArrayList();

        for(int i = 0; i < rowcount; i++)
        {
            InstrumentCalibrationValue newRow = new InstrumentCalibrationValue();

            newRow.setDataFilePrimaryKey(parentFile.getDataFilePrimaryKey());
            newRow.setMooringID(selectedMooring.getMooringID());
            newRow.setInstrumentID(parentFile.getInstrumentID());
            newRow.setNew(true);

            rows.add(newRow);
        }

        super.fireTableDataChanged();
        return true;
    }

	@Override
	public void selectDataByFilter(int arg0, int arg1, String arg2)
	{
		// TODO Auto-generated method stub
		
	}

}
