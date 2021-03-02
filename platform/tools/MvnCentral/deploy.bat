@echo off

REM install wget into PATH
REM install maven into PATH
REM install GnuPGP
REM add your account settings for ossrh into maven setup (use authentication token)
REM copy this script into an empty directory
REM run this script
REM goto https://oss.sonatype.org/#welcome, staging repositories, deuni-hildesheim...*, close for check/deploy

setlocal ENABLEDELAYEDEXPANSION
SET VERSION=0.1.0
SET TARGET=https://oss.sonatype.org/service/local/staging/deploy/maven2
SET REPO=ossrh-iip
SET DEPLOYCMD=mvn gpg:sign-and-deploy-file -Durl=%TARGET% -DrepositoryId=%REPO%
SET FOLDER=target\jars

REM call %DEPLOYCMD% -DpomFile=%FOLDER%\platformDependencies-%VERSION%.pom -Dfile=%FOLDER%\platformDependencies-%VERSION%.pom
for /r %%f in (%FOLDER%\*%VERSION%.jar) do (
  SET fn=%%~nf
  SET an=!fn:%VERSION%=!
  call :DeployArtifact !an! %VERSION%
)

REM done here, don't go into "sub-program"
goto :end

REM deploy a certain artifact (including classified artifacts)
REM param1: name/id of the artifact
REM param2: version of the artifact to deploy
:DeployArtifact
    setlocal
    SET ARTIFACTNAME=%1
    SET ARTIFACTVERSION=%2
    SET ARTIFACTPREFIX=%ARTIFACTNAME%%ARTIFACTVERSION%
    SET POM=%ARTIFACTPREFIX%.pom
    SET JAR=%ARTIFACTPREFIX%.jar
    SET SOURCES=%ARTIFACTPREFIX%-sources.jar
    SET TESTS=%ARTIFACTPREFIX%-tests.jar
    SET TESTSOURCES=%ARTIFACTPREFIX%-test-sources.jar
    SET JAVADOC=%ARTIFACTPREFIX%-javadoc.jar
    SET TESTJAVADOC=%ARTIFACTPREFIX%-test-javadoc.jar
    
    REM deploy to central
    call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%JAR%
    call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%TESTS% -Dclassifier=tests
    call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%SOURCES% -Dclassifier=sources
    call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%TESTSOURCES% -Dclassifier=test-sources
    call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%JAVADOC% -Dclassifier=javadoc
    call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%TESTJAVADOC% -Dclassifier=test-javadoc

    endlocal
    goto :eof
    
:end