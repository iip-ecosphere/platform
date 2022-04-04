# IIP-Ecosphere platform: RapidMiner RTSA integration

Generic integration of the [RapidMiner](https://rapidminer.com) Real-Time Scoring Agent (RTSA) for AI components and 
processes.

So far, the integration is preliminary, i.e., separating mechanisms, ports and the API path are currently fixed. Due to IPR and license reasons, RTSA itself is not part of this integration and must be added (also in the automated  integration) as external packages. For the regression tests, we use a rather simple fake/mock implementation that sets in of RTSA is not present (extracted in src/main/resources/rtsa).

For test packaging in the platform/application instantiation, the build process of this package creates two fake ZIP files in `target/fake`, one for RTSA and one for a deployment. The fake RTSA contains the respective class from testing, the deployment zip is intentionally more or less empty.

Manual building may require `mvn package -DskipTests -Djacoco.skip=true`.


