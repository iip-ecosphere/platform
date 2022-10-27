@echo off
setlocal enabledelayedexpansion

cd Files

set plCount=0
for /F "tokens=*" %%i in (ProcessesIDs.info) do (
  set /a plCount+=1
  set pl[!plCount!]=%%i
)

echo Waiting platform to be stopped

start /separate cmd.exe /c "echo "Stopping platform... Please don't close it" & SendSignalCtrlC64.exe %pl[7]% > exit.log"
TIMEOUT 3 > nul
start /separate cmd.exe /c "echo "Stopping platform... Please don't close it" & SendSignalCtrlC64.exe %pl[9]% >> exit.log"

:Start1
TIMEOUT 3 > nul
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %pl[7]%"') do (
  set plpid=%%i
)
IF NOT "%plpid%" == "INFO: No tasks are running which match the specified criteria." (GOTO Start1) ELSE (Echo Waiting platform to be stopped)

:Start2
TIMEOUT 3 > nul
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %pl[9]%"') do (
  set plpid=%%i
)
IF NOT "%plpid%" == "INFO: No tasks are running which match the specified criteria." (GOTO Start2) ELSE (Echo Waiting platform to be stopped)

echo "Platform is stopped"

echo Waiting broker to be stopped

start /separate cmd.exe /c "echo "Stopping broker... Please don't close it" & taskkill /F /PID %pl[1]% >> exit.log"
TIMEOUT 3 > nul
start /separate cmd.exe /c "echo "Stopping broker... Please don't close it" & taskkill /F /PID %pl[3]% >> exit.log"
TIMEOUT 3 > nul
start /separate cmd.exe /c "echo "Stopping broker... Please don't close it" & taskkill /F /PID %pl[4]% >> exit.log"
TIMEOUT 3 > nul
start /separate cmd.exe /c "echo "Stopping broker... Please don't close it" & taskkill /F /PID %pl[5]% >> exit.log"

:Start3
TIMEOUT 3 > nul
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %pl[1]%"') do (
  set brpid=%%i
)
IF NOT "%brpid%" == "INFO: No tasks are running which match the specified criteria." (GOTO Start3) ELSE (Echo Waiting broker to be stopped)

:Start4
TIMEOUT 3 > nul
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %pl[3]%"') do (
  set brpid=%%i
)
IF NOT "%brpid%" == "INFO: No tasks are running which match the specified criteria." (GOTO Start4) ELSE (Echo Waiting broker to be stopped)

:Start5
TIMEOUT 3 > nul
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %pl[4]%"') do (
  set brpid=%%i
)
IF NOT "%brpid%" == "INFO: No tasks are running which match the specified criteria." (GOTO Start5) ELSE (Echo Waiting broker to be stopped)

:Start6
TIMEOUT 3 > nul
for /f "tokens=*" %%i in ('tasklist /fi "pid eq %pl[5]%"') do (
  set brpid=%%i
)
IF NOT "%brpid%" == "INFO: No tasks are running which match the specified criteria." (GOTO Start6) ELSE (Echo Waiting broker to be stopped)

echo "broker is stopped"

echo " " > ProcessesIDs.info
