/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.biopod;

import edu.jhuapl.nstd.biopod.messages.BioPodMessage;
import edu.jhuapl.nstd.biopod.messages.DiagnosticsMessage;
import edu.jhuapl.nstd.biopod.messages.IbacSleep;
import edu.jhuapl.nstd.biopod.messages.IbacStatus;
import edu.jhuapl.nstd.biopod.messages.ParticleCountMessage;
import edu.jhuapl.nstd.biopod.messages.PodHeartbeat;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author southmk1
 */
public class BioPodInterface {

    private ReadThread readThread;
    private WriteThread writeThread;
    private ListenerThread listenerThread;
    private DetectionListener DetectionListener;
    private DatagramSocket socket;
    InetAddress remoteIp;
    private int remotePort;
    InetAddress localIp;
    private int localPort;

    /**
     * BioPod interface. Receives the serial port as a parameter, and all operations
     * are synchronized on this port.
     * @param serialPort
     */
    public BioPodInterface(InetAddress localIp, int localPort, InetAddress remoteIp, int remotePort) {

        this.localIp = localIp;
        this.localPort = localPort;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;

        try {
            socket = new DatagramSocket(localPort);
            socket.setSoTimeout(200);
        } catch (SocketException ex) {
            Logger.getLogger(BioPodInterface.class.getName()).log(Level.SEVERE, null, ex);
        }


        DetectionListener = new DetectionListener();

        readThread = new ReadThread(this, socket);
        writeThread = new WriteThread(socket,remoteIp,remotePort);
        listenerThread = new ListenerThread(readThread);

        //spin up threads
        new Thread(readThread).start();
        new Thread(writeThread).start();
        new Thread(listenerThread).start();

    }

    public void setState(boolean s)
    {
        if(s)
           sendMessageToPod(new IbacStatus());
        else
           sendMessageToPod(new IbacSleep());
    }

    /**
     * Notify all internal threads to shut down.
     */
    public void shutdown() {
        readThread.setShutdown(true);
        writeThread.setShutdown(true);
        listenerThread.setShutdown(true);
    }

    /**
     * Add a persistent listener to the interface. Everytime a message of messageType is called,
     * the listener will also be notified.
     * @param messageType
     * @param listener
     */
    public void addPersistentListener(int messageType, BioPodMessageListener listener) {
        listenerThread.addPersistentListener(messageType, listener);
    }

    /**
     * Adds a transient listener to the listener queue. If the message is received before
     * it the listener times out, it will be notified. If it itimes out the timedOut
     * method will be called. In all cases the listener is removed after notification.
     * @param messageType
     * @param listener
     */
    public void addTransientListener(int messageType, BioPodMessageTimeoutListener listener) {
        listenerThread.addTransientListener(messageType, listener);
    }

    /**
     * Adds a transient listener with default timout behavior of do nothing.
     * @param messageType
     * @param listener
     * @param timeout
     */
    public void addTransientListener(int messageType, BioPodMessageListener listener, int timeout) {
        listenerThread.addTransientListener(messageType, new DefaultBioPodTimeoutListener(listener, timeout));
    }

    /**
     * Removes the first intance of the persistent listener if present
     * @param messageType
     * @param listener
     */
    public void removePersistentListener(int messageType, BioPodMessageListener listener) {
        listenerThread.removePersistentListener(messageType, listener);
    }

    /**
     * Removes the first intance of the transient listener if present
     * @param messageType
     * @param listener
     */
    public void removeTransientListener(int messageType, BioPodMessageTimeoutListener listener) {
        listenerThread.removeTransientListener(messageType, listener);
    }

    /**
     * Adds a chem positive listener. only notified when priorty 3 chem detections are found
     * @param listener
     */
    public void addDetectionListener(BioPodMessageListener listener) {
        DetectionListener.detectionListeners.add(listener);
    }

    /**
     * remove a chem positive listener. only notified when priorty 3 chem detections are found
     * @param listener
     */
    public void removeDetectionListener(BioPodMessageListener listener) {
        DetectionListener.detectionListeners.remove(listener);
    }

    /**
     * Adds a chem positive listener. only notified when priorty 3 chem detections are found
     * @param listener
     */
    public void addNonDetectionListener(BioPodMessageListener listener) {
        DetectionListener.nondetectionListeners.add(listener);
    }

    /**
     * remove a chem positive listener. only notified when priorty 3 chem detections are found
     * @param listener
     */
    public void removeNonDetectionListener(BioPodMessageListener listener) {
        DetectionListener.nondetectionListeners.remove(listener);
    }

    public void sendMessage(BioPodMessage m) {
        writeThread.writeMessage(m);
    }

    public void sendMessageToPod(BioPodMessage m) {
        m.setFromIP(((localIp.getAddress()[0] & 0xFF) << 24) |
                ((localIp.getAddress()[1] & 0xFF) << 16) |
                ((localIp.getAddress()[2] & 0xFF) << 8) |
                ((localIp.getAddress()[3] & 0xFF)));
        m.setFromPort(localPort);
        sendMessage(m);
    }

    /**
     * This thread is responsbile for listening to the read threads queue.
     * When a new message appears on the queue, all relevant listeners are notified.
     * This decouples listening and reading, the only caveat is that the listeners
     * may not complete the first call from the listener before they are called again.
     */
    protected class ListenerThread implements Runnable {
        /*
         *List of listeners that will be called for every message of specified type
         */

        private Map<Integer, List<BioPodMessageListener>> persistentListeners;

        /*
        List of listeners that will be removed in a FIFO fashion
         */
        private Map<Integer, List<BioPodMessageTimeoutListener>> transientListeners;

        /*
        Internal list of message response types that are supported by this interface. All other messages will be dropped on the floor.
         */
        private List<BioPodMessage> messageTemplates;
        private ReadThread readThread;
        private final Object shutdownMutex = new Object();
        private boolean shutdown = false;

        protected ListenerThread(ReadThread readThread) {
            messageTemplates = new ArrayList<BioPodMessage>();
            //add templates. All of these messages will be properly constructed and
            //parsed if found
            messageTemplates.add(new DiagnosticsMessage());
            messageTemplates.add(new ParticleCountMessage());
            messageTemplates.add(new PodHeartbeat());


            persistentListeners = Collections.synchronizedMap(new HashMap<Integer, List<BioPodMessageListener>>());
            transientListeners = Collections.synchronizedMap(new HashMap<Integer, List<BioPodMessageTimeoutListener>>());

            this.readThread = readThread;
        }

        protected void addPersistentListener(int key, BioPodMessageListener listener) {
            if (persistentListeners.get(key) == null) {
                persistentListeners.put(key, new ArrayList<BioPodMessageListener>());
            }
            persistentListeners.get(key).add(listener);
        }

        protected void addTransientListener(int key, BioPodMessageTimeoutListener listener) {
            if (transientListeners.get(key) == null) {
                transientListeners.put(key, new ArrayList<BioPodMessageTimeoutListener>());
            }
            transientListeners.get(key).add(listener);
        }

        protected void removePersistentListener(int key, BioPodMessageListener listener) {
            if (persistentListeners.get(key) == null) {
                return;
            }
            persistentListeners.get(key).remove(listener);
        }

        protected void removeTransientListener(int key, BioPodMessageTimeoutListener listener) {
            if (transientListeners.get(key) == null) {
                return;
            }
            transientListeners.get(key).remove(listener);
        }

        public void run() {
            while (true) {
                if (isShutdown()) {
                    break;
                }
                BioPodMessage m = readThread.readMessage(1000);
                if (m != null) {
                    //Check to see if it is a supported parseable message
                    BioPodMessage response = null;
                    for (BioPodMessage t : messageTemplates) {
                        if (t.isInstanceOf(m)) {
                            try {
                                response = (BioPodMessage) t.getClass().getConstructor().newInstance();
                                response.parseBioPodMessage(m);
                            } catch (InstantiationException ex) {
                                //TODO logging
                            } catch (IllegalAccessException ex) {
                                //TODO logging
                            } catch (IllegalArgumentException ex) {
                                //TODO logging
                            } catch (InvocationTargetException ex) {
                                //TODO logging
                            } catch (NoSuchMethodException ex) {
                                //TODO logging
                            } catch (SecurityException ex) {
                                //TODO logging
                            }
                        }
                    }
                    BioPodMessage messageForListener = response != null ? response : m;
                    List<BioPodMessageTimeoutListener> tl = transientListeners.get(messageForListener.getMessageType());
                    while (tl != null && tl.size() > 0) {
                        BioPodMessageTimeoutListener timeoutListener = tl.remove(0);
                        if (timeoutListener.isTimedOut()) {
                            timeoutListener.timedOut();
                            continue;
                        } else {
                            timeoutListener.handleMessage(m);
                        }

                    }
                    List<BioPodMessageListener> pl = persistentListeners.get(messageForListener.getMessageType());
                    if (pl != null && pl.size() > 0) {
                        for (BioPodMessageListener sml : pl) {
                            sml.handleMessage(messageForListener);
                        }
                    }
                }
            }
        }

        protected boolean isShutdown() {
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
    }

    /**
     * This thread is responsbile for actively reading the incoming port.
     * Once a complete message is received, it is added to a queue and the listener thread
     * is notified.
     */
    protected class ReadThread implements Runnable {

        private ConcurrentLinkedQueue<BioPodMessage> incomingResponses;
        private long timeOfLastReceive = 0;
        private final Object timeMutex = new Object();
        private Boolean shutdown = false;
        private final Object shutdownMutex = new Object();
        private BioPodInterface bioPod;
        private DatagramSocket socket = null;
        private byte[] buf = new byte[256 + 12];
        private DatagramPacket packetBuffer;

        protected ReadThread(BioPodInterface bioPod, DatagramSocket socket) {
            incomingResponses = new ConcurrentLinkedQueue<BioPodMessage>();
            this.socket = socket;
            packetBuffer = new DatagramPacket(buf, buf.length);
            this.bioPod = bioPod;

        }

        public void run() {
            BioPodMessage m = new BioPodMessage();
            boolean received = false;
            while (true) {
                try {
                    if (isShutdown()) {
                        break;
                    }
                    synchronized (socket) {
                        try {
                            socket.receive(packetBuffer);
                            received = true;
                        } catch (IOException ex) {
                            received = false;
                        }
                    }

                    if (received) {
                        for (int i = 0; i < packetBuffer.getLength(); i++) {
                            m.parseByte(packetBuffer.getData()[i]);
                            if (m.isValidMessage()) {
                                synchronized (incomingResponses) {
                                    incomingResponses.add(m);
                                    incomingResponses.notifyAll();
                                }
                                synchronized (timeMutex) {
                                    timeOfLastReceive = new Date().getTime();
                                }
                                m = new BioPodMessage();
                            }
                        }
                    }
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BioPodInterface.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        //shutting down
        }

        protected BioPodMessage readMessage(long timeout) {
            BioPodMessage m = null;

            synchronized (incomingResponses) {
                if (incomingResponses.size() > 0) {
                    m = incomingResponses.remove();
                } else {
                    try {
                        incomingResponses.wait(timeout);
                    } catch (InterruptedException ex) {
                        //TODO log wait faulure
                    }
                    if (incomingResponses.size() > 0) {
                        m = incomingResponses.remove();
                    }
                }

            }
            return m;
        }

        /**
         * @return the shutdown
         */
        protected boolean isShutdown() {
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
            synchronized (this.incomingResponses) {
                incomingResponses.notifyAll();
            }
        }

        /**
         * @return the timeOfLastSend
         */
        protected long getTimeOfLastReceive() {
            long value;
            synchronized (timeMutex) {
                value = timeOfLastReceive;
            }
            return value;
        }
    }

    /**
     * this thread listens to its write queue. When a new message apepars, it writes
     * the message to the output port.
     */
    protected class WriteThread implements Runnable {

        private ConcurrentLinkedQueue<BioPodMessage> outgoingMessages;
        private long timeOfLastSend = 0;
        private final Object timeMutex = new Object();
        private Boolean shutdown = false;
        private final Object shutdownMutex = new Object();
        private DatagramSocket socket;
        InetAddress remoteIp;
        int remotePort;

        protected WriteThread(DatagramSocket socket, InetAddress remoteIp, int remotePort) {
            outgoingMessages = new ConcurrentLinkedQueue<BioPodMessage>();

            this.socket = socket;
            this.remoteIp = remoteIp;
            this.remotePort = remotePort;
        }

        public void run() {
            while (true) {
                if (isShutdown()) {
                    break;
                }
                if (outgoingMessages.size() == 0) {
                    synchronized (outgoingMessages) {
                        try {
                            outgoingMessages.wait(1000);
                        } catch (InterruptedException ex) {
                            //TODO log wait faulure
                        }
                    }
                }
                if (isShutdown()) {
                    break;
                }
                BioPodMessage m = null;
                synchronized (outgoingMessages) {
                    if (outgoingMessages.size() > 0) {
                        m = outgoingMessages.remove();

                    }
                }
                if (m != null)
                {
                    DatagramPacket packet = new DatagramPacket(m.toByteArray(), m.toByteArray().length, remoteIp, remotePort);
                    try {
                        socket.send(packet);
                    } catch (IOException ex) {
                        Logger.getLogger(BioPodInterface.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    //throttle
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    //TODO log sleep failure
                }
            }
        }

        protected void writeMessage(BioPodMessage m) {
            synchronized (outgoingMessages) {
                outgoingMessages.add(m);
                outgoingMessages.notifyAll();
            }
        }

        /**
         * @return the shutdown
         */
        protected boolean isShutdown() {
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

            synchronized (this.outgoingMessages) {
                outgoingMessages.notifyAll();
            }
        }

        /**
         * @return the timeOfLastSend
         */
        protected long getTimeOfLastSend() {
            long value;
            synchronized (timeMutex) {
                value = timeOfLastSend;
            }
            return value;
        }
    }

    /**
     * This is a helper listener used to help further distinguish within a message type.
     * Because listeners at this level are only triggered based on message types,
     * additional layers are required to trigger based on values within the
     * individual messages.
     */
    protected class DetectionListener implements BioPodMessageListener {

        private List<BioPodMessageListener> detectionListeners;
        private List<BioPodMessageListener> nondetectionListeners;

        protected DetectionListener() {
            detectionListeners = new ArrayList<BioPodMessageListener>();
            nondetectionListeners = new ArrayList<BioPodMessageListener>();
        }

        public void handleMessage(BioPodMessage m) {
            List<BioPodMessageListener> listeners;
            BioPodMessage cdr = (BioPodMessage) m;
        //TODO what is a detection?
           /* if (cdr.getPriority() == 2) {
        listeners = nondetectionListeners;
        } else if (cdr.getPriority() == 3) {
        listeners = detectionListeners;
        } else {
        return;
        }
        for (BioPodMessageListener l : listeners) {
        l.handleMessage(m);
        }
         * */

        }
    }
}
