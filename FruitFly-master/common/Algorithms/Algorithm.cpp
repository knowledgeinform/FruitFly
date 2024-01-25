//---------------------------------------------------------------------------------------
/*! \class  File Name:	Algorithm.cpp
// \author Author:		Jason A.Stipes
// \brief  Purpose:		Base class for algorithms.  Contains methods and storage for setting and
//				retrieving parameters to be used by the algorithm.
//
// \note Notes:	
//
// \note Routines: see Algorithm Class
//
// \note Copyright (c) 2008 Johns Hopkins University 
// \note Applied Physics Laboratory.  All Rights Reserved.
// \date 2008
*/
//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------

#include "Algorithm.h"


/*! \fn Algorithm::Algorithm(void)
 *  \brief Default Constructor - empty
 *  \param 
 *  \exception 
 *  \return 
 */
Algorithm::Algorithm(void)
{
}

/*! \fn Algorithm::~Algorithm(void)
 *  \brief Default Constructor - empty
 *  \param 
 *  \exception 
 *  \return 
 */
Algorithm::~Algorithm(void)
{
}

/*! \fn float Algorithm::getParameter(string key, float defaultValue)
 *  \brief Gets a parameter as a float given a string key
 *  \param key - The key to the parameter of interest - is case sensitive
 *	\param defaultValue - if no parameter is stored under the spec'd key, return
 *						  the default value.
 *  \exception 
 *  \return The floating point value stored under key
 */
float Algorithm::getParameter(string key, float defaultValue)
{
	hash_map<string,float>::iterator iter;

	iter = parameters.find(key);
	if (iter != parameters.end()) 
	{
		return iter->second;
	}
	else
	{
		setParameter(key, defaultValue);
		return defaultValue;
	}

	return 0.0f;
}

/*! \fn void Algorithm::setParameter(string key, float value)
 *  \brief Sets a parameter as a float under the string key
 *  \param key - The key to the parameter of interest - is case sensitive
 *	\param value - the value to store associated with the spec'd key
 *  \exception 
 *  \return void
 */
void Algorithm::setParameter(string key, float value)
{
	hash_map<string,float>::iterator iter;

	iter = parameters.find(key);
	if (iter != parameters.end()) 
		iter->second = value;
	else
		parameters.insert(pair<string,float>(key,value));
}

/*! \fn void Algorithm::setParameters(hash_map<string,float>* parameters)
 *  \brief Gets a parameter as a float given a string key
 *  \param parameters - The hashmap of keys and values
 *  \exception 
 *  \return void
 */
void Algorithm::setParameters(hash_map<string,float>* parameters)
{
	this->parameters = *parameters;
}

/*! \fn hash_map<string,float>* Algorithm::getParameters()
 *  \brief Gets a parameter as a float given a string key
 *  \param 
 *  \exception 
 *  \return The hashmap of keys and values
 */
hash_map<string,float>* Algorithm::getParameters()
{
	return &this->parameters;
}
