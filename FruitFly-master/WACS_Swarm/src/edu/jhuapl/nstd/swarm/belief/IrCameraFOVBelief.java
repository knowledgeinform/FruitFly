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
public class IrCameraFOVBelief extends Belief
{
    public static final String BELIEF_NAME = "IrCameraFOVBelief";

    protected float m_MaxAngleFromForwardDeg;
    protected float m_MinAngleFromForwardDeg;

    public IrCameraFOVBelief(float maxAngFromForwardDeg, float minAngFromForwardDeg)
    {
        timestamp = new Date(System.currentTimeMillis());
        m_MaxAngleFromForwardDeg = maxAngFromForwardDeg;
        m_MinAngleFromForwardDeg = minAngFromForwardDeg;
    }

    public void setMaxAngleFromForwardDeg (float angle)
    {
        m_MaxAngleFromForwardDeg = angle;
    }

    public float getMaxAngleFromForwardDeg ()
    {
        return m_MaxAngleFromForwardDeg;
    }

    public void setMinAngleFromForwardDeg (float angle)
    {
        m_MinAngleFromForwardDeg = angle;
    }

    public float getMinAngleFromForwardDeg ()
    {
        return m_MinAngleFromForwardDeg;
    }


    @Override
    protected void addBelief(Belief b)
    {
         IrCameraFOVBelief belief = (IrCameraFOVBelief)b;
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this.m_MaxAngleFromForwardDeg = belief.getMaxAngleFromForwardDeg();
          this.m_MinAngleFromForwardDeg = belief.getMinAngleFromForwardDeg();
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
