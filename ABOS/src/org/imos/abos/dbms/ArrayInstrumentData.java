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
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.wiley.LabMaster.Common;
import org.wiley.util.SQLWrapper;
import org.wiley.util.StringUtilities;

public class ArrayInstrumentData  implements Cloneable
{

    public ArrayInstrumentData()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }

    public static boolean deleteDataForFile(Integer instrumentDataFileID)
    {
        String deleteSQL = "delete from array_instrument_data"
                + " where instrument_id = "
                + instrumentDataFileID
                ;

        boolean success = Common.executeSQL(deleteSQL);
        return success;
    }

    boolean isNew = false;
    static Logger logger = Logger.getLogger(ArrayInstrumentData.class.getName());

    String message = "";

    public static int[] getColumnWidths()
    {
        return new int[]
        {
            60,
            230
        };
    }
    public static String getDefaultSortOrder()
    {
        return "";
    }

    public static Class getColumnClass(int column)
    {
        return columnClasses[column];
    }

    public static int getColumnCount()
    {
        return columnNames.length;
    }

    public static String getColumnName(int column)
    {
        return columnNames[column];
    }

    boolean isEdited = false;
    protected static SQLWrapper query    = new SQLWrapper();

    Integer sourceFileID;
    Integer instrumentID;
    String mooringID;
    Timestamp dataTimestamp;
    Double latitude;
    Double longitude;
    Double depth;
    String parameterCode;
    private Double[] parameterValue;
    String qualityCode;

    private static String[] columnNames = new String[]
    {
        "Source File ID",
        "Instrument ID",
        "Mooring ID",
        "Timestamp",
        "Latitude",
        "Longitude",
        "Depth",
        "Param Code",
        "Value",
        "Qual Code"
    };

    private static Class[] columnClasses = new Class[]
    {
        Integer.class,
        Integer.class,
        String.class,
        Timestamp.class,
        Double.class,
        Double.class,
        Double.class,
        String.class,
        BigDecimal[].class,
        String.class
    };

    private static String selectSQL = "select"
                + " source_file_id,"
                + " instrument_id,"
                + " mooring_id,"
                + " data_timestamp,"
                + " latitude,"
                + " longitude,"
                + " depth,"
                + " parameter_code,"
                + " parameter_value,"
                + " quality_code"
                + " from array_instrument_data"
                ;



    private static String insertSQL  = "insert into array_instrument_data"
            + "("
            + " source_file_id,"
            + " instrument_id,"
            + " mooring_id,"
            + " data_timestamp,"
            + " latitude,"
            + " longitude,"
            + " depth,"
            + " parameter_code,"
            + " parameter_value,"
            + " quality_code"
            + ")"
            + " values "
            + "("
            + "?,?,?,?,?,?,?,?,?,?"
            + ")"
            ;

    public static String getInsertSQL()
    {
        return insertSQL;
    }

    public static String getUpdateSQL()
    {
        return "update array_instrument_data set "
                + " source_file_id = ?,"
                //+ " instrument_id = ?,"
                + " mooring_id = ?,"
                //+ " data_timestamp = ?,"
                + " latitude = ?,"
                + " longitude = ?,"
                + " depth = ?,"
                //+ " parameter_code = ?,"
                + " parameter_value = ?,"
                + " quality_code = ?"
                + " where instrument_id = ?"
                + " and data_timestamp = ?"
                + " and parameter_code = ?"
                ;
    }

    public static String getDeleteSQL()
    {
        return "delete from array_instrument_data"
                + " where instrument_id = ?"
                + " and data_timestamp = ?"
                + " and parameter_code = ?"
                ;
    }

    public static String getSelectSQL()
    {
        return selectSQL;
    }

    public static String getTableName()
    {
        return "array_instrument_data";
    }

    public String getLastErrorMessage()
    {
        return message;
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

            boolean OK = doDelete(ps, true);

            if(OK)
            {
                message = "";
            }
            ps.close();
            return OK;

        }
        catch (SQLException ex)
        {
            logger.error(ex);
        }
        return false;
    }

    private boolean doDelete(PreparedStatement psql, boolean isMaster)
    {
        try
        {
            int i = 1;
            
            psql.setInt(i++, instrumentID);
            psql.setTimestamp(i++, dataTimestamp);
            psql.setString(i++, parameterCode);

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

    public Object getColumn(int columnIndex)
    {

        if(columnIndex == 0)
            return sourceFileID;
        else if(columnIndex == 1)
            return instrumentID;
        else if(columnIndex == 2)
            return mooringID;
        else if(columnIndex == 3)
            return dataTimestamp;
        else if(columnIndex == 4)
            return latitude;
        else if(columnIndex == 5)
            return longitude;
        else if(columnIndex == 6)
            return depth;
        else if(columnIndex == 7)
            return parameterCode;
        else if(columnIndex == 8)
            return parameterValue;
        else if(columnIndex == 9)
            return qualityCode;
        else
            return null;
    }

    protected boolean setColumn(int aColumn, Object value)
    {
        
        if(aColumn == 0)
            return setSourceFileID(value);
        else if(aColumn == 1)
            return setInstrumentID(value);
        else if(aColumn == 2)
            return setMooringID((String)value);
        else if(aColumn == 3)
            return setDataTimestamp(value);
        else if(aColumn == 4)
            return setLatitude(value);
        else if(aColumn == 5)
            return setLongitude(value);
        else if(aColumn == 6)
            return setDepth(value);
        else if(aColumn == 7)
            return setParameterCode((String)value);
        else if(aColumn == 8)
            return false;//setParameterValue(value);
        else if(aColumn == 9)
            return setQualityCode(value);
        else
            return false;
    }

    public boolean isNew()
    {
         return isNew;
    }

    public boolean setQualityCode(Object value)
    {
        if(value == null)
        {
            qualityCode = "Y";
            isEdited = true;
            return true;
        }
        if(value instanceof String)
        {
            setQualityCode((String) value);
            isEdited = true;
            return true;
        }
        
        return false;
    }

    public boolean setQualityCode(String value)
    {
        if(value == null)
        {
            qualityCode = null;
            isEdited = true;
            return true;
        }
        if(value.trim().isEmpty())
        {
            qualityCode = null;
            isEdited = true;
            return true;
        }
        else
        {
            qualityCode = value.trim();

            isEdited = true;
            return true;
        }
    }

    public String getQualityCode()
    {
        return qualityCode;
    }

    void setNew(boolean b)
    {
        isNew = b;
    }

    public boolean update()
    {
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

        if( ! validateFields())
            return false;

        try
        {
            PreparedStatement ps = Common.getConnection().prepareStatement(getUpdateSQL());

            boolean updateOK = doUpdate(ps);

            ps.close();

            isEdited = false;
            return updateOK;

        }
        catch (SQLException ex)
        {
            logger.error(ex);
            message = ex.getMessage();
        }
        return false;
    }

    private boolean doUpdate(PreparedStatement psql)
    {
        try
        {
            int i = 1;

            psql.setInt(i++, sourceFileID);
            
            psql.setString(i++, mooringID);
            
            psql.setDouble(i++, latitude);
            psql.setDouble(i++, longitude);
            psql.setDouble(i++, depth);
            
            Array array = Common.getConnection().createArrayOf("Numeric", parameterValue);
            psql.setArray(i++, array);
            
            psql.setString(i++, qualityCode);

            psql.setInt(i++, instrumentID);
            psql.setTimestamp(i++, dataTimestamp);
            psql.setString(i++, parameterCode);

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

        setSourceFileID((Number)currentRow.elementAt(i++));
        setInstrumentID((Number)currentRow.elementAt(i++));
        setMooringID( (String) currentRow.elementAt(i++) );
        setDataTimestamp( (Timestamp) currentRow.elementAt(i++) );
        setLatitude( (Number) currentRow.elementAt(i++) );
        setLongitude( (Number) currentRow.elementAt(i++) );
        setDepth( (Number) currentRow.elementAt(i++) );
        setParameterCode((String) currentRow.elementAt(i++));
        Array a = (Array)currentRow.elementAt(i++);
        try
        {
            logger.debug("returned " + a.getBaseTypeName() + " " + a);
            setParameterValue( (BigDecimal[])a.getArray() );
        }
        catch (SQLException ex)
        {
            logger.error(ex);
        }
        setQualityCode((String) currentRow.elementAt(i++));

        isNew = false;
        isEdited = false;
    }

    public boolean setSourceFileID(Object value)
    {
        if(value == null)
        {
            sourceFileID = null;
            isEdited = true;
            return true;
        }
        if(value instanceof Number)
        {
            sourceFileID = ((Number) value).intValue();
            isEdited = true;
            return true;
        }

        return false;
    }

    public Integer getSourceFileID()
    {
        return sourceFileID;
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

    public Integer getInstrumentID()
    {
        return instrumentID;
    }

    public boolean setMooringID(String string)
    {
        if(string != null)
        {
            mooringID = string;
            isEdited = true;
            return true;
        }
        else
            return false;
    }
    
    public String getMooringID()
    {
        return mooringID;
    }

    public boolean setDataTimestamp(Object date)
    {
        if(date == null)
        {
            dataTimestamp = null;
            return false;
        }
        if(date instanceof Timestamp )
            return setDataTimestamp((Timestamp) date);

        if(date instanceof Date )
            return setDataTimestamp(new Timestamp(((Date)date).getTime()));

        return true;
    }

    public boolean setDataTimestamp(Timestamp date)
    {
        dataTimestamp = date;
        return true;
    }

    public Timestamp getDataTimestamp()
    {
        return dataTimestamp;
    }

    public boolean setLatitude(Object value)
    {
        if(value == null)
        {
            latitude = null;
            isEdited = true;
            return true;
        }
        if(value instanceof Number)
        {
            latitude = ((Number) value).doubleValue();
            isEdited = true;
            return true;
        }

        return false;
    }

    public Double getLatitude()
    {
        return latitude;
    }

    public boolean setLongitude(Object value)
    {
        if(value == null)
        {
            longitude = null;
            isEdited = true;
            return true;
        }
        if(value instanceof Number)
        {
            longitude = ((Number) value).doubleValue();
            isEdited = true;
            return true;
        }

        return false;
    }

    public Double getLongitude()
    {
        return longitude;
    }

    public boolean setDepth(Object value)
    {
        if(value == null)
        {
            depth = null;
            isEdited = true;
            return true;
        }
        if(value instanceof Number)
        {
            depth = ((Number) value).doubleValue();
            isEdited = true;
            return true;
        }

        return false;
    }

    public Double getDepth()
    {
        return depth;
    }

    public boolean setParameterCode(String string)
    {
        if(string == null)
        {
            parameterCode = null;
            return false;
        }
        if(string.trim().isEmpty())
        {
            parameterCode = null;
            return false;
        }
        parameterCode = string.trim();
        isEdited = true;

        return false;
    }

    public String getParameterCode()
    {
        if(parameterCode == null)
            return null;
        else
            return parameterCode.trim();
    }

    public boolean setParameterValue(Object[] value)
    {
        if(value == null)
        {
            parameterValue = null;
            isEdited = true;
            return true;
        }
        if(value instanceof Double[])
        {
            parameterValue = ((Double[]) value);
            isEdited = true;
            return true;
        }

        return false;
    }

    public Double[] getParameterValue()
    {
        return parameterValue;
    }

    
    private static ArrayList<ArrayInstrumentData> doSelect(String sql)
    {
        ArrayList<ArrayInstrumentData> items = new ArrayList();

        query.setConnection( Common.getConnection() );
        query.executeQuery( sql);

            Vector dataSet = query.getData();
            
            System.out.println("ArrayInstrumentData::doSelect " + sql + " " + dataSet.size());
            
            if ( ! ( dataSet == null ) )
            {
                for( int i = 0; i < dataSet.size(); i++ )
                {
                    Vector currentRow = (Vector) dataSet.elementAt( i );

                    ArrayInstrumentData row = new ArrayInstrumentData();
                    row.create( currentRow );

                    items.add(row);
                }
            }

        return items;
    }
    /**
     * get all the rows in the database
     * NB: This is HIGHLY undesirable for this table
     * @return
     */
    public static ArrayList<ArrayInstrumentData> selectAll()
    {
        return doSelect( selectSQL
                        + getDefaultSortOrder());
    }

    public static ArrayList<ArrayInstrumentData> selectByInstrument(Integer ID)
    {
        return doSelect( selectSQL
                        + " where instrument_id = "
                        + ID
                        + getDefaultSortOrder());
    }

    public static ArrayList<ArrayInstrumentData> selectBySourceFile(Integer ID)
    {
        return doSelect( selectSQL
                        + " where source_file_id = "
                        + ID
                        + getDefaultSortOrder());
    }

    public static ArrayList<ArrayInstrumentData> selectByMooring(String ID)
    {
        return doSelect( selectSQL
                        + " where mooring_id = "
                        + StringUtilities.quoteString(ID)
                        + getDefaultSortOrder());
    }

    public static ArrayList<ArrayInstrumentData> selectByParameterCode(String ID)
    {
        return doSelect( selectSQL
                        + " where parameter_code = "
                        + StringUtilities.quoteString(ID)
                        + getDefaultSortOrder());
    }

    public static ArrayList<ArrayInstrumentData> selectByParameterCodeAndMooring(String param, String mooring)
    {
        return doSelect( selectSQL
                        + " where mooring_id = "
                        + StringUtilities.quoteString(mooring)
                        + " and parameter_code = "
                        + StringUtilities.quoteString(param)
                        + getDefaultSortOrder());
    }

    /**
     * select data at 1 hour intervals for the specified instrument, mooring and parameter code
     *
     * @param insID
     * @param moorID
     * @param paramCode
     * @return an ArrayList of RawInstrumentData
     */
    public static ArrayList<ArrayInstrumentData> selectHourDataForInstrumentAndMooringAndParameter(Integer insID, String moorID, String paramCode)
    {
        String sql = "select distinct on (date_trunc('hour',data_timestamp) )"
                    + " source_file_id,"
                    + " instrument_id,"
                    + " mooring_id,"
                    + " date_trunc('hour',data_timestamp) as data_timestamp,"
                    + " latitude,"
                    + " longitude,"
                    + " depth,"
                    + " parameter_code,"
                    + " parameter_value,"
                    + " quality_code"
                    + " from array_instrument_data"
                    ;

        return doSelect( sql
                        + " where instrument_id = "
                        + insID
                        + " and mooring_id = "
                        + StringUtilities.quoteString(moorID)
                        + " and parameter_code = "
                        + StringUtilities.quoteString(paramCode)
                        + getDefaultSortOrder());
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException
    {
        return super.clone();
    }

    public boolean insert()
    {
        if( ! validateFields())
            return false;

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
            message = ex.getMessage();
            return false;
        }
    }

    private boolean validateFields()
    {
        if(mooringID == null)
        {
            message = "You must enter a mooring ID.";
            return false;
        }
        if(mooringID.trim().isEmpty())
        {
            message = "You must enter a mooring ID.";
            return false;
        }
        if(mooringID.trim().length() > 20)
        {
            message = "Codes must be no more than 20 characters in length.";
            return false;
        }

        if(parameterCode != null && parameterCode.trim().length() > 80)
        {
            message = "Codes must be no more than 20 characters in length.";
            return false;
        }

        message = "";
        return true;
    }

    protected boolean doInsert(PreparedStatement psql)
    {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        
        try
        {
            int i = 1;

            psql.setInt(i++, sourceFileID);
            psql.setInt(i++, instrumentID);
            psql.setString(i++, mooringID);
            psql.setTimestamp(i++, dataTimestamp, cal);
            psql.setDouble(i++, latitude);
            psql.setDouble(i++, longitude);
            psql.setDouble(i++, depth);
            psql.setString(i++, parameterCode);
 
            Array array = Common.getConnection().createArrayOf("numeric", parameterValue);
            psql.setArray(i++, array);

            psql.setString(i++, qualityCode);

            //System.out.println("ArrayInstrumentData::doInsert " + psql);
            
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
                return false;
            }
        }
        catch (SQLException ex)
        {
            //ex.printStackTrace();
            logger.error(ex);
            message = ex.getMessage();
            return false;
        }
    }
    
    public static void main(String[] args)
    {
        String $HOME = System.getProperty("user.home");

        if(args.length == 0)
        {
            PropertyConfigurator.configure("log4j.properties");
            org.wiley.core.Common.build("ABOS.properties");
        }
        
        ArrayInstrumentData d = new ArrayInstrumentData();
        
        ArrayList<ArrayInstrumentData> al =d.selectAll();
        
        System.out.println("selected " + al.size());
        
        for(ArrayInstrumentData s : al)
        {
            System.out.print(s.getDataTimestamp() + " data (");
            for(Double sd : s.getParameterValue())
            {
                System.out.print(" " + sd);
            }
            System.out.println(")");
        }
    }
}

