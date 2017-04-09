package org.imos.abos.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ucar.nc2.Attribute;

public class NortekParse
{
    private static Logger log = Logger.getLogger(NortekParse.class);

    HashMap<Integer, String> packetName = new HashMap<Integer, String>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat sdfms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
	public NortekParse()
	{
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        sdf.setTimeZone(TimeZone.getDefault());
        sdfms.setTimeZone(TimeZone.getDefault());
        
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
        packetName.put(0x30 ,"AWAC Wave Data/Aquadopp Profiler Wave Burst Data");
        packetName.put(0x31 ,"AWAC Wave Data Header/Aquadopp Profiler Wave Burst Data Header");
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
        packetName.put(0x71 ,"Vector with IMU");
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

    public void close() throws IOException
    {
        buffer.clear();
        in.close();
        fileInputStream.close();
    }
        
    int structStart = -1;
    int dataStart = -1;
    int structEnd = -1;
    int id = -1;
    int size = -1;
    
    public ArrayList<Attribute> attributes = new ArrayList<Attribute>();
    
    public Object read() throws IOException
    {
    	structStart = buffer.position();
    	
    	if (buffer.remaining() < 1)
    		return null;
        
        int sync = buffer.get() & 0xff;
        if (sync != 0xa5)
        {
			//log.warn("bad sync " + sync + " " + new String(Character.toChars(sync)));
			
			return 0;
        }

        id = buffer.get() & 0xff;
        
        if (id == 0x10)
        	size = 12;
        else
        	size = buffer.getShort() & 0xffff;
    	
        //log.debug("sync " + sync + " id " + id + " size " + size + " : " + packetName.get(id));

    	dataStart = buffer.position();
    	structEnd = structStart + 2 * size;
    	
    	log.trace("Size Words " + size);
        		
		if (readCheckSum() < 0)
		{
			log.warn("Check sum error");
			
			// TODO: Move back to start of packet incase the packet size is bad ?
			
			return 0;
		}

		buffer.position(dataStart);
		Object readObject = 0;
    	switch (id)
    	{
    		case 0:
    	    	readObject = readUserConfig();
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
    			readObject = readVectorVelocityHeader();
    			break;
    		case 17:
    			readObject = readVectorSystemData();
    			break;
    		case 16:
    			readObject = readVectorVelocityData();
    			break;
    		case 33:
    			readObject = readAquadoppProfileVelocityData();
    			break;
    		case 42:
    			readObject = readHRAquadoppProfileVelocityData();
    			break;
    		case 48:
    			readObject = readAwacWaveData();
    			break;
    		case 49:
    			readObject = readAwacWaveDataHeader();
    			break;
    		case 113:
    			readObject = readVectorIMUData();
    			break;
    		default:
    			log.error("Unknown packet " + id);
    	}
    	
    	buffer.position(structEnd);
    	
    	//log.debug("read size " + in.size() + " position " + buffer.position());
        
        return readObject;
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
    	
    	//log.debug("TS  " + year + " " + sdf.format(ts));    
    	
    	return ts;
    }

    // data blocks
    
    int dataPoint = 0;
    
    public class VectorVelocityData
    {
    	public int count;

    	int anaIn2LSB;
    	int pressureMSB;
    	int anaIn2MSB;
    	int pressureLSW;
    	
    	public int anaIn1;
    	public float pressure;
    	public int anaIn2;    	
    	    	
    	public int velB1;
    	public int velB2;
    	public int velB3;

    	public int ampB1;
    	public int ampB2;
    	public int ampB3;

    	public int corB1;
    	public int corB2;
    	public int corB3;
    	
    	public String toString()
    	{
        	return ("VectorVelocityData: " + dataPoint + " data " + count + " P " + pressure + " ana " + anaIn1);
    	}
    }

    ArrayList<VectorVelocityData> velocityData = new ArrayList<VectorVelocityData>();
    
    private Object readVectorVelocityData()
	{
    	VectorVelocityData vvd = new VectorVelocityData();
    	
    	dataPoint++;

    	vvd.anaIn2LSB = buffer.get() & 0xff;
    	vvd.count = buffer.get() & 0xff;
    	vvd.pressureMSB = buffer.get() & 0xff;
    	vvd.anaIn2MSB = buffer.get();
    	vvd.pressureLSW = buffer.getShort() & 0xffff;
    	vvd.anaIn1 = buffer.getShort();
    	
    	vvd.velB1 = buffer.getShort();
    	vvd.velB2 = buffer.getShort();
    	vvd.velB3 = buffer.getShort();

    	vvd.ampB1 = buffer.get() & 0xff;
    	vvd.ampB2 = buffer.get() & 0xff;
    	vvd.ampB3 = buffer.get() & 0xff;

    	vvd.corB1 = buffer.get() & 0xff;
    	vvd.corB2 = buffer.get() & 0xff;
    	vvd.corB3 = buffer.get() & 0xff;
    	
    	vvd.pressure = (vvd.pressureLSW + vvd.pressureMSB * 65536) * 0.001f;
    	vvd.anaIn2 = vvd.anaIn2LSB | (vvd.anaIn2MSB * 256);
    	
    	velocityData.add(vvd);
    	
    	if (velocityData.size() == NRecords)
    	{
    		log.info("velocityData records " + velocityData.size());
    		
    		return velocityData;
    	}
    	
    	return vvd;
	}
    
    public class AwacWaveData
    {
    	public float pressure;
    	public float dist1;
    	int anaIn;
    	
    	public float vel1;
    	public float vel2;
    	public float vel3;
    	public float vel4;

    	public int amp1;
    	public int amp2;
    	public int amp3;
    	public int amp4;    	

    	public String toString()
    	{
        	return (String.format("WAVE data point %d dist %f press %f vel %f %f %f amp %d %d %d", dataPoint, dist1, pressure, vel1, vel2, vel2, amp1, amp2, amp3));
    	}
    }
    
    ArrayList<AwacWaveData> waveData = new ArrayList<AwacWaveData>();
    
    private Object readAwacWaveData()
	{
    	AwacWaveData awd = new AwacWaveData();
    	
    	dataPoint++;

    	awd.pressure = (buffer.getShort() & 0xffff) * 0.001f;
    	awd.dist1 = buffer.getShort() & 0xffff;
    	awd.anaIn = buffer.getShort() & 0xffff;
    	
    	awd.vel1 = buffer.getShort();
    	awd.vel2 = buffer.getShort();
    	awd.vel3 = buffer.getShort();
    	awd.vel4 = buffer.getShort();

    	awd.amp1 = buffer.get() & 0xff;
    	awd.amp2 = buffer.get() & 0xff;
    	awd.amp3 = buffer.get() & 0xff;
    	awd.amp4 = buffer.get() & 0xff;
    	
    	waveData.add(awd);
    	
    	log.debug(awd);
    	
    	if (waveData.size() == NRecords)
    	{
    		log.info("waveData records " + waveData.size());
    		
    		return waveData;
    	}
    	return awd;
	}       
    
    ArrayList<IMU> imuData = new ArrayList<IMU>();
    
    public class IMU
    {
    	int ensCnt;
    	int AHRSId;
    	
		public float stabX;
		public float stabY;
		public float stabZ;
		public float AngRateX;
		public float AngRateY;
		public float AngRateZ;
		public float StabMagX;
		public float StabMagY;
		public float StabMagZ;

		public float DeltaAngX;
		public float DeltaAngY;
		public float DeltaAngZ;
		public float DeltaVelX;
		public float DeltaVelY;
		public float DeltaVelZ;
		
		public float accelX;
		public float accelY;
		public float accelZ;
		public float M11;
		public float M12;
		public float M13;
		public float M21;
		public float M22;
		public float M23;
		public float M31;
		public float M32;
		public float M33;
		
		double timer;  
		
		public String toString()
		{
			return ("IMU: " + dataPoint + " cnt " + ensCnt + " IMU ID " + AHRSId);
		}
    }
    
    private Object readVectorIMUData()
    {
    	IMU imu = new IMU();
    	
    	imu.ensCnt = buffer.get() & 0xff;
    	imu.AHRSId = buffer.get() & 0xff;
    	
    	if (imu.AHRSId == 0xD2)
    	{
    		imu.stabX = buffer.getFloat();
    		imu.stabY = buffer.getFloat();
    		imu.stabZ = buffer.getFloat();
    		imu.AngRateX = buffer.getFloat();
    		imu.AngRateY = buffer.getFloat();
    		imu.AngRateZ = buffer.getFloat();
    		imu.StabMagX = buffer.getFloat();
    		imu.StabMagY = buffer.getFloat();
    		imu.StabMagZ = buffer.getFloat();
    		imu.timer = (buffer.getInt() & 0xffffffff)/62.5;
    		
    		log.trace("IMU gryo stab accel, ang rate, mag " + imu.stabZ + " timer " + imu.timer);
    	}
    	else if (imu.AHRSId == 0xc3)
    	{
    		imu.DeltaAngX = buffer.getFloat();
    		imu.DeltaAngY = buffer.getFloat();
    		imu.DeltaAngZ = buffer.getFloat();
    		imu.DeltaVelX = buffer.getFloat();
    		imu.DeltaVelY = buffer.getFloat();
    		imu.DeltaVelZ = buffer.getFloat();    		

    		imu.timer = (buffer.getInt() & 0xffffffff)/62.5;

    		log.trace("IMU deltaAng,deltaVel timer " + imu.timer);
    	}
    	else if (imu.AHRSId == 0xcc)
    	{
    		imu.accelX = buffer.getFloat();
    		imu.accelY = buffer.getFloat();
    		imu.accelZ = buffer.getFloat();
    		imu.AngRateX = buffer.getFloat();
    		imu.AngRateY = buffer.getFloat();
    		imu.AngRateZ = buffer.getFloat();
    		imu.StabMagX = buffer.getFloat();
    		imu.StabMagY = buffer.getFloat();
    		imu.StabMagZ = buffer.getFloat();
    		imu.M11 = buffer.getFloat();
    		imu.M12 = buffer.getFloat();
    		imu.M13 = buffer.getFloat();
    		imu.M21 = buffer.getFloat();
    		imu.M22 = buffer.getFloat();
    		imu.M23 = buffer.getFloat();
    		imu.M31 = buffer.getFloat();
    		imu.M32 = buffer.getFloat();
    		imu.M33 = buffer.getFloat();

    		imu.timer = (buffer.getInt() & 0xffffffff)/62.5;

    		log.trace("IMU accel, ang rate, mag, MAT " + imu.accelX + " " + imu.accelY + " " + imu.accelZ + " timer " + imu.timer);
    	
    	}
    	else
    	{
    		log.warn("Unknown IMU message " + imu.AHRSId);
    	}
    	
    	imuData.add(imu);
    	
    	if (imuData.size() == NRecords)
    	{
    		log.info("IMU data Records " + imuData.size());
    		
    		return imuData;
    	}
    	
    	return imu;
    }
    
    // config packets
    public Date clockDeploy = null;
    public int NBins = -1;
    public int Mode = 0;
    public int wMode = 0;
    public int NSamp = 0;
    
    private ArrayList<Attribute> readUserConfig()
    {
    	// This is the last config packet at the start of the datafile
    	
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
    	NBins = buffer.getShort() & 0xffff;
    	int BinLength = buffer.getShort() & 0xffff;
		attributes.add(new Attribute("nortek_userconfig_binlength", BinLength));
		
    	int MeasInterval = buffer.getShort() & 0xffff;
		attributes.add(new Attribute("nortek_userconfig_measinterval", MeasInterval));

		byte[] DeployName = new byte[6];
        buffer.get(DeployName);
    	int WrapMode = buffer.getShort() & 0xffff;
    	clockDeploy = readTimestamp();
    	attributes.add(new Attribute("nortek_userconfig_clock_deploy", netcdfDate.format(clockDeploy)));
    	
    	int DiagInterval = buffer.getInt() & 0xffffffff;
    	Mode = buffer.getShort() & 0xffff;
    	int AdjSoundSpeed = buffer.getShort() & 0xffff;
    	int NSampDiag = buffer.getShort() & 0xffff;
    	int NBeamsCellDiag = buffer.getShort() & 0xffff;
    	int NPingsDiag = buffer.getShort() & 0xffff;
    	    	
    	int ModeTest = buffer.getShort() & 0xffff;
    	int AnaInAddr = buffer.getShort() & 0xffff;
    	int SWVersion = buffer.getShort() & 0xffff;
		attributes.add(new Attribute("nortek_userconfig_swveraion", SWVersion));

		int Spare = buffer.getShort() & 0xffff;
        byte[] VelAdjTable = new byte[180];
        buffer.get(VelAdjTable);
        byte[] Comments = new byte[180];
        buffer.get(Comments);
        log.debug("User Comment " + cleanString(Comments));
    	wMode = buffer.getShort() & 0xffff;
    	
    	int DynPercPos = buffer.getShort() & 0xffff;
    	int wT1 = buffer.getShort() & 0xffff;
    	int wT2 = buffer.getShort() & 0xffff;
    	int wT3 = buffer.getShort() & 0xffff;
    	NSamp = buffer.getShort() & 0xffff;
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
        
    	log.debug(String.format("Mode 0x%02x WAVE Mode 0x%02x DiagInt %d NSampDiag %d NSampWave %d",  Mode, wMode, DiagInterval, NSampDiag, NSamp));
    	
        log.info("User NBins " + NBins + " NPings " + NPings + " time deploy "  + clockDeploy);
        
        for(Attribute a : attributes)
        {
        	log.info("Attribute : " + a.toString());
        }
        
        return attributes;
    }
    
    private void readHeadConfig()
    {
        int Config = buffer.getShort() & 0xffff;
        int Frequency = buffer.getShort() & 0xffff;
        int Type = buffer.getShort() & 0xffff;
        byte[] SerialNo = new byte[12];
        buffer.get(SerialNo);
        String sn = cleanString(SerialNo);
        
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
    	attributes.add(new Attribute("nortek_headconfig_serialno", sn));
    	attributes.add(new Attribute("nortek_headconfig_type", Type));

        log.info("Head Beams " + NBeams + " freq " + Frequency + " kHz" + " serial no. " + sn);
    }
    
    public String serialNo;
    
    public String cleanString(byte[] ba)
    {
    	for(int i=0;i<ba.length;i++)
    	{
    		if (!Character.isLetterOrDigit(ba[i]) && ba[i] != '.')
    		{
    			ba[i] = ' ';
    		}
    	}
    	
    	return new String(ba, StandardCharsets.UTF_8).trim();
    }
    
	public void readHWconfig()
    {    
    	byte[] SerialNo = new byte[14];
    	buffer.get(SerialNo);
    	serialNo = cleanString(SerialNo);
    	
    	int Config = buffer.getShort() & 0xffff;
    	int Frequency = buffer.getShort() & 0xffff;
    	int PICversion = buffer.getShort() & 0xffff;
    	int HWversion = buffer.getShort() & 0xffff;
    	int RecSize = (buffer.getShort() & 0xffff);

    	attributes.add(new Attribute("nortek_hwconfig_HWversion", HWversion));
    	attributes.add(new Attribute("nortek_hwconfig_recsize", RecSize));
    	attributes.add(new Attribute("nortek_hwconfig_serialno", serialNo));
    	
    	log.info(String.format("HW Config 0x%02x Serial Number '%s' ", Config, serialNo));
    }
    
    // self contained data
    
    private void readProbeCheck()
    {
    	int samples = buffer.getShort() & 0xffff;
    	
    	log.debug("Probe Check samples " + samples );
    }   
    
    public class VelocityDataHeader
    {
    	public Timestamp ts;
    	
    	public String toString()
    	{
    		return ("VelocityDataHeader: " + sdf.format(ts) + " NRecords " + NRecords);
    	}
    }
    
    private VelocityDataHeader readVectorVelocityHeader()
	{
    	VelocityDataHeader vdh = new VelocityDataHeader();
    	
    	vdh.ts = readTimestamp();
    
    	dataPoint = 0;
    	imuData.clear();
    	velocityData.clear();
    	
    	NRecords = buffer.getShort() & 0xffff;

    	return vdh;
	}

	public int NRecords;

	public class WaveDataHeader
    {
    	public Timestamp ts;
    	public float blanking;
    	public float battery;
    	public float soundSpeed;
    	public float head;
    	public float pitch;
    	public float roll;
    	public float minPres;
    	public float maxPres;
    	public float temp;
    	public float cellSize;
    	
    	public int noise1;
    	public int noise2;
    	public int noise3;
    	public int noise4;

    	public int procMagn1;
    	public int procMagn2;
    	public int procMagn3;
    	public int procMagn4;

    	public String toString()
    	{
    		return ("WaveDataHeader: " + sdf.format(ts) + " NRecords " + NRecords + " Blanking " + blanking);
    	}
    }
    
    private WaveDataHeader readAwacWaveDataHeader()
	{
    	WaveDataHeader wdh = new WaveDataHeader();
    	
    	wdh.ts = readTimestamp();
    
    	dataPoint = 0;
    	
    	waveData.clear();
    	
    	NRecords = buffer.getShort() & 0xffff;
    	wdh.blanking = (buffer.getShort() & 0xffff) * 0.1f;
    	wdh.battery = (buffer.getShort() & 0xffff) * 0.1f;
    	wdh.soundSpeed = (buffer.getShort() & 0xffff) * 0.1f;
    	wdh.head = (buffer.getShort()) * 0.1f;
    	wdh.pitch = (buffer.getShort()) * 0.1f;
    	wdh.roll = (buffer.getShort()) * 0.1f;
    	wdh.minPres = (buffer.getShort() & 0xffff) * 0.001f;
    	wdh.maxPres = (buffer.getShort() & 0xffff) * 0.001f;
    	wdh.temp = (buffer.getShort()) * 0.01f;
    	wdh.cellSize = (buffer.getShort() & 0xffff);

    	wdh.noise1 = (buffer.get() & 0xff);
    	wdh.noise2 = (buffer.get() & 0xff);
    	wdh.noise3 = (buffer.get() & 0xff);
    	wdh.noise4 = (buffer.get() & 0xff);
    	
    	wdh.procMagn1 = (buffer.getShort() & 0xffff);
    	wdh.procMagn2 = (buffer.getShort() & 0xffff);
    	wdh.procMagn3 = (buffer.getShort() & 0xffff);
    	wdh.procMagn4 = (buffer.getShort() & 0xffff);

    	log.debug(wdh);

    	return wdh;
	}
        
    public class ProfileVelocityData
    {
    	public Timestamp ts;
    	public float battery;
    	float soundSpeed;
    	public float anaIn1;
    	public float anaIn2;
    	public float heading;
    	public float pitch;
    	public float roll;
    	public float temp;
    	int error;
    	int status;
    	public float anaIn;
    	public int pressureMSB;
    	public int pressureLSW;
    	public float pressure;
    	public float velocityA[];
    	public float velocityB[];
    	public float velocityC[];
    	byte ampA[];
    	byte ampB[];
    	byte ampC[];
    }
    
    private ProfileVelocityData readAquadoppProfileVelocityData()
	{
    	ProfileVelocityData vd = new ProfileVelocityData();
    	
    	vd.ts = readTimestamp();
    	vd.error = buffer.getShort() & 0xffff;
    	vd.anaIn1 = (buffer.getShort() & 0xffff);
    	vd.battery = (buffer.getShort() & 0xffff) * 0.1f;
    	
    	vd.anaIn2 = (buffer.getShort() & 0xffff);
    	vd.soundSpeed =  vd.anaIn2 * 0.1f;

    	vd.heading = (buffer.getShort() & 0xffff) * 0.1f;
    	vd.pitch = (buffer.getShort() & 0xffff) * 0.1f;
    	vd.roll = (buffer.getShort() & 0xffff) * 0.1f;
    	
    	vd.pressureMSB = buffer.get() & 0xff;
    	vd.status = buffer.get() & 0xff;
    	vd.pressureLSW = buffer.getShort() & 0xffff;
    	
    	vd.pressure = (vd.pressureMSB * 65536 + vd.pressureLSW) * 0.001f;

    	vd.temp = (buffer.getShort() & 0xffff) * 0.01f;
    	
    	// creating these will have to be GC'ed later, is there a better way?
    	vd.velocityA = new float[NBins]; 
    	vd.velocityB = new float[NBins]; 
    	vd.velocityC = new float[NBins]; 
    	vd.ampA = new byte[NBins]; 
    	vd.ampB = new byte[NBins]; 
    	vd.ampC = new byte[NBins]; 
    
    	for (int i=0;i<NBins;i++)
    	{
    		vd.velocityA[i] = buffer.getShort();
    	}
    	for (int i=0;i<NBins;i++)
    	{
    		vd.velocityB[i] = buffer.getShort();
    	}
    	for (int i=0;i<NBins;i++)
    	{
    		vd.velocityC[i] = buffer.getShort();
    	}
    	for (int i=0;i<NBins;i++)
    	{
    		vd.ampA[i] = buffer.get();
    	}
    	for (int i=0;i<NBins;i++)
    	{
    		vd.ampB[i] = buffer.get();
    	}
    	for (int i=0;i<NBins;i++)
    	{
    		vd.ampC[i] = buffer.get();
    	}

    	log.debug("ProfileVelocityData:" + sdf.format(vd.ts) + " pressure " + vd.pressure + " temp " + vd.temp + " head " + vd.heading);
    	
    	return vd;
	}
    public class HRProfileVelocityData
    {
    	public Timestamp ts;
    	public int millisec;
    	public float battery;
    	float soundSpeed;
    	public float anaIn1;
    	public float anaIn2;
    	public float heading;
    	public float pitch;
    	public float roll;
    	public float temp;
    	int error;
    	int status;
    	public float anaIn;
    	public int pressureMSB;
    	public int pressureLSW;
    	public float pressure;
    	public int beams;
    	public int cells;
    	public int vlag2[] = new int[3];
    	public int alag[] = new int[3];;
    	public int clag[] = new int[3];;
    	public float velocity[][];
    	byte amp[][];
    	byte cor[][];
    	
    	public String toString()
    	{
        	return ("HRProfileVelocityData:" + sdfms.format(ts) + " ms " + millisec + " pressure " + pressure + " temp " + temp + " beams " + beams + " cells " + cells + " pitch " + pitch + " roll " + roll);
    	}
    }

    private HRProfileVelocityData readHRAquadoppProfileVelocityData()
	{
    	HRProfileVelocityData vd = new HRProfileVelocityData();
    	
    	vd.ts = readTimestamp();
    	vd.millisec = buffer.getShort() & 0xffff;
    	vd.error = buffer.getShort() & 0xffff;
    	vd.battery = (buffer.getShort() & 0xffff) * 0.1f;
    	
    	vd.ts.setTime(vd.ts.getTime() + vd.millisec);
    	
    	vd.soundSpeed =  (buffer.getShort() & 0xffff) * 0.1f;

    	vd.heading = (buffer.getShort()) * 0.1f;
    	vd.pitch = (buffer.getShort()) * 0.1f;
    	vd.roll = (buffer.getShort()) * 0.1f;
    	
    	vd.pressureMSB = buffer.get() & 0xff;
    	vd.status = buffer.get() & 0xff;
    	vd.pressureLSW = buffer.getShort() & 0xffff;
    	
    	vd.pressure = (vd.pressureMSB * 65536 + vd.pressureLSW) * 0.001f;

    	vd.temp = (buffer.getShort() & 0xffff) * 0.01f;
    	vd.anaIn1 = (buffer.getShort() & 0xffff) * 0.01f;
    	vd.anaIn2 = (buffer.getShort() & 0xffff) * 0.01f;
    	vd.beams = buffer.get() & 0xff;
    	vd.cells = buffer.get() & 0xff;
    	
    	// creating these will have to be GC'ed later, is there a better way?
    	vd.velocity = new float[vd.beams][vd.cells]; 
    	vd.amp = new byte[vd.beams][vd.cells]; 
    	vd.cor = new byte[vd.beams][vd.cells]; 
    
    	for (int beam=0;beam<vd.beams;beam++)
    	{
	    	for (int cell=0;cell<vd.cells;cell++)
	    	{
	    		vd.velocity[beam][cell] = buffer.getShort();
	    	}
    	}
    	for (int beam=0;beam<vd.beams;beam++)
    	{
	    	for (int cell=0;cell<vd.cells;cell++)
	    	{
	    		vd.amp[beam][cell] = buffer.get();
	    	}
    	}
    	for (int beam=0;beam<vd.beams;beam++)
    	{
	    	for (int cell=0;cell<vd.cells;cell++)
	    	{
	    		vd.cor[beam][cell] = buffer.get();
	    	}
    	}

    	return vd;
	}
    
    public class VectorSystemData
    {
    	public Timestamp ts;
    	public float battery;
    	float soundSpeed;
    	public float heading;
    	public float pitch;
    	public float roll;
    	public float temp;
    	int error;
    	int status;
    	public float anaIn;
    	
    	public String toString()
    	{
    		return sdf.format(ts) + " battery " + battery + " temperature " + temp;
    	}
    }
    	    
    private VectorSystemData readVectorSystemData()
	{
    	VectorSystemData sd = new VectorSystemData();
    			
    	sd.ts = readTimestamp();
    	
    	sd.battery = (buffer.getShort() & 0xffff) * 0.1f;
    	
    	sd.soundSpeed = (buffer.getShort() & 0xffff) * 0.1f;
    	sd.heading = (buffer.getShort() & 0xffff) * 0.1f;
    	sd.pitch = (buffer.getShort() & 0xffff) * 0.1f;
    	sd.roll = (buffer.getShort() & 0xffff) * 0.1f;
    	sd.temp = (buffer.getShort() & 0xffff) * 0.01f;
    	
    	sd.error = buffer.get() & 0xff;
    	sd.status = buffer.get() & 0xff;
    	sd.anaIn = (buffer.getShort() & 0xffff) * 0.01f;
    	
    	return sd;
	}

    public static void main(String args[])
    {
    	String home = System.getProperty("user.home");
        PropertyConfigurator.configure("log4j.properties");

    	NortekParse np = new NortekParse();
    	
    	try
		{
			np.open(new File(args[0]));
			
			Object o;
			while((o = np.read()) != null)
			{
				NortekParse.log.trace("Read Object : " + o);
			}
			
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
