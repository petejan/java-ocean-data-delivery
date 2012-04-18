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
 * @modified
 *
 * 2012-04-18 PDW Added extra columns for netCDF descriptors
 */
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.wiley.LabMaster.Common;
import org.wiley.util.SQLWrapper;

public class ParameterCodes  implements Cloneable
{

    private boolean isNew = false;
    private static Logger logger = Logger.getLogger(ParameterCodes.class.getName());

    private String message = "";

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
        return " order by code";
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

    private boolean isEdited = false;
    protected static SQLWrapper query    = new SQLWrapper();
    private boolean DEBUG = true;

    private String code;
    private String description;
    private String netCDFName;
    private String units;
    private String displayCode;

    private static String[] columnNames = new String[]
    {
        "Code",
        "Description",
        "NetCDF Name",
        "Units",
        "Display"
    };

    private static Class[] columnClasses = new Class[]
    {
        String.class,
        String.class,
        String.class,
        String.class,
        Boolean.class
    };

    private static String selectSQL = "select"
                + " code,"
                + " description,"
                + " netcdf_name,"
                + " units,"
                + " display_code"
                + " from parameters"
                ;

    

    private static String insertSQL  = "insert into parameters"
            + "("
            + " code,"
            + " description,"
            + " netcdf_name,"
            + " units,"
            + " display_code"
            + ")"
            + " values "
            + "("
            + "?,?,?,?,?"
            + ")"
            ;
    
    public static String getInsertSQL()
    {
        return insertSQL;
    }

    public static String getUpdateSQL()
    {
        return "update parameters "
                + " set description = ?,\n"
                + " netcdf_name = ?,\n"
                + " units = ?,\n"
                + " display_code = ?\n"
                + " where code = ?";
    }

    public static String getDeleteSQL()
    {
        return "delete from parameters where code = ?";
    }

    public static String getSelectSQL()
    {
        return selectSQL;
    }
    public static String getTableName()
    {
        return "parameters";
    }

    /**
     * select a single ParameterCodes object using its unique ID
     * @param id
     * @return a ParameterCodes object, or null if not found
     */
    public static ParameterCodes selectByID(String id)
    {
        String sql = selectSQL
                + " where code = '"
                + id.trim()
                + "'"
                ;
        query.setConnection( Common.getConnection() );
        query.executeQuery( sql );

            Vector dataSet = query.getData();
            if ( ! ( dataSet == null ) )
            {
                for( int i = 0; i < dataSet.size(); i++ )
                {
                    Vector currentRow = (Vector) dataSet.elementAt( i );

                    ParameterCodes row = new ParameterCodes();
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
                if (Common.isDataMirroringEnabled())
                {
                    PreparedStatement mirrorSQL = Common.getMirrorConnection().prepareStatement(getDeleteSQL());
                    boolean mirrorDelete = doDelete(mirrorSQL, false);

                    mirrorSQL.close();
                }
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

    public String getParameterCode()
    {
        if(code == null)
            return null;
        else
            return code.trim();
    }

    private boolean doDelete(PreparedStatement psql, boolean isMaster)
    {
        try
        {
            psql.setString(1, code);

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
            return code;
        else if(columnIndex == 1)
            return description;
        else if(columnIndex == 2)
            return netCDFName;
        else if(columnIndex == 3)
            return units;
        else if(columnIndex == 4)
            return getDisplayCodeAsBoolean();
        else
            return null;
    }

    public boolean setParameterCode(Object value)
    {
        if(value == null)
        {
            code = null;
            isEdited = true;
            return true;
        }
        if(value instanceof String)
        {
            setParameterCode((String) value);
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
            return setParameterCode((String) value);
        else if(aColumn == 1)
            return setDescription(value);
        else if(aColumn == 2)
            return setNetCDFName((String) value);
        else if(aColumn == 3)
            return setUnits((String) value);
        else if(aColumn == 4)
            return setDisplayCode(value);
        else
            return false;
    }

    protected boolean setDescription(Object value)
    {
        if(value == null)
        {
            description = null;
            isEdited = true;
            return true;
        }
        if(value instanceof String)
        {
            setDescription((String) value);
            isEdited = true;
            return true;
        }

        return false;
    }

    protected boolean setDisplayCode(Object value)
    {
        if(value == null)
        {
            displayCode = "Y";
            isEdited = true;
            return true;
        }
        if(value instanceof String)
        {
            setDisplayCode((String) value);
            isEdited = true;
            return true;
        }
        if(value instanceof Boolean)
        {
            setDisplayCode((Boolean) value);
            isEdited = true;
            return true;
        }

        return false;
    }

    protected boolean setDisplayCode(String value)
    {
        if(value == null)
        {
            displayCode = "Y";
            isEdited = true;
            return true;
        }
        if(value.trim().isEmpty())
        {
            displayCode = "Y";
            isEdited = true;
            return true;
        }
        else
        {
            if(value.startsWith("Y"))
                displayCode = "Y";
            else
                displayCode = "N";

            isEdited = true;
            return true;
        }
    }

    protected boolean setDisplayCode(Boolean value)
    {
        if(value == null)
            return setDisplayCode((String) null);
        else
        {
            if(value.booleanValue())
                return setDisplayCode("Y");
            else
                return setDisplayCode("N");
        }
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

            boolean updateOK = doUpdate(ps, true);

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

    private boolean doUpdate(PreparedStatement psql, boolean isMaster)
    {
        try
        {
            psql.setString(1, description);
            psql.setString(2, netCDFName);
            psql.setString(3, units);
            psql.setString(4, displayCode);
            psql.setString(5, code);

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
        setParameterCode( (String) currentRow.elementAt(0) );
        setDescription((String) currentRow.elementAt(1));
        setNetCDFName((String) currentRow.elementAt(2));
        setUnits((String) currentRow.elementAt(3));
        setDisplayCode((String) currentRow.elementAt(4));

        isNew = false;
        isEdited = false;
    }

    public boolean setParameterCode(String string)
    {
        if(string != null)
        {
            code = string;
            isEdited = true;
            return true;
        }
        else
            return false;
    }


    public void setDescription(String string)
    {
        description = string;
        isEdited = true;
    }

    public String getDescription()
    {
        if(description == null || description.trim().isEmpty())
            return null;
        else
            return description.trim();
    }

    public boolean setNetCDFName(String string)
    {
        if(string != null && (! string.isEmpty()))
        {
            netCDFName = string.trim();
        }
        else
        {
            netCDFName = null;
        }

        isEdited = true;
        return true;
    }

    public String getNetCDFName()
    {
        if(netCDFName == null || netCDFName.trim().isEmpty())
            return null;
        else
            return netCDFName.trim();
    }

    public boolean setUnits(String string)
    {
        if(string != null && (! string.isEmpty()))
        {
            units = string.trim();
        }
        else
        {
            units = null;
        }

        isEdited = true;
        return true;
    }

    public String getUnits()
    {
        if(units == null || units.trim().isEmpty())
            return null;
        else
            return units.trim();
    }

    /**
     * get all the codes that are currently active ie can be displayed to a user
     * @return
     */
    public static ArrayList<ParameterCodes> selectAllActiveCodes()
    {
        return doSelect( selectSQL
                + " where display_code is null or  display_code != 'N'"
                + getDefaultSortOrder());
    }

    private static ArrayList<ParameterCodes> doSelect(String sql)
    {
        ArrayList<ParameterCodes> items = new ArrayList();

        query.setConnection( Common.getConnection() );
        query.executeQuery( sql);

            Vector dataSet = query.getData();
            if ( ! ( dataSet == null ) )
            {
                for( int i = 0; i < dataSet.size(); i++ )
                {
                    Vector currentRow = (Vector) dataSet.elementAt( i );

                    ParameterCodes row = new ParameterCodes();
                    row.create( currentRow );

                    items.add(row);
                }
            }

        return items;
    }
    /**
     * get all the codes in the database
     * @return
     */
    public static ArrayList<ParameterCodes> selectAll()
    {
        return doSelect( selectSQL
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

            boolean insertOK = doInsert(ps, true);

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
        if(code == null)
        {
            message = "You must enter a parameter code.";
            return false;
        }
        if(code.trim().isEmpty())
        {
            message = "You must enter a parameter code.";
            return false;
        }
        if(code.trim().length() > 20)
        {
            message = "Codes must be no more than 20 characters in length.";
            return false;
        }

        if(description != null && description.trim().length() > 80)
        {
            message = "Description must be no more than 80 characters in length.";
            return false;
        }

        if(displayCode != null && (! displayCode.trim().isEmpty()))
        {
            if(displayCode.toUpperCase().startsWith("Y") || displayCode.toUpperCase().startsWith("N"))
            {
                // this is ok
            }
            else
            {
                message = "Allowable values for the active code flag field are Y or N.";
                return false;
            }
        }
        message = "";
        return true;
    }

    private boolean doInsert(PreparedStatement psql, boolean isMaster)
    {
        try
        {
            psql.setString(1, code);
            psql.setString(2, description);
            psql.setString(3, netCDFName);
            psql.setString(4, units);
            psql.setString(5, displayCode);

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

    public Boolean getDisplayCodeAsBoolean()
    {
        if(displayCode == null)
            return new Boolean(true);

        if(displayCode.trim().isEmpty())
            return new Boolean(true);

        if(displayCode.startsWith("N"))
            return new Boolean(false);
        else
            return new Boolean(true);
    }
}

