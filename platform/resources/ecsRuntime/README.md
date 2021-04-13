# ECS runtime of the IIP-Ecosphere platform

Interfaces (SPI) and basic implementation of container and service management agent running on compute resources.

Contains a startup program for running the ECS runtime. The setup requires service management implementation and 
AAS implementation to be hooked in properly via JSL. Depending on the setup, also requires a proper setup of the 
network manager and a service manager. Intended to be called from a separate project with adequate dependencies.

## Missing
* ECS Monitoring