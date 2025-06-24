import unittest
import sys
import os
import argparse
from TestUtils import runTestsFromFile, runTestsFromTestFile, readTestDataJson, runAllTestsFromFile, getListOfDeserializedData
from Service import ServiceState
import json
from ExamplePythonService import ExamplePythonService
from datatypes.PythonTestInput import PythonTestInput
from datatypes.PythonTestInputImpl import PythonTestInputImpl
from datatypes.PythonTestOutput import PythonTestOutput
from datatypes.PythonTestOutputImpl import PythonTestOutputImpl
from serializers.PythonTestInputSerializer import PythonTestInputSerializer

os.chdir(os.environ['PRJ_HOME'] + "/src/main/python")

class ExamplePythonServiceTest(unittest.TestCase):
    def test_inputTest(self):
        # Change name to correct .json, access rawData for values i.e. rawData["InputType"]["Value1"]
        rawData = ""
        service = ExamplePythonService()
        service.setState(ServiceState.STARTING)
        service.setState(ServiceState.RUNNING)
        serializerPythonTestInput = PythonTestInputSerializer()
        service.attachIngestor(assertionIngestor)
        runAllTestsFromFile(service.getId(), os.environ['PRJ_HOME'] + "/src/test/resources/testData-ExamplePythonService.json")
        
        # prepare your data, e.g., loading from json
        """ Needed to enable the loading of resources like the finished service would """
        
        # instantiate your service here, e.g., service = PyService()
        # call your service here, e.g., service.processNewInput(impl)
        
        service.setState(ServiceState.STOPPING)
        service.setState(ServiceState.STOPPED)
        
        # do your asserts here
        self.assertTrue(True)


def assertionIngestor(data):
    # add your assertions in this block, data will be your returned data
    print("output", data)

if __name__ == "__main__":
    """ Makes unittest and argparse work together """
    unittest.main(argv=["first-arg-is-ignored"], exit=False)
