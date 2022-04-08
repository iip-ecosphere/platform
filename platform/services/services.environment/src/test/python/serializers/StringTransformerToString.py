from TypeTranslatorToString import TypeTranslatorToString
import Registry

import sys

class StringTransformerToString(TypeTranslatorToString):
    """String->String transformer."""

    def __init__(self):
        """Initializes the transformer.""" 
        Registry.serializers["S"] = self
        Registry.types[str] = "S"

    def readFrom(self, data: str):
        return data

    def writeTo(self, source) -> str:
        return source

StringTransformerToString()