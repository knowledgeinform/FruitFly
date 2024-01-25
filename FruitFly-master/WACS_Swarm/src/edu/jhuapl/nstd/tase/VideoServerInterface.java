/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.tase;

import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.display.Adam7;
import edu.jhuapl.nstd.swarm.belief.GuaranteedVideoDataBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientStreamStatusBelief;
import edu.jhuapl.nstd.swarm.belief.SatCommImageTransmissionFinishedBelief;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.HashSet;
import java.util.BitSet;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 *
 * @author xud1
 */
public class VideoServerInterface 
{
    private final String VIDEOSERVER_HOST = "localHost";
    private final String POD_HOST = "localHost";
    private final short VIDEOSERVER_PORT = (short)Config.getConfig().getPropertyAsInteger("EthernetVideoClient.RemotePort", 3999);    
    private final short POD_PORT = (short)Config.getConfig().getPropertyAsInteger("", 3900);     
    private final int MAX_DATAGRAM_SIZE = Config.getConfig().getPropertyAsInteger("EthernetVideoClient.MaxRxPacketSize", 65507);  
    private final double IMAGE_COMPRESSION_QUALITY = Config.getConfig().getPropertyAsDouble("EthernetVideoClient.ImageCompressionQuality", 0.2);
    private final Object clientReadyLock = new Object();
    private final Object frameReceiptLock = new Object();
    private final Object latestFrameLock = new Object();
    private final Object clientSnapshotLock = new Object();
    private static final int MAX_INTERLACE_PASS = 7;
    
    private VideoStreamThread mStreamingThread= null;
    private FrameUpdateThread mUpdateThread = null;
    private String mClientHost;
    private int mReceivedFrame;
    private short mClientPort;
    private boolean mClientReady = false;
    private boolean mSnapshot = false;
    private byte[] mLatestFrame;   
    
    BeliefManager mBeliefManager;
    
    public VideoServerInterface(String clientHost, short clientPort, BeliefManager beliefMgr)
    {
        mClientHost = clientHost;
        mClientPort = clientPort;
        mUpdateThread = new FrameUpdateThread();
        mStreamingThread = new VideoStreamThread();
        mBeliefManager = beliefMgr;
        initializeInterface();
    }
                
    private void initializeInterface()
    {
        mUpdateThread.start();    
    }
            
    private class Adam7InterlaceThread extends Thread
    {
        // sample 7 partition/interlaces from the source image
        BlockingQueue<byte[]> interlacedFrames;
        BufferedImage sourceImage;
        
        Adam7InterlaceThread(byte[] sourceImageBytes, BlockingQueue<byte[]> frames)
        {
            try
            {
                InputStream in = new ByteArrayInputStream(sourceImageBytes);
                sourceImage = ImageIO.read(in);
                interlacedFrames = frames;    
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
        }
        
        @Override 
        public void run()
        {
            Adam7.Interlace(interlacedFrames, sourceImage, (float)IMAGE_COMPRESSION_QUALITY);
            System.out.println("Adam7 interlacer finished!");
        }                
    }
    
    // handles communication with ground station
    private class VideoStreamThread extends Thread
    {        
        @Override
        public void run()
        {
            System.out.println("Pod video streaming started!" + "\n");            
            BlockingQueue<byte[]> pFrameQueue = new LinkedBlockingQueue<byte[]>();
            Adam7InterlaceThread interlaceThread = null;
            DatagramSocket socket = null;
            boolean pFramesDelivered = true;
            int recentlySentHash = 0;
            byte[] pFrameOut = null;
            HashSet deliveredFrames = new HashSet();
            
            while(true)
            {
                boolean ready = getClientReady();
                
                if(ready)
                {
                    byte[] currentFrame = getLatestFrame();
                    
                    if(currentFrame != null)
                    {
                        byte[] outgoingFrame = null;
                        boolean guaranteedMode = getClientGuaranteedMode();
                        
                        if(guaranteedMode)
                        {                                                       
                            if( (interlaceThread == null || !interlaceThread.isAlive()) && pFramesDelivered)
                            {
                                System.out.println("Video_Server: InterlaceThread started" + "\n");
                                // start a new iteration of adam7 interlace
                                interlaceThread = new Adam7InterlaceThread(currentFrame, pFrameQueue);
                                interlaceThread.start();
                                pFramesDelivered = false;                                
                                
                                try
                                {
                                    // get the first frame when ready
                                    pFrameOut = pFrameQueue.take();
                                    recentlySentHash = (BitSet.valueOf(pFrameOut)).hashCode();
                                    System.out.println("Video_Server: First outgoint frame: checksum = " + recentlySentHash + "; size = " + pFrameOut.length + "\n");
                                }
                                catch (InterruptedException ex)
                                {
                                    System.err.println(ex.getMessage());
                                }
                            }
                            else
                            {
                                int receipt = getClientReceivedFrame();
                                if( (receipt == recentlySentHash) )
                                {
                                    // delivery of previously sent interlace confirmed   
                                    deliveredFrames.add(receipt);
                                    
                                    if(deliveredFrames.size() < MAX_INTERLACE_PASS)
                                    {                                                                         
                                        try
                                        {
                                            // get the next avaialable frame
                                            pFrameOut = pFrameQueue.take();
                                            recentlySentHash = (BitSet.valueOf(pFrameOut)).hashCode();
                                            System.out.println("Video_Server: Next frame to be sent: checksum = " + recentlySentHash + "; size = " + pFrameOut.length + "\n");
                                        }
                                        catch (InterruptedException ex)
                                        {
                                            System.err.println(ex.getMessage());
                                        }
                                    }
                                    else if(deliveredFrames.size() == MAX_INTERLACE_PASS)
                                    {
                                        SatCommImageTransmissionFinishedBelief stfb = new SatCommImageTransmissionFinishedBelief(WACSAgent.AGENTNAME);
                                        mBeliefManager.put(stfb);
                                        
                                        System.out.println("Video_Server: All frames delivered, last receipt = " + recentlySentHash + "\n");
                                        pFramesDelivered = true;
                                        setClientReady(false); 
                                        recentlySentHash = 0;
                                        pFrameOut = null;
                                        deliveredFrames.clear();
                                    }
                                }                                                                
                                else if(deliveredFrames.contains(receipt))
                                {
                                    System.out.println("Video_Server: " + receipt + " has already been delivered!" + "\n"); 
                                }     
                                else
                                {
                                    System.out.println("Video_Server: " + getClientReceivedFrame() + " did not match checksum: " + recentlySentHash + " resending..." + "\n");
                                }
                            }   
                            
                            if( !pFramesDelivered )
                            {
                                GuaranteedVideoDataBelief dataBelief = new GuaranteedVideoDataBelief(WACSAgent.AGENTNAME, pFrameOut.length, pFrameOut);
                                mBeliefManager.put(dataBelief);
                                setClientReady(false); 
                            }
                        }
                        else
                        {
                            // send non-guaranteed frame as UDP Datagram
                            System.out.println("Sending non-progressive image" + "\n");
                            outgoingFrame = currentFrame;
                            
                            try
                            {                         
                                if (outgoingFrame != null)
                                {
                                    // send the non-guaranteed frames directly over UDP
                                    socket = new DatagramSocket();
                                    DatagramPacket packet = new DatagramPacket(outgoingFrame, outgoingFrame.length, InetAddress.getByName(mClientHost), mClientPort);
                                    socket.send(packet);
                                    setClientReady(false);                        
                                }
                            }
                            catch (SocketException ex) 
                            {
                                System.err.println(ex.getMessage());
                            } 
                            catch (UnknownHostException ex) 
                            {
                                System.err.println(ex.getMessage());
                            } 
                            catch (IOException ex) 
                            {
                                System.err.println(ex.getMessage());
                            }                                 
                        }                                                                                                        
                    }
                }
                
                try
                {
                    Thread.sleep(200);
                }
                catch(InterruptedException ex)
                {
                    if(socket != null)
                    {
                        socket.disconnect();
                        socket.close();
                    }
                    System.out.println("Pod -> Client video streaming stopped!" + "\n");
                    return;
                }                
            }
        }
    }    
    
    // handles communication with the video server
    private class FrameUpdateThread extends Thread
    {
        @Override
        public void run() 
        {
            byte[] frameRequestBytes = null;
            byte[] receiveBuffer = new byte[MAX_DATAGRAM_SIZE];            
            DatagramSocket requestSocket = null;
            DatagramSocket receiveSocket = null;
            DatagramPacket requestPacket = null;
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);                    
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            try 
            {
                // generate request datagram
                InetAddress addressBytes = InetAddress.getByName(POD_HOST);
                dos.write(addressBytes.getAddress());
                dos.writeShort(POD_PORT);
                frameRequestBytes = baos.toByteArray();
                requestSocket = new DatagramSocket();
                requestPacket = new DatagramPacket(frameRequestBytes, frameRequestBytes.length, InetAddress.getByName(VIDEOSERVER_HOST), VIDEOSERVER_PORT);
                
                // setup receive socket
                receiveSocket = new DatagramSocket(POD_PORT);
                receiveSocket.setSoTimeout(3000);                
            } 
            catch (SocketException ex)
            {
                System.err.println(ex.getMessage());
            }
            catch (IOException ex) 
            {
                System.err.println(ex.getMessage());
            }

            while (true) 
            {
                try 
                {
                    // send frame request
                    requestSocket.send(requestPacket);
                    
                    // wait to receive frame
                    receiveSocket.receive(receivePacket);
                    
                    // update latest frame
                    setLatestFrame(receivePacket.getData().clone());
                    
                    Thread.sleep(100);
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
                    System.out.println("VideoServer -> POD transmission timed out...");
                    System.out.println(ex.getMessage());
                } 
                catch (InterruptedException ex)
                {
                    if (requestSocket != null) 
                    {
                        requestSocket.disconnect();
                        requestSocket.close();
                    }
                    
                    System.out.println("VideoServer -> POD streaming stopped!");
                    return;
                }
            }
        }
    }        
    
    public void setLatestFrame(byte[] frame)
    {
        synchronized(latestFrameLock)
        {
            mLatestFrame = frame;
        }
    }
    
    public void setGuaranteedMode(boolean snapshot)
    {
        synchronized(clientSnapshotLock)
        {
            mSnapshot = snapshot;
        }
    }
    
    public void setClientReceivedFrame(int received)
    {
        synchronized(frameReceiptLock)
        {
            mReceivedFrame = received;
        }
    }
    
    public void setClientReady(boolean ready)
    {
        synchronized(clientReadyLock)
        {
            mClientReady = ready;
        }
    }
    
    public byte[] getLatestFrame()
    {
        byte[] lastestFrame;        
        synchronized(latestFrameLock)
        {
            lastestFrame = mLatestFrame;
        }
        
        return lastestFrame;
    }
    
    public boolean getClientGuaranteedMode()
    {
        boolean snapshot;
        synchronized(clientSnapshotLock)
        {
            snapshot = mSnapshot;
        }
        return snapshot;
    }
    
    public int getClientReceivedFrame()
    {
        int frameReceipt;
        synchronized(frameReceiptLock)
        {
            frameReceipt = mReceivedFrame;
        }
        return frameReceipt;
    }
    
    public boolean getClientReady()
    {
        boolean clientReady;
        
        synchronized(clientReadyLock)
        {
            clientReady = mClientReady;
        }
        return clientReady;
    } 
    
    public void startPodStreaming()
    {
        System.out.println("StartPodStreaming called...");
        
        if (mStreamingThread.isAlive())
        {
            // need to reset Adam7 states by restarting streaming thread            
            stopPodStreaming();
        }    
        mStreamingThread = new VideoStreamThread();
        mStreamingThread.start();
        
        //update stream status
        VideoClientStreamStatusBelief streamStatusBelief = new VideoClientStreamStatusBelief(WACSAgent.AGENTNAME, true);
        mBeliefManager.put(streamStatusBelief);
    }
    
    public void stopPodStreaming()
    {
        try 
        {
            mStreamingThread.interrupt();
            mStreamingThread.join();
            
            //update stream status
            VideoClientStreamStatusBelief streamStatusBelief = new VideoClientStreamStatusBelief(WACSAgent.AGENTNAME, false);
            mBeliefManager.put(streamStatusBelief);
            System.out.println("Pod video streaming terminated!");
        } 
        catch (InterruptedException ex) 
        {
            System.out.println(ex.getMessage());
        }  
    }    
}