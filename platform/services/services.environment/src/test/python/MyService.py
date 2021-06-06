from Service import ServiceState
from Service import ServiceKind

#TODO for now just for testing, shall implement Service
class MyService:
    """Demonstration and testing service (wrapper).""" 
    
    name = "MyService"
    version = "1.0.0"
    description = "Default Service"
    state = ServiceState.STOPPED
    
    def __init__(self):
        """Initializes the service.""" 
        
        pass
        
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
          str
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
        """Returns the state of the service.
        
        Returns:
          ServiceState
            The state of the service.
        """
        
        return self.state

    def passivate(self):
        """Passivates the service if it can be passivated."""
        
        if self.state == ServiceState.RUNNING:
            self.state = ServiceState.PASSIVATED

    def activate(self):
        """Activate the service if it can be activated."""
        
        if self.state == ServiceState.PASSIVATED:
            self.state = ServiceState.RUNNING

    def setState(self, newState):
        """Changes the state.
        
        Parameters:
          - newState -- the new state (ServiceState)
        """
        
        self.state = newState
