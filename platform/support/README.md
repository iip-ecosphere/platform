# IIP-Ecosphere support layer

Basic functionality for the IIP-Ecosphere in terms of:
  * Basic [support functions](https://github.com/iip-ecosphere/platform/tree/main/platform/support/support/README.md)
  * Asset Administration Shell (AAS) [abstraction](https://github.com/iip-ecosphere/platform/tree/main/platform/support/support.aas/README.md) with visitor, communication protocol support and useful recipes for deployment and I/O
  * [Default AAS client abstraction implementation](https://github.com/iip-ecosphere/platform/tree/main/platform/support/support.aas.basyx/README.md) for [Eclipse Basyx](https://www.eclipse.org/basyx/)
  * [Default AAS server abstraction implementation](https://github.com/iip-ecosphere/platform/tree/main/platform/support/support.aas.basyx.server/README.md) for [Eclipse Basyx](https://www.eclipse.org/basyx/)
  * Additional [AAS support functionality for the IIP-Ecosphere platform](https://github.com/iip-ecosphere/platform/tree/main/platform/support/support.iip-aas/README.md)
  * Simple default [system metrics implementation](https://github.com/iip-ecosphere/platform/tree/main/platform/support/support.dfltSysMetrics/README.md) refining the system monitoring interface in `support.aas`.
  * [System metrics for Phoenix Contact/PLCnext](https://github.com/iip-ecosphere/platform/tree/main/platform/support/support.sysMetrics.plcnext/README.md) refining the system monitoring interface in `support.aas`.
  * [Semantic ID resolution based on the ECLASS webservice](https://github.com/iip-ecosphere/platform/tree/main/platform/support/support.semanticId.eclass/README.md) refining the semantic id resolution interface in `support.aas`.