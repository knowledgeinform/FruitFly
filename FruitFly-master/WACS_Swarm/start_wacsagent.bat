@echo off
SET waitYear=2010
SET waitMonth=08

:WaitStart
FOR /F "TOKENS=1* DELIMS= " %%A IN ('DATE/T') DO SET CDATE=%%B
FOR /F "TOKENS=1,2 eol=/ DELIMS=/ " %%A IN ('DATE/T') DO SET mm=%%B
FOR /F "TOKENS=1,2 DELIMS=/ eol=/" %%A IN ('echo %CDATE%') DO SET dd=%%B
FOR /F "TOKENS=2,3 DELIMS=/ " %%A IN ('echo %CDATE%') DO SET yyyy=%%B
FOR /F "TOKENS=1,2* DELIMS=: " %%A IN ('TIME/T') DO SET CTIME=%%C%%A%%B

echo testing for time sync: current = %mm%,%yyyy% - thresh = %waitMonth%,%waitYear%
IF %yyyy% LSS %waitYear% GOTO WaitStart
IF %yyyy% GTR %waitYear% GOTO WaitEnd
IF %mm% LSS %waitMonth% GOTO WaitStart

:WaitEnd
echo time sync detected
ant wacsagent > "wacsagentLog_"%mm%%dd%%yyyy%"_"%CTIME%".txt"