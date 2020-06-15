/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imos.dwm.dbms;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.wiley.LabMaster.Common;

/**
 *
 * @author peter
 */
public class Array2DInstrumentData extends ArrayInstrumentData
{
    private Double[][] parameterValue2D;
    
    protected boolean doInsert(PreparedStatement psql)
    {
        try
        {
            int i = 1;

            psql.setInt(i++, sourceFileID);
            psql.setInt(i++, instrumentID);
            psql.setString(i++, mooringID);
            psql.setTimestamp(i++, dataTimestamp);
            psql.setDouble(i++, latitude);
            psql.setDouble(i++, longitude);
            psql.setDouble(i++, depth);
            psql.setString(i++, parameterCode);
 
            Array array = Common.getConnection().createArrayOf("numeric", parameterValue2D);
            psql.setArray(i++, array);

            psql.setString(i++, qualityCode);

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
    
    public boolean setParameterValue(Object[][] value)
    {
        if(value == null)
        {
            parameterValue2D = null;
            isEdited = true;
            return true;
        }
        if(value instanceof Double[][])
        {
            parameterValue2D = ((Double[][]) value);
            isEdited = true;
            return true;
        }

        return false;
    }

    public Double[][] getParameterValue2D()
    {
        return parameterValue2D;
    }

    
    
    
}
