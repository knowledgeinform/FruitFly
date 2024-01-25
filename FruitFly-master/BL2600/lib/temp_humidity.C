/*** BeginHeader EE03_status,RH_read,Temp_read */
unsigned char EE03_status(void);
float RH_read(void);
float Temp_read(void);
/*** EndHeader */

#class auto

#use "dio_pins.c"

#define DELAY_FAKTOR 5 //setup clock-frequency
#define ACK 1 // define acknowledge
#define NAK 0 // define not-acknowledge
#define OFF 0
#define ON 1


// variables
unsigned char rh_low;
unsigned char rh_high;
unsigned char temp_low;
unsigned char temp_high;
unsigned char checksum_03;
unsigned int rh_ee03;
unsigned int temp_ee03;
float rh;
float temperature;



/*-------------------------------------------------------------------------*/
void set_SCL(void)
{
	digOut(TH_CLOCK, ON);
} // set port-pin (SCL)

/*-------------------------------------------------------------------------*/
void clear_SCL(void)
{
	digOut(TH_CLOCK, OFF);
} // clear port-pin (SCL)

/*-------------------------------------------------------------------------*/
void set_SDA(void)
{
   digOut(TH_DATA, ON);
} // set port-pin (SDA)

/*-------------------------------------------------------------------------*/
void clear_SDA(void)
{
   digOut(TH_DATA, OFF);
} // clear port-pin (SDA)

/*-------------------------------------------------------------------------*/
int read_SDA(void)
{
	return digIn(TH_DATA);
   return -1;
} // read SDA-pin status

/*-------------------------------------------------------------------------*/
void delay(unsigned int value) // delay- routine
{
	while (--value != 0);
}

/*-------------------------------------------------------------------------*/
void E2Bus_start(void) // send Start condition to E2 Interface
{
	set_SDA();
	set_SCL();
	delay(30*DELAY_FAKTOR);
	clear_SDA();
	delay(30*DELAY_FAKTOR);
}

/*-------------------------------------------------------------------------*/
void E2Bus_stop(void) // send Stop condition to E2 Interface
{
	clear_SCL();
	delay(20*DELAY_FAKTOR);
	clear_SDA();
	delay(20*DELAY_FAKTOR);
	set_SCL();
	delay(20*DELAY_FAKTOR);
	set_SDA();
	delay(20*DELAY_FAKTOR);
}

/*-------------------------------------------------------------------------*/
void E2Bus_send(unsigned char value) // send Byte to E2 Interface
{
	unsigned char i;
	unsigned char maske;
   maske = 0x80;
	for (i=8;i>0;i--)
	{
 	  clear_SCL();
	   delay(10*DELAY_FAKTOR);
	   if ((value & maske) != 0)
	   {
	      set_SDA();
	   }
	   else
	   {
	      clear_SDA();
	   }
	   delay(20*DELAY_FAKTOR);
	   set_SCL();
	   maske >>= 1;
	   delay(30*DELAY_FAKTOR);
	   clear_SCL();
	}
	set_SDA();
}

/*-------------------------------------------------------------------------*/
char check_ack(void) // check for acknowledge
{
	int input;
	delay(30*DELAY_FAKTOR);
	set_SCL();
	delay(15*DELAY_FAKTOR);
	input = read_SDA();
	delay(15*DELAY_FAKTOR);
	if(input == 1)
		return NAK;
	else
		return ACK;
}

/*-------------------------------------------------------------------------*/
void send_ack(void) // send acknowledge
{
	delay(15*DELAY_FAKTOR);
	clear_SDA();
	delay(15*DELAY_FAKTOR);
	set_SCL();
	delay(30*DELAY_FAKTOR);
	set_SDA();
}

/*-------------------------------------------------------------------------*/
void send_nak(void) // send NOT-acknowledge
{
	delay(15*DELAY_FAKTOR);
	clear_SDA();
	delay(15*DELAY_FAKTOR);
	set_SCL();
	delay(30*DELAY_FAKTOR);
	set_SCL();
}

/*-------------------------------------------------------------------------*/
unsigned char E2Bus_read(void) // read Byte from E2 Interface
{
	unsigned char data_in;
	unsigned char maske;
   data_in = 0x00;
   maske = 0x80;
	for (maske=0x80;maske>0;maske >>=1)
	{
   	clear_SCL();
	   delay(30*DELAY_FAKTOR);
	   set_SCL();
	   delay(15*DELAY_FAKTOR);
	   if (read_SDA())
	   {data_in |= maske;}
	   delay(15*DELAY_FAKTOR);
	   clear_SCL();
	}
	return data_in;
}




/*-------------------------------------------------------------------------*/
unsigned char EE03_status(void)
{
	unsigned char stat_ee03;
   #GLOBAL_INIT
	{
	   rh_ee03= 0;
	   temp_ee03= 0;
	}

	E2Bus_start(); // start condition for E2-Bus
	E2Bus_send(0x71); // main command for STATUS request
	if (check_ack()==ACK)
	{
   	stat_ee03 = E2Bus_read(); // read status byte
		send_ack();
	   checksum_03 = E2Bus_read(); // read checksum
		send_nak(); // send NAK ...
		E2Bus_stop(); // ... and stop condition to terminate
		if (((stat_ee03 + 0x71) % 256) == checksum_03) // checksum OK?
		return stat_ee03;
	}
	return 0xFF; // in error case return 0xFF
   //return stat_ee03;
}

/*-------------------------------------------------------------------------*/
float RH_read(void)
{
	rh = -1; // default value (error code)
	E2Bus_start();
	E2Bus_send(0x81); // MW1-low request
	if (check_ack()==ACK)
	{
   	rh_low = E2Bus_read();
		send_ack();
		checksum_03 = E2Bus_read();
		send_nak(); // terminate communication
		E2Bus_stop();
		if (((0x81 + rh_low) % 256) == checksum_03) // checksum OK?
		{
      	E2Bus_start();
			E2Bus_send(0x91); // MW1-high request
			check_ack();
			rh_high = E2Bus_read();
			send_ack();
			checksum_03 = E2Bus_read();
			send_nak(); // terminate communication
			E2Bus_stop();
			if (((0x91 + rh_high) % 256) == checksum_03) // checksum OK?
			{
         	rh_ee03=rh_low+256*(unsigned int)rh_high;
				// yes-> calculate humidity value
				rh=(float)rh_ee03/100;
				// overwrite default (error) value
			}
  		}
	E2Bus_stop();
	}
	return rh;
}

/*-------------------------------------------------------------------------*/
float Temp_read(void)
{
	temperature = -300; // default value (error code)
	E2Bus_start();
	E2Bus_send(0xA1); // MW2-low request
	if (check_ack()==ACK)
	{
   	temp_low = E2Bus_read();
		send_ack();
		checksum_03 = E2Bus_read();
		send_nak(); // terminate communication
		E2Bus_stop();
		if (((0xA1 + temp_low) % 256) == checksum_03) // checksum OK?
		{
      	E2Bus_start();
			E2Bus_send(0xB1); // MW2-high request
			check_ack();
			temp_high = E2Bus_read();
			send_ack(); // terminate communication
			checksum_03 = E2Bus_read();
			send_nak();
			E2Bus_stop();
		  	if (((0xB1 + temp_high) % 256) == checksum_03) // checksum OK?
		  	{
         	temp_ee03=temp_low+256*temp_high; //yes->calculate temperature
				temperature=((float)temp_ee03/100) - 273.15;
				// overwrite default (error) value
		  	}
	  	}
		E2Bus_stop();
	}
	return temperature;
}