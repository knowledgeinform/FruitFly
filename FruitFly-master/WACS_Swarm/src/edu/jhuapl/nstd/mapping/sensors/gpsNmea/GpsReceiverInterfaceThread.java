/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.mapping.sensors.gpsNmea;
import edu.jhuapl.nstd.mapping.sensors.gpsNmea.NMEAClient;
import edu.jhuapl.nstd.mapping.sensors.gpsNmea.NMEAEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

//import edu.jhuapl.emaps.Protobufs;
//import edu.jhuapl.nstd.mapping.segway.NMEAStreamLogger;

/**
 * The simplest you can write
 */
public class GpsReceiverInterfaceThread extends NMEAClient
{
    static public class GpsReading
    {
        static public enum FixQuality
        {
            INVALID(0),
            GPS_FIX(1),
            DGPS_FIX(2),
            RTK_FIX(4);

            private int m_Value;

            FixQuality (int value)
            {
                m_Value = value;
            }

            public int getValue ()
            {
                return m_Value;
            }
        }

        public GpsReading ()
        {
            m_TimestampSec = -1;
            m_FixQuality = FixQuality.INVALID;
            m_LatDecDeg = 0;
            m_LonDecDeg = 0;
            m_NumSatellites = 0;
            m_PositionUncertaintyMeters = 1000;
            m_AltitudeMSLMeters = 0;
            m_GeoidHeightAboveWGS84Meters = 0;
            m_TimeSinceDGPSSec = 0;
            m_DGPSStationId = -1;
            m_rawMessage = "";
        }

        public GpsReading(GpsReading gpsDataToCopy)
        {
            gpsDataToCopy.copyTo(this);
        }

        public void copyTo (GpsReading copy)
        {
            copy.m_TimestampSec = this.m_TimestampSec;
            copy.m_LatDecDeg = this.m_LatDecDeg;
            copy.m_LonDecDeg = this.m_LonDecDeg;
            copy.m_FixQuality = this.m_FixQuality;
            copy.m_NumSatellites = this.m_NumSatellites;
            copy.m_PositionUncertaintyMeters = this.m_PositionUncertaintyMeters;
            copy.m_AltitudeMSLMeters = this.m_AltitudeMSLMeters;
            copy.m_GeoidHeightAboveWGS84Meters = this.m_GeoidHeightAboveWGS84Meters;
            copy.m_TimeSinceDGPSSec = this.m_TimeSinceDGPSSec;
            copy.m_DGPSStationId = this.m_DGPSStationId;
            copy.m_rawMessage = this.m_rawMessage;
        }

        public boolean isFixValid (double positionUncertaintyThresholdMeters)
        {
            return (m_FixQuality != FixQuality.INVALID &&
                    m_PositionUncertaintyMeters <= positionUncertaintyThresholdMeters);
        }

        public String m_rawMessage;
        public long m_TimestampSec;
        public double m_LatDecDeg;
        public double m_LonDecDeg;

        public FixQuality m_FixQuality;
        public int m_NumSatellites;
        public double m_PositionUncertaintyMeters;
        public double m_AltitudeMSLMeters;
        public double m_GeoidHeightAboveWGS84Meters;
        public double m_TimeSinceDGPSSec;
        public int m_DGPSStationId;
        public long m_systemTimeLatestReading;

        public void readObject(DataInputStream input) throws IOException
        {
            try
            {
                byte dataFormatIdx = input.readByte();

                if (dataFormatIdx >= 0)
                {
                    m_TimestampSec = input.readLong();
                    m_LatDecDeg = input.readDouble();
                    m_LonDecDeg = input.readDouble();
                    m_FixQuality = FixQuality.valueOf(input.readUTF());
                    m_NumSatellites = input.readInt();
                    if (dataFormatIdx >= 1)
                        m_PositionUncertaintyMeters = input.readDouble();
                    else
                        input.readDouble();    //Used to be HDOP, but not really
                    m_AltitudeMSLMeters = input.readDouble();
                    m_GeoidHeightAboveWGS84Meters = input.readDouble();
                    m_TimeSinceDGPSSec = input.readDouble();
                    m_DGPSStationId = input.readInt();
                }
            }
            catch (IOException e)
            {
                throw e;
            }
        }

        public void writeObject(DataOutputStream output) throws IOException
        {
            output.writeByte (1); //data format
            output.writeLong(m_TimestampSec);
            output.writeDouble(m_LatDecDeg);
            output.writeDouble(m_LonDecDeg);
            output.writeUTF(m_FixQuality.name());
            output.writeInt(m_NumSatellites);
            output.writeDouble(m_PositionUncertaintyMeters);
            output.writeDouble(m_AltitudeMSLMeters);
            output.writeDouble(m_GeoidHeightAboveWGS84Meters);
            output.writeDouble(m_TimeSinceDGPSSec);
            output.writeInt(m_DGPSStationId);
        }
/*
        public Protobufs.GpsTag getProtobuf() {
            Protobufs.GpsTag.Builder gps_tag = Protobufs.GpsTag.newBuilder()
                .setTimestamp(m_TimestampSec)
                .setLat(m_LatDecDeg)
                .setLon(m_LonDecDeg)
                .setAlt(m_AltitudeMSLMeters)
                .setFixQuality(m_FixQuality.name())
                .setNumSats(m_NumSatellites)
                .setGeoidHeightAboveWGS84(m_GeoidHeightAboveWGS84Meters)
                .setTimeSinceDGPSSec(m_TimeSinceDGPSSec)
                .setStationIdDGPS(m_DGPSStationId);
            return gps_tag.build();
        }
*/
    }

    GpsReading m_LocalGpsReading = new GpsReading();
    final Object m_LockObject = new Object();

    String m_GpsPrefix = "GP";
    String[] m_GpsSentences = {"RMC", "GGA", "GSA"};
    NMEAStreamLogger m_NMEAStreamLogger;
    
    public GpsReceiverInterfaceThread()
    {
        this(null);
    }
    
    public GpsReceiverInterfaceThread(NMEAStreamLogger nmeaStreamLogger) {
        this.m_NMEAStreamLogger = nmeaStreamLogger;
        this.setDevicePrefix(m_GpsPrefix);
        this.setSentenceArray(m_GpsSentences);
    }    

    public void dataDetectedEvent(NMEAEvent e)
    {
        //System.out.println("Received:" + e.getContent());

        String gpsMessage = e.getContent();
        
        long currentTime = System.currentTimeMillis();
        if(m_NMEAStreamLogger!=null) {
            m_NMEAStreamLogger.logNMEAStream(currentTime, gpsMessage);
        }
        
        /*
        if (gpsMessage.startsWith("$" + m_GpsPrefix + "RMC"))
        {
            try
            {
                GpsReading newGpsReading = new GpsReading();
                newGpsReading.m_systemTimeLatestReading = currentTime;
                newGpsReading.m_rawMessage = gpsMessage;
                NMEAStringParsers.parseRMC(gpsMessage, newGpsReading);

                synchronized (m_LockObject)
                {
                    newGpsReading.copyTo(m_LocalGpsReading);
                }
            }
            catch (Exception ex)
            {
                System.err.println ("Caught exception parsing NMEA stream: " + gpsMessage);
                ex.printStackTrace();
                System.err.println ("Caught this exception and ignored it, moving on...");
            }
        }
        */
        
        if (gpsMessage.startsWith("$" + m_GpsPrefix + "GGA"))
        {
            /*TODO:
			add in a parse GST option here, pair with GGA values and send with GpsReading as a single packet (example file gstlog.txt in radmaps);
            verify GPS coordinates arent reused for different scan positions (without filter, too) into converter;*/
            
            try
            {
                GpsReading newGpsReading = new GpsReading();
                newGpsReading.m_rawMessage = gpsMessage;
                NMEAStringParsers.parseGGA(gpsMessage, newGpsReading);

                synchronized (m_LockObject)
                {
                    newGpsReading.copyTo(m_LocalGpsReading);
                }
            }
            catch (Exception ex)
            {
                //System.err.println ("Caught exception parsing NMEA stream: " + gpsMessage);
                //ex.printStackTrace();
                //System.err.println ("Caught this exception and ignored it, moving on...");
            }
        }

    }

    public void open (String comPort, int baudRate) throws Exception
    {
        this.initClient();
        this.setReader (new GpsReceiverSerialInterface(comPort, baudRate, this.getListeners()));

    }

    public void start ()
    {
        this.startWorking();
    }

    public void close()
    {
        this.stopWorking();
    }

    public void copyLatestData(GpsReading gpsData)
    {
        synchronized (m_LockObject)
        {
            m_LocalGpsReading.copyTo(gpsData);
            m_LocalGpsReading = new GpsReading();
        }
    }




    public static void main(String[] args)
    {
        //String prefix = "GP";
        //String[] array = {"RMC", "GGA", "GSA"};

        //GpsReceiverInterfaceThread customClient = new GpsReceiverInterfaceThread(prefix, array);
        //customClient.initClient();
        //customClient.setReader(new GpsReceiverSerialInterface(customClient.getListeners())); // Serial Port reader
        //customClient.startWorking();

        try
        {
            GpsReceiverInterfaceThread gpsClient = new GpsReceiverInterfaceThread();
            gpsClient.open("COM5", 19200);
            gpsClient.start();
            System.out.println ("Successfully connected to GPS receiver");

            while (true)
            {
                GpsReading gpsData = new GpsReceiverInterfaceThread.GpsReading ();
                gpsClient.copyLatestData (gpsData);

                System.out.println ("Current data: " + gpsData.m_TimestampSec + " - " + (gpsData.m_FixQuality!=GpsReading.FixQuality.INVALID) + ": " + gpsData.m_LatDecDeg + ", " + gpsData.m_LonDecDeg);

                Thread.sleep (100);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
