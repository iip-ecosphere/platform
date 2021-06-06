import logging as logger
logger.basicConfig(level="DEBUG")
from Service import ServiceState
from Service import ServiceKind

PREFIX_STATUS = "status/"
PREFIX_SERVICE = "operations/service/"

# mimicks de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper
def composeResult(value, exception):
    """Composes a result in the format of the IIP-Ecosphere JsonResultWrapper.
        For now, just returns the value as long as we use this on the example.

    Parameters:
        value -- the value to return, may be None
        exception -- the exception message to return. If given, takes
            precedence over value.
    """

    if not(exception is None):
        return ''
        #return {"exception" : "+exception+"}
    else:
        if value is None:
            return ''
            #return {}
        else:
            return value
            #return {"result" : " + value + "}

# the Python correspondence of the VabIipOperationsBuilder (support.aas.basxy)
class VabIipOperationsBuilder: 
    """Dynamic mapping path names to getter, setter and operation functions of a service.""" 

    getters = {}
    setters = {}
    operations = {}

    def getPrefixStatus(self):
        """Returns the prefix path for the status/properties of the service.
        
        Returns:
          str
            prefix path
        """ 
        
        return PREFIX_STATUS

    def getPrefixService(self):
        """Returns the prefix path for the services/operations of the service.
        
        Returns:
          str
            prefix path
        """ 
        
        return PREFIX_SERVICE
        
    def getGetter(self, path):
        """Returns the getter function assigned to the given path. Getter functions do 
        not take arguments but return the actual value.
        
        Returns:
          function
            getter function or None
        """ 
        
        name = path.split(self.getPrefixStatus(), 1)[1]
        return self.getters.get(name)    
        
    def getSetter(self, path):
        """Returns the setter function assigned to the given path. Setter functions 
        take the new value as argument and return None.
        
        Returns:
          function
            setter function or None
        """ 
        
        name = path.split(self.getPrefixStatus(), 1)[1]
        return self.setters.get(name)
        
    def getOperation(self, path):
        """Returns the operation function assigned to the given path. Operation 
        functions may take arbitrary parameters and may return a value.
        
        Returns:
          function
            operation function or None
        """ 
        
        name = path.split(self.getPrefixService(), 1)[1]
        return self.operations.get(name)

    def defineOperation(self, name, func):
        """Defines the operation function for the given name (without service 
        prefix). Operation functions may take arbitrary parameters and may return a value.
        
        Parameters:
          name -- the name of the operation 
          func -- the actual function
        """ 
        
        self.operations[name] = func
    
    def defineProperty(self, name, getter, setter):
        """Defines the getter or setter function for the given property (without status 
        prefix). Getter functions do not take arguments but return the actual value. Setter 
        functions take the new value as argument and return None.
        
        Parameters:
          name -- the name of the property 
          getter -- the actual getter function (may be None for write-only)
          setter -- the actual setter function (may be None for read-only)
        """ 
        
        self.getters[name] = getter
        self.setters[name] = setter
    
