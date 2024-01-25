/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.util;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.position.Region;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author
 * biggimh1
 */
//TODO comparision between polygons should use .equals method not == or !=
public class PolygonRegion extends Region{

    protected int _length;
    protected LatLonAltPosition[] _vertices; 
    protected RectangleRegion _boundingRectangle;

    public PolygonRegion(LatLonAltPosition vertices[]) throws Exception {
        super(vertices);
        if (vertices.length < 3) {
            throw new RuntimeException("PolygonRegion requires 3 or more vertices.");
        }
        _vertices = new LatLonAltPosition[vertices.length];
        _length = vertices.length;
        System.arraycopy(vertices, 0, _vertices, 0, _length);

        //init();
    }

    public PolygonRegion(PolygonRegion other){
        super(other._vertices);
        _vertices = new LatLonAltPosition[other._length];
        _length = other._length;
        
        System.arraycopy(other._vertices, 0, _vertices, 0, other._length);
        //init();
    }

    @Override
    public RectangleRegion getMinimumBoundingRectangle() {
        if (_boundingRectangle == null) {
            LatLonAltPosition[] rect = new LatLonAltPosition[4];

            rect[0] = new LatLonAltPosition(minimumLatitude, westernmostLongitude, new Altitude(0, Length.METERS));
            rect[1] = new LatLonAltPosition(minimumLatitude, easternmostLongitude, new Altitude(0, Length.METERS));
            rect[2] = new LatLonAltPosition(maximumLatitude, easternmostLongitude, new Altitude(0, Length.METERS));
            rect[3] = new LatLonAltPosition(maximumLatitude, westernmostLongitude, new Altitude(0, Length.METERS));

            try {
                _boundingRectangle = new RectangleRegion(rect);
            } catch (Exception e) {
                //Cant get here
            }
        }
        return _boundingRectangle;
    }

    public boolean contains(LatLonAltPosition position) {
        // This code is a direct translation of the Region.java code.
        if (getMinimumBoundingRectangle().contains(position)) {
            boolean inside = false;
            int count = _length;

            Latitude lat = position.getLatitude();
            Longitude lon = position.getLongitude();

            for (int i = 0, j = count - 1; i < count; j = i++) {
                LatLonAltPosition tmpI = _vertices[i];

                // Not in original "inside" algorithm, but we want both inside
                // *and* on the line! 
                if (position == tmpI) {
                    return true;
                }

                LatLonAltPosition tmpJ = _vertices[j];


                Latitude latI = tmpI.getLatitude();
                Latitude latJ = tmpJ.getLatitude();
                Longitude lonI = tmpI.getLongitude();
                Longitude lonJ = tmpJ.getLongitude();

                if (latI == latJ && lat == latJ
                        && ((lon.isWestOfOrOn(lonI) && lon.isEastOfOrOn(lonJ))
                        || (lon.isEastOfOrOn(lonI) && lon.isWestOfOrOn(lonJ)))) {
                    return true; //position is on a horizontal line, and lines are considered inclusive
                    //this accounts for a the boundry condition created by the <= < pattern below
                }

                if ((latI.isSouthOfOrOn(lat) && lat.isSouthOf(latJ)) || //only counts one line at vertices
                        (latJ.isSouthOfOrOn(lat) && lat.isSouthOf(latI))) {
                    double breadthOfLine = lonJ.angleBetween(lonI).getDoubleValue(Angle.DEGREES);
                    double percentOffEndpoint = (lat.getDoubleValue(Angle.DEGREES) - latI.getDoubleValue(Angle.DEGREES)) / (latJ.getDoubleValue(Angle.DEGREES) - latI.getDoubleValue(Angle.DEGREES));
                    Longitude lonAtPositionLat = lonI.plus(new Angle(breadthOfLine * percentOffEndpoint, Angle.DEGREES));
                    if (lon == lonAtPositionLat) {
                        return true; //position is on this line, and therefore this region contains position
                        //accounts for problem of points not counting the line they are on, which fails
                        //when points are a western edge
                    }
                    if (lon.isWestOf(lonAtPositionLat)) {
                        inside = !inside;
                    }
                }
            }
            return inside;
        } else {
            // If it isnt in the minimum bounding rectangle, it 
            // certainly is not in the polygon...
            return false;
        }
    }

    public boolean intersects(PolygonRegion other) {
        return other.intersectsWith(this);
    }

    public boolean intersectsWith(PolygonRegion other) {
        // directly from jlib's Region.java
        double x1 = 0;
        double y1 = 0;
        double x2 = 0;
        double y2 = 0;
        double x3 = 0;
        double y3 = 0;
        double x4 = 0;
        double y4 = 0;

        LatLonAltPosition tmpPos;
        Longitude westEnd;

        if (westernmostLongitude.isWestOf(other.westernmostLongitude)) {
            westEnd = westernmostLongitude;
        } else {
            westEnd = other.westernmostLongitude;
        }


        // Grab the last point as the starting point for this region.  Since the 
        // iteration starts at index 0, the first line will represent the 
        // "closing segment" that connects the first and last point in the list...
        tmpPos = _vertices[_length - 1];
        y1 = tmpPos.getLatitude().getDoubleValue(Angle.DEGREES);
        x1 = tmpPos.getLongitude().angleBetween(westEnd).getDoubleValue(Angle.DEGREES);
        // for every position in this region...
        for (int i = 0; i < _length; i++) {
            // Grab the next point in the list for this region...
            tmpPos = _vertices[i];
            y2 = tmpPos.getLatitude().getDoubleValue(Angle.DEGREES);
            x2 = tmpPos.getLongitude().angleBetween(westEnd).getDoubleValue(Angle.DEGREES);

            // Grab the last point as the starting point for other.  Since the
            // iteration starts at index 0, the first line will represent the
            // "closing segment" that connects the first and last point in the list...
            tmpPos = other._vertices[other._length - 1];
            y3 = tmpPos.getLatitude().getDoubleValue(Angle.DEGREES);
            x3 = tmpPos.getLongitude().angleBetween(westEnd).getDoubleValue(Angle.DEGREES);
            // for every position in other...
            for (int j = 0; j < other._length; j++) {
                // Grab the next point in the list for other...
                tmpPos = other._vertices[j];
                y4 = tmpPos.getLatitude().getDoubleValue(Angle.DEGREES);
                x4 = tmpPos.getLongitude().angleBetween(westEnd).getDoubleValue(Angle.DEGREES);

                // if the lines interest, the regions must intersect, we are done!
                if (linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4)) {
                    return true;
                } // end if lines intersect

                // set "last point" to "current point" for other
                x3 = x4;
                y3 = y4;
            } // end for j 

            // set "last point" to "current point" for this region
            x1 = x2;
            y1 = y2;
        } // end for i

        // If we got this far, no lines intersect.  Therefore, the only way
        // these regions can intersect is if one wholly contains the other...

        int i = _length - 1;
        // for every position in this region...
        for (; i >= 0; i--) {
            // if the other region does not contain this point,
            // then we dont have to check any more for this point.
            // Break the loop... (we cant simply exit here because
            // we still need to see if this contains other).
            if (!other.contains(_vertices[i])) {
                break;
            }
        } // end for i

        // If i is less than zero, we never got to "break" and therefore
        // other must completely contain this region.
        if (i < 0) {
            return true;
        }

        i = other._length - 1;
        // for every position in other...
        for (; i >= 0; i--) {
            // if this region does not contain this point,
            // then we dont have to check any more for this point.
            // We can return immediately, since there are no more
            // conditions to check...
            if (!contains(other._vertices[i])) {
                return false;
            }
        } // end for i

        // If we get here, than all of the points in other
        // are contained in this region- so they intersect!
        return true;
    }

    public boolean linesIntersect(double x1, double y1,
            double x2, double y2,
            double x3, double y3,
            double x4, double y4) {
        // from http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/
        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3))
                / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
        double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3))
                / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));

        return (ua > 0 && ua < 1 && ub > 0 && ub < 1);

    }

    public LatLonAltPosition[] getPoints() {
        return this._vertices;
    }

    public class CHSort implements Comparator<LatLonAltPosition> {

        LatLonAltPosition _origin;

        public CHSort(LatLonAltPosition o) {
            _origin = o;
        }

        @Override
        public int compare(LatLonAltPosition ll1, LatLonAltPosition ll2) {

            NavyAngle polar1 = _origin.getBearingTo(ll1);
            NavyAngle polar2 = _origin.getBearingTo(ll2);

            if (polar1.isLessThan(polar2)) {
                return -1;
            }
            return 1;

        }

        public int ccw(LatLonAltPosition ll1, LatLonAltPosition ll2, LatLonAltPosition ll3) {
            //double angle = ll2.getLocalVectorTo(ll1).clockwiseAngleXYBetween(ll2.getLocalVectorTo(ll3));
            double angle = ll2.getBearingTo(ll1).clockwiseAngleTo(ll2.getBearingTo(ll3)).getDoubleValue(Angle.RADIANS);
            if (angle == 0.0) {
                return 0;
            }
            if (angle > Math.PI) {
                return 1;
            }
            return -1;
        }
    }

    public PolygonRegion convexHull(LatLonAltPosition[] points) throws Exception {
        if (points.length < 3) {
            throw new RuntimeException("[PolygonRegion] points must be > 3.");
        }

        LatLonAltPosition southernmost = points[0];
        for (int i = 1; i < (int) points.length; ++i) {
            if (points[i].getLatitude().isSouthOf(southernmost.getLatitude())) {
                southernmost = points[i];
            }
        }

        CHSort comparator = new CHSort(southernmost);
        Arrays.sort(points, comparator);

        assert (points[0] == southernmost);

        ArrayList<LatLonAltPosition> hull = new ArrayList<LatLonAltPosition>();
        hull.add(southernmost);
        hull.add(points[1]);
        hull.add(points[2]);

        //cout << "HULL: " << hull[0].toString() << " " << hull[1].toString() << " " << hull[2].toString() << " " << endl;

        for (int i = 3; i < points.length; ++i) {
            while (comparator.ccw(hull.get(hull.size() - 2), hull.get(hull.size() - 1), points[i]) <= 0) {
                hull.remove(hull.size());
            }
            hull.add(points[i]);
        }

        //cout << "HULL2: " << hull[0].toString() << " " << hull[1].toString() << " " << hull[2].toString() << " " << endl;

        LatLonAltPosition[] p = new LatLonAltPosition[hull.size()];
        p = hull.toArray(p);

        return new PolygonRegion(p);
    }

    public double distanceTo(LatLonAltPosition loc) {
        double shortLen = 99999999;

        for (int i = 0; i < _length; ++i) {
            int indxp1 = (i + 1) % _length;
            double dist = distanceToSegment(loc, indxp1, i);
            if (dist < shortLen) {
                shortLen = dist;
                //std::cout << "*";
            }
            //std::cout << "[" << i << "," << indxp1 << "]" << dist << endl;
        }

        return shortLen;
    }

    public double distanceToSegment(LatLonAltPosition c, int a, int b) {
        //keep the same connotation for distance from point c to
        //line segment ab

        Double[] ab = new Double[2];
        double ab_;
        Double[] ac = new Double[2];
        double ax, ay;
        double bx, by;
        //c = 0,0;

        double bearingA = c.getBearingTo(_vertices[a]).getDoubleValue(Angle.RADIANS);
        double rangeA = c.getRangeTo(_vertices[a]).getDoubleValue(Length.METERS);
        double bearingB = c.getBearingTo(_vertices[b]).getDoubleValue(Angle.RADIANS);
        double rangeB = c.getRangeTo(_vertices[b]).getDoubleValue(Length.METERS);

        //std::cout << "  BR - A: " << bearingA << ", " << rangeA << endl;
        //std::cout << "  BR - B: " << bearingB << ", " << rangeB << endl;
        ax = Math.cos(bearingA) * rangeA;
        ay = Math.sin(bearingA) * rangeA;
        bx = Math.cos(bearingB) * rangeB;
        by = Math.sin(bearingB) * rangeB;
        //std::cout << "  A: " << ax << "," << ay << endl;
        //std::cout << "  B: " << bx << "," << by << endl;

        ab[0] = bx - ax;
        ab[1] = by - ay;
        ab_ = Math.sqrt(Math.pow(ab[0], 2) + Math.pow(ab[1], 2));

        ac[0] = -ax;
        ac[1] = -ay;

        double acDotab = (ac[0] * ab[0] + ac[1] * ab[1]) / ab_;

        if (acDotab <= 0) {
            //outside of a on ab
            //std::cout << "  outside of A, returning A" << endl;
            return rangeA;
        } else if (acDotab >= ab_) {
            //outside of b on ab
            //std::cout << "  outside of B, returning B" << endl;
            return rangeB;
        } else {
            //std::cout << "  in the  middle, returning something..." << endl;
            double px = ax + ab[0] * acDotab / ab_;
            double py = ay + ab[1] * acDotab / ab_;
            return Math.sqrt(Math.pow(px, 2) + Math.pow(py, 2));
        }
    }

    public boolean equals(PolygonRegion right) {
        if (_length != right._length) {
            return false;
        }
        for (int i = 0; i < _length; i++) {
            if (_vertices[i] != right._vertices[i]) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        String str = "length: " + _length;
        for (int i = 0; i < _length; i++) {
            str += " " + _vertices[i].toString();
        }

        return str;
    }

    public LatLonAltPosition getCentroid() {

        double latSum = 0;
        double lonSum = 0;
        for (int i = 0; i < _length; i++) {
            latSum += _vertices[i].getLatitude().getDoubleValue(Angle.DEGREES);
            lonSum += _vertices[i].getLongitude().getDoubleValue(Angle.DEGREES);
        }

        LatLonAltPosition centroid = new LatLonAltPosition(new Latitude(latSum / _length, Angle.DEGREES), new Longitude(lonSum / _length, Angle.DEGREES), new Altitude(0, Length.METERS));

        return centroid;
    }

    public int getLength() {
        return this._length;
    }

    public byte[] serialize() {
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(new byte[4 +24*_length]);
        buffer.putInt(_length);
        
        for(LatLonAltPosition p: this._vertices){
            writeLatLonAlt(buffer, p);
        }

        return buffer.array();
    }

    /*
     * Size is size if int + length * size of latLonAltPosition
     */
    public int serializeSize() {
        return 4 +24*_length;
    }

    
    private void writeLatLonAlt(java.nio.ByteBuffer buffer, LatLonAltPosition p) {
        buffer.putDouble(p.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES));
        buffer.putDouble(p.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES));
        buffer.putDouble(p.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS));
    }
}
