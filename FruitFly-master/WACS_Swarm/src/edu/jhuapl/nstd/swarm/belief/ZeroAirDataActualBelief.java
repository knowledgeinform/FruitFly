/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class ZeroAirDataActualBelief extends ZeroAirDataBelief
{
    public static final String BELIEF_NAME = "ZeroAirDataActualBelief";
 
    public ZeroAirDataActualBelief(Double altMSLm, Double basePressurePa)
    {
        super(altMSLm, basePressurePa);
    }
    
    /**
     * Returns the unique name for this belief type.
     * @return A unique name for this belief type.
     */
    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }
}
