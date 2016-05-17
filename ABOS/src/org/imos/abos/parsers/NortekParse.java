package org.imos.abos.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

public class NortekParse
{
    private static Logger log = Logger.getLogger(NortekParse.class);

    HashMap<Integer, String> packetName = new HashMap<Integer, String>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    
	public NortekParse()
	{
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        sdf.setTimeZone(TimeZone.getDefault());
        
        packetName.put(0x00 ,"User Configuration");
        packetName.put(0x01 ,"Aquadopp Velocity Data");
        packetName.put(0x02 ,"Vectrino distance data");
        packetName.put(0x04 ,"Head Configuration");
        packetName.put(0x05 ,"Hardware Configuration");
        packetName.put(0x06 ,"Aquadopp Diagnostics Data Header");
        packetName.put(0x07 ,"Vector and Vectrino Probe Check data");
        packetName.put(0x10 ,"Vector Velocity Data");
        packetName.put(0x11 ,"Vector System Data");
        packetName.put(0x12 ,"Vector Velocity Data Header");
        packetName.put(0x20 ,"AWAC Velocity Profile Data");
        packetName.put(0x21 ,"Aquadopp Profiler Velocity Data");
        packetName.put(0x24 ,"Continental Data");
        packetName.put(0x2a ,"High Resolution Aquadopp Profiler Data");
        packetName.put(0x30 ,"AWAC Wave Data");
        packetName.put(0x31 ,"AWAC Wave Data Header");
        packetName.put(0x36 ,"AWAC Wave Data SUV");
        packetName.put(0x42 ,"AWAC Stage Data");
        packetName.put(0x50 ,"Vectrino velocity data header");
        packetName.put(0x51 ,"Vectrino velocity data");
        packetName.put(0x60 ,"Wave parameter estimates");
        packetName.put(0x61 ,"Wave band estimates");
        packetName.put(0x62 ,"Wave energy spectrum");
        packetName.put(0x63 ,"Wave Fourier coefficient spectrum");
        packetName.put(0x65 ,"Cleaned up AST time series");
        packetName.put(0x6a ,"Awac Processed Velocity Profile Data");
        packetName.put(0x71 ,"vector with IMU");
        packetName.put(0x80 ,"Aquadopp Diagnostics Data");                       
	}

	FileInputStream fileInputStream;
    FileChannel in;
    MappedByteBuffer buffer;
    
    public long open(File filename) throws FileNotFoundException, IOException
    {
    	log.info("Open File " + filename);
    	
        fileInputStream = new FileInputStream(filename);
        in = fileInputStream.getChannel();

        buffer = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());

        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.load();
        
        long records = in.size() - 1;

        return records;
    }
    
    int structStart = -1;
    int dataStart = -1;
    int structEnd = -1;
    int id = -1;
    int size = -1;
    
    public ArrayList<Attribute> attributes = new ArrayList<Attribute>();
    
    public int read() throws IOException
    {
    	structStart = buffer.position();
    	
    	if (buffer.remaining() < 1)
    		return -1;
        
        int sync = buffer.get() & 0xff;
        if (sync != 0xa5)
        {
            return -1;
        }

        id = buffer.get() & 0xff;
        
        if (id == 0x10)
        	size = 12;
        else
        	size = buffer.getShort() & 0xffff;
    	
        log.debug("sync " + sync + " id " + id + " size " + size + " : " + packetName.get(id));

    	dataStart = buffer.position();
    	structEnd = structStart + 2 * size;
    	
    	log.trace("Size Words " + size);
        		
		if (readCheckSum() < 0)
			return -1;

		buffer.position(dataStart);
    	switch (id)
    	{
    		case 0:
    	    	readUserConfig();
    	    	break;
    		case 4:
    	    	readHeadConfig();
    	    	break;
    		case 5:
    	    	readHWconfig();
    	    	break;
    		case 7:
    	    	readProbeCheck();
    	    	break;
    		case 18:
    			readVectorVelocityHeader();
    			break;
    		case 17:
    			readVectorSystemData();
    			break;
    		case 16:
    			readVectorVelocityData();
    			break;
    		case 113:
    			readVectorIMUData();
    			break;
    		default:
    			log.error("Unknown packet " + id);
    	}
    	
    	buffer.position(structEnd);
        
        return (int)(in.size() - buffer.position());
    }

    public int readCheckSum()
    {
    	buffer.position(structStart);
    	int sum = 0xb58c;
    	for(int i=0;i<size-1;i++)
    	{
    		sum += buffer.getShort() & 0xffff;
    	}
    	sum = sum & 0xffff;
    	int packetSum = buffer.getShort() & 0xffff;
    	structEnd = buffer.position();
    	
    	log.trace("sum " + (sum & 0xffff) + " packet " + packetSum);
    	if (sum != packetSum)
    		return -1;
    	    	
    	return dataStart;
    }
    
    private int BCD2int(int bcd)
    {
    	return (bcd >> 4) * 10 + (bcd & 0x0f);    	
    }
    
    private Timestamp readTimestamp()
    {
    	int min = BCD2int(buffer.get() & 0xff);
    	int sec = BCD2int(buffer.get() & 0xff);
    	int day = BCD2int(buffer.get() & 0xff);
    	int hour = BCD2int(buffer.get() & 0xff);
    	int year = BCD2int(buffer.get() & 0xff);
    	int mon = BCD2int(buffer.get() & 0xff);
    	
    	// BCD conversion
    	
    	GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    	
    	c.set(GregorianCalendar.YEAR, year+2000);
    	c.set(GregorianCalendar.MONTH, mon-1);
    	c.set(GregorianCalendar.DAY_OF_MONTH, day);
    	c.set(GregorianCalendar.HOUR, hour);
    	c.set(GregorianCalendar.MINUTE, min);
    	c.set(GregorianCalendar.SECOND, sec);
    	c.set(GregorianCalendar.MILLISECOND, 0);
    	Timestamp ts = new Timestamp(c.getTime().getTime());
    	
    	log.trace(sdf.format(ts));    
    	
    	return ts;
    }

    int dataPoint = 0;
    float vel[][];
    int amp[][];
    int cor[][];
    float ana1[];
    float ana2[];
    public float pressure[];
    
    public ArrayList velData = new ArrayList();
    public ArrayList ampData = new ArrayList();
    public ArrayList corData = new ArrayList();
    
    public ArrayList pressureData = new ArrayList();
    public ArrayList ana1Data = new ArrayList();
    public ArrayList ana2Data = new ArrayList();
    
    private void readVectorVelocityData()
	{
    	dataPoint++;

    	int anaIn2LSB = buffer.get() & 0xff;
    	int count = buffer.get() & 0xff;
    	int pressureMSB = buffer.get() & 0xff;
    	int anaIn2MSB = buffer.get();
    	int pressureLSW = buffer.getShort() & 0xffff;
    	int anaIn1 = buffer.getShort();
    	
    	int velB1 = buffer.getShort();
    	int velB2 = buffer.getShort();
    	int velB3 = buffer.getShort();
    	vel[dataPoint][0] = velB1;
    	vel[dataPoint][1] = velB2;
    	vel[dataPoint][2] = velB3;

    	int ampB1 = buffer.get() & 0xff;
    	int ampB2 = buffer.get() & 0xff;
    	int ampB3 = buffer.get() & 0xff;
    	amp[dataPoint][0] = ampB1;
    	amp[dataPoint][1] = ampB2;
    	amp[dataPoint][2] = ampB3;

    	int corB1 = buffer.get() & 0xff;
    	int corB2 = buffer.get() & 0xff;
    	int corB3 = buffer.get() & 0xff;
    	cor[dataPoint][0] = corB1;
    	cor[dataPoint][1] = corB2;
    	cor[dataPoint][2] = corB3;
    	
    	float pressure = (pressureLSW + pressureMSB * 65536) * 0.001f;
    	int anaIn2 = anaIn2LSB | (anaIn2MSB * 256);
    	
    	this.ana1[dataPoint] = anaIn1;
    	this.ana2[dataPoint] = anaIn2;
    	this.pressure[dataPoint] = pressure;

    	log.debug("Vel " + dataPoint + " data " + count + " P " + pressure);
	}

    float stabAccel[][];
    float angRate[][];
    float stabMag[][];
    
    public ArrayList stabAccelData = new ArrayList();
    public ArrayList angRateData = new ArrayList();
    public ArrayList stabMagData = new ArrayList();
    
    private void readVectorIMUData()
    {
    	int ensCnt = buffer.get() & 0xff;
    	int AHRSId = buffer.get() & 0xff;
    	
    	if (AHRSId == 0xD2)
    	{
    		float stabX = buffer.getFloat();
    		float stabY = buffer.getFloat();
    		float stabZ = buffer.getFloat();
    		float AngRateX = buffer.getFloat();
    		float AngRateY = buffer.getFloat();
    		float AngRateZ = buffer.getFloat();
    		float StabMagX = buffer.getFloat();
    		float StabMagY = buffer.getFloat();
    		float StabMagZ = buffer.getFloat();
    		double timer = (buffer.getInt() & 0xffffffff)/62.5;
    		
        	stabAccel[dataPoint][0] = stabX;
        	stabAccel[dataPoint][1] = stabY;
        	stabAccel[dataPoint][2] = stabZ;
        	angRate[dataPoint][0] = AngRateX;
        	angRate[dataPoint][1] = AngRateY;
        	angRate[dataPoint][2] = AngRateZ;
        	stabMag[dataPoint][0] = StabMagX;
        	stabMag[dataPoint][1] = StabMagY;
        	stabMag[dataPoint][2] = StabMagZ;
        	
    		log.debug("IMU gryo stab accel, ang rate, mag " + StabMagX + " timer " + timer);
    	}
    	
    	log.debug("IMU DataPoint " + dataPoint + " cnt " + ensCnt + " IMU ID " + AHRSId);
    }
    private void readProbeCheck()
    {
    	int samples = buffer.getShort() & 0xffff;
    	
    	log.debug("Probe Check " + samples );
    }
    private void readUserConfig()
    {
        SimpleDateFormat netcdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    	int T1 = buffer.getShort() & 0xffff; // transmit pulse length (counts)
    	int T2 = buffer.getShort() & 0xffff; // blanking distance (counts)
    	int T3 = buffer.getShort() & 0xffff; // receive length (counts)
    	int T4 = buffer.getShort() & 0xffff; // time between pings (counts)
    	int T5 = buffer.getShort() & 0xffff; // time between burst sequences (counts)
    	
    	attributes.add(new Attribute("nortek_userconfig_T1", T1));
    	attributes.add(new Attribute("nortek_userconfig_T2", T2));
    	attributes.add(new Attribute("nortek_userconfig_T3", T3));
    	attributes.add(new Attribute("nortek_userconfig_T4", T4));
    	attributes.add(new Attribute("nortek_userconfig_T5", T5));
    	
    	int NPings = buffer.getShort() & 0xffff;
    	int AvgInterval = buffer.getShort() & 0xffff;
    	int NBeams = buffer.getShort() & 0xffff;
    	int TimCtrlReg = buffer.getShort() & 0xffff;
    	int PwrCtrlReg = buffer.getShort() & 0xffff;
    	int A1 = buffer.getShort() & 0xffff;
    	int B0 = buffer.getShort() & 0xffff;
    	int B1 = buffer.getShort() & 0xffff;
    	int CompassUpdRate = buffer.getShort() & 0xffff;
    	int CoordSystem = buffer.getShort() & 0xffff;
    	switch (CoordSystem)
    	{
    		case 0:
    			attributes.add(new Attribute("nortek_userconfig_coordsystem", "ENU"));
    		case 1:
    			attributes.add(new Attribute("nortek_userconfig_coordsystem", "XYZ"));
    		case 2:
    			attributes.add(new Attribute("nortek_userconfig_coordsystem", "BEAM"));    		    	
    	}
    	int NBins = buffer.getShort() & 0xffff;
    	int BinLength = buffer.getShort() & 0xffff;
		attributes.add(new Attribute("nortek_userconfig_binlength", BinLength));
		
    	int MeasInterval = buffer.getShort() & 0xffff;
		attributes.add(new Attribute("nortek_userconfig_measinterval", MeasInterval));

		byte[] DeployName = new byte[6];
        buffer.get(DeployName);
    	int WrapMode = buffer.getShort() & 0xffff;
    	Date clockDeploy = readTimestamp();
    	attributes.add(new Attribute("nortek_userconfig_clock_deploy", sdf.format(clockDeploy)));
    	
    	int DiagInterval = buffer.getInt() & 0xffffffff;
    	int Mode = buffer.getShort() & 0xffff;
    	int ModeTest = buffer.getShort() & 0xffff;
    	int AnaInAddr = buffer.getShort() & 0xffff;
    	int SWVersion = buffer.getShort() & 0xffff;
		attributes.add(new Attribute("nortek_userconfig_swveraion", SWVersion));

		int Spare = buffer.getShort() & 0xffff;
        byte[] VelAdjTable = new byte[180];
        buffer.get(VelAdjTable);
        byte[] Comments = new byte[180];
        buffer.get(Comments);
    	int wMode = buffer.getShort() & 0xffff;
    	int DynPercPos = buffer.getShort() & 0xffff;
    	int wT1 = buffer.getShort() & 0xffff;
    	int wT2 = buffer.getShort() & 0xffff;
    	int wT3 = buffer.getShort() & 0xffff;
    	int NSamp = buffer.getShort() & 0xffff;
    	int wA1 = buffer.getShort() & 0xffff;
    	int wB0 = buffer.getShort() & 0xffff;
    	int wB1 = buffer.getShort() & 0xffff; // samples / burst for ArrayList
    	int Spare1 = buffer.getShort() & 0xffff;
    	int AnaOutScale = buffer.getShort() & 0xffff;
    	int CorrThresh = buffer.getShort() & 0xffff;
    	int Spare2 = buffer.getShort() & 0xffff;
    	int TiLag2 = buffer.getShort() & 0xffff;
        byte[] Spare3 = new byte[30];
        buffer.get(Spare3);
        byte[] QualConst = new byte[16];
        buffer.get(QualConst);
        
        log.info("User NBins " + NBins + " NPings " + NPings + " time deploy "  + clockDeploy);
    }
    
    private void readHeadConfig()
    {
        int Config = buffer.getShort() & 0xffff;
        int Frequency = buffer.getShort() & 0xffff;
        int Type = buffer.getShort() & 0xffff;
        byte[] SerialNo = new byte[12];
        buffer.get(SerialNo);
        byte[] System = new byte[176];
        buffer.get(System);
        byte[] Spare = new byte[22];
        buffer.get(Spare);
        int NBeams = buffer.getShort() & 0xffff;

        String ConfigS = "";
        if ((Config & 0x01) != 0)
        {
        	ConfigS += "pressure_sensor";
        }
        if ((Config & 0x02) != 0)
        {
        	if (ConfigS.length() > 0) ConfigS += "; ";
        	ConfigS += "magnetometer_sensor";
        }
        if ((Config & 0x03) != 0)
        {
        	if (ConfigS.length() > 0) ConfigS += "; ";
        	ConfigS += "tilt_sensor";
	        if ((Config & 0x04) != 0)
	        {
	        	if (ConfigS.length() > 0) ConfigS += "; ";
	        	ConfigS += "tilt_sensor_down";
	        }
	        else
	        {
	        	if (ConfigS.length() > 0) ConfigS += "; ";
	        	ConfigS += "tilt_sensor_up";        	
	        }
        }
    	attributes.add(new Attribute("nortek_headconfig_config", ConfigS));
    	attributes.add(new Attribute("nortek_headconfig_frequency", Frequency));
    	attributes.add(new Attribute("nortek_headconfig_beams", NBeams));
    	attributes.add(new Attribute("nortek_headconfig_serialno", new String(SerialNo)));
    	attributes.add(new Attribute("nortek_headconfig_type", Type));

        log.info("Head Beams " + NBeams + " freq " + Frequency + " kHz" + " serial no. " + new String(SerialNo));
    }
    
    
    public ArrayList<Timestamp> ts = new ArrayList<Timestamp>();
    public int NRecords = -1;
    
    private void readVectorVelocityHeader()
	{
    	Timestamp ts = readTimestamp();
    
    	this.ts.add(ts);
    	
    	saveData();

    	dataPoint = -1;
    	
    	NRecords = buffer.getShort() & 0xffff;

    	vel = new float[NRecords][3];
    	amp = new int[NRecords][3];
    	cor = new int[NRecords][3];
    	ana1 = new float[NRecords];
    	ana2 = new float[NRecords];
    	pressure = new float[NRecords];

    	stabAccel = new float[NRecords][3];
    	angRate = new float[NRecords][3];
    	stabMag = new float[NRecords][3];

    	log.debug(sdf.format(ts) + " NRecords " + NRecords);
	}
    
    public ArrayList<Timestamp> sdTS = new ArrayList<Timestamp>();
    public ArrayList sdBattery = new ArrayList();
    public ArrayList sdTemp = new ArrayList();
    public ArrayList sdHead = new ArrayList();
    public ArrayList sdPitch = new ArrayList();
    public ArrayList sdRoll = new ArrayList();
    
    private void readVectorSystemData()
	{
    	Timestamp ts = readTimestamp();
    	sdTS.add(ts);
    	
    	float battery = (buffer.getShort() & 0xffff) * 0.1f;
    	sdBattery.add(battery);
    	
    	float soundSpeed = (buffer.getShort() & 0xffff) * 0.1f;
    	float heading = (buffer.getShort() & 0xffff) * 0.1f;
    	float pitch = (buffer.getShort() & 0xffff) * 0.1f;
    	float roll = (buffer.getShort() & 0xffff) * 0.1f;
    	float temp = (buffer.getShort() & 0xffff) * 0.01f;
    	
    	sdTemp.add(temp);
    	sdHead.add(heading);
    	sdPitch.add(pitch);
    	sdRoll.add(roll);
    	
    	int error = buffer.get() & 0xff;
    	int status = buffer.get() & 0xff;
    	float anaIn = (buffer.getShort() & 0xffff) * 0.01f;
    	
    	log.debug(sdf.format(ts) + " battery " + battery + " temperature " + temp);
	}

    public String serialNo;
	public void readHWconfig()
    {    
    	byte[] SerialNo = new byte[14];
    	buffer.get(SerialNo);
    	int trim = 14;
    	for (int i=0;i<14;i++)
    	{
    		if (SerialNo[i] < 0)
    			trim = i;
    		
    		log.trace(i + " " + SerialNo[i]);
    	}
    	
    	serialNo = new String(SerialNo).substring(0, trim);
    	
    	int Config = buffer.getShort() & 0xffff;
    	int Frequency = buffer.getShort() & 0xffff;
    	int PICversion = buffer.getShort() & 0xffff;
    	int HWversion = buffer.getShort() & 0xffff;
    	int RecSize = (buffer.getShort() & 0xffff);

    	attributes.add(new Attribute("nortek_hwconfig_HWversion", HWversion));
    	attributes.add(new Attribute("nortek_hwconfig_recsize", RecSize));
    	attributes.add(new Attribute("nortek_hwconfig_serialno", serialNo));
    	
    	log.info("HW Serial Number '" + serialNo + "'");
    }
    
	protected void saveData()
	{
    	if (dataPoint > 0)
    	{
    		velData.add(vel);
    		ampData.add(amp);
    		corData.add(cor);
    		
    		pressureData.add(this.pressure);
    		ana1Data.add(this.ana1);
    		ana2Data.add(this.ana2);
    		
        	stabAccelData.add(stabAccel);
        	angRateData.add(angRate);
        	stabMagData.add(stabMag);

        	log.debug("Vel " + dataPoint + " P " + pressure[0] + " " + pressureData.size() + " ts " + this.ts.size());
    	}
		
	}
    public void close() throws IOException
    {
    	saveData();
    	
        buffer.clear();
        in.close();
        fileInputStream.close();
    }
    
    public static void main(String args[])
    {
    	String home = System.getProperty("user.home");
        PropertyConfigurator.configure(home + "/ABOS/log4j.properties");

    	NortekParse np = new NortekParse();
    	
    	try
		{
			np.open(new File(args[0]));
			
			while(np.read() != -1)
			{
				
			}
			
			int sdSize = np.sdTS.size();
			int tsSize = np.ts.size();
			
			np.log.info("N system data " + sdSize + " N vel " + tsSize);
			np.log.info("Data Start " + np.sdf.format(np.ts.get(0)) + " to " + np.sdf.format(np.ts.get(tsSize-1)));
			np.log.info("System data Start " + np.sdf.format(np.sdTS.get(0)) + " to " + np.sdf.format(np.sdTS.get(sdSize-1)));
			
			np.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
}
