#include <WinSock2.h>


void ByteSwapInt16(short* const pInt16)
{
	*pInt16 = ntohs(*pInt16);
}

void ByteSwapInt32(int* const pInt32)
{
	*pInt32 = ntohl(*pInt32);
}

void ByteSwapFloat(float* const pFloat)
{
	ByteSwapInt32((int*)pFloat);
}

void ByteSwapInt64(long long* const pInt64)
{
	*pInt64 = ((long long)ntohl((*pInt64) & 0xFFFFFFFF)) << 32 | ntohl((*pInt64) >> 32);
}

void ByteSwapDouble(double* const pDouble)
{
	ByteSwapInt64((long long*)pDouble);
}
