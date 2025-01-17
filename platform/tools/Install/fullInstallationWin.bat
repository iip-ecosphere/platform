@echo off
setlocal
cd %~dp0
setlocal ENABLEDELAYEDEXPANSION

echo "Oktoflow platform installation (Windows)"
echo "For installing prerequisites or Angular, administrator permissions may be required!"

REM Check current Docker 

set RecommendDocker=20.10.7
set /a count = 0
for /F "tokens=2 delims= " %%a in ('docker version') do (
   set /a count += 1
   if !count!==2 (
      set dockerVersion=%%a
      goto :dockerCheck
   )
)

:dockerCheck

if "%dockerVersion%" == "%RecommendDocker%" set dockerVersionCheck="Ok"
if NOT "%dockerVersion%" == "%RecommendDocker%" set dockerVersionCheck="Diff"
if "%dockerVersion%" == "is." set dockerVersionCheck="Non"
if %count% EQU 0 set dockerVersionCheck="Non"

if %dockerVersionCheck% == "Ok" goto :endDockerCheck

if %dockerVersionCheck% == "Diff" (
   echo "You have Docker version %dockerVersion%, it is recommended to have version %RecommendDocker%"
   goto :answerDocker
)

if %dockerVersionCheck% == "Non" (
   echo "Docker is not installed"
   echo "If you intend to use Docker containers generated by the platform, then you should install Docker"
   echo "It is recommended to have version %RecommendDocker%"
   echo "You should use Windows Subsystem for Linux (WSL) to run Docker on Windows (recommended to follow this link https://learn.microsoft.com/en-us/windows/wsl/install-manual)"
)

:answerDocker
echo =====================================================
set /P c=Do you want to continue the platform installation without Docker (else terminate)? [y/n]
if /I "%c%" EQU "N" goto :installEndNow
if /I "%c%" EQU "Y" goto :endDockerCheck
goto :answerDocker

:endDockerCheck

if not exist "Platform" mkdir "Platform"
cd Platform

:prerequisites
echo =====================================================
echo "Installing prerequisites Java 17, Maven version 3.9.7, and Python version 3.9"
echo "This action will change Windows Environment Variables"
set /P c=Do you want to install the prerequisites (skip only if already installed)? [y/n]
if /I "%c%" EQU "Y" goto :prerequisitesYes
if /I "%c%" EQU "N" goto :prerequisitesNo
goto :prerequisites

:prerequisitesYes

echo =====================================================
echo "Make sure that Python is set to OFF, in the (App execution aliases) for Windows 11 - Settings > Apps > apps & features > App execution aliases"
pause

REM Check current Java version 

for /F "tokens=1* delims=:" %%a in ('java -version 2^>^&1') do (
   set javaVersion=%%a
   goto :endJavaVersion
)

:endJavaVersion
(for /f "tokens=3 delims= " %%a in ('echo %javaVersion%') do set "javaVersion2=%%a")
(for /f "tokens=1,2 delims=." %%a in ('echo %javaVersion2%') do set "javaVersion3=%%a.%%b")
set "javaVersion4=!javaVersion3:"=!"
set /a javaVersion4=%javaVersion4

set /a javaLimit=17

if %javaVersion4% GEQ %javaLimit% set javaVersionCheck="Ok"
if %javaVersion4% LSS %javaLimit% set javaVersionCheck="Diff"
if %javaVersion4% EQU 0 set javaVersionCheck="Non"

REM Check current Maven version

for /F "tokens=1* delims=:" %%a in ('mvn -version 2^>^&1') do (
   set mvnVersion=%%a
   goto :endMvnVersion
)
:endMvnVersion

(for /f "tokens=3 delims= " %%a in ('echo "%mvnVersion%"' ) do set "mvnVersion2=%%a")

set RecommendMvn=3.9.7

if "%mvnVersion2%" == "%RecommendMvn%" set mvnVersionCheck="Ok"
if NOT "%mvnVersion2%" == "%RecommendMvn%" set mvnVersionCheck="Diff"
if "%mvnVersion2%" == "not" set mvnVersionCheck="Non"

REM Check current Python version

for /F "tokens=1* delims=:" %%a in ('python --version 2^>^&1') do (
   set pythonVersion=%%a
   goto :endpythonVersion
)
:endpythonVersion

(for /f "tokens=2 delims= " %%a in ('echo %pythonVersion%' ) do set pythonVersion2=%%a)
(for /f "tokens=1,2 delims=." %%a in ('echo %pythonVersion2%' ) do set pythonVersion3=%%a.%%b)

set RecommendPython=3.9

if "%pythonVersion3%" == "%RecommendPython%" set pythonVersionCheck="Ok"
if NOT "%pythonVersion3%" == "%RecommendPython%" set pythonVersionCheck="Diff"
if "%pythonVersion3%" == "is." set pythonVersionCheck="Non"

REM Install Java version 17 

if %javaVersionCheck% == "Ok" goto :skipJava
if %javaVersionCheck% == "Diff" goto :askJava
if %javaVersionCheck% == "Non" goto :installJava

:askJava
echo "The Java version you have is less than the minimum requirement Java %javaLimit% for the IIP-Ecosphere platform"

:answerJava
set /P c=Do you want to install Java 17 - you should have at least Java %javaLimit%? - You can't skip this step; if you skip it the installation will end. [y/n]?
if /I "%c%" EQU "Y" goto :installJava
if /I "%c%" EQU "N" goto :installEndNow
goto :answerJava

:installJava

curl https://download.oracle.com/java/17/archive/jdk-17.0.10_windows-x64_bin.zip -o openjdk.zip
tar xzpvf openjdk.zip
setx JAVA_HOME "%cd%\jdk-17"
SET JAVA_HOME=%cd%\jdk-17
setx Path "%Path%;%JAVA_HOME%\bin"
SET Path=%Path%;%JAVA_HOME%\bin
netsh advfirewall firewall add rule name="Java" dir=in action=allow program="%JAVA_HOME%\bin\java.exe" enable=yes

:skipJava

REM Install Maven version 3.9.7

if %mvnVersionCheck% == "Ok" goto :skipMaven
if %mvnVersionCheck% == "Diff" goto :askMaven
if %mvnVersionCheck% == "Non" goto :installMaven

:askMaven
echo "You have Maven version %mvnVersion2%, it is recommended to have version %RecommendMvn%"

:answerMaven
set /P c=Do you want to install Maven %RecommendMvn% and replace Maven default to %RecommendMvn%? - You might skip this step [y/n]?
if /I "%c%" EQU "Y" goto :installMaven
if /I "%c%" EQU "N" goto :skipMaven
goto :answerMaven

:installMaven

curl https://archive.apache.org/dist/maven/maven-3/3.9.7/binaries/apache-maven-3.9.7-bin.zip -o maven.zip
tar xzpvf maven.zip
setx MAVEN_HOME "%cd%\apache-maven-3.9.7"
SET MAVEN_HOME=%cd%\apache-maven-3.9.7
setx Path "%Path%;%MAVEN_HOME%\bin"
SET Path=%Path%;%MAVEN_HOME%\bin

:skipMaven

REM Install Python version 3.9

if %pythonVersionCheck% == "Ok" goto :skipPython
if %pythonVersionCheck% == "Diff" goto :askPython
if %pythonVersionCheck% == "Non" goto :installPython

:askPython
echo "You have Maven version %pythonVersion3%, it is recommended to have version %RecommendPython%"

:answerPython
set /P c=Do you want to install Python %RecommendPython% and replace Python default to %RecommendPython%? - You might skip this step [y/n]?
if /I "%c%" EQU "Y" goto :installPython
if /I "%c%" EQU "N" goto :skipPython
goto :answerMaven

:installPython

curl https://www.python.org/ftp/python/3.9.6/python-3.9.6-amd64.exe -o python-3.9.6-amd64.exe
python-3.9.6-amd64.exe InstallAllUsers=1 PrependPath=1 Include_test=0 /quiet
SET Path=%Path%;C:\Program Files\Python39\Scripts\;C:\Program Files\Python39\
pip install pyflakes

:skipPython

:prerequisitesNo

for /f "tokens=1-2 delims=:" %%a in ('ipconfig^|find "IPv4"') do set ip=%%b
set ip=%ip: =%
echo %ip%

if %dockerVersionCheck% == "Non" (
   goto :RegistryEnd
)

:choiceRegistry
set /P c=Do you want to start Docker Private Registry (You should have Docker installed, be carefull it will restart Docker service) for the platform? - You might skip this step [Y/N]?
if /I "%c%" EQU "Y" goto :RegistryYes
if /I "%c%" EQU "N" goto :RegistryEnd
goto :choiceRegistry

:RegistryYes
if not exist "%programdata%\DockerDesktop\config\" mkdir "%programdata%\DockerDesktop\config\"
echo { > "%programdata%\DockerDesktop\config\daemon.json"
echo   "insecure-registries" : ["%ip%:5001"] >> "%programdata%\DockerDesktop\config\daemon.json"
echo } >> "%programdata%\DockerDesktop\config\daemon.json"

TASKKILL /F /IM "Docker Desktop.exe" /T 
"%PROGRAMFILES%\Docker\Docker\Docker Desktop.exe"
echo wait 2 minites to restart docker
Timeout /T 100 /NOBREAK > NUL

docker  run -d --restart=always --name registry -e REGISTRY_HTTP_ADDR=0.0.0.0:5001 -p 5001:5001 registry:2

RegistryRun="Yes"

:RegistryEnd

if not exist "Install" mkdir "Install"
cd Install

curl https://jenkins-2.sse.uni-hildesheim.de/view/IIP-Ecosphere/job/IIP_Install/lastSuccessfulBuild/artifact/platform/tools/Install/install.zip -o install.zip
tar xzpvf install.zip

cd platformDependencies/
python -m pip install -r requirements.txt
cd ..
    
@echo off
set "replace=147.172.178.145"
set "replaced=%ip%"

set "source=src\main\easy\TechnicalSetup.ivml"
set "target=TechnicalSetup.ivml"

(
   for /F "tokens=1* delims=:" %%a in ('findstr /N "^" %source%') do (
      set "line=%%b"
      if defined line set "line=!line:%replace%=%replaced%!"
      echo(!line!
   )
) > %target%

move TechnicalSetup.ivml src\main\easy\TechnicalSetup.ivml

if NOT %dockerVersionCheck% == "Non" (
   RegistryRun="No"
   goto :noContainersChange
)

set "replace=true"
set "replaced=false"

set "source=src\main\easy\TechnicalSetup.ivml"
set "target=TechnicalSetup.ivml"

(
   for /F "tokens=1* delims=:" %%a in ('findstr /N "^" %source%') do (
      set "line=%%b"
      if "%%b" == "    containerGeneration = true;" set "line=!line:%replace%=%replaced%!"
	  if "%%b" == "    platformContainerGeneration = true;" set "line=!line:%replace%=%replaced%!"
      echo(!line!
   )
) > %target%

move TechnicalSetup.ivml src\main\easy\TechnicalSetup.ivml

:noContainersChange

if NOT "%RegistryRun%" == "Yes" (
  goto :noRegistryExist
)

for /f "tokens=1,*" %%a in ('TYPE src\main\easy\TechnicalSetup.ivml ^| find /N "generation setup"') do set LineNum2=%%a
SET LineNum2=!LineNum2:[=!
SET LineNum2=!LineNum2:]=!
set /A LineNum2=!LineNum2!-1
echo !LineNum2!

set /a count = 0
(
  for /F "tokens=1* delims=:" %%a in ('findstr /N "^" src\main\easy\TechnicalSetup.ivml') do (
    set "line=%%b"
    set /a count += 1
    if !count!==!LineNum2! (
      echo.
      echo     // ---------- Registry ------------
      echo.
      echo     containerManager = DockerContainerManager {
      echo         registry = "%ip%:5001"
      echo     };
    )
    echo(!line!
  )
) > TechnicalSetup.ivml

move TechnicalSetup.ivml src\main\easy\TechnicalSetup.ivml

:noRegistryExist

call mvn install -Diip.easy.tracing=TOP

REM Angular version check missing
echo "To use the management UI for the platform, you should install angular version 14."
echo "This step requires administrator permissions."
set /P c=Do you want to continue with the Angular installation (else terminate)? [y/n]
if /I "%c%" EQU "N" goto :installEndHint
if /I "%c%" EQU "Y" goto :installAngular

:installAngular

curl https://nodejs.org/download/release/v16.10.0/node-v16.10.0-x64.msi -o node-v16.10.0-x64.msi
echo "Installing node.js. This may take a while..."
node-v16.10.0-x64.msi
setx Path "%Path%;C:\Program Files\nodejs"
SET Path=%Path%;C:\Program Files\nodejs
call npm install -g @angular/cli@14.2.11
call npm install -g express@4.18.1
call npm install -g cors@2.8.5

:installEndHint

echo "The following commands were created in Platform\Install\gen:"
echo "- broker\broker.bat starts the configured communication broker (cd needed)"
echo "- platform.bat starts the central platform services"
echo "- mgtUi.bat starts the Angular-based management UI (Angular required, http://localhost:4200)"
echo "- per device that shall execute services, either ECS-Runtime and service manager or the combined"
echo "  combined ECS-Runtime-Servicemanager must be executed"
echo "  - ecs.bat starts the ECS-Runtime"
echo "  - serviceMgr.bat starts the service manager"
echo "  - ecsServiceMgr.bat starts the combined ECS-Runtime/Service-Manager"
echo "- cli.bat starts the platform command line interface"
echo "In individual shells, start at least the broker, the central services and the device services, then" 
echo "the included application (cli.bat deploy artifacts/deployment.yaml). On a permanent installation, only" 
echo "accessing the UI or the CLI is needed."
echo "Please consult the installation overview for more information."

:installEndNow

endlocal
