//=============================== UNCLASSIFIED ================================== 
// 
// Copyright (c) 1999 The Johns Hopkins University/Applied Physics Laboratory 
// Developed by JHU/APL. 
// 
// This material may be reproduced by or for the U.S. Government pursuant to 
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988). 
// For any other permissions please contact JHU/APL. 
// 
//=============================== UNCLASSIFIED ================================== 
package edu.jhuapl.nstd.swarm.belief;

import java.util.*;

import java.io.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.util.*;

public class CloudDetectionBelief extends Belief implements BeliefExternalizable
{

    public static final String BELIEF_NAME = "CloudDetectionBelief";
    protected long timeLastSent = 0;
    protected transient static List<CloudDetection> _detections = Collections.synchronizedList(new LinkedList<CloudDetection>());
    protected transient double _degrade = 1.0 - Config.getConfig().getPropertyAsDouble("cloudDecay", 0.0);
    protected transient boolean _swap = Config.getConfig().getPropertyAsBoolean("CloudDetectionBelief.swap", false);
    protected transient int _historylength = Config.getConfig().getPropertyAsInteger("CloudDetectionBelief.historyLength", 30);
    protected static final Object lock = new Object();

    public CloudDetectionBelief()
    {
        super("none");
        //_detections = new LinkedList<CloudDetection>();
        timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
    }

    public Object getLock()
    {
            return lock;
    }

    public CloudDetectionBelief(String agentID, LatLonAltPosition pos, double scaledValue, short source, short id, short rawValue)
    {
        this (agentID, pos, scaledValue, source, id, rawValue, System.currentTimeMillis());
    }

    public CloudDetectionBelief(String agentID, LatLonAltPosition pos, double scaledValue, short source, short id, short rawValue, long timeMs)
    {
        super(agentID);
        //_detections = new LinkedList<CloudDetection>();
        CloudDetection d = new CloudDetection(pos, scaledValue, source, id, rawValue, timeMs);

        synchronized (lock)
        {
             _detections.add(d);
        }
        timestamp = new Date(timeMs);
    }

    public String getName()
    {
        return BELIEF_NAME;
    }

    public List<CloudDetection> getDetections()
    {
        return _detections;
    }

    public void clearDetections()
    {
        synchronized (lock)
        {
            _detections.clear();
        }

        timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
    }
    
    public int getNumDetections()
    {
           return  _detections.size();
    }

    public void addBelief(Belief b)
    {
        addBelief (b, false);
    }
            
    public void addBelief(Belief b, boolean overwriteSameTimestamp)
    {
        CloudDetectionBelief cdb = (CloudDetectionBelief) b;
        if (b.getTimeStamp().compareTo(timestamp) > 0)
        {
            timestamp = b.getTimeStamp();
        }
        
        synchronized (lock)
        {
            Iterator <CloudDetection> itr = cdb.getDetections().iterator();
            LinkedList <CloudDetection> detsToAdd = new LinkedList<CloudDetection>();
            LinkedList <CloudDetection> detsToRemove = new LinkedList<CloudDetection>();
            
            //for (CloudDetection cd : cdb.getDetections())
            while (itr.hasNext())
            {
                CloudDetection cd = itr.next();
                boolean forceAdd = false;
                if (overwriteSameTimestamp)
                {
                    Iterator <CloudDetection> newItr = this.getDetections().iterator();
                    while (newItr.hasNext())
                    {
                        CloudDetection cdLocal = newItr.next();
                        
                        //If the detection in the local list has the same timestamp as the list being added,
                        //delete the copy in the local list
                        if (cdLocal.getTime() == cd.getTime())
                        {
                            detsToRemove.add(cdLocal);
                            forceAdd = true;
                        }
                    }
                }
                    
                if ((!_detections.contains(cd) && !detsToAdd.contains(cd)) || forceAdd)
                {
                    detsToAdd.add(cd);
                }
            }
            
            for (CloudDetection cd : detsToRemove)
            {
                _detections.remove(cd);
            }
            for (CloudDetection cd : detsToAdd)
            {
                if (!_detections.contains(cd) || cd.getScaledValue() > 0.5)
                    _detections.add(cd);
            }
        }
    }

    public void update()
    {
        try
        {
            super.update();
        }
        catch (Exception e)
        {
            System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }

    public byte[] serialize() throws IOException
    {

        setTransmissionTime();
        ByteArrayOutputStream baStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baStream);
        writeExternal(out);
        out.flush();
        out.close();
        return baStream.toByteArray();
    }

    public static Belief deserialize(InputStream iStream, Class clas) throws IOException
    {
        DataInputStream in = new DataInputStream(iStream);

        CloudDetectionBelief belief = null;

        try
        {
            belief = (CloudDetectionBelief) clas.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);

        return belief;
    }

    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);
        synchronized (lock)
        {
            //int size = _detections.size();
            int size = Math.min(_detections.size(), _historylength);
            byte[] data = new byte[2 + ((4 * 4 + 8 + 4 + 4) * size)];
            int index = ByteManipulator.addShort(data, (short) size, 0, _swap);

            for (int i = 0; i < size; i++)
            {
                CloudDetection cd = _detections.get(_detections.size() - 1 - i);
                byte[] cdBytes = cd.writeExternal();
                System.arraycopy(cdBytes, 0, data, index, cdBytes.length);
                index += cdBytes.length;
            }

            out.write(data);
        }
    }

    public void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);
        int size = (int) in.readShort();

        synchronized (lock)
        {
            for (int i = 0; i < size; i++)
            {
                CloudDetection d = new CloudDetection();
                d.readExternal(in);
                if (!_detections.contains(d))
                {
                    _detections.add(d);
                }
            }
        }
    }
}
