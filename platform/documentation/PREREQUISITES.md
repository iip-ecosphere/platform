# oktoflow platform: Prerequisites / Technical Requirements

## Execution

### Basic setup

The [install package](../tools/Install/README.md) offers scripts for step-wise installing the prerequisites as well as more details on the installation.

- [JDK 21](https://github.com/adoptium/temurin21-binaries/releases/tag/jdk-21.0.2%2B13) (tested with JDK 21.0.2), [JDK 17](https://github.com/adoptium/temurin17-binaries/releases/tag/jdk-17.0.10%2B7) (tested with JDK 17.0.10); either one in in path, evironment variable ``JAVA_HOME`` set to JDK installation directory
- Maven 3.9.7 in path
- Python in path with the following packages (required if Python services are used, required for building and basic operations) 

| Python     | 3.8.10 | 3.9.21 | 3.13.5   |
| ---------- | ------ | ------ | -------- |
| pyyaml     | 6.0    | 6.0    |  6.0.2   |
| pyflakes   | 3.2.0  | 3.3.2  |  3.4.0   |
| websockets | 11.0.2 | 11.0.2 | 15.0.1   |

If you plan for a manual installation, you can find (some) archived Java and Maven binaries in [the online install folder](https://projects.sse.uni-hildesheim.de/oktoflow/). A simple way of installing Java/Maven is to unpack the binaries and to add the the respective binaries to your path.

### Python in the build process

We execute Python in the build process of the platform and the apps to validate Python code as well as to execute tests. However, Python setups may be rather individual, ranging from one central Python per device to multiple ones as well as multiple virtual environments. Where Python is executed in the platform and the build processes, we first take the user specified Python into account and, if not explicitly given, try to identify one let it be through environment variables (for Python the binary can be specified in the environment variable `IIP_PYTHON`), the first one in the operating system path as well as some standard locations. In particular for the build processes, also a specification of further Python interpreter parameters like the virtual environment to use (venv, conda, mamba) may be relevant, either explicitly specified or via the environment variable `IIP_PYTHONARGS`). 
For Maven the corresponding command line arguments are `-Dpython.binary` and `-Dpython.pythonArgs`.

### Python service setup

Depending on the use of Python packages/libraries by your/used platform services, installation of further Python libraries may be required. Services shall declare their full dependencies in the configuration model, including the required Python version, so that containers can be build automatically from this information. For development, testing or bare metal installation, the Python dependencies above as well as the following dependencies are needed.

| oktoflow Component    | Python        | 3.8.10    |  3.9.21     | 3.13.5      |
| --------------------- | ------------- | --------- | ----------- | ----------- |
| kiservices.functions  | pyzbar        | 0.1.9     |  0.1.9      | 0.1.9       |
| kiservices.functions  | opencv-python | 4.5.5.64  |  4.5.5.64   | 4.12.0.88   |
| kiservices.functions  | numpy         | 1.20.1    |  1.20.1     | 2.2.4       |
| kiservices.functions  | pillow        | 9.1.0     |  9.1.0      | 11.3.0      |
| flower service/server | flwr          | 1.2.0     | in prep.    | in prep.    |
| flower service/server | tensorflow*   | 2.9.0     | in prep.    | in prep.    |
| flower service/server | pytorch*      | 2.0.1     | in prep.    | in prep.    |
| flower service/server | numpy         | 1.22.0    | in prep.    | in prep.    |

Alternative dependency within one component are marked by *.

### Virtualization

For virtualized execution, automated creation of containers

- Docker in path

### Web User Interface

We use/test against the following versions

- node.js 22.14.0 (also 22.0.0/20.11.1/18.19.1 and newer shall work)
- npm version 10.9.2
- Angular 19.2.5

For the Web-UI (see also (Angular version compatibility matrix)[https://angular.io/guide/versions]). 

## Development

- Execution requirements as basis
- Eclipse 2024-06 (4.32.0), Eclipse 2024-09 (4.33.0)
- Eclipse Checkstyle plugin 10.14.2-10.18.2
- M2E Maven integration 2.6.1
- eGit 6.10-7.0.0 (LFS, see below)

Helpful:
- eclEmma 3.1.9 Plugin for Eclipse
- PyDev 12.1.0-12.2.0 Plugin for Eclipse
- Findbugs 3.0.1 Plugin for Eclipse
- xText 2.35.0-2.36.0
- create a global checkstyle configuration named "IIP Code Conventions" and set `/platformDependencies/iipCodeConventions.xml' as project relative configuration in Eclipse (`Window|Properties|Checkstyle`).
- EASy-Producer 3.10.0 as [pre-built nightly Eclipse installation](https://projects.sse.uni-hildesheim.de/eclipse/easy-nightly/) or as plugin for Eclipse from the [SSE EASy-producer nightly update site](https://projects.sse.uni-hildesheim.de/eclipse/update-sites/easy_nightly/).
    - May require `-Xms512m` and `-Xmx5120m` in `eclipse.ini` 
    - Do not use the XTextBuilder "org.eclipse.xtext.ui.shared.xtextBuilder" rather than "de.uni_hildesheim.sse.EASy-Producer.Builder". On or models, the XtextBuilder may crash or hang Eclipse. The EASy-Builder just focuses on markers. Uncheck 'General|Workspace|Refresh using native hooks or polling' and 'General|Workspace|Refresh on access' or, if needed, 'Project|Build Automatically'. 
    - Depending on your workspace use, it might be required to adjust the file associations so that EASy-Producer editors are default for `*.ivml`, `*.vil`, `*.vtl` and `*.text`.
    - Please keep EASy-Producer updated on relevant changes so that model and EASy-Producer fit together.

## Git LFS

Please note that several Python models exceed the permitted file size of github, thus, [GIT Large File Support](https://git-lfs.com/) is required. We manage ``*.pickle``, ``*.h5``, ``*.tflite`` with LFS. 

  * On the local repository, ``git lfs install`` must be executed. 
  * For pushing (after a git commit/push), an additional command is needed, e.g. ``git lfs push origin main --all``. If it hangs, restart the command. 
  * Receiving files requires ``git lfs pull``.

## Git Longpaths (Windows)

On Windows, dependent on your version of git, it might be necessary to enable `longpaths` using

`git config --system core.longpaths true`

## Git Authentication

Git does not allow for username/password commits anymore. Please create an auth token and use that as password.
