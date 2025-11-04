# Connectors Component REST extension in the Transport Layer of the oktoflow platform

Generic REST machine connector for bi-directional access to devices and machines and already installed platforms. 
Using services shall utilize the ConnectorExtensionDescriptor to declare the (generated) Spring response classes 
the connector shall work with.

We run the tests without AAS factory installed in order to simplify the test. If required, additionally also an AAS server according to the ``AasPartRegistry`` must be initiated.

This plugin utilizes local versions of Spring and FasterXML/Jackson. 
