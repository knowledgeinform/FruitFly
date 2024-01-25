#pragma once
#include "MultiCoreAlgorithm.h"
#include "pthread.h"
#include "PMutexC.h"
#include <stdio.h>
#include <stdarg.h>


template <class TYPE> class MultiCoreAlgorithm
{
public:
	MultiCoreAlgorithm(void(*f)(int, int, int, TYPE*), int startIndex, int endIndex, int step=1, int CPUs=-1);
	void setFunction(void(*f)(int, int, int, TYPE*), int startIndex, int endIndex, int step=1, int CPUs=-1);
	~MultiCoreAlgorithm(void);
	bool RunThreads(TYPE* params);
	int getNumThreads();

protected:
	pthread_t*  threads;
	static void(*loopFunction)(int, int, int, TYPE*);
	static int startIndex;
	static int endIndex;
	static int step;
	static int tCount;
	static int chunkSize;
	int numCPUs;

	static PMutexC dataMutex;
	

private:
	static void* threadProc(void *params);
};

 template <class TYPE> void(*MultiCoreAlgorithm<TYPE>::loopFunction)(int, int, int, TYPE*);
 template <class TYPE> int MultiCoreAlgorithm<TYPE>::startIndex = 0;
 template <class TYPE> int MultiCoreAlgorithm<TYPE>::endIndex = 0;
 template <class TYPE> int MultiCoreAlgorithm<TYPE>::step = 0;
 template <class TYPE> int MultiCoreAlgorithm<TYPE>::tCount = 0;
 template <class TYPE> int MultiCoreAlgorithm<TYPE>::chunkSize = 0;
 template <class TYPE> PMutexC MultiCoreAlgorithm<TYPE>::dataMutex;


template <class TYPE>  MultiCoreAlgorithm<TYPE>::MultiCoreAlgorithm(void(*f)(int, int, int, TYPE*), int startIndex, int endIndex, int step, int CPUs)
{
	this->loopFunction = f;
	this->startIndex = startIndex;
	this->endIndex = endIndex;
	this->step = step;
	if(CPUs > 0)
		numCPUs = CPUs;
	else
		numCPUs = pthread_num_processors_np();
}

template <class TYPE>  void MultiCoreAlgorithm<TYPE>::setFunction(void(*f)(int, int, int, TYPE*), int startIndex, int endIndex, int step, int CPUs)
{
	this->loopFunction = f;
	this->startIndex = startIndex;
	this->endIndex = endIndex;
	this->step = step;
	if(CPUs > 0)
		numCPUs = CPUs;
	else
		numCPUs = pthread_num_processors_np();
}

template <class TYPE> int MultiCoreAlgorithm<TYPE>::getNumThreads()
{
	return numCPUs;
}


template <class TYPE> MultiCoreAlgorithm<TYPE>::~MultiCoreAlgorithm(void)
{
}

template <class TYPE> bool MultiCoreAlgorithm<TYPE>::RunThreads(TYPE* params)
{
	bool retval = true;

	threads = (pthread_t*) malloc(numCPUs*sizeof(pthread_t*));

	chunkSize = abs(startIndex - endIndex)/numCPUs;
	tCount = 0;

	for(int i=0; i<numCPUs; i++)
	{
		pthread_create(&threads[i], NULL, MultiCoreAlgorithm<TYPE>::threadProc, (void*)&params[i]);
	}

	for(int i=0; i<numCPUs; i++)
	{
		pthread_join(threads[i], NULL);
	}

	return retval;

}

template <class TYPE> void* MultiCoreAlgorithm<TYPE>::threadProc(void* params)
{
	int start, stop;
	dataMutex.Lock();
	start = tCount*chunkSize;
	stop = min((tCount+1)*chunkSize-1, endIndex);
	tCount++;
	dataMutex.Unlock();

	
	int nOldState;
	pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, &nOldState);
	pthread_setcanceltype(PTHREAD_CANCEL_ASYNCHRONOUS, &nOldState);

	loopFunction(start, stop, step, (TYPE*)params);


	return NULL;
}