# AAS Component AAS/BaSxy client extension of the Support Layer of the IIP-Ecosphere platform

Client-side Asset Administration Shell (AAS) abstraction based on [Eclipse BaSyx](https://www.eclipse.org/basyx/).  

- Currently with implementations for AAS, asset, sub-model, sub-model elements, properties and operations; abilities 
  and other concepts will be added incrementally on demand.
- Load/store abstractions for XML and AASX (for only one AAS)
- Deployment abstractions (local and remote deployment)
- Implementation-level abstractions (direct VAB, not ControlComponent-based) for TCP and HTTP
- AAS Visitor
- `shortID` validation for recommended/forbidden names. As in BaSyx, shortId must neither be ``value`` nor ``invocationList``. In addition, and more strict than BaSyx, we require that shortIds comply to the RegEx ``[a-zA-Z][a-zA-Z0-9_]+``.
- Supported identifier translation: ``urn:`` with URN syntax, ``urnText:`` arbitrary text to be used as raw URN, 
- Optional TLS for AAS can be setup through keystores. The AAS servers require a certificate with alias "tomcat", also for VAB.

This package is intended to represent in particular the client side of an AAS (which may imply a local server). For a compliant AAS server, i.e., for remote deployment of AAS, please refer to the [Basyx AAS server abstraction](../support.aas.basxy.server/README.md), which complements the
client side with server-sided functionality. We separated client and server side in particular to manage, reduce and optimizer the dependencies and resource usage on (Edge) client side.

May be, future versions need to switch to lazy loading of deployed AAS instances (supported now by submodel elements collection, but no submodel or AAS).

Please note that in more recent versions of BaSyx, the protocol schema of the AAS registry and the AAS server are assumed to be the same, e.g., http/http or https/https.

**Missing**
- Various AAS concepts (incremental addition as needed by the platform)
- Authentication/RBAC
- Events
- Improved resilience for failing VAB connections. Currently, failing connections are disabled for 1 minute after the first failure. Consider an integration with the network manager (port release).
