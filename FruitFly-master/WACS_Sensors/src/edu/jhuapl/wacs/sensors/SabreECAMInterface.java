/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.wacs.sensors;

import edu.jhuapl.wacs.util.XCommSerialPort;

/**
 *
 * @author humphjc1
 */
public class SabreECAMInterface {

    /**
     * Serial interface to sensor
     */
    private XCommSerialPort m_SerialPort;
    
    /**
     * String for port number to sensor;
     */
    private String m_PortId;
    
    /**
     * Baud rate for serial port
     */
    private final int m_BaudRate = 57600;
    
    /**
     * If true, output log information during process
     */
    private final boolean m_VERBOSE = true;
    
    
    
    
    /**
     * Wake-up #blade command
     */
    private final String m_WakeUpCommand = "#blade\r";
    
    /**
     * Wake-up #blade command bytes
     */
    private final byte[] m_WakeUpCommandBytes = m_WakeUpCommand.getBytes();
    
    /**
     * Password input command, can be overridden through constructor
     */
    private String m_PasswordCommand = "19256saf\r";
    
    /**
     * Password input command bytes
     */
    private byte[] m_PasswordCommandBytes = m_PasswordCommand.getBytes();
    
    /**
     * Size of fixed-size-message input buffer
     */
    private final int MESSAGE_BUFFER_SIZE = 256;
    
    /**
     * Buffer used to read fixed-size messages from serial port
     */
    private byte[] m_MessageBuffer = new byte[MESSAGE_BUFFER_SIZE];
    
    
    /**
     * Default constructor, no initialization occurs
     */
    public SabreECAMInterface ()
    {
        m_SerialPort = null;
    }
    
    /**
     * Constructor, specifies alternate password for SabreECAM connection
     * 
     * @param password Password to connect to SabreECAM unit
     */
    public SabreECAMInterface (String password)
    {
        m_SerialPort = null;
        
        m_PasswordCommand = password;
        if (!m_PasswordCommand.endsWith("\r"))
            m_PasswordCommand += "\r";
        m_PasswordCommandBytes = m_PasswordCommand.getBytes();
    }
    
    /**
     * Opens a connection to the serial port and attempts to initialize the SabreECAM interface
     * @param portID Serial port comm string
     * @return True if port opened succesfully, false otherwise
     */
    public boolean initialize (String portID)
    {
        m_PortId = portID;
        
        //Connect to serial port
        if (!openPort())
            return false;
        
        //Initialize the ECAM unit
        if (!init())
            return false;
        
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
    }

    private boolean openPort ()
    {
        try 
        {
            m_SerialPort = new XCommSerialPort (m_PortId, m_BaudRate);
            if (m_SerialPort == null)
                throw new Exception ("null");
            if (m_VERBOSE)
                System.out.println ("Connected to serial port on " + m_PortId + ", searching for SabreECAM");
        }
        catch (Exception e)
        {
            System.err.println ("Error: Could not connect to serial port on \"" + m_PortId + "\" at BR: " + m_BaudRate + ".\n  Connect SabreECAM failed.");
            return false;
        }
        return true;
    }
    
    private boolean init ()
    {
        if (!m_SerialPort.isPortGood())
            return false;
            
        //send #blade to wake-up unit
        m_SerialPort.sendBytes (m_WakeUpCommandBytes);
        
        //should receive "Login: " if successfully woken up, ">" if already connected, nothing if fail
        if (getNonEndlineBytes (m_MessageBuffer, 7))
        {
            //Compare text that was received
            String recvd = new String(m_MessageBuffer, 0, 7);
            if (recvd.compareToIgnoreCase("Login: ") != 0)
            {
                didNotConnect();
                return false;
            }
            
            if (m_VERBOSE)
            {
                System.out.println ("Connected to SabreECAM device");
                System.out.println ("SabreECAM device has been woken up");
            }
            
            
            
            //send password
            m_SerialPort.sendBytes(m_PasswordCommandBytes);
            
            //should receive ">" prompt if success, login prompt again if fail
            if (getNonEndlineBytes (m_MessageBuffer, 1))
            {
                recvd = new String(m_MessageBuffer, 0, 1);
                if (recvd.compareToIgnoreCase(">") != 0)
                {
                    didNotLogin();
                    return false;
                }

                if (m_VERBOSE)
                {
                    System.out.println ("Password accepted on SabreECAM device, logged in");
                }
            }
            else 
            {
                didNotLogin();
                return false;
            }
        }
        else if (getNonEndlineBytes (m_MessageBuffer, 1))
        {
            String recvd = new String(m_MessageBuffer, 0, 1);
            if (recvd.compareToIgnoreCase(">") != 0)
            {
                didNotConnect();
                return false;
            }
            
            if (m_VERBOSE)
            {
                System.out.println ("Connected to SabreECAM device");
                System.out.println ("SabreECAM device was already logged-in");
            }
        }
        else 
        {   
            didNotConnect();
            return false;
        }
        
        
        //send sync time
        //should receive nothing if success, nothing if fail
        //set reporting intervals via GA/GC
        //receive?
        //set spectral logging window?  LS?
        //receive?
        
        return true;
    }
    
    /**
     * Print function called when unable to connect to SabreECAM, clears read buffer
     * @return 
     */
    private void didNotConnect()
    {
        //Clear the read buffer
        m_SerialPort.readAvailableBytes();
        
        if (m_VERBOSE)
        {
            System.out.println ("Did not receive log-in prompt - not connected to SabreECAM device");
        }
    }
    
    /**
     * Print function called when unable to submit password to SabreECAM, clears read buffer
     * @return 
     */
    private void didNotLogin()
    {
        //Clear the read buffer
        m_SerialPort.readAvailableBytes();
        
        if (m_VERBOSE)
        {
            System.out.println ("SabreECAM device rejected password");
        }
    }
    
    private boolean getNonEndlineBytes(byte[] m_MessageBuffer, int count) 
    {
        
        boolean complete = false;
        
        while (!complete)
        {
            if (m_SerialPort.readFixedBytes(m_MessageBuffer, count))
            {
                int offset = 0;
                //Prompt may have been preceeded by /r/n characters, detect where they end
                while (m_MessageBuffer[offset] == 13 || m_MessageBuffer[offset] == 10 && offset <= count)
                    offset++;
                
                if (offset < count)
                {
                   //There was atleast one non-endline character received. 'offset' is the index of the first such character 
                
                    //We'll add the rest ('offset' more) of the prompt to the end (after 'count') and have 'count' good bytes read in
                    if (offset > 0)
                    {
                        m_SerialPort.readFixedBytes(m_MessageBuffer, offset, count);
                    
                        //shift good bytes back to zero
                        for (int i = 0; i < count; i ++)
                            m_MessageBuffer[i] = m_MessageBuffer[i+offset];
                    }
                    
                    complete = true;
                }
                else
                {
                    //We only got /r or /n characters, so we need to read the full amount again
                }
            }
            else
            {
                return false;
            }
        }
        return true;
    }
    
    
    
    public static void main(String args[]) 
    {
        SabreECAMInterface intr = new SabreECAMInterface();
        if (!intr.initialize("COM1"))
        {
            System.out.println ("Did not initialize SabreECAMInterface");
        }
    
    }
}
