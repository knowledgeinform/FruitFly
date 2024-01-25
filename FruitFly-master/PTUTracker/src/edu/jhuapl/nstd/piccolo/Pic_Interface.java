package edu.jhuapl.nstd.piccolo;

import java.io.*;
import java.lang.*;
import java.net.*;
import java.text.*;
import java.util.*;

import edu.jhuapl.nstd.util.*;

public class Pic_Interface implements Runnable {

    private static int LOITER_WAYPOINT_INDEX = 88;
    private static int INTERCEPT_WAYPOINT_INDEX = 89;
    private boolean _usingTCP,  _connected;
    private String _serialPort,  _ipAddr;
    private int _serialBaud,  _ipPort;
    private XCommSerialPort _xcommPort;
    private Socket _oiSocket;
    private boolean _isRunning = false;
    private boolean _shutdown = false;
    private int _autopilotNum;
    private List<Pic_TelemetryListener> _telemListeners = new ArrayList<Pic_TelemetryListener>();

    // local state-machine variables for parsing Picollo stream
    General_Parse_State _piccoloState;
    byte[] _generalHeader, _piccoloParseData;
    int _parseIdx, _parseBytesToRead;
    int _parseDataSize;
    int _parseCRC, _receivedCRC;

    // state-machine variables for Autopilot streams
    AP_Parse_State _apState;
    int _apParseIdx;
    int _apParseCRC, _apReceivedCRC;
    byte[] _apPacket;

    public Pic_Interface(int apNum) {
        //this(apNum, "COM1", 9600, false);
        this(apNum, "127.0.0.1", 2000, true);
    }

    public Pic_Interface(int apNum, String port_or_ipAddr, int baud_or_port, boolean usingTCP) {
        _autopilotNum = apNum;

        _usingTCP = usingTCP;
        if (usingTCP) {
            _ipAddr = port_or_ipAddr;
            _ipPort = baud_or_port;
        } else {
            _serialPort = port_or_ipAddr;
            _serialBaud = baud_or_port;
        }

        _isRunning = false;
        _connected = false;
        _piccoloState = General_Parse_State.HEADER1;
        _apState = AP_Parse_State.SYNC1;
        _generalHeader = new byte[14];
        _piccoloParseData = new byte[1024];
        _apPacket = new byte[512];
    }

    public boolean isRunning() {
        return _isRunning;
    }

    public void forceShutdown() {
        _shutdown = true;
    }

    public void addPicTelemetryListener(Pic_TelemetryListener obj) {
        _telemListeners.add(obj);
    }

    public void removePicTelemetryListener(Pic_TelemetryListener obj) {
        _telemListeners.remove(obj);
    }

    private boolean connect() {

        try {

            if (_usingTCP) {
                System.out.println("**************************Connecting to IP " + _ipAddr + " at port " + _ipPort);
                _oiSocket = new Socket(_ipAddr, _ipPort);
                _connected = true;
            } else {
                System.out.println("Connecting to serial port " + _serialPort + " at baud " + _serialBaud);
                _xcommPort = new XCommSerialPort(_serialPort, _serialBaud);
                _connected = true;
            }

        } catch (Exception ex) {
            System.out.println("Unable to connect, using TCP = " + _usingTCP);
            _connected = false;
        }
        return _connected;
    }

    @Override
    public void run() {
        byte[] bytesRead = null;
        int i;
        InputStream tcpIn = null;
        int numBytesRead = 0;
        try {
            _isRunning = true;
            while (!_shutdown) {

                // first we connect
                if (!_connected && !connect()) {
                    Thread.sleep(1000);
                    continue;
                }

                if (_usingTCP) {
                    tcpIn = _oiSocket.getInputStream();
                    bytesRead = new byte[1024];
                }

                while (!_shutdown) {

                    try {

                        if (_usingTCP) {
                            numBytesRead = tcpIn.read(bytesRead);
                        } else {
                            bytesRead = _xcommPort.readAvailableBytes();
                            numBytesRead = bytesRead == null ? 0 : bytesRead.length;
                        }

                    } catch (Exception ex) {
                        System.out.println("ERROR reading...");
                        _connected = false;
                        break;
                    }

                    if (bytesRead != null) {
                        for (i = 0; i < numBytesRead; i++) {
                            parseTopPiccoloByte(bytesRead[i]);
                        }
                    }
                }

            }

            System.out.println("PiccoloAP Shutting down...");
            _isRunning = false;
        } catch (Exception e) {
            e.printStackTrace();
            _isRunning = false;
        }

    }

    private enum AP_Parse_State {

        SYNC1, /* 0xA0 */
        SYNC2, /* 0x05 */
        PACKET_TYPE, /* type of packet (different meaning for AutoPilot and UAV) */
        SIZE, /* size of data in this packet */
        READING_PAYLOAD, /* Data bytes of payload */
        AP_CRC_Hi, /* Error-checking CRC */
        AP_CRC_Lo
    }

    private enum General_Parse_State {

        HEADER1, /* 0x5A */
        HEADER2, /* 0xA5 */
        READING_GENERAL_STREAM_HEADER, /* reading 10 bytes of header */
        HEADER_CHECK, /* XOR checksum of first 12 bytes of message */
        READING_DATA, /* Data bytes of stream packet */
        GS_CRC_Hi, /* Error-checking CRC */
        GS_CRC_Lo
    }

    private void parseTopPiccoloByte(byte newByte) {
        int checksum;
        int i, sourceID;
        boolean quit = false;

        switch (_piccoloState) {

            case HEADER1:
                if (newByte == (byte) 0x5A) {
                    _piccoloState = General_Parse_State.HEADER2;
                    _parseIdx = 0;
                    _generalHeader[_parseIdx++] = newByte;
                    _parseCRC = CRC16OneByte(newByte, 0);
                }
                break;

            case HEADER2:
                if (newByte == (byte) 0xA5) {
                    _piccoloState = General_Parse_State.READING_GENERAL_STREAM_HEADER;
                    _generalHeader[_parseIdx++] = newByte;
                    _parseBytesToRead = 13;	/* read 13 total header bytes */
                    _parseCRC = CRC16OneByte(newByte, _parseCRC);
                } else if (newByte == (byte) 0x5A) {
                    /* check for duplicate 0x5A bytes */
                    _piccoloState = General_Parse_State.HEADER2;
                    _parseIdx = 0;
                    _generalHeader[_parseIdx++] = newByte;
                    _parseCRC = CRC16OneByte(newByte, 0);
                } else {
                    _piccoloState = General_Parse_State.HEADER1;
                }
                break;

            case READING_GENERAL_STREAM_HEADER:
                _generalHeader[_parseIdx++] = newByte;
                _parseCRC = CRC16OneByte(newByte, _parseCRC);
                if (_parseIdx == _parseBytesToRead) {
                    /* done reading bytes in header */
                    _piccoloState = General_Parse_State.HEADER_CHECK;
                }
                break;

            case HEADER_CHECK:
                checksum = 0;
                for (i = 0; i < 13; i++) {
                    checksum ^= _generalHeader[i];
                }
                if (checksum != newByte) {
                    System.out.println("PiccoloParser: TOP LEVEL HEADER ERROR, " + checksum + " != " + newByte);
                    _piccoloState = General_Parse_State.HEADER1;
                } else {
                    _generalHeader[_parseIdx++] = newByte;
                    _parseCRC = CRC16OneByte(newByte, _parseCRC);
                    _piccoloState = General_Parse_State.READING_DATA;
                    _parseIdx = 0;
                    _parseDataSize = (int) (_generalHeader[12]) & 0x00FF;	/* pick out size */
                    _parseBytesToRead = _parseDataSize;
                    if (_parseIdx == _parseBytesToRead) {
                        /* done reading data bytes */
                        _piccoloState = General_Parse_State.GS_CRC_Hi;
                    }
                }
                break;

            case READING_DATA:
                _piccoloParseData[_parseIdx++] = newByte;
                _parseCRC = CRC16OneByte(newByte, _parseCRC);
                if (_parseIdx == _parseBytesToRead) {
                    /* done reading data bytes */
                    _piccoloState = General_Parse_State.GS_CRC_Hi;
                }
                break;

            case GS_CRC_Hi:
                _receivedCRC = ((int) newByte << 8) & 0x0000FF00;
                _piccoloState = General_Parse_State.GS_CRC_Lo;
                break;

            case GS_CRC_Lo:
                _receivedCRC |= ((int) newByte & 0x00FF);
                /* now we check the CRC */
                if (_parseCRC != _receivedCRC) {
                    System.out.println("PiccoloParser: Top Level CRC ERROR\n");
                } else {
                    sourceID = (((_generalHeader[4] & 0x00FF) << 8) | (_generalHeader[5] & 0x00FF));

                    if (sourceID != 0 && _generalHeader[10] == (byte) 3) {
                        for (i = 0; i < _parseDataSize; i++) {
                            parseAutopilotByte(_piccoloParseData[i]);
                        }
                    }
                }
                _piccoloState = General_Parse_State.HEADER1;
                break;

            default:
                System.out.println("Whoa mama, what state am I in? (" + _piccoloState + ")");
                _piccoloState = General_Parse_State.HEADER1;
                break;
        }

    }

    private void parseAutopilotByte(byte newByte) {

        switch (_apState) {
            case SYNC1:
                if (newByte == (byte) 0xA0) {
                    _apState = AP_Parse_State.SYNC2;
                    _apParseIdx = 0;
                    _apPacket[_apParseIdx++] = newByte;
                    _apParseCRC = CRC16OneByte(newByte, 0);
                }
                break;

            case SYNC2:
                if (newByte == (byte) 0x05) {
                    _apState = AP_Parse_State.PACKET_TYPE;
                    _apPacket[_apParseIdx++] = newByte;
                    _apParseCRC = CRC16OneByte(newByte, _apParseCRC);
                } else if (newByte == (byte) 0xA0) {
                    /* check for duplicate 0xA0 bytes */
                    _apState = AP_Parse_State.SYNC2;
                    _apParseIdx = 0;
                    _apPacket[_apParseIdx++] = newByte;
                    _apParseCRC = CRC16OneByte(newByte, 0);
                } else {
                    _apState = AP_Parse_State.SYNC1;
                }
                break;

            case PACKET_TYPE:
            case SIZE:
                _apPacket[_apParseIdx++] = newByte;
                _apParseCRC = CRC16OneByte(newByte, _apParseCRC);
                _apState = (_apState == AP_Parse_State.PACKET_TYPE ? AP_Parse_State.SIZE : AP_Parse_State.READING_PAYLOAD);

                // added logic for zero-length packets
                if (_apState == AP_Parse_State.READING_PAYLOAD) {
                    if (_apPacket[3] + 4 == _apParseIdx) {
                        _apState = AP_Parse_State.AP_CRC_Hi;
                    } else if (_apPacket[3] + 4 < _apParseIdx) {
                        System.out.println("Strange, read in more UAV bytes than size");
                        _apState = AP_Parse_State.SYNC1;
                    }
                }
                break;

            case READING_PAYLOAD:
                _apPacket[_apParseIdx++] = newByte;
                _apParseCRC = CRC16OneByte(newByte, _apParseCRC);
                if (_apPacket[3] + 4 == _apParseIdx) {
                    _apState = AP_Parse_State.AP_CRC_Hi;
                } // quick check for errors in parsing, to make sure of no buffer overflow
                else if (_apPacket[3] + 4 < _apParseIdx) {
                    System.out.println("Strange, read in more UAV bytes than size");
                    _apState = AP_Parse_State.SYNC1;
                }
                break;

            case AP_CRC_Hi:
                _apReceivedCRC = ((int) newByte << 8) & 0x0000FF00;
                _apState = AP_Parse_State.AP_CRC_Lo;
                break;

            case AP_CRC_Lo:
                _apReceivedCRC |= ((int) newByte & 0x00FF);
                /* now we check the CRC */
                if (_apParseCRC != _apReceivedCRC) {
                    System.out.println("PiccoloParser: AP UAV STREAM CRC ERROR\n");
                } else {
                    // GOOD PACKET, pass on to appropriate parsing
                    parseUavTelemetry();
                }
                _apState = AP_Parse_State.SYNC1;
                break;

            default:
                System.out.println("Whoa mama, what ap UAV state am I in? (" + _apState + ")");
                _apState = AP_Parse_State.SYNC1;
                break;
        }

    }

    private void parseUavTelemetry() {
        short roll, pitch, wwest, wsouth;
        int yaw, comp, airsp; // unsigned
        short vn, ve, vd;
        int lat, lon, alt, temp;
        Pic_Telemetry telem = new Pic_Telemetry();

        if (_apPacket[2] == (byte) 69) {	/* hi res telemetry packet */

            /* ok, now we are ready to grab the lat/lon from the packet */
            roll = (short) (((_apPacket[40] & 0x00FF) << 8) + (_apPacket[41] & 0x00FF));
            pitch = (short) (((_apPacket[42] & 0x00FF) << 8) + (_apPacket[43] & 0x00FF));
            yaw = ((_apPacket[44] & 0x00FF) << 8) + (_apPacket[45] & 0x00FF);
            vn = (short) (((_apPacket[26] & 0x00FF) << 8) + (_apPacket[27] & 0x00FF));
            ve = (short) (((_apPacket[28] & 0x00FF) << 8) + (_apPacket[29] & 0x00FF));
            vd = (short) (((_apPacket[30] & 0x00FF) << 8) + (_apPacket[31] & 0x00FF));
            comp = ((_apPacket[80] & 0x00FF) << 8) + (_apPacket[81] & 0x00FF);
            lat = ((_apPacket[12] & 0x00FF) << 24) +
                    ((_apPacket[13] & 0x00FF) << 16) +
                    ((_apPacket[14] & 0x00FF) << 8) +
                    (_apPacket[15] & 0x00FF);
            lon = ((_apPacket[16] & 0x00FF) << 24) +
                    ((_apPacket[17] & 0x00FF) << 16) +
                    ((_apPacket[18] & 0x00FF) << 8) +
                    (_apPacket[19] & 0x00FF);
            alt = ((_apPacket[20] & 0x00FF) << 16) +
                    ((_apPacket[21] & 0x00FF) << 8) +
                    (_apPacket[22] & 0x00FF);
            airsp = ((_apPacket[58] & 0x00FF) << 8) + (_apPacket[59] & 0x00FF);
            wsouth = (short) (((_apPacket[48] & 0x00FF) << 8) + (_apPacket[49] & 0x00FF));
            wwest = (short) (((_apPacket[50] & 0x00FF) << 8) + (_apPacket[51] & 0x00FF));

            telem.VelNorth = (float) vn / 100.0;
            telem.VelEast = (float) ve / 100.0;
            telem.VelDown = (float) vd / 100.0;
            telem.TrueHeading = ((float) comp / 10000.0f) * (180.0f / Math.PI);
            telem.Lat = (double) lat / 1000.0 / 3600.0;
            telem.Lon = (double) lon / 1000.0 / 3600.0;
            telem.AltEllip = ((double) alt / 100.0) - 1000.0;
            telem.WindSouth = (double) wsouth / 100.0;
            telem.WindWest = (double) wwest / 100.0;
            telem.Roll = (float) roll / 10000.0 * (180.0 / Math.PI);
            telem.Pitch = (float) pitch / 10000.0 * (180.0 / Math.PI);
            telem.Yaw = (float) yaw / 10000.0 * (180.0 / Math.PI);
            telem.IndAirSpeed = ((double) airsp - 2000.0) / 100.0;

            temp = ((int) _apPacket[32] & 0x00FF);
            telem.GPS_Status = temp >> 5;

            temp = ((int) _apPacket[24] & 0x00FF) >> 2;
            telem.PDOP = (double) temp * 0.2;

            for (Pic_TelemetryListener listen : _telemListeners) {
                listen.handlePic_Telemetry(telem);
            }
        }

        if (_apPacket[2] == (byte) 70) {	/* lo res telemetry packet */

            /* ok, now we are ready to grab the lat/lon from the packet */
            lat = ((_apPacket[11] & 0x00FF) << 16) +
                    ((_apPacket[12] & 0x00FF) << 8) +
                    (_apPacket[13] & 0x00FF);
            lon = ((_apPacket[14] & 0x00FF) << 16) +
                    ((_apPacket[15] & 0x00FF) << 8) +
                    (_apPacket[16] & 0x00FF);
            alt = ((_apPacket[17] & 0x00FF) << 8) +
                    (_apPacket[18] & 0x00FF);
            roll = (short) _apPacket[32];
            pitch = (short) _apPacket[33];
            yaw = (short) (_apPacket[34] & 0x00FF);
            vn = (short) _apPacket[21];
            ve = (short) _apPacket[22];
            vd = (short) _apPacket[23];
            comp = (_apPacket[54] & 0x00FF);
            airsp = (_apPacket[42] & 0x00FF);
            wsouth = (short) _apPacket[36];
            wwest = (short) _apPacket[37];

            telem.Lat = ((double) lat / (16777216.0 / (2.0 * Math.PI)) - Math.PI) * (180.0 / Math.PI);
            telem.Lon = ((double) lon / (16777216.0 / (2.0 * Math.PI)) - Math.PI) * (180.0 / Math.PI);
            telem.AltEllip = ((double) alt * 2.0) - 1000.0;
            telem.Roll = (double) roll * (360.0 / 256.0);
            telem.Pitch = (double) pitch * (180.0 / 256.0);
            telem.Yaw = (double) yaw * (360.0 / 256.0);
            telem.VelNorth = vn;
            telem.VelEast = ve;
            telem.VelDown = vd;
            telem.TrueHeading = (double) comp * (360.0 / 256.0);
            telem.WindSouth = (double) wsouth * 2.0;
            telem.WindWest = (double) wwest * 2.0;
            telem.IndAirSpeed = (double) airsp - 20.0;

            temp = ((int) _apPacket[24] & 0x00FF);
            telem.GPS_Status = temp >> 5;

            temp = ((int) _apPacket[19] & 0x00FF) >> 2;
            telem.PDOP = (double) temp * 0.2;

            for (Pic_TelemetryListener listen : _telemListeners) {
                listen.handlePic_Telemetry(telem);
            }
        }
    }

    public void sendFastDataRate() {
        byte[] cmd = new byte[26];
        int crc;
        int i;

        cmd[0] = (byte) 0x5A;
        cmd[1] = (byte) 0xA5;

        cmd[2] = (byte) ((_autopilotNum >> 8) & 0x00FF);
        cmd[3] = (byte) (_autopilotNum & 0x00FF);


        cmd[4] = (byte) 0xFF;
        cmd[5] = (byte) 0xFE;

        cmd[6] = 0x00; // total length of packet
        cmd[7] = 0x1A;

        cmd[8] = 0x00;
        cmd[9] = 0x00;
        cmd[10] = 0x03;
        cmd[11] = 0x00;
        cmd[12] = 0x0A;	// length of sub-packet

        cmd[13] = 0x00;	// x-or of header
        for (i = 0; i < 13; i++) {
            cmd[13] ^= cmd[i];
        }

/////////////////////////
        //cmd[14] = 0xA0;
        cmd[15] = 0x05;
        cmd[16] = 0x02;	// BANDWIDTH packet type
        cmd[17] = 0x04;	// length

        //cmd[18] = 0xFF;	// current link
        cmd[19] = 0x00;	// high res telem, fast data rate
        cmd[20] = 0x7F;	// slow down nav
        cmd[21] = 0x7F;	// slow down control

        crc = 0;
        for (i = 14; i <= 21; i++) {
            crc = CRC16OneByte(cmd[i], crc);
        }
        cmd[22] = (byte) ((crc >> 8) & 0x00FF);	// CRC (high)
        cmd[23] = (byte) (crc & 0x00FF);	// CRC (low)
////////////////////////////

        crc = CRC16(cmd, 24);
        cmd[24] = (byte) ((crc >> 8) & 0x00FF);	// CRC
        cmd[25] = (byte) (crc & 0x00FF);	// CRC

        try {
            if (_usingTCP) {
                _oiSocket.getOutputStream().write(cmd);
            } else {
                _xcommPort.sendBytes(cmd);
            }
        } catch (Exception ex) {
            _connected = false;
            System.out.println("PICCOLO FAST DATA RATE SEND ERROR:");
            ex.printStackTrace();
        }
    }


    // radius MUST be less than (or equal to) 2550 meters
    private void changeWaypoint(int waypoint, double lat, double lon, double altMSL, double radiusM, boolean rightTurn) {
        if (radiusM > 2550.0) {
            System.out.println("WARNING: radius exceeds max, reducing to 2550.0m");
            radiusM = 2550.0;
        }

        byte[] cmd = new byte[40];
        int crc;
        int i, masLat, masLon, masAlt;

        cmd[0] = 0x5A;
        cmd[1] = (byte) 0xA5;

        cmd[2] = (byte) ((_autopilotNum >> 8) & 0x00FF);
        cmd[3] = (byte) (_autopilotNum & 0x00FF);

        cmd[4] = (byte) 0xFF;
        cmd[5] = (byte) 0xFE;

        cmd[6] = 0x00; // sequence num
        cmd[7] = 0x1A;

        cmd[8] = 0x00; // ack num
        cmd[9] = 0x00;

        cmd[10] = 0x03;	 // stream
        cmd[11] = 0x00;	 // flags

        cmd[12] = 0x18;	// length of sub-packet (24 bytes)

        cmd[13] = 0x00;	// x-or of header
        for (i = 0; i < 13; i++) {
            cmd[13] ^= cmd[i];
        }

/////////////////////////
        cmd[14] = (byte) 0xA0;
        cmd[15] = 0x05;
        cmd[16] = 0x08;	// WAYPOINT packet type
        cmd[17] = 0x12;	// length (18bytes)

        masLat = (int) (lat * 3600.0 * 1000.0);
        cmd[18] = (byte) ((masLat >> 24) & 0x00FF);
        cmd[19] = (byte) ((masLat >> 16) & 0x00FF);
        cmd[20] = (byte) ((masLat >> 8) & 0x00FF);
        cmd[21] = (byte) (masLat & 0x00FF);
        masLon = (int) (lon * 3600.0 * 1000.0);
        cmd[22] = (byte) ((masLon >> 24) & 0x00FF);
        cmd[23] = (byte) ((masLon >> 16) & 0x00FF);
        cmd[24] = (byte) ((masLon >> 8) & 0x00FF);
        cmd[25] = (byte) (masLon & 0x00FF);

        cmd[26] = (byte) (rightTurn ? 0x21 : 0x01);
        cmd[27] = (byte) (radiusM / 10.0);

        masAlt = (int) altMSL;

        cmd[28] = (byte) ((masAlt >> 8) & 0x00FF);
        cmd[29] = (byte) (masAlt & 0x00FF);
        cmd[30] = 0x00;
        cmd[31] = 0x00;
        cmd[32] = (byte) (waypoint);
        cmd[33] = (byte) (waypoint - 1);
        cmd[34] = 0x00;
        cmd[35] = (byte) 0xC0;

        crc = 0;
        for (i = 14; i <= 35; i++) {
            crc = CRC16OneByte(cmd[i], crc);
        }
        cmd[36] = (byte) ((crc >> 8) & 0x00FF);	// CRC (high)
        cmd[37] = (byte) (crc & 0x00FF);	// CRC (low)
////////////////////////////

        crc = CRC16(cmd, 38);
        cmd[38] = (byte) ((crc >> 8) & 0x00FF);	// CRC
        cmd[39] = (byte) (crc & 0x00FF);	// CRC

        //// Cmd 2
        byte[] cmd1 = new byte[40];

        cmd1[0] = 0x5A;
        cmd1[1] = (byte) 0xA5;

        cmd1[2] = (byte) ((_autopilotNum >> 8) & 0x00FF);
        cmd1[3] = (byte) (_autopilotNum & 0x00FF);

        cmd1[4] = (byte) 0xFF;
        cmd1[5] = (byte) 0xFE;

        cmd1[6] = 0x00; // sequence num
        cmd1[7] = 0x1A;

        cmd1[8] = 0x00; // ack num
        cmd1[9] = 0x00;

        cmd1[10] = 0x03;	 // stream
        cmd1[11] = 0x00;	 // flags

        cmd1[12] = 0x18;	// length of sub-packet (24 bytes)

        cmd1[13] = 0x00;	// x-or of header
        for (i = 0; i < 13; i++) {
            cmd1[13] ^= cmd1[i];
        }

/////////////////////////
        cmd1[14] = (byte) 0xA0;
        cmd1[15] = 0x05;
        cmd1[16] = 0x08;	// WAYPOINT packet type
        cmd1[17] = 0x12;	// length (18bytes)

        masLat = (int) (lat * 3600.0 * 1000.0);
        cmd1[18] = (byte) ((masLat >> 24) & 0x00FF);
        cmd1[19] = (byte) ((masLat >> 16) & 0x00FF);
        cmd1[20] = (byte) ((masLat >> 8) & 0x00FF);
        cmd1[21] = (byte) (masLat & 0x00FF);
        masLon = (int) (lon * 3600.0 * 1000.0);
        cmd1[22] = (byte) ((masLon >> 24) & 0x00FF);
        cmd1[23] = (byte) ((masLon >> 16) & 0x00FF);
        cmd1[24] = (byte) ((masLon >> 8) & 0x00FF);
        cmd1[25] = (byte) (masLon & 0x00FF);

        cmd1[26] = (byte) (rightTurn ? 0x21 : 0x01);
        cmd1[27] = (byte) (radiusM / 10.0);

        masAlt = (int) altMSL;

        cmd1[28] = (byte) ((masAlt >> 8) & 0x00FF);
        cmd1[29] = (byte) (masAlt & 0x00FF);
        cmd1[30] = 0x00;
        cmd1[31] = 0x00;
        cmd1[32] = (byte) (waypoint - 1);
        cmd1[33] = (byte) (waypoint);
        cmd1[34] = 0x00;
        cmd1[35] = (byte) 0xC0;

        crc = 0;
        for (i = 14; i <= 35; i++) {
            crc = CRC16OneByte(cmd1[i], crc);
        }
        cmd1[36] = (byte) ((crc >> 8) & 0x00FF);	// CRC (high)
        cmd1[37] = (byte) (crc & 0x00FF);	// CRC (low)
////////////////////////////

        crc = CRC16(cmd1, 38);
        cmd1[38] = (byte) ((crc >> 8) & 0x00FF);	// CRC
        cmd1[39] = (byte) (crc & 0x00FF);	// CRC


        try {
            if (_usingTCP) {
                _oiSocket.getOutputStream().write(cmd);
                _oiSocket.getOutputStream().write(cmd1);
            } else {
                _xcommPort.sendBytes(cmd);
                _xcommPort.sendBytes(cmd1);
            }
        } catch (Exception ex) {
            _connected = false;
            System.out.println("PICCOLO WAYPOINT SEND ERROR:");
            ex.printStackTrace();
        }
    }

    // radius MUST be less than (or equal to) 2550 meters
    public void changeInterceptWaypoint(double lat, double lon, double altMSL, double radiusM, boolean rightTurn) {
        changeWaypoint(INTERCEPT_WAYPOINT_INDEX, lat, lon, altMSL, radiusM, rightTurn);
    }

    // radius MUST be less than (or equal to) 2550 meters
    public void changeLoiterWaypoint(double lat, double lon, double altMSL, double radiusM, boolean rightTurn) {
        changeWaypoint(LOITER_WAYPOINT_INDEX, lat, lon, altMSL, radiusM, rightTurn);
    }

    private void sendToWaypoint(int waypoint) {
        byte[] cmd = new byte[24];
        int crc;
        int i, maslat, maslon;

        cmd[0] = 0x5A;
        cmd[1] = (byte) 0xA5;

        cmd[2] = (byte) ((_autopilotNum >> 8) & 0x00FF);
        cmd[3] = (byte) (_autopilotNum & 0x00FF);

        cmd[4] = (byte) 0xFF;
        cmd[5] = (byte) 0xFE;

        cmd[6] = 0x00; // sequence num
        cmd[7] = 0x1A;

        cmd[8] = 0x00; // ack num
        cmd[9] = 0x00;

        cmd[10] = 0x03;	 // stream
        cmd[11] = 0x00;	 // flags

        cmd[12] = 0x08;	// length of sub-packet (8 bytes)

        cmd[13] = 0x00;	// x-or of header
        for (i = 0; i < 13; i++) {
            cmd[13] ^= cmd[i];
        }

/////////////////////////
        cmd[14] = (byte) 0xA0;
        cmd[15] = 0x05;
        cmd[16] = 0x0A;	// TRACK packet type
        cmd[17] = 0x02;	// length (2 bytes)

        cmd[18] = (byte) waypoint;
        cmd[19] = 0x01;

        crc = 0;
        for (i = 14; i <= 19; i++) {
            crc = CRC16OneByte(cmd[i], crc);
        }
        cmd[20] = (byte) ((crc >> 8) & 0x00FF);	// CRC (high)
        cmd[21] = (byte) (crc & 0x00FF);	// CRC (low)
////////////////////////////

        crc = CRC16(cmd, 22);
        cmd[22] = (byte) ((crc >> 8) & 0x00FF);	// CRC
        cmd[23] = (byte) (crc & 0x00FF);	// CRC

        try {
            if (_usingTCP) {
                _oiSocket.getOutputStream().write(cmd);
            } else {
                _xcommPort.sendBytes(cmd);
            }
        } catch (Exception ex) {
            _connected = false;
            System.out.println("PICCOLO TRACK SEND ERROR:");
            ex.printStackTrace();
        }
    }

    public void sendToInterceptWaypoint() {
        sendToWaypoint(INTERCEPT_WAYPOINT_INDEX);
    }

    public void sendToLoiterWaypoint() {
        sendToWaypoint(LOITER_WAYPOINT_INDEX);
    }

    /*! Calculates a 16-bit cyclic redundancy check on blocks of 8-bit data of
    arbitrary length.
    \param pBuf points to a buffer of data.
    \param len is the size of the buffer in bytes.
    \return The 16-bit crc of the buffer.*/
    private int CRC16(byte[] pBuf, int len) {
        int crc = 0;
        int i;

        for (i = 0; i < len; i++) {
            crc = (int) (_crctable[(pBuf[i] ^ crc) & 0xff] ^ (crc >> 8));
        }

        return crc;
    }


    /*! Calculate the new 16-bit cyclic redundancy check that results from adding
    a byte to an existing crc.
    \param Byte is the new byte.
    \param crc is the existing crc value.
    \return The new crc value*/
    private int CRC16OneByte(byte Byte, int crc) {
        return (int) (_crctable[(Byte ^ crc) & 0xff] ^ (crc >> 8));
    }
    private static int[] _crctable = {
        0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
        0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
        0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
        0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
        0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
        0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
        0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
        0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
        0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
        0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
        0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
        0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
        0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
        0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
        0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
        0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
        0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
        0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
        0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
        0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
        0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
        0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
        0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
        0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
        0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
        0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
        0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
        0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
        0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
        0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
        0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
        0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040
    };
}


