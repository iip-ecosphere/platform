from Version import Version
from Service import ServiceState
from Service import ServiceKind
from AbstractService import AbstractService
from interfaces.MyServiceInterface import MyServiceInterface
import Registry

from datatypes.Rec12 import Rec12
from datatypes.Rec13 import Rec13

class MyService(MyServiceInterface):
    """Demonstration and testing service (wrapper).""" 

    rec12ingestor = None
    
    def __init__(self):
        """Initializes the service.""" 
        super().__init__()

    # may specialize operations

    def receiveRec13(self, data: Rec13):
        print(str(data))

    def transformRec12Rec12(self, data: Rec12):
        #do some complex stuff based on data
        self.ingest(data)

    def transformRec13Rec13(self, data: Rec13) -> Rec13:
        #do some stuff based on data
        return data

    def transformStringString(self, data: str):
        #do some stuff based on data
        self.ingest(data)

    def reconfigure(self, values:dict):
        print("RECONFIGURED: " + str(values))

#registers itself
MyService()