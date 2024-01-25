/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wacs_extdisplayvideo;

import ch.randelshofer.media.avi.AVIOutputStream;
import com.sun.jmx.remote.internal.ClientCommunicatorAdmin;
import edu.jhuapl.nstd.util.ByteManipulator;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.imageio.ImageIO;

/**
 *
 * @author humphjc1
 */
public class WACS_ExtDisplayVideo extends Thread
{
    ConcurrentLinkedQueue <BufferedImage> m_ImageList = new ConcurrentLinkedQueue<BufferedImage>();
    int port_number = 5000;			/* Port number to use */
    
    boolean m_KillThread = false;
    boolean m_ThreadDone = false;
    
    WACS_ExtDisplayVideo ()
    {
        setDaemon(false);
        
        new Thread () {
            public void run ()
            {
                AVIOutputStream output = null;
                try
                {
                    File file = new File ("VideoRec_" + System.currentTimeMillis()/1000 + ".avi");
                    file.createNewFile();
                    output = new AVIOutputStream(file, AVIOutputStream.VideoFormat.JPG);
                    output.setFrameRate(15);
                }
                catch (Exception e)
                {
                    m_KillThread = true;
                    e.printStackTrace();
                }
                
                m_ThreadDone = false;
                while (!m_KillThread)
                {
                    if (m_ImageList.size() > 0)
                    {
                        BufferedImage img = m_ImageList.remove();
                        try
                        {
                            output.writeFrame (img);   
                        }
                        catch (Exception e)
                        {
                            m_KillThread = true;
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        try
                        {
                            Thread.sleep(10);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                
                try
                {
                    output.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                m_ThreadDone = true;
            }
        }.start();
    }
    
    public void run ()
    {
        try
        {
            Socket socket = null;
            while (true)
            {
                try
                {
                    socket = new Socket("localhost", 5000);
                    break;
                }
                catch (Exception e)
                {
                    //just loop until connection is made
                    Thread.sleep(100);
                }
            }
            
            byte runningConfirmBytes[] = {1};
            socket.getOutputStream().write(runningConfirmBytes);
            
            byte receiveBytes[] = new byte [50000000];
            while (true)
            {
                
                int readCount = socket.getInputStream().read(receiveBytes);
                if (readCount > 0)
                {
                    if (receiveBytes[0] == 1)
                    {
                        m_KillThread = true;
                        break;
                    }
                    else
                    {
                        byte[] array = new byte[4];
                        int width = ByteManipulator.getInt(receiveBytes, 1, true);
                        int height = ByteManipulator.getInt(receiveBytes, 5, true);
                        int reqdBytes = 9+(width)*(height)*3;
                        while (reqdBytes > readCount)
                            readCount += socket.getInputStream().read(receiveBytes, readCount, (reqdBytes-readCount));
                        
                        if (m_ImageList.size() < 10)
                        {
                            //Don't continually expand image queue
                            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                            for (int h = 0; h < height; h ++)
                            {
                                for (int w = 0; w < width; w ++)
                                {
                                    int red = ((int)(receiveBytes[9+h*(width)*3+w*3])+256)%256;
                                    int blue = ((int)(receiveBytes[9+h*(width)*3+w*3+1])+256)%256;
                                    int green = ((int)(receiveBytes[9+h*(width)*3+w*3+2])+256)%256;
                                    Color col = new Color(red, blue, green);
                                    img.setRGB(w, height-h-1, col.getRGB());
                                }
                            }
                            
                            m_ImageList.add(img);
                            System.out.println ("Number of images in java queue: " + m_ImageList.size());
                        }
                        
                    }
                }
            }
            
            while (!m_ThreadDone)
                Thread.sleep (100);
            
            byte stoppedConfirmBytes[] = {0};
            socket.getOutputStream().write(stoppedConfirmBytes);
            socket.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        // TODO code application logic here
        WACS_ExtDisplayVideo vid = new WACS_ExtDisplayVideo ();
        vid.start();
    }
}
