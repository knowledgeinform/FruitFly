package edu.jhuapl.nstd.piccolo;

import edu.jhuapl.nstd.swarm.util.Config;
import java.lang.*;

public class TestPiccolo implements Pic_TelemetryListener {


	public void handlePic_Telemetry(Long currentTime, Pic_Telemetry telem) {
		System.out.println("Pic Telemetry => ");
		System.out.println("\t(lat,lon,alt) = ("+telem.Lat+","+telem.Lon+","+telem.AltWGS84+")");
		System.out.println("\t(r,p,y,comp) = ("+telem.Roll+","+telem.Pitch+","+telem.Yaw+","+telem.TrueHeading+")");
		System.out.println("\t(WindSouth,WindWest,IAS) = ("+telem.WindSouth+","+telem.WindWest+","+telem.IndAirSpeed_mps+")");
		System.out.println("\t(vnorth,vest,vdown) = ("+telem.VelNorth+","+telem.VelEast+","+telem.VelDown+")");
		System.out.println("\t(PDOP,Status) = ("+telem.PDOP+","+telem.GPS_Status+")\n\n");
	}

	public static void main(String[] args) {


		TestPiccolo tp = new TestPiccolo();
                int planeNum = Config.getConfig().getPropertyAsInteger("PiccoloAutopilotInterface.planenum", 1);
		Pic_Interface pi = new Pic_Interface(planeNum, "COM5", 2000, true);
                //Pic_Interface pi = new Pic_Interface(planeNum, ".\\PiccoloLogs\\PiccoloLog_1288029087500_00000.log", 57600, false, true);
                //Pic_Interface pi = new Pic_Interface(planeNum, "COM1", 115200, false);
		pi.addPicTelemetryListener(tp);

                int crc = 0;
                crc = pi.CRC16OneByte((byte)128, crc);
                crc = pi.CRC16OneByte((byte)127, crc);

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
