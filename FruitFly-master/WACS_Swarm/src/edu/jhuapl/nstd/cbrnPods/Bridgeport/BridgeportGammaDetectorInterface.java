package edu.jhuapl.nstd.cbrnPods.Bridgeport;

public interface BridgeportGammaDetectorInterface
{	
	/**
     * Accessor for whether the main processing thread is running.
     * 
     * @return True if thread loop is active
     */
    public boolean isRunning();
	
	/**
     * Stop the main Bridgeport processing thread.
     */
	public void shutdown();
	
	/**
     * Add a listener for gamma messages
     * 
     * @param listener Listener to add
     */
    public void addBridgeportMessageListener(BridgeportEthernetMessageListener listener) throws BridgeportMessageListenerException;
    
    /**
     * Remove a listener for gamma messages
     * 
     * @param listener Listener to remove
     */
    public void removeBridgeportMessageListener(BridgeportEthernetMessageListener listener) throws BridgeportMessageListenerException;
    
	/**
     * Add a listener for gamma messages
     * 
     * @param listener Listener to add
     */
    public void addBridgeportMessageListener(BridgeportUsbMessageListener listener) throws BridgeportMessageListenerException;
    
    /**
     * Remove a listener for gamma messages
     * 
     * @param listener Listener to remove
     */
    public void removeBridgeportMessageListener(BridgeportUsbMessageListener listener) throws BridgeportMessageListenerException;
}
