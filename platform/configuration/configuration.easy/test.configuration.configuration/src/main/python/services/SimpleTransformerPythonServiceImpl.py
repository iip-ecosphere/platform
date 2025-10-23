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
        print('PYTHON-SERVICE:\tThe method processPythonTestInput is working.')
        result = PythonTestOutputImpl()
        result.setId(data.getId())
        result.setValue1(data.getValue1())
        result.setValue2(data.getValue2())
        result.setConfidence(random.uniform(0, 1))
        result.setPrediction(result.getConfidence() > 0.75)
        
        self.ingest(result) # asynchronous processing, call ingest from parent       

#registers itself
ExamplePythonService()
