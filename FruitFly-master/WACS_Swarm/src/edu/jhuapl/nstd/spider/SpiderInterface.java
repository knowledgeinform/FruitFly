/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.spider;

import edu.jhuapl.nstd.spider.messages.AutoResponseCommand;
import edu.jhuapl.nstd.spider.messages.ChemicalDetectionResponse;
import edu.jhuapl.nstd.spider.messages.MetResponse;
import edu.jhuapl.nstd.spider.messages.OkPowerDownResponse;
import edu.jhuapl.nstd.spider.messages.SpiderMessage;
import edu.jhuapl.nstd.spider.messages.SpiderMessageType;
import edu.jhuapl.nstd.util.XCommSerialPort;
import java.lang.reflect.InvocationTargetException;
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
public class SpiderInterface {

    private ReadThread readThread;
    private WriteThread writeThread;
    private ListenerThread listenerThread;
    private ChemicalDetectionListener chemicalDetectionListener;

    /**
     * Spider interface. Receives the serial port as a parameter, and all operations
     * are synchronized on this port.
     * @param serialPort
     */
    public SpiderInterface(XCommSerialPort serialPort)
    {
        chemicalDetectionListener = new ChemicalDetectionListener();

        readThread = new ReadThread(serialPort, this);
        writeThread = new WriteThread(serialPort);
        listenerThread = new ListenerThread(readThread);
        listenerThread.addPersistentListener(SpiderMessageType.SPIDER_CHEMICAL_DETECTION, chemicalDetectionListener);

        //spin up threads
        new Thread(readThread).start();
        new Thread(writeThread).start();
        new Thread(listenerThread).start();

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
    public void addPersistentListener(int messageType, SpiderMessageListener listener) {
        listenerThread.addPersistentListener(messageType, listener);
    }

    /**
     * Adds a transient listener to the listener queue. If the message is received before
     * it the listener times out, it will be notified. If it itimes out the timedOut
     * method will be called. In all cases the listener is removed after notification.
     * @param messageType
     * @param listener
     */
    public void addTransientListener(int messageType, SpiderMessageTimeoutListener listener) {
        listenerThread.addTransientListener(messageType, listener);
    }

    /**
     * Adds a transient listener with default timout behavior of do nothing.
     * @param messageType
     * @param listener
     * @param timeout
     */
    public void addTransientListener(int messageType, SpiderMessageListener listener, int timeout) {
        listenerThread.addTransientListener(messageType, new DefaultSpiderTimeoutListener(listener, timeout));
    }

    /**
     * Removes the first intance of the persistent listener if present
     * @param messageType
     * @param listener
     */
    public void removePersistentListener(int messageType, SpiderMessageListener listener) {
        listenerThread.removePersistentListener(messageType, listener);
    }

    /**
     * Removes the first intance of the transient listener if present
     * @param messageType
     * @param listener
     */
    public void removeTransientListener(int messageType, SpiderMessageTimeoutListener listener) {
        listenerThread.removeTransientListener(messageType, listener);
    }

    /**
     * Adds a chem positive listener. only notified when priorty 3 chem detections are found
     * @param listener
     */
    public void addChemDetectionListener(SpiderMessageListener listener) {
        chemicalDetectionListener.detectionListeners.add(listener);
    }

    /**
     * remove a chem positive listener. only notified when priorty 3 chem detections are found
     * @param listener
     */
    public void removeChemDetectionListener(SpiderMessageListener listener) {
        chemicalDetectionListener.detectionListeners.remove(listener);
    }

    /**
     * Adds a chem positive listener. only notified when priorty 3 chem detections are found
     * @param listener
     */
    public void addChemNonDetectionListener(SpiderMessageListener listener) {
        chemicalDetectionListener.nondetectionListeners.add(listener);
    }

    /**
     * remove a chem positive listener. only notified when priorty 3 chem detections are found
     * @param listener
     */
    public void removeChemNonDetectionListener(SpiderMessageListener listener) {
        chemicalDetectionListener.nondetectionListeners.remove(listener);
    }

    public void sendMessage(SpiderMessage m) {
        writeThread.writeMessage(m);
    }

    public void sendMessageToSpider(SpiderMessage m) {
        m.setToId(02);
        m.setFromId(05);
        m.setPriority(2);
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

        private Map<Integer, List<SpiderMessageListener>> persistentListeners;

        /*
        List of listeners that will be removed in a FIFO fashion
         */
        private Map<Integer, List<SpiderMessageTimeoutListener>> transientListeners;

        /*
        Internal list of message response types that are supported by this interface. All other messages will be dropped on the floor.
         */
        private List<SpiderMessage> messageTemplates;
        private ReadThread readThread;
        private final Object shutdownMutex = new Object();
        private boolean shutdown = false;

        protected ListenerThread(ReadThread readThread) {
            messageTemplates = new ArrayList<SpiderMessage>();
            //add templates. All of these messages will be properly constructed and
            //parsed if found
            messageTemplates.add(new ChemicalDetectionResponse());
            messageTemplates.add(new OkPowerDownResponse());
            messageTemplates.add(new MetResponse());

            persistentListeners = Collections.synchronizedMap(new HashMap<Integer, List<SpiderMessageListener>>());
            transientListeners = Collections.synchronizedMap(new HashMap<Integer, List<SpiderMessageTimeoutListener>>());

            this.readThread = readThread;
        }

        protected void addPersistentListener(int key, SpiderMessageListener listener) {
            if (persistentListeners.get(key) == null) {
                persistentListeners.put(key, new ArrayList<SpiderMessageListener>());
            }
            persistentListeners.get(key).add(listener);
        }

        protected void addTransientListener(int key, SpiderMessageTimeoutListener listener) {
            if (transientListeners.get(key) == null) {
                transientListeners.put(key, new ArrayList<SpiderMessageTimeoutListener>());
            }
            transientListeners.get(key).add(listener);
        }

        protected void removePersistentListener(int key, SpiderMessageListener listener) {
            if (persistentListeners.get(key) == null) {
                return;
            }
            persistentListeners.get(key).remove(listener);
        }

        protected void removeTransientListener(int key, SpiderMessageTimeoutListener listener) {
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
                SpiderMessage m = readThread.readMessage(1000);
                if (m != null) {
                    //Check to see if it is a supported parseable message
                    SpiderMessage response = null;
                    for (SpiderMessage t : messageTemplates) {
                        if (t.isInstanceOf(m)) {
                            try {
                                response = (SpiderMessage) t.getClass().getConstructor().newInstance();
                                response.parseSpiderMessage(m);
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
                    SpiderMessage messageForListener = response != null ? response : m;
                    List<SpiderMessageTimeoutListener> tl = transientListeners.get(messageForListener.getMessageType());
                    while (tl != null && tl.size() > 0) {
                        SpiderMessageTimeoutListener timeoutListener = tl.remove(0);
                        if (timeoutListener.isTimedOut()) {
                            timeoutListener.timedOut();
                            continue;
                        } else {
                            timeoutListener.handleMessage(m);
                        }

                    }
                    List<SpiderMessageListener> pl = persistentListeners.get(messageForListener.getMessageType());
                    if (pl != null && pl.size() > 0) {
                        for (SpiderMessageListener sml : pl) {
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

        private ConcurrentLinkedQueue<SpiderMessage> incomingResponses;
        private long timeOfLastReceive = 0;
        private final Object timeMutex = new Object();
        private Boolean shutdown = false;
        private final Object shutdownMutex = new Object();
        private XCommSerialPort serialPort;
        private SpiderInterface spider;

        protected ReadThread(XCommSerialPort serialPort, SpiderInterface spider) {
            incomingResponses = new ConcurrentLinkedQueue<SpiderMessage>();

            this.serialPort = serialPort;
            this.spider = spider;

        }

        public void run() {
            byte[] readBytes;
            SpiderMessage m = new SpiderMessage();
            while (true) {
                try {
                    if (isShutdown()) {
                        break;
                    }
                    synchronized (serialPort) {
                        readBytes = serialPort.readAvailableBytes();
                    }
                    if (readBytes != null) {
                        for (int i = 0; i < readBytes.length; i++) {
                            m.parseByte(readBytes[i]);
                            if (m.isValidMessage()) {
                                synchronized (incomingResponses) {
                                    incomingResponses.add(m);
                                    incomingResponses.notifyAll();
                                }

                                //Send response
                                if (m.getMessageType() > 31) {
                                    AutoResponseCommand response = new AutoResponseCommand();
                                    response.setSequence(m.getChecksum());
                                    response.setToId(m.getFromId());
                                    response.setFromId(m.getToId());
                                    response.setPriority(m.getPriority());
                                    spider.sendMessage(response);
                                }
                                synchronized (timeMutex) {
                                    timeOfLastReceive = new Date().getTime();
                                }
                                m = new SpiderMessage();
                            }
                        }
                    }
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SpiderInterface.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        //shutting down
        }

        protected SpiderMessage readMessage(long timeout) {
            SpiderMessage m = null;

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

        private ConcurrentLinkedQueue<SpiderMessage> outgoingMessages;
        private long timeOfLastSend = 0;
        private final Object timeMutex = new Object();
        private Boolean shutdown = false;
        private final Object shutdownMutex = new Object();
        private XCommSerialPort serialPort;

        protected WriteThread(XCommSerialPort serialPort) {
            outgoingMessages = new ConcurrentLinkedQueue<SpiderMessage>();
            this.serialPort = serialPort;
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
                SpiderMessage m = null;
                synchronized (outgoingMessages) {
                    if (outgoingMessages.size() > 0) {
                        m = outgoingMessages.remove();

                    }
                }
                if (m != null && !serialPort.sendBytes(m.toByteArray())) {
                    //TODO log send failure
                }
                try {
                    //throttle
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    //TODO log sleep failure
                }
            }
        }

        protected void writeMessage(SpiderMessage m) {
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
    protected class ChemicalDetectionListener implements SpiderMessageListener {

        private List<SpiderMessageListener> detectionListeners;
        private List<SpiderMessageListener> nondetectionListeners;

        protected ChemicalDetectionListener() {
            detectionListeners = new ArrayList<SpiderMessageListener>();
            nondetectionListeners = new ArrayList<SpiderMessageListener>();
        }

        public void handleMessage(SpiderMessage m) {
            List<SpiderMessageListener> listeners;
            ChemicalDetectionResponse cdr = (ChemicalDetectionResponse) m;
            if (cdr.getPriority() == 2) {
                listeners = nondetectionListeners;
            } else if (cdr.getPriority() == 3) {
                listeners = detectionListeners;
            } else {
                return;
            }
            for (SpiderMessageListener l : listeners) {
                l.handleMessage(m);
            }

        }
    }
}
