package edu.jhuapl.nstd.tase;

import java.io.*;
import java.lang.*;

public class TestTASE implements TASE_PointingAnglesListener, TASE_TelemetryListener {

	public void handleTASE_PointingAngles(TASE_PointingAngles angles) {
		//System.out.println("TASE Pointing Angles => (pan,tilt) = ("+
		//				   angles.Pan+","+angles.Tilt+")\n");
	}

	public void handleTASE_Telemetry(TASE_Telemetry telem) {
		//System.out.println("TASE Telemetry => ");
		//System.out.println("\t(lat,lon,alt) = ("+telem.Lat+","+telem.Lon+","+telem.AltEllip+")");
		//System.out.println("\t(roll,pitch,yaw) = ("+telem.CameraRoll+","+telem.CameraPitch+","+telem.CameraYaw+")");
		//System.out.println("\t(PDOP,status) = ("+telem.PDOP+","+telem.GPS_Status+")\n");
	}

	public static void main(String[] args) {

		try {

			TestTASE tt = new TestTASE();
			TASE_Interface ti = new TASE_Interface("COM1");
			ti.addTASEPointingAnglesListener(tt);
			ti.addTASETelemetryListener(tt);

			(new Thread(ti)).start();

			int counter=0;
			double lat=33.0, lon=-76.0, alt=0.0;
			Thread.sleep(5000);
			while (true) {
                if (counter>=4) {
                    System.out.println("-----STOWING-----");
                    ti.sendTASE_Stow();
                } else {
                    System.out.println("Sending SPOI("+lat+","+lon+","+alt+") ...");
                    ti.sendTASE_SPOI(lat, lon, alt, 0.0, 0.0, 0.0);
                }

				counter++;
				switch (counter) {
					case 0:
						lat=33.0; lon=-76.0;
						break;
					case 1:
						lat=33.0; lon=76.0;
						break;
					case 2:
						lat=-33.0; lon=-76.0;
						break;
					case 3:
						lat=-33.0; lon=76.0;
						break;
                    case 4:
                        break;
					default:
						counter = 0;
						break;
				}
				Thread.sleep(5000);
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
	}
}
