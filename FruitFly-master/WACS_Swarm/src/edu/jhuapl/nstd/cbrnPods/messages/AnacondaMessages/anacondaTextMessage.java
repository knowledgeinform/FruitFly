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
public class anacondaTextMessage extends cbrnPodMsg 
{
    long anacondaTimestamp;
    boolean messageTruncated;
    String message;

    
    public anacondaTextMessage() {
        super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_TEXT, 0);
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        timestampMs = 1000*readDataUnsignedInt();
        anacondaTimestamp = readDataUnsignedInt();
        int messageLength = readDataByte();
        messageTruncated = readDataBool();
        
        StringBuilder sb = new StringBuilder(messageLength);
        for (int i = 0; i < messageLength; i++) {
            char newByte = (char) readDataByte();
            if (newByte != 0)
                sb.append(newByte);
        }
        message = sb.toString();
    }
    
    public void setAnacondaTimestamp (long newAnacondaTimestamp)
    {
        anacondaTimestamp = newAnacondaTimestamp;
    }
    
    public long getAnacondaTimestamp ()
    {
        return anacondaTimestamp;
    }
    
    public void setMessageTruncated (boolean newVal)
    {
        messageTruncated = newVal;
    }
    
    public boolean getMessageTruncated ()
    {
        return messageTruncated;
    }
    
    public void setMessage (String newMessage)
    {
        message = newMessage;
    }
    
    public String getMessage ()
    {
        return message;
    }
}
