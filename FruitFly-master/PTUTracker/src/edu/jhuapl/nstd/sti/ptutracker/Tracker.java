    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.sti.ptutracker;

/**
 *
 * @author southmk1
 */
public interface Tracker {

    public void sendGetStatus();

    public void sendMoveToAbsZero();

    public void sendMoveToCoordinates(int panDegrees, int tiltDegrees);

    public int getPanDegrees();

    public int getTiltDegrees();
}
