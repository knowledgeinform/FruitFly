/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.wacs.satcomm;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.WACSDisplayAgent;
import edu.jhuapl.nstd.swarm.belief.Belief;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.nstd.swarm.belief.PodCommandBelief;
import edu.jhuapl.nstd.swarm.belief.PodCommandBeliefSatComm;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.wacs.SatCommMessageArbitrator;

/**
 *
 * @author humphjc1
 */
public class PodCommandSatCommPacket extends GenericSatCommPacket
{
    private int m_DesiredPodCommand;
    private int m_PodCommand;
    
    
    public PodCommandSatCommPacket (BeliefManagerWacs belMgr, int messageType, int messageLength, long defaultSendPeriodMs)
    {
        super (belMgr, messageType, messageLength, Config.getConfig().getPropertyAsLong("SatComm.PodCommandSendPeriod.Ms", defaultSendPeriodMs));   
    }
    
    @Override
    protected void parseActualPacketDetails (byte []bufferedMsg, int startPos)
    {
        int bufferPos = startPos;
        
        int commandType = 0;
        commandType |= (0xFF & bufferedMsg[bufferPos++]);
        
        m_PodCommand = commandType;
    }
        
    @Override
    protected boolean desiredPacketEqualsActualPacket()
    {
        if (m_PodCommand != m_DesiredPodCommand || 
                m_ActualTimestampMs != m_DesiredTimestampMs)
        {
            //Pod Command packet is a special case where timestamp will matter in comparison.  Since the same command
            //could be sent several times and should be used each time, compare the timestamp of the command as well
            //as the type of the command.
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
        PodCommandBeliefSatComm rdb = new PodCommandBeliefSatComm(WACSDisplayAgent.AGENTNAME, m_PodCommand, m_ActualTimestampMs);
        return rdb;
    }
    
    @Override
    protected boolean getDesiredPacketDetails ()
    {
        PodCommandBelief podCommandBelief = (PodCommandBelief) m_BeliefManager.get(PodCommandBelief.BELIEF_NAME);
        
        if (podCommandBelief != null)
        {
            m_DesiredPodCommand = podCommandBelief.getCommandCode();
            m_DesiredTimestampMs = podCommandBelief.getTimeStamp().getTime();
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
        int commandType = m_DesiredPodCommand;
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (commandType));
        
        return bufferPos;
    }
}
