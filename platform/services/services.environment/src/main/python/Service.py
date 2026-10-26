from enum import Enum
from Version import Version

class ServiceState(Enum):
    """Denotes the service state. Names and ordinals must be aligned with Java."""
    
    AVAILABLE = 0
    DEPLOYING = 1
    CREATED = 2
    STARTING = 3
    RUNNING = 4
    FAILED = 5
    STOPPING = 6
    STOPPED = 7
    PASSIVATING = 8
    PASSIVATED = 9
    MIGRATING = 10
    ACTIVATING = 11
    RECOVERING = 12
    RECOVERED = 13
    RECONFIGURING = 14
    UPDATING = 15
    UNDEPLOYING = 16
    UNKOWN = 17

class ServiceKind(Enum):
    """Denotes the service kind. Names and ordinals must be aligned with Java."""
    
    SOURCE_SERVICE = 0
    TRANSFORMATION_SERVICE = 1
    SINK_SERVICE = 2
    PROBE_SERVICE = 3
    SERVER = 4

def composeIdShort(*names):
    """Composes multiple id parts to a single AAS idShort"""
    res = ""
    for n in names:
        if len(res) == 0:
            res = n
        elif len(n) > 0:
            res = res + "_" + n
            #if len(n) == 1:
            #    res = res + n[0].upper()
            #elif len(n) > 1:
            #    res = res + n[0].upper() + n[1:]
    return res

class Service:
    """Interface of an administrative service interface."""

    def getId(self) -> str:
        """Returns the unique id of the service.
        
        Returns:
          str
            The id of the service.
        """ 
        
        raise NotImplementedError
    
    def getName(self) -> str:
        """Returns the name of the service.
        
        Returns:
          str
            The name of the service.
        """ 

        raise NotImplementedError
    
    def getVersion(self) -> Version:
        """Returns the version of the service.
        
        Returns:
          Version
            The version of the service.
        """
        
        raise NotImplementedError
    
    def getDescription(self) -> str:
        """Returns the description of the service.
        
        Returns:
          str
            The description of the service, may be empty.
        """
        raise NotImplementedError

    def getState(self) -> ServiceState:
        """Returns the state of the service. [R4c]
        
        Returns:
          ServiceState
            The state of the service.
        """
        
        raise NotImplementedError

    def setState(self, state:ServiceState):
        """Changes the state. [R133c]
        
        Parameters:
          - newState -- the new state (ServiceState)
        """
        
        raise NotImplementedError
    
    def isDeployable(self) -> bool:
        """Returns whether the service is deployable in distributable manner or fixed in deployment location.
        
        Returns:
          bool
            Whether the state is deployable.
        """
        
        raise NotImplementedError
    
    def getKind(self) -> ServiceKind:
        """Returns the service kind.
        
        Returns:
          ServiceKind
            The service kind.
        """
        
        raise NotImplementedError
        
    def getNetMgtKeyAddress(self):
        """Returns the resolved address of the netMgtKey of the service.
        
        Returns:
          str (for now)
            Either None or host:port.
        """
        
        raise NotImplementedError

    def migrate(self, resourceId:str):
        """Migrates a service. However, it may be required to migrate/move the containing artifact. [adaptation]
        
        Parameters:
          resourceId -- the id of the resource to deploy to (str)
        """

        pass
        
    def update(self, location:str):
        """Updates the service by the service in the given URL location. This operation is being called to notify about
        a running service update, requiring the service to save its states and free its resources. An implementation
        may perform a full update cycle, stop-update-start, or, when service reloading is enabled a short 
        update cycle update-updated.
        
        Parameters:
          location -- the location from where to update the service (str)
        """
        
        pass

    def updated(self):
        """Notifies the service that the update has been completed in a short update cycle, i.e., start will not be 
        rather than required resources and state shall be restored now.
        """
        
        pass
       
    def switchTo(self, targetId:str):
        """Switches to an interface-compatible service. This method cares for stopping the old service, performing
        a handover if adequate, starting the target service. [adaptation]
        
        Parameters:
          targetId -- the id of the target service (str)
        """
        
        pass
   
    def activate(self):
        """Activates the service. [adaptation]"""
        
        pass

    def passivate(self):
        """Passivates the service. [adaptation]"""
        
        pass
    
    def reconfigure(self, values:dict):
        """Reconfigures the service. [adaptation]
        
        Parameters:
          - values -- the (service-specific) name-value mapping that shall lead to a reconfiguration of the service; 
            values come either as primitive values or as JSON structures complying with the parameter descriptor. The 
            service is responsible for correct JSON de-serialization according to its descriptor.
        """
        
        pass

