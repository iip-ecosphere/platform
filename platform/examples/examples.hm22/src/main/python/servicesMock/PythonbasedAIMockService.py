import sys
from datatypes.ImageInput import ImageInput
from datatypes.ImageInputImpl import ImageInputImpl
from datatypes.AiResult import AiResult
from datatypes.AiResultImpl import AiResultImpl
from datatypes.Command import Command
from datatypes.CommandImpl import CommandImpl
from interfaces.PythonbasedAIInterface import PythonbasedAIInterface
import random

class PythonbasedAIMockService(PythonbasedAIInterface):
    """Example Python Mock Service implementing generated interface."""
    
    def __init__(self):
        """Initializes the service.""" 
        super().__init__()
        
    def processImageInput(self, data: ImageInput):
        """Asynchronous data processing method. Use self.ingest(data) to pass the result back to the data stream."""

        colors = ['blue', 'red', 'brown', 'yellow', 'green']
        result = AiResultImpl()
        result.setImage(data.getImage())
        result.setProductId(data.getProductId())

        if not data.getQrCodeDetected():
            result.setOneWindowConfidence(random.uniform(0, 1))
            result.setTwoWindowsConfidence(random.uniform(0, 1))
            result.setThreeWindowsConfidence(random.uniform(0, 1))
            result.setWheelColour(random.choice(colors))
            result.setEngraving(bool(random.getrandbits(1)))
            result.setEngravingConfidence(random.uniform(0, 1))
            result.setScratch(bool(random.getrandbits(1)))
            result.setScratchConfidence(random.uniform(0, 1))
            
        self.ingest(result)
        sys.stderr.write("PY: " + str(result)+"\n") # do not use print
    
    def processCommand(self, data: Command):
        """Asynchronous data processing method. Use self.ingest(data) to pass the result back to the data stream."""
        
        # nothing to do for now
        sys.stderr.write("PY: " + str(data)+"\n") # do not use print
    
#registers itself
PythonbasedAIMockService()