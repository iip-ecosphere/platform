import sys

import logging as logger
logger.basicConfig(level="DEBUG")
import argparse
import importlib
import time
import os
import json
import base64
from codecs import decode
import Registry
from Service import ServiceState
from Service import ServiceKind
import asyncio
import websockets
import signal
import traceback

sId = ""
avgResponseTime = 0
numberResponseTimeSamples = 0
responseFunction = None # one arg, response data in respective format; returns response or None
outStore = {}
id = 0

def updateResponseTime(startTime):
    """ Update the global average response time
    
    Parameters:
      - startTime -- start time measured by time.time()"""

    global numberResponseTimeSamples
    global avgResponseTime
    newValue = time.perf_counter() - startTime
    if numberResponseTimeSamples < 0:
        numberResponseTimeSamples = 0
        avgResponseTime = 0
    
    numberResponseTimeSamples += 1
    avgResponseTime = (avgResponseTime * (numberResponseTimeSamples - 1) + newValue) / numberResponseTimeSamples 

def printStdout(text): 
    """ Use in here to write to stdout, independent whether redirected or not 
    
    Parameters:
      - text -- what to print, converted to string, newline added"""
      
    sys.__stdout__.write(str(text) + "\n")  

def flushStdout(): 
    """ Flushes original stdout """
    
    sys.__stdout__.flush()  

def printStderr(text): 
    """ Use in here to write to stderr, independent whether redirected or not
    
    Parameters:
      - text -- what to print, converted to string, newline added"""
      
    sys.__stderr__.write(str(text) + "\n")  
    
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
        default="", help='The operation mode of the environment (values: console, WS, default: console).')    
    parser.add_argument('--port', dest='port', action='store', nargs=1, type=int, required=False,
        default=9111, help='The local server port for modes like "WS".')    
    parser.add_argument('--sid', dest='sId', action='store', nargs=1, type=str, required=True,
        default="", help='Id of the service to execute.')
    parser.add_argument('--netMgtKeyAddress', dest='netMgtKeyAddress', action='store', nargs=1, type=str, required=False,
        default=None, help='Resolved address of the netMgtKey of the service via the platform network management.')    
    parser.add_argument('--configure', dest='config', action='store', nargs=1, type=str, required=False,
        default="", help='JSON value map to be passed to the service for initial reconfiguration.')
        
    args = parser.parse_args(a)
    consoleMode = len(args.mode) > 0 and args.mode[0]=='console'
    wsMode = len(args.mode) > 0 and (args.mode[0]=='WS' or args.mode[0]=='ws')
    
    global responseFunction
    if consoleMode:
        responseFunction = consoleIngestResult
        ingestorFunction = consoleIngestResult
        sys.stdout = sys.stderr
    elif wsMode:
        sys.stdout = sys.stderr
        responseFunction = wsIngestResult
        ingestorFunction = wsIngestResult

    modulesPath = getArg(args.modulesPath)
    sys.path.append(modulesPath)
    # dynamically load (generated) modules
    loadModules(modulesPath, getArg(args.datatypesPackage))
    loadModules(modulesPath, getArg(args.serializersPackage))
    loadModules(modulesPath, getArg(args.interfacesPackage))
    loadModules(modulesPath, getArg(args.servicesPackage))

    #print("services:         " + str(Registry.services))
    #print("types:            " + str(Registry.types))
    #print("serializers:      " + str(Registry.serializers))
    #print("receivers:        " + str(Registry.receivers))
    #print("senders:          " + str(Registry.senders))
    #print("asyncTransformers:" + str(Registry.asyncTransformers))
    #print("syncTransformers: " + str(Registry.syncTransformers))
    #print("sid: " + str(args.sId))
    
    global sId
    sId = args.sId[0]
    if (args.netMgtKeyAddress is not None):
        Registry.netMgtKeyAddresses[sId] = args.netMgtKeyAddress[0]

    if len(args.config) > 0:
        service = Registry.services.get(sId)
        if service:
            service.reconfigure(json.loads(args.data))

    # register ingestors for types/services
    for (symb,s) in Registry.services.items():
        s.attachIngestor(ingestorFunction)

    if consoleMode:
        console(a, args.data, sId)
    elif wsMode:
        signal.signal(signal.SIGINT, wsStop)
        asyncio.run(wsMain(args.port[0]))
    else: # currently no alternative, just use console as fallback
        console(a, args.data, sId)

def getArg(arg):
   if isinstance(arg, list):
       return arg[0]
   else:
       return arg

def loadModules(modulesPath, modulesDir):
    if len(modulesDir) > 0:
        path = modulesPath + "/" + modulesDir
        files = os.listdir(path)
        for f in files:
            split = os.path.splitext(f)
            if split[1] == ".py":
                moduleName = modulesDir + "." + split[0]
                #sys.stderr.write("loading " + moduleName + " in " + path + "\n")
                try:
                    importlib.import_module(moduleName)
                    print("Python ServiceEnvironment [Info]: Loaded " + moduleName)
                except ModuleNotFoundError as exception:
                    sys.stderr.write("Python ServiceEnvironment [Warn]: While loading " + moduleName + ": ModuleNotFoundError " + str(exception) + "\n")

# common for all modes

def processRequest(sId, type, data):
    result = None
    if type.startswith('*'):
        service = Registry.services.get(sId)
        if service:
            if type == '*setstate':
                service.setState(ServiceState[str(data)])
            elif type == '*migrate':
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
            elif type == '*SERVER': # fixed base64 encoding-decoding
                service.receivedClientServer(base64.b64decode(data))
        else:
            sys.stderr.write("Python ServiceEnvironment [Warn]: Cannot pass " + type + " to service - no service\n")
    else :
        try :
            serializer = Registry.serializers.get(type)
            if serializer:
                d = serializer.readFrom(str(data))
                funcId = sId+"_"+type
                func = Registry.asyncTransformers.get(funcId)
                startTime = time.perf_counter()
                if func:
                    func(d) #ingestor takes result
                    updateResponseTime(startTime)
                else:
                    func = Registry.syncTransformers.get(funcId)
                    updateResponseTime(startTime)
                    if func:
                        global responseFunction
                        responseFunction(func(d))
        except Exception as err:
            sys.stderr.write("Exception/error in service:\n")
            traceback.print_tb(err.__traceback__)  
    return result

# console mode

def consoleIngestResult(data): 
    result = None
    typeInfo = Registry.types.get(type(data))
    if typeInfo:
        serializer = Registry.serializers.get(typeInfo)
        if serializer:
            global avgResponseTime
            result = typeInfo + "|" + str(int(avgResponseTime)) + "|" + serializer.writeTo(data)
            printStdout(result)
            flushStdout()
    else: # assume client-server-communication
        result = "*SERVER|0|" + base64.b64encode(data).decode('utf-8')
        printStdout(result)
        flushStdout()

def console(a, data, sId):
    """ Starts the command line based service environment
    
    Parameters:
      - a -- the program arguments as array (for testing, not just command line arguments)
      - data -- the data string to operate on in synchronous data processing"""

    if data is not None:
        d = decode(data[0], 'unicode-escape') # unescape in particular quotes
        # for now: just forward once
        consoleProcess(d, sId)
    else:
        # for now: receive and forward
        while True:
            try:
                consoleProcess(input(), sId)
            except EOFError as e:
                break
            # prevent active waiting, sleep for 5m
            time.sleep(5/1000)

def consoleProcess(composedData, sId):
    tmp = composedData.split(sep="|")
    if len(tmp) > 1:
        type = tmp[0]
        data = tmp[1]
        processRequest(sId, type, data)
        
# WS parts

def wsIngestResult(data): 
    result = None
    typeInfo = Registry.types.get(type(data))
    if typeInfo:
        serializer = Registry.serializers.get(typeInfo)
        if serializer:
            global avgResponseTime
            result = {}
            result["type"] = typeInfo
            result["time"] = avgResponseTime
            result["data"] = serializer.writeTo(data)
    else: # assume client-server-communication
            result = {}
            result["type"] = "*SERVER"
            result["time"] = 0
            result["data"] = base64.b64encode(data).decode('utf-8')
    if result:
        asyncio.create_task(wsSend(result))
        
async def wsSend(data):        
    global websocket
    await websocket.send(json.dumps(data))

async def wsHandler(ws):
    global websocket
    websocket = ws
    while True:
        message = await websocket.recv()
        data = json.loads(message)
        if "type" in data and "data" in data:
            global sId
            result = processRequest(sId, data["type"], data["data"])
            if result:
                wsIngestResult(result)

async def wsMain(port):
    log = logger.getLogger('websockets.server')
    log.disabled = True
    print("Starting Websockets server on port " + str(port))
    async with websockets.serve(wsHandler, "127.0.0.1", port):
        await asyncio.Future()  # run forever

def wsStop(signum, frame):
    asyncio.get_event_loop().stop()