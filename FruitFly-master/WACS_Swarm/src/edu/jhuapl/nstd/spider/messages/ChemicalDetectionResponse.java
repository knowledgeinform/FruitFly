/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.spider.messages;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author southmk1
 */
public class ChemicalDetectionResponse extends SpiderMessage {
    /*
     * 4.6.1 Message Type 41, SPIDER Chemical Detection

    This message is sent from the SPIDER when a target chemical is detected. Note that this is sent at either at priority 2 for samples where no peaks have been detected, or at priority 3 (maximum) where a peak(s) has been detected.

    Message Length:		63 bytes
    Message Repetition Rate:	1 per second  //comment from Mark Meehan – do we start receiving this message when the SPIDER mode changes to Search ( 04h)? And is it at a half hertz or one hertz rate?
    Message Structure:
    Data State Byte
    PIP Mode Byte
    PIP Status Byte
    IMS 1 Status Byte
    IMS 2 Status Byte
    IMS Summary (33 bytes)
    GPS (19 bytes)
    Met Sensor (6 bytes)

    Total Packet Length		68 Bytes
     */


    //Data State Byte
    private boolean imsADataValid;
    private boolean imsAFlashOk;
    private boolean imsBDataValid;
    private boolean imsBFlashOk;
    private boolean gpsDataValid;
    private boolean metDataValid;
    private boolean rtcDataInGps;
    private boolean spare;

    //PIPModeByte
    private SpiderModeEnum pipMode;

    //PipStatus Byte
    private boolean grabSamplerOpen;
    private boolean pitotValveOpen;
    private boolean pitotHeaterOk;
    private boolean pitotAirOver10C;
    private boolean plenumHeaterOk;
    private boolean plenumAirOver10C;
    private boolean boxHeaterOk;
    private boolean boxOver10C;

    //Ims1 Status Byte
    private boolean ims1DpOk;
    private boolean ims1DtOk;
    private boolean ims1PressureOk;
    private boolean ims1CoronaOk;
    private boolean ims1Spare;
    private SpiderModeEnum ims1Mode;
    //Ims2 Status Byte
    private boolean ims2DpOk;
    private boolean ims2DtOk;
    private boolean ims2PressureOk;
    private boolean ims2CoronaOk;
    private boolean ims2Spare;
    private SpiderModeEnum ims2Mode;

    //Ims Summary
    private int ims1SieveType;
    private int ims2SieveType;
    private int ims1WindowsSet;
    private int ims2WindowsSet;
    private ImsPeakReport[] ims1PositivePeaks;
    private ImsPeakReport[] ims1NegativePeaks;
    private ImsPeakReport[] ims2PositivePeaks;
    private ImsPeakReport[] ims2NegativePeaks;

    //GPS
    private long gpsTimeOfWeek;
    private int gpsLat;
    private int gpsLon;
    private int gpsAlt;
    private int speedOverGround;
    private int trackOverGround;
    private SpiderGpsModeEnum gpsState;

    //Met
    private int pressure;
    private int temperature;
    private int humidity;

    public ChemicalDetectionResponse() {
        super(SpiderMessageType.SPIDER_CHEMICAL_DETECTION, HeaderSize + ChecksumSize + 63);
    }

    @Override
    public void parseSpiderMessage(SpiderMessage m) {
        super.parseSpiderMessage(m);
        int idx = 0;
        //Data State Byte ( byte 0 )
        imsADataValid = m.readDataBitAsBool(idx, 0);
        imsAFlashOk = m.readDataBitAsBool(idx, 1);
        imsBDataValid = m.readDataBitAsBool(idx, 2);
        imsBFlashOk = m.readDataBitAsBool(idx, 3);
        gpsDataValid = m.readDataBitAsBool(idx, 4);
        metDataValid = m.readDataBitAsBool(idx, 5);
        rtcDataInGps = m.readDataBitAsBool(idx, 6);
        spare = m.readDataBitAsBool(idx, 7);
        idx++;

        //pip mode byte ( byte 1 )
        pipMode = SpiderModeEnum.values()[m.readDataByte(idx) >> 5 & 0x07];
        idx++;

        //pip status byte (byte 2)
        grabSamplerOpen = m.readDataBitAsBool(idx, 0);
        pitotValveOpen = m.readDataBitAsBool(idx, 1);
        pitotHeaterOk = m.readDataBitAsBool(idx, 2);
        pitotAirOver10C = m.readDataBitAsBool(idx, 3);
        plenumHeaterOk = m.readDataBitAsBool(idx, 4);
        plenumAirOver10C = m.readDataBitAsBool(idx, 5);
        boxHeaterOk = m.readDataBitAsBool(idx, 6);
        boxOver10C = m.readDataBitAsBool(idx, 7);
        idx++;

        //ims1 status byte ( byte 3 )
        ims1DpOk = m.readDataBitAsBool(idx, 0);
        ims1DtOk = m.readDataBitAsBool(idx, 1);
        ims1PressureOk = m.readDataBitAsBool(idx, 2);
        ims1CoronaOk = m.readDataBitAsBool(idx, 3);
        ims1Spare = m.readDataBitAsBool(idx, 4);
        ims1Mode = SpiderModeEnum.values()[m.readDataByte(idx) >> 5 & 0x03];
        idx++;

        //ims2 status byte ( byte 4 )
        ims2DpOk = m.readDataBitAsBool(idx, 0);
        ims2DtOk = m.readDataBitAsBool(idx, 1);
        ims2PressureOk = m.readDataBitAsBool(idx, 2);
        ims2CoronaOk = m.readDataBitAsBool(idx, 3);
        ims2Spare = m.readDataBitAsBool(idx, 4);
        ims2Mode = SpiderModeEnum.values()[m.readDataByte(idx) >> 5 & 0x03];
        idx++;

        //ims summary
        ims1SieveType = m.readDataBit(idx, 7);
        ims2SieveType = m.readDataBit(idx, 6);
        ims1WindowsSet = ((m.readDataBit(idx, 5) << 2) | (m.readDataBit(idx, 4) << 1) | (m.readDataBit(idx, 3)));
        ims2WindowsSet = ((m.readDataBit(idx, 2) << 2) | (m.readDataBit(idx, 1) << 1) | (m.readDataBit(idx, 0)));
        idx++;

        ims1PositivePeaks = new ImsPeakReport[8];
        for (int i = 0; i < 8; i++) {
            ims1PositivePeaks[i] = new ImsPeakReport(m.readDataByte(idx++));
        }
        ims1NegativePeaks = new ImsPeakReport[8];
        for (int i = 0; i < 8; i++) {
            ims1NegativePeaks[i] = new ImsPeakReport(m.readDataByte(idx++));
        }
        ims2PositivePeaks = new ImsPeakReport[8];
        for (int i = 0; i < 8; i++) {
            ims2PositivePeaks[i] = new ImsPeakReport(m.readDataByte(idx++));
        }
        ims2NegativePeaks = new ImsPeakReport[8];
        for (int i = 0; i < 8; i++) {
            ims2NegativePeaks[i] = new ImsPeakReport(m.readDataByte(idx++));
        }

        //GPS
        //gpsWeek = m.readDataUnsignedInt(idx);
        //idx += 4;
        gpsTimeOfWeek = m.readDataUnsignedInt(idx);
        idx += 4;
        gpsLat = m.readDataInt(idx);
        idx += 4;
        gpsLon = m.readDataInt(idx);
        idx += 4;
        gpsAlt = m.readDataShort(idx);
        idx += 2;
        gpsState = SpiderGpsModeEnum.values()[m.readDataByte(idx++) >> 5 & 0x03];

        //
        speedOverGround = m.readDataShort(idx);
        idx += 2;
        trackOverGround = m.readDataShort(idx);
        idx += 2;

        //Met
        pressure = m.readDataShort(idx);
        idx += 2;
        temperature = m.readDataShort(idx);
        idx += 2;
        humidity = m.readDataShort(idx);
        idx += 2;


    }

    public int getDetectionStrength()
    {
        int detection = 0;
        for (int i = 0; i < 8; i++)
        {
            detection += ims1PositivePeaks[i].amplitude;
            detection += ims1NegativePeaks[i].amplitude;
            detection += ims2PositivePeaks[i].amplitude;
            detection += ims2NegativePeaks[i].amplitude;
        }
        return detection;
    }

    protected  HashMap<Integer, Integer> getIMS1DetectionString()
    {
        try
        {
            HashMap<Integer, Integer> detection = new HashMap<Integer,Integer>();
            for (int i = 0; i < 8; i++)
            {
                detection.put(ims1PositivePeaks[i].peakIdentifier, ims1PositivePeaks[i].amplitude);
                detection.put(ims1NegativePeaks[i].peakIdentifier, ims1NegativePeaks[i].amplitude);
            }
            return detection;
        }
        catch(Exception e)
        {
            return null;
        }
    }

    protected  HashMap<Integer, Integer> getIMS2DetectionString()
    {
        try
        {
            HashMap<Integer, Integer> detection = new HashMap<Integer,Integer>();
            for (int i = 0; i < 8; i++)
            {
                detection.put(ims2PositivePeaks[i].peakIdentifier, ims2PositivePeaks[i].amplitude);
                detection.put(ims2NegativePeaks[i].peakIdentifier, ims2NegativePeaks[i].amplitude);
            }
            return detection;
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public String getDetectionString(HashMap<Integer,String> types)
    {
        String retval = "Spider Chem Detections:\nChem Detector #1\n";

        HashMap<Integer, Integer> detections = getIMS1DetectionString();
        if(detections != null)
        {
            for(Integer o : detections.keySet())
            {
                retval = retval + types.get(o) + ": " + detections.get(o) + "\n";
            }
        }

        retval = retval + "\nChem Detector #2\n";

        detections = getIMS2DetectionString();
        if(detections != null)
        {
            for(Integer o : detections.keySet())
            {
                retval = retval + types.get(o) + ": " + detections.get(o) + "\n";
            }
        }
        return retval;
        
    }



    /**
     * @return the imsADataValid
     */
    public boolean isImsADataValid() {
        return imsADataValid;
    }

    /**
     * @param imsADataValid the imsADataValid to set
     */
    public void setImsADataValid(boolean imsADataValid) {
        this.imsADataValid = imsADataValid;
    }

    /**
     * @return the imsAFlashOk
     */
    public boolean isImsAFlashOk() {
        return imsAFlashOk;
    }

    /**
     * @param imsAFlashOk the imsAFlashOk to set
     */
    public void setImsAFlashOk(boolean imsAFlashOk) {
        this.imsAFlashOk = imsAFlashOk;
    }

    /**
     * @return the imsBDataValid
     */
    public boolean isImsBDataValid() {
        return imsBDataValid;
    }

    /**
     * @param imsBDataValid the imsBDataValid to set
     */
    public void setImsBDataValid(boolean imsBDataValid) {
        this.imsBDataValid = imsBDataValid;
    }

    /**
     * @return the imsBFlashOk
     */
    public boolean isImsBFlashOk() {
        return imsBFlashOk;
    }

    /**
     * @param imsBFlashOk the imsBFlashOk to set
     */
    public void setImsBFlashOk(boolean imsBFlashOk) {
        this.imsBFlashOk = imsBFlashOk;
    }

    /**
     * @return the gpsDataValid
     */
    public boolean isGpsDataValid() {
        return gpsDataValid;
    }

    /**
     * @param gpsDataValid the gpsDataValid to set
     */
    public void setGpsDataValid(boolean gpsDataValid) {
        this.gpsDataValid = gpsDataValid;
    }

    /**
     * @return the metDataValid
     */
    public boolean isMetDataValid() {
        return metDataValid;
    }

    /**
     * @param metDataValid the metDataValid to set
     */
    public void setMetDataValid(boolean metDataValid) {
        this.metDataValid = metDataValid;
    }

    /**
     * @return the rtcDataInGps
     */
    public boolean isRtcDataInGps() {
        return rtcDataInGps;
    }

    /**
     * @param rtcDataInGps the rtcDataInGps to set
     */
    public void setRtcDataInGps(boolean rtcDataInGps) {
        this.rtcDataInGps = rtcDataInGps;
    }

    /**
     * @return the spare
     */
    public boolean isSpare() {
        return spare;
    }

    /**
     * @param spare the spare to set
     */
    public void setSpare(boolean spare) {
        this.spare = spare;
    }

    /**
     * @return the pipMode
     */
    public SpiderModeEnum getPipMode()
    {

        return pipMode;
    }

    /**
     * @param pipMode the pipMode to set
     */
    public void setPipMode(SpiderModeEnum pipMode) {
        this.pipMode = pipMode;
    }

    /**
     * @return the grabSamplerOpen
     */
    public boolean isGrabSamplerOpen() {
        return grabSamplerOpen;
    }

    /**
     * @param grabSamplerOpen the grabSamplerOpen to set
     */
    public void setGrabSamplerOpen(boolean grabSamplerOpen) {
        this.grabSamplerOpen = grabSamplerOpen;
    }

    /**
     * @return the pitotValveOpen
     */
    public boolean isPitotValveOpen() {
        return pitotValveOpen;
    }

    /**
     * @param pitotValveOpen the pitotValveOpen to set
     */
    public void setPitotValveOpen(boolean pitotValveOpen) {
        this.pitotValveOpen = pitotValveOpen;
    }

    /**
     * @return the pitotHeaterOk
     */
    public boolean isPitotHeaterOk() {
        return pitotHeaterOk;
    }

    /**
     * @param pitotHeaterOk the pitotHeaterOk to set
     */
    public void setPitotHeaterOk(boolean pitotHeaterOk) {
        this.pitotHeaterOk = pitotHeaterOk;
    }

    /**
     * @return the pitotAirOver10C
     */
    public boolean isPitotAirOver10C() {
        return pitotAirOver10C;
    }

    /**
     * @param pitotAirOver10C the pitotAirOver10C to set
     */
    public void setPitotAirOver10C(boolean pitotAirOver10C) {
        this.pitotAirOver10C = pitotAirOver10C;
    }

    /**
     * @return the plenumHeaterOk
     */
    public boolean isPlenumHeaterOk() {
        return plenumHeaterOk;
    }

    /**
     * @param plenumHeaterOk the plenumHeaterOk to set
     */
    public void setPlenumHeaterOk(boolean plenumHeaterOk) {
        this.plenumHeaterOk = plenumHeaterOk;
    }

    /**
     * @return the plenumAirOver10C
     */
    public boolean isPlenumAirOver10C() {
        return plenumAirOver10C;
    }

    /**
     * @param plenumAirOver10C the plenumAirOver10C to set
     */
    public void setPlenumAirOver10C(boolean plenumAirOver10C) {
        this.plenumAirOver10C = plenumAirOver10C;
    }

    /**
     * @return the boxHeaterOk
     */
    public boolean isBoxHeaterOk() {
        return boxHeaterOk;
    }

    /**
     * @param boxHeaterOk the boxHeaterOk to set
     */
    public void setBoxHeaterOk(boolean boxHeaterOk) {
        this.boxHeaterOk = boxHeaterOk;
    }

    /**
     * @return the boxOver10C
     */
    public boolean isBoxOver10C() {
        return boxOver10C;
    }

    /**
     * @param boxOver10C the boxOver10C to set
     */
    public void setBoxOver10C(boolean boxOver10C) {
        this.boxOver10C = boxOver10C;
    }

    /**
     * @return the ims1DpOk
     */
    public boolean isIms1DpOk() {
        return ims1DpOk;
    }

    /**
     * @param ims1DpOk the ims1DpOk to set
     */
    public void setIms1DpOk(boolean ims1DpOk) {
        this.ims1DpOk = ims1DpOk;
    }

    /**
     * @return the ims1DtOk
     */
    public boolean isIms1DtOk() {
        return ims1DtOk;
    }

    /**
     * @param ims1DtOk the ims1DtOk to set
     */
    public void setIms1DtOk(boolean ims1DtOk) {
        this.ims1DtOk = ims1DtOk;
    }

    /**
     * @return the ims1PressureOk
     */
    public boolean isIms1PressureOk() {
        return ims1PressureOk;
    }

    /**
     * @param ims1PressureOk the ims1PressureOk to set
     */
    public void setIms1PressureOk(boolean ims1PressureOk) {
        this.ims1PressureOk = ims1PressureOk;
    }

    /**
     * @return the ims1CoronaOk
     */
    public boolean isIms1CoronaOk() {
        return ims1CoronaOk;
    }

    /**
     * @param ims1CoronaOk the ims1CoronaOk to set
     */
    public void setIms1CoronaOk(boolean ims1CoronaOk) {
        this.ims1CoronaOk = ims1CoronaOk;
    }

    /**
     * @return the ims1Spare
     */
    public boolean isIms1Spare() {
        return ims1Spare;
    }

    /**
     * @param ims1Spare the ims1Spare to set
     */
    public void setIms1Spare(boolean ims1Spare) {
        this.ims1Spare = ims1Spare;
    }

    /**
     * @return the ims1Mode
     */
    public SpiderModeEnum getIms1Mode() {
        return ims1Mode;
    }

    /**
     * @param ims1Mode the ims1Mode to set
     */
    public void setIms1Mode(SpiderModeEnum ims1Mode) {
        this.ims1Mode = ims1Mode;
    }

    /**
     * @return the ims2DpOk
     */
    public boolean isIms2DpOk() {
        return ims2DpOk;
    }

    /**
     * @param ims2DpOk the ims2DpOk to set
     */
    public void setIms2DpOk(boolean ims2DpOk) {
        this.ims2DpOk = ims2DpOk;
    }

    /**
     * @return the ims2DtOk
     */
    public boolean isIms2DtOk() {
        return ims2DtOk;
    }

    /**
     * @param ims2DtOk the ims2DtOk to set
     */
    public void setIms2DtOk(boolean ims2DtOk) {
        this.ims2DtOk = ims2DtOk;
    }

    /**
     * @return the ims2PressureOk
     */
    public boolean isIms2PressureOk() {
        return ims2PressureOk;
    }

    /**
     * @param ims2PressureOk the ims2PressureOk to set
     */
    public void setIms2PressureOk(boolean ims2PressureOk) {
        this.ims2PressureOk = ims2PressureOk;
    }

    /**
     * @return the ims2CoronaOk
     */
    public boolean isIms2CoronaOk() {
        return ims2CoronaOk;
    }

    /**
     * @param ims2CoronaOk the ims2CoronaOk to set
     */
    public void setIms2CoronaOk(boolean ims2CoronaOk) {
        this.ims2CoronaOk = ims2CoronaOk;
    }

    /**
     * @return the ims2Spare
     */
    public boolean isIms2Spare() {
        return ims2Spare;
    }

    /**
     * @param ims2Spare the ims2Spare to set
     */
    public void setIms2Spare(boolean ims2Spare) {
        this.ims2Spare = ims2Spare;
    }

    /**
     * @return the ims2Mode
     */
    public SpiderModeEnum getIms2Mode() {
        return ims2Mode;
    }

    /**
     * @param ims2Mode the ims2Mode to set
     */
    public void setIms2Mode(SpiderModeEnum ims2Mode) {
        this.ims2Mode = ims2Mode;
    }

    /**
     * @return the ims1SieveType
     */
    public int getIms1SieveType() {
        return ims1SieveType;
    }

    /**
     * @param ims1SieveType the ims1SieveType to set
     */
    public void setIms1SieveType(int ims1SieveType) {
        this.ims1SieveType = ims1SieveType;
    }

    /**
     * @return the ims2SieveType
     */
    public int getIms2SieveType() {
        return ims2SieveType;
    }

    /**
     * @param ims2SieveType the ims2SieveType to set
     */
    public void setIms2SieveType(int ims2SieveType) {
        this.ims2SieveType = ims2SieveType;
    }

    /**
     * @return the ims1WindowsSet
     */
    public int getIms1WindowsSet() {
        return ims1WindowsSet;
    }

    /**
     * @param ims1WindowsSet the ims1WindowsSet to set
     */
    public void setIms1WindowsSet(int ims1WindowsSet) {
        this.ims1WindowsSet = ims1WindowsSet;
    }

    /**
     * @return the ims2WindowsSet
     */
    public int getIms2WindowsSet() {
        return ims2WindowsSet;
    }

    /**
     * @param ims2WindowsSet the ims2WindowsSet to set
     */
    public void setIms2WindowsSet(int ims2WindowsSet) {
        this.ims2WindowsSet = ims2WindowsSet;
    }

    /**
     * @return the ims1PositivePeaks
     */
    public ImsPeakReport[] getIms1PositivePeaks() {
        return ims1PositivePeaks;
    }

    /**
     * @param ims1PositivePeaks the ims1PositivePeaks to set
     */
    public void setIms1PositivePeaks(ImsPeakReport[] ims1PositivePeaks) {
        this.ims1PositivePeaks = ims1PositivePeaks;
    }

    /**
     * @return the ims1NegativePeaks
     */
    public ImsPeakReport[] getIms1NegativePeaks() {
        return ims1NegativePeaks;
    }

    /**
     * @param ims1NegativePeaks the ims1NegativePeaks to set
     */
    public void setIms1NegativePeaks(ImsPeakReport[] ims1NegativePeaks) {
        this.ims1NegativePeaks = ims1NegativePeaks;
    }

    /**
     * @return the ims2PositivePeaks
     */
    public ImsPeakReport[] getIms2PositivePeaks() {
        return ims2PositivePeaks;
    }

    /**
     * @param ims2PositivePeaks the ims2PositivePeaks to set
     */
    public void setIms2PositivePeaks(ImsPeakReport[] ims2PositivePeaks) {
        this.ims2PositivePeaks = ims2PositivePeaks;
    }

    /**
     * @return the ims2NegativePeaks
     */
    public ImsPeakReport[] getIms2NegativePeaks() {
        return ims2NegativePeaks;
    }

    /**
     * @param ims2NegativePeaks the ims2NegativePeaks to set
     */
    public void setIms2NegativePeaks(ImsPeakReport[] ims2NegativePeaks) {
        this.ims2NegativePeaks = ims2NegativePeaks;
    }

   

    /**
     * @return the gpsTimeOfWeek
     */
    public long getGpsTimeOfWeek() {
        return gpsTimeOfWeek;
    }

    /**
     * @param gpsTimeOfWeek the gpsTimeOfWeek to set
     */
    public void setGpsTimeOfWeek(long gpsTimeOfWeek) {
        this.gpsTimeOfWeek = gpsTimeOfWeek;
    }

    /**
     * @return the gpsLat
     */
    public int getGpsLat() {
        return gpsLat;
    }

    /**
     * @param gpsLat the gpsLat to set
     */
    public void setGpsLat(int gpsLat) {
        this.gpsLat = gpsLat;
    }

    /**
     * @return the gpsLon
     */
    public int getGpsLon() {
        return gpsLon;
    }

    /**
     * @param gpsLon the gpsLon to set
     */
    public void setGpsLon(int gpsLon) {
        this.gpsLon = gpsLon;
    }

    /**
     * @return the gpsAlt
     */
    public int getGpsAlt() {
        return gpsAlt;
    }

    /**
     * @param gpsAlt the gpsAlt to set
     */
    public void setGpsAlt(int gpsAlt) {
        this.gpsAlt = gpsAlt;
    }

    /**
     * @return the gpsState
     */
    public SpiderGpsModeEnum getGpsState() {
        return gpsState;
    }

    /**
     * @param gpsState the gpsState to set
     */
    public void setGpsState(SpiderGpsModeEnum gpsState) {
        this.gpsState = gpsState;
    }

    /**
     * @return the pressure
     */
    public int getPressure() {
        return pressure;
    }

    /**
     * @param pressure the pressure to set
     */
    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    /**
     * @return the temperature
     */
    public int getTemperature() {
        return temperature;
    }

    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    /**
     * @return the humidity
     */
    public int getHumidity() {
        return humidity;
    }

    /**
     * @param humidity the humidity to set
     */
    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    /**
     * @return the speedOverGround
     */
    public int getSpeedOverGround() {
        return speedOverGround;
    }

    /**
     * @param speedOverGround the speedOverGround to set
     */
    public void setSpeedOverGround(int speedOverGround) {
        this.speedOverGround = speedOverGround;
    }

    /**
     * @return the trackOverGround
     */
    public int getTrackOverGround() {
        return trackOverGround;
    }

    /**
     * @param trackOverGround the trackOverGround to set
     */
    public void setTrackOverGround(int trackOverGround) {
        this.trackOverGround = trackOverGround;
    }

    public class ImsPeakReport {

        private int amplitude;
        private int peakIdentifier;

        public ImsPeakReport(int b) {
            amplitude = b >> 5 & 0x7; //0b111
            peakIdentifier = b & 0x1f; //0b11111
        }

        public byte toByte() {
            return (byte) (amplitude & 0x7 << 5 | peakIdentifier & 0x1f);
        }

        /**
         * @return the amplitude
         */
        public int getAmplitude() {
            return amplitude;
        }

        /**
         * @param amplitude the amplitude to set
         */
        public void setAmplitude(int amplitude) {
            this.amplitude = amplitude;
        }

        /**
         * @return the peakIdentifier
         */
        public int getPeakIdentifier() {
            return peakIdentifier;
        }

        /**
         * @param peakIdentifier the peakIdentifier to set
         */
        public void setPeakIdentifier(int peakIdentifier) {
            this.peakIdentifier = peakIdentifier;
        }
    }
}
