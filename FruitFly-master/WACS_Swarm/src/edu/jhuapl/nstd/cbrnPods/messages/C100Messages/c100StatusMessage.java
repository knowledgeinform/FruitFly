package edu.jhuapl.nstd.cbrnPods.messages.C100Messages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 *
 * @author humphjc1
 */
public class c100StatusMessage extends cbrnPodMsg
{    
    /**
     * Text output of the status and version line
     */
    private String m_StatusAndVersion;
    
    //State of servo control
    private int servo1ToggledOpen;
    
    //Last duty cycle sent to servo
    private int lastServo1Duty;
    
    //True if servo control is overridden by user, not under auto control
    private boolean servo1ManualOverride;
    
    
    public c100StatusMessage() {
        super(cbrnSensorTypes.SENSOR_C100, cbrnPodMsg.C100_STATUS_TYPE, 0);
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        timestampMs = 1000*readDataUnsignedInt();
        servo1ToggledOpen = readDataByte();
        lastServo1Duty = readDataByte ();
        servo1ManualOverride = readDataBool();
        
        
        StringBuilder sb = new StringBuilder(super.length - HeaderSize - ChecksumSize);
        for (int i = 0; i < super.length - HeaderSize - ChecksumSize - 4; i++) {
            char newByte = (char) readDataByte();
            if (newByte != 0)
                sb.append(newByte);
            else
                break;
        }
        m_StatusAndVersion = sb.toString();
    }
    
    /**
     * Accessor for status and version text
     * @return
     */
    public String getStatusAndVersion ()
    {
        return m_StatusAndVersion;
    }
    
    /**
     * Modifier for status and version text
     * @param newVal
     */
    public void setStatusAndVersion (String newVal)
    {
        m_StatusAndVersion = newVal;
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
}
