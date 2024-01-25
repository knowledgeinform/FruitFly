/*** BeginHeader  */
long int htol(char * s);

void SatCom_Init_IO();
void SatComLED(int color, int on);
void InitSwitch();
void SatCom_Power_Modem(int on);
int Read_Modem_Carrier_Detect();

#define TESTNUM 1
#define SPI_SER_C
#define SPI_CLK_DIVISOR 1000
#define SPI_RX_PORT SPI_RX_PD
#define SPI_CLOCK_MODE 0
#define CLOCK_PORT D
#define CLOCK_BIT 2

#use "spi.lib"



/*** EndHeader */


//#define GREEN_LED_ON  BitWrPortI(PADR, &PADRShadow, 1, 1);
//#define RED_LED_ON    BitWrPortI(PADR, &PADRShadow, 1, 3);
//#define YELLOW_LED_ON BitWrPortI(PADR, &PADRShadow, 1, 5);

//#define GREEN_LED_OFF  BitWrPortI(PADR, &PADRShadow, 0, 1);
//#define RED_LED_OFF    BitWrPortI(PADR, &PADRShadow, 0, 3);
//#define YELLOW_LED_OFF BitWrPortI(PADR, &PADRShadow, 0, 5);


void SatCom_Power_Modem(int on)
{
  BitWrPortI(PDDR, &PDDRShadow, on, 1);
}

int Read_Modem_Carrier_Detect()
{
 	return BitRdPortI(PBDR, 1);
}


void SatCom_Init_IO()
{

   BitWrPortI(PEDDR, &PEDDRShadow, 1, 2);
   BitWrPortI(PEDR, &PEDRShadow, 1, 2);
   BitWrPortI(PEDR, &PEDRShadow, 0, 2);

	// Initialize Port B Bit 0 to output  (Modem DTR)
 	BitWrPortI(PBDDR, &PBDDRShadow, 1, 0);
   // Make sure Port B Bit 0 not set to alternate function
 	//BitWrPortI(PBFR,  &PBFRShadow, 0, 0);
   //Turn On
   BitWrPortI(PBDR, &PBDRShadow, 1, 0);

   // Initialize Port B Bit 1 to inpu  (Modem DCD)
 	BitWrPortI(PBDDR, &PBDDRShadow, 0, 1);

   // Initialize Port D Bit 1 to output  (Modem Power)
 	BitWrPortI(PDDDR, &PDDDRShadow, 1, 1);
   // Make sure Port D Bit 1 not set to alternate function
 	BitWrPortI(PDFR,  &PDFRShadow, 0, 1);

   //Disable Slave Port, Make Port A output
   BitWrPortI(SPCR, &SPCRShadow, 1, 2);
   BitWrPortI(SPCR, &SPCRShadow, 0, 3);
   BitWrPortI(SPCR, &SPCRShadow, 0, 4);

   //Turn LEDs off
    BitWrPortI(PADR, &PADRShadow, 0, 1);
    BitWrPortI(PADR, &PADRShadow, 0, 3);
    BitWrPortI(PADR, &PADRShadow, 0, 5);

}

void SatComLED(int color, int on)
{
    switch(color)
    {
    	//Yellow
      case 0:
         BitWrPortI(PADR, &PADRShadow, on, 1);
         break;
      //Red
      case 1:
         BitWrPortI(PADR, &PADRShadow, on, 3);
         break;
    	//Green
      case 2:
         BitWrPortI(PADR, &PADRShadow, on, 5);
         break;
    }

}

void InitSwitch()
{

   char adc_write[3] = {0x02,0x01,0x41};
   int i;
   int adc_sample;

   BitWrPortI(SPCR, &SPCRShadow, 1, 2);
   BitWrPortI(SPCR, &SPCRShadow, 0, 3);

   BitWrPortI(PDDDR, &PDDDRShadow, 1, 2); //PD2 as an output
   BitWrPortI(PCDDR, &PCDDRShadow, 1, 3);//PC3 as an output
   BitWrPortI(PDDDR, &PDDDRShadow, 0, 3); //PD3 as an input

   BitWrPortI(PCALR, &PCALRShadow, 0, 7);//make sure port c, bit 3's alt output is correct
   BitWrPortI(PCALR, &PCALRShadow, 0, 6);//make sure port c, bit 3's alt output is correct
   BitWrPortI(PCFR, &PCFRShadow, 1, 3); //PC3 to alternate output

   BitWrPortI(PDALR, &PDALRShadow, 0, 5);//make sure PD2 alt output is spiclk
   BitWrPortI(PDALR, &PDALRShadow, 0, 4); //make sure PD2 alt output is spiclk
   BitWrPortI(PDFR, &PDFRShadow, 1, 2); //set PD2 to alternate output

   BitWrPortI(SCCR, &SCCRShadow, 1, 4); //set up Serial Port C for input on D
   BitWrPortI(SCCR, &SCCRShadow, 0, 5); //set up serial port c for input on d
   BitWrPortI(SCCR, &SCCRShadow, 1, 3); //clocked serial with internal clock
   BitWrPortI(SCCR, &SCCRShadow, 1, 2); //clocked serial with internal clock

   BitWrPortI(SCER, &SCERShadow, 1, 3); //MSB first


   BitWrPortI(PADR, &PADRShadow, 1, 3);
   BitWrPortI(PCDR, &PCDRShadow, 1, 2);   // chip select high
   SPIinit();

   	for(i = 0; i<30000; i++)
      {
           BitWrPortI(PCDR, &PCDRShadow, 1, 2);   // chip select high
      }
      for(i = 0; i<30000; i++)
      {
           BitWrPortI(PCDR, &PCDRShadow, 1, 2);   // chip select high
      }
      for(i = 0; i<30000; i++)
      {
           BitWrPortI(PCDR, &PCDRShadow, 1, 2);   // chip select high
      }
      for(i = 0; i<30000; i++)
      {
           BitWrPortI(PCDR, &PCDRShadow, 1, 2);   // chip select high
      }
      BitWrPortI(PADR, &PADRShadow, 1, 5);

      BitWrPortI(PCDR, &PCDRShadow, 0, 2);   // chip select low

      SPIWrite(adc_write, 3);
      BitWrPortI(PCDR, &PCDRShadow, 1, 2);   // chip select high

      BitWrPortI(PADR, &PADRShadow, 1, 1);


}


