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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.wiley.LabMaster.Common;
import org.wiley.core.dbms.SystemParameters;
import org.wiley.util.DateUtilities;
import org.wiley.util.SQLWrapper;
import org.wiley.util.StringUtilities;

public class InstrumentCalibrationFile  implements Cloneable
{

    public static ArrayList<InstrumentCalibrationFile> selectFilesWithCalibrationValuesForMooring(Mooring selectedMooring)
    {
        return doSelect( selectSQL
                        + " where exists "
                        + " ( "
                        + " select datafile_pk  "
                        + " from instrument_calibration_values cav "
                        + " where cav.datafile_pk = instrument_calibration_files.datafile_pk "
                        + " and cav.mooring_id = "
                        + StringUtilities.quoteString(selectedMooring.getMooringID())
                        + " ) "
                        + getDefaultSortOrder());
    }

    private boolean isNew = false;
    private static Logger logger = Logger.getLogger(InstrumentCalibrationFile.class.getName());

    private String message = "";

    public static int[] getColumnWidths()
    {
        return new int[]
        {
            40,
            60,
            60,
            180,
            120,
            60,
            60,
            160
        };
    }

    public static int[] getLookupColumnWidths()
    {
        return getColumnWidths();
    }

    public static String getDefaultSortOrder()
    {
        return " order by instrument_id";
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

    private File dataFile;

    private Integer datafilePrimaryKey;
    private Integer instrumentID;
    private String filePath;
    private String fileName;
    private Timestamp validFrom;
    private Timestamp validTo;
    private Timestamp processingDate;
    private String processingClass;

    private static String[] columnNames = new String[]
    {
        "PK",
        "Instrument ID",
        "File Path",
        "File Name",
        "Valid From",
        "To",
        "Processed",
        "Class"
    };



    private static Class[] columnClasses = new Class[]
    {
        Integer.class,
        Integer.class,
        String.class,
        String.class,
        Timestamp.class,
        Timestamp.class,
        Timestamp.class,
        String.class
    };

    private static String selectFileDataSQL = "select"
                + " file_data"
                + " from instrument_calibration_files"
                ;

    private static String selectSQL = "select"
                + " datafile_pk,"
                + " instrument_id,"
                + " file_path,"
                + " file_name,"
                + " validity_start,"
                + " validity_end,"
                + " processing_date,"
                + " processing_class"
                + " from instrument_calibration_files"
                ;


    private static String insertSQL  = "insert into instrument_calibration_files"
            + "("
            + " datafile_pk,"
            + " instrument_id,"
            + " file_path,"
            + " file_name,"
            + " file_data,"
            + " validity_start,"
            + " validity_end,"
            + " processing_date,"
            + " processing_class"
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
        return "update instrument_calibration_files set "
                + " instrument_id = ?,"
                + " file_path = ?,"
                + " file_name = ?,"
                + " validity_start = ?,"
                + " validity_end = ?,"
                + " processing_date = ?,"
                + " processing_class = ?"
                + " where datafile_pk = ?"
                ;
    }

    public static String getDeleteSQL()
    {
        return "delete from instrument_calibration_files"
            + " where datafile_pk = ?"
            ;
    }

    public static String getSelectSQL()
    {
        return selectSQL;
    }
    public static String getTableName()
    {
        return "InstrumentCalibrationFile";
    }

    /**
     * select a single InstrumentCalibrationFile using its unique ID
     * @param id
     * @return a InstrumentCalibrationFile object, or null if not found
     */
    public static InstrumentCalibrationFile selectByInstrumentCalibrationFileID(Integer id)
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

                    InstrumentCalibrationFile row = new InstrumentCalibrationFile();
                    row.create( currentRow );
                    return row;
                }
            }

        return null;
    }

    /**
     * select a single InstrumentCalibrationFile using its unique ID
     * @param id
     * @return a InstrumentCalibrationFile object, or null if not found
     */
    public static InstrumentCalibrationFile selectByDatafilePrimaryKey(Integer id)
    {
        String sql = selectSQL
                + " where datafile_pk = "
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

                    InstrumentCalibrationFile row = new InstrumentCalibrationFile();
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
     * NB: this should be wrapped in a transEquipmentInstrumentCalibrationFile
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

    /**
     * return a File object containing the data from the file as loaded into the database
     * NB: the File will be created in the TEMP_DIR location as defined in System Parameters
     * if this parameter has been defined, otherwise in its original location.
     *
     * @return a File object, or null
     */
    public File getInstrumentCalibrationFile()
    {
        if(dataFile == null)
        {
            //
            // no file object has been created indicating that the BLOB of data content for the file
            // hasn't been fetched from the back end database, so fetch it. This is slooooow.....
            //
            selectDataForFile();
        }
        return dataFile;
    }

    /**
     * return the ID (primary key) of the instrument that created this data file
     * @return
     */
    public Integer getInstrumentID()
    {
        return instrumentID;
    }


    private boolean doDelete(PreparedStatement psql)
    {
        try
        {
            psql.setInt(1, datafilePrimaryKey);

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
                message = "Attempted deletion of " + affectedRows + "!";
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
            return instrumentID;
        else if(columnIndex == 2)
            return filePath;
        else if(columnIndex == 3)
            return fileName;
        else if(columnIndex == 4)
            return validFrom;
        else if(columnIndex == 5)
            return validTo;
        else if(columnIndex == 6)
            return processingDate;
        else if(columnIndex == 7)
            return processingClass;
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

    public boolean setProcessingClass(Object value)
    {
        if(value == null)
        {
            processingClass = null;
            isEdited = true;
            return true;
        }
        if(value instanceof String)
        {
            processingClass = ((String)value).trim();
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
            return setInstrumentID(value);
        else if(aColumn == 2)
            return setFilePath(value);
        else if(aColumn == 3)
            return setFileName(value);
        else if(aColumn == 4)
            return setValidityFrom(value);
        else if(aColumn == 5)
            return setValidityTo(value);
        else if(aColumn == 6)
            return setProcessingDate(value);
        else if(aColumn == 7)
            return setProcessingClass(value);
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
        String SQL = "select nextval('datafile_sequence') ";
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
            setDataFilePrimaryKey( InstrumentCalibrationFile.getNextSequenceNumber() );
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
            FileInputStream   fis = new FileInputStream(dataFile);
            int i = 1;

            psql.setInt(i++, instrumentID);
            psql.setString(i++, filePath);
            psql.setString(i++, fileName);
            psql.setTimestamp(i++, validFrom);
            psql.setTimestamp(i++, validTo);
            psql.setTimestamp(i++, processingDate);
            psql.setString(i++, processingClass);

            psql.setInt(i++, datafilePrimaryKey);

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
        catch(FileNotFoundException fex)
        {
            logger.error(fex);
            message = fex.getMessage();
            return false;
        }
        catch (SQLException ex)
        {
            logger.error(ex);
            message = ex.getMessage();
            return false;
        }
    }

    /**
     * assign values to the InstrumentCalibrationFile instance created
     * NB: we do NOT routinely fetch the data file BLOB (contents) as this can be many Mb in size
     * and when listing all the files created be an instrument or for a mooring, this is an invitation
     * to a memory overflow.
     *
     * @param currentRow
     */
    private void create(Vector currentRow)
    {
        int i = 0;

        setDataFilePrimaryKey( (Number) currentRow.elementAt(i++) );
        setInstrumentID( (Number) currentRow.elementAt(i++) );
        setFilePath((String) currentRow.elementAt(i++));
        setFileName((String) currentRow.elementAt(i++));

        setValidityFrom((Timestamp) currentRow.elementAt(i++));
        setValidityTo((Timestamp) currentRow.elementAt(i++));
        setProcessingDate((Timestamp) currentRow.elementAt(i++));
        setProcessingClass((String) currentRow.elementAt(i++));


        isNew = false;
        isEdited = false;
    }

    public String getProcessingClass()
    {
        if(processingClass != null)
        {
            if(processingClass.trim().isEmpty())
                return null;
            else
                return processingClass.trim();
        }
        else
            return null;
    }


    private void selectDataForFile()
    {
        String sql = selectFileDataSQL
                    + " where datafile_pk = "
                    + this.datafilePrimaryKey
                    ;

        query.setConnection( Common.getConnection() );
        query.executeQuery( sql);
        Vector dataSet = query.getData();
        if ( ! ( dataSet == null ) )
        {
            //
            // should be only 1 row
            //
            for( int i = 0; i < dataSet.size(); i++ )
            {
                Vector currentRow = (Vector) dataSet.elementAt( i );
                setFileData(currentRow.elementAt(0));
            }
        }
    }

    private static ArrayList<InstrumentCalibrationFile> doSelect(String sql)
    {
        ArrayList<InstrumentCalibrationFile> items = new ArrayList();

        query.setConnection( Common.getConnection() );
        query.executeQuery( sql);

        Vector dataSet = query.getData();
        if ( ! ( dataSet == null ) )
        {
            for( int i = 0; i < dataSet.size(); i++ )
            {
                Vector currentRow = (Vector) dataSet.elementAt( i );

                InstrumentCalibrationFile row = new InstrumentCalibrationFile();
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
     * select all data files linked to a specified instrument
     * @return
     */
    public static ArrayList<InstrumentCalibrationFile> selectCalibrationFilesForInstrument(Integer ID)
    {
        return doSelect( selectSQL
                        + " where instrument_id = "
                        + ID
                        + getDefaultSortOrder());
    }

    /**
     * select all data files linked to a specified mooring
     * @param ID
     * @return
     */
    public static ArrayList<InstrumentCalibrationFile> selectCalibrationFilesForMooring(String ID)
    {
        return doSelect( selectSQL
                        + " where mooring_id = "
                        + StringUtilities.quoteString(ID)
                        + getDefaultSortOrder());
    }

    public static ArrayList<InstrumentCalibrationFile> selectFilesWithCalibrationValues()
    {
        return doSelect( selectSQL
                        + " where exists "
                        + " ( "
                        + " select datafile_pk  "
                        + " from instrument_calibration_values cav "
                        + " where cav.datafile_pk = instrument_calibration_files.datafile_pk "
                        + " ) "
                        + getDefaultSortOrder());
    }

    public static ArrayList<InstrumentCalibrationFile> selectFilesWithCalibrationValuesForMooring(String ID)
    {
        return doSelect( selectSQL
                        + " where exists "
                        + " ( "
                        + " select datafile_pk  "
                        + " from instrument_calibration_values cav "
                        + " where cav.datafile_pk = instrument_calibration_files.datafile_pk "
                        + " and mooring_id = "
                        + StringUtilities.quoteString(ID)
                        + " ) "
                        + getDefaultSortOrder());
    }


    public static ArrayList<InstrumentCalibrationFile> selectAllActiveCodes()
    {
        return selectAll();
    }

    public static ArrayList<InstrumentCalibrationFile> selectAll()
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

        if(getFilePath() == null)
        {
            message = "A blank or null file name is not permitted.";
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
        if(validFrom == null)
            validFrom = Common.current();

        if(validTo == null)
            validTo = new Timestamp(DateUtilities.addDays(Common.today(), 365).getTime());
        
        try
        {
            FileInputStream   fis = new FileInputStream(dataFile);
            int i = 1;

            psql.setInt(i++, datafilePrimaryKey);

            psql.setInt(i++, instrumentID);
            psql.setString(i++, filePath);
            psql.setString(i++, fileName);
            psql.setBinaryStream(i++, fis, (int) dataFile.length());
            psql.setTimestamp(i++, validFrom);
            psql.setTimestamp(i++, validTo);
            psql.setTimestamp(i++, processingDate);
            psql.setString(i++, processingClass);

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
        catch(FileNotFoundException fex)
        {
            logger.error(fex);
            message = fex.getMessage();
            return false;
        }
        catch (SQLException ex)
        {
            logger.error(ex);
            message = ex.getMessage();
            return false;
        }
    }

    public String getFilePath()
    {
        if(filePath != null)
        {
            if(filePath.trim().isEmpty())
                return null;
            else
                return filePath.trim();
        }
        else
            return null;
    }

    public String getFileName()
    {
        if(fileName != null)
        {
            if(fileName.trim().isEmpty())
                return null;
            else
                return fileName.trim();
        }
        else
            return null;
    }

    public boolean setFilePath(String string)
    {
        isEdited = true;

        if(string != null)
        {
            filePath = string.trim();
            return true;
        }
        else
            return true;
    }

    public boolean setProcessingDate(Object date)
    {
        if(date == null)
        {
            processingDate = null;
            return true;
        }
        if(date instanceof Timestamp )
            return setProcessingDate((Timestamp) date);

        if(date instanceof Date )
            return setProcessingDate(new Timestamp(((Date)date).getTime()));

        return true;
    }


    public boolean setValidityFrom(Object date)
    {
        if(date == null)
        {
            validFrom = null;
            return true;
        }
        if(date instanceof Timestamp )
            return setValidityFrom((Timestamp) date);

        if(date instanceof Date )
            return setValidityFrom(new Timestamp(((Date)date).getTime()));

        return true;
    }

    public boolean setValidityFrom(Timestamp date)
    {
        validFrom = date;
        return true;
    }

    public Timestamp getValidityFrom()
    {
        return validFrom;
    }

    public boolean setValidityTo(Object date)
    {
        if(date == null)
        {
            validTo = null;
            return true;
        }
        if(date instanceof Timestamp )
            return setValidityTo((Timestamp) date);

        if(date instanceof Date )
            return setValidityTo(new Timestamp(((Date)date).getTime()));

        return true;
    }

    public boolean setValidityTo(Timestamp date)
    {
        validTo = date;
        return true;
    }

    public Timestamp getValidityTo()
    {
        return validTo;
    }

    public boolean setProcessingDate(Timestamp date)
    {
        processingDate = date;
        return true;
    }

    public Timestamp getProcessingDate()
    {
        return processingDate;
    }

    public boolean setFilePath(Object value)
    {
        if(value == null)
        {
            filePath = null;
            return true;
        }
        else if(value instanceof String)
        {
            return setFilePath((String) value);
        }
        else
            return false;
    }

    public boolean setFileName(Object value)
    {
        if(value == null)
        {
            fileName = null;
            return true;
        }
        else if(value instanceof String)
        {
            return setFileName((String) value);
        }
        else
            return false;
    }

    public boolean setFileName(String value)
    {
        if(value == null)
        {
            fileName = null;
        }
        else
        {
            fileName =  value.trim();
        }
        return true;
    }

    public boolean setFileData(Object value)
    {
        SystemParameters sp = SystemParameters.selectByPrimaryKey("TEMP_DIR");
        if(value == null)
        {
            dataFile = null;
        }
        else if(value instanceof byte[])
        {
            //
            // return from database is a byte array
            //
            byte[] foobar = (byte[]) value;

            String tempFileName = filePath.trim();

            if(sp != null)
            {
                String tempPath = sp.getParameterValue();
                // tempFileName = tempPath + File.separator + fileName;
                tempFileName = fileName;
            }

            try
            {
                dataFile = new File(tempFileName);
                FileOutputStream fos = new FileOutputStream(dataFile);
                for(int i = 0; i < foobar.length; i++)
                {
                    fos.write(foobar[i]);
                }
                fos.close();
            }
            catch(FileNotFoundException fex)
            {
                logger.error(fex);
                return false;
            }
            catch(IOException ioex)
            {
                logger.error(ioex);
                return false;
            }
        }
        else if(value instanceof InputStream)
        {
            try
            {
                InputStream is = (InputStream) value;

                String tempFileName = filePath.trim() + fileName;
                dataFile = new File(tempFileName);
                FileOutputStream fos = new FileOutputStream(dataFile);

                byte[] buffer = new byte[1];
                while (is.read(buffer) > 0)
                {
                    fos.write(buffer);
                }
                fos.close();

                return true;
            }
            catch(FileNotFoundException fex)
            {
                logger.error(fex);
                return false;
            }
            catch(IOException ioex)
            {
                logger.error(ioex);
                return false;
            }
        }

        return false;
    }

    public boolean setSelectedFile(File selectedFile)
    {
        if(selectedFile != null)
        {
            dataFile = selectedFile;
            filePath = selectedFile.getPath();
            fileName = selectedFile.getName();
            return true;
        }

        return false;
    }

}
