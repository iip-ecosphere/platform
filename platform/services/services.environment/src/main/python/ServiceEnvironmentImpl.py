import sys

import logging as logger
logger.basicConfig(level="DEBUG")
import argparse
import importlib
import time
import os
import json
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
    parser.add_argument('--modulesPath', dest='modulesPath', action='store', nargs=1, type=str, required=False, 
        default=str(os.getcwd()), help='The parent folder with the services, serializes and interfaces package to import.')
    parser.add_argument('--servicesPackage', dest='servicesPackage', action='store', nargs=1, type=str, required=False, 
        default="services", help='The package with service modules to import (default "services").')
    parser.add_argument('--serializersPackage', dest='serializersPackage', action='store', nargs=1, type=str, 
        required=False, default="serializers", help='The package with serializer modules to import (default "serializers").')
    parser.add_argument('--interfacesPackage', dest='interfacesPackage', action='store', nargs=1, type=str, 
        required=False, default="interfaces", help='The package with service interfaces modules to import (default "interfaces").')
    parser.add_argument('--datatypesPackage', dest='datatypesPackage', action='store', nargs=1, type=str, 
        required=False, default="datatypes", help='The package with datatype modules to import (default "datatypes").')
    parser.add_argument('--mode', dest='mode', action='store', nargs=1, type=str, required=False,
        default="", help='The operation mode of the environment (values: console, default: console).')    
    parser.add_argument('--sid', dest='sId', action='store', nargs=1, type=str, required=True,
        default="", help='Id of the service to execute.')    
    parser.add_argument('--configure', dest='config', action='store', nargs=1, type=str, required=False,
        default="", help='JSON value map to be passed to the service for 'reconfiguration'.')    
        
    args = parser.parse_args(a)

    modulesPath = args.modulesPath[0]
    sys.path.append(modulesPath)
    # dynamically load (generated) modules
    loadModules(modulesPath, args.datatypesPackage)
    loadModules(modulesPath, args.serializersPackage)
    loadModules(modulesPath, args.interfacesPackage)
    loadModules(modulesPath, args.servicesPackage)

    #sys.stderr.write("services:         " + str(Registry.services)+"\n")
    #sys.stderr.write("types:            " + str(Registry.types)+"\n")
    #sys.stderr.write("serializers:      " + str(Registry.serializers)+"\n")
    #sys.stderr.write("receivers:        " + str(Registry.receivers)+"\n")
    #sys.stderr.write("senders:          " + str(Registry.senders)+"\n")
    #sys.stderr.write("asyncTransformers:" + str(Registry.asyncTransformers)+"\n")
    #sys.stderr.write("syncTransformers: " + str(Registry.syncTransformers)+"\n")
    #sys.stderr.write("sid: " + str(args.sId)+"\n")
    
    sId = args.sId[0]

    if len(args.config) > 0:
        service = Registry.services[sId]
        service.reconfigure(json.loads(data))

    if len(args.mode) > 0 and args.mode[0]=='console':
        console(a, args.data, sId)
    else:
        console(a, args.data, sId)

def loadModules(modulesPath, modulesDir):
    if len(modulesDir) > 0:
        path = modulesPath + "/" + modulesDir
        files = os.listdir(path)
        for f in files:
            split = os.path.splitext(f)
            if split[1] == ".py":
                moduleName = modulesDir + "." + split[0]
                #sys.stderr.write("loading " + moduleName + " in " + path + "\n")
                importlib.import_module(moduleName)

def consoleIngestResult(data): 
    result = None
    typeInfo = Registry.types[type(data)]
    if typeInfo is not None:
        serializer = Registry.serializers[typeInfo]
        if serializer is not None:
            result = typeInfo + "|" + serializer.writeTo(data)    
            print(result)
            sys.stdout.flush()

def console(a, data, sId):
    """ Starts the command line based service environment
    
    Parameters:
      - a -- the program arguments as array (for testing, not just command line arguments)
      - data -- the data string to operate on in synchronous data processing"""

    # register ingestors for types/services
    for (symb,s) in Registry.services.items():
        s.attachIngestor(consoleIngestResult)

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
    tmp = composedData.split(sep="|")
    if len(tmp) > 1:
        type = tmp[0]
        data = tmp[1]
        
        if type.startswith('*'):
            service = Registry.services[sId]
            if type == '*migrate':
                service.migrate(data)
            elif type == '*update':
                service.update(data)
            elif type == '*switch':
                service.switchTo(data)
            elif type == '*recfg':
                service.reconfigure(json.loads(data))
            elif type == '*activate':
                service.activate()
            elif type == '*passivate':
                service.passivate()
        else :
            serializer = Registry.serializers[type]
            if serializer is not None:
                d = serializer.readFrom(str(data))
                func = Registry.asyncTransformers[sId+"_"+type]
                if func is not None:
                    func(d) #ingestor takes result
                else:
                    func = Registry.syncTransformers[sId]
                    if func is not None:
                        consoleIngestResult(func(d))
        