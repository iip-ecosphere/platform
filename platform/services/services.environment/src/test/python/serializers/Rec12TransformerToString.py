from TypeTranslatorToString import TypeTranslatorToString
import Registry
from json import JSONEncoder
import json
from datatypes.Rec12 import Rec12

class Rec12TransformerToString(TypeTranslatorToString):
    """Rec12<->JSON transformer."""

    class Rec12Encoder(JSONEncoder):
        """JSON encoder class for Rec12."""
    
        def default(self, o):
            """Provides access to the attributes in o.
            
            Parameters:
              - o -- the object to serialize
            Returns:
              dict
                the attributes
            """
            return o.__dict__


    def __init__(self):
        """Initializes the transformer.""" 
        Registry.serializers["Rec12"] = self
        Registry.types[Rec12] = "Rec12"

    def readFrom(self, data: str):
        result = Rec12()
        result.__dict__ = json.loads(data)
        return result

    def writeTo(self, source) -> str:
        return Rec12Encoder().encode(source).encode("UTF-8")

Rec12TransformerToString()