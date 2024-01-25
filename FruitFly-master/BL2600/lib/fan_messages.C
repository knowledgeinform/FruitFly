/*** BeginHeader fanState,do_fan_command*/
void fanState (char on);
void do_fan_command (char* incomingUcMessageBody, char* LogMessage);

char fanToggledOn;
char fanManualOverride;

/*** EndHeader */

#use "dio_pins.c"

//Constants defining specific heater commands
#define FAN_CONFIG_TEMP 0x11
#define FAN_CONFIG_HUMIDITY 0x12
#define FAN_SET_ON 0x13
#define FAN_SET_OFF 0x14
#define FAN_AUTO_CONTROL 0x15


void fanState (char on)
{
  	#GLOBAL_INIT
   {
	   fanToggledOn = 0xFF;
      fanManualOverride = 0;
   }

   digHout (fanOutputHC, on);
   fanToggledOn = on;
}

void do_fan_command (char* incomingUcMessageBody, char* LogMessage)
{
	//Type of command is in first byte, then any necessary data follows in the body data field
 	switch((int)incomingUcMessageBody[0])
   {
      case FAN_CONFIG_TEMP:
       	setTemperatureLimitFan((float)incomingUcMessageBody[1]);
         sprintf(LogMessage,"FAN_CONFIG_TEMP received: %f\r\n\0",getTemperatureLimitFan());
         break;
      case FAN_CONFIG_HUMIDITY:
       	setHumidityLimitFan((float)incomingUcMessageBody[1]);
         sprintf(LogMessage,"FAN_CONFIG_HUMIDITY received: %f\r\n\0",getHumidityLimitFan());
         break;
      case FAN_SET_ON:
         fanManualOverride = 1;
         fanState (1);
         sprintf(LogMessage,"FAN_SET_OPEN received: \r\n\0");
         break;
      case FAN_SET_OFF:
         fanManualOverride = 1;
         fanState (0);
         sprintf(LogMessage,"FAN_SET_CLOSED received: \r\n\0");
         break;
      case FAN_AUTO_CONTROL:
       	fanManualOverride = 0;
         sprintf(LogMessage,"FAN_AUTO_CONTROL received: \r\n\0");
         break;

		default:
        	sprintf(LogMessage,"FAN_UNKNOWN received: %d\r\n\0",incomingUcMessageBody[0]);
        	printf("Unable to parse FAN UC_COMMAND_TYPE %d\n\0",incomingUcMessageBody[0] );
   }
}