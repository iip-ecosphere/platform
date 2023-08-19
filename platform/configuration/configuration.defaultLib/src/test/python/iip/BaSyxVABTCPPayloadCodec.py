import socket
import logging as logger
logger.basicConfig(level="DEBUG")
import struct

class BaSyxVABTCPPayloadCodec:
    """The Python side of BaSyxVABTCPPayloadCodec: Encodes/decodes binary payloads. """ 

    def __init__(self):
        pass
        
    def parsePayload(self, conn):
        """Parses the payload from the given network connection.
        
        Arguments:
          - conn -- the network connection to read from
        """ 
        
        frameSize = struct.unpack('i', conn.recv(4))[0]
        data = conn.recv(frameSize)
        return self.decodePayload(data)
        
    def decodePayload(self, data):
        """Decodes the payload in data.
        
        Arguments:
          - data -- payload in binary data
          
        Returns:
          Tuple
            Descriptive information (may be None) and decoded payload
        """ 
        
        infoLen = struct.unpack('i', data[0:4])[0]
        if infoLen < 0:
            info = None
            payload = data[4:]
        else:
            info = data[4:4+infoLen]
            payload = data[4+infoLen:]
        return (info, payload)
        
    def encodePayload(self, info, payload):
        """Encodes info and payload into data.
        
        Arguments:
          - info -- descriptive, optional information (may be None)
          - payload -- the payload to be encoded
          
        Returns:
          list
            The encoded bytes.
        """ 

        if info is None:
            return struct.pack('<ii', 4 + 4 + len(payload), -1) + payload
        else:
            return struct.pack('<ii', 4 + 4 + len(info) + len(payload)) + info.encode('UTF-8') + payload

    def sendData(self, conn, info, payload):
        """Encodes info and payload and sends it to conn.
        
        Arguments:
          - conn -- network connection
          - info -- descriptive, optional information (may be None)
          - payload -- the payload to be encoded
        """ 

        bts = self.encodePayload(info, payload)
        conn.sendall(bts)
