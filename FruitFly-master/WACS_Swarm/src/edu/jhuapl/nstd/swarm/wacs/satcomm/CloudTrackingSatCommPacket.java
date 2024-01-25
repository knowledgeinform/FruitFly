/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.wacs.satcomm;

import edu.jhuapl.nstd.swarm.WACSDisplayAgent;
import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior;
import edu.jhuapl.nstd.swarm.belief.Belief;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.nstd.swarm.belief.ParticleCloudTrackingTypeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCloudTrackingTypeCommandedBeliefSatComm;
import edu.jhuapl.nstd.swarm.util.Config;

/**
 *
 * @author humphjc1
 */
public class CloudTrackingSatCommPacket extends GenericSatCommPacket
{
    private ParticleCloudPredictionBehavior.TRACKING_TYPE m_DesiredTrackingType = ParticleCloudPredictionBehavior.TRACKING_TYPE.MIXTURE;
    private ParticleCloudPredictionBehavior.TRACKING_TYPE m_TrackingType;
    
    
    public CloudTrackingSatCommPacket (BeliefManagerWacs belMgr, int messageType, int messageLength, long defaultSendPeriodMs)
    {
        super (belMgr, messageType, messageLength, Config.getConfig().getPropertyAsLong("SatComm.CloudTrackingTypeSendPeriod.Ms", defaultSendPeriodMs));   
    }
    
    @Override
    protected void parseActualPacketDetails (byte []bufferedMsg, int startPos)
    {
        int bufferPos = startPos;
        
        int trackingType = 0;
        trackingType |= (0xFF & bufferedMsg[bufferPos++]);
        
        m_TrackingType = ParticleCloudPredictionBehavior.TRACKING_TYPE.values()[trackingType];
    }
        
    @Override
    protected boolean desiredPacketEqualsActualPacket()
    {
        if (m_TrackingType != m_DesiredTrackingType)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
        
    @Override
    protected Belief createBelief()
    {
        ParticleCloudTrackingTypeCommandedBeliefSatComm belief = new ParticleCloudTrackingTypeCommandedBeliefSatComm(m_TrackingType, m_ActualTimestampMs);
        return belief;
    }
    
    @Override
    protected boolean getDesiredPacketDetails ()
    {
        ParticleCloudTrackingTypeCommandedBelief belief = (ParticleCloudTrackingTypeCommandedBelief) m_BeliefManager.get(ParticleCloudTrackingTypeCommandedBelief.BELIEF_NAME);
        
        if (belief != null)
        {
            m_DesiredTrackingType = belief.getTrackingType();
            m_DesiredTimestampMs = belief.getTimeStamp().getTime();
            return true;
        }
        else
            return false;
    }
            
    @Override
    protected int fillDesiredPacketBuffer (byte [] sendBuffer, int bufferPos)
    {
        ///////////////////////////////////////////////////////
        //Define message fields
        int trackingType = m_DesiredTrackingType.ordinal();
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (trackingType));
        
        return bufferPos;
    }
}
