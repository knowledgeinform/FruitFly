/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.nstd.swarm.wacs.WacsMode;

/**
 *
 * @author humphjc1
 */
public interface ShadowWavsmSimChangeListener 
{

    public void setLoiterLatRad(double newVal);

    public void setLoiterLonRad(double newVal);

    public void setLoiterRadiusM(double newVal);

    public void setLoiterAltMslM(double newVal);

    public void setLoiterCWDir(boolean newVal);

    public void setWacsMode(WacsMode mode);

    public void setLoiterValid(boolean newVal);

    public void setStrikeValid(boolean newVal);

    public void setStrikeLatRad(double newVal);

    public void setStrikeLonRad(double newVal);

    public void setStrikeAltMslM(double newVal);

    public void setTcdlActive(boolean newVal);

    public void setSatCommThrottle(boolean newVal);
    
}
