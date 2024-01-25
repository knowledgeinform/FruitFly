/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.C100Messages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 *
 * @author humphjc1
 */
public class c100ConfigCommand extends cbrnPodCommand
{
    private char configOpt = 0;
    private int intParam = 0;
    
    public c100ConfigCommand()
    {
         super(cbrnSensorTypes.SENSOR_C100, cbrnPodCommand.C100_CONFIG_CMD, 6);
    }

    /**
     * @param configOpt Config option for this command
     * @param param Variable to store
     */
    public c100ConfigCommand(char configOpt, int param)
    {
         super(cbrnSensorTypes.SENSOR_C100, cbrnPodCommand.C100_CONFIG_CMD, 6);
         setOpt (configOpt);
         setParameter(param);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, configOpt);
        writeDataInt(2, intParam);
        return super.toByteArray();
    }

    /*
     * @param newConfigOpt Config option for this command
    */
    public void setOpt (char newConfigOpt)
    {
        configOpt = newConfigOpt;
    }
    
    /**
     * @return Config option
     */
    public int getOpt ()
    {
        return configOpt;
    }
    
    /**
     * @return the parameter
     */
    public int getParameter() {
        return intParam;
    }

    /**
     * @param param Parameter value to store
     */
    public void setParameter(int param) {
        intParam = param;
    }

}
