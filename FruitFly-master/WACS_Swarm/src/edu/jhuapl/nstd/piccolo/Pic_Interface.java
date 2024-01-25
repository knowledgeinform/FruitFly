
package edu.jhuapl.nstd.piccolo;

import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.nstd.swarm.util.MathUtils;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.swarm.util.Config;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;

import edu.jhuapl.nstd.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;



public class Pic_Interface implements Runnable {
	private static int LOITER_WAYPOINT_INDEX = 88;
	private static int INTERCEPT_WAYPOINT_INDEX = 89;
        
	private boolean _usingTCP, _connected;
        private boolean _readFromLog;
	private String _serialPort, _ipAddr, _filename;
	private int _serialBaud, _ipPort;
	private XCommSerialPort _xcommPort;
	private Socket _oiSocket;
        private BufferedReader _fileStream;

	private boolean _isRunning = false;
	private boolean _shutdown = false;
	private int _autopilotNum;

	private List<Pic_TelemetryListener> _telemListeners = new ArrayList<Pic_TelemetryListener>();
        private List<Pic_RawStreamListener> _streamListeners = new ArrayList<Pic_RawStreamListener>();

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
        static public boolean showPicDisplay;
        private boolean m_LastAglNavFilterGood = true;
        private double m_MaximumAltitudeLeadingCommand_m;
        private double m_LastAltMSL_m = -1;



	public Pic_Interface(int apNum)
	{
	    //this(apNum, "COM1", 9600, false);
		this(apNum, "COM5", 9600, true);
	}

        public Pic_Interface(int apNum, String port_or_ipAddr, int baud_or_port, boolean usingTCP)
	{
            this(apNum, port_or_ipAddr, baud_or_port, usingTCP, false);
        }

	public Pic_Interface(int apNum, String port_or_ipAddr_or_filename, int baud_or_port, boolean usingTCP, boolean readFromFile)
	{
            showPicDisplay = Config.getConfig().getPropertyAsBoolean("PicInterface.ShowPicDisplay", false);
            m_MaximumAltitudeLeadingCommand_m = Config.getConfig().getPropertyAsDouble("PicInterface.MaxAltLeadingCommand.Meters", 100);
            
		_autopilotNum = apNum;

		_usingTCP = usingTCP;
                _readFromLog = readFromFile;
		if (usingTCP) {
			_ipAddr = port_or_ipAddr_or_filename;
			_ipPort = baud_or_port;
		} else if (!_readFromLog) {
			_serialPort = port_or_ipAddr_or_filename;
			_serialBaud = baud_or_port;
		} else {
                    _filename = port_or_ipAddr_or_filename;
                }

		_isRunning = false;
		_connected = false;
		_piccoloState = General_Parse_State.HEADER1;
		_apState = AP_Parse_State.SYNC1;
		_generalHeader = new byte[14];
		_piccoloParseData = new byte[1024];
		_apPacket = new byte[512];

                setupLog ();
	}


	public boolean isRunning()
	{
		return _isRunning;
	}

	public void forceShutdown()
	{
		_shutdown = true;
	}

	public void addPicTelemetryListener(Pic_TelemetryListener obj) {
		_telemListeners.add(obj);
	}
	public void removePicTelemetryListener(Pic_TelemetryListener obj) {
		_telemListeners.remove(obj);
	}

	public void addPicRawStreamListener(Pic_RawStreamListener obj) {
		_streamListeners.add(obj);
	}
	public void removePicRawStreamListener(Pic_RawStreamListener obj) {
		_streamListeners.remove(obj);
	}

	private boolean connect() {

		try {

			if (_usingTCP) {
				System.out.println("Piccolo Interface: **************************Connecting to IP "+_ipAddr+" at port "+_ipPort);
				_oiSocket = new Socket(_ipAddr, _ipPort);
				_connected = true;
			} else if (!_readFromLog) {
				System.out.println("Piccolo Interface: Connecting to serial port "+_serialPort+" at baud "+_serialBaud);
				_xcommPort = new XCommSerialPort(_serialPort, _serialBaud);
				_connected = true;
			} else {
                            System.out.println ("Piccolo Interface: Running from log file: " + _filename);
                            _fileStream = new BufferedReader(new FileReader (_filename));
                            _connected = true;
                        }


		} catch ( Exception ex ) {
			System.out.println("Unable to connect, using TCP = "+_usingTCP + ",from file = " + _readFromLog);
			_connected = false;
		}
		return _connected;
	}

	
    @Override
    public void run()
	{
		byte[] bytesRead=null;
		int i;
		InputStream tcpIn=null;
        int numBytesRead=0;
            long lastLogTimestamp = -1;
            long lastCurrTimestamp = -1;

		try {
			_isRunning = true;
			while (!_shutdown) {

				// first we connect
				if (!_connected && !connect()) {
                                    System.out.println ("Waiting for Piccolo connection in read thread...");
					Thread.sleep(1000);
					continue;
				}

				if (_usingTCP) {
					tcpIn = _oiSocket.getInputStream();
					bytesRead = new byte[1024];
				}
                                if (_readFromLog) {
					bytesRead = new byte[1024];
				}

				while (!_shutdown) {

					try {

						if (_usingTCP) {
							numBytesRead = tcpIn.read(bytesRead);
						} else if (!_readFromLog) {
                                                    bytesRead = _xcommPort.readAvailableBytes();
                                                    if(bytesRead==null)
                                                        numBytesRead = 0;
                                                    else
                                                        numBytesRead = bytesRead.length;
						} else {
                                                    String logLine = _fileStream.readLine();

                                                    String timeString = logLine.substring(0, logLine.indexOf("\t"));
                                                    String tokenString = logLine.substring(logLine.indexOf("\t") + 1);

                                                    long timestamp = Long.parseLong(timeString);
                                                    long currTime = System.currentTimeMillis();
                                                    String[] tokens = tokenString.split("\t");

                                                    if (lastLogTimestamp < 0 || (currTime-lastCurrTimestamp)>(timestamp-lastLogTimestamp))
                                                    {
                                                    }
                                                    else
                                                    {
                                                        Thread.sleep ((timestamp-lastLogTimestamp)-(currTime-lastCurrTimestamp));
                                                    }

                                                    lastLogTimestamp = timestamp;
                                                    lastCurrTimestamp = currTime;
                                                    for (int j = 0; j < tokens.length; j ++)
                                                        bytesRead[j] = Byte.valueOf(tokens[j]);
                                                    numBytesRead = tokens.length;
                                                }
						
					} catch ( Exception ex ) {
						System.out.println("ERROR reading...");
						_connected = false;
						break;
					}
					
					if (bytesRead != null)
                                        {
						logBytesRecvd (bytesRead, numBytesRead, System.currentTimeMillis());

                                                for (Pic_RawStreamListener listen : _streamListeners)
                                                    listen.handlePic_RawStream(bytesRead, numBytesRead);
                                                
                                                for (i=0; i<numBytesRead; i++)
							parseTopPiccoloByte(bytesRead[i]);

                                        }

                                        Thread.sleep(10);
				}

			}

			System.out.println("PiccoloAP Shutting down...");
                        _xcommPort.close();
			_isRunning = false;
		} catch (Exception e) {
			e.printStackTrace();
			_isRunning = false;
                        _xcommPort.close();
		}

	}

    

	private enum AP_Parse_State {
		SYNC1,						  /* 0xA0 */
		SYNC2,							  /* 0x05 */
		PACKET_TYPE,					  /* type of packet (different meaning for AutoPilot and UAV) */
		SIZE,							  /* size of data in this packet */
		READING_PAYLOAD,				  /* Data bytes of payload */
		AP_CRC_Hi,							  /* Error-checking CRC */
		AP_CRC_Lo
	}

	private enum General_Parse_State {
		HEADER1,					  /* 0x5A */
		HEADER2,						  /* 0xA5 */
		READING_GENERAL_STREAM_HEADER,	  /* reading 10 bytes of header */
		HEADER_CHECK,					  /* XOR checksum of first 12 bytes of message */
		READING_DATA,					  /* Data bytes of stream packet */
		GS_CRC_Hi,							  /* Error-checking CRC */
		GS_CRC_Lo
	}

	private void parseTopPiccoloByte(byte newByte) {
		int checksum;
		int i, sourceID;
		boolean quit=false;

		switch (_piccoloState) {
			
			case HEADER1:
				//if (newByte == (byte)0x5A) {
                                if (newByte == (byte)0x24) {
					_piccoloState = General_Parse_State.HEADER2;
					_parseIdx = 0;
					_generalHeader[_parseIdx++] = newByte;
					_parseCRC = CRC16OneByte( newByte, 0 );
				}
				break;

			case HEADER2:
                                if (newByte == (byte) 0x47) {
				//if (newByte == (byte)0xA5) {
					_piccoloState = General_Parse_State.READING_GENERAL_STREAM_HEADER;
					_generalHeader[_parseIdx++] = newByte;
					//_parseBytesToRead = 13;	/* read 13 total header bytes */
                                        _parseBytesToRead = 6;
					_parseCRC = CRC16OneByte( newByte, _parseCRC );
                                } else if (newByte == (byte)0x24) {
                                //} else if (newByte == (byte)0x5A) {
					/* check for duplicate 0x5A bytes */
					_piccoloState = General_Parse_State.HEADER2;
					_parseIdx = 0;
					_generalHeader[_parseIdx++] = newByte;
					_parseCRC = CRC16OneByte( newByte, 0 );
				} else {
					_piccoloState = General_Parse_State.HEADER1;
				}
				break;

			case READING_GENERAL_STREAM_HEADER:
				_generalHeader[_parseIdx++] = newByte;
				_parseCRC = CRC16OneByte( newByte, _parseCRC );
				if (_parseIdx == _parseBytesToRead) {
					/* done reading bytes in header */
					_piccoloState = General_Parse_State.HEADER_CHECK;
				}
				break;

			case HEADER_CHECK:
				checksum = 0;
				for (i=0; i<6; i++) {
					checksum ^= _generalHeader[i];
				}
				if (checksum != newByte) {
					System.out.println("PiccoloParser: TOP LEVEL HEADER ERROR, "+checksum+" != "+newByte);
					_piccoloState = General_Parse_State.HEADER1;
				} else {
					_generalHeader[_parseIdx++] = newByte;
					_parseCRC = CRC16OneByte( newByte, _parseCRC );
					_piccoloState = General_Parse_State.READING_DATA;
					_parseIdx = 0;
					_parseDataSize = (int)(_generalHeader[12]) & 0x00FF;	/* pick out size */
					_parseBytesToRead = _parseDataSize;
					if (_parseIdx == _parseBytesToRead) {
						/* done reading data bytes */
						_piccoloState = General_Parse_State.GS_CRC_Hi;
					}
				}
				break;

			case READING_DATA:
				_piccoloParseData[_parseIdx++] = newByte;
				_parseCRC = CRC16OneByte( newByte, _parseCRC );
				if (_parseIdx == _parseBytesToRead) {
					/* done reading data bytes */
					_piccoloState = General_Parse_State.GS_CRC_Hi;
				}
				break;

			case GS_CRC_Hi:
				_receivedCRC = ((int)newByte << 8) & 0x0000FF00;
				_piccoloState = General_Parse_State.GS_CRC_Lo;
				break;

			case GS_CRC_Lo:
				_receivedCRC |= ((int)newByte & 0x00FF);
				/* now we check the CRC */
				if (_parseCRC != _receivedCRC) {
					System.out.println("PiccoloParser: Top Level CRC ERROR\n");
				} else {
					sourceID = (((_generalHeader[4] & 0x00FF) << 8) | (_generalHeader[5]&0x00FF));

					if (sourceID!=0 && _generalHeader[10]==(byte)3) {
						for (i=0; i<_parseDataSize; i++)
							parseAutopilotByte(_piccoloParseData[i]);
					}
				}
				_piccoloState = General_Parse_State.HEADER1;
				break;

			default:
				System.out.println("Whoa mama, what state am I in? ("+_piccoloState+")");
				_piccoloState = General_Parse_State.HEADER1;
				break;
		}

	}

	private void parseAutopilotByte(byte newByte) {

		switch (_apState) {
			case SYNC1:
				if (newByte == (byte)0xA0) {
					_apState = AP_Parse_State.SYNC2;
					_apParseIdx = 0;
					_apPacket[_apParseIdx++] = newByte;
					_apParseCRC = CRC16OneByte(newByte, 0);
				}
				break;

			case SYNC2:
                                if (newByte == (byte)0x05) {
					_apState = AP_Parse_State.PACKET_TYPE;
					_apPacket[_apParseIdx++] = newByte;
					_apParseCRC = CRC16OneByte(newByte, _apParseCRC);
                                } else if (newByte == (byte)0xA0) {
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
				_apState = (_apState==AP_Parse_State.PACKET_TYPE ? 
							AP_Parse_State.SIZE : 
							AP_Parse_State.READING_PAYLOAD);

				// added logic for zero-length packets
				if (_apState == AP_Parse_State.READING_PAYLOAD) {
					if (_apPacket[3] + 4 == _apParseIdx)
						_apState = AP_Parse_State.AP_CRC_Hi;
					else if (_apPacket[3] + 4 < _apParseIdx) {
						System.out.println("Strange, read in more UAS bytes than size");
						_apState = AP_Parse_State.SYNC1;
					}
				}
				break;

			case READING_PAYLOAD:
				_apPacket[_apParseIdx++] = newByte;
				_apParseCRC = CRC16OneByte(newByte, _apParseCRC);
				if (_apPacket[3] + 4 == _apParseIdx)
					_apState = AP_Parse_State.AP_CRC_Hi;

				// quick check for errors in parsing, to make sure of no buffer overflow
				else if (_apPacket[3] + 4 < _apParseIdx) {
					System.out.println("Strange, read in more UAS bytes than size");
					_apState = AP_Parse_State.SYNC1;
				}
				break;

			case AP_CRC_Hi:
				_apReceivedCRC = ((int)newByte << 8) & 0x0000FF00;
				_apState = AP_Parse_State.AP_CRC_Lo;
				break;

			case AP_CRC_Lo:
				_apReceivedCRC |= ((int)newByte & 0x00FF);
				/* now we check the CRC */
				if (_apParseCRC != _apReceivedCRC) {
					System.out.println("PiccoloParser: AP UAS STREAM CRC ERROR\n");
				} else {
					// GOOD PACKET, pass on to appropriate parsing
					parseUavTelemetry();
				}
				_apState = AP_Parse_State.SYNC1;
				break;

			default:
				System.out.println("Whoa mama, what ap UAS state am I in? ("+_apState+")");
				_apState = AP_Parse_State.SYNC1;
				break;
		}

	}


	private void parseUavTelemetry()
        {
            int roll=-1313, pitch=-1313;
            int wwest=-1313, wsouth=-1313;
            int yaw=-1313, comp=-1313, airsp=-1313; // unsigned
            int vn=-1313, ve=-1313, vd=-1313;
            int staticP=-1313;
            short ota=-1313;
            int lat=-1313, lon=-1313, alt=-1313;
            int pdop=-1313, gpsStatusByte0=-1313;
            int gpsWeek=-1313; 
            long gpsToW=-1313;
            char dataflag0, dataflag1;
            boolean dataflag0bit0,dataflag0bit1,dataflag0bit2,dataflag0bit3,dataflag0bit4,dataflag0bit5,dataflag0bit6,dataflag0bit7;
            boolean gpsInPacket=false, calcSensorDataInPacket=false, rawSensorDataInPacket=false, magnetometerInPacket=false, aglDataInPacket=false, fuelDataInPacket=false, aglDataInPacketIsLaser=false, baroAltIsCm=false;
            short numActuatorsInPacket=0;
            boolean aglIsValidLaser=false;
            int aglCm=-1313;
            Pic_Telemetry telem = new Pic_Telemetry();

            char navHealth0, navHealth1;
            boolean navHealthBit0, navHealthBit1, navHealthBit2,
                navHealthBit3,
                navHealthBit4,
                navHealthBit5,
                navHealthBit6,
                navHealthBit7,
                navHealthBit8,
                navHealthBit9,
                navHealthBit10,
                navHealthBit11,
                navHealthBit12,
                navHealthBit13,
                navHealthBit14,
                navHealthBit15;

            if (_apPacket[2] == (byte)69) {	/* hi res telemetry packet */

                int pIndex = 4;
                dataflag0 = (char)_apPacket[pIndex];
                pIndex ++;
                dataflag0bit0 = (dataflag0 & (1<<(7-0))) != 0;
                dataflag0bit1 = (dataflag0 & (1<<(7-1))) != 0;
                dataflag0bit2 = (dataflag0 & (1<<(7-2))) != 0;
                dataflag0bit3 = (dataflag0 & (1<<(7-3))) != 0;
                dataflag0bit4 = (dataflag0 & (1<<(7-4))) != 0;
                dataflag0bit5 = (dataflag0 & (1<<(7-5))) != 0;
                dataflag0bit6 = (dataflag0 & (1<<(7-6))) != 0;
                dataflag0bit7 = (dataflag0 & (1<<(7-7))) != 0;
                gpsInPacket = dataflag0bit0;
                calcSensorDataInPacket = dataflag0bit1;
                rawSensorDataInPacket = dataflag0bit2;
                magnetometerInPacket = dataflag0bit3;
                aglDataInPacket = dataflag0bit4;
                fuelDataInPacket = dataflag0bit5;
                aglDataInPacketIsLaser = dataflag0bit6;
                baroAltIsCm = !dataflag0bit7;
                aglIsValidLaser = (aglDataInPacket && aglDataInPacketIsLaser && m_LastAglNavFilterGood);

                dataflag1 = (char)_apPacket[pIndex];
                pIndex ++;
                numActuatorsInPacket = (short)(dataflag1 & (0x003F));

                pIndex += 2;    //Ignoring limits field
                pIndex += 4;    //Ignoring time field


                if (gpsInPacket)    //These bytes are only present if bit0 of dataflag1 is set
                {
                    lat = ((_apPacket[pIndex] & 0x00FF)<<24) +
                              ((_apPacket[pIndex+1] & 0x00FF)<<16) +
                              ((_apPacket[pIndex+2] & 0x00FF)<<8) +
                              (_apPacket[pIndex+3] & 0x00FF);
                    pIndex += 4;
                    lon = ((_apPacket[pIndex] & 0x00FF)<<24) +
                              ((_apPacket[pIndex+1] & 0x00FF)<<16) +
                              ((_apPacket[pIndex+2] & 0x00FF)<<8) +
                              (_apPacket[pIndex+3] & 0x00FF);
                    pIndex += 4;
                    alt = ((_apPacket[pIndex] & 0x00FF)<<16) +
                              ((_apPacket[pIndex+1] & 0x00FF)<<8) +
                              (_apPacket[pIndex+2] & 0x00FF);
                    pIndex += 3;

                    pIndex ++;      //Reserved byte
                    pdop = ((int)_apPacket[pIndex] & 0x00FF) >> 2;
                    pIndex ++;

                    pIndex ++;     //Ignoring satellites field byte 1

                    vn = (int)((short)((_apPacket[pIndex] & 0x00FF)<<8) + (short)(_apPacket[pIndex+1] & 0x00FF));
                    pIndex += 2;
                    ve = (int)((short)((_apPacket[pIndex] & 0x00FF)<<8) + (short)(_apPacket[pIndex+1] & 0x00FF));
                    pIndex += 2;
                    vd = (int)((short)((_apPacket[pIndex] & 0x00FF)<<8) + (short)(_apPacket[pIndex+1] & 0x00FF));
                    pIndex += 2;

                    gpsStatusByte0 = ((int)_apPacket[pIndex] & 0x00FF);
                    pIndex ++;

                    pIndex ++;       //Ignoring 2nd byte of GPS status field
                    
                    //GPS week field
                    gpsWeek = ((_apPacket[pIndex] & 0x00FF)<<8) +
                              (_apPacket[pIndex+1] & 0x00FF);
                    pIndex += 2;     
                    //GPS time-of-week field
                    gpsToW = ((_apPacket[pIndex] & 0x00FF)<<24) +
                              ((_apPacket[pIndex+1] & 0x00FF)<<16) +
                              ((_apPacket[pIndex+2] & 0x00FF)<<8) +
                              (_apPacket[pIndex+3] & 0x00FF);
                    pIndex += 4;     
                    
                }

                if (calcSensorDataInPacket)
                {
                    roll = (int)((short)((_apPacket[pIndex] & 0x00FF)<<8) + (short)(_apPacket[pIndex+1] & 0x00FF));
                    pIndex += 2;
                    pitch = (int)((short)((_apPacket[pIndex] & 0x00FF)<<8) + (short)(_apPacket[pIndex+1] & 0x00FF));
                    pIndex += 2;
                    yaw = ((_apPacket[pIndex] & 0x00FF)<<8) + (_apPacket[pIndex+1] & 0x00FF);
                    pIndex += 2;

                    pIndex += 2;        //Ignoring baro alt

                    wsouth = (int)((short)((_apPacket[pIndex] & 0x00FF)<<8) + (short)(_apPacket[pIndex+1] & 0x00FF));
                    pIndex += 2;
                    wwest = (int)((short)((_apPacket[pIndex] & 0x00FF)<<8) + (short)(_apPacket[pIndex+1] & 0x00FF));
                    pIndex += 2;

                    pIndex += 2;        //Ignoring left RPM field
                    pIndex += 2;        //Ignoring right RPM field
                    pIndex ++;          //Ignoring density ratio field
                }

                if (rawSensorDataInPacket)
                {
                    ota = (short)(_apPacket[pIndex] & 0x00FF);
                    pIndex ++;
                    airsp = ((_apPacket[pIndex] & 0x00FF)<<8) + (_apPacket[pIndex+1] & 0x00FF);
                    pIndex +=2;
                    staticP = (int)(((_apPacket[pIndex] & 0x00FF)<<8) + (_apPacket[pIndex+1] & 0x00FF));
                    pIndex +=2;

                    pIndex +=2;     //Ignoring roll rate
                    pIndex +=2;     //Ignoring pitch rate
                    pIndex +=2;     //Ignoring yaw rate
                    pIndex +=2;     //Ignoring x accel
                    pIndex +=2;     //Ignoring y accel
                    pIndex +=2;     //Ignoring z accel
                }

                if (magnetometerInPacket)
                {
                    pIndex += 2;    //Ignoring x mag field
                    pIndex += 2;    //Ignoring y mag field
                    pIndex += 2;    //Ignoring z mag field

                    comp = ((_apPacket[pIndex] & 0x00FF)<<8) + (_apPacket[pIndex+1] & 0x00FF);
                    pIndex +=2;
                }

                if (aglDataInPacket)
                {
                    aglCm = (int)(((_apPacket[pIndex] & 0x00FF)<<8) + (_apPacket[pIndex+1] & 0x00FF));
                    pIndex +=2;
                }

                if (numActuatorsInPacket > 0)
                {
                    pIndex += 2*numActuatorsInPacket;
                    //Not reading any actuators
                }

                if (fuelDataInPacket)
                {
                    pIndex += 4;
                    //Not reading any fuel data
                }

                if (calcSensorDataInPacket)
                {
                    pIndex ++;      //Ignoring weight-on-wheels stuff

                    pIndex += 3;    //Ignoring baro-alt field
                }


                telem.VelNorth = (float)vn / 100.0;
                telem.VelEast = (float)ve / 100.0;
                telem.VelDown = (float)vd / 100.0;
                telem.TrueHeading = ((float)comp / 10000.0f) * (180.0f / Math.PI);
                telem.Lat = (double)lat / 1000.0 / 3600.0;
                telem.Lon = (double)lon / 1000.0 / 3600.0;
                telem.AltWGS84 = ((double)alt / 100.0) - 1000.0;
                telem.AltMSL = MathUtils.convertAltAbvElliptoMsl(telem.Lat, telem.Lon, telem.AltWGS84);
                telem.WindSouth = (double)wsouth / 100.0;
                telem.WindWest = (double)wwest / 100.0;
                telem.Roll = (float)roll / 10000.0 * (180.0/Math.PI);
                telem.Pitch = (float)pitch / 10000.0 * (180.0/Math.PI);
                telem.Yaw = (float)yaw / 10000.0 * (180.0/Math.PI);
                telem.IndAirSpeed_mps = ((double)airsp - 2000.0) / 100.0;
                telem.StaticPressPa =  staticP * 2;
                telem.OutsideAirTempC = ota;
                telem.m_TimestampMs = MathUtils.getEpochTimestampMsFromGpsTow(gpsWeek, gpsToW);
                
                telem.GPS_Status = gpsStatusByte0 >> 5;
                telem.PDOP = (double)pdop * 0.2;

                telem.AltLaserValid = aglIsValidLaser;
                telem.AltLaser_m = aglCm/100.0;
                if (telem.AltLaser_m > 655)
                    telem.AltLaserValid = false;

                m_LastAltMSL_m = telem.AltMSL;
                        
                for (Pic_TelemetryListener listen : _telemListeners)
                        listen.handlePic_Telemetry(System.currentTimeMillis(), telem);

                doPicDisplay ();
            }


            if (_apPacket[2] == (byte)71) {	/* hi res system status packet */

                int pIndex = 4;

                pIndex += 3; //Main power
                pIndex += 3; //Servo power
                pIndex += 1; //Internal V
                pIndex += 1; //Board T
                pIndex += 1; //Data source

                navHealth0 = (char)_apPacket[pIndex];
                pIndex ++;
                navHealth1 = (char)_apPacket[pIndex];
                pIndex ++;
                navHealthBit0 = (navHealth0 & (1<<(7-0))) != 0;
                navHealthBit1 = (navHealth0 & (1<<(7-1))) != 0;
                navHealthBit2 = (navHealth0 & (1<<(7-2))) != 0;
                navHealthBit3 = (navHealth0 & (1<<(7-3))) != 0;
                navHealthBit4 = (navHealth0 & (1<<(7-4))) != 0;
                navHealthBit5 = (navHealth0 & (1<<(7-5))) != 0;
                navHealthBit6 = (navHealth0 & (1<<(7-6))) != 0;
                navHealthBit7 = (navHealth0 & (1<<(7-7))) != 0;
                navHealthBit8 = (navHealth1 & (1<<(7-0))) != 0;
                navHealthBit9 = (navHealth1 & (1<<(7-1))) != 0;
                navHealthBit10 = (navHealth1 & (1<<(7-2))) != 0;
                navHealthBit11 = (navHealth1 & (1<<(7-3))) != 0;
                navHealthBit12 = (navHealth1 & (1<<(7-4))) != 0;
                navHealthBit13 = (navHealth1 & (1<<(7-5))) != 0;
                navHealthBit14 = (navHealth1 & (1<<(7-6))) != 0;
                navHealthBit15 = (navHealth1 & (1<<(7-7))) != 0;

                m_LastAglNavFilterGood = navHealthBit4;
            }
	}


	public void sendFastDataRate() {
		byte[] cmd = new  byte[26];
		int crc;
		int i;

		cmd[0] = (byte)0x5A;
		cmd[1] = (byte)0xA5;

		cmd[2] = (byte)((_autopilotNum>>8) & 0x00FF);
		cmd[3] = (byte)(_autopilotNum & 0x00FF);


		cmd[4] = (byte)0xFF;
		cmd[5] =(byte)0xFE;

		cmd[6] = 0x00; // total length of packet
		cmd[7] = 0x1A;

		cmd[8] = 0x00;
		cmd[9] = 0x00;
		cmd[10] = 0x03;
		cmd[11] = 0x00;
		cmd[12] = 0x0A;	// length of sub-packet

		cmd[13] = 0x00;	// x-or of header
		for (i = 0; i < 13; i++)
			cmd[13] ^= cmd[i];

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
		for (i=14; i<=21; i++)
			crc = CRC16OneByte(cmd[i], crc);
		cmd[22] = (byte)((crc >> 8) & 0x00FF);	// CRC (high)
		cmd[23] = (byte)(crc & 0x00FF);	// CRC (low)
////////////////////////////

		crc = CRC16( cmd, 24 );
		cmd[24] = (byte)((crc >> 8) & 0x00FF);	// CRC
		cmd[25] = (byte)(crc & 0x00FF);	// CRC

                sendRawCommandBytes(cmd);
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
		cmd[1] = (byte)0xA5;

		cmd[2] = (byte)((_autopilotNum>>8) & 0x00FF);
		cmd[3] = (byte)(_autopilotNum & 0x00FF);

		cmd[4] = (byte)0xFF;
		cmd[5] = (byte)0xFE;

		cmd[6] = 0x00; // sequence num
		cmd[7] = 0x1A;

		cmd[8] = 0x00; // ack num
		cmd[9] = 0x00;

		cmd[10] = 0x03;	 // stream
		cmd[11] = 0x00;	 // flags

		cmd[12] = 0x18;	// length of sub-packet (24 bytes)

		cmd[13] = 0x00;	// x-or of header
		for (i = 0; i < 13; i++)
			cmd[13] ^= cmd[i];

/////////////////////////
		cmd[14] = (byte)0xA0;
		cmd[15] = 0x05;
		cmd[16] = 0x08;	// WAYPOINT packet type
		cmd[17] = 0x12;	// length (18bytes)

		masLat = (int)(lat * 3600.0 * 1000.0);
		cmd[18] = (byte)((masLat>>24) & 0x00FF);
		cmd[19] = (byte)((masLat>>16) & 0x00FF);
		cmd[20] = (byte)((masLat>>8) & 0x00FF);
		cmd[21] = (byte)(masLat & 0x00FF);
		masLon = (int)(lon * 3600.0 * 1000.0);
		cmd[22] = (byte)((masLon>>24) & 0x00FF);
		cmd[23] = (byte)((masLon>>16) & 0x00FF);
		cmd[24] = (byte)((masLon>>8) & 0x00FF);
		cmd[25] = (byte)(masLon & 0x00FF);

		cmd[26] = (byte)(rightTurn ? 0x21 : 0x01);
		cmd[27] = (byte)(radiusM/10.0);

                if (m_LastAltMSL_m > 0) 
                {
                    if (altMSL > m_LastAltMSL_m + m_MaximumAltitudeLeadingCommand_m)
                        altMSL = m_LastAltMSL_m + m_MaximumAltitudeLeadingCommand_m;
                    if (altMSL < m_LastAltMSL_m - m_MaximumAltitudeLeadingCommand_m)
                        altMSL = m_LastAltMSL_m - m_MaximumAltitudeLeadingCommand_m;
                }
                if(Config.getConfig().getPropertyAsBoolean("PicInterface.UseAgl", true))
                {
                    //sending AGL, convert MSL altitude to AGL by subtracting ground altitude
                    double groundMSL = DtedGlobalMap.getDted().getJlibAltitude(lat, lon).getDoubleValue(Length.METERS);
                    masAlt = (int)(altMSL - groundMSL);
                }
                else
                {
                    //sending WGS84, convert MSL altitude to WGS84 by using utility function
                    masAlt = (int)MathUtils.convertMslToAltAbvEllip(lat, lon, altMSL);
                }

		cmd[28] = (byte)((masAlt>>8) & 0x00FF);
		cmd[29] = (byte)(masAlt & 0x00FF);
		cmd[30] = 0x00;
		cmd[31] = 0x00;
		cmd[32] = (byte)(waypoint);
		cmd[33] = (byte)(waypoint-1);
		cmd[34] = 0x00;
		
                if(Config.getConfig().getPropertyAsBoolean("PicInterface.UseAgl", true))
                    cmd[35] = (byte)0x10;
                else
                    cmd[35] = 0x00;

		crc = 0;
		for (i=14; i<=35; i++)
			crc = CRC16OneByte(cmd[i], crc);
		cmd[36] = (byte)((crc >> 8) & 0x00FF);	// CRC (high)
		cmd[37] = (byte)(crc & 0x00FF);	// CRC (low)
////////////////////////////

		crc = CRC16( cmd, 38 );
		cmd[38] = (byte)((crc >> 8) & 0x00FF);	// CRC
		cmd[39] = (byte)(crc & 0x00FF);	// CRC

        //// Cmd 2
        byte[] cmd1 = new byte[40];

		cmd1[0] = 0x5A;
		cmd1[1] = (byte)0xA5;

		cmd1[2] = (byte)((_autopilotNum>>8) & 0x00FF);
		cmd1[3] = (byte)(_autopilotNum & 0x00FF);

		cmd1[4] = (byte)0xFF;
		cmd1[5] = (byte)0xFE;

		cmd1[6] = 0x00; // sequence num
		cmd1[7] = 0x1A;

		cmd1[8] = 0x00; // ack num
		cmd1[9] = 0x00;

		cmd1[10] = 0x03;	 // stream
		cmd1[11] = 0x00;	 // flags

		cmd1[12] = 0x18;	// length of sub-packet (24 bytes)

		cmd1[13] = 0x00;	// x-or of header
		for (i = 0; i < 13; i++)
			cmd1[13] ^= cmd1[i];

/////////////////////////
		cmd1[14] = (byte)0xA0;
		cmd1[15] = 0x05;
		cmd1[16] = 0x08;	// WAYPOINT packet type
		cmd1[17] = 0x12;	// length (18bytes)

		masLat = (int)(lat * 3600.0 * 1000.0);
		cmd1[18] = (byte)((masLat>>24) & 0x00FF);
		cmd1[19] = (byte)((masLat>>16) & 0x00FF);
		cmd1[20] = (byte)((masLat>>8) & 0x00FF);
		cmd1[21] = (byte)(masLat & 0x00FF);
		masLon = (int)(lon * 3600.0 * 1000.0);
		cmd1[22] = (byte)((masLon>>24) & 0x00FF);
		cmd1[23] = (byte)((masLon>>16) & 0x00FF);
		cmd1[24] = (byte)((masLon>>8) & 0x00FF);
		cmd1[25] = (byte)(masLon & 0x00FF);

		cmd1[26] = (byte)(rightTurn ? 0x21 : 0x01);
		cmd1[27] = (byte)(radiusM/10.0);

                if(Config.getConfig().getPropertyAsBoolean("PicInterface.UseAgl", true))
                {
                    //sending AGL, convert MSL altitude to AGL by subtracting ground altitude
                    double groundMSL = DtedGlobalMap.getDted().getJlibAltitude(lat, lon).getDoubleValue(Length.METERS);
                    masAlt = (int)(altMSL - groundMSL);
                }
                else
                {
                    //sending WGS84, convert MSL altitude to WGS84 by using utility function
                    masAlt = (int)MathUtils.convertMslToAltAbvEllip(lat, lon, altMSL);
                }

		cmd1[28] = (byte)((masAlt>>8) & 0x00FF);
		cmd1[29] = (byte)(masAlt & 0x00FF);
		cmd1[30] = 0x00;
		cmd1[31] = 0x00;
		cmd1[32] = (byte)(waypoint-1);
		cmd1[33] = (byte)(waypoint);
		cmd1[34] = 0x00;
		if(Config.getConfig().getPropertyAsBoolean("PicInterface.UseAgl", true))
                    cmd1[35] = (byte)0x10;
                else
                    cmd1[35] = 0x00;

		crc = 0;
		for (i=14; i<=35; i++)
			crc = CRC16OneByte(cmd1[i], crc);
		cmd1[36] = (byte)((crc >> 8) & 0x00FF);	// CRC (high)
		cmd1[37] = (byte)(crc & 0x00FF);	// CRC (low)
////////////////////////////

		crc = CRC16( cmd1, 38 );
		cmd1[38] = (byte)((crc >> 8) & 0x00FF);	// CRC
		cmd1[39] = (byte)(crc & 0x00FF);	// CRC


                sendRawCommandBytes(cmd);
                sendRawCommandBytes(cmd1);
	}

	// radius MUST be less than (or equal to) 2550 meters
	public void changeInterceptWaypoint(double lat, double lon, double altMSL, double radiusM, boolean rightTurn) {
		changeWaypoint(INTERCEPT_WAYPOINT_INDEX,lat,lon,altMSL,radiusM,rightTurn);
	}

	// radius MUST be less than (or equal to) 2550 meters
	public void changeLoiterWaypoint(double lat, double lon, double altMSL, double radiusM, boolean rightTurn) {
		changeWaypoint(LOITER_WAYPOINT_INDEX,lat,lon,altMSL,radiusM,rightTurn);
	}

        private void sendToWaypoint(int waypoint) {
		byte[] cmd = new byte[24];
		int crc;
		int i, maslat, maslon;

		cmd[0] = 0x5A;
		cmd[1] = (byte)0xA5;

		cmd[2] = (byte)((_autopilotNum>>8) & 0x00FF);
		cmd[3] = (byte)(_autopilotNum & 0x00FF);

		cmd[4] = (byte)0xFF;
		cmd[5] = (byte)0xFE;

		cmd[6] = 0x00; // sequence num
		cmd[7] = 0x1A;

		cmd[8] = 0x00; // ack num
		cmd[9] = 0x00;

		cmd[10] = 0x03;	 // stream
		cmd[11] = 0x00;	 // flags

		cmd[12] = 0x08;	// length of sub-packet (8 bytes)

		cmd[13] = 0x00;	// x-or of header
		for (i = 0; i < 13; i++)
			cmd[13] ^= cmd[i];

/////////////////////////
		cmd[14] = (byte)0xA0;
		cmd[15] = 0x05;
		cmd[16] = 0x0A;	// TRACK packet type
		cmd[17] = 0x02;	// length (2 bytes)

		cmd[18] = (byte)waypoint;
		cmd[19] = 0x01;

		crc = 0;
		for (i=14; i<=19; i++)
			crc = CRC16OneByte(cmd[i], crc);
		cmd[20] = (byte)((crc >> 8) & 0x00FF);	// CRC (high)
		cmd[21] = (byte)(crc & 0x00FF);	// CRC (low)
////////////////////////////

		crc = CRC16( cmd, 22 );
		cmd[22] = (byte)((crc >> 8) & 0x00FF);	// CRC
		cmd[23] = (byte)(crc & 0x00FF);	// CRC

		sendRawCommandBytes(cmd);
	}
        
        public void sendRawCommandBytes(byte[] m_CommandByteBuffer) 
        {
            sendRawCommandBytes(m_CommandByteBuffer, m_CommandByteBuffer.length);
        }
        
        public void sendRawCommandBytes(byte[] m_CommandByteBuffer, int commandBytes) 
        {
            try 
            {
                if (_usingTCP)
                        _oiSocket.getOutputStream().write(m_CommandByteBuffer, 0, commandBytes);
                else if (!_readFromLog)
                        _xcommPort.sendBytes(m_CommandByteBuffer, 0, commandBytes);
                
                logBytesSent (m_CommandByteBuffer, commandBytes, System.currentTimeMillis());
            } 
            catch ( Exception ex ) 
            {
                _connected = false;
                System.out.println("PICCOLO TRACK SEND ERROR:");
                ex.printStackTrace();
            }
        }

        public void zeroAirData(Double altMSLm)
        {
		byte[] cmd = new byte[26];
		int crc;
		int i, alt;

		cmd[0] = 0x5A;
		cmd[1] = (byte)0xA5;

		cmd[2] = (byte)((_autopilotNum>>8) & 0x00FF);
		cmd[3] = (byte)(_autopilotNum & 0x00FF);

		cmd[4] = (byte)0xFF;
		cmd[5] = (byte)0xFE;

		cmd[6] = 0x00; // sequence num
		cmd[7] = 0x1A;

		cmd[8] = 0x00; // ack num
		cmd[9] = 0x00;

		cmd[10] = 0x03;	 // stream
		cmd[11] = 0x00;	 // flags

		cmd[12] = 0x0A;	// length of sub-packet (10 bytes)

		cmd[13] = 0x00;	// x-or of header
		for (i = 0; i < 13; i++)
			cmd[13] ^= cmd[i];

/////////////////////////
		cmd[14] = (byte)0xA0;
		cmd[15] = 0x05;
		cmd[16] = 0x2D;	// Air Data Zero
		cmd[17] = 0x04;	// length (4 bytes)

                if (altMSLm == null)
                    alt = -500001;  //no altitude setting
                else
                    alt = ((int)altMSLm.doubleValue())*100;
		cmd[18] = (byte)((alt>>24) & 0x00FF);
		cmd[19] = (byte)((alt>>16) & 0x00FF);
		cmd[20] = (byte)((alt>>8) & 0x00FF);
		cmd[21] = (byte)(alt & 0x00FF);

		crc = 0;
		for (i=14; i<=21; i++)
			crc = CRC16OneByte(cmd[i], crc);
		cmd[22] = (byte)((crc >> 8) & 0x00FF);	// CRC (high)
		cmd[23] = (byte)(crc & 0x00FF);	// CRC (low)
////////////////////////////

		crc = CRC16( cmd, 24 );
		cmd[24] = (byte)((crc >> 8) & 0x00FF);	// CRC
		cmd[25] = (byte)(crc & 0x00FF);	// CRC

		sendRawCommandBytes(cmd);
	}

        public void setAltimeter(Double basePressurePa)
        {
		byte[] cmd = new byte[27];
		int crc;
		int i, press;

		cmd[0] = 0x5A;
		cmd[1] = (byte)0xA5;

		cmd[2] = (byte)((_autopilotNum>>8) & 0x00FF);
		cmd[3] = (byte)(_autopilotNum & 0x00FF);

		cmd[4] = (byte)0xFF;
		cmd[5] = (byte)0xFE;

		cmd[6] = 0x00; // sequence num
		cmd[7] = 0x1A;

		cmd[8] = 0x00; // ack num
		cmd[9] = 0x00;

		cmd[10] = 0x03;	 // stream
		cmd[11] = 0x00;	 // flags

		cmd[12] = 0x0B;	// length of sub-packet (11 bytes)

		cmd[13] = 0x00;	// x-or of header
		for (i = 0; i < 13; i++)
			cmd[13] ^= cmd[i];

/////////////////////////
		cmd[14] = (byte)0xA0;
		cmd[15] = 0x05;
		cmd[16] = 0x0F;	// Altitmer setting
		cmd[17] = 0x05;	// length (5 bytes)

                if (basePressurePa == null || basePressurePa.doubleValue() < 0)
                {
                    System.out.println ("No base pressure specified for altimeter setting, ignoring...");
                    return;
                }
                else
                    press = ((int)basePressurePa.doubleValue());
		cmd[18] = (byte)((press>>24) & 0x00FF);
		cmd[19] = (byte)((press>>16) & 0x00FF);
		cmd[20] = (byte)((press>>8) & 0x00FF);
		cmd[21] = (byte)(press & 0x00FF);
                cmd[22] = 0x00;

		crc = 0;
		for (i=14; i<=22; i++)
			crc = CRC16OneByte(cmd[i], crc);
		cmd[23] = (byte)((crc >> 8) & 0x00FF);	// CRC (high)
		cmd[24] = (byte)(crc & 0x00FF);	// CRC (low)
////////////////////////////

		crc = CRC16( cmd, 25 );
		cmd[25] = (byte)((crc >> 8) & 0x00FF);	// CRC
		cmd[26] = (byte)(crc & 0x00FF);	// CRC

		sendRawCommandBytes(cmd);
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
        static public int CRC16(byte[] pBuf, int startIndex, int len)
        {
		int crc = 0;
		int i;

		for (i = startIndex; i < (startIndex + len); i++) {
			crc = (int)(_crctable[(pBuf[i]^crc) & 0xff] ^ (crc >> 8));
		}

		return crc;
	}

	static public int CRC16(byte[] pBuf, int len)
	{
            return CRC16(pBuf, 0, len);
        }


	/*! Calculate the new 16-bit cyclic redundancy check that results from adding
	a byte to an existing crc.
	\param Byte is the new byte.
	\param crc is the existing crc value.
	\return The new crc value*/
	static public int CRC16OneByte(byte Byte, int crc)
	{
		return(int)(_crctable[(Byte^crc) & 0xff] ^ (crc >> 8));
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





        /**
         * Buffered writer object to save files to
         */
        private BufferedWriter m_LogFileWriter = null;

        /**
         * file object
         */
        private File m_LogFile = null;

        /**
         * File size limit for files
         */
        private int MAX_LOG_FILE_BYTES = 10000000;

        /**
         * Counter for filenames to differentiate files if they get too large
         */
        private int m_LogFileCounter = 0;

        /**
         * Name of output file name to log data
         */
        private String m_LogFilename;

        /**
         * Log folder
         */
        String m_LogFolderName;


        private void setupLog ()
        {
            m_LogFolderName = ".\\PiccoloLogs";
            while (m_LogFolderName.endsWith ("\\") || m_LogFolderName.endsWith("/"))
                m_LogFolderName = m_LogFolderName.substring(0, m_LogFolderName.length() - 1);
            try
            {
                File outputFolder = new File (m_LogFolderName);
                if (!outputFolder.exists())
                    outputFolder.mkdirs();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            m_LogFolderName += "/";
            m_LogFilename = m_LogFolderName + "PiccoloLog_" + System.currentTimeMillis() + "_";

        }

        private void ensureLogFile()
        {
            if (m_LogFile == null ||  m_LogFile.length() > MAX_LOG_FILE_BYTES)
            {
                //We need a new file
                try
                {
                    if (m_LogFileWriter != null)
                    {
                        m_LogFileWriter.flush();
                        m_LogFileWriter.close();
                    }
                }
                catch (Exception e)
                {
                    System.err.println("Could not flush output stream before making new file for Piccolo log");
                }

                Formatter format = new Formatter();
                format.format("%1$s%2$05d%3$s", m_LogFilename, m_LogFileCounter++, ".log");
                m_LogFile = new File(format.out().toString());

                try
                {
                    OutputStream ist = new FileOutputStream(m_LogFile);
                    m_LogFileWriter = new BufferedWriter(new OutputStreamWriter(ist));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        private void logBytesRecvd (byte[] bytesRead, int numBytesRead, long timestamp)
        {
            logBytes(bytesRead, numBytesRead, true, timestamp);
        }
                
        private void logBytesSent (byte[] bytesRead, int numBytesRead, long timestamp)
        {
            logBytes(bytesRead, numBytesRead, false, timestamp);   
        }

        private void logBytes (byte[] bytesRead, int numBytesRead, boolean recvd, long timestamp)
        {
            ensureLogFile();

            try
            {
                m_LogFileWriter.write (System.currentTimeMillis() + "\t");
                m_LogFileWriter.write (Boolean.toString(recvd)+ "\t");
                for (int i = 0; i < numBytesRead; i ++)
                    m_LogFileWriter.write (bytesRead[i] + "\t");
                m_LogFileWriter.newLine();
                m_LogFileWriter.flush();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }


        static private PiccoloDisplay picDisplay = null;
        static private final Object lockObject = new Object();
        private void doPicDisplay()
        {
            if (!showPicDisplay)
                return;

            synchronized (lockObject)
            {
                if (picDisplay == null)
                {
                    picDisplay = new PiccoloDisplay();
                    picDisplay.setLocation (200,200);
                    picDisplay.setVisible(true);
                }
                else if (!picDisplay.isVisible())
                    picDisplay.setVisible(true);
            }


            int roll=-1313, pitch=-1313;
            int wwest=-1313, wsouth=-1313;
            int yaw=-1313, comp=-1313, airsp=-1313; // unsigned
            int vn=-1313, ve=-1313, vd=-1313;
            int staticP=-1313;
            short ota=-1313;
            int lat=-1313, lon=-1313, alt=-1313;
            int pdop=-1313, gpsStatusByte0=-1313;
            char dataflag0, dataflag1;
            boolean dataflag0bit0,dataflag0bit1,dataflag0bit2,dataflag0bit3,dataflag0bit4,dataflag0bit5,dataflag0bit6,dataflag0bit7;
            boolean gpsInPacket=false, calcSensorDataInPacket=false, rawSensorDataInPacket=false, magnetometerInPacket=false, aglDataInPacket=false, fuelDataInPacket=false, aglDataInPacketIsLaser=false, baroAltIsCm=false;
            short numActuatorsInPacket=0;
            boolean aglIsValidLaser=false;
            int aglCm=-1313;
            int gpsWeek=-1313; 
            long gpsToW=-1313;
            

            if (_apPacket[2] == (byte)69) {	/* hi res telemetry packet */

                int displayIndex = 0;
                int pIndex = 4;
                dataflag0 = (char)_apPacket[pIndex];
                picDisplay.setField(displayIndex++, "data flag0", (_apPacket[pIndex] & 0x00FF) + "");
                pIndex ++;
                dataflag0bit0 = (dataflag0 & (1<<(7-0))) != 0;
                dataflag0bit1 = (dataflag0 & (1<<(7-1))) != 0;
                dataflag0bit2 = (dataflag0 & (1<<(7-2))) != 0;
                dataflag0bit3 = (dataflag0 & (1<<(7-3))) != 0;
                dataflag0bit4 = (dataflag0 & (1<<(7-4))) != 0;
                dataflag0bit5 = (dataflag0 & (1<<(7-5))) != 0;
                dataflag0bit6 = (dataflag0 & (1<<(7-6))) != 0;
                dataflag0bit7 = (dataflag0 & (1<<(7-7))) != 0;
                gpsInPacket = dataflag0bit0;
                calcSensorDataInPacket = dataflag0bit1;
                rawSensorDataInPacket = dataflag0bit2;
                magnetometerInPacket = dataflag0bit3;
                aglDataInPacket = dataflag0bit4;
                fuelDataInPacket = dataflag0bit5;
                aglDataInPacketIsLaser = dataflag0bit6;
                baroAltIsCm = !dataflag0bit7;
                aglIsValidLaser = (aglDataInPacket && aglDataInPacketIsLaser);

                picDisplay.setField (displayIndex++, "gpsInPacket", gpsInPacket + "");
                picDisplay.setField (displayIndex++, "calcSensorDataInPacket", calcSensorDataInPacket + "");
                picDisplay.setField (displayIndex++, "rawSensorDataInPacket", rawSensorDataInPacket + "");
                picDisplay.setField (displayIndex++, "magnetometerInPacket", magnetometerInPacket + "");
                picDisplay.setField (displayIndex++, "aglDataInPacket", aglDataInPacket + "");
                picDisplay.setField (displayIndex++, "fuelDataInPacket", fuelDataInPacket + "");
                picDisplay.setField (displayIndex++, "aglDataInPacketIsLaser", aglDataInPacketIsLaser + "");

                dataflag1 = (char)_apPacket[pIndex];
                picDisplay.setField(displayIndex++, "data flag1", (_apPacket[pIndex] & 0x00FF) + "");
                pIndex ++;
                numActuatorsInPacket = (short)(dataflag1 & (0x003F));

                picDisplay.setField(displayIndex++, "limits0", (_apPacket[pIndex] & 0x00FF) + "");
                pIndex ++;
                picDisplay.setField(displayIndex++, "limits1", (_apPacket[pIndex] & 0x00FF) + "");
                pIndex ++;

                picDisplay.setField(displayIndex++, "time", ((_apPacket[pIndex] & 0x00FF)<<24) +
                          ((_apPacket[pIndex+1] & 0x00FF)<<16) +
                          ((_apPacket[pIndex+2] & 0x00FF)<<8) +
                          (_apPacket[pIndex+3] & 0x00FF)
                          + "");
                pIndex += 4;


                if (gpsInPacket)    //These bytes are only present if bit0 of dataflag1 is set
                {
                    picDisplay.setField(displayIndex++, "lat deg", (((_apPacket[pIndex] & 0x00FF)<<24) +
                              ((_apPacket[pIndex+1] & 0x00FF)<<16) +
                              ((_apPacket[pIndex+2] & 0x00FF)<<8) +
                              (_apPacket[pIndex+3] & 0x00FF))/ 3600.0 / 1000.0
                          + "");
                    pIndex += 4;
                    picDisplay.setField(displayIndex++, "lon deg", (((_apPacket[pIndex] & 0x00FF)<<24) +
                              ((_apPacket[pIndex+1] & 0x00FF)<<16) +
                              ((_apPacket[pIndex+2] & 0x00FF)<<8) +
                              (_apPacket[pIndex+3] & 0x00FF)) / 3600.0 / 1000.0
                          + "");
                    pIndex += 4;
                    picDisplay.setField(displayIndex++, "height m WGS84", (((_apPacket[pIndex] & 0x00FF)<<16) +
                              ((_apPacket[pIndex+1] & 0x00FF)<<8) +
                              (_apPacket[pIndex+2] & 0x00FF)) / 100.0 - 1000.0
                          + "");
                    pIndex += 3;

                    pIndex ++;      //Reserved byte

                    int satStatus = ((_apPacket[pIndex] & 0x00FF)<<8) + (_apPacket[pIndex+1] & 0x00FF);
                    pIndex += 2;
                    picDisplay.setField(displayIndex++, "Dilution of Precision", ((satStatus & 0xFC00)>>10)*0.2 + "");
                    picDisplay.setField(displayIndex++, "Visible Satellites", ((satStatus & 0x03E0)>>5) + "");
                    picDisplay.setField(displayIndex++, "Tracked Satellites", ((satStatus & 0x001F)) + "");

                    picDisplay.setField(displayIndex++, "V North (m/s)", ((int)((short)((_apPacket[pIndex] & 0x00FF)<<8) + (short)(_apPacket[pIndex+1] & 0x00FF))) / 100.0
                          + "");
                    pIndex += 2;
                    picDisplay.setField(displayIndex++, "V East (m/s)", ((int)((short)((_apPacket[pIndex] & 0x00FF)<<8) + (short)(_apPacket[pIndex+1] & 0x00FF))) / 100.0
                          + "");
                    pIndex += 2;
                    picDisplay.setField(displayIndex++, "V Down (m/s)", ((int)((short)((_apPacket[pIndex] & 0x00FF)<<8) + (short)(_apPacket[pIndex+1] & 0x00FF))) / 100.0
                          + "");
                    pIndex += 2;

                    picDisplay.setField(displayIndex++, "GPS Status", (((_apPacket[pIndex] & 0x00FF)<<8) +
                          ((_apPacket[pIndex+1] & 0x00FF)))
                          + "");
                    pIndex += 2;

                    //GPS week field
                    gpsWeek = ((_apPacket[pIndex] & 0x00FF)<<8) +
                              (_apPacket[pIndex+1] & 0x00FF);
                    picDisplay.setField(displayIndex++, "GPS Week", gpsWeek + "");
                    pIndex += 2;     
                    //GPS time-of-week field
                    gpsToW = ((_apPacket[pIndex] & 0x00FF)<<24) +
                              ((_apPacket[pIndex+1] & 0x00FF)<<16) +
                              ((_apPacket[pIndex+2] & 0x00FF)<<8) +
                              (_apPacket[pIndex+3] & 0x00FF);
                    picDisplay.setField(displayIndex++, "GPS Time of Week", gpsToW + "");
                    pIndex += 4;
                    
                    long timestampMs = MathUtils.getEpochTimestampMsFromGpsTow(gpsWeek, gpsToW);
                    picDisplay.setField(displayIndex++, "GPS Converted Time", timestampMs + "");
                }

                if (calcSensorDataInPacket)
                {
                    picDisplay.setField(displayIndex++, "Roll (rad)", ((int)((short)((_apPacket[pIndex] & 0x00FF)<<8) + (short)(_apPacket[pIndex+1] & 0x00FF))) / 10000.0
                            + "");
                    pIndex += 2;
                    picDisplay.setField(displayIndex++, "Pitch (rad)", ((int)((short)((_apPacket[pIndex] & 0x00FF)<<8) + (short)(_apPacket[pIndex+1] & 0x00FF))) / 10000.0
                              + "");
                    pIndex += 2;
                    picDisplay.setField(displayIndex++, "Yaw (rad)", (((_apPacket[pIndex] & 0x00FF)<<8) + (_apPacket[pIndex+1] & 0x00FF)) / 10000.0
                              + "");
                    pIndex += 2;


                    picDisplay.setField(displayIndex++, "baro alt m (2bytes)", ((int)((short)((_apPacket[pIndex] & 0x00FF)<<8) + (short)(_apPacket[pIndex+1] & 0x00FF))) / 100.0
                          + "");
                    pIndex += 2;

                    picDisplay.setField(displayIndex++, "Wind From South (m/s)", ((int)((short)((_apPacket[pIndex] & 0x00FF)<<8) + (short)(_apPacket[pIndex+1] & 0x00FF))) / 100.0
                          + "");
                    pIndex += 2;
                    picDisplay.setField(displayIndex++, "Wind From West (m/s)", ((int)((short)((_apPacket[pIndex] & 0x00FF)<<8) + (short)(_apPacket[pIndex+1] & 0x00FF))) / 100.0
                          + "");
                    pIndex += 2;

                    pIndex += 2;        //Ignoring left RPM field
                    pIndex += 2;        //Ignoring right RPM field

                    picDisplay.setField(displayIndex++, "density Rat", (_apPacket[pIndex]&0x00FF)/200.0 + "");
                    pIndex ++;
                }

                if (rawSensorDataInPacket)
                {
                    picDisplay.setField(displayIndex++, "OAT C", (_apPacket[pIndex] & 0x00FF) + "");
                    pIndex ++;

                    picDisplay.setField(displayIndex++, "IAS m/s", (((_apPacket[pIndex] & 0x00FF)<<8) + (_apPacket[pIndex+1] & 0x00FF) - 2000.0) / 100.0
                          + "");
                    pIndex +=2;

                    picDisplay.setField(displayIndex++, "Static P (Pa)", ((int)(((_apPacket[pIndex] & 0x00FF)<<8) + (_apPacket[pIndex+1] & 0x00FF))) * 2.0
                          + "");
                    pIndex +=2;

                    pIndex +=2;     //Ignoring roll rate
                    pIndex +=2;     //Ignoring pitch rate
                    pIndex +=2;     //Ignoring yaw rate
                    pIndex +=2;     //Ignoring x accel
                    pIndex +=2;     //Ignoring y accel
                    pIndex +=2;     //Ignoring z accel
                }

                if (magnetometerInPacket)
                {
                    pIndex += 2;    //Ignoring x mag field
                    pIndex += 2;    //Ignoring y mag field
                    pIndex += 2;    //Ignoring z mag field

                    picDisplay.setField(displayIndex++, "Compass Heading (rad)", (((_apPacket[pIndex] & 0x00FF)<<8) + (_apPacket[pIndex+1] & 0x00FF)) / 10000.0
                          + "");
                    pIndex +=2;
                }

                if (aglDataInPacket)
                {
                    picDisplay.setField(displayIndex++, "AGL (m)", ((int)(((_apPacket[pIndex] & 0x00FF)<<8) + (_apPacket[pIndex+1] & 0x00FF))) / 100.0
                          + "");
                    pIndex +=2;
                }

                if (numActuatorsInPacket > 0)
                {
                    pIndex += 2*numActuatorsInPacket;
                    //Not reading any actuators
                }

                if (fuelDataInPacket)
                {
                    pIndex += 4;
                    //Not reading any fuel data
                }

                if (calcSensorDataInPacket)
                {
                    pIndex ++;      //Ignoring weight-on-wheels stuff

                    picDisplay.setField(displayIndex++, "baro alt m (3bytes)", ((int)(((_apPacket[pIndex] & 0x00FF)<<16) + ((_apPacket[pIndex+1] & 0x00FF)<<8) + (_apPacket[pIndex+2] & 0x00FF))) / 100.0 - 1000
                          + "");
                    pIndex += 3;
                }

                picDisplay.setField(displayIndex++, "Last AGL Nav Filter Good", "" + m_LastAglNavFilterGood);
            }
        }


        public static void main (String args[])
        {
            boolean replayFile = true;
            String filename = "C:/Documents and Settings/humphjc1/My Documents/wacs/Pod Logs/Dugway Logs - May 2011/5-25-2011 [Bt]/PiccoloLogs/PiccoloLog_1306336864218.log";

            if (!replayFile)
            {
                Pic_Interface pi = new Pic_Interface(123);
                //pi._apPacket = new byte[] {-96,5,69,100,-20,10,-96,71,0,5,-7,65,7,110,73,-113,-19,102,13,103,5,64,64,0,44,8,0,0,0,0,-1,-2,-32,0,6,62,14,56,-109,88,-3,80,1,83,-119,-23,0,0,5,6,0,0,0,0,0,0,-56,16,18,-70,-110,-10,0,0,0,0,0,0,1,84,0,0,-8,119,0,1,0,0,-1,-116,39,16,0,0,0,0,0,0,-1,-116,39,16,0,0,0,0,64,-1,80,88,127,1,-121,-41,11,-47,-43,-36};
                pi._apPacket = new byte[] {-96,5,69,80,-18,0,32,67,0,2,-53,-74,0,0,0,65,-1,-1,-1,21,1,-71,-11,0,-4,32,0,4,-1,-3,-1,-8,0,0,6,95,0,2,-111,29,2,50,0,116,-58,37,0,0,0,0,0,0,0,0,0,0,-75,23,8,97,-73,-90,0,65,-1,-38,-1,-94,0,23,-1,-107,-8,95,0,107,61,-84,0,0,127,1,-71,-11,-79,-110,-63,-104};
                //0,0,0,0,0,0,0,0,0,0,-75,23,8,97,-73,-90,0,65,-1,-38,-1,-94,0,23,-1,-107,-8,95,0,107,61,-84,0,0,127,1,-71,-11,-79,-110,-63,-104
                /*pi._apPacket = new byte[] {90,-91,
                                            -1,         //message type
                                            -2,
                                            80,-80,     //data flags
                                            85,-19,     //limits
                                            0,0,3,0,  //time
                                            86,-13,-96,5,  //lat
                                            69,80,-18,0, //lon
                                            32,67,0,    //height
                                            2,          //reserved
                                            -53,-74,       //satellites
                                            0,0,        //V north  0.02 m/s
                                            0,65,        //V east  -0.02 m/s
                                            -1,-1,      //V down   -0.02 m/s
                                            -1,21,      //GPS status
                                            1,-71,       //gps week
                                            -11,0,-4,32,    //gps tow
                                            0,4,      //roll
                                            -1,-3,       //pitch
                                            -1,-8,   //yaw
                                            0,0,        //baro alt
                                            6,95,        //wsouth  -12.86
                                            0,2,        //wwest  -12.86
                                            -111,29,2,50,    //RPM
                                            0,        //Density Ratio
                                            116,         //OAT
                                            -58,37,     //IAS
                                            -110,-10,   //Static P
                                            0,0,0,0,0,0,1,84,0,0,-8,119,0,1,0,0,-1,-116,
                                            39,16,      //Compass
                                            //0,5,        //AGL 0.05 m
                                            -95,18,        //AGL   412.34 m
                                            0,0,0,0,-1,-116,39,16,0,0,0,0,64,-1,80,88,127,1,-121,-41,11,-47,-43,-36};*/

                pi.showPicDisplay = true;
                pi.parseUavTelemetry();
            }
            else
            {
                Pic_Interface pi = new Pic_Interface(123, filename, 123, false, true);
                pi.showPicDisplay = true;
                (new Thread(pi)).start();

                while (true)
                {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Pic_Interface.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
}


