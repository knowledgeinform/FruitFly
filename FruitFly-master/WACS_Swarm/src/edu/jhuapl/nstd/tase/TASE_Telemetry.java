package edu.jhuapl.nstd.tase;

public class TASE_Telemetry {
    public double Lat, Lon, AltEllip;
    public double CameraRoll, CameraPitch, CameraYaw;
    public double PDOP;
    public int GPS_Status;

    public TASE_Telemetry() {
	Lat = Lon = AltEllip = CameraRoll = CameraPitch = CameraYaw = 0.0;
	PDOP = Double.MAX_VALUE;
	GPS_Status = 0;
    }

    public TASE_Telemetry(double lat, double lon, double alt,
			  double roll, double pitch, double yaw,
			  double pdop, int status) {
	Lat = lat;
	Lon = lon;
	AltEllip = alt;
	CameraRoll = roll;
	CameraPitch = pitch;
	CameraYaw = yaw;
	PDOP = pdop;
	GPS_Status = status;
    }
    
}
