/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.cbrnPods.actions.podAction;
import edu.jhuapl.nstd.cbrnPods.messages.Pod.podHeartbeatMessage;
import edu.jhuapl.nstd.swarm.util.ByteManipulator;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class CBRNHeartbeatBelief extends Belief
{
    public static final String BELIEF_NAME = "cbrnHeartbeatBelief";
    
    protected Date m_LastHeartbeatMessagePod0Timestamp;
    protected podHeartbeatMessage m_LastHeartbeatMessagePod0;
    protected Date m_LastHeartbeatMessagePod1Timestamp;
    protected podHeartbeatMessage m_LastHeartbeatMessagePod1;

    public static final int SERIAL_C = 1;
    public static final int SERIAL_E = 2;
    
    
    public CBRNHeartbeatBelief ()
    {
        super("unspecified");
        timestamp = null;
        
        m_LastHeartbeatMessagePod0Timestamp = null;
        m_LastHeartbeatMessagePod0 = null;
        m_LastHeartbeatMessagePod1Timestamp = null;
        m_LastHeartbeatMessagePod1 = null;
    }
    
    public CBRNHeartbeatBelief (String agentID, podHeartbeatMessage heartbeatPod0, podHeartbeatMessage heartbeatPod1)
    {
        this (agentID, heartbeatPod0, heartbeatPod1, new Date(System.currentTimeMillis()));
    }
    
    public CBRNHeartbeatBelief (String agentID, podHeartbeatMessage heartbeatPod0, podHeartbeatMessage heartbeatPod1, Date time)
    {
        super(agentID);
        timestamp = time;
        if (heartbeatPod0 != null)
            setLastHeartbeat (new podHeartbeatMessage (heartbeatPod0));
        if (heartbeatPod1 != null)
            setLastHeartbeat (new podHeartbeatMessage (heartbeatPod1));
    }

    public String getLogMessage (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;

        if (chosenMsg == null)
            return "none";

        return chosenMsg.getLogMessage ();


    }
    
    /*private podHeartbeatMessage getLastHeartbeatPod0 ()
    {
        if (m_LastHeartbeatMessagePod0 == null)
            return null;
        
        return new podHeartbeatMessage (m_LastHeartbeatMessagePod0);
    }
    
    private Date getLastHeartbeatPod0Timestamp ()
    {
        return m_LastHeartbeatMessagePod0Timestamp;
    }
    
    private podHeartbeatMessage getLastHeartbeatPod1 ()
    {
        if (m_LastHeartbeatMessagePod1 == null)
            return null;
        
        return new podHeartbeatMessage (m_LastHeartbeatMessagePod1);
    }
    
    private Date getLastHeartbeatPod1Timestamp ()
    {
        return m_LastHeartbeatMessagePod1Timestamp;
    }*/
    
    private void setLastHeartbeat (podHeartbeatMessage heartbeat)
    {
        if (heartbeat == null)
            return;
        
        if (heartbeat.getPodNumber() == 0)
        {
            m_LastHeartbeatMessagePod0Timestamp = new Date(heartbeat.getTimestampMs());
            m_LastHeartbeatMessagePod0 = heartbeat;
        }
        else if (heartbeat.getPodNumber() == 1)
        {
            m_LastHeartbeatMessagePod1Timestamp = new Date(heartbeat.getTimestampMs());
            m_LastHeartbeatMessagePod1 = heartbeat;
        }
    }
    
    @Override
    protected void addBelief(Belief b) 
    {
        CBRNHeartbeatBelief belief = (CBRNHeartbeatBelief)b;
        
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
            
            if (belief.m_LastHeartbeatMessagePod0Timestamp != null && (m_LastHeartbeatMessagePod0Timestamp == null || belief.m_LastHeartbeatMessagePod0Timestamp.compareTo(m_LastHeartbeatMessagePod0Timestamp) > 0))
            {
                setLastHeartbeat(belief.m_LastHeartbeatMessagePod0);
            }
            if (belief.m_LastHeartbeatMessagePod1Timestamp != null && (m_LastHeartbeatMessagePod1Timestamp == null || belief.m_LastHeartbeatMessagePod1Timestamp.compareTo(m_LastHeartbeatMessagePod1Timestamp) > 0))
            {
                setLastHeartbeat(belief.m_LastHeartbeatMessagePod1);
            }
        }
    }

    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
    
    
    
    public long getTimestampMs (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return -1;
        
        return chosenMsg.getTimestampMs();
    }
    
    public boolean getLastLogCommandOn (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return false;
        
        return chosenMsg.getLastLogCommandOn();
    }
    
    public boolean getActualLogStateOn (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return false;
        
        return chosenMsg.getActualLogStateOn();
    }
    
    public String getTHControlString (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return "";
        
        return chosenMsg.getTHControlString();
    }
    
    public long getLastSerialRecSec (int serialNumber, int podNumber)
    {
        if (serialNumber == SERIAL_C)
            return getLastCRecvSec(podNumber);
        else if (serialNumber == SERIAL_E)
            return getLastERecvSec(podNumber);
        else
            return 0;
    }
    
    public long getLastCRecvSec (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return -1;
        
        return chosenMsg.getLastCRecvSec();
    }
    
    public long getLastERecvSec (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return -1;
        
        return chosenMsg.getLastERecvSec();
    }
    
    public float getTemperature (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return -300;
        
        return chosenMsg.getTemperature();
    }
    
    public long getTemperatureUpdatedSec (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return -1;
        
        return chosenMsg.getTemperatureUpdatedSec();
    }
    
    public float getHumidity (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return -1;
        
        return chosenMsg.getHumidity();
    }
    
    public long getHumidityUpdatedSec (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return -1;
        
        return chosenMsg.getHumidityUpdatedSec();
    }
    
    public int getServoToggledOpen (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return -100;
        
        return chosenMsg.getServo0ToggledOpen();
    }
    
    public int getLastServoDuty (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return -1;
        
        return chosenMsg.getLastServo0Duty();
    }
    
    public int getfanToggledOn (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return -1;
        
        return chosenMsg.getFanToggledOn();
    }
    
    public int getHeaterToggledOn (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return -1;
        
        return chosenMsg.getHeaterToggledOn();
    }
    
    public boolean getServoManualOverride (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return false;
        
        return chosenMsg.getServo0ManualOverride();
    }
    
    public boolean getFanManualOverride (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return false;
        
        return chosenMsg.getFanManualOverride();
    }
    
    public boolean getHeaterManualOverride (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return false;
        
        return chosenMsg.getHeaterManualOverride();
    }
    
    public float getVoltage (int podNumber)
    {
        podHeartbeatMessage chosenMsg = null;
        if (podNumber == 0)
            chosenMsg = m_LastHeartbeatMessagePod0;
        else if (podNumber == 1)
            chosenMsg = m_LastHeartbeatMessagePod1;
        
        if (chosenMsg == null)
            return -1;
        
        return chosenMsg.getVoltage();
    }
    
    
    @Override
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
        Belief belief = null;

        try 
        {
            belief = (Belief)clas.newInstance();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);
        return belief;
    }

    @Override
	public void writeExternal(DataOutput out) throws IOException {

		super.writeExternal(out);
                
                byte[] bytes = new byte[20];
                int index = 0;
                
                if (m_LastHeartbeatMessagePod0Timestamp != null)
                    index = ByteManipulator.addLong(bytes,m_LastHeartbeatMessagePod0Timestamp.getTime(),index,false);
                else
                    index = ByteManipulator.addLong(bytes,-1,index,false);
                
                if (m_LastHeartbeatMessagePod1Timestamp != null)
                    index = ByteManipulator.addLong(bytes,m_LastHeartbeatMessagePod1Timestamp.getTime(),index,false);
                else
                    index = ByteManipulator.addLong(bytes,-1,index,false);
                
                out.write(bytes);
                
                writeExternal (out, m_LastHeartbeatMessagePod0);
                writeExternal (out, m_LastHeartbeatMessagePod1);
	}
        
        private void writeExternal (DataOutput out, podHeartbeatMessage message) throws IOException
        {
            byte[] bytes = new byte[112];
            int index = 0;
            
            if (message == null)
                index = ByteManipulator.addLong(bytes,-1,index,false);
            else
            {
                index = ByteManipulator.addLong(bytes,message.getTimestampMs(),index,false);
                index = ByteManipulator.addInt(bytes,message.getPodNumber(),index,false);
                index = ByteManipulator.addBoolean(bytes,message.getLastLogCommandOn(),index,false);
                index = ByteManipulator.addBoolean(bytes,message.getActualLogStateOn(),index,false);
                index = ByteManipulator.addLong(bytes,message.getLastCRecvSec(),index,false);
                index = ByteManipulator.addLong(bytes,message.getLastERecvSec(),index,false);
                index = ByteManipulator.addFloat(bytes,message.getVoltage(),index,false);
                index = ByteManipulator.addLong(bytes,message.getVoltageUpdatedSec(),index,false);
                index = ByteManipulator.addFloat(bytes,message.getTemperature(),index,false);
                index = ByteManipulator.addLong(bytes,message.getTemperatureUpdatedSec(),index,false);
                index = ByteManipulator.addFloat(bytes,message.getHumidity(),index,false);
                index = ByteManipulator.addLong(bytes,message.getHumidityUpdatedSec(),index,false);
                index = ByteManipulator.addInt(bytes,message.getServo0ToggledOpen(),index,false);
                index = ByteManipulator.addInt(bytes,message.getLastServo0Duty(),index,false);
                index = ByteManipulator.addInt(bytes,message.getFanToggledOn(),index,false);
                index = ByteManipulator.addInt(bytes,message.getHeaterToggledOn(),index,false);
                index = ByteManipulator.addBoolean(bytes,message.getServo0ManualOverride(),index,false);
                index = ByteManipulator.addBoolean(bytes,message.getFanManualOverride(),index,false);
                index = ByteManipulator.addBoolean(bytes,message.getHeaterManualOverride(),index,false);
                index = ByteManipulator.addInt(bytes, message.getServoTempLimit(), index, false);
                index = ByteManipulator.addInt(bytes, message.getServoHumidityLimit(), index, false);
                index = ByteManipulator.addInt(bytes, message.getFanTempLimit(), index, false);
                index = ByteManipulator.addInt(bytes, message.getFanHumidityLimit(), index, false);
                index = ByteManipulator.addInt(bytes, message.getHeaterTempLimit(), index, false);
                index = ByteManipulator.addInt(bytes, message.getHeaterHumidityLimit(), index, false);
                index = ByteManipulator.addShort(bytes, message.getLastLogErrCode(), index, false);
            }
            
            out.write(bytes);
        }

    @Override
	public void readExternal(DataInput in) throws IOException 
        {
            try
            {
                m_LastHeartbeatMessagePod0 = new podHeartbeatMessage();
                m_LastHeartbeatMessagePod1 = new podHeartbeatMessage();
                
                super.readExternal(in);

                byte[] bytes  = new byte[20];
                in.readFully(bytes);
                
                long readTime = -1;
                int index = 0;
                
                readTime = ByteManipulator.getLong(bytes, index, false);
                if (readTime > 0)
                    m_LastHeartbeatMessagePod0Timestamp = new Date(readTime);
                else
                    m_LastHeartbeatMessagePod0Timestamp = null;
                index += 8;
                
                readTime = ByteManipulator.getLong(bytes, index, false);
                if (readTime > 0)
                    m_LastHeartbeatMessagePod1Timestamp = new Date(readTime);
                else
                    m_LastHeartbeatMessagePod1Timestamp = null;
                index += 8;
                
                if (!readExternal (in, m_LastHeartbeatMessagePod0))
                    m_LastHeartbeatMessagePod0 = null;
                if (!readExternal (in, m_LastHeartbeatMessagePod1))
                    m_LastHeartbeatMessagePod1 = null;
                
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        
        public boolean readExternal(DataInput in, podHeartbeatMessage message) throws IOException 
        {
            byte[] bytes  = new byte[112];
            in.readFully(bytes);

            long readTime = -1;
            int index = 0;
           
            readTime = ByteManipulator.getLong(bytes, index, false);
            if (readTime > 0)
                message.setTimestampMs(readTime);
            else
                return false;
            index += 8;

            message.setPodNumber(ByteManipulator.getInt(bytes, index, false));
            index += 4;
            message.setLastLogCommandOn(ByteManipulator.getBoolean(bytes, index, false));
            index += 1;
            message.setActualLogStateOn(ByteManipulator.getBoolean(bytes, index, false));
            index += 1;
            message.setLastCRecvSec(ByteManipulator.getLong(bytes, index, false));
            index += 8;
            message.setLastERecvSec(ByteManipulator.getLong(bytes, index, false));
            index += 8;
            message.setVoltage(ByteManipulator.getFloat(bytes, index, false));
            index += 4;
            message.setVoltageUpdatedSec(ByteManipulator.getLong(bytes, index, false));
            index += 8;
            message.setTemperature(ByteManipulator.getFloat(bytes, index, false));
            index += 4;
            message.setTemperatureUpdatedSec(ByteManipulator.getLong(bytes, index, false));
            index += 8;
            message.setHumidity(ByteManipulator.getFloat(bytes, index, false));
            index += 4;
            message.setHumidityUpdatedSec(ByteManipulator.getLong(bytes, index, false));
            index += 8;
            message.setServo0ToggledOpen(ByteManipulator.getInt(bytes, index, false));
            index += 4;
            message.setLastServo0Duty(ByteManipulator.getInt(bytes, index, false));
            index += 4;
            message.setFanToggledOn(ByteManipulator.getInt(bytes, index, false));
            index += 4;
            message.setHeaterToggledOn(ByteManipulator.getInt(bytes, index, false));
            index += 4;
            message.setServo0ManualOverride(ByteManipulator.getBoolean(bytes, index, false));
            index += 1;
            message.setFanManualOverride(ByteManipulator.getBoolean(bytes, index, false));
            index += 1;
            message.setHeaterManualOverride(ByteManipulator.getBoolean(bytes, index, false));
            index += 1;     
            message.setServoTempLimit(ByteManipulator.getInt(bytes, index, false));
            index += 4;     
            message.setServoHumidityLimit(ByteManipulator.getInt(bytes, index, false));
            index += 4;     
            message.setFanTempLimit(ByteManipulator.getInt(bytes, index, false));
            index += 4;     
            message.setFanHumidityLimit(ByteManipulator.getInt(bytes, index, false));
            index += 4;     
            message.setHeaterTempLimit(ByteManipulator.getInt(bytes, index, false));
            index += 4;     
            message.setHeaterHumidityLimit(ByteManipulator.getInt(bytes, index, false));
            index += 4;     
            message.setLastLogErrCode(ByteManipulator.getShort(bytes, index, false));
            index += 2;     
            
            return true;
	}
            
            
}
