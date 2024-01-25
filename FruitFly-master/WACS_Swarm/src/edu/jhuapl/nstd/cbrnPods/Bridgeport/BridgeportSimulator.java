/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.Bridgeport;

import edu.jhuapl.jlib.net.SocketLoggerThread;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author humphjc1
 */
public class BridgeportSimulator
{

    String m_FilenameBase;
    int m_FilenameIndex;
    File m_CurrentRawFile;
    DataInputStream m_SocketInputStream;
    OutputStream m_SocketOutputStream;
    Socket m_Socket;
    boolean m_Running;
    InetAddress m_SendToAddress;
    ServerSocket m_CmdSocketListener;
    Socket m_CmdSocket;


    /**
     * Port number for sending commands to Digi board
     */
    private final int COMMAND_PORT = 9933;

    /**
     * Port number for receiving data from Digi board
     */
    private final int DATA_PORT = 9932;

    /**
     * Port number to listen to for responses from UDP broadcast
     */
    private final int BROADCAST_REPLY_PORT = 9931;

    /**
     * Port number for sending UDP broadcast to get all connected instruments
     */
    private final int UDP_BROADCAST_TO_PORT = 9930;

    /**
     * Port number for sending UDP broadcast to get all connected instruments
     */
    private final int UDP_BROADCAST_FROM_PORT = 9913;

    /**
     * Maximum size for read buffer
     */
    private final int MAX_BUFFER_SIZE = 16384;

    /**
     * Maximum size for write buffer
     */
    private final int MAX_COMMAND_SIZE = 1024;

    /**
     * Size of header for output commands
     */
    private final int OUTPUT_HEADER_SIZE = 8;

    /**
     * Size of command to send on UDP broadcast and to receive in reply
     */
    private final int UDP_BROADCAST_LENGTH = 64;

    /**
     * Size of header for data input
     */
    private final int INPUT_HEADER_SIZE = 12;

    /**
     * Reduction in replay delay.  If 10, replay is 10x faster (sleep is 10x reduced)
     */
    private final double PLAYBACK_THREAD_SPEED_REDUCTION = 1.0;




    public BridgeportSimulator (String filename)
    {
        m_FilenameBase = filename.substring(0,filename.lastIndexOf("_") + 1);

        int startIdx = filename.lastIndexOf("_") + 1;
        int endIdx = filename.lastIndexOf(".");
        m_FilenameIndex = Integer.parseInt (filename.substring(startIdx, endIdx));
    }

    public void run ()
    {
        runUdpListener();
        sendUdpReply();
        setupCommandSocket();
        runTcpPlayback();
    }

    public void runUdpListener ()
    {
        try
        {
            DatagramSocket udpSocket = new DatagramSocket(UDP_BROADCAST_TO_PORT);
            DatagramPacket recvPacket = new DatagramPacket(new byte[UDP_BROADCAST_LENGTH], UDP_BROADCAST_LENGTH);

            System.out.println ("Simulator waiting for UDP broadcast");
            udpSocket.receive(recvPacket);
            m_SendToAddress = recvPacket.getAddress();
            System.out.println ("Broadcast received");


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void sendUdpReply ()
    {
        try
        {
            Socket replySocket = new Socket(m_SendToAddress, BROADCAST_REPLY_PORT);
            OutputStream output = replySocket.getOutputStream();
             
            byte [] replyBytes = {0,1,2,3,4,5,(byte)192,(byte)168,1,115, 10, 11};
            output.write(replyBytes);

            Thread.sleep (15);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setupCommandSocket ()
    {
        try
        {
            System.out.println ("Waiting for command");
            m_CmdSocketListener = new ServerSocket(COMMAND_PORT);
            m_CmdSocket = m_CmdSocketListener.accept();
            System.out.println ("Command received");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void runTcpPlayback ()
    {

        System.out.println ("Running TCP playback");
        m_Running = true;
        byte[] headerBytes = new byte[MAX_BUFFER_SIZE];
        byte[] dataBytes = new byte[MAX_BUFFER_SIZE];
        byte[] combBytes = new byte [MAX_BUFFER_SIZE];
        long newReadTimestamp = -1;
        long lastReadTimestamp = -1;
        long lastCurrTimestamp = System.currentTimeMillis();
        long newCurrTimestamp = -1;
        int byteLengthHeader = -1;
        int byteLengthData = -1;
        int readBytesHeader = -1;
        int readBytesData = -1;


        while (m_Running)
        {
            try
            {
                int dataType = -1;

                try
                {
                    newReadTimestamp = m_SocketInputStream.readLong();
                }
                catch (Exception e)
                {
                    //We reached the end of a file, either because m_SocketInputStream is null (haven't started yet) or we read past EOF.
                    if (!getNextTcpPlaybackFile())
                    {
                        //We didn't get a new file
                        m_Running = false;
                        continue;
                    }

                    newReadTimestamp = m_SocketInputStream.readLong();
                }

                byteLengthHeader = m_SocketInputStream.readInt();
                readBytesHeader = m_SocketInputStream.read(headerBytes, 0, byteLengthHeader);

                if (byteLengthHeader == readBytesHeader)
                {
                    if (headerBytes[0] == '#' && headerBytes[1] == '#' && headerBytes[2] == '#' && headerBytes[3] == '#' &&
                            headerBytes[4] == '*' && headerBytes[5] == '*' && headerBytes[6] == '*' && headerBytes[7] == '*')
                    {
                        //Message logged, print to screen
                        System.out.println (new String (headerBytes, 0, readBytesHeader));

                    }
                    else
                    {
                        m_SocketInputStream.readLong(); //timestamp for data
                        byteLengthData = m_SocketInputStream.readInt();
                        readBytesData = m_SocketInputStream.read(dataBytes, 0, byteLengthData);

                        if (byteLengthData == readBytesData)
                        {
                            //Real message found logged
                            newCurrTimestamp = System.currentTimeMillis();
                            if (lastReadTimestamp > 0)
                                Thread.sleep ((long)((newReadTimestamp - lastReadTimestamp)/PLAYBACK_THREAD_SPEED_REDUCTION));
                            lastReadTimestamp = newReadTimestamp;


                            m_Socket = new Socket(m_SendToAddress, DATA_PORT);
                            m_SocketOutputStream = m_Socket.getOutputStream();

                            for (int i = 0; i < readBytesHeader; i ++)
                                combBytes[i] = headerBytes[i];
                            for (int i = 0; i < readBytesData; i ++)
                                combBytes[i+readBytesHeader] = dataBytes[i];

                            m_SocketOutputStream.write(combBytes, 0, readBytesData+readBytesHeader);
                            System.out.println ("Wrote " + (readBytesData+readBytesHeader) + " to stream");

                            m_SocketOutputStream.flush();
                            Thread.sleep (30);
                            m_SocketOutputStream.close();
                            m_Socket.close();

                        }
                        else
                        {
                            System.out.println ("Crapped our pants reading bytes from file");
                            m_Running = false;
                        }
                    }
                }
                else
                {
                    System.out.println ("Crapped our pants reading bytes from file");
                    m_Running = false;
                }
            }
            catch (Exception e)
            {
                m_Running = false;
                System.out.println ("Playback file, index " + m_FilenameIndex + ", ended abruptly: " + m_FilenameBase);
            }
        }

        try
        {
            m_SocketInputStream.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println ("Playback ended for logs: " + m_FilenameBase);
    }



    /**
     * This is called when we hit the end of a tcp playback file.  Returns whether a next file is available.
     *
     * @return boolean True if a new file has been located, false if we're done with this chain
     */
    private boolean getNextTcpPlaybackFile()
    {
        Formatter format = new Formatter();
        format.format("%1$s%2$05d%3$s", m_FilenameBase, m_FilenameIndex++, ".tcp");
        m_CurrentRawFile = new File(format.out().toString());

        try
        {
            m_SocketInputStream = new DataInputStream(new FileInputStream(m_CurrentRawFile));
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }



    public static void main (String [] args)
    {
        BridgeportSimulator sim = new BridgeportSimulator("c:/documents and settings/humphjc1/desktop/BridgeportGammaTcpRaw_1290526960203_00000.tcp");
        sim.run();
        //sim.runTcpPlayback();
    }



}
