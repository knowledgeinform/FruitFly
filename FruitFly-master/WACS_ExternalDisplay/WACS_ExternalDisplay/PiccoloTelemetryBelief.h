#ifndef PICCOLOTELEMETRYBELIEF_H
#define PICCOLOTELEMETRYBELIEF_H

#include "stdafx.h"
#include "BeliefTemplate.h"
#include "ShadowUavDisplay.h"
#include "DakotaUavDisplay.h"

/**
	\class PiccoloTelemetryBelief
	\brief Class representing a WACS Swarm belief - telemetry from on-board Piccolo autopilot
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class PiccoloTelemetryBelief : public BeliefTemplate
{
	public:
		/**
		\brief Default constructor, sets invalid values
		*/
		PiccoloTelemetryBelief();

		/**
		\brief Constructor, sets specified parameters
		\param altitudeWGS84_m Altitude above WGS84 ellipsoid, meters
		\param altitudeWGS84_m Altitude above MSL, meters
		\param GpsStatus GPS status flags
		\param indicatedAirSpeed_mps IAS, meters per second
		\param latitude_deg Latitude decimal degrees
		\param longitude_deg Longitude decimal degrees
		\param gpsPDOP GPS PDOP value
		\param pitch_deg Pitch decimal degrees
		\param roll_deg Roll decimal degrees
		\param trueHeading_deg True heading decimal degrees
		\param velocityDown_mps Velocity down component, meters per second
		\param velocityEast_mps Velocity East component, meters per second
		\param velocityNorth_mps Velocity North component, meters per second
		\param windFromSouth_mps Wind from South component, meters per second
		\param windFromWest_mps Wind from West component, meters per second
		\param yaw_deg Yaw decimal degreers
		\param staticPressure_pa Static pressure Pascals
		\param outsideAirTemperature_C OAT, Celcius
		\param altLaser_m Laser altimeter range, meters
		\param altLaserValid If true, altLaser_m is valid data.  If false, invalid data
		*/
		PiccoloTelemetryBelief(double altitudeWGS84_m, double altitudeMSL_m, int GpsStatus, double indicatedAirSpeed_mps, double latitude_deg, double longitude_deg, double gpsPDOP, double pitch_deg, double roll_deg, double trueHeading_deg, double velocityDown_mps, double velocityEast_mps, double velocityNorth_mps, double windFromSouth_mps, double windFromWest_mps, double yaw_deg, double staticPressure_pa, double outsideAirTemperature_C, double altLaser_m, bool altLaserValid);

		/**
		\brief Destructor
		*/
		~PiccoloTelemetryBelief();

		/**
		\brief Modifier for whether altitude will be reported in feet in overlay display
		\param convertAltitudeToFeet If true, altitude will be feet.  If false, meters
		\return void
		*/
		void convertAltitudeToFeet (bool convertAltitudeToFeet) {m_ConvertAltitudeToFeet = convertAltitudeToFeet;}

		/**
		\brief Paints the relevant contents of this belief as an overlay in an OpenGL scene.  Assumes screen coordinates are pixel
		dimensions, provided as parameters.  Draws text of current altitude
		\param glWidth Width pixels of GL scene
		\param glHeight Height pixels of GL scene
		\return void
		*/
		virtual void drawGlOverlay (int glWidth, int glHeight);

		/**
		\brief Paint the contents of this belief in an OpenGL scene.  Display UAV with proper location and orientation.

		\param centerPointPosition Offset for all OpenGL locations.  This real-world location corresponds to OpenGL location 0,0,0.
		\param estLatToMConv Estimated conversion from latitude to meters for the area.  Used to map latitudes in meter space
		\param estLonToMConv Estimated conversion from longitude to meters for the area.  Used to map longitudes in meter space
		\return void
		*/
		void drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv);
		
		/**
		\brief Update RawDataDialog object with data from this belief.  Rows of dialog are filled with 
		contents of private variables.
		\param dataRows List of data rows in a RawDataDialog object to be updated with data for this belief
		\return void
		*/
		virtual void updateDataRows (DataRows* dataRows);
		
		/**
		\brief Accessor for altitude
		\return Altitude above WGS84, meters
		*/
		double getAltitudeWGS84_m () {return m_AltitudeWGS84_m;}

		/**
		\brief Accessor for altitude
		\return Altitude above MSL, meters
		*/
		double getAltitudeMSL_m () {return m_AltitudeMSL_m;}
		
		/**
		\brief Accessor for GPS status
		\return GPS status
		*/
		int getGpsStatus() {return m_GpsStatus;}
		
		/**
		\brief Accessor for IAS
		\return IAS, meters per second
		*/
		double getIndicatedAirSpeed_mps() {return m_IndicatedAirSpeed_mps;}
		
		/**
		\brief Accessor for Latitude
		\return Latitude, decimal degrees
		*/
		double getLatitude_deg() {return m_Latitude_deg;}
		
		/**
		\brief Accessor for Longitude
		\return Longitude, decimal degrees
		*/
		double getLongitude_deg() {return m_Longitude_deg;}
		
		/**
		\brief Accessor for GPS PDOP
		\return GPS PDOP
		*/
		double getGpsPDOP() {return m_GpsPDOP;}
		
		/**
		\brief Accessor for pitch
		\return Pitch decimal degrees
		*/
		double getPitch_deg() {return m_Pitch_deg;}
		
		/**
		\brief Accessor for roll
		\return Roll decimal degrees
		*/
		double getRoll_deg() {return m_Roll_deg;}
		
		/**
		\brief Accessor for true heading
		\return True heading from magnetometer, decimal degrees
		*/
		double getTrueHeading_deg() {return m_TrueHeading_deg;}
		
		/**
		\brief Accessor for velocity down component
		\return velocity down component, meters per second
		*/
		double getVelocityDown_mps() {return m_VelocityDown_mps;}
		
		/**
		\brief Accessor for velocity east component
		\return velocity east component, meters per second
		*/
		double getVelocityEast_mps() {return m_VelocityEast_mps;}
		
		/**
		\brief Accessor for velocity north component
		\return velocity north component, meters per second
		*/
		double getVelocityNorth_mps() {return m_VelocityNorth_mps;}
		
		/**
		\brief Accessor for wind from south component
		\return wind from south component, meters per second
		*/
		double getWindFromSouth_mps() {return m_WindFromSouth_mps;}
		
		/**
		\brief Accessor for wind from west component
		\return wind from west component, meters per second
		*/
		double getWindFromWest_mps() {return m_WindFromWest_mps;}
		
		/**
		\brief Accessor for yaw
		\return Yaw, decimal degrees
		*/
		double getYaw_deg() {return m_Yaw_deg;}
		
		/**
		\brief Accessor for static pressure
		\return Static pressure, Pa
		*/
		double getStaticPressure_pa() {return m_StaticPressure_pa;}
		
		/**
		\brief Accessor for OAT
		\return OAT, Celcius
		*/
		double getOutsideAirTemperature_C() {return m_OutsideAirTemperature_C;}
		
		/**
		\brief Accessor for laser altimeter reading
		\return Laser AGL reading, meters
		*/
		double getAltLaser_m() {return m_AltLaser_m;}
		
		/**
		\brief Accessor for whether laser altimeter reading is valid or not
		\return True if laser altimeter is valid, false if invalid
		*/
		bool getAltLaserValid() {return m_AltLaserValid;}
		
		/**
		\brief Modifier for altitude
		\param altitudeWGS84_m New altitude above WGS84 ellipsoid, meters
		\return void
		*/
		void setAltitudeWGS84_m (double altitudeWGS84_m) {m_AltitudeWGS84_m = altitudeWGS84_m;}
		
		/**
		\brief Modifier for altitude
		\param altitudeWGS84_m New altitude above MSL, meters
		\return void
		*/
		void setAltitudeMSL_m (double altitudeMSL_m) {m_AltitudeMSL_m = altitudeMSL_m;}
		
		/**
		\brief Modifier for GPS status flags
		\param GpsStatus New GPS status flags
		\return void
		*/
		void setGpsStatus(int GpsStatus) {m_GpsStatus = GpsStatus;}
		
		/**
		\brief Modifier for IAS
		\param indicatedAirSpeed_mps New IAS meters per second
		\return void
		*/
		void setIndicatedAirSpeed_mps(double indicatedAirSpeed_mps) {m_IndicatedAirSpeed_mps = indicatedAirSpeed_mps;}
		
		/**
		\brief Modifier for latitude
		\param latitude_deg New latitude decimal degrees
		\return void
		*/
		void setLatitude_deg(double latitude_deg) {m_Latitude_deg = latitude_deg;}
		
		/**
		\brief Modifier for longitude
		\param longitude_deg New longitude decimal degrees
		\return void
		*/
		void setLongitude_deg(double longitude_deg) {m_Longitude_deg = longitude_deg;}
		
		/**
		\brief Modifier for GPS PDOP
		\param gpsPDOP new GPS PDOP value
		\return void
		*/
		void setGpsPDOP(double gpsPDOP) {m_GpsPDOP = gpsPDOP;}
		
		/**
		\brief Modifier for pitch 
		\param pitch_deg New pitch decimal degrees
		\return void
		*/
		void setPitch_deg(double pitch_deg) {m_Pitch_deg = pitch_deg;}
		
		/**
		\brief Modifier for roll
		\param roll_deg New roll decimal degrees
		\return void
		*/
		void setRoll_deg(double roll_deg) {m_Roll_deg = roll_deg;}
		
		/**
		\brief Modifier for true heading
		\param trueHeading_deg New true heading decimal degrees
		\return void
		*/
		void setTrueHeading_deg(double trueHeading_deg) {m_TrueHeading_deg = trueHeading_deg;}
		
		/**
		\brief Modifier for velocity down
		\param velocityDown_mps New velocity down-component, meters per second
		\return void
		*/
		void setVelocityDown_mps(double velocityDown_mps) {m_VelocityDown_mps = velocityDown_mps;}
		
		/**
		\brief Modifier for velocity east
		\param velocityEast_mps New velocity east-component, meters per second
		\return void
		*/
		void setVelocityEast_mps(double velocityEast_mps) {m_VelocityEast_mps = velocityEast_mps;}
		
		/**
		\brief Modifier for velocity north
		\param velocityNorth_mps New velocity north-component, meters per second
		\return void
		*/
		void setVelocityNorth_mps(double velocityNorth_mps) {m_VelocityNorth_mps = velocityNorth_mps;}
		
		/**
		\brief Modifier for wind from south
		\param windFromSouth_mps New wind from south meters per second
		\return void
		*/
		void setWindFromSouth_mps(double windFromSouth_mps) {m_WindFromSouth_mps = windFromSouth_mps;}
		
		/**
		\brief Modifier for wind from west
		\param windFromWest_mps New wind from west meters per second
		\return void
		*/
		void setWindFromWest_mps(double windFromWest_mps) {m_WindFromWest_mps = windFromWest_mps;}
		
		/**
		\brief Modifier for yaw 
		\param yaw_deg New yaw decimal degrees
		\return void
		*/
		void setYaw_deg(double yaw_deg) {m_Yaw_deg = yaw_deg;}
		
		/**
		\brief Modifier for static pressure
		\param staticPressure_pa New static pressure value, pascals
		\return void
		*/
		void setStaticPressure_pa(double staticPressure_pa) {m_StaticPressure_pa = staticPressure_pa;}
		
		/**
		\brief Modifier for OAT
		\param outsideAirTemperature_C New OAT value, celcius
		\return void
		*/
		void setOutsideAirTemperature_C(double outsideAirTemperature_C) {m_OutsideAirTemperature_C = outsideAirTemperature_C;}
		
		/**
		\brief Modifier for laser AGL value
		\param altLaser_m new laser AGL reading, meters
		\return void
		*/
		void setAltLaser_m(double altLaser_m) {m_AltLaser_m = altLaser_m;}
		
		/**
		\brief Modifier for whether laser AGL value is valid
		\param altLaserValid True if laser AGL value is valid, false if invalid
		\return void
		*/
		void setAltLaserValid(bool altLaserValid) {m_AltLaserValid = altLaserValid;}

		/**
		\brief Modifier for DTED-calculated AGL value
		\param altDtedAgl_m new DTED-calculated AGL, meters
		\return void
		*/
		void setAltDtedAgl_m (double altDtedAgl_m) {m_AltDtedAgl_m = altDtedAgl_m;}

		static UavDisplayBase *m_UavDisplay; //!< UavDisplay object that displays a UAV with wing-mounted pods

	private:

		double m_AltitudeWGS84_m; //!< Altitude above WGS84 ellipsoid, meters
		double m_AltitudeMSL_m; //!< Altitude above MSL, meters
		int m_GpsStatus; //!< GPS status flags
		double m_IndicatedAirSpeed_mps; //!< IAS, meters per second
		double m_Latitude_deg; //!< Latitude decimal degrees
		double m_Longitude_deg;	 //!< Longitude decimal degrees
		double m_GpsPDOP; //!< GPS PDOP value
		double m_Pitch_deg; //!< Pitch decimal degrees
		double m_Roll_deg; //!< Roll decimal degrees
		double m_TrueHeading_deg; //!< True heading decimal degrees
		double m_VelocityDown_mps; //!< Velocity down component, meters per second
		double m_VelocityEast_mps; //!< Velocity East component, meters per second
		double m_VelocityNorth_mps; //!< Velocity North component, meters per second
		double m_WindFromSouth_mps; //!< Wind from South component, meters per second
		double m_WindFromWest_mps; //!< Wind from West component, meters per second
		double m_Yaw_deg; //!< Yaw decimal degreers
		double m_StaticPressure_pa; //!< Static pressure Pascals
		double m_OutsideAirTemperature_C; //!< OAT, Celcius
		double m_AltLaser_m; //!< Laser altimeter range, meters
		bool m_AltLaserValid; //!< If true, m_AltLaser_m is valid data.  If false, invalid data
		double m_AltDtedAgl_m; //!< AGL altitude of UAV calculated using barometric altitude and DTED ground data

		double m_HeaderColorRed; //!< Header text red color component
		double m_HeaderColorGreen; //!< Header text green color component
		double m_HeaderColorBlue; //!< Header text blue color component
		double m_HeaderColorAlpha; //!< Header text alpha color component

		double m_HeaderFontSize; //!< Header text font size
		double m_HeaderHorizontalPixels; //!< Header text horizontal pixels location from left edge
		double m_HeaderVerticalPixels; //!< Header text vertical pixels location from bottom edge

		double m_TextColorRed; //!< Text red color component
		double m_TextColorGreen; //!< Text green color component
		double m_TextColorBlue; //!< Text blue color component
		double m_TextColorAlpha; //!< Text alpha color component

		double m_RowFontSize; //!< Data text font size
		double m_RowHorizontalPixels; //!< Data text horizontal pixels location from left edge
		double m_Row1VerticalPixels; //!< Data text row1 vertical pixels location from bottom edge
		double m_Row2VerticalPixels; //!< Data text row2 vertical pixels location from bottom edge

		UavDisplayBase::UAV_TYPES m_UavType; //!< Type of UAV to display
		bool m_ConvertAltitudeToFeet; //!< If true, display altitude in feet.  Otherwise, meters

		/**
		\brief Read config settings from file
		\return void
		*/
		void readConfig();
};


#endif



