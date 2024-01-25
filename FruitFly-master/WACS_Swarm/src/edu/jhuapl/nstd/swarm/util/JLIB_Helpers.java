package edu.jhuapl.nstd.swarm.util;

import java.util.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;

public class JLIB_Helpers {

	public static LatLonAltPosition[] getBoundingLatLon(Vector<LatLonAltPosition> points) {
		Latitude northLat=null, southLat=null;
		Longitude eastLon=null, westLon=null;

		for (LatLonAltPosition pos : points) {
			//System.out.println("pos -> "+pos);

			if (northLat==null || pos.getLatitude().compareTo(northLat)>0)
				northLat = pos.getLatitude();
			if (southLat==null || pos.getLatitude().compareTo(southLat)<0)
				southLat = pos.getLatitude();

			if (eastLon==null || pos.getLongitude().compareTo(eastLon)>0)
				eastLon = pos.getLongitude();
			if (westLon==null || pos.getLongitude().compareTo(westLon)<0)
				westLon = pos.getLongitude();
		}

		LatLonAltPosition[] ans = new LatLonAltPosition[2];
		ans[0] = new LatLonAltPosition(northLat, westLon, Altitude.ZERO);
		//System.out.println("NorthWest corner: "+ans[0]);
		ans[1] = new LatLonAltPosition(southLat, eastLon, Altitude.ZERO);
		//System.out.println("SouthEast corner: "+ans[1]);
		//System.out.println("BOUNDING BOX: (given "+points.size()+" points)");
		//System.out.println("ans[0] = "+ans[0]);
		//System.out.println("ans[1] = "+ans[1]);
		return ans;
	}

        /* toECEF
         * Function will return a 3 element array with the ECEF coordinates of given position
         * IMPORTANT: altitude passed in must be ellipsoid height
         */
/*        public static double[] toECEF(AbsolutePosition position) {
            LatLonAltPosition pos = position.asLatLonAltPosition();
            double slat = Math.sin(pos.getLatitude().getDoubleValue(Angle.RADIANS));
            double clat = Math.cos(pos.getLatitude().getDoubleValue(Angle.RADIANS));
            double slon = Math.sin(pos.getLongitude().getDoubleValue(Angle.RADIANS));
            double clon = Math.cos(pos.getLongitude().getDoubleValue(Angle.RADIANS));
            double e2 = WGS84_Eccentricity*WGS84_Eccentricity;
            double r_earth = WGS84_SemiMajorAxisRadius_m / Math.sqrt(1 - (e2*slat*slat));
            double abs_alt = r_earth + pos.getAltitude().getDoubleValue(Length.METERS);
            double[] ecef = new double[3];
            ecef[0] = abs_alt * clat * clon;
            ecef[1] = abs_alt * clat * slon;
            ecef[2] = (r_earth * (1-e2) + pos.getAltitude().getDoubleValue(Length.METERS)) * slat;
            return ecef;
        }

        public static double[] toLV(double[] ecef, AbsolutePosition origin) {
            LatLonAltPosition orig = origin.asLatLonAltPosition();
            double[] origECEF = JLIB_Helpers.toECEF(origin);
            double[] diff = new double[3];
            double[] lv = new double[3];

            for (int i=0; i<3; i++)
                diff[i] = ecef[i] - origECEF[i];

            double slat = Math.sin(orig.getLatitude().getDoubleValue(Angle.RADIANS));
            double clat = Math.cos(orig.getLatitude().getDoubleValue(Angle.RADIANS));
            double slon = Math.sin(orig.getLongitude().getDoubleValue(Angle.RADIANS));
            double clon = Math.cos(orig.getLongitude().getDoubleValue(Angle.RADIANS));

            lv[0] = (-slon*diff[0]) + (clon*diff[1]);
            lv[1] = (slat*-clon*diff[0]) + (slat*-slon*diff[1]) + (clat*diff[2]);
            lv[2] = (clat*clon*diff[0]) + (clat*slon*diff[1]) + (slat*diff[2]);
            return lv;
        }
*/   
        public static Angle NavyAngle2Angle(NavyAngle navy) {
            Angle ang = navy.asAngle();
            ang = ang.negate();
            ang = ang.plus(Angle.RIGHT_ANGLE);
            return ang;
        }
        
        public static double angle0to360(double angle_deg) {
            while (angle_deg < 0)
                angle_deg += 360.0;
            while (angle_deg >= 360.0)
                angle_deg -= 360.0;
            return angle_deg;
        }
        public static double angle180to180(double angle_deg) {
            while (angle_deg < -180.0)
                angle_deg += 360.0;
            while (angle_deg >= 180.0)
                angle_deg -= 360.0;
            return angle_deg;
        }
        public static double angle0to2PI(double angle_rad) {
            while (angle_rad < 0)
                angle_rad += 2*Math.PI;
            while (angle_rad >= 2*Math.PI)
                angle_rad -= 2*Math.PI;
            return angle_rad;
        }
        public static double anglePItoPI(double angle_rad) {
            while (angle_rad < -Math.PI)
                angle_rad += 2*Math.PI;
            while (angle_rad >= Math.PI)
                angle_rad -= 2*Math.PI;
            return angle_rad;
        }

	public static void main(String[] args) {

            /*
		// point location
		LatLonAltPosition c0 = new LatLonAltPosition(new Latitude(39.18254891672069, Angle.DEGREES),
													 new Longitude(-77.01278846995321, Angle.DEGREES),
													 Altitude.ZERO);
		// base location
		LatLonAltPosition c1 = new LatLonAltPosition(new Latitude(39.16101938957875, Angle.DEGREES),
													 new Longitude(-76.89999241746898, Angle.DEGREES),
													 Altitude.ZERO);
		Length r0 = new Length(8000.0, Length.METERS);
		Length r1 = new Length(8000.0, Length.METERS);

		Vector3 u = new Vector3(c0.getRangeBearingHeightOffsetTo(c1));
		u = new Vector3( u.getY().getDoubleValue(Length.METERS), 
						 u.getX().getDoubleValue(Length.METERS), 
						 u.getZ().getDoubleValue(Length.METERS),
						 Length.METERS );
		System.out.println("u = "+u);
		Vector3 v = new Vector3( u.getY().getDoubleValue(Length.METERS), 
								 u.getX().times(-1.0).getDoubleValue(Length.METERS), 
								 u.getZ().getDoubleValue(Length.METERS),
								 Length.METERS );
		System.out.println("v = "+v);

		double r0d = r0.getDoubleValue(Length.METERS);
		double r1d = r1.getDoubleValue(Length.METERS);
		double ulen = u.lengthDouble();
		System.out.println("ulen = "+ulen);
		double s = 0.5 * ((((r0d*r0d) - (r1d*r1d)) / (ulen*ulen)) + 1.0);
		double tSquared = ((r0d*r0d) / (ulen*ulen)) - (s*s);

		if (tSquared < 0) {
			System.out.println("Circles do not intersect.");
		} else if (tSquared == 0) {
			System.out.println("Circles intersect in exactly one place");
			LatLonAltPosition X = c0.translatedBy(u.scaled(s).asRangeBearingHeightOffset()).asLatLonAltPosition();
			System.out.println("X = "+X);
		} else {
			System.out.println("Circles intersect in two places");
			
			LatLonAltPosition X1 = c0.translatedBy(u.scaled(s).asRangeBearingHeightOffset()).asLatLonAltPosition();
			double t1 = Math.sqrt(tSquared);
			X1 = X1.translatedBy(v.scaled(t1).asRangeBearingHeightOffset()).asLatLonAltPosition();
			System.out.println("X1 = "+X1);
			
			LatLonAltPosition X2 = c0.translatedBy(u.scaled(s).asRangeBearingHeightOffset()).asLatLonAltPosition();
			double t2 = -t1;
			X2 = X2.translatedBy(v.scaled(t2).asRangeBearingHeightOffset()).asLatLonAltPosition();
			System.out.println("X2 = "+X2);




			
			RangeBearingHeightOffset betweenCenters = c0.getRangeBearingHeightOffsetTo(c1);
			System.out.println("between centers bearing = "+betweenCenters.getBearing());
			LatLonAltPosition middle = c0.translatedBy( new RangeBearingHeightOffset(betweenCenters.getRange().dividedBy(2.0),
																					 betweenCenters.getBearing(),
																					 Length.ZERO) ).asLatLonAltPosition();
			System.out.println("middle position = "+middle);
			Length middleTowardC1 = r0.minus(c0.getRangeTo(middle));
			LatLonAltPosition c0edge = middle.translatedBy(new RangeBearingHeightOffset(middleTowardC1,
																						betweenCenters.getBearing(),
																						Length.ZERO) ).asLatLonAltPosition();
			System.out.println("c0edge = "+c0edge);
			Length middleTowardC0 = r1.minus(c1.getRangeTo(middle));
			LatLonAltPosition c1edge = middle.translatedBy(new RangeBearingHeightOffset(middleTowardC0,
																						betweenCenters.getBearing().plus(Angle.HALF_CIRCLE),
																						Length.ZERO) ).asLatLonAltPosition();
			System.out.println("c1edge = "+c1edge);
		}
                

            LatLonAltPosition pos0 = new LatLonAltPosition(
                    new Latitude(39.164011111111108, Angle.DEGREES),
                    new Longitude(-76.896741666666671, Angle.DEGREES),
                    new Altitude(10.0, Length.METERS));
            LatLonAltPosition pos1 = new LatLonAltPosition(
                    new Latitude(39.1646480345394, Angle.DEGREES),
                    new Longitude(-76.8959234954267, Angle.DEGREES),
                    new Altitude(10.0, Length.METERS));

            double[] ecef1 = JLIB_Helpers.toECEF(pos1);
            double[] lv1 = JLIB_Helpers.toLV(ecef1, pos0);

            System.out.println("ECEF 1: "+ecef1[0]+","+ecef1[1]+","+ecef1[2]);
            System.out.println("LV 1: "+lv1[0]+","+lv1[1]+","+lv1[2]);
*/
            // answers should be:
            //   ECEF 1: 1122677.6959063124,-4822865.54432918,4006512.039178218
            //   LV 1: 70.71078883162672,70.71078927808281,-7.844685235340876E-4

	}


}
