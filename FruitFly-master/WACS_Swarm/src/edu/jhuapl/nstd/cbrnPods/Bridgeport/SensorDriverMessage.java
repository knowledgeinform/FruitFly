package edu.jhuapl.nstd.cbrnPods.Bridgeport;

import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.nstd.cbrnPods.RNHistogram;

/**
 * This class models the telemetry of gamma, neutron, high-field, and temperature sensors
 * returned through an XML message stream provided by the SensorDriver application.
 * 
 * @author olsoncc1
 *
 */
public class SensorDriverMessage
{
	// private int unitId;
	
	/** Timestamp of when the XML message was created and sent from the SensorDriver application. */
	private int timestamp;
	
	// private int numGammaDetectors;
	// private int numNeutronDetectors;
	// private int numTemperatureSensors;
	// private int numHighFieldDetectors;
	
	private double temperature;
	
	public List<GammaDetectorMessage> gammaDetectorMessageList;
	
	// TODO These should be implemented when they are needed.
//	public List<NeutronDetectorMessage> neutronDetectorMessageList;
//	public List<TemperatureSensorMessage> temperatureSensorMessageList;
//	public List<HighFieldDetectorMessage> highFieldDetectorMessageList;
	
	public SensorDriverMessage()
	{
		gammaDetectorMessageList = new ArrayList<GammaDetectorMessage>();
	}
	
	public int getTimestamp()
	{
		return timestamp;
	}
	
	public void setTimestamp(int timestamp)
	{
		this.timestamp = timestamp;
	}
	
	public double getTemperature()
	{
		return temperature;
	}
	
	public void setTemperature(double temperature)
	{
		this.temperature = temperature;
	}
	
	public List<GammaDetectorMessage> getGammaDetectorMessageList()
	{
		return gammaDetectorMessageList;
	}
	
	public void addGammaDetectorMessage(GammaDetectorMessage msg)
	{
		gammaDetectorMessageList.add(msg);
	}
	
	public class GammaDetectorMessage
	{
		/** Identification number of this gamma detector as defined in the SensorDriver config file. */
		private int id;
		
		/** Serial number of this gamma detector as defined in the SensorDriver config file. */
		private int serialNumber;
		
		/**
		 * If the gamma detector with this id is working properly, this status will be true.
		 */
		private boolean status;
		
		/** The timestamp of when the sample spectrum was taken. */
		private int sampleTime;
		
		/** The high voltage setting. */
		private double highVoltage;
		
		/** The coarse gain setting. */
		private double coarseGain;
		
		/** The fine gain setting. */
		private double fineGain;
		
		/** Live time + dead time = real time (in milliseconds)*/
		private int realTime;
		
		/** Time in millisections that the gamma detector is on. */
		private int liveTime;
		
		/** Time in millisections that the gamma detector is off and processing a spectrum reading. */
		private int deadTime;
		
		/** Total count / live time */
		private double countRate;
		
		/** Total gamma particle count for this spectrum. */
		private int totalCount;
		
		/** Spectrum. */
		private RNHistogram spectrum;

		/**
		 * @return the id
		 */
		public int getId()
		{
			return id;
		}

		/**
		 * @return the serialNumber
		 */
		public int getSerialNumber()
		{
			return serialNumber;
		}

		/**
		 * @return the status
		 */
		public boolean getStatus()
		{
			return status;
		}

		/**
		 * @return the sampleTime
		 */
		public int getSampleTime()
		{
			return sampleTime;
		}

		/**
		 * @return the highVoltage
		 */
		public double getHighVoltage()
		{
			return highVoltage;
		}

		/**
		 * @return the coarseGain
		 */
		public double getCoarseGain()
		{
			return coarseGain;
		}

		/**
		 * @return the fineGain
		 */
		public double getFineGain()
		{
			return fineGain;
		}

		/**
		 * @return the realTime
		 */
		public int getRealTime()
		{
			return realTime;
		}

		/**
		 * @return the liveTime
		 */
		public int getLiveTime()
		{
			return liveTime;
		}

		/**
		 * @return the deadTime
		 */
		public int getDeadTime()
		{
			return deadTime;
		}

		/**
		 * @return the spectrum
		 */
		public RNHistogram getSpectrum()
		{
			return spectrum;
		}

		/**
		 * Get gamma counts per second.
		 * @return the countRate
		 */
		public double getCountRate()
		{
			return countRate;
		}

		/**
		 * @return the totalCount
		 */
		public int getTotalCount()
		{
			return totalCount;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(int id)
		{
			this.id = id;
		}

		/**
		 * @param serialNumber the serialNumber to set
		 */
		public void setSerialNumber(int serialNumber)
		{
			this.serialNumber = serialNumber;
		}

		/**
		 * @param status the status to set
		 */
		public void setStatus(boolean status)
		{
			this.status = status;
		}

		/**
		 * @param sampleTime the sampleTime to set
		 */
		public void setSampleTime(int sampleTime)
		{
			this.sampleTime = sampleTime;
		}

		/**
		 * @param highVoltage the highVoltage to set
		 */
		public void setHighVoltage(double highVoltage)
		{
			this.highVoltage = highVoltage;
		}

		/**
		 * @param coarseGain the coarseGain to set
		 */
		public void setCoarseGain(double coarseGain)
		{
			this.coarseGain = coarseGain;
		}

		/**
		 * @param fineGain the fineGain to set
		 */
		public void setFineGain(double fineGain)
		{
			this.fineGain = fineGain;
		}

		/**
		 * @param realTime the realTime to set
		 */
		public void setRealTime(int realTime)
		{
			this.realTime = realTime;
		}

		/**
		 * @param liveTime the liveTime to set
		 */
		public void setLiveTime(int liveTime)
		{
			this.liveTime = liveTime;
		}

		/**
		 * @param deadTime the deadTime to set
		 */
		public void setDeadTime(int deadTime)
		{
			this.deadTime = deadTime;
		}

		/**
		 * @param spectrum the spectrum to set
		 */
		public void setSpectrum(RNHistogram spectrum)
		{
			this.spectrum = spectrum;
		}

		/**
		 * Set gamma counts per second.
		 * 
		 * @param countRate the countRate to set
		 */
		public void setCountRate(double countRate)
		{
			this.countRate = countRate;
		}

		/**
		 * @param totalCount the totalCount to set
		 */
		public void setTotalCount(int totalCount)
		{
			this.totalCount = totalCount;
		}

	}
	
//	private class NeutronDetectorMessage
//	{
//		// TODO Implement when needed
//	}
//	
//	private class TemperatureSensorMessage
//	{
//		// TODO Implement when needed
//	}
//	
//	private class HighFieldDetectorMessage
//	{
//		// TODO Implement when needed
//	}
}