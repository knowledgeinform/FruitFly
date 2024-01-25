/*** BeginHeader do_servo_command,setPWM,getLastServoDuty, setPWMOpen, setPWMClosed */
char do_servo_command (char* incomingUcMessageBody, char* LogMessage, char* responseBuffer, int* responseLen, char biopodType);
void setPWM(char index, char val);
void incPWM(char index, char val);
void decPWM(char index, char val);
char getLastServoDuty (char index);

void setPWMOpen (char index);
void setPWMClosed (char index);

#define MAX_PWM_PULSEWIDTH 50
#define MIN_PWM_PULSEWIDTH 15
#define PWM_PREF_PERIOD 432


int  isrCount;	//Number of times ISR has been called since reset
int  pwmPulseWidth[2];  //Pulse width of hi-state
int  pwmPeriod;  //Total period of PWM signal
char pwmHilo;  //State of hi or lo in PWM signal
char pwmState; //PWM flags for multiple servo state

char servoToggledOpen[2];
char servoManualOverride[2];

/*** EndHeader */

#memmap xmem
#class auto


//Constants defining specific servo commands
#define SERVO_SET_PWM 0x11
#define SERVO_INC_PWM 0x12
#define SERVO_DEC_PWM 0x13
#define SERVO_CONFIG_OPEN 0x14
#define SERVO_CONFIG_CLOSED 0x15
#define SERVO_CONFIG_TEMP 0x16
#define SERVO_CONFIG_HUMIDITY 0x17
#define SERVO_SET_OPEN 0x18
#define SERVO_SET_CLOSED 0x19
#define SERVO_AUTO_CONTROL 0x20


//RTC conversions
#use "rtc_commands.c"

#use "dio_pins.c"


//Prototypes for message building
char lastServoDuty[2];

char getLastServoDuty (char index)
{
	return lastServoDuty[index];
}

char calcPulseWidth (char index, int val)  //Int to get negative values
{
	char retVal;

   #GLOBAL_INIT
   {
	   servoToggledOpen[0] = 0xFF;
      servoToggledOpen[1] = 0xFF;
      servoManualOverride[0] = 0;
      servoManualOverride[1] = 0;
      pwmState = 0;
   }

 	if (val < 0)
   {
   	retVal = MIN_PWM_PULSEWIDTH;
      val = 0;
   }
	else if (val > 100)
   {
   	retVal = MAX_PWM_PULSEWIDTH;
      val = 100;
   }
   else
   {
   	retVal = (MAX_PWM_PULSEWIDTH - MIN_PWM_PULSEWIDTH) * val / 100 + MIN_PWM_PULSEWIDTH;
   }

   lastServoDuty[index] = val;
   return retVal;
}

void setPWM(char index, char val)
{
	val = calcPulseWidth (index, (int)val);
   pwmPulseWidth[index] = (int)val;
   servoToggledOpen[index] = 0xFF;
}

void incPWM(char index, char val)
{
	val = calcPulseWidth (index, (int)getLastServoDuty(index) + val);
   pwmPulseWidth[index] = (int)val;
 	servoToggledOpen[index] = 0xFF;
}

void decPWM(char index, char val)
{
	val = calcPulseWidth (index, (int)getLastServoDuty(index) - val);
   pwmPulseWidth[index] = (int)val;
   servoToggledOpen[index] = 0xFF;
}

void setPWMOpen (char index)
{
 	setPWM (index, getServoOpenPWM(index));
   servoToggledOpen[index] = 0x01;
}

void setPWMClosed (char index)
{
 	setPWM (index, getServoClosedPWM(index));
   servoToggledOpen[index] = 0x00;
}

//If this is the 2nd servo (used to control sensor flow), and the servo is set open automatically, do not allow it to be overriden.
char allowServoOverride (char index)
{
	if (index == 1 && servoManualOverride[index] == 0 && servoToggledOpen[index] == 1)
		return 0;
	return 1;
}

char do_servo_command(char* incomingUcMessageBody, char* LogMessage, char* responseBuffer, int* responseLen, char biopodType)
{
	int servoIdx;

   //Type of command is in first byte, then any necessary data follows in the body data field
 	switch((int)incomingUcMessageBody[0])
   {
      case SERVO_SET_PWM:
      	if (allowServoOverride (incomingUcMessageBody[2]) == 0)
   			break;
         servoManualOverride[incomingUcMessageBody[2]] = 1;
         setPWM (incomingUcMessageBody[2], incomingUcMessageBody[1]);
         sprintf(LogMessage,"SERVO_SET_PWM received: %d\r\n\0",incomingUcMessageBody[1]);
         break;
      case SERVO_INC_PWM:
      	if (allowServoOverride (incomingUcMessageBody[2]) == 0)
   			break;
         servoManualOverride[incomingUcMessageBody[2]] = 1;
         incPWM (incomingUcMessageBody[2], incomingUcMessageBody[1]);
         sprintf(LogMessage,"SERVO_INC_PWM received: %d\r\n\0",incomingUcMessageBody[1]);
         break;
      case SERVO_DEC_PWM:
      	if (allowServoOverride (incomingUcMessageBody[2]) == 0)
   			break;
         servoManualOverride[incomingUcMessageBody[2]] = 1;
         decPWM (incomingUcMessageBody[2], incomingUcMessageBody[1]);
         sprintf(LogMessage,"SERVO_DEC_PWM received: %d\r\n\0",incomingUcMessageBody[1]);
         break;
      case SERVO_CONFIG_OPEN:
       	setServoOpenPWM(incomingUcMessageBody[2], incomingUcMessageBody[1]);
         sprintf(LogMessage,"SERVO_CONFIG_OPEN received: %d\r\n\0",
         getServoOpenPWM(incomingUcMessageBody[2]));
         break;
      case SERVO_CONFIG_CLOSED:
       	setServoClosedPWM(incomingUcMessageBody[2], incomingUcMessageBody[1]);
         sprintf(LogMessage,"SERVO_CONFIG_CLOSED received: %d\r\n\0",
         getServoClosedPWM(incomingUcMessageBody[2]));
         break;
      case SERVO_CONFIG_TEMP:
       	setTemperatureLimitServo((float)incomingUcMessageBody[1]);
         sprintf(LogMessage,"SERVO_CONFIG_TEMP received: %f\r\n\0",getTemperatureLimitServo());
         break;
      case SERVO_CONFIG_HUMIDITY:
       	setHumidityLimitServo((float)incomingUcMessageBody[1]);
         sprintf(LogMessage,"SERVO_CONFIG_HUMIDITY received: %f\r\n\0",getHumidityLimitServo());
         break;
      case SERVO_SET_OPEN:
      	if (allowServoOverride (incomingUcMessageBody[1]) == 0)
   			break;
         servoManualOverride[incomingUcMessageBody[1]] = 1;
         setPWMOpen (incomingUcMessageBody[1]);
         sprintf(LogMessage,"SERVO_SET_OPEN received: \r\n\0");
         break;
      case SERVO_SET_CLOSED:
      	if (allowServoOverride (incomingUcMessageBody[1]) == 0)
   			break;
         servoManualOverride[incomingUcMessageBody[1]] = 1;
         setPWMClosed (incomingUcMessageBody[1]);
			sprintf(LogMessage,"SERVO_SET_CLOSED received: \r\n\0");
         break;
      case SERVO_AUTO_CONTROL:
       	servoManualOverride[incomingUcMessageBody[1]] = 0;
         sprintf(LogMessage,"SERVO_AUTO_CONTROL received: \r\n\0");
         break;

		default:
        	sprintf(LogMessage,"SERVO_UNKNOWN received: %d\r\n\0",incomingUcMessageBody[0]);
        	printf("Unable to parse SERVO UC_COMMAND_TYPE %d\n\0",incomingUcMessageBody[0] );
   }

	return -1;
}

