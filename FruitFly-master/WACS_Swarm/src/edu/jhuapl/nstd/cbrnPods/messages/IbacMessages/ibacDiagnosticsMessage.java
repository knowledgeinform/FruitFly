package edu.jhuapl.nstd.cbrnPods.messages.IbacMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 * The device doesnt tell you when alarms are cleared.
 * I have a time of last alarm received, and this can be subtracted from the
 * message timestamp to get the time since last alarm
 *
 * this is a combination of $diagnositcs message, as well as data from $fault messages
 * receiving either one triggers the entire message to be sent
 * @author southmk1
 */
public class ibacDiagnosticsMessage extends cbrnPodMsg 
{
    //timestamp of last pressure fault
    private long LastPressureFault;
    //pressure reading at the time of fault *10
    private float PressureAtFault;
    //timestamp
    private long LaserPowerLowFault;
    //timestamp
    private long LaserPowerAboveFault;
    //timestamp
    private long LaserCurrentOutFault;
    //current at startup (int)
    private int LaserInitialCurrent;
    //current laser current (that caused fault) (int)
    private int LaserCurrentCurrent;

    //timestamp
    private long BackgroundLghtBelowFault;
    //timestamp
    private long LastCollectingSample;
    //timestamp
    private long LastUnitAlarm;

    private boolean IsSystemReady;
    private boolean CollectionDiskSpinning;

    //system responded to a $sleep command. cleared when you send any command,
    //but only in microcontroller. see sleep command comments for details
    private boolean Sleeping;

    //string response from $s command
    private String StatusAndVersion;

    //timestamp of last diagnostics message from ibac
    private long DiagnosticsTimeStamp;

    /*
     * For the following, the precision is after conversion
     * as is the data range.
     *
     * EX: .1f (*10) 0-5 means to convert
     * divide int value by 10, and the range should be 0-5
     * */

    //.1f (10*) 0-5
    private float OutletPressure;
    private boolean PressureAlarm;

    //.1f (10*) -20-90
    private float Temperature;
    private boolean TemperatureAlarm;

    //int 0-800
    private int LaserPowerMonitor;
    private boolean LaserPowerAlarm;

    //.1f (10*) 0-80
    private float LaserCurrentMonitor;
    private boolean LaserCurrentMonitorAlarm;

    //.2f (100*) 0-5
    private float BackgroundMonitor;
    private boolean BackgroundAlarm;

    //.1f (10*) 0-35
    private float InputVoltage;
    private boolean InputVoltageAlarm;

    //int 0-2000
    private int InputCurrent;
    private boolean InputCurrentAlarm;

    public ibacDiagnosticsMessage() 
    {
        super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodMsg.IBAC_DIAGNOSTICS_TYPE, 0);
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        //ibac data
        timestampMs = 1000*readDataUnsignedInt();
        LastPressureFault = readDataUnsignedInt();
        PressureAtFault = readDataByte()/10.0f;
        LaserPowerLowFault = readDataUnsignedInt();
        LaserPowerAboveFault = readDataUnsignedInt();
        LaserCurrentOutFault = readDataUnsignedInt();
        LaserInitialCurrent = readDataByte();
        LaserCurrentCurrent = readDataByte();
        BackgroundLghtBelowFault = readDataUnsignedInt();
        LastCollectingSample = readDataUnsignedInt();
        LastUnitAlarm = readDataUnsignedInt();
        IsSystemReady = readDataBool();
        CollectionDiskSpinning = readDataBool();
        Sleeping = readDataBool();
        StringBuilder sb = new StringBuilder(128);
        for (int i = 0; i < 128; i++) {
            char newByte = (char) readDataByte();
            if (newByte != 0)
                sb.append(newByte);
        }
        StatusAndVersion = sb.toString();

        //diagnositcs data

        DiagnosticsTimeStamp = readDataUnsignedInt();
        OutletPressure = readDataByte()/10.0f;
        PressureAlarm = readDataBool();
        Temperature = readDataShort()/10.0f;
        TemperatureAlarm = readDataBool();
        LaserPowerMonitor = readDataShort();
        LaserPowerAlarm = readDataBool();
        LaserCurrentMonitor = readDataShort()/10.0f;
        LaserCurrentMonitorAlarm = readDataBool();
        BackgroundMonitor = readDataShort()/100.0f;
        BackgroundAlarm = readDataBool();
        InputVoltage = readDataShort()/10.0f;
        InputVoltageAlarm = readDataBool();
        InputCurrent = readDataShort();
        InputCurrentAlarm = readDataBool();

    }

    /**
     * @return the LastPressureFault
     */
    public long getLastPressureFault() {
        return LastPressureFault;
    }

    /**
     * @param LastPressureFault the LastPressureFault to set
     */
    public void setLastPressureFault(long LastPressureFault) {
        this.LastPressureFault = LastPressureFault;
    }

    /**
     * @return the PressureAtFault
     */
    public float getPressureAtFault() {
        return PressureAtFault;
    }

    /**
     * @param PressureAtFault the PressureAtFault to set
     */
    public void setPressureAtFault(float PressureAtFault) {
        this.PressureAtFault = PressureAtFault;
    }

    /**
     * @return the LaserPowerLowFault
     */
    public long getLaserPowerLowFault() {
        return LaserPowerLowFault;
    }

    /**
     * @param LaserPowerLowFault the LaserPowerLowFault to set
     */
    public void setLaserPowerLowFault(long LaserPowerLowFault) {
        this.LaserPowerLowFault = LaserPowerLowFault;
    }

    /**
     * @return the LaserPowerAboveFault
     */
    public long getLaserPowerAboveFault() {
        return LaserPowerAboveFault;
    }

    /**
     * @param LaserPowerAboveFault the LaserPowerAboveFault to set
     */
    public void setLaserPowerAboveFault(long LaserPowerAboveFault) {
        this.LaserPowerAboveFault = LaserPowerAboveFault;
    }

    /**
     * @return the LaserCurrentOutFault
     */
    public long getLaserCurrentOutFault() {
        return LaserCurrentOutFault;
    }

    /**
     * @param LaserCurrentOutFault the LaserCurrentOutFault to set
     */
    public void setLaserCurrentOutFault(long LaserCurrentOutFault) {
        this.LaserCurrentOutFault = LaserCurrentOutFault;
    }

    /**
     * @return the LaserInitialCurrent
     */
    public int getLaserInitialCurrent() {
        return LaserInitialCurrent;
    }

    /**
     * @param LaserInitialCurrent the LaserInitialCurrent to set
     */
    public void setLaserInitialCurrent(int LaserInitialCurrent) {
        this.LaserInitialCurrent = LaserInitialCurrent;
    }

    /**
     * @return the LaserCurrentCurrent
     */
    public int getLaserCurrentCurrent() {
        return LaserCurrentCurrent;
    }

    /**
     * @param LaserCurrentCurrent the LaserCurrentCurrent to set
     */
    public void setLaserCurrentCurrent(int LaserCurrentCurrent) {
        this.LaserCurrentCurrent = LaserCurrentCurrent;
    }

    /**
     * @return the BackgroundLghtBelowFault
     */
    public long getBackgroundLghtBelowFault() {
        return BackgroundLghtBelowFault;
    }

    /**
     * @param BackgroundLghtBelowFault the BackgroundLghtBelowFault to set
     */
    public void setBackgroundLghtBelowFault(long BackgroundLghtBelowFault) {
        this.BackgroundLghtBelowFault = BackgroundLghtBelowFault;
    }

    /**
     * @return the LastCollectingSample
     */
    public long getLastCollectingSample() {
        return LastCollectingSample;
    }

    /**
     * @param LastCollectingSample the LastCollectingSample to set
     */
    public void setLastCollectingSample(long LastCollectingSample) {
        this.LastCollectingSample = LastCollectingSample;
    }

    /**
     * @return the LastUnitAlarm
     */
    public long getLastUnitAlarm() {
        return LastUnitAlarm;
    }

    /**
     * @param LastUnitAlarm the LastUnitAlarm to set
     */
    public void setLastUnitAlarm(long LastUnitAlarm) {
        this.LastUnitAlarm = LastUnitAlarm;
    }

    /**
     * @return the IsSystemReady
     */
    public boolean isIsSystemReady() {
        return IsSystemReady;
    }

    /**
     * @param IsSystemReady the IsSystemReady to set
     */
    public void setIsSystemReady(boolean IsSystemReady) {
        this.IsSystemReady = IsSystemReady;
    }

    /**
     * @return the CollectionDiskSpinning
     */
    public boolean isCollectionDiskSpinning() {
        return CollectionDiskSpinning;
    }

    /**
     * @param CollectionDiskSpinning the CollectionDiskSpinning to set
     */
    public void setCollectionDiskSpinning(boolean CollectionDiskSpinning) {
        this.CollectionDiskSpinning = CollectionDiskSpinning;
    }

    /**
     * @return the Sleeping
     */
    public boolean isSleeping() {
        return Sleeping;
    }

    /**
     * @param Sleeping the Sleeping to set
     */
    public void setSleeping(boolean Sleeping) {
        this.Sleeping = Sleeping;
    }

    /**
     * @return the StatusAndVersion
     */
    public String getStatusAndVersion() {
        return StatusAndVersion;
    }

    /**
     * @param StatusAndVersion the StatusAndVersion to set
     */
    public void setStatusAndVersion(String StatusAndVersion) {
        this.StatusAndVersion = StatusAndVersion;
    }

    /**
     * @return the DiagnosticsTimeStamp
     */
    public long getDiagnosticsTimeStamp() {
        return DiagnosticsTimeStamp;
    }

    /**
     * @param DiagnosticsTimeStamp the DiagnosticsTimeStamp to set
     */
    public void setDiagnosticsTimeStamp(long DiagnosticsTimeStamp) {
        this.DiagnosticsTimeStamp = DiagnosticsTimeStamp;
    }

    /**
     * @return the OutletPressure
     */
    public float getOutletPressure() {
        return OutletPressure;
    }

    /**
     * @param OutletPressure the OutletPressure to set
     */
    public void setOutletPressure(float OutletPressure) {
        this.OutletPressure = OutletPressure;
    }

    /**
     * @return the PressureAlarm
     */
    public boolean isPressureAlarm() {
        return PressureAlarm;
    }

    /**
     * @param PressureAlarm the PressureAlarm to set
     */
    public void setPressureAlarm(boolean PressureAlarm) {
        this.PressureAlarm = PressureAlarm;
    }

    /**
     * @return the Temperature
     */
    public float getTemperature() {
        return Temperature;
    }

    /**
     * @param Temperature the Temperature to set
     */
    public void setTemperature(float Temperature) {
        this.Temperature = Temperature;
    }

    /**
     * @return the TemperatureAlarm
     */
    public boolean isTemperatureAlarm() {
        return TemperatureAlarm;
    }

    /**
     * @param TemperatureAlarm the TemperatureAlarm to set
     */
    public void setTemperatureAlarm(boolean TemperatureAlarm) {
        this.TemperatureAlarm = TemperatureAlarm;
    }

    /**
     * @return the LaserPowerMonitor
     */
    public int getLaserPowerMonitor() {
        return LaserPowerMonitor;
    }

    /**
     * @param LaserPowerMonitor the LaserPowerMonitor to set
     */
    public void setLaserPowerMonitor(int LaserPowerMonitor) {
        this.LaserPowerMonitor = LaserPowerMonitor;
    }

    /**
     * @return the LaserPowerAlarm
     */
    public boolean isLaserPowerAlarm() {
        return LaserPowerAlarm;
    }

    /**
     * @param LaserPowerAlarm the LaserPowerAlarm to set
     */
    public void setLaserPowerAlarm(boolean LaserPowerAlarm) {
        this.LaserPowerAlarm = LaserPowerAlarm;
    }

    /**
     * @return the LaserCurrentMonitor
     */
    public float getLaserCurrentMonitor() {
        return LaserCurrentMonitor;
    }

    /**
     * @param LaserCurrentMonitor the LaserCurrentMonitor to set
     */
    public void setLaserCurrentMonitor(float LaserCurrentMonitor) {
        this.LaserCurrentMonitor = LaserCurrentMonitor;
    }

    /**
     * @return the LaserCurrentMonitorAlarm
     */
    public boolean isLaserCurrentMonitorAlarm() {
        return LaserCurrentMonitorAlarm;
    }

    /**
     * @param LaserCurrentMonitorAlarm the LaserCurrentMonitorAlarm to set
     */
    public void setLaserCurrentMonitorAlarm(boolean LaserCurrentMonitorAlarm) {
        this.LaserCurrentMonitorAlarm = LaserCurrentMonitorAlarm;
    }

    /**
     * @return the BackgroundMonitor
     */
    public float getBackgroundMonitor() {
        return BackgroundMonitor;
    }

    /**
     * @param BackgroundMonitor the BackgroundMonitor to set
     */
    public void setBackgroundMonitor(float BackgroundMonitor) {
        this.BackgroundMonitor = BackgroundMonitor;
    }

    /**
     * @return the BackgroundAlarm
     */
    public boolean isBackgroundAlarm() {
        return BackgroundAlarm;
    }

    /**
     * @param BackgroundAlarm the BackgroundAlarm to set
     */
    public void setBackgroundAlarm(boolean BackgroundAlarm) {
        this.BackgroundAlarm = BackgroundAlarm;
    }

    /**
     * @return the InputVoltage
     */
    public float getInputVoltage() {
        return InputVoltage;
    }

    /**
     * @param InputVoltage the InputVoltage to set
     */
    public void setInputVoltage(float InputVoltage) {
        this.InputVoltage = InputVoltage;
    }

    /**
     * @return the InputVoltageAlarm
     */
    public boolean isInputVoltageAlarm() {
        return InputVoltageAlarm;
    }

    /**
     * @param InputVoltageAlarm the InputVoltageAlarm to set
     */
    public void setInputVoltageAlarm(boolean InputVoltageAlarm) {
        this.InputVoltageAlarm = InputVoltageAlarm;
    }

    /**
     * @return the InputCurrent
     */
    public int getInputCurrent() {
        return InputCurrent;
    }

    /**
     * @param InputCurrent the InputCurrent to set
     */
    public void setInputCurrent(int InputCurrent) {
        this.InputCurrent = InputCurrent;
    }

    /**
     * @return the InputCurrentAlarm
     */
    public boolean isInputCurrentAlarm() {
        return InputCurrentAlarm;
    }

    /**
     * @param InputCurrentAlarm the InputCurrentAlarm to set
     */
    public void setInputCurrentAlarm(boolean InputCurrentAlarm) {
        this.InputCurrentAlarm = InputCurrentAlarm;
    }
}
