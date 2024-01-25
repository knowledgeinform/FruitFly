#ifndef CLOUDPREDICTIONBELIEF_H
#define CLOUDPREDICTIONBELIEF_H

#include "stdafx.h"
#include "BeliefTemplate.h"

/**
	\class CloudPredictionBelief
	\brief Class representing a WACS Swarm belief - predicted cloud location based on propogation and degradation of past positive detections
	\author John Humphreys
	\date 2011
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class CloudPredictionBelief : public BeliefTemplate
{
	public:
		/**
		\brief Default constructor, sets invalid values
		*/
		CloudPredictionBelief();

		/**
		\brief Constructor, sets specified parameters
		\param latitude of NW corner of matrix, decimal degrees
		\param longitude of NW corner of matrix, decimal degrees
		\param altitude of bottom of matrix, MSL meters
		\param latitude of SE corner of matrix, decimal degrees
		\param longitude of SE corner of matrix, decimal degrees
		\param altitude of top of matrix, MSL meters
		\param number of horizontal cells in matrix
		\param number of vertical cells in matrix
		\param length of cells in matrix, meters
		\param minimum estimated cloud altitude, MSL meters
		\param maximum estimated cloud altitude, MSL meters
		\param current predicted center (most dense) of cloud, latitude decimal degrees
		\param current predicted center (most dense) of cloud, longitude decimal degrees
		\param current predicted center (most dense) of cloud, altitude MSL meters
		\param predicted center (most dense) of cloud when UAV intercepts cloud next, latitude decimal degrees
		\param predicted center (most dense) of cloud when UAV intercepts cloud next, longitude decimal degrees
		\param predicted center (most dense) of cloud when UAV intercepts cloud next, altitude MSL meters
		*/
		CloudPredictionBelief(double matrixBottomNWCornerPosition_LatDeg,
								double matrixBottomNWCornerPosition_LonDeg,
								double matrixBottomNWCornerAltitudeMSL_m,
								double matrixTopSECornerPosition_LatDeg,
								double matrixTopSECornerPosition_LonDeg,
								double matrixTopSECornerAltitudeMSL_m,
								int matrixSizeX_Cells,
								int matrixSizeY_Cells,
								double matrixCellHorizontalSideLength_m,
								double cloudMinAltitudeMsl_m,
								double cloudMaxAltitudeMsl_m,
								double predictedCloudCenterPosition_LatDeg,
								double predictedCloudCenterPosition_LonDeg,
								double predictedCloudCenterAltitudeMSL_m,
								double predictedCloudInterceptPosition_LatDeg,
								double predictedCloudInterceptPosition_LonDeg,
								double predictedCloudInterceptAltitudeMSL_m
								);

		/**
		\brief Destructor
		*/
		~CloudPredictionBelief();

		/**
		\brief Destroys the static OpenGL objects
		\return void
		*/
		static void destroyGlObjects ();

		/**
		\brief Paint the contents of this belief in an OpenGL scene.  Display plume center location.

		\param centerPointPosition Offset for all OpenGL locations.  This real-world location corresponds to OpenGL location 0,0,0.
		\param estLatToMConv Estimated conversion from latitude to meters for the area.  Used to map latitudes in meter space
		\param estLonToMConv Estimated conversion from longitude to meters for the area.  Used to map longitudes in meter space
		\return void
		*/
		virtual void drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv);
		
		/**
		\brief Update RawDataDialog object with data from this belief.  Rows of dialog are filled with 
		contents of private variables.
		\param dataRows List of data rows in a RawDataDialog object to be updated with data for this belief
		\return void
		*/
		virtual void updateDataRows (DataRows* dataRows);
		
		
		
		/**
		\brief Accessor for latitude of NW corner of matrix 
		\return latitude of NW corner of matrix, decimal degrees
		*/
		double getMatrixBottomNWCornerPosition_LatDeg ()
		{
			return m_MatrixBottomNWCornerPosition_LatDeg;
		}

		/**
		\brief Accessor for longitude of NW corner of matrix
		\return longitude of NW corner of matrix, decimal degrees
		*/
		double getMatrixBottomNWCornerPosition_LonDeg ()
		{
			return m_MatrixBottomNWCornerPosition_LonDeg;
		}

		/**
		\brief Accessor for altitude of bottom of matrix
		\return altitude of bottom of matrix, MSL meters
		*/
		double getMatrixBottomNWCornerAltitudeMSL_m()
		{
			return m_MatrixBottomNWCornerAltitudeMSL_m;
		}

		/**
		\brief Accessor for latitude of SE corner of matrix
		\return latitude of SE corner of matrix, decimal degrees
		*/
		double getMatrixTopSECornerPosition_LatDeg ()
		{
			return m_MatrixTopSECornerPosition_LatDeg;
		}

		/**
		\brief Accessor for longitude of SE corner of matrix
		\return longitude of SE corner of matrix, decimal degrees
		*/
		double getMatrixTopSECornerPosition_LonDeg ()
		{
			return m_MatrixTopSECornerPosition_LonDeg;
		}

		/**
		\brief Accessor for altitude of top of matrix
		\return altitude of top of matrix, MSL meters
		*/
		double getMatrixTopSECornerAltitudeMSL_m()
		{
			return m_MatrixTopSECornerAltitudeMSL_m;
		}

		/**
		\brief Accessor for number of horizontal cells in matrix
		\return number of horizontal cells in matrix
		*/
		int getMatrixSizeX_Cell ()
		{
			return m_MatrixSizeX_Cells;
		}

		/**
		\brief Accessor for number of vertical cells in matrix
		\return number of vertical cells in matrix
		*/
		int getMatrixSizeY_Cells ()
		{
			return m_MatrixSizeY_Cells;
		}

		/**
		\brief Accessor for length of cells in matrix
		\return length of cells in matrix, meters
		*/
		double getMatrixCellHorizontalSideLength_m ()
		{
			return m_MatrixCellHorizontalSideLength_m;
		}

		/**
		\brief Accessor for minimum estimated cloud altitude
		\return minimum estimated cloud altitude, MSL meters
		*/
		double getCloudMinAltitudeMsl_m ()
		{
			return m_CloudMinAltitudeMsl_m;
		}

		/**
		\brief Accessor for maximum estimated cloud altitude
		\return maximum estimated cloud altitude, MSL meters
		*/
		double getCloudMaxAltitudeMsl_m ()
		{
			return m_CloudMaxAltitudeMsl_m;
		}

		/**
		\brief Accessor for current predicted center (most dense) of cloud, latitude
		\return current predicted center (most dense) of cloud, latitude decimal degrees
		*/
		double getPredictedCloudCenterPosition_LatDeg ()
		{
			return m_PredictedCloudCenterPosition_LatDeg;
		}

		/**
		\brief Accessor for current predicted center (most dense) of cloud, longitude
		\return current predicted center (most dense) of cloud, longitude decimal degrees
		*/
		double getPredictedCloudCenterPosition_LonDeg ()
		{
			return m_PredictedCloudCenterPosition_LonDeg;
		}

		/**
		\brief Accessor for current predicted center (most dense) of cloud, altitude
		\return current predicted center (most dense) of cloud, altitude MSL meters
		*/
		double getPredictedCloudCenterAltitudeMSL_m ()
		{
			return m_PredictedCloudCenterAltitudeMSL_m;
		}

		/**
		\brief Accessor for predicted center (most dense) of cloud when UAV intercepts cloud next, latitude
		\return predicted center (most dense) of cloud when UAV intercepts cloud next, latitude decimal degrees
		*/
		double getPredictedCloudInterceptPosition_LatDeg ()
		{
			return m_PredictedCloudInterceptPosition_LatDeg;
		}

		/**
		\brief Accessor for predicted center (most dense) of cloud when UAV intercepts cloud next, longitude
		\return predicted center (most dense) of cloud when UAV intercepts cloud next, longitude decimal degrees
		*/
		double getPredictedCloudInterceptPosition_LonDeg ()
		{
			return m_PredictedCloudInterceptPosition_LonDeg;
		}

		/**
		\brief Accessor for predicted center (most dense) of cloud when UAV intercepts cloud next, altitude
		\return predicted center (most dense) of cloud when UAV intercepts cloud next, altitude MSL meters
		*/
		double getPredictedCloudInterceptAltitudeMSL_m ()
		{
			return m_PredictedCloudInterceptAltitudeMSL_m;
		}



		/**
		\brief Modifier for latitude of NW corner of matrix 
		\param latitude of NW corner of matrix, decimal degrees
		\return void
		*/
		void setMatrixBottomNWCornerPosition_LatDeg (double matrixBottomNWCornerPosition_LatDeg)
		{
			m_MatrixBottomNWCornerPosition_LatDeg = matrixBottomNWCornerPosition_LatDeg;
		}

		/**
		\brief Modifier for longitude of NW corner of matrix
		\param longitude of NW corner of matrix, decimal degrees
		\return void
		*/
		void setMatrixBottomNWCornerPosition_LonDeg (double matrixBottomNWCornerPosition_LonDeg)
		{
			m_MatrixBottomNWCornerPosition_LonDeg = matrixBottomNWCornerPosition_LonDeg;
		}

		/**
		\brief Modifier for altitude of bottom of matrix
		\param altitude of bottom of matrix, MSL meters
		\return void
		*/
		void setMatrixBottomNWCornerAltitudeMSL_m(double matrixBottomNWCornerAltitudeMSL_m)
		{
			m_MatrixBottomNWCornerAltitudeMSL_m = matrixBottomNWCornerAltitudeMSL_m;
		}

		/**
		\brief Modifier for latitude of SE corner of matrix
		\param latitude of SE corner of matrix, decimal degrees
		\return void
		*/
		void setMatrixTopSECornerPosition_LatDeg (double matrixTopSECornerPosition_LatDeg)
		{
			m_MatrixTopSECornerPosition_LatDeg = matrixTopSECornerPosition_LatDeg;
		}

		/**
		\brief Modifier for longitude of SE corner of matrix
		\param longitude of SE corner of matrix, decimal degrees
		\return void
		*/
		void setMatrixTopSECornerPosition_LonDeg (double matrixTopSECornerPosition_LonDeg)
		{
			m_MatrixTopSECornerPosition_LonDeg = matrixTopSECornerPosition_LonDeg;
		}

		/**
		\brief Modifier for altitude of top of matrix
		\param altitude of top of matrix, MSL meters
		\return void
		*/
		void setMatrixTopSECornerAltitudeMSL_m(double matrixTopSECornerAltitudeMSL_m)
		{
			m_MatrixTopSECornerAltitudeMSL_m = matrixTopSECornerAltitudeMSL_m;
		}

		/**
		\brief Modifier for number of horizontal cells in matrix
		\param number of horizontal cells in matrix
		\return void
		*/
		void setMatrixSizeX_Cell (int matrixSizeX_Cells)
		{
			m_MatrixSizeX_Cells = matrixSizeX_Cells;
		}

		/**
		\brief Modifier for number of vertical cells in matrix
		\param number of vertical cells in matrix
		\return void
		*/
		void setMatrixSizeY_Cells (int matrixSizeY_Cells)
		{
			m_MatrixSizeY_Cells = matrixSizeY_Cells;
		}

		/**
		\brief Modifier for length of cells in matrix
		\param length of cells in matrix, meters
		\return void
		*/
		void setMatrixCellHorizontalSideLength_m (double matrixCellHorizontalSideLength_m)
		{
			m_MatrixCellHorizontalSideLength_m = matrixCellHorizontalSideLength_m;
		}

		/**
		\brief Modifier for minimum estimated cloud altitude
		\param minimum estimated cloud altitude, MSL meters
		\return void
		*/
		void setCloudMinAltitudeMsl_m (double cloudMinAltitudeMsl_m)
		{
			m_CloudMinAltitudeMsl_m = cloudMinAltitudeMsl_m;
		}

		/**
		\brief Modifier for maximum estimated cloud altitude
		\param maximum estimated cloud altitude, MSL meters
		\return void
		*/
		void setCloudMaxAltitudeMsl_m (double cloudMaxAltitudeMsl_m)
		{
			m_CloudMaxAltitudeMsl_m = cloudMaxAltitudeMsl_m;
		}

		/**
		\brief Modifier for current predicted center (most dense) of cloud, latitude
		\param current predicted center (most dense) of cloud, latitude decimal degrees
		\return void
		*/
		void setPredictedCloudCenterPosition_LatDeg (double predictedCloudCenterPosition_LatDeg)
		{
			m_PredictedCloudCenterPosition_LatDeg = predictedCloudCenterPosition_LatDeg;
		}

		/**
		\brief Modifier for current predicted center (most dense) of cloud, longitude
		\param current predicted center (most dense) of cloud, longitude decimal degrees
		\return void
		*/
		void setPredictedCloudCenterPosition_LonDeg (double predictedCloudCenterPosition_LonDeg)
		{
			m_PredictedCloudCenterPosition_LonDeg = predictedCloudCenterPosition_LonDeg;
		}

		/**
		\brief Modifier for current predicted center (most dense) of cloud, altitude
		\param current predicted center (most dense) of cloud, altitude MSL meters
		\return void
		*/
		void setPredictedCloudCenterAltitudeMSL_m (double predictedCloudCenterAltitudeMSL_m)
		{
			m_PredictedCloudCenterAltitudeMSL_m = predictedCloudCenterAltitudeMSL_m;
		}

		/**
		\brief Modifier for predicted center (most dense) of cloud when UAV intercepts cloud next, latitude
		\param predicted center (most dense) of cloud when UAV intercepts cloud next, latitude decimal degrees
		\return void
		*/
		void setPredictedCloudInterceptPosition_LatDeg (double predictedCloudInterceptPosition_LatDeg)
		{
			m_PredictedCloudInterceptPosition_LatDeg = predictedCloudInterceptPosition_LatDeg;
		}

		/**
		\brief Modifier for predicted center (most dense) of cloud when UAV intercepts cloud next, longitude
		\param predicted center (most dense) of cloud when UAV intercepts cloud next, longitude decimal degrees
		\return void
		*/
		void setPredictedCloudInterceptPosition_LonDeg (double predictedCloudInterceptPosition_LonDeg)
		{
			m_PredictedCloudInterceptPosition_LonDeg = predictedCloudInterceptPosition_LonDeg;
		}

		/**
		\brief Modifier for predicted center (most dense) of cloud when UAV intercepts cloud next, altitude
		\param predicted center (most dense) of cloud when UAV intercepts cloud next, altitude MSL meters
		\return void
		*/
		void setPredictedCloudInterceptAltitudeMSL_m (double predictedCloudInterceptAltitudeMSL_m)
		{
			m_PredictedCloudInterceptAltitudeMSL_m = predictedCloudInterceptAltitudeMSL_m;
		}


	private:

		double m_MatrixBottomNWCornerPosition_LatDeg; //!< Latitude of NW corner of cloud matrix
		double m_MatrixBottomNWCornerPosition_LonDeg; //!< Longitude of NW corner of cloud matrix
		double m_MatrixBottomNWCornerAltitudeMSL_m; //!< Altitude MSL meters of bottom of cloud matrix
		double m_MatrixTopSECornerPosition_LatDeg; //!< Latitude of SE corner of cloud matrix
		double m_MatrixTopSECornerPosition_LonDeg; //!< Longitude of SE corner of cloud matrix
		double m_MatrixTopSECornerAltitudeMSL_m; //!< Altitude MSL meters of top of cloud matrix
		int m_MatrixSizeX_Cells; //!< Horizontal (longitude) cell count of cloud matrix
		int m_MatrixSizeY_Cells; //!< Vertical (latitude) cell count of cloud matrix
		double m_MatrixCellHorizontalSideLength_m; //!< Side length of a cell, meters
		double m_CloudMinAltitudeMsl_m; //!< Minimum cloud altitude based on particle distribution, meters MSL
		double m_CloudMaxAltitudeMsl_m; //!< Maximum cloud altitude based on particle distribution, meters MSL
		double m_PredictedCloudCenterPosition_LatDeg; //!< Predicted center (heaviest) location of cloud, latitude
		double m_PredictedCloudCenterPosition_LonDeg; //!< Predicted center (heaviest) location of cloud, longitude
		double m_PredictedCloudCenterAltitudeMSL_m; //!< Predicted center (heaviest) location of cloud, altitude MSL meters
		double m_PredictedCloudInterceptPosition_LatDeg; //!< Predicted intercept location of cloud and UAV, latitude
		double m_PredictedCloudInterceptPosition_LonDeg; //!< Predicted intercept location of cloud and UAV, longitude
		double m_PredictedCloudInterceptAltitudeMSL_m; //!< Predicted intercept location of cloud and UAV, altitude MSL meters
		//byte m_PredictionMatrix[][];   //Ignoring byte array for now  

		double m_CloudLocationColorRed; //!< Cloud location sphere red color component
		double m_CloudLocationColorGreen; //!< Cloud location sphere green color component
		double m_CloudLocationColorBlue; //!< Cloud location sphere blue color component
		double m_CloudLocationColorAlpha; //!< Cloud location sphere alpha color component
		double m_CloudLocationSizeM; //!< Radius meters of cloud location spheres
		
		
		/**
		\brief Read config settings
		\return void
		*/
		void readConfig ();

		/**
		\brief Initialize quadric object used to display OpenGL stuff
		\return void
		*/
		void initQuadric ();



		static GLUquadricObj *quadricObj; //!< GL Quadric object to use for displaying cloud location spheres
		static int displayList; //!< GL display list to display cloud location spheres
};


#endif



