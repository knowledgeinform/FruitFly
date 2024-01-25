#ifndef CLOUDDETECTIONBELIEF_H
#define CLOUDDETECTIONBELIEF_H

#include "stdafx.h"
#include "BeliefTemplate.h"
#include "CloudDetection.h"

#include <list>
using namespace std;

/**
	\class CloudDetectionBelief
	\brief Historical collection of CloudDetection datapoints
	\author John Humphreys
	\date 2011
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class CloudDetectionBelief : public BeliefTemplate
{
	public:
		/**
		\brief Default constructor, generate lists and GLUquadricObj instance
		*/
		CloudDetectionBelief();

		/**
		\brief Destructor, destroy GLUquadricObj instance and delete lists
		*/
		~CloudDetectionBelief();

		/**
		\brief Add newDetection to the appropriate CloudDetection list based on newDetection's source value
		\param newDetection New CloudDetection to store
		\return void
		*/
		void addCloudDetection (CloudDetection* newDetection);

		/**
		\brief Add newDetection to the chemical CloudDetection list
		\param newDetection New CloudDetection to store
		\return void
		*/
		void addChemicalDetection (CloudDetection* newDetection);
		
		/**
		\brief Add newDetection to the particle CloudDetection list
		\param newDetection New CloudDetection to store
		\return void
		*/
		void addParticleDetection (CloudDetection* newDetection);

		/**
		\brief Accessor for chemical detections list
		\return Constant pointer to internal chemical detections list.  Can not be modified
		*/
		const list <CloudDetection*>* const getChemicalDetections() {return m_ChemicalDetections;}
		
		/**
		\brief Accessor for particle detections list
		\return Constant pointer to internal particle detections list.  Can not be modified
		*/
		const list <CloudDetection*>* const getParticleDetections() {return m_ParticleDetections;}

		/**
		\brief Paints the relevant contents of this belief as an overlay in an OpenGL scene.  Assumes screen coordinates are pixel
		dimensions, provided as parameters.  Draws text of total positive detections
		\param glWidth Width pixels of GL scene
		\param glHeight Height pixels of GL scene
		\return void
		*/
		virtual void drawGlOverlay (int glWidth, int glHeight);

		/**
		\brief Paint the contents of this belief in an OpenGL scene.  Display spheres for all cloud detections
		in lists.
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
		void updateDataRows (DataRows* dataRows);

		/**
		\brief Toggle whether particle detections are displayed by calls to drawGL
		\param drawParticleDetections If true, drawGL will paint particle detections.  If false, drawGL will ignore particle detections
		\return void
		*/
		void toggleDisplayParticles (bool drawParticleDetections) {m_DisplayParticleDetections = drawParticleDetections;}
		
		/**
		\brief Toggle whether chemical detections are displayed by calls to drawGL
		\param drawChemicalDetections If true, drawGL will paint chemical detections.  If false, drawGL will ignore chemical detections
		\return void
		*/
		void toggleDisplayChemical (bool drawChemicalDetections) {m_DisplayChemicalDetections = drawChemicalDetections;}

	protected:

		list <CloudDetection*>* m_ChemicalDetections; //!< List of all chemical detections collected
		list <CloudDetection*>* m_ParticleDetections; //!< List of all particle detections collected
		bool m_DisplayChemicalDetections; //!< If true, drawGL will paint particle detections.  If false, drawGL will ignore particle detections
		bool m_DisplayParticleDetections; //!< If true, drawGL will paint chemical detections.  If false, drawGL will ignore chemical detections

		int m_LastParticleDetectionTime; //!< Timestamp of last particle detection
		int m_LastChemicalDetectionTime; //!< Timestamp of last particle chemical

		GLUquadricObj *quadricObj; //!< GL Quadric object to use for displaying detection spheres
		int displayList; //!< GL display list to display detection spheres

		double m_ParticleDetectionColorRed; //!< Particle detection sphere red color component
		double m_ParticleDetectionColorGreen; //!< Particle detection sphere green color component
		double m_ParticleDetectionColorBlue; //!< Particle detection sphere blue color component
		double m_ParticleDetectionColorAlpha; //!< Particle detection sphere alpha color component
		double m_CloudDetectionSizeM; //!< Radius meters of detection spheres

		double m_ChemicalDetectionColorRed; //!< Chemical detection sphere red color component
		double m_ChemicalDetectionColorGreen; //!< Chemical detection sphere green color component
		double m_ChemicalDetectionColorBlue; //!< Chemical detection sphere blue color component
		double m_ChemicalDetectionColorAlpha; //!< Chemical detection sphere alpha color component

		double m_HeaderColorRed; //!< Header text red color component
		double m_HeaderColorGreen; //!< Header text green color component
		double m_HeaderColorBlue; //!< Header text blue color component
		double m_HeaderColorAlpha; //!< Header text alpha color component

		double m_HeaderFontSize; //!< Header text font size
		double m_HeaderHorizontalPixels; //!< Header text horizontal pixels location from left edge
		double m_HeaderVerticalPixels; //!< Header text vertical pixels location from bottom edge

		double m_RowFontSize; //!< Data text font size
		double m_RowHorizontalPixels; //!< Data text horizontal pixels location from left edge
		double m_Row1VerticalPixels; //!< Data text row1 vertical pixels location from bottom edge
		double m_Row2VerticalPixels; //!< Data text row2 vertical pixels location from bottom edge

};

#endif