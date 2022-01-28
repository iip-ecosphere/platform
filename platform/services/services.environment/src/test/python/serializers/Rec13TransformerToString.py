from TypeTransatorToString import TypeTransatorToString
import Registry
from json import JSONEncoder
import json
from datatypes.Rec13 import Rec13

class Rec13TransformerToString(TypeTransatorToString):
    """Rec13<->JSON transformer."""

    class Rec13Encoder(JSONEncoder):
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
        Registry.serializers["Rec13"] = self
        Registry.types[Rec13] = "Rec13"

    def readFrom(self, data: str):
        result = Rec13()
        result.__dict__ = json.loads(data)
        return result

    def writeTo(self, source) -> str:
        return Rec13Encoder().encode(source).encode("UTF-8")

Rec13TransformerToString()