package edu.jhuapl.nstd.mapping.sensors.gpsNmea;

import java.util.ArrayList;
import edu.jhuapl.nstd.mapping.sensors.gpsNmea.NMEAEvent;
import edu.jhuapl.nstd.mapping.sensors.gpsNmea.NMEAListener;
import edu.jhuapl.nstd.mapping.sensors.gpsNmea.NMEAException;

/**
 * A Controller.
 * This class is final, and can be used as it is.
 * 
 * @see ocss.nmea.api.NMEAReader
 * @see ocss.nmea.api.NMEAClient
 * @see ocss.nmea.api.NMEAEvent
 * @see ocss.nmea.api.NMEAException
 * 
 * @version 1.0
 * @author Olivier Le Diouris
 * 
 */
public final class NMEAParser extends Thread
{
  protected String nmeaPrefix = "";
  private String[] nmeaSentence = null;
  private boolean m_Running = false;

  private String nmeaStream = "";
  private Object nmeaStreamLock = new Object ();
  private final static long MAX_STREAM_SIZE = 2048;
  private final static String NMEA_EOS = "\r\n";
  private transient ArrayList NMEAListeners = null; // new ArrayList(2);

  /**
   * @param The ArrayList of the Listeners instanciated by the NMEAClient
   */
  public NMEAParser(ArrayList al)
  {  
      this.setName ("RADMAPS-" + this.getClass().getSimpleName());
    System.out.println("Creating NMEA parser");
    NMEAListeners = al;
    this.addNMEAListener(new NMEAListener()
      {
        public void dataRead(NMEAEvent e)
        {
//        System.out.println("Receieved Data:" + e.getContent());
            String next = e.getContent();
            synchronized (nmeaStreamLock)
            {
            if (nmeaStream != null && !nmeaStream.isEmpty() && !nmeaStream.startsWith("$"))
            {
                System.out.println ("Stream became garbage: nmeaStream = " + nmeaStream.substring(0, Math.min(nmeaStream.length(), 100)));
                System.out.println ("Next NMEA data would have been: " + next);
                
                
                if (nmeaStream.contains("$"))
                {
                    System.out.println ("Found $ sign in nmea data stream, skipping ahead to that!");
                    nmeaStream = nmeaStream.substring(nmeaStream.indexOf("$"));
                    System.out.println ("new nmea stream: " + nmeaStream);
                    nmeaStream += next;
                }
                else if (next.contains("$"))
                {
                    System.out.println ("Found $ sign in next data stream, skipping ahead to that!");
                    next = next.substring(next.indexOf("$"));
                    System.out.println ("new next: " + next);
                    nmeaStream = next;
                }
                else
                {
                    System.out.println ("Unknown data in nmea buffer and next buffer, clearing it: " + nmeaStream.substring(0, Math.min(nmeaStream.length(), 100)));
                    nmeaStream = "";
                }
            }
            else
                nmeaStream += e.getContent();
            }
        }
      });
  }

  public String getNmeaPrefix()
  { return this.nmeaPrefix; }
  public void setNmeaPrefix(String s)
  { this.nmeaPrefix = s; }

  public String[] getNmeaSentence()
  { return this.nmeaSentence; }
  public void setNmeaSentence(String[] sa)
  { this.nmeaSentence = sa; }

  /*public String getNmeaStream()
  { return this.nmeaStream; }
  public void setNmeaStream(String s)
  { this.nmeaStream = s; }*/

  public String detectSentence() throws NMEAException
  {
    String ret = null;
    int idx;

    synchronized (nmeaStreamLock)
    {
    try
    {
        //System.out.println ("Curr NMEA: " + nmeaStream.substring(0, Math.min(nmeaStream.length(), 15)));
      if (interesting())
      {
        int end = nmeaStream.indexOf(NMEA_EOS);
        ret = nmeaStream.substring(0, end);
        nmeaStream = nmeaStream.substring(end + NMEA_EOS.length());
      }
      else
      {
        if (nmeaStream.length() > MAX_STREAM_SIZE)
        {
            System.out.println ("Could never resolve nmea data in buffer, clearing it: " + nmeaStream.substring(0, Math.min(nmeaStream.length(), 100)));
            nmeaStream = ""; // Reset to avoid OutOfMemoryException
        }
        return null; // Not enough info
      }  
    }
    catch (NMEAException e)
    {
      throw e;
    }
    }
    return ret;
  }

  private boolean interesting() throws NMEAException
  {
    if (nmeaPrefix.length() == 0)
      throw new NMEAException("NMEA Prefix is not set");
      
    int beginIdx = nmeaStream.indexOf("$" + this.nmeaPrefix);
    int endIdx   = nmeaStream.indexOf(NMEA_EOS);

    if (beginIdx == -1 && endIdx == -1)
      return false; // No beginning, no end !
      
    if (endIdx > -1 && endIdx < beginIdx) // Seek the beginning of a sentence
    {
      nmeaStream = nmeaStream.substring(endIdx + NMEA_EOS.length());
      beginIdx = nmeaStream.indexOf("$" + this.nmeaPrefix);
    }

    if (beginIdx== -1)
      return false;
    else
    {
      while (true)
      {
        try
        {
          // The stream should here begin with $XX
          if (nmeaStream.length() > 6) // "$" + prefix + XXX
          {
            if ((endIdx = nmeaStream.indexOf(NMEA_EOS)) > -1)
            {                
              for (int i=0; i<this.nmeaSentence.length; i++)
              {
                if (nmeaStream.startsWith("$" + nmeaPrefix + nmeaSentence[i]))
                  return true;
              }
              nmeaStream = nmeaStream.substring(endIdx + NMEA_EOS.length());
            }
            else
              return false; // unfinished sentence
          }
          else
            return false; // Not long enough - Not even sentence ID
        }
        catch (Exception e)
        {
          e.printStackTrace();
          System.out.println("nmeaStream.length = " + nmeaStream.length() + ", Stream:[" + nmeaStream + "]");
        }
      } // End of infinite loop
    }
  }

  protected void fireDataDetected(NMEAEvent e)
  {
    for (int i=0; i<NMEAListeners.size(); i++)
    {
      NMEAListener l = (NMEAListener)NMEAListeners.get(i);
      l.dataDetected(e);
    }
  }

  public synchronized void addNMEAListener(NMEAListener l)
  {
    if (!NMEAListeners.contains(l))
    {
      NMEAListeners.add(l);
    }
  }

  public synchronized void removeNMEAListener(NMEAListener l)
  {
    NMEAListeners.remove(l);
  }

  public void loop()
  {
    m_Running = true;
    while (m_Running)
    {
      try 
      { 
        String s = "";
        while ( (s = detectSentence()) != null)
        {
          this.fireDataDetected(new NMEAEvent(this, s));
        }
      } 
      catch (Exception e)
      {
        e.printStackTrace();
      }
      try
      {
//      System.out.println("Parser Taking a 1s nap");
        Thread.sleep(50);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  public void run()
  {
    System.out.println("NMEA Parser Running");
    loop();
  }
  
  public void close()
  {
      m_Running = false;
  }

}