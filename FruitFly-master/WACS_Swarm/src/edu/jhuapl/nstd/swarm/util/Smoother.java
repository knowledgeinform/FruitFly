package edu.jhuapl.nstd.swarm.util;

import java.util.logging.*;




import java.util.*;

import java.io.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;

public class Smoother {
	BufferedReader reader;
	StringTokenizer tok;
	PrintWriter writer;
	FileOutputStream out;

	public Smoother() {
		try {
			out = new FileOutputStream(new File("ice.playback.metrics.smoothed"),false);
			writer = new PrintWriter(out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void smooth(String agentID,String filename) {
		try {
			double lat = -1;
			double lon = -1;
			String str = "";
			String prev = "";
			StringTokenizer tok;
			boolean smoothed = false;
			int sameCount = 1;
			reader = new BufferedReader(new FileReader(filename));
			while (((str = reader.readLine()) != null)) {
				Logger.getLogger("GLOBAL").info(str);
				if (!str.equals(prev) && !smoothed) {
					writer.write(str + "\n");
					writer.flush();
					prev = str;
					continue;
				}
				else if (str.equals(prev)) {
					sameCount++;
					prev = str;
					smoothed = true;
					continue;
				}
				else if (!str.equals(prev) && smoothed) {
					tok = new StringTokenizer(str);
					String line = str;
					tok.nextToken();
					lat = Double.valueOf(tok.nextToken());
					tok.nextToken();
					lon = Double.valueOf(tok.nextToken());
					tok = new StringTokenizer(prev);
					double prevLat,prevLon;
					prevLat = prevLon = -1;
					tok.nextToken();
					prevLat = Double.valueOf(tok.nextToken());
					tok.nextToken();
					prevLon = Double.valueOf(tok.nextToken());
					LatLonAltPosition prevPos = new LatLonAltPosition(
							new Latitude(prevLat,Angle.DEGREES),
							new Longitude(prevLon,Angle.DEGREES),
							Altitude.ZERO);
					LatLonAltPosition newPos = new LatLonAltPosition(
							new Latitude(lat,Angle.DEGREES),
							new Longitude(lon,Angle.DEGREES),
							Altitude.ZERO);
					NavyAngle angle = prevPos.getBearingTo(newPos);
					Length distance = prevPos.getRangeTo(newPos);
					distance = distance.dividedBy(new Unitless((double)sameCount));
					RangeBearingHeightOffset rbho = new RangeBearingHeightOffset(distance,angle,Length.ZERO);
					int count = 1;
					do {
						writer.write(agentID + " " + prevPos.getLatitude().getDoubleValue(Angle.DEGREES) + " deg(latitude) " + 
								prevPos.getLongitude().getDoubleValue(Angle.DEGREES) + "\n");
						writer.flush();
						prevPos = prevPos.translatedBy(rbho).asLatLonAltPosition();
						count++;
					} while (count < sameCount);
					sameCount = 1;
					smoothed = false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Smoother s = new Smoother();
		s.smooth("iceAgent", "ice.playback.metrics");
	}
}

