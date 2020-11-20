#IIP-Ecosphere platform

This is for all code realizing the IIP-Ecosphere platform. So far present:
* Managed Platform dependencies
* Transport Layer
    * Transport component
    * Transport extension for MQTT v3 and v5
    * Transport extension for AMQP

More to come soon. 

#Guidelines
* There is an overall architecture in the project ownCloud. Please consult the architecture first to understand 
  how existing and new parts are related.
* Java projects are created with Eclipse (2019-08). Use iipCodeFormatter.xml
  from ``platformDependencies`` as formatter. Set text editor print margin to
  120 characters.
* For now, we use JDK 8 in order to be compliant with Edge devices. To have exchangable projects across development 
  installations, use in Eclipse the execution environment JavaSE-1.8 as JRE system library. This may be relaxed in 
  future.
* A related Java checkstyle definition is based on Checkstyle 8.18. The style 
  definition is in ``platformDependencies`` and shall be added as a project 
  local definition named ``IIP Code Conventions``to Checkstyle before importing 
  the other projects.
* The Java package prefix shall be ``de.iip-ecosphere.platform``. Test packages shall start with ``test.de.iip-ecosphere.platform`` 
* Java files shall contain the license header. Existing files can be used as a template.
* Building Java-parts happens with Maven (based on centralized dependency 
  management/parent POM). Groups are or start with ``de.iip-ecosphere.platform`` 
  to comply with Maven central deployment. Also ensure that POMs contain a valid URL, description, licenses, 
  developers and SCM section. Existing POMs can be used as a template. Use existing test artificts for reuse and, 
  where possible, build your tests on existing functionality. 
* CI and SNAPSHOT deployment currently are done via SSE-CI/SSE-Maven-Repo. 
  For legacy reasons on the CI server Jenkins, we add a ``build-jk.xml`` ANT 
  file that executes Maven and deploys the artifacts.
