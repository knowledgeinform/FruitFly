#ifndef SwarmSpyH
#define SwarmSpyH

typedef void (*BeliefHandlerCallbackFunc)(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms);

void SwarmSpy_Init(const char* const multicastAddress, const int multicastPortNum);
void SwarmSpy_RegisterBeliefHandlerCallback(const char* const szBeliefName, BeliefHandlerCallbackFunc callbackFunc);


#endif