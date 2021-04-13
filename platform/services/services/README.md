# Service and service management interfaces of the IIP-Ecosphere platform

The basic service and service management interfaces of the IIP-Ecosphere platform. Also provides the AAS of the 
services, including

* The artifacts containing the services.
* The individual services and their typed connectors (after artifacts are added and before they are removed) including 
  the dynamic service state.
* The relations among the services while services are alive.
* Contributions to the device resources in terms of operations to manage services, e.g., start/stop services.

Contains a startup program for running the service manager in standalone mode, e.g., in a services container. The
setup Requires service management implementation and AAS implementation to be hooked in properly via JSL. Depending on 
the setup, also requires a proper setup of the network manager via JSL. Intended to be called from a separate project 
with adequate dependencies.