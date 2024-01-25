/*** BeginHeader udp_init,send_udp_message,udp_recv_packet*/
scofunc int send_udp_message(char * buffer, int length);
scofunc int udp_recv_packet(char *buffer, int maxSize);
void udp_init(int local_port);
/*** EndHeader */

#memmap xmem
#class auto


#use "dcrtcp.lib"


//Incoming data socket connection
udp_Socket incoming_sock;

//Outgoing data socket connection
udp_Socket outgoing_sock;

//Port number of Rabbit board to connect to PC
int localPort;

//IP address of PC to connect to
long remoteIp;

//Port number of PC to connect to
int remotePort;

//Buffer for incoming messages from UDP socket
char incomingUdpbuffer[256+12];


//Initialize UDP socket connections
void udp_init(int local_port)
{
	sock_init(1);

	memset(&outgoing_sock,0,sizeof(outgoing_sock));
	memset(&incoming_sock,0,sizeof(incoming_sock));

   //listen for commands from all ip addresses
   localPort = local_port;
   if(!udp_open(&incoming_sock, local_port, -1, 0, NULL)) {
		printf("udp_open failed!\n");
		exit(0);
	}

   remoteIp = resolve(REMOTE_IP);
   remotePort = REMOTE_PORT;

   //Outgoing commands
   if(!udp_open(&outgoing_sock, localPort, remoteIp, remotePort, NULL)) {
		printf("udp_open for response failed!\n");
      exit(0);
	}


}

//Send a message to the remote connection on the UDP network
scofunc int send_udp_message(char * buffer, int length)
{
   int retval;

   //Send message on outgoing socket to remote connection
   retval = udp_send(&outgoing_sock, buffer, length);
	if (retval < 0) {
   	//printf("Error sending message\n");
      //Assume error message will be printed by caller
   }

   //Return number of bytes sent
	return retval;
}

scofunc int udp_recv_packet(char *buffer, int maxSize)
{
	int retVal;
   int bytesReceived;


	memset(buffer,0,sizeof(buffer));
   waitfor( (bytesReceived= udp_recv(&incoming_sock, buffer, maxSize))!=-1);


	return bytesReceived;
}