package edu.jhuapl.nstd.cbrnPods.messages.Canberra;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;


public class CanberraDetectionMessage extends cbrnPodMsg
{
	/**
	 * Detection message count.
	 */
	private int count;
	
	/**
	 * Filtered dose rate in uG/h.
	 */
	private double filteredDoseRate;
	
	/**
	 * Unfiltered dose rate in uG/h.
	 */
	private double unfilteredDoseRate;
	
	/**
	 * Mission accumulated dose in uG.
	 */
	private double missionAccumulatedDose;
	
	/**
	 * Peak rate in uG/h.
	 */
	private double peakRate;
	
	/**
	 * Temperature in Celsius.
	 */
	private double temperature;
	
	/**
	 * Number of hrs, mins, secs after the device was powered on.
	 * In the form "hh:mm:ss".
	 */
	private String relativeTime;
	
	/**
	 * CanberraDetectionMessage constructor.
	 */
	public CanberraDetectionMessage()
	{
		super(cbrnSensorTypes.SENSOR_CANBERRA, cbrnPodMsg.CANBERRA_DETECTION_REPORT, 0);
    }
	
	@Override
	public void parseBioPodMessage(cbrnPodMsg m)
    {
		super.parseBioPodMessage(m);

		CanberraDetectionMessage cdm = (CanberraDetectionMessage)m;
		
        this.timestampMs = cdm.timestampMs;
        this.count = cdm.count;
        this.filteredDoseRate = cdm.filteredDoseRate;
        this.unfilteredDoseRate = cdm.unfilteredDoseRate;
        this.missionAccumulatedDose = cdm.missionAccumulatedDose;
        this.peakRate = cdm.peakRate;
        this.temperature = cdm.temperature;
        this.relativeTime = cdm.relativeTime;
    }
    
	/**
	 * Returns the detection count.
	 * 
	 * @return the count
	 */
	public int getCount()
	{
		return count;
	}
	/**
	 * Returns the filtered dose rate in uG/h.
	 * 
	 * @return the filteredDoseRate
	 */
	public double getFilteredDoseRate()
	{
		return filteredDoseRate;
	}
	/**
	 * Returns the unfiltered dose rate in uG/h.
	 * 
	 * @return the unfilteredDoseRate
	 */
	public double getUnfilteredDoseRate()
	{
		return unfilteredDoseRate;
	}
	/**
	 * Returns the mission accumulated dose in uG.
	 * 
	 * @return the missionAccumulatedDose
	 */
	public double getMisssionAccumulatedDose()
	{
		return missionAccumulatedDose;
	}
	/**
	 * Returns the peak rate in uG/h.
	 * 
	 * @return the peakRate
	 */
	public double getPeakRate()
	{
		return peakRate;
	}
	/**
	 * Returns the temperature in Celcius.
	 * 
	 * @return the temperature
	 */
	public double getTemperature()
	{
		return temperature;
	}
	/**
	 * Returns the number of hours, minutes, and seconds since the device was powered on.
	 * 
	 * @return the relativeTime in the form hh:mm:ss
	 */
	public String getRelativeTime()
	{
		return relativeTime;
	}

	/**
	 * Set the detection message count.
	 * 
	 * @param count the count to set
	 */
	public void setCount(int count)
	{
		this.count = count;
	}

	/**
	 * Set the filtered dose rate in uG/h.
	 * 
	 * @param filteredDoseRate the filteredDoseRate to set
	 */
	public void setFilteredDoseRate(double filteredDoseRate)
	{
		this.filteredDoseRate = filteredDoseRate;
	}

	/**
	 * Set the unfilted dose rate in uG/h.
	 * 
	 * @param unfilteredDoseRate the unfilteredDoseRate to set
	 */
	public void setUnfilteredDoseRate(double unfilteredDoseRate)
	{
		this.unfilteredDoseRate = unfilteredDoseRate;
	}

	/**
	 * Set the mission accumulated dose in uG.
	 * 
	 * @param missionDose the missionDose to set
	 */
	public void setMisssionAccumulatedDose(double missionDose)
	{
		this.missionAccumulatedDose = missionDose;
	}

	/**
	 * Set the mission peak rate uG/h.
	 * 
	 * @param peakRate the peakRate to set
	 */
	public void setPeakRate(double peakRate)
	{
		this.peakRate = peakRate;
	}

	/**
	 * Set the internal temperature in Celcius.
	 * 
	 * @param temperature the temperature to set
	 */
	public void setTemperature(double temperature)
	{
		this.temperature = temperature;
	}

	/**
	 * Set the time of this data reading relative to the startup time of the instrument.
	 * 
	 * @param relativeTime 
	 */
	public void setRelativeTime(String relativeTime)
	{
		this.relativeTime = relativeTime;
	}
}