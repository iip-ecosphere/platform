import unittest

import sys
import os
import argparse

sys.path.insert(6, "../../../target/pySrc/iip")
sys.path.insert(7, "../../../target/pySrc")
from TestUtils import runTestsFromFile, runTestsFromTestFile, readTestDataJson, runAllTestsFromFile
""" will be the relativve paths from impl.impl to impl.model, given through pom /gen onwards! """
sys.path.insert(1, "../../../gen/hm23/ApplicationInterfaces/src/main/python")
sys.path.insert(2, ("../../../gen/hm23/ApplicationInterfaces/src/main/python/iip"))
""" always fixed value as these directorys are generated in the impl.impl """
sys.path.insert(3, "../../main/python/services") 
sys.path.insert(5, "../../main/python/servicesTF") 
sys.path.insert(4, "../../main/python")

import json

from PythonbasedAI import PythonbasedAI
from datatypes.ImageInput import ImageInput
from datatypes.ImageInputImpl import ImageInputImpl
from datatypes.Command import Command
from datatypes.CommandImpl import CommandImpl

from Service import ServiceState
from datatypes.AiResult import AiResult
from datatypes.AiResultImpl import AiResultImpl
from serializers.ImageInputSerializer import ImageInputSerializer
from serializers.CommandSerializer import CommandSerializer

os.chdir("../../main/python") 

class PythonbasedAITest(unittest.TestCase):

    def test_inputTest(self):
    
        #Change name to correct .json, access rawData for values i.e. rawData["InputType"]["Value1"]
        rawData = ""
        service = PythonbasedAI()
        
        service.setState(ServiceState.STARTING)
        service.setState(ServiceState.RUNNING)
        
        serializerImageInput = ImageInputSerializer()
        serializerCommand = CommandSerializer()
        
        service.attachIngestor(assertionIngestor)
        
        runAllTestsFromFile(service.getId(), "../../test/resources/testData-PythonbasedAI.json")
        
        # prepare your data, e.g., loading from json
        """Needed to enable the loading of resources like the finished service would"""

        # instantiate your service here, e.g., service = PyService()
        # call your service here, e.g., service.processNewInput(impl)
        service.setState(ServiceState.STOPPING)
        service. setState(ServiceState.STOPPED)
        # do your asserts here
        self.assertTrue(True)

def assertionIngestor(data):
    print("Asserted!", data)

if __name__ == "__main__":
    """Makes unittest and argparse work together"""
    unittest.main(argv=["first-arg-is-ignored"], exit=False)
