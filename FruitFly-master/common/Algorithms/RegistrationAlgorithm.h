//---------------------------------------------------------------------------------------
/*!\class  File Name:	RegistrationAlgorithm.h
// \author Author:		Jason A.Stipes
// \brief  Purpose:		Base Class (Interface) for Registration Algorithms
//
// \note   Notes:	
//
// \note   Routines:  see RegistrationAlgorithm class
//
// \note   Copyright (c) 2008 Johns Hopkins University 
// \note   Applied Physics Laboratory.  All Rights Reserved.
*/
//---------------------------------------------------------------------------------------
#pragma once

#include "PointSet.h"
#include "Algorithm.h"


class RegistrationAlgorithm: public Algorithm
{
public:

	//!Constructor
	RegistrationAlgorithm(void);

	//!Destructor
	virtual ~RegistrationAlgorithm(void);

	//!Registers two pointsets
	virtual float doRegistration(PointSet* ref, PointSet* scan);

	//!Registers multiple pointsets
	virtual float doRegistration(PointSet** scans,  const int numScans);

	//!Registers two pointsets, either segmented or unsegmented.
	virtual bool RegisterFile(PointSet* ref, PointSet* targ, float &rms, bool useAdjusted, float &initialRMS);


protected:
	// The alignment error
	float error;

};
