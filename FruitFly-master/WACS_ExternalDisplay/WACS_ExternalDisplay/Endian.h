#ifndef ENDIAN_H
#define ENDIAN_H

#include "stdafx.h"

/**
	\class EndianUtility
	\author John Humphreys

	\brief A utility class for swapping the order of bytes for standard C datatypesS
	\note Copyright (c) 2008 Johns Hopkins University
	\note Applied Physics Laboratory.  All Rights Reserved
*/
class EndianUtility
{
	public:
		/**
		\brief Utility function to swap bytes on a short value
		\param pInt16 Pointer to short value
		\return void
		*/
		static void ByteSwapInt16(short* const pInt16);

		/**
		\brief Utility function to swap bytes on a integer value
		\param pInt32 Pointer to integer value
		\return void
		*/
		static void ByteSwapInt32(int* const pInt32);

		/**
		\brief Utility function to swap bytes on a float value
		\param pFloat Pointer to float value
		\return void
		*/
		static void ByteSwapFloat(float* const pFloat);

		/**
		\brief Utility function to swap bytes on a double value
		\param pDouble Pointer to double value
		\return void
		*/
		static void ByteSwapDouble(double* const pDouble);

		/**
		\brief Utility function to swap bytes on a long long value
		\param pInt64 Pointer to long long value
		\return void
		*/
		static void ByteSwapInt64(long long* const pInt64);
};

#endif