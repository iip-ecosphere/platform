from Version import Version
from Service import ServiceState
from Service import ServiceKind
from AbstractService import AbstractService
import Registry

class MyServer(AbstractService):
    """Demonstration and Python-based server.""" 

    ingestor = None ## gen

    def __init__(self):
        """Initializes the server.""" 
        # generated part (so far, may desire interface class)
        super().__init__("test-py", "test-py", Version("0.2.1"), "Test Python Server", True, ServiceKind.SERVER)
        Registry.services['test-py'] = self

        # no data I/O

    def attachIngestor(self, ingestor): ## gen
        self.ingestor = ingestor

    def ingest(self, data): ## gen
        if self.ingestor is not None:
            self.ingestor(data)

    # ---------------- optional service management -----------------

    def reconfigure(self, values:dict):
        print("SERVER RECONFIGURED: " + str(values))
        
    def setState(self, state:ServiceState):
        super().setState(state);
        print("SERVER CHANGED STATE: " + str(state));

    # ------------------ optional server-client-communication ------------------

    def receivedClientServer(self, data): ## gen
        print("FROM CLIENT: " + str(data));
        #do some stuff based on data

#registers itself
MyServer()