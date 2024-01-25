/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages;
import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.cbrnPods.messages.compositeHistogramMessage;
import java.util.LinkedList;

/**
 *
 * @author stipeja1
 */
public class bladewerxCompositeHistogramMessage extends compositeHistogramMessage
{   
    public bladewerxCompositeHistogramMessage() 
    {
        super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodMsg.BLADEWERX_COMPOSITE_HISTOGRAM, 0);
    }
}
