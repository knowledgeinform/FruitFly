/*** BeginHeader setActualLogState,retrieveFlashData,getActualLogState,setServoOpenPWM */
void retrieveFlashData ();

void setActualLogState (char on);
char getActualLogState ();

void setServoOpenPWM (char index, int pwm);
void setServoClosedPWM (char index, int pwm);
void setTemperatureLimitServo (float limit);
void setHumidityLimitServo (float limit);
void setTemperatureLimitFan (float limit);
void setHumidityLimitFan (float limit);
void setTemperatureLimitHeater (float limit);
void setHumidityLimitHeater (float limit);
void setAnacondaSampleUsed (char index, char used);
void setC100SampleUsed (char index, char used);
int getServoOpenPWM (char index);
int getServoClosedPWM (char index);
float getTemperatureLimitServo ();
float getHumidityLimitServo ();
float getTemperatureLimitFan ();
float getHumidityLimitFan ();
float getTemperatureLimitHeater ();
float getHumidityLimitHeater ();
char getAnacondaSampleUsed (char index);
char getC100SampleUsed (char index);

/*** EndHeader */

char actualLogState;
int servoOpenPWM[2];
int servoClosedPWM[2];
float temperatureLimitServo;
float humidityLimitServo;
float temperatureLimitHeater;
float humidityLimitHeater;
float temperatureLimitFan;
float humidityLimitFan;
char anacondaSampleUsed [4];
char c100SampleUsed [4];

#define numVars 13
void* save_data[numVars];
unsigned int save_lens[numVars];


void writeUB()
{
	writeUserBlockArray(0, save_data, save_lens, numVars);
}

void retrieveFlashData ()
{
	readUserBlockArray(save_data, save_lens, numVars, 0);
}


void setActualLogState (char on)
{
	#GLOBAL_INIT
	{
	   save_data[0] = &actualLogState;
	   save_lens[0] = sizeof(actualLogState);
	   save_data[1] = &servoOpenPWM[0];
	   save_lens[1] = sizeof(servoOpenPWM[0]);
	   save_data[2] = &servoClosedPWM[0];
	   save_lens[2] = sizeof(servoClosedPWM[0]);
	   save_data[3] = &temperatureLimitServo;
	   save_lens[3] = sizeof(temperatureLimitServo);
	   save_data[4] = &humidityLimitServo;
	   save_lens[4] = sizeof(humidityLimitServo);
	   save_data[5] = &temperatureLimitFan;
	   save_lens[5] = sizeof(temperatureLimitFan);
	   save_data[6] = &humidityLimitFan;
	   save_lens[6] = sizeof(humidityLimitFan);
	   save_data[7] = &temperatureLimitHeater;
	   save_lens[7] = sizeof(temperatureLimitHeater);
	   save_data[8] = &humidityLimitHeater;
	   save_lens[8] = sizeof(humidityLimitHeater);
	   // Added at end for backwards compatibility
	   save_data[9] = &servoOpenPWM[1];
	   save_lens[9] = sizeof(servoOpenPWM[1]);
	   save_data[10] = &servoClosedPWM[1];
	   save_lens[10] = sizeof(servoClosedPWM[1]);
      save_data[11] = &anacondaSampleUsed;
	   save_lens[11] = sizeof(anacondaSampleUsed);
      save_data[12] = &c100SampleUsed;
	   save_lens[12] = sizeof(c100SampleUsed);

	}

   if (on != actualLogState)
   {
		actualLogState = on;
   	writeUB();
   }
}

char getActualLogState ()
{
	return actualLogState;
}

void setServoOpenPWM (char index, int pwm)
{
	servoOpenPWM[index] = pwm;
	writeUB();
}

void setServoClosedPWM (char index, int pwm)
{
	servoClosedPWM[index] = pwm;
	writeUB();
}

void setTemperatureLimitServo (float limit)
{
 	temperatureLimitServo = limit;
   writeUB();
}

void setHumidityLimitServo (float limit)
{
 	humidityLimitServo = limit;
   writeUB();
}

void setTemperatureLimitFan (float limit)
{
 	temperatureLimitFan = limit;
   writeUB();
}

void setHumidityLimitFan (float limit)
{
 	humidityLimitFan = limit;
   writeUB();
}

void setTemperatureLimitHeater (float limit)
{
 	temperatureLimitHeater = limit;
   writeUB();
}

void setHumidityLimitHeater (float limit)
{
 	humidityLimitHeater = limit;
   writeUB();
}

int getServoOpenPWM (char index)
{
	return servoOpenPWM[index];
}

int getServoClosedPWM (char index)
{
	return servoClosedPWM[index];
}

float getTemperatureLimitServo ()
{
	return temperatureLimitServo;
}

float getHumidityLimitServo ()
{
	return humidityLimitServo;
}

float getTemperatureLimitFan ()
{
	return temperatureLimitFan;
}

float getHumidityLimitFan ()
{
	return humidityLimitFan;
}

float getTemperatureLimitHeater ()
{
	return temperatureLimitHeater;
}

float getHumidityLimitHeater ()
{
	return humidityLimitHeater;
}

void setAnacondaSampleUsed (char index, char used)
{
	if (index < 0 || index > 3)
   {
       anacondaSampleUsed [0] = used;
       anacondaSampleUsed [1] = used;
       anacondaSampleUsed [2] = used;
       anacondaSampleUsed [3] = used;
   }
   else
   {
    	anacondaSampleUsed [index] = used;
   }
   writeUB();
}

void setC100SampleUsed (char index, char used)
{
	if (index < 0 || index > 3)
   {
		 c100SampleUsed [0] = used;
       c100SampleUsed [1] = used;
       c100SampleUsed [2] = used;
       c100SampleUsed [3] = used;
   }
   else
   {
    	c100SampleUsed [index] = used;
   }
   writeUB();
}

char getAnacondaSampleUsed (char index)
{
	return anacondaSampleUsed [index];
}

char getC100SampleUsed (char index)
{
	return c100SampleUsed [index];
}