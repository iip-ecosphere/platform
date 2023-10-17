contains resources for RapidMiner RTSA
- RTSA-<ver>.zip with <ver> replaced by version specifier such as 0.14.5 is RTSA itself. The version is specified in 
  Services.ivml.
- <service-id>-<ver>.zip is the RTSA instance for a certain service created by RapidMiner Studio with <service-id> 
  denoting the service name as specified in the configuration and <ver> the version of the service. The internal 
  structure is
     home
	    deployments
		    <service-instance>.zip
- for the fake services, the service-instance.zip may contain a file called spec.yml to define the behaviour of the 
  fake RTSA		    