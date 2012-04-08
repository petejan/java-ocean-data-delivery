/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.instrument;

import org.imos.abos.dbms.InstrumentCalibrationValue;

/**
 *
 * @author peter
 */
public class SeabirdSBE43Constants extends AbstractInstrumentConstants
{
    
    public  Double Soc;
    public  Double VOffset;
    public  Double Tau20;

    public  Double A_Coefficient;
    public  Double B_Coefficient;
    public  Double C_Coefficient;
    public  Double E_Nominal_Coefficient;

    public  Double D1_Coefficient;
    public  Double D2_Coefficient;

    public  Double H1_Coefficient;
    public  Double H2_Coefficient;
    public  Double H3_Coefficient;

    @Override
    protected void parse(InstrumentCalibrationValue row)
    {
        //
        // here we get into hard-coded parameter names so be wary....
        //
        String paramName = row.getParameterCode();
        String paramValue = row.getParameterValue();
        String dataType = row.getDataType();

        if(paramName.equalsIgnoreCase("A_COEFF"))
        {
            A_Coefficient = parseDouble(paramValue);
            return;
        }
        if(paramName.equalsIgnoreCase("B_COEFF"))
        {
            B_Coefficient = parseDouble(paramValue);
            return;
        }
        if(paramName.equalsIgnoreCase("C_COEFF"))
        {
            C_Coefficient = parseDouble(paramValue);
            return;
        }
        if(paramName.equalsIgnoreCase("D1_COEFF"))
        {
            D1_Coefficient = parseDouble(paramValue);
            return;
        }
        if(paramName.equalsIgnoreCase("D2_COEFF"))
        {
            D2_Coefficient = parseDouble(paramValue);
            return;
        }
        if(paramName.equalsIgnoreCase("E_NOMINAL"))
        {
            E_Nominal_Coefficient = parseDouble(paramValue);
            return;
        }
        if(paramName.equalsIgnoreCase("H1_COEFF"))
        {
            H1_Coefficient = parseDouble(paramValue);
            return;
        }
        if(paramName.equalsIgnoreCase("H2_COEFF"))
        {
            H2_Coefficient = parseDouble(paramValue);
            return;
        }
        if(paramName.equalsIgnoreCase("H3_COEFF"))
        {
            H3_Coefficient = parseDouble(paramValue);
            return;
        }
        if(paramName.equalsIgnoreCase("SOC"))
        {
            Soc = parseDouble(paramValue);
            return;
        }
        if(paramName.equalsIgnoreCase("TAU_20"))
        {
            Tau20 = parseDouble(paramValue);
            return;
        }
        if(paramName.equalsIgnoreCase("V_OFFSET"))
        {
            VOffset = parseDouble(paramValue);
            return;
        }
        //
        // if none of these, see if it's a base defined value
        //
        super.parse(row);
    }
    
}
