from Version import Version
from Service import ServiceState
from Service import ServiceKind
from AbstractService import AbstractService
import Registry

Registry.services['1234']=MyService()

class MyService(AbstractService):
    """Demonstration and testing service (wrapper).""" 
    
    def __init__(self):
        """Initializes the service.""" 
        super().__init__("1234", "MyService", Version("1.2.3"), "Default Service", True, ServiceKind.TRANSFORMATION_SERVICE)

    # may specialize operations