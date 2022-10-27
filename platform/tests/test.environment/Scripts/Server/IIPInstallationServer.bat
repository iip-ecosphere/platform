cd %~dp0

rmdir /s Install /q
mkdir Install
cd Install

curl https://jenkins-2.sse.uni-hildesheim.de/view/IIP-Ecosphere/job/IIP_Install/lastSuccessfulBuild/artifact/install.tar.gz -O install.tar.gz
tar xzpvf install.tar.gz

cd platformDependencies
call mvn install -U
cd..

call mvn package -DskipTests -U

for /f "tokens=1-2 delims=:" %%a in ('ipconfig^|find "IPv4"') do set ip=%%b
set ip=%ip: =%
echo %ip%

@echo off
set "replace=147.172.178.145"
set "replaced=%ip%"

set "source=src\main\easy\InstallTest.ivml"
set "target=InstallTest.ivml"

setlocal enableDelayedExpansion
(
   for /F "tokens=1* delims=:" %%a in ('findstr /N "^" %source%') do (
      set "line=%%b"
      if defined line set "line=!line:%replace%=%replaced%!"
      echo(!line!
   )
) > %target%
endlocal

move InstallTest.ivml src\main\easy\InstallTest.ivml

if "%~2"=="True" (
  setlocal ENABLEDELAYEDEXPANSION
  for /f "tokens=1,*" %%a in ('TYPE src\main\easy\InstallTest.ivml ^| find /N "Application"') do set LineNum=%%a
  SET LineNum=!LineNum:[=!
  SET LineNum=!LineNum:]=!
  set /A LineNum=!LineNum!+1
  echo !LineNum!
  
  for /f "tokens=1,*" %%a in ('TYPE src\main\easy\InstallTest.ivml ^| find /N "generation setup"') do set LineNum2=%%a
  SET LineNum2=!LineNum2:[=!
  SET LineNum2=!LineNum2:]=!
  set /A LineNum2=!LineNum2!-1
  echo !LineNum2!

  set /a count = 0
  (
    for /F "tokens=1* delims=:" %%a in ('findstr /N "^" src\main\easy\InstallTest.ivml') do (
      set "line=%%b"
      set /a count += 1
      if !count!==!LineNum! echo         createContainer = true
      if !count!==!LineNum2! (
        echo.
        echo     // ---------- Device ------------
        echo.
        echo     EcsDevice device = {
        echo         containerType = ContainerType::C1Ecs_C2Svc_App
        echo     };
      )
      echo(!line!
    )
  ) > InstallTest.ivml
  endlocal
  
  move InstallTest.ivml src\main\easy\InstallTest.ivml
  
  echo Containers Added
)

call mvn exec:java 

rmdir /s DeviceFolder /q
mkdir DeviceFolder
cd DeviceFolder
mkdir ecsJars
mkdir broker
mkdir svcJars
mkdir ecsSvcJars

cd..
Xcopy "gen\ecsJars" "DeviceFolder\ecsJars" /E /H /C /I
Xcopy "gen\broker" "DeviceFolder\broker" /E /H /C /I
Xcopy "gen\svcJars" "DeviceFolder\svcJars" /E /H /C /I
Xcopy "gen\ecsSvcJars" "DeviceFolder\ecsSvcJars" /E /H /C /I
copy /y gen\ecs.sh DeviceFolder\ecs.sh
copy /y gen\ecs.bat DeviceFolder\ecs.bat
copy /y gen\serviceMgr.sh DeviceFolder\serviceMgr.sh
copy /y gen\serviceMgr.bat DeviceFolder\serviceMgr.bat
copy /y gen\ecsServiceMgr.sh DeviceFolder\ecsServiceMgr.sh
copy /y gen\ecsServiceMgr.bat DeviceFolder\ecsServiceMgr.bat
copy /y gen\SimpleMeshTestingApp\target\SimpleMeshTestingApp-0.1.0-SNAPSHOT-bin.jar DeviceFolder\SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar

