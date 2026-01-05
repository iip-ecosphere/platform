# oktoflow platform: Plugin dictionary

This document collects informations about all plugins shipped with the oktoflow platform.

Plugins are installed through the build process and in code addressed by their implementing types or their (symbolic) identifiers. At least the primary (symbolic) id must be given, while further optional (secondary) ids may be used in particular for migration purposes, e.g., from qualified Java class names over time to primary ids.

Plugin ids formatted in bold are the default plugins for the respective task. Pluing ids marked with * indicate multi-plugins, i.e., where not a single rather than all known plugins of a certain kind will be taken into account.

## Libraries (support)

Basic library integrations in the support layer. Typically, plugins are not addressed by their id rather than their implementing interface type, i.e., depending on the use of these plugins, one of all must be present.

| primary id | secondary Ids | Description | implementing Interface | Path |
| --- |  --- | --- | --- |--- |
| bytecode-bytebuddy | | Java bytecode manipulation using bytebuddy | `de.iip_ecosphere.platform.support.bytecode.Bytecode` | [support.bytecode-bytebuddy](../support/support.bytecode-bytebuddy) |
| commons-apache | | Java basic support functions for files, system, IO using apache commons | `de.iip_ecosphere.platform.support.commons.Commons` | [support.commons-apache](../support/support.commons-apache) |
| http-apache | | Java HTTP client using apache | `de.iip_ecosphere.platform.support.http.Http` | [support.http-apache](../support/support.http-apache) |
| json-jackson | | Java JSON support using jackson, jsoniter and javax.json | `de.iip_ecosphere.platform.support.json.Json` | [support.json-jackson](../support/support.json-jackson) |
| log-slf4j-simple | | Java logging support using slf4j and it's simple plugin | `de.iip_ecosphere.platform.support.logging.ILoggerFactory` | [support.log-slf4j-simple](../support/log-slf4j-simple) |
| processInfo-OSHI | | Java OS process access using OSHI | `de.iip_ecosphere.platform.support.processInfo.ProcessInfoFactory` | [support.processInfo-OSHI](../support/processInfo-OSHI) |
| rest-spark| | Java HTTP/REST serving support access using sparkjava and glassfish/jersey | `de.iip_ecosphere.platform.support.rest.Rest` | [support.rest-spark](../support/rest-spark) |
| ssh-sshd| | Java SSH client/server support access using sshd | `de.iip_ecosphere.platform.support.ssh.Ssh` | [support.ssh-sshd](../support/ssh-sshd) |
| metrics-micrometer | | Java metric probe support using micrometer| `de.iip_ecosphere.platform.support.metrics.MetricsFactory` | [support.metrics-micrometer](../support/metrics-micrometer) |
| websocket-websocket | | Java websocket support using the websocket library | `de.iip_ecosphere.platform.support.websocket.WebsocketFactory` | [support.websocket](../support/websocket) |
| yaml-snakeyaml | **yaml** | Java YAML support using jackson, jsoniter | `de.iip_ecosphere.platform.support.yaml.Yaml` | [support.yaml-snakeyaml](../support/support.yaml-snakeyaml) |

## Capabilities (support)

Multiple semanticId resolvers may be present while only one `systemMetrics` plugin shall be present.

| primary id | secondary Ids | Description | implementing Interface | Path |
| --- |  --- | --- | --- | --- |
|semanticId-eclass*| | ECLASS semantic id resolution (in addition to the oktoflow internal semanticId catalogues) | `de.iip_ecosphere.platform.support.semanticId.SemanticIdResolver` | [support.semanticId.eclass](../support/support.semanticId.eclass) |
|**systemMetrics**| | Alternative system metrics plugin for [Bitmoteco](https://www.bitmotec.com/) | `de.iip_ecosphere.platform.support.metrics.SystemMetrics` | [support.sysMetrics.bitmotec](../support/support.sysMetrics.bitmotec) |
|systemMetrics| | Alternative system metrics plugin for [PLCNext](https://www.phoenixcontact.com/de-de/produkte/plcnext-technology) | `de.iip_ecosphere.platform.support.metrics.SystemMetrics` | [support.sysMetrics.plcnext](../support/support.sysMetrics.plcnext) |

## AAS

Although alternatively used by the platform core (`aas-default`), multiple AAS plugins may be present and utilized on demand.

| primary id | secondary Ids | Description | implementing Interface | Path |
| --- |  --- | --- | --- | --- |
| **aas-default** | aas-basyx-1.3 | AAS implementation via BaSyX 1.3 | `de.iip_ecosphere.platform.support.aas.AasFactory` | [support.aas.basyx](../support/support.aas.basyx) |
| aas.basyx-server | aas.basyx-server-1.3 | Server dependencies for BaSyX 1.3 | `de.iip_ecosphere.platform.support.aas.AasServerRecipeDescriptor` | [support.aas.basyx.server](../support/support.aas.basyx.server) |
| aas-basyx-1.0 | | AAS implementation via BaSyX 1.0 | `de.iip_ecosphere.platform.support.aas.AasFactory` | [support.aas.basyx1_0](../support/support.aas.basyx1_0) |
| aas-basyx-1.5 | | AAS implementation via BaSyX 1.5 | `de.iip_ecosphere.platform.support.aas.AasFactory` | [support.aas.basyx1_5](../support/support.aas.basyx1_5) |
| aas-basyx-2.0 | | AAS implementation via BaSyX 2.0 | `de.iip_ecosphere.platform.support.aas.AasFactory` | [support.aas.basyx2](../support/support.aas.basyx2) |

## Connectors

| primary id | secondary Ids | Description | implementing Interface | Path |
| --- |  --- | --- | --- | --- |
| **connector-AAS** | `de.iip_ecosphere.platform.connectors.aas.AasConnector` | AAS connector using the default or a given AAS plugin | `de.iip_ecosphere.platform.connectors.Connector` | [connectors.aas](../connectors/connectors.aas) |
| **connector-ADS** | `de.iip_ecosphere.platform.connectors.ads.AdsConnector` | Beckhoff ADS connector  | `de.iip_ecosphere.platform.connectors.Connector` | [connectors.ads](../connectors/connectors.ads) |
| **connector-file** | `de.oktoflow.platform.connectors.file.FileConnector` | File connector with orthogonal file formats like CSV, JSON | `de.iip_ecosphere.platform.connectors.Connector` | [connectors.file](../connectors/connectors.file) |
| **connector-influx** | connector-influx-v2, `de.iip_ecosphere.platform.connectors.influx.InfluxConnector` | File connector with orthogonal file formats like CSV, JSON | `de.iip_ecosphere.platform.connectors.Connector` | [connectors.influx](../connectors/connectors.influx) |
| **connector-modbusTcp** | `de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusTcpIpConnector` | Modbus/TCP connector | `de.iip_ecosphere.platform.connectors.Connector` | [connectors.modbustcpipv1](../connectors/connectors.modbustcpipv1) |
| connector-mqtt-v3 | `de.iip_ecosphere.platform.connectors.mqttv3.PahoMqttv3Connector` | MQTT v3 connector | `de.iip_ecosphere.platform.connectors.Connector` | [connectors.mqttv3](../connectors/connectors.mqttv3) |
| connector-mqtt-v5 | `de.iip_ecosphere.platform.connectors.mqttv5.PahoMqttv5Connector` | MQTT v5 connector | `de.iip_ecosphere.platform.connectors.Connector` | [connectors.mqttv5](../connectors/connectors.mqttv5) |
| **connector-mqtt** | `de.iip_ecosphere.platform.connectors.mqtt.MqttConnectorFactory` | MQTT connector dynamically selecting between MQTT v3 and MQTT v5 based on device information using either connector-mqtt-v3 or connector-mqtt-v5 plugin| `de.iip_ecosphere.platform.connectors.Connector` | [connectors.mqtt](../connectors/connectors.mqtt) |
| **connector-opcua** | connector-opcua-v1, `de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector` | OPC UA connector| `de.iip_ecosphere.platform.connectors.Connector` | [connectors.opcuav1](../connectors/connectors.opcuav1) |
| **connector-rest** | `de.iip_ecosphere.platform.connectors.rest.RESTConnector` | REST connector| `de.iip_ecosphere.platform.connectors.Connector` | [connectors.rest](../connectors/connectors.rest) |
| **connector-serial** | `de.oktoflow.platform.connectors.serial.JSerialCommConnector` | Serial connector| `de.iip_ecosphere.platform.connectors.Connector` | [connectors.serial](../connectors/connectors.serial) |

## Generic services

| primary id | secondary Ids | Description | implementing Interface | Path |
| --- |  --- | --- | --- |--- |
| **service-rtsa** |  `de.iip_ecosphere.platform.kiServices.rapidminer.rtsa.MultiRtsaRestService` | Multi-type REST-based RTSA service | [kiServices.rapidminer.rtsa](../reusableIntelligentServices/kiServices.rapidminer.rtsa) |
| service-rtsa-single |  `de.iip_ecosphere.platform.kiServices.rapidminer.rtsa.RtsaRestService` | Single-type REST-based RTSA service (legacy) | [kiServices.rapidminer.rtsa](../reusableIntelligentServices/kiServices.rapidminer.rtsa) |
| **service-kodex** |  `de.iip_ecosphere.platform.kiServices.rapidminer.rtsa.MultiKodexRestService` | Multi-type REST-based RTSA service | [security.services.kodex](../securityDataProtection/security.services.kodex) |
| service-kodex-single |  `de.iip_ecosphere.platform.security.services.kodex.KodexRestService` | Single-type REST-based RTSA service (legacy) | [security.services.kodex](../securityDataProtection/security.services.kodex) |
| service-kodex-single-cmd |  `de.iip_ecosphere.platform.security.services.kodex.KodexService` | Single-type command line stream-based RTSA service (legacy) | [security.services.kodex](../securityDataProtection/security.services.kodex) |

## Service management

Service management plugins are alternative, i.e., only one shall be present/loaded.

| primary id | secondary Ids | Description | implementing Interface | Path |
| --- |  --- | --- | --- | --- |
| **services** | services-spring | Service management and execution with Spring Cloud Stream | `de.iip_ecosphere.platform.services.ServiceFactoryDescriptor` | [services.spring](../services/services.spring) |

## Device management

Device management plugins are alternative, i.e., only one shall be present/loaded.

| primary id | secondary Ids | Description | implementing Interface | Path |
| --- |  --- | --- | --- | --- |
| **deviceMgt** | deviceMgt-basic | Basic in-memory device management | `de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistry` |  [deviceMgt.basicRegistry](../resources/deviceMgt.basicRegistry) |
| deviceMgt | deviceMgt-thingsboard | Device management using thingsboard | `de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistry` |  [deviceMgt.thingsboard](../resources/deviceMgt.thingsboard) |
| **deviceMgtStorage** | deviceMgtStorage-minio | Device management storage using minio | `de.iip_ecosphere.platform.deviceMgt.registry.StorageFactoryDescriptor` |  [deviceMgt.minio](../resources/deviceMgt.minio) |
| deviceMgtStorage | deviceMgtStorage-s3mock | Device management storage using S3 | `de.iip_ecosphere.platform.deviceMgt.registry.StorageFactoryDescriptor` |  [deviceMgt.s3mock](../resources/deviceMgt.s3mock) |

## ECS runtime

ECS runtime plugins are alternative, i.e., only one shall be present/loaded.

| primary id | secondary Ids | Description | implementing Interface | Path |
| --- |  --- | --- | --- | --- |
| **container** | container-docker | Container management plugin for Docker | `de.iip_ecosphere.platform.ecsRuntime.EcsFactoryDescriptor` | [ecsRuntime.docker](../resources/ecsRuntime.docker) |
| container | container-LCX | Container management plugin for LXC | `de.iip_ecosphere.platform.ecsRuntime.EcsFactoryDescriptor` | [ecsRuntime.lxc](../resources/ecsRuntime.lxc) |
| container | container-K8S | Container management plugin for kubernetes | `de.iip_ecosphere.platform.ecsRuntime.EcsFactoryDescriptor` | [ecsRuntime.kubernetes](../resources/ecsRuntime.kubernetes) |

## Monitoring

Monitoring plugins are alternative, i.e., only one shall be present/loaded.

| primary id | secondary Ids | Description | implementing Interface | Path |
| --- |  --- | --- | --- | --- |
| **monitoring** | monitoring-prometheus | Central monitoring service using [Prometheus](https://prometheus.io/) | `de.iip_ecosphere.platform.monitoring.MonitoringDescriptor` | [monitoring.prometheus](../resources/monitoring.prometheus) |

## Configuration

Configuration plugins are alternative, i.e., only one shall be present/loaded.

| primary id | secondary Ids | Description | implementing Interface | Path |
| --- |  --- | --- | --- | --- |
| **configuration** | configuration-easy | Configuration technology and code generation by EASy-Producer (IVML, VIL, VTL) | `de.iip_ecosphere.platform.configuration.cfg.ConfigurationFactoryDescriptor` | [configuration.easy](../configuration/configuration.easy) |
