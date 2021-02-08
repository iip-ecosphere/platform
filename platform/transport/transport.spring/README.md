# Transport layer of the IIP-Ecosphere platform: Streaming basics

Basic classes for soft-realtime manner based on [Spring cloud stream](https://spring.io/projects/spring-cloud-stream).
This component is not part of the basic transport component, as we consider Spring cloud stream as one potential
technology to combine streams. However, as for the protocols and the wire serialization, we aim at keeping the platform
open on the streaming layer. Therefore, specific services shall not be directly based on Spring cloud stream rather than
bound by generated glue code to the respective streaming approach. 

This component is directly based on the transport components. This component provides basic classes, e.g., type 
conversion based on the serialization mechanism defined in the transport component. Protocol-specific binders are
defined in further projects named `transport.streaming.<protocol>`. For uniform usage and a future transport 
AAS Protocol-specific binders shall exhibit the (default) server connection information as TransportParameter bean.

## Spring hints:

- Enable debugging: `logging.level.org.springframework.cloud=DEBUG` and/or `logging.level.org.springframework.integration=DEBUG` 