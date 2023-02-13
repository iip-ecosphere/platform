from AbstractService import AbstractService
import Registry
from Version import Version
from Service import ServiceState
from Service import ServiceKind

from datatypes.Rec12 import Rec12
from datatypes.Rec13 import Rec13

class MyServiceInterface(AbstractService):

    ingestor = None
    
    def __init__(self):
        """Initializes the service.""" 
        super().__init__("1234", "MyService", Version("1.2.3"), "Default Service", True, ServiceKind.TRANSFORMATION_SERVICE)
        Registry.services['1234'] = self
        Registry.receivers['1234_Rec13']=self.receiveRec13
        Registry.asyncTransformers['1234_Rec12']=self.transformRec12Rec12
        Registry.syncTransformers['1234_Rec13']=self.transformRec13Rec13
        Registry.asyncTransformers['1234_S']=self.transformStringString
        
    def receiveRec13(self, data: Rec13):
        """Sink method, receives data.
        
        Parameters:
          - data -- the data object
        """
        raise NotImplementedError
    
    def attachIngestor(self, ingestor):
        self.ingestor = ingestor

    def ingest(self, data):
        if self.ingestor is not None:
            self.ingestor(data)

    def transformRec12Rec12(self, data: Rec12):
        """Transforms data asynchronously.
        
        Parameters:
          - data -- the data to be transformed. Pass result to associated ingestor.
        """
        raise NotImplementedError
    def transformRec13Rec13(self, data: Rec13) -> Rec13:
        """Transforms data synchronously.
        
        Parameters:
          - data -- the data to be transformed. 
        Returns:
          the transformed data
        """
    
    def transformStringString(self, data: str):
        """Transforms data asynchronously.
        
        Parameters:
          - data -- the data to be transformed. Pass result to associated ingestor.
        """
        raise NotImplementedError

    # ------------------ optional server-client-communication ------------------

    def receivedFromServer(self, data):
        """Receives data from the server.
        
        Parameters:
          - data -- the data object (type unknown, depends on client-server agreement)
        """
        raise NotImplementedError
        