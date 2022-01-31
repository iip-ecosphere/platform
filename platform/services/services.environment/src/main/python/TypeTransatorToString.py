class TypeTransatorToString:
    """String representation serializer interface."""

    def readFrom(self, data: str):
        """Turns a string representation into an object.
        
        Parameters:
          - data -- the string representation
        Returns:
          object
            the deserialized object
        """
        raise NotImplementedError

    def writeTo(self, source) -> str:
        """Turns an object into a string representation.
        
        Parameters:
          - source -- the object
        Returns:
          bytes
            the serialized string representation
        """
        raise NotImplementedError

