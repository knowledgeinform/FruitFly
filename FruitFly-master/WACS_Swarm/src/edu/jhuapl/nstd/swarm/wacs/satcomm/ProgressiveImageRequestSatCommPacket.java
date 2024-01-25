/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.wacs.satcomm;

import edu.jhuapl.nstd.swarm.belief.Belief;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.nstd.swarm.belief.VideoClientSatCommFrameRequestBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.VideoClientSatCommFrameRequestBelief;
import java.nio.ByteBuffer;
import java.util.Date;
/**
 *
 * @author xud1
 */
public class ProgressiveImageRequestSatCommPacket extends GenericSatCommPacket
{
    private int m_Receipt = 0;
    private int m_DesiredReceipt = 0;

    public ProgressiveImageRequestSatCommPacket(BeliefManagerWacs belMgr, int messageType, int messageLength, long defaultSendPeriodMs)
    {
        super (belMgr, messageType, messageLength, defaultSendPeriodMs);
    }
    
    @Override
    protected void parseActualPacketDetails (byte []bufferedMsg, int startPos)
    {
        m_Receipt = ByteBuffer.wrap(bufferedMsg, startPos, 4).getInt();
        startPos +=4;        
    }
    
    @Override
    protected boolean desiredPacketEqualsActualPacket()
    {
        boolean equal = false;
        
        if (m_Receipt == m_DesiredReceipt)
        {
            return true;
        }

        return equal;
    }    
    
    @Override
    protected Belief createBelief()
    {
        VideoClientSatCommFrameRequestBeliefSatComm belief = new VideoClientSatCommFrameRequestBeliefSatComm(m_BeliefManager.getName(), m_Receipt, false, new Date (m_ActualTimestampMs));
        return belief;
    }    
    
    @Override
    protected boolean getDesiredPacketDetails ()
    {
        VideoClientSatCommFrameRequestBelief belief = (VideoClientSatCommFrameRequestBelief) m_BeliefManager.get(VideoClientSatCommFrameRequestBelief.BELIEF_NAME);
        
        if (belief != null)
        {
            m_DesiredReceipt = belief.getClientReceipt();
            m_DesiredTimestampMs = belief.getTimeStamp().getTime();
            return true;
        }
        else
        {
            return false;
        }        
    } 
    
    @Override
    protected int fillDesiredPacketBuffer (byte [] sendBuffer, int bufferPos)
    {        
        ///////////////////////////////////////////////////////
        //Define message fields        
        int receipt = m_DesiredReceipt;
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields        
        sendBuffer[bufferPos++] = (byte)(0xFF & (receipt >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (receipt >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (receipt >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & receipt);
        
        return bufferPos;
    }    
}
