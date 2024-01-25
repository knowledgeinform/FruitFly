/*** BeginHeader timestamp,timestring,gateway,dhcp,ip,netmask,socketconfig,ips,ports */
extern char socketconfig[4096];
extern long timestamp;
extern char timestring[40];
extern char dhcp[6];
extern char ip[16];
extern char netmask[16];
extern char gateway[16];
extern char ips[9][16];
extern uint16 ports[9];
extern uint16 reply[9];
extern uint16 statusintervalms;
extern char remote[9][8];
extern char status[9][8];
extern char dialnumber[32];
extern char password[32];
extern char ismaster[4];
extern char sattype[16];
extern char useencryption[4];
extern char ismasterchecked[8];
extern char useencryptionchecked[8];

extern char oldpassword[32];
extern char password1[32];
extern char password2[32];
extern char keyphrase[32];
extern char modemstatus[16];
extern char modemstatushtml[512];
/*** EndHeader */

char socketconfig[4096];
char gateway[16];
long timestamp;
char timestring[40];
char dhcp[6];
char ip[16];
char netmask[16];
char ips[9][16];
uint16 ports[9];
uint16 reply[9];
uint16 statusintervalms;
char remote[9][8];
char status[9][8];
char dialnumber[32];
char password[32];
char ismaster[4];
char sattype[16];
char useencryption[4];
char ismasterchecked[8];
char useencryptionchecked[8];

char oldpassword[32];
char password1[32];
char password2[32];
char keyphrase[32];
char modemstatus[16];
char modemstatushtml[512];