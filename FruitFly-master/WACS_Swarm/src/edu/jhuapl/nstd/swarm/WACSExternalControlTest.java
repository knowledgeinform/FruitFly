package edu.jhuapl.nstd.swarm;

import java.io.DataOutputStream;
import java.net.Socket;

public class WACSExternalControlTest
{
    public static void main(String args[])
    {
        try
        {
//            Socket spawnSocket = new Socket("localhost", 10776);
//            DataOutputStream spawnOutputStream = new DataOutputStream(spawnSocket.getOutputStream());
//
//            // Start WACS Agent
//            spawnOutputStream.writeShort(0);
//            spawnOutputStream.writeShort(4);
//
//            Thread.sleep(5000);

            Socket socket = new Socket("localhost", 10777);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            //outputStream.writeShort(3);
            //outputStream.writeShort(4);
            //outputStream.flush();
//
//            // Terminate WACS Agent
//            outputStream.writeShort(3);
//            outputStream.writeShort(4);
//
//            Thread.sleep(5000);
//
//            // Start WACS Agent
//            spawnOutputStream.writeShort(0);
//            spawnOutputStream.writeShort(4);
//
//            Thread.sleep(5000);

//            socket = new Socket("localhost", 10777);
//            outputStream = new DataOutputStream(socket.getOutputStream());
            

            // Set gimbal look location
            outputStream.writeShort(1);
            outputStream.writeShort(28);
            outputStream.writeDouble(40.1659518);
            outputStream.writeDouble(-113.1205855);
            outputStream.writeDouble(1315);

            // Set loiter location
            outputStream.writeShort(5);
            outputStream.writeShort(20);
            outputStream.writeDouble(40.1267120);
            outputStream.writeDouble(-113.1668997);

            // Set explosion time
            outputStream.writeShort(6);
            outputStream.writeShort(12);
            outputStream.writeLong(System.currentTimeMillis() + 600000);

            // Go into loiter mode
            outputStream.writeShort(2);
            outputStream.writeShort(6);
            outputStream.writeShort(0);

            Thread.sleep(100000);

            // Go into intercept mode
            outputStream.writeShort(2);
            outputStream.writeShort(6);
            outputStream.writeShort(1);

            Thread.sleep(25000);

            // Set gimbal look location
            outputStream.writeShort(1);
            outputStream.writeShort(28);
            outputStream.writeDouble(40.17);
            outputStream.writeDouble(-113.16);
            outputStream.writeDouble(1315);

            // Go into loiter mode
            outputStream.writeShort(2);
            outputStream.writeShort(6);
            outputStream.writeShort(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
