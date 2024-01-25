/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages;

/**
 *
 * @author stipeja1
 */
public enum AnacondaModeEnum {

    Idle (0),
    Search1 (1),
    Search2 (2),
    Search3 (3),
    Search4 (4),
    Pod (5),
    Standby (6),
    Airframe (7);
    
    
    private final int m_Value;
    private AnacondaModeEnum (int value)
    {
        m_Value = value;
    }
    public int getValue ()
    {
        return m_Value;
    }
    public static AnacondaModeEnum fromValue (int value)
    {
        if (value == Search1.m_Value)
            return Search1;
        else if (value == Search2.m_Value)
            return Search2;
        else if (value == Search3.m_Value)
            return Search3;
        else if (value == Search4.m_Value)
            return Search4;
        else if (value == Pod.m_Value)
            return Pod;
        else if (value == Standby.m_Value)
            return Standby;
        else if (value == Airframe.m_Value)
            return Airframe;
        else //if (value == Idle.m_Value)
            return Idle;
    }
}

