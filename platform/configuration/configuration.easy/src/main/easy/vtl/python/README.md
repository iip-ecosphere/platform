# IIP-Ecosphere platform: Variability Instantiation Templates for Python

The Python templates create interfaces and serializers to be used within the Python service environment of the platform (see `services.environment`). As the integration with the distributed service execution happens in Java, not so many different artifacts must be created for Python, i.e., less VTL templates are in this directory. Python files are packaged and distributed through Maven artifacts and, ultimately, IIP-Ecosphere service artifacts.

* `PythonBasics` basic VTL operations to map configured types to Python, e.g., how to turn an arbitrary name into a Java identifier. 
* `PythonType` creates classes/interfaces to represent data sent to/received from Python services. 
* `PythonJsonSerializer` creates the (default) JSON wire format transport mechanism to link Python Services and the Python Service Environment with their Java counterpart integrated into the service execution, i.e., to serialize the classes created by `PythonType`. 
* In shared platform interface mode: `PythonServiceInterface` creates the API for individual services specified in the configuration model to be realized in Python. Use the generated interfaces (including the type interfaces) to realize your custom services.
* In non-shared platform interface mode: `PythonMeshElementInterface` creates the API for individual services specified in the configuration model to be realized in Python. Use the generated interfaces (including the type interfaces) to realize your custom services.
* `PythonAssembly` creates a Maven assembly descriptor to package all Python artifacts of a service. 

The scripts may be executed multiple times, e.g., for configured data types or services.