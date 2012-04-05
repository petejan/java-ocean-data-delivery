/*
 * Neonatal Screening Software Project
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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.wiley.LabMaster.Common;
import org.wiley.util.SQLWrapper;

public class InstrumentDataFileHeader  implements Cloneable
{

    private boolean isNew = false;
    private static Logger logger = Logger.getLogger(InstrumentDataFileHeader.class.getName());

    private String message = "";

    public static int[] getColumnWidths()
    {
        return new int[]
        {
            60,
            60,
            240
        };
    }

    public static int[] getLookupColumnWidths()
    {
        return getColumnWidths();
    }

    public static String getDefaultSortOrder()
    {
        return " order by line";
    }

    public static Class getColumnClass(int column)
    {
        return columnClasses[column];
    }

    public static Class getLookupColumnClass(int column)
    {
        return columnClasses[column];
    }

    public static int getColumnCount()
    {
        return columnNames.length;
    }

    public static int getLookupColumnCount()
    {
        return columnNames.length;
    }

    public static String getColumnName(int column)
    {
        return columnNames[column];
    }

    public static String getLookupColumnName(int column)
    {
        return columnNames[column];
    }

    private boolean isEdited = false;
    protected static SQLWrapper query    = new SQLWrapper();


    private Integer datafilePrimaryKey;
    private Integer lineNumber;
    private String lineText;
    
    private ArrayList<String> datafileHeaders;

    private static String[] columnNames = new String[]
    {
        "PK",
        "Line#",
        "Text"
    };

    
    private static Class[] columnClasses = new Class[]
    {
        Integer.class,
        Integer.class,
        String.class
    };

    private static String selectSQL = "select"
                + " datafile_pk,"
                + " line,"
                + " line_text"
                + " from instrument_datafile_headers"
                ;


    private static String insertSQL  = "insert into instrument_datafile_headers"
            + "("
            + " datafile_pk,"
            + " line,"
            + " line_text"
            + ")"
            + " values "
            + "("
            + "?,?,?"
            + ")"
            ;


    public static String getInsertSQL()
    {
        return insertSQL;
    }

    public static String getUpdateSQL()
    {
        return "update instrument_datafile_headers set "
                + " line_text = ?"
                + " where datafile_pk = ? and line = ?";
    }

    public static String getDeleteSQL()
    {
        return "delete from instrument_datafile_headers"
            + " where datafile_pk = ? and line = ?"
            ;
    }

    public static String getDeleteAllSQL()
    {
        return "delete from instrument_datafile_headers"
            + " where datafile_pk = ?"
            ;
    }

    public static String getSelectSQL()
    {
        return selectSQL;
    }
    public static String getTableName()
    {
        return "InstrumentDataFileHeader";
    }

    public String getLastErrorMessage()
    {
        return message;
    }
    /**
     * delete the underlying database row and all dependent data
     * NB: this should be wrapped in a transEquipmentInstrumentDataFileHeader
     *
     * @return true if successful, otherwise false
     */
    public boolean delete()
    {
        try
        {
            PreparedStatement ps = Common.getConnection().prepareStatement(getDeleteSQL());

            boolean OK = doDelete(ps, true);

            ps.close();
            return OK;

        }
        catch (SQLException ex)
        {
            logger.error(ex);
        }
        return false;
    }

    public static boolean deleteAllForPK(Integer pk)
    {
        try
        {
            PreparedStatement psql = Common.getConnection().prepareStatement(getDeleteAllSQL());

            psql.setInt(1, pk);

            int affectedRows = psql.executeUpdate();

            psql.close();
        }
        catch (SQLException ex)
        {
            logger.error(ex);
        }
        return false;
    }

    public Integer getLineNumber()
    {
        return lineNumber;
    }


    private boolean doDelete(PreparedStatement psql, boolean isMaster)
    {
        try
        {
            psql.setInt(1, datafilePrimaryKey);
            psql.setInt(2, lineNumber);

            int affectedRows = psql.executeUpdate();
            if(affectedRows == 1)
            {
                isNew = false;
                isEdited = false;
                message = "";

                return true;
            }
            else
            {
                if(isMaster)
                    Common.showMessage("Delete Failure", "Delete of row failed.");
                return false;
            }
        }
        catch (SQLException ex)
        {
            logger.error(ex);
            message = ex.getMessage();
            return false;
        }
    }

    protected Object getColumn(int columnIndex)
    {
        if(columnIndex == 0)
            return datafilePrimaryKey;
        if(columnIndex == 1)
            return lineNumber;
        else if(columnIndex == 2)
            return lineText;
        else
            return null;
    }

    public boolean setDataFilePrimaryKey(Object value)
    {
        if(value == null)
        {
            datafilePrimaryKey = null;
            isEdited = true;
            return true;
        }
        if(value instanceof Number)
        {
            datafilePrimaryKey = ((Number) value).intValue();
            isEdited = true;
            return true;
        }

        return false;
    }

    public Integer getDataFilePrimaryKey()
    {
        return datafilePrimaryKey;
    }

    public boolean setLineNumber(Object value)
    {
        if(value == null)
        {
            lineNumber = null;
            isEdited = true;
            return true;
        }
        if(value instanceof Number)
        {
            lineNumber = ((Number) value).intValue();
            isEdited = true;
            return true;
        }

        return false;
    }

    

    public boolean setLineText(Object value)
    {
        if(value == null)
        {
            lineText = null;
            isEdited = true;
            return true;
        }
        if(value instanceof String)
        {
            lineText = ((String)value).trim();
            isEdited = true;
            return true;
        }

        return false;
    }

    public boolean isNew()
    {
         return isNew;
    }

    protected boolean setColumn(int aColumn, Object value)
    {
        if(aColumn == 0)
            return setDataFilePrimaryKey(value);
        if(aColumn == 1)
            return setLineNumber(value);
        else if(aColumn == 2)
            return setLineText(value);
        else
            return false;
    }

    protected void setNew(boolean b)
    {
        isNew = b;
    }

    public boolean update()
    {
        if( ! checkData() )
            return false;
        //
        // see if this is a newly created row - this can only happen at the moment
        // via the editable table
        //
        if(isNew)
            return insert();
        //
        // check to see if the row has been edited before doing any database updates
        //
        if( ! isEdited )
        {
            return true;
        }
        try
        {
            PreparedStatement ps = Common.getConnection().prepareStatement(getUpdateSQL());

            boolean updateOK = doUpdate(ps, true);

            if(updateOK)
            {
                isEdited = false;
                message = "";
            }
            ps.close();


            return updateOK;

        }
        catch (SQLException ex)
        {
            logger.error(ex);
            message = ex.getMessage();
        }
        return false;
    }

    private boolean doUpdate(PreparedStatement psql, boolean isMaster)
    {
        try
        {
            int i = 1;
   
            psql.setString(i++, lineText);
            
            psql.setInt(i++, datafilePrimaryKey);
            psql.setInt(i++, lineNumber);

            int affectedRows = psql.executeUpdate();
            if(affectedRows == 1)
            {
                isNew = false;
                isEdited = false;
                message = "";
                return true;
            }
            else
            {
                if(isMaster)
                    Common.showMessage("Update Failure", "Update of row failed.");
                return false;
            }
        }
        catch (SQLException ex)
        {
            logger.error(ex);
            message = ex.getMessage();
            return false;
        }
    }

    private void create(Vector currentRow)
    {
        int i = 0;

        setDataFilePrimaryKey( (Number) currentRow.elementAt(i++) );
        setLineNumber( (Number) currentRow.elementAt(i++) );
        setLineText((String) currentRow.elementAt(i++));

        isNew = false;
        isEdited = false;
    }

    public String getLineText()
    {
        if(lineText != null)
        {
            return lineText.trim();
        }
        else
            return null;
    }


    private static ArrayList<InstrumentDataFileHeader> doSelect(String sql)
    {
        ArrayList<InstrumentDataFileHeader> items = new ArrayList();

        query.setConnection( Common.getConnection() );
        query.executeQuery( sql);

        Vector dataSet = query.getData();
        if ( ! ( dataSet == null ) )
        {
            for( int i = 0; i < dataSet.size(); i++ )
            {
                Vector currentRow = (Vector) dataSet.elementAt( i );

                InstrumentDataFileHeader row = new InstrumentDataFileHeader();
                row.create( currentRow );

                items.add(row);
            }
        }

        if(items.size() > 0)
            return items;
        else
            return null;
    }

    /**
     * select all rows for the instrument data file
     * @return
     */
    public static ArrayList<InstrumentDataFileHeader> selectHeaderDataForFile(Integer ID)
    {
        return doSelect( selectSQL
                        + " where datafile_pk = "
                        + ID
                        + getDefaultSortOrder());
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException
    {
        return super.clone();
    }

    private boolean checkData()
    {
        if(getLineNumber() == null)
        {
            message = "A blank or null line number is not permitted.";
            return false;
        }

        if(getLineText() == null)
        {
            message = "A blank or null text line is not permitted.";
            return false;
        }
        

        return true;
    }

    public boolean insert()
    {
        if( ! checkData() )
            return false;

        try
        {
            PreparedStatement ps = Common.getConnection().prepareStatement(getInsertSQL());

            boolean insertOK = doInsert(ps, true);
            if(insertOK)
            {
                message = "";
            }
            ps.close();
            return insertOK;
        }
        catch (SQLException ex)
        {
            logger.error(ex);
            message = ex.getMessage();
            return false;
        }
    }

    private boolean doInsert(PreparedStatement psql, boolean isMaster)
    {
        try
        {
            int i = 1;

            psql.setInt(i++, datafilePrimaryKey);

            psql.setInt(i++, lineNumber);
            psql.setString(i++, lineText);
            
            int affectedRows = psql.executeUpdate();
            if(affectedRows == 1)
            {
                isNew = false;
                isEdited = false;
                message = "";

                return true;
            }
            else
            {
                if(isMaster)
                    Common.showMessage("Insert Failure", "Insert of row failed.");
                return false;
            }
        }
        catch (SQLException ex)
        {
            logger.error(ex);
            message = ex.getMessage();
            return false;
        }
    }
  

    public void setFileHeaders(ArrayList<String> headers)
    {
        datafileHeaders = headers;
    }

    /**
     * write any header data to the database
     */
    protected void insertOrUpdateHeaders()
    {
        //
        // clean out any existing data
        //
        InstrumentDataFileHeader.deleteAllForPK(datafilePrimaryKey);

        InstrumentDataFileHeader idh = new InstrumentDataFileHeader();
        idh.setDataFilePrimaryKey(datafilePrimaryKey);

        for(int i = 0; i < datafileHeaders.size(); i++)
        {
            idh.setLineNumber(i);
            idh.setLineText(datafileHeaders.get(i));
            idh.insert();
        }
    }

}
