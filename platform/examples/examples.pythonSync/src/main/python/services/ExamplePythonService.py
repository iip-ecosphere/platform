from datatypes.PythonTestInput import PythonTestInput
from datatypes.PythonTestInputImpl import PythonTestInputImpl
from datatypes.PythonTestOutput import PythonTestOutput
from datatypes.PythonTestOutputImpl import PythonTestOutputImpl
from interfaces.ExamplePythonServiceInterface import ExamplePythonServiceInterface
import random

class ExamplePythonService(ExamplePythonServiceInterface):
    """Example Python Service implementing generated interface."""
    
    def __init__(self):
        """Initializes the service.""" 
        super().__init__()
        
    def transformPythonTestInput(self, data: PythonTestInput) -> PythonTestOutput:
        """Synchronous data processing/transformation method.
    
        Parameters:
          - data -- the data to process
        Returns:
          the processed data              
        """
        result = PythonTestOutputImpl()
        result.id = data.id
        result.value1 = data.value1
        result.value2 = data.value2
        result.confidence = random.uniform(0, 1)
        result.prediction = result.confidence > 0.75
        
        return result # synchronous processing

#registers itself
ExamplePythonService()