/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.etd;

import edu.jhuapl.nstd.piccolo.Pic_RawStreamListener;
import edu.jhuapl.nstd.swarm.ReplayNmeaEtdLogs;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.util.XCommSerialPort;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kayjl1
 */
public class Etd_Interface implements Runnable {
    String _ipAddr;
    String mask;
    int _ipPort;
    
    private Socket _oiSocket;
    private boolean _isRunning = false;
    private boolean _connected = false;
    private boolean _shutdown = false;
    private String _etdSource = "TCP";
    
    private List<EtdListener> _etdListeners = new ArrayList<EtdListener>();
    private int _etdReadPeriodMs = 500;
    
    private ReplayNmeaEtdLogs _replayNmeaEtdLogs = null;
    
    public Etd_Interface() {
        _isRunning = false;
        _connected = false;
        _etdSource = Config.getConfig().getProperty("Etd.source", "TCP");
        
        _ipAddr = Config.getConfig().getProperty("Etd.ipaddr", "192.168.0.17");
        mask = Config.getConfig().getProperty("Etd.mask", "255.255.255.0");
        _ipPort = Config.getConfig().getPropertyAsInteger("Etd.ipport", 5000);
        _etdReadPeriodMs = Config.getConfig().getPropertyAsInteger("Etd.periodMs", 500);
    }
    
    public void setReplayNmeaEtdLogs(ReplayNmeaEtdLogs replayNmeaEtdLogs) {
        _replayNmeaEtdLogs = replayNmeaEtdLogs;
    }

    public boolean isRunning()
    {
            return _isRunning;
    }

    public void forceShutdown()
    {
            _shutdown = true;
    }

    public void addEtdListener(EtdListener obj) {
            _etdListeners.add(obj);
    }
    public void removeEtdListener(EtdListener obj) {
            _etdListeners.remove(obj);
    }    

    private boolean connect() {
        try {
            System.out.println("TD Interface: **************************Connecting to IP "+_ipAddr+" at port "+_ipPort);
            _oiSocket = new Socket(_ipAddr, _ipPort);
            _connected = true;
        } catch ( Exception ex ) {
                System.out.println("Unable to connect, using TCP");
                _connected = false;
        }
        return _connected;
    }   
        
    @Override
    public void run() {
        BufferedReader tcpIn=null;
        
        File outFile;
        
        try {
            new File(".\\rawEtdLogs").mkdirs();
            outFile = new File (".\\rawEtdLogs\\rawEtdLog_" + System.currentTimeMillis() + ".txt");
            outFile.createNewFile();
        } catch (Exception e) {
            System.out.println("Error opening raw ETD log file");
            return;
        }

        try(BufferedWriter logWriter = new BufferedWriter(new FileWriter(outFile))){
        //try(FileWriter logWriter = new FileWriter(outFile)) {
            _isRunning = true;
            String simLines[] = {"Conc: 0, BKG: 121.4, PA1: 0.04, PA2: 0.05, L1: 1.05, L2: 1.06, N1: 2713, N2: 2695; LTemp:23.06,ATemp: 29.95,PACTemp: 30.92,LPow: 1.05,RefSig: 1.12,Flow: 3.23,ExtSig: 0.00,ResFQ: 1662.0,Warn: None,LastSt: [];",
                "Conc: -15, BKG: 121.4, PA1: 0.05, PA2: 0.05, L1: 1.05, L2: 1.06, N1: 2713, N2: 2695;LTemp:24.06,ATemp: 29.95,PACTemp: 30.92,LPow: 1.05,RefSig: 1.12,Flow: 3.23,ExtSig: 0.00,ResFQ: 1662.0,Warn: None,LastSt: []; ",
                "Conc: 30, BKG: 121.4, PA1: 0.1, PA2: 0.05, L1: 1.05, L2: 1.06, N1: 2713, N2: 2695;LTemp:24.06,ATemp: 29.95,PACTemp: 30.92,LPow: 1.05,RefSig: 1.12,Flow: 3.23,ExtSig: 0.00,ResFQ: 1662.0,Warn: None,LastSt: [];sampleOne: Piezo;",
                "Conc: 45, BKG: 121.4, PA1: 0.00001, PA2: 0.05, L1: 1.05, L2: 1.06, N1: 2713, N2: 2695;LTemp:24.06,ATemp: 29.95,PACTemp: 30.92,LPow: 1.05,RefSig: 1.12,Flow: 3.23,ExtSig: 0.00,ResFQ: 1662.0,Warn: None,LastSt: []; Piezo displacement out of bounds;",
                "LTemp:24.06,ATemp: 29.95,PACTemp: 30.92,LPow: 1.05,RefSig: 1.12,Flow: 3.23,ExtSig: 0.00,ResFQ: 1662.0,Warn: None,LastSt: [];",
                "LTemp:30.06,ATemp: 29.95,PACTemp: 30.92,LPow: 1.05,RefSig: 1.12,Flow: 3.23,ExtSig: 0.00,ResFQ: 1662.0,Warn: None,LastSt: []; Piezo displacement out of bounds;",
                "Conc: 60, BKG: 121.4, PA1: 0.04, PA2: 0.05, L1: 1.05, L2: 1.06, N1: 2713, N2: 2695;LTemp:30.06,ATemp: 29.95,PACTemp: 30.92,LPow: 1.05,RefSig: 1.12,Flow: 3.23,ExtSig: 0.00,ResFQ: 1662.0,Warn: None,LastSt: []; Piezo displacement out of bounds;"
            };
            int i = 0;
            while (!_shutdown) {
                long currentTime = System.currentTimeMillis();
                if(_etdSource.equalsIgnoreCase("Sim")) {
                    for (EtdListener listen : _etdListeners) {
                        String simLine = simLines[i];
                        listen.handleEtd(currentTime, simLine);
                        logWriter.write("Time: " + currentTime + ", " + simLine);
                        logWriter.newLine();
                        logWriter.flush();
                    }
                    i++;
                    if(i>=simLines.length) {
                        i=0;
                    }
                    Thread.sleep(_etdReadPeriodMs);
                } else if(_etdSource.equalsIgnoreCase("TCP")) {
                    // first we connect
                    if (!_connected && !connect()) {
                        System.out.println ("Waiting for ETD connection in read thread...");
                            Thread.sleep(1000);
                            continue;
                    }

                    tcpIn = new BufferedReader(new InputStreamReader(_oiSocket.getInputStream()));

                    String tcpLine = null;
                    try {
                        tcpLine = tcpIn.readLine();
                    } catch ( Exception ex ) {
                            System.out.println("ERROR reading etd...");
                            _connected = false;
                            continue;
                    }

                    if(logWriter!=null && tcpLine !=null) {
                        try {
                            logWriter.write("Time: " + currentTime + ", " + tcpLine);
                            logWriter.newLine();
                            logWriter.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (tcpLine != null) {
                        for (EtdListener listen : _etdListeners) {
                            listen.handleEtd(currentTime, tcpLine);
                        }
                        System.out.println("TCP: " + tcpLine);
                    } else {
                        _connected = false;
                        continue;
                    }
                    Thread.sleep(_etdReadPeriodMs);
                } else if(_etdSource.equals("File")) {
                    if(_replayNmeaEtdLogs!=null) {
                        String etdMessage = _replayNmeaEtdLogs.getEtdMessage();
                        currentTime = System.currentTimeMillis(); //update current time because may have slept during getEtdMessage
                        if(etdMessage!=null) {
                            for (EtdListener listen : _etdListeners) { 
                                listen.handleEtd(currentTime, etdMessage);
                            }
                            
                            if(logWriter!=null) {
                                try {
                                    logWriter.write("Time: " + currentTime + ", " + etdMessage);
                                    logWriter.newLine();
                                    logWriter.flush();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("Etd.source should be TCP, Sim, or File");
                }
            }
            System.out.println("ETD Shutting down...");
            _isRunning = false;
        } catch (Exception e) {
            e.printStackTrace();
            _isRunning = false;
        }
    }
}
