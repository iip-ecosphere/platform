# IIP-Ecosphere platform: RapidMiner RTSA fake

Fake version of the [RapidMiner](https://rapidminer.com) [Real-Time Scoring Agent (RTSA)](https://docs.rapidminer.com/latest/scoring-agent/install/) for testing. Please note that the **original RTSA** requires **exactly JDK 8** while the **fake RTSA** emulates RTSA and thus must be JDK compliant but runs with **JDK 8 and newer**. Anway, no dependencies that require a more modern JDK than 8 are allowed here, i.e., also no dependencies to the IIP-Ecosphere platform.

For test packaging in the platform/application instantiation, the build process of this package creates two fake ZIP files in `target/fake`, one for RTSA and one for a deployment. The fake RTSA contains the respective class from testing, the deployment ZIP is intentionally more or less empty. For testing data flows, the Fake RTSA can be configured to react on incoming data. The deployment ZIP contains a JAR file in the folder `home/deployments`, which usually consists of the implementation of the deployment. In the case of the fake RTSA it contains a YAML file called `spec.yml` with the following format:

    path: <String>
    mappings:
      <String>: <String>

The `path` indicates the desired REST path/endpoint attached to the base path services. The mappings relate a field name in the input data to a function specification. As function specification, we currently offer 
- `PASS`: just pass the value on
- `SKIP`: do not emit the value to output, filter
- `RANDOM_BOOLEAN`: the value of the field shall be a random boolean
- `RANDOM_PROBABILITY`: the value of the field shall be a random double in [0;1]. 
- `RANDOM_SELECT(args)`: the value shall be a random selection from args, whereby args are separated by commas. Strings may be given in usual quotes.
Fields not given in the data but specified in `spec.yml` will be added to the output. 
