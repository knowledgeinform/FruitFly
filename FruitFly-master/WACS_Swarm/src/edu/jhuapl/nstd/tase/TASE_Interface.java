package edu.jhuapl.nstd.tase;

import edu.jhuapl.nstd.util.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TASE_Interface implements Runnable
{
    private XCommSerialPort _serialPort;
    private boolean _isRunning = false;
    private boolean _shutdown = false;
    private List<TASE_TelemetryListener> _telemListeners = new ArrayList<TASE_TelemetryListener>();
    private List<TASE_PointingAnglesListener> _angleListeners = new ArrayList<TASE_PointingAnglesListener>();
    // parse state variables
    TASE_Parse_State _currState;
    int _size, _group, _type;
    byte[] _currPacket;
    int _currIdx;
    int _checksum, _checksumTASE;

    public TASE_Interface() throws Exception
    {
        this("COM1", 57600);
    }

    public TASE_Interface(String port) throws Exception
    {
        this(port, 57600);
    }

    public TASE_Interface(String port, int baud) throws Exception
    {
        try
        {
            System.out.println ("Connecting to TASE at " + port + ":" + baud);
            _serialPort = new XCommSerialPort(port, baud);
            System.out.println ("Connection to TASE succesful!");
        }
        catch (Exception ex)
        {
            System.out.println ("Connection to TASE failed!");
            Logger.getLogger(TASE_Interface.class.getName()).log(Level.SEVERE, null, ex);
        }
        _currState = TASE_Parse_State.SYNC1;
    }

    public boolean isRunning()
    {
        return _isRunning;
    }

    public void forceShutdown()
    {
        _shutdown = true;
    }

    public void addTASETelemetryListener(TASE_TelemetryListener obj)
    {
        _telemListeners.add(obj);
    }

    public void addTASEPointingAnglesListener(TASE_PointingAnglesListener obj)
    {
        _angleListeners.add(obj);
    }

    public void removeTASETelemetryListener(TASE_TelemetryListener obj)
    {
        _telemListeners.remove(obj);
    }

    public void removeTASEPointingAnglesListener(TASE_PointingAnglesListener obj)
    {
        _angleListeners.remove(obj);
    }

    public void run()
    {
        if (_serialPort != null)
        {
            byte[] bytesRead;
            int i;

            try
            {
                _isRunning = true;
                while (!_shutdown)
                {
                    bytesRead = _serialPort.readAvailableBytes();
                    if (bytesRead != null)
                    {
                        for (i = 0; i < bytesRead.length; i++)
                        {
                            parseTaseByte(bytesRead[i]);
                        }
                    }

                    Thread.sleep(100);
                }

                System.out.println("TASE Shutting down...");
                _serialPort.close();
                _isRunning = false;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                _serialPort.close();
                _isRunning = false;
            }
        }
    }

    private enum TASE_Parse_State
    {

        SYNC1,
        SYNC2,
        SYNC3,
        SIZE,
        GROUP_BYTE,
        PACKET_TYPE,
        READING_DATA,
        CHECKSUM_HI,
        CHECKSUM_LO
    }

    private void parseTaseByte(byte newByte)
    {
        //Bad size, start over at new sync
        if (_size <= 0 && _currState.ordinal() > TASE_Parse_State.SIZE.ordinal())
        {
            System.out.println ("Bad size in TASE data, restarting at sync");
            _currState = TASE_Parse_State.SYNC1;
        }
        

        switch (_currState)
        {
            case SYNC1:
                if (newByte == (byte) 0x00)
                {
                    // ok, start the parsing
                    _checksum = 0;
                    _currState = TASE_Parse_State.SYNC2;
                }
                break;

            case SYNC2:
                if (newByte == (byte) 0xFF)
                {
                    // keep going
                    _checksum += (int) newByte & 0x00FF;
                    _currState = TASE_Parse_State.SYNC3;
                }
                else
                {
                    if (newByte == 0x00)
                    {
                        // duplicate first bytes
                        _checksum = 0;
                        _currState = TASE_Parse_State.SYNC2;
                    }
                    else
                    {
                        _currState = TASE_Parse_State.SYNC1;
                    }
                }
                break;

            case SYNC3:
                if (newByte == (byte) 0x5A)
                {
                    // keep going
                    _checksum += (int) newByte & 0x00FF;
                    _currState = TASE_Parse_State.SIZE;
                }
                else
                {
                    if (newByte == 0x00)
                    {
                        // duplicate first bytes
                        _checksum = 0;
                        _currState = TASE_Parse_State.SYNC2;
                    }
                    else
                    {
                        _currState = TASE_Parse_State.SYNC1;
                    }
                }
                break;

            case SIZE:
                _checksum += (int) newByte & 0x00FF;
                _size = (int) newByte & 0x00FF;
                _size -= 2;
                _currState = TASE_Parse_State.GROUP_BYTE;
                break;

            case GROUP_BYTE:
                _checksum += (int) newByte & 0x00FF;
                _group = (int) newByte & 0x00FF;
                _currState = TASE_Parse_State.PACKET_TYPE;
                break;

            case PACKET_TYPE:
                _checksum += (int) newByte & 0x00FF;
                _type = (int) newByte & 0x00FF;
                _currState = TASE_Parse_State.READING_DATA;
                _currIdx = 0;
                _currPacket = new byte[_size];
                if (_currIdx == _currPacket.length)
                {
                    _currState = TASE_Parse_State.CHECKSUM_HI;
                }
                break;

            case READING_DATA:
                _checksum += (int) newByte & 0x00FF;
                _currPacket[_currIdx++] = newByte;
                if (_currIdx == _currPacket.length)
                {
                    _currState = TASE_Parse_State.CHECKSUM_HI;
                }
                break;

            case CHECKSUM_HI:
                _checksumTASE = ((int) newByte & 0x00FF) << 8;
                _currState = TASE_Parse_State.CHECKSUM_LO;
                break;

            case CHECKSUM_LO:
                _checksumTASE |= ((int) newByte & 0x00FF);
                if (_checksum == _checksumTASE)
                {
                    //System.out.println("TASE: read valid packet (group="+_group+", type="+_type+")");

                    if (_group == 0x10 && _type == 0x41)
                    {
                        parseGeoRefData(_currPacket);
                    }
                    if (_group == 0x08 && _type == 0x10)
                    {
                        parseGimbalAngle(_currPacket);
                    }

                }
                else
                {
                    System.out.println("TASE XSUM ERROR: " + _checksum + " != " + _checksumTASE);
                }
                _currState = TASE_Parse_State.SYNC1;
                break;

            default:
                System.out.println("Whoa mama, what state am I in? (" + _currState + ")");
                _currState = TASE_Parse_State.SYNC1;
                break;
        }
    }

    private void parseGeoRefData(byte[] packet)
    {
        int lat, lon, alt;
        int pdop, status;
        short roll, pitch, yaw;
        TASE_Telemetry telem;

        lat = ((int) packet[4] & 0x00FF) << 24;
        lat |= ((int) packet[5] & 0x00FF) << 16;
        lat |= ((int) packet[6] & 0x00FF) << 8;
        lat |= ((int) packet[7] & 0x00FF);

        lon = ((int) packet[8] & 0x00FF) << 24;
        lon |= ((int) packet[9] & 0x00FF) << 16;
        lon |= ((int) packet[10] & 0x00FF) << 8;
        lon |= ((int) packet[11] & 0x00FF);

        alt = ((int) packet[12] & 0x00FF) << 24;
        alt |= ((int) packet[13] & 0x00FF) << 16;
        alt |= ((int) packet[14] & 0x00FF) << 8;
        alt |= ((int) packet[15] & 0x00FF);

        roll = (short) (((int) packet[34] & 0x00FF) << 8);
        roll |= (int) packet[35] & 0x00FF;

        pitch = (short) (((int) packet[36] & 0x00FF) << 8);
        pitch |= (int) packet[37] & 0x00FF;

        yaw = (short) (((int) packet[38] & 0x00FF) << 8);
        yaw |= (int) packet[39] & 0x00FF;

        pdop = (int) packet[31] & 0x00FF;
        status = (int) packet[32] & 0x00FF;
        status >>= 5;

        telem = new TASE_Telemetry((double) lat / 1000.0 / 3600.0,
                (double) lon / 1000.0 / 3600.0,
                (double) alt / 100.0,
                (double) roll / 100.0,
                (double) pitch / 100.0,
                (double) yaw / 100.0,
                (double) pdop * 0.1,
                status);
        for (TASE_TelemetryListener listen : _telemListeners)
        {
            listen.handleTASE_Telemetry(telem);
        }
    }

    private void parseGimbalAngle(byte[] packet)
    {
        short pan, tilt;
        TASE_PointingAngles angles;

        pan = (short) (((int) packet[0] & 0x00FF) << 8);
        pan |= (int) packet[1] & 0x00FF;

        tilt = (short) (((int) packet[2] & 0x00FF) << 8);
        tilt |= (int) packet[3] & 0x00FF;

        angles = new TASE_PointingAngles((double) pan / 100.0, (double) tilt / 100.0);
        for (TASE_PointingAnglesListener listen : _angleListeners)
        {
            listen.handleTASE_PointingAngles(angles);
        }
    }

    public void sendTASE_SPOI(double lat, double lon, double altEllip,
            double vNorth, double vEast, double vDown)
    {
        sendVPS_DisableTracking();
        try
        {
            Thread.sleep(250);
        }
        catch (Exception e)
        {
        }

        int masLat, masLon, cmAlt;
        short cmNorth, cmEast, cmDown;
        int checksum = 0, i, idx = 0;
        byte[] packet = new byte[27];

        masLat = (int) (lat * 1000.0 * 3600.0);
        masLon = (int) (lon * 1000.0 * 3600.0);
        cmAlt = (int) (altEllip * 100.0);
        cmNorth = (short) (vNorth * 100.0);
        cmEast = (short) (vEast * 100.0);
        cmDown = (short) (vDown * 100.0);

        // header
        packet[idx++] = (byte) 0x00;
        packet[idx++] = (byte) 0xFF;
        packet[idx++] = (byte) 0x5A;
        packet[idx++] = (byte) 0x15;
        packet[idx++] = (byte) 0x10;
        packet[idx++] = (byte) 0x40;

        // data
        packet[idx++] = (byte) (masLat >> 24 & 0x00FF);
        packet[idx++] = (byte) (masLat >> 16 & 0x00FF);
        packet[idx++] = (byte) (masLat >> 8 & 0x00FF);
        packet[idx++] = (byte) (masLat & 0x00FF);
        packet[idx++] = (byte) (masLon >> 24 & 0x00FF);
        packet[idx++] = (byte) (masLon >> 16 & 0x00FF);
        packet[idx++] = (byte) (masLon >> 8 & 0x00FF);
        packet[idx++] = (byte) (masLon & 0x00FF);
        packet[idx++] = (byte) (cmAlt >> 24 & 0x00FF);
        packet[idx++] = (byte) (cmAlt >> 16 & 0x00FF);
        packet[idx++] = (byte) (cmAlt >> 8 & 0x00FF);
        packet[idx++] = (byte) (cmAlt & 0x00FF);
        packet[idx++] = (byte) (cmNorth >> 8 & 0x00FF);
        packet[idx++] = (byte) (cmNorth & 0x00FF);
        packet[idx++] = (byte) (cmEast >> 8 & 0x00FF);
        packet[idx++] = (byte) (cmEast & 0x00FF);
        packet[idx++] = (byte) (cmDown >> 8 & 0x00FF);
        packet[idx++] = (byte) (cmDown & 0x00FF);
        packet[idx++] = 0x00;

        // checksum
        for (i = 0; i < idx; i++)
        {
            checksum += (int) (packet[i]) & 0x00FF;
        }
        packet[idx++] = (byte) (checksum >> 8 & 0x00FF);
        packet[idx++] = (byte) (checksum & 0x00FF);

        try
        {
            _serialPort.sendBytes(packet);
        }
        catch (Exception ex)
        {
            System.out.println("TASE SPOI SEND ERROR:");
            ex.printStackTrace();
        }
    }

    public void sendTASE_Stow()
    {
        int pan, tilt;
        int checksum = 0, i, idx = 0;
        byte[] packet = new byte[16];

        pan = (int) (0.0 * 100.0);
        tilt = (int) (90.0 * 100.0);

        // header
        packet[idx++] = (byte) 0x00;
        packet[idx++] = (byte) 0xFF;
        packet[idx++] = (byte) 0x5A;
        packet[idx++] = (byte) 0x0A;	// size
        packet[idx++] = (byte) 0x00;	// group
        packet[idx++] = (byte) 0x80;	// packet type

        // data
        packet[idx++] = (byte) (pan >> 8 & 0x00FF);
        packet[idx++] = (byte) (pan & 0x00FF);
        packet[idx++] = (byte) (tilt >> 8 & 0x00FF);
        packet[idx++] = (byte) (tilt & 0x00FF);
        packet[idx++] = (byte) 0x00;
        packet[idx++] = (byte) 0x00;
        packet[idx++] = (byte) 0x20;
        packet[idx++] = (byte) 0x00;

        // checksum
        for (i = 0; i < idx; i++)
        {
            checksum += (int) (packet[i]) & 0x00FF;
        }
        packet[idx++] = (byte) (checksum >> 8 & 0x00FF);
        packet[idx++] = (byte) (checksum & 0x00FF);

        try
        {
            _serialPort.sendBytes(packet);
        }
        catch (Exception ex)
        {
            System.out.println("TASE SPOI SEND ERROR:");
            ex.printStackTrace();
        }
    }

    public void sendTASE_PointForward()
    {
        sendTASE_PointPanTilt(0, 0);
    }

    public void sendTASE_PointPanTilt(double panDegrees, double tiltDegrees)
    {
        if (_serialPort == null)
            return;
        
        int pan, tilt;
        int checksum = 0, i, idx = 0;
        byte[] packet = new byte[16];

        pan = (int) (panDegrees * 100.0);
        tilt = (int) (tiltDegrees * 100.0);

        // header
        packet[idx++] = (byte) 0x00;
        packet[idx++] = (byte) 0xFF;
        packet[idx++] = (byte) 0x5A;
        packet[idx++] = (byte) 0x0A;	// size
        packet[idx++] = (byte) 0x00;	// group
        packet[idx++] = (byte) 0x80;	// packet type

        // data
        packet[idx++] = (byte) (pan >> 8 & 0x00FF);
        packet[idx++] = (byte) (pan & 0x00FF);
        packet[idx++] = (byte) (tilt >> 8 & 0x00FF);
        packet[idx++] = (byte) (tilt & 0x00FF);
        packet[idx++] = (byte) 0x00;
        packet[idx++] = (byte) 0x00;
        packet[idx++] = (byte) 0x20;
        packet[idx++] = (byte) 0x00;

        // checksum
        for (i = 0; i < idx; i++)
        {
            checksum += (int) (packet[i]) & 0x00FF;
        }
        packet[idx++] = (byte) (checksum >> 8 & 0x00FF);
        packet[idx++] = (byte) (checksum & 0x00FF);

        try
        {
            _serialPort.sendBytes(packet);
        }
        catch (Exception ex)
        {
            System.out.println("TASE SPOI SEND ERROR:");
            ex.printStackTrace();
        }
    }

    public void sendTASE_RetractDeploy(boolean deploy)
    {
        int checksum = 0, i, idx = 0;
        byte[] packet = new byte[27];

        // header
        packet[idx++] = (byte) 0x00;
        packet[idx++] = (byte) 0xFF;
        packet[idx++] = (byte) 0x5A;
        packet[idx++] = (byte) 0x04;	// size
        packet[idx++] = (byte) 0x00;	// group
        packet[idx++] = (byte) 0x43;	// packet type

        // data
        packet[idx++] = (byte) (deploy ? 0x01 : 0x00);
        packet[idx++] = (byte) 0x00;
        packet[idx++] = 0x00;

        // checksum
        for (i = 0; i < idx; i++)
        {
            checksum += (int) (packet[i]) & 0x00FF;
        }
        packet[idx++] = (byte) (checksum >> 8 & 0x00FF);
        packet[idx++] = (byte) (checksum & 0x00FF);

        try
        {
            _serialPort.sendBytes(packet);
        }
        catch (Exception ex)
        {
            System.out.println("TASE SPOI SEND ERROR:");
            ex.printStackTrace();
        }

    }

    public void sendVPS_DisableTracking()
    {
        if (_serialPort != null)
        {
            int checksum = 0, i, idx = 0;
            byte[] packet = new byte[16];

            //
            // Send track command (disable)
            //

            // header
            packet[idx++] = (byte) 0x00;
            packet[idx++] = (byte) 0xFF;
            packet[idx++] = (byte) 0x5A;
            packet[idx++] = (byte) 0x09;	// size
            packet[idx++] = (byte) 0x00;	// group
            packet[idx++] = (byte) 0x50;	// packet type

            // data
            packet[idx++] = (byte) 0; //Initial Track X
            packet[idx++] = (byte) 0;
            packet[idx++] = (byte) 0; //Initial Track Y
            packet[idx++] = (byte) 0;
            packet[idx++] = (byte) 0; //Disengage tracking
            packet[idx++] = (byte) 100; //Track window size
            packet[idx++] = (byte) 255; //Video Stamp

            // checksum
            for (i = 0; i < idx; i++)
            {
                checksum += (int) (packet[i]) & 0x00FF;
            }
            packet[idx++] = (byte) (checksum >> 8 & 0x00FF);
            packet[idx++] = (byte) (checksum & 0x00FF);

            try
            {
                _serialPort.sendBytes(packet, 15);
            }
            catch (Exception ex)
            {
                System.out.println("TASE DISABLE VPS TRACKING SEND ERROR:");
                ex.printStackTrace();
            }
        }
    }

    public void sendVPS_DisableSymbology()
    {
        if (_serialPort != null)
        {
            int checksum = 0, i, idx = 0;
            byte[] packet = new byte[16];

            //
            // Send symbology command (disable)
            //

            // header
            packet[idx++] = (byte) 0x00;
            packet[idx++] = (byte) 0xFF;
            packet[idx++] = (byte) 0x5A;
            packet[idx++] = (byte) 0x04;	// size
            packet[idx++] = (byte) 0x00;	// group
            packet[idx++] = (byte) 0x51;	// packet type

            // data
            packet[idx++] = (byte) 0x00; //Turn all symbology off
            packet[idx++] = (byte) 0x00;

            // checksum
            for (i = 0; i < idx; i++)
            {
                checksum += (int) (packet[i]) & 0x00FF;
            }
            packet[idx++] = (byte) (checksum >> 8 & 0x00FF);
            packet[idx++] = (byte) (checksum & 0x00FF);

            try
            {
                _serialPort.sendBytes(packet, 10);
            }
            catch (Exception ex)
            {
                System.out.println("TASE DISABLE VPS TRACKING SEND ERROR:");
                ex.printStackTrace();
            }
        }
    }
}
