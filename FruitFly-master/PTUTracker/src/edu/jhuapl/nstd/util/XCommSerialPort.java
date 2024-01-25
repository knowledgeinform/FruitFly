package edu.jhuapl.nstd.util;

import java.io.*;
import java.util.*;
import javax.comm.*;



public class XCommSerialPort {

	private CommPortIdentifier _portID;
	private SerialPort _serialPort=null;
	private int _baudRate;
	private int _dataBits;
	private int _stopBits;
	private int _flowControl;
	private int _parity;
	private InputStream _in;
	private OutputStream _out;
    private boolean _goodport;

	public XCommSerialPort() {
		this("COM1",9600);
	}

	public XCommSerialPort(String port, int baud) {
		this (port, baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, SerialPort.FLOWCONTROL_NONE);
	}

	public XCommSerialPort(String port, int baud, int dataBits, int stopBits, int parity, int flowControl) 
    {
        _goodport = false;
		try {
			Enumeration en = CommPortIdentifier.getPortIdentifiers();
			while (en.hasMoreElements()) {
				System.out.println("Found comm port: "+((CommPortIdentifier)en.nextElement()).getName());
			}
			_portID = CommPortIdentifier.getPortIdentifier(port);
			_serialPort = (SerialPort)_portID.open("XCommSerialPort",2000);


			int i = 0;
			for (; i < 10; i ++) {
				try {
					_serialPort.setSerialPortParams(baud, dataBits, stopBits, parity);
					break;
				} catch (Exception e) {
					Thread.sleep(100);
				}
			}
			if (i == 10) {
				System.out.println("Iterating 'setSerialPortParams' failed.");
				throw new Exception("Iterating 'setSerialPortParams' failed.");
			}

			_serialPort.setFlowControlMode(flowControl);
			_in = new DataInputStream(_serialPort.getInputStream());
			_out = new DataOutputStream(_serialPort.getOutputStream());
            _goodport = true;
		}
        catch (Exception e)
        {

			e.printStackTrace();
		}
	}

    public void close()
    {
        if(isPortGood())
        {
            _serialPort.close();
        }
    }

	public byte [] readAvailableBytes() 
    {
        if(isPortGood())
        {
            try {
                int size = _in.available();
                if (size > 0) {
                    byte [] rez = new byte[size];
                    _in.read(rez,0,size);
                    return rez;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
		return null;
	}

	public boolean sendBytes(byte[] command) 
    {
        if(isPortGood())
        {
            try {
                int size = command.length;
                if (size > 0) {
                    _out.write(command);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
		return false;

	}

	public byte[] readFixedBytes(int numBytesReq) {
		return readFixedBytes(numBytesReq, 1500);
	}

	// readTimeOut in milliseconds
	public byte[] readFixedBytes(int numBytesReq, int readTimeOut) {
		try {
			long initTime = System.currentTimeMillis();
			boolean done = false;

			while (!done) {
				int size = _in.available();
				if (size >= numBytesReq) {
					byte[] rez = new byte[numBytesReq];
					_in.read(rez, 0, numBytesReq);

					size = _in.available();
					return rez;
				} else {
					if (System.currentTimeMillis() - initTime > readTimeOut)
						done = true;
					Thread.sleep(100);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

    /**
     * @return the _goodport
     */
    public boolean isPortGood()
    {
        return _goodport;
    }

}








