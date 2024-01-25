/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.cbrnPods.messages.compositeHistogramMessage;

/**
 *
 * @author stipeja1
 */
public class bridgeportCompositeHistogramMessage extends compositeHistogramMessage
{   
    public bridgeportCompositeHistogramMessage() 
    {
        super(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodMsg.BRIDGEPORT_COMPOSITE_HISTOGRAM, 0);
    }
}
