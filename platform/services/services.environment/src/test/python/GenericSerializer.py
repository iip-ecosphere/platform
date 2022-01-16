from json import JSONEncoder
import json
import sys
import os

# Importing module from directory 'main'
cwd = os.getcwd()
main_dir = cwd.replace('test', 'main')
sys.path.insert(0, main_dir)

from Serializer import Serializer

class GenericSerializer(Serializer):
    """ Provides methods to:
    - translate an instance of a given class to a bytes type
    - extracts a instance of a given class from a bytes type
    """

    def __init__(self, cls):
        self.cls = cls  # a class to be serialized

    class Encoder(JSONEncoder):

        def default(self, o):
            """Provides access to the attributes in o.

            Parameters:
              - o -- the object to serialize
            Returns:
              dict
                the attributes
            """
            return o.__dict__

    def readFrom(self, data: bytes):
        """Turns bytes into an instance of given class.

        Parameters:
          - data -- the data bytes
        Returns:
          object
            the deserialized object
        """

        result = self.cls()
        result.setParameters(json.loads(data))
        return result

    def writeTo(self, source) -> bytes:
        """Turns an instance of a given class into bytes.

        Parameters:
          - source -- the object
        Returns:
          bytes
            the serialized data bytes
        """
        json_obj = GenericSerializer.Encoder().encode(source)
        b = json_obj.encode("UTF-8")
        return b
