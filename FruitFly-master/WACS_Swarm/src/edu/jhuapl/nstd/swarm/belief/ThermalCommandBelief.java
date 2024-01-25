/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import java.util.Date;

/**
 *
 * @author stipeja1
 */
public class ThermalCommandBelief extends Belief
{

    public static final String BELIEF_NAME = "ThermalCommandBelief";

    public static enum ThermalCommand
    {
        FanOn,
        FanOff,
        HeaterOn,
        HeaterOff,
        AutoOn,
        AutoOff
    }

    private int    _pod;
    ThermalCommand _command;

    public ThermalCommandBelief(String agentID,ThermalCommand s, int pod)
    {
        super(agentID);
        timestamp = new Date(System.currentTimeMillis());
        _command = s;
        _pod = pod;

    }

    public void setThermalCommand(ThermalCommand s)
    {
        _command = s;
    }

    public ThermalCommand getThermalCommand()
    {
        return _command;
    }

    public String getStateText()
    {
        String retval = "";
        if(_command == null)
            return "Unknown";

        switch(_command)
        {
                    case FanOn:
                        retval = "Pod "+ _pod + " Fan On Manual";
                        break;
                   case FanOff:
                        retval = "Pod "+ _pod + " Fan Off Manual";
                        break;
                    case HeaterOn:
                        retval = "Pod "+ _pod + " Heater On Manual";
                        break;
                    case HeaterOff:
                        retval = "Pod "+ _pod + " Heater Off Manual";
                        break;
                    case AutoOn:
                        retval = "Pod "+ _pod + " Auto On";
                        break;
                    case AutoOff:
                        retval = "Pod "+ _pod + " Auto Off";
                        break;
                    default:
                        retval = "Pod "+ _pod + " Unknown";
        }
        return retval;
    }


    @Override
    protected void addBelief(Belief b)
    {
         ThermalCommandBelief belief = (ThermalCommandBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._command = belief.getThermalCommand();
          this._pod = belief.getPod();
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
     * @return the _pod
     */
    public int getPod() {
        return _pod;
    }

    /**
     * @param pod the _pod to set
     */
    public void setPod(int pod) {
        this._pod = pod;
    }


}
