/*
 * Copyright (c) 2016, pete
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
package org.imos.abos.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pete
 */
public class GetMRUtimeOffset
{
	// Generate input file with
	// find MRU -name "*.TXT" -exec grep -h "string GPRMC.*,A,.*" {} \; > SOFS-5-RMC.txt
	
    public static void main(String args[])
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        
        SimpleDateFormat sdfCR1000 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfGPS = new SimpleDateFormat("HHmmss.SS ddMMyy");
        
        //System.out.println("Arg 0 " + args[0]);
        
        File f = new File(args[0]);
        
        try
        {
            BufferedReader lbf = new BufferedReader(new FileReader(f));
            
            for (String x = lbf.readLine(); x != null; x = lbf.readLine())
            {
                String[] s = x.split(" |,");
                
                if (s.length > 16)
                {
                    //System.out.println("split " + s[0] + " " + s[1] + " --- " + s[8] + " " + s[16]);
                    String tsCR1000 = s[0] + " " + s[1];
                    String tsGPS = s[8] + " " + s[16];

                    try
                    {
                        Date tCR1000 = sdfCR1000.parse(tsCR1000);
                        Date tGPS = sdfGPS.parse(tsGPS);

                        long tdiff = (tCR1000.getTime() - tGPS.getTime()) / 1000;
                        System.out.println("date CR1000 : " + sdfCR1000.format(tCR1000) + " GPS : " + sdfCR1000.format(tGPS) + " diff :" + tdiff + " seconds");
                    }
                    catch (ParseException ex)
                    {
//                        Logger.getLogger(GetMRUtimeOffset.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(GetMRUtimeOffset.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(GetMRUtimeOffset.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
}
