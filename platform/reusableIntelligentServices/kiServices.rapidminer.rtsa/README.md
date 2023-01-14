# IIP-Ecosphere platform: RapidMiner RTSA integration

Generic integration of the [RapidMiner](https://rapidminer.com) [Real-Time Scoring Agent (RTSA)](https://docs.rapidminer.com/latest/scoring-agent/install/) for AI components and processes.

So far, the integration is preliminary, i.e., separating mechanisms, ports and the API path are currently fixed. Due to IPR and license reasons, RTSA itself is not part of this integration and must be added (also in the automated  integration) as external packages. For the regression tests, we use a rather simple fake/mock implementation that sets in of RTSA is not present (extracted in src/main/resources/rtsa). Please note that the **original RTSA** requires **exactly JDK 8** while the **fake RTSA** runs with **JDK 8 and newer**.

RTSA usually ships in one ZIP for the agent (to be named RTSA-version.ZIP) and one or multiple deployments (one per service, named according to the service). The RTSA ZIP may contain the RTSA folders directly in the top-level directory or in one singular directory located in the top-level directory. If a special packaging is applied, i.e., RTSA and deployment are shipped together, e.g., for experimental purposes, use an empty RTSA ZIP and an all-encompassing deployment.ZIP including the RTSA.

Testing of this component relies on the [fake version of RTSA](https://github.com/iip-ecosphere/platform/tree/main/platform/reusableIntelligentServices/kiServices.rapidminer.rtsaFake/README.md).

If you are **not running the tests with Java 8**, specify `-Diip.test.java8=<path to java binary for JDK 8>`.

