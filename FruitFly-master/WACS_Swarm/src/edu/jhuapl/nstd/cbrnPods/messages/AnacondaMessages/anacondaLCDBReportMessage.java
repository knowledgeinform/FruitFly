package edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages;

import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 * This class is used to store the contents of an Anaconda LCDB report message.
 * 
 * @author humphjc1
 */
public class anacondaLCDBReportMessage extends anacondaLCDReportMessage {

    public anacondaLCDBReportMessage() {
        super(cbrnPodMsg.ANACONDA_LCDB_REPORT, 0);
    }

}
