#ifndef UAVDISPLAYBASE_H
#define UAVDISPLAYBASE_H


/**
	\class UavDisplayBase
	\brief Base class for defining a UAV display object
	\author John Humphreys
	\date 2012
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class UavDisplayBase
{
	public:
		/**
		\brief Default constructor, does nothing
		*/
		UavDisplayBase();
		
		/**
		\brief Destructor, destroys OpenGL allocated resources
		*/
		~UavDisplayBase();

		/**
		\brief Call display list to paint UAV in OpenGL scene.  UAV must be positioned and 
		oriented before calling this.
		\return void
		*/
		void displayUav();

		enum UAV_TYPES {SHADOW, DAKOTA};

	protected:
		
		int displayList;  //!< Display call list for displaying UAV
		GLUquadricObj *quadricObj; //!< Quadric object for GLU shape creation

		/**
		\brief Initialize Uav display object, generates display call list for UAV display
		\return void
		*/
		void init ();

		/**
		\brief Read config settings from file for UAV size/color
		\return void
		*/
		virtual void readConfig() = 0;

		/**
		\brief Generate display call list for OpenGL UAV display
		\return void
		*/
		virtual void generateCallList () = 0;
};


#endif