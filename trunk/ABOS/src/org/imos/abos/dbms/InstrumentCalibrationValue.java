/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.dbms;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.wiley.core.Common;
import org.wiley.core.dbms.SystemParameters;
import org.wiley.util.DateVerifier;
import org.wiley.util.SQLWrapper;
import org.wiley.util.StringUtilities;
import org.wiley.util.TimeStampVerifier;

/**
 *
 * @author peter
 * @modified
 * 20090126 PDW Added code to insert new rows and to check if a row exists
 * 20090402 PDW More robustness checks
 * 20100510 PDW Added more methods to get data
 *
 */
public class InstrumentCalibrationValue implements Cloneable
{
    private static Logger logger = Logger.getLogger(InstrumentCalibrationValue.class.getName());

    public static String[] definedDataTypes = SystemParameters.definedDataTypes;

    private static String getDefaultSortOrder()
    {
        return " order by datafile_pk, param_code";
    }

    private Integer instrumentID = null;
    private String mooringID = null;
    private Integer datafilePrimaryKey = null;
    private String parameterCode = null;
    private String description = null;
    private String dataType = null;
    private String dataValue = null;

    protected static SQLWrapper query    = new SQLWrapper();
    private boolean isNewRow = false;

    private boolean isOuterJoinRow = false;

    private boolean isEdited = false;
    private String lastErrorMessage = null;

    private static String selectSQL = "select"
                + " instrument_id,"
                + " mooring_id,"
                + " datafile_pk,"
                + " param_code,"
                + " description,"
                + " data_type,"
                + " data_value"
                + " from instrument_calibration_values"
                ;

    private static String[] createTableSQL = new String[]
    {
    };
    private static String[] createIndexSQL = new String[]
    {

    }
    ;

    private static String insertSQL  = "insert into instrument_calibration_values"
            + "("
            + " instrument_id,"
            + " mooring_id,"
            + " datafile_pk,"
            + " param_code,"
            + " description,"
            + " data_type,"
            + " data_value"
            + ")"
            + " values "
            + "("
            + "?,?,?,?,?,?,?"
            + ")"
            ;

    private static String updateSQL = "update instrument_calibration_values "
                    + "set description = ?,"
                    + "data_type = ?,"
                    + " data_value = ?"
                    + "where datafile_pk = ? and param_code = ?";

    private static String deleteSQL = "delete from instrument_calibration_values where datafile_pk = ? and param_code = ?";

    public static String[] getCreateTableSQL()
    {
        return createTableSQL;
    }

    public static String[] getCreateIndexSQL()
    {
        return createIndexSQL;
    }

    public static String getInsertSQL()
    {
        return insertSQL;
    }

    public static String getSelectSQL()
    {
        return selectSQL;
    }

    /**
     * delete the underlying database row and all dependent data
     * NB: this should be wrapped in a transaction
     *
     * @return true if successful, otherwise false
     */
    public boolean delete()
    {
        try
        {
            PreparedStatement ps = Common.getConnection().prepareStatement(getDeleteSQL());

            boolean OK = doDelete(ps);

            ps.close();
            return OK;

        }
        catch (SQLException ex)
        {
            logger.error(ex);
        }
        return false;
    }

    public String getLastErrorMessage()
    {
        return lastErrorMessage;
    }

    /**
     * this method is used from the collections class to set values in this
     * instance of a InstrumentCalibrationValue object
     *
     * @param aColumn
     * @param value
     * @return true if the value was set without errors, otherwise false
     */
    protected boolean setDataEntryColumn(int aColumn, Object value)
    {
        if(aColumn == 0)
            return setParameterCode((String) value);
        else if(aColumn == 1)
            return setDescription((String) value);
        else if(aColumn == 2)
            return setDataType((String) value);
        else if(aColumn == 3)
            return setDataValue((String) value);
        else
            return false;
    }

    protected boolean setColumn(int aColumn, Object value)
    {
        if(aColumn == 0)
            return setInstrumentID(value);
        else if(aColumn == 1)
            return setMooringID((String) value);
        else if(aColumn == 2)
            return setDataFilePrimaryKey( value );
        else if(aColumn == 3)
            return setParameterCode((String) value);
        else if(aColumn == 4)
            return setDescription((String) value);
        else if(aColumn == 5)
            return setDataType((String) value);
        else if(aColumn == 6)
            return setDataValue((String) value);
        else
            return false;
    }

    protected boolean isNew()
    {
        return isNewRow;
    }

    /**
     * return the actual value of this parameter
     * @return
     */
    public String getParameterValue()
    {
        if(dataValue != null)
        {
            if(dataValue.trim().isEmpty())
                return null;
            else
                return dataValue.trim();
        }
        else
            return null;    // should never happen is defined as not null in dbms
    }

    /**
     * check that the data value is or can be cast to the defined data type
     * @return true if ok, otherwise false
     */
    private boolean isDataAcceptable()
    {
        if(mooringID == null  || mooringID.trim().isEmpty())
        {
            lastErrorMessage = "The patient ID must not be null or blank.";
            return false;
        }

        if(parameterCode == null  || parameterCode.trim().isEmpty())
        {
            lastErrorMessage = "The parameter code must not be null or blank.";
            return false;
        }

        if(dataType == null)
        {
            lastErrorMessage = "The data type must not be NULL.";
            return false;
        }
        if(dataValue == null)
        {
            lastErrorMessage = "The data value must not be NULL.";
            return false;
        }

        dataType = dataType.trim();
        dataValue = dataValue.trim();

        if(dataType.equalsIgnoreCase(definedDataTypes[0]))
        {
            //
            // String - return true
            //
            return true;
        }
        else if(dataType.equalsIgnoreCase(definedDataTypes[1]))
        {
            //
            // number - is it?
            //
            try
            {
                java.math.BigDecimal val = new java.math.BigDecimal(dataValue);
                return true;
            }
            catch(NumberFormatException nfe)
            {
                lastErrorMessage = "Value " + dataValue + " cannot be converted to a number.";
                return false;
            }
        }
        else if(dataType.equalsIgnoreCase(definedDataTypes[2]))
        {
            //
            // Date - is it?
            //
            DateVerifier dv = new DateVerifier();
            boolean OK = dv.parseDate(dataValue);
            if( ! OK )
            {
                lastErrorMessage = "Value " + dataValue + " cannot be converted to a Date.";
            }
            return OK;
        }
        else if(dataType.equalsIgnoreCase(definedDataTypes[3]))
        {
            //
            // Timestamp - is it?
            //
            TimeStampVerifier dv = new TimeStampVerifier();
            boolean OK = dv.parseDate(dataValue);
            if( ! OK )
            {
                lastErrorMessage = "Value " + dataValue + " cannot be converted to a Timestamp.";
            }
            return OK;
        }
        else if(dataType.equalsIgnoreCase(definedDataTypes[4]))
        {
            //
            // Boolean - is it?
            //
            if(dataValue.equalsIgnoreCase("TRUE")
                    || dataValue.equalsIgnoreCase("YES")
                    || dataValue.equalsIgnoreCase("1"))
            {
                dataValue = "TRUE";
                return true;
            }
            else if(dataValue.equalsIgnoreCase("FALSE")
                    || dataValue.equalsIgnoreCase("NO")
                    || dataValue.equalsIgnoreCase("0"))
            {
                dataValue = "FALSE";
                return true;
            }
            else
            {
                lastErrorMessage = "Value " + dataValue + " cannot be converted to a Boolean, must be either TRUE or FALSE.";
                return false;
            }
        }

        return false;
    }

    private boolean doInsert(PreparedStatement psql)
    {
        try
        {
            psql.setInt(1, instrumentID);
            psql.setString(2, mooringID);
            psql.setInt(3, datafilePrimaryKey);
            psql.setString(4, parameterCode);
            psql.setString(5, description);
            psql.setString(6, dataType);
            psql.setString(7, dataValue);

            int affectedRows = psql.executeUpdate();
            if(affectedRows == 1)
            {
                isNewRow = false;
                isOuterJoinRow = false;
                isEdited = false;
                lastErrorMessage = "";

                return true;
            }
            else
            {
                return false;
            }
        }
        catch (SQLException ex)
        {
            logger.error(ex);
            lastErrorMessage = ex.getMessage();
            return false;
        }
    }

    private boolean doDelete(PreparedStatement psql)
    {
        try
        {
            psql.setString(1, mooringID);
            psql.setString(2, parameterCode);

            int affectedRows = psql.executeUpdate();
            if(affectedRows == 1)
            {
                isNewRow = false;
                isEdited = false;
                lastErrorMessage = null;

                return true;
            }
            else
            {
                return false;
            }
        }
        catch (SQLException ex)
        {
            logger.error(ex);
            lastErrorMessage = ex.getMessage();
            return false;
        }
    }

    private String getDeleteSQL()
    {
        return deleteSQL;
    }


    private String getUpdateSQL()
    {
        return updateSQL;
    }

    public static String getDataEntryColumnName(int column)
    {
        if (column >= dataEntryColumnNames.length )
        {
            return null;
        }
        if (dataEntryColumnNames[column] != null)
        {
            return dataEntryColumnNames[column];
        }
        else
        {
            return null;
        }
    }

    public static String getColumnName(int column)
    {
        if (column >= columnNames.length )
        {
            return null;
        }
        if (columnNames[column] != null)
        {
            return columnNames[column];
        }
        else
        {
            return null;
        }
    }

    private static String[] dataEntryColumnNames = new String[]
                                     {
                                     "Param Code",
                                     "Description",
                                     "Data Type",
                                     "Data Value"
                                     };

    private static String[] columnNames = new String[]
                                     {
                                     "Patient ID",
                                     "Param Code",
                                     "Data Type",
                                     "Data Value"
                                     };

    private static Class[]dataEntryColumnTypes = {
                                        String.class,
                                        String.class,
                                        String.class,
                                        String.class
                                        };

    private static Class[]columnTypes = {
                                        String.class,
                                        String.class,
                                        String.class,
                                        String.class
                                        };

    public static Class getDataEntryColumnClass(int column)
    {
        return dataEntryColumnTypes[ column ];
    }

    public static Class getColumnClass(int column)
    {
        return columnTypes[ column ];
    }

    public static int getDataEntryColumnCount()
    {
        return dataEntryColumnNames.length;
    }

    public static int getColumnCount()
    {
        return columnNames.length;
    }

    public static int[] getDataEntryColumnWidths()
    {
        return new int[] {85, 250, 85, 150 };
    }

    public static int[] getColumnWidths()
    {
        return new int[] {85, 85, 300 };
    }

    /**
     * select all rows from the table. Not sure if this is a useful method, use with caution
     * @return if no data exists, return an empty ArrayList
     */
    public static ArrayList<InstrumentCalibrationValue> selectAll()
    {

        return doSelect( selectSQL + getDefaultSortOrder());
    }

    /**
     * select all database rows with the specified parameter code. Useful for plotting, perhaps.
     * @param code
     * @return if no data exists, return an empty ArrayList
     */
    public static ArrayList<InstrumentCalibrationValue> selectByParamCode(String code)
    {
        String sql = selectSQL
                + " where param_code = "
                + StringUtilities.quoteString(code)
                + getDefaultSortOrder()
                ;
        return doSelect(sql);
    }

    /**
     * select all data for a specified instrument and mooring. 
     * this method is required when the calibration coefficients come from more than 1 cal file
     * so as to get them all without needing multiple selects...
     * 
     * @param instrumentID
     * @param mooringID
     * @return an ArrayList of classes
     */
    public static ArrayList<InstrumentCalibrationValue> selectByInstrumentAndMooring(Integer instrumentID, String mooringID)
    {
        String sql = selectSQL
                + " where instrument_id = "
                + instrumentID
                + " and mooring_id = "
                + StringUtilities.quoteString(mooringID)
                + getDefaultSortOrder()
                ;
        return doSelect(sql);
    }
    /**
     * select all data for the specified cal file
     *
     * @param code
     * @return if no data exists, return an empty ArrayList
     */
    public static ArrayList<InstrumentCalibrationValue> selectByCalibrationFileID(Integer code)
    {
        String sql = selectSQL
                + " where datafile_pk = "
                + code
                + getDefaultSortOrder()
                ;
        return doSelect(sql);
    }

    private static ArrayList<InstrumentCalibrationValue> doSelect(String sql)
    {
        ArrayList<InstrumentCalibrationValue> items = new ArrayList();

        query.setConnection( Common.getConnection() );
        query.executeQuery( sql);

            Vector dataSet = query.getData();
            if ( ! ( dataSet == null ) )
            {
                for( int i = 0; i < dataSet.size(); i++ )
                {
                    Vector currentRow = (Vector) dataSet.elementAt( i );

                    InstrumentCalibrationValue row = new InstrumentCalibrationValue();
                    row.create( currentRow );

                    items.add(row);
                }
            }
        return items;
    }

    public boolean insert()
    {
        if ( ! isDataAcceptable() )
        {
            //
            // might be the user didn't want to fill in one of the values
            // this is ok, we just don't do an insert
            //
            if(dataValue == null && isOuterJoinRow)
                return true;
            else
                return false;
        }

        try
        {
            PreparedStatement ps = Common.getConnection().prepareStatement(getInsertSQL());

            boolean insertOK = doInsert(ps);

            ps.close();
            return insertOK;
        }
        catch (SQLException ex)
        {
            logger.error(ex);
            lastErrorMessage = ex.getMessage();
            return false;
        }
    }

    public boolean update()
    {
        //
        // see if this is a newly created row - this can only happen at the moment
        // via the editable table
        //
        if(isNewRow)
            return insert();
        //
        // check to see if the row has been edited before doing any database updates
        //
        if( ! isEdited )
        {
            return true;
        }

        if ( ! isDataAcceptable() )
            return false;

        try
        {
            PreparedStatement ps = Common.getConnection().prepareStatement(getUpdateSQL());

            boolean updateOK = doUpdate(ps);

            ps.close();

            if(updateOK)
                isEdited = false;

            return updateOK;

        }
        catch (SQLException ex)
        {
            logger.error(ex);
            lastErrorMessage = ex.getMessage();
        }
        return false;
    }

    private boolean doUpdate(PreparedStatement psql)
    {
        try
        {
            psql.setString(1, description);
            psql.setString(2, dataType);
            psql.setString(3, dataValue);
            psql.setInt(4, datafilePrimaryKey);
            psql.setString(5, parameterCode);

            int affectedRows = psql.executeUpdate();
            if(affectedRows == 1)
            {
                isNewRow = false;
                isEdited = false;
                lastErrorMessage = null;
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (SQLException ex)
        {
            logger.error(ex);
            lastErrorMessage = ex.getMessage();
            return false;
        }
    }

    private void create(Vector currentRow)
    {
        int i = 0;

        this.setInstrumentID((Number)currentRow.elementAt( i++ ) );
        this.setMooringID( (String) currentRow.elementAt( i++ ) );
        this.setDataFilePrimaryKey((Number)currentRow.elementAt( i++ ) );
        this.setParameterCode( (String) currentRow.elementAt( i++ ) );
        this.setDescription( (String) currentRow.elementAt( i++ ) );
        this.setDataType( (String) currentRow.elementAt( i++ ) );
        this.setDataValue( (String) currentRow.elementAt( i++ ) );

        isNewRow = false;
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

    public boolean setInstrumentID(Object value)
    {
        if(value == null)
        {
            instrumentID = null;
            isEdited = true;
            return true;
        }
        if(value instanceof Number)
        {
            instrumentID = ((Number) value).intValue();
            isEdited = true;
            return true;
        }

        return false;
    }

    public boolean setDescription(String string)
    {
        if(string == null || string.trim().isEmpty())
        {
            lastErrorMessage = "A null description is not an acceptable value.";
            return false;
        }
        description = string;
        isEdited = true;

        return true;
    }

    public boolean setMooringID(String string)
    {
        if(string == null || string.trim().isEmpty())
        {
            lastErrorMessage = "A null mooring ID is not an acceptable value.";
            return false;
        }
        mooringID = string.trim();
        isEdited = true;

        return true;
    }

    public boolean setParameterCode(String string)
    {
        if(string == null || string.trim().isEmpty())
        {
            lastErrorMessage = "A null parameter code is not an acceptable value.";
            return false;
        }
        parameterCode = string.trim();
        isEdited = true;

        return true;
    }

    protected void setNew(boolean b)
    {
        isNewRow = b;
    }

    protected void setOuterJoinRow(boolean b)
    {
        isOuterJoinRow = b;
    }

    public Object getDataEntryColumn( int col )
    {
        if( col == 0 )
            return parameterCode;
        else if ( col == 1 )
            return description;
        else if ( col == 2 )
            return dataType;
        else if ( col == 3 )
            return dataValue;
        else
            return null;
    }

    public Object getColumn( int col )
    {
        if( col == 0 )
            return instrumentID;
        else if( col == 1 )
            return mooringID;
        else if ( col == 2 )
            return datafilePrimaryKey;
        else if ( col == 3 )
            return parameterCode;
        else if ( col == 4 )
            return description;
        else if ( col == 5 )
            return dataType;
        else if ( col == 6 )
            return dataValue;
        else
            return null;
    }

    @Override
    public Object clone()
    throws CloneNotSupportedException
    {
        return super.clone();
    }

    private boolean setParameterDescription(String s)
    {
        description = s;
        return true;
    }

    public boolean setDataType(String string)
    {
        if(string == null || string.trim().isEmpty())
        {
            lastErrorMessage = "A null data type is not an acceptable value.";
            return false;
        }

        dataType = string;
        isEdited = true;

        return true;
    }

    /**
     * return the current data value of this object
     * @return
     */
    public String getDataValue()
    {
        return dataValue;
    }

    public boolean setDataValue(Object o)
    {
        if(o == null )
        {
            lastErrorMessage = "A null data value is not an acceptable value.";
            return false;
        }

        dataValue = o.toString();
        isEdited = true;

        return true;
    }

    public boolean setDataValue(Timestamp o)
    {
        if(o == null )
        {
            lastErrorMessage = "A null data value is not an acceptable value.";
            return false;
        }

        dataValue = o.toString();
        isEdited = true;

        return true;
    }

    public boolean setDataValue(String string)
    {
        if(string == null || string.trim().isEmpty())
        {
            lastErrorMessage = "A null data value is not an acceptable value.";
            return false;
        }

        dataValue = string;
        isEdited = true;

        return true;
    }

    protected void setDataValueToNull()
    {
        dataValue = null;
    }

    /**
     * get the string containing the parameter code
     * @return
     */
    public String getParameterCode()
    {
        return parameterCode;
    }

    /**
     * get the data type of the value - will be one of the defined data types
     * @return
     */
    public String getDataType()
    {
        return dataType;
    }
}

