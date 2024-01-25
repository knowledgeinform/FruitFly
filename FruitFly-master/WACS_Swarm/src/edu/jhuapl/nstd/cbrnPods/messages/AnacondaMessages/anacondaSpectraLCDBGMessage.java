/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 *
 * @author humphjc1
 */
public class anacondaSpectraLCDBGMessage extends anacondaSpectraMessage {

    public anacondaSpectraLCDBGMessage() {
        super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDB_G_SPECTRA, 0);
    }
}
