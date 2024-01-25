package edu.jhuapl.nstd.cbrnPods.Canberra;

import edu.jhuapl.nstd.cbrnPods.messages.Canberra.CanberraDetectionMessage;

public interface CanberraDetectionMessageListener
{
	/**
	 * Receive CanberraDetectionMessage from the Canberra instrument.
	 * 
	 * @param m Received message
	 */
	public void handleDetectionMessage(CanberraDetectionMessage m);
}