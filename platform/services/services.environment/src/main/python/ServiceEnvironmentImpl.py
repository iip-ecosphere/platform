import sys

import logging as logger
logger.basicConfig(level="DEBUG")
import argparse
import importlib
import time
import os
import Registry

# for command line version of service environment: do not emit anything to system out rather than intended results

def start():
    """ Starts the service environment by parsing the command line arguments """
    start(sys.argv)

def start(a):
    """ Starts the service environment by parsing the given arguments
    
    Parameters:
      - a -- the program arguments as array (for testing, not just command line arguments)"""

    # parse command line arguments
    parser = argparse.ArgumentParser(description='IIP-Ecosphere Python Service Environment')
    parser.add_argument('--data', dest='data', action='store', nargs=1, type=str, required=False, 
        help='Data to be processed in synchronous mode')
    parser.add_argument('--services', dest='services', action='store', nargs=1, type=str, required=False, 
        default="", help='The parent folder with the service package to import.')
    parser.add_argument('--servicesPackage', dest='servicesPackage', action='store', nargs=1, type=str, required=False, 
        default="services", help='The package with service modules to import (default "services").')
    parser.add_argument('--serializers', dest='serializers', action='store', nargs=1, type=str, required=False, 
        default="", help='The parent folder with serializer package to import.')
    parser.add_argument('--serializersPackage', dest='serializersPackage', action='store', nargs=1, type=str, 
        required=False, default="serializers", help='The package with serializer modules to import (default "serializers").')
    parser.add_argument('--mode', dest='mode', action='store', nargs=1, type=str, required=False,
        default="", help='The operation mode of the environment (values: console, default: console).')    
    #parser.add_argument('--sources', dest='sources', action='store', nargs=1, type=str, required=True,
    #    default="", help='Service ids of services taking inputs.')    
    #parser.add_argument('--sinks', dest='sinks', action='store', nargs=1, type=str, required=True,
    #    default="", help='Service ids of services producing outputs.')    
        
    args = parser.parse_args(a)

    # dynamically load (generated) serializer modules
    if len(args.serializers) > 0:
        files = os.listdir(args.serializers[0] + "/" + args.serializersPackage)
        sys.path.append(args.serializers[0])
        for f in files:
            f = os.path.splitext(f)[0]
            #sys.stderr.write("loading serializer " + f + "\n")
            importlib.import_module(f, package=args.serializersPackage)
        
    # dynamically load (generated) service modules
    if len(args.services) > 0:
        files = os.listdir(args.services[0] + "/" + args.servicesPackage)
        sys.path.append(args.services[0])
        for f in files:
            f = os.path.splitext(f)[0]
            #sys.stderr.write("loading service " + f + "\n")
            importlib.import_module(f, package=args.servicesPackage)

    #configure services for ingestion
    sId = ""

    if len(args.mode) > 0 and args.mode[0]=='console':
        console(a, args.data, sId)
    else:
        console(a, args.data, sId)

def console(a, data, sId):
    """ Starts the command line based service environment
    
    Parameters:
      - a -- the program arguments as array (for testing, not just command line arguments)
      - data -- the data string to operate on in synchronous data processing"""

    if data is not None:
        # for now: just forward once
        process(data[0], sId)
    else:
        # for now: receive and forward
        while True:
            try:
                process(input(), sId)
            except EOFError as e:
                break

## further modes go here

def process(composedData, sId):
    #tmp = composedData.split(sep="|")
    #if len(tmp) > 1:
    #    type = tmp[0]
    #    data = tmp[1]
    #    
    #    serializer = Registry.serializers[type]
    #    if serializer is not None:
    #        d = serializer.readFrom(data)
    #        service = Registry.services[sId]
    #        if service is not None:
    #            service.process(d)
        
    #compose
    print(composedData)
    sys.stdout.flush()