/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.ParticleCollectorMode;
import java.util.Date;

/**
 *
 * @author stipeja1
 */
public class ParticleCollectorStateBelief extends Belief
{

    public static final String BELIEF_NAME = "ParticleCollectorStateBelief";

    protected ParticleCollectorMode _state;
    private boolean _sample1full;
    private boolean _sample2full;
    private boolean _sample3full;
    private boolean _sample4full;


    public ParticleCollectorStateBelief(String agentID,ParticleCollectorMode s)
    {
        this (agentID, s, new Date (System.currentTimeMillis()));
    }
    
    public ParticleCollectorStateBelief(String agentID,ParticleCollectorMode s, Date time)
    {
        super(agentID);
        timestamp = time;
        _state = s;
        _sample1full = false;
        _sample2full = false;
        _sample3full = false;
        _sample4full = false;

    }


    public ParticleCollectorStateBelief(String agentID,ParticleCollectorMode s, boolean s1, boolean s2, boolean s3, boolean s4)
    {
        super(agentID);
        timestamp = new Date(System.currentTimeMillis());
        _state = s;
        _sample1full = s1;
        _sample2full = s2;
        _sample3full = s3;
        _sample4full = s4;

    }

    public void setParticleCollectorState(ParticleCollectorMode s)
    {
        _state = s;
    }

    public ParticleCollectorMode getParticleCollectorState()
    {
        return _state;
    }

    public String getStateText()
    {
        String retval = "";
        if(_state == null)
            return "Unknown";

        switch(_state)
        {
                    case Cleaning:
                        retval = "Cleaning";
                        break;
                    case Priming:
                        retval = "Priming";
                        break;
                    case Collecting:
                        retval = "Collecting";
                        break;
                    case StoringSample1:
                        retval = "Storing Sample 1";
                        break;
                    case StoringSample2:
                        retval = "Storing Sample 2";
                        break;
                    case StoringSample3:
                        retval = "Storing Sample 3";
                        break;
                    case StoringSample4:
                        retval = "Storing Sample 4";
                        break;
                    case Reset:
                        retval = "Reset";
                        break;
                    default:
                        retval = "Idle";
        }
        return retval;
    }


    @Override
    protected void addBelief(Belief b)
    {
         ParticleCollectorStateBelief belief = (ParticleCollectorStateBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._state = belief.getParticleCollectorState();
        }
    }

    @Override
    public boolean equals (Object obj)
    {
        if (obj == null || !(obj instanceof ParticleCollectorStateBelief))
        {
            return false;
        }
        return (getParticleCollectorState() == ((ParticleCollectorStateBelief)obj).getParticleCollectorState());
    }


      /**
   * Retuns the unique name for this belief type.
   * @return A unique name for this belief type.
   */
  public String getName()
  {
    return BELIEF_NAME;
  }

      /**
     * @return the _sample1full
     */
    public boolean isSample1full() {
        return _sample1full;
    }

    /**
     * @param sample1full the _sample1full to set
     */
    public void setSample1full(boolean sample1full) {
        this._sample1full = sample1full;
    }

    /**
     * @return the _sample2full
     */
    public boolean isSample2full() {
        return _sample2full;
    }

    /**
     * @param sample2full the _sample2full to set
     */
    public void setSample2full(boolean sample2full) {
        this._sample2full = sample2full;
    }

    /**
     * @return the _sample3full
     */
    public boolean isSample3full() {
        return _sample3full;
    }

    /**
     * @param sample3full the _sample3full to set
     */
    public void setSample3full(boolean sample3full) {
        this._sample3full = sample3full;
    }

    /**
     * @return the _sample4full
     */
    public boolean isSample4full() {
        return _sample4full;
    }

    /**
     * @param sample4full the _sample4full to set
     */
    public void setSample4full(boolean sample4full) {
        this._sample4full = sample4full;
    }

}