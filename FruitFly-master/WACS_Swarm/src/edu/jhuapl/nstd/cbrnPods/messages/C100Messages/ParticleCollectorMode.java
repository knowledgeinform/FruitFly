/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.C100Messages;

/**
 *
 * @author stipeja1
 */
public enum ParticleCollectorMode
{
    Idle (0),
    StoringSample1 (1),
    StoringSample2 (2),
    StoringSample3 (3),
    StoringSample4 (4),
    Collecting (5),
    Cleaning (6),
    Priming (7),
    Reset (-1);


    private final int m_Value;
    private ParticleCollectorMode (int value)
    {
        m_Value = value;
    }
    public int getValue ()
    {
        return m_Value;
    }
    public static ParticleCollectorMode fromValue (int value)
    {
        if (value == StoringSample1.m_Value)
            return StoringSample1;
        else if (value == StoringSample2.m_Value)
            return StoringSample2;
        else if (value == StoringSample3.m_Value)
            return StoringSample3;
        else if (value == StoringSample4.m_Value)
            return StoringSample4;
        else if (value == Collecting.m_Value)
            return Collecting;
        else if (value == Cleaning.m_Value)
            return Cleaning;
        else if (value == Priming.m_Value)
            return Priming;
        else //if (value == Idle.m_Value)
            return Idle;
    }
}
