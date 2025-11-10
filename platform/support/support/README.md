# IIP-Ecosphere platform: Basic support libary

Basic support classes for the platform, Maven plugins etc. Must remain on Java 8 as some (external) components enforce Java 8. 

Based on `support.boot`. Over time, further basic classes from `support.aas` may be moved here.

## Local installation properties

Some services have specific requirements regarding their execution environment, e.g., a specific Java, Python version, or Python "venv" virtual environments and nothing else. While such dependencies shall be captured in the configuration model, somewhere the actual installation paths of such software components in the executing system must be defined. This is done in the `installedDependencies.yml` file, which contains a simple mapping of logical names to installation paths, typically to a binary executable. An example of such a file follows:

    locations:
      PYTHON2: /var/lib/python2/bin/python
      PYTHON3: /var/lib/python3/bin/python
      myVenv: /Home/user/myVenv/bin/python
      JAVA11: c:\program files\Java\Java11\bin\java.exe
    envMappings:
      py-service: conda-dflt
      ai-service: conda-dflt
      
The path listing always starts with the keyword `locations`. Below, the logical names and their respective operating system dependent installation locations are stated. By default, the installed dependencies mechanism knows the actual Java version it is running within. For that default Java, the logical name is `JAVA`. Further, there is an entry based on the actual version, e.g., `JAVA8`, `JAVA11`, `JAVA13`. So far, `JAVA` as well as `JAVA8` and `JAVA11` are the only pre-defined names as they are used in platform service implementation code. All other names, in particular for Python are (currently) defined in the configuration model and attached to the service definitions there. For the Python "venv" virtual environment, the name (myVenv) should match the attribute venvName inside the Python service definition, refering to the venv environment path. 

In addition, the installed dependencies file can specify optional environment mappings, i.e., names of execution environments e.g., for Python Conda to the actual implementation. Thus, a (Python) service can query for the actual mapping with its own name and the installed dependencies mechanism returns the actually implemented environment, e.g., for created containers. This allows for re-using but also separating environments as needed.

The container generation of the platform instantiation produces and installs a `installedDependencies.yml` by default. If you head for individual installations, please take care of a respective `installedDependencies.yml` file. The `installedDependencies.yml` may be located in the classpath, the actual directory (as default value for the folder determined by the `iip.installedDeps` system property) or the system root directory.
