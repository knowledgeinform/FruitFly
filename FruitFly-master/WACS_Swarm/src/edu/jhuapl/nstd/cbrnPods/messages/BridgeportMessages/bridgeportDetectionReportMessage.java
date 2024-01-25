package edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.AETNADetectionReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 *
 * @author stipeja1
 */
public class bridgeportDetectionReportMessage extends AETNADetectionReportMessage
{	
    public bridgeportDetectionReportMessage()
    {
        super(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodMsg.BRIDGEPORT_DETECTION_REPORT, 0);
    }
}
