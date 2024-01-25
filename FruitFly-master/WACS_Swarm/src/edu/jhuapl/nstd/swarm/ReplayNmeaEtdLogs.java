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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;

/**
 *
 * @author kayjl1
 */
public class ReplayNmeaEtdLogs {
    Enumeration portList;
    CommPortIdentifier portId;
    SerialPort serialPort;
    OutputStream outputStream;
    String nmeaString;
    String etdString;
    String comPort;
    String _ipAddr;
    String mask;
    int _ipPort;
    
    ServerSocket _serverSocket;
    Socket _oiSocket;
    boolean _isRunning = false;
    boolean _connected = false;   
    
    String[] nmeaLines = null;
    String[] etdLines = null;
    String _etdMessage = null;
    String _nmeaMessage = null;
    Long _etdTime = null;
    Long _nmeaTime = null;
    
    int iNmeaLines = 0;
    int iEtdLines = 0;
    Long timeAdjustment = null;
    BufferedWriter _tcpOut = null;
    
    private static final Object lockObj = new Object();
    
    ReplayNmeaEtdLogs() {     
        _ipAddr = Config.getConfig().getProperty("Etd.ipaddr", "192.168.0.17");
        mask = Config.getConfig().getProperty("Etd.mask", "255.255.255.0");
        _ipPort = Config.getConfig().getPropertyAsInteger("Etd.ipport", 5000);
        comPort = Config.getConfig().getProperty("PiccoloAutopilotInterface.ComPort", "COM6");

        if(Config.getConfig().getProperty("Etd.source").equalsIgnoreCase("File")) {
            readEtdFromFile();
        }

        if(Config.getConfig().getProperty("Piccolo.source").equalsIgnoreCase("File")) {
            readNMEAFromFile();
        }
    }
    
    private void setTimeAdjustment() {
        long fileStartTime = Long.MAX_VALUE;
        
        if(etdLines==null && nmeaLines==null) {
            timeAdjustment = 0L;
            return;
        }
        
        if(etdLines!=null) {
            String nextEtdLine = etdLines[0];
            String[] etdSplit = nextEtdLine.split(":|,");
            fileStartTime = Math.min(Long.parseLong(etdSplit[1].trim()), fileStartTime);
        }
        
        if(nmeaLines!=null) {
            String nextNmeaLine = nmeaLines[0];
            String[] nmeaSplit = nextNmeaLine.split(":|,");
            fileStartTime = Math.min(Long.parseLong(nmeaSplit[1].trim()), fileStartTime);
        }
        timeAdjustment = System.currentTimeMillis()-fileStartTime;
    }

    public static void main(String[] args) {
        ReplayNmeaEtdLogs replayNmeaEtdLogs = new ReplayNmeaEtdLogs();
        
        replayNmeaEtdLogs.sendEtdAndNmea();
    }
    
    private void updateEtd() {
        synchronized(lockObj) {
            if(timeAdjustment==null) {
                setTimeAdjustment();
            }
        }
        
        if(etdLines!=null && iEtdLines<etdLines.length) {
            String nextEtdLine = etdLines[iEtdLines];
            String[] etdSplit = nextEtdLine.split(":|,");
            _etdMessage = nextEtdLine.substring(nextEtdLine.indexOf(",")+1, nextEtdLine.length()).trim();
            _etdTime = Long.parseLong(etdSplit[1].trim());
            iEtdLines++;  
        } else {
            _etdMessage = null;
            _etdTime = null;
        }
    }
    
    private void updateNmea() {
        synchronized(lockObj) {
            if(timeAdjustment==null) {
                setTimeAdjustment();
            }
        }
        
        if(nmeaLines!=null && iNmeaLines<nmeaLines.length) {
            String nextNmeaLine = nmeaLines[iNmeaLines];
            String[] nmeaSplit = nextNmeaLine.split(":|,");
            _nmeaMessage = nextNmeaLine.substring(nextNmeaLine.indexOf(",")+1, nextNmeaLine.length()).trim();
            _nmeaTime = Long.parseLong(nmeaSplit[1].trim());
            iNmeaLines++;        
        } else {
            _nmeaMessage = null;
            _nmeaTime = null;
        }
    }
    
    public String getEtdMessage() {
        updateEtd();
        if(_etdTime!=null) {
            long nextTime = _etdTime+timeAdjustment;

            try {
                Thread.sleep(Math.max(nextTime-System.currentTimeMillis(), 0));
            } catch(Exception e) {
                System.out.println("Error with Thread.sleep");
            }
            
        }
        
        return _etdMessage;
    }
    
    public String getNmeaMessage() {
        updateNmea();
        if(_nmeaTime!=null) {
            long nextTime = _nmeaTime+timeAdjustment;
            try {
                Thread.sleep(Math.max(nextTime-System.currentTimeMillis(), 0));
            } catch(Exception e) {
                System.out.println("error with Thread.sleep");
            }
        }
        
        return _nmeaMessage;
    }
    
    public void sendEtdAndNmea() {  
        updateEtd();
        updateNmea();
        
        while(_etdTime!=null || _nmeaTime!=null) {
            long nextTime;
            if(_etdTime==null) {
                nextTime = _nmeaTime;
            } else if (_nmeaTime==null) {
                nextTime = _etdTime;
            } else {
                nextTime = Math.min(_nmeaTime, _etdTime);
            }
            
            nextTime = nextTime + timeAdjustment;
            long sleepTime = nextTime - System.currentTimeMillis();
            
            try {
                Thread.sleep(Math.max(nextTime - System.currentTimeMillis(), 0));
            } catch (Exception e) {
                System.out.println("Error with Thread.sleep");
            }
            
            if(_nmeaTime==null) {
                sendEtdMessage(_etdMessage);
                updateEtd();
            } else if (_etdTime==null) {
                sendNmeaMessage(_nmeaMessage);
                updateNmea();
            }else if(_nmeaTime<=_etdTime) {
                sendNmeaMessage(_nmeaMessage);
                updateNmea();
            } else {
                sendEtdMessage(_etdMessage);
                updateEtd();
            }
        }
        
        try {
            _tcpOut.close();
        } catch(Exception e) {
            System.out.println("Error closing BufferedWriter");
        }
    }
    
    
    private void readNMEAFromFile() {
        try {
            String nmeaFilename = Config.getConfig().getProperty("Piccolo.replayFilename");
            nmeaString = new String(Files.readAllBytes(Paths.get(nmeaFilename)));
            System.out.println("Reading NMEA File: " + nmeaFilename);
            nmeaLines = nmeaString.split("\\r?\\n");
        } catch(Exception e) {
           System.out.println("Error reading GPS log for replay");
        }
    }
    
    private void readEtdFromFile() {
        try {
            String etdFilename = Config.getConfig().getProperty("Etd.replayFilename");
            etdString = new String(Files.readAllBytes(Paths.get(etdFilename)));
            System.out.println("Reading ETD File: " + etdFilename);
            etdLines = etdString.split("\\r?\\n");
        } catch(Exception e) {
           System.out.println("Error reading GPS log for replay");
        }
    }
    
    private boolean connect() {
        try {
            System.out.println("TD Interface: **************************Connecting to IP "+_ipAddr+" at port "+_ipPort);
            _serverSocket = new ServerSocket(_ipPort);
            _oiSocket = _serverSocket.accept();
            _tcpOut = new BufferedWriter(new OutputStreamWriter(_oiSocket.getOutputStream()));
            _connected = true;
        } catch ( Exception ex ) {
                System.out.println("Unable to connect, using TCP");
                _connected = false;
        }
        return _connected;
    }   
        
    private void sendEtdMessage(String etdMessage) {
        System.out.println(etdMessage);
        while (!_connected && !connect()) {
            System.out.println ("Waiting for ETD connection in read thread...");
            
            try{
                Thread.sleep(100);
            } catch (Exception e) {
                System.out.println("Error during Thread.sleep");
            }
        }

        String tcpLine = null;
        try {
            _tcpOut.write(etdMessage + "\n");
            _tcpOut.flush();
        } catch ( Exception ex ) {
                System.out.println("ERROR writing etd...");
                _connected = false;
                
                try {
                    _tcpOut.close();
                } catch(Exception e) {
                    System.out.println("Error closing BufferedWriter");
                }
                return;
        }        
    }
    
    private void sendNmeaMessage(String nmeaMessage) {
        System.out.println(nmeaMessage);
        portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                 if (portId.getName().equals(comPort)) {
                    try {
                        serialPort = (SerialPort)
                            portId.open("ReplayNMEA", 2000);
                    } catch (PortInUseException e) {}
                    try {
                        outputStream = serialPort.getOutputStream();
                    } catch (IOException e) {}
                    try {
                        serialPort.setSerialPortParams(9600,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                    } catch (UnsupportedCommOperationException e) {}
                    try {
                        //while(nmeaMessage.) {
                            outputStream.write((nmeaMessage + "\r\n").getBytes());
                            Thread.sleep(50);
                        //}
                    } catch(Exception e) {
                        System.out.println("Error writing GPS for spoofing");
                    }
                }
            }
        }        
    }
}

