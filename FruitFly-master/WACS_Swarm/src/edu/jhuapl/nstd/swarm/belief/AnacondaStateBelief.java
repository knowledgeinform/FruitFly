/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.AnacondaModeEnum;
import java.util.Date;

/**
 *
 * @author stipeja1
 */
public class AnacondaStateBelief extends Belief
{
    public static final String BELIEF_NAME = "AnacondaStateBelief";
    
    AnacondaModeEnum _state;
    private boolean _sample1full;
    private boolean _sample2full;
    private boolean _sample3full;
    private boolean _sample4full;

    public AnacondaStateBelief(String agentID,AnacondaModeEnum s)
    {
        this (agentID, s, new Date(System.currentTimeMillis()));
    }
    
    public AnacondaStateBelief(String agentID,AnacondaModeEnum s, Date time)
    {
        super(agentID);
        timestamp = time;
        _state = s;
        _sample1full = false;
        _sample2full = false;
        _sample3full = false;
        _sample4full = false;
    }
    
    public AnacondaStateBelief(String agentID,AnacondaModeEnum s, boolean s1, boolean s2, boolean s3, boolean s4)
    {
        this (agentID, s, new Date(System.currentTimeMillis()));
        _sample1full = s1;
        _sample2full = s2;
        _sample3full = s3;
        _sample4full = s4;
    }

    public void setAnacondState(AnacondaModeEnum s)
    {
        _state = s;
    }

    public AnacondaModeEnum getAnacondState()
    {
        return _state;
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

    public String getStateText()
    {
        String retval = "";
        if(_state == null)
            return "Unknown";
        
        switch(_state)
        {
                    case Pod:
                        retval = "Pod";
                        break;
                    case Standby:
                        retval = "Standby";
                        break;
                    case Search1:
                        retval = "Search 1";
                        break;
                    case Search2:
                        retval = "Search 2";
                        break;
                    case Search3:
                        retval = "Search 3";
                        break;
                    case Search4:
                        retval = "Search 4";
                        break;
                    case Idle:
                        retval = "Idle";
                        break;
                    case Airframe:
                        retval = "Airframe";
                        break;
        }
        return retval;
    }
    

    @Override
    protected void addBelief(Belief b)
    {
         AnacondaStateBelief belief = (AnacondaStateBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._state = belief.getAnacondState();
          this._sample1full = belief._sample1full;
          this._sample2full = belief._sample2full;
          this._sample3full = belief._sample3full;
          this._sample4full = belief._sample4full;
        }
    }
    
    @Override
    public boolean equals (Object obj)
    {
        if (obj == null || !(obj instanceof AnacondaStateBelief))
        {
            return false;
        }
        return (getAnacondState() == ((AnacondaStateBelief)obj).getAnacondState());
    }

      /**
   * Retuns the unique name for this belief type.
   * @return A unique name for this belief type.
   */
  public String getName()
  {
    return BELIEF_NAME;
  }

}
