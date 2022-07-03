# IIP-Ecosphere platform: Default system metrics implementation

Uses [JSensors](https://github.com/profesorfalken/jSensors) to provide default measures. Specific Edge devices may 
require different libraries/integrations. CPU temperatures require execution as admin. This metrics implementation
is intentionally not marked as enabled among multiple implementations, i.e., if it is present with other metric
implementations, the first enabled device-specific one will be the active one. If there are multiple implementations 
but none is enabled, this metric implementation is marked to act as fallback.
