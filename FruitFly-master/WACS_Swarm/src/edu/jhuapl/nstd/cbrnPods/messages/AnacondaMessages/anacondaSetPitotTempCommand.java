/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 *
 * @author humphjc1
 */
public class anacondaSetPitotTempCommand extends cbrnPodCommand
{
    private short temp;   //Desired temperature
    
    public anacondaSetPitotTempCommand()
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_SET_PITOT_HEATERTEMP, 3);
    }
    
    /**
     * @param newTemp Desired temperature
     */
    public anacondaSetPitotTempCommand (short newTemp)
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_SET_PITOT_HEATERTEMP, 3);
         setTemp (newTemp);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataShort(1, temp);
        return super.toByteArray();
    }

    /**
     * @return the Desired temperature
     */
    public short getTemp() {
        return temp;
    }

    /**
     * @param newTemp Desired temperature
     */
    public void setTemp(short newTemp) {
        this.temp = newTemp;
    }

}
