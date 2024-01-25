#ifndef CONSTANTS_H
#define CONSTANTS_H


#ifndef PI
	#define PI      3.14159265358979323
#endif

#define RAD2DEG   (180.0/PI)
#define DEG2RAD   (PI/180.0)

#define LN2			0.69314718055994530941723212145818
#define INVLN2		1.0/LN2


#define INV2		0.50
#define INV3		1.0/3.0
#define INV4		0.250
#define INV6		1.0/6.0
#define INV8		0.1250
#define INV16		1.0/16.0
#define INV18		1.0/18.0
#define INV24		1.0/24.0
#define INV32		1.0/32.0
#define INV60		1.0/60.0
#define INV64		1.0/64.0
#define INV96		1.0/96.0
#define INV120		1.0/120.0
#define INV127		1.0/127.0
#define INV180		1.0/180.0
#define INV184		1.0/184.0
#define INV255		1.0/255.0
#define INV256		1.0/256.0
#define INV360		1.0/360.0
#define INV720		1.0/720.0
#define INV1024		1.0/1024.0
#define INV1852		1.0/1852.0
#define INV3072		1.0/3072.0
#define INV3600		1.0/3600.0
#define DEG2M		60.0*1852.0
#define INV16384	1.0/16384.0
#define M2DEG		1.0/60.0/1852.0
#define FT2M		0.3048
#define M2FT        3.28083989501
#define NMI2DEG		1.0/60.0
#define NMI2M		1852.0
#define M2NMI		1.0/NMI2M
#define DEG2M10		1.0/10.0/360.0 * 60.0 * 1852.0
#define DEG2M100	1.0/100.0/360.0 * 60.0 * 1852.0

#define C 2.9979e08

#define M_1_PI		1.0/PI
#define INVPI		1.0/PI
#define HALFPI		PI * 0.5
#define TWOPI		PI * 2.0

#define INVMAXSHRT	1.0/32767.0
#define INVMAXUSHRT	1.0/65534.0
#define INVMAXULNG	1.0/2147483647.0


#define M2DEG10		1.0/60.0/1852.0*10.0*360.0
#define LLSCALE2	0.5/10.0/360.0










#define RESET_VIEWS				0x0401
#define UPDATE_WINDOW			0x0402
#define NEW_MOSAIC				0x0403
#define REMOVE_MOSAIC			0x0404
#define SET_CALIB_WINDOW		0x0405
#define DEFINE_CAMERA_CENTER	0x0406
#define SET_SLINGSHOT_SPEED		0x0407
#define REVERSE_SLINGSHOT		0x0408
#define MY_PAINT				0x0409
#define RESET_RUNWAYS			0x040A
#define MOUSE_UNCLICKED			0x040B

#endif