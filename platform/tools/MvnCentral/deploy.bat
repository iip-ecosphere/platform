@echo off

REM install maven into PATH
REM install GnuPGP
REM add your account settings for ossrh into maven setup (use authentication token)
REM copy this script into an empty directory
REM run mvn package
REM run this script (once for basix, once for platform with different version numbers if needed)
REM goto https://oss.sonatype.org/#welcome, staging repositories, deiip-ecosphere...*, close for check/deploy

setlocal ENABLEDELAYEDEXPANSION
SET VERSION=0.2.0
SET TARGET=https://oss.sonatype.org/service/local/staging/deploy/maven2
SET REPO=ossrh-iip
SET DEPLOYCMD=mvn gpg:sign-and-deploy-file -Durl=%TARGET% -DrepositoryId=%REPO%
SET FOLDER=target\jars

for /r %%f in (%FOLDER%\*%VERSION%.jar) do (
  SET fn=%%~nf
  SET an=!fn:%VERSION%=!
  call :DeployArtifact !an! %VERSION%
)

REM alternative if matches do not work
REM call :DeployArtifact basyx.components.AASServer- 1.0.0
REM call :DeployArtifact basyx.components.lib- 1.0.0
REM call :DeployArtifact basyx.components.registry- 1.0.2
REM call :DeployArtifact basyx.sdk- 1.0.0

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
    SET PYTHON=%ARTIFACTPREFIX%-python.jar
    SET EASY=%ARTIFACTPREFIX%-easy.jar
    SET INTERFACES=%ARTIFACTPREFIX%-interfaces.jar
    SET BINS=%ARTIFACTPREFIX%-bins.jar
    
    REM deploy to central
    if EXIST %FOLDER%\%JAR% (
        echo "JAR %FOLDER%\%JAR%"
        call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%JAR%
        echo "TESTS %FOLDER%\%TESTS%"
        call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%TESTS% -Dclassifier=tests
        echo "SOURCES %FOLDER%\%SOURCES%"
        call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%SOURCES% -Dclassifier=sources
        if EXIST %FOLDER%\%TESTSOURCES% (
          echo "TEST-SOURCES %FOLDER%\%TESTSOURCES%"
          call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%TESTSOURCES% -Dclassifier=test-sources
        )
        if EXIST %FOLDER%\%JAVADOC% (
          echo "JAVADOC %FOLDER%\%JAVADOC%"
          call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%JAVADOC% -Dclassifier=javadoc
        )
        if EXIST %FOLDER%\%TESTJAVADOC% (
          echo "TEST-JAVADOC %FOLDER%\%TESTJAVADOC%"
          call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%TESTJAVADOC% -Dclassifier=test-javadoc
        )
        if EXIST %FOLDER%\%PYTHON% (
          echo "PYTHON %FOLDER%\%PYTHON%"
          call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%PYTHON% -Dclassifier=python
        )
        if EXIST %FOLDER%\%EASY% (
          echo "EASY %FOLDER%\%EASY%"
          call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%EASY% -Dclassifier=easy
        )
        if EXIST %FOLDER%\%INTERFACES% (
          echo "EASY %FOLDER%\%INTERFACES%"
          call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%INTERFACES% -Dclassifier=interfaces
        )
        if EXIST %FOLDER%\%BINS% (
          echo "EASY %FOLDER%\%BINS%"
          call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%BINS% -Dclassifier=bin
        )
    ) else (
        echo "POM %FOLDER%\%POM%"
        call %DEPLOYCMD% -DpomFile=%FOLDER%\%POM% -Dfile=%FOLDER%\%POM%
    )

    endlocal
    goto :eof
    
:end