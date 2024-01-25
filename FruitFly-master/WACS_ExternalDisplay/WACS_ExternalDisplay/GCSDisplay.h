#ifndef GCSDISPLAY_H
#define GCSDISPLAY_H


/**
	\class GCSDisplay
	\brief Class containing OpenGL calls to display GCS truck
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class GCSDisplay
{
	public:
		/**
		\brief Default constructor, generates display call list for truck display
		*/
		GCSDisplay();

		/**
		\brief Destructor, destroys OpenGL allocated resources
		*/
		~GCSDisplay();

		/**
		\brief Call display list to paint truck in OpenGL scene.  Truck must be positioned before
		calling this.
		\return void
		*/
		void displayGCS();

	private:
		
		GLUquadricObj *quadricObj; //!< Quadric object for GLU shape creation

		int displayList;  //!< Display call list for displaying GCS truck

		double gcsScale; //!< Scale of GCS truck
		double truckColorRed; //!< Red component of color of truck
		double truckColorBlue; //!< Blue component of color of truck
		double truckColorGreen; //!< Green component of color of truck
		double wheelColorRed; //!< Red component of color of wheels
		double wheelColorBlue; //!< Blue component of color of wheels
		double wheelColorGreen; //!< Green component of color of wheels
		double wheelRadiusM; //!< Radius meters of wheels
		double wheelThicknessM; //!< Thickness/width meters of wheels
		double truckLengthM; //!< Truck cargo length meters
		double truckWidthM; //!< Truck cargo width meters
		double truckHeightM; //!< Truck cargo height meters
		double cabLengthM; //!< Truck cab length meters
		double cabWidthM; //!< Truck cab width meters
		double cabHeightM; //!< Truck cab height meters
		double headingDecDeg; //!< Truck heading decimal degrees


		/**
		\brief Read config settings from file for truck size/color
		\return void
		*/
		void readConfig();

		/**
		\brief Generate display call list for OpenGL truck display
		\return void
		*/
		void generateCallList ();
};


#endif