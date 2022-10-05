By convention, service implementations must be in module "services" (specified in IVML as servicesPackageName in 
the service). As we want to be able to mock the flow, there is a similar class reacting on the same input, but just 
mocking the AI. It will become active by changing the module name to be used by the Python service environment in 
the IVML file through the variable flowTest.

Service-specific information is in the parent class constructor. Just call it.
Further, services must register themselves through self-instantiation in the last line.

This is not the folder for the Plan B robot scripts as this folder goes into a different form of packaging.