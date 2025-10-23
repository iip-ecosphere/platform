# oktoflow platform: Configuration component

This part consists of all components that provide or modify the oktoflow platform configuration, either at before installation or during runtime. Currently, this component consists of 
- the [configuration plugin interface](configuration.interface/README.md).
- the [EASy-Producer configuration plugin](configuration.easy/README.md).
- the [configuration integration and the configuration meta model](configuration/README.md).
- the [configuration default library](configuration.defaultLib/README.md) of type or device-specific functionality for type or devices defined in the configuration meta-model.
- the [Maven plugin](configuration.maven/README.md) for embedding the IVML/VIL platform instantiator through the plugin interface and the default configuration plugin.

Further parts for dynamic deployment or adaptation are foreseen in the platform architecture.
