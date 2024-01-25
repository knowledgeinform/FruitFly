//---------------------------------------------------------------------------------------
/*!\class  File Name:	RegistrationAlgorithm.cpp
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

#include "RegistrationAlgorithm.h"

/*! \fn RegistrationAlgorithm::RegistrationAlgorithm(void)
 *  \brief Default Constructor
 *  \exception 
 *  \return 
 */
RegistrationAlgorithm::RegistrationAlgorithm(void)
{
}


/*! \fn RegistrationAlgorithm::~RegistrationAlgorithm(void)
 *  \brief Destructor
 *  \exception 
 *  \return 
 */
RegistrationAlgorithm::~RegistrationAlgorithm(void)
{
}

/*! \fn float RegistrationAlgorithm::doRegistration(PointSet* ref, PointSet* scan)
 *  \brief Empty default doRegistration method
 *  \param ref - The reference point set
 *  \param scan - The point set to be aligned
 *  \exception 
 *  \return 
 */
float RegistrationAlgorithm::doRegistration(PointSet* ref, PointSet* scan)
{
	float retval=0;

	return retval;
}

/*! \fn float RegistrationAlgorithm::doRegistration(PointSet** scans,  const int numScans)
 *  \brief Empty default doRegistration method
 *  \param scans - an array of scans to be aligned with one another
 *  \param numScans - The number of scans to be aligned
 *  \exception 
 *  \return 
 */
float RegistrationAlgorithm::doRegistration(PointSet** scans,  const int numScans)
{
	error = getParameter("error", 9999.0f);

	for(int i=1; i<numScans; i++)
	{
		error = this->doRegistration(scans[0],scans[i]);
		setParameter("error",error);
	}

	return error;
}

/*! \fn bool RegistrationAlgorithm::RegisterFile(PointSet* ref, PointSet* targ, float &rms, bool useAdjusted, float &initialRMS)
 *  \brief Method to register two pointsets - overridden by inheriting classes
 *  \param ref - The reference scan
 *  \param targ - The scan to be aligned
 *  \param rms - Error value is returned in this variable
 *  \param useAdjusted - If true, align files with the _adj extension
 *  \param initialRMS - An estimate of initial error
 *  \exception 
 *  \return 
 */
bool RegistrationAlgorithm::RegisterFile(PointSet* ref, PointSet* targ, float &rms, bool useAdjusted, float &initialRMS)
{

	return true;
}


	

