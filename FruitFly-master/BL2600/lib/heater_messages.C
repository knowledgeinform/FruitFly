/*** BeginHeader heaterState */
void heaterState (char on);

char heaterToggledOn;
char heaterManualOverride;

/*** EndHeader */

#use "dio_pins.c"

//Constants defining specific heater commands
#define HEATER_CONFIG_TEMP 0x11
#define HEATER_CONFIG_HUMIDITY 0x12
#define HEATER_SET_ON 0x13
#define HEATER_SET_OFF 0x14
#define HEATER_AUTO_CONTROL 0x15


void heaterState (char on)
{
  	#GLOBAL_INIT
   {
      heaterToggledOn = 0xFF;
		heaterManualOverride = 0;
   }

   #if BIOPOD_TYPE == 0
	digHout (heaterAltOutputHC, on);
	digHout (heaterOutputHC, on);
   digHout (heaterFanOutputHC, on);
   #endif
   heaterToggledOn = on;
}

void do_heater_command (char* incomingUcMessageBody, char* LogMessage)
{
	//Type of command is in first byte, then any necessary data follows in the body data field
 	switch((int)incomingUcMessageBody[0])
   {
      case HEATER_CONFIG_TEMP:
       	setTemperatureLimitHeater((float)incomingUcMessageBody[1]);
         sprintf(LogMessage,"HEATER_CONFIG_TEMP received: %f\r\n\0",getTemperatureLimitHeater());
         break;
      case HEATER_CONFIG_HUMIDITY:
       	setHumidityLimitHeater((float)incomingUcMessageBody[1]);
         sprintf(LogMessage,"HEATER_CONFIG_HUMIDITY received: %f\r\n\0",getHumidityLimitHeater());
         break;
      case HEATER_SET_ON:
         heaterManualOverride = 1;
         heaterState (1);
         sprintf(LogMessage,"HEATER_SET_OPEN received: \r\n\0");
         break;
      case HEATER_SET_OFF:
         heaterManualOverride = 1;
         heaterState (0);
         sprintf(LogMessage,"HEATER_SET_CLOSED received: \r\n\0");
         break;
      case HEATER_AUTO_CONTROL:
       	heaterManualOverride = 0;
         sprintf(LogMessage,"HEATER_AUTO_CONTROL received: \r\n\0");
         break;

		default:
        	sprintf(LogMessage,"HEATER_UNKNOWN received: %d\r\n\0",incomingUcMessageBody[0]);
        	printf("Unable to parse HEATER UC_COMMAND_TYPE %d\n\0",incomingUcMessageBody[0] );
   }
}