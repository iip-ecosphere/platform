# IIP-Ecosphere platform test artifacts: simpleStream.spring

A simple (controllable) stream to test the stream-based deployment for the spring cloud stream integration. The build process creates two artifacts, one including the MQTT broker for standalone execution and one without the broker. However, excluding dependencies with the Spring Maven plugin is tedious as dependencies are not excluded transitively. So far, the package without the broker is experimental. In real systems, the broker is supposed to be installed on the target system, so that this problem only so far occurs for testing artifacts.

Control happens via Spring command line parameters/application.yml, in particular

  * `test.debug` enable or disable test output (boolean, default `false`)
  * `test.ingestCount` maximum number of messages until the JVM is terminated (integer, default `100`)
  * `mqtt.port` TCP port of the MQTT v5 broker (integer, default `8883`)
  * `mqtt.host` host of the MQTT v5 broker (string, default `localhost`)

