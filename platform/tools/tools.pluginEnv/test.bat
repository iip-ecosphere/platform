@echo off
SET TEST=test.de.iip_ecosphere.platform.support.aas.basyx2.plugintest.AllTests
java -cp @target\standalone\cp -Dorg.springframework.boot.logging.LoggingSystem=none -Dokto.test.noPlugins=true -Dokto.test.aas.failRbacOp=false test.de.iip_ecosphere.platform.support.TestUtils %TEST% > log 2>&1