package edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.AETNADetectionReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 * 
 * @author olsoncc1
 *
 */
public class bladewerxAETNADetectionReportMessage extends AETNADetectionReportMessage
{
	/**
	 * Constructor for the Bladewerx Detection Report Message
	 * generated when using the AETNA template library.
	 */
	public bladewerxAETNADetectionReportMessage()
	{
		super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodMsg.BLADEWERX_AETNA_DETECTION_REPORT, 0);
	}
}