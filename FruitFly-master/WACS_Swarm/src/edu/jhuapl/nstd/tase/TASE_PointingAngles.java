package edu.jhuapl.nstd.tase;

public class TASE_PointingAngles {
    public double Pan, Tilt;

    public TASE_PointingAngles() {
	Pan = Tilt = 0.0;
    }

    public TASE_PointingAngles(double pan, double tilt) {
	Pan = pan;
	Tilt = tilt;
    }
    
}
