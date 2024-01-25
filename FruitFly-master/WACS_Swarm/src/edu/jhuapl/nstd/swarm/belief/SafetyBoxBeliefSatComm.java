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
public class SafetyBoxBeliefSatComm extends SafetyBoxBelief
{
    public static final String BELIEF_NAME = SafetyBoxBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    
    public SafetyBoxBeliefSatComm()
    {
        super ();
    }

    public SafetyBoxBeliefSatComm(String publishingAgentName,
                           double latitude1_deg,
                           double longitude1_deg,
                           double latitude2_deg,
                           double longitude2_deg,
                           double maxAltitude_m,
                           boolean maxAlt_IsAGL,
                           double minAltitude_m,
                           boolean minAlt_IsAGL,
                           double maxRadius_m,
                           double minRadius_m,
                           boolean isPermanent)
    {
        this (publishingAgentName, latitude1_deg, longitude1_deg, latitude2_deg, longitude2_deg, maxAltitude_m, maxAlt_IsAGL, minAltitude_m, minAlt_IsAGL, maxRadius_m, minRadius_m, isPermanent, new Date (System.currentTimeMillis()));
    }
    
    public SafetyBoxBeliefSatComm(String publishingAgentName,
                           double latitude1_deg,
                           double longitude1_deg,
                           double latitude2_deg,
                           double longitude2_deg,
                           double maxAltitude_m,
                           boolean maxAlt_IsAGL,
                           double minAltitude_m,
                           boolean minAlt_IsAGL,
                           double maxRadius_m,
                           double minRadius_m,
                           boolean isPermanent,
                           Date timestamp)
    {
        super (publishingAgentName, latitude1_deg, longitude1_deg, latitude2_deg, longitude2_deg, maxAltitude_m, maxAlt_IsAGL, minAltitude_m, minAlt_IsAGL, maxRadius_m, minRadius_m, isPermanent, timestamp);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
