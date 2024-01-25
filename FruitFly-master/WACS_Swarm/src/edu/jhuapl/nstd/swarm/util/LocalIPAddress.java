package edu.jhuapl.nstd.swarm.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


public class LocalIPAddress
{
    static public InetAddress getLocalHost()
    {
        return getLocalHost ((byte)Config.getConfig().getPropertyAsInteger("LocalIPAddress.preferrednetwork", 192));
    }

    static public InetAddress getLocalHost(final int preferredFirstAddressByte)
    {
        return getLocalHost ((byte)preferredFirstAddressByte);
    }
    
    static public InetAddress getLocalHost(final byte preferredFirstAddressByte)
    {
        InetAddress fallbackAddress = null;
        try
        {
            Enumeration<NetworkInterface> networkInterfaceList = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaceList.hasMoreElements())
            {
                NetworkInterface networkInterface = networkInterfaceList.nextElement();
                Enumeration<InetAddress> ipAddressList = networkInterface.getInetAddresses();
                while (ipAddressList.hasMoreElements())
                {
                    InetAddress ipAddress = ipAddressList.nextElement();
                    byte firstAddressByte = ipAddress.getAddress()[0];
                    if (firstAddressByte == preferredFirstAddressByte)
                    {
                        return ipAddress;
                    }

                    // Set the fallback address if its currently null or is the loopback or is an IPv6 address
                    if (fallbackAddress == null || fallbackAddress.isLoopbackAddress() || (fallbackAddress.getAddress().length > 4))
                    {
                        fallbackAddress = ipAddress;
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return fallbackAddress;
    }
}
