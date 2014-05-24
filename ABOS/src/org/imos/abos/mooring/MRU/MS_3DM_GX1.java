package org.imos.abos.mooring.MRU;

import java.nio.ByteBuffer;

public class MS_3DM_GX1
{

    final static int CMD_GYRO_STABQ_VECT = 0x0c;
    final static int CMD_TEMP = 0x07;
    final static int CMD_SERIALNO = 0xF1;
    final static int CMD_WRITEEE = 0x29;
    final static int CMD_READEE = 0x28;
    final static int CMD_CONTINOUS = 0x10;

    public static short calcSum(ByteBuffer b)
    {
        int i;
        int sum = 0;

        for (i = 0; i < ((b.capacity() / 2)); i++)
        {
            if (i == 0)
            {
                sum = b.get();
            }
            else
            {
                sum += b.getShort();
            }
            //System.out.printf("%3d : 0x%04x\n", i, sum & 0xffff);
        }

        return (short) (sum & 0xffff);
    }
}
