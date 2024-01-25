package edu.jhuapl.nstd.piccolo;

public class Pic_Telemetry {
    public double Lat, Lon, AltWGS84, AltMSL, AltLaser_m;
    public double Roll, Pitch, Yaw, TrueHeading;
    public double WindSouth, WindWest;
    public double VelNorth, VelEast, VelDown;
    public double IndAirSpeed_mps;
    public double PDOP;
    public int GPS_Status;
    public double StaticPressPa;
    public double OutsideAirTempC;
    public boolean AltLaserValid;
    public long m_TimestampMs;
    public String rawMessage;

    public Pic_Telemetry() {
    }

    public Pic_Telemetry(Pic_Telemetry telem)
    {
        this.Lat = telem.Lat;
        this.Lon = telem.Lon;
        this.AltWGS84 = telem.AltWGS84;
        this.AltMSL = telem.AltMSL;
        this.AltLaser_m = telem.AltLaser_m;
        this.Roll = telem.Roll;
        this.Pitch = telem.Pitch;
        this.Yaw = telem.Yaw;
        this.TrueHeading = telem.TrueHeading;
        this.WindSouth = telem.WindSouth;
        this.WindWest = telem.WindWest;
        this.VelNorth = telem.VelNorth;
        this.VelEast = telem.VelEast;
        this.VelDown = telem.VelDown;
        this.IndAirSpeed_mps = telem.IndAirSpeed_mps;
        this.PDOP = telem.PDOP;
        this.GPS_Status = telem.GPS_Status;
        this.StaticPressPa = telem.StaticPressPa;
        this.OutsideAirTempC = telem.OutsideAirTempC;
        this.AltLaserValid = telem.AltLaserValid;
        this.m_TimestampMs = telem.m_TimestampMs;
        this.rawMessage = telem.rawMessage;
    }

    public String toLogString ()
    {
        return String.format (
            "[Lat: %3.6f Lon: %3.6f AltWGS84: %3.3f AltMSL: %3.3f AltAGL_m: %3.3f AltLaserValid: %s Roll: %3.3f Pitch: %3.3f Yaw: %3.3f TrueHeading: %3.3f WindSouth: %3.3f WindWest: %3.3f VelNorth: %3.3f VelEast: %3.3f VelDown: %3.3f IndAirSpeed: %3.3f PDOP: %3.3f GPS_Status: %d StaticPressPa: %3.3f OutsideAirTempC: %3.3f]",
            Lat, Lon, AltWGS84, AltMSL, AltLaser_m, (AltLaserValid ? "Y" : "N"),
            Roll, Pitch, Yaw, TrueHeading,
            WindSouth, WindWest,
            VelNorth, VelEast, VelDown,
            IndAirSpeed_mps,
            PDOP,
            GPS_Status,
            StaticPressPa,
            OutsideAirTempC
            );
    }

}
