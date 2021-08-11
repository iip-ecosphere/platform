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
    parser.add_argument('--port', dest='port', action='store', nargs=1, type=int, required=True, help='The TCP port.')
    parser.add_argument('--protocol', dest='protocol', action='store', nargs=1, type=str, required=False,
                        default="VAB-TCP",
                        help='The implementation protocol (see AasFactory).')
    args = parser.parse_args()

    builder = VabIipOperationsBuilder()
    for service in services:
        mapService(builder, service)

    port = args.port[0]
    if args.protocol == "" or args.protocol == "VAB-TCP":
        server = BaSyxTCPServer(builder, port)
        # other protocols would go into here
    else:
        logger.info("Protocol '" + args.protocol + "' unknown. Using default.")
        server = BaSyxTCPServer(builder, port)

    server.start()
