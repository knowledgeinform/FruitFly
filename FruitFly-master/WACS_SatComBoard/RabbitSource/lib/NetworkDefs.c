/*** BeginHeader  */
/*
 * NETWORK CONFIGURATION
 */
#define TCPCONFIG 		0
#define USE_ETHERNET    0x01

#define REDIRECTTO 		"/index.shtml"

#define DEFAULT_PORT 57190
#define DEFAULT_IP  "233.1.3.3"
#define DEFAULT_WAIT 5
#define DEFAULT_PORT_OUT 57191


#define DEFAULT_LOCAL_IP			"192.168.100.50"
#define DEFAULT_LOCAL_NETMASK		"255.255.255.0"
#define DEFAULT_LOCAL_GATEWAY		"192.168.1.1"

#define MAX_UDP_SOCKETS				8

#define ETH_MTU                	1489		// Set the MTU to max Ethernet/WiFi MTU
#define TCP_BUF_SIZE           	8192		// Make the TCP tx and rx buffers 4K each
#define MAX_UDP_SOCKET_BUFFERS	MAX_UDP_SOCKETS+2

/*
 * Defines the size of the buffer that is used to receive UDP
 * datagrams.
 */
#define BUFFER_SIZE	8192
#define MY_BUFFER_SIZE	8192

/*
 * Enable the use of multicasting.  Note that this does not
 * enable IGMP, so that if multicast routing is required, this
 * will not suffice.
 */
#define USE_MULTICAST

/*
 * Enable the use of IGMP.  When multicast groups are joined
 * and left, reports will be issued that an IGMP router on the
 * local network can use.  If USE_IGMP is defined to 2, then
 * IGMPv2 will be used.  USE_IGMP can also be defined to be 1,
 * which means that IGMPv1 will be used.  Note however, that
 * the IGMPv2 client is compatible with both IGMPv1 and IGMPv2
 * routers, so there is not much reason to set USE_IGMP to
 * anything other than 2.
 *
 * This sample will work with the following line commented out,
 * but the multicast datagrams will not be routed across
 * subnets.
 */
#define USE_IGMP 3

/*
 * HTTP_MAXBUFFER is used for each web server to hold received and
 * transferred information.  In particular, it holds each header line
 * received from the web client.  Digest authentication generally
 * results in a large WWW-Authenticate header that can be larger than
 * the default HTTP_MAXBUFFER value of 256.  512 bytes should be
 * sufficient.
 */
#define HTTP_MAXBUFFER				4096

/*
 * By default, digest authentication is turned off.  Note that you
 * can set USE_HTTP_BASIC_AUTHENTICATION to 0 to remove the code for
 * basic authentication at compile time.
 */
#define USE_HTTP_DIGEST_AUTHENTICATION	1

/*
 * If you want to associate multiple users with a particular
 * resource (web page), then you need to set the following macro.
 * This macro defines how many users can be associated with a web
 * page, and defaults to 1.
 */
#define SSPEC_USERSPERRESOURCE 	1


//Increase the number of http resources allowed int he sspec table
#define SSPEC_MAXSPEC 144

/*
 * If you are using only the ZSERVER.LIB functions to manage the table
 * of HTTP pages, then you can disable code support for the
 * http_flashspec[] array.
 */
#define HTTP_NO_FLASHSPEC

/*** EndHeader */

/*** BeginHeader update_socket_config_webvar,SocketConfiguration,
 SocketParams,NetworkConfiguration, netconfig, Socket_Count,
 sockconfig, sockparams*/
void update_socket_config_webvar(void);

// Used for the configuration structure
typedef struct
{
	int  		valid;

   uint8    destnum;

   longword	ip;         // IP address to receive from and send to
                        // Use 0 to use only the first IP address to send
                        // messages to this port.  Use -1 to accept all and
                        // UDP broadcast all messages outgoing

	uint16	port;			// Port number (local and remote) of the socket

	uint8		iface;		// Interface on which to start the socket

   uint8    remote;		// 1 if this destination is across the satcom link

   uint8		enablestatus;

} SocketConfiguration;

// Used for the configuration structure
typedef struct
{
   int valid;
   int reconnect_wait;
   int statusintervalms;
} SocketParams;


// Used for the configuration structure
typedef struct
{
	int		valid;    //should be 12345 if file is valid
   byte		usedhcp;  //1 = use dhcp
   longword	ip;
   longword netmask;
   longword gateway;
} NetworkConfiguration;

extern NetworkConfiguration netconfig;

extern int socket_count;
extern SocketConfiguration sockconfig[MAX_UDP_SOCKETS+1];
extern SocketParams sockparams;

/*** EndHeader */

#use "WebVariables.c"
#use "dcrtcp.lib"

int socket_count;
SocketConfiguration sockconfig[MAX_UDP_SOCKETS+1];
SocketParams sockparams;
NetworkConfiguration netconfig;

void update_socket_config_webvar()
{
	char ipaddr[20];
   int ix =0;
   int i,d;

   ix += sprintf(&socketconfig[ix],"<H3><U>CURRENT SOCKET CONFIGURATION PARAMETERS</U></H3>\n");
   ix += sprintf(&socketconfig[ix],"<H3>Main Socket</H3>\n");
   ix += sprintf(&socketconfig[ix],"<TABLE BORDER=0 CELLSPACING=2 CELLPADDING=1>\n" \
   		"<TR>\n" \
         "<TD WIDTH=50  ALIGN=CENTER><B><U>Only Receive</U></B></TD>\n" \
         "<TD WIDTH=150 ALIGN=CENTER><B><U>IP Address</U></B></TD>\n" \
         "<TD WIDTH=150 ALIGN=CENTER><B><U>Port</U></B></TD>\n" \
         "<TD WIDTH=150 ALIGN=CENTER><B><U>Reply To Dest</U></B></TD>\n" \
	      "</TR>\n");
   ix += sprintf(&socketconfig[ix],
         "<TR>\n" \
         "<TD WIDTH=50  ALIGN=CENTER><B>%s</B></TD>\n" \
			"<TD WIDTH=150 ALIGN=CENTER><B>%s</B></TD>\n" \
         "<TD WIDTH=150 ALIGN=CENTER><B>%u</B></TD>\n" \
         "<TD WIDTH=150 ALIGN=CENTER><B>%d</B></TD>\n" \
	      "</TR></TABLE>\n",
         (sockconfig[MAX_UDP_SOCKETS].remote==1?"Yes":"No"),
	   	inet_ntoa(ips[MAX_UDP_SOCKETS],sockconfig[MAX_UDP_SOCKETS].ip),
	   	sockconfig[MAX_UDP_SOCKETS].port,
         sockconfig[MAX_UDP_SOCKETS].destnum);

   ix += sprintf(&socketconfig[ix],"<H3>Destination Sockets</H3>\n");
   ix += sprintf(&socketconfig[ix],"<TABLE BORDER=0 CELLSPACING=2 CELLPADDING=1>\n" \
   		"<TR>\n" \
      	"<TD WIDTH=50  ALIGN=CENTER><B><U>Dest #</U></B></TD>\n" \
         "<TD WIDTH=50  ALIGN=CENTER><B><U>Remote</U></B></TD>\n" \
         "<TD WIDTH=50  ALIGN=CENTER><B><U>Enable Status</U></B></TD>\n" \
         "<TD WIDTH=150 ALIGN=CENTER><B><U>IP Address</U></B></TD>\n" \
         "<TD WIDTH=150 ALIGN=CENTER><B><U>Port</U></B></TD>\n" \
         "<TD WIDTH=150 ALIGN=CENTER><B><U>Reply To Dest</U></B></TD>\n" \
	      "</TR>\n");

   ports[MAX_UDP_SOCKETS] =  sockconfig[MAX_UDP_SOCKETS].port;
   if(sockconfig[MAX_UDP_SOCKETS].remote)
         	strcpy(remote[MAX_UDP_SOCKETS],"checked");
   reply[MAX_UDP_SOCKETS] = sockconfig[MAX_UDP_SOCKETS].destnum;


   statusintervalms = sockparams.statusintervalms;


   for(i=0; i<(MAX_UDP_SOCKETS); i++)
   {
      d = pow(2.0,(float)i);
      remote[i][0] = 0;
   	if(sockconfig[i].valid)
      {
	   	ix += sprintf(&socketconfig[ix],
         "<TR>\n" \
      	"<TD WIDTH=50  ALIGN=CENTER><B>%d</B></TD>\n" \
         "<TD WIDTH=50  ALIGN=CENTER><B>%s</B></TD>\n" \
         "<TD WIDTH=50  ALIGN=CENTER><B>%s</B></TD>\n" \
			"<TD WIDTH=150 ALIGN=CENTER><B>%s</B></TD>\n" \
         "<TD WIDTH=150 ALIGN=CENTER><B>%u</B></TD>\n" \
         "<TD WIDTH=150 ALIGN=CENTER><B>%d</B></TD>\n" \
	      "</TR>\n",
	   	d,
         (sockconfig[i].remote==1?"Yes":"No"),
         (sockconfig[i].enablestatus==1?"Yes":"No"),
	   	inet_ntoa(ips[i],sockconfig[i].ip),
	   	sockconfig[i].port,
         sockconfig[i].destnum);


         reply[i] = sockconfig[i].destnum;
         ports[i] =  sockconfig[i].port;
         if(sockconfig[i].remote)
         	strcpy(remote[i],"checked");
         if(sockconfig[i].enablestatus == 1)
         	strcpy(status[i],"checked");

      }
      else
      {
        ips[i][0] = 0;
        ports[i] = 0;
        reply[i] = sockconfig[i].destnum;
      }
   }
   ix += sprintf(&socketconfig[ix],"</TABLE>");

   ix += sprintf(&socketconfig[ix],"<TABLE BORDER=0 CELLSPACING=2 CELLPADDING=1>\n" \
      "<TR>\n" \
     "<TD WIDTH=150  ALIGN=LEFT><B>Status Interval mS:</B></TD>\n" \
      "<TD WIDTH=150 ALIGN=LEFT><B>%u</B></TD>\n" \
      "</TR></TABLE>\n", sockparams.statusintervalms);

}


