/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.wacs.util;

import java.io.*;
import java.util.*;
import javax.comm.*;

/**
 *
 * @author humphjc1
 */
public class XCommSerialPort
{

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
        private final boolean outputPorts = false;

	public XCommSerialPort() throws Exception
    {
		this("COM1",9600);
	}

	public XCommSerialPort(String port, int baud) throws Exception
    {
		this (port, baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, SerialPort.FLOWCONTROL_NONE);
	}

	public XCommSerialPort(String port, int baud, int dataBits, int stopBits, int parity, int flowControl) throws Exception
    {
        _goodport = false;

        if (outputPorts)
        {
            Enumeration en = CommPortIdentifier.getPortIdentifiers();
            while (en.hasMoreElements())
            {
                System.out.println("Found comm port: "+((CommPortIdentifier)en.nextElement()).getName());
            }
        }
        _portID = CommPortIdentifier.getPortIdentifier(port);
        _serialPort = (SerialPort)_portID.open("XCommSerialPort", 2000);


        int i = 0;
        for (; i < 10; i ++)
        {
            try {
                _serialPort.setSerialPortParams(baud, dataBits, stopBits, parity);
                break;
            } catch (Exception e) {
                Thread.sleep(100);
            }
        }
        if (i == 10)
        {
            throw new Exception("Iterating 'setSerialPortParams' failed.");
        }

        _serialPort.setFlowControlMode(flowControl);
        _in = new DataInputStream(_serialPort.getInputStream());
        _out = new DataOutputStream(_serialPort.getOutputStream());
        _goodport = true;
	}

    public void close()
    {
        if (_serialPort != null)
        {
            _serialPort.close();
        }
    }

	public byte[] readAvailableBytes() 
    {
        if(isPortGood())
        {
            try
            {
                int size = _in.available();
                if (size > 0)
                {
                    byte [] rez = new byte[size];
                    _in.read(rez,0,size);
                    return rez;
                }
            } 
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
		return null;
	}
	
	public int readAvailableBytes(byte[] buffer)
	{
		return readAvailableBytes(buffer, 0);
	}
	
	public int readAvailableBytes(byte[] buffer, int offset)
	{
		if (isPortGood())
		{
			try
			{
				int numBytesToRead = _in.available();
				if (numBytesToRead > (buffer.length - offset))
				{
					numBytesToRead = buffer.length - offset;
				}
				
				_in.read(buffer, offset, numBytesToRead);
				return numBytesToRead;				 
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

        return -1;
	}

	public boolean sendBytes(byte[] command, int numBytes)
	{
		return sendBytes(command, numBytes, 0);
	}
	
	public boolean sendBytes(byte[] command, int numBytes, int offset)
	{
		if (isPortGood())
		{
			try
			{
				if (numBytes > 0)
				{
					_out.write(command, offset, numBytes);
					return true;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

    	return false;
	}

	public boolean sendBytes(byte[] command) 
    {
        if(isPortGood())
        {
            try
            {
                int size = command.length;
                if (size > 0)
                {
                    _out.write(command);
                    return true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
		return false;

	}
	
	public boolean readFixedBytes(byte[] buffer, int numBytesToRead)
	{
		return readFixedBytes(buffer, numBytesToRead, 0);
	}
	
	public boolean readFixedBytes(byte[] buffer, int numBytesToRead, int offset)
	{
		return readFixedBytes(buffer, numBytesToRead, offset, 1500);
	}
	
	public boolean readFixedBytes(byte[] buffer, int numBytesToRead, int offset, int timeout)
	{
        boolean finishedRead = false;

		try
		{
			long initTime = System.currentTimeMillis();
						
			while (!finishedRead && ((System.currentTimeMillis() - initTime) < timeout))
			{
				int numBytesAvailable = _in.available();
				if (numBytesAvailable >= numBytesToRead) 
				{
					int numBytesRead = _in.read(buffer, offset, numBytesToRead);
					finishedRead = true;					
				}
			}							
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

        return finishedRead;
	}

	public byte[] readFixedBytes(int numBytesReq) 
	{
		return readFixedBytes(numBytesReq, 1500);
	}

	// readTimeOut in milliseconds
	public byte[] readFixedBytes(int numBytesReq, int readTimeOut)
	{
		byte[] buffer = new byte[numBytesReq];
		readFixedBytes(buffer, numBytesReq);
        return buffer;
	}

    public boolean isPortGood()
    {
        return _goodport;
    }

}
