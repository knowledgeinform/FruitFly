/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.nstd.swarm.wacs.SatCommMessageArbitrator;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author humphjc1
 */
public class ShadowWGVSMSimulator 
{
    SendThread gcsSendThread;
    SendThread satCommSendThread;
    SendThread wavsmSendThread;
    
    RecvThread gcsRecvThread;
    RecvThread satCommRecvThread;
    RecvThread remoteRecvThread;
        

    public ShadowWGVSMSimulator() 
    {
        gcsSendThread = new SendThread("233.1.3.3", 47191);
        gcsSendThread.start();
        satCommSendThread = new SendThread("233.1.3.3", 57191);
        satCommSendThread.start();
        wavsmSendThread = new SendThread("233.1.3.1", 56191);
        wavsmSendThread.start();
        
        gcsRecvThread = new RecvThread("233.1.3.3", 47190);
        gcsRecvThread.start();
        satCommRecvThread = new RecvThread("233.1.3.3", 57190);
        satCommRecvThread.start();
        remoteRecvThread = new RecvThread("233.1.3.2", 56190);
        remoteRecvThread.start();
        
        
        while (true)
        {
            try {
                Thread.sleep (100000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ShadowWGVSMSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private class SendThread extends Thread 
    {
        ConcurrentLinkedQueue<DatagramPacket> m_DataToSend = new ConcurrentLinkedQueue<DatagramPacket>();
        
        String m_MulticastHostname;
        InetAddress m_MulticastAddress;
        int m_MulticastPort;
        
        public SendThread (String hostName, int commPort)
        {
            this.setName ("WACS-WGVSMSimSendThread" + hostName + ":" + commPort);
            m_MulticastHostname = hostName;
            try
            {
                m_MulticastAddress = InetAddress.getByName(m_MulticastHostname);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            m_MulticastPort = commPort;
        }
        
        public void run ()
        {
            try
            {            
                InetAddress remoteIPAddress = InetAddress.getByName(m_MulticastHostname);
                MulticastSocket socket;
                socket = new MulticastSocket(m_MulticastPort);
                socket.joinGroup(remoteIPAddress);

                while (true)
                {
                    DatagramPacket packet = m_DataToSend.poll();
                    if (packet != null)
                    {
                        packet.setPort(m_MulticastPort);
                        packet.setAddress(m_MulticastAddress);
                        socket.send(packet);
                        //System.out.println ("Send packet to " + m_MulticastPort);
                    }
                    else
                    {
                        Thread.sleep (10);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        public void queuePacket (DatagramPacket packet)
        {
            m_DataToSend.add(packet);
        }
    }
    
    
    
    
    
    private class RecvThread extends Thread 
    {
        String m_RecvMulticastHostname;
        int m_RecvMulticastPort;
        
        public RecvThread (String hostName, int port)
        {
            this.setName ("WACS-WGVSMSimRecvThread" + hostName + ":" + port);
            m_RecvMulticastHostname = hostName;
            m_RecvMulticastPort = port;
        }
        
        public void run ()
        {
            try
            {            
                MulticastSocket receiveSocket;
                receiveSocket = new MulticastSocket(m_RecvMulticastPort);
                InetAddress remoteIPAddress = InetAddress.getByName(m_RecvMulticastHostname);
                receiveSocket.joinGroup(remoteIPAddress);

                while (true)
                {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                    receiveSocket.receive(packet);
                    //System.out.println ("Recv packet from " + m_RecvMulticastPort);
                    
                    int messageType = SatCommMessageArbitrator.verifyPacketHeader (receiveData, packet.getLength());
                    boolean crcGood = SatCommMessageArbitrator.verifyPacketCrc (receiveData, packet.getLength());

                    if (messageType == SatCommMessageArbitrator.MSGTYPE_INVALID || !crcGood)
                    {
                        //Packet got mangled from original version, just throw it away
                        System.err.println ("Bad packet received and ignored!  Type: " + messageType);
                        continue;
                    }
                    if (messageType == SatCommMessageArbitrator.MSGTYPE_RECEIPT)
                    {
                        if ((SatCommMessageArbitrator.getPacketDestination(receiveData)&0xFF) == 0x04)
                        {
                            ByteBuffer receiveBuffer = ByteBuffer.wrap(receiveData);
                            SatCommMessageArbitrator.checkAndPrintReceiptMessage (receiveData, receiveBuffer);
                        }
                        else
                        {
                            //Destination field says it shouldn't have gotten here
                            System.err.println ("Receipt message received that had wrong destination set - ignored!");
                        }
                        continue;
                    }
                    
                    byte[] buffer = packet.getData();
                    if ((SatCommMessageArbitrator.getPacketDestination(buffer)&0x01) != 0 || (SatCommMessageArbitrator.getPacketDestination(buffer)&0x02) != 0)
                    {
                        gcsSendThread.queuePacket(packet);
                        satCommSendThread.queuePacket(packet);
                        wavsmSendThread.queuePacket(packet);
                    }
                    else if ((SatCommMessageArbitrator.getPacketDestination(buffer)&0x08) != 0)
                    {
                        gcsSendThread.queuePacket(packet);
                        satCommSendThread.queuePacket(packet);
                    }
                    
                    
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    static public void main(String[] args)
    {
        Thread.currentThread().setName ("WACS-ShadowWgvsmSimulator");
        ShadowWGVSMSimulator simulator = new ShadowWGVSMSimulator();
    }
}
