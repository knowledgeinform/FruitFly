/*** BeginHeader tempHumidityControls */
void tempHumidityControls (float temperature, float humidity, char* thMsg);

/*** EndHeader */

#use "flash_handler.c"

#use "servo_messages.c"

#use "heater_messages.c"

#use "fan_messages.c"


void toggleFan (char on);
void toggleServo (char open);
void toggleHeater (char on);

const float temperatureBuffer = 2;
const float humidityBuffer = 5;


void tempHumidityControls (float temperature, float humidity, char* thMsg)
{
   if (temperature < getTemperatureLimitHeater() || (heaterToggledOn == 1 && temperature < getTemperatureLimitHeater() + temperatureBuffer))
   {
		toggleHeater (1);
      toggleFan (0);
      toggleServo (0);
   }
   else
   {
      if (temperature > getTemperatureLimitFan() || humidity > getHumidityLimitFan() || (fanToggledOn == 1 && (temperature > getTemperatureLimitFan() - temperatureBuffer || humidity > getHumidityLimitFan() - humidityBuffer)))
      {
         toggleFan (1);
      }
      else
      {
			toggleFan (0);
      }

      if (temperature > getTemperatureLimitServo() || humidity > getHumidityLimitServo() || (servoToggledOpen[0] == 1 && (temperature > getTemperatureLimitServo() - temperatureBuffer || humidity > getHumidityLimitServo() - humidityBuffer)))
      {
         toggleServo (1);
      }
      else
      {
      	toggleServo (0);
      }

      if (BIOPOD_TYPE == 0 && fanToggledOn == 0 && servoToggledOpen[0] == 0 && (humidity > getHumidityLimitHeater() || (heaterToggledOn == 1 && (humidity > getHumidityLimitHeater() - humidityBuffer))))
      {
	      toggleHeater (1);
      }
      else
      {
         toggleHeater (0);
      }
   }

   sprintf (thMsg, "Fan %d, Servo %d, Heater %d\r\n\0", fanToggledOn, servoToggledOpen[0], heaterToggledOn);
}

void toggleFan (char on)
{
	if (on != 0 && on != 1)
	   return;
   if (fanManualOverride == 1)
   	return;

	if (on != fanToggledOn)
	{
      fanState (on);

      fanToggledOn = on;
   }
}

void toggleServo (char open)
{
	if (open != 0 && open != 1)
	   return;
   if (servoManualOverride[0] == 1)
   	return;

	if (open != servoToggledOpen[0])
	{
   	if (open == 1)
        	setPWMOpen (0);
      else
        	setPWMClosed (0);

      servoToggledOpen[0] = open;
   }
}

void toggleHeater (char on)
{
	if (on != 0 && on != 1)
	   return;
   if (heaterManualOverride == 1)
   	return;

	if (on != heaterToggledOn)
	{
   	heaterState (on);

      heaterToggledOn = on;
   }
}