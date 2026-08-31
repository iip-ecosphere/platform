#!/bin/bash
TEST=test.de.iip_ecosphere.platform.support.aas.basyx2.plugintest.AllTests
TIMEOUT=120000
java -cp @target/standalone/cp -Dorg.springframework.boot.logging.LoggingSystem=none -Dokto.test.noPlugins=true -Dokto.test.aas.failRbacOp=false -Dokto.test.aas.failAvailable=false -Dokto.test.timeout=${TIMEOUT} test.de.iip_ecosphere.platform.support.TestUtils ${TEST} > log 2>&1