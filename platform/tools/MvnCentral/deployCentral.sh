#!/usr/bin/bash

# Pre-requisites:
# 1. install wget into PATH
# 2. install curl into PATH
# 3. install GnuPGP, define default secret key (or change script)
# 4. add your account settings to settings.sh as USER=... and PASSWORD=...
# 5. copy this script as well as insert.txt into an empty directory
# 6. adjust release version number
# 7. run this script
# 8. goto https://central.sonatype.org and release the artifacts

source settings.sh
BEARER=$(printf "${USER}:${PASSWORD}" | base64)

# --- Variables ---
NAMESPACEPATH="de/iip-ecosphere/platform"
NAMESPACE="de.iip-ecosphere.platform"
LOCALREPO="http://projects.sse.uni-hildesheim.de/qm/maven/${NAMESPACEPATH}"
OKTO_VERSION="0.8.0" # does not work with snapshots as they have time stamp in names
BASEDIR="./tmp"
DIR="${BASEDIR}/${NAMESPACEPATH}"
TARGET="https://central.sonatype.com/api/v1/publisher"
REPO="central.deploy"
# Note: Maven command parts are kept separate for easier execution in Bash
SIGNCMD="gpg --armor --detach-sign --yes"
DEPLOYCMD="mvn -X deploy:deploy-file -Durl=$TARGET -DrepositoryId=$REPO"
PREFIX=""

SCRIPTDIR_ABS=$(realpath .)
BASEDIR_ABS=$(realpath ${BASEDIR})
# Create tmp directory
mkdir -p "$DIR"

# --- Function Definition ---
DownloadArtifact() {
   local URL="$1"
   local FILE="$2"

   wget ${URL} -O ${FILE}
   local exit_code=$?
   if [[ $exit_code -eq 0 ]]; then
	   if [[ "$URL" == *.pom ]]; then
		   #is it a POM
		   LINE=$(awk '/<license>/{print NR; exit}' ${FILE})
		   #does it have the license, author, SCM stuff included; if yes, parent pom and leave it, if no insert it -> central
		   if [[ -z "${LINE}" ]]; then
			   #if no license, include it before closing tag
			   echo "INSERTING insert.txt into ${FILE}"
			   LINE=$(awk '/<\/project>/{line=NR} END{print line}' ${FILE})
			   sed -i "${LINE}e cat ${SCRIPTDIR_ABS}/insert.txt" ${FILE}
		   fi
	   fi
	   $SIGNCMD ${FILE}
	   md5sum ${FILE} | awk '{print $1}' > ${FILE}.md5
	   sha1sum ${FILE} | awk '{print $1}' > ${FILE}.sha1
   else
       # wget touches always, even if failure
       rm ${FILE}
   fi
}

# --- Function Definition ---
# param1: prefix to be added to path ($1)
# param2: name/id of the artifact ($2)
# param3: version of the artifact to deploy ($3)
# param4: deploy only the POM ($4)
DeployArtifact() {
    local PREFIX="$1"
    local ARTIFACTNAME="$2"
    local ARTIFACTVERSION="$3"
    local MODE="$4"

    local ARTIFACTPREFIX="${ARTIFACTNAME}-${ARTIFACTVERSION}"
    local URL_PATH_PREFIX=""
    local LOCAL_FILE_PREFIX=""
    local ADIR="${DIR}"

    if [ -z "$PREFIX" ]; then
        URL_PATH_PREFIX="${ARTIFACTNAME}/"
        LOCAL_FILE_PREFIX=""
        ADIR="${ADIR}/${ARTIFACTNAME}"
    else
        URL_PATH_PREFIX="${PREFIX}.${ARTIFACTNAME}/"
        LOCAL_FILE_PREFIX=""
        ARTIFACTPREFIX="${PREFIX}.${ARTIFACTPREFIX}"
        ADIR="${ADIR}/${PREFIX}.${ARTIFACTNAME}"
    fi
    ADIR="${ADIR}/${ARTIFACTVERSION}"
    mkdir -p "${ADIR}"

    local POM="${ARTIFACTPREFIX}.pom"
    local FULL_URL_PREFIX="${LOCALREPO}/${URL_PATH_PREFIX}${ARTIFACTVERSION}"

    # Download relevant physical artifacts
    DownloadArtifact "${FULL_URL_PREFIX}/${POM}" "${ADIR}/${LOCAL_FILE_PREFIX}${POM}"

    if [[ "$MODE" == *"java"* || "$MODE" == *"mvn"* ]]; then
        local JAR="${ARTIFACTPREFIX}.jar"
        local SOURCES="${ARTIFACTPREFIX}-sources.jar"
        local JAVADOC="${ARTIFACTPREFIX}-javadoc.jar"
        local TESTS="${ARTIFACTPREFIX}-tests.jar"
        local TESTSOURCES="${ARTIFACTPREFIX}-test-sources.jar"
        local TESTJAVADOC="${ARTIFACTPREFIX}-test-javadoc.jar"
        DownloadArtifact "${FULL_URL_PREFIX}/${JAR}" "${ADIR}/${LOCAL_FILE_PREFIX}${JAR}"
        DownloadArtifact "${FULL_URL_PREFIX}/${SOURCES}" "${ADIR}/${LOCAL_FILE_PREFIX}${SOURCES}"
        DownloadArtifact "${FULL_URL_PREFIX}/${JAVADOC}" "${ADIR}/${LOCAL_FILE_PREFIX}${JAVADOC}"
        DownloadArtifact "${FULL_URL_PREFIX}/${TEST}" "${ADIR}/${LOCAL_FILE_PREFIX}${TESTS}"
        DownloadArtifact "${FULL_URL_PREFIX}/${TESTSOURCES}" "${ADIR}/${LOCAL_FILE_PREFIX}${TESTSOURCES}"
        DownloadArtifact "${FULL_URL_PREFIX}/${TESTJAVADOC}" "${ADIR}/${LOCAL_FILE_PREFIX}${TESTJAVADOC}"
    fi
    if [[ "$MODE" == *"plugin"* ]]; then
        local PLUGIN="${ARTIFACTPREFIX}-plugin.zip"
        local PLUGINTEST="${ARTIFACTPREFIX}-plugin-test.zip"
        DownloadArtifact "${FULL_URL_PREFIX}/${PLUGIN}" "${ADIR}/${LOCAL_FILE_PREFIX}${PLUGIN}"
        DownloadArtifact "${FULL_URL_PREFIX}/${PLUGINTEST}" "${ADIR}/${LOCAL_FILE_PREFIX}${PLUGINTEST}"
    fi
    if [[ "$MODE" == *"core"* ]]; then
        local CORE="${ARTIFACTPREFIX}-core.jar"
        DownloadArtifact "${FULL_URL_PREFIX}/${CORE}" "${ADIR}/${LOCAL_FILE_PREFIX}${CORE}"
    fi
    if [[ "$MODE" == *"bin"* ]]; then
        local BIN="${ARTIFACTPREFIX}-bin.zip"
        DownloadArtifact "${FULL_URL_PREFIX}/${BIN}" "${ADIR}/${LOCAL_FILE_PREFIX}${BIN}"
    fi
    if [[ "$MODE" == *"python"* ]]; then
        local PYTHON="${ARTIFACTPREFIX}-python.zip"
        DownloadArtifact "${FULL_URL_PREFIX}/${PYTHON}" "${ADIR}/${LOCAL_FILE_PREFIX}${PYTHON}"
    fi
    if [[ "$MODE" == *"easy"* ]]; then
        local EASY="${ARTIFACTPREFIX}-easy.zip"
        local EASYTEST="${ARTIFACTPREFIX}-easy-test.zip"
        local TEMPLATE="${ARTIFACTPREFIX}-template.zip"
        DownloadArtifact "${FULL_URL_PREFIX}/${EASY}" "${ADIR}/${LOCAL_FILE_PREFIX}${EASY}"
        DownloadArtifact "${FULL_URL_PREFIX}/${EASYTEST}" "${ADIR}/${LOCAL_FILE_PREFIX}${EASYTEST}"
        DownloadArtifact "${FULL_URL_PREFIX}/${TEMPLATE}" "${ADIR}/${LOCAL_FILE_PREFIX}${TEMPLATE}"
    fi
    if [[ "$MODE" == *"spring"* ]]; then
        local SPRING="${ARTIFACTPREFIX}-spring.zip"
        DownloadArtifact "${FULL_URL_PREFIX}/${SPRING}" "${ADIR}/${LOCAL_FILE_PREFIX}${SPRING}"
    fi
    if [[ "$MODE" == *"full"* ]]; then
        local FULL="${ARTIFACTPREFIX}-full.zip"
        DownloadArtifact "${FULL_URL_PREFIX}/${FULL}" "${ADIR}/${LOCAL_FILE_PREFIX}${FULL}"
    fi
    if [[ "$MODE" == *"artifacts"* ]]; then
        local ARTIFACTS="${ARTIFACTPREFIX}-artifacts.zip"
        DownloadArtifact "${FULL_URL_PREFIX}/${ARTIFACTS}" "${ADIR}/${LOCAL_FILE_PREFIX}${ARTIFACTS}"
    fi
}

# --- Artifact Deployment ---

# deploy the individual artifacts

DeployArtifact "${PREFIX}" "tools.lib" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "dependency-plugin" "$OKTO_VERSION" "mvn"
DeployArtifact "${PREFIX}" "invoker-plugin" "$OKTO_VERSION" "mvn"
DeployArtifact "${PREFIX}" "maven-python" "$OKTO_VERSION" "mvn"

DeployArtifact "${PREFIX}" "platformDependencies" "$OKTO_VERSION" "pomOnly"
DeployArtifact "${PREFIX}" "platformDependenciesBOM" "$OKTO_VERSION" "pomOnly"
DeployArtifact "${PREFIX}" "platformDependenciesSpring" "$OKTO_VERSION" "pomOnly"

DeployArtifact "${PREFIX}" "libs.ads" "$OKTO_VERSION" "java"

DeployArtifact "${PREFIX}" "support.boot" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "support" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "support.aas" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "support.log-slf4j-simple" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.commons-apache" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.yaml-snakeyaml" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.websocket-websocket" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.ssh-sshd" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.rest-spark" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.processInfo-oshi" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.metrics-micrometer" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.json-jackson" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.http-apache" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.bytecode-bytebuddy" "$OKTO_VERSION" "java plugin"

DeployArtifact "${PREFIX}" "support.dfltSysMetrics" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.metrics.plcNext" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.metrics.bitmotec" "$OKTO_VERSION" "java plugin"

DeployArtifact "${PREFIX}" "support.aas.basyx" "$OKTO_VERSION" "java plugin core"
DeployArtifact "${PREFIX}" "support.aas.basyx.server" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.aas.basyx1_0" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.aas.basyx1_5" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.aas.basyx2.common" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "support.aas.basyx2" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "support.aas.basyx2.server" "$OKTO_VERSION" "java plugin"

DeployArtifact "${PREFIX}" "support.iip-aas" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "support.semanticId.eclass" "$OKTO_VERSION" "java plugin"

DeployArtifact "${PREFIX}" "transport" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "transport.amqp" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "transport.mqttv3" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "transport.mqttv5" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "transport.spring" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "transport.spring.amqp" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "transport.spring.generic" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "transport.spring.hivemqv3" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "transport.spring.hivemqv5" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "transport.spring.mqttv3" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "transport.spring.mqttv5" "$OKTO_VERSION" "java"

DeployArtifact "${PREFIX}" "test.mqtt.hivemq" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "test.mqtt.moquette" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "test.amqp.qpid" "$OKTO_VERSION" "java plugin"

DeployArtifact "${PREFIX}" "connectors" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "connectors.opcuav1" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "connectors.modbustcpipv1" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "connectors.rest" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "connectors.mqttv3" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "connectors.mqttv5" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "connectors.mqtt" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "connectors.aas" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "connectors.ads" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "connectors.serial" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "connectors.file" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "connectors.influx" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "connectors.influxv3" "$OKTO_VERSION" "java plugin"

DeployArtifact "${PREFIX}" "services.environment" "$OKTO_VERSION" "java python"
DeployArtifact "${PREFIX}" "services.environment.spring" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "services.spring.loader" "$OKTO_VERSION" "java spring"
DeployArtifact "${PREFIX}" "services" "$OKTO_VERSION" "java"
#DeployArtifact "${PREFIX}" "test.simpleStream.spring" "$OKTO_VERSION" "java spring full"
DeployArtifact "${PREFIX}" "services.spring" "$OKTO_VERSION" "java plugin"

DeployArtifact "${PREFIX}" "kiServices.functions" "$OKTO_VERSION" "java python"
DeployArtifact "${PREFIX}" "kiServices.rapidminer.rtsa" "$OKTO_VERSION" "java plugin" 
DeployArtifact "${PREFIX}" "kiServices.rapidminer.rtsaFake" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "security.services.kodex" "$OKTO_VERSION" "java bin plugin" 

DeployArtifact "${PREFIX}" "ecsRuntime" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "ecsRuntime.docker" "$OKTO_VERSION" "java plugin"
#depends on jlxc-snapshots, deploy with EASY before (!)
DeployArtifact "${PREFIX}" "ecsRuntime.lxc" "$OKTO_VERSION" "java plugin"
#not part of release -> check whether all Mvn artifacts are created
#DeployArtifact "${PREFIX}" "ecsRuntime.kubernetes" "$OKTO_VERSION" "java plugin"

DeployArtifact "${PREFIX}" "deviceMgt" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "deviceMgt.basicRegistry" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "deviceMgt.minio" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "deviceMgt.s3mock" "$OKTO_VERSION" "java plugin"
DeployArtifact "${PREFIX}" "deviceMgt.thingsboard" "$OKTO_VERSION" "java plugin"

DeployArtifact "${PREFIX}" "monitoring" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "monitoring.prometheus" "$OKTO_VERSION" "java plugin"

DeployArtifact "${PREFIX}" "platform" "$OKTO_VERSION" "java"

DeployArtifact "${PREFIX}" "configuration.interface" "$OKTO_VERSION" "java"
DeployArtifact "${PREFIX}" "configuration.easy" "$OKTO_VERSION" "java plugin easy"
DeployArtifact "${PREFIX}" "apps.ServiceImpl" "$OKTO_VERSION" "java artifacts"
DeployArtifact "${PREFIX}" "configuration.configuration" "$OKTO_VERSION" "java easy"
DeployArtifact "${PREFIX}" "configuration-plugin" "$OKTO_VERSION" "mvn"
DeployArtifact "${PREFIX}" "configuration.defaultLib" "$OKTO_VERSION" "java"

DeployArtifact "${PREFIX}" "managementUi" "$OKTO_VERSION" "java bin"

#NAMESPACEPATH="de/oktoflow/platform"
#NAMESPACE="de.oktoflow.platform"
#DIR="${BASEDIR}/${NAMESPACEPATH}"
#LOCALREPO="http://projects.sse.uni-hildesheim.de/qm/maven/${NAMESPACEPATH}"
#DeployArtifact "${PREFIX}" "component" "$OKTO_VERSION" "java"

SCRIPT_DIR=$PWD
cd $BASEDIR
rm -f ${SCRIPT_DIR}/oktoflow.zip
zip -r ${SCRIPT_DIR}/oktoflow.zip .
cd $SCRIPT_DIR

curl --request POST --verbose --header "Authorization: Bearer ${BEARER}" --form bundle=@oktoflow.zip "https://central.sonatype.com/api/v1/publisher/upload?publishingType=USER_MANAGED&name=oktoflow"

echo -e "\n\nDeployment process finished."
