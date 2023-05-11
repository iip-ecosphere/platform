import Registry
import json
import traceback
import time

"""Method to run tests based on a Service ID, a dataObject and the type of the dataobject"""
def runTestsFromFile(sId, d, dtype):
    print(type(d))
    result = ""
    funcId = sId+"_"+dtype
    func = Registry.asyncTransformers.get(funcId)
    startTime = time.perf_counter()
    if func:
        func(d) #ingestor takes result
        print(func)
        updateResponseTime(startTime)
    else:
        func = Registry.syncTransformers.get(funcId)
        updateResponseTime(startTime)
        if func:
            result = (func(d))
            print(func)
    return result

"""A method just to create data objects from defined testfiles"""
def serializeDataFromTestFile(rawData, dtype):
    """Needed some preprocessing as the first "json.loads" messes up the structure for the one used by the serializer"""
    data = str(rawData[list(rawData)[0]]).replace('\'', "\"")
    
    result = ""
    serializer = Registry.serializers.get(dtype)
    print("got serializer", serializer)
    if serializer:
        result = serializer.readFrom(str(data)) #d is a concrete Object of the needed type!
    
    return result

"""Runs a single test based on one data point form a test file"""
def runTestsFromTestFile(sId, rawData):
    dtype = list(rawData)[0]
    dtype = dtype[0].upper() + dtype[1:]#Python serializers seem to register with uppercase, test data files assume lower case keys!(Seems to be fine in java
    dataPoint = serializeDataFromTestFile(rawData, dtype)
    """might be able to get by without dtype IF we can utilise the output of type(dataPoint) by correctly splitting and extracting the type
    Unsure IF there is a secure way to split this to always get the needed element!"""
    result = runTestsFromFile(sId, dataPoint, dtype) 
    print(result)

"""Method running all Tests of a testFile in one go"""
def runAllTestsFromFile(sId, path):
    rawData = readTestDataJson(path)
    runTestsFromTestFile(sId, rawData)

"""Reads the datapoints form a file and stores them in a array
Due to the way the data is returned it CANNOT be used for json.loads again without exchanging all ' with "
"""
def readTestDataJson(path):
        objects = []
        try :
            with open (path, "r") as f:
                for line in f: #This is needed to make the json.loader handle multiple top level objects
                    rawData = json.loads(line) #Needs "loads" instead of "load" -> load has issues with strings
                    objects.append(rawData)

                    #print(line)
        except:
            traceback.print_exc()
            
            print("Could not load the object json properly!") 
        return objects