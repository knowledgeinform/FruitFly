/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.wacs.sensors;

import edu.jhuapl.wacs.util.XCommSerialPort;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author humphjc1
 */
public class C100Interface extends Thread {

    /**
     * Serial interface to sensor
     */
    private XCommSerialPort m_SerialPort = null;
    
    /**
     * String for port number to sensor;
     */
    private String m_PortId;
    
    /**
     * Baud rate for serial port
     */
    private final int m_BaudRate = 57600;
    
    /**
     * If true, output log information to std IO during process
     */
    private final boolean m_VERBOSE = true;
    
    /**
     * Queue for commands to send to C100
     */
    LinkedList <String> m_CommandQueue = new LinkedList <String> ();
    
    /**
     * Size of fixed-size-message input buffer
     */
    private final int MESSAGE_BUFFER_SIZE = 256;
    
    /**
     * Buffer used to read fixed-size messages from serial port
     */
    private byte[] m_MessageBuffer = new byte[MESSAGE_BUFFER_SIZE];
    
    /**
     * Number of bytes left in the message buffer after the last read that wasn't fully parsed, so left for next time
     * hoping to complete a message
     */
    int m_leftoverBytes;
    
    /**
     * Log stream for logging message traffic
     */
    DataOutputStream m_log = null;
    
    /**
     * If true, collection thread is active.
     */
    private boolean m_Running = false;
    
    /**
     * If true, the thread may be active but will not proceed
     */
    private boolean m_Paused = false;
    
    /**
     * If true, initialize has been called
     */
    private boolean m_Initialized = false;
    
    /**
     * List of listeners for message traffic (logs)
     */
    ArrayList <C100MessageListener> _listeners = new ArrayList <C100MessageListener> ();
    
    /**
     * If true, the $sys or $cfg messages were recently sent (last second or so)
     */
    private boolean sysSent;
    
    
    /**
     * Default constructor, no initialization occurs
     */
    public C100Interface ()
    {
        m_SerialPort = null;
    }
    
    /**
     * Opens a connection to the serial port and attempts to initialize the C100 interface
     * @param portID Serial port comm string
     * @return True if port opened succesfully, false otherwise
     */
    public boolean initialize (String portID)
    {
        m_PortId = portID;
        
        //Open log file
        if (m_log == null)
        {
            if (!initLog())
                return false;
        }
        
        //Connect to serial port
        if (!openPort())
            return false;
        
        
        m_Initialized = true;
        return true;
    }
    
    /**
     * Closes the serial port connection to reclaim resources
     * @return None
     */
    public void finalize ()
    {
        if (m_SerialPort.isPortGood())
            m_SerialPort.close();
        
        try {
            m_log.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    /**
     * Create log file
     * 
     * @return Boolean status of operation
     */
    private boolean initLog ()
    {
        try 
        {
            m_log = new DataOutputStream(new FileOutputStream(new File ("C100_MessageLog_" + System.currentTimeMillis() + ".log")));
            logMsg("Log started: " + (new Date().toString()));
        }
        catch (Exception e)
        {
            System.err.println ("Error: Could not initialize log file for C100 logging.");
            return false;
        }
        return true;
    }
    
    /**
     * Open connection on serial port
     * 
     * @return Boolean status of operation
     */
    private boolean openPort ()
    {
        try 
        {
            if (m_SerialPort != null)
            {
                if (m_SerialPort.isPortGood())
                    m_SerialPort.close();
                logMsg ("Existing serial connection closed.");
            }
            
            m_SerialPort = new XCommSerialPort (m_PortId, m_BaudRate);
            if (m_SerialPort == null)
                throw new Exception ("null");
            logMsg ("Connected to serial port on " + m_PortId + ", searching for C100");
        }
        catch (Exception e)
        {
            logMsg ("Error: Could not connect to serial port on \"" + m_PortId + "\" at BR: " + m_BaudRate + ".\n  Connect C100 failed.");
            return false;
        }
        return true;
    }
    
    public void pause()
    {
        m_Paused = true;
    }
    
    public void play ()
    {
        m_Paused = false;
    }
            
           
    @Override
    public void run ()
    {
        m_Running = true;
        m_leftoverBytes = 0;
        
        boolean first = true;
        while (m_Initialized == false)
        {
            if (first)
            {
                String msg = "Waiting for C100 interface to be initialized...";
                System.err.println (msg);
                notifyListeners(msg);
            }
            
            first = false;
            
            try {
                Thread.sleep (1);
            } catch (Exception e) { e.printStackTrace(); }
        }
        
        
        
        while (m_Running)
        {
            first = true;
            while (m_Paused)
            {
                if (first)
                    logMsg ("Interface paused.");
                first = false;
                
                try {
                    Thread.sleep (250);
                } catch (Exception e) { e.printStackTrace(); }
            }
            if (!first)
                logMsg ("Interface resumed live.");
            
            
            if (m_CommandQueue.size() > 0)
            {
                //Send command to C100
                String cmd = m_CommandQueue.remove();
                logMsg ("Command sent: " + cmd);
                try 
                {
                    m_SerialPort.sendBytes (cmd.getBytes());
                }
                catch (Exception e)
                {
                    logMsg ("Last command not sent successfully.");
                }
                        
                
            }
            
            //Read new data
            if (m_leftoverBytes == m_MessageBuffer.length)
            {
                //We kept a bunch of useless data that overloaded our buffer, so clear and start fresh
                m_leftoverBytes = 0;
            }
            int bytesRead = m_SerialPort.readAvailableBytes(m_MessageBuffer, m_leftoverBytes);
            
            //New bytes are available to be parsed
            if (bytesRead > 0)
            {
                //Pretend we read any leftover bytes this time
                bytesRead += m_leftoverBytes; 
            
                int bufIdx = 0;
                int newBufIdx = 0;
                
                //Loop and process all full messages in the message buffer
                while (true)  
                {
                    newBufIdx = processBuffer(bufIdx, bytesRead);
                    if (newBufIdx == bytesRead)
                    {
                        //This happens if somehow we got a message with no parseable tokens ($ signs), throw 
                        //it away and continue.
                        
                        //This could also happen if we fully read a message and it was the last message in the buffer.
                        //In that case, stop looping any continue anyway.
                        
                        //We will prevent this from occuring if we are reading non-formatted $sys responses
                        m_leftoverBytes = 0;
                        break;
                    }
                    else if (newBufIdx == bytesRead - 1)
                    {
                        //This happens if we were reading a parseable token but ran out of bytes before
                        //completing.  We should store bytes for the next iteration and read again.
                        m_leftoverBytes = (newBufIdx - bufIdx + 1);
                        for (int i = 0; i < m_leftoverBytes; i ++)
                        {
                            m_MessageBuffer[i] = m_MessageBuffer[i+bufIdx];
                        }
                        break;
                    }
                    else
                    {
                        //In this case, we fully read a message and it was not the last bytes in the buffer.
                        //Continue looping through message buffer.  Don't have to do anything special
                    }
                    bufIdx = newBufIdx;
                }
            }
            else
            {
                //If no message, sleep to free processor time
                try {
                    //Don't sleep so long if we're expecting the end of a message...
                    if (m_leftoverBytes > 0)
                        Thread.sleep (5);
                    else
                        Thread.sleep (50);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
        
    }
    
    /**
     * Process the next message in the buffer starting at the specified index
     * 
     * @param bufIdx Index to start parsing in the message buffer
     * @param bytesRead Number of bytes in the buffer that are valid
     * @return Next index in the message buffer after the message that was just parsed.
     */
    private int processBuffer (int bufIdx, int bytesRead)
    {
        //Process message received from C100
        if (m_MessageBuffer[bufIdx] == '$')
        {
            //Should be sync'ed, parse
            
            String message = new String(m_MessageBuffer, bufIdx, bytesRead - bufIdx);
            int lastIdx = message.indexOf("\r\n");
            if (lastIdx < 0)
            {
                //We don't have a full message here, so send back appropriate index indicating we
                //need more bytes
                return bytesRead - 1;
            }
            
            message = message.substring(0, lastIdx);
            //We could then parse the message if desired
            logMsg ("Message received: " + message);
            
            //Indicate where to start parsing next message
            return (bufIdx + lastIdx + 2);
        }
        else
        {
            //Definitely out of sync, clear and continue reading to get back in sync
            int nextSign = bufIdx+1;
            for (; nextSign < bytesRead; nextSign ++)
            {
                if (m_MessageBuffer[nextSign] == '$')
                    break;
            }
            
            //Log the unprocessed part of this message
            logMsg ("Message received: " + new String(m_MessageBuffer).substring(bufIdx, nextSign));
            
            if (!sysSent)   //Ignore non-formatted read-out from $sys command
                logMsg ("Last message was out of sync and not parsed.");
            
            //If nextSign < bytesRead, we found a $ character, so try to parse next time starting from there
            //If nextSign == bytesRead, there are no $ signs, so we don't have the start of a message anywhere
            //Send back value to indicate whole message is parsed
            return nextSign;
        }
    }
    
    /**
     * Log message to log file
     * 
     * @param msg String to write
     */
    private void logMsg (String msg)
    {
        //Trim endline characters to make log pretty
        while (msg.length() > 0 && (msg.endsWith("\r") || msg.endsWith("\n")))
        {
            msg = msg.substring(0, msg.length()-1);
        }
        
        //Pre-attach timestamp
        msg = new Date().toString() + " --- " + msg;
        
        try 
        {
            m_log.writeBytes(msg);
            m_log.writeBytes("\r\n");
            
            if (m_VERBOSE)
            {
                System.out.println (msg);
            }
            
            notifyListeners(msg);
        }
        catch (Exception e)
        {
            System.err.println ("Error logging message to file: " + msg);
        }
    }
    
    /**
     * Accessor for whether collection thread is active
     * 
     * @return True if thread is active, false otherwise.
     */
    public boolean isRunning ()
    {
        return m_Running;
    }
    
    /**
     * Stop the collection thread (gracefully, not immediately)
     */
    public void kill ()
    {
        m_Running = false;
    }
    
    /**
     * Generate command to prime system for specified time period and add to command queue
     * @param sec Number of seconds to prime system
     */
    public void prime (int sec)
    {
        String command = "$prime," + sec + "\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Generate command to turn dry collector on and purge sample line "for a few seconds" and add to command queue
     */
    public void collectAndPurge ()
    {
        String command = "$collect\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Generate command to turn dry collector off and add to command queue
     */
    public void collectOff ()
    {
        String command = "$collect,0\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Generate command to turn dry collector on and add to command queue
     */
    public void collectOn ()
    {
        String command = "$collect,1\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Generate command to generate a wet sample and add to command queue.
     * @param y Y-value for command arugment.  Will be forced within 1 and 4
     */
    public void sample (int y)
    {
        if (y < 1)
            y = 1;
        else if (y > 4)
            y = 4;
        
        String command = "$sample," + y + "\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Generate command to flush collector with fluid and add to command queue
     */
    public void clean ()
    {
        String command = "$clean\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Generate command to retrieve status information from C100 and add to command queue
     */
    public void status()
    {
        String command = "$status\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Generate command using raw string input
     */
    public void raw(String command)
    {
        m_CommandQueue.add(command);
    }
    
    /**
     * Sets the time to run pump 2 at beginning
     *  of a collection to purge the
     *  tubing of any liquid that might
     *  potentially spin off collector.
     *  Please note that sample
     *  generation cannot be initiated
     *  until after this time is complete.
     *  This parameter might be set to
     *  zero if no residual liquid is found
     *  to spin off when collector initially
     *  starts.
     * 
     * @param sec Time parameter
     */
    public void timep2 (int sec)
    {
        String command = "$timep2," + sec + "\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Sets the time to run pump 2 at end of
     *  sample and end of cleaning
     *  cycle to purge the tubing
     * 
     * @param sec Time parameter
     */
   public void extrap2 (int sec)
    {
        String command = "$extrap2," + sec + "\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Sets the time to run pump 1 at start of
     *  priming to fill tubing from bottle
     *  to RAC
     * 
     * @param sec Time parameter
     */
    public void priming1 (int sec)
    {
        String command = "$priming1," + sec + "\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Sets the time to run RAC dry after
     *  priming is complete to make
     *  sure no residual liquid is left on
     *  the collector surface
     * 
     * @param sec Time parameter
     */
    public void primingd (int sec)
    {
        String command = "$primingd," + sec + "\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Sets the time to run pump 2 at the end of
     *  priming to purge the tubing
     * 
     * @param sec Time parameter
     */
    public void priming2 (int sec)
    {
        String command = "$priming2," + sec + "\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Sets the clean cycle time when pump 1
     *  is on and RAC is “pulsing on
     *  and off”. During cleaning, pump
     *  1 is on until "cl" time. Once "cl”
     *  is reached from the start of the
     *  clean cycle, pump 1 will turn off
     *  and RAC and pump 2 will be on.
     *  10 seconds later, RAC is off,
     *  pump 2 on for the remaining
     *  $extrap2 time
     * 
     * @param sec Time parameter
     */
    public void cl (int sec)
    {
        String command = "$cl," + sec + "\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Sets the sampling cycle time (wet and
     *  dry)
     * 
     * @param sec Time parameter
     */
    public void s (int sec)
    {
        String command = "$s," + sec + "\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Sets the rpm speed of the C100
     * 
     * @param rpm RPM parameter
     */
    public void rs (int rpm)
    {
        String command = "$rs," + rpm + "\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Sets the rpm speed of the C100
     * 
     * @param rpm RPM parameter
     */
    public void valve (int valveNum, boolean open)
    {
        String command = "$v" + valveNum + "," + (open?1:0) + "\r";
        m_CommandQueue.add(command);
    }
    
    /**
     * Send the necessary commands to get all timing settings printed out by the C100.  Assume sending in default timing
     * will work well enough.
     */
    public void requestSettings ()
    {
        String command = "$sys\r";
        m_CommandQueue.add(command);
        sysSent = true;
        
        
        try {
            Thread.sleep(750);
        } catch (InterruptedException ex) {
            Logger.getLogger(C100Interface.class.getName()).log(Level.SEVERE, null, ex);
        }
        sysSent = false;
        
        
        command = "$verbose,1\r";
        m_CommandQueue.add(command);
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            Logger.getLogger(C100Interface.class.getName()).log(Level.SEVERE, null, ex);
        }
        command = "$cfg\r";
        m_CommandQueue.add(command);
        try {
            Thread.sleep(750);
        } catch (InterruptedException ex) {
            Logger.getLogger(C100Interface.class.getName()).log(Level.SEVERE, null, ex);
        }
        command = "$verbose,0\r";
        m_CommandQueue.add(command);
        
        sysSent = false;
    }
    
    
    public void addMessageListener (C100MessageListener listener)
    {
        _listeners.add (listener);
    }
    
    public void removeMessageListener (C100MessageListener listener)
    {
        _listeners.remove (listener);
    }
    
    private void notifyListeners (String message)
    {
        for (int i = 0; i < _listeners.size(); i ++)
        {
            _listeners.get(i).handleMessage(message);
        }
    }
}
