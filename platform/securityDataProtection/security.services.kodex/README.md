# IIP-Ecosphere platform: Generic Anonymziation/Pseudonymization Service

This component realizes the integration of [KIPROTECT kodex]() as (deployable) generic anonymization/pseudonymization service. The KODEX community edition is AGPL-3.0; as we use and do not modify the binary, there is no license issue if we use, loosely integrate and distribute the official binary.

The KODEX multi-type REST service requires 
  - as java resource a JSON file `kodexMapping.<serviceName>` linking symbolic type names (from the configuration model) to hexadecimal kodex stream ids  
  - the KODEX `data.yaml` file (to be packaged with the binary) indicating the API and the actions as well as stating the blueprints
  - the KODEX `api.yaml` file (to be packaged with the binary)
  - the KODEX `actions.yaml` file (to be packaged with the binary)

All files are specific to the use of the service and created by the code generation. `src/test/resources` contains static forms for testing.
