# IIP-Ecosphere platform: RapidMiner RTSA integration

Generic integration of the [RapidMiner](https://rapidminer.com) [Real-Time Scoring Agent (RTSA)](https://docs.rapidminer.com/latest/scoring-agent/install/) for AI components and processes.

So far, the integration is preliminary, i.e., separating mechanisms, ports and the API path are currently fixed. Due to IPR and license reasons, RTSA itself is not part of this integration and must be added (also in the automated  integration) as external packages. For the regression tests, we use a rather simple fake/mock implementation that sets in of RTSA is not present (extracted in src/main/resources/rtsa). Please note that the **original RTSA** requires **exactly JDK 8** while the **fake RTSA** runs with **JDK 8 and newer**.

RTSA usually ships in one ZIP for the agent (to be named RTSA-version.ZIP) and one or multiple deployments (one per service, named according to the service). The RTSA ZIP may contain the RTSA folders directly in the top-level directory or in one singular directory located in the top-level directory. If a special packaging is applied, i.e., RTSA and deployment are shipped together, e.g., for experimental purposes, use an empty RTSA ZIP and an all-encompassing deployment.ZIP including the RTSA.

For test packaging in the platform/application instantiation, the build process of this package creates two fake ZIP files in `target/fake`, one for RTSA and one for a deployment. The fake RTSA contains the respective class from testing, the deployment ZIP is intentionally more or less empty. For testing data flows, the Fake RTSA can be configured to react on incoming data. The deployment ZIP shall therefore contain a YAML file called `spec.yml` according to the following format:

    path: <String>
    mappings:
      <String>: <String>

The `path` indicates the desired REST path/endpoint attached to the base path services. The mappings relate a field name in the input data to a function specification. As function specification, we currently offer 
- PASS: just pass the value on
- SKIP: do not emit the value to output, filter
- RANDOM_BOOLEAN: the value of the field shall be a random boolean
- RANDOM_PROBABILITY: the value of the field shall be a random double in [0;1]. 
- RANDOM_SELECT(args): the value shall be a random selection from args, whereby args are separated by commas. Strings may be given in usual quotes.
Fields not given in the data but specified in `spec.yml` will be added to the output. 

Building and testing in an IDE requires execution of Maven as neither classpath file nor jar folder for fake RTSA may exist.
Manual building may require `mvn package -DskipTests -Djacoco.skip=true`
