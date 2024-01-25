/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.biopod.messages;

import edu.jhuapl.nstd.swarm.util.Config;

/**
 *  This is a combination of $baseline data and $trace data
 * from last messages received. receiving either triggers the sending of this
 *
 * @author southmk1
 */
public class ParticleCountMessage extends BioPodMessage {

    //timestamp of message
    private long timestamp;

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
    private long CSA;
    //F Large MA particle counts (C-L-A) %.1f 0-50000
    private long CLA;
    //G Small MA fluorescent particle counts (BC-S-A) %.1f 0-50000
    private long BCSA;
    //H Large MA fluorescent particle counts (BC-L-A) %.1f 0-50000
    private long BCLA;
    //I Small MA biological percent (B%-S-A) %.1f 0-100
    private int BpSA;
    // J Large MA biological percent (B%-L-A) %.1f 0-100
    private int BpLA;
    //K Instantaneous size fraction percent (SF-I) %.1f 0-100
    private int SFI;
    // L MA size fraction percent (SF-A) %.1f 0-100
    private int SFA;
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
    private long BCLABaseline;
    //b  	Large MA biological percentage baseline 	%.1f 	0-100
    private int BpLABaseline;
    //C 	Size fraction baseline 	%.1f 	0-100
    private int SizeFractionBaseline;

    private int  _detectionthresh;

    public ParticleCountMessage()
    {
        super(BioPodMessage.BIO_POD_PARTICLE_COUNT_MSG, HeaderSize + ChecksumSize);
        _detectionthresh = Config.getConfig().getPropertyAsInteger("podAction.particledetection.detectionthresh", 500);
    }

    @Override
    public void parseBioPodMessage(BioPodMessage m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);
        timestamp = readDataUnsignedInt();

        //particle data
        particleCountCollectionTime = readDataUnsignedInt();
        CSI = readDataShort();
        CLI = readDataShort();
        BCSI = readDataShort();
        BCLI = readDataShort();
        CSA = readDataUnsignedInt();
        CLA = readDataUnsignedInt();
        BCSA = readDataUnsignedInt();
        BCLA = readDataUnsignedInt();
        BpSA = readDataShort();
        BpLA = readDataShort();
        SFI = readDataShort();
        SFA = readDataShort();
        AlarmCounter = readDataShort();
        ValidBaseline = readDataBool();
        AlarmStatus = readDataBool();
        AlarmLatchState = readDataBool();

        //baseline data
        baselineDataTime = readDataUnsignedInt();
        BCLABaseline = readDataUnsignedInt();
        BpLABaseline = readDataShort();
        SizeFractionBaseline = readDataShort();
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
    public long getCSA() {
        return CSA;
    }

    /**
     * @param CSA the CSA to set
     */
    public void setCSA(long CSA) {
        this.CSA = CSA;
    }

    /**
     * @return the CLA
     */
    public long getCLA() {
        return CLA;
    }

    /**
     * @param CLA the CLA to set
     */
    public void setCLA(long CLA) {
        this.CLA = CLA;
    }

    /**
     * @return the BCSA
     */
    public long getBCSA() {
        return BCSA;
    }

    /**
     * @param BCSA the BCSA to set
     */
    public void setBCSA(long BCSA) {
        this.BCSA = BCSA;
    }

    /**
     * @return the BCLA
     */
    public long getBCLA() {
        return BCLA;
    }

    /**
     * @param BCLA the BCLA to set
     */
    public void setBCLA(long BCLA) {
        this.BCLA = BCLA;
    }

    /**
     * @return the BpSA
     */
    public int getBpSA() {
        return BpSA;
    }

    /**
     * @param BpSA the BpSA to set
     */
    public void setBpSA(int BpSA) {
        this.BpSA = BpSA;
    }

    /**
     * @return the BpLA
     */
    public int getBpLA() {
        return BpLA;
    }

    /**
     * @param BpLA the BpLA to set
     */
    public void setBpLA(int BpLA) {
        this.BpLA = BpLA;
    }

    /**
     * @return the SFI
     */
    public int getSFI() {
        return SFI;
    }

    /**
     * @param SFI the SFI to set
     */
    public void setSFI(int SFI) {
        this.SFI = SFI;
    }

    /**
     * @return the SFA
     */
    public int getSFA() {
        return SFA;
    }

    /**
     * @param SFA the SFA to set
     */
    public void setSFA(int SFA) {
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
    public long getBCLABaseline() {
        return BCLABaseline;
    }

    /**
     * @param BCLABaseline the BCLABaseline to set
     */
    public void setBCLABaseline(long BCLABaseline) {
        this.BCLABaseline = BCLABaseline;
    }

    /**
     * @return the BpLABaseline
     */
    public int getBpLABaseline() {
        return BpLABaseline;
    }

    /**
     * @param BpLABaseline the BpLABaseline to set
     */
    public void setBpLABaseline(int BpLABaseline) {
        this.BpLABaseline = BpLABaseline;
    }

    /**
     * @return the SizeFractionBaseline
     */
    public int getSizeFractionBaseline() {
        return SizeFractionBaseline;
    }

    /**
     * @param SizeFractionBaseline the SizeFractionBaseline to set
     */
    public void setSizeFractionBaseline(int SizeFractionBaseline) {
        this.SizeFractionBaseline = SizeFractionBaseline;
    }

    public String getDetectionString()
    {
        String retval = "BioPod Detections:\nLarge Counts: " + this.CLI +
                "\nSmall Counts: "+ this.CSI +
                "\nBio Large Counts: "+ this.BCLI +
                "\nBio Small Counts: "+ this.BCSI + "\n";

        return retval;

    }


    public boolean getDetection()
    {
        if ( CSI + CLI > _detectionthresh)
            return true;
        else
            return false;
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
