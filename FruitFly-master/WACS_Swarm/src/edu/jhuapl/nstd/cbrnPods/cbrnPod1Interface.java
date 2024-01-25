/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods;

import java.io.File;
import java.net.InetAddress;

import edu.jhuapl.nstd.cbrnPods.Bridgeport.GammaCalibration;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.GammaEthernetCountRates;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.SensorDriverMessage;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMessageListener;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxAETNADetectionReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxCompositeHistogramMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxDetectionMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxDllDetectionReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxStatusMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxPumpMessages.bladewerxPumpStatusMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportCompositeHistogramMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportConfigurationMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportDetectionReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportHistogramMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportStatisticsMessage;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacDiagnosticsMessage;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacParticleCountMessage;
import edu.jhuapl.nstd.cbrnPods.messages.countMessage;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.util.Config;

/**
 *
 * Interface to handle messages from Tracker Pod (named pod 1)
 *
 * @author humphjc1
 */
public class cbrnPod1Interface extends cbrnPodGenericInterface
{

    /**
     * Listener for messages from IBAC
     */
    ListenerThread ibacListenerThread;
    
    /**
     * Listener for messages from Bladewerx
     */
    ListenerThread bladewerxListenerThread;
    
    /**
     * Listener for messages about Bladewerx pump
     */
    ListenerThread bladewerxPumpListenerThread;
    
    /**
     * Listener for messages from Bridgeport
     */
    ListenerThread bridgeportListenerThread;

    /**
     * Processing thread for Bladewerx alpha data
     */
    bladewerxProcessor m_BladewerxProcessor;

    /**
     * Processing thread from Bridgeport gamma data
     */
    bridgeportProcessor m_BridgeportProcessor;
    
    /**
     * Open an interface to the rabbit board on WACS BioPod #1
     * 
     * @param localIp Local IP address
     * @param localPort Local port number
     * @param remoteIp Rabbit board IP address
     * @param remotePort rabbit board port number
     * @param belMgr BeliefManager from Swarm.  Can be null.
     */
    public cbrnPod1Interface(InetAddress localIp, int localPort, InetAddress remoteIp, int remotePort, BeliefManager belMgr)
    {
        super (localIp, localPort, remoteIp, remotePort, belMgr);
        
        boolean processAlpha = Config.getConfig().getPropertyAsBoolean("cbrnPodsInterfaceTest.processAlpha", true);
        boolean playbackAlpha = Config.getConfig().getPropertyAsBoolean("cbrnPodsInterfaceTest.playbackAlpha", false);
        boolean processGamma = Config.getConfig().getPropertyAsBoolean("cbrnPodsInterfaceTest.processGamma", true);
        
        // IBAC setup
        ibacListenerThread = new ListenerThread (readThread, cbrnSensorTypes.SENSOR_IBAC);
        
        ibacListenerThread.addMessageType (new ibacDiagnosticsMessage());
        ibacListenerThread.addMessageType (new ibacParticleCountMessage());
        
        // Bridgeport setup
        bridgeportListenerThread = new ListenerThread (readThread, cbrnSensorTypes.SENSOR_BRIDGEPORT);
        
        bridgeportListenerThread.addMessageType (new bridgeportHistogramMessage());
        bridgeportListenerThread.addMessageType (new bridgeportStatisticsMessage());
        bridgeportListenerThread.addMessageType (new bridgeportConfigurationMessage());
        bridgeportListenerThread.addMessageType (new bridgeportCompositeHistogramMessage());
        bridgeportListenerThread.addMessageType (new bridgeportDetectionReportMessage());
        
        if (processGamma)
        {
            m_BridgeportProcessor = new bridgeportProcessor (this);
        }
        else
        {
            m_BridgeportProcessor = null;
        }
              
        // Bladewerx setup
        bladewerxListenerThread = new ListenerThread (readThread, cbrnSensorTypes.SENSOR_BLADEWERX);
        bladewerxPumpListenerThread = new ListenerThread (readThread, cbrnSensorTypes.SENSOR_BLADEWERX_PUMP);
        
        bladewerxListenerThread.addMessageType (new bladewerxDetectionMessage());
        bladewerxListenerThread.addMessageType (new bladewerxStatusMessage());
        bladewerxListenerThread.addMessageType (new bladewerxCompositeHistogramMessage());
        bladewerxListenerThread.addMessageType (new bladewerxDllDetectionReportMessage());        
        bladewerxListenerThread.addMessageType (new bladewerxAETNADetectionReportMessage());
        bladewerxListenerThread.addMessageType (new countMessage());
        
        bladewerxPumpListenerThread.addMessageType (new bladewerxPumpStatusMessage());
        
        if (processAlpha)
        {
        	m_BladewerxProcessor = new bladewerxProcessor (this, belMgr);
        	
        	if(playbackAlpha)
             {
        		String alphaRawLogFilePath = Config.getConfig().getProperty("cbrnPodsInterfaceTest.playbackAlphaRawLogFile");
        		if(alphaRawLogFilePath != null)
        		{
        			File alphaRawLogFile = new File(alphaRawLogFilePath);
        			if(alphaRawLogFile.exists())
	        		{
	            		bladewerxProcessor.createFilePlaybackThread(m_BladewerxProcessor, alphaRawLogFile.getAbsolutePath()).start();
	        		}
	        		else
	        		{
	        			System.err.println("Failed to find the specified raw alpha playback log file: \"" + alphaRawLogFile.getAbsolutePath() + "\".");
	        			System.exit(1);
	        		}
        		}
        		else
        		{
        			System.err.println("No raw alpha playback log file was specified in the config file.");
        			System.exit(1);
        		}
             }
             else
             {
        	        bladewerxListenerThread.addPersistentListener(cbrnPodMsg.BLADEWERX_DETECTION_TYPE, m_BladewerxProcessor);
     	            bladewerxListenerThread.addPersistentListener(cbrnPodMsg.BLADEWERX_STATUS_TYPE, m_BladewerxProcessor);
                    bladewerxListenerThread.addPersistentListener(cbrnPodMsg.COUNT_ITEM, m_BladewerxProcessor);
             } 
        }
        else
        {
            m_BladewerxProcessor = null;
        }       
        
        //spin up threads
        Thread it = new Thread(ibacListenerThread);
        it.setName ("WACS-IbacListenerThread");
        it.start();
        Thread bt = new Thread(bladewerxListenerThread);
        bt.setName ("WACS-BladewerxListenerThread");
        bt.start();
        Thread pt = new Thread(bladewerxPumpListenerThread);
        pt.setName ("WACS-BladewerxPumpListenerThread");
        pt.start();
        Thread gt = new Thread(bridgeportListenerThread);
        gt.setName ("WACS-BridgeportListenerThread");
        gt.start();
    }

    /**
     * Shutdown listener threads
     */
    public void shutdown ()
    {
        super.shutdown();
        ibacListenerThread.setShutdown(true);
        bladewerxListenerThread.setShutdown(true);
        bladewerxPumpListenerThread.setShutdown(true);
        bridgeportListenerThread.setShutdown(true);
    }

    /**
     * Add a listener for messages from the IBAC
     *
     * @param messageType Type of message listener wants to receive
     * @param listener Listener to receive messages
     */
    void addPersistentIbacListener(int messageType, cbrnPodMessageListener listener)
    {
        ibacListenerThread.addPersistentListener(messageType, listener);
    }
    
    /**
     * Add a listener for messages from the Bladewerx
     *
     * @param messageType Type of message listener wants to receive
     * @param listener Listener to receive messages
     */
    void addPersistentBladewerxListener(int messageType, cbrnPodMessageListener listener)
    {
        bladewerxListenerThread.addPersistentListener(messageType, listener);
    }

    /**
     * Add a listener for messages from the Bladewerx pump
     *
     * @param messageType Type of message listener wants to receive
     * @param listener Listener to receive messages
     */
    void addPersistentBladewerxPumpListener(int messageType, cbrnPodMessageListener listener)
    {
        bladewerxPumpListenerThread.addPersistentListener(messageType, listener);
    }
    
    /**
     * Add a listener for messages from the Bridgeport
     *
     * @param messageType Type of message listener wants to receive
     * @param listener Listener to receive messages
     */
    void addPersistentBridgeportListener(int messageType, cbrnPodMessageListener listener)
    {
        bridgeportListenerThread.addPersistentListener(messageType, listener);
    }

    /**
     * Remove a listener for messages from the IBAC
     *
     * @param messageType Type of message listener wants to stop receiving
     * @param listener Listener that was receiving messages
     */
    void removePersistentIbacListener(int messageType, cbrnPodMessageListener listener)
    {
        ibacListenerThread.removePersistentListener(messageType, listener);
    }
    
    /**
     * Remove a listener for messages from the Bladewerx
     *
     * @param messageType Type of message listener wants to stop receiving
     * @param listener Listener that was receiving messages
     */
    void removePersistentBladewerxListener(int messageType, cbrnPodMessageListener listener)
    {
        bladewerxListenerThread.removePersistentListener(messageType, listener);
    }
    
    /**
     * Remove a listener for messages from the Bladewerx pump
     *
     * @param messageType Type of message listener wants to stop receiving
     * @param listener Listener that was receiving messages
     */
    void removePersistentBladewerxPumpListener(int messageType, cbrnPodMessageListener listener)
    {
        bladewerxPumpListenerThread.removePersistentListener(messageType, listener);
    }
    
    /**
     * Remove a listener for messages from the Bridgeport
     *
     * @param messageType Type of message listener wants to stop receiving
     * @param listener Listener that was receiving messages
     */
    void removePersistentBridgeportListener(int messageType, cbrnPodMessageListener listener)
    {
        bridgeportListenerThread.removePersistentListener(messageType, listener);
    }

    /**
     * Adds count rate data from the bridgeport sensor to the bridgeport processor for analysis
     * @param m Count rate message
     */
    void processBridgeportData(GammaEthernetCountRates m) 
    {
        if (m_BridgeportProcessor != null)
            m_BridgeportProcessor.incomingData (m);
    }

    /**
     * Adds histogram data from the bridgeport sensor to the bridgeport processor for analysis
     * @param m Histogram message
     */
    void processBridgeportData(RNHistogram m)
    {
        if (m_BridgeportProcessor != null)
            m_BridgeportProcessor.incomingData (m);
    }

    /**
     * Adds calibration data from the bridgeport sensor to the bridgeport processor for analysis
     * @param m Calibration message
     */
    void processBridgeportData(GammaCalibration m)
    {
        if (m_BridgeportProcessor != null)
            m_BridgeportProcessor.incomingData (m);
    }
    
    protected void processUsbBridgeportData(SensorDriverMessage m)
    {
        if (m_BridgeportProcessor != null)
            m_BridgeportProcessor.addUsbData(m);
    }
}
