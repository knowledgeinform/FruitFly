/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.biopod.messages;

/**
 * The device doesnt tell you when alarms are cleared.
 * I have a time of last alarm received, and this can be subtracted from the
 * message timestamp to get the time since last alarm
 *
 * this is a combination of $diagnositcs message, as well as data from $fault messages
 * receiving either one triggers the entire message to be sent
 * @author southmk1
 */
public class DiagnosticsMessage extends BioPodMessage {
    //tiemstamp of message
    private long timestamp;

    //timestamp of last pressure fault
    private long LastPressureFault;
    //pressure reading at the time of fault *10
    private int PressureAtFault;
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
    private int OutletPressure;
    private boolean PressureAlarm;

    //.1f (10*) -20-90
    private int Temperature;
    private boolean TemperatureAlarm;

    //int 0-800
    private int LaserPowerMonitor;
    private boolean LaserPowerAlarm;

    //.1f (10*) 0-80
    private int LaserCurrentMonitor;
    private boolean LaserCurrentMonitorAlarm;

    //.2f (100*) 0-5
    private int BackgroundMonitor;
    private boolean BackgroundAlarm;

    //.1f (10*) 0-35
    private int InputVoltage;
    private boolean InputVoltageAlarm;

    //int 0-2000
    private int InputCurrent;
    private boolean InputCurrentAlarm;

    public DiagnosticsMessage() {
        super(BioPodMessage.BIO_POD_DIAGNOSTICS_MSG, HeaderSize + ChecksumSize);
    }

    @Override
    public void parseBioPodMessage(BioPodMessage m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        //ibac data
        timestamp = readDataUnsignedInt();
        LastPressureFault = readDataUnsignedInt();
        PressureAtFault = readDataByte();
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
            sb.append((char) readDataByte());
        }
        StatusAndVersion = sb.toString();

        //diagnositcs data

        DiagnosticsTimeStamp = readDataUnsignedInt();
        OutletPressure = readDataByte();
        PressureAlarm = readDataBool();
        Temperature = readDataShort();
        TemperatureAlarm = readDataBool();
        LaserPowerMonitor = readDataShort();
        LaserPowerAlarm = readDataBool();
        LaserCurrentMonitor = readDataShort();
        LaserCurrentMonitorAlarm = readDataBool();
        BackgroundMonitor = readDataShort();
        BackgroundAlarm = readDataBool();
        InputVoltage = readDataShort();
        InputVoltageAlarm = readDataBool();
        InputCurrent = readDataShort();
        InputCurrentAlarm = readDataBool();

    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
    public int getPressureAtFault() {
        return PressureAtFault;
    }

    /**
     * @param PressureAtFault the PressureAtFault to set
     */
    public void setPressureAtFault(int PressureAtFault) {
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
    public int getOutletPressure() {
        return OutletPressure;
    }

    /**
     * @param OutletPressure the OutletPressure to set
     */
    public void setOutletPressure(int OutletPressure) {
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
    public int getTemperature() {
        return Temperature;
    }

    /**
     * @param Temperature the Temperature to set
     */
    public void setTemperature(int Temperature) {
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
    public int getLaserCurrentMonitor() {
        return LaserCurrentMonitor;
    }

    /**
     * @param LaserCurrentMonitor the LaserCurrentMonitor to set
     */
    public void setLaserCurrentMonitor(int LaserCurrentMonitor) {
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
    public int getBackgroundMonitor() {
        return BackgroundMonitor;
    }

    /**
     * @param BackgroundMonitor the BackgroundMonitor to set
     */
    public void setBackgroundMonitor(int BackgroundMonitor) {
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
    public int getInputVoltage() {
        return InputVoltage;
    }

    /**
     * @param InputVoltage the InputVoltage to set
     */
    public void setInputVoltage(int InputVoltage) {
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
