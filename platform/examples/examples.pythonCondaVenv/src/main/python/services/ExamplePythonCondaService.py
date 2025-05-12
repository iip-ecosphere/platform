from datatypes.PythonCondaTestInput import PythonCondaTestInput
from datatypes.PythonCondaTestInputImpl import PythonCondaTestInputImpl
from datatypes.PythonVenvTestInput import PythonVenvTestInput
from datatypes.PythonVenvTestInputImpl import PythonVenvTestInputImpl
from interfaces.ExamplePythonCondaServiceInterface import ExamplePythonCondaServiceInterface
import random

class ExamplePythonCondaService(ExamplePythonCondaServiceInterface):
    """Example Python Conda Service implementing generated interface."""
    
    def __init__(self):
        """Initializes the service.""" 
        super().__init__()
        
    def processPythonCondaTestInput(self, data: PythonCondaTestInput):
        """Asynchronous data processing method. Use self.ingest(data) to pass the result back to the data stream.
    
        Parameters:
          - data -- the data to process
        """
        result = PythonVenvTestInputImpl()
        result.id = data.id
        result.value1 = data.value1
        result.value2 = data.value2
        result.confidence = random.uniform(0, 1)
        result.prediction = result.confidence > 0.75
        result.env1 = "Conda"
        
        self.ingest(result) # asynchronous processing, call ingest from parent       

#registers itself
ExamplePythonCondaService()