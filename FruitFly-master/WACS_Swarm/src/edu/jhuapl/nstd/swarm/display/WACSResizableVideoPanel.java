/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;
/**
 *
 * @author xud1
 */
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.Inet4Address;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import edu.jhuapl.nstd.swarm.util.Config;
import java.awt.Color;
import java.awt.Font;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.VideoClientRecorderCmdBelief;
import edu.jhuapl.nstd.swarm.WACSDisplayAgent;
import edu.jhuapl.nstd.swarm.belief.Belief;
import edu.jhuapl.nstd.swarm.belief.SatCommImageTransmissionFinishedBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientRecorderStatusBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientStreamCmdBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientFrameRequestBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientSatCommFrameRequestBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientConverterCmdBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientConversionFinishedBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientStreamStatusBelief;
import java.awt.FontMetrics;
import java.io.File;
import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.HashSet;
import javax.swing.JLabel;

public class WACSResizableVideoPanel extends javax.swing.JPanel 
{
    private static final String REMOTE_IP = Config.getConfig().getProperty("EthernetVideoClient.RemoteIp","192.168.100.51");
    private static final short LOCAL_IMAGE_LISTENING_PORT = (short)Config.getConfig().getPropertyAsInteger("EthernetVideoClient.LocalPort", 4995);
    private static final short LOCAL_PROGRESS_LISTENING_PORT = 4994;
    private static final String LOCAL_IMAGE_PATH = Config.getConfig().getProperty("EthernetVideoClient.LocalImagePath","C:\\WACSVideo");
    private static final int MAX_DATAGRAM_SIZE = Config.getConfig().getPropertyAsInteger("EthernetVideoClient.MaxRxPacketSize", 65507);
    private static final int CONNECTIVITY_DROPPED_TIME_MS = Config.getConfig().getPropertyAsInteger("EthernetVideoClient.ConnectionDroppedTime.Ms",3000);
    private static final int SATCOMM_CONNECTIVITY_DROPPED_TIME_MS = Config.getConfig().getPropertyAsInteger("EthernetVideoClient.SatCommConnectionDroppedTime.Ms",10000000);
    private static final int RECORDER_STATUS_UPDATE_RATE_MS = Config.getConfig().getPropertyAsInteger("EthernetVideoClient.RecorderStatusUpdateRate.Ms", 100);
    private static final int DEFAULT_HEIGHT= Config.getConfig().getPropertyAsInteger("EthernetVideoClient.DefaultHeight", 240);
    private static final int DEFAULT_WIDTH = Config.getConfig().getPropertyAsInteger("EthernetVideoClient.DefaultWidth", 320);
    private static final Double ASPECT_RATIO = (double)DEFAULT_HEIGHT/DEFAULT_WIDTH;
    private static final int MAX_PLAYBACK_SPEED = 8;
    private static final int DEFAULT_PLAYBACK_WAIT_TIME_MS = 128;
    private static final int MAX_INTERLACE_PASS = 7;
    private final Object mSatCommLock = new Object();
    private final Object mConnectionErrorLock = new Object();
    private final Object mReadyForFrameLock = new Object();
    private final Object mInterlaceLock = new Object();
    private final Object mCurrentFrameLock = new Object();   
    private final Object mPlaybackLock = new Object();
    private final Object mImageRxProgressLock = new Object();
    
    private boolean mConnectionError = false;
    private boolean mPaused = false;
    private boolean mPlaybackMode = false;
    private boolean mUserInterupt = false;
    private boolean mSatCommMode = false;    
    private boolean mReadyForFrame = true;
    private boolean mStreamState = false; // on(true), off(false)
    private boolean mRecorderState = false; // on(true), off(false)
    private double mPlaybackSpeed = 1.0;
    private double mSatCommImageRxProgress;
    private StreamingThread mStreamingThread;
    private DisplayThread mDisplayThread;
    private VideoPlaybackThread mPlaybackThread;
    private StatusMonitorThread mStatusUpdateThread;
    private BlockingQueue<byte[]> mRxDataQueue = new LinkedBlockingQueue<byte[]>();
    private BlockingQueue<byte[]> mPImageQueue = new LinkedBlockingQueue<byte[]>();
    private BufferedImage mCurrentFrame = null;    
    private BeliefManager mBeliefManager = null;
    private JSlider mPlaybackSlider;
    private JToggleButton mPlayPauseButton;
    private JLabel mMsgLabel;
    private JToggleButton mConvertButton;
    private JToggleButton mStreamButton;
    private JToggleButton mRecordButton;
    private JToggleButton mSatCommButton;
    private File mSessionPath;
    private List<File> mPlaybackList = new ArrayList<File>();
    private VideoPlaybackState mPlaybackState;
    private ImageSize mOptimalImageSize= new ImageSize(320, 240);
    private HashSet mReceivedInterlaces = new HashSet();
    private BufferedImage mProgressiveImage = null;

    public WACSResizableVideoPanel(BeliefManager beliefManager) 
    {
        this.mBeliefManager = beliefManager;        
        mStreamingThread = new StreamingThread();
        mDisplayThread = new DisplayThread();
        mStatusUpdateThread = new StatusMonitorThread();

        initComponents();
        StartVideoClient();
    }          
    
    @Override
    public void paintComponent (Graphics g)
    {        
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g; 
        BufferedImage currentFrame = GetCurrentFrame();
                
        if ( currentFrame != null && !GetConnectionError() )
        {
            BufferedImage resizedImage;
            
            // resize image based on the new dimension
            if(currentFrame.getWidth() != mOptimalImageSize.width)
            {                
                resizedImage = new BufferedImage(mOptimalImageSize.width, mOptimalImageSize.height, currentFrame.getType());
                Graphics2D resizeGraph = resizedImage.createGraphics();
                resizeGraph.drawImage(currentFrame, 0, 0, mOptimalImageSize.width, mOptimalImageSize.height, null);
                resizeGraph.dispose();
            }
            else
            {
                resizedImage = currentFrame;
            }                       
            g2D.drawImage(resizedImage, 0, 0, mOptimalImageSize.width, mOptimalImageSize.height, null);             
        }      
        else
        {
            g2D.setColor(Color.RED);
            g2D.setFont(new Font("Tahoma", Font.PLAIN, 24)); 
            
            String s = "NO VIDEO";
            
            FontMetrics fm = g2D.getFontMetrics(new Font("Tahoma", Font.PLAIN, 24));
            java.awt.geom.Rectangle2D rect = fm.getStringBounds(s, g2D);
            int x = (this.getWidth() - (int)(rect.getWidth()))/2;
            int y = (this.getHeight() - (int)(rect.getHeight()))/2 + fm.getAscent();
            g2D.drawString(s, x, y);                       
        }
                    
        // draw the video stream stats   
        boolean playback = GetPlaybackMode();
        if (playback || mRecorderState)
        {
            Composite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f);
            g2D.setComposite(comp);
            g2D.setColor(Color.RED);
            g2D.setFont(new Font("Tahoma", Font.PLAIN, 12));
            
            if (playback)
            {
                g2D.drawString(new String("Playback Speed : " + (int)(mPlaybackSpeed/0.01) + "%"), 10, 30);
            }
        }
        
        if(GetSatCommMode())
        {

            mMsgLabel.setText(new String("Received: " + (GetReceivedInterlacePass() + 1) + "/" + MAX_INTERLACE_PASS + "[" + 100*(double)Math.round(GetSatCommImageRxProgress()*100)/100 + " %") + "]");
        }
    }
    
    protected void estimateOptimalImageSize(java.awt.event.ComponentEvent evt)
    {               
        if ((int)(evt.getComponent().getWidth()*ASPECT_RATIO) <= evt.getComponent().getHeight())
        {
            mOptimalImageSize.width = evt.getComponent().getWidth();
            mOptimalImageSize.height = (int)(evt.getComponent().getWidth()*ASPECT_RATIO);
        }
        else
        {
            while(mOptimalImageSize.height > evt.getComponent().getHeight())
            {
                mOptimalImageSize.width = (int)(mOptimalImageSize.width * .75);
                mOptimalImageSize.height = (int)(mOptimalImageSize.width*ASPECT_RATIO);                
            }
        }
        this.repaint();
    }
    
    private void StartVideoClient()
    {                    
        File recordingPath = new File(LOCAL_IMAGE_PATH);
        File sessionPath = new File(LOCAL_IMAGE_PATH + "\\" + GetTimeStampString());
        mSessionPath = sessionPath;
        
        // create local video recording path 
        if (!recordingPath.exists())
        {
            if (recordingPath.mkdir())
            {
                System.out.println("Created local video recording directory" + recordingPath);                
            }
            else
            {
                System.out.println("Failed to create local video recording directory" + recordingPath);                
            }   
        }
        
        if (sessionPath.mkdir())
        {
            System.out.println("Created local video session directory" + sessionPath); 
        }
        else
        {
            System.out.println("Failed to creat local video session directory" + sessionPath); 
        }
        
        // initiate streaming on the pod
        VideoClientStreamCmdBelief belief = new VideoClientStreamCmdBelief(WACSDisplayAgent.AGENTNAME, getClientHost(), LOCAL_IMAGE_LISTENING_PORT, true);
        mBeliefManager.put(belief);    
        
        // start receive and display threads
        mStreamingThread.start();
        mDisplayThread.start();   
        mStatusUpdateThread.start();
    }    
    
    private String getClientHost()
    {
        InetAddress localhost;
        InetAddress[] allIp;
        String clientHost = "0.0.0.0";
        
        try
        {
            localhost = Inet4Address.getLocalHost();
            allIp = Inet4Address.getAllByName(localhost.getCanonicalHostName());
            
            for (int i = 0; i < allIp.length; i++) 
            {
                if (allIp[i].getHostAddress().substring(0, 5).equals(REMOTE_IP.substring(0, 5))) 
                {
                    clientHost = allIp[i].getHostAddress();
                    break;
                }
            }            
        }
        catch(UnknownHostException ex)
        {
            System.out.println(ex.getMessage());
        }    
        return clientHost;
    }       
    
    private class DisplayThread extends Thread 
    {
        @Override
        public void run() 
        {
            while (true) 
            {            
                try
                {
                    if(GetSatCommMode())
                    {     
                        byte[] data = mPImageQueue.poll();
                        if (data != null)
                        {
                            UpdateFrameImage(data, GetReceivedInterlacePass());
                        }
                    }
                    else
                    {
                        byte[] data = mRxDataQueue.poll();
                        if (data != null)
                        {                            
                            UpdateFrameImage(data);
                        }
                    }
                    Thread.sleep(100);
                }
                catch (InterruptedException ex) 
                {
                    System.out.println("DisplayThread interrupted!");
                    return;
                }
            }
        }
    }
        
    private class StreamingThread extends Thread 
    {   
        private boolean stop = false;
        public void Stop()
        {
            stop = true;
        }
        
        @Override
        public void run() 
        {
            System.out.println("Video Streaming Thread Started!");
            
            byte[] receiveBuffer = new byte[MAX_DATAGRAM_SIZE];
            DatagramPacket datagram = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            DatagramSocket socket = null;            
            HashSet receivedFrames = new HashSet();
            HashSet receivedUptoDate = new HashSet();
            int receipt = -1;
            
            try 
            {
                socket = new DatagramSocket(LOCAL_IMAGE_LISTENING_PORT);
            } 
            catch (SocketException ex) 
            {
                System.err.println(ex.getMessage());
            }            
            
            while (!stop) 
            {                        
                try 
                {       
                    boolean guaranteed = GetSatCommMode();
                    boolean ready = GetReadyForFrame();
                    Belief belief;                    
                    
                    if(ready)
                    {                                                 
                        if(!guaranteed)
                        {
                            //System.out.println("VideoClient: Non-Guaranteed Frame Request Belief!");
                            belief = new VideoClientFrameRequestBelief(WACSDisplayAgent.AGENTNAME);
                            socket.setSoTimeout(CONNECTIVITY_DROPPED_TIME_MS);
                        }
                        else
                        {
                            System.out.println("VideoClient: Sent guaranteed frame request, receipt = " + receipt + "\n");
                            belief = new VideoClientSatCommFrameRequestBelief(WACSDisplayAgent.AGENTNAME, receipt, false);
                            socket.setSoTimeout(SATCOMM_CONNECTIVITY_DROPPED_TIME_MS);
                        }
                                
                        mBeliefManager.put(belief); 
                        socket.receive(datagram);       
                        if(GetConnectionError())
                        {
                            // re-established connection, clear error
                            SetConnectionError(false);
                        }
                        
                        if (!guaranteed) 
                        {
                            System.out.println("VideoClient: Received non-guaranteed frame!" + "\n");
                            mRxDataQueue.put(datagram.getData().clone());
                        } 
                        else 
                        {                                                                          
                            // unpack progressive video data (packet = (short)length + frameData
                            byte[] data = datagram.getData().clone();
                            byte[] payloadLength = new byte[4];
                            ByteArrayInputStream in = new ByteArrayInputStream(data);
                            in.read(payloadLength, 0, payloadLength.length);
                            int length = ByteBuffer.wrap(payloadLength).getInt();                                                                                    
                            
                            if (length > 0)
                            {                               
                                byte[] frameData = new byte[length];
                                in.read(frameData, 0, frameData.length);
                                receipt = (BitSet.valueOf(frameData)).hashCode();
                                System.out.println("VideoClient: Received guaranteed video data, size = " + length + "; " + "hash = " + receipt + "\n");                                

                                if (receivedUptoDate.add(receipt)) 
                                {
                                    receivedFrames.add(receipt);
                                    SetReceivedInterlaces((HashSet) receivedFrames.clone());
                                    mPImageQueue.put(frameData);                                    

                                    if (receivedFrames.size() == MAX_INTERLACE_PASS) 
                                    {
                                        //send the receipt for the last interlace
                                        belief = new VideoClientSatCommFrameRequestBelief(WACSDisplayAgent.AGENTNAME, receipt, true);
                                        mBeliefManager.put(belief);

                                        // received all 7 interlaces, reset interlace pass count and previous hashcode, also wipe progressive image cache
                                        receipt = -1;
                                        SetReadyForFrame(false);
                                        receivedFrames.clear();
                                        
                                        // enable button and untoggle 
                                        System.out.println("VideoClient: Received the last pass!" + "\n");                                                                             
                                    }
                                } 
                                else 
                                {
                                    System.out.println(receipt + "has already been received!" + "\n");
                                }
                            }
                        }                                                                                               
                    } 
                }
                catch (IOException ex) 
                {
                    // socket receive timed out
                    System.out.println("socket timedout, retrying..." + "\n");   
                    if(!GetSatCommMode())
                    {
                        SetConnectionError(true);
                    }
                    repaint();
                }
                catch (InterruptedException ex)
                {                   
                    System.out.println(ex.getMessage());
                }                
            }
            
            if (stop) 
            {
                System.out.println("Video client streaming stopping..." + "\n");
                socket.disconnect();
                socket.close();
                SetConnectionError(true);
                repaint();
                return;
            }
        }
    }         
    
    private class VideoPlaybackThread extends Thread
    {                
        private WACSResizableVideoPanel mPanelHandle;
        private VideoPlaybackState mState;
        private int mCurrentIndex;
        
        VideoPlaybackThread(VideoPlaybackState state, WACSResizableVideoPanel panelHandle)
        {
            mState = state;
            mPanelHandle = panelHandle;
            mPlaybackSlider.setMaximum(mState.mPlaybackFiles.length-1);
            mPlaybackSlider.setMinimum(0); 
        }              
        
        public void setPlaybackWaitTime(int waitTime)
        {
            mState.mPlaybackWaitTime_ms = waitTime;
        }
        
        @Override
        public void run()
        {
            while (true)
            {
                for (mCurrentIndex = mState.mStartingIndex; mCurrentIndex < mState.mPlaybackFiles.length; mCurrentIndex++)
                {
                    try
                    {
                        SetCurrentFrame(ImageIO.read(mState.mPlaybackFiles[mCurrentIndex]));
                        mPanelHandle.repaint();
                        mPlaybackSlider.setValue(mCurrentIndex);
                        
                        if (mCurrentIndex ==  mState.mPlaybackFiles.length - 1)
                        {
                            mState.mStartingIndex = 0;
                        }
                        
                        Thread.sleep(mState.mPlaybackWaitTime_ms);
                    }
                    catch (IOException ex)
                    {
                        System.out.println(ex.getMessage());
                    }
                                       
                    catch (InterruptedException ex)                   
                    {                      
                        // save current playback state
                        mState.mStartingIndex = mCurrentIndex;
                        mPlaybackState = mState; 
                        mUserInterupt = false;
                        return;
                    }                    
                }
            }
        }
    }
    
    private class StatusMonitorThread extends Thread
    {        
        @Override
        public void run()
        {
            long mLastConverterUpdate = -1;
            long mLastSatCommUpdate = -1;
            DatagramSocket socket = null;
            try
            {
                socket = new DatagramSocket(LOCAL_PROGRESS_LISTENING_PORT);
                socket.setSoTimeout(3000);
            }
            catch(SocketException ex)
            {
                ex.printStackTrace();
            }
            
            while(true)
            {
                VideoClientStreamStatusBelief streamBelief = (VideoClientStreamStatusBelief)mBeliefManager.get(VideoClientStreamStatusBelief.BELIEF_NAME);
                VideoClientRecorderStatusBelief recorderBelief = (VideoClientRecorderStatusBelief)mBeliefManager.get(VideoClientRecorderStatusBelief.BELIEF_NAME);                            
                VideoClientConversionFinishedBelief conversionFinishedBelief = (VideoClientConversionFinishedBelief)mBeliefManager.get(VideoClientConversionFinishedBelief.BELIEF_NAME);
                SatCommImageTransmissionFinishedBelief satCommImageTxFinishedBelief = (SatCommImageTransmissionFinishedBelief)mBeliefManager.get(SatCommImageTransmissionFinishedBelief.BELIEF_NAME);
                
                if(streamBelief != null && streamBelief.getStreamState() != mStreamState)
                {
                    // update stream button
                    mStreamState =streamBelief.getStreamState();
                    mStreamButton.setEnabled(true);       
                    
                    if(mStreamState == true)
                    {                            
                        System.out.println("Pod video streaming started!");
                        SetReadyForFrame(true);
                        mPlayPauseButton.setEnabled(true);
                        mPlayPauseButton.setSelected(true);
                        mPlayPauseButton.setText("Pause");   
                        mSatCommButton.setEnabled(true);
                        mStreamButton.setText("Stop Stream"); 
                        mPaused = false;              
                    }
                    else
                    {               
                        System.out.println("Pod video streaming terminated!");
                        mSatCommButton.setEnabled(false);
                        mStreamButton.setText("Stream");
                        mPlayPauseButton.setEnabled(false);
                        mPlayPauseButton.setSelected(false);
                        mPlayPauseButton.setText("Play");                                                
                    }
                }
                
                if(recorderBelief != null && recorderBelief.getState().isRecording != mRecorderState)
                {
                    mRecorderState = recorderBelief.getState().isRecording;
                    mRecordButton.setEnabled(true);                    
                    
                    if (mRecorderState) 
                    {                       
                        System.out.println("Pod recorder started!");   
                    }
                    else
                    {                      
                        System.out.println("Pod recorder stopped!");
                        mRecordButton.setSelected((false));
                    }                                                                                         
                }
                
                if (satCommImageTxFinishedBelief != null && satCommImageTxFinishedBelief.getTimeStamp().getTime() > mLastSatCommUpdate && GetSatCommMode())
                {
                    // SatComm Image Transmission Completed                    
                    mLastSatCommUpdate = satCommImageTxFinishedBelief.getTimeStamp().getTime();
                    mSatCommButton.setEnabled(true);
                    mSatCommButton.setSelected(false);     
                    mStreamButton.setEnabled(true);
                    mPlayPauseButton.setEnabled(true);
                }
                
                if(conversionFinishedBelief != null && conversionFinishedBelief.getTimeStamp().getTime() > mLastConverterUpdate)
                {
                    // Video Conversion Completed                   
                    mLastConverterUpdate = conversionFinishedBelief.getTimeStamp().getTime();                                                     
                    mConvertButton.setEnabled(true); 
                    mConvertButton.setSelected(false);
                }                
                
                if (GetSatCommMode()) 
                {
                    byte[] receiveBuffer = new byte[MAX_DATAGRAM_SIZE];
                    DatagramPacket datagram = new DatagramPacket(receiveBuffer, receiveBuffer.length);          

                    try 
                    {
                        socket.receive(datagram);
                        
                        byte[] data = datagram.getData().clone();
                        byte[] satCommImgProgress = new byte[8];
                        ByteArrayInputStream in = new ByteArrayInputStream(data);
                        in.read(satCommImgProgress, 0, satCommImgProgress.length);
                        double progress = ByteBuffer.wrap(satCommImgProgress).getDouble();
                        
                        SetSatCommImageRxProgress(progress);
                        UpdateDisplayPanel();                                                
                    } 
                    catch (SocketException ex) 
                    {
                        ex.printStackTrace();                        
                    }       
                    catch (IOException ex)
                    {
                        // Image Transmission status timeout ignored
                    }
                }
                
                try
                {
                    Thread.sleep(RECORDER_STATUS_UPDATE_RATE_MS);                    
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    private void UpdateDisplayPanel()
    {
        this.repaint();
    }
    
    private void UpdateFrameImage(byte[] image, int pass)
    {          
        if (!GetPlaybackMode() && !mUserInterupt) 
        {                     
            if (pass == 0)
            {
                // start a new iteration of progressive images
                mProgressiveImage = new BufferedImage(320, 240, BufferedImage.TYPE_BYTE_GRAY);
            }
            
            BufferedImage currentFrame = Adam7.Reconstruct(mProgressiveImage, image, pass);                                                
            SetCurrentFrame(currentFrame);                   
            this.repaint();
            
            // write the final progressive image to local storage
            try 
            {
                File outputFile = new File(mSessionPath.toString() + "\\" + System.currentTimeMillis() + "_Guaranteed_" + (pass + 1) + ".jpg");
                ImageIO.write(currentFrame, "jpg", outputFile);
            } 
            catch (IOException ex) 
            {
                System.out.println(ex.getMessage());
            }
            
            if (pass == 6)
            {
                // reset the recieved interlace count to zero
                SetReceivedInterlaces(new HashSet());
            }
        }      
    }
    
    private void UpdateFrameImage(byte[] incomingStream) 
    {
        try
        {
            InputStream in = new ByteArrayInputStream(incomingStream);
            BufferedImage img = ImageIO.read(in);
            File outputFile = new File(mSessionPath.toString() + "\\" + System.currentTimeMillis() + ".jpg");
            
            if(img != null)
            {          
                ImageIO.write(img, "jpg", outputFile);
                mPlaybackList.add(outputFile);

                if (!GetPlaybackMode() && !mUserInterupt) {
                    SetCurrentFrame(img);
                    if (!mPaused) 
                    {
                        this.repaint();
                    }
                }
            }
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());            
        }        
    }            
    
    private class ImageSize
    {        
        private int width;
        private int height;
        
        ImageSize(int lWidth, int lHeight)
        {
            width = lWidth;
            height = lHeight;
        }
    }
    
    private class VideoPlaybackState
    {
        private File[] mPlaybackFiles;
        private int mStartingIndex = 0;
        private int mPlaybackWaitTime_ms = DEFAULT_PLAYBACK_WAIT_TIME_MS;
        
        VideoPlaybackState(File[] lFiles, int lIndex, int lWaitTime)
        {
            mPlaybackFiles = lFiles;
            mStartingIndex = lIndex;
            mPlaybackWaitTime_ms = lWaitTime;
        }
    }
    
    protected void PlayPauseButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        // toggle button
        mPaused = !mPaused;
        
        if(GetSatCommMode())
        {
            SetSatCommMode(false);
            SetReadyForFrame(true);
            mPaused = false;
            
            try
            {
                mDisplayThread.interrupt();
                mDisplayThread.join();
            }
            catch(InterruptedException ex)
            {                
            }            
            System.out.println("Display thread stopped!");
            mDisplayThread = new DisplayThread();
            mDisplayThread.start();                                        
        }
        
        if (GetPlaybackMode())
        {            
            if( mPaused )
            {
                // pause playback if pause button is pressed
                mPlaybackThread.interrupt();                
            }
            else
            {
                // resume playback if pause button is pressed 
                if( mPlaybackThread == null || !mPlaybackThread.isAlive())
                {
                    mPlaybackThread = new VideoPlaybackThread(mPlaybackState, this);
                    mPlaybackThread.start();
                }
            }
        }
    }
    
    protected void PlaybackActionPerformed(java.awt.event.ActionEvent evt)
    {
        // toggle button
        boolean playback = GetPlaybackMode();
        SetPlaybackMode(!playback);
        
        if(GetPlaybackMode() && (mPlaybackThread == null || !mPlaybackThread.isAlive()))
        {             
            // transition to video playback mode
            SetConnectionError(false);
            mPlaybackSlider.setEnabled(true);
            File[] files = new File[mPlaybackList.size()];
            mPlaybackList.toArray(files);
            
            mPlaybackState = new VideoPlaybackState(files, 0, DEFAULT_PLAYBACK_WAIT_TIME_MS);
            mPlaybackThread = new VideoPlaybackThread(mPlaybackState, this);
            mPlaybackThread.start();
            
            if (mPaused)
            {
                // untoggle pause button 
                mPlayPauseButton.doClick();
            }
        }
        else
        {
            // exit playback mode
            mPlaybackThread.interrupt();
            mPlaybackSlider.setValue(0);  
            mPlaybackSlider.setEnabled(false);
            
            if (mPaused)
            {
                // untoggle pause button
                mPlayPauseButton.doClick();
            }
        }
    }
    
            
    protected void RecordButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        // toggle button
        boolean recordCmd = !mRecorderState;
        //SetRecordMode(!record);
        mRecordButton.setEnabled(false);
        
        VideoClientRecorderCmdBelief belief = new VideoClientRecorderCmdBelief(WACSDisplayAgent.AGENTNAME, recordCmd);
        mBeliefManager.put(belief);           
    }
    
    protected void StreamActionPerformed(java.awt.event.ActionEvent evt)
    {
        boolean streamCmd = !mStreamState;                        
        VideoClientStreamCmdBelief belief = new VideoClientStreamCmdBelief(WACSDisplayAgent.AGENTNAME, getClientHost(), LOCAL_IMAGE_LISTENING_PORT, streamCmd);
        mBeliefManager.put(belief);          
        mStreamButton.setEnabled(false);
        mPlayPauseButton.setEnabled(false);
        mSatCommButton.setEnabled(false);
        
        if(!streamCmd)
        {
            SetSatCommMode(false);            
            try
            {
                mStreamingThread.Stop();
                mStreamingThread.join();
            }
            catch(InterruptedException ex)
            {
                System.out.println(ex.getMessage());
            }
        }
        else
        {
            // restart video streaming
            mStreamingThread = new StreamingThread();
            mStreamingThread.start();
            SetConnectionError(false);           
        }        
    }
        
    protected void SatCommImageButton(java.awt.event.ActionEvent evt)
    {        
        if( !GetReadyForFrame() || !GetSatCommMode())
        {
            // if not currently ready for new frame
            SetSatCommMode(true);    
            SetReadyForFrame(true);
            mPaused = true;
            mSatCommButton.setEnabled(false);
            mPlayPauseButton.setEnabled(false);
            mStreamButton.setEnabled(false);
        }
    }
    
    protected void ConvertVideoActionPerformed(java.awt.event.ActionEvent evt)
    {        
        VideoClientConverterCmdBelief belief = new VideoClientConverterCmdBelief(WACSDisplayAgent.AGENTNAME);
        mBeliefManager.put(belief);
        
        mConvertButton.setEnabled(false);
    }
    
    protected void SlowDownActionPerformed(java.awt.event.ActionEvent evt)
    {            
        if (GetPlaybackMode())
        {
            if ((mPlaybackSpeed / 2) >= (1/MAX_PLAYBACK_SPEED))
            {
                mPlaybackSpeed /= 2;
                mPlaybackThread.setPlaybackWaitTime((int)(DEFAULT_PLAYBACK_WAIT_TIME_MS/mPlaybackSpeed));            
            }         
        }
    }
    
    protected void SpeedUpActionPerformed(java.awt.event.ActionEvent evt)
    {        
        if (GetPlaybackMode())
        {
            if ( (mPlaybackSpeed * 2) <= MAX_PLAYBACK_SPEED)
            {
                mPlaybackSpeed *= 2;
                mPlaybackThread.setPlaybackWaitTime((int)(DEFAULT_PLAYBACK_WAIT_TIME_MS/mPlaybackSpeed));            
            }     
        }
    }
    
    protected void SliderPositionSelected(java.awt.event.MouseEvent evt)
    {    
        if (GetPlaybackMode())
        {
            // restart video playback thread with its previous state and the new slider value
            JSlider source = (JSlider)evt.getSource();
            if(!mPlaybackThread.isAlive())
            {                
                mPlaybackState.mStartingIndex = source.getValue();
                mPlaybackThread = new VideoPlaybackThread(mPlaybackState, this);
                mPlaybackThread.start();
            }
        }
    }
    
    protected void SliderDragged(java.awt.event.MouseEvent evt)
    {
        if (GetPlaybackMode())
        {
            // signal playback thread to stop and also prevent live stream from updating the screen
            // while the slider is still being adjusted
            mUserInterupt = true;
            mPlaybackThread.interrupt();
        }
    }
    
    private int GetReceivedInterlacePass()
    {
        int pass = 0;
        synchronized(mInterlaceLock)
        {
            if (mReceivedInterlaces.size() > 0)
            {
                pass = mReceivedInterlaces.size() - 1;
            }
        }        
        return pass;
    }
    
    private void SetReceivedInterlaces(HashSet received)
    {
        synchronized(mInterlaceLock)
        {
            mReceivedInterlaces = received;
        }
    }
    
    private boolean GetSatCommMode()
    {
        boolean satCommMode;
        synchronized(mSatCommLock)
        {
            satCommMode = mSatCommMode;
        }
            
        return satCommMode;
    }
    
    private void SetSatCommMode(boolean satCommMode)
    {
        synchronized(mSatCommLock)
        {
            mSatCommMode = satCommMode;
        }
    }
    
    private void SetReadyForFrame(boolean ready)
    {
        synchronized(mReadyForFrameLock)
        {
            mReadyForFrame = ready;
        }
    }
    
    private boolean GetReadyForFrame()
    {
        boolean ready;
        synchronized(mReadyForFrameLock)
        {
            ready = mReadyForFrame;
        }
        return ready;
    }
    
    private BufferedImage GetCurrentFrame()
    {
        BufferedImage currentFrame;
        synchronized(mCurrentFrameLock)
        {
            currentFrame = mCurrentFrame;
        }
        return currentFrame;
    }
    
    private void SetCurrentFrame(BufferedImage currentFrame)
    {
        synchronized(mCurrentFrameLock)
        {
            mCurrentFrame = currentFrame;
        }
    }
    
    public void SetRecordButtonHandle(JToggleButton recordButton)
    {
        mRecordButton = recordButton;
    }
    
    public void SetStreamButtonHandle(JToggleButton streamButton)
    {
        mStreamButton = streamButton;
    }
    
    public void SetRecConversionButtonHandle(JToggleButton convertButton)
    {
        mConvertButton = convertButton;
    }
    
    public void SetSatCommButtonHandle(JToggleButton satCommButton)
    {
        mSatCommButton = satCommButton;
    }
    
    public void SetPlaybackSliderHandle(JSlider playbackSlider)
    {
        mPlaybackSlider = playbackSlider;
    }
    
    public void SetPlayPauseButtonHandle(JToggleButton playPauseButton)
    {
        mPlayPauseButton = playPauseButton;
    }
    
    public void SetMsgLabelHandle(JLabel msgLabel)
    {
        mMsgLabel = msgLabel;
    }
    
    private String GetTimeStampString()
    {
        Timestamp currentTime = new Timestamp(new Date().getTime());
        java.text.DateFormat df = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss"); 
        return df.format(currentTime);
    }
    
    private void SetConnectionError(boolean status)
    {
        if(GetPlaybackMode() || GetSatCommMode())
        {
            status = false;
        }
        
        synchronized(mConnectionErrorLock)
        {
            mConnectionError = status;
        }
    }
    
    private boolean GetConnectionError()
    {
        boolean mConnError;
        
        synchronized(mConnectionErrorLock)
        {
            mConnError = mConnectionError;
        }
        return mConnError;
    }    
    
    private void SetPlaybackMode(boolean playback)
    {
        synchronized(mPlaybackLock)
        {
            mPlaybackMode = playback;
        }                    
    }
    
    private boolean GetPlaybackMode()
    {
        boolean playback;
        
        synchronized(mPlaybackLock)
        {
            playback = mPlaybackMode;
        }
        
        return playback;
    }   
    
    private void SetSatCommImageRxProgress(double progress)
    {
        synchronized(mImageRxProgressLock)
        {
            mSatCommImageRxProgress = progress;
        }
    }
    
    private double GetSatCommImageRxProgress()
    {
        double progress;
        synchronized(mImageRxProgressLock)
        {
            progress = mSatCommImageRxProgress;
        }
        
        return progress;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        setMinimumSize(new java.awt.Dimension(320, 240));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 336, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 251, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
