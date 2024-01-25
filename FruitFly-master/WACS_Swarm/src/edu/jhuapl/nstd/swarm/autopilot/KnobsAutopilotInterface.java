/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.nstd.swarm.belief.BeliefManager;

/**
 *
 * @author humphjc1
 */
public interface KnobsAutopilotInterface 
{
    public abstract class CommandMessage 
    {
        public long timestamp_ms;
        public double rollCommand_rad;
        public double airspeedCommand_mps;
        public double altitudeCommand_m;

        public abstract void populateSpecificDetails(TelemetryMessage telemetryMessage, BeliefManager belMgr);
    }
    
    public abstract class TelemetryMessage
    {
        public long timestamp_ms;
        public double longitude_rad;
        public double latitude_rad;
        public double barometricAltitudeMsl_m;
        public double trueHeading_rad;
        public double groundSpeedNorth_mps;
        public double groundSpeedEast_mps;
        public double trueAirspeed_mps;
        public double indicatedAirspeed_mps;
        public double pitch_rad;
        public double roll_rad;
    }
    
    public abstract void setCommandMessage(CommandMessage copyFrom);

    public abstract void copyLatestCommandMessage(CommandMessage copyTo);

    public abstract boolean copyLatestTelemetryMessage(TelemetryMessage copyTo);
    
    public abstract CommandMessage getBlankCommandMessage();
    
    public abstract TelemetryMessage getBlankTelemetryMessage();
}
