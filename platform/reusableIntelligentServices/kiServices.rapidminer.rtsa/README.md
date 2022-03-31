# IIP-Ecosphere platform: RapidMiner RTSA integration

Generic integration of the [RapidMiner](https://rapidminer.com) Real-Time Scoring Agent (RTSA) for AI components and 
processes.

So far, the integration is preliminary, i.e., separating mechanisms, ports and the API path are currently fixed. Due
to IPR and license reasons, RTSA itself is not part of this integration and must be added (also in the automated 
integration) as external packages. For the regression tests, we use a rather simple fake/mock implementation that
sets in of RTSA is not present (extracted in src/main/resources/rtsa).


