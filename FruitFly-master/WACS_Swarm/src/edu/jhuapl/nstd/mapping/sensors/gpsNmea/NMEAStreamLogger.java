/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.mapping.sensors.gpsNmea;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author RADMAPS
 */
public class NMEAStreamLogger extends Thread {
    private int m_LoggingLevel;
    
    private FileWriter m_NMEAStreamLogWriter;
    private BufferedWriter m_NMEAStreamLogBufferedWriter;    
    private Queue<String> m_NMEAStreamLogStrings;
    
    public NMEAStreamLogger(String logFolder, int loggingLevel) {
        this.setDaemon(true);
        m_LoggingLevel = loggingLevel;
        
        m_NMEAStreamLogStrings = new LinkedList<String>();
        
        if(m_LoggingLevel>=3) {
            File f = new File(logFolder);
            f.mkdir();
        }
        
        String nmeaStreamLog = logFolder + "nmeaStreamLog_" + System.currentTimeMillis() + ".csv";
        
        if(m_LoggingLevel>=3) {
            try {
                m_NMEAStreamLogWriter = new FileWriter(nmeaStreamLog);
                m_NMEAStreamLogBufferedWriter = new BufferedWriter(m_NMEAStreamLogWriter);
            } catch (Exception e) {
                m_NMEAStreamLogBufferedWriter = null;
            }
        }
    }
    
    @Override
    public void run() {
        while (true)
        {   
            writeData();
            
            try {
                Thread.sleep (50);
            } catch (InterruptedException ex) {
                System.out.println("Error while logging NMEA");
            }  
        }
    }    
    
    public void finishWriting() {
        while(!m_NMEAStreamLogStrings.isEmpty()) {
            writeData();
        }
        
        if(m_LoggingLevel>=3) {
            try {
                m_NMEAStreamLogBufferedWriter.flush();
                m_NMEAStreamLogBufferedWriter.close();
                m_NMEAStreamLogWriter.flush();
                m_NMEAStreamLogWriter.close();
            } catch (Exception e) {
                System.out.println("Error while closing NMEA Stream log file");
            }
        }
    }
    
    private void writeData() {
        if(!m_NMEAStreamLogStrings.isEmpty() && m_NMEAStreamLogBufferedWriter!=null) {
            try {
                m_NMEAStreamLogBufferedWriter.write(m_NMEAStreamLogStrings.remove());
                m_NMEAStreamLogBufferedWriter.flush();
            } catch (Exception e) {
                m_NMEAStreamLogStrings.clear();
                System.out.println("Error writing NMEA Stream log");
            }
        }    
    }
    
    public void logNMEAStream(Long currentTime, String nmeaString) {
        if(m_LoggingLevel>=3) {
            m_NMEAStreamLogStrings.add("Time: " + currentTime + ", " + nmeaString + "\n");
        }
    }    
}
