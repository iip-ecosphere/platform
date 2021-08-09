# Security And Privacy

This section discusses aspects related to the security and privacy mechanisms of the platform, which are documented in [^1].

The IIP ecosphere platform mandates a reliable authentication and authorization mechanism as well as role-based access management to control access to platform resources. In the following sections we will outline a strategy to achieve these requirements using open-source software components.

## Authentication, Authorization & Encryption

IIP Ecosphere strives to create a distributed, federated system of platform deployments that interlinks, among others, manufacturers, service providers and data consumers. To facilitate and secure the communication and exchange of data in such an ecosystem, secure and reliable mechanisms for authentication, authorization and encryption of data are needed. These mechanisms should provide the following functionality:

* Reliable identification and authentication of platform actors.
* Fine graned authorization for specific resources based on role-based access control mechanisms.
* End-to-end encryption of all data traffic between platform actors.
* Assuring connectivity between relevant actors independent.

 The [EPS open-source software](https://github.com/kiprotect/eps) was designed with these requirements and already provides most of the necessary components to secure the communication in the IIP ecosphere ecosystem. It provides mutual certificate-based authentication via TLS 1.3, role-based authorization and access management based on a federated service directory and bidirectional, protocol-agnostic communication channels. It was designed using privacy and security by design principles and is already used production. The EPS software can be extended to support specific data transport protocols such as AMQP or MQTT as well as specific API schemas or asset administration shells (AAS).

 ## Data Security & Protection

 In addition to authentication, authorization and encryption, sensitive or personal data also needs to be protected while in transit or at rest. Personal data needs to be processed in accordance with the GDPR while sensitive data may be regarded as a business secret and thereby will also need to be protected.

 The [Kodex open-source software](https://github.com/kiprotect/kodex) was designed to protect structed data using privacy- & security-enhancing transformations such as anonymization, pseudonymization or encryption. It can be easily adapted in order to protect sensitive and personal data within the IIP ecosphere system. Kodex supports reading data from a variety of input sources. It can perform stream or batch processing of data and can send the transformed data to a variety of output destinations. Parameters (e.g. encryption keys) can be centrally managed, transformation configurations can be specified using a structured configuration language which describes all necessary transformation on the input data. The configuration can be managed via the IIP ecosphere platform, e.g. based on the type of data being processed and/or the origin or destination of the data. For example, time-series data originating from an industrial manufacturing process and destined for a machine learning platform can be protected using structure- & format-preserving pseudonymization, which keeps the data usable to most ML methods but obfuscates the original data values, making it harder for an adversary to derive concrete information from the data. Similarly, statistical data originating from industrial machines and destined for benchmarking can be anonymized using a differentially private data aggregation, ensuring that e.g. a given company cannot learn anything about the efficiency or workload of a competitor that contributes data to the statistical analysis, while ensuring that all companies can e.g. obtain valid statistical data for benchmarking.

[^1]: https://www.iip-ecosphere.eu/wp-content/uploads/2021/03/IIP-2021_002.pdf

## Implementation

To integrate the EPS and Kodex software components with the IIP Ecosphere platform, the following requirements need to be met:

* Specification of the IIP Ecosphere platform API(s): In order to implement role-based access control mechanisms using the EPS system, the external API of the IIP Ecosphere platform needs to be specified. This then enables us to specify role-based permissions for specific actors.
* Specification of data transport 

## Modeling of Data Streams

In the platform, a specific data stream (e.g. sensor data from a machine) can be described using a descriptive data structure or modeling language. This data structure specifies the input format of the data stream and annotates individual data fields with structural & semantic information. This information can then be used to create a specific data transformation in Kodex that e.g. pseudonymizes or anonymizes the data, depending on the destination of the data.

### Example

The following file contains an example (pseudo-)configuration that illustrates how we could describe a given data stream. The configuration file can e.g. be read by an utility that then generates appropriate configuration for EPS and Kodex which enables the usage of the data in the ecosystem.

```yaml
name: Temperature milling machine
description: This time series data contains the temperature of a milling machine.
schema: # description of the data schema
	fields:
		- name: timestamp # the timestamp
		  validators:
		  	- type: IsTime
		  	  config:
		  	  	format: rfc3339
	    - name: temperature # the temperature field
	      validators:
	      	- type: IsFloat
	      	  config:
	      	  	hasMin: true
	      	  	min: -100
	      	  	hasMax: true
	      	  	max: 200
properties: # properties of the data
	type: stream # this is a continuous data stream
	personalData: false # this is not personal data
	sensitive: true # this is sensitive data
recipients: # who can receive this data
	- group: predictionService
	  transforms:
	  	- type: pseudonymize
    - group: timeSeriesDatabase
      transforms: []
channels: # the channels through which this data is available on the platform
	- type: amqp # data is provided via AMQP
	  config:
	  	address: {{address}} # template variable
```

Based on this configuration, Kodex can e.g. create a configuration that continuously reads data from an AMQP data source, transforms it using pseudonymization and other privacy-enhancing techniques and then forwards the transformed data to an external service for processing via the EPS system. The external service can then e.g. perform prediction on the data and return the predicted temperature, which can be fed to a time series database using Kodex.