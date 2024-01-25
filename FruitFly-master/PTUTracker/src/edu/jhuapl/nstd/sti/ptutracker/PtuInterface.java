/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.sti.ptutracker;

import edu.jhuapl.nstd.util.XCommSerialPort;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author southmk1
 */
public class PtuInterface implements Tracker, Runnable {

    private XCommSerialPort serialPort;
    private int panDegrees;
    private int tiltDegrees;
    private final Object degreeMutex = new Object();
    private static final double degreesPerStepPan = 0.025714278;
    private static final double degreesPerStepTilt = 0.0128571389;
    private static final int maxPanSpeed = 1024;
    private static final int maxTiltSpeed = 1024;
    private static final int maxPanSpeedJog = 512;
    private static final int maxTiltSpeedJog = 512;
    private Vector<PropertyChangeListener> listeners = new Vector<PropertyChangeListener>();
    private boolean stop = false;

    public PtuInterface(XCommSerialPort serialPort) {
        this.serialPort = serialPort;
        new Thread(this).start();
    }

    public void shutdown() {
        stop = true;
    }

    public void init() {

        serialPort.sendBytes("ft\n".getBytes());
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(PtuInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] pp = serialPort.readAvailableBytes();
        if (pp == null) {
            return;
        } else {
            System.err.println(new String(pp));
        }


    }

    public void sendGetStatus() {

        int panSteps;
        int tiltSteps;
        synchronized (serialPort) {
            try {
                serialPort.sendBytes("pp\n".getBytes());
                Thread.sleep(10);
                byte[] pp = serialPort.readAvailableBytes();
                if (pp == null) {
                    return;
                } else {
                    System.err.println(new String(pp));
                }
                String[] ppString = new String(pp).split(" ");
                panSteps = Integer.parseInt(ppString[ppString.length - 1].trim());

                serialPort.sendBytes("tp\n".getBytes());
                Thread.sleep(10);
                byte[] tp = serialPort.readAvailableBytes();
                if (tp == null) {
                    return;
                } else {
                    System.err.println(new String(tp));
                }
                String[] tpString = new String(tp).split(" ");
                tiltSteps = Integer.parseInt(tpString[tpString.length - 1].trim());
            } catch (Exception ex) {
                return;
            }
        }
        int oldPan;
        int oldTilt;
        synchronized (degreeMutex) {
            oldPan = panDegrees;
            oldTilt = tiltDegrees;
            panDegrees = (int) (panSteps * degreesPerStepPan * 10);
            tiltDegrees = (int) (tiltSteps * degreesPerStepTilt * 10);
        }
        for (PropertyChangeListener pcl : listeners) {
            pcl.propertyChange(new PropertyChangeEvent(this, "panDegrees", oldPan, panDegrees));
            pcl.propertyChange(new PropertyChangeEvent(this, "tiltDegrees", oldTilt, tiltDegrees));
        }

    }

    public void addListener(PropertyChangeListener pcl) {
        listeners.add(pcl);
    }

    public void removeListener(PropertyChangeListener pcl) {
        listeners.remove(pcl);
    }

    public void sendMoveToAbsZero() {
        sendMoveToCoordinates(0, 0);

    }

    public void sendMoveToCoordinates(int panDegreesCmd, int tiltDegreesCmd) {
        if ((panDegreesCmd < panDegrees + 5 && panDegreesCmd > panDegrees - 5) && (tiltDegreesCmd < tiltDegrees + 5 && tiltDegreesCmd > tiltDegrees - 5)) {
            System.err.println("Ignoring command, pan/tilt within 1 degree");
            return;
        }

        while (panDegreesCmd > 1800) {
            panDegreesCmd -= 3600;
        }
        while (panDegreesCmd < -1800) {
            panDegreesCmd += 3600;
        }

        tiltDegreesCmd = tiltDegreesCmd < -890 ? -890 : tiltDegreesCmd;
        tiltDegreesCmd = tiltDegreesCmd > 0 ? 0 : tiltDegreesCmd;
        int panSteps = (int) (panDegreesCmd / degreesPerStepPan / 10.0);
        int tiltSteps = (int) (tiltDegreesCmd / degreesPerStepTilt / 10.0);

        synchronized (serialPort) {
            //send pan to steps
            serialPort.sendBytes(String.format("pp%d\n", panSteps).getBytes());
            //throw away responses
            byte[] response;
            response = serialPort.readAvailableBytes();
            if (response != null) {
                System.err.println(new String(response));
            }
            //send tilt to steps;
            System.err.println("Warning: Tilt axis disabled!");
        /*serialPort.sendBytes(String.format("tp%d\n", tiltSteps).getBytes());
        //throw away response
        response = serialPort.readAvailableBytes();
        if (response != null) {
        System.err.println(new String(response));
        }*/
        }
    }

    public void sendSetJogSpeed(double panSpeed, double tiltSpeed) {
        int panStepSpeed = (int) (panSpeed * maxPanSpeedJog);
        int tiltStepSpeed = (int) (tiltSpeed * maxTiltSpeedJog);
        synchronized (serialPort) {
            //set to jog mode
            serialPort.sendBytes("cv\n".getBytes());
            //throw away responses
            byte[] response;
            response = serialPort.readAvailableBytes();
            if (response != null) {
                System.err.println(new String(response));
            }

            //send pan to steps
            serialPort.sendBytes(String.format("ps%d\n", panStepSpeed).getBytes());
            //throw away responses
            response = serialPort.readAvailableBytes();
            if (response != null) {
                System.err.println(new String(response));
            }
            //send tilt to steps;
            System.err.println("Warning: Tilt axis disabled!");
            /*serialPort.sendBytes(String.format("ts%d\n", tiltStepSpeed).getBytes());
            //throw away response
            response = serialPort.readAvailableBytes();
            if (response != null) {
            System.err.println(new String(response));
            }
             */
            //turn off jog mode
            serialPort.sendBytes("ci\n".getBytes());
            //throw away responses
            response = serialPort.readAvailableBytes();
            if (response != null) {
                System.err.println(new String(response));
            }

            //set normal control velocities
            //send pan to steps
            serialPort.sendBytes(String.format("ps%d\n", maxPanSpeed).getBytes());
            //throw away responses
            response = serialPort.readAvailableBytes();
            if (response != null) {
                System.err.println(new String(response));
            }
            //send tilt to steps;
            serialPort.sendBytes(String.format("ts%d\n", maxTiltSpeed).getBytes());
            //throw away response
            response = serialPort.readAvailableBytes();
            if (response != null) {
                System.err.println(new String(response));
            }
        }
    }

    public int getPanDegrees() {
        int pan;
        synchronized (degreeMutex) {
            pan = panDegrees;
        }
        return pan;
    }

    public int getTiltDegrees() {
        int tilt;
        synchronized (degreeMutex) {
            tilt = tiltDegrees;
        }
        return tilt;
    }

    public void run() {
        init();
        while (!stop) {
            this.sendGetStatus();
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                Logger.getLogger(PtuInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
