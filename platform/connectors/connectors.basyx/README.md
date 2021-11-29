# Connectors Component AAS extension in the Transport Layer of the IIP-Ecosphere platform

Asset Administration Shell (AAS) providing bi-directional access to machines and already installed platforms based on the IIP-Ecosphere AAS abstraction.  

Specific interpretation of settings: This connector supports TLS. However, due to the underlying default implementation BaSyx, which assumes registries to be in plaintext, we currently ignore the key store settings. If the backend AAS server is TLS encrypted, please prefix the endpoint path with the schema, e.g., instead of `registry` for a plaintext backend AAS server, use `https:registry`.

**Missing**
- Authentication
- Event-based reception (events unclear in BaSyx)