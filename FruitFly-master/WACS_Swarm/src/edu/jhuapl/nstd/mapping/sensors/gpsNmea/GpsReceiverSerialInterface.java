package edu.jhuapl.nstd.mapping.sensors.gpsNmea;

/*import javax.comm.*;*/

import gnu.io.*;

import java.util.*;
import java.io.*;

public class GpsReceiverSerialInterface extends NMEAReader // Notice the extended class
{
    String m_ComPortName = null;
    CommPort m_ComPort = null;
    int m_BaudRate = 4800;


    public GpsReceiverSerialInterface(String comPort, int baudRate, ArrayList al) throws Exception
    {
        super(al);
        m_ComPortName = comPort;
        m_BaudRate = baudRate;


        super.enableReading();
        // Opening Serial port COM1
        CommPortIdentifier com = null;
        try
        {
            // uses the win32com.dll library to get identifier if javax.comm.* is used
            // uses the gnu.io.*
            com = CommPortIdentifier.getPortIdentifier(m_ComPortName);
        }
        catch (NoSuchPortException nspe)
        {
            throw new Exception ("GPS Comm Port not found");
        }

        try
        {
            // 2000 number of milliseconds to wait for the port to open
            m_ComPort = com.open("GpsReceiverSerialInterface", 2000);

        }
        catch (PortInUseException piue)
        {
          throw new Exception ("GPS Comm Port already in use");
        }

        int portType = com.getPortType();
        if (portType == com.PORT_SERIAL)
        {
            SerialPort sp = (SerialPort)m_ComPort;
            try
            {
                // Settings for B&G Hydra
                sp.setSerialPortParams(m_BaudRate,
                                       SerialPort.DATABITS_8,
                                       SerialPort.STOPBITS_1,
                                       SerialPort.PARITY_NONE);
            }
            catch (UnsupportedCommOperationException ucoe)
            {
                throw new Exception ("GPS Comm Port: Unsupported Comm Operation");
            }
        }
    }

    public void read()
    {
    // Reading on Serial Port
        try
        {
            new File(".\\rawNmeaLogs").mkdirs();
            File outFile = new File (".\\rawNmeaLogs\\rawNmeaLog_" + System.currentTimeMillis() + ".txt");
            outFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

            byte[] buffer = new byte[4096];
            InputStream theInput = m_ComPort.getInputStream();
            System.out.println("Reading GPS serial port...");

            while (canRead()) // Loop
            {
                int bytesRead = theInput.read(buffer, 0, 1024);
                if (bytesRead == -1)
                  break;
                //System.out.println("Read " + bytesRead + " characters");
                // Count up to the first not null
                int nn = bytesRead;
                for (int i=0; i<Math.min(buffer.length, bytesRead); i++)
                {
                  if (buffer[i] == 0)
                  {
                    nn = i;
                    break;
                  }
                }
                byte[] toPrint = new byte[nn];
                for (int i=0; i<nn; i++)
                  toPrint[i] = buffer[i];
                // Broadcast event

                String bytePrint = "";
                for (int i = 0; i < bytesRead; i ++)
                {
                    bytePrint += buffer[i] + " ";
                }

                String toPrintString = new String (toPrint);
                writer.write(bytePrint + "\r\n");
                writer.write(toPrintString + "\r\n");
                writer.flush();
                super.fireDataRead(new NMEAEvent(this, toPrintString)); // Broadcast the event

                Thread.sleep (10);
            }

            m_ComPort.close();
            System.out.println("Stop GPS Reading serial port.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
