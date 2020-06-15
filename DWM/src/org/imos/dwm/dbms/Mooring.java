/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.dwm.dbms;

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

public class Mooring  implements Cloneable
{

    private boolean isNew = false;
    private static Logger logger = Logger.getLogger(Mooring.class.getName());

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
            60,
            60
        };
    }

    public static int[] getLookupColumnWidths()
    {
        return new int[]
        {
            60,
            120
        };
    }

    public static String getDefaultSortOrder()
    {
        return " order by mooring_id";
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

    private String mooringID;
    private String shortDescription;
    private String facility;
    private Timestamp timestampIn;
    private Timestamp timestampOut;
    private Double latitudeIn;
    private Double longitudeIn;
    private Double latitudeOut;
    private Double longitudeOut;

    private static String[] columnNames = new String[]
    {
        "Mooring ID",
        "Description",
        "Facility",
        "Time In",
        "Time Out",
        "Lat In",
        "Long In",
        "Lat Out",
        "Long Out"
    };

    private static String[] lookupColumnNames = new String[]
    {
        "Mooring ID",
        "Description"
    };

    private static Class[] columnClasses = new Class[]
    {
        String.class,
        String.class,
        String.class,
        Timestamp.class,
        Timestamp.class,
        Double.class,
        Double.class,
        Double.class,
        Double.class
    };

    private static Class[] lookupColumnClasses = new Class[]
    {
        String.class,
        String.class
    };

    private static String selectSQL = "select"
                + " mooring_id,"
                + " short_description,"
                + " facility,"
                + " timestamp_in,"
                + " timestamp_out,"
                + " latitude_in,"
                + " longitude_in,"
                + " latitude_out,"
                + " longitude_out"
                + " from mooring"
                ;


    private static String insertSQL  = "insert into Mooring"
            + "("
            + " mooring_id,"
            + " short_description,"
            + " facility,"
            + " timestamp_in,"
            + " timestamp_out,"
            + " latitude_in,"
            + " longitude_in,"
            + " latitude_out,"
            + " longitude_out"
            + ")"
            + " values "
            + "("
            + "?,?,?,?,?,?,?,?"
            + ")"
            ;


    public static String getInsertSQL()
    {
        return insertSQL;
    }

    public static String getUpdateSQL()
    {
        return "update Mooring set "
                + " short_description = ?,"
                + " facility = ?,"
                + " timestamp_in = ?,"
                + " timestamp_out = ?,"
                + " latitude_in = ?,"
                + " longitude_in = ?,"
                + " latitude_out = ?,"
                + " longitude_out = ?"
                + " where mooring_id = ?";
    }

    public static String getDeleteSQL()
    {
        return "delete from mooring"
            + " where mooring_id = ?"
            ;
    }

    public static String getSelectSQL()
    {
        return selectSQL;
    }
    public static String getTableName()
    {
        return "Mooring";
    }

    /**
     * select a single Mooring using its unique ID
     * @param id
     * @return a Mooring object, or null if not found
     */
    public static Mooring selectByMooringID(String id)
    {
        String sql = selectSQL
                + " where Mooring_id = "
                + StringUtilities.quoteString(id)
                ;

        query.setConnection( Common.getConnection() );
        query.executeQuery( sql );

            Vector dataSet = query.getData();
            if ( ! ( dataSet == null ) )
            {
                for( int i = 0; i < dataSet.size(); i++ )
                {
                    Vector currentRow = (Vector) dataSet.elementAt( i );

                    Mooring row = new Mooring();
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
     * NB: this should be wrapped in a transEquipmentMooring
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

    public String getMooringID()
    {
        return mooringID;
    }


    private boolean doDelete(PreparedStatement psql, boolean isMaster)
    {
        try
        {
            psql.setString(1, mooringID);

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
            return mooringID;
        else if(columnIndex == 1)
            return shortDescription;
        else if(columnIndex == 2)
            return facility;
        else if(columnIndex == 3)
            return timestampIn;
        else if(columnIndex == 4)
            return timestampOut;
        else if(columnIndex == 5)
            return latitudeIn;
        else if(columnIndex == 6)
            return longitudeIn;
        else if(columnIndex == 7)
            return latitudeOut;
        else if(columnIndex == 8)
            return longitudeOut;
        else
            return null;
    }

    public boolean setMooringID(Object value)
    {
        if(value == null)
        {
            mooringID = null;
            isEdited = true;
            return true;
        }
        if(value instanceof String)
        {
            mooringID = ((String) value).trim();
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
            return setMooringID(value);
        else if(aColumn == 1)
            return setShortDescription(value);
        else if(aColumn == 2)
            return setFacility(value);
        else if(aColumn == 3)
            return setTimestampIn(value);
        else if(aColumn == 4)
            return setTimestampOut(value);
        else if(aColumn == 5)
            return setLatitudeIn(value);
        else if(aColumn == 6)
            return setLongitudeIn(value);
        else if(aColumn == 7)
            return setLatitudeOut(value);
        else if(aColumn == 8)
            return setLongitudeOut(value);
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

            psql.setString(i++, shortDescription);
            psql.setString(i++, facility);

            if(timestampIn != null)
                psql.setTimestamp(i++, timestampIn);
            else
                psql.setNull(i++, java.sql.Types.TIMESTAMP);

            if(timestampOut != null)
                psql.setTimestamp(i++, timestampOut);
            else
                psql.setNull(i++, java.sql.Types.TIMESTAMP);

            if(latitudeIn != null)
                psql.setDouble(i++, latitudeIn);
            else
                psql.setNull(i++, java.sql.Types.DOUBLE);

            if(longitudeIn != null)
                psql.setDouble(i++, longitudeIn);
            else
                psql.setNull(i++, java.sql.Types.DOUBLE);

            if(latitudeOut != null)
                psql.setDouble(i++, latitudeOut);
            else
                psql.setNull(i++, java.sql.Types.DOUBLE);

            if(longitudeOut != null)
                psql.setDouble(i++, longitudeOut);
            else
                psql.setNull(i++, java.sql.Types.DOUBLE);



            psql.setString(i++, mooringID);

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
        setMooringID( (String) currentRow.elementAt(0) );
        setShortDescription((String) currentRow.elementAt(1));
        setFacility((String) currentRow.elementAt(2));
        setTimestampIn((Timestamp) currentRow.elementAt(3));
        setTimestampOut((Timestamp) currentRow.elementAt(4));
        setLatitudeIn((Number) currentRow.elementAt(5));
        setLongitudeIn((Number) currentRow.elementAt(6));
        setLatitudeOut((Number) currentRow.elementAt(7));
        setLongitudeOut((Number) currentRow.elementAt(8));

        isNew = false;
        isEdited = false;
    }

    private static ArrayList<Mooring> doSelect(String sql)
    {
        ArrayList<Mooring> items = new ArrayList();

        query.setConnection( Common.getConnection() );
        query.executeQuery( sql);

        Vector dataSet = query.getData();
        if ( ! ( dataSet == null ) )
        {
            for( int i = 0; i < dataSet.size(); i++ )
            {
                Vector currentRow = (Vector) dataSet.elementAt( i );

                Mooring row = new Mooring();
                row.create( currentRow );

                items.add(row);
            }
        }

        if(items.size() > 0)
            return items;
        else
            return null;
    }

    public static ArrayList<Mooring> selectAllActiveCodes()
    {
        return selectAll();
    }

    public static ArrayList<Mooring> selectAll()
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
        if(getMooringID() == null)
        {
            message = "A blank or null Mooring ID is not permitted.";
            return false;
        }

        if(getShortDescription() == null)
        {
            message = "A blank or null equipment make is not permitted.";
            return false;
        }
        
        if(getTimestampIn() == null)
        {
            message = "A blank or null timestamp in is not permitted.";
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

            psql.setString(i++, mooringID);
            psql.setString(i++, shortDescription);
            psql.setString(i++, facility);

            if(timestampIn != null)
                psql.setTimestamp(i++, timestampIn);
            else
                psql.setNull(i++, java.sql.Types.TIMESTAMP);

            if(timestampOut != null)
                psql.setTimestamp(i++, timestampOut);
            else
                psql.setNull(i++, java.sql.Types.TIMESTAMP);

            if(latitudeIn != null)
                psql.setDouble(i++, latitudeIn);
            else
                psql.setNull(i++, java.sql.Types.DOUBLE);

            if(longitudeIn != null)
                psql.setDouble(i++, longitudeIn);
            else
                psql.setNull(i++, java.sql.Types.DOUBLE);

            if(latitudeOut != null)
                psql.setDouble(i++, latitudeOut);
            else
                psql.setNull(i++, java.sql.Types.DOUBLE);

            if(longitudeOut != null)
                psql.setDouble(i++, longitudeOut);
            else
                psql.setNull(i++, java.sql.Types.DOUBLE);

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

    public String getShortDescription()
    {
        if(shortDescription != null)
        {
            if(shortDescription.trim().isEmpty())
                return null;
            else
                return shortDescription.trim();
        }
        else
            return null;
    }

    public String getFacility()
    {
        if(facility != null)
        {
            if(facility.trim().isEmpty())
                return null;
            else
                return facility.trim();
        }
        else
            return null;
    }

    public Timestamp getTimestampIn()
    {
        return timestampIn;
    }

    public Timestamp getTimestampOut()
    {
        return timestampOut;
    }

    
    public boolean setTimestampIn(Object date)
    {
        if(date == null)
        {
            timestampIn = null;
            return true;
        }
        if(date instanceof Timestamp )
            return setTimestampIn((Timestamp) date);

        if(date instanceof Date )
            return setTimestampIn(new Timestamp(((Date)date).getTime()));

        return true;
    }

    public boolean setTimestampOut(Object date)
    {
        if(date == null)
        {
            timestampOut = null;
            return true;
        }
        if(date instanceof Timestamp )
            return setTimestampOut((Timestamp) date);

        if(date instanceof Date )
            return setTimestampOut(new Timestamp(((Date)date).getTime()));

        return true;
    }

    public boolean setTimestampIn(Timestamp date)
    {
        timestampIn = date;
        return true;
    }

    public boolean setTimestampOut(Timestamp date)
    {
        timestampOut = date;
        return true;
    }

    
    public boolean setShortDescription(Object value)
    {
        if(value == null)
        {
            shortDescription = null;
            return true;
        }
        else if(value instanceof String)
        {
            return setShortDescription((String) value);
        }
        else
            return false;
    }

    public boolean setShortDescription(String value)
    {
        if(value == null)
        {
            shortDescription = null;
            return true;
        }
        else if(value instanceof String)
        {
            shortDescription = value;
            
            return true;
        }
        else
            return false;
    }

    public boolean setFacility(Object value)
    {
        if(value == null)
        {
            facility = null;
            return true;
        }
        else if(value instanceof String)
        {
            return setFacility((String) value);
        }
        else
            return false;
    }
    public boolean setFacility(String value)
    {
        if(value == null)
        {
            facility = null;
            return true;
        }
        else if(value instanceof String)
        {
            facility = value;
            return true;
        }
        else
            return false;
    }

    public boolean setLatitudeIn(Object value)
    {
        if(value == null)
        {
            latitudeIn = null;
            isEdited = true;
            return true;
        }
        if(value instanceof Number)
        {
            latitudeIn = ((Number) value).doubleValue();
            isEdited = true;
            return true;
        }

        return false;
    }

    public Double getLatitudeIn()
    {
        return latitudeIn;
    }

    public boolean setLongitudeIn(Object value)
    {
        if(value == null)
        {
            longitudeIn = null;
            isEdited = true;
            return true;
        }
        if(value instanceof Number)
        {
            longitudeIn = ((Number) value).doubleValue();
            isEdited = true;
            return true;
        }

        return false;
    }

    public Double getLongitudeIn()
    {
        return longitudeIn;
    }

    public boolean setLatitudeOut(Object value)
    {
        if(value == null)
        {
            latitudeOut = null;
            isEdited = true;
            return true;
        }
        if(value instanceof Number)
        {
            latitudeOut = ((Number) value).doubleValue();
            isEdited = true;
            return true;
        }

        return false;
    }

    public Double getLatitudeOut()
    {
        return latitudeOut;
    }

    public boolean setLongitudeOut(Object value)
    {
        if(value == null)
        {
            longitudeOut = null;
            isEdited = true;
            return true;
        }
        if(value instanceof Number)
        {
            longitudeOut = ((Number) value).doubleValue();
            isEdited = true;
            return true;
        }

        return false;
    }

    public Double getLongitudeOut()
    {
        return longitudeOut;
    }

    /**
     * assign an instrument to a specified mooring
     * @param MID
     * @param instrumentID
     * @return true if successful, false otherwise. Most likely failure cause would be a PK
     *          error if the instrument is already assigned - possible.
     */
    public static boolean assignAttachedInstrument(String MID, Integer instrumentID, Double depth)
    {
        String SQL = "insert into mooring_attached_instruments"
                + "(mooring_id, instrument_id, depth)"
                + " values "
                + "("
                + StringUtilities.quoteString(MID)
                + ","
                + instrumentID
                + ","
                + depth
                + ")"
                ;
        
        return Common.executeSQL(SQL);
    }
}

