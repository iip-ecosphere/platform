# Connectors Component OPC UA extension in the Transport Layer of the IIP-Ecosphere platform

OPC UA v1 machine/platform for bi-directional access to machines and already installed platforms based on [Eclipse Milo](https://projects.eclipse.org/projects/iot.milo). We run this test without AAS factory installed in order to simplify 
the test. If required, additionally also an AAS server according to the ``AasPartRegistry`` must be initiated.

Qualified names for the generic model access start with the typical OPC UA top folders `Objects`, `Types`, 
`Views`, whereby `Objects` is used for accessing instances. Nested nodes down to variables or methods are separated by `/`.

**Missing**
- Authentication/Security (TLS is implemented/prepared but not tested)
- Unsure: further custom types apart from Structs
- Create a test against another server than Milo
