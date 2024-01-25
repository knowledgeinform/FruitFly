/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.TimeManagerFactory;
import java.io.DataInput;
import java.io.IOException;
import java.util.Date;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import java.io.*;
import java.util.*;
import edu.jhuapl.nstd.swarm.TimeManagerFactory;
import edu.jhuapl.nstd.swarm.util.ByteManipulator;
import edu.jhuapl.nstd.swarm.util.Config;

/**
 *
 * @author kayjl1
 */
public class EtdDetectionListBelief extends Belief implements BeliefExternalizable
{

    public static final String BELIEF_NAME = "EtdDetectionListBelief";
    protected long timeLastSent = 0;
    protected transient static List<EtdDetection> _detections = Collections.synchronizedList(new LinkedList<EtdDetection>());
    protected transient double _degrade = 1.0 - Config.getConfig().getPropertyAsDouble("EtdDetectionListBelief.decaytime", 0.0);
    protected transient boolean _swap = Config.getConfig().getPropertyAsBoolean("EtdDetectionListBelief.swap", false);
    protected transient int _historylength = Config.getConfig().getPropertyAsInteger("EtdDetectionListBelief.historyLength", 30);
    protected static final Object lock = new Object();

    public EtdDetectionListBelief()
    {
        super("none");
        //_detections = new LinkedList<CloudDetection>();
        timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
    }
    
    public EtdDetectionListBelief(String agentID)
    {
        super(agentID);
        //_detections = new LinkedList<CloudDetection>();
        timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
    }

    public Object getLock()
    {
            return lock;
    }

    public EtdDetectionListBelief(String agentID, Float concentration, AbsolutePosition pos, long timeMs)
    {
        super(agentID);
        //_detections = new LinkedList<CloudDetection>();
        EtdDetection d = new EtdDetection(concentration, pos, timeMs);

        synchronized (lock)
        {
            addDetection(d);
             while (_detections.size()>_historylength) {
                _detections.remove(0);
            }
        }
        timestamp = new Date(timeMs);
    }

    public String getName()
    {
        return BELIEF_NAME;
    }

    public List<EtdDetection> getDetections()
    {
        return _detections;
    }
    
    public List<EtdDetection> getFilteredDetections()
    {
        double[] filter = new double[]{0.0038, 0.0167, 0.0524, 0.1187, 0.1938, 0.2282, 0.1938, 0.1187, 0.0524, 0.0167, 0.0038};
        List<EtdDetection> filteredDetections = new LinkedList<EtdDetection>();
        
        int halfFilterSize = (int) Math.floor(filter.length/2.0);
        
        for(int iDetections=halfFilterSize; iDetections<_detections.size()-halfFilterSize; iDetections++) {
            float filteredValue = 0;
            for(int iFilter=0; iFilter<filter.length; iFilter++) {
                filteredValue += _detections.get(iDetections-halfFilterSize+iFilter).getConcentration()*filter[iFilter];
            }
            EtdDetection filteredDetection = new EtdDetection(filteredValue, _detections.get(iDetections).getPosition(), _detections.get(iDetections).getTime());
            filteredDetections.add(filteredDetection);
        }
        
        return filteredDetections;
    }
    
    private void addDetection(EtdDetection d) {
        _detections.add(d);
        Collections.sort(_detections);
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
        EtdDetectionListBelief cdb = (EtdDetectionListBelief) b;
        if (b.getTimeStamp().compareTo(timestamp) > 0)
        {
            timestamp = b.getTimeStamp();
        }
        
        synchronized (lock)
        {
            Iterator <EtdDetection> itr = cdb.getDetections().iterator();
            LinkedList <EtdDetection> detsToAdd = new LinkedList<EtdDetection>();
            LinkedList <EtdDetection> detsToRemove = new LinkedList<EtdDetection>();
            
            //for (CloudDetection cd : cdb.getDetections())
            while (itr.hasNext())
            {
                EtdDetection cd = itr.next();
                boolean forceAdd = false;
                if (overwriteSameTimestamp)
                {
                    Iterator <EtdDetection> newItr = this.getDetections().iterator();
                    while (newItr.hasNext())
                    {
                        EtdDetection cdLocal = newItr.next();
                        
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
            
            for (EtdDetection cd : detsToRemove)
            {
                _detections.remove(cd);
            }
            for (EtdDetection cd : detsToAdd)
            {
                if (!_detections.contains(cd))
                    addDetection(cd);
                while (_detections.size()>_historylength) {
                    _detections.remove(0);
                }
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

        EtdDetectionListBelief belief = null;

        try
        {
            belief = (EtdDetectionListBelief) clas.newInstance();
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
                EtdDetection cd = _detections.get(_detections.size() - 1 - i);
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
                EtdDetection d = new EtdDetection();
                d.readExternal(in);
                if (!_detections.contains(d))
                {
                    addDetection(d);
                    while (_detections.size()>_historylength) {
                        _detections.remove(0);
                    }
                }
            }
        }
    }

}
