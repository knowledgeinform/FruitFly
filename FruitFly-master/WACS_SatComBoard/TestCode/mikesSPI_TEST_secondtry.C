/*************************************************************************
   Samples\SPI\spi_test.c

   ZWorld, 2001

   test out SPI driver with an NS ADC0831 chip. Uses serial channel B for
   the SPI data.

   PB7 acts as the CS line on the ADC
   PB0 is the serial B clock line(SCLK)

   PC4 is the data output(MOSI)
   PC5 is the data input(MISO)

   Reads two bytes worth with each chip select.
   The first two bits are not part of the data. They are always 1 and
   then 0 .  This is followed by 8 bits of data for the sample, and
   then 6 extra bits.

************************************************************************/
#class auto


#define SPI_SER_C
#define SPI_CLK_DIVISOR 1000
#define SPI_RX_PORT SPI_RX_PD
#define SPI_CLOCK_MODE 0
#define CLOCK_PORT D
#define CLOCK_BIT 2

#use "spi.lib"

void main()
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
  // while(1)
  // {
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
      //hold CS low for conversion time
      //for(i = 0;i < 10;i++)
      //{
         BitWrPortI(PCDR, &PCDRShadow, 0, 2);   // chip select low
      //}

      SPIWrite(adc_write, 3);
      BitWrPortI(PCDR, &PCDRShadow, 1, 2);   // chip select high
      //printf("SPI bytes = 0x%x 0x%x\n", adc_reading[0], adc_reading[1]);
      //adc_sample = (adc_reading[0] <<2 ) + (adc_reading[1] >> 6) & 0xff;
      //printf("ADC value: %d\n", adc_sample);
      BitWrPortI(PADR, &PADRShadow, 1, 1);
  // }
}