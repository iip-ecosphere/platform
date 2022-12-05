import unittest

import sys
import os
import argparse
parser = argparse.ArgumentParser()
parser.add_argument("relModelPath", help="relative path to the impl.model project")
args = parser.parse_args()

""" will be the relativve paths from impl.impl to impl.model, given through pom /gen onwards! """
sys.path.insert(1, (args.relModelPath))
sys.path.insert(2, (args.relModelPath + "/iip"))
""" always fixed value as these directorys are generated in the impl.impl """
sys.path.insert(3, "../../main/python/services") 

import json

from PyService import PyService
from datatypes.NewInput import NewInput
from datatypes.NewInputImpl import NewInputImpl
from datatypes.NewOutput import NewOutput
from datatypes.NewOutputImpl import NewOutputImpl

class TestPyServive(unittest.TestCase):

    def test_inputTest(self):
    
        #Change name to correct .json, access rawData for values i.e. rawData["InputType"]["Value1"]
        rawData = ""
        try :
            with open ("../resources/testData-PyService.json", "r") as f:
                rawData = json.load(f)
        except:
            print("You need to edit the template files in /src/test/resources to be correct json") 


        # prepare your data, e.g., loading from json
        """Needed to enable the loading of resources like the finished service would"""
        os.chdir("../../main/python") 

        # instantiate your service here, e.g., service = PyService()
        # call your service here, e.g., service.processNewInput(impl)
     
        # do your asserts here
        self.assertTrue(True)

if __name__ == "__main__":
    """Makes unittest and argparse work together"""
    unittest.main(argv=["first-arg-is-ignored"], exit=False)
