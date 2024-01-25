/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods;

import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxCompositeHistogramMessage;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacParticleCountMessage;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMessageListener;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.cbrnPods.messages.Pod.podHeartbeatMessage;
import edu.jhuapl.nstd.cbrnPods.messages.Pod.podShutdownLogCommand;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic interface to CBRN Pods, has basic communication functions
 *
 * @author humphjc1
 */
public abstract class cbrnPodGenericInterface {

    /**
     * IP address of rabbit board we connect to
     */
    private InetAddress m_RemoteIp;
    
    /**
     * Port number on rabbit board to connect to
     */
    private int m_RemotePort;
    
    /**
     * Our IP address
     */
    protected InetAddress m_LocalIp;
    
    /**
     * Our port number to connect with rabbit board
     */
    protected int m_LocalPort;
    
    /**
     * UDP network socket connection
     */
    private DatagramSocket m_Socket = null;
    
    /**
     * Thread to read messages from UDP network, passed from Rabbit
     */
    protected ReadThread readThread = null;
    
    /**
     * Thread to write messages to UDP network, passed to Rabbit
     */
    protected WriteThread writeThread;
    
    /**
     * Thread to send biopod (board messages) to listeners
     */
    ListenerThread podListenerThread;
    
    /**
     * Logger for all message traffic
     */
    static final cbrnPodsInterfaceLogger messageLogger = new cbrnPodsInterfaceLogger ("log");

    /**
     * Bel Manager
     */
    BeliefManager beliefManager;
    
    
    
    /**
     * Open an interface to a rabbit board on a WACS BioPod
     * 
     * @param localIp Local IP address
     * @param localPort Local port number
     * @param remoteIp Rabbit board IP address
     * @param remotePort rabbit board port number
     * @param belMgr BeliefManager from Swarm.  Can be null.
     */
    public cbrnPodGenericInterface(InetAddress localIp, int localPort, InetAddress remoteIp, int remotePort, BeliefManager belMgr)
    {
        beliefManager = belMgr;
        m_RemoteIp = remoteIp;
        m_RemotePort = remotePort;
        m_LocalIp = localIp;
        m_LocalPort = localPort;
        
        
        try 
        {
            //Connect to socket for comms with Rabbit board
            m_Socket = new DatagramSocket(m_LocalPort);
            m_Socket.setSoTimeout(200);
        } 
        catch (SocketException ex) 
        {
            ex.printStackTrace();
        }

        //Thread to read messages from rabbit board
        readThread = new ReadThread(this, m_Socket);
        //Thread to write messages to rabbit board
        writeThread = new WriteThread(m_Socket,remoteIp,remotePort);
        
        //spin up threads
        Thread rt = new Thread(readThread);
        rt.setName("WACS-CBRNReadThread" + m_LocalPort);
        rt.start();
        Thread wt = new Thread(writeThread);
        wt.setName ("WACS-CBRNWriteThread" + m_LocalPort);
        wt.start();
        
        /**
         * Start listener thread for pod messages
         */
        podListenerThread  = new ListenerThread (readThread, cbrnSensorTypes.RABBIT_BOARD);
        podListenerThread.addMessageType (new podHeartbeatMessage());
        Thread pt = new Thread(podListenerThread);
        pt.setName ("WACS-PodListenerThread" + m_LocalPort);
        pt.start();

    }
    
    /**
     * Notify all internal threads to shut down.
     */
    public void shutdown ()
    {
        sendCommand_AttachIP(new podShutdownLogCommand());
        readThread.setShutdown(true);
        writeThread.setShutdown(true);
        podListenerThread.setShutdown(true);
    }

    /**
     * Send a command to the rabbit board
     * @param m Command to be sent
     */
    public void sendCommand(cbrnPodCommand m) {
        writeThread.writeMessage(m);
    }

    /**
     * Sent a command to the rabbit board, but first attach the local (from) IP address and port to the message
     *
     * @param m Command to be sent
     */
    public void sendCommand_AttachIP(cbrnPodCommand m) {
        m.setFromIP(((m_LocalIp.getAddress()[0] & 0xFF) << 24) |
                ((m_LocalIp.getAddress()[1] & 0xFF) << 16) |
                ((m_LocalIp.getAddress()[2] & 0xFF) << 8) |
                ((m_LocalIp.getAddress()[3] & 0xFF)));
        m.setFromPort(m_LocalPort);
        sendCommand(m);
    }

    /**
     * Add a listener for messages from the pod
     *
     * @param messageType Type of message listener wants to receive
     * @param listener Listener to receive messages
     */
    void addPersistentPodListener(int messageType, cbrnPodMessageListener listener)
    {
        podListenerThread.addPersistentListener(messageType, listener);
    }

    /**
     * Insert a message into the read thread.  Use this to manually add a message to be sent to listeners of the message even
     * though it didn't come from the pods.  Generally, this is used by the RN processors to send processing results messages.
     *
     * @param newMsg Message to be sent
     */
    void insertMessage(cbrnPodMsg newMsg) 
    {
        readThread.insertMessage(newMsg);
    }
    
    /**
     * Remove a listener for messages from the pod
     *
     * @param messageType Type of message listener wants to stop receiving
     * @param listener Listener that was receiving messages
     */
    void removePersistentPodListener(int messageType, cbrnPodMessageListener listener)
    {
        podListenerThread.removePersistentListener(messageType, listener);
    }
    
    
    
    /**
     * This thread is responsible for listening to the read threads queue.
     * When a new message appears on the queue, all relevant listeners are notified.
     * This decouples listening and reading, the only caveat is that the listeners
     * may not complete the first call from the listener before they are called again.
     */
    protected class ListenerThread implements Runnable {
        
        /**
         * List of listeners that will be called for every message of specified type
         */
        private Map<Integer, List<cbrnPodMessageListener>> persistentListeners;

        /**
         * Lock object for persistentListeners
         */
        private final Object persistentListenersMutex = new Object();

        /**
         * Internal list of message response types that are supported by this interface. All other messages will be dropped on the floor.
         */
        private List<cbrnPodMsg> messageTemplates;

        /**
         * Thread that reads messages from the rabbit board
         */
        private ReadThread readThread;

        /**
         * Shutdown lock object
         */
        private final Object shutdownMutex = new Object();

        /**
         * If true, gracefully shutdown the thread
         */
        private boolean shutdown = false;
        
        /**
         * Type of sensor that we are listening for messages from
         */
        private int sensorType;

        /**
         * Create object to send messages to listeners after being read from a ReadThread
         *
         * @param readThread Thread that reads the messages
         * @param sensorType Sensor index this listener thread is servicing
         */
        protected ListenerThread(ReadThread readThread, int sensorType)
        {
            messageTemplates = new ArrayList<cbrnPodMsg>();
            this.sensorType = sensorType;

            persistentListeners = Collections.synchronizedMap(new HashMap<Integer, List<cbrnPodMessageListener>>());

            this.readThread = readThread;
        }

        /**
         * Add a listener for messages of a given message type
         *
         * @param key Message type index
         * @param listener Listener to receive messages
         */
        protected void addPersistentListener(int key, cbrnPodMessageListener listener)
        {
            synchronized (persistentListenersMutex)
            {
                if (persistentListeners.get(key) == null) {
                    persistentListeners.put(key, new ArrayList<cbrnPodMessageListener>());
                }
                persistentListeners.get(key).add(listener);
            }
        }

        /**
         * Remove a listener for messages of a given message type
         *
         * @param key Message type index
         * @param listener Listener to stop receiving messages
         */
        protected void removePersistentListener(int key, cbrnPodMessageListener listener) 
        {
            synchronized (persistentListenersMutex)
            {
                if (persistentListeners.get(key) == null) {
                    return;
                }
                persistentListeners.get(key).remove(listener);
            }
        }

        /**
         * Run listener thread, sending messages to listeners who have registered for them.
         */
        public void run()
        {
            while (true)
            {
                if (isShutdown())
                    break;
             
                cbrnPodMsg m = readThread.readMessage(1000, sensorType);
                //If we have received a message...
                if (m != null)
                {
                    cbrnPodMsg response = null;

                    //Check to see if the message is a supported parseable message
                    for (cbrnPodMsg t : messageTemplates)
                    {
                        if (t.isInstanceOf(m))
                        {
                            try 
                            {
                                //Parse the message bytes and form the message object
                                response = (cbrnPodMsg) t.getClass().getConstructor().newInstance();
                                response.parseBioPodMessage(m);
                            } catch (InstantiationException ex) {
                                ex.printStackTrace();
                            } catch (IllegalAccessException ex) {
                                ex.printStackTrace();
                            } catch (IllegalArgumentException ex) {
                                ex.printStackTrace();
                            } catch (InvocationTargetException ex) {
                                ex.printStackTrace();
                            } catch (NoSuchMethodException ex) {
                                ex.printStackTrace();
                            } catch (SecurityException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    synchronized (persistentListenersMutex)
                    {
                        //For all listeners who have registered for the new message, send a copy to them
                        cbrnPodMsg messageForListener = response != null ? response : m;
                        List<cbrnPodMessageListener> pl = persistentListeners.get(messageForListener.getMessageType());
                        if (pl != null && pl.size() > 0)
                        {
                            for (cbrnPodMessageListener sml : pl) {
                                sml.handleMessage(messageForListener);
                            }
                        }
                    }
                }
                else
                {
                    try {
                        Thread.sleep (100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(cbrnPodGenericInterface.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        /**
         * Return whether the shutdown flag has been set
         * @return Shutdown flag
         */
        protected boolean isShutdown()
        {
            boolean value;
            synchronized (shutdownMutex) {
                value = shutdown;
            }
            return value;
        }

        /**
         * @param shutdown the shutdown to set
         */
        protected void setShutdown(boolean shutdown) {
            synchronized (shutdownMutex) {
                this.shutdown = shutdown;
            }
        }

        /**
         * Adds a message type that this listener thread understands and can parse.  All message types must be defined through
         * here to ensure they are parseable.
         *
         * @param msgTemplate Template of message type that can be parsed by this thread
         */
        void addMessageType(cbrnPodMsg msgTemplate)
        {
            messageTemplates.add(msgTemplate);
        }
    }
    
    
    /**
     * This thread is responsbile for actively reading the incoming port.
     * Once a complete message is received, it is added to a queue and the listener thread
     * is notified.
     */
    protected class ReadThread implements Runnable 
    {
        /**
         * Queue of messages read from the data port and ready to be forwarded to listeners through the listener thread
         */
        private ConcurrentLinkedQueue<cbrnPodMsg> incomingResponses;

        /**
         * Timestamp when the last message was received
         */
        private long timeOfLastReceive = 0;

        /**
         * Lock object for timeOfLastReceive
         */
        private final Object timeMutex = new Object();

        /**
         * If true, shutdown thread
         */
        private Boolean shutdown = false;

        /**
         * Lock object for shutdown variable
         */
        private final Object shutdownMutex = new Object();
        
        /**
         * Socket to read messages from
         */
        private DatagramSocket socket = null;

        /**
         * Buffer to read messages into.  Max message size is 600 bytes, plus 12 bytes header
         */
        private byte[] buf = new byte[600 + 12];

        /**
         * DatagramPacket buffer to read messages into
         */
        private DatagramPacket packetBuffer;

        /**
         * Max time we'll spin on a message waiting for someone to claim it.  If no one does (no one is registered),
         * we'll wait this long and then skip it.
         */
        private long maxLagTimeMs = 5000;
        /**
         * Counter used for tracking lag time in listening for messages.
         */
        private long startLagTimeMs = -1;
        /**
         * Sensor types that have timed out for listeners.  We'll just permanently skip them from now on.
         */
        private Vector<Integer> skipSensorTypes = new Vector<Integer> ();



        /**
         * Create object to read messages from pods
         *
         * @param bioPod Interface to read from.  Currently not used and can be null
         * @param socket Socket to read messages from.
         */
        protected ReadThread(cbrnPodGenericInterface bioPod, DatagramSocket socket)
        {
            incomingResponses = new ConcurrentLinkedQueue<cbrnPodMsg>();
            this.socket = socket;
            packetBuffer = new DatagramPacket(buf, buf.length);
        }

        /**
         * Insert a message into the incomingResponses buffer.  Manually adds a message that didn't come from the interface but
         * will be sent to listener thread
         *
         * @param m Message to be inserted
         */
        public void insertMessage (cbrnPodMsg m)
        {
            synchronized (incomingResponses) {
                incomingResponses.add(m);
                incomingResponses.notifyAll();
            }
        }

        /**
         * Start read thread
         *
         */
        public void run() {
            cbrnPodMsg m;
            boolean received = false;

            while (true)
            {
                try
                {
                    if (isShutdown())
                        break;

                    if (socket == null)
                    {
                        Thread.sleep (1000);
                        System.err.println ("Null socket in cbrnPodInterface readthread port " + (socket==null?-1:socket.getLocalPort()));
                        continue;
                    }

                    //Read data from socket
                    synchronized (socket) {
                        try {
                            socket.receive(packetBuffer);
                            received = true;
                        } catch (IOException ex) {
                            received = false;
                        }
                    }

                    //If data has been read
                    if (received) 
                    {
                        m = new cbrnPodMsg();
                        //Step through each byte in the message and parse until message is fully read
                        for (int i = 0; i < packetBuffer.getLength(); i++)
                        {
                            m.parseByte(packetBuffer.getData()[i]);
                            //Once message is fully read...
                            if (m.isValidMessage())
                            {
                                //Add message to queue to be sent to listener thread
                                synchronized (incomingResponses)
                                {
                                    incomingResponses.add(m);
                                    incomingResponses.notifyAll();
                                    messageLogger.logMessage(m);
                                }
                                synchronized (timeMutex)
                                {
                                    timeOfLastReceive = new Date().getTime();
                                }
                                
                            }
                        }
                    }
                    else
                    {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(cbrnPodGenericInterface.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (Exception e)
                {
                    System.err.println ("Error in received message on port " + socket.getLocalPort());
                    e.printStackTrace();
                }
            }
        }

        /**
         * Access the next message in the queue for the given sensor type.
         *
         * @param timeout Max time to spend waiting for a new message
         * @param sensorType Sensor to return a message for
         * @return next message for the requested sensor, or null
         */
        protected cbrnPodMsg readMessage(long timeoutMs, int sensorType)
        {
            cbrnPodMsg m = null;

            synchronized (incomingResponses) 
            {
                //If any type of message is ready to be sent out...
                if (incomingResponses.size() > 0) 
                {
                    //Peek at the message but don't remove from queue
                    m = incomingResponses.peek();
                }
                else
                {
                    //Otherwise, wait for a bit or until next message if received...
                    try
                    {
                        incomingResponses.wait(timeoutMs);
                    } 
                    catch (InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }
                    if (incomingResponses.size() > 0)
                    {
                        m = incomingResponses.peek();
                    }
                }


                boolean skipType = false;
                //If a message was available in the queue...
                if (m != null)
                {
                    //If the message is not from the sensor that was requested
                    if (m.getSensorType() != sensorType)
                    {
                        if (startLagTimeMs < 0)
                        {
                            //If this is our first look at the message, start a timestamp counter so we know how long its been
                            //since this message was first requested
                            startLagTimeMs = System.currentTimeMillis();
                        }
                        else if (System.currentTimeMillis() - startLagTimeMs > maxLagTimeMs)
                        {
                            //If this message hasn't been used for long enough (defined by maxLagTimeMs), ignore it and permanently ignore
                            //this message type.  Can occur if not all message were requested.
                            skipType = true;
                            skipSensorTypes.add (m.getSensorType());
                        }
                        else if (skipSensorTypes.size() > 0)
                        {
                            //If this message type has already been flagged as one we are ignoring, just throw it away now and move on
                            for (int i = 0; i < skipSensorTypes.size(); i ++)
                            {
                                if (m.getSensorType() == skipSensorTypes.get(i))
                                {
                                    skipType = true;
                                    break;
                                }
                            }
                        }

                        if (skipType)
                        {
                            //Log skipping of the message
                            startLagTimeMs = -1;
                            incomingResponses.remove();
                            System.out.println (maxLagTimeMs + " ms elapsed, no listeners requested message.  Skipping for sensor: " + m.getSensorType());
                        }

                        m = null;
                    }
                    else
                    {
                        //Message's sensor type matches the requested sensot type, remove from queue and return it.
                        m = incomingResponses.remove();
                        startLagTimeMs = -1;
                    }
                            
                }

            }
            return m;
        }

        /**
         * @return the shutdown
         */
        protected boolean isShutdown()
        {
            boolean value;
            synchronized (shutdownMutex)
            {
                value = shutdown;
            }
            return value;
        }

        /**
         * @param shutdown the shutdown to set
         */
        protected void setShutdown(boolean shutdown)
        {
            synchronized (shutdownMutex)
            {
                this.shutdown = shutdown;
            }
            synchronized (this.incomingResponses)
            {
                incomingResponses.notifyAll();
            }
        }

        /**
         * @return the timeOfLastSend
         */
        protected long getTimeOfLastReceive()
        {
            long value;
            synchronized (timeMutex)
            {
                value = timeOfLastReceive;
            }
            return value;
        }
    }
    
    /**
     * this thread listens to its write queue. When a new message appears, it writes
     * the message to the output port.
     */
    protected class WriteThread implements Runnable
    {

        /**
         * Queue of messages to be sent through the socket to the Rabbit board
         */
        private ConcurrentLinkedQueue<cbrnPodCommand> outgoingMessages;

        /**
         * Timestamp when last message was sent
         */
        private long timeOfLastSendMs = 0;

        /**
         * Lock object for timeOfLastSendMs object
         */
        private final Object timeMutex = new Object();

        /**
         * If true, gracefully shutdown the thread
         */
        private Boolean shutdown = false;

        /**
         * Lock object for shutdown flag
         */
        private final Object shutdownMutex = new Object();

        /**
         * Socket to write commands through
         */
        private DatagramSocket socket;

        /**
         * IP Address to send commands to
         */
        InetAddress remoteIp;

        /**
         * Port to send commands to
         */
        int remotePort;


        /**
         * Create object to send commands to rabbit board
         *
         * @param socket Socket to send commands through
         * @param remoteIp IP Address of rabbit board
         * @param remotePort Port number of rabbit board
         */
        protected WriteThread(DatagramSocket socket, InetAddress remoteIp, int remotePort)
        {
            outgoingMessages = new ConcurrentLinkedQueue<cbrnPodCommand>();

            this.socket = socket;
            this.remoteIp = remoteIp;
            this.remotePort = remotePort;
        }

        /**
         * Run thread to write messages
         */
        public void run()
        {
            while (true)
            {
                if (isShutdown())
                    break;
                
                //If we have no messages to send...
                if (outgoingMessages.size() == 0)
                {
                    synchronized (outgoingMessages)
                    {
                        try
                        {
                            //Delay until one is ready
                            outgoingMessages.wait(1000);
                        } 
                        catch (InterruptedException ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }
                if (isShutdown())
                    break;

                cbrnPodCommand m = null;
                synchronized (outgoingMessages)
                {
                    //If a message is ready to written...
                    if (outgoingMessages.size() > 0) 
                    {
                        //Remove it from the queue
                        m = outgoingMessages.remove();
                    }
                }

                //If we have a message ready to be sent
                if (m != null)
                {
                    //Convert to bytes and send on socket
                    byte[] bytes = m.toByteArray();
                    messageLogger.logCommand(m);
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, remoteIp, remotePort);
                    try
                    {
                        synchronized (socket)
                        {
                            socket.send(packet);
                        }
                    } 
                    catch (IOException ex)
                    {
                        Logger.getLogger(cbrnPodGenericInterface.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try
                {
                    //throttle
                    Thread.sleep(100);
                } catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }
            }
        }

        /**
         * Add a command to the queue to be sent to the rabbit board
         *
         * @param m Command to be sent
         */
        protected void writeMessage(cbrnPodCommand m)
        {
            synchronized (outgoingMessages)
            {
                outgoingMessages.add(m);
                outgoingMessages.notifyAll();
            }
        }

        /**
         * @return the shutdown
         */
        protected boolean isShutdown()
        {
            boolean value;
            synchronized (shutdownMutex)
            {
                value = shutdown;
            }
            return value;
        }

        /**
         * @param shutdown the shutdown to set
         */
        protected void setShutdown(boolean shutdown)
        {
            synchronized (shutdownMutex)
            {
                this.shutdown = shutdown;
            }

            synchronized (this.outgoingMessages)
            {
                outgoingMessages.notifyAll();
            }
        }

        /**
         * @return the timeOfLastSend
         */
        protected long getTimeOfLastSend()
        {
            long value;
            synchronized (timeMutex)
            {
                value = timeOfLastSendMs;
            }
            return value;
        }
    }
}
