package edu.jhuapl.nstd.swarm.comms;

import edu.jhuapl.nstd.swarm.util.Config;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class CommsFramework
  implements LocatorInterface, CommsFrameworkInterface
{
  boolean simFlag;
  InetAddress groupAddr;
  int groupPort;
  int ttl;
  CommsFrameworkConfig config;
  private byte[] systemUniqueAddr = new byte[5];

  private int SimuSystemUniqueAddr = 0;

  private static CommsFramework singlton = null;

  protected MulticastSocket soc = null;
  private CommsTxServiceInterface txService;
  private CommsRxServiceInterface rxService;
  private CommsOamService oamService;
  private long commsInitFailures = 0L;
  private String lastInitFailureMsg = "";
  private boolean useLoopback = true;

  protected CommsFramework(boolean simulation, String configFileName) throws CommsException
  {
    try
    {
      this.useLoopback = Config.getConfig().getPropertyAsBoolean("multicast.useLoopback", true);
      this.ttl = Config.getConfig().getPropertyAsInteger("multicast.ttl", -1);
      this.commsInitFailures = 0L;
      this.lastInitFailureMsg = "";

      this.config = new CommsFrameworkConfig();

      this.SimuSystemUniqueAddr = this.config.systemUniqueID;

      byte[] buf = new Integer(this.SimuSystemUniqueAddr).toString().getBytes("UTF-8");
      int j = this.systemUniqueAddr.length;

      for (int i = buf.length; (i > 0) && (j > 0); --j) {
        this.systemUniqueAddr[(j - 1)] = buf[(i - 1)];

        --i;
      }

      for (; j > 0; --j) {
        this.systemUniqueAddr[(j - 1)] = 0;
      }

      if (simulation)
      {
        int tmp178_177 = 0;
        byte[] tmp178_174 = this.systemUniqueAddr; tmp178_174[tmp178_177] = (byte)(tmp178_174[tmp178_177] | 0x40);
      }
      else
      {
        int tmp193_192 = 0;
        byte[] tmp193_189 = this.systemUniqueAddr; tmp193_189[tmp193_192] = (byte)(tmp193_189[tmp193_192] & 0xFFFFFFBF);
      }

      this.simFlag = simulation;
    }
    catch (UnsupportedEncodingException e)
    {
      this.commsInitFailures += 1L;
      this.lastInitFailureMsg = ("UTF-8 encoding not supported : " + e.getMessage());
      e.printStackTrace();
    }

    initMulticastSocket();

    this.txService = new CommsTxService(this);

    this.rxService = new CommsRxService(this);

    this.oamService = new CommsOamService(this);

    if ((this.oamService == null) || 
      (this.oamService.stats == null)) return;
    this.oamService.stats.initFailCount += this.commsInitFailures;
    this.oamService.stats.lastInitFailMsg = this.lastInitFailureMsg;
  }

  protected MulticastSocket initMulticastSocket()
    throws CommsException
  {
    try
    {
      this.groupAddr = InetAddress.getByName(this.config.groupAddr);
      this.groupPort = this.config.groupPort;

      this.soc = new MulticastSocket(this.groupPort);
      if (this.ttl > 0)
          this.soc.setTimeToLive(this.ttl);

      if (Config.getConfig().hasProperty("networkInterface")) {
        String iface = Config.getConfig().getProperty("networkInterface");
        this.soc.setNetworkInterface(NetworkInterface.getByName(iface));
      }

      this.soc.joinGroup(this.groupAddr);

      if (this.config.debug) {
        System.out.println("disable loop back flag = " + this.config.enableLoopBack);
      }
      if (this.useLoopback) this.soc.setLoopbackMode(this.config.enableLoopBack);

      this.soc.setReceiveBufferSize(this.config.rxTxBuffSize);
      this.soc.setSendBufferSize(this.config.rxTxBuffSize);

      System.out.println("Socket receive Buffer size = " + this.soc.getReceiveBufferSize());
      System.out.println("Socket transmit Buffer size = " + this.soc.getSendBufferSize());

      return this.soc;
    }
    catch (UnknownHostException e)
    {
      this.commsInitFailures += 1L;
      this.lastInitFailureMsg = ("Unknown Host name used for IP multicast address : " + e.getMessage());
      e.printStackTrace();

      CommsException ex = new CommsException("Could not create IPaddress object, use a valid and absolute multicast IPv4 Address, don't use DNS host name.");
      throw ex;
    }
    catch (SocketException e)
    {
      this.commsInitFailures += 1L;
      this.lastInitFailureMsg = ("UDP Multicast Socket Failure : " + e.getMessage());
      e.printStackTrace();

      CommsException ex = new CommsException("Failed to disable loopback on the multicast socket : " + e.getMessage());
      throw ex;
    }
    catch (IOException e)
    {
      this.commsInitFailures += 1L;
      this.lastInitFailureMsg = ("UDP IO Failure (failuer to create multicast UDP socket : " + e.getMessage());
      e.printStackTrace();

      CommsException ex = new CommsException("Failed to create multicast UDP socket on port : " + this.groupPort + " and Address : " + this.groupAddr + " " + e.getMessage());
      throw ex;
    }
  }

  CommsOamService getOamService()
  {
    return this.oamService;
  }

  public CommsMulticastSocket getMulticastSocket()
  {
    return new CommsMulticastSocketImpl(this.soc);
  }

  public static CommsFramework getInstance(boolean simulation, String configFileName)
    throws CommsException
  {
    if (simulation) {
      return new CommsFramework(simulation, configFileName);
    }
    if (singlton == null) {
      singlton = new CommsFramework(simulation, configFileName);
    }

    return singlton;
  }

  public CommsTxServiceInterface getTxInterface()
  {
    return this.txService;
  }

  public CommsRxServiceInterface getRxInterface() {
    return this.rxService;
  }

  public CommsOamServiceInterface getOamInterface()
  {
    return this.oamService;
  }

  public byte[] getSysUniqueId() {
    return this.systemUniqueAddr;
  }

  public byte[] getCurrentLocation() {
    return new byte[5];
  }

  public long getDistance(byte[] values) {
    return 0L;
  }
}