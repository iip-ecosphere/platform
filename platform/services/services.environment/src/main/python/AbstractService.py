import logging as logger
logger.basicConfig(level="DEBUG")
from Version import Version
from Service import ServiceState
from Service import ServiceKind
from Service import Service
import Registry

class AbstractService(Service):
    """Basic implementation of the administrative service interface."""

    def __init__(self, id:str, name:str, version:Version, description:str, deployable:bool, kind:ServiceKind):
        """Initializes the service.
        
        Parameters:
          - id -- the unique id of the service, taken from the descriptor (str)
          - name -- the descriptive name of the service, taken from the descriptor (str)
          - version -- the version of the service, taken from the descriptor (Version)
          - description -- the textual description of the service, may be empty, taken from the descriptor (str)
          - deployable -- whether the service is deployable, taken from the descriptor (bool)
          - kind -- the service kind, taken from the descriptor (ServiceKind)
        """ 
        
        self.id = id
        self.name = name
        self.version = version
        self.description = description
        self.deployable = deployable
        self.kind = kind
        self.state = ServiceState.AVAILABLE
        self.netMgtKeyAddress = Registry.netMgtKeyAddresses.get(self.id) # value or none

    def getId(self):
        """Returns the unique id of the service.
        
        Returns:
          str
            The id of the service.
        """ 
        
        return self.id
    
    def getName(self):
        """Returns the name of the service.
        
        Returns:
          str
            The name of the service.
        """ 

        return self.name
    
    def getVersion(self):
        """Returns the version of the service.
        
        Returns:
          Version
            The version of the service.
        """
        
        return self.version
    
    def getDescription(self):
        """Returns the description of the service.
        
        Returns:
          str
            The description of the service.
        """
        return self.description

    def getState(self):
        """Returns the state of the service. [R4c]
        
        Returns:
          ServiceState
            The state of the service.
        """
        
        return self.state

    def setState(self, state:ServiceState):
        """Changes the state. [R133c]
        
        Parameters:
          - newState -- the new state (ServiceState)
        """
        self.state = state # check whether this is permissible
    
    def getNetMgtKeyAddress(self):
        """Returns the resolved address of the netMgtKey of the service.
        
        Returns:
          str (for now)
            Either None or host:port.
        """
        
        return self.netMgtKeyAddress
    
    def isDeployable(self):
        """Returns whether the service is deployable in distributable manner or fixed in deployment location.
        
        Returns:
          bool
            Whether the state is deployable.
        """
        
        return self.deployable
    
    def getKind(self):
        """Returns the service kind.
        
        Returns:
          ServiceKind
            The service kind.
        """
        
        return self.kind

    def activate(self):
        """Activates the service. [adaptation]"""
        
        if self.state == ServiceState.PASSIVATED:
            self.state = ServiceState.RUNNING # preliminary consider state machine

    def passivate(self):
        """Passivates the service. [adaptation]"""
        
        if self.state == ServiceState.RUNNING:
            self.state = ServiceState.PASSIVATED # preliminary consider state machine
    
