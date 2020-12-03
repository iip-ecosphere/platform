#IIP-Ecosphere platform

This is for all code realizing the IIP-Ecosphere platform. So far present:
* Managed Platform dependencies (parent POM)
* Transport Layer
    * Transport component
         * Transport connector for MQTT v3 and v5 based on [Eclipse Paho](https://www.eclipse.org/paho/)
         * Transport connector for AMQP based on [RabbitMQ](https://www.rabbitmq.com/)
    * Optional transport support for [Spring cloud stream](https://spring.io/projects/spring-cloud-stream)
         * Transport connector binder for MQTT v3 and v5 based on [Eclipse Paho](https://www.eclipse.org/paho/)
         * Transport connector binder for AMQP based on [RabbitMQ](https://www.rabbitmq.com/)
    * Platform/Machine connectors component
       * Platform connector for OPC UA v1 based on [Eclipse Milo](https://projects.eclipse.org/projects/iot.milo)
       * Platform connector for Asset Administration Shell/[BaSyx](https://www.eclipse.org/basyx/)
       * Platform connector for MQTT v3 and v5 based on [Eclipse Paho](https://www.eclipse.org/paho/)
 * Services (starting)

More to come soon with next focus on edge/stream performance, configuration integration and edge deployment. 

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
* Building Java-parts happens with Maven (based on a parent POM). Groups are or start with ``de.iip-ecosphere.platform`` to comply with Maven central deployment. Use basic information from the parent 
  POM as far as possible, e.g., valid URL, description, licenses, developers and SCM section. Redefine parts only if 
  needed except for dependencies - please define explicitly your minimum set of required dependencies (easing later
  deployment) Use existing test artificts for reuse and, where possible, build your tests on existing functionality. 
* CI and SNAPSHOT deployment currently are done via SSE-CI/SSE-Maven-Repo. 
  For legacy reasons on the CI server Jenkins, we add a ``build-jk.xml`` ANT 
  file that executes Maven and deploys the artifacts.
