# AAS Component AAS/BaSxy extension of the Support Layer of the oktoflow platform

Asset Administration Shell (AAS) abstraction based on [Eclipse BaSyx v2](https://www.eclipse.org/basyx/).  

- Not necessarily complete, AAS concepts will be added incrementally on demand.
- Load/store abstractions for XML, JSON and AASX
- Deployment abstractions (local and remote deployment)
- Implementation-level abstractions (direct VAB, not ControlComponent-based) for TCP and HTTP
- AAS Visitor
- Supported identifier translation: ``urn:`` with URN syntax, ``urnText:`` arbitrary text to be used as raw URN, 
- Optional TLS for AAS can be setup through keystores. The AAS servers require a certificate with alias "tomcat", also for asset implementations.

This package is mainly intended to represent in particular the client side of an AAS (which may imply a local server) as well as in-memory sever instances.

**Missing**
- Various AAS concepts (incremental addition as needed by the platform)
- Events
