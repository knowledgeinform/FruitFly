/*** BeginHeader get1970RTC,set1970RTC */
unsigned long get1970RTC ();
void set1970RTC (unsigned long rtc);


/*** EndHeader */

const unsigned long SECS_BETWEEN_1970_1980 = ((long)60*60*24*365*10)/*10 basic years*/ + ((long)60*60*24*2)/*2 leap days*/ + 8/*8 leap seconds*/;


unsigned long get1970RTC ()
{
	unsigned long rtc;

   rtc = read_rtc();
   //Add 10 years to offset from 1980 to 1970
   rtc += SECS_BETWEEN_1970_1980;
   return rtc;
}

void set1970RTC (unsigned long rtc)
{
	//unsigned long rtCheck;

   //Subtract 10 years to offset from 1970 to 1980
   rtc -= SECS_BETWEEN_1970_1980;
   write_rtc(rtc);

   //rtCheck = read_rtc();
   //printf ("rtCheck = %ld\r\n\0", rtCheck);
}