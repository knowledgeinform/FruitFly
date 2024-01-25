// Bladewerx.cpp : Defines the exported functions for the DLL application.
//

#include "stdafx.h"
#include "Bladewerx.h"



#import "..\bwPFAPL.tlb" named_guids, raw_interfaces_only, no_namespace

IbwPFAPLObj *m_pPfObj = NULL;
#define NUM_CHANNELS 256
bool initialized = false;



JNIEXPORT jint JNICALL Java_edu_jhuapl_nstd_cbrnPods_bladewerxProcessor_nativeInitialize
  (JNIEnv *, jobject)
{
	if (initialized)
		return 0;

	CoInitialize (NULL);
	HRESULT hr = CoCreateInstance(CLSID_bwPFAPLObj, NULL, CLSCTX_INPROC_SERVER, IID_IbwPFAPLObj, (void**)&m_pPfObj);
	
	if (SUCCEEDED(hr) && m_pPfObj)
	{
		initialized = true;
		return 0;
	}
	else
	{
		return 1;
	}
}



JNIEXPORT jint JNICALL Java_edu_jhuapl_nstd_cbrnPods_bladewerxProcessor_nativeStart
  (JNIEnv *, jobject)
{
	return (int)m_pPfObj->Start ();
}

JNIEXPORT jint JNICALL Java_edu_jhuapl_nstd_cbrnPods_bladewerxProcessor_nativeStop
  (JNIEnv *, jobject)
{
	return (int)m_pPfObj->Stop ();
}

JNIEXPORT jint JNICALL Java_edu_jhuapl_nstd_cbrnPods_bladewerxProcessor_nativeReset
  (JNIEnv *, jobject)
{
	return (int)m_pPfObj->Reset ();
}

JNIEXPORT jint JNICALL Java_edu_jhuapl_nstd_cbrnPods_bladewerxProcessor_nativeResetPeaks
  (JNIEnv *, jobject)
{
	return (int)m_pPfObj->ResetPeaks ();
}

JNIEXPORT jint JNICALL Java_edu_jhuapl_nstd_cbrnPods_bladewerxProcessor_nativeFitSpectrum
  (JNIEnv *, jobject, jboolean bFastParam)
{
	BOOL bFast;
	if (bFastParam == JNI_FALSE)
		bFast = FALSE;
	else
		bFast = TRUE;

	return (int)m_pPfObj->FitSpectrum (bFast);
}

JNIEXPORT jint JNICALL Java_edu_jhuapl_nstd_cbrnPods_bladewerxProcessor_nativeGetSpectrum
  (JNIEnv * env, jobject, jfloatArray plSpectrumParam, jintArray plTotalCountsParam)
{
	ULONG plSpectrum[NUM_CHANNELS];
	ULONG lTotalCounts;

	int retVal = (int)m_pPfObj->GetSpectrum (plSpectrum, &lTotalCounts);

	jfloat elements[NUM_CHANNELS];
	for (int i = 0; i < NUM_CHANNELS; i ++)
		elements[i] = (jfloat)plSpectrum[i];
	env->SetFloatArrayRegion (plSpectrumParam, 0, NUM_CHANNELS, elements);

	jint counts[1];
	counts[0] = lTotalCounts;
	env->SetIntArrayRegion (plTotalCountsParam, 0, 1, counts);

	return retVal;
}

JNIEXPORT jint JNICALL Java_edu_jhuapl_nstd_cbrnPods_bladewerxProcessor_nativeGetFittedSpectrum
  (JNIEnv *env, jobject, jfloatArray pSpectrumParam)
{
	FLOAT pSpectrum[NUM_CHANNELS];
	
	int retVal = (int)m_pPfObj->GetFittedSpectrum (pSpectrum);

	jfloat elements[NUM_CHANNELS];
	for (int i = 0; i < NUM_CHANNELS; i ++)
		elements[i] = pSpectrum[i];
	env->SetFloatArrayRegion (pSpectrumParam, 0, NUM_CHANNELS, elements);

	return retVal;
}

JNIEXPORT jint JNICALL Java_edu_jhuapl_nstd_cbrnPods_bladewerxProcessor_nativeGetFitParameters
  (JNIEnv * env, jobject, jshort nPeakParam, jfloatArray pCountsParam, jfloatArray pVarianceParam, jfloatArray pChannelParam)
{
	SHORT nPeak = (SHORT) nPeakParam;
	FLOAT pCounts, pVariance, pChannel;

	int retVal = (int)m_pPfObj->GetFitParameters (nPeak, &pCounts, &pVariance, &pChannel);

	jfloat val[1];
	val[0] = pCounts;
	env->SetFloatArrayRegion (pCountsParam, 0, 1, val);

	val[0] = pVariance;
	env->SetFloatArrayRegion (pVarianceParam, 0, 1, val);

	val[0] = pChannel;
	env->SetFloatArrayRegion (pChannelParam, 0, 1, val);

	return retVal;
}

JNIEXPORT jint JNICALL Java_edu_jhuapl_nstd_cbrnPods_bladewerxProcessor_nativeGetFitResults
  (JNIEnv * env, jobject, jfloatArray GoFParam, jintArray lElapsedParam)
{
	FLOAT GoF;
	ULONG lElapsed;

	int retVal = (int)m_pPfObj->GetFitResults (&GoF, &lElapsed);

	jfloat val[1];
	val[0] = GoF;
	env->SetFloatArrayRegion (GoFParam, 0, 1, val);

	jint vall[1];
	vall[0] = lElapsed;
	env->SetIntArrayRegion (lElapsedParam, 0, 1, vall);

	return retVal;
}

JNIEXPORT jint JNICALL Java_edu_jhuapl_nstd_cbrnPods_bladewerxProcessor_nativeGetPeakChannel
  (JNIEnv * env, jobject, jshort nPeakParam, jshortArray nChannelParam)
{
	SHORT nChannel;
	SHORT nPeak = (SHORT)nPeakParam;
	
	int retVal = (int)m_pPfObj->get_PeakChannel (nPeak, &nChannel);

	jshort val[1];
	val[0] = nChannel;
	env->SetShortArrayRegion (nChannelParam, 0, 1, val);

	return retVal;
}

JNIEXPORT jint JNICALL Java_edu_jhuapl_nstd_cbrnPods_bladewerxProcessor_nativePutPeakChannel
  (JNIEnv *, jobject, jshort nPeakParam, jshort nChannelParam)
{
	short nPeak = (short)nPeakParam;
	short nChannel = (short)nChannelParam;

	return (int)m_pPfObj->put_PeakChannel (nPeak, nChannel);
}

JNIEXPORT jint JNICALL Java_edu_jhuapl_nstd_cbrnPods_bladewerxProcessor_nativeGetReferencePeak
  (JNIEnv * env, jobject, jshort nPeakParam, jbooleanArray bReferenceParam)
{
	LONG bReference;
	SHORT nPeak = (SHORT)nPeakParam;
	
	int retVal = (int)m_pPfObj->get_ReferencePeak (nPeak, &bReference);

	jboolean val[1];
	if (bReference == 0)
		val[0] = JNI_FALSE;
	else
		val[0] = JNI_TRUE;
	env->SetBooleanArrayRegion (bReferenceParam, 0, 1, val);

	return retVal;
}

JNIEXPORT jint JNICALL Java_edu_jhuapl_nstd_cbrnPods_bladewerxProcessor_nativePutReferencePeak
  (JNIEnv *, jobject, jshort nPeakParam, jboolean bReferenceParam)
{
	short nPeak = (short)nPeakParam;
	BOOL bReference = (BOOL) (bReferenceParam != JNI_FALSE);

	return (int)m_pPfObj->put_ReferencePeak (nPeak, bReference);
}

JNIEXPORT jint JNICALL Java_edu_jhuapl_nstd_cbrnPods_bladewerxProcessor_nativeRawSpectrum
  (JNIEnv * env, jobject, jfloatArray plSpectrumParam)
{
	ULONG plSpectrum[NUM_CHANNELS];

	jfloat* elements;
	elements = env->GetFloatArrayElements (plSpectrumParam, 0);

	for (int i = 0; i < NUM_CHANNELS; i ++)
		plSpectrum[i] = (int)elements[i];
	
	int rest = (int)m_pPfObj->put_RawSpectrum (plSpectrum);
	return rest;
}
