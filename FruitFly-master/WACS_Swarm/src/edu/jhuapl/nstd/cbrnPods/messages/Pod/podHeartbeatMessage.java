package edu.jhuapl.nstd.cbrnPods.messages.Pod;

import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import java.io.Serializable;

/**
 * Heartbeat command used to let the user know the pods are working.
 *  
 * @author humphjc1
 */
public class podHeartbeatMessage extends cbrnPodMsg implements Serializable
{    
    //Biopod type
    private int podNumber;
    
    //Last thing we sent to board about whether to log or not.  True = on
    private boolean lastLogCommandOn;
    
    //Whether the board is logging or not.  True = on
    private boolean actualLogStateOn;
    
    //Timestamp when last message received on Serial C
    private long lastCRecvSec;
    
    //Timestamp when last message received on Serial E
    private long lastERecvSec;
    
    //Voltage last recorded
    private float voltage;
    
    //Timestamp when last voltage received
    private long voltageUpdatedSec;
    
    //Temperature last recorded
    private float temperature;
    
    //Timestamp when last temperature received
    private long temperatureUpdatedSec;
    
    //Humidity last recorded
    private float humidity;
    
    //Timestamp when last humidity received
    private long humidityUpdatedSec;
    
    //State of servo control
    private int servo0ToggledOpen;
    
    //Last duty cycle sent to servo
    private int lastServo0Duty;
    
    //State of fan control
    private int fanToggledOn;
    
    //State of heater control
    private int heaterToggledOn;
    
    //True if servo control is overridden by user, not under auto TH control
    private boolean servo0ManualOverride;
    
    //True if fan control is overridden by user, not under auto TH control
    private boolean fanManualOverride;
    
    //True if heater control is overridden by user, not under auto TH control
    private boolean heaterManualOverride;
    
    //Servo temperature limit
    private int servoTempLimit;
    
    //Servo humidity limit
    private int servoHumidityLimit;
    
    //fan temperature limit
    private int fanTempLimit;
    
    //fan humidity limit
    private int fanHumidityLimit;
    
    //heater temperature limit
    private int heaterTempLimit;
    
    //heater humidity limit
    private int heaterHumidityLimit;
    
    //Last error code from logging (actually the first error code encountered from the last time there were errors)
    private short lastLogErrCode;
    
    
    /**
     * podHeartbeatMessage constructor
     */
    public podHeartbeatMessage() 
    {
        super(cbrnSensorTypes.RABBIT_BOARD, cbrnPodMsg.POD_HEARTBEAT_TYPE, 0);
    }
    
    /**
     * podHeartbeatMessage constructor
     * @param m_LastHeartbeatMessage
     */
    public podHeartbeatMessage(podHeartbeatMessage m_LastHeartbeatMessage) 
    {
        if (m_LastHeartbeatMessage == null)
            return;
        
        this.timestampMs = m_LastHeartbeatMessage.timestampMs;
        this.podNumber = m_LastHeartbeatMessage.podNumber;
        this.lastLogCommandOn = m_LastHeartbeatMessage.lastLogCommandOn;
        this.actualLogStateOn = m_LastHeartbeatMessage.actualLogStateOn;
        this.lastCRecvSec = m_LastHeartbeatMessage.lastCRecvSec;
        this.lastERecvSec = m_LastHeartbeatMessage.lastERecvSec;
        this.voltage = m_LastHeartbeatMessage.voltage;
        this.voltageUpdatedSec = m_LastHeartbeatMessage.voltageUpdatedSec;
        this.temperature = m_LastHeartbeatMessage.temperature;
        this.temperatureUpdatedSec = m_LastHeartbeatMessage.temperatureUpdatedSec;
        this.humidity = m_LastHeartbeatMessage.humidity;
        this.humidityUpdatedSec = m_LastHeartbeatMessage.humidityUpdatedSec;
        this.servo0ToggledOpen = m_LastHeartbeatMessage.servo0ToggledOpen;
        this.lastServo0Duty = m_LastHeartbeatMessage.lastServo0Duty;
        this.fanToggledOn = m_LastHeartbeatMessage.fanToggledOn;
        this.heaterToggledOn = m_LastHeartbeatMessage.heaterToggledOn;
        this.servo0ManualOverride = m_LastHeartbeatMessage.servo0ManualOverride;
        this.fanManualOverride = m_LastHeartbeatMessage.fanManualOverride;
        this.heaterManualOverride = m_LastHeartbeatMessage.heaterManualOverride;
        this.servoTempLimit = m_LastHeartbeatMessage.servoTempLimit;
        this.servoHumidityLimit = m_LastHeartbeatMessage.servoHumidityLimit;
        this.fanTempLimit = m_LastHeartbeatMessage.fanTempLimit;
        this.fanHumidityLimit = m_LastHeartbeatMessage.fanHumidityLimit;
        this.heaterTempLimit = m_LastHeartbeatMessage.heaterTempLimit;
        this.heaterHumidityLimit = m_LastHeartbeatMessage.heaterHumidityLimit;
        this.lastLogErrCode = m_LastHeartbeatMessage.lastLogErrCode;
    }
    
    public boolean equals (podHeartbeatMessage compare)
    {
        return (
            this.timestampMs == compare.timestampMs &&
            this.podNumber == compare.podNumber &&
            this.lastLogCommandOn == compare.lastLogCommandOn &&
            this.actualLogStateOn == compare.actualLogStateOn &&
            this.lastCRecvSec == compare.lastCRecvSec &&
            this.lastERecvSec == compare.lastERecvSec &&
            this.voltage == compare.voltage &&
            this.voltageUpdatedSec == compare.voltageUpdatedSec &&
            this.temperature == compare.temperature &&
            this.temperatureUpdatedSec == compare.temperatureUpdatedSec &&
            this.humidity == compare.humidity &&
            this.humidityUpdatedSec == compare.humidityUpdatedSec &&
            this.servo0ToggledOpen == compare.servo0ToggledOpen &&
            this.lastServo0Duty == compare.lastServo0Duty &&
            this.fanToggledOn == compare.fanToggledOn &&
            this.heaterToggledOn == compare.heaterToggledOn &&
            this.servo0ManualOverride == compare.servo0ManualOverride &&
            this.fanManualOverride == compare.fanManualOverride &&
            this.heaterManualOverride == compare.heaterManualOverride && 
            this.servoTempLimit == compare.servoTempLimit && 
            this.servoHumidityLimit == compare.servoHumidityLimit && 
            this.fanTempLimit == compare.fanTempLimit && 
            this.fanHumidityLimit == compare.fanHumidityLimit && 
            this.heaterTempLimit == compare.heaterTempLimit && 
            this.heaterHumidityLimit == compare.heaterHumidityLimit && 
            this.lastLogErrCode == compare.lastLogErrCode
            );
    }

    
    @Override
    public void parseBioPodMessage(cbrnPodMsg m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        //pod data
        timestampMs = 1000*readDataUnsignedInt();
        podNumber = readDataByte();
        lastLogCommandOn = (readDataByte()==1);
        actualLogStateOn = (readDataByte()==1);
        lastCRecvSec = readDataUnsignedInt();
        lastERecvSec = readDataUnsignedInt();
        voltage = readDataByte()/ 8.0f;
        voltageUpdatedSec = readDataUnsignedInt();
        temperature = readDataUnsignedInt() / 1000.0f;
        temperatureUpdatedSec = readDataUnsignedInt();
        humidity =  readDataUnsignedInt() / 1000.0f;
        humidityUpdatedSec = readDataUnsignedInt();
        servo0ToggledOpen = readDataByte();
        lastServo0Duty = readDataByte ();
        fanToggledOn = readDataByte();
        heaterToggledOn = readDataByte();
        servo0ManualOverride = readDataBool();
        fanManualOverride = readDataBool();
        heaterManualOverride = readDataBool();
        servoTempLimit = readDataByte();
        servoHumidityLimit = readDataByte();
        fanTempLimit = readDataByte();
        fanHumidityLimit = readDataByte();
        heaterTempLimit = readDataByte();
        heaterHumidityLimit = readDataByte();
        lastLogErrCode = (short)readDataShort();
    }

    public String getLogMessage ()
    {
        return "" + timestampMs + "," +
        podNumber + "," +
        lastLogCommandOn + "," +
        actualLogStateOn + "," +
        lastCRecvSec + "," +
        lastERecvSec + "," +
        voltage + "," +
        voltageUpdatedSec + "," +
        temperature + "," +
        temperatureUpdatedSec + "," +
        humidity + "," +
        humidityUpdatedSec + "," +
        servo0ToggledOpen + "," +
        lastServo0Duty + "," +
        fanToggledOn + "," +
        heaterToggledOn + "," +
        servo0ManualOverride + "," +
        fanManualOverride + "," +
        heaterManualOverride + "," +
        servoTempLimit + "," +
        servoHumidityLimit + "," +
        fanTempLimit + "," +
        fanHumidityLimit + "," +
        heaterTempLimit + "," +
        heaterHumidityLimit + "," + 
        lastLogErrCode;
    }
    
    /**
     * @return Biopod number of this pod
     */
    public int getPodNumber () 
    {
        return podNumber;
    }
    
    /**
     * 
     * @param newNum Biopod number of this pod to set
     */
    public void setPodNumber (int newNum)
    {
        podNumber = newNum;
    }
    
    /**
     * @return Whether the last command to the board was to activate logging
     */
    public boolean getLastLogCommandOn () 
    {
        return lastLogCommandOn;
    }
    
    /**
     * 
     * @param newOn Whether the last command to the board was to activate logging
     */
    public void setLastLogCommandOn (boolean newOn)
    {
        lastLogCommandOn = newOn;
    }
    
    /**
     * @return Whether the board is logging
     */
    public boolean getActualLogStateOn () 
    {
        return actualLogStateOn;
    }
    
    /**
     * 
     * @param newOn Whether the board is logging
     */
    public void setActualLogStateOn (boolean newOn)
    {
        actualLogStateOn = newOn;
    }
    
    /**
     * @return the timestamp of last serial C recieve
     */
    public long getLastCRecvSec() {
        return lastCRecvSec;
    }

    /**
     * @param recv the timestamp of the last serial C receive
     */
    public void setLastCRecvSec(long recv) {
        this.lastCRecvSec = recv;
    }
    
    /**
     * @return the timestamp of last serial F recieve
     */
    public long getLastERecvSec() {
        return lastERecvSec;
    }

    /**
     * @param recv the timestamp of the last serial F receive
     */
    public void setLastERecvSec(long recv) {
        this.lastERecvSec = recv;
    }
    
    /**
     * 
     * @return Last recorded voltage
     */
    public float getVoltage ()
    {
        return voltage;
    }
    
    /**
     * 
     * @param newVal New value for voltage
     */
    public void setVoltage (float newVal)
    {
        voltage = newVal;
    }
    
    /**
     * @return the timestamp of last voltage update
     */
    public long getVoltageUpdatedSec() {
        return voltageUpdatedSec;
    }

    /**
     * @param recv the timestamp of last voltage update
     */
    public void setVoltageUpdatedSec(long recv) {
        this.voltageUpdatedSec = recv;
    }
    
    /**
     * 
     * @return Last recorded temperature
     */
    public float getTemperature ()
    {
        return temperature;
    }
    
    /**
     * 
     * @param newVal New value for temperature
     */
    public void setTemperature (float newVal)
    {
        temperature = newVal;
    }
    
    /**
     * @return the timestamp of last temperature update
     */
    public long getTemperatureUpdatedSec() {
        return temperatureUpdatedSec;
    }

    /**
     * @param recv the timestamp of last temperature update
     */
    public void setTemperatureUpdatedSec(long recv) {
        this.temperatureUpdatedSec = recv;
    }
    
    /**
     * 
     * @return Last recorded humidity
     */
    public float getHumidity ()
    {
        return humidity;
    }
    
    /**
     * 
     * @param newVal New value for humidity
     */
    public void setHumidity (float newVal)
    {
        humidity = newVal;
    }
    
    /**
     * @return the timestamp of last humidity update
     */
    public long getHumidityUpdatedSec() {
        return humidityUpdatedSec;
    }

    /**
     * @param recv the timestamp of last humidity update
     */
    public void setHumidityUpdatedSec(long recv) {
        this.humidityUpdatedSec = recv;
    }
    
    /**
     * @return Status of servo control
     */
    public int getServo0ToggledOpen ()
    {
        return servo0ToggledOpen;
    }
    
    /**
     * @param newVal New value for status
     */
    public void setServo0ToggledOpen (int newVal)
    {
        servo0ToggledOpen = newVal;
    }
    
    /**
     * @return Last duty cycle sent to servo
     */
    public int getLastServo0Duty ()
    {
        return lastServo0Duty;
    }
    
    /**
     * @param newVal New value for Last duty cycle sent to servo
     */
    public void setLastServo0Duty (int newVal)
    {
        lastServo0Duty = newVal;
    }
    
    /**
     * @return Status of fan control
     */
    public int getFanToggledOn ()
    {
        return fanToggledOn;
    }
    
    /**
     * @param newVal New value for status
     */
    public void setFanToggledOn (int newVal)
    {
        fanToggledOn = newVal;
    }
    
    /**
     * @return Status of heater control
     */
    public int getHeaterToggledOn ()
    {
        return heaterToggledOn;
    }
    
    /**
     * @param newVal New value for status
     */
    public void setHeaterToggledOn (int newVal)
    {
        heaterToggledOn = newVal;
    }
    
    /**
     * @return True if user is overriding auto servo control
     */
    public boolean getServo0ManualOverride ()
    {
        return servo0ManualOverride;
    }
    
    /**
     * @param override If user is overriding auto servo control
     */
    public void setServo0ManualOverride (boolean override)
    {
        servo0ManualOverride = override;
    }
    
    /**
     * @return True if user is overriding auto fan control
     */
    public boolean getFanManualOverride ()
    {
        return fanManualOverride;
    }
    
    /**
     * @param override If user is overriding auto fan control
     */
    public void setFanManualOverride (boolean override)
    {
        fanManualOverride = override;
    }
    
    /**
     * @return True if user is overriding auto heater control
     */
    public boolean getHeaterManualOverride ()
    {
        return heaterManualOverride;
    }
    
    /**
     * @param override If user is overriding auto heater control
     */
    public void setHeaterManualOverride (boolean override)
    {
        heaterManualOverride = override;
    }
    
    /**
     * @return servo temperature limit
     */
    public int getServoTempLimit ()
    {
        return servoTempLimit;
    }
    
    /**
     * @param newLimit servo temperature limit
     */
    public void setServoTempLimit (int newLimit)
    {
        servoTempLimit = newLimit;
    }
    
    /**
     * @return servo humidity limit
     */
    public int getServoHumidityLimit ()
    {
        return servoHumidityLimit;
    }
    
    /**
     * @param newLimit servo humidity limit
     */
    public void setServoHumidityLimit (int newLimit)
    {
        servoHumidityLimit = newLimit;
    }
    
    /**
     * @return fan temperature limit
     */
    public int getFanTempLimit ()
    {
        return fanTempLimit;
    }
    
    /**
     * @param newLimit fan temperature limit
     */
    public void setFanTempLimit (int newLimit)
    {
        fanTempLimit = newLimit;
    }
    
    /**
     * @return fan humidity limit
     */
    public int getFanHumidityLimit ()
    {
        return fanHumidityLimit;
    }
    
    /**
     * @param newLimit fan humidity limit
     */
    public void setFanHumidityLimit (int newLimit)
    {
        fanHumidityLimit = newLimit;
    }
    
    /**
     * @return heater temperature limit
     */
    public int getHeaterTempLimit ()
    {
        return heaterTempLimit;
    }
    
    /**
     * @param newLimit heater temperature limit
     */
    public void setHeaterTempLimit (int newLimit)
    {
        heaterTempLimit = newLimit;
    }
    
    /**
     * @return heater humidity limit
     */
    public int getHeaterHumidityLimit ()
    {
        return heaterHumidityLimit;
    }
    
    /**
     * @param newLimit heater humidity limit
     */
    public void setHeaterHumidityLimit (int newLimit)
    {
        heaterHumidityLimit = newLimit;
    }
    
    /**
     * @return Last error code for loggin on board
     */
    public short getLastLogErrCode ()
    {
        return lastLogErrCode;
    }
    
    /**
     * @param newCode New error code for loggin on board
     */
    public void setLastLogErrCode (short newCode)
    {
        lastLogErrCode = newCode;
    }
    
    public String getTHControlString ()
    {
        String retVal = "";
        if (getFanManualOverride() || getHeaterManualOverride() || getServo0ManualOverride())
            retVal += "Manual ";
        else
            retVal += "Auto ";
        
        if (getFanToggledOn()==1 && getServo0ToggledOpen()==1)
            retVal += "Cooling";
        else if (getHeaterToggledOn()==1)
            retVal += "Heating";
        else if (getFanToggledOn()==0 && getServo0ToggledOpen()==0 && getHeaterToggledOn()==0)
            retVal += "Stable";
        else
            retVal += "Custom";
        
        return retVal;
    }
}
