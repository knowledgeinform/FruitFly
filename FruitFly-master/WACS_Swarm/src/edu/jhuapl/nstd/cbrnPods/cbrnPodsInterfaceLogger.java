/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods;

import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodBytes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.swarm.util.Config;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Object to log all message traffic through the cbrnpodsinterface
 *
 * @author humphjc1
 */
public class cbrnPodsInterfaceLogger extends Thread 
{

    /**
     * Object storing message, message type, and timestamp.
     */
    private class messageData
    {
        /**
         * Message bytes
         */
        public cbrnPodBytes m_Message;

        /**
         * If true, message is a command to the rabbit.  If false, message is a message from the rabbit
         */
        public boolean m_isCommand;

        /**
         * Timestamp of message
         */
        public long m_Timestamp;

        /**
         * Create object
         *
         * @param msg
         * @param isCommand
         * @param timestamp
         */
        public messageData (cbrnPodBytes msg, boolean isCommand, long timestamp)
        {
            m_Message = msg;
            m_isCommand = isCommand;
            m_Timestamp = timestamp;
        }
    };

    /**
     * Messages to log
     */
    LinkedList <messageData> m_LogQueue = new LinkedList <messageData> ();
    
    /**
     * Lock object for log queue
     */
    private final Object m_LogQueueLock = new Object ();
    
    /**
     * True if thread is running
     */
    private boolean m_Running;

    /**
     * Log streams
     */
    HashMap <Integer, DataOutputStream> m_LogStreams = new HashMap <Integer, DataOutputStream> ();

    /**
     * Output foldername for logs
     */
    String m_OutputFolderName;
    
    
    /**
     * Create logger interface
     *
     * @param filePrefix Start of filename to log data to.  Filename will be finished with sensor type and appropriate suffix.
     */
    public cbrnPodsInterfaceLogger (String filePrefix)
    {
        try
        {
            this.setName ("WACS-CBRNInterfaceLogger");
            setDaemon(true);
            m_Running = false;


            //Set up output folder
            m_OutputFolderName = Config.getConfig().getProperty("cbrnPodsInterfaceLogger.OutputFolderName", "./MessageLogs");
            while (m_OutputFolderName.endsWith ("\\") || m_OutputFolderName.endsWith("/"))
                m_OutputFolderName = m_OutputFolderName.substring(0, m_OutputFolderName.length() - 1);
            try
            {
                File outputFolder = new File (m_OutputFolderName);
                if (!outputFolder.exists())
                    outputFolder.mkdirs();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            m_OutputFolderName += "/";
            String baselineFilename = m_OutputFolderName + filePrefix + "_" + System.currentTimeMillis() + "_";

            //For each sensor defined in the interface, open a separate log file for it.
            Field fields[] = cbrnSensorTypes.class.getFields();
            for (int i = 0; i < fields.length; i ++)
            {
                String filename = baselineFilename + fields[i].getName() + ".log";
                DataOutputStream output = new DataOutputStream (new BufferedOutputStream(new FileOutputStream (new File (filename))));

                int value = fields[i].getInt(fields[i]);
                m_LogStreams.put(new Integer (value), output);
            }

            //Start logger thread
            this.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            m_Running = false;
        }
    }

    /**
     * Log a message traveling from the CPU to the rabbit board
     *
     * @param msg
     */
    public void logMessage (cbrnPodMsg msg)
    {
        logBytes (msg, false);
    }

    /**
     * Log a command traveling from the rabbit board to the CPU
     *
     * @param msg
     */
    public void logCommand (cbrnPodCommand msg)
    {
        logBytes (msg, true);
    }

    /**
     * Log message bytes to file
     *
     * @param msg Message to log
     * @param isCommand If true, message is a command to the rabbit board
     */
    private void logBytes (cbrnPodBytes msg, boolean isCommand)
    {
        //Add to a queue to be logged later
        synchronized (m_LogQueueLock)
        {
            m_LogQueue.push (new messageData (msg, isCommand, System.currentTimeMillis()));
        }
    }

    /**
     * Gracefully end the logging thread
     */
    public void killThread ()
    {
        m_Running = false;
    }

    /**
     * Start the logging thread
     */
    public void run ()
    {
        m_Running = true;
        
        while (m_Running)
        {
            synchronized (m_LogQueueLock)
            {
                if (m_LogQueue.size() > 0)
                {
                    //When message data is available, log it
                    messageData msgData = m_LogQueue.removeLast();
                    
                    doLog (msgData.m_Message, msgData.m_isCommand, msgData.m_Timestamp);
                }
            }
            try
            {
                Thread.sleep (1000);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Logs message data to the appropriate log file
     *
     * @param messageBytes Message to be logged
     * @param isCommand If true, message is a command from the CPU to the rabbit
     * @param timestamp Timestamp of message
     */
    private void doLog (cbrnPodBytes messageBytes, boolean isCommand, long timestamp)
    {
        String logMsg = timestamp + "\t" + (isCommand?"0":"1") + "\t" + messageBytes.toLogString () + "\r\n";

        //Get output stream based on sensor type
        DataOutputStream logStream = m_LogStreams.get(messageBytes.getSensorType());

        try
        {
            //Write message to file
            logStream.writeBytes(logMsg);
            logStream.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
