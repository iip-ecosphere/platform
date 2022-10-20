@echo off
setlocal enabledelayedexpansion

cd Files\Install\gen\broker

start /separate cmd.exe /c "echo "Running broker... Please don't close it" & broker.bat > broker.log"

:Start1
echo Waiting broker to be Ready
TIMEOUT 3
for /f "tokens=*" %%i in ('findstr /c:"Qpid Broker Ready" broker.log') do (
  set brokerReady=%%i
)
IF "%brokerReady%" == "" GOTO Start1

echo "Broker is started"

cd ..

start /separate cmd.exe /c "echo "Running platform... Please don't close it" & platform.bat > platform.log"

:Start2
echo Waiting platform to be Ready
TIMEOUT 3
for /f "tokens=*" %%i in ('findstr /c:"Startup completed" platform.log') do (
  set platformReady=%%i
)
IF "%platformReady%" == "" GOTO Start2

echo "Platform is started"

cd ..
cd ..

wmic process where "commandline like '%%brokerJars%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" > ProcessesIDs.info
wmic process where "commandline like '%%broker.bat%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" >> ProcessesIDs.info

wmic process where "commandline like '%%plJars%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" >> ProcessesIDs.info
wmic process where "commandline like '%%platform.bat%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" >> ProcessesIDs.info

echo "Broker and Platform are Running... Please don't close it"

set plCount=0
for /F "tokens=*" %%i in (ProcessesIDs.info) do (
  set /a plCount+=1
  set pl[!plCount!]=%%i
)

:platform
TIMEOUT 10 > nul
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %pl[7]%"') do (
  set plpid=%%i
)
IF NOT "%plpid%" == "INFO: No tasks are running which match the specified criteria." GOTO platform

:broker
TIMEOUT 10 > nul
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %pl[1]%"') do (
  set brpid=%%i
)
IF NOT "%brpid%" == "INFO: No tasks are running which match the specified criteria." GOTO broker
