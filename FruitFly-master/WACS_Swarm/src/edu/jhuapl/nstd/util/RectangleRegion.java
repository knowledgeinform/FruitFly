/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.util;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;

/**
 *
 * @author
 * biggimh1
 */
public class RectangleRegion extends PolygonRegion {

    public RectangleRegion(LatLonAltPosition[] rect) throws Exception {
        super(rect);
        
    }

    @Override
    public RectangleRegion getMinimumBoundingRectangle() {
        return this;
    }
    
    public boolean intersectsWith(RectangleRegion other) {
        return minimumLatitude.isSouthOf(other.maximumLatitude) && (maximumLatitude.isNorthOf(other.minimumLatitude)
                && easternmostLongitude.isEastOf(other.westernmostLongitude) && westernmostLongitude.isWestOf(other.easternmostLongitude));
    }

    public RectangleRegion getIntersection(RectangleRegion other) throws Exception {
        if (!intersectsWith(other)) {
            return this;
        }

        Latitude s = minimumLatitude;
        if (other.minimumLatitude.isNorthOf(minimumLatitude)) {
            s = other.minimumLatitude;
        }
        Latitude n = maximumLatitude;
        if (other.maximumLatitude.isSouthOf(maximumLatitude)) {
            n = other.maximumLatitude;
        }

        Longitude e = easternmostLongitude;
        if (other.easternmostLongitude.isWestOf(easternmostLongitude)) {
            e = other.easternmostLongitude;
        }
        Longitude w = westernmostLongitude;
        if (other.westernmostLongitude.isEastOf(westernmostLongitude)) {
            w = other.westernmostLongitude;
        }

       
        LatLonAltPosition swCorner = new LatLonAltPosition(s, w, Altitude.ZERO);
        LatLonAltPosition neCorner = new LatLonAltPosition(n, e, Altitude.ZERO);
        
        if (swCorner.getLatitude().isNorthOf(neCorner.getLatitude())) {
            LatLonAltPosition temp = swCorner;
            swCorner = neCorner;
            neCorner = temp;
        }
        if (swCorner.getLongitude().isEastOf(neCorner.getLongitude())) {
            LatLonAltPosition temp = swCorner;
            swCorner = neCorner;
            neCorner = temp;
        }

        LatLonAltPosition[] verts = new LatLonAltPosition[4];
        _length = 4;

        verts[0] = swCorner;
        verts[1] = new LatLonAltPosition(neCorner.getLatitude(), swCorner.getLongitude(), Altitude.ZERO);
        verts[2] = neCorner;
        verts[3] = new LatLonAltPosition(swCorner.getLatitude(), neCorner.getLongitude(), Altitude.ZERO);

       return new RectangleRegion(verts);
    }

    @Override
    public boolean contains(LatLonAltPosition position) {
        Latitude latitude = position.getLatitude();
        if (latitude.isSouthOf(minimumLatitude)) {
            return false;
        }
        if (latitude.isNorthOf(maximumLatitude)) {
            return false;
        }
        Longitude longitude = position.getLongitude();
        if (longitude.isEastOf(easternmostLongitude)) {
            return false;
        }
        if (longitude.isWestOf(westernmostLongitude)) {
            return false;
        }
        return true;
    }

    public double getArea() {
        return new LatLonAltPosition(minimumLatitude, westernmostLongitude, Altitude.ZERO).getRangeTo(new LatLonAltPosition(minimumLatitude, easternmostLongitude, Altitude.ZERO)).getDoubleValue(Length.METERS)
                * new LatLonAltPosition(maximumLatitude, westernmostLongitude, Altitude.ZERO).getRangeTo(new LatLonAltPosition(minimumLatitude, westernmostLongitude, Altitude.ZERO)).getDoubleValue(Length.METERS);
    }

}
