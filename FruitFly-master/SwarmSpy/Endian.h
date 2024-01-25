#ifndef ENDIAN_H
#define ENDIAN_H

void ByteSwapInt16(short* const pInt16);
void ByteSwapInt32(int* const pInt32);
void ByteSwapFloat(float* const pFloat);
void ByteSwapDouble(double* const pDouble);
void ByteSwapInt64(long long* const pInt64);

#endif