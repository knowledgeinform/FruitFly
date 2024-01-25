/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.wacs.GCSSatCommMessageAbritrator;
import edu.jhuapl.nstd.swarm.wacs.PodSatCommMessageAribtrator;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author humphjc1
 */
public class BeliefManagerWacs extends BeliefManagerImpl
{
    boolean m_IgnoreGcsSatCommBeliefs;
    boolean m_IgnorePodSatCommBeliefs;
    
    public static final String SATCOMM_EXTENSION = "SatComm";
    
    protected ConcurrentHashMap <String, Belief> m_LocalSatCommBeliefs = new ConcurrentHashMap<String, Belief>();

    public BeliefManagerWacs(String name, boolean ignoreGcsSatComm, boolean ignorePodSatComm) throws IOException 
    {
        super(name);
        m_IgnoreGcsSatCommBeliefs = ignoreGcsSatComm;
        m_IgnorePodSatCommBeliefs = ignorePodSatComm;
    }
    
    
    @Override
    public boolean put (Belief belief)
    {
        if (belief.getName().endsWith(SATCOMM_EXTENSION))
        {
            Belief oldBelief = m_LocalSatCommBeliefs.get(belief.getName());
            if (/*!belief.equals(oldBelief) || */oldBelief == null || belief.isNewerThan(oldBelief))
                m_LocalSatCommBeliefs.put(belief.getName(), belief);
            return true;
        }
        
        return super.put(belief);
    }
    
    public Belief getSatCommBelief(String beliefName)
    {
        if (beliefName.endsWith(SATCOMM_EXTENSION))
            return m_LocalSatCommBeliefs.get(beliefName);
        else
            return m_LocalSatCommBeliefs.get(beliefName + SATCOMM_EXTENSION);
    }
    
    @Override
    public Belief get(String beliefName)
    {
        return get (beliefName, false);
    }
    
    public Belief get(String beliefName, boolean notSatComm)
    {
        Belief requested = super.get(beliefName);
        if (notSatComm)
            return requested;
        
        Belief satCommVersion = getSatCommBelief(beliefName);
        
        
        if (beliefName.equals (AgentPositionBelief.BELIEF_NAME) && requested != null && satCommVersion != null)
        {
            //We need to resolve the data within the agent position belief, because we could have local GCS display position being updated, but the WACS agent position updated through sat comm
            AgentPositionBelief posBlf = (AgentPositionBelief) requested;
            AgentPositionBelief satCommBlf = (AgentPositionBelief) satCommVersion;
            
            satCommBlf.addBelief(posBlf);
            return satCommBlf;
        }
        
        if (beliefName.equals (CloudDetectionBelief.BELIEF_NAME) && requested != null && satCommVersion != null)
        {
            //We need to resolve the data within the cloud detection belief, because we could have higher resolution, more accurate data in the 
            //non-sat-comm version, but could have data from no-LOS-comms-times stored in the SatComm version
            CloudDetectionBelief cdBlf = (CloudDetectionBelief) requested;
            CloudDetectionBelief satCommBlf = (CloudDetectionBelief) satCommVersion;
            
            //Add any data from the non-satcomm belief to the satcomm belief, overwriting anything with the same timestamp
            satCommBlf.addBelief(cdBlf, true);
            return satCommBlf;
        }
        
        /*if (beliefName.equals (RacetrackDefintionBelief.BELIEF_NAME))
        {
            RacetrackDefintionBelief b = (RacetrackDefintionBelief)super.get(RacetrackDefintionBelief.BELIEF_NAME);
            if (b != null)
            {
                System.out.println ("DEFINITION: " + b.getTimeStamp().getTime() + " " + b.getStartPosition().getLatitude().getDoubleValue(Angle.DEGREES));
            }
            else
            {
                System.out.println ("NO RACETRACK DEFINITION BELIEF DEFINED YET!!");
            }
            
            RacetrackOrbitBelief bo = (RacetrackOrbitBelief)super.get(RacetrackOrbitBelief.BELIEF_NAME);
            if (bo != null)
            {
                System.out.println ("ORBIT: " + bo.getTimeStamp().getTime() + " " + bo.getLatitude1().getDoubleValue(Angle.DEGREES));
            }
            else
            {
                System.out.println ("NO RACETRACK ORBIT BELIEF DEFINED YET!!");
            }
        }*/
        
        Belief retVal = null;
        if (requested != null && (satCommVersion == null || !satCommVersion.isNewerThan(requested)))
            retVal = requested;
        else 
            retVal = satCommVersion;
     
        return retVal;
    }
}
