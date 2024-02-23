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
        
    def processPythonTestInput(self, data: PythonTestInput):
        """Asynchronous data processing method. Use self.ingest(data) to pass the result back to the data stream.
    
        Parameters:
          - data -- the data to process
        """
        result = PythonTestOutputImpl()
        result.id = data.id
        result.value1 = data.value1
        result.value2 = data.value2
        result.confidence = random.uniform(0, 1)
        result.prediction = result.confidence > 0.75
        
        self.ingest(result) # asynchronous processing, call ingest from parent       

#registers itself
ExamplePythonService()