#ifndef BELIEFCOLLECTION_H
#define BELIEFCOLLECTION_H

#include "stdafx.h"

#include "AgentPositionBelief.h"
#include "ShadowAgentPositionBelief.h"
#include "GCSAgentPositionBelief.h"
#include "CircularOrbitBelief.h"
#include "PiccoloTelemetryBelief.h"
#include "ParticleDetectionBelief.h"
#include "AgentModeBelief.h"
#include "CloudDetectionBelief.h"
#include "AnacondaDetectionBelief.h"
#include "CloudPredictionBelief.h"
#include "ExplosionBelief.h"
#include "MetBelief.h"
#include "RacetrackOrbitBelief.h"
#include "IrExplosionAlgorithmEnabledBelief.h"
#include "PropogatingCloudDetectionBelief.h"
#include "LoiterApproachPathBelief.h"

#include "RawDataDialog.h"
#include "DTEDObject.h"
#include "pthread.h"

/**
	\struct PropogationThreadParams
	\brief Parameters to pass into a thread to allow it to function and end gracefully
	\author John Humphreys
	\date 2011
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
struct PropogationThreadParams
{
	/**
	\brief Default constructor, does nothing
	*/
	PropogationThreadParams () {}

	CRITICAL_SECTION* m_CurrentBeliefsLock; //!< Pointer to lock variable for accessing beliefs
	BeliefTemplate** m_CurrentBeliefs; //!< Pointer to current belief collection
	int m_PropogatedCloudDetectionBeliefIdx; //!< Index in m_BeliefCollection where propogating particles are
	int m_MetBeliefIdx; //!< Index in m_BeliefCollection where wind bearing is
	bool* m_KillThread; //!< If true, this thread should end gracefully.  Set externally
	bool* m_ThreadDone; //!< If true, this thread has ended gracefully.  Set internally
	bool* m_PausePropogation; //!< If true, pause the propogation.  Set externally
};


/**
	\class BeliefCollection
	\brief Collection of WACS Swarm belief objects.  This stores all current beliefs and provides
	access to them.
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class BeliefCollection
{
	public:
		/**
		\brief Default constructor.  Create list of beliefs and initialize lock object
		*/
		BeliefCollection ();

		/**
		\brief Destructor.  Deletes all beliefs and static display objects
		*/
		~BeliefCollection ();

		/**
		\brief Deletes all current beliefs.  List of beliefs will be full of NULL values
		\return void
		*/
		void clearBeliefs();

		/**
		\brief Deletes all current beliefs.  If any beliefs are desired to be initialized on boot, they will be added here.
		Other beliefs will be set to NULL in list
		\return void
		*/
		void resetBeliefs();
		
		/**
		\brief Sets window size of current GL scene.  Used to properly size GL overlays, if needed (like MET belief pointing arrows)
		\param winW Width pixels of GL scene
		\param winH Height pixels of GL scene
		\return void
		*/
		void setWindowSize (int winW, int winH);

		/**
		\brief Sets camera angles of current GL scene.  Used to properly orient GL overlays, if needed (like MET belief pointing direction)
		\param pitchDegrees Current pitch degrees of camera
		\param headingDegrees Current heading degrees of camera
		\return void
		*/
		void setCameraAngles (double pitchDegrees, double headingDegrees);

		/**
		\brief Draw belief overlays to a 2D window, with edge coordinates from 0 to 1
		\param drawAgentPositionBelief If true, display UAV agent in OpenGL
		\param drawGCSAgentPositionBelief If true, display GCS agent in OpenGL
		\param drawCircularOrbitBelief If true, display circular orbit in OpenGL
		\param drawBiopodDetectionBelief If true, display biopod detections in OpenGL (from CloudDetectionBelief, not ParticleDetectionBelief)
		\param anacondaDetectionBelief If true, display Anaconda detections in OpenGL (from CloudDetectionBelief, not AnacondaDetectionBelief)
		\param uavDisplayFromPicTel If true (and if drawAgentPositionBelief is true), UAV display should be done using PiccoloTelemetryBelief data instead of AgentPositionBelief data
		\param drawPredictedCloud If true, display predicted cloud location
		\param drawExplosionBelief If true, display explosion belief indicators
		\param drawMetBelief If true, display Met belief information
		\param drawIrExplosionAlgorithmEnabledBelief If true, display IrExplosionAlgorithmEnabledBelief information
		\param drawSensorOverlay If true, draw sensor overlay data in scene
		\param convertAltitudeToFeet If true, convert altitudes to feet in overlay display
		\return void
		*/
		void drawGlOverlay (bool drawAgentPositionBelief, bool drawGCSAgentPositionBelief, bool drawCircularOrbitBelief, bool drawBiopodDetectionBelief, bool anacondaDetectionBelief, bool uavDisplayFromPicTel, bool drawPredictedCloud, bool drawExplosionBelief, bool drawMetBelief, bool drawIrExplosionAlgorithmEnabledBelief, bool drawSensorOverlays, bool convertAltitudeToFeet);

		/**
		\brief Paints a shaded box behind the sensor overlay text
		\param glWidth Width pixels of GL scene
		\param glHeight Height pixels of GL scene
		\return void
		*/
		void drawSensorOverlayShadedBox (int glWidth, int glHeight);

		/**
		\brief Draw beliefs in OpenGL scene according to boolean flags.
		\param centerPointPosition Offset for all OpenGL locations.  This real-world location corresponds to OpenGL location 0,0,0.
		\param estLatToMConv Estimated conversion from latitude to meters for the area.  Used to map latitudes in meter space
		\param estLonToMConv Estimated conversion from longitude to meters for the area.  Used to map longitudes in meter space
		\param drawAgentPositionBelief If true, display UAV agent in OpenGL
		\param drawGCSAgentPositionBelief If true, display GCS agent in OpenGL
		\param drawCircularOrbitBelief If true, display circular orbit in OpenGL
		\param drawBiopodDetectionBelief If true, display biopod detections in OpenGL (from CloudDetectionBelief, not ParticleDetectionBelief)
		\param anacondaDetectionBelief If true, display Anaconda detections in OpenGL (from CloudDetectionBelief, not AnacondaDetectionBelief)
		\param uavDisplayFromPicTel If true (and if drawAgentPositionBelief is true), UAV display should be done using PiccoloTelemetryBelief data instead of AgentPositionBelief data
		\param drawPredictedCloud If true, display predicted cloud location
		\param drawExplosionBelief If true, display explosion belief indicators
		\param drawMetBelief If true, display Met belief information
		\param drawIrExplosionAlgorithmEnabledBelief If true, display IrExplosionAlgorithmEnabledBelief information
		\param drawPropogatedDetections If true, display propogated CloudDetections
		\param drawLoiterApproachPath If true, draw approach path points
		\return void
		*/
		void drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv, bool drawAgentPositionBelief, bool drawGCSAgentPositionBelief, bool drawCircularOrbitBelief, bool drawBiopodDetectionBelief, bool anacondaDetectionBelief, bool uavDisplayFromPicTel, bool drawPredictedCloud, bool drawExplosionBelief, bool drawMetBelief, bool drawIrExplosionAlgorithmEnabledBelief, bool drawPropogatedDetections, bool drawLoiterApproachPath);
		
		/**
		\brief Start thread to propogate cloud detections
		\return void
		*/
		void startPropogationThread ();

		/**
		\brief Stop thread to propogate cloud detections
		\return void
		*/
		void stopPropogationThread ();

		/**
		\brief Stores the DTEDObject for later use
		\param dted Loaded DTED data for a given region
		\return void
		*/
		void setDtedObject (DTEDObject* dted);

		/**
		\brief Update current UAV AgentPositionBelief object by merging newBelief positions with current history
		\param newBelief New agent position history to merge with current history
		\return True if newBelief was added succesfully.
		*/
		bool updateAgentPositionBelief (AgentPositionBelief* newBelief);

		/**
		\brief Update current GCS AgentPositionBelief object by replacing it with newBelief 
		\param newBelief New GCS position data to use
		\return True if newBelief was added succesfully.
		*/
		bool updateGCSPositionBelief (AgentPositionBelief* newBelief);
		
		/**
		\brief Update current CircularOrbitBelief object by replacing it with newBelief 
		\param newBelief New circular orbit data to use
		\return True if newBelief was added succesfully.
		*/
		bool updateCircularOrbitBelief (CircularOrbitBelief* newBelief);
		
		/**
		\brief Update current PiccoloTelemetryBelief object by replacing it with newBelief 
		\param newBelief New piccolo telemetry data to use
		\return True if newBelief was added succesfully.
		*/
		bool updatePiccoloTelemetryBelief (PiccoloTelemetryBelief* newBelief);
		
		/**
		\brief Update current ParticleDetectionBelief object by replacing it with newBelief 
		\param newDetection New particle detection data to use
		\return True if newBelief was added succesfully.
		*/
		bool updateParticleDetectionBelief (ParticleDetectionBelief* newDetection);
		
		/**
		\brief Update current AnacondaDetectionBelief object by replacing it with newBelief 
		\param newBelief New anaconda detection data to use
		\return True if newBelief was added succesfully.
		*/
		bool updateAnacondaDetectionBelief (AnacondaDetectionBelief* newBelief);
		
		/**
		\brief Update current AgentModeBelief object by replacing it with newBelief 
		\param newBelief New agent mode data to use
		\return True if newBelief was added succesfully.
		*/
		bool updateAgentModeBelief (AgentModeBelief* newBelief);
		
		/**
		\brief Update current CloudDetectionBelief object by adding newDetection to the list
		\param newDetection new CloudDetection object to store
		\return True if newDetection was added succesfully
		*/
		bool updateCloudDetectionBelief (CloudDetection* newDetection);

		/**
		\brief Update current CloudPredictionBelief object by adding newBelief to the list
		\param newBelief new CloudPredictionBelief object to store
		\return True if newBelief was added succesfully
		*/
		bool updateCloudPredictionBelief (CloudPredictionBelief* newBelief);

		/**
		\brief Update current ExplosionBelief object by adding newBelief to the list
		\param newBelief new ExplosionBelief object to store
		\return True if newBelief was added succesfully
		*/
		bool updateExplosionBelief (ExplosionBelief* newBelief);

		/**
		\brief Update current MetBelief object by adding newBelief to the list
		\param newBelief new MetBelief object to store
		\return True if newBelief was added succesfully
		*/
		bool updateMetBelief (MetBelief* newBelief);

		/**
		\brief Update current RacetrackOrbitBelief object by adding newBelief to the list
		\param newBelief new RacetrackOrbitBelief object to store
		\return True if newBelief was added succesfully
		*/
		bool updateRacetrackOrbitBelief (RacetrackOrbitBelief* newBelief);

		/**
		\brief Update current IrExplosionAlgorithmEnabledBelief object by adding newBelief to the list
		\param newBelief new IrExplosionAlgorithmEnabledBelief object to store
		\return True if newBelief was added succesfully
		*/
		bool updateIrExplosionAlgorithmEnabledBelief (IrExplosionAlgorithmEnabledBelief* newBelief);

		/**
		\brief Update current LoiterApproachPathBelief object by adding newBelief to the list
		\param newBelief new LoiterApproachPathBelief object to store
		\return True if newBelief was added succesfully
		*/
		bool updateLoiterApproachPathBelief (LoiterApproachPathBelief* newBelief);

		/**
		\brief Set DataRows object for the belief collection.
		\param dataRows DataRows object to populate with raw data from beliefs
		\return void
		*/
		void setDataRows (DataRows* dataRows) ;
		
		/**
		\brief Display UAV AgentPositionBelief data in the RawDataDialog represented by m_DataRows object
		\return void
		*/
		void displayAgentPositionBeliefRawData ();
		
		/**
		\brief Display GCS AgentPositionBelief data in the RawDataDialog represented by m_DataRows object
		\return void
		*/
		void displayGCSPositionBeliefRawData ();
		
		/**
		\brief Display CircularOrbitBelief data in the RawDataDialog represented by m_DataRows object
		\return void
		*/
		void displayCircularOrbitBeliefRawData ();
		
		/**
		\brief Display PiccoloTelemetryBelief data in the RawDataDialog represented by m_DataRows object
		\return void
		*/
		void displayPiccoloTelemetryBeliefRawData ();
		
		/**
		\brief Display ParticleDetectionBelief data in the RawDataDialog represented by m_DataRows object
		\return void
		*/
		void displayParticleDetectionBeliefRawData ();
		
		/**
		\brief Display AnacondaDetectionBelief data in the RawDataDialog represented by m_DataRows object
		\return void
		*/
		void displayAnacondaDetectionBeliefRawData ();
		
		/**
		\brief Display AgentModeBelief data in the RawDataDialog represented by m_DataRows object
		\return void
		*/
		void displayAgentModeBeliefRawData ();
		
		/**
		\brief Display CloudDetectionBelief data in the RawDataDialog represented by m_DataRows object
		\return void
		*/
		void displayCloudDetectionBeliefRawData ();

		/**
		\brief Display CloudPredictionBelief data in the RawDataDialog represented by m_DataRows object
		\return void
		*/
		void displayCloudPredictionBeliefRawData ();
		
		/**
		\brief Display ExplosionBelief data in the RawDataDialog represented by m_DataRows object
		\return void
		*/
		void displayExplosionBeliefRawData ();

		/**
		\brief Display MetBelief data in the RawDataDialog represented by m_DataRows object
		\return void
		*/
		void displayMetBeliefRawData ();

		/**
		\brief Display RacetrackOrbitBelief data in the RawDataDialog represented by m_DataRows object
		\return void
		*/
		void displayRacetrackOrbitBeliefRawData ();
		
		/**
		\brief Display IrExplosionAlgorithmEnabledBelief data in the RawDataDialog represented by m_DataRows object
		\return void
		*/
		void displayIrExplosionAlgorithmEnabledBeliefRawData ();

		/**
		\brief Display LoiterApproachPathBelief data in the RawDataDialog represented by m_DataRows object
		\return void
		*/
		void displayLoiterApproachPathBeliefRawData ();
		
		/**
		\brief Clear all rows in the RawDataDialog represented by m_DataRows object
		\return void
		*/
		void clearDisplayRawData();

		/**
		\brief Display the selected belief's data [m_RawDataDisplayOpt] in the RawDataDialog represented
		by m_DataRows object
		\return void
		*/
		void updateRawDataDialog ();

	private:
		static const int m_BeliefListSize = 25; //!< Size of belief collection.
		BeliefTemplate** m_CurrentBeliefs; //!< List of current beliefs of each type
		CRITICAL_SECTION m_CurrentBeliefsLock; //!< Lock object for current beliefs array

		static const int m_AgentPositionBeliefIdx = 0; //!< Index in belief collection for UAV AgentPositionBelief
		static const int m_CircularOrbitBeliefIdx = 1; //!< Index in belief collection for CircularOrbitBelief
		static const int m_ParticleDetectionBeliefIdx = 2; //!< Index in belief collection for ParticleDetectionBelief
		static const int m_AnacondaDetectionBeliefIdx = 3; //!< Index in belief collection for AnacondaDetectionBelief
		static const int m_GCSPositionBeliefIdx = 4; //!< Index in belief collection for GCS AgentPositionBelief
		static const int m_PiccoloTelemetryBeliefIdx = 5; //!< Index in belief collection for PiccoloTelemetryBelief
		static const int m_AgentModeBeliefIdx = 6; //!< Index in belief collection for AgentModeBelief
		static const int m_CloudDetectionBeliefIdx = 7; //!< Index in belief collection for CloudDetectionBelief
		static const int m_CloudPredictionBeliefIdx = 8; //!< Index in belief collection for CloudPredictionBelief
		static const int m_ExplosionBeliefIdx = 9; //!< Index in belief collection for ExplosionBelief
		static const int m_MetBeliefIdx = 10; //!< Index in belief collection for MetBelief
		static const int m_RacetrackOrbitBeliefIdx = 11; //!< Index in belief collection for RacetrackOrbitBelief
		static const int m_IrExplosionAlgorithmEnabledBeliefIdx = 12; //!< Index in belief collection for IrExplosionAlgorithmEnabledBelief		
		static const int m_PropogatedCloudDetectionBeliefIdx = 13; //!< Index in belief collection for PropogatedCloudDetectionBelief		
		static const int m_LoiterApproachPathBeliefIdx = 14; //!< Index in belief collection for LoiterApproachPathBelief		

		double m_GlPitchDegrees; //!< Current pitch of camera in OpenGL scene
		double m_GlHeadingDegrees; //!< Current heading of camera in OpenGL scene
		double m_GlWidth; //!< Current width pixels of OpenGL scene
		double m_GlHeight; //!< Current height pixels of OpenGL scene

		int m_RawDataDisplayOpt; //!< Index of the belief to be displayed in the RawDataDialog represented by m_DataRows object
		DataRows* m_DataRows; //!< Stores label boxes of RawDataDialog to display raw data of beliefs to

		double m_ShadedBoxColorRed; //!< Shaded box text red color component
		double m_ShadedBoxColorGreen; //!< Shaded box text green color component
		double m_ShadedBoxColorBlue; //!< Shaded box text blue color component
		double m_ShadedBoxColorAlpha; //!< Shaded box text alpha color component
		double m_ShadedBoxHeightPixels; //!< Shaded box pixels height from bottom edge
		
		DTEDObject* m_DtedObject; //!< DTED Data loaded for map

		pthread_t m_PropogationThread; //!< Thread to propogate cloud detections
		PropogationThreadParams* m_PropogationThreadParams;
		bool m_KillPropogationThread; //!< If true, the propogation thread should end gracefully
		bool m_PropogationThreadDone; //!< If true, the propogation thread has ended gracefully
};

#endif