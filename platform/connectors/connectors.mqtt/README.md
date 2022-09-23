# Connectors Component generic MQTT extension in the Transport Layer of the IIP-Ecosphere platform

This component introduces a connector that dynamically selects which of the existing MQTT connectors to (re-)use and instantiate. This component implements a ConnectorFactory that reacts on the version of the device-provided service. For further information, please refer to the employed [MQTT v3](../connectors.mqttv3/README.md) or [MQTT v5](../connectors.mqttv5/README.md) connectors.

