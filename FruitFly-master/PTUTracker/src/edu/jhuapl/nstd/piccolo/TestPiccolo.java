package edu.jhuapl.nstd.piccolo;

import java.lang.*;

public class TestPiccolo implements Pic_TelemetryListener {


	public void handlePic_Telemetry(Pic_Telemetry telem) {
		System.out.println("Pic Telemetry => ");
		System.out.println("\t(lat,lon,alt) = ("+telem.Lat+","+telem.Lon+","+telem.AltEllip+")");
		System.out.println("\t(r,p,y,comp) = ("+telem.Roll+","+telem.Pitch+","+telem.Yaw+","+telem.TrueHeading+")");
		System.out.println("\t(WindSouth,WindWest,IAS) = ("+telem.WindSouth+","+telem.WindWest+","+telem.IndAirSpeed+")");
		System.out.println("\t(vnorth,vest,vdown) = ("+telem.VelNorth+","+telem.VelEast+","+telem.VelDown+")");
		System.out.println("\t(PDOP,Status) = ("+telem.PDOP+","+telem.GPS_Status+")\n\n");
	}

	public static void main(String[] args) {

		TestPiccolo tp = new TestPiccolo();
		Pic_Interface pi = new Pic_Interface(280, "176.16.2.105", 2000, true);
		pi.addPicTelemetryListener(tp);

		(new Thread(pi)).start();

		try {
			Thread.sleep(5000);
			System.out.println("Changing waypoint of intercept");
			pi.changeInterceptWaypoint(0.1, 0.1, 98.7, 500, false);
			System.out.println("Changing waypoint of loiter");
			pi.changeLoiterWaypoint(0.2, 0.2, -13.2, 1000, true);
			
			Thread.sleep(5000);
			System.out.println("Tracking intercept");
			pi.sendToInterceptWaypoint();
			
			Thread.sleep(5000);
			System.out.println("Tracking loiter");
			pi.sendToLoiterWaypoint();

		} catch ( Exception ex ) {
		}
	}
}
