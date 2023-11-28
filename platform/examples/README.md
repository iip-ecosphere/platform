# oktoflow platform: Examples and demo use cases

To demonstrate and validate the oktoflow platform, we collect in this folder a set of examples and demo use cases.

* Assembling and executing platform containers on [PHOENIX CONTACT](https://www.phoenixcontact.com) AXC 3152 successful. Trying to improve container sizes. Specific code may follow when internal MQTT/OPC UA sources are connected.
* [VDW OPC-UA](examples/examples.vdw/README.md): Utilizing the [VDW](https://vdw.de/) OPC-UA server through the respective platform connector.
* [RapidMiner RTSA example](examples/examples.rtsa/README.md): Automated integration of RapidMiner Real Time Scoring Agent (RTSA) AI environment into a simple service chain/application. Please note - the RTSA is a commercial product and not included in this example rather than a simple fake of RTSA.
* [Python service example](examples.python/README.md): Integrating an asynchronous Python (pseudo AI) service into a simple service chain/application.
* [Python service example](examples.pythonSync/README.md): Integrating a synchronous Python (pseudo AI) service into a simple service chain/application.
* [KODEX service example](examples.python/README.md): Integrating the platform-provided KODEX pseudonymizer/anonymizer into a simple service chain/application.
* [MIP sensor connector/AI example](examples.MIP/README.md): Using the MIP specific MQTT connector from the configuration meta-model playing a one-step ping pong with a related platform AI service for improving the id detection. Just an example, cannot be executed with a sensor from MIP technologies.
* [HM'22/TddT'22 demonstrator](examples.python/README.md): The source code of the HM'22/TddT'22 demonstrator as an example for a more complex AI-enabled application.

The [examples](examples/README.md) component contains helpful reusable basic functionality for examples and apps.

More to come soon.