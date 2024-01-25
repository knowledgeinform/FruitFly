/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.wacs;

import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior;

/**
 *
 * @author humphjc1
 */
public enum WacsMode 
{
    NO_MODE (0),
    LOITER (1),
    INTERCEPT (2),
    INGRESS (3),
    EGRESS (4);
    
    
    private final int m_Value;
    private WacsMode (int value)
    {
        m_Value = value;
    }
    public int getValue ()
    {
        return m_Value;
    }
    public String getModeString ()
    {
        if (m_Value == LOITER.m_Value)
            return LoiterBehavior.MODENAME;
        else if (m_Value == INTERCEPT.m_Value)
            return ParticleCloudPredictionBehavior.MODENAME;
        else if (m_Value == INGRESS.m_Value)
            return "ingress";
        else if (m_Value == EGRESS.m_Value)
            return "egress";
        else //if (value == NO_MODE.m_Value)
            return "";
    }
    public static WacsMode fromValue (int value)
    {
        if (value == LOITER.m_Value)
            return LOITER;
        else if (value == INTERCEPT.m_Value)
            return INTERCEPT;
        else if (value == INGRESS.m_Value)
            return INGRESS;
        else if (value == EGRESS.m_Value)
            return EGRESS;
        else //if (value == NO_MODE.m_Value)
            return NO_MODE;
    }
    public static WacsMode fromString (String string)
    {
        if (string == null)
            return NO_MODE;
        else if (string.equals (LoiterBehavior.MODENAME))
            return LOITER;
        else if (string.equals (ParticleCloudPredictionBehavior.MODENAME))
            return INTERCEPT;
        else
            return NO_MODE;
    }
}
