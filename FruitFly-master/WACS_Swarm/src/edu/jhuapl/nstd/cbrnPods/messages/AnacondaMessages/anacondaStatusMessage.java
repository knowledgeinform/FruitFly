/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 *
 * @author humphjc1
 */
public class anacondaStatusMessage extends cbrnPodMsg 
{
    long anacondaTimestamp;
    int lcdaStatus;
    int lcdbStatus;
    int lcdaCurrMode;
    int lcdaReqMode;
    int lcdbCurrMode;
    int lcdbReqMode;
    int manifoldTargTemp;
    int manifoldActTemp;
    int pitotTargTemp;
    int pitotActTemp;
    float voltage;
    float current;
    boolean pitotValveStatus;
    boolean manifoldHeaterStatus;
    boolean pitotHeaterStatus;
    boolean srsPumpsSupply;
    boolean srsValveSupply;
    boolean srsValveDrive;
    boolean systemInformation;
    boolean lcdbInformation;
    boolean lcdaInformation;
    boolean lcdbTestFile;
    boolean lcdaTestFile;
    boolean delete4;
    boolean delete3;
    boolean delete2;
    boolean delete1;
    boolean lcdbHSpectrum;
    boolean lcdaHSpectrum;
    boolean lcdbGSpectrum;
    boolean lcdaGSpectrum;
    boolean lcdbBusy;
    boolean lcdaBusy;
    boolean lcdbReset;
    boolean lcdaReset;
    boolean rs232External;
    boolean podCommsValid;
    boolean externalCommsValid;
    boolean sdSocketEmpty;
    boolean srs4;
    boolean srs3;
    boolean srs2;
    boolean srs1;
    boolean debugStatus;
    boolean sample1Used;
    boolean sample2Used;
    boolean sample3Used;
    boolean sample4Used;
    
    
    public anacondaStatusMessage() {
        super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_STATUS_TYPE, 0);
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        timestampMs = 1000*readDataUnsignedInt();
        anacondaTimestamp = readDataUnsignedInt();
        lcdaStatus = readDataInt();
        lcdbStatus = readDataInt();
        lcdaCurrMode = readDataByte();
        lcdaReqMode = readDataByte();
        lcdbCurrMode = readDataByte();
        lcdbReqMode = readDataByte();
        manifoldTargTemp = readDataShort();
        manifoldActTemp = readDataShort();
        pitotTargTemp = readDataShort();
        pitotActTemp = readDataShort();
        voltage = readDataFloat ();
        current = readDataFloat ();
        pitotValveStatus = readDataBool ();
        manifoldHeaterStatus = readDataBool ();
        pitotHeaterStatus = readDataBool ();
        srsPumpsSupply = readDataBool ();
        srsValveSupply = readDataBool ();
        srsValveDrive = readDataBool ();
        systemInformation = readDataBool ();
        lcdbInformation = readDataBool ();
        lcdaInformation = readDataBool ();
        lcdbTestFile = readDataBool ();
        lcdaTestFile = readDataBool ();
        delete4 = readDataBool ();
        delete3 = readDataBool ();
        delete2 = readDataBool ();
        delete1 = readDataBool ();
        lcdbHSpectrum = readDataBool ();
        lcdaHSpectrum = readDataBool ();
        lcdbGSpectrum = readDataBool ();
        lcdaGSpectrum = readDataBool ();
        lcdbBusy = readDataBool ();
        lcdaBusy = readDataBool ();
        lcdbReset = readDataBool ();
        lcdaReset = readDataBool ();
        rs232External = readDataBool ();
        podCommsValid = readDataBool ();
        externalCommsValid = readDataBool ();
        sdSocketEmpty = readDataBool ();
        srs4 = readDataBool ();
        srs3 = readDataBool ();
        srs2 = readDataBool ();
        srs1 = readDataBool ();
        debugStatus = readDataBool ();
        sample1Used = readDataBool ();
        sample2Used = readDataBool ();
        sample3Used = readDataBool ();
        sample4Used = readDataBool ();
    }

     public AnacondaModeEnum getAnacondaMode()
    {
         AnacondaModeEnum retval = null;
        //20	14	ModeIdle
        //21	15	ModeSearch1
        //22	16	ModeSearch2
        //23	17	ModeSearch3
        //24	18	ModeSearch4
        //25	19	ModeStandby
        //26	1A	ModeAirframe
        //27	1B	ModePod
        switch(lcdaCurrMode)
        {
            case 20:
                retval = AnacondaModeEnum.Idle;
                break;
            case 21:
                retval = AnacondaModeEnum.Search1;
                break;
            case 22:
                retval = AnacondaModeEnum.Search2;
                break;
            case 23:
                retval = AnacondaModeEnum.Search3;
                break;
            case 24:
                retval = AnacondaModeEnum.Search4;
                break;
            case 25:
                retval = AnacondaModeEnum.Standby;
                break;
            case 26:
                retval = AnacondaModeEnum.Airframe;
                break;
            case 27:
                retval = AnacondaModeEnum.Pod;
                break;
        }
        return retval;
    }
    
    public void setAnacondaTimestamp (long newAnacondaTimestamp)
    {
        anacondaTimestamp = newAnacondaTimestamp;
    }
    
    public long getAnacondaTimestamp ()
    {
        return anacondaTimestamp;
    }
    
    public void setLcdaStatus (int newStatus)
    {
        lcdaStatus = newStatus;
    }
    
    public int getLcdaStatus ()
    {
        return lcdaStatus;
    }
    
    public void setLcdbStatus (int newStatus)
    {
        lcdbStatus = newStatus;
    }
    
    public int getLcdbStatus ()
    {
        return lcdbStatus;
    }
    
    public void setLcdaCurrMode (int newMode)
    {
        lcdaCurrMode = newMode;
    }
    
    public int getLcdaCurrMode ()
    {
        return lcdaCurrMode;
    }
    
    public void setLcdaReqMode (int newMode)
    {
        lcdaReqMode = newMode;
    }
    
    public int getLcdaReqMode ()
    {
        return lcdaReqMode;
    }
    
    public void setLcdbCurrMode (int newMode)
    {
        lcdbCurrMode = newMode;
    }
    
    public int getLcdbCurrMode ()
    {
        return lcdbCurrMode;
    }
    
    public void setLcdbReqMode (int newMode)
    {
        lcdbReqMode = newMode;
    }
    
    public int getLcdbReqMode ()
    {
        return lcdbReqMode;
    }
    
    public void setManifoldTargTemp (int newTemp)
    {
        manifoldTargTemp = newTemp;
    }
    
    public int getManifoldTargTemp ()
    {
        return manifoldTargTemp;
    }
    
    public void setManifoldActTemp (int newTemp)
    {
        manifoldActTemp = newTemp;
    }
    
    public int getManifoldActTemp ()
    {
        return manifoldActTemp;
    }
    
    public void setPitotTargTemp (int newTemp)
    {
        pitotTargTemp = newTemp;
    }
    
    public int getPitotTargTemp ()
    {
        return pitotTargTemp;
    }
    
    public void setPitotActTemp (int newTemp)
    {
        pitotActTemp = newTemp;
    }
    
    public int getPitotActTemp ()
    {
        return pitotActTemp;
    }
    
    public void setVoltage (float newVoltage)
    {
        voltage = newVoltage;
    }
    
    public float getVoltage ()
    {
        return voltage;
    }
    
    public void setCurrent (float newCurrent)
    {
        current = newCurrent;
    }
    
    public float getCurrent ()
    {
        return current;
    }
    
    public void setPitotValueStatus (boolean newVal)
    {
        pitotValveStatus = newVal;
    }
    
    public boolean getPitotValveStatus ()
    {
        return pitotValveStatus;
    }
    
    public void setManifoldHeaterStatus (boolean newVal)
    {
        manifoldHeaterStatus = newVal;
    }
    
    public boolean getManifoldHeaterStatus ()
    {
        return manifoldHeaterStatus;
    }
    
    public void setPitotHeaterStatus (boolean newVal)
    {
        pitotHeaterStatus = newVal;
    }
    
    public boolean getPitotHeaterStatus ()
    {
        return pitotHeaterStatus;
    }
    
    public void setSrsPumpSupply (boolean newVal)
    {
        srsPumpsSupply = newVal;
    }
    
    public boolean getSrsPumpSupply ()
    {
        return srsPumpsSupply;
    }
    
    public void setSrsValveSupply (boolean newVal)
    {
        srsValveSupply = newVal;
    }
    
    public boolean getSrsValveSupply ()
    {
        return srsValveSupply;
    }
    
    public void setSrsValveDrive (boolean newVal)
    {
        srsValveDrive = newVal;
    }
    
    public boolean getSrsValveDrive ()
    {
        return srsValveDrive;
    }
    
    public void setSystemInformation (boolean newVal)
    {
        systemInformation = newVal;
    }

    public boolean getSystemInformation ()
    {
        return systemInformation;
    }

    public void setLcdbInformation (boolean newVal)
    {
        lcdbInformation = newVal;
    }

    public boolean getLcdbInformation ()
    {
        return lcdbInformation;
    }

    public void setLcdaInformation (boolean newVal)
    {
        lcdaInformation = newVal;
    }

    public boolean getLcdaInformation ()
    {
        return lcdaInformation;
    }

    public void setLcdbTestFile (boolean newVal)
    {
        lcdbTestFile = newVal;
    }

    public boolean getLcdbTestFile ()
    {
        return lcdbTestFile;
    }

    public void setLcdaTestFile (boolean newVal)
    {
        lcdaTestFile = newVal;
    }

    public boolean getLcdaTestFile ()
    {
        return lcdaTestFile;
    }

    public void setDelete4 (boolean newVal)
    {
        delete4 = newVal;
    }

    public boolean getDelete4 ()
    {
        return delete4;
    }

    public void setDelete3 (boolean newVal)
    {
        delete3 = newVal;
    }

    public boolean getDelete3 ()
    {
        return delete3;
    }

    public void setDelete2 (boolean newVal)
    {
        delete2 = newVal;
    }

    public boolean getDelete2 ()
    {
        return delete2;
    }

    public void setDelete1 (boolean newVal)
    {
        delete1 = newVal;
    }

    public boolean getDelete1 ()
    {
        return delete1;
    }

    public void setRs232External (boolean newVal)
    {
        rs232External = newVal;
    }
    
    public boolean getRs232External ()
    {
        return rs232External;
    }
    
    public void setPodCommsValid (boolean newVal)
    {
        podCommsValid = newVal;
    }
    
    public boolean getPodCommsValid ()
    {
        return podCommsValid;
    }
    
    public void setExternalCommsValid (boolean newVal)
    {
        externalCommsValid = newVal;
    }
    
    public boolean getExternalCommsValid ()
    {
        return externalCommsValid;
    }
    
    public void setSdSocketEmpty (boolean newVal)
    {
        sdSocketEmpty = newVal;
    }
    
    public boolean getSdSocketEmpty ()
    {
        return sdSocketEmpty;
    }
    
    public void setSrs4 (boolean newVal)
    {
        srs4 = newVal;
    }
    
    public boolean getSrs4 ()
    {
        return srs4;
    }
    
    public void setSrs3 (boolean newVal)
    {
        srs3 = newVal;
    }
    
    public boolean getSrs3 ()
    {
        return srs3;
    }
    
    public void setSrs2 (boolean newVal)
    {
        srs2 = newVal;
    }
    
    public boolean getSrs2 ()
    {
        return srs2;
    }
    
    public void setSrs1 (boolean newVal)
    {
        srs1 = newVal;
    }
    
    public boolean getSrs1 ()
    {
        return srs1;
    }
    
    public void setDebugStatus (boolean newVal)
    {
        debugStatus = newVal;
    }
    
    public boolean getDebugStatus ()
    {
        return debugStatus;
    }
    
    public void setLcdbHSpectrum (boolean newVal)
    {
        lcdbHSpectrum = newVal;
    }
    
    public boolean getLcdbHSpectrum ()
    {
        return lcdbHSpectrum;
    }
    
    public void setLcdaHSpectrum (boolean newVal)
    {
        lcdaHSpectrum = newVal;
    }
    
    public boolean getLcdaHSpectrum ()
    {
        return lcdaHSpectrum;
    }
    
    public void setLcdbGSpectrum (boolean newVal)
    {
        lcdbGSpectrum = newVal;
    }
    
    public boolean getLcdbGSpectrum ()
    {
        return lcdbGSpectrum;
    }
    
    public void setLcdaGSpectrum (boolean newVal)
    {
        lcdaGSpectrum = newVal;
    }
    
    public boolean getLcdaGSpectrum ()
    {
        return lcdaGSpectrum;
    }
    
    public void setLcdbBusy (boolean newVal)
    {
        lcdbBusy = newVal;
    }
    
    public boolean getLcdbBusy ()
    {
        return lcdbBusy;
    }
    
    public void setLcdaBusy (boolean newVal)
    {
        lcdaBusy = newVal;
    }
    
    public boolean getLcdaBusy ()
    {
        return lcdaBusy;
    }
    
    public void setLcdbReset (boolean newVal)
    {
        lcdbReset = newVal;
    }
    
    public boolean getLcdbReset ()
    {
        return lcdbReset;
    }
    
    public void setLcdaReset (boolean newVal)
    {
        lcdaReset = newVal;
    }
    
    public boolean getLcdaReset ()
    {
        return lcdaReset;
    }
    
    public void setSample1Used (boolean used)
    {
        sample1Used = used;
    }
    
    public boolean getSample1Used ()
    {
        return sample1Used;
    }
    
    public void setSample2Used (boolean used)
    {
        sample2Used = used;
    }
    
    public boolean getSample2Used ()
    {
        return sample2Used;
    }
    
    public void setSample3Used (boolean used)
    {
        sample3Used = used;
    }
    
    public boolean getSample3Used ()
    {
        return sample3Used;
    }
    
    public void setSample4Used (boolean used)
    {
        sample4Used = used;
    }
    
    public boolean getSample4Used ()
    {
        return sample4Used;
    }
}



