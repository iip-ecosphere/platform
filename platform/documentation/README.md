# IIP-Ecosphere platform documentation

## Further documents 
* Platform handbook via [Zenodo](https://doi.org/10.5281/zenodo.5168946) and the [IIP-Ecosphere Website](https://www.iip-ecosphere.eu/). The links may become active/updated some days after a release.
* [Guideline to open the projects and setup the environment](../documentation/Guideline.pdf?raw=true)
* [Release guideline](../documentation/RELEASE.md)
* [Install information](../documentation/INSTALL.md) for own installation, Docker containers and for playing with pre-packaged containers.

## Guidelines
* There is an overall **architecture** and a **platform handbook** in the IIP-Ecosphere ownCloud. Please consult the architecture first to understand 
  how existing and new parts are related.
* **Java projects** are created with Eclipse (2020-12-R). Use iipCodeFormatter.xml from ``platformDependencies`` as formatter. Set text editor print margin to 120 characters.
* For now, we use **JDK 8** in order to be compliant with Edge devices. In particular, for the lower layers that shall be used on edge devices, code must be executable on JDK 8. For higher layers, this limitation may be relaxed. To achieve exchangeable projects across development installations, use in Eclipse the execution environment ``JavaSE-1.8`` as JRE system library. This may be relaxed in future. To be on the safe side, install a recent (open) JDK 1.8 and associated it in Eclipse to JavaSE-1.8 so that only the JDK 1.8 library is available, i.e., no newer operations. 
* Use **Javadoc** to describe the parts and pieces as well as their contract and intention. We assume that parameters of reference types are passed in with instances unless the documentation indicates that null (in Javadoc in bold font) can be used.
* A related Java **Checkstyle** definition is based on Checkstyle 8.35 (definitions are working also with 8.36.1). Please use it. The style definition is in ``platformDependencies`` and shall be added as a project local definition named ``IIP Code Conventions``to Checkstyle before importing the other projects (see [setup guideline](../documentation/Guideline.pdf?raw=true) for details). Please use empty lines as paragraph breaks in text, i.e., whenever a unit of code has been completed that is not an own method. An empty line after each line of code is not recommended in this project. However, empty lines in this sense are not part of the Checkstyle rules.
* Please install **FindBugs** or **Spotbugs** to avoid obvious programming problems.
* The **Java package prefix** shall be ``de.iip-ecosphere.platform``. Test packages shall start with ``test.de.iip-ecosphere.platform`` 
* Java files shall contain the **license header**. Existing files can be used as a template.
* Building Java parts happens with **Maven** (based on a parent POM). Groups are or start with ``de.iip-ecosphere.platform`` to comply with Maven central deployment. Use basic information from the parent 
  POM as far as possible, e.g., valid URL, description, licenses, developers and SCM section. Redefine parts only if 
  needed except for dependencies - please define explicitly your minimum set of required dependencies (easing later
  deployment) Use existing test artifacts for reuse and, where possible, build your tests on existing functionality. 
* Specific functional components (with pre-scribed versions in the overall platform dependencies)
  * For *logging*, we use SLF4J.
  * For JSON encoding/decoding we use jackson (core, preferred data-bind)
* For **tests with network**, e.g., communication protocols or AAS servers, please do not rely always on the same ports as subsequent tests may unexpectedly fail. Check the other tests for their port numbers or, better, use free ephemeral ports (see basic support component, ``NetUtils``) instead. Spring-based tests may require a specific initializer to override static configuration settings with dynamic information such as an ephemeral port. See tests in ``transport.spring.*`` for an example.
* **CI and SNAPSHOT deployment** currently are done via SSE-CI/SSE-Maven-Repo. 
  For legacy reasons on the CI server Jenkins, we add a ``build-jk.xml`` ANT 
  file that executes Maven and deploys the artifacts.
* For new components, please write a **short documentation** in terms of a ``README.md`` and hook that component appropriately into the parent documentations.

## Issues

* So far, there are **no binary releases of BaSyx**. As BaSxy is still evolving (as the IIP-Ecosphere platform), interfaces, classes and imports may change. Therefore, we rely on a certain version in the SSE Maven repository with the same group/artifact id as the official BaSyx code. Thus, if you compiled and installed an own BaSyx version after the point in time when our BaSyx version was packaged, your BaSyx version may syntactically differ from our version, which may lead to execution problems, such as *classes/operations missing*. In this case, please remove BaSyx from your local Maven repository and rely on the version for the IIP-Ecosphere platform. If you still need the other BaSyx version, using two local Maven repositories may be a solution. [spotted/contributed by Miguel Gomez Casado]
* Due to the **restriction to JDK 8**, libraries that are compiled for newer Java versions may not be executed. We try to avoid such libraries, in particular in production code. However, in test code, we cannot always avoid such libraries, e.g., if there is no alternative functionality (so far, we are not aware of a MQTT v5 broker for JDK 1.8). In such cases, *execution on a more recent JDK may be adequate* (you can control this by selecting the JDK, e.g., in the respective Eclipse execution environment). This does not taint broker installations, e.g., on edge devices, as there any (even binary, e.g., C based) version that provides the required protocol may be used. Please *check the documentation/README.md* whether the problem is already known, and if not, please inform the platform team about the problem. [spotted/contributed by Miguel Gomez Casado]