package org.imos.abos.mooring.MRU;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.ReadableByteChannel;

public class decode
{
    int tickRoll = 0;
    int checkErrors = 0;
    int msgs = 0;

    public class mruRecord
    {
        double ticks;
        double ttime;
        int command;
    }

    public void setMsgs(int d)
    {
        msgs = d;
    }
    
    public class mruStabQ extends mruRecord
    {
        public quaternion stab;
        public vector mag, accel, gyro, accelWorld;
        public vector pry;

        public mruStabQ()
        {
            stab = new quaternion();
            mag = new vector();
            accel = new vector();
            gyro = new vector();
        }
    }

    public int command2len(int command)
    {
        int len;

        switch (command)
        {
            case MS_3DM_GX1.CMD_TEMP:
                len = 7;
                break;
            case MS_3DM_GX1.CMD_CONTINOUS:
                len = 7;
                break;
            case MS_3DM_GX1.CMD_READEE:
                len = 7;
                break;
            case MS_3DM_GX1.CMD_WRITEEE:
                len = 7;
                break;
            case MS_3DM_GX1.CMD_SERIALNO:
                len = 5;
                break;
            case MS_3DM_GX1.CMD_GYRO_STABQ_VECT:
                len = 31;
                break;
            default:
                len = -1;
                break;
        }

        return len;
    }

    public mruRecord read(ByteBuffer b)
    {
        mruRecord ret = new mruRecord();

        //int len = b.capacity();
        int len;
        int sum = 0;
        int command;

        b.order(ByteOrder.BIG_ENDIAN);

        command = b.get() & 0xff;
        sum = command;
        len = command2len(command);
        int nInts = (len / 2) + 1;
        int ints[] = new int[nInts];
        ints[0] = command;

        if (msgs >= 2)
        {
            System.out.println("decode::read len " + len + " nInts " + nInts + " command " + command);
        }
        if (len < 0)
        {
            return null;
        }

        for (int n = 1; n < nInts; n++)
        {
            ints[n] = b.getShort();
            // System.out.printf("%2d, 0x%02x 0x%02x, 0x%04x, sum 0x%04x\n", n, msg[n * 2 - 1], msg[n * 2], ints[n], (sum & 0xffff));
            if (n < nInts - 1)
            {
                sum = (sum + ints[n]) & 0xffff;
            }
            //System.out.printf("%2d, 0x%04x 0x%04x\n", n, (ints[n] & 0xffff), sum & 0xffff);
        }
        ints[nInts - 1] = ints[nInts - 1] & 0xffff;
        if (msgs >= 3)
        {
            System.out.printf("Sum 0x%04x in packet 0x%04x\n", sum, ints[nInts - 1] & 0xffff);
        }
        if (sum == ints[nInts - 1])
        {
            //System.out.println("Sum ok");
        }
        else
        {
            if (msgs >= 1)
            {
                System.out.println(" Check sum error, command " + ints[0]);
            }
            checkErrors++;

            return null;
        }

        if (ints[0] == MS_3DM_GX1.CMD_GYRO_STABQ_VECT)
        {
            mruStabQ r = new mruStabQ();
            r.command = command;

            r.stab.Q1 = ints[1] / 8192.0;
            r.stab.Q2 = ints[2] / 8192.0;
            r.stab.Q3 = ints[3] / 8192.0;
            r.stab.Q4 = ints[4] / 8192.0;
            if (msgs >= 2)
            {
                System.out.println("stab " + r.stab + " euler (P,R,Y) " + r.stab.eulerAngles());
            }

            r.mag.x = ints[5] / (32768000.0 / 2000);
            r.mag.y = ints[6] / (32768000.0 / 2000);
            r.mag.z = ints[7] / (32768000.0 / 2000);
            if (msgs >= 2)
            {
                System.out.println("mag (Guass) " + r.mag);
            }

            r.accel.x = 9.81 * ints[8] / (32768000.0 / 8500);
            r.accel.y = 9.81 * ints[9] / (32768000.0 / 8500);
            r.accel.z = 9.81 * ints[10] / (32768000.0 / 8500);
            if (msgs >= 2)
            {
                System.out.println("Accel (m/s^2) " + r.accel);
            }

            r.gyro.x = ints[11] / (32768000.0 / 10000);
            r.gyro.y = ints[12] / (32768000.0 / 10000);
            r.gyro.z = ints[13] / (32768000.0 / 10000);
            if (msgs >= 2)
            {
                System.out.println("AngleRate (rad/s) " + r.gyro);
            }

            r.accelWorld = r.stab.rotate(r.accel);
            if (msgs >= 2)
            {
                System.out.println("World Coord Accel (m/s^2) " + r.accelWorld);
            }

            // the MRU is upside down
            r.pry = r.stab.eulerAngles();
            if (r.pry.y < 0)
            {
                r.pry.y = r.pry.y + 180;
            }
            else
            {
                r.pry.y = r.pry.y - 180;
            }

            //System.out.println((ticks + tickRoll * 0.0065536 * 65535) + "," + accelWorld);

            if (msgs >= 2)
            {
                System.out.println("pry ," + r.pry + ",accel ," + r.accel + " , gyro ," + r.gyro + " ,accel-w ," + r.accelWorld);
            }

            ret = r;
        }
        else
        {
            if ((ints[0] == MS_3DM_GX1.CMD_READEE) | (ints[0] == MS_3DM_GX1.CMD_WRITEEE))
            {
                System.out.println("EEPROM data = " + ints[1]);
            }
            else
            {
                if ((ints[0] == MS_3DM_GX1.CMD_CONTINOUS))
                {
                    System.out.printf("Cont Mode command 0x%02x\n", ints[1]);
                }
                else
                {
                    if ((ints[0] == MS_3DM_GX1.CMD_SERIALNO))
                    {
                        System.out.println("serial number = " + ints[1]);
                    }
                    else
                    {
                        if ((ints[0] == MS_3DM_GX1.CMD_TEMP))
                        {
                            System.out.println("Temp = " + ints[1] + " " + (((ints[1] * 5.0 / 65536) - 0.5) * 100));
                        }
                    }
                }
            }
        }

        double t = (ints[nInts - 2] & 0xffff) * 0.0065536;
        // System.out.println("Ticks (sec)   " + t);				

        if (t < ret.ticks)
        {
            tickRoll++;
        }
        ret.ticks = t;

        return ret;
    }

    public static void main(String[] args)
    {
        decode d = new decode();
        final int len = 31;
        d.msgs = 2;

        try
        {
            FileInputStream is = new FileInputStream(new File(args[0]));

            FileChannel ch = is.getChannel( );
            MappedByteBuffer mb = ch.map( MapMode.READ_ONLY, 0L, ch.size( ) );

            boolean eof = false;

            while (mb.hasRemaining())
                {
                d.read(mb);
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
