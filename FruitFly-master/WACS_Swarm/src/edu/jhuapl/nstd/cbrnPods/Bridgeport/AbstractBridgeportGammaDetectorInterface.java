package edu.jhuapl.nstd.cbrnPods.Bridgeport;

import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.util.Config;

public abstract class AbstractBridgeportGammaDetectorInterface extends Thread implements BridgeportGammaDetectorInterface
{
    /**
     * Belief Manager from Swarm.  Can be null, but must be defined to include proper metrics data in log files
     */
    protected BeliefManager beliefManager;
    
    /**
     * If true, thread is alive;
     */
    protected boolean m_Running = false;
    
    /**
     * If true, the main thread will playback gamma spectra data from a log file, and sequentially through its connection chain of files.
     * If false, the main thread will collecte gamma data from the Bridgeport device.
     */
    private boolean m_PlaybackEnabled;
    
    /**
     * Abstract parent class 
     * @param belMgr
     */
	public AbstractBridgeportGammaDetectorInterface(BeliefManager belMgr)
	{
		beliefManager = belMgr;
		
		m_PlaybackEnabled = Config.getConfig().getPropertyAsBoolean("cbrnPodsInterfaceTest.playbackGamma", false);
	}
	
	/**
     * Add a listener for gamma messages
     * 
     * @param listener Listener to add
     */
    public void addBridgeportMessageListener(BridgeportEthernetMessageListener listener) throws BridgeportMessageListenerException
    {
    	throw new BridgeportMessageListenerException("This method should not be called, unless it has been overridden.");
    }
    
    /**
     * Remove a listener for gamma messages
     * 
     * @param listener Listener to remove
     */
    public void removeBridgeportMessageListener(BridgeportEthernetMessageListener listener) throws BridgeportMessageListenerException
    {
    	throw new BridgeportMessageListenerException("This method should not be called, unless it has been overridden.");
    }
    
	/**
     * Add a listener for gamma messages
     * 
     * @param listener Listener to add
     */
    public void addBridgeportMessageListener(BridgeportUsbMessageListener listener) throws BridgeportMessageListenerException
    {
    	throw new BridgeportMessageListenerException("This method should not be called, unless it has been overridden.");
    }
    
    /**
     * Remove a listener for gamma messages
     * 
     * @param listener Listener to remove
     */
    public void removeBridgeportMessageListener(BridgeportUsbMessageListener listener) throws BridgeportMessageListenerException
    {
    	throw new BridgeportMessageListenerException("This method should not be called, unless it has been overridden.");
    }
	
    /**
     * Run interface thread.  Either run from file or live from sensor.
     */
    @Override
    public void run() 
    {
        if (m_PlaybackEnabled)
            runPlayback();
        else
        {
            //Empty while loop to call runLive until returns true - signal of graceful exit or non-recoverable error
            while (!runLive());
        }
    }
    
    /**
     * Method for obtaining data from a live Bridgeport sensor.
     * Return true if finished gracefully or non-recoverable error, false if an error has occurred while
     * 
     * @return
     */
    protected abstract boolean runLive();
	
    /**
     * Method for playing back data from a log file.
     */
    protected abstract void runPlayback();
    
    /**
     * Accessor for whether the main processing thread is running
     * 
     * @return True if thread loop is active
     */
    public boolean isRunning() 
    {
        return m_Running;
    }
    
    /**
     * Stop the main processing thread.
     */
    public void shutdown() 
    {
        m_Running = false;
    }
}