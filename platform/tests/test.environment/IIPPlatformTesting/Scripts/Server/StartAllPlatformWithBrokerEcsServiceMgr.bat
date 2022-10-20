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

start /separate cmd.exe /c "echo "Running ecs... Please don't close it" & ecs.bat --iip.id=%1 > ecs.log"

:ecsStart
echo Waiting ecs to be Ready
TIMEOUT 3
for /f "tokens=*" %%i in ('findstr /c:"Startup completed" ecs.log') do (
  set ecsReady=%%i
)
IF "%ecsReady%" == "" GOTO ecsStart

echo "Ecs is started"

start /separate cmd.exe /c "echo "Running serviceMgr... Please don't close it" & serviceMgr.bat --iip.id=%1 > serviceMgr.log"

:serviceMgrStart
echo Waiting serviceMgr to be Ready
TIMEOUT 3
for /f "tokens=*" %%i in ('findstr /c:"Startup completed" serviceMgr.log') do (
  set serviceMgrReady=%%i
)
IF "%serviceMgrReady%" == "" GOTO serviceMgrStart

echo "ServiceMgr is started"

cd ..
cd ..

wmic process where "commandline like '%%brokerJars%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" > ProcessesIDs.info
wmic process where "commandline like '%%broker.bat%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" >> ProcessesIDs.info

wmic process where "commandline like '%%plJars%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" >> ProcessesIDs.info
wmic process where "commandline like '%%platform.bat%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" >> ProcessesIDs.info

wmic process where "commandline like '%%ecsJars%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" >> ProcessesIDs.info
wmic process where "commandline like '%%ecs.bat%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" >> ProcessesIDs.info

wmic process where "commandline like '%%svcJars%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" >> ProcessesIDs.info
wmic process where "commandline like '%%serviceMgr.bat%%' and not commandline like 'wmic%%'" get processid | find /v "ProcessId" >> ProcessesIDs.info
