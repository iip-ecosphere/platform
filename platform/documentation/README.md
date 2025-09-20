# oktoflow platform documentation

This folder contains the documentation of the okotflow platform.

* The platform [handbook](PlatformHandbook.pdf)
* [Install information](INSTALL.md) for own installation, Docker containers and for playing with pre-packaged containers.
* The oktoflow [FAQ](FAQ.md)
* The oktoflow [HOW-TO](HOWTO.MD)
* The oktoflow [concepts documentation](concepts/concepts.MD)

## Technical Guidelines
* The technical requirements are documented in [PREREQUISITES](PREREQUISITES.md).
* There is an overall **architecture** described in the (**platform handbook**)[PlatformHandbook.pdf]. Please consult the architecture first to understand how existing and new parts are related.
* Please consider the [guideline on how to open the code projects and setup the environment](Guideline.pdf?raw=true).
* Please note that special characters like whitespaces in folder names (in particular on Windows) may cause the installation, platform installation or examples to fail.  
* **Java projects** are created with Eclipse. Import ``iipCodeFormatter.xml`` from ``platformDependencies`` as formatter settings. Set text editor print margin to 120 characters. For Python development, please install the `PyDev` Eclipse plugin, which is supported by our generated application templates.
* Since version 0.7.1 we use **JDK 17** for build and execution. To achieve exchangeable projects across development installations, use in Eclipse the execution environment ``JavaSE-17`` as JRE system library.
* Consequently, source code compliance is ``JavaSE-17``, i.e. we ignore deprecation warnings for JDKs with higher version number until we officially demand, e.g., JKD 21.
* Use **Javadoc** to describe the parts and pieces as well as their contract and intention. We assume that parameters of reference types are passed in with instances unless the documentation indicates that null (in Javadoc in bold font) can be used.
* A related Java **Checkstyle** definition is in ``platformDependencies``. The style definition is in ``platformDependencies`` and shall be added as a project local definition named ``IIP Code Conventions`` to Checkstyle before importing the other projects (see [setup guideline](../documentation/Guideline.pdf?raw=true) for details). Checkstyle shall be enabled on all platform component projects and Checkstyle errors shall be fixed before committing. Please use empty lines as paragraph breaks in text, i.e., whenever a unit of code has been completed that is not an own method. An empty line after each line of code is not recommended in this project. However, empty lines in this sense are not part of the Checkstyle rules.
* Please install **FindBugs** or **Spotbugs** to avoid obvious programming problems.
* The **Java package prefix** for production components shall be ``de.oktoflow.platform`` (older packages still)``de.iip-ecosphere.platform``. Test packages shall similarly start with ``test.de.oktoflow.platform`` (older packages still ``test.de.iip-ecosphere.platform``) while also the package prefix for production components can be used to prevent unneeded (otherwise required) public methods.
* Java files shall contain the **license header**. Existing files can be used as a template.
* Building Java parts happens with **Maven** based on the parent POM in ``platformDependencies``. Groups identifiers are or start with ``de.iip-ecosphere.platform`` to comply with Maven central deployment. Use basic information from the parent POM as far as possible, e.g., valid URL, description, licenses, developers and SCM section. Redefine parts only if needed except for dependencies - please define explicitly your minimum set of required dependencies (easing later deployment) Use existing test artifacts for reuse and, where possible, build your tests on existing functionality. In some cases, Eclipse shows errors on the parent POM, which cannot be validated with a standalone command line Maven installation. In theses cases, often, a refresh of the Maven information in Eclipse (project context menu, Maven sub-menu), a restart of Eclipse to refresh internal Maven caches or even the execution of a command line Maven on the respective project and a restart of Eclipse may resolve the issue. Currently, the Maven build cache works for oktoflow on Linux but not on Windows, i.e., it is currently disabled.
* Specific functional components (with pre-scribed versions in the overall platform dependencies)
  * For *logging*, we use SLF4J. Typically, lower level dependency such as Spring or BaSyx ship with a respective logging setup, e.g., logback. Thus, individual projects of the platform may only specify a SLF4J implementation dependency for the testing scope but not for the production code. Consult the platform handbook for logging and exception rules, i.e., do not just ignore exceptions, leave a TODO comment or just emit the stacktrace to the console, which is typically not an adequate exception handling.
  * For JSON encoding/decoding in the platform code we use jackson (core, preferred data-bind). However, for data processing along service chains, different (faster) libraries may be used. These dependencies are then introduced by the code generation.
* For new components, please write a **brief documentation** in terms of a ``README.md`` and hook that component appropriately into the parent documentations. Add the new project to the top-level platform POM and to the CentralMaven dependencies for release deployment.
* **Binaries** that are created through build processes, i.e., usually in `target` shall not be committed. `target` shall be listed in `.gitignore`.
* For **tests with network**, e.g., communication protocols or AAS servers, please do not rely always on the same ports as subsequent tests may unexpectedly fail. Check the other tests for their port numbers or, better, use free ephemeral ports (see basic support component, ``NetUtils``) instead. Spring-based tests may require a specific initializer to override static configuration settings with dynamic information such as an ephemeral port. See tests in ``transport.spring.*`` for an example.
* **CI and SNAPSHOT deployment** currently are done via SSE-CI/SSE-Maven-Repo. For legacy reasons on the CI server Jenkins, we add a ``build-jk.xml`` ANT file that executes/configures Maven and deploys the artifacts.
* **Docker deployment**: Currently, basic installation/demonstration/development containers are built and deployed automatically using a github action script. The script can be triggered manually and will be triggered automatically when a new version of the platform is created. Two Docker containers (one including the platform servers and a setup for a resource on the same machine and one for the CLI) are built and deployed automatically with suffix ".latest" to DockerHub, similarly for a Linux development container including EASy-Producer. Docker containers of (intermediate) "milestone" builds shall be achieved on DockerHub with the respective date (see [readme](https://github.com/iip-ecosphere/platform/blob/main/platform/tools/Install/container/readme.txt) in the install package).

## Further documents 
* [Catalogue of formats employed by the platform](../documentation/FORMATS.md).
* [Listing of administrative actions](../documentation/ACTIONS.md).
* [Guideline on how to create platform releases](../documentation/RELEASE.md)
* [Guide for adding EASy-Plugin to Eclipse under Linux](../documentation/Guide_Adding_Eclipse-EASy-Plugin_on_Linux.pdf)
