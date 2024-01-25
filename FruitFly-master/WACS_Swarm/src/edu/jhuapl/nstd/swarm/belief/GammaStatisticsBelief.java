package edu.jhuapl.nstd.swarm.belief;

import java.util.Date;

/**
 *
 * @author stipeja1
 */
public class GammaStatisticsBelief extends Belief
{
    public static final String BELIEF_NAME = "GammaStatisticsBelief";
    private int m_RealTicks;
    private int m_NumEvents;
    private int m_NumTriggers;
    private int m_DeadTicks;
    private float m_RealTime;
    private float m_EventRate;
    private float m_TriggerRate;
    private float m_DeadTimeFraction;
    private float m_InputRate;

    public GammaStatisticsBelief(String agentID)
    {
        this (agentID, new Date (System.currentTimeMillis()));
    }
    
    public GammaStatisticsBelief(String agentID, Date time)
    {
        super(agentID);
        timestamp = time;
        m_RealTicks = 0;
        m_NumEvents = 0;
        m_NumTriggers = 0;
        m_DeadTicks = 0;
        m_RealTime = 0;
        m_EventRate = 0;
        m_TriggerRate = 0;
        m_DeadTimeFraction = 0;
        m_InputRate = 0;
    }


    @Override
    protected void addBelief(Belief b)
    {
         GammaStatisticsBelief belief = (GammaStatisticsBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
            this.timestamp = belief.getTimeStamp();
            m_RealTicks = belief.getRealTicks();
            m_NumEvents = belief.getNumEvents();
            m_NumTriggers = belief.getNumTriggers();
            m_DeadTicks = belief.getDeadTicks();
            m_RealTime = belief.getRealTime();
            m_EventRate = belief.getEventRate();
            m_TriggerRate = belief.getTriggerRate();
            m_DeadTimeFraction = belief.getDeadTimeFraction();
            m_InputRate = belief.getInputRate();
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

    /**
     * @return the m_RealTicks
     */
    public int getRealTicks() {
        return m_RealTicks;
    }

    /**
     * @param m_RealTicks the m_RealTicks to set
     */
    public void setRealTicks(int m_RealTicks) {
        this.m_RealTicks = m_RealTicks;
    }

    /**
     * @return the m_NumEvents
     */
    public int getNumEvents() {
        return m_NumEvents;
    }

    /**
     * @param m_NumEvents the m_NumEvents to set
     */
    public void setNumEvents(int m_NumEvents) {
        this.m_NumEvents = m_NumEvents;
    }

    /**
     * @return the m_NumTriggers
     */
    public int getNumTriggers() {
        return m_NumTriggers;
    }

    /**
     * @param m_NumTriggers the m_NumTriggers to set
     */
    public void setNumTriggers(int m_NumTriggers) {
        this.m_NumTriggers = m_NumTriggers;
    }

    /**
     * @return the m_DeadTicks
     */
    public int getDeadTicks() {
        return m_DeadTicks;
    }

    /**
     * @param m_DeadTicks the m_DeadTicks to set
     */
    public void setDeadTicks(int m_DeadTicks) {
        this.m_DeadTicks = m_DeadTicks;
    }

    /**
     * @return the m_RealTime
     */
    public float getRealTime() {
        return m_RealTime;
    }

    /**
     * @param m_RealTime the m_RealTime to set
     */
    public void setRealTime(float m_RealTime) {
        this.m_RealTime = m_RealTime;
    }

    /**
     * @return the m_EventRate
     */
    public float getEventRate() {
        return m_EventRate;
    }

    /**
     * @param m_EventRate the m_EventRate to set
     */
    public void setEventRate(float m_EventRate) {
        this.m_EventRate = m_EventRate;
    }

    /**
     * @return the m_TriggerRate
     */
    public float getTriggerRate() {
        return m_TriggerRate;
    }

    /**
     * @param m_TriggerRate the m_TriggerRate to set
     */
    public void setTriggerRate(float m_TriggerRate) {
        this.m_TriggerRate = m_TriggerRate;
    }

    /**
     * @return the m_DeadTimeFraction
     */
    public float getDeadTimeFraction() {
        return m_DeadTimeFraction;
    }

    /**
     * @param m_DeadTimeFraction the m_DeadTimeFraction to set
     */
    public void setDeadTimeFraction(float m_DeadTimeFraction) {
        this.m_DeadTimeFraction = m_DeadTimeFraction;
    }

    /**
     * @return the m_InputRate
     */
    public float getInputRate() {
        return m_InputRate;
    }

    /**
     * @param m_InputRate the m_InputRate to set
     */
    public void setM_InputRate(float m_InputRate) {
        this.m_InputRate = m_InputRate;
    }


}
