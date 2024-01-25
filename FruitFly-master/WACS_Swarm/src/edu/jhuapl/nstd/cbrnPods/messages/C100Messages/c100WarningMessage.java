package edu.jhuapl.nstd.cbrnPods.messages.C100Messages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 *
 * @author humphjc1
 */
public class c100WarningMessage extends cbrnPodMsg
{
    /**
     * Text output of the warning line
     */
    private String m_Warning;


    public c100WarningMessage() {
        super(cbrnSensorTypes.SENSOR_C100, cbrnPodMsg.C100_WARNING_TYPE, 0);
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        timestampMs = 1000*readDataUnsignedInt();

        StringBuilder sb = new StringBuilder(super.length - HeaderSize - ChecksumSize);
        for (int i = 0; i < super.length - HeaderSize - ChecksumSize - 4; i++) {
            char newByte = (char) readDataByte();
            if (newByte != 0)
                sb.append(newByte);
            else
                break;
        }
        m_Warning = sb.toString();
    }

    /**
     * Accessor for Warning text
     * @return
     */
    public String getWarning ()
    {
        return m_Warning;
    }

    /**
     * Modifier for Warning text
     * @param newVal
     */
    public void setWarning (String newVal)
    {
        m_Warning = newVal;
    }
}
