import unittest
import sys
import os
import argparse
from TestUtils import runTestsFromFile, runTestsFromTestFile, readTestDataJson, runAllTestsFromFile, getListOfDeserializedData
from Service import ServiceState
import json
from ExamplePythonVenvService import ExamplePythonVenvService
from datatypes.PythonVenvTestInput import PythonVenvTestInput
from datatypes.PythonVenvTestInputImpl import PythonVenvTestInputImpl
from datatypes.PythonVenvTestOutput import PythonVenvTestOutput
from datatypes.PythonVenvTestOutputImpl import PythonVenvTestOutputImpl
from serializers.PythonVenvTestInputSerializer import PythonVenvTestInputSerializer

os.chdir(os.environ['PRJ_HOME'] + "/src/main/python")

class ExamplePythonVenvServiceTest(unittest.TestCase):
    def test_inputTest(self):
        # Change name to correct .json, access rawData for values i.e. rawData["InputType"]["Value1"]
        rawData = ""
        service = ExamplePythonVenvService()
        service.setState(ServiceState.STARTING)
        service.setState(ServiceState.RUNNING)
        serializerPythonVenvTestInput = PythonVenvTestInputSerializer()
        service.attachIngestor(assertionIngestor)
        print(os.environ['PRJ_HOME'] + "/src/test/resources/testData-ExamplePythonVenvService.json")
        runAllTestsFromFile(service.getId(), os.environ['PRJ_HOME'] + "/src/test/resources/testData-ExamplePythonVenvService.json")
        
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
