package org.imos.abos.mooring.MRU;

import java.text.DecimalFormat;

public class vector
{
    public double x, y, z;
    DecimalFormat df3 = new DecimalFormat("#0.00000");

    public vector()
    {
        x = 0;
        y = 0;
        z = 0;
    }

    public String toString()
    {
        return df3.format(x) + ", " + df3.format(y) + ", " + df3.format(z);
    }
}
