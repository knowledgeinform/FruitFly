#ifndef GLCAMERA_H
#define GLCAMERA_H

#include <windows.h>		// Header File For Windows
#include "include\gl\gl.h"			// Header File For The OpenGL32 Library
#include "include\gl\glu.h"			// Header File For The GLu32 Library
#include "include\gl\glaux.h"		// Header File For The Glaux Library

#include "glQuaternion.h"
#include "glPoint.h"
#include "glVector.h"
#include "Constants.h"


/**
	\class glCamera
	\brief An object to represent a camera in an OpenGL scene
	\author Kevin Murphy, John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class glCamera  {
	public:

		/**
		\brief Constructor, set all values to default
		\return none
		*/
		glCamera();

		/**
		\brief Destructor
		*/
		virtual ~glCamera();

		/**
		\brief Change the velocity of the camera through space
		\param vel Incremental change to velocity
		\return void
		*/
		void ChangeVelocity(GLfloat vel);

		/**
		\brief Change the heading of the camera in space
		\param degrees Incremental change to heading
		\return void
		*/
		void ChangeHeading(GLfloat degrees);

		/**
		\brief Change the pitch of the camera in space
		\param degrees Incremental change to pitch
		\return void
		*/
		void ChangePitch(GLfloat degrees);

		/**
		\brief Call a series of OpenGL functions to establish this cameras position in space
		\param position If true, position will be changed.  If false, only rotations will happen
		\return void
		*/
		void SetPerspective(bool position);

		/**
		\brief Moves the camera to the specified position, but doesn't update OpenGL scenery.  Updates will
		occur next time OpenGL is rendered
		\param x New x position for camera
		\param y New y position for camera
		\param z New z position for camera
		\return void
		*/
		void MovePerspective(float x, float y, float z);
		

		/**
		\brief Max rate of change for pitch
		*/
		GLfloat m_MaxPitchRate;

		/**
		\brief Max rate of change for heading
		*/
		GLfloat m_MaxHeadingRate;

		/**
		\brief Current camera heading
		*/
		GLfloat m_HeadingDegrees;

		/**
		\brief Current camera pitch
		*/
		GLfloat m_PitchDegrees;

		/**
		\brief Maximum rate of change of velocity
		*/
		GLfloat m_MaxForwardVelocity;

		/**
		\brief Current forward velocity
		*/
		GLfloat m_ForwardVelocity;

		/**
		\brief Matrix representing camera heading
		*/
		glQuaternion m_qHeading;
		
		/**
		\brief Matrix representing camera pitch
		*/
		glQuaternion m_qPitch;

		/**
		\brief Position of camera
		*/
		glPoint m_Position;

		/**
		\brief cos of the Heading value
		*/
		double cosHeading;
		
		/**
		\brief sin of the Heading value
		*/
		double sinHeading;
		
		/**
		\brief cos of the Heading value
		*/
		double cosPitch;
		
		/**
		\brief sin of the Pitch value
		*/
		double sinPitch;

		/**
		\brief Calculate sin/cos values for Heading and Pitch
		\return void
		*/
		void updateTrig();

		/**
		\brief Calculate the camera position based on the specified offsets using the heading/pitch

		\param xOffset Lateral movement of camera
		\param yOffset Forward movement of camera
		\param zOffset Vertical movement of camera
		\param estLatToMConv Estimated conversion from latitude to meters for the area.  Used to map latitudes in meter space
		\param estLonToMConv Estimated conversion from longitude to meters for the area.  Used to map longitudes in meter space
		\return void
		*/
		void calcFromOffsets(double xOffset, double yOffset, double zOffset, double estLatToMConv, double estLonToMConv);

		/**
		\brief Pointing direction of camera
		*/
		glVector m_DirectionVector;

};

#endif
