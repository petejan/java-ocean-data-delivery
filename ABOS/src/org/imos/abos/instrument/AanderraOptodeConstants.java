/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.instrument;

import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentCalibrationValue;
import org.imos.abos.dbms.Mooring;

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
    public Double A_Coefficient = -5.00;
    public Double B_Coefficient = 9.00;

    public Double BPhaseVoltConstant = 10.0;
    public Double BPhaseVoltMultiplier = 12.0;

    public Double BPhaseConstant = 0.894295;
    public Double BPhaseMultiplier = 1.10633;

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
    }

    @Override
    protected void parse(InstrumentCalibrationValue row)
    {
        //
        // here we get into hard-coded parameter names so be wary....
        //
        String paramName = row.getParameterCode();
        String paramValue = row.getParameterValue();

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
    }

    private double[] parseData(String data)
    {
        StringTokenizer st = new StringTokenizer(data,",");

        double[] coeffs = new double[st.countTokens()];

        for(int i = 0; i < st.countTokens(); i++)
        {
            String s = st.nextToken();
            try
            {
                Double d = new Double(s);
                coeffs[i] = d;
            }
            catch(NumberFormatException nex)
            {
                logger.error(nex);
            }
        }

        return coeffs;
    }
}
