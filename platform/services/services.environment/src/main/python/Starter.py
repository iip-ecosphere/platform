import logging as logger
logger.basicConfig(level="DEBUG")
import argparse
from BaSyxTCPServer import BaSyxTCPServer

from VabIipOperationsBuilder import VabIipOperationsBuilder
from ServiceMapper import mapService

def start(services):
    """Starts the Python Java framework for the given instances of administratove service wrappers.
    
    Parameters:
      services -- list of Service instances
    """ 
    
    parser = argparse.ArgumentParser(description='VAB/TCP Server')
    parser.add_argument('--port', dest='port', action='store', nargs=1, type=int, required=True, help='The TCP port')
    args = parser.parse_args()
    
    #preliminary
    
    builder = VabIipOperationsBuilder()
    for service in services:
        mapService(builder, service)

    # further args may go for data server port, VAB protocol (TCP/HTTP-REST)
    server = BaSyxTCPServer(builder, args.port[0])
    server.start()
