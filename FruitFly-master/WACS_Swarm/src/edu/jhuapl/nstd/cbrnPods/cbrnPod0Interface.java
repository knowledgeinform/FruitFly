/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods;

import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaLCDAReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaLCDBReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDAGMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDAHMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDBGMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDBHMessage;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMessageListener;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaStatusMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaTextMessage;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.c100ActionMessage;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.c100FlowStatusMessage;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.c100StatusMessage;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.c100WarningMessage;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import java.net.InetAddress;

/**
 *
 * Interface to handle messages from Collector Pod (named pod 0)
 *
 * @author humphjc1
 */
public class cbrnPod0Interface extends cbrnPodGenericInterface
{
    /**
     * Listener for messages from C100
     */
    ListenerThread c100ListenerThread;

    /**
     * Listener for messages from Anaconda
     */
    ListenerThread anacondaListenerThread;
    
    /**
     * Open an interface to the rabbit board on WACS BioPod #0
     * 
     * @param localIp Local IP address
     * @param localPort Local port number
     * @param remoteIp Rabbit board IP address
     * @param remotePort rabbit board port number
     * @param belMgr BeliefManager from Swarm.  Can be null.
     */
    public cbrnPod0Interface(InetAddress localIp, int localPort, InetAddress remoteIp, int remotePort, BeliefManager belMgr)
    {
        super (localIp, localPort, remoteIp, remotePort, belMgr);
        
        c100ListenerThread  = new ListenerThread (readThread, cbrnSensorTypes.SENSOR_C100);
        anacondaListenerThread  = new ListenerThread (readThread, cbrnSensorTypes.SENSOR_ANACONDA);

        //Register types of messages in the listener threads
        c100ListenerThread.addMessageType (new c100StatusMessage());
        c100ListenerThread.addMessageType (new c100ActionMessage());
        c100ListenerThread.addMessageType (new c100FlowStatusMessage());
        c100ListenerThread.addMessageType (new c100WarningMessage());
        
        anacondaListenerThread.addMessageType (new anacondaStatusMessage());
        anacondaListenerThread.addMessageType (new anacondaLCDAReportMessage());
        anacondaListenerThread.addMessageType (new anacondaLCDBReportMessage());
        anacondaListenerThread.addMessageType (new anacondaTextMessage());
        anacondaListenerThread.addMessageType (new anacondaSpectraLCDAGMessage());
        anacondaListenerThread.addMessageType (new anacondaSpectraLCDAHMessage());
        anacondaListenerThread.addMessageType (new anacondaSpectraLCDBGMessage());
        anacondaListenerThread.addMessageType (new anacondaSpectraLCDBHMessage());
        
        
        //spin up threads
        Thread ct = new Thread(c100ListenerThread);
        ct.setName ("WACS-C100ListenerThread");
        ct.start();
        Thread at = new Thread(anacondaListenerThread);
        at.setName ("WACS-AnacondaListenerThread");
        at.start();
    }

    /**
     * Shutdown listener threads
     */
    public void shutdown ()
    {
        super.shutdown();
        c100ListenerThread.setShutdown(true);
        anacondaListenerThread.setShutdown(true);
    }

    /**
     * Add a listener for messages from the C100
     *
     * @param messageType Type of message listener wants to receive
     * @param listener Listener to receive messages
     */
    void addPersistentC100Listener(int messageType, cbrnPodMessageListener listener) 
    {
        c100ListenerThread.addPersistentListener(messageType, listener);
    }
    
    /**
     * Add a listener for messages from the anaconda
     *
     * @param messageType Type of message listener wants to receive
     * @param listener Listener to receive messages
     */
    void addPersistentAnacondaListener(int messageType, cbrnPodMessageListener listener)
    {
        anacondaListenerThread.addPersistentListener(messageType, listener);
    }

    /**
     * Remove a listener for messages from the c100
     *
     * @param messageType Type of message listener wants to stop receiving
     * @param listener Listener that was receiving messages
     */
    void removePersistentC100Listener(int messageType, cbrnPodMessageListener listener)
    {
        c100ListenerThread.removePersistentListener(messageType, listener);
    }
    
    /**
     * Remove a listener for messages from the anaconda
     *
     * @param messageType Type of message listener wants to stop receiving
     * @param listener Listener that was receiving messages
     */
    void removePersistentAnacondaListener(int messageType, cbrnPodMessageListener listener)
    {
        anacondaListenerThread.removePersistentListener(messageType, listener);
    }
}
