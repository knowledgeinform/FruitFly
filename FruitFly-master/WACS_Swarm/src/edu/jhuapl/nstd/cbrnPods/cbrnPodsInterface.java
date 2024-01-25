/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods;

import java.net.InetAddress;
import java.net.UnknownHostException;

import edu.jhuapl.nstd.cbrnPods.Bridgeport.BridgeportEthernetMessageListener;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.BridgeportGammaDetectorEthernetInterface;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.BridgeportGammaDetectorInterface;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.BridgeportGammaDetectorUsbInterface;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.BridgeportMessageListenerException;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.BridgeportUsbMessageListener;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.GammaCalibration;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.GammaEthernetCountRates;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.SensorDriverMessage;
import edu.jhuapl.nstd.cbrnPods.Canberra.CanberraDetectionMessageListener;
import edu.jhuapl.nstd.cbrnPods.Canberra.CanberraGammaDetectorInterface;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMessageListener;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportHistogramCommand;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportStatisticsCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Canberra.CanberraDetectionMessage;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.CanberraDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.CountItem;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.LocalIPAddress;


/**
 *
 * Interface to the WACS CBRN pods
 *
 * @author humphjc1
 */
public class cbrnPodsInterface implements BridgeportEthernetMessageListener, BridgeportUsbMessageListener, CanberraDetectionMessageListener
{

    /**
     * Index for collector pod
     */
    public static final int COLLECTOR_POD = 0;

    /**
     * Index for tracker pod
     */
    public static final int TRACKER_POD = 1;
    /**
     * Interface to pod 0, containing C100 and Anaconda
     */
    cbrnPod0Interface m_Pod0;
    
    /**
     * Interface to pod 1, containing IBAC and Bladewerx, also saves Bridgeport data
     */
    cbrnPod1Interface m_Pod1;
    
    /**
     * Interface to bridgeport gamma detector
     */
    BridgeportGammaDetectorInterface m_Bridgeport = null;
    
    /**
     * Interface to the Canberra gamma detector
     */
    private CanberraGammaDetectorInterface canberraInterface = null;
    
    /**
     * Histogram counter, number received from Bridgeport
     */
    int m_BridgeportHistCount;

    /**
     * Belief manager
     */
    BeliefManager beliefManager;
    
    
    
    /**
     * Pods interface.  Interfaces with both pods, handles sending commands and receiving messages, as well as forwarding messages
     * to listeners
     *
     * @param belMgr Belief Manager from Swarm, can be null
     */
    public cbrnPodsInterface(BeliefManager belMgr)
    {
    	beliefManager = belMgr;
    	m_BridgeportHistCount = 0;
    	
    	try
        {
	    	// Get config settings
	        int localPort_0 = Config.getConfig().getPropertyAsInteger("cbrnPodsInterfaceTest.localport0", 1313);   // Port number for collector pod on local computer
	        int remotePort_0 = Config.getConfig().getPropertyAsInteger("cbrnPodsInterfaceTest.remoteport0", 1315); // Port number of collector pod rabbit board
	        int localPort_1 = Config.getConfig().getPropertyAsInteger("cbrnPodsInterfaceTest.localport1", 1314);   // Port number for tracker pod on local computer
	        int remotePort_1 = Config.getConfig().getPropertyAsInteger("cbrnPodsInterfaceTest.remoteport1", 1315); // Port number of tracker pod rabbit board
 
	        InetAddress localIP = LocalIPAddress.getLocalHost(); 												   // IP address of local computer
	        String pod0addr = Config.getConfig().getProperty("cbrnPodsInterfaceTest.pod0ip","192.168.1.113");	   
	        InetAddress remoteIP_0 =    InetAddress.getByName(pod0addr);										   // IP address of collector pod rabbit board
	        String pod1addr = Config.getConfig().getProperty("cbrnPodsInterfaceTest.pod1ip","192.168.1.114");      
	        InetAddress remoteIP_1 =    InetAddress.getByName(pod1addr);										   // IP address of tracker pod rabbit board
	    	
	        // Make interfaces to individual pods
	        m_Pod0 = new cbrnPod0Interface (localIP, localPort_0, remoteIP_0, remotePort_0, beliefManager);
	        m_Pod1 = new cbrnPod1Interface (localIP, localPort_1, remoteIP_1, remotePort_1, beliefManager);
	        
	      	// Connect to bridgeport sensor (in the tracker pod, not connected to Rabbit board)
	        if (Config.getConfig().getPropertyAsBoolean("cbrnPodsInterfaceTest.connectBridgeport", false))
	        {
	        	if (Config.getConfig().getPropertyAsBoolean("cbrnPodsInterfaceTest.useBridgeportUsbInterface", false))
		        {
	        		m_Bridgeport = new BridgeportGammaDetectorUsbInterface(beliefManager);
	        		addBridgeportListener((BridgeportUsbMessageListener) this);
		        }
	        	else
		        {
	        		m_Bridgeport = new BridgeportGammaDetectorEthernetInterface(beliefManager);
	        		addBridgeportListener((BridgeportEthernetMessageListener) this);
		        }
	            
	            //m_Bridgeport = new BridgeportGammaDetectorInterface ("C:\\Documents and Settings\\humphjc1\\My Documents\\wacs\\WACS_Swarm\\GammaLogs\\BridgeportGammaRaw_1294923435265_00000.dat", false);
	            //m_Bridgeport = new BridgeportGammaDetectorInterface("C:\\Documents and Settings\\humphjc1\\My Documents\\wacs\\Pod Logs\\Gamma Isotope Testing\\BridgeportGammaRaw_1288242168546_00013.dat", false);
	            
	        }
	        
	        // Connect to the Canberra sensor (in the tracker pod, not connected to Rabbit board)
	        if(Config.getConfig().getPropertyAsBoolean("cbrnPodsInterfaceTest.connectCanberra", false))
	        {
	        	canberraInterface = new CanberraGammaDetectorInterface();
	        	addCanberraDetectionListener((CanberraDetectionMessageListener) this);
	        }
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
    }

    
    /**
     * Notify all internal threads to shut down.
     */
    public void shutdown() 
    {
        m_Pod0.shutdown();
        m_Pod1.shutdown();
        
        // Shutdown the Bridgeport interface.
        if (m_Bridgeport != null)
        {
            m_Bridgeport.shutdown();
            int maxLoops = 50;
            int loopCtr = 0;
            while (m_Bridgeport.isRunning())
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                if (loopCtr++ > maxLoops)
                {
                    break;
                }
            }
        }
        
        // Shutdown the Canberra interface.
        if(canberraInterface != null)
        {
        	canberraInterface.shutdown();
        }
    
    }

    /**
     * Add a persistent listener to the interface. Every time a message of messageType is called,
     * the listener will also be notified.
     * @param sensorType
     * @param messageType
     * @param listener
     */
    public void addPersistentListener(int sensorType, int messageType, cbrnPodMessageListener listener) 
    {
        switch (sensorType)
        {
            case cbrnSensorTypes.SENSOR_IBAC:
                m_Pod1.addPersistentIbacListener (messageType, listener);
                break;
            case cbrnSensorTypes.SENSOR_C100:
                m_Pod0.addPersistentC100Listener (messageType, listener);
                break;
            case cbrnSensorTypes.SENSOR_BLADEWERX:
                m_Pod1.addPersistentBladewerxListener (messageType, listener);
                break;
            case cbrnSensorTypes.SENSOR_BLADEWERX_PUMP:
                m_Pod1.addPersistentBladewerxPumpListener (messageType, listener);
                break;
            case cbrnSensorTypes.SENSOR_BRIDGEPORT:
                m_Pod1.addPersistentBridgeportListener (messageType, listener);
                break;
            case cbrnSensorTypes.SENSOR_ANACONDA:
                m_Pod0.addPersistentAnacondaListener (messageType, listener);
                break;
            case cbrnSensorTypes.RABBIT_BOARD:
                m_Pod0.addPersistentPodListener (messageType, listener);
                m_Pod1.addPersistentPodListener (messageType, listener);
                break;
            
            default:
                System.err.println ("Can't add persistent listener for unknown sensor type: " + sensorType);
                break;
        }
    }

    /**
     * Register a listener for Bridgeport message
     * @param listener
     */
    public void addBridgeportListener(BridgeportUsbMessageListener listener)
    {
        if (m_Bridgeport != null)
        {
			try
			{
				m_Bridgeport.addBridgeportMessageListener(listener);
			}
			catch (BridgeportMessageListenerException e)
			{
				// This should never happen
				e.printStackTrace();
			}
        }
    }
    
    /**
     * Register a listener for Bridgeport message
     * @param listener
     */
    public void addBridgeportListener(BridgeportEthernetMessageListener listener)
    {
        if (m_Bridgeport != null)
        {
			try
			{
				m_Bridgeport.addBridgeportMessageListener(listener);
			}
			catch (BridgeportMessageListenerException e)
			{
				// This should never happen
				e.printStackTrace();
			}
        }
    }
    
    /**
     * Register a listener for Canberra detection message
     * @param listener
     */
    public void addCanberraDetectionListener(CanberraDetectionMessageListener listener)
    {
        if (canberraInterface != null)
        {
			canberraInterface.addCanberraDetectionMessageListener(listener);
        }
    }

    /**
     * Removes the first instance of the persistent listener, if present
     * @param sensorType
     * @param messageType
     * @param listener
     */
    public void removePersistentListener(int sensorType, int messageType, cbrnPodMessageListener listener) 
    {
        switch (sensorType)
        {
            case cbrnSensorTypes.SENSOR_IBAC:
                m_Pod1.removePersistentIbacListener (messageType, listener);
                break;
            case cbrnSensorTypes.SENSOR_C100:
                m_Pod0.removePersistentC100Listener (messageType, listener);
                break;
            case cbrnSensorTypes.SENSOR_BLADEWERX:
                m_Pod1.removePersistentBladewerxListener (messageType, listener);
                break;
            case cbrnSensorTypes.SENSOR_BLADEWERX_PUMP:
                m_Pod1.removePersistentBladewerxPumpListener (messageType, listener);
                break;
            case cbrnSensorTypes.SENSOR_BRIDGEPORT:
                m_Pod1.removePersistentBridgeportListener (messageType, listener);
                break;
            case cbrnSensorTypes.SENSOR_ANACONDA:
                m_Pod0.removePersistentAnacondaListener (messageType, listener);
                break;
            case cbrnSensorTypes.RABBIT_BOARD:
                m_Pod0.removePersistentPodListener (messageType, listener);
                m_Pod1.removePersistentPodListener (messageType, listener);
                break;
            default:
                System.err.println ("Can't remove persistent listener for unknown sensor type: " + sensorType);
                break;
        }
    }

    /**
     * Remove a listener of Bridgeport messages
     * @param listener
     */
    public void removeBridgeportListener(BridgeportUsbMessageListener listener)
    {
        if (m_Bridgeport != null)
        {
			try
			{
				m_Bridgeport.removeBridgeportMessageListener(listener);
			}
			catch (BridgeportMessageListenerException e)
			{
				// This should never happen
				e.printStackTrace();
			}
        }
    }
    
    /**
     * Remove a listener of Bridgeport messages
     * @param listener
     */
    public void removeBridgeportListener(BridgeportEthernetMessageListener listener)
    {
        if (m_Bridgeport != null)
        {
			try
			{
				m_Bridgeport.removeBridgeportMessageListener(listener);
			}
			catch (BridgeportMessageListenerException e)
			{
				// This should never happen
				e.printStackTrace();
			}
        }
    }
    
    /**
     * Remove a listener of Canberra detection messages
     * @param listener
     */
    public void removeCanberraDetectionListener(CanberraDetectionMessageListener listener)
    {
        if (canberraInterface != null)
        {
            canberraInterface.removeCanberraDetectionMessageListener(listener);
        }
    }

    /**
     * Send a command to the appropriate rabbit board, based on sensor type
     *
     * @param m Command to be sent
     */
    public void sendCommand(cbrnPodCommand m) 
    {
        switch (m.getSensorType())
        {
            case cbrnSensorTypes.SENSOR_IBAC:
                m_Pod1.sendCommand_AttachIP (m);
                break;
            case cbrnSensorTypes.SENSOR_C100:
                m_Pod0.sendCommand_AttachIP (m);
                break;
            case cbrnSensorTypes.SENSOR_BLADEWERX:
                m_Pod1.sendCommand_AttachIP (m);
                break;
            case cbrnSensorTypes.SENSOR_BLADEWERX_PUMP:
                m_Pod1.sendCommand_AttachIP (m);
                break;
            case cbrnSensorTypes.SENSOR_ANACONDA:
                m_Pod0.sendCommand_AttachIP (m);
                break;
            case cbrnSensorTypes.RABBIT_BOARD:
                m_Pod0.sendCommand_AttachIP (m);
                m_Pod1.sendCommand_AttachIP (m);
                break;
            case cbrnSensorTypes.SENSOR_BRIDGEPORT:
                m_Pod1.sendCommand_AttachIP (m);
                break;
            default:
                System.err.println ("Can't send command to unknown sensor type: " + m.getSensorType());
                break;
        }
    }

    /**
     * Send a command to a specific board, should be a generic message and not sensor specific
     *
     * @param m Command to be sent
     * @param boardNum Board number to send to
     */
    public void sendCommandToBoard(cbrnPodCommand m, int boardNum) 
    {
        switch (m.getSensorType())
        {
            case cbrnSensorTypes.SENSOR_SERVO:
            case cbrnSensorTypes.SENSOR_FAN:
            case cbrnSensorTypes.SENSOR_HEATER:
            {
                if (boardNum == 0)
                    m_Pod0.sendCommand_AttachIP (m);
                else if (boardNum == 1)
                    m_Pod1.sendCommand_AttachIP (m);
                break;
            }
            default:
                System.err.println ("Can't send command for sensor type: " + m.getSensorType() + " to a specified board.");
                System.err.println ("Let the interface handle where this message goes");
                break;
        }
    }

    /**
     * Receives histogram messages from the Bridgeport sensor.  Sends a command to the rabbit board to log the data
     * and forwards the message towards the Bridgeport processor
     *
     * @param m Received message
     */
    @Override
    public void handleHistogramMessage(RNHistogram m)
    {
        //Send message to biopod for logging
        bridgeportHistogramCommand[] newList = bridgeportHistogramCommand.getCommandPackets(m.toLogString(), m_BridgeportHistCount++);
        for (int i = 0; i < newList.length; i ++)
        {
            sendCommand(newList[i]);
        }
        
        m_Pod1.processBridgeportData (m);
    }

    /**
     * Receives statistics messages from the Bridgeport sensor.  Sends a command to the rabbit board to log the data
     * and forwards the message towards the Bridgeport processor
     *
     * @param m Received message
     */
    @Override
    public void handleStatisticsMessage(GammaEthernetCountRates m) 
    {
        //Send message to biopod for logging
        bridgeportStatisticsCommand newCmd = new bridgeportStatisticsCommand (m.toLogString().getBytes());
        sendCommand(newCmd);
        
        m_Pod1.processBridgeportData (m);
    }

    /**
     * Receives calibration messages from the Bridgeport sensor.  Does not send a command to the rabbit board to log the data,
     * but forwards the message towards the Bridgeport processor
     *
     * @param m Received message
     */
    @Override
    public void handleCalibrationMessage(GammaCalibration m)
    {
        //Send message to biopod for logging ????????????????
        //bridgeportCalibrationCommand newCmd = new bridgeportCalibrationCommand (m.toLogString().getBytes());
        //sendCommand(newCmd);

        m_Pod1.processBridgeportData (m);
    }
    
    /**
     * Handles messages sent by a Bridgeport sensor over USB
     * 
     * @param m the message sent by the SensorDriver, which interprets Bridgeport
     * USB messages
     */
    @Override
    public void handleSensorDriverMessage(SensorDriverMessage m)
    {
        m_Pod1.processUsbBridgeportData(m);
    }

    public void resetBridgeportBackgroundSpectra()
    {
        if (m_Pod1 != null)
        {
            m_Pod1.m_BridgeportProcessor.resetBackground();
        }
    }
    
    /**
     * Sets the high voltage threshold for the USB interface Bridgeport.
     * 
     * @param hv
     */
    public void setHighVoltage(double hv)
    {
    	if(m_Bridgeport != null)
        {
            ((BridgeportGammaDetectorUsbInterface) m_Bridgeport).setHighVoltage(hv);
        }
    }
    
    /**
     * Sets the fine gain setting for the USB interface Bridgeport.
     * 
     * @param fg 
     */
    public void setFineGain(double fg)
    {
        if(m_Bridgeport != null)
        {
            ((BridgeportGammaDetectorUsbInterface) m_Bridgeport).setFineGain(fg);
        }
    }

    @Override
    public void handleDetectionMessage(CanberraDetectionMessage m)
    {
//        CanberraDetectionBelief blf = new CanberraDetectionBelief();
//        blf.setCount(m.getCount());
//        blf.setFilteredDoseRate(m.getFilteredDoseRate());
//        blf.setMissionAccumulatedDose(m.getMisssionAccumulatedDose());
//        blf.setPeakRate(m.getPeakRate());
//        blf.setRelativeTime(m.getRelativeTime());
//        blf.setTemperature(m.getTemperature());
//        blf.setUnfilteredDoseRate(m.getUnfilteredDoseRate());
//        
//        beliefManager.put(blf);
    }
}
