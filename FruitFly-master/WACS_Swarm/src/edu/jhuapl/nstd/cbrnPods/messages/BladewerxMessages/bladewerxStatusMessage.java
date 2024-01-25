/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 *
 * @author humphjc1
 */
public class bladewerxStatusMessage extends cbrnPodMsg
{    
    /**
     * Scale setting on MCA
     */
    private int m_Scale;
    
    /**
     * Threshold setting on MCA
     */
    private int m_Threshold;
    
    /**
     * Gain setting on MCA
     */
    private int m_Gain;
    
    /**
     * Offset setting on MCA
     */
    private int m_Offset;
    
    /**
     * Last flow rate reported from MCA.  Only updated when requested
     */
    private int m_Flow;
    
    /**
     * Last battery level reported from MCA.  Only updated when requested
     */
    private int m_Battery;
    
    /**
     * True when in calibration mode, false otherwise
     */
    private boolean m_Calibrating;

    /**
     * True when bladewerx power has been toggled on.
     */
    private boolean m_PowerOn;
    
    //State of servo control
    private int servo1ToggledOpen;
    
    //Last duty cycle sent to servo
    private int lastServo1Duty;
    
    //True if servo control is overridden by user, not under auto control
    private boolean servo1ManualOverride;
    
    /**
     * Text output of the status and version line
     */
    private String m_Version;
    
    
    public bladewerxStatusMessage() {
        super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodMsg.BLADEWERX_STATUS_TYPE, 0);
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        timestampMs = 1000*readDataUnsignedInt();
        
        servo1ToggledOpen = readDataByte();
        lastServo1Duty = readDataByte ();
        servo1ManualOverride = readDataBool();
        m_Scale = readDataByte();
        m_Threshold = readDataByte();
        m_Gain = readDataByte();
        m_Offset = readDataByte();
        m_Flow = readDataShort();
        m_Battery = readDataShort();
        m_Calibrating = readDataBool();
        m_PowerOn = readDataBool();
        
        StringBuilder sb = new StringBuilder(super.length - HeaderSize - ChecksumSize - getReadIndex());
        for (int i = 0; i < super.length - HeaderSize - ChecksumSize - getReadIndex(); i++) {
            char newByte = (char) readDataByte();
            if (newByte != 0)
                sb.append(newByte);
        }
        m_Version = sb.toString();
    }
    
    /**
     * Set scale
     * @param new value
     */
    public void setScale (int scale)
    {
        m_Scale = scale;
    }
    
    /**
     * Get scale
     * \return requested value
     */
    public int getScale ()
    {
        return m_Scale;
    }
    
    /**
     * Set threshold
     * @param new value
     */
    public void setThreshold (int threshold)
    {
        m_Threshold = threshold;
    }
    
    /**
     * Get threshold
     * \return requested value
     */
    public int getThreshold ()
    {
        return m_Threshold;
    }
    
    /**
     * Set gain
     * @param new value
     */
    public void setGain (int gain)
    {
        m_Gain = gain;
    }
    
    /**
     * Get gain
     * \return requested value
     */
    public int getGain ()
    {
        return m_Gain;
    }
    
    /**
     * Set offset
     * @param new value
     */
    public void setOffset (int offset)
    {
        m_Offset = offset;
    }
    
    /**
     * Get offset
     * \return requested value
     */
    public int getOffset ()
    {
        return m_Offset;
    }
    
    /**
     * Set flow value
     * @param new value
     */
    public void setFlow (int flow)
    {
        m_Flow = flow;
    }
    
    /**
     * Get flow value
     * \return requested value
     */
    public int getFlow ()
    {
        return m_Flow;
    }
    
    /**
     * Set battery value
     * @param new value
     */
    public void setBattery (int battery)
    {
        m_Battery = battery;
    }
    
    /**
     * Get battery value
     * \return requested value
     */
    public int getBattery ()
    {
        return m_Battery;
    }
    
    /**
     * Set calibrating value
     * @param new value
     */
    public void setCalibrating (boolean calibrating)
    {
        m_Calibrating = calibrating;
    }
    
    /**
     * Get calibrating value
     * \return requested value
     */
    public boolean getCalibrating ()
    {
        return m_Calibrating;
    }

    /**
     * Set power on state
     * @param new value
     */
    public void setPowerOn (boolean powerOn)
    {
        m_PowerOn = powerOn;
    }

    /**
     * Get power on state
     * \return requested value
     */
    public boolean getPowerOn ()
    {
        return m_PowerOn;
    }
    
    /**
     * @return Status of servo control
     */
    public int getServo1ToggledOpen ()
    {
        return servo1ToggledOpen;
    }
    
    /**
     * @param newVal New value for status
     */
    public void setServo1ToggledOpen (int newVal)
    {
        servo1ToggledOpen = newVal;
    }
    
    /**
     * @return Last duty cycle sent to servo
     */
    public int getLastServo1Duty ()
    {
        return lastServo1Duty;
    }
    
    /**
     * @param newVal New value for Last duty cycle sent to servo
     */
    public void setLastServo1Duty (int newVal)
    {
        lastServo1Duty = newVal;
    }
    
    /**
     * @return True if user is overriding auto servo control
     */
    public boolean getServo1ManualOverride ()
    {
        return servo1ManualOverride;
    }
    
    /**
     * @param override If user is overriding auto servo control
     */
    public void setServo1ManualOverride (boolean override)
    {
        servo1ManualOverride = override;
    }
    
    /**
     * Accessor for version text
     * @return Version
     */
    public String getVersion ()
    {
        return m_Version;
    }
    
    /**
     * Modifier for version text
     * @param newVal
     */
    public void setVersion (String newVal)
    {
        m_Version = newVal;
    }

}
