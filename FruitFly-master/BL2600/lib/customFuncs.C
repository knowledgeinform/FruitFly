/*** BeginHeader customSerModeOpt2,convertBytesToFloat,convertBytesToLong,convertBytesToInt,converIntToBytes,convertFloatToBytes,convertLongToBytes,flipFloat */
void customSerModeOpt2 ();
float convertBytesToFloat (char* bytes, char bigEndian);
long convertBytesToLong (char* bytes, char bigEndian);
int convertBytesToInt (char* bytes, char bigEndian);
void convertIntToBytes (int value, char* bytes, char bigEndian);
void convertFloatToBytes (float value, char* bytes, char bigEndian);
void convertLongToBytes (long value, char* bytes, char bigEndian);
float flipFloat (float oldVal);
/*** EndHeader */


void customSerModeOpt2 ()
{
   //---------------------------------------------------------------------
	// Initialize signal pin PE3 for RS485 transmit control, and
   // initially set the signal to disable the RS485 transmitter.
   //---------------------------------------------------------------------
   WrPortI(PEDDR, &PEDDRShadow, (PEDDRShadow|0x08));
   WrPortI(PEFR,  &PEFRShadow,  (PEFRShadow&~0x08));
   WrPortI(PEDR,  &PEDRShadow,  (PEDRShadow&~0x08));
   WrPortI(PDCR,  &PDCRShadow,  (PDCRShadow&~0x0F));

   //---------------------------------------------------------------------
   // Initialize signal pin PD5 for selecting either RS485 or RS232
   // as an application serial port option.
   //---------------------------------------------------------------------
   WrPortI(PDDCR, &PDDCRShadow, (PDDCRShadow&~0x20));
   WrPortI(PDDDR, &PDDDRShadow, (PDDDRShadow|0x20));
   WrPortI(PDFR,  &PDFRShadow,  (PDFRShadow&~0x20));
   WrPortI(PDDR,  &PDDRShadow,  (PDDRShadow&~0x20));
   WrPortI(PDCR,  &PDCRShadow,  (PDCRShadow&~0xF0));

   //---------------------------------------------------------------------
   // Enable serial port E (bit 6 of PGFR controls serial port E)
   //---------------------------------------------------------------------
   WrPortI(PGFR, &PGFRShadow, (PGFRShadow|0x40));
   WrPortI(PGCR, &PGCRShadow, (PGCRShadow&~0xF0));

   //---------------------------------------------------------------------
   // Enable serial port F (bit 2 of PGFR controls serial port F)
   //---------------------------------------------------------------------
   WrPortI(PGFR, &PGFRShadow, (PGFRShadow|0x04));
   WrPortI(PGCR, &PGCRShadow, (PGCRShadow&~0x0F));


   // Set pin configuration for RTS/CTS, and set serial port E
   // for RS232 operation.
   WrPortI(PDDR,  &PDDRShadow,  (PDDRShadow&~0x20));
   WrPortI(PGFR,  &PGFRShadow,  (PGFRShadow&~0x04));  //This initializes RTS and would set it high if we didn't set RTS low before calling this
   WrPortI(PGDDR, &PGDDRShadow, (PGDDRShadow|0x04));
   WrPortI(PGDDR, &PGDDRShadow, (PGDDRShadow&~0x08));
   WrPortI(PGDCR, &PGDCRShadow, (PGDCRShadow&~0x04));
   //WrPortI(PGDR,  &PGDRShadow,  (PGDRShadow&~0x04));   //This seems to just sets RTS high, but so ignore it
}




float convertBytesToFloat (char* bytes, char bigEndian)
{
	float computeVal;
   unsigned char* itrChar;
   int itrIdx;

   computeVal = 0;
   itrChar = (char*) &computeVal;

	if (bigEndian == 0)
   {
	   for (itrIdx = 0; itrIdx < 4; itrIdx ++)
	   {
	      (*itrChar) = bytes[itrIdx];
	      itrChar++;
	   }
   }
   else
   {
	   for (itrIdx = 3; itrIdx >= 0; itrIdx --)
	   {
	      (*itrChar) = bytes[itrIdx];
	      itrChar++;
	   }
   }

   return computeVal;
}

long convertBytesToLong (char* bytes, char bigEndian)
{
	long computeVal;
   unsigned char* itrChar;
   int itrIdx;

   computeVal = 0;
   itrChar = (char*) &computeVal;

   if (bigEndian == 0)
   {
	   for (itrIdx = 0; itrIdx < 4; itrIdx ++)
	   {
	      (*itrChar) = bytes[itrIdx];
	      itrChar++;
	   }
   }
   else
   {
	   for (itrIdx = 3; itrIdx >= 0; itrIdx --)
	   {
	      (*itrChar) = bytes[itrIdx];
	      itrChar++;
	   }
   }

   return computeVal;
}

int convertBytesToInt (char* bytes, char bigEndian)
{
	int computeVal;
   unsigned char* itrChar;
   int itrIdx;

   computeVal = 0;
   itrChar = (char*) &computeVal;

   if (bigEndian == 0)
   {
	   for (itrIdx = 0; itrIdx < 2; itrIdx ++)
	   {
	      (*itrChar) = bytes[itrIdx];
	      itrChar++;
	   }
   }
   else
   {
	   for (itrIdx = 1; itrIdx >= 0; itrIdx --)
	   {
	      (*itrChar) = bytes[itrIdx];
	      itrChar++;
	   }
   }

   return computeVal;
}

void convertLongToBytes (long value, char* bytes, char bigEndian)
{
   unsigned char* itrChar;
   int itrIdx;

   itrChar = (char*) &value;

   if (bigEndian == 0)
   {
	   for (itrIdx = 0; itrIdx < 4; itrIdx ++)
	   {
	      bytes[itrIdx] = (*itrChar);
	      itrChar++;
	   }
   }
   else
   {
	   for (itrIdx = 3; itrIdx >= 0; itrIdx --)
	   {
	      bytes[itrIdx] = (*itrChar);
	      itrChar++;
	   }
   }
}

void convertFloatToBytes (float value, char* bytes, char bigEndian)
{
	unsigned char* itrChar;
   int itrIdx;

   itrChar = (char*) &value;

   if (bigEndian == 0)
   {
	   for (itrIdx = 0; itrIdx < 4; itrIdx ++)
	   {
	      bytes[itrIdx] = (*itrChar);
	      itrChar++;
	   }
   }
   else
   {
	   for (itrIdx = 3; itrIdx >= 0; itrIdx --)
	   {
	      bytes[itrIdx] = (*itrChar);
	      itrChar++;
	   }
   }
}

void convertIntToBytes (int value, char* bytes, char bigEndian)
{
   unsigned char* itrChar;
   int itrIdx;

   itrChar = (char*) &value;

   if (bigEndian == 0)
   {
	   for (itrIdx = 0; itrIdx < 2; itrIdx ++)
	   {
	      bytes[itrIdx] = (*itrChar);
	      itrChar++;
	   }
   }
   else
   {
	   for (itrIdx = 1; itrIdx >= 0; itrIdx --)
	   {
	      bytes[itrIdx] = (*itrChar);
	      itrChar++;
	   }
   }
}

float flipFloat (float oldVal)
{
 	unsigned char* oldC;
   unsigned char* retC;
   float retVal;
   int i;

   oldC = (unsigned char*)&oldVal;
   retC = (unsigned char*)&retVal;
   retC += 3;

	for (i = 0; i < 4; i ++)
   {
	   *retC = *oldC;
      oldC ++;
      retC --;
   }

   return retVal;
}