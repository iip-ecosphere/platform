@echo off
setlocal enabledelayedexpansion

cd Files 
mkdir platformFiles
cd platformFiles

wget http://192.168.81.100:4200/download/DeviceFolder.tar.gz
tar xzpvf DeviceFolder.tar.gz

start /separate cmd.exe /c "echo "Running ecs... Please don't close it" & ecs.bat --iip.id=%2 > ecs.log"

:ecsStart
echo Waiting ecs to be Ready
TIMEOUT 3
for /f "tokens=*" %%i in ('findstr /c:"Startup completed" ecs.log') do (
  set ecsReady=%%i
)
IF "%ecsReady%" == "" GOTO ecsStart

wmic process where "commandline like '%%ecsJars%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" > ecsProcessesIDs.info
wmic process where "commandline like '%%ecs.bat%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" >> ecsProcessesIDs.info

echo "Ecs is started"

start /separate cmd.exe /c "echo "Running serviceMgr... Please don't close it" & serviceMgr.bat --iip.id=%2 > serviceMgr.log"

:serviceMgrStart
echo Waiting serviceMgr to be Ready
TIMEOUT 3
for /f "tokens=*" %%i in ('findstr /c:"Startup completed" serviceMgr.log') do (
  set serviceMgrReady=%%i
)
IF "%serviceMgrReady%" == "" GOTO serviceMgrStart

wmic process where "commandline like '%%svcJars%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" > svcProcessesIDs.info
wmic process where "commandline like '%%serviceMgr.bat%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" >> svcProcessesIDs.info

echo "ServiceMgr is started"
echo "ServiceMgr and Ecs are Running... Please don't close it"

set svcCount=0
for /F "tokens=*" %%i in (svcProcessesIDs.info) do (
  set /a svcCount+=1
  set svc[!svcCount!]=%%i
)

:serviceMgr
TIMEOUT 10 > nul
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %svc[1]%"') do (
  set svcpid=%%i
)
IF NOT "%svcpid%" == "INFO: No tasks are running which match the specified criteria." GOTO serviceMgr

set ecsCount=0
for /F "tokens=*" %%i in (ecsProcessesIDs.info) do (
  set /a ecsCount+=1
  set ecs[!ecsCount!]=%%i
)

:ecs
TIMEOUT 10 > nul
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %ecs[1]%"') do (
  set svcpid=%%i
)
IF NOT "%svcpid%" == "INFO: No tasks are running which match the specified criteria." GOTO ecs