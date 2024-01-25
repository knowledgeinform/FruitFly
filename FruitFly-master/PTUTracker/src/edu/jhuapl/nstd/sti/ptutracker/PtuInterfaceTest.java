/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.sti.ptutracker;

import edu.jhuapl.nstd.util.XCommSerialPort;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author southmk1
 */
public class PtuInterfaceTest {

    public static void main(String[] args) {
        XCommSerialPort port = new XCommSerialPort("COM1", 19200, 8, 1, 0, 0);
        PtuInterface ptu = new PtuInterface(port);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int pan,tilt;

        // get the size of the board
        while (true) {
            while (true) {
                System.out.println("Enter Pan: ");
                try {
                    pan = Integer.parseInt(br.readLine());
                    break;
                } catch (IOException e) {
                    System.out.println("Invalid number, try again");
                }
            }

            while (true) {
                System.out.println("Enter Tilt: ");
                try {
                    tilt = Integer.parseInt(br.readLine());
                    break;
                } catch (IOException e) {
                    System.out.println("Invalid number, try again");
                }
            }
            ptu.sendMoveToCoordinates(pan, tilt);
            ptu.sendGetStatus();
            System.out.println("Pan: "+ptu.getPanDegrees());
            System.out.println("Tilt: "+ptu.getTiltDegrees());

        }
    }
}
