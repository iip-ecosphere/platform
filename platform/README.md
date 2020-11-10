#IIP-Ecosphere platform

This is for all code realizing the IIP-Ecosphere platform. So far present:
* Connection and Transport Layer
    * Transport component (MQTT)
    * Transport extension for AMQP

More to come soon. 

#Guidelines (to be refined)
* Java projects are created with Eclipse (2019-08). Use iipCodeFormatter.xml
  from ``platformDependencies`` as formatter. Set text editor print margin to
  120 characters.
* A related Java checkstyle definition is based on Checkstyle 8.18. The style 
  definition is in ``platformDependencies`` and shall be added as a project 
  local definition named ``IIP Code Conventions``to Checkstyle before importing 
  the other projects.
* The Java package prefix shall be ``de.iip-ecosphere.platform``
* Building Java-parts happens with Maven (based on centralized dependency 
  management/parent POM). Groups are or start with ``de.iip-ecosphere.platform``
* CI and SNAPSHOT deployment currently are done via SSE-CI/SSE-Maven-Repo. 
  For legacy reasons on the CI server Jenkins, we add a ``build-jk.xml`` ANT 
  file that executes Maven and deploys the artifacts.
* In the lower platform layers use (for now) JDK 8 in order to be compliant
  with Edge devices. This recommendation may be relaxed in future.
