/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.sti.ptutracker.app;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

/**
 *
 * @author southmk1
 */
public class ApplicationState implements Serializable {

    /**
     * @return the applicationState
     */
    public static ApplicationState getApplicationState() {
        return applicationState;
    }


    private String trackerComPort;
    private String comOrIp;
    private String portOrBaud;
    private int pilotId;
    private boolean isTCP;
    private Vector<LatLonAltPosition> gcsPositions;
    private Vector<LatLonAltPosition> uavPositions;
    private static String filename = "tracker.config";
    private static ApplicationState applicationState;

    private double magneticDeclination;
    private double calibrationHeading;

    private double calibrationTilt;
    private double calbirationPan;


    static {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
            applicationState = (ApplicationState) ois.readObject();
            ois.close();
        } catch (Exception ex) {
            System.err.println("Error loading application state. restoring defaults");
            applicationState = new ApplicationState();
        }
    }

    private ApplicationState() {
        trackerComPort = "COM1";
        comOrIp = "176.16.2.105";
        portOrBaud = "2000";
        pilotId = 2195;
        isTCP = true;
        magneticDeclination = 0.0;
        calibrationHeading = 0.0;

        calibrationTilt=0.0;
        calbirationPan=0.0;

        gcsPositions = new Vector<LatLonAltPosition>();
        uavPositions = new Vector<LatLonAltPosition>();

        gcsPositions.add(new LatLonAltPosition(new Latitude(40.103725, Angle.DEGREES), new Longitude(-113.189115, Angle.DEGREES), new Altitude(1295, Length.METERS)));
        uavPositions.add(new LatLonAltPosition(new Latitude(40.10432, Angle.DEGREES), new Longitude(-113.18964, Angle.DEGREES), new Altitude(1295, Length.METERS)));
    }

    public static void saveToFile() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
            oos.writeObject(applicationState);
            oos.close();
        } catch (Exception ex) {
            System.err.println("Error loading application state. restoring defaults");
            applicationState = new ApplicationState();
        }
    }


    /**
     * @return the comOrIp
     */
    public String getComOrIp() {
        return comOrIp;
    }

    /**
     * @param comOrIp the comOrIp to set
     */
    public void setComOrIp(String comOrIp) {
        this.comOrIp = comOrIp;
    }

    /**
     * @return the portOrBaud
     */
    public String getPortOrBaud() {
        return portOrBaud;
    }

    /**
     * @param portOrBaud the portOrBaud to set
     */
    public void setPortOrBaud(String portOrBaud) {
        this.portOrBaud = portOrBaud;
    }

    /**
     * @return the pilotId
     */
    public int getPilotId() {
        return pilotId;
    }

    /**
     * @param pilotId the pilotId to set
     */
    public void setPilotId(int pilotId) {
        this.pilotId = pilotId;
    }

    /**
     * @return the isTCP
     */
    public boolean isIsTCP() {
        return isTCP;
    }

    /**
     * @param isTCP the isTCP to set
     */
    public void setIsTCP(boolean isTCP) {
        this.isTCP = isTCP;
    }

    /**
     * @return the gcsPositions
     */
    public Vector<LatLonAltPosition> getGcsPositions() {
        return gcsPositions;
    }

    /**
     * @param gcsPositions the gcsPositions to set
     */
    public void setGcsPositions(Vector<LatLonAltPosition> gcsPositions) {
        this.gcsPositions = gcsPositions;
    }

    /**
     * @return the uavPositions
     */
    public Vector<LatLonAltPosition> getUavPositions() {
        return uavPositions;
    }

    /**
     * @param uavPositions the uavPositions to set
     */
    public void setUavPositions(Vector<LatLonAltPosition> uavPositions) {
        this.uavPositions = uavPositions;
    }

    /**
     * @return the trackerComPort
     */
    public String getTrackerComPort() {
        return trackerComPort;
    }

    /**
     * @param trackerComPort the trackerComPort to set
     */
    public void setTrackerComPort(String trackerComPort) {
        this.trackerComPort = trackerComPort;
    }

    /**
     * @return the magneticDeclination
     */
    public double getMagneticDeclination() {
        return magneticDeclination;
    }

    /**
     * @param magneticDeclination the magneticDeclination to set
     */
    public void setMagneticDeclination(double magneticDeclination) {
        this.magneticDeclination = magneticDeclination;
    }

    /**
     * @return the calibrationHeading
     */
    public double getCalibrationHeading() {
        return calibrationHeading;
    }

    /**
     * @param calibrationHeading the calibrationHeading to set
     */
    public void setCalibrationHeading(double calibrationHeading) {
        this.calibrationHeading = calibrationHeading;
    }

    /**
     * @return the calibrationTilt
     */
    public double getCalibrationTilt() {
        return calibrationTilt;
    }

    /**
     * @param calibrationTilt the calibrationTilt to set
     */
    public void setCalibrationTilt(double calibrationTilt) {
        this.calibrationTilt = calibrationTilt;
    }

    /**
     * @return the calbirationPan
     */
    public double getCalibrationPan() {
        return calbirationPan;
    }

    /**
     * @param calbirationPan the calbirationPan to set
     */
    public void setCalibrationPan(double calbirationPan) {
        this.calbirationPan = calbirationPan;
    }
}
