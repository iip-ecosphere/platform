from enum import Enum
import logging as logger
logger.basicConfig(level="DEBUG")
import json
from Service import ServiceState
from Service import ServiceKind
from VabIipOperationsBuilder import VabIipOperationsBuilder

class ResourceNotFoundException(Exception):
    """Raised when an operation on a resource fails as the resource
    is not there.

    Attributes:
        message -- explanation of why the resource was not found
    """

    def __init__(self, message):
        self.message = message

class JSONProvider:
    """Corrsponding BaSyx class for Python. Provides BaSyx operations to be mapped into 
    service operations based on given access paths."""

    def __init__(self, builder:VabIipOperationsBuilder):
        """Initializes the provider.
        
        Parameters:
          - builder --- the operations builder containing the path-function mappings (VabIipOperationsBuilder)
        """
        self.builder = builder

    def getModelPropertyValue(self, path):
        """Returns the value of a model property.
        
        Parameters:
          - path -- the access path including state prefix
        
        Returns:
          value of the specified model property
          
        Raises:
          ResourceNotFoundException -- if path points to nothing registered
        """
        
        getter = self.builder.getGetter(path)
        if not(getter is None):
            return getter()
        else:
            raise ResourceNotFoundException("Getter for '" + path + "' not found.")

    def setModelPropertyValue(self, path, newValue):
        """Changes the value of a model property.
        
        Parameters:
          - path -- the access path including state prefix
          - newValue -- the new value of the property (must comply with the property type)
        
        Raises:
          ResourceNotFoundException -- if path points to nothing registered
        """

        setter = self.builder.getSetter(path)
        if not(setter is None):
            setter(newValue)
            return ''
        else:
            raise ResourceNotFoundException("Setter for '" + path + "' not found.")

    def createValue(self, path, newEntity):
        """Creates a model entity at path.
        
        Parameters:
          - path -- the access path 
          - newEntity -- the value to be stored at path
        
        Raises:
          ResourceNotFoundException -- always, not supported
        """
        
        raise ResourceNotFoundException("Element '" + path + "' not supported.")

    def deleteValue(self, path):
        """Delets (the value of) a model entity at path.
        
        Parameters:
          - path -- the access path 
        
        Raises:
          ResourceNotFoundException -- always, not supported
        """
        
        raise ResourceNotFoundException("Element '" + path + "' not supported.")
    
    def deleteValue(self, path, parameter):
        """Delets (the value of) a model entity at path.
        
        Parameters:
          - path -- the access path 
          - parameter -- parameters to be considered
        
        Raises:
          ResourceNotFoundException -- always, not supported
        """
        
        raise ResourceNotFoundException("Element '" + path + "' not supported.")

    def invoke(self, path, argsJson):
        """Invokes a model operation.
        
        Parameters:
          - path -- the access path including service prefix
          - argsJson -- the operation arguments as JSON list
          
        Returns:
          - the operation result depending on the operation
        
        Raises:
          ResourceNotFoundException -- if path points to nothing registered
        """

        func = self.builder.getOperation(path)
        args = json.loads(argsJson)
        logger.debug("INVOKE " + str(args)+" "+str(type(args)))
        # split params -> types 
        if not(func is None):
            return func(args)
        else:
            raise ResourceNotFoundException("Operation for '" + path + "' not found.")
            
