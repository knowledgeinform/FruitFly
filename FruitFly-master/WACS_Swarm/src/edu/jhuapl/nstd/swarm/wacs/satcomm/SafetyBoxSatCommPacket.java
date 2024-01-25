/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.wacs.satcomm;

import edu.jhuapl.nstd.swarm.belief.Belief;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.nstd.swarm.belief.SafetyBoxBelief;
import edu.jhuapl.nstd.swarm.belief.SafetyBoxBeliefSatComm;
import edu.jhuapl.nstd.swarm.util.Config;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class SafetyBoxSatCommPacket extends GenericSatCommPacket
{
    private double m_DesiredSafetyMaxRadius_m;
    private double m_DesiredSafetyMinRadius_m;
    private double m_DesiredSafetyMinAltitude_m;
    private double m_DesiredSafetyMaxAltitude_m;
    private boolean m_DesiredSafetyMinAlt_IsAGL;
    private boolean m_DesiredSafetyMaxAlt_IsAGL;
    private double m_DesiredSafetyLatitude1_deg;
    private double m_DesiredSafetyLongitude1_deg;
    private double m_DesiredSafetyLatitude2_deg;
    private double m_DesiredSafetyLongitude2_deg;
    private boolean m_DesiredSafetyIsPermanent;
    
    private double m_SafetyMaxRadius_m;
    private double m_SafetyMinRadius_m;
    private double m_SafetyMinAltitude_m;
    private double m_SafetyMaxAltitude_m;
    private boolean m_SafetyMinAlt_IsAGL;
    private boolean m_SafetyMaxAlt_IsAGL;
    private double m_SafetyLatitude1_deg;
    private double m_SafetyLongitude1_deg;
    private double m_SafetyLatitude2_deg;
    private double m_SafetyLongitude2_deg;
    private boolean m_SafetyIsPermanent;
    
    
    public SafetyBoxSatCommPacket (BeliefManagerWacs belMgr, int messageType, int messageLength, long defaultSendPeriodMs)
    {
        super (belMgr, messageType, messageLength, Config.getConfig().getPropertyAsLong("SatComm.SafetyBoxSendPeriod.Ms", defaultSendPeriodMs));   
    }
    
    @Override
    protected void parseActualPacketDetails (byte []bufferedMsg, int startPos)
    {
        int bufferPos = startPos;
        
        int maxRadiusM = 0;
        maxRadiusM |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        maxRadiusM |= (0xFF & bufferedMsg[bufferPos++]);
        
        int minRadiusM = 0;
        minRadiusM |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        minRadiusM |= (0xFF & bufferedMsg[bufferPos++]);
        
        int minAltM = 0;
        minAltM |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        minAltM |= (0xFF & bufferedMsg[bufferPos++]);
        
        int maxAltM = 0;
        maxAltM |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        maxAltM |= (0xFF & bufferedMsg[bufferPos++]);
        
        boolean minAltAgl = (bufferedMsg[bufferPos++]!=0);
        boolean maxAltAgl = (bufferedMsg[bufferPos++]!=0);
        
        long lat1LONG;
        lat1LONG = ByteBuffer.wrap(bufferedMsg, bufferPos, 8).getLong();
        bufferPos += 8;
        
        long lon1LONG;
        lon1LONG = ByteBuffer.wrap(bufferedMsg, bufferPos, 8).getLong();
        bufferPos += 8;
        
        long lat2LONG;
        lat2LONG = ByteBuffer.wrap(bufferedMsg, bufferPos, 8).getLong();
        bufferPos += 8;
        
        long lon2LONG;
        lon2LONG = ByteBuffer.wrap(bufferedMsg, bufferPos, 8).getLong();
        bufferPos += 8;
        
        boolean permanent = (bufferedMsg[bufferPos++]!=0);
        
        m_SafetyMaxRadius_m = maxRadiusM;
        m_SafetyMinRadius_m = minRadiusM;
        m_SafetyMinAltitude_m = minAltM;
        m_SafetyMaxAltitude_m = maxAltM;
        m_SafetyMinAlt_IsAGL = minAltAgl;
        m_SafetyMaxAlt_IsAGL = maxAltAgl;
        m_SafetyLatitude1_deg = Double.longBitsToDouble(lat1LONG);
        m_SafetyLongitude1_deg = Double.longBitsToDouble(lon1LONG);
        m_SafetyLatitude2_deg = Double.longBitsToDouble(lat2LONG);
        m_SafetyLongitude2_deg = Double.longBitsToDouble(lon2LONG);
        m_SafetyIsPermanent = permanent;
    }
        
    @Override
    protected boolean desiredPacketEqualsActualPacket()
    {
        if (Math.abs(m_SafetyMaxRadius_m - m_DesiredSafetyMaxRadius_m) > 1.5 || 
                    Math.abs(m_SafetyMinRadius_m - m_DesiredSafetyMinRadius_m) > 1.5 || 
                    Math.abs(m_SafetyMaxAltitude_m - m_DesiredSafetyMaxAltitude_m) > 1.5 || 
                    Math.abs(m_SafetyMinAltitude_m - m_DesiredSafetyMinAltitude_m) > 1.5 || 
                    m_SafetyMaxAlt_IsAGL != m_DesiredSafetyMaxAlt_IsAGL || 
                    m_SafetyMinAlt_IsAGL != m_DesiredSafetyMinAlt_IsAGL || 
                    Math.abs(m_SafetyLatitude1_deg - m_DesiredSafetyLatitude1_deg) > .0001 || 
                    Math.abs(m_SafetyLongitude1_deg - m_DesiredSafetyLongitude1_deg) > .0001 || 
                    Math.abs(m_SafetyLatitude2_deg - m_DesiredSafetyLatitude2_deg) > .0001 || 
                    Math.abs(m_SafetyLongitude2_deg - m_DesiredSafetyLongitude2_deg) > .0001 || 
                    m_SafetyIsPermanent != m_DesiredSafetyIsPermanent)
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
        SafetyBoxBeliefSatComm sbb = new SafetyBoxBeliefSatComm(m_BeliefManager.getName(), m_SafetyLatitude1_deg, m_SafetyLongitude1_deg, m_SafetyLatitude2_deg, m_SafetyLongitude2_deg, m_SafetyMaxAltitude_m, m_SafetyMaxAlt_IsAGL, m_SafetyMinAltitude_m, m_SafetyMinAlt_IsAGL, m_SafetyMaxRadius_m, m_SafetyMinRadius_m, m_SafetyIsPermanent, new Date (m_ActualTimestampMs));
        return sbb;
    }
    
    @Override
    protected boolean getDesiredPacketDetails ()
    {
        //Safety box belief
        SafetyBoxBelief safetyBelief = (SafetyBoxBelief) m_BeliefManager.get(SafetyBoxBelief.BELIEF_NAME);
        if (safetyBelief != null)
        {
            m_DesiredSafetyMaxRadius_m = safetyBelief.getMaxRadius_m();
            m_DesiredSafetyMinRadius_m = safetyBelief.getMinRadius_m();
            m_DesiredSafetyMinAltitude_m = safetyBelief.getMinAltitude_m();
            m_DesiredSafetyMaxAltitude_m = safetyBelief.getMaxAltitude_m();
            m_DesiredSafetyMinAlt_IsAGL = safetyBelief.getMinAlt_IsAGL();
            m_DesiredSafetyMaxAlt_IsAGL = safetyBelief.getMaxAlt_IsAGL();
            m_DesiredSafetyLatitude1_deg = safetyBelief.getLatitude1_deg();
            m_DesiredSafetyLongitude1_deg = safetyBelief.getLongitude1_deg();
            m_DesiredSafetyLatitude2_deg = safetyBelief.getLatitude2_deg();
            m_DesiredSafetyLongitude2_deg = safetyBelief.getLongitude2_deg();
            m_DesiredSafetyIsPermanent = safetyBelief.getIsPermanent();
            m_DesiredTimestampMs = safetyBelief.getTimeStamp().getTime();
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
        int maxRadiusM = (int)m_DesiredSafetyMaxRadius_m;
        int minRadiusM = (int)m_DesiredSafetyMinRadius_m;
        int minAltM = (int)m_DesiredSafetyMinAltitude_m;
        int maxAltM = (int)m_DesiredSafetyMaxAltitude_m;
        boolean minAltAgl = m_DesiredSafetyMinAlt_IsAGL;
        boolean maxAltAgl = m_DesiredSafetyMaxAlt_IsAGL;
        double lat1Deg = m_DesiredSafetyLatitude1_deg;
        long lat1DegLONG = Double.doubleToLongBits(lat1Deg);
        double lon1Deg = m_DesiredSafetyLongitude1_deg;
        long lon1DegLONG = Double.doubleToLongBits(lon1Deg);
        double lat2Deg = m_DesiredSafetyLatitude2_deg;
        long lat2DegLONG = Double.doubleToLongBits(lat2Deg);
        double lon2Deg = m_DesiredSafetyLongitude2_deg;
        long lon2DegLONG = Double.doubleToLongBits(lon2Deg);
        boolean permanent = m_DesiredSafetyIsPermanent;

        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (maxRadiusM >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (maxRadiusM));

        sendBuffer[bufferPos++] = (byte)(0xFF & (minRadiusM >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (minRadiusM));

        sendBuffer[bufferPos++] = (byte)(0xFF & (minAltM >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (minAltM));

        sendBuffer[bufferPos++] = (byte)(0xFF & (maxAltM >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (maxAltM));

        sendBuffer[bufferPos++] = (byte)(0xFF & (minAltAgl?1:0));
        sendBuffer[bufferPos++] = (byte)(0xFF & (maxAltAgl?1:0));

        sendBuffer[bufferPos++] = (byte)(0xFF & (lat1DegLONG >> 56));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lat1DegLONG >> 48));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lat1DegLONG >> 40));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lat1DegLONG >> 32));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lat1DegLONG >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lat1DegLONG >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lat1DegLONG >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lat1DegLONG));

        sendBuffer[bufferPos++] = (byte)(0xFF & (lon1DegLONG >> 56));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lon1DegLONG >> 48));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lon1DegLONG >> 40));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lon1DegLONG >> 32));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lon1DegLONG >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lon1DegLONG >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lon1DegLONG >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lon1DegLONG));

        sendBuffer[bufferPos++] = (byte)(0xFF & (lat2DegLONG >> 56));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lat2DegLONG >> 48));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lat2DegLONG >> 40));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lat2DegLONG >> 32));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lat2DegLONG >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lat2DegLONG >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lat2DegLONG >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lat2DegLONG));

        sendBuffer[bufferPos++] = (byte)(0xFF & (lon2DegLONG >> 56));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lon2DegLONG >> 48));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lon2DegLONG >> 40));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lon2DegLONG >> 32));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lon2DegLONG >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lon2DegLONG >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lon2DegLONG >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lon2DegLONG));

        sendBuffer[bufferPos++] = (byte)(0xFF & (permanent?1:0));
        
        return bufferPos;
    }
}
