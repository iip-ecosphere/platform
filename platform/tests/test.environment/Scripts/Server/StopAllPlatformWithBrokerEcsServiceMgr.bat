@echo off
setlocal enabledelayedexpansion

cd Files

set plCount=0
for /F "tokens=*" %%i in (ProcessesIDs.info) do (
  set /a plCount+=1
  set svc[!plCount!]=%%i
)
start /separate cmd.exe /c "echo "Stopping serviceMgr... Please don't close it" & SendSignalCtrlC64.exe %svc[13]% > exit.log"
TIMEOUT 3
start /separate cmd.exe /c "echo "Stopping serviceMgr... Please don't close it" & SendSignalCtrlC64.exe %svc[15]% >> exit.log"


:Start1
echo Waiting serviceMgr to be stopped
TIMEOUT 3
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %svc[13]%"') do (
  set svcpid=%%i
)
IF NOT "%svcpid%" == "INFO: No tasks are running which match the specified criteria." GOTO Start1

:Start2
echo Waiting serviceMgr to be stopped
TIMEOUT 3
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %svc[15]%"') do (
  set svcpid=%%i
)
IF NOT "%svcpid%" == "INFO: No tasks are running which match the specified criteria." GOTO Start2

echo "serviceMgr is stopped"

start /separate cmd.exe /c "echo "Stopping ecs... Please don't close it" & SendSignalCtrlC64.exe %svc[9]% >> exit.log"
TIMEOUT 3
start /separate cmd.exe /c "echo "Stopping ecs... Please don't close it" & SendSignalCtrlC64.exe %svc[11]% >> exit.log"


:Start3
echo Waiting ecs to be stopped
TIMEOUT 3
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %svc[9]%"') do (
  set ecspid=%%i
)
IF NOT "%ecspid%" == "INFO: No tasks are running which match the specified criteria." GOTO Start3

:Start4
echo Waiting ecs to be stopped
TIMEOUT 3
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %svc[11]%"') do (
  set ecspid=%%i
)
IF NOT "%ecspid%" == "INFO: No tasks are running which match the specified criteria." GOTO Start4

echo "ecs is stopped"

start /separate cmd.exe /c "echo "Stopping platform... Please don't close it" & SendSignalCtrlC64.exe %svc[5]% >> exit.log"
TIMEOUT 3
start /separate cmd.exe /c "echo "Stopping platform... Please don't close it" & SendSignalCtrlC64.exe %svc[7]% >> exit.log"


:Start1
echo Waiting platform to be stopped
TIMEOUT 3
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %svc[5]%"') do (
  set plpid=%%i
)
IF NOT "%plpid%" == "INFO: No tasks are running which match the specified criteria." GOTO Start1

:Start2
echo Waiting platform to be stopped
TIMEOUT 3
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %svc[7]%"') do (
  set plpid=%%i
)
IF NOT "%plpid%" == "INFO: No tasks are running which match the specified criteria." GOTO Start2

echo "Platform is stopped"

start /separate cmd.exe /c "echo "Stopping broker... Please don't close it" & taskkill /F /PID %svc[1]% >> exit.log"
TIMEOUT 3
start /separate cmd.exe /c "echo "Stopping broker... Please don't close it" & taskkill /F /PID %svc[3]% >> exit.log"


:Start3
echo Waiting broker to be stopped
TIMEOUT 3
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %svc[1]%"') do (
  set brpid=%%i
)
IF NOT "%brpid%" == "INFO: No tasks are running which match the specified criteria." GOTO Start3

:Start4
echo Waiting broker to be stopped
TIMEOUT 3
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %svc[3]%"') do (
  set brpid=%%i
)
IF NOT "%brpid%" == "INFO: No tasks are running which match the specified criteria." GOTO Start4

echo "broker is stopped"

echo " " > ProcessesIDs.info
