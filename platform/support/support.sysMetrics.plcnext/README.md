# IIP-Ecosphere platform: System metrics implementation for Phoenix Contact / PLCnext

Uses [PLCnext internal gRPC](https://www.plcnext.help/te/Service_Components/gRPC_Introduction.htm) to provide default measures. Seems to require AXC firmware 2022.0 LTS and newer.

If you start this system metrics implementation in a container, e.g. for Docker specify `-v /run/plcnext/:/run/plcnext/`. `/run/plcnext/grpc.sock` must be accessible for reading/writing. To allow for flexible integrations, the metrics plugin takes the environment setting `iip.devices.plcNext.grpc.sock` into account, which by default points to ' /run/plcnextThe metrics implementation tries to set the permissions accordingly.

