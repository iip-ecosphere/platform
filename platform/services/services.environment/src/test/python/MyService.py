from Version import Version
from Service import ServiceState
from Service import ServiceKind
from AbstractService import AbstractService

class MyService(AbstractService):
    """Demonstration and testing service (wrapper).""" 
    
    def __init__(self):
        """Initializes the service.""" 
        super().__init__("1234", "MyService", Version("1.0.0"), "Default Service", True, ServiceKind.TRANSFORMATION_SERVICE)

    # may specialize operations