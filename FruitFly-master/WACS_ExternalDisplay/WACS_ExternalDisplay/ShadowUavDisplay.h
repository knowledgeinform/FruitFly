#ifndef SHADOWUAVDISPLAY_H
#define SHADOWUAVDISPLAY_H

#include "UavDisplayBase.h"

/**
	\class ShadowUavDisplay
	\brief Class containing OpenGL calls to display Shadow UAV with WACS pods
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class ShadowUavDisplay : public UavDisplayBase
{
	public:
		/**
		\brief Default constructor, generates display call list for UAV display
		*/
		ShadowUavDisplay();
		
		/**
		\brief Destructor, destroys OpenGL allocated resources
		*/
		~ShadowUavDisplay();

	private:
		
		double planeScale; //!< Scale of UAV
		double uavColorRed; //!< Red component of UAV color
		double uavColorBlue; //!< Blue component of UAV color
		double uavColorGreen; //!< Green component of UAV color
		double highlightColorRed; //!< Red component of highlight color
		double highlightColorBlue; //!< Blue component of highlight color
		double highlightColorGreen; //!< Green component of highlight color
		double engineColorRed; //!< Red component of engine color
		double engineColorBlue; //!< Blue component of engine color
		double engineColorGreen; //!< Green component of engine color
		double fuselageLength; //!< Length of fuselage, meters
		double fuselageWidth; //!< Width of fuselage, meters
		double fuselageHeight; //!< Height of fuselage, meters
		double fuselageFrontLength; //!< Length of fuselage front, meters
		double fuselageFrontAngleRad; //!< Angle of the front nose of the fuselage, radians
		double wingSpan; //!< Wing span meters
		double wingTipHighlightColorLength; //!< Length of tip of wings to use highlight color instead of UAV color, meters
		bool highlightTail; //!< If true, highlight tail with highlight color.
		bool highlightNose; //!< If true, highlight nose with highlight color;
		double wingDepth; //!< Wing depth meters
		double wingThickness; //!< Wing thickness meters
		double wingRearFromCenter; //!< Wing distance rear from fuselage center, meters
		double wingUpFromCenter; //!< Wing distance up from fuselage center, meters
		double tailRearFromWingRear; //!< Distance from rear of tail to rear of wing, meters
		double tailRodDiameter; //!< Diameter of tail rods, meters
		double tailWidth; //!< Tail width, meters
		double tailThickness; //!< Tail thickness, meters
		double tailHeight; //!< Tail height, meters
		double tailDepth; //!< Tail depth, meters
		double podDiameter; //!< Pod diameter, meters
		double podLength; //!< Pod length, meters
		double podOutFromCenter; //!< Lateral Distance from pod center to fuselage center, meters
		double podRearOfWingCenter; //!< Distance backward from wing center for pod center, meters
		double engineDiameter;	//!< Diameter of engine cylinder, meters
		double engineDepth; //!< Depth of engine engine clyinder, meters
		double propLength;	//!< Length of propellers, meters
		double propThickness;	//!< Thickness of propellers, meters
		double propDiskAlpha;	//!< Alpha component of propeller disk


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