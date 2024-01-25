//---------------------------------------------------------------------------------------
/*!\class  File Name:	Algorithm.h
// \author Author:		Jason A.Stipes
// \brief  Purpose:		Base class for algorithms.  Contains methods and storage for setting and
//				retrieving parameters to be used by the algorithm.
//
// \note   Notes:	
//
// \note   Routines: see Algorithm Class
//
// \note   Copyright (c) 2008 Johns Hopkins University 
// \note   Applied Physics Laboratory.  All Rights Reserved.
// \date   2008
*/
//---------------------------------------------------------------------------------------

#pragma once

#include <stdlib.h>
#include <hash_map>
#include <string>

using namespace std;
using namespace stdext;

class Algorithm
{
public:

	//!Constructor
	Algorithm(void);

	//!Destructor
	virtual ~Algorithm(void);

	//!sets the parameter hashmap
	void setParameters(hash_map<string,float>* parameters);

	//!gets the parameter hashmap
	hash_map<string,float>* getParameters();

	//!sets a parameter
	void  setParameter(string key, float value);

	//!gets a parameter
	float getParameter(string key, float defaultValue);
	
protected:

	//!stores the parameters and values
	hash_map<string,float> parameters;

	//!turns on debug output
	bool debugOn;

};
