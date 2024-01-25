/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.Bridgeport;

import edu.jhuapl.nstd.cbrnPods.RNHistogram;
import edu.jhuapl.nstd.swarm.belief.CountItem;


/**
 *
 * Listener interface for Bridgeport messages
 * @author humphjc1
 */
public interface BridgeportEthernetMessageListener
{
    /**
     * Receive histogram messages from the Bridgeport sensor.
     *
     * @param m Received message
     */
    public void handleHistogramMessage(RNHistogram m);
    
    /**
     * Receive statistics messages from the Bridgeport sensor.
     *
     * @param m Received message
     */
    public void handleStatisticsMessage(GammaEthernetCountRates m);

    /**
     * Receive calibration messages from the Bridgeport sensor.
     *
     * @param m Received message
     */
    public void handleCalibrationMessage(GammaCalibration m);
}
