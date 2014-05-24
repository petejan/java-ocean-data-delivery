/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imos.abos.netcdf;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.imos.abos.dbms.Mooring;

import org.wiley.core.Common;
import org.wiley.util.SQLWrapper;
import org.wiley.util.StringUtilities;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

/**
 *
 * @author jan079
 */
public class NetCDFfile
{
    protected TimeZone tz = TimeZone.getTimeZone("UTC");
    private static Logger logger = Logger.getLogger(NetCDFfile.class.getName());
    protected NetcdfFileWriter dataFile;
    protected static SQLWrapper query = new SQLWrapper();  
    private String authority = "OS";
    private String site = "SOTS";
    private String mooringString = null;
    private String deployment = null;
    
    private Mooring mooring = null;
    
    public Variable vTime;
    public Variable vLat;
    public Variable vLon;
    public Dimension timeDim;
    public Dimension latDim;
    public Dimension lonDim;
    public final boolean timeIsDoubleDays = true;
    public SimpleDateFormat netcdfDate;
    long anchorTime;
    
    public NetCDFfile(NetcdfFileWriter d)
    {
        TimeZone.setDefault(tz);
        dataFile = d;        
        netcdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        anchorTime = 0;
        try
        {
            java.util.Date ts = df.parse("1950-01-01 00:00:00");
            anchorTime = ts.getTime();
        }
        catch (ParseException pex)
        {
            logger.error(pex);
        }        
    }
    
    public void setMooring(Mooring m)
    {
        mooring = m;
        deployment = mooring.getMooringID();
        mooringString = deployment.substring(0, deployment.indexOf("-"));
    }

    public void setAuthority(String a)
    {
        authority = a;
    }
    public void setSite(String s)
    {
        site = s;
    }
    public String getMooringName()
    {
        return mooringString;
    }
    public String getDeployment()
    {
        return deployment;
    }
    
    private void addGlobal(String from, Vector attributeSet)
    {
        if (attributeSet != null && attributeSet.size() > 0)
        {
            for (int i = 0; i < attributeSet.size(); i++)
            {
                Vector row = (Vector) attributeSet.get(i);
                String name = (String)(row.get(0));
                String type = (String)(row.get(1));
                String value = (String)(row.get(2));

                logger.debug(from + " : " + name + " = " + value);

                if (type.startsWith("NUMBER"))
                {
                    dataFile.addGroupAttribute(null, new Attribute(name.trim(), new Double(value.trim())));
                }
                else
                {
                    dataFile.addGroupAttribute(null, new Attribute(name.trim(), value.trim()));
                }
            }
        }
    }
    
    public void writeGlobalAttributes()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'");
        df.setTimeZone(tz);

        query.setConnection(Common.getConnection());
        
        String SQL = "SELECT attribute_name, attribute_type, attribute_value FROM netcdf_attributes "
                + " WHERE naming_authority = '*'"
                + " AND site = '*' AND mooring = '*' AND deployment = '*' AND instrument_id ISNULL AND parameter = '*'" 
                + " ORDER BY attribute_name";
        
        query.executeQuery(SQL);
        Vector attributeSet = query.getData();
        addGlobal("GLOBAL", attributeSet);

        SQL = "SELECT attribute_name, attribute_type, attribute_value FROM netcdf_attributes "
                + " WHERE naming_authority = " + StringUtilities.quoteString(authority)
                + " AND site = '*' AND mooring = '*' AND deployment = '*' AND instrument_id ISNULL AND parameter = '*'" 
                + " ORDER BY attribute_name";
        
        query.executeQuery(SQL);
        attributeSet = query.getData();
        addGlobal("AUTHORITY", attributeSet);

        SQL = "SELECT attribute_name, attribute_type, attribute_value FROM netcdf_attributes "
                + " WHERE (naming_authority = " + StringUtilities.quoteString(authority) + " OR naming_authority = '*')"
                + " AND site = " + StringUtilities.quoteString(site) 
                + " AND mooring = '*' AND deployment = '*' AND instrument_id ISNULL AND parameter = '*'" 
                + " ORDER BY attribute_name";
        
        query.executeQuery(SQL);
        attributeSet = query.getData();
        addGlobal("SITE", attributeSet);

        SQL = "SELECT attribute_name, attribute_type, attribute_value FROM netcdf_attributes "
                + " WHERE (naming_authority = " + StringUtilities.quoteString(authority) + " OR naming_authority = '*')"
                + " AND (site = " + StringUtilities.quoteString(site) + " OR site = '*')"
                + " AND mooring = " + StringUtilities.quoteString(mooringString) 
                + " AND deployment = '*' AND instrument_id ISNULL AND parameter = '*'" 
                + " ORDER BY attribute_name";
        
        query.executeQuery(SQL);
        attributeSet = query.getData();
        addGlobal("MOORING", attributeSet);

        SQL = "SELECT attribute_name, attribute_type, attribute_value FROM netcdf_attributes "
                + " WHERE (naming_authority = " + StringUtilities.quoteString(authority) + " OR naming_authority = '*')"
                + " AND (site = " + StringUtilities.quoteString(site) + " OR site = '*')"
                + " AND (mooring = " + StringUtilities.quoteString(mooringString) + " OR mooring = '*')"
                + " AND deployment = " + StringUtilities.quoteString(deployment) 
                + " AND instrument_id ISNULL AND parameter = '*'" 
                + " ORDER BY attribute_name";
        
        query.executeQuery(SQL);
        attributeSet = query.getData();
        addGlobal("DEPLOYMENT", attributeSet);
        
        dataFile.addGroupAttribute(null, new Attribute("date_created", df.format(Calendar.getInstance().getTime())));
    }

    public void writeCoordinateVariableAttributes()
    {
        vTime.addAttribute(new Attribute("name", "time"));
        vTime.addAttribute(new Attribute("standard_name", "time"));
        vTime.addAttribute(new Attribute("long_name", "time of measurement"));
        if (timeIsDoubleDays)
        {
            vTime.addAttribute(new Attribute("units", "days since 1950-01-01T00:00:00Z"));
        }
        else
        {
            vTime.addAttribute(new Attribute("units", "hours since 1950-01-01T00:00:00Z"));
        }
        vTime.addAttribute(new Attribute("axis", "T"));
        vTime.addAttribute(new Attribute("valid_min", 0.0));
        vTime.addAttribute(new Attribute("valid_max", 999999999));
        vTime.addAttribute(new Attribute("calendar", "gregorian"));

        vLat.addAttribute(new Attribute("standard_name", "latitude"));
        vLat.addAttribute(new Attribute("long_name", "latitude of anchor"));
        vLat.addAttribute(new Attribute("units", "degrees_north"));
        vLat.addAttribute(new Attribute("axis", "Y"));
        vLat.addAttribute(new Attribute("valid_min", -90.0));
        vLat.addAttribute(new Attribute("valid_max", 90.0));
        vLat.addAttribute(new Attribute("reference", "WGS84"));
        vLat.addAttribute(new Attribute("coordinate_reference_frame", "urn:ogc:crs:EPSG::4326"));
        vLat.addAttribute(new Attribute("comment", "Anchor Location"));   
        
        vLon.addAttribute(new Attribute("standard_name", "longitude"));
        vLon.addAttribute(new Attribute("long_name", "longitude of anchor"));
        vLon.addAttribute(new Attribute("units", "degrees_east"));
        vLon.addAttribute(new Attribute("axis", "X"));
        vLon.addAttribute(new Attribute("valid_min", -180.0));
        vLon.addAttribute(new Attribute("valid_max", 180.0));
        vLon.addAttribute(new Attribute("reference", "WGS84"));
        vLon.addAttribute(new Attribute("coordinate_reference_frame", "urn:ogc:crs:EPSG::4326"));
        vLon.addAttribute(new Attribute("comment", "Anchor Location"));        
    }       

    public void writePosition(Double latitudeIn, Double longitudeOut) throws IOException, InvalidRangeException
    {
        ArrayDouble.D1 lat = new ArrayDouble.D1(1);
        ArrayDouble.D1 lon = new ArrayDouble.D1(1);

        lat.set(0, latitudeIn);
        lon.set(0, longitudeOut);

        dataFile.write(vLat, lat);
        dataFile.write(vLon, lon);
    }

    public ucar.ma2.Array times;
    
    public void createCoordinateVariables(int RECORD_COUNT)
    {
        timeDim = dataFile.addDimension(null, "TIME", RECORD_COUNT);

        latDim = dataFile.addDimension(null, "LATITUDE", 1);
        vLat = dataFile.addVariable(null, "LATITUDE", DataType.DOUBLE, "LATITUDE");
        lonDim = dataFile.addDimension(null, "LONGITUDE", 1);
        vLon = dataFile.addVariable(null, "LONGITUDE", DataType.DOUBLE, "LONGITUDE");
    }
    
    public void writeCoordinateVariables(ArrayList<Timestamp> timeArray)
    {
        if (timeIsDoubleDays)
        {
            times = new ArrayDouble.D1(timeDim.getLength());
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(tz);
            for (int i = 0; i < timeDim.getLength(); i++)
            {
                Timestamp ts = timeArray.get(i);
                long offsetTime = (ts.getTime() - anchorTime) / 1000;
                double elapsedHours = ((double) offsetTime) / (3600 * 24);
                times.setDouble(i, elapsedHours);
            }
        }
        else
        {
            times = new ArrayInt.D1(timeDim.getLength());
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(tz);
            for (int i = 0; i < timeDim.getLength(); i++)
            {
                Timestamp ts = timeArray.get(i);
                long offsetTime = (ts.getTime() - anchorTime) / 1000;
                int elapsedHours = (int) offsetTime / 3600;
                times.setInt(i, elapsedHours);
            }
        }

        ArrayList<Dimension> tdlist = new ArrayList<Dimension>();
        tdlist.add(timeDim);
        if (timeIsDoubleDays)
        {
            vTime = dataFile.addVariable(null, "TIME", DataType.DOUBLE, tdlist);
        }
        else
        {
            vTime = dataFile.addVariable(null, "TIME", DataType.INT, tdlist);
        }
    }
}
