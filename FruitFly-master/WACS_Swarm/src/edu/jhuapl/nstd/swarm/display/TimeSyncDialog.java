/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.jlib.math.DateTime;
import edu.jhuapl.jlib.math.Time;
import edu.jhuapl.jlib.net.IPAddress;
import edu.jhuapl.nstd.piccolo.Pic_Interface;
import edu.jhuapl.nstd.piccolo.Pic_Telemetry;
import edu.jhuapl.nstd.piccolo.Pic_TelemetryListener;
import edu.jhuapl.nstd.swarm.util.MathUtils;
import edu.jhuapl.nstd.swarm.wacs.SatCommMessageArbitrator;
import edu.jhuapl.nstd.util.Colors;
import edu.jhuapl.nstd.util.SystemTime;
import gnu.io.CommPortIdentifier;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

/**
 *
 * @author humphjc1
 */
public class TimeSyncDialog extends javax.swing.JFrame 
{
    public static final int LOCATION_POD = 0;
    public static final int LOCATION_GCS = 1;
    
    private static final int SYNCOPTION_NONE = 0;
    private static final int SYNCOPTION_TIMESERVER = 1;
    private static final int SYNCOPTION_PICCOLO = 2;
    private static final int SYNCOPTION_GPS = 3;
    private static final int SYNCOPTION_LOCALTIME = 4;

    private static final int STATUS_NOTCONNECTED = 0;
    private static final int STATUS_NOTSYNCED = 1;
    private static final int STATUS_SYNCED = 2;
    private static final int STATUS_PENDING = 3;
    private static final int STATUS_REMOTE = 4;
    public static final int STATUS_SYNCCOMPLETE = 5;
    
    private int m_MyLocationType;
    private InetAddress m_MulticastIpAddr;
    private int m_MulticastSendPort;
    private int m_MulticastRecvPort;
    private int m_MulticastPeriod_ms;
    private TimeSyncDialog m_DialogPtr;
    
    private int m_MyLocationStatus;
    private long m_MyLocationTimestampMs;
    private int m_RemoteLocationStatus;
    private long m_RemoteLocationTimestampMs;
    
    private int m_TimeSyncTimeDelayMs;
    private int m_TimeSyncTimeCountdownMs;
    private boolean m_AllowRemoteSync;
    private boolean m_ForbidTimeServerSync;
    private boolean m_ForbidPiccoloSync;
    private boolean m_ForbidGpsSync;
    private int m_SyncOption;
    private String m_UseTimeServerIpAddr;
    private String m_UsePiccoloComPort;
    private String m_UseGpsComPort;
    private int m_WindowCornerX;
    private int m_WindowCornerY;
    
    private boolean m_TimeSyncThreadRunning;
    private boolean m_ConnectedToPiccolo;
    //private Thread m_PiccoloConnectionThread = null;
    private Pic_Interface m_PicInterface = null;
    
    private DecimalFormat m_DecFormat1 = new DecimalFormat ("0.#");
    
    
    /**
     * Creates new form TimeSyncDialog
     */
    public TimeSyncDialog(final int locationIdentifier) 
    {
        initComponents();
        m_DialogPtr = this;

        m_MyLocationType = locationIdentifier;
        if (m_MyLocationType == 0)
        {
            m_MyLocationOutputLabel.setText ("WACS Pods");
            m_RemoteLocationOutputLabel.setText("WACS GCS");
        }
        else //if (m_MyLocationType == 1)
        {
            m_MyLocationOutputLabel.setText ("WACS GCS");
            m_RemoteLocationOutputLabel.setText("WACS Pods");
        }
        m_MyLocationStatusLabel.setBackground(Colors.LIGHT_RED);
        m_RemoteLocationStatusLabel.setBackground(Colors.LIGHT_RED);
        m_ConnectedToPiccolo = false;
        
        readConfig();
        initializeDialog ();
        
        new TimeSyncThread().start();
        new DialogUpdateThread().start();
        new NetworkSendThread().start();
        new NetworkRecvThread().start();
    }
    
    private void readConfig ()
    {
        try
        {
            File file = new File ("./config/timeSyncDialogConfig.txt");
            BufferedReader reader = new BufferedReader (new FileReader (file));
            
            String readLine = reader.readLine();
            while (readLine != null)
            {
                String tokens[] = readLine.split ("\\W+");
                
                if (tokens[0].equals ("MulticastIpAddr"))
                {
                    try
                    {
                        byte list[] = new byte [4];
                        for (int i = 0; i < 4; i ++)
                            list[i] = (byte)Integer.parseInt(tokens[i+1]);
                        m_MulticastIpAddr = Inet4Address.getByAddress(list);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else if (tokens[0].equals ("MulticastSendPort"))
                {
                    m_MulticastSendPort = Integer.parseInt(tokens[1]);
                }
                else if (tokens[0].equals ("MulticastRecvPort"))
                {
                    m_MulticastRecvPort = Integer.parseInt(tokens[1]);
                }
                else if (tokens[0].equals ("MulticastPeriod_ms"))
                {
                    m_MulticastPeriod_ms = Integer.parseInt(tokens[1]);
                }
                else if (tokens[0].equals ("AllowRemoteSync"))
                {
                    m_AllowRemoteSync = Boolean.parseBoolean(tokens[1]);
                }
                else if (tokens[0].equals ("SyncOption"))
                {
                    m_SyncOption = Integer.parseInt(tokens[1]);
                }
                else if (tokens[0].equals ("UseTimeServerIpAddr"))
                {
                    m_UseTimeServerIpAddr = tokens[1] + "." + tokens[2] + "." + tokens[3] + "." + tokens[4];
                }
                else if (tokens[0].equals ("UsePiccoloComPort"))
                {
                    m_UsePiccoloComPort = tokens[1];
                }
                else if (tokens[0].equals ("UseGpsComPort"))
                {
                    m_UseGpsComPort = tokens[1];
                }
                else if (tokens[0].equals ("UseLocalTimeDelayMs"))
                {
                    m_TimeSyncTimeDelayMs = Integer.parseInt(tokens[1]);
                }
                else if (tokens[0].equals ("ForbidTimeServerSync"))
                {
                    m_ForbidTimeServerSync = Boolean.parseBoolean(tokens[1]);
                }
                else if (tokens[0].equals ("ForbidPiccoloSync"))
                {
                    m_ForbidPiccoloSync = Boolean.parseBoolean(tokens[1]);
                }
                else if (tokens[0].equals ("ForbidGpsSync"))
                {
                    m_ForbidGpsSync = Boolean.parseBoolean(tokens[1]);
                }
                else if (tokens[0].equals ("WindowCornerX"))
                {
                    m_WindowCornerX = Integer.parseInt(tokens[1]);
                }
                else if (tokens[0].equals ("WindowCornerY"))
                {
                    m_WindowCornerY = Integer.parseInt(tokens[1]);
                }
                
                readLine = reader.readLine();
            }
            reader.close();
        }
        catch (Exception e)
        {
            System.err.println ("No config file found for time sync dialog, using defaults");

            try
            {
                byte list[] = new byte[] {(byte)224,(byte)10,(byte)100,(byte)13};
                m_MulticastIpAddr = Inet4Address.getByAddress(list);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            
            m_MulticastSendPort = 57190;
            m_MulticastRecvPort = 57191;
            m_MulticastPeriod_ms = 250;

            m_AllowRemoteSync = true;
            m_SyncOption = SYNCOPTION_NONE;
            m_UseTimeServerIpAddr = "192.168.1.1";
            m_UsePiccoloComPort = "COM1";
            m_UseGpsComPort = "COM1";
            m_TimeSyncTimeDelayMs = 3000;
            m_TimeSyncTimeCountdownMs = 0;
            m_WindowCornerX = 50;
            m_WindowCornerY = 50;
            
            m_ForbidTimeServerSync = true;
            m_ForbidPiccoloSync = true;
            m_ForbidGpsSync = true;
        }
        
        if (m_ForbidTimeServerSync && m_SyncOption == SYNCOPTION_TIMESERVER)
            m_SyncOption = SYNCOPTION_NONE;
        if (m_ForbidPiccoloSync && m_SyncOption == SYNCOPTION_PICCOLO)
            m_SyncOption = SYNCOPTION_NONE;
        if (m_ForbidGpsSync && m_SyncOption == SYNCOPTION_GPS)
            m_SyncOption = SYNCOPTION_NONE;
    }
    
    private void saveConfig()
    {
        try
        {
            File file = new File ("./config/timeSyncDialogConfig.txt");
            if (!file.exists())
                file.createNewFile();
            
            BufferedWriter writer = new BufferedWriter (new FileWriter (file));
            writer.write("MulticastIpAddr = " + m_MulticastIpAddr.getHostAddress() + "\r\n");
            writer.write("MulticastSendPort = " + m_MulticastSendPort + "\r\n");
            writer.write("MulticastRecvPort = " + m_MulticastRecvPort + "\r\n");
            writer.write("MulticastPeriod_ms = " + m_MulticastPeriod_ms + "\r\n");
            writer.write("AllowRemoteSync = " + m_AllowRemoteSync + "\r\n");
            writer.write("SyncOption = " + m_SyncOption + "\r\n");
            writer.write("UseTimeServerIpAddr = " + m_UseTimeServerIpAddr + "\r\n");
            writer.write("UsePiccoloComPort = " + m_UsePiccoloComPort + "\r\n");
            writer.write("UseGpsComPort = " + m_UseGpsComPort + "\r\n");
            writer.write("UseLocalTimeDelayMs = " + m_TimeSyncTimeDelayMs + "\r\n");
            writer.write("ForbidTimeServerSync = " + m_ForbidTimeServerSync + "\r\n");
            writer.write("ForbidPiccoloSync = " + m_ForbidPiccoloSync + "\r\n");
            writer.write("ForbidGpsSync = " + m_ForbidGpsSync + "\r\n");
            writer.write("WindowCornerX = " + m_WindowCornerX + "\r\n");
            writer.write("WindowCornerY = " + m_WindowCornerY + "\r\n");
            
            writer.flush();
            writer.close();
        }
        catch (Exception e)
        {
            System.err.println ("Could not save time sync dialog configuration file");
            e.printStackTrace();
        }
    }
    
    private void initializeDialog ()
    {
        m_MyLocationStatus = STATUS_NOTSYNCED;
        m_MyLocationTimestampMs = -1;
        m_RemoteLocationStatus = STATUS_NOTCONNECTED;
        m_RemoteLocationTimestampMs = -1;
                
        m_AllowRemoteSyncCheckBox.setSelected(m_AllowRemoteSync);
        int syncOption = m_SyncOption;
        m_AllowRemoteSyncCheckBoxActionPerformed (null);  //This might clear the syncOption value
        m_SyncOption = syncOption;
        
        m_UseTimeServerCheckBox.setSelected(m_SyncOption == SYNCOPTION_TIMESERVER);
        m_UseTimeServerCheckBoxActionPerformed(null);
        m_UsePiccoloCheckbox.setSelected(m_SyncOption == SYNCOPTION_PICCOLO);
        m_UsePiccoloCheckboxActionPerformed(null);
        m_UseGPSCheckBox.setSelected(m_SyncOption == SYNCOPTION_GPS);
        m_UseGPSCheckBoxActionPerformed(null);
        m_UseLocalTimeCheckBox.setSelected(m_SyncOption == SYNCOPTION_LOCALTIME);
        m_UseLocalTimeCheckBoxActionPerformed(null);

        this.setLocation(m_WindowCornerX, m_WindowCornerY);
    }
    
    private void parseDialogOpts ()
    {
        m_AllowRemoteSync = m_AllowRemoteSyncCheckBox.isSelected();
        
        if (m_UseTimeServerCheckBox.isSelected())
            m_SyncOption = SYNCOPTION_TIMESERVER;
        m_UseTimeServerIpAddr = m_UseTimeServerInputBox.getText();
    
        try
        {
            if (m_UsePiccoloCheckbox.isSelected())
                m_SyncOption = SYNCOPTION_PICCOLO;
            m_UsePiccoloComPort = m_UsePiccoloComInputBox.getSelectedItem().toString();
        }
        catch (Exception e)
        {}
    
        try
        {
            if (m_UseGPSCheckBox.isSelected())
                m_SyncOption = SYNCOPTION_GPS;
            m_UseGpsComPort = m_UseGPSInputBox.getSelectedItem().toString();
        }
        catch (Exception e)
        {}

        if (m_UseLocalTimeCheckBox.isSelected())
            m_SyncOption = SYNCOPTION_LOCALTIME;
        
        m_WindowCornerX = this.getX();
        m_WindowCornerY = this.getY();
    }
    
    private void startWacsCloseTimeSync()
    {
        parseDialogOpts ();
        saveConfig();
        
        try
        {
            String batchFilename = "TimeSyncStartWacs.bat";
            BufferedReader batchReader = new BufferedReader(new FileReader(batchFilename));
            
            String filenameReplacement;
            if (m_MyLocationType == LOCATION_POD)
                filenameReplacement = "wacsagentLog_";
            else //if (m_MyLocationType == LOCATION_GCS)
                filenameReplacement = "wacsdisplayLog_";
            DateTime currDateTime = new DateTime(System.currentTimeMillis(), Time.MILLISECONDS);
            String dateText = currDateTime.format("yyyy-MM-dd_HH-mm-ss");
            filenameReplacement += dateText + ".txt";
            
            boolean error = true;
            String runLine = batchReader.readLine();
            if (runLine != null)
            {
                runLine = runLine.replaceAll("LOGFILENAMETXT", filenameReplacement);
                
                Process pid = Runtime.getRuntime().exec(runLine);
                System.out.println ("WACS software succesfully started.");
                error = false;
            }
            
            if (error)
            {
                JOptionPane.showMessageDialog (m_DialogPtr, "Don't know how to start WACS software on this computer.\r\n  Press \'OK\', then start WACS software manually.", "Start WACS Software", JOptionPane.WARNING_MESSAGE);
            }
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog (m_DialogPtr, "Don't know how to start WACS software on this computer.\r\n  Press \'OK\', then start WACS software manually.", "Start WACS Software", JOptionPane.WARNING_MESSAGE);
        }
        
        
        System.exit(0);
    }
    
    private void fillComboBoxWithCommPorts (JComboBox box)
    {
        box.removeAllItems();
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while ( portEnum.hasMoreElements() ) 
        {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            box.addItem(portIdentifier.getName());
        }    
    }
    
    private void enableTimeSyncCheckboxes (boolean enable)
    {
        m_UseTimeServerCheckBox.setEnabled(enable && !m_ForbidTimeServerSync);
        m_UseTimeServerCheckBoxActionPerformed(null);
        m_UsePiccoloCheckbox.setEnabled(enable && !m_ForbidPiccoloSync);
        m_UsePiccoloCheckboxActionPerformed(null);
        m_UseGPSCheckBox.setEnabled(enable && !m_ForbidGpsSync);
        m_UseGPSCheckBoxActionPerformed(null);
        m_UseLocalTimeCheckBox.setEnabled(enable);
        m_UseLocalTimeCheckBoxActionPerformed(null);
    }
    
    private void unselectTimeSyncCheckboxes (int skipOpt)
    {
        if (skipOpt != SYNCOPTION_TIMESERVER)
        {
            m_UseTimeServerCheckBox.setSelected(false);
            m_UseTimeServerCheckBoxActionPerformed(null);
        }
        if (skipOpt != SYNCOPTION_PICCOLO)
        {
            m_UsePiccoloCheckbox.setSelected(false);
            m_UsePiccoloCheckboxActionPerformed(null);
        }
        if (skipOpt != SYNCOPTION_GPS)
        {
            m_UseGPSCheckBox.setSelected(false);
            m_UseGPSCheckBoxActionPerformed(null);
        }
        if (skipOpt != SYNCOPTION_LOCALTIME)
        {
            m_UseLocalTimeCheckBox.setSelected(false);
            m_UseLocalTimeCheckBoxActionPerformed(null);
        }
    }
    
    private class DialogUpdateThread extends Thread
    {
        public DialogUpdateThread ()
        {
            setDaemon (true);
        }
        
        @Override
        public void run ()
        {
            int lastMyLocationStatus = -1;
            int lastRemoteLocationStatus = -1;
            
            while (true)
            {   
                if (lastMyLocationStatus != m_MyLocationStatus)
                {
                    lastMyLocationStatus = m_MyLocationStatus;
                    if (m_MyLocationStatus == STATUS_NOTSYNCED)
                    {
                        m_MyLocationStatusLabel.setBackground(Colors.LIGHT_RED);
                        m_MyLocationStatusLabel.setText("NOT TIME-SYNCED");
                    }
                    else if (m_MyLocationStatus == STATUS_PENDING)
                    {
                        m_MyLocationStatusLabel.setBackground(Color.ORANGE);
                        m_MyLocationStatusLabel.setText("PENDING SYNC");
                    }
                    else if (m_MyLocationStatus == STATUS_SYNCED)
                    {
                        m_MyLocationStatusLabel.setBackground(Color.GREEN);
                        m_MyLocationStatusLabel.setText("TIME-SYNCED");
                    }
                    else if (m_MyLocationStatus == STATUS_REMOTE)
                    {
                        m_MyLocationStatusLabel.setBackground(Color.YELLOW);
                        m_MyLocationStatusLabel.setText("REMOTE SYNC");
                    }
                }
                
                if (lastRemoteLocationStatus != m_RemoteLocationStatus)
                {
                    lastRemoteLocationStatus = m_RemoteLocationStatus;
                    if (m_RemoteLocationStatus == STATUS_NOTCONNECTED)
                    {
                        m_RemoteLocationStatusLabel.setBackground(Colors.LIGHT_RED);
                        m_RemoteLocationStatusLabel.setText("NOT CONNECTED");
                    }
                    else if (m_RemoteLocationStatus == STATUS_NOTSYNCED)
                    {
                        m_RemoteLocationStatusLabel.setBackground(Colors.LIGHT_RED);
                        m_RemoteLocationStatusLabel.setText("NOT TIME-SYNCED");
                    }
                    else if (m_RemoteLocationStatus == STATUS_PENDING)
                    {
                        m_RemoteLocationStatusLabel.setBackground(Color.ORANGE);
                        m_RemoteLocationStatusLabel.setText("PENDING SYNC");
                    }
                    else if (m_RemoteLocationStatus == STATUS_SYNCED)
                    {
                        m_RemoteLocationStatusLabel.setBackground(Color.GREEN);
                        m_RemoteLocationStatusLabel.setText("TIME-SYNCED");
                    }
                    else if (m_RemoteLocationStatus == STATUS_REMOTE)
                    {
                        m_RemoteLocationStatusLabel.setBackground(Color.YELLOW);
                        m_RemoteLocationStatusLabel.setText("REMOTE SYNC");
                    }
                }
                
                m_MyLocationTimestampMs = System.currentTimeMillis();
                DateTime myTime = new DateTime(m_MyLocationTimestampMs, Time.MILLISECONDS);
                String myTimeText = myTime.format("MM/dd/yyyy HH:mm:ss");
                m_MyLocationTimeOutput.setText (myTimeText);
                
                if (m_RemoteLocationTimestampMs > 0)
                {
                    DateTime remoteTime = new DateTime(m_RemoteLocationTimestampMs, Time.MILLISECONDS);
                    String remoteTimeText = remoteTime.format("MM/dd/yyyy HH:mm:ss");
                    m_RemoteLocationTimeOutput.setText (remoteTimeText);
                }
                else
                {
                    m_RemoteLocationTimeOutput.setText ("");
                }
                
                try
                {
                    Thread.sleep (100);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    };
    
    private class TimeSyncThread extends Thread
    {
        public TimeSyncThread ()
        {
            setDaemon(true);
        }
        
        @Override
        public void run ()
        {
            boolean warnedTimeServer = false;
            boolean warnedGps = false;
            final int sleepTimeMs = 100;
            
            m_TimeSyncThreadRunning = true;
            while (m_TimeSyncThreadRunning)
            {
                if (m_MyLocationStatus == STATUS_SYNCED)
                    break;
                
                //Only try time sync if we aren't allowing the remote time sync
                if (!m_AllowRemoteSync)
                {
                    try
                    {
                        if (m_SyncOption == SYNCOPTION_TIMESERVER)
                        {
                            if (!m_ForbidTimeServerSync)
                            {
                                if (checkIfTimeSyncDelayPassed (sleepTimeMs))
                                {
                                    //try time sync to time server
                                    if (!warnedTimeServer)
                                        JOptionPane.showMessageDialog(m_DialogPtr, "Sync time to time server unimplemented", "Error", JOptionPane.WARNING_MESSAGE);
                                    warnedTimeServer = true;
                                }
                            }
                        }
                        else if (m_SyncOption == SYNCOPTION_PICCOLO)
                        {
                            if (!m_ForbidPiccoloSync)
                            {
                                try
                                {
                                    if (!m_ConnectedToPiccolo)
                                    {
                                        m_ConnectedToPiccolo = true;
                                        //try time sync to Piccolo
                                        m_PicInterface = new Pic_Interface(0, m_UsePiccoloComPort, 57600, false);
                                        m_PicInterface.addPicTelemetryListener(new Pic_TelemetryListener() {

                                            @Override
                                            public void handlePic_Telemetry(Long currentTime, Pic_Telemetry telem) 
                                            {
                                                if (telem.PDOP < 5 && checkIfTimeSyncDelayPassed (sleepTimeMs))
                                                {
                                                    m_PiccoloTimeSyncStatusOutput.setText ("Piccolo acquired GPS!");
                                                    //Good lock, time sync
                                                    m_TimeSyncThreadRunning = false;
                                                    m_PicInterface.forceShutdown();
                                                    m_ConnectedToPiccolo = false;
                                                    
                                                    setSystemTime(new Date (telem.m_TimestampMs));
                                                }
                                                else
                                                {
                                                    m_PiccoloTimeSyncStatusOutput.setText ("Piccolo GPS: PDOP=" + m_DecFormat1.format(telem.PDOP) + " Date=" + (new Date(telem.m_TimestampMs).toString()));
                                                }
                                            }
                                        });
                                        Thread th = (new Thread(m_PicInterface));
                                        th.setName ("PiccoloAPInterface");
                                        th.start();
                                        m_PiccoloTimeSyncStatusOutput.setText ("No connection to Piccolo on: " + m_UsePiccoloComPort);
                                    }
                                }
                                catch (Exception e)
                                {
                                    m_PiccoloTimeSyncStatusOutput.setText ("No connection to Piccolo!");
                                    System.out.println ("Unable to time sync to Piccolo GPS");
                                    e.printStackTrace();
                                    Thread.sleep (1000);
                                    m_ConnectedToPiccolo = false;
                                }
                            }
                        }
                        else if (m_SyncOption == SYNCOPTION_GPS)
                        {
                            if (checkIfTimeSyncDelayPassed (sleepTimeMs))
                            {
                                if (!m_ForbidGpsSync)
                                {
                                    //try time sync to GPS
                                    if (!warnedGps)
                                        JOptionPane.showMessageDialog(m_DialogPtr, "Sync time to GPS unimplemented", "Error", JOptionPane.WARNING_MESSAGE);
                                    warnedGps = true;
                                }
                            }
                        }
                        else if (m_SyncOption == SYNCOPTION_LOCALTIME)
                        {
                            m_MyLocationStatus = STATUS_PENDING;
                            if (checkIfTimeSyncDelayPassed (sleepTimeMs))
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        //We aren't going to bother reporting anything, because this will repeatedly fail until success.  Just assume
                        //the user will wait until it works.
                    }
                }
                else //if (m_AllowRemoteSync)
                {
                    m_MyLocationStatus = STATUS_REMOTE;
                    if (m_RemoteLocationStatus == STATUS_SYNCED && m_RemoteLocationTimestampMs > 0)
                    {
                        if (checkIfTimeSyncDelayPassed (sleepTimeMs))
                        {
                            DateTime dateTime = new DateTime(m_RemoteLocationTimestampMs, Time.MILLISECONDS);
                            Date date = dateTime.asDate();

                            setSystemTime (date);
                            break;
                        }
                    }
                }
                
                try 
                {
                    Thread.sleep (sleepTimeMs);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            
            m_AllowRemoteSyncCheckBox.setEnabled(false);
            enableTimeSyncCheckboxes(false);
            m_MyLocationStatus = STATUS_SYNCED;
            
            try 
            {
                Thread.sleep (1000);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            
            while (true)
            {
                if (m_RemoteLocationStatus == STATUS_SYNCED)
                {
                    startWacsCloseTimeSync();
                    break;
                }
                
                try 
                {
                    Thread.sleep (sleepTimeMs);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    };
    
    private boolean checkIfTimeSyncDelayPassed (long sleepTimeMs)
    {
        m_TimeSyncTimeCountdownMs -= sleepTimeMs;
        m_TimeSyncStatusLabel.setText ("Synchronizing in " + (int)Math.ceil(m_TimeSyncTimeCountdownMs/1000.0) + " seconds...");

        if (m_TimeSyncTimeCountdownMs <= 0)
            return true;
        return false;
    }
    
    private void setSystemTime (Date date)
    {
        boolean isWindows = false;
        if (System.getProperty("os.name").contains("indows"))
            isWindows = true;

        if (isWindows)
        {
            String timeString = "" + (date.getYear()+1900) + "," + 
                (date.getMonth()+1) + "," + 
                date.getDate() + "," +
                date.getDay() + "," + 
                date.getHours() + "," +
                date.getMinutes() + "," + 
                date.getSeconds() + "," + 
                0 + ",";

            SystemTime.JNISetLocalTime(timeString);
        }
        else
        {
            JOptionPane.showMessageDialog (m_DialogPtr, "Don't know how to set system time on this computer.\r\n  Change computer clock manually, press \'OK\' when complete", "Set System Time", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private class NetworkRecvThread extends Thread
    {
        public void NetworkRecvThread ()
        {
            setDaemon (true);
        }
        
        @Override
        public void run ()
        {            
            int bufSize = SatCommMessageArbitrator.MSGLENGTH_TIMESYNC;
            byte recvBuffer[] = new byte [bufSize];
            
            try
            {
                MulticastSocket sock = new MulticastSocket(m_MulticastRecvPort);
                sock.joinGroup(m_MulticastIpAddr);
                DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length, m_MulticastIpAddr, m_MulticastRecvPort);

                while (true)
                {
                    try
                    {
                        sock.receive(recvPacket);
                        
                        int messageType = SatCommMessageArbitrator.verifyPacketHeader (recvBuffer, bufSize);
                        boolean crcGood = SatCommMessageArbitrator.verifyPacketCrc (recvBuffer, bufSize);

                        if (messageType !=  SatCommMessageArbitrator.MSGTYPE_TIMESYNC || !crcGood)
                        {
                            //Packet got mangled from original version, just throw it away
                            System.err.println ("Packet received and ignored while waiting for time sync!");
                        }
                        else
                        {
                            ByteBuffer buf = ByteBuffer.wrap(recvBuffer, SatCommMessageArbitrator.getHeaderSize(), SatCommMessageArbitrator.getDataBlockLength(bufSize));
                            int type = buf.get();
                            long timeMs = buf.getLong();
                            int status = buf.get();

                            if (type != m_MyLocationType)
                            {
                                m_RemoteLocationTimestampMs = timeMs;
                                m_RemoteLocationStatus = status;
                                if (m_RemoteLocationStatus == STATUS_SYNCCOMPLETE)
                                    m_RemoteLocationStatus = STATUS_SYNCED;
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };
    
    private class NetworkSendThread extends Thread
    {
        public void NetworkSendThread ()
        {
            setDaemon (true);
        }
        
        @Override
        public void run ()
        {            
            int bufSize = SatCommMessageArbitrator.MSGLENGTH_TIMESYNC;
            byte sendBuffer[] = new byte[bufSize];
            
            try
            {
                MulticastSocket sock = new MulticastSocket(m_MulticastSendPort);
                sock.joinGroup(m_MulticastIpAddr);
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, m_MulticastIpAddr, m_MulticastSendPort);
            
                while (true)
                {
                    try
                    {
                        int destination;
                        if (m_MyLocationType == LOCATION_GCS)
                            destination = SatCommMessageArbitrator.DESTINATION_WACSPOD;
                        else //if (m_MyLocationType == LOCATION_POD)
                            destination = SatCommMessageArbitrator.DESTINATION_WACSGCS;
                        int dataBlockLength = SatCommMessageArbitrator.getDataBlockLength(bufSize);
                        SatCommMessageArbitrator.formMessageHeader(sendBuffer, destination, 2, SatCommMessageArbitrator.MSGTYPE_TIMESYNC, dataBlockLength, System.currentTimeMillis());
                        
                        ByteBuffer dataBlockBuffer = ByteBuffer.wrap(sendBuffer, SatCommMessageArbitrator.getHeaderSize(), dataBlockLength);
                        dataBlockBuffer.put((byte)m_MyLocationType).putLong(m_MyLocationTimestampMs).put((byte)m_MyLocationStatus).array();
                        sendPacket.setData(sendBuffer);
                        
                        //Compute CRC before sending message
                        int bufLoc = bufSize - SatCommMessageArbitrator.getCrcSize();
                        int crc = SatCommMessageArbitrator.compute2ByteCRC_CCITT (sendBuffer, bufLoc);
                        sendBuffer [bufLoc++] = (byte)(0xFF & (crc >> 8));        //MSB-first CRC field
                        sendBuffer [bufLoc++] = (byte)(0xFF & (crc));           //MSB-first CRC field
                        sock.send(sendPacket);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    try 
                    {
                        Thread.sleep (m_MulticastPeriod_ms);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }   
        }
    };
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_MyLocationLabel = new javax.swing.JLabel();
        m_MyLocationOutputLabel = new javax.swing.JLabel();
        m_RemoteLocationLabel = new javax.swing.JLabel();
        m_RemoteLocationOutputLabel = new javax.swing.JLabel();
        m_RemoteLocationStatusLabel = new javax.swing.JLabel();
        m_MyLocationStatusLabel = new javax.swing.JLabel();
        m_AllowRemoteSyncCheckBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        m_UseTimeServerCheckBox = new javax.swing.JCheckBox();
        m_UsePiccoloCheckbox = new javax.swing.JCheckBox();
        m_UsePiccoloComInputBox = new javax.swing.JComboBox();
        m_ExitButton = new javax.swing.JButton();
        m_UseTimeServerInputBox = new javax.swing.JTextField();
        m_UseGPSCheckBox = new javax.swing.JCheckBox();
        m_UseGPSInputBox = new javax.swing.JComboBox();
        m_UseLocalTimeCheckBox = new javax.swing.JCheckBox();
        m_TimeSyncStatusLabel = new javax.swing.JLabel();
        m_MyLocationTimeLabel = new javax.swing.JLabel();
        m_MyLocationTimeOutput = new javax.swing.JLabel();
        m_RemoteLocationTimeOutput = new javax.swing.JLabel();
        m_RemoteLocationTimeLabel = new javax.swing.JLabel();
        m_PiccoloTimeSyncStatusOutput = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("WACS Time Sync");

        m_MyLocationLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_MyLocationLabel.setText("My Location:");

        m_MyLocationOutputLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_MyLocationOutputLabel.setText("WACS GCS");

        m_RemoteLocationLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_RemoteLocationLabel.setText("Remote Location:");

        m_RemoteLocationOutputLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_RemoteLocationOutputLabel.setText("WACS Pods");

        m_RemoteLocationStatusLabel.setBackground(new java.awt.Color(255, 51, 51));
        m_RemoteLocationStatusLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_RemoteLocationStatusLabel.setText("NOT TIME-SYNCED");
        m_RemoteLocationStatusLabel.setOpaque(true);

        m_MyLocationStatusLabel.setBackground(new java.awt.Color(255, 51, 51));
        m_MyLocationStatusLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_MyLocationStatusLabel.setText("NOT TIME-SYNCED");
        m_MyLocationStatusLabel.setOpaque(true);

        m_AllowRemoteSyncCheckBox.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_AllowRemoteSyncCheckBox.setText("Allow remote location to time-sync my location");
        m_AllowRemoteSyncCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AllowRemoteSyncCheckBoxActionPerformed(evt);
            }
        });

        m_UseTimeServerCheckBox.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_UseTimeServerCheckBox.setText("Use local time server for time-sync");
        m_UseTimeServerCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_UseTimeServerCheckBoxActionPerformed(evt);
            }
        });

        m_UsePiccoloCheckbox.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_UsePiccoloCheckbox.setText("Use local Piccolo Autopilot for time-sync");
        m_UsePiccoloCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_UsePiccoloCheckboxActionPerformed(evt);
            }
        });

        m_UsePiccoloComInputBox.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_UsePiccoloComInputBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "COM1", "COM2" }));
        m_UsePiccoloComInputBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_UsePiccoloComInputBoxActionPerformed(evt);
            }
        });

        m_ExitButton.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_ExitButton.setText("Exit");
        m_ExitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_ExitButtonActionPerformed(evt);
            }
        });

        m_UseTimeServerInputBox.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_UseTimeServerInputBox.setText("192.168.1.100");

        m_UseGPSCheckBox.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_UseGPSCheckBox.setText("Use local GPS receiver for time-sync");
        m_UseGPSCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_UseGPSCheckBoxActionPerformed(evt);
            }
        });

        m_UseGPSInputBox.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_UseGPSInputBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "COM1", "COM2" }));

        m_UseLocalTimeCheckBox.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_UseLocalTimeCheckBox.setText("Use local computer time for time-sync");
        m_UseLocalTimeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_UseLocalTimeCheckBoxActionPerformed(evt);
            }
        });

        m_TimeSyncStatusLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_TimeSyncStatusLabel.setForeground(new java.awt.Color(51, 153, 0));
        m_TimeSyncStatusLabel.setText("No Local Time Sync Option Selected");
        m_TimeSyncStatusLabel.setEnabled(false);

        m_MyLocationTimeLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_MyLocationTimeLabel.setText("Time:");

        m_MyLocationTimeOutput.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_MyLocationTimeOutput.setText("12/12/1985 12:00:00AM UTC");

        m_RemoteLocationTimeOutput.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_RemoteLocationTimeOutput.setText("12/12/1985 12:00:00AM UTC");

        m_RemoteLocationTimeLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        m_RemoteLocationTimeLabel.setText("Time:");

        m_PiccoloTimeSyncStatusOutput.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        m_PiccoloTimeSyncStatusOutput.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(m_RemoteLocationTimeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_RemoteLocationTimeOutput, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(m_ExitButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(m_MyLocationLabel)
                                .addGap(55, 55, 55)
                                .addComponent(m_MyLocationOutputLabel)
                                .addGap(39, 39, 39)
                                .addComponent(m_MyLocationStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(10, 10, 10))
                            .addComponent(jSeparator1)
                            .addComponent(m_UseLocalTimeCheckBox)
                            .addComponent(m_UsePiccoloCheckbox)
                            .addComponent(m_UseGPSCheckBox)
                            .addComponent(m_UseTimeServerCheckBox)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(m_UseTimeServerInputBox, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(m_UseGPSInputBox, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(m_UsePiccoloComInputBox, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(m_PiccoloTimeSyncStatusOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                            .addComponent(m_AllowRemoteSyncCheckBox)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(m_TimeSyncStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(39, 39, 39)
                                .addComponent(m_MyLocationTimeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(m_MyLocationTimeOutput, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(m_RemoteLocationLabel)
                                .addGap(18, 18, 18)
                                .addComponent(m_RemoteLocationOutputLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_RemoteLocationStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(10, 10, 10)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_MyLocationLabel)
                    .addComponent(m_MyLocationOutputLabel)
                    .addComponent(m_MyLocationStatusLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_MyLocationTimeLabel)
                    .addComponent(m_MyLocationTimeOutput))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_RemoteLocationLabel)
                    .addComponent(m_RemoteLocationOutputLabel)
                    .addComponent(m_RemoteLocationStatusLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_RemoteLocationTimeLabel)
                    .addComponent(m_RemoteLocationTimeOutput))
                .addGap(18, 18, 18)
                .addComponent(m_TimeSyncStatusLabel)
                .addGap(18, 18, 18)
                .addComponent(m_AllowRemoteSyncCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_UseTimeServerCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_UseTimeServerInputBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_UsePiccoloCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_UsePiccoloComInputBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_PiccoloTimeSyncStatusOutput))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(m_UseGPSCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_UseGPSInputBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(m_UseLocalTimeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(m_ExitButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void m_AllowRemoteSyncCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AllowRemoteSyncCheckBoxActionPerformed
        // TODO add your handling code here:
        m_AllowRemoteSync = m_AllowRemoteSyncCheckBox.isSelected();
        m_TimeSyncStatusLabel.setText ("");
        
        if (!m_AllowRemoteSync)
            m_MyLocationStatus = STATUS_NOTSYNCED;
        enableTimeSyncCheckboxes (!m_AllowRemoteSync);
        if (m_AllowRemoteSync)
            m_MyLocationStatus = STATUS_REMOTE;
        
    }//GEN-LAST:event_m_AllowRemoteSyncCheckBoxActionPerformed

    private void m_UseTimeServerCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_UseTimeServerCheckBoxActionPerformed
        // TODO add your handling code here:
        
        boolean enabled = m_UseTimeServerCheckBox.isSelected() && m_UseTimeServerCheckBox.isEnabled();
        m_UseTimeServerInputBox.setEnabled(enabled);
        m_UseTimeServerInputBox.setText (m_UseTimeServerIpAddr);
        
        m_TimeSyncStatusLabel.setText ("");
        
        if (m_SyncOption == SYNCOPTION_TIMESERVER && !enabled)
        {
            m_SyncOption = SYNCOPTION_NONE;
            m_MyLocationStatus = STATUS_NOTSYNCED;
        }
        else if (enabled)
        {
            m_SyncOption = SYNCOPTION_TIMESERVER;
            m_MyLocationStatus = STATUS_PENDING;
            unselectTimeSyncCheckboxes(SYNCOPTION_TIMESERVER);
        }
    }//GEN-LAST:event_m_UseTimeServerCheckBoxActionPerformed

    private void m_ExitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_ExitButtonActionPerformed
        // TODO add your handling code here:
        parseDialogOpts ();
        saveConfig();
        System.exit (0);
    }//GEN-LAST:event_m_ExitButtonActionPerformed

    private void m_UseGPSCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_UseGPSCheckBoxActionPerformed
        // TODO add your handling code here:
        boolean enabled = m_UseGPSCheckBox.isSelected() && m_UseGPSCheckBox.isEnabled();
        m_UseGPSInputBox.setEnabled(enabled);
        if (!m_ForbidGpsSync)
            fillComboBoxWithCommPorts (m_UseGPSInputBox);
        
        m_TimeSyncStatusLabel.setText ("");
        
        
        if (m_SyncOption == SYNCOPTION_GPS && !enabled)
        {
            m_SyncOption = SYNCOPTION_NONE;
            m_MyLocationStatus = STATUS_NOTSYNCED;
        }
        else if (enabled)
        {
            m_SyncOption = SYNCOPTION_GPS;
            m_MyLocationStatus = STATUS_PENDING;
            unselectTimeSyncCheckboxes(SYNCOPTION_GPS);
        }
    }//GEN-LAST:event_m_UseGPSCheckBoxActionPerformed

    private void m_UsePiccoloCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_UsePiccoloCheckboxActionPerformed
        // TODO add your handling code here:
        boolean enabled = m_UsePiccoloCheckbox.isSelected() && m_UsePiccoloCheckbox.isEnabled();
        m_UsePiccoloComInputBox.setEnabled(enabled);
        m_PiccoloTimeSyncStatusOutput.setEnabled (enabled);
        
        m_TimeSyncStatusLabel.setText ("");
                
        String maintainCurrentSelection = null;
        if (m_UsePiccoloComPort != null)
            maintainCurrentSelection = m_UsePiccoloComPort;
        if (!m_ForbidPiccoloSync)
            fillComboBoxWithCommPorts (m_UsePiccoloComInputBox);
        if (maintainCurrentSelection != null)
            m_UsePiccoloComInputBox.setSelectedItem(maintainCurrentSelection);
        
        if (m_SyncOption == SYNCOPTION_PICCOLO && !enabled)
        {
            m_SyncOption = SYNCOPTION_NONE;
            m_MyLocationStatus = STATUS_NOTSYNCED;
        }
        else if (enabled)
        {
            m_SyncOption = SYNCOPTION_PICCOLO;
            m_MyLocationStatus = STATUS_PENDING;
            unselectTimeSyncCheckboxes(SYNCOPTION_PICCOLO);
        }
    }//GEN-LAST:event_m_UsePiccoloCheckboxActionPerformed

    private void m_UseLocalTimeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_UseLocalTimeCheckBoxActionPerformed
        // TODO add your handling code here:
        boolean enabled = m_UseLocalTimeCheckBox.isSelected() && m_UseLocalTimeCheckBox.isEnabled();
        m_TimeSyncStatusLabel.setText ("");
        m_TimeSyncTimeCountdownMs = m_TimeSyncTimeDelayMs;
        
        if (m_SyncOption == SYNCOPTION_LOCALTIME && !enabled)
        {
            m_SyncOption = SYNCOPTION_NONE;
            m_MyLocationStatus = STATUS_NOTSYNCED;
        }
        else if (enabled)
        {
            m_SyncOption = SYNCOPTION_LOCALTIME;
            m_MyLocationStatus = STATUS_PENDING;
            unselectTimeSyncCheckboxes(SYNCOPTION_LOCALTIME);
        }
    }//GEN-LAST:event_m_UseLocalTimeCheckBoxActionPerformed

    private void m_UsePiccoloComInputBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_UsePiccoloComInputBoxActionPerformed
        // TODO add your handling code here:
        if (m_PicInterface != null && m_PicInterface.isRunning())
            m_PicInterface.forceShutdown();
        m_PicInterface = null;
        m_ConnectedToPiccolo = false;
        parseDialogOpts ();
        
        m_TimeSyncStatusLabel.setText ("");
        
    }//GEN-LAST:event_m_UsePiccoloComInputBoxActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox m_AllowRemoteSyncCheckBox;
    private javax.swing.JButton m_ExitButton;
    private javax.swing.JLabel m_MyLocationLabel;
    private javax.swing.JLabel m_MyLocationOutputLabel;
    private javax.swing.JLabel m_MyLocationStatusLabel;
    private javax.swing.JLabel m_MyLocationTimeLabel;
    private javax.swing.JLabel m_MyLocationTimeOutput;
    private javax.swing.JLabel m_PiccoloTimeSyncStatusOutput;
    private javax.swing.JLabel m_RemoteLocationLabel;
    private javax.swing.JLabel m_RemoteLocationOutputLabel;
    private javax.swing.JLabel m_RemoteLocationStatusLabel;
    private javax.swing.JLabel m_RemoteLocationTimeLabel;
    private javax.swing.JLabel m_RemoteLocationTimeOutput;
    private javax.swing.JLabel m_TimeSyncStatusLabel;
    private javax.swing.JCheckBox m_UseGPSCheckBox;
    private javax.swing.JComboBox m_UseGPSInputBox;
    private javax.swing.JCheckBox m_UseLocalTimeCheckBox;
    private javax.swing.JCheckBox m_UsePiccoloCheckbox;
    private javax.swing.JComboBox m_UsePiccoloComInputBox;
    private javax.swing.JCheckBox m_UseTimeServerCheckBox;
    private javax.swing.JTextField m_UseTimeServerInputBox;
    // End of variables declaration//GEN-END:variables
}
