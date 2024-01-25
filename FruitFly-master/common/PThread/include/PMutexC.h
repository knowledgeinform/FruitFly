///
/// Wrapper class around POSIX Mutex API
///
#ifndef PMutexCH
#define PMutexCH

#include "pthread.h"

class PMutexC
{
	public:
		PMutexC();
		virtual ~PMutexC();
	
		int Lock();
		int Unlock();

		int TryLock();

	private:
		pthread_mutex_t      m_oMutexInfo;
		pthread_mutexattr_t  m_oMutexAttributes;
};

#endif