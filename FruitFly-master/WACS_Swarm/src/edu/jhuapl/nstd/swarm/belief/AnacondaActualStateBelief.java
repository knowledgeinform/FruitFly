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
public class AnacondaActualStateBelief extends AnacondaStateBelief
{
    public static final String BELIEF_NAME = "AnacondaActualStateBelief";

    public AnacondaActualStateBelief(String agentID,AnacondaModeEnum s)
    {
        super(agentID, s);
    }
    
    public AnacondaActualStateBelief(String agentID,AnacondaModeEnum s, Date time)
    {
        super(agentID, s, time);
    }
    
    public AnacondaActualStateBelief(String agentID,AnacondaModeEnum s, boolean s1, boolean s2, boolean s3, boolean s4)
    {
        super (agentID, s, s1, s2, s3, s4);
    }
    
    
    @Override
    protected void addBelief(Belief b)
    {
         AnacondaActualStateBelief belief = (AnacondaActualStateBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._state = belief.getAnacondState();
          this.setSample1full(belief.isSample1full());
          this.setSample2full(belief.isSample2full());
          this.setSample3full(belief.isSample3full());
          this.setSample4full(belief.isSample4full());
        }
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
