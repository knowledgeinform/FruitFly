/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.wacs.satcomm;

import edu.jhuapl.nstd.swarm.WACSDisplayAgent;
import edu.jhuapl.nstd.swarm.belief.Belief;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.nstd.swarm.wacs.SatCommMessageArbitrator;

/**
 *
 * @author humphjc1
 */
public abstract class GenericSatCommPacket 
{
    protected BeliefManagerWacs m_BeliefManager;
    
    protected long m_PacketSendPeriodMs;
    private boolean m_PacketEchoRequired;
    private boolean m_RemoteRequiresEcho;
    protected int m_MessageType;
    protected int m_MessageLength;
    protected long m_LastSentMessage_TimestampMs;
    
    private boolean m_DesiredSettingsValid;
    protected long m_DesiredTimestampMs;
    private boolean m_ActualSettingsValid;
    protected long m_ActualTimestampMs;
    
    public GenericSatCommPacket (BeliefManagerWacs belMgr, int messageType, int messageLength, long packetSendPeriodMs)
    {
        m_BeliefManager = belMgr;

        m_PacketEchoRequired = false;
        m_RemoteRequiresEcho = false;
        m_PacketSendPeriodMs = packetSendPeriodMs;
        m_MessageType = messageType;
        m_MessageLength = messageLength;
        m_LastSentMessage_TimestampMs = -1;

        m_DesiredSettingsValid = false;
        m_DesiredTimestampMs = -1;
        m_ActualSettingsValid = false;
        m_ActualTimestampMs = -1;
    }
    
    public void processMessage (byte [] bufferedMsg, int startPos, long dataTimestampMs)
    {
        byte value = bufferedMsg[startPos++];
        m_ActualSettingsValid = (0x01 & (value)) != 0;
        m_RemoteRequiresEcho = (0x01 & (value >> 1)) != 0;
        
        parseActualPacketDetails (bufferedMsg, startPos);
        
        m_ActualTimestampMs = dataTimestampMs;
        
        boolean desiredEqualsActual = desiredPacketEqualsActualPacket();
        //System.out.println ("Generic SATCOMM: Receiving message type: " + this.m_MessageType + "   required=" + m_PacketEchoRequired + ",remoteRequred=" + m_RemoteRequiresEcho + ",equal=" + desiredEqualsActual);
        
        m_PacketEchoRequired = false;
        if (((m_DesiredTimestampMs < m_ActualTimestampMs && !desiredEqualsActual) || !m_DesiredSettingsValid) && m_ActualSettingsValid)
        {
            Belief satCommBelief = createBelief();
            m_BeliefManager.put(satCommBelief);
        }
    }
    
    /**
     * Read packet details from buffer
     * @param bufferedMsg
     * @param startPos 
     */
    protected abstract void parseActualPacketDetails (byte []bufferedMsg, int startPos);
        
    /**
     * Return true if details in desired packet match (accounting for possible rounding errors) the details
     * in the actual packet.  Return false otherwise.
     * @return 
     */
    protected abstract boolean desiredPacketEqualsActualPacket();
        
    /**
     * Create the satcomm belief for this packet and publish it to the belief network
     */
    protected abstract Belief createBelief();
    
    
    public void formAndSendBelief (int destination, int headerSize, SatCommMessageArbitrator networkSender)
    {
        //Occassionally, check to see if safety box should be checked and sent out.
        if (System.currentTimeMillis() - m_LastSentMessage_TimestampMs > m_PacketSendPeriodMs)
        {
            m_LastSentMessage_TimestampMs = System.currentTimeMillis();
            
            //If subclass can't fill details of packet, then set bad packet settings so that someone else will send us their packet
            if (!getDesiredPacketDetails ())
            {
                m_DesiredSettingsValid = false;
                m_DesiredTimestampMs = System.currentTimeMillis();
            }
            else
            {
                m_DesiredSettingsValid = true;
            }

            if ((m_DesiredSettingsValid && m_DesiredTimestampMs > m_ActualTimestampMs && !desiredPacketEqualsActualPacket()) || m_RemoteRequiresEcho || (!m_DesiredSettingsValid && ((m_ActualSettingsValid && m_ActualTimestampMs > 0) || m_ActualTimestampMs <= 0)))
            {
                //System.out.println ("Generic SATCOMM: Sending message type: " + this.m_MessageType + "   required=" + m_PacketEchoRequired + ",remoteRequired=" + m_RemoteRequiresEcho + ",equal=" + desiredPacketEqualsActualPacket());
                if (m_RemoteRequiresEcho)
                    m_RemoteRequiresEcho = false;
                else
                    m_PacketEchoRequired = true;

                //desired message is more recent than message received through satellite, send new one to satellite and require feedback
                int msgDestination = destination;
                int msgPriority = 2;
                int msgType = m_MessageType;
                int fullMsgLength = m_MessageLength;

                //Message timestamp is based on when the data was created, not message is formed
                long msgTimeMs = m_DesiredTimestampMs;

                //Get a new buffer, filling in the header information
                byte sendBuffer[] = networkSender.getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);
                if (sendBuffer == null)
                    return;

                int bufferPos = headerSize;

                byte value = 0;
                value |= (m_DesiredSettingsValid?1:0);
                value |= ((m_PacketEchoRequired?1:0) << 1);
                sendBuffer[bufferPos++] = (byte)(0xFF & value);
                bufferPos = fillDesiredPacketBuffer (sendBuffer, bufferPos);
                
                ///////////////////////////////////////////////////////
                //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
                networkSender.sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);
            }
        }
    }

    /**
     * Get local copy of settings to be sent
     * @return True if settings are valid, false if invalid
     */
    protected abstract boolean getDesiredPacketDetails ();
            
    /**
     * Fill buffer with local copy of settings to be sent
     * @param sendBuffer
     * @param bufferPos Start position in sendBuffer to fill data
     * @return Updated bufferPos value
     */
    protected abstract int fillDesiredPacketBuffer (byte [] sendBuffer, int bufferPos);
    
    public int getMessageType ()
    {
        return m_MessageType;
    }
            
    
}
