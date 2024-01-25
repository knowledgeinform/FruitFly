#ifndef LOCKOBJECT_H
#define LOCKOBJECT_H

#include "stdafx.h"

/**
	\class LockObject
	\brief Class to lock on a critical section when instantiated and release critical section when destroyed
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class LockObject
{
	public:
		/**
		\brief Constructor, locks the object
		\param lock Object to lock on
		*/
		LockObject (CRITICAL_SECTION *lock)
		{
			m_Lock = lock;
			EnterCriticalSection (m_Lock);
		}

		/**
		\brief Destructor, releases the lock
		*/
		~LockObject ()
		{
			LeaveCriticalSection (m_Lock);
		}

	private:
		/**
		\brief Locking critical section
		*/
		CRITICAL_SECTION *m_Lock;

};

#endif