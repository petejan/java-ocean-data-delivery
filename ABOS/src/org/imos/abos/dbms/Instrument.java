/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - 2012
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.dbms;

/**
 *
 * @author peter
 */
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.wiley.LabMaster.Common;
import org.wiley.util.SQLWrapper;
import org.wiley.util.StringUtilities;

public class Instrument  implements Cloneable
{

    private boolean isNew = false;
    private static Logger logger = Logger.getLogger(Instrument.class.getName());

    private String message = "";

    public static int[] getColumnWidths()
    {
        return new int[]
        {
            60,
            120,
            60,
            60,
            60,
            60,
            60,
            80,
            80
        };
    }

    public static int[] getLookupColumnWidths()
    {
        return new int[]
        {
            60,
            80,
            120,
            60,
            60,
            60
        };
    }

    public static String getDefaultSortOrder()
    {
        return " order by make, model, serial_number";
    }

    public static Class getColumnClass(int column)
    {
        return columnClasses[column];
    }

    public static Class getLookupColumnClass(int column)
    {
        return lookupColumnClasses[column];
    }

    public static int getColumnCount()
    {
        return columnNames.length;
    }

    public static int getLookupColumnCount()
    {
        return lookupColumnNames.length;
    }

    public static String getColumnName(int column)
    {
        return columnNames[column];
    }

    public static String getLookupColumnName(int column)
    {
        return lookupColumnNames[column];
    }

    private boolean isEdited = false;
    protected static SQLWrapper query    = new SQLWrapper();

    private Integer instrumentID;
    
    private String make;
    private String model;
    private String serialNumber;
    private String assetCode;

    private Timestamp dateAcquired;
    private Timestamp dateDisposed;

    private String instrumentType;
    private String instrumentStatus;

    private static String[] columnNames = new String[]
    {
        "Instrument ID",
        "Make",
        "Model",
        "S/Num",
        "Asset Code",
        "Acquired",
        "Disposed",
        "Type",
        "Status"
    };

    @Override
    public String toString()
    {
        return make + "-" + model + "-" + serialNumber;
    }
    
    private static String[] lookupColumnNames = new String[]
    {
        "Instrument ID",
        "Type",
        "Make",
        "Model",
        "S/Num",
        "Asset Code"
    };

    private static Class[] columnClasses = new Class[]
    {
        Integer.class,
        String.class,
        String.class,
        String.class,
        String.class,
        Timestamp.class,
        Timestamp.class,
        String.class,
        String.class
    };

    private static Class[] lookupColumnClasses = new Class[]
    {
        Integer.class,
        String.class,
        String.class,
        String.class,
        String.class,
        String.class
    };

    private static String selectSQL = "select"
                + " instrument_id,"
                + " make,"
                + " model,"
                + " serial_number,"
                + " asset_code,"
                + " date_acquired,"
                + " date_disposed,"
                + " instrument_type,"
                + " instrument_status"
                + " from instrument"
                ;

    
    private static String insertSQL  = "insert into instrument"
            + "("
            + " instrument_id,"
            + " make,"
            + " model,"
            + " serial_number,"
            + " asset_code,"
            + " date_acquired,"
            + " date_disposed,"
            + " instrument_type,"
            + " instrument_status"
            + ")"
            + " values "
            + "("
            + "?,?,?,?,?,?,?,?,?"
            + ")"
            ;
    
    
    public static String getInsertSQL()
    {
        return insertSQL;
    }

    public static String getUpdateSQL()
    {
        return "update instrument set "
                + " make = ?,"
                + " model = ?,"
                + " serial_number = ?,"
                + " asset_code = ?,"
                + " date_acquired = ?,"
                + " date_disposed = ?,"
                + " instrument_type = ?,"
                + " instrument_status = ?"
                + " where instrument_id = ?";
    }

    public static String getDeleteSQL()
    {
        return "delete from instrument"
            + " where instrument_id = ?"
            ;
    }

    public static String getSelectSQL()
    {
        return selectSQL;
    }
    public static String getTableName()
    {
        return "Instrument";
    }

    /**
     * select a single Instrument using its unique ID
     * @param id
     * @return a Instrument object, or null if not found
     */
    public static Instrument selectByInstrumentID(Integer id)
    {
        String sql = selectSQL
                + " where instrument_id = "
                + id
                ;

        query.setConnection( Common.getConnection() );
        query.executeQuery( sql );

            Vector dataSet = query.getData();
            if ( ! ( dataSet == null ) )
            {
                for( int i = 0; i < dataSet.size(); i++ )
                {
                    Vector currentRow = (Vector) dataSet.elementAt( i );

                    Instrument row = new Instrument();
                    row.create( currentRow );
                    return row;
                }
            }

        return null;
    }

    public String getLastErrorMessage()
    {
        return message;
    }
    /**
     * delete the underlying database row and all dependent data
     * NB: this should be wrapped in a transEquipmentInstrument
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

    public Integer getInstrumentID()
    {
        return instrumentID;
    }
    

    private boolean doDelete(PreparedStatement psql, boolean isMaster)
    {
        try
        {
            psql.setInt(1, instrumentID);

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

    Object getColumn(int columnIndex)
    {
        if(columnIndex == 0)
            return instrumentID;
        else if(columnIndex == 1)
            return make;
        else if(columnIndex == 2)
            return model;
        else if(columnIndex == 3)
            return serialNumber;
        else if(columnIndex == 4)
            return assetCode;
        else if(columnIndex == 5)
            return dateAcquired;
        else if(columnIndex == 6)
            return dateDisposed;
        else if(columnIndex == 7)
            return instrumentType;
        else if(columnIndex == 8)
            return instrumentStatus;
        else
            return null;
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

    public boolean setInstrumentType(Object value)
    {
        if(value == null)
        {
            instrumentType = null;
            isEdited = true;
            return true;
        }
        if(value instanceof String)
        {
            instrumentType = ((String)value).trim();
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
            return setInstrumentID(value);
        else if(aColumn == 1)
            return setMake(value);
        else if(aColumn == 2)
            return setModel(value);
        else if(aColumn == 3)
            return setSerialNumber(value);
        else if(aColumn == 4)
            return setAssetCode(value);
        else if(aColumn == 5)
            return setAcquisitionDate(value);
        else if(aColumn == 6)
            return setDisposalDate(value);
        else if(aColumn == 7)
            return setInstrumentType(value);
        else if(aColumn == 8)
            return setInstrumentStatus(value);
        else
            return false;
    }

    /**
     * fetch the next value from a sequence used for primary keys
     * @return a new sequence value
     */
    public static Number getNextSequenceNumber()
    {
        Number nextVal = null;
        String SQL = "select nextval('instrument_sequence') ";
        logger.trace("Executing " + SQL);

        query.setConnection( Common.getConnection() );
        query.executeQuery( SQL );

        Vector dataSet = query.getData();
        if ( ! ( dataSet == null ) )
        {
            for( int i = 0; i < dataSet.size(); i++ )
            {
                Vector currentRow = (Vector) dataSet.elementAt( i );
                nextVal = (Number) currentRow.elementAt(0);
            }
        }
        return nextVal;
    }

    protected void setNew(boolean b)
    {
        isNew = b;

        if(isNew)
            setInstrumentID( Instrument.getNextSequenceNumber() );
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
                if (Common.isDataMirroringEnabled())
                {
                    PreparedStatement mirrorSQL = Common.getMirrorConnection().prepareStatement(getInsertSQL());
                    boolean mirrorDelete = doUpdate(mirrorSQL, false);

                    mirrorSQL.close();
                }
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
            psql.setString(i++, make);
            psql.setString(i++, model);
            psql.setString(i++, serialNumber);
            psql.setString(i++, assetCode);

            psql.setTimestamp(i++, dateAcquired);

            if(dateDisposed != null)
                psql.setTimestamp(i++, dateDisposed);
            else
                psql.setNull(i++, java.sql.Types.TIMESTAMP);

            psql.setString(i++, instrumentType);
            psql.setString(i++, instrumentStatus);

            psql.setInt(i++, instrumentID);

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
        setInstrumentID( (Number) currentRow.elementAt(0) );
        setMake((String) currentRow.elementAt(1));
        setModel((String) currentRow.elementAt(2));
        setSerialNumber((String) currentRow.elementAt(3));
        setAssetCode((String) currentRow.elementAt(4));
        setAcquisitionDate((Timestamp) currentRow.elementAt(5));
        setDisposalDate((Timestamp) currentRow.elementAt(6));
        setInstrumentType((String) currentRow.elementAt(7));
        setInstrumentStatus((String) currentRow.elementAt(8));

        isNew = false;
        isEdited = false;
    }

    public String getInstrumentType()
    {
        if(instrumentType != null)
        {
            if(instrumentType.trim().isEmpty())
                return null;
            else
                return instrumentType.trim();
        }
        else
            return null;
    }


    private static ArrayList<Instrument> doSelect(String sql)
    {
        ArrayList<Instrument> items = new ArrayList();

        query.setConnection( Common.getConnection() );
        query.executeQuery( sql);

        logger.trace("doSelect " + sql);
        
        Vector dataSet = query.getData();
        if ( ! ( dataSet == null ) )
        {
            for( int i = 0; i < dataSet.size(); i++ )
            {
                Vector currentRow = (Vector) dataSet.elementAt( i );

                Instrument row = new Instrument();
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
     * select all instruments which are not currently assigned to an assembly
     * @return
     */
    public static ArrayList<Instrument> selectUnassignedInstruments()
    {
        return doSelect( selectSQL
                        + " where instrument_type is null "
                        + " and date_disposed is null"
                        + getDefaultSortOrder());
    }

    /**
     * select all instruments with a data file linked to the specified mooring
     * 
     * @param mooringID
     * @return
     */
    public static ArrayList<Instrument> selectDataFilesForMooring(String mooringID)
    {
        return doSelect( selectSQL
                        + " where instrument_id in "
                        + "("
                        + " select distinct instrument_id from instrument_data_files "
                        + " where mooring_id = "
                        + StringUtilities.quoteString(mooringID)
                        + ")"
                        + getDefaultSortOrder());
    }
    
    public static ArrayList<Instrument> selectForRawData(String mooringID, String param)
    {
        return doSelect( selectSQL
                        + " where instrument_id in "
                        + "("
                        + " select distinct instrument_id from raw_instrument_data "
                        + " where parameter_code LIKE "
                        + StringUtilities.quoteString(param)
                        + " AND mooring_id = "
                        + StringUtilities.quoteString(mooringID)
                        + ")"
                        + getDefaultSortOrder());
    }
    
    /**
     * select all instruments with a calibration file linked to the specified mooring
     * 
     * @param mooringID
     * @return
     */
    public static ArrayList<Instrument> selectInstrumentsWithCalibrationFilesForMooring(String mooringID)
    { 
        return doSelect( selectSQL
                        + " where instrument_id in "
                        + "("
                        + " select distinct instrument_id from instrument_calibration_values "
                        + " where mooring_id = "
                        + StringUtilities.quoteString(mooringID)
                        + ")"
                        + getDefaultSortOrder());
    }

    public static ArrayList<Instrument> selectInstrumentsWithCalibrationFilesForMooring(Mooring mooring)
    {
        return selectInstrumentsWithCalibrationFilesForMooring(mooring.getMooringID());
    }
    /**
     * select all instruments assigned to a mooring regardless of whether they have any
     * data files linked to them - this can happen with instruments attached to an SBE16
     *
     * @param mooringID
     * @return
     */
    public static ArrayList<Instrument> selectInstrumentsAttachedToMooring(String mooringID)
    {
        return doSelect( selectSQL
                        + " where instrument_id in "
                        + "("
                        + " select distinct instrument_id from mooring_attached_instruments "
                        + " where mooring_id = "
                        + StringUtilities.quoteString(mooringID)
                        + ")"
                        + getDefaultSortOrder());
    }
    
    public static ArrayList<Instrument> selectInstrumentsAttachedToMooringAtDepth(String mooringID, double depth)
    {
        return doSelect( selectSQL
                        + " where instrument_id in "
                        + "("
                        + " select distinct instrument_id from mooring_attached_instruments "
                        + " where mooring_id = "
                        + StringUtilities.quoteString(mooringID)
                        + " AND depth = " + depth
                        + ")"
                        + getDefaultSortOrder());
    }
    
    public static ArrayList<Instrument> selectInstrumentsWithCalibrationFilesAttachedToMooring(String mooringID)
    {
        return doSelect( selectSQL
                        + " where instrument_id in "
                        + "("
                        + " select distinct mooring_attached_instruments.instrument_id"
                        + " from mooring_attached_instruments, instrument_calibration_files"
                        + " where mooring_id = "
                        + StringUtilities.quoteString(mooringID)
                        + " and instrument_calibration_files.instrument_id = mooring_attached_instruments.instrument_id"
                        + ")"
                        + getDefaultSortOrder());
    }

    public static ArrayList<Instrument> selectByMake(String make)
    {
        if(make == null)
            return null;

        if(make.trim().isEmpty())
            return null;

        if(! make.trim().endsWith("%"))
            make = make.trim() + "%";
        
        return doSelect( selectSQL
                        + " where make like "
                        + StringUtilities.quoteString(make)
                        + getDefaultSortOrder());
    }
    
    public static ArrayList<Instrument> selectBySerialNumber(String s_num)
    {
        if(s_num == null)
            return null;

        if(s_num.trim().isEmpty())
            return null;

        if(! s_num.trim().endsWith("%"))
            s_num = s_num.trim() + "%";
        
        return doSelect( selectSQL
                        + " where serial_number like "
                        + StringUtilities.quoteString(s_num)
                        + getDefaultSortOrder());
    }
    
    public static ArrayList<Instrument> selectByModel(String mod)
    {
        if(mod == null)
            return null;

        if(mod.trim().isEmpty())
            return null;

        if(! mod.trim().endsWith("%"))
            mod = mod.trim() + "%";
        
        return doSelect( selectSQL
                        + " where model like "
                        + StringUtilities.quoteString(mod)
                        + getDefaultSortOrder());
    }

    /**
     * select all instruments where at least 1 of their data files belongs to the specified mooring
     * 
     * @param mooring
     * @return
     */
    public static ArrayList<Instrument> selectInstrumentsForMooring(String mooring)
    {
        if(mooring == null)
            return null;

        if(mooring.trim().isEmpty())
            return null;

        return doSelect( selectSQL
                        + " where instrument_id in "
                        + "("
                        + "select distinct instrument_id from mooring_attached_instruments"
                        + " where mooring_id = "
                        + StringUtilities.quoteString(mooring)
                        + ")"
                        + getDefaultSortOrder());
    }

    public static ArrayList<Instrument> selectAllActiveCodes()
    {
        return selectAll();
    }

    public static ArrayList<Instrument> selectAll()
    {
        return doSelect( selectSQL + getDefaultSortOrder());
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException
    {
        return super.clone();
    }

    private boolean checkData()
    {
        if(getInstrumentID() == null)
        {
            message = "A blank or null instrument ID is not permitted.";
            return false;
        }

        if(getInstrumentType() == null)
        {
            message = "A blank or null instrument type is not permitted.";
            return false;
        }
        if(getMake() == null)
        {
            message = "A blank or null equipment make is not permitted.";
            return false;
        }
        if(getModel() == null)
        {
            message = "A blank or null equipment model is not permitted.";
            return false;
        }
        if(getAcquisitionDate() == null)
        {
            message = "A blank or null acquisition date is not permitted.";
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
                if (Common.isDataMirroringEnabled())
                {
                    PreparedStatement mirrorSQL = Common.getMirrorConnection().prepareStatement(getInsertSQL());
                    boolean mirrorDelete = doInsert(mirrorSQL, false);

                    mirrorSQL.close();
                }
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
            
            psql.setInt(i++, instrumentID);
            psql.setString(i++, make);
            psql.setString(i++, model);
            psql.setString(i++, serialNumber);
            psql.setString(i++, assetCode);

            psql.setTimestamp(i++, dateAcquired);

            if(dateDisposed != null)
                psql.setTimestamp(i++, dateDisposed);
            else
                psql.setNull(i++, java.sql.Types.TIMESTAMP);

            psql.setString(i++, instrumentType);
            psql.setString(i++, instrumentStatus);

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

    public String getEquipmentStatus()
    {
        return instrumentStatus;
    }

    public String getMake()
    {
        if(make != null)
        {
            if(make.trim().isEmpty())
                return null;
            else
                return make.trim();
        }
        else
            return null;
    }

    public boolean setMake(String string)
    {
        isEdited = true;

        if(string != null)
        {
            make = string.trim();
            return true;
        }
        else
            return true;
    }

    public String getModel()
    {
        if(model != null)
        {
            if(model.trim().isEmpty())
                return null;
            else
                return model.trim();
        }
        else
            return null;
    }

    public String getSerialNumber()
    {
        if(serialNumber != null)
        {
            if(serialNumber.trim().isEmpty())
                return null;
            else
                return serialNumber.trim();
        }
        else
            return null;
    }

    public String getAssetCode()
    {
        if(assetCode != null)
        {
            if(assetCode.trim().isEmpty())
                return null;
            else
                return assetCode.trim();
        }
        else
            return null;
    }

    public boolean setModel(String string)
    {
        isEdited = true;

        if(string != null)
        {
            model = string.trim();
            return true;
        }
        else
            return true;
    }

    public boolean setAcquisitionDate(Object date)
    {
        if(date == null)
        {
            dateAcquired = null;
            return true;
        }
        if(date instanceof Timestamp )
            return setAcquisitionDate((Timestamp) date);

        if(date instanceof Date )
            return setAcquisitionDate(new Timestamp(((Date)date).getTime()));

        return true;
    }

    public boolean setAcquisitionDate(Timestamp date)
    {
        dateAcquired = date;
        return true;
    }

    public Timestamp getAcquisitionDate()
    {
        return dateAcquired;
    }

    public boolean setDisposalDate(Object date)
    {
        if(date == null)
        {
            dateDisposed = null;
            return true;
        }
        if(date instanceof Timestamp )
            return setAcquisitionDate((Timestamp) date);

        if(date instanceof Date )
            return setAcquisitionDate(new Timestamp(((Date)date).getTime()));

        return true;
    }

    public boolean setDisposalDate(Timestamp date)
    {
        dateDisposed = date;
        return true;
    }

    public Timestamp getDisposalDate()
    {
        return dateDisposed;
    }

    public boolean setMake(Object value)
    {
        if(value == null)
        {
            make = null;
            return true;
        }
        else if(value instanceof String)
        {
            return setMake((String) value);
        }
        else
            return false;
    }

    public boolean setModel(Object value)
    {
        if(value == null)
        {
            model = null;
            return true;
        }
        else if(value instanceof String)
        {
            return setModel((String) value);
        }
        else
            return false;
    }

    public boolean setSerialNumber(Object value)
    {
        if(value == null)
        {
            serialNumber = null;
            return true;
        }
        else if(value instanceof String)
        {
            return setSerialNumber((String) value);
        }
        else
            return false;
    }

    public boolean setSerialNumber(String value)
    {
        if(value == null)
        {
            serialNumber = null;
        }
        else
        {
            serialNumber =  value.trim();
        }
        return true;
    }

    public boolean setAssetCode(Object value)
    {
        if(value == null)
        {
            assetCode = null;
            return true;
        }
        else if(value instanceof String)
        {
            return setAssetCode((String) value);
        }
        else
            return false;
    }

    public boolean setAssetCode(String value)
    {
        if(value == null)
        {
            assetCode = null;
        }
        else
        {
            assetCode =  value.trim();
        }
        return true;
    }

    protected boolean setInstrumentStatus(Object value)
    {
        if(value == null)
        {
            instrumentStatus = null;
            isEdited = true;
            return true;
        }
        if(value instanceof String)
        {
            instrumentStatus = ((String) value);
            isEdited = true;
            return true;
        }

        return false;
    }
}
