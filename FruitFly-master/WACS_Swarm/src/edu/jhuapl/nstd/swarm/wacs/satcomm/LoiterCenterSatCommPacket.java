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
import edu.jhuapl.nstd.swarm.belief.Belief;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionCommandedBeliefSatComm;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.wacs.SatCommMessageArbitrator;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class LoiterCenterSatCommPacket extends GenericSatCommPacket
{
    private double m_DesiredLoiterCenterLatitudeDeg;
    private double m_DesiredLoiterCenterLongitudeDeg;
    private double m_LoiterCenterLatitudeDeg;
    private double m_LoiterCenterLongitudeDeg;
    
    public LoiterCenterSatCommPacket (BeliefManagerWacs belMgr, int messageType, int messageLength, long defaultSendPeriodMs)
    {
        super (belMgr, messageType, messageLength, Config.getConfig().getPropertyAsLong("SatComm.LoiterCenterSendPeriod.Ms", defaultSendPeriodMs));   
    }
    
    @Override
    protected void parseActualPacketDetails (byte []bufferedMsg, int startPos)
    {
        int bufferPos = startPos;
        
        int latRadPacked = 0;
        latRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        latRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        latRadPacked |= (0xFF & bufferedMsg[bufferPos++]);
        int lonRadPacked = 0;
        lonRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        lonRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        lonRadPacked |= (0xFF & bufferedMsg[bufferPos++]);
        bufferPos++;
        
        m_LoiterCenterLatitudeDeg = Math.max(-90, Math.min(90, SatCommMessageArbitrator.extractLatDegFromRad3BytePacked(latRadPacked)));
        if (m_LoiterCenterLatitudeDeg == -90)
            m_LoiterCenterLatitudeDeg = 90;
        m_LoiterCenterLongitudeDeg = Math.max(-180, Math.min(180, SatCommMessageArbitrator.extractLonDegFromRad3BytePacked(lonRadPacked)));
        if (m_LoiterCenterLongitudeDeg == -180)
            m_LoiterCenterLongitudeDeg = 180;
    }
        
    @Override
    protected boolean desiredPacketEqualsActualPacket()
    {
        if (Math.abs(m_LoiterCenterLatitudeDeg - m_DesiredLoiterCenterLatitudeDeg) > .0001 || 
                    Math.abs(m_LoiterCenterLongitudeDeg - m_DesiredLoiterCenterLongitudeDeg) > .0001)
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
        LatLonAltPosition lla = new LatLonAltPosition(new Latitude (m_LoiterCenterLatitudeDeg, Angle.DEGREES), new Longitude (m_LoiterCenterLongitudeDeg, Angle.DEGREES), Altitude.ZERO);
        RacetrackDefinitionCommandedBeliefSatComm rdb = new RacetrackDefinitionCommandedBeliefSatComm(lla, m_ActualTimestampMs);
        return rdb;
    }
    
    @Override
    protected boolean getDesiredPacketDetails ()
    {
        RacetrackDefinitionCommandedBelief racetrackBelief = (RacetrackDefinitionCommandedBelief) m_BeliefManager.get(RacetrackDefinitionCommandedBelief.BELIEF_NAME);
        
        if (racetrackBelief != null)
        {
            LatLonAltPosition lla = racetrackBelief.getStartPosition().asLatLonAltPosition();
            m_DesiredLoiterCenterLatitudeDeg = lla.getLatitude().getDoubleValue(Angle.DEGREES);
            m_DesiredLoiterCenterLongitudeDeg = lla.getLongitude().getDoubleValue(Angle.DEGREES);
            m_DesiredTimestampMs = racetrackBelief.getTimeStamp().getTime();
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
        int latRadPacked = SatCommMessageArbitrator.computeLatDegAsRad3BytePacked (m_DesiredLoiterCenterLatitudeDeg);
        int lonRadPacked = SatCommMessageArbitrator.computeLonDegAsRad3BytePacked (m_DesiredLoiterCenterLongitudeDeg);

        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (latRadPacked >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (latRadPacked >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (latRadPacked));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lonRadPacked >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lonRadPacked >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lonRadPacked));
        sendBuffer[bufferPos++] = (byte)(0);
        
        return bufferPos;
    }
}

