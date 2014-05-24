package org.imos.abos.mooring.MRU;
import java.text.DecimalFormat;


public class quaternion
{
	DecimalFormat df3 = new DecimalFormat("#0.000");
	
	double Q1, Q2, Q3, Q4;
	
	public String toString()
	{
		return df3.format(Q1) + ", " + df3.format(Q2) + ", " + df3.format(Q3) + ", " + df3.format(Q4);
	}
	
	public vector eulerAngles()
	{
		vector v = new vector();
		
		v.y = Math.toDegrees(Math.atan2(2 * (Q1 * Q2 + Q3 * Q4), 1.0 - 2 * (Q2 * Q2 + Q3 * Q3)));
		v.x = Math.toDegrees(Math.asin(2 * (Q1 * Q3 - Q4 * Q2)));
		v.z = Math.toDegrees(Math.atan2(2 * (Q1 * Q4 + Q2 * Q3), 1.0 - 2 * Q3 * Q3 + Q4 * Q4));
		
		return v;
	}
	
	public quaternion conjugate()
	{
	    quaternion q = new quaternion();
	    
	    q.Q1 = Q1;
	    q.Q2 = -Q2;
	    q.Q3 = -Q3;
	    q.Q4 = -Q4;
	    
	    return q;
	}
	
	public quaternion product(quaternion in)
	{
	    quaternion out= new quaternion();
	    
	    out.Q1 = Q1 * in.Q1 - Q2 * in.Q2 - Q3 * in.Q3 - Q4 * in.Q4;
	    out.Q2 = Q1 * in.Q2 + Q2 * in.Q1 + Q3 * in.Q4 - Q4 * in.Q3;
	    out.Q3 = Q1 * in.Q3 + Q3 * in.Q1 + Q4 * in.Q2 - Q2 * in.Q4;
	    out.Q4 = Q1 * in.Q4 + Q4 * in.Q1 + Q2 * in.Q3 - Q3 * in.Q2;
	    
	    return out;
	}
	
	public vector rotate(vector in)
	{
	    vector out = new vector();
	    quaternion q = new quaternion();
	    
	    q.Q1 = 0;
	    q.Q2 = in.x;
	    q.Q3 = in.y;
	    q.Q4 = in.z;
	    
	    quaternion qc = conjugate();
	    quaternion qp = product(q);
	    quaternion qout = qp.product(qc);
	    
	    out.x = qout.Q2;
	    out.y = qout.Q3;
	    out.z = qout.Q4;
	    
	    return out;
	    
	}
}