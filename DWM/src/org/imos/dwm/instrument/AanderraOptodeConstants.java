/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.dwm.instrument;

import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.imos.dwm.dbms.Instrument;
import org.imos.dwm.dbms.InstrumentCalibrationValue;
import org.imos.dwm.dbms.Mooring;

/**
 *
 * @author peter
 */
public class AanderraOptodeConstants extends AbstractInstrumentConstants
{
    private static Logger logger = Logger.getLogger(AanderraOptodeConstants.class.getName());

    public double C0Coeffs[] =
    {
        //4.94529E+03, -1.67764E+02, 3.41751E+00, -3.07691E-02
    };
    public double C1Coeffs[] =
    {
        //-2.69898E+02, 8.31507E+00, -1.74439E-01, 1.62807E-03
    };
    public double C2Coeffs[] =
    {
        //5.97638E+00, -1.67971E-01, 3.75245E-03, -3.67960E-05
    };
    public double C3Coeffs[] =
    {
        //-6.18186E-02, 1.60203E-03, -3.91116E-05, 4.02796E-07
    };
    public double C4Coeffs[] =
    {
        //2.45035E-04, -5.90218E-06, 1.59999E-07, -1.71557E-09
    };

    /*
     * these all need to be parameterised somewhere!
     */
    public Double TempVoltConstant = -5.00;
    public Double TempVoltMultiplier = 9.00;

    public Double BPhaseVoltConstant = 10.0;
    public Double BPhaseVoltMultiplier = 12.0;

    public Double BPhaseConstant = 0.894295;
    public Double BPhaseMultiplier = 1.10633;
    
    public Double UchidaC1Coefficient;
    public Double UchidaC2Coefficient;
    public Double UchidaC3Coefficient;
    public Double UchidaC4Coefficient;
    public Double UchidaC5Coefficient;
    public Double UchidaC6Coefficient;
    public Double UchidaC7Coefficient;
    public Double UchidaC8Coefficient;
    public Double UchidaC9Coefficient;

    public void setInstrumentAndMooring(Instrument ins, Mooring m)
    {
        if(ins != null && m != null)
        {
            //
            // get any/all calibration data values we need
            //
            ArrayList<InstrumentCalibrationValue> values = InstrumentCalibrationValue.selectByInstrumentAndMooring
                                                            (ins.getInstrumentID(), m.getMooringID());
            if(values != null && values.size() > 0)
            {

                for(int i = 0; i < values.size(); i++)
                {
                    parse(values.get(i));

                }
            }
        }
        else
        {
            logger.error("AanderraOptodeConstants:: Instrument " + ins + " Mooring " + m);
        }
    }

    @Override
    protected void parse(InstrumentCalibrationValue row)
    {
        //
        // here we get into hard-coded parameter names so be wary....
        //
        String paramName = row.getParameterCode();
        String paramValue = row.getParameterValue();
        
        // logger.debug("AanderraOptodeConstants::parse " + paramName + " " + paramValue);

        if(paramName.equalsIgnoreCase("C0"))
        {
            C0Coeffs = parseData(paramValue);
        }
        else if(paramName.equalsIgnoreCase("C1"))
        {
            C1Coeffs = parseData(paramValue);
        }
        else if(paramName.equalsIgnoreCase("C2"))
        {
            C2Coeffs = parseData(paramValue);
        }
        else if(paramName.equalsIgnoreCase("C3"))
        {
            C3Coeffs = parseData(paramValue);
        }
        else if(paramName.equalsIgnoreCase("C4"))
        {
            C4Coeffs = parseData(paramValue);
        }
        else if(paramName.equalsIgnoreCase("BPhaseConstant"))
        {
            BPhaseConstant = parseDataValue(paramValue);
        }
        else if(paramName.equalsIgnoreCase("BPhaseMultiplier"))
        {
            BPhaseMultiplier = parseDataValue(paramValue);
        }
        else if(paramName.equalsIgnoreCase("TempVoltConstant"))
        {
            TempVoltConstant = parseDataValue(paramValue);
        }
        else if(paramName.equalsIgnoreCase("TempVoltMultiplier"))
        {
            TempVoltMultiplier = parseDataValue(paramValue);
        }
        else if(paramName.equalsIgnoreCase("BPhaseVoltConstant"))
        {
            BPhaseVoltConstant = parseDataValue(paramValue);
        }
        else if(paramName.equalsIgnoreCase("BPhaseVoltMultiplier"))
        {
            BPhaseVoltMultiplier = parseDataValue(paramValue);
        }
        else if(paramName.equalsIgnoreCase("UCHIDA_C1"))
        {
            UchidaC1Coefficient = parseDataValue(paramValue);
        }
        else if(paramName.equalsIgnoreCase("UCHIDA_C2"))
        {
            UchidaC2Coefficient = parseDataValue(paramValue);
        }
        else if(paramName.equalsIgnoreCase("UCHIDA_C3"))
        {
            UchidaC3Coefficient = parseDataValue(paramValue);
        }
        else if(paramName.equalsIgnoreCase("UCHIDA_C4"))
        {
            UchidaC4Coefficient = parseDataValue(paramValue);
        }
        else if(paramName.equalsIgnoreCase("UCHIDA_C5"))
        {
            UchidaC5Coefficient = parseDataValue(paramValue);
        }
        else if(paramName.equalsIgnoreCase("UCHIDA_C6"))
        {
            UchidaC6Coefficient = parseDataValue(paramValue);
        }
        else if(paramName.equalsIgnoreCase("UCHIDA_C7"))
        {
            UchidaC7Coefficient = parseDataValue(paramValue);
        }
        else if(paramName.equalsIgnoreCase("UCHIDA_C8"))
        {
            UchidaC8Coefficient = parseDataValue(paramValue);
        }
        else if(paramName.equalsIgnoreCase("UCHIDA_C9"))
        {
            UchidaC9Coefficient = parseDataValue(paramValue);
        }
        
    }

    private Double parseDataValue(String datum)
    {
        try
        {
            Double d = new Double(datum);
            return d;
        }
        catch(NumberFormatException nex)
        {
            logger.error(nex);
            return null;
        }
    }
    private double[] parseData(String data)
    {
        StringTokenizer st = new StringTokenizer(data,",");

        double[] coeffs = new double[st.countTokens()];

        //for(int i = 0; i < st.countTokens(); i++)
        int i = 0;
        while(st.hasMoreTokens())
        {
            String s = st.nextToken();
            try
            {
                Double d = new Double(s);
                coeffs[i] = d;
                // System.out.println("parseData: " + i + " input " + s + " " + coeffs[i]);
                i++;
            }
            catch(NumberFormatException nex)
            {
                logger.error(nex);
            }
        }

        return coeffs;
    }
}
