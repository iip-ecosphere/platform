# oktoflow platform FAQ

## Management UI does not show information

*Symptom:* The management UI does not show information rather than a text about CORS.

*Reason:* The Cross origin resource security (CORS) is not configured correctly. Your browser tries to access the platform AAS (registry and server running on different ports) from the webapp and fails.

*Solution:* Add the variable ``aasAccessControlAllowOrigin`` to your configuration model, usually to ``TechnicalSettings.ivml``. Typical value for this variable is ``"*"``, but you may want to set something more specific here. By default, this variable is currently not set in the configuration meta model, but we added it to the install package in version 0.7.0. Please note that CORS settings are not completely passed to the AAS infrastructure until version 0.6.0, i.e., you need to have version 0.7.0.

## Error parsing HTTP header

*Symptom:* A part of the platform (platform server, ECS runtime, service manager or platform command line interface) issues an exception with the following message:
``org.apache.coyote.http11.AbstractHttp11Processor.process Error parsing HTTP request header Note: further occurrences of HTTP header parsing errors will be logged at DEBUG level.``

*Reason:* One reason may be that a client such as the command line interface tries to access a platform server (AAS server, registry) with an encrypted protocol (HTTPS) while the server is running a non-encrypted protocol (HTTP). 

*Solution:* Ensure that the certificates for client and server side do match.

## Maven artifact missing

*Symptom:* While working with the platform against a release version in Maven, it appears that one of the (non-java) artifacts is missing.

*Reason:* Although we carefully check the artifacts before a release, it may be the case that the automatic deployment (script) missed some.

*Solution:* Please let us know about the problem via GitHub. 

## *XXX* has been compiled by a more recent version of the Java Runtime

*Symptom:* While executing in particular central parts of the platform, this error message/exception may occur.

*Reason:* Maven tends to resolve dependencies to the most recent version using a given version number as minimum, in particular if version ranges are allowed. As long as dependencies do not change or the specified version range is feasible, no such problems shall occur. It may occur upon the first resolution, i.e., during installation or when dependencies are updated, e.g., during Continuous Integration (CI) when Maven is requested to search for more recent snapshots. However, in particular for Eclipse components which declare version ranges, compiler settings currently seem to be changed from JDK 8 to JDK 11, i.e., even a minor version change may suddenly (upon an unintended update) lead to this failure.

*Solution:* As quick fix, use the JDK version indicated by the class version number to execute the respective platform component, i.e., usually JDK 11. Alternatively, you may force Maven to download a compatible, previous version number by creating a simple POM and deleting the failing version. In any case, please let us know about the problem via GitHub. We will try to fix the version numbers for central parts such as Eclipse components to a version range that allows for safe execution.

## Platform code cannot be setup in Eclipse, e.g., parent POM missing

*Symptom:* Your IDE reports missing Maven artifacts and shows compilation errors, in particular the parent POM of the platform is missing. Similarly, the code style checking may fail due to missing style definition file.

*Reason:* The parent POM of the platform defines the versions of non-singleton/wrapped libraries (cf. Section 4 of the platform handbook). Without that particular POM, compilation cannot run successfully as the artifact version numbers/ranges are missing. If you are working with a release version, it may also be the case that one of the released artifacts is missing (cf. Section 9.8.2 of the platform handbook).

*Solution:* Please refer to the [code setup guide](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/INSTALL.MD).

## Unknown platform coding conventions

*Symptom:* After a first contact with the platform code it seems that you are missing detailed information about applied conventions on how to write code and you cannot find all conventions in this document.

*Reason:* Although we tried to capture the most important conventions in this document, this document is not intended to be a programmer’s guide, i.e., we do not necessarily repeat all coding conventions here.

*Solution:* Please refer to the platform coding guidelines in GitHub.

## Maven does not find app dependencies

*Symptom:* When executing the platform instantiation, Maven complains about missing dependencies.

*Reason:* Typically, Maven dependencies for apps that ship as examples with the platform are deployed into a public Maven repository. If Maven is executed locally, sometimes required artifacts are not deployed correctly. 

*Solution:* Please open a shell, navigate into the respective directory of the app or the app installations and run mvn install. 

## Execution of application fails due to Java CompileError

*Symptom:* An app is built correctly but when starting it, a Java CompileError occurs and prevents the app from starting up. The messages indicate that packages are missing that are actually in the app fat jar.

*Reason:* We observed this, if app implementation projects override compile settings defined by the platform dependencies. In more details, when you create a new app implementation project, your IDE initially does not know that you will rely on the platform dependencies and may sets up the compile settings according to your local IDE compile settings. When inheriting your POM from the platform dependencies to rely on the platform's build setup, the IDE compile settings remain in your POM and in extreme cases may conflict with class files included from the generation. This can lead to a Java CompileError (a kind of class loading link error).

*Solution:* Please remove any local compiler setup from your POM files and run Maven on app implementation and app project again.

## Services do not start due to problems with javax.el.ExpressionFactory

*Symptom:* When starting services, the service manager reports class loading or instantiation problems for ``javax.el.ExpressionFactory``.

*Reason:* The Java Expression Language (EL) is required by Hibernate, which in turn is used by Spring Boot/Cloud Stream. The EL ships in two parts, interfaces and implementation. Over the time, several versions and implementations of both parts occurred. The Spring packages used by the platform declare a dependency to ``jakarta.el``, which ships both parts in the same jar (version 3.0.3). However, due to transitive dependencies, e.g., to Tomcat from BaSyx, further versions such as ``tomcat-el-api`` or the original ``javax.el`` may be parts of service implementation dependencies. Multiple versions of the interfaces may, dependent on the classloading sequence, interfere and cause the described symptom. In the generated parts, we try to prevent such overlaps, but, however, we cannot be aware of the dependencies declared by your implementing classes. 

*Solution:* Identify all interfaces and implementations of EL and exclude superfluous ones from the dependencies. Alternatively, try to enforce a class loading sequence that loads jakarta.el before all other EL interfaces and implementations. Typically, the generated parts and the default Spring service packaging take care of that. Similarly, the ZIP service artifact including an explicit classpath file are packaged to consider this issue, but due the use of wildcards for ZIP service artifacts not containing a classpath file, an intended class loading sequence cannot be guaranteed then.
 
## Service execution through platform fails

*Symptom:* When starting services through the platform, the service manager reports a state change to FAILED instead of RUNNING.

*Reason:* There are multiple reasons that can cause this symptom:
1.	Failures in the service code, e.g., a Python-based service cannot be started because the required Python script is not correctly packaged or cannot be executed due to implementation errors.
2.	Communication failures as the network communication is not set up correctly, e.g., required ports are already used or not accessible (firewall, not declared as external ports in containers, container is not running in host network mode, etc.)
3.	Timing issues in particular when services are started (the first time) in a container.

*Solution:* Depending on the actual reason, e.g., failures in service code must be solved or communication failures can be addressed by correct network configuration (including the respective settings in container descriptors). Timing issues often occur when the waitingTime for the service manager is not set correctly. The default value is 1 minute, but on resource-constrained devices, 2 or 3 minutes may be more adequate.

## Why do platform scripts always/not check for recent dependency snapshots

*Symptom:* When starting platform services/applications or executing build commands, the Maven build process may check for most recent snapshots. 

*Reason:* As long as the platform is in development, it is convenient have the most recent snapshot builds available. By default, snapshots are enabled only on the SSE Maven snapshot repository, not for other used Maven repositories. Over the time, we changed our strategy and in particular since version 0.7.0 fall back to the default update strategies (daily).

*Solution:* You can decide on the update behavior, per build process using the maven switch `-U` or globally via the maven settings file.
Go to your local Maven repository (usually in your home directory in the folder ``.m2``) and modify the settings file there. If there is no settings file, you can create a new one as shown below (for always updating snapshots on the SSE maven repositories). To become effective, it is important that the repository ids are stated as in the platform dependencies pom.

    <settings xmlns=http://maven.apache.org/SETTINGS/1.1.0
     xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance
     xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0
      http://maven.apache.org/xsd/settings-1.1.0.xsd">
    
      <activeProfiles>
        <activeProfile>github</activeProfile>
      </activeProfiles>
    
      <profiles>
        <profile>
          <id>github</id>
          <repositories>       
           <repository>
             <id>SSE-mvn</id>
             <name>SSE</name>
             <url>https://projects.sse.uni-hildesheim.de/qm/maven/</url>
             <layout>default</layout>
             <releases>
                <enabled>true</enabled>
             </releases>
             <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
             </snapshots>
           </repository>
          <repositories>
          <pluginRepositories>
            <pluginRepository>
             <id>SSE-mvn-plugins</id>
             <name>SSE Maven</name>
             <url>https://projects.sse.uni-hildesheim.de/qm/maven</url>
             <releases>
                <enabled>true</enabled>
             </releases>
             <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
             </snapshots>
            </pluginRepository>
          </pluginRepositories>
        </profile>
       </profiles>
     </settings>

If there is already a repositories section, please add the contents for the “SSE” repository as shown above. 

## My configuration settings do not affect the instantiation

*Symptom:* You changed settings in the configuration model by assigning new values to variables. This does not seem to have an effect on the instantiation.

*Reason:* A typical reason is that the variables are not frozen after setting the value. Without freezing, the values are not taken over into the instantiation (see also Section 8 of the platform handbook). Please do also consider that adding new variables with values and freezing them must not necessarily lead to an effect. If the type is a service, an application, a mesh etc. the instantiation will take that up as the types are known. Just adding a String, Integer or Real variable somewhere potentially implies that the instantiation does not know how an instantiation of that variable shall take place.

*Solution:* Freeze your variables in the right place. If it is a pre-defined variable, freeze them in one of the top-level IVML modules/projects of your configuration. Services, applications, or meshes are typically frozen in the file where they are defined as new variables. In this case, freezing usually happens automatically as these modules declare a freeze block containing a “.” (freeze all new variables in this project). Please consider that a frozen variable intentionally cannot be changed anymore in IVML projects that import the project where the variable was frozen.


## My application/example build process unintendedly executes Java tests/Javadoc

*Symptom:* You execute a one-step maven build process, usually for a all-in-one application/example. Thereby Java test/Javadoc errors occur, although the application/example shall be built correctly and also individual build steps can be executed successfully.

*Reason:* The build process accidentally defines build steps on the project itself rather than the profiles (usually ``App`` or ``EasyGen``). Although you do not see any build steps, they may be inherited from a parent Maven POM, e.g., the platform dependencies POM.

*Solution:* Disable these build steps on top level. If you rely on the multi-step execution of the oktoflow invoker-plugin, then typically placing system properties in the configuration of the respective execution disables the problematic build steps, e.g.,

      <systemProperties>
       <systemProperty>
        <key>skipTests</key>
        <value>true</value>
       </systemProperty>
       <systemProperty>
        <key>maven.javadoc.skip</key>
        <value>true</value>
       </systemProperty>
      </systemProperties>

## Installation error while building an Image Container during platform instantiation.

*Symptom:* An error during the instantation due to an installation error in building an image container while installing Java or Python dependencies.

*Reason:* An external modification on an image that we us as base image, e.g., ``python:3.8.17-slim-buster``. Such modifications may lead to missing files that shall be fetched during an installation of the operating system or of further software components. An example that happened to us in August 2023 was a container using ``python:3.8.17-slim-buster`` as base image into which Java 11 shall be installed.

*Solution:* Change the base image version to an older (or if available and feasible, newer) version where the issues does not occur. Unfortunately, this may require some tries and may lead to some errors. In the example above, we changed ``python:3.8.17-slim-buster`` to ``python:3.8.16-slim-buster``, which then allowed for an installation of Java 11.


## Python dependency/module not found

*Symptom:* You are trying to deploy an application with Python services and Python complains about missing dependencies, i.e., module names that cannot be resolved.

*Reason:* When you are deploying an application to a generated container, this symptom shall not occur if all Python services have their required dependencies declared in the configuration model. When deploying an application on a bare operating system, the platform will not touch the installation, i.e., you are responsible for installing the respective packages. Depending on the installation of the platform, even a different user may be in charge of executing your application, e.g., the platform is started automatically via systemd (potentially as root) and you are deploying via CLI from user space. In this case, dependencies may be missing although you already have installed them (into a different account).

*Solution:* For containerized applications, please check the declared dependencies in the configuration model for their completeness. For non-containerized applications, please figure out, which account is executing the platform services, in particular the service manager, and complete the installation of missing dependencies for that user or as global dependencies. If multiple Python versions are installed on your target system, you may have to set the environment variable ``IIP_PYTHON`` to the respective Python binary. See also "Considerations for a Permanent or Distributed Installation" in the platform handbook.


## How do I upgrade platform/examples/applications

*Symptom:* There is a new version of the platform. How can I upgrade?

*Reason:* The platform changes and we publish a release form time to time. Both, applications and applications need to be re-instantiated so that new dependencies are taken up and potentially changed interfaces can be addressed correctly. In seldom cases, the also application-specific code must be adjusted manually.

*Solution:* The platform version depends on the version in the Maven POMs and the configuration meta model. In following cases, adjustments to the platform configuration may be needed depending on the changes that were applied in the upgrade.
 
  * Platform:  
    * Grab a new install package, transfer your configuration into that package and run `mvn install`. This will obtain the most recent configuration meta model corresponding to the platform version.
    * Change the version in the Maven POM of your platform, run `mvn -U install -Dunpack.force=true` in the main folder of your platform installation.
  * Application: Change the version in the Maven POM of your platform, run `mvn -U install -Dunpack.force=true` in the main folder of the application.
  * Example: Update the code via git and then run `mvn -U install -Dunpack.force=true` in the main folder of the example.
    