package edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages;

import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 * This class is used to store the contents of an Anaconda LCDA report message.
 * 
 * @author humphjc1
 */
public class anacondaLCDAReportMessage extends anacondaLCDReportMessage {

    public anacondaLCDAReportMessage() {
        super(cbrnPodMsg.ANACONDA_LCDA_REPORT, 0);
    }

}
