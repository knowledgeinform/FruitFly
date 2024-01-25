#ifndef XvidEncoderH
#define XvidEncoderH


void InitEncoding(const char* const szOutputFileName, const int nFrameWidth, const int nFrameHeight);
void EncodeFrame(const unsigned char* const pInputBuffer);
void TermiateEncoding();


#endif