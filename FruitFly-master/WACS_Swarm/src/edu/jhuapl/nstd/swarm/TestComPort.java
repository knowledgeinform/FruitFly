/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm;

import edu.jhuapl.nstd.swarm.util.Config;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;

/**
 *
 * @author FruitFly
 */
public class TestComPort {
    static Enumeration portList;
    static CommPortIdentifier portId;
    static SerialPort serialPort;
    static InputStream inputStream;
    static String messageString;
    

    public static void main(String[] args) {

        String comPort = "COM6";
        
        portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                 if (portId.getName().equals(comPort)) {
                    try {
                        serialPort = (SerialPort)
                            portId.open("SpofGPS", 2000);
                    } catch (PortInUseException e) {}
                    try {
                        inputStream = serialPort.getInputStream();
                    } catch (IOException e) {}
                    try {
                        serialPort.setSerialPortParams(9600,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                    } catch (UnsupportedCommOperationException e) {}
                    byte[] readBuffer = new byte[60];

                    try {
                        while (inputStream.available() > 0) {
                            int numBytes = inputStream.read(readBuffer);
                            System.out.print(new String(readBuffer));
                        }
                        
                    } catch (IOException e) {
                        System.out.println(e);
                    }
                 }
            }
        }    
    }
}
