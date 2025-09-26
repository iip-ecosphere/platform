## Considerations for a Permanent or Distributed Installation

For a **permanent installation**, the instantiation process also generates service specifications for Linux systemd, for both, integrated installation of platform services, ECS runtime and service manager on a single machine/container and additional no-dependencies service specifications for device/container installation. The generation of these system service descriptors and their execution is based on several settings in the platform configuration (mentioned below), in particular, as Linux systemd does not take system environment variables into account. These system service descriptors assume an installation of the generated jars (including containing directory, e.g., `plJars`) in the instDir (by default `/opt/iip`). For execution, the Java specified in javaExe (default `/opt/iip/java`, intended to be a link to the actual JVM) is used. After completion of the startup process, the respective platform executable creates a file containing the process identification (PID) in the pidDir (by default /run for Linux and the temporary folder for Windows). These PID files are, e.g., considered by the startup scripts of the generated containers.

If applications shall be executed in **non-containerized** manner, it may be required to install additional programs or libraries that are not shipped with a packaged application. This may be operating system programs that were excluded from packaging as well as **Python dependencies**. Often, Python installations assume that required Python packages are installed per user, i.e. in the user home. If a different process, e.g., via systemd shall execute your services, then the required dependencies must be also available in the search paths of that respective user, i.e., either installed global or as root user. If your installation includes multiple versions of Python, it might be needed to set the environment variable IIP_PYTHON (cf. [platform handbook](../PlatformHandbook.pdf) Section 3.5.2.2). For systemd, IIP_PYTHON may have to be set explicitly in the systemd system descriptor.

A distributed setup with **multiple servers acting in different roles** may require a more individualized configuration. We summarize the configuration variables that are of particular importance for such a setup in the table below.

| IVML Variable | Semantics | Default |
| --- | --- | --- |
| transport.globalHost | Global communication broker (depending on transport protocol) for inter-device communication, monitoring, tracing, etc. Default port is 8883, may differ depending on transport protocol. | localhost |
| transport.localPort | Network port for local broker instances, i.e., brokers for intra-device data transport. | transport.port |
| transport.localEcsPort | Network port of local broker instances (in ECS-runtime container). Must differ from global transport port if ECS-runtime/service manager shall be executed on central servers/broker. | 8889 |
| aasServer.host | Global AAS server host name as used by all devices. |     |
| aasServer.serverHost | Replaces aasServer.host on the server if the AAS server process shall listen to a dedicated network. | \-  |
| aasRegistryServer.host  <br>aasRegistryServer.serverHost | Akin to aasServer, but for the AAS registry. |     |
| platformMonitoring.host | Server hosting the central platform monitoring, by default Prometheus. May be pre-installed. | 127.0.0.1 |
| platformMonitoring.exporterHost | Server hosting the Prometheus data exporter. | 127.0.0.1 |
| platformMonitoring.alertMgrHost | Server running the Prometheus alert Manager. May be pre-installed. | 127.0.0.1 |
| managementUI.port | Port of the platform management UI. Currently started on same server as aasServer. | 4200 |
| containerManager.registry | Server hosting the local Docker registry with port. Start before platform. | 192.168.2.1:5001 |

In a real installation, there are also variables in the configuation model that need consideration, e.g.:

```
  instDir = "/iip/actual";
  javaExe = "/iip/jdk/bin/java";
  pidDir = "/run";
  artifactsFolder = "/iip/actual/artifacts";
  artifactsUriPrefix = "http://...";
  modelBaseFolder = "/iip/actual ";
```

The first three variable assignments define the actual installation locations on the target file system. `instDir` is the installation home directory of the platform, `javaExe` the Java JVM binary and `pidDir` the directory where to write process identification files (PID) to. Although technical installation settings, this information is used to instantiate scripts as actual or adequate default value. For example, Linux systemd service descriptors usually cannot work with system environment variables and need absolute paths. The generated system descriptors will then be ready to use on your system.

The following two settings define the folder where container descriptors, application/service artifacts and deployment plans are located for download. This can be a path considered by the UI webserver or a path handled by an already installed, external webserver. To compose the correct URLs in your installation, i.e., to enable devices the download of these descriptors, the `artifactsUriPrefix` must be set up with correct server name and path.

The last example setting becomes relevant when the management user interface shall display the actual configuration and the user interface shall be enabled to modify the configuration, e.g., to compose new applications in drag & drop fashion. Dependent on your installation, the modelBaseFolder points to the directory where the configuration model (defaults as usual `src/main/easy` and `src/test/easy` which may not be adequate for an installation) is located. The model base folder may be the same used for setting up the platform, it may be located in a different directory due to web server access permissions, it may be a symbolic links or combined with symbolic links if parts of the model shall be located in a separate application directory for development, etc.

As the management UI is based on the platform AAS, which is typically running on a different network port and may even run on a different server, access may need to be granted, in particular in terms of CORS (see also [platform handbook](../PlatformHandbook.pdf) Section 3.12).

For all of these settings, do the assignments in `TechnicalSettings.ivml` and consider freezing the variables, i.e., mention the variable name in the freeze block (usually at the end) of `TechnicalSettings.ivml`. Without freezing, the actual value will not become available to the platform instantiation and a default value will be used instead, i.e., your settings will not have an effect.

Transporting containers to a target device can be done by obtaining them from the local Docker registry. Often a local/private Docker registry is preferred over a public repository as services may rely on IPR protected code, which shall not be made available to the public. We discuss the setup of a local Docker registry in the [installation manual](../INSTALL.md).

Although a platform configuration ensures that the setup of the individual processes is consistent, there are needs for overwriting individual configuration values at runtime, e.g., if the platform runs with ephemeral AAS ports, subsequent platform processes must be adjusted based on this dynamic information. Moreover, in vendor store settings, pre-instantiated platform components might be helpful, for which just a few network settings shall be replaced. This happens in the oktoflow-local.yml file (same structure as the application.yml or iipecosphere.yml files created by the platform process), which is searched in the folder where the platform is started, the harddisk system root folder or the user's home folder.