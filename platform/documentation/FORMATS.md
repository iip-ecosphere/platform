# IIP-Ecosphere platform: Data formats

This file is supposed to provide a catalogue of various fixed data formats, e.g., setup, descriptors, that the IIP-Ecosphere platform is employing. While the individual formats shall be discussed in/through the README.md of the individual components, this catalog shall provide an easy access through listing the names of the respective formats and giving a hyperlink to the documentation.

**The catalogue is being setup and currently not meant to be complete. Please feel free to add missing entries.**

## Descriptors

- Container deployment descriptor [Docker](../resources/ecsRuntime.docker/README.md) and (in preparation) [LXC](../resources/ecsRuntime.lxc/README.md)
- Service deployment descriptor for [Spring](../services/services.spring/README.md)

## Setup files

- [Management UI](../managementUI/README.md)
- [Central platform services](../platform/README.md) with [configuration](../configuration/configuration/README.md) and possibly [monitoring](../resources/monitoring.prometheus/README.md)
- Monitoring with [Prometheus](../resources/monitoring.prometheus/README.md)
- ECS runtime for [Docker](../resources/ecsRuntime.docker/README.md) and (in preparation) [LXC](../resources/ecsRuntime.lxc/README.md)
- Service manager for [Spring](../services/services.spring/README.md)
- Device/container software installation paths, aka [InstalledDependencies](../services/services.environment/README.md)
- Local [YAML-based Identity store](../support/support.aas/README.md)
- Distributed testing and evaluation environment

## Semantic Id resolution catalogues

- Local [YAML-based Semantic Id catalogue format](../support/support.iip-aas/README.md)