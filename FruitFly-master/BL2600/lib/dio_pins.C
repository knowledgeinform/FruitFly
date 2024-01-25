/*** BeginHeader */
#define SERVO_OUTPUT_CHANNEL 15
#define TH_CLOCK  1
#define TH_DATA  2


#define fanOutputHC 1

#if BIOPOD_TYPE == 0
#define heaterFanOutputHC 0
#define heaterOutputHC 2
#define heaterAltOutputHC 3
#else
#define pumpOutputHC 0
#define bladewerxPowerHC 2
#endif


/*** EndHeader