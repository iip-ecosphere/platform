@echo off
setlocal enabledelayedexpansion

cd Files 

set svcCount=0
for /F "tokens=*" %%i in (svcProcessesIDs.info) do (
  set /a svcCount+=1
  set svc[!svcCount!]=%%i
)

echo Waiting serviceMgr to be stopped

start /separate cmd.exe /c "echo "Stopping serviceMgr... Please don't close it" & SendSignalCtrlC64.exe %svc[1]% > exit.log"
TIMEOUT 3 > nul
start /separate cmd.exe /c "echo "Stopping serviceMgr... Please don't close it" & SendSignalCtrlC64.exe %svc[3]% >> exit.log"

:svc1Start
TIMEOUT 3 > nul
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %svc[1]%"') do (
  set svcpid=%%i
)
IF NOT "%svcpid%" == "INFO: No tasks are running which match the specified criteria." (GOTO svc1Start) ELSE (Echo Waiting serviceMgr to be stopped)

:svc2Start
TIMEOUT 3 > nul
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %svc[3]%"') do (
  set svcpid=%%i
)
IF NOT "%svcpid%" == "INFO: No tasks are running which match the specified criteria." (GOTO svc2Start) ELSE (Echo Waiting serviceMgr to be stopped)

echo "ServiceMgr is stopped"

set ecsCount=0
for /F "tokens=*" %%i in (ecsProcessesIDs.info) do (
  set /a ecsCount+=1
  set ecs[!ecsCount!]=%%i
)

echo Waiting ecs to be stopped

start /separate cmd.exe /c "echo "Stopping ecs... Please don't close it" & SendSignalCtrlC64.exe %ecs[1]% >> exit.log"
TIMEOUT 3 > nul
start /separate cmd.exe /c "echo "Stopping ecs... Please don't close it" & SendSignalCtrlC64.exe %ecs[3]% >> exit.log"

:ecs1Start
TIMEOUT 3 > nul
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %ecs[1]%"') do (
  set ecspid=%%i
)
IF NOT "%ecspid%" == "INFO: No tasks are running which match the specified criteria." (GOTO ecs1Start) ELSE (Echo Waiting ecs to be stopped)

:ecs2Start
TIMEOUT 3 > nul
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %ecs[3]%"') do (
  set ecspid=%%i
)
IF NOT "%ecspid%" == "INFO: No tasks are running which match the specified criteria." (GOTO ecs2Start) ELSE (Echo Waiting ecs to be stopped)

echo "Ecs is stopped"