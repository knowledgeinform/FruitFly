/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.C100Messages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 *
 * @author humphjc1
 */
public class c100ActionMessage extends cbrnPodMsg
{    
    /**
     * If true, message indicates the C100 is cleaning the sample line
     */
    private int m_CleaningLine;
    
    /**
     * If true, message indicates the C100 is generating a wet sample
     */
    private int m_GeneratingSample;
    
    /**
     * If true, message indicates the C100 is cleaning the dry collector
     */
    private int m_CleaningCollector;
    
    /**
     * If true, message indicates the dry collector status is on
     */
    private int m_DryCollectorStatus;
    
    /**
     * If true, message indicates system is priming
     */
    private int m_Priming;

    /**
     * Active wet sampling being generated, if any
     */
    private int m_ActiveSample;
    
    /**
     * Fluid error state
     */
    private boolean m_FluidErrorState;
    
    /**
     * True if sample 1 has been used since reset command
     */
    private boolean m_Sample1Used;
    
    /**
     * True if sample 2 has been used since reset command
     */
    private boolean m_Sample2Used;
    
    /**
     * True if sample 3 has been used since reset command
     */
    private boolean m_Sample3Used;
    
    /**
     * True if sample 4 has been used since reset command
     */
    private boolean m_Sample4Used;

    
    public c100ActionMessage()
    {
        super(cbrnSensorTypes.SENSOR_C100, cbrnPodMsg.C100_ACTION_TYPE, 0);
        m_ActiveSample = 1;
    }


    public ParticleCollectorMode getParticleCollectorMode()
    {
        if(m_CleaningLine == 1)
            return ParticleCollectorMode.Cleaning;
        else if(m_CleaningCollector == 1)
            return ParticleCollectorMode.Cleaning;
        else if(m_CleaningLine == 1)
            return ParticleCollectorMode.Cleaning;
        else if(m_Priming == 1)
            return ParticleCollectorMode.Priming;
        else if(m_GeneratingSample == 1)
        {
            switch(m_ActiveSample)
            {
                case 1:
                    return ParticleCollectorMode.StoringSample1;
                case 2:
                    return ParticleCollectorMode.StoringSample2;
                case 3:
                    return ParticleCollectorMode.StoringSample3;
                case 4:
                    return ParticleCollectorMode.StoringSample4;
            }
        }
        else if(m_DryCollectorStatus == 1)
            return ParticleCollectorMode.Collecting;

        return ParticleCollectorMode.Idle;
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        timestampMs = 1000*readDataUnsignedInt();
        
        m_CleaningLine = readDataByte();
        m_GeneratingSample = readDataByte();
        m_CleaningCollector = readDataByte();
        m_DryCollectorStatus = readDataByte();
        m_Priming = readDataByte();
        
        m_ActiveSample = readDataByte();
        m_FluidErrorState = readDataBool();
        m_Sample1Used = readDataBool();
        m_Sample2Used = readDataBool();
        m_Sample3Used = readDataBool();
        m_Sample4Used = readDataBool();
    }
    
    /**
     * Accessor for state of line cleaning
     * @return
     */
    public int getCleaningLine ()
    {
        return m_CleaningLine;
    }
    
    /**
     * Modifier for state of line cleaning
     * @param newVal
     */
    public void setCleaningLine (int newVal)
    {
        m_CleaningLine = newVal;
    }
    
    /**
     * Accessor for state of 
     * @return
     */
    public int getGeneratingSample ()
    {
        return m_GeneratingSample;
    }
    
    /**
     * Modifier for state of generating wet sample
     * @param newVal
     */
    public void setGeneratingSample (int newVal)
    {
        m_GeneratingSample = newVal;
    }
    
    /**
     * Accessor for state of cleaning the dry collector
     * @return
     */
    public int getCleaningCollector ()
    {
        return m_CleaningCollector;
    }
    
    /**
     * Modifier for state of cleaning the dry collector
     * @param newVal
     */
    public void setCleaningCollector (int newVal)
    {
        m_CleaningCollector = newVal;
    }
    
    /**
     * Accessor for dry collector status
     * @return
     */
    public int getDryCollectorStatus ()
    {
        return m_DryCollectorStatus;
    }
    
    /**
     * Modifier for dry collector status
     * @param newVal
     */
    public void setDryCollectorStatus (int newVal)
    {
        m_DryCollectorStatus = newVal;
    }
    
    /**
     * Accessor for priming status
     * @return
     */
    public int getPrimingStatus ()
    {
        return m_Priming;
    }
    
    /**
     * Modifier for priming status
     * @param newVal
     */
    public void setPrimingStatus (int newVal)
    {
        m_Priming = newVal;
    }

    /**
     * @return the ActiveSample
     */
    public int getActiveSample() {
        return m_ActiveSample;
    }

    /**
     * @param newActiveSample the ActiveSample to set
     */
    public void setActiveSample(int newActiveSample) {
        this.m_ActiveSample = newActiveSample;
    }
    
    /**
     * @return the m_FluidErrorState
     */
    public boolean getFluidErrorState() {
        return m_FluidErrorState;
    }

    /**
     * @param m_Sample1Used the m_FluidErrorState to set
     */
    public void setFluidErrorState(boolean m_FluidErrorState) {
        this.m_FluidErrorState = m_FluidErrorState;
    }
            
    /**
     * @return the m_Sample1Used
     */
    public boolean getSample1Used() {
        return m_Sample1Used;
    }

    /**
     * @param m_Sample1Used the m_Sample1Used to set
     */
    public void setSample1Used(boolean m_Sample1Used) {
        this.m_Sample1Used = m_Sample1Used;
    }

    /**
     * @return the m_Sample2Used
     */
    public boolean getSample2Used() {
        return m_Sample2Used;
    }

    /**
     * @param m_Sample2Used the m_Sample2Used to set
     */
    public void setSample2Used(boolean m_Sample2Used) {
        this.m_Sample2Used = m_Sample2Used;
    }

    /**
     * @return the m_Sample3Used
     */
    public boolean getSample3Used() {
        return m_Sample3Used;
    }

    /**
     * @param m_Sample3Used the m_Sample3Used to set
     */
    public void setSample3Used(boolean m_Sample3Used) {
        this.m_Sample3Used = m_Sample3Used;
    }

    /**
     * @return the m_Sample4Used
     */
    public boolean getSample4Used() {
        return m_Sample4Used;
    }

    /**
     * @param m_Sample4Used the m_Sample4Used to set
     */
    public void setSample4Used(boolean m_Sample4Used) {
        this.m_Sample4Used = m_Sample4Used;
    }
    
}
