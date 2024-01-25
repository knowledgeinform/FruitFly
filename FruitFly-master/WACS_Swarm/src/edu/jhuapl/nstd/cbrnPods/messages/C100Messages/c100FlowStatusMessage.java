package edu.jhuapl.nstd.cbrnPods.messages.C100Messages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 *
 * @author humphjc1
 */
public class c100FlowStatusMessage extends cbrnPodMsg
{
    /**
     * Text output of the status line
     */
    private String m_Status;


    public c100FlowStatusMessage() {
        super(cbrnSensorTypes.SENSOR_C100, cbrnPodMsg.C100_FLOWSTATUS_TYPE, 0);
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
        m_Status = sb.toString();
    }

    /**
     * Accessor for status and version text
     * @return
     */
    public String getStatus ()
    {
        return m_Status;
    }

    /**
     * Modifier for status and version text
     * @param newVal
     */
    public void setStatus (String newVal)
    {
        m_Status = newVal;
    }
}
