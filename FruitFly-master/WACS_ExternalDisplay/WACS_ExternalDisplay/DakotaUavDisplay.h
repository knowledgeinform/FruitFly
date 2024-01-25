#ifndef DAKOTAUAVDISPLAY_H
#define DAKOTAUAVDISPLAY_H

#include "UavDisplayBase.h"

/**
	\class DakotaUavDisplay
	\brief Class containing OpenGL calls to display Dakota UAV with WACS pods
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class DakotaUavDisplay : public UavDisplayBase
{
	public:
		/**
		\brief Default constructor, generates display call list for UAV display
		*/
		DakotaUavDisplay();
		
		/**
		\brief Destructor, destroys OpenGL allocated resources
		*/
		~DakotaUavDisplay();

	private:
		
		double planeScale; //!< Scale of UAV
		double uavColorRed; //!< Red component of UAV color
		double uavColorBlue; //!< Blue component of UAV color
		double uavColorGreen; //!< Green component of UAV color
		double podColorRed; //!< Red component of pod color
		double podColorBlue; //!< Blue component of pod color
		double podColorGreen; //!< Green component of pod color
		double fuselageFrontLength; //!<Length of front section of fuselage, meters
		double fuselageFrontWidth; //!<Width of front of fuselage, meters
		double fuselageFrontHeight; //!<Height of front of fuselage, meters
		double fuselageCenterLength; //!<Length of center section of fuselage, meters
		double fuselageCenterWidth; //!<Width of center of fuselage, meters
		double fuselageCenterHeight; //!<Height of center of fuselage, meters
		double fuselageRearLength; //!<Length of rear section of fuselage, meters
		double fuselageRearWidth; //!<Width of rear of fuselage, meters
		double fuselageRearHeight; //!<Height of rear of fuselage, meters
		double wingSpan; //!< Wing span meters
		double wingDepth; //!< Wing depth meters
		double wingThickness; //!< Wing thickness meters
		double wingRearFromCenter; //!< Wing distance rear from fuselage center, meters
		double wingUpFromCenter; //!< Wing distance up from fuselage center, meters
		double podDiameter; //!< Pod diameter, meters
		double podLength; //!< Pod length, meters
		double podOutFromCenter; //!< Lateral Distance from pod center to fuselage center, meters
		double podRearOfWingCenter; //!< Distance backward from wing center for pod center, meters
		double propLength;	//!< Length of propellers, meters
		double propThickness;	//!< Thickness of propellers, meters
		double propDiskAlpha;	//!< Alpha component of propeller disk
		double tailBaseLength;  //!< Base length of tail fin, meters
		double tailTopLength;  //!< Top length of tail fin, meters
		double tailWidth;  //!< Width of tail fin, meters
		double tailHeight;  //!< Height of tail fin, meters
		double elevatorWidth; //!< Width of elevator piece, meters
		double elevatorHeight; //!< Height (thickness) of elevator piece, meters
		double elevatorLength; //!< Length of elevator piece, meters

		double fuselageFrontSideAngleRad;
		double fuselageFrontSideAngleSin;
		double fuselageFrontSideAngleCos;
		double fuselageFrontTopAngleRad;
		double fuselageFrontTopAngleSin;
		double fuselageFrontTopAngleCos;
		double fuselageRearSideAngleRad;
		double fuselageRearSideAngleSin;
		double fuselageRearSideAngleCos;
		double fuselageRearTopAngleRad;
		double fuselageRearTopAngleSin;
		double fuselageRearTopAngleCos;
	

		/**
		\brief Read config settings from file for UAV size/color
		\return void
		*/
		virtual void readConfig();

		/**
		\brief Generate display call list for OpenGL UAV display
		\return void
		*/
		virtual void generateCallList ();

};


#endif