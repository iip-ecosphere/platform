from datatypes.PythonVenvTestInput import PythonVenvTestInput
from datatypes.PythonVenvTestInputImpl import PythonVenvTestInputImpl
from datatypes.PythonVenvTestOutput import PythonVenvTestOutput
from datatypes.PythonVenvTestOutputImpl import PythonVenvTestOutputImpl
from interfaces.ExamplePythonVenvServiceInterface import ExamplePythonVenvServiceInterface
import random

class ExamplePythonVenvService(ExamplePythonVenvServiceInterface):
    """Example Python Venv Service implementing generated interface."""
    
    def __init__(self):
        """Initializes the service.""" 
        super().__init__()
        
    def processPythonVenvTestInput(self, data: PythonVenvTestInput):
        """Asynchronous data processing method. Use self.ingest(data) to pass the result back to the data stream.
    
        Parameters:
          - data -- the data to process
        """
        result = PythonVenvTestOutputImpl()
        result.id = data.id
        result.value1 = data.value1
        result.value2 = data.value2
        result.confidence = random.uniform(0, 1)
        result.prediction = result.confidence > 0.75
        result.env1 = "Conda"
        result.env2 = "Venv"
        
        self.ingest(result) # asynchronous processing, call ingest from parent       

#registers itself
ExamplePythonVenvService()