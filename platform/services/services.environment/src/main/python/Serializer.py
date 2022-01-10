class Serializer:
    """Transport serializer interface."""

    def readFrom(self, data: bytes):
        """Turns bytes into an object.
        
        Parameters:
          - data -- the data bytes
        Returns:
          object
            the deserialized object
        """
        raise NotImplementedError

    def writeTo(self, source) -> bytes:
        """Turns an object into bytes.
        
        Parameters:
          - source -- the object
        Returns:
          bytes
            the serialized data bytes
        """
        raise NotImplementedError

