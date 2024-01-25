///
/// Wrapper class around POSIX thread API
///
#ifndef PThreadCH
#define PThreadCH

#include "pthread.h"

class PThreadC
{
	public:
		PThreadC();
		virtual ~PThreadC();

		virtual void Start();
		virtual void Stop();

		void WaitForDeath();
	
	protected:
		virtual void Run() = 0;

	private:
		static void* ThreadProc(void *pThread);

		pthread_t  m_oThreadInfo;
};

#endif