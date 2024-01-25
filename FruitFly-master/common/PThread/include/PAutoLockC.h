///
/// Automatically locks and unlocks a AutoLock
///
#ifndef PAutoLockCH
#define PAutoLockCH

class PMutexC;


class PAutoLockC
{
	public:
		PAutoLockC(PMutexC &oMutex);
		virtual ~PAutoLockC();
	
	private:
		PMutexC &m_oMutex;
};

#endif