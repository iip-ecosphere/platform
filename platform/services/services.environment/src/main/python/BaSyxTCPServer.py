import socket
import logging as logger
logger.basicConfig(level="DEBUG")
import argparse
from VABBaSyxTCPInterface import VABBaSyxTCPInterface

HOST = '127.0.0.1'  # Standard loopback interface address (localhost)

class BaSyxTCPServer:
    """Corresponding class representing a BaSyx TCP VAB server"""

    def __init__(self, builder, port):
        """Initializes the server.
        
        Parameters:
           - builder -- service builder (VabIipOperationsBuilder) 
           - port -- the network port to run the server on
        """
        self.vabInterface = VABBaSyxTCPInterface(builder)
        self.port = port

    def start(self):
        """Starts the server."""
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.bind((HOST, self.port))
            logger.info('Bound to ' + HOST + ':' + str(self.port))
            while True:
                s.listen()
                conn, addr = s.accept()
                with conn:
                    #while True: # this would be typical, but BaSyx closes each connection
                    self.vabInterface.parse(conn, addr)