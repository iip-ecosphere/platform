# Connectors Component in the Transport Layer of the IIP-Ecosphere platform

The Connectors Component provides bi-directional access to machines and already installed platforms. Therefore, we 
define a connector interface that can work with model-based and payload-based protocols, whereby model-based access
is optional. An annotation declares the capabilities of a concrete connector implementation. As soon as data arrived
(event-based or alternatively polling-based), data is converted into a platform-internal data type as using and refining mechanisms defined in the Transport Component.

This component does not define concrete connectors. This happens in further extending components.

**Missing**
- Connectors AAS 
- Authentication/Security (basic connector parameters included but not tested)

