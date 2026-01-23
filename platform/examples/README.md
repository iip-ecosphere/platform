# oktoflow platform: Examples and demo use cases

To demonstrate and validate the oktoflow platform, we collect in this folder a set of examples and demo use cases.

* Assembling and executing platform containers on [PHOENIX CONTACT](https://www.phoenixcontact.com) AXC 3152 successful. Trying to improve container sizes. Specific code may follow when internal MQTT/OPC UA sources are connected.
* [VDW OPC-UA](examples/examples.vdw/README.md): Utilizing the [VDW](https://vdw.de/) OPC-UA server through the respective platform connector.
* [RapidMiner RTSA example](examples/examples.rtsa/README.md): Automated integration of RapidMiner Real Time Scoring Agent (RTSA) AI environment into a simple service chain/application. Please note - the RTSA is a commercial product and not included in this example rather than a simple fake of RTSA.
* [Python service example](examples.python/README.md): Integrating an asynchronous Python (pseudo AI) service into a simple service chain/application.
* [Python service example](examples.pythonSync/README.md): Integrating a synchronous Python (pseudo AI) service into a simple service chain/application.
* [KODEX service example](examples.python/README.md): Integrating the platform-provided KODEX pseudonymizer/anonymizer into a simple service chain/application.
* [MIP sensor connector/AI example](examples.MIP/README.md): Using the MIP specific MQTT connector from the configuration meta-model playing a one-step ping pong with a related platform AI service for improving the id detection. Just an example, cannot be executed with a sensor from MIP technologies.
* [Tutorial example](examples.templates/README.md): The source code of the tutorial examples with a model and an implementation project.
* [HM'22/TddT'22 demonstrator](https://github.com/iip-ecosphere/examples/tree/main/examples.hm22/README.md): The source code of the HM'22/TddT'22 demonstrator as an example for a more complex AI-enabled application.
* [EMO'23/HM'23 demonstrator](https://github.com/iip-ecosphere/examples/tree/main/examples.emo23/README.md): The source code of the EMO'23/HM'23 demonstrator as an example for a more complex AI-enabled application with three AI services.
* [MODBUS/TCP example](examples.modbusTcp/README.md): Integrating a MODBUS/TCP connector into a simple service mesh.
* [REST example](examples.rest/README.md): Integrating a REST connector into a simple service mesh.
* [Python venv example](examples.pythonCondaVenv/README.md): Demonstrating the use of Conda/venv for Python services.

For a more detailed summary, see [../documentation/examples/examples.md].
