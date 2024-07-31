# oktoflow platform: Support Layer

Basic functionality for the oktoflow platform in terms of:
  * Basic [support functions](support/README.md)
  * Asset Administration Shell (AAS) [abstraction](support.aas/README.md) with visitor, communication protocol support and useful recipes for deployment and I/O
  * [Default AAS client abstraction implementation](support.aas.basyx/README.md) for [Eclipse Basyx](https://www.eclipse.org/basyx/)
  * [Default AAS server abstraction implementation](support.aas.basyx.server/README.md) for [Eclipse Basyx](https://www.eclipse.org/basyx/)
  * [AAS client plugin](support.aas.basyx1_0/README.md) for [Eclipse Basyx 1.0.1](https://www.eclipse.org/basyx/), retrofitted alternative version as "extension" of [support.aas.basyx](support.aas.basyx/README.md)
  * [AAS client plugin](support.aas.basyx1_5/README.md) for [Eclipse Basyx 1.5.1](https://www.eclipse.org/basyx/), retrofitted alternative version as "extension" of [support.aas.basyx](support.aas.basyx/README.md)
  * Additional [AAS support functionality for the oktoflow platform](support.iip-aas/README.md)
  * Simple default [system metrics implementation](support.dfltSysMetrics/README.md) refining the system monitoring interface in `support.aas`.
  * [System metrics for Phoenix Contact/PLCnext](support.sysMetrics.plcnext/README.md) refining the system monitoring interface in `support.aas`.
  * [System metrics for Bitmotec](support.sysMetrics.bitmotec/README.md) refining the system monitoring interface in `support.aas`.
  * [Semantic ID resolution based on the ECLASS webservice](support.semanticId.eclass/README.md) refining the semantic id resolution interface in `support.aas`.
  
Also basic (proprietary) libraries belong to the support layer:  
  * [Beckhoff TwinCat ADS integration library](support/libs.ads/README.md).