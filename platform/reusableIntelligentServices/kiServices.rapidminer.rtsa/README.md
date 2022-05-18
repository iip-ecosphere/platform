# IIP-Ecosphere platform: RapidMiner RTSA integration

Generic integration of the [RapidMiner](https://rapidminer.com) [Real-Time Scoring Agent (RTSA)](https://docs.rapidminer.com/latest/scoring-agent/install/) for AI components and processes.

So far, the integration is preliminary, i.e., separating mechanisms, ports and the API path are currently fixed. Due to IPR and license reasons, RTSA itself is not part of this integration and must be added (also in the automated  integration) as external packages. For the regression tests, we use a rather simple fake/mock implementation that sets in of RTSA is not present (extracted in src/main/resources/rtsa). Please note that the **original RTSA** requires **exactly JDK 8** while the **fake RTSA** runs with **JDK 8 and newer**.

For test packaging in the platform/application instantiation, the build process of this package creates two fake ZIP files in `target/fake`, one for RTSA and one for a deployment. The fake RTSA contains the respective class from testing, the deployment ZIP is intentionally more or less empty. For testing data flows, the Fake RTSA can be configured to react on incoming data. The deployment ZIP shall therefore contain a YAML file called `spec.yml` according to the following format:

    path: <String>
    mappings:
      <String>: <String>

The `path` indicates the desired REST path/endpoint attached to the base path services. The mappings relate a field name in the input data to a function specification. As function specification, we currently offer PASS, SKIP, RANDOM_BOOLEAN and RANDOM_PROBABILITY. Fields not given in the data but specified in `spec.yml` will be added to the output. 

Manual building may require `mvn package -DskipTests -Djacoco.skip=true`


