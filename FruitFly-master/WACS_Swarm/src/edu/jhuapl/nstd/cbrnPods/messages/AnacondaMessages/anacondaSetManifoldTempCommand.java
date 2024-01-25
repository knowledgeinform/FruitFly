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
public class anacondaSetManifoldTempCommand extends cbrnPodCommand
{
    private short temp;   //Desired temperature
    
    public anacondaSetManifoldTempCommand()
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_SET_MANIFOLD_HEATERTEMP, 3);
    }
    
    /**
     * @param newTemp Desired temperature
     */
    public anacondaSetManifoldTempCommand (short newTemp)
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_SET_MANIFOLD_HEATERTEMP, 3);
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
