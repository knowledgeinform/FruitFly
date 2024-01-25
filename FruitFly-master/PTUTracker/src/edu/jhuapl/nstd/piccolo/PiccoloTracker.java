/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.piccolo;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.sti.ptutracker.Tracker;
import edu.jhuapl.nstd.util.GeoUtil;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author southmk1
 */
public class PiccoloTracker implements Pic_TelemetryListener, Runnable {

    private Tracker tracker;
    private Pic_Interface piccolo;
    private LatLonAltPosition gcsPosition = new LatLonAltPosition(new Latitude(0, Angle.DEGREES), new Longitude(0, Angle.DEGREES), new Altitude(0, Length.METERS));
    private LatLonAltPosition uavPosition = new LatLonAltPosition(new Latitude(0, Angle.DEGREES), new Longitude(0, Angle.DEGREES), new Altitude(0, Length.METERS));
    private int calibrationHeading = 0;
    private int magneticDeclination = 0;
    private int trackerOffsetPan = 0;
    private int trackerOffsetTilt = 0;
    private int panDegInt = 0,  tiltDegInt = 0;
    private boolean isAuto = true;
    private boolean stop = false;

    public PiccoloTracker(Tracker tracker, Pic_Interface piccolo) {
        this.tracker = tracker;
        this.piccolo = piccolo;
        piccolo.addPicTelemetryListener(this);
        //spin up threads
        new Thread(this).start();
    }

    public void handlePic_Telemetry(Pic_Telemetry telem) {
        setUavPosition(new LatLonAltPosition(new Latitude(telem.Lat, Angle.DEGREES),
                new Longitude(telem.Lon, Angle.DEGREES),
                new Altitude(telem.AltEllip, Length.METERS)));
    }

    public void sendMoveToCoordinates(int panDegrees, int tiltDegrees) {
        panDegInt = panDegrees + trackerOffsetPan - (calibrationHeading + magneticDeclination);
        tiltDegInt = tiltDegrees + trackerOffsetTilt;
        tracker.sendMoveToCoordinates(panDegInt, tiltDegInt);
    }

    public int getPanDegrees() {
        return tracker.getPanDegrees() - trackerOffsetPan + (calibrationHeading + magneticDeclination);
    }

    public int getTiltDegrees() {
        return tracker.getTiltDegrees() - trackerOffsetTilt;
    }

    public void shutdown() {
        stop = true;
    }

    public void run() {
        long lastRun = 0;
        while (!stop) {
            lastRun = System.currentTimeMillis();
            if (uavPosition != null && gcsPosition != null) {
                int[] pantilt = GeoUtil.calculatePointingAngles(gcsPosition, uavPosition);
                // add in tracking offsets (and calibration heading) (and magnetic variance)
                if (isAuto) {
                    sendMoveToCoordinates(pantilt[0], pantilt[1]);
                }
            }
            try {
                //to ensure we update at 1 hz 
                while (lastRun + 500 > System.currentTimeMillis()) {
                    Thread.sleep(50);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(PiccoloTracker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * @return the gcsPosition
     */
    public LatLonAltPosition getGcsPosition() {
        synchronized (gcsPosition) {
            return new LatLonAltPosition(
                    gcsPosition.getLatitude(),
                    gcsPosition.getLongitude(),
                    gcsPosition.getAltitude());
        }
    }

    /**
     * @param gcsPosition the gcsPosition to set
     */
    public void setGcsPosition(LatLonAltPosition gcsPosition) {
        synchronized (gcsPosition) {
            this.gcsPosition = gcsPosition;
        }
    }

    /**
     * @return the uavPosition
     */
    public LatLonAltPosition getUavPosition() {
        synchronized (uavPosition) {
            return new LatLonAltPosition(
                    uavPosition.getLatitude(),
                    uavPosition.getLongitude(),
                    uavPosition.getAltitude());
        }
    }

    /**
     * @param uavPosition the uavPosition to set
     */
    public void setUavPosition(LatLonAltPosition uavPosition) {
        synchronized (uavPosition) {
            this.uavPosition = uavPosition;
        }
    }

    /**
     * @return the calibrationHeading
     */
    public int getCalibrationHeading() {
        return calibrationHeading;
    }

    /**
     * @param calibrationHeading the calibrationHeading to set
     */
    public void setCalibrationHeading(int calibrationHeading) {
        this.calibrationHeading = calibrationHeading;
    }

    /**
     * @return the magneticDeclination
     */
    public int getMagneticDeclination() {
        return magneticDeclination;
    }

    /**
     * @return the trackerOffsetPan
     */
    public int getTrackerOffsetPan() {
        return trackerOffsetPan;
    }

    /**
     * @return the trackerOffsetTilt
     */
    public int getTrackerOffsetTilt() {
        return trackerOffsetTilt;
    }

    /**
     * @return the isAuto
     */
    public boolean isIsAuto() {
        return isAuto;
    }

    /**
     * @param isAuto the isAuto to set
     */
    public void setIsAuto(boolean isAuto) {
        this.isAuto = isAuto;
    }

    /**
     * @param magneticDeclination the magneticDeclination to set
     */
    public void setMagneticDeclination(int magneticDeclination) {
        this.magneticDeclination = magneticDeclination;
    }

    /**
     * @param trackerOffsetPan the trackerOffsetPan to set
     */
    public void setTrackerOffsetPan(int trackerOffsetPan) {
        this.trackerOffsetPan = trackerOffsetPan;
    }

    /**
     * @param trackerOffsetTilt the trackerOffsetTilt to set
     */
    public void setTrackerOffsetTilt(int trackerOffsetTilt) {
        this.trackerOffsetTilt = trackerOffsetTilt;
    }
}
