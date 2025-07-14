import sys
# This statement asserts only integer, not fractional part.
# It means the python version has to be at least 3.0 to be true.
assert sys.version_info[0] > 2, 'Python Version needs to be higher than 2.'

import logging as logger
import argparse
from BaSyxTCPServer import BaSyxTCPServer
from BaSyxHTTPServer import BaSyxHTTPServer
from VabIipOperationsBuilder import VabIipOperationsBuilder
from ServiceMapper import mapService
from RequestProcessor import RequestProcessor
from BaSyx2RequestProcessor import BaSyx2RequestProcessor


def start(services):
    """Starts the Python Java framework for the given instances of administrative service wrappers.

    Parameters:
      services -- list of Service instances
    """

    parser = argparse.ArgumentParser(description='VAB/TCP Server')
    parser.add_argument('--port', dest='port', type=int, required=True, help='The TCP port.')
    parser.add_argument('--metaModel', dest='metaModel', type=str, required=False, 
        default="v2", help='The AAS metamodel version (see AasFactory).')
    parser.add_argument('--protocol', dest='protocol', type=str, required=False,
        default="", help='The implementation protocol (see AasFactory).')
    parser.add_argument('--log', dest='log', type=str, required=False,
        default="ERROR", help='The logging level (ERROR, WARNING, INFO, DEBUG).')
    args = parser.parse_args()

    logger.basicConfig(level=args.log)
    if args.metaModel == "v3":
        reqProc = BaSyx2RequestProcessor()
    else:
        reqProc = RequestProcessor()
    builder = VabIipOperationsBuilder()
    for service in services:
        mapService(builder, service)

    port = args.port
    protocol = args.protocol
    if protocol == "":
        mm = args.metaModel
        if mm == "v2":
            protocol = "VAB-TCP"
        elif mm == "v3":
            protocol = "AAS-REST"
    if protocol == "VAB-TCP":
        server = BaSyxTCPServer(builder, port)
    elif protocol == "VAB-HTTP":
        server = BaSyxHTTPServer(builder, port, reqProc)
    elif protocol == "AAS-REST":
        server = BaSyxHTTPServer(builder, port, reqProc)
    else:
        logger.info("Protocol '" + protocol + "' unknown. Using default.")
        server = BaSyxTCPServer(builder, port)

    server.start()
