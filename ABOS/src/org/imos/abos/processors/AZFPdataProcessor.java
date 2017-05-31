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
package org.imos.abos.processors;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.parsers.AZFPdataParser;
import org.wiley.core.Common;

/**
 *
 * @author jan079
 */
public class AZFPdataProcessor
{
    HashMap<String, AZFPdataParser.FileType> hm = new HashMap<String, AZFPdataParser.FileType>();

    String param;
    AZFPdataParser.FileType fileType;
    
    Map<AZFPdataParser.FileType, String> stringType = new EnumMap<AZFPdataParser.FileType, String>(AZFPdataParser.FileType.class);
    Map<AZFPdataParser.FileType, Integer> indexType = new EnumMap<AZFPdataParser.FileType, Integer>(AZFPdataParser.FileType.class);

    int index;   
    
    public AZFPdataProcessor()
    {
        sumN[0] = 0;
        sumN[1] = 0;
        sumN[2] = 0;
        sumN[3] = 0;
        stringType.put(AZFPdataParser.FileType.C_38, "SV_038");
        stringType.put(AZFPdataParser.FileType.C_125, "SV_125");
        stringType.put(AZFPdataParser.FileType.C_200, "SV_200");
        stringType.put(AZFPdataParser.FileType.C_455, "SV_455");
        
        indexType.put(AZFPdataParser.FileType.C_38, 0);
        indexType.put(AZFPdataParser.FileType.C_125, 1);
        indexType.put(AZFPdataParser.FileType.C_200, 2);
        indexType.put(AZFPdataParser.FileType.C_455, 3);
        
        hm.put("SV_038", AZFPdataParser.FileType.C_38);
        hm.put("SV_125", AZFPdataParser.FileType.C_125);
        hm.put("SV_200", AZFPdataParser.FileType.C_200);
        hm.put("SV_455", AZFPdataParser.FileType.C_455);        
        
        rowSS[0] = new SummaryStatistics();
        rowSS[1] = new SummaryStatistics();
        rowSS[2] = new SummaryStatistics();
        rowSS[3] = new SummaryStatistics();
        rowSSCM[0] = new SummaryStatistics();
        rowSSCM[1] = new SummaryStatistics();
        rowSSCM[2] = new SummaryStatistics();
        rowSSCM[3] = new SummaryStatistics();
    }

    public static void main(String args[])
    {
        String $HOME = System.getProperty("user.home");
        PropertyConfigurator.configure("log4j.properties");
        Common.build("ABOS.properties");

        AZFPdataProcessor proc = new AZFPdataProcessor();

        proc.run();
    }

    double[] svv = new double[10000];
    double[][] sum = new double[10000][4];
    double vPk;
    int iPk;
    double zscale = 181.33063 / 10000;
    double d;
    int[] sumN = new int[4];

    SummaryStatistics[] rowSS = new SummaryStatistics[4];
    SummaryStatistics[] rowSSCM = new SummaryStatistics[4];

    public void sample(Double[] svs, AZFPdataParser.FileType fileType)
    {
        vPk = Double.MIN_VALUE;
        iPk = 0;

        for (int i = 0; i < svs.length; i++)
        {
            svv[i] = Math.pow(10, svs[i] / 10);
            d = i * zscale;
            //System.out.print(" ," + svs[i]);
            if (fileType == AZFPdataParser.FileType.C_455) // 455 kHz
            {
                if ((i > 500) && (i < 2500))
                {
                    if (vPk < svv[i])
                    {
                        vPk = svv[i];
                        iPk = i;
                    }
                    rowSS[index].addValue(svv[i]);
                    rowSSCM[index].addValue(svv[i] * d);                    
                }
            }
            else if ((i > 1000) && ((i < 1500) || (i > 2000))) // take out inital ring down and surface return
            {
                if (vPk < svv[i])
                {
                    vPk = svv[i];
                    iPk = i;
                }
                rowSS[index].addValue(svv[i]);
                rowSSCM[index].addValue(svv[i] * d);
            }
            sum[i][index] += svv[i];
            sumN[index]++;
        }
    }

    public void output()
    {
        double sa = 10 * Math.log10(rowSS[index].getSum() / (rowSS[index].getN() * zscale));
        double cm = rowSSCM[index].getSum() / rowSS[index].getSum();
        double ea = Math.pow(rowSS[index].getSum(), 2) / rowSS[index].getSumsq();
        System.out.print("," + sa + "," + cm + "," + ea + "," + 10 * Math.log10(rowSS[index].getMax()) + "," + rowSS[index].getStandardDeviation() + "," + rowSS[index].getSumsq() + "," + 10 * Math.log10(vPk) + "," + (iPk * zscale));
        System.out.println();
    }

    private void run()
    {
        try
        {
            Connection con = Common.getConnection();
            con.setAutoCommit(false);

            Statement stmt = con.createStatement();
            stmt.setFetchSize(100);
            ResultSet rs = stmt.executeQuery("SELECT data_timestamp, parameter_code, parameter_value FROM array_instrument_data WHERE "
                    + "mooring_id = 'SOFS-3-2012' AND parameter_code IN ('SV_038','SV_125','SV_200','SV_455') "
                    + "AND data_timestamp > '2012-08-01' ORDER BY parameter_code, data_timestamp");

            int lastSv = -1;

            int count = 0;
            System.out.println("ts ,param ,sv, sa ,cm ,ea ,max ,sd ,sumSq, pk, dpk");
            Timestamp t = null;
            while (rs.next())
            {
                t = rs.getTimestamp("data_timestamp");
                param = rs.getString("parameter_code").trim();

                if (count == 0)
                {
                    System.err.println("first ts " + t);
                }
                
                fileType = hm.get(param);
                index = indexType.get(fileType);
                if ((lastSv != -1) && (lastSv != index))
                {
                    System.out.println();
                }
                lastSv = index;

                Array z = rs.getArray("parameter_value");
                Double[] svs = (Double[]) z.getArray();

                sampleInit(t, fileType);
                sample(svs, fileType);
                output();
                count++;
            }
            System.err.println("last ts " + t);
            
            System.out.println();
            for (int i = 0; i < 10000; i++)
            {
                System.out.print(" ," + i * zscale);
            }
            System.out.println();

            for (int j = 0; j < 4; j++)
            {
                System.out.print(j);
                for (int i = 0; i < 10000; i++)
                {
                    System.out.print(" ," + 10 * Math.log10(sum[i][j] / sumN[j]));
                }
                System.out.println();
            }

            rs.close();
        }
        catch (SQLException ex)
        {
            Logger.getLogger(AZFPdataProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sampleInit(Timestamp dataTimestamp, AZFPdataParser.FileType fileType)
    {
        this.fileType = fileType;
        index = indexType.get(fileType);
        param = stringType.get(fileType);

        rowSS[index].clear();
        rowSSCM[index].clear();

        param = stringType.get(fileType);
        index = indexType.get(fileType);

        System.out.print(dataTimestamp + " ," + param);
    }
}
