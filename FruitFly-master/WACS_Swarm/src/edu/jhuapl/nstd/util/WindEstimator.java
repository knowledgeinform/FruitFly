package edu.jhuapl.nstd.util;

import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;


public class WindEstimator
{
    static public class EstimatedWind
    {
        public Speed speed;
        public NavyAngle blowingToHeading;
    }

    static public  void estimateWind(double planeSpeedNorth_mps,
                                     double planeSpeedEast_mps,
                                     double planeAirSpeed_mps,
                                     double planePitch_rad,
                                     double planeHeading_rad,
                                     EstimatedWind estimatedWind)
    {
        double windVelocityNorth_mps = planeSpeedNorth_mps - planeAirSpeed_mps * Math.cos(planePitch_rad) * Math.cos(planeHeading_rad);
        double windVelocityEast_mps = planeSpeedEast_mps - planeAirSpeed_mps * Math.cos(planePitch_rad) * Math.sin(planeHeading_rad);

        estimatedWind.speed = new Speed(Math.sqrt (windVelocityNorth_mps * windVelocityNorth_mps + windVelocityEast_mps * windVelocityEast_mps), Speed.METERS_PER_SECOND);
        estimatedWind.blowingToHeading = new NavyAngle(Math.atan2(windVelocityEast_mps, windVelocityNorth_mps), Angle.RADIANS);
    }
}
