/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.tase;


import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.belief.VideoClientRecorderStatusBelief;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 *
 * @author xud1
 */

public class RecorderInterface extends Thread
{
    private BeliefManager mBeliefManager;
    private String mAgentID;
    
    public RecorderInterface(BeliefManager beliefManager, String agentID)
    {
        this.mBeliefManager = beliefManager;        
        this.mAgentID = agentID;
    }        
        
    public static void SendRecorderCommand(Boolean recording)
    {
        final int COMMAND_TYPE = 0; // 0 = recorder command, 1 = time sync(deprecated)
        final int START_RECORDER = 0; 
        final int PAUSE_RECORDER = 1;

        try
        {                      
            String cmdBroadcastIP = Config.getConfig().getProperty("EthernetVideoClient.RecorderListeningHost", "192.168.100.255");
            short cmdBroadcastPort = (short)Config.getConfig().getPropertyAsInteger("EthernetVideoClient.RecorderListeningPort", 5377);
            byte[] cmdSerialized = new byte[128];
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte((byte) COMMAND_TYPE);

            if (recording) 
            {
                dos.writeInt(START_RECORDER);
            } 
            else 
            {
                dos.writeInt(PAUSE_RECORDER);
            }

            cmdSerialized = baos.toByteArray();
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(cmdSerialized, cmdSerialized.length, InetAddress.getByName(cmdBroadcastIP), cmdBroadcastPort);
            socket.send(packet);                                                
        }
        catch (SocketException ex) 
        {
            System.out.println(ex.getMessage());
        } 
        catch (UnknownHostException ex) 
        {
            System.out.println(ex.getMessage());
        } 
        catch (IOException ex) 
        {
            System.out.println(ex.getMessage());
        }        
    }
    
    
    @Override
    public void run()
    {
        final int STATUS_PACKET_SIZE = 32;
        final short STATUS_LISTEN_PORT = 5747;
        byte[] receiveBuffer = new byte[STATUS_PACKET_SIZE];
        DatagramPacket datagram = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        DatagramSocket socket = null;

        try 
        {
            socket = new DatagramSocket(STATUS_LISTEN_PORT);
        } 
        catch (SocketException ex) 
        {
            System.out.println(ex.getMessage());
        }

        while (true) 
        {
            try 
            {
                socket.receive(datagram);
                ByteBuffer bb = ByteBuffer.wrap(datagram.getData().clone());

                RecorderStatus status = new RecorderStatus();
                status.hasReceivedTimeSync = (bb.getInt() == 1)?true:false;
                status.timestamp = (bb.getInt() == 1)?true:false; // time in seconds
                status.isRecording = (bb.getInt() == 1)?true:false;
                status.frameRate = (bb.getInt() == 1)?true:false;
                status.numFramesInQueue = (bb.getInt() == 1)?true:false;
                status.numFramesDropped = (bb.getInt() == 1)?true:false;
                status.isAdeptPresent = (bb.getInt() == 1)?true:false;
                status.numWarpQueueUnderflows = (bb.getInt() == 1)?true:false;
                
                VideoClientRecorderStatusBelief rsb = new VideoClientRecorderStatusBelief(mAgentID, status);
                mBeliefManager.put(rsb);
                Thread.sleep(1000);              
            } 
            catch (IOException ex) 
            {
                System.out.println(ex.getMessage());
            }
            catch (InterruptedException ex)
            {
                System.out.println(ex.getMessage());
            }
        }                   
    }
}

