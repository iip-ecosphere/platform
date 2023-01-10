from datatypes.PythonSyncTestInput import PythonSyncTestInput
from datatypes.PythonSyncTestInputImpl import PythonSyncTestInputImpl
from datatypes.PythonSyncTestOutput import PythonSyncTestOutput
from datatypes.PythonSyncTestOutputImpl import PythonSyncTestOutputImpl
from interfaces.ExamplePythonSyncServiceInterface import ExamplePythonSyncServiceInterface
import random

class ExamplePythonSyncService(ExamplePythonSyncServiceInterface):
    """Example Python Sync Service implementing generated interface."""
    
    def __init__(self):
        """Initializes the service.""" 
        super().__init__()
        
    def transformPythonSyncTestInput(self, data: PythonSyncTestInput) -> PythonSyncTestOutput:
        """Synchronous data processing/transformation method.
    
        Parameters:
          - data -- the data to process
        Returns:
          the processed data              
        """
        result = PythonSyncTestOutputImpl()
        result.id = data.id
        result.value1 = data.value1
        result.value2 = data.value2
        result.confidence = random.uniform(0, 1)
        result.prediction = result.confidence > 0.75
        
        return result # synchronous processing

#registers itself
ExamplePythonSyncService()