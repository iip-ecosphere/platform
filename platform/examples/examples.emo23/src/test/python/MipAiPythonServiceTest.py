import unittest

import sys
import os
import argparse

sys.path.insert(6, "../../../target/pySrc/iip")
sys.path.insert(7, "../../../target/pySrc")
from TestUtils import runTestsFromFile, runTestsFromTestFile, readTestDataJson, runAllTestsFromFile, getListOfDeserializedData
""" will be the relativve paths from impl.impl to impl.model, given through pom /gen onwards! """
sys.path.insert(1, "../../../gen/hm23/ApplicationInterfaces/src/main/python")
sys.path.insert(2, ("../../../gen/hm23/ApplicationInterfaces/src/main/python/iip"))
""" always fixed value as these directorys are generated in the impl.impl """
sys.path.insert(4, "../../main/python")

from Service import ServiceState
import json

from servicesMip.MipAiPythonService import MipAiPythonService

from datatypes.MipMqttOutput import MipMqttOutput
from datatypes.MipMqttOutputImpl import MipMqttOutputImpl

from datatypes.MipAiPythonOutput import MipAiPythonOutput
from datatypes.MipAiPythonOutputImpl import MipAiPythonOutputImpl
from datatypes.MipMqttInput import MipMqttInput
from datatypes.MipMqttInputImpl import MipMqttInputImpl

from serializers.MipMqttOutputSerializer import MipMqttOutputSerializer


os.chdir("../../main/python") 

class MipAiPythonServiceTest(unittest.TestCase):

    def test_inputTest(self):
    
        #Change name to correct .json, access rawData for values i.e. rawData["InputType"]["Value1"]
        rawData = ""
        service = MipAiPythonService()

        service.setState(ServiceState.STARTING)
        service.setState(ServiceState.RUNNING)
        print("Pre Serializer from file")
        serializerMipMqttOutput = MipMqttOutputSerializer()
        
        
        service.attachIngestor(assertionIngestor) 

        runAllTestsFromFile(service.getId(), "../../test/resources/testData-MipAiPythonService.json")
        
        # prepare your data, e.g., loading from json
        """Needed to enable the loading of resources like the finished service would"""

        # instantiate your service here, e.g., service = PyService()
        # call your service here, e.g., service.processNewInput(impl)

        service.setState(ServiceState.STOPPING)
        service. setState(ServiceState.STOPPED)	            

        # do your asserts here
        self.assertTrue(True)

def assertionIngestor(data):
    #add your assertions in this block, data will be your returned data
    serializerMipMqttOutput = MipMqttOutputSerializer()
    print("output", serializerMipMqttOutput.writeTo(data))

if __name__ == "__main__":
    """Makes unittest and argparse work together"""
    unittest.main(argv=["first-arg-is-ignored"], exit=False)
