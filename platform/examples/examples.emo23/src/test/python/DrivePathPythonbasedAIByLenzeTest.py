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
sys.path.insert(5, "../../main/python/sericesTF") 
sys.path.insert(4, "../../main/python")

import json

from DrivePathPythonbasedAIByLenze import DrivePathPythonbasedAIByLenze

from datatypes.LenzeDriveMeasurement import LenzeDriveMeasurement
from datatypes.LenzeDriveMeasurementImpl import LenzeDriveMeasurementImpl
from datatypes.AggregatedPlcEnergyMeasurement import AggregatedPlcEnergyMeasurement
from datatypes.AggregatedPlcEnergyMeasurementImpl import AggregatedPlcEnergyMeasurementImpl

from datatypes.DriveAiResult import DriveAiResult
from datatypes.DriveAiResultImpl import DriveAiResultImpl

from serializers.LenzeDriveMeasurementSerializer import LenzeDriveMeasurementSerializer
from serializers.AggregatedPlcEnergyMeasurementSerializer import AggregatedPlcEnergyMeasurementSerializer

from Service import ServiceState

os.chdir("../../main/python") 

class DrivePathPythonbasedAIByLenzeTest(unittest.TestCase):

    def test_inputTest(self):
    
        #Change name to correct .json, access rawData for values i.e. rawData["InputType"]["Value1"]
        rawData = ""
        service = DrivePathPythonbasedAIByLenze()
        serializerLenzeDriveMeasurement = LenzeDriveMeasurementSerializer()
        serializerAggregatedPlcEnergyMeasurement = AggregatedPlcEnergyMeasurementSerializer()
        
        service.setState(ServiceState.STARTING)
        
        service.attachIngestor(assertionIngestor)                

        runAllTestsFromFile(service.getId(), "../../test/resources/testData-DrivePathPythonbasedAIByLenze.json")
        
        # prepare your data, e.g., loading from json
        """Needed to enable the loading of resources like the finished service would"""

        # instantiate your service here, e.g., service = PyService()
        # call your service here, e.g., service.processNewInput(impl)
     
        # do your asserts here
        self.assertTrue(True)

def assertionIngestor(data):
    #add your assertions in this block, data will be your returned data
    print("output", vars(data))

if __name__ == "__main__":
    """Makes unittest and argparse work together"""
    unittest.main(argv=["first-arg-is-ignored"], exit=False)
