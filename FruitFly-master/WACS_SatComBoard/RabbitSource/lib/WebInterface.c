/*** BeginHeader  */


/*
 * HTTP_MAXBUFFER is used for each web server to hold received and
 * transferred information.  In particular, it holds each header line
 * received from the web client.  Digest authentication generally
 * results in a large WWW-Authenticate header that can be larger than
 * the default HTTP_MAXBUFFER value of 256.  512 bytes should be
 * sufficient.
 */
#define HTTP_MAXBUFFER			 512

#define HTTP_MAXSERVERS        1			// Have only one active server.

/*
 * If you are using only the ZSERVER.LIB functions to manage the table
 * of HTTP pages, then you can disable code support for the
 * http_flashspec[] array.
 */
#define HTTP_NO_FLASHSPEC

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
#define SSPEC_USERSPERRESOURCE 	3

/*
 *  memmap forces the code into xmem.  Since the typical stack is larger
 *  than the root memory, this is commonly a desirable setting.  Another
 *  option is to do #memmap anymem 8096 which will force code to xmem when
 *  the compiler notices that it is generating within 8096 bytes of the
 *  end.
 */

#define ETH_MTU                1489		// Set the MTU to max Ethernet/WiFi MTU
#define TCP_BUF_SIZE           8192		// Make the TCP tx and rx buffers 4K each
#define MAX_TCP_SOCKET_BUFFERS 1		// Two sockets, one for HTTP and one for echo.


#memmap xmem
#use "dcrtcp.lib"
#use "http.lib"

void httptask(void* ptr);
void initpage();
int get_ip_form(HttpState *state);


/*
 *  ximport is a Dynamic C language feature that takes the binary image
 *  of a file, places it in extended memory on the controller, and
 *  associates a symbol with the physical address on the controller of
 *  the image.
 */
#ximport "./web/index.shtml"		index_html
#ximport "./web/WACSLogo.jpg"		wacslogo_jpg
#ximport "./web/status.shtml"		status_shtml

// This is the prototype for the CGI function that is used to process the
// numbers to be summed, to prompt on the stdio window for the sum, and to
// display the sum back to the web browser.
int proc_ip_form(HttpState *state);
int get_ip_form(HttpState *state);
int proc_dest_form(HttpState *state);
int get_dest_form(HttpState *state);
int start_stop(HttpState *state);
int get_start_stop(HttpState *state);
long counter;
long latency;
long throughput;



// This associates file extensions with file types.  The default for / must
// be first.
SSPEC_MIMETABLE_START
	SSPEC_MIME_FUNC(".shtml", MIMETYPE_HTML, shtml_handler),
	SSPEC_MIME(".html", MIMETYPE_HTML),
   SSPEC_MIME(".jpg", MIMETYPE_JPG),
	SSPEC_MIME(".cgi", MIMETYPE_HTML)
SSPEC_MIMETABLE_END




	int user1;
	int page1;
	int page2;
	int ch;

/*** EndHeader */

int get_ip_form(HttpState *state)
{

	sprintf(state->buffer, "<tr><td>Test1</td><td>Test2</td><td>Test3</td></tr>\r\n");
   state->headerlen = strlen(state->buffer);
   state->headeroff = 0;
   state->substate++;

   return 0;
}

      /*
<FORM ACTION="setIP.cgi" METHOD="POST">
	   <TABLE BORDER=0 CELLSPACING=2 CELLPADDING=1>
	      <TR>
         	<TD><B>IP Configuration:</B></TD>
	         <TD>
            	<input type="radio" name="cert" value="static" checked>  Static
               <input type="radio" name="cert" value="dhcp"> DHCP
	         <TD>
	      </TR>
	      <TR>
	         <TD><B>IP Address: </B></TD>
	         <TD><INPUT TYPE="TEXT" NAME="ipaddress" SIZE=15></TD>
	      <TR>
	   </TABLE>
	   <INPUT TYPE="SUBMIT" VALUE="Submit">
</FORM>

	<FORM ACTION="webprocess.cgi" METHOD="POST">
	   <TABLE BORDER=0 CELLSPACING=2 CELLPADDING=1>
	      <TR>
	         <TD><B>Number 1:</B></TD>
	         <TD><INPUT TYPE="TEXT" NAME="number1" SIZE=5></TD>
	      </TR>
	      <TR>
	         <TD><B>Number 2:</B></TD>
	         <TD><INPUT TYPE="TEXT" NAME="number2" SIZE=5></TD>
	      <TR>
	   </TABLE>
	   <INPUT TYPE="SUBMIT" VALUE="Submit">
	</FORM>
*/



void initpage()
{

/*
	 * HTTP_DIGEST_AUTH is the default authentication type when
	 * digest authentication has been enabled, so this line is not
	 * strictly necessary.  The other possible values are
	 * HTTP_BASIC_AUTH and HTTP_NO_AUTH.
	 */
	http_setauthentication(HTTP_DIGEST_AUTH);
	printf("Using digest authentication\n");

	/*
	 * The following lines add three users, a web page, and an image, and
	 * associates the users with the web page.  The userx_enabled
	 * variables are used to keep track of which users are current
	 * enabled.
	 */
	user1 = sauth_adduser("admin", "wacs", SERVER_HTTP);


	page1 = sspec_addxmemfile("/", index_html, SERVER_HTTP);
	sspec_adduser(page1, user1);
	sspec_setrealm(page1, "Admin");

	page2 = sspec_addxmemfile("index.html", index_html, SERVER_HTTP);
	sspec_adduser(page2, user1);
	sspec_setrealm(page2, "Admin");

	sspec_addxmemfile("/WACSLogo.jpg", wacslogo_jpg, SERVER_HTTP);

}


// This structure associates resource names with their locations in memory.
SSPEC_RESOURCETABLE_START
	SSPEC_RESOURCE_XMEMFILE("/", index_html),
	SSPEC_RESOURCE_XMEMFILE("/index.shtml", index_html),
   SSPEC_RESOURCE_XMEMFILE("/status.shtml", status_shtml)
	//SSPEC_RESOURCE_FUNCTION("/procipform.cgi", proc_ip_form)
   SSPEC_RESOURCE_FUNCTION("/ipform", get_ip_form)
   //SSPEC_RESOURCE_FUNCTION("/procdestform.cgi", proc_dest_form)
   //SSPEC_RESOURCE_FUNCTION("/startstop", start_stop)
   //SSPEC_RESOURCE_FUNCTION("/startform", get_start_stop)
   //SSPEC_RESOURCE_ROOTVAR("latency", &latency, INT32, "%ld")
   //SSPEC_RESOURCE_ROOTVAR("throughput", &throughput, INT32, "%ld")
   //SSPEC_RESOURCE_ROOTVAR("ipaddr", &ip, CHAR, "%s")
	//SSPEC_RESOURCE_ROOTVAR("counter", &counter, INT32, "%ld")
SSPEC_RESOURCETABLE_END



/*
 *		Run the HTTP server in the background
 */
void httptask(void* ptr)
{
	http_init();

	while(1)
   {
		http_handler();
	}
}

