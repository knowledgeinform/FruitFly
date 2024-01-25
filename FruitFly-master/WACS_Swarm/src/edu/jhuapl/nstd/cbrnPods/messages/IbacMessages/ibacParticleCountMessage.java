package edu.jhuapl.nstd.cbrnPods.messages.IbacMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.swarm.util.Config;

/**
 *  This is a combination of $baseline data and $trace data
 * from last messages received. receiving either triggers the sending of this
 *
 * @author southmk1
 */
public class ibacParticleCountMessage extends cbrnPodMsg
{
    //timestamp of last $trace data received
    private long particleCountCollectionTime;

    /*
     * %d = can use straight
     * %.1f = divide by 10 to get float
     * %.2f = divide by 100 etc.
     *
     * longs were used where unsigned int values might truncate values
     * */
    //A Small instantaneous particle counts (C-S-I) %d 0-50000
    private int CSI;
    //B Large instantaneous particle counts (C-L-I) %d 0-50000
    private int CLI;
    //C Small instantaneous fluorescent particle counts (BC-S-I) %d 0-50000
    private int BCSI;
    //D Large instantaneous fluorescent particle counts (BC-L-I) %d 0-50000
    private int BCLI;
    //E Small MA particle counts (C-S-A) %.1f 0-50000
    private float CSA;
    //F Large MA particle counts (C-L-A) %.1f 0-50000
    private float CLA;
    //G Small MA fluorescent particle counts (BC-S-A) %.1f 0-50000
    private float BCSA;
    //H Large MA fluorescent particle counts (BC-L-A) %.1f 0-50000
    private float BCLA;
    //I Small MA biological percent (B%-S-A) %.1f 0-100
    private float BpSA;
    // J Large MA biological percent (B%-L-A) %.1f 0-100
    private float BpLA;
    //K Instantaneous size fraction percent (SF-I) %.1f 0-100
    private float SFI;
    // L MA size fraction percent (SF-A) %.1f 0-100
    private float SFA;
    //M Alarm state counter %d 0-32767
    private int AlarmCounter;
    //N Valid baseline data? %d 0-1
    private boolean ValidBaseline;
    //O Alarm status %d 0-1
    private boolean AlarmStatus;
    //P Alarm latch state %d 0-1
    private boolean AlarmLatchState;
    private /*
     * typedef struct baseline_data{
    unsigned long TimeStamp;
    unsigned long BCLABaseline;
    unsigned int BpLABaseline;
    unsigned int SizeFractionBaseline;
    }BaselineData;
     * */

    //timestamp of last baseline message received
    long baselineDataTime;

    //A 	Large MA fluorescent particle counts baseline 	%.1f 	0-50000
    private float BCLABaseline;
    //b  	Large MA biological percentage baseline 	%.1f 	0-100
    private float BpLABaseline;
    //C 	Size fraction baseline 	%.1f 	0-100
    private float SizeFractionBaseline;

    public ibacParticleCountMessage()
    {
        super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodMsg.IBAC_PARTICLE_COUNT_TYPE, 0);
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);
        timestampMs = 1000*readDataUnsignedInt();

        //particle data
        particleCountCollectionTime = readDataUnsignedInt();
        CSI = readDataShort();
        CLI = readDataShort();
        BCSI = readDataShort();
        BCLI = readDataShort();
        CSA = readDataUnsignedInt()/10.0f;
        CLA = readDataUnsignedInt()/10.0f;
        BCSA = readDataUnsignedInt()/10.0f;
        BCLA = readDataUnsignedInt()/10.0f;
        BpSA = readDataShort()/10.0f;
        BpLA = readDataShort()/10.0f;
        SFI = readDataShort()/10.0f;
        SFA = readDataShort()/10.0f;
        AlarmCounter = readDataShort();
        ValidBaseline = readDataBool();
        AlarmStatus = readDataBool();
        AlarmLatchState = readDataBool();

        //baseline data
        baselineDataTime = readDataUnsignedInt();
        BCLABaseline = readDataUnsignedInt()/10.0f;
        BpLABaseline = readDataShort()/10.0f;
        SizeFractionBaseline = readDataShort()/10.0f;
    }

    /**
     * @return the particleCountCollectionTime
     */
    public long getParticleCountCollectionTime() {
        return particleCountCollectionTime;
    }

    /**
     * @param particleCountCollectionTime the particleCountCollectionTime to set
     */
    public void setParticleCountCollectionTime(long particleCountCollectionTime) {
        this.particleCountCollectionTime = particleCountCollectionTime;
    }

    /**
     * @return the CSI
     */
    public int getCSI() {
        return CSI;
    }

    /**
     * @param CSI the CSI to set
     */
    public void setCSI(int CSI) {
        this.CSI = CSI;
    }

    /**
     * @return the CLI
     */
    public int getCLI() {
        return CLI;
    }

    /**
     * @param CLI the CLI to set
     */
    public void setCLI(int CLI) {
        this.CLI = CLI;
    }

    /**
     * @return the BCSI
     */
    public int getBCSI() {
        return BCSI;
    }

    /**
     * @param BCSI the BCSI to set
     */
    public void setBCSI(int BCSI) {
        this.BCSI = BCSI;
    }

    /**
     * @return the BCLI
     */
    public int getBCLI() {
        return BCLI;
    }

    /**
     * @param BCLI the BCLI to set
     */
    public void setBCLI(int BCLI) {
        this.BCLI = BCLI;
    }

    /**
     * @return the CSA
     */
    public float getCSA() {
        return CSA;
    }

    /**
     * @param CSA the CSA to set
     */
    public void setCSA(float CSA) {
        this.CSA = CSA;
    }

    /**
     * @return the CLA
     */
    public float getCLA() {
        return CLA;
    }

    /**
     * @param CLA the CLA to set
     */
    public void setCLA(float CLA) {
        this.CLA = CLA;
    }

    /**
     * @return the BCSA
     */
    public float getBCSA() {
        return BCSA;
    }

    /**
     * @param BCSA the BCSA to set
     */
    public void setBCSA(float BCSA) {
        this.BCSA = BCSA;
    }

    /**
     * @return the BCLA
     */
    public float getBCLA() {
        return BCLA;
    }

    /**
     * @param BCLA the BCLA to set
     */
    public void setBCLA(float BCLA) {
        this.BCLA = BCLA;
    }

    /**
     * @return the BpSA
     */
    public float getBpSA() {
        return BpSA;
    }

    /**
     * @param BpSA the BpSA to set
     */
    public void setBpSA(float BpSA) {
        this.BpSA = BpSA;
    }

    /**
     * @return the BpLA
     */
    public float getBpLA() {
        return BpLA;
    }

    /**
     * @param BpLA the BpLA to set
     */
    public void setBpLA(float BpLA) {
        this.BpLA = BpLA;
    }

    /**
     * @return the SFI
     */
    public float getSFI() {
        return SFI;
    }

    /**
     * @param SFI the SFI to set
     */
    public void setSFI(float SFI) {
        this.SFI = SFI;
    }

    /**
     * @return the SFA
     */
    public float getSFA() {
        return SFA;
    }

    /**
     * @param SFA the SFA to set
     */
    public void setSFA(float SFA) {
        this.SFA = SFA;
    }

    /**
     * @return the AlarmCounter
     */
    public int getAlarmCounter() {
        return AlarmCounter;
    }

    /**
     * @param AlarmCounter the AlarmCounter to set
     */
    public void setAlarmCounter(int AlarmCounter) {
        this.AlarmCounter = AlarmCounter;
    }

    /**
     * @return the ValidBaseline
     */
    public boolean isValidBaseline() {
        return ValidBaseline;
    }

    /**
     * @param ValidBaseline the ValidBaseline to set
     */
    public void setValidBaseline(boolean ValidBaseline) {
        this.ValidBaseline = ValidBaseline;
    }

    /**
     * @return the AlarmStatus
     */
    public boolean isAlarmStatus() {
        return AlarmStatus;
    }

    /**
     * @param AlarmStatus the AlarmStatus to set
     */
    public void setAlarmStatus(boolean AlarmStatus) {
        this.AlarmStatus = AlarmStatus;
    }

    /**
     * @return the AlarmLatchState
     */
    public boolean isAlarmLatchState() {
        return AlarmLatchState;
    }

    /**
     * @param AlarmLatchState the AlarmLatchState to set
     */
    public void setAlarmLatchState(boolean AlarmLatchState) {
        this.AlarmLatchState = AlarmLatchState;
    }

    /**
     * @return the baselineDataTime
     */
    public long getBaselineDataTime() {
        return baselineDataTime;
    }

    /**
     * @param baselineDataTime the baselineDataTime to set
     */
    public void setBaselineDataTime(long baselineDataTime) {
        this.baselineDataTime = baselineDataTime;
    }

    /**
     * @return the BCLABaseline
     */
    public float getBCLABaseline() {
        return BCLABaseline;
    }

    /**
     * @param BCLABaseline the BCLABaseline to set
     */
    public void setBCLABaseline(float BCLABaseline) {
        this.BCLABaseline = BCLABaseline;
    }

    /**
     * @return the BpLABaseline
     */
    public float getBpLABaseline() {
        return BpLABaseline;
    }

    /**
     * @param BpLABaseline the BpLABaseline to set
     */
    public void setBpLABaseline(float BpLABaseline) {
        this.BpLABaseline = BpLABaseline;
    }

    /**
     * @return the SizeFractionBaseline
     */
    public float getSizeFractionBaseline() {
        return SizeFractionBaseline;
    }

    /**
     * @param SizeFractionBaseline the SizeFractionBaseline to set
     */
    public void setSizeFractionBaseline(float SizeFractionBaseline) {
        this.SizeFractionBaseline = SizeFractionBaseline;
    }

    public boolean getDetection(double threshold)
    {
        if ( CSI + CLI > threshold)
            return true;
        else
            return false;
    }


    public double getHitStrength()
    {
        return CSI + CLI;
    }
}
