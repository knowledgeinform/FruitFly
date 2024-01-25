package shadowantennapointingclient;

import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;


public class ShadowTelemetryListener extends Thread
{
    static public class TelemetryMessage
    {
        public TelemetryMessage()
        {
            messageLength = 172;
            messageVersion = 0;
            busNumber = 0;
        }

        public long timestamp_ms;
        public int messageLength;
        public int messageVersion;
        public int busNumber;
        public int knobsModeCommand;   //0-internal  1-external
        public int insMode;            //0-init  1-align w/GPS  2-full INS(air/gnd)  3-INS no GPS(air)  4-dead reckoning  5-INS no GPS(gnd)  6-align w/o GPS
        public double longitude_rad;
        public double latitude_rad;
        public double groundSpeedNorth_mps;
        public double groundSpeedEast_mps;
        public double trueHeading_rad;
        public double magneticDeclination_rad;
        public double roll_rad;
        public double pitch_rad;
        public double gpsAltitude_m;
        public double barometricAltitude_m;
        public double verticalSpeed_mps;
        public double verticalAcceleration_mps2;
        public double indicatedAirspeed_mps;
        public double trueAirspeed_mps;
        public double airTemp_k;
        public double gpsPositionalDilutionOfPrecision;
        public double fuelLevel_l;
        public double gpsTimeOfWeek_s;
        public int gpsWeekNumber;
        public int flapsPosition;      //0-Up(no flaps)  1-mid(partial flaps)  2-down(full flaps)
    }


    private final Object m_lock = new Object();
    private TelemetryMessage m_telemetryMessage = null;
    private int m_receivePort;

    public ShadowTelemetryListener()
    {        
        m_receivePort = 9674;
    }

    public boolean copyLatestTelemetryMessage(TelemetryMessage copyTo)
    {
        synchronized (m_lock)
        {
            if (m_telemetryMessage != null && m_telemetryMessage.timestamp_ms != copyTo.timestamp_ms)
            {
                copyTo.timestamp_ms = m_telemetryMessage.timestamp_ms;
                copyTo.messageLength = m_telemetryMessage.messageLength;
                copyTo.messageVersion = m_telemetryMessage.messageVersion;
                copyTo.busNumber = m_telemetryMessage.busNumber;
                copyTo.knobsModeCommand = m_telemetryMessage.knobsModeCommand;
                copyTo.insMode = m_telemetryMessage.insMode;
                copyTo.longitude_rad = m_telemetryMessage.longitude_rad;
                copyTo.latitude_rad = m_telemetryMessage.latitude_rad;
                copyTo.groundSpeedNorth_mps = m_telemetryMessage.groundSpeedNorth_mps;
                copyTo.groundSpeedEast_mps = m_telemetryMessage.groundSpeedEast_mps;
                copyTo.trueHeading_rad = m_telemetryMessage.trueHeading_rad;
                copyTo.magneticDeclination_rad = m_telemetryMessage.magneticDeclination_rad;
                copyTo.roll_rad = m_telemetryMessage.roll_rad;
                copyTo.pitch_rad = m_telemetryMessage.pitch_rad;
                copyTo.gpsAltitude_m = m_telemetryMessage.gpsAltitude_m;
                copyTo.barometricAltitude_m = m_telemetryMessage.barometricAltitude_m;
                copyTo.verticalSpeed_mps = m_telemetryMessage.verticalSpeed_mps;
                copyTo.verticalAcceleration_mps2 = m_telemetryMessage.verticalAcceleration_mps2;
                copyTo.indicatedAirspeed_mps = m_telemetryMessage.indicatedAirspeed_mps;
                copyTo.trueAirspeed_mps = m_telemetryMessage.trueAirspeed_mps;
                copyTo.airTemp_k = m_telemetryMessage.airTemp_k;
                copyTo.gpsPositionalDilutionOfPrecision = m_telemetryMessage.gpsPositionalDilutionOfPrecision;
                copyTo.fuelLevel_l = m_telemetryMessage.fuelLevel_l;
                copyTo.gpsTimeOfWeek_s = m_telemetryMessage.gpsTimeOfWeek_s;
                copyTo.gpsWeekNumber = m_telemetryMessage.gpsWeekNumber;
                copyTo.flapsPosition = m_telemetryMessage.flapsPosition;

                return true;
            }
            else
            {
                return false;
            }
        }
    }

    private int calcChecksum(final byte[] data, final int dataSize)
    {
        int checksum = 0;
        for (int i = 0; i < dataSize; ++i)
        {
            checksum += (data[i] & 0xFF);
        }

        return checksum;
    }

    @Override
    public void run()
    {
        try
        {
            byte[] receiveData = new byte[1024];
            ByteBuffer receiveBuffer = ByteBuffer.wrap(receiveData);
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            DatagramSocket receiveSocket = new DatagramSocket(m_receivePort);

            while (true)
            {
                receiveSocket.receive(packet);
                int calculatedChecksum = calcChecksum(receiveData, packet.getLength() - 4);
                int receivedChecksum = receiveBuffer.getInt(packet.getLength() - 4);

                if (calculatedChecksum == receivedChecksum)
                {
                    receiveBuffer.rewind();

                    synchronized (m_lock)
                    {
                        if (m_telemetryMessage == null)
                        {
                            m_telemetryMessage = new TelemetryMessage();
                        }

                        m_telemetryMessage.timestamp_ms = System.currentTimeMillis();
                        m_telemetryMessage.messageLength = receiveBuffer.getInt();
                        m_telemetryMessage.messageVersion = receiveBuffer.getInt();
                        m_telemetryMessage.busNumber = receiveBuffer.getInt();
                        m_telemetryMessage.knobsModeCommand = receiveBuffer.getInt();
                        m_telemetryMessage.insMode = receiveBuffer.getInt();
                        m_telemetryMessage.longitude_rad = receiveBuffer.getDouble();
                        m_telemetryMessage.latitude_rad = receiveBuffer.getDouble();
                        m_telemetryMessage.groundSpeedNorth_mps = receiveBuffer.getDouble();
                        m_telemetryMessage.groundSpeedEast_mps = receiveBuffer.getDouble();
                        m_telemetryMessage.trueHeading_rad = receiveBuffer.getDouble();
                        m_telemetryMessage.magneticDeclination_rad = receiveBuffer.getDouble();
                        m_telemetryMessage.roll_rad = receiveBuffer.getDouble();
                        m_telemetryMessage.pitch_rad = receiveBuffer.getDouble();
                        m_telemetryMessage.gpsAltitude_m = receiveBuffer.getDouble();
                        m_telemetryMessage.barometricAltitude_m = receiveBuffer.getDouble();
                        m_telemetryMessage.verticalSpeed_mps = receiveBuffer.getDouble();
                        m_telemetryMessage.verticalAcceleration_mps2 = receiveBuffer.getDouble();
                        m_telemetryMessage.indicatedAirspeed_mps = receiveBuffer.getDouble();
                        m_telemetryMessage.trueAirspeed_mps = receiveBuffer.getDouble();
                        m_telemetryMessage.airTemp_k = receiveBuffer.getDouble();
                        m_telemetryMessage.gpsPositionalDilutionOfPrecision = receiveBuffer.getDouble();
                        m_telemetryMessage.fuelLevel_l = receiveBuffer.getDouble();
                        m_telemetryMessage.gpsTimeOfWeek_s = receiveBuffer.getDouble();
                        m_telemetryMessage.gpsWeekNumber = receiveBuffer.getInt();
                        m_telemetryMessage.flapsPosition = receiveBuffer.getInt();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
