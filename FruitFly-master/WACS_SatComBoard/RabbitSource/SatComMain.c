

//=========================================================================
//=========================================================================
//WACS SATCOM Board Main Program
//=========================================================================
//=========================================================================



#class auto 			// Change default storage class for local variables: on the stack


#define RS232_DEBUG

#use "SatComBoard.c"
#use "Web.c"
#use "Network.c"
#use "SatModem.c"

/* uCOS configuration */
#define OS_MAX_TASKS           6  		// Maximum number of tasks system can create (less stat and idle tasks)
#define OS_SEM_EN					 0			// Enable semaphores
#define OS_SEM_POST_EN			 0       // Enable old style post to semaphore
#define OS_TIME_DLY_HMSM_EN	 1
#define OS_MAX_EVENTS			 32 		// MAX_TCP_SOCKET_BUFFERS + 2 + 1 (1 semaphore is used in this app)
#define STACK_CNT_256          1
#define STACK_CNT_512			 2			// LED update task + main()
#define STACK_CNT_4K         	 4			// TCP/IP needs a 2K stack

#use "ucos2.lib"

/*
 *		This task turns on the LEDs.  If you define LEDS at the
 *		top of the program it turns on and off leds on PORT A.
 *		This setting would be useful on a board like the RCM2100
 *		that has PORT A connected to the development board LEDs.
 *
 *		If you do not define LEDS it prints a message to the
 *		STDIO window when the LINK on the Ethernet goes up or
 *		down.
 */

void ledtask(void* ptr)
{

	while(1)
   {

			SatComLED(TESTNUM,1);     // Turn LED on

			OSTimeDlyHMSM(0,0,0,250);

         SatComLED(1,0);   // Turn LED off

         OSTimeDlyHMSM(0,0,0,250);
	}

}

void ticktask(void* ptr)
{

	while(1)
   {
     tcp_tick(NULL);
     OSTimeDlyHMSM(0,0,0,10);
	}

}




void main()
{
   int error;

   OSInit();				// init uC/OS

   SatCom_Init_IO();

   SatCom_Power_Modem(1);

   InitSwitch();

   network_init();

   web_init();

   modem_init();

	/*
	 *		Start tasks.  ledtask runs every 250ms, echo every
	 *		second when not connected and every 100ms when
	 *		connected.  httptask runs otherwise.
	 */

	//error = OSTaskCreate(ledtask,NULL,512,1);
	error = OSTaskCreate(network_task,NULL,4096,1);
	//error = OSTaskCreate(web_task,NULL,4096,2);
   error = OSTaskCreate(modem_task,NULL,4096,0);
  // error = OSTaskCreate(ticktask,NULL,4096,4);

	OSStart();				// start uC/OS
}