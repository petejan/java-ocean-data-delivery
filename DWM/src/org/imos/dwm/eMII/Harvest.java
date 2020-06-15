/*
 * Copyright (c) 2014, jan079
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.imos.dwm.eMII;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayFloat;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.time.CalendarDate;

/**
 *
 * @author jan079
 */
public class Harvest
{

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Connection conn = null;

    public Harvest()
    {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void dbConnect()
    {
        try
        {
            Class.forName("org.postgresql.Driver");

            String url = "jdbc:postgresql://localhost/harvest";
            Properties props = new Properties();
            props.setProperty("user", "abos_ts");
            props.setProperty("password", "");
            conn = DriverManager.getConnection(url, props);

            System.out.println("Harvest::dbConnect() Connected to database");
        }
        catch (ClassNotFoundException ex)
        {
            Logger.getLogger(Harvest.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SQLException ex)
        {
            Logger.getLogger(Harvest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    int fileId = -1;
    List<CalendarDate> dates;

    private void insertFile() throws SQLException, IOException
    {
        Statement s = conn.createStatement();

        String sql = "INSERT INTO indexed_file VALUES (nextval('indexed_file_id'), '" + netFile.getName() + "',''," + netFile.length() + ",'" + sdf.format(new Date()) + "')";

        // System.out.println("Harvest::insertFile() SQL " + sql);
        s.execute(sql);

        ResultSet rs = s.executeQuery("SELECT currval('indexed_file_id')");
        rs.next();
        fileId = rs.getInt(1);

        System.out.println("Harvest::insertFile() fileId " + fileId);

        Variable varT = ncd.findVariable("TIME");
        if (varT == null)
        {
            varT = ncd.findVariable("TIME");
        }
        if (varT == null)
        {
            System.err.println("No TIME variable, giving up");

            return;
        }
        System.out.println("Harvest::insertFile() Time type " + varT.getDataType());

        Attribute tunit = varT.findAttribute("units");

        System.out.println("Harvest::insertFile() Time type " + tunit.getDataType() + " value " + tunit.toString());

        CoordinateAxis1DTime tm = CoordinateAxis1DTime.factory(ncd, new VariableDS(null, varT, true), null);
        dates = tm.getCalendarDates();

        Date t0 = new Date(dates.get(0).getMillis());
        System.out.println("Harvest::insertFile() " + sdf.format(t0) + " CoordinateAxis1DTime " + tm);
    }

    private class DepthTimeSeries
    {

        Variable v;
        int i;
        float depth;
        String instrument = null;
        String serialNumber = null;
        String standardName = null;
        String units = null;
        int tsId;
        ArrayFloat.D2 array;
        ArrayByte.D2 qc;

        public String toString()
        {
            return "depth=" + depth + " i=" + i + " instrument=" + instrument + " serialNumber=" + serialNumber + " variable=" + v.getShortName() + " standard_name " + standardName + " units " + units;
        }
    }

    TreeMap<Float, List<DepthTimeSeries>> map = new TreeMap<Float, List<DepthTimeSeries>>();

    int timeSeriesId = -1;

    private void insertTimeSeries() throws SQLException, IOException
    {
        String site_code = ncd.findGlobalAttribute("site_code").getStringValue();
        String platform_code = ncd.findGlobalAttribute("platform_code").getStringValue();
        String deployment_code = ncd.findGlobalAttribute("deployment_code").getStringValue();
        String time_coverage_start = ncd.findGlobalAttribute("time_coverage_start").getStringValue();
        String time_coverage_end = ncd.findGlobalAttribute("time_coverage_end").getStringValue();
        String title = ncd.findGlobalAttribute("title").getStringValue();

        double lat = ncd.findVariable(null, "LATITUDE").readScalarDouble();
        double lon = ncd.findVariable(null, "LONGITUDE").readScalarDouble();

        System.out.println("Harvest::insertTimeSeries() lat, lon " + lat + " " + lon);

        // create a time series for each (unique) nominal depth
        List<Variable> vars = ncd.getVariables();

        // build a list of aux variable names
        ArrayList<String> auxVarNames = new ArrayList<String>();
        for (Variable var : vars)
        {
            System.out.println("Harvest::insertTimeSeries() finding Aux variables " + var.getShortName());

            Attribute aAv = var.findAttribute("ancillary_variables");
            if (aAv != null)
            {
                auxVarNames.add(aAv.getStringValue());
                System.err.println(" has AUX var : " + aAv.getStringValue());
            }
        }
        for (Variable var : vars)
        {
            System.out.println("Harvest::insertTimeSeries() extracting depths for var " + var.getShortName());

            if (!auxVarNames.contains(var.getShortName()))
            {
                if (var.getDimension(0).getShortName().equals("TIME") && !var.getShortName().equals("TIME"))
                {
                    Dimension dDepth = var.getDimension(1);
                    Variable vDepth = ncd.findVariable(dDepth.getShortName());
                    if (vDepth != null)
                    {
                        ArrayFloat.D1 depths;
                        depths = (ArrayFloat.D1) vDepth.read();
                        for (int i = 0; i < depths.getSize(); i++)
                        {
                            float depth = depths.get(i);

                            DepthTimeSeries dts = new DepthTimeSeries();
                            dts.depth = depth;
                            dts.i = i;
                            String inst[] = var.findAttribute("sensor_name").getStringValue().split(";");
                            String sn[] = var.findAttribute("sensor_serial_number").getStringValue().split(";");

                            dts.instrument = inst[i];
                            dts.serialNumber = sn[i];
                            dts.v = var;

                            Attribute aSn = var.findAttribute("standard_name");
                            if (aSn == null)
                            {
                                aSn = var.findAttribute("long_name");
                            }
                            if (aSn == null)
                            {
                                aSn = var.findAttribute("name");
                            }
                            if (aSn != null)
                            {
                                dts.standardName = aSn.getStringValue();
                            }
                            dts.units = var.findAttribute("units").getStringValue();

                            List<DepthTimeSeries> current = map.get(depth);
                            if (current == null)
                            {
                                current = new ArrayList<DepthTimeSeries>();
                                current.add(dts);
                                map.put(depth, current);
                            }
                            else
                            {
                                current.add(dts);
                            }
                        }
                    }
                    else
                    {
                        System.out.println("Harvest::insertTimeSeries() Depth not found");
                    }
                }
            }
        }
    //  id bigserial NOT NULL,
        //  file_id bigint NOT NULL,
        //  site_code text NOT NULL,
        //  platform_code text NOT NULL,
        //  deployment_code text NOT NULL,
        //  "LATITUDE" double precision,
        //  "LATITUDE_quality_control" character(1),
        //  "LONGITUDE" double precision,
        //  "LONGITUDE_quality_control" character(1),
        //  geom geometry(Geometry,4326),
        //  instrument_nominal_depth real,
        //  site_depth_at_deployment real,
        //  instrument text,
        //  instrument_serial_number text,
        //  time_coverage_start timestamp with time zone,
        //  time_coverage_end timestamp with time zone,
        //  time_deployment_start timestamp with time zone,
        //  time_deployment_end timestamp with time zone,
        //  comment text,
        //  history text,
        //  depth_b boolean,
        //  sea_water_temperature_b boolean,
        //  sea_water_electrical_conductivity_b boolean,
        //  sea_water_salinity_b boolean,
        //  sea_water_pressure_b boolean,
        //  sea_water_pressure_due_to_sea_water_b boolean,

        String sql = "INSERT INTO timeseries ("
                + "id, "
                + "file_id, "
                + "site_code, "
                + "platform_code, "
                + "deployment_code, "
                + "\"LATITUDE\", "
                + "\"LATITUDE_quality_control\", "
                + "\"LONGITUDE\", "
                + "\"LONGITUDE_quality_control\", "
                + "geom, "
                + "instrument_nominal_depth, "
                + "instrument, "
                + "instrument_serial_number, "
                + "time_coverage_start, "
                + "time_coverage_end, "
                + "time_deployment_start, "
                + "time_deployment_end, "
                + "comment, "
                + "history, "
                + "depth_b, "
                + "sea_water_temperature_b, "
                + "sea_water_electrical_conductivity_b, "
                + "sea_water_salinity_b, "
                + "sea_water_pressure_b, "
                + "sea_water_pressure_due_to_sea_water_b) "
                + " VALUES ( "
                + " nextval('timeseries_id'), "
                + fileId + ","
                + "'" + site_code + "',"
                + "'" + platform_code + "',"
                + "'" + deployment_code + "',"
                + lat + "," + "1,"
                + lon + "," + "1,"
                + "ST_SetSRID(ST_MakePoint("+lon+","+lat+"), 4326),"
                + "?,"
                + "?,"
                + "?,"
                + "'" + time_coverage_start + "',"
                + "'" + time_coverage_end + "',"
                + "'" + time_coverage_start + "',"
                + "'" + time_coverage_end + "',"
                + "'" + title + "',"
                + "null,"
                + "false,"
                + "?,"
                + "?,"
                + "?,"
                + "false,"
                + "?"
                + ")";

        PreparedStatement s = conn.prepareStatement(sql);
        Statement q = conn.createStatement();

        boolean hasTemperature = false;
        boolean hasCond = false;
        boolean hasSalinity = false;
        boolean hasPressureRel = false;

        for (Map.Entry<Float, List<DepthTimeSeries>> entry : map.entrySet())
        {
            System.out.println("Harvest::insertTimeSeries() map key : " + entry.getKey());
            List<DepthTimeSeries> current = entry.getValue();
            for (DepthTimeSeries dts : current)
            {
                System.out.println(" " + dts);
                if (dts.standardName.equals("sea_water_temperature"))
                {
                    hasTemperature = true;
                }
                if (dts.standardName.equals("sea_water_electrical_conductivity"))
                {
                    hasCond = true;
                }
                if (dts.standardName.equals("sea_water_practical_salinity"))
                {
                    hasSalinity = true;
                }
                if (dts.standardName.equals("sea_water_pressure_due_to_sea_water"))
                {
                    hasPressureRel = true;
                }
            }
            System.out.println("Harvest::insertTimeSeries() Site Code, platform_code, deployment_code " + site_code + " " + platform_code + " " + deployment_code);

            s.setFloat(1, entry.getKey());
            s.setString(2, current.get(0).instrument.trim());
            s.setString(3, current.get(0).serialNumber.trim());
            s.setBoolean(4, hasTemperature);
            s.setBoolean(5, hasCond);
            s.setBoolean(6, hasSalinity);
            s.setBoolean(7, hasPressureRel);

            // System.out.println("Harvest::insertTimeSeries() timeseries SQL : " + s.toString());
            s.execute();

            ResultSet rs = q.executeQuery("SELECT currval('timeseries_id')");
            rs.next();
            timeSeriesId = rs.getInt(1);

            for (DepthTimeSeries dts : current)
            {
                dts.tsId = timeSeriesId;
            }

            System.out.println("Harvest::insertTimeSeries() timeSeriesId " + timeSeriesId);
        }
    }

    public void insertMeasurements() throws SQLException, IOException
    {
        int measurementsAdded = 0;
//  ts_id bigint NOT NULL,
//  index bigint NOT NULL,
//  "TIME" timestamp with time zone NOT NULL,
//  "TIME_quality_control" character(1),
//  "DEPTH" real,
//  "DEPTH_quality_control" character(1),
//  "TEMP" real,
//  "TEMP_quality_control" character(1),
//  "CNDC" real,
//  "CNDC_quality_control" character(1),
//  "PSAL" real,
//  "PSAL_quality_control" character(1),
//  "PRES" real,
//  "PRES_quality_control" character(1),
//  "PRES_REL" real,
//  "PRES_REL_quality_control" character(1),

        String sql = "INSERT INTO measurement ("
                + "ts_id, "
                + "index, "
                + "\"TIME\", "
                + "\"TIME_quality_control\", "
                + "\"DEPTH\", "
                + "\"DEPTH_quality_control\", "
                + "\"TEMP\", "
                + "\"TEMP_quality_control\", "
                + "\"CNDC\", "
                + "\"CNDC_quality_control\", "
                + "\"PSAL\", "
                + "\"PSAL_quality_control\", "
                + "\"PRES\", "
                + "\"PRES_quality_control\", "
                + "\"PRES_REL\", "
                + "\"PRES_REL_quality_control\") "
                + " VALUES ( ?, "
                + " nextval('measurement_id'), "
                + "?," // TIME
                + "?,"
                + "?," // DEPTH
                + "0,"
                + "?," // TEMP
                + "?,"
                + "?," // CNDC
                + "?,"
                + "?," // PSAL
                + "?,"
                + "null," // PRES
                + "9,"
                + "?," // PRES_REL
                + "?"
                + ")";

        PreparedStatement s = conn.prepareStatement(sql);

        boolean hasTemperature = false;
        boolean hasCond = false;
        boolean hasSalinity = false;
        boolean hasPressureRel = false;
        float d;

        for (Map.Entry<Float, List<DepthTimeSeries>> entry : map.entrySet())
        {
            System.out.println("Harvest::insertMeasurements() map key : " + entry.getKey());
            List<DepthTimeSeries> current = entry.getValue();
            int[] shape = current.get(0).v.getShape();
            s.setInt(1, current.get(0).tsId);
            for (DepthTimeSeries dts : current)
            {
                dts.array = (ArrayFloat.D2) dts.v.read();
                Attribute aAV = dts.v.findAttribute("ancillary_variables");
                Variable qc = null;
                if (aAV != null)
                {
                    qc = ncd.findVariable(aAV.getStringValue());
                    //System.out.println("Harvest::insertMeasurements() read QC " + aAV.getStringValue() + " " + qc);
                    dts.qc = (ArrayByte.D2) qc.read();
                }
                if (qc == null)
                {
                    dts.qc = null;
                }
            }
            s.setFloat(4, entry.getKey());
            for (int i = 0; i < shape[0]; i++)
            {
                s.setTimestamp(2, new Timestamp(dates.get(i).getMillis()));
                s.setString(3, "1");
                
                s.setFloat(5, Float.NaN);
                s.setInt(6, 9);
                s.setFloat(7, Float.NaN);
                s.setInt(8, 9);
                s.setFloat(9, Float.NaN);
                s.setInt(10, 9);
                s.setFloat(11, Float.NaN);
                s.setInt(12, 9);

                for (DepthTimeSeries dts : current)
                {
                    //System.out.println(" " + dts);
                    if (dts.standardName.equals("sea_water_temperature"))
                    {
                        d = dts.array.get(i, dts.i);
                        if (!Float.isNaN(d))
                        {
                            hasTemperature = true;
                            s.setFloat(5, d);
                            if (dts.qc != null)
                            {
                                s.setInt(6, dts.qc.get(i, dts.i));
                            }
                        }
                    }
                    if (dts.standardName.equals("sea_water_electrical_conductivity"))
                    {
                        d = dts.array.get(i, dts.i);
                        if (!Float.isNaN(d))
                        {
                            hasCond = true;
                            s.setFloat(7, d);
                            if (dts.qc != null)
                            {
                                s.setInt(8, dts.qc.get(i, dts.i));
                            }
                        }
                    }
                    if (dts.standardName.equals("sea_water_practical_salinity"))
                    {
                        d = dts.array.get(i, dts.i);
                        if (!Float.isNaN(d))
                        {
                            hasSalinity = true;
                            s.setFloat(9, d);
                            if (dts.qc != null)
                            {
                                s.setInt(10, dts.qc.get(i, dts.i));
                            }
                        }
                    }
                    if (dts.standardName.equals("sea_water_pressure_due_to_sea_water"))
                    {
                        d = dts.array.get(i, dts.i);
                        if (!Float.isNaN(d))
                        {
                            hasPressureRel = true;
                            s.setFloat(11, d);
                            if (dts.qc != null)
                            {
                                s.setInt(12, dts.qc.get(i, dts.i));
                            }
                        }
                    }
                }

                //System.out.println("SQL " + s.toString());
                if (hasTemperature || hasCond || hasSalinity || hasPressureRel)
                {
                    s.execute();
                    measurementsAdded++;
                }
            }
            System.out.println("Harvest::insertMeasurements() added " + measurementsAdded);
            
        }

        System.out.println("Harvest::insertMeasurements() added " + measurementsAdded);
    }
    NetcdfDataset ncd;
    File netFile;

    public void read(String filename)
    {
        ncd = null;
        try
        {
            System.out.println("Harvest::read() " + filename);

            netFile = new File(filename);
            ncd = NetcdfDataset.openDataset(filename);

            insertFile();
            insertTimeSeries();
            insertMeasurements();
        }
        catch (IOException ioe)
        {
            System.out.println("trying to open " + filename + " " + ioe);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (null != ncd)
            {
                try
                {
                    ncd.close();
                }
                catch (IOException ioe)
                {
                    System.out.println("trying to close " + filename + " " + ioe);
                }
            }
        }
    }

    static public void main(String[] args)
    {
        Harvest h = new Harvest();

        if (args.length == 0)
        {
            System.err.println("Usage : Harvest <filename> ...");

            return;
        }

        h.dbConnect();

        for (String s : args)
        {
            h.read(s);
        }
    }

}
