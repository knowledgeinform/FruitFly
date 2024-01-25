package edu.jhuapl.nstd.util;

import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import java.io.*;

public class GeoUtil {

    public static final double PI = Math.PI;

    /**
     * @return the geoUtil
     */
    public static GeoUtil getGeoUtil() {
        return geoUtil;
    }

    /**
     * @param aGeoUtil the geoUtil to set
     */
    public static void setGeoUtil(GeoUtil aGeoUtil) {
        geoUtil = aGeoUtil;
    }
    private float[][] _ellipsoidMap;

    private static GeoUtil geoUtil = new GeoUtil();

    public GeoUtil() {
        readInEllipsoidMap();
    }

    private void readInEllipsoidMap() {
        String filename = "geoidHeight15min.img";
        int i, j, val;
        byte[] b = new byte[4];

        try {
            _ellipsoidMap = new float[1441][721];

            InputStream is = this.getClass().getClassLoader().getResourceAsStream(filename);
            if (is == null) {
                System.out.println("Error: unable to open ellipsoid file: " + filename);
                return;
            }
            DataInputStream dis = new DataInputStream(is);

            for (j = 720; j >= 0; j--) {
                for (i = 0; i < 1441; i++) {
                    try {
                        dis.readFully(b);
                        val = (((b[3] & 0xFF) << 24) + ((b[2] & 0xFF) << 16) + ((b[1] & 0xFF) << 8) + ((b[0] & 0xFF) << 0));
                        _ellipsoidMap[i][j] = Float.intBitsToFloat(val);
                    } catch (EOFException eof) {
                        System.out.println("ERROR: End of File; not enough data in ellipsoid map");
                        break;
                    }
                }
            }

            /*for (i=0; i<8; i++) {
            for (j=0; j<8; j++)
            printf("[%d,%d]=%f ", i, j, ellipsoidMap[i][j]);
            printf("\n");
            } */
            dis.close();
        } catch (IOException ioe) {
            System.out.println("ERROR: error reading ellipsoid map");
            ioe.printStackTrace();
        }
    }

    public double getGeoidHeight(double lat, double lon) {
        double swLat, swLon, topLat, leftLon, btmLat, rightLon;
        int col1, col2, row1, row2;
        float upLeft, upRight, btmLeft, btmRight;
        double left, right;
        double geoidHeight;
        double perRightOfLeft, perDownOfTop;

        if (lon < 0) {
            lon += 360.0;
        }
        if (lat < -90.0 || lat > 90.0 || lon < 0 || lon > 360) {
            System.out.println("ERROR: lat/lon (" + lat + "," + lon + ") out of bounds");
            return 0.0;
        }

        swLat = ((int) (lat * 4.0) / 4.0);	/* range -90 to 90 */
        swLon = ((int) (lon * 4.0) / 4.0);	/* range 0 to 360 */

        col1 = (int) Math.round((double) (swLon * 4.0));
        col2 = col1 + 1;
        row1 = (int) Math.round((double) ((swLat + 90.0) * 4.0));
        row2 = row1 + 1;
        if (row2 > 720) {
            row1--;
            row2--;
        }
        if (col2 > 1440) {
            col1--;
            col2--;
        }

        topLat = ((row1 / 4.0) - 90.0);
        btmLat = ((row2 / 4.0) - 90.0);
        leftLon = (col1 / 4.0);
        rightLon = (col2 / 4.0);

        perRightOfLeft = (lon - leftLon) / (rightLon - leftLon);
        perDownOfTop = (lat - topLat) / (btmLat - topLat);

        upLeft = _ellipsoidMap[col1][row1];
        upRight = _ellipsoidMap[col2][row1];
        btmLeft = _ellipsoidMap[col1][row2];
        btmRight = _ellipsoidMap[col2][row2];


        left = (upLeft * (1.0 - perDownOfTop)) + (btmLeft * perDownOfTop);
        right = (upRight * (1.0 - perDownOfTop)) + (btmRight * perDownOfTop);
        geoidHeight = (left * (1.0 - perRightOfLeft)) + (right * perRightOfLeft);

        System.out.println("Geoid Height(" + lat + "," + lon + ") found as " + geoidHeight);

        return geoidHeight;
    }

    public double convertSeaLevel2Ellipsoid(double lat, double lon, double altSeaLevel) {
        double swLat, swLon, topLat, leftLon, btmLat, rightLon;
        int col1, col2, row1, row2;
        double upLeft, upRight, btmLeft, btmRight;
        double left, right;
        double geoidHeight;
        double perRightOfLeft, perDownOfTop;

        if (lon < 0) {
            lon += 360.0;
        }
        if (lat < -90.0 || lat > 90.0 || lon < 0 || lon > 360) {
            System.err.println(String.format("ERROR: lat/lon (%f,%f) out of bounds\n", lat, lon));
            return 0.0;
        }

        swLat = ((int) (lat * 4.0) / 4.0);	/* range -90 to 90 */
        swLon = ((int) (lon * 4.0) / 4.0);	/* range 0 to 360 */

        col1 = (int) (swLon * 4.0);
        col2 = col1 + 1;
        row1 = (int) ((swLat + 90.0) * 4.0);
        row2 = row1 + 1;
        if (row2 > 720) {
            row1--;
            row2--;
        }
        if (col2 > 1440) {
            col1--;
            col2--;
        }

        topLat = ((row1 / 4.0) - 90.0);
        btmLat = ((row2 / 4.0) - 90.0);
        leftLon = (col1 / 4.0);
        rightLon = (col2 / 4.0);

        perRightOfLeft = (lon - leftLon) / (rightLon - leftLon);
        perDownOfTop = (lat - topLat) / (btmLat - topLat);

        upLeft = _ellipsoidMap[col1][row1];
        upRight = _ellipsoidMap[col2][row1];
        btmLeft = _ellipsoidMap[col1][row2];
        btmRight = _ellipsoidMap[col2][row2];


        left = (upLeft * (1.0 - perDownOfTop)) + (btmLeft * perDownOfTop);
        right = (upRight * (1.0 - perDownOfTop)) + (btmRight * perDownOfTop);
        geoidHeight = (left * (1.0 - perRightOfLeft)) + (right * perRightOfLeft);

        /*
        printf("Lat/Lon entered: (%f, %f)\n", lat, lon);
        printf("Lat Grid: (%f, %f) (rows=%d,%d)\n", topLat, btmLat, row1, row2);
        printf("Lon Grid: (%f, %f) (cols=%d,%d)\n", leftLon, rightLon, col1, col2);
        printf("Percent lat: %f, Percent Lon: %f\n", perDownOfTop, perRightOfLeft );
        printf("Top Heights: (%f, %f)\n", upLeft, upRight);
        printf("Btm Heights: (%f, %f)\n", btmLeft, btmRight);
         */
        System.err.println(String.format("Geoid Height(%f,%f) found as %f\n", lat, lon, geoidHeight));

        return (altSeaLevel + geoidHeight);
    }

    public static double[] g2e(double lat, double lon, double altEllipsoid) {
        double a, e, deg2rad, e2;
        double rlat, rlon, slat, clat, slon, clon;
        double r_earth, abs_alt;
        double pos_x;
        double pos_y;
        double pos_z;

        a = 6378137.0;			/* WGS-84 semimajor axis (meters) */
        e = 0.0818191908426;		/* WGS-84 eccentricity  */
        deg2rad = PI / 180.0;
        e2 = e * e;
        rlat = lat * deg2rad;
        rlon = lon * deg2rad;
        slat = Math.sin(rlat);
        clat = Math.cos(rlat);
        slon = Math.sin(rlon);
        clon = Math.cos(rlon);
        r_earth = a / Math.sqrt(1 - (e2 * slat * slat));
        /*printf("r_earth = %f\n", r_earth); */
        abs_alt = r_earth + altEllipsoid;
        /*printf("abs_alt = %f\n", abs_alt); */

        pos_x = abs_alt * clat * clon;
        /*printf("pos_x = %f\n", *pos_x); */
        pos_y = abs_alt * clat * slon;
        /*printf("pos_y = %f\n", *pos_y); */
        pos_z = (r_earth * (1 - e2) + altEllipsoid) * slat;
        /*printf("pos_z = %f\n", *pos_z); */

        return new double[]{pos_x, pos_y, pos_z};
    }

    public static int staticcomputeRangeToUAV(double[] vec_ecef) {
        double range;

        range = Math.sqrt((vec_ecef[0]) * (vec_ecef[0]) +
                (vec_ecef[1]) * (vec_ecef[1]) +
                (vec_ecef[2]) * (vec_ecef[2]));
        return (int) range;
    }

    public static double[] convertECEF_2_NED(double lat, double lon, double[] vec_ecef) {
        double latRad, lonRad;
        double[] c = new double[9];
        double[] vec_ned = new double[3];

        latRad = lat * (PI / 180.0);
        lonRad = lon * (PI / 180.0);
        c[0] = -Math.sin(latRad) * Math.cos(lonRad);
        c[1] = -Math.sin(latRad) * Math.sin(lonRad);
        c[2] = Math.cos(latRad);

        c[3] = -Math.sin(lonRad);
        c[4] = Math.cos(lonRad);
        c[5] = 0.0;

        c[6] = -Math.cos(latRad) * Math.cos(lonRad);
        c[7] = -Math.cos(latRad) * Math.sin(lonRad);
        c[8] = -Math.sin(latRad);

        vec_ned[0] = (vec_ecef[0] * c[0]) + (vec_ecef[1] * c[1]) + (vec_ecef[2] * c[2]);
        vec_ned[1] = (vec_ecef[0] * c[3]) + (vec_ecef[1] * c[4]) + (vec_ecef[2] * c[5]);
        vec_ned[2] = (vec_ecef[0] * c[6]) + (vec_ecef[1] * c[7]) + (vec_ecef[2] * c[8]);
        return vec_ned;
    }

    public static void main(String[] args) {

        GeoUtil e2m = new GeoUtil();
        double val = e2m.getGeoidHeight(32.765, -76.345);

    }

     public static int[] calculatePointingAngles(LatLonAltPosition sourcePosition, LatLonAltPosition destinationPosition) {
        double panDeg, tiltDeg;
        int panDegIntLocal,tiltDegIntLocal;
        double distNED;
        // convert vector to NED coordinates, compute pan/tilt
        double[] vec_ecef = new double[3];
        double[] gcs_ecef = GeoUtil.g2e(sourcePosition.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES),
                sourcePosition.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES),
                sourcePosition.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS));

        double[] uav_ecef = GeoUtil.g2e(destinationPosition.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES),
                destinationPosition.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES),
                destinationPosition.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS));

        // and compute the vector from gcs to uav (uav-gcs)
        for (int i = 0; i < 3; i++) {
            vec_ecef[i] = uav_ecef[i] - gcs_ecef[i];
        }


        double[] vec_ned = GeoUtil.convertECEF_2_NED(sourcePosition.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES),
                sourcePosition.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES),
                vec_ecef);
        distNED = Math.sqrt((vec_ned[0] * vec_ned[0]) + (vec_ned[1] * vec_ned[1]) + (vec_ned[2] * vec_ned[2]));
        panDeg = Math.atan2(vec_ned[1], vec_ned[0]) * (180.0 / Math.PI);
        if (vec_ned[2] == 0.0) {
            tiltDeg = 0.0;
        } else {
            tiltDeg = Math.asin(-vec_ned[2] / distNED) * (180.0 / Math.PI);
        }
        tiltDeg = -(90.0 - tiltDeg); // Only Negative Tilt for Quickset
        panDegIntLocal = (int) (panDeg * 10.0);
        tiltDegIntLocal = (int) (tiltDeg * 10.0);

        

        return new int[]{panDegIntLocal,tiltDegIntLocal};
    }
}
