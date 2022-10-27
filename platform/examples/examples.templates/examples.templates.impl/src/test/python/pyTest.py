import unittest

import sys
import os
import argparse
parser = argparse.ArgumentParser()
parser.add_argument("relModelPath", help="relative path to the impl.model project")
args = parser.parse_args()

"""Further Notes: We should auto generate one template for each possible python service and make them callable separately 
through maven -->> we shall generate one maven execution command like we did for the python test for each pyService!"""

""" Paths are temporary set but seem to work like this paths can only be hardcoded from the /gen onwards! """
""" Temp as reminder! :../../../../examples.templates.model """
sys.path.insert(1, (args.relModelPath + '/gen/test/ApplicationInterfaces/src/main/python'))
sys.path.insert(2, (args.relModelPath + '/gen/test/ApplicationInterfaces/src/main/python/iip'))
""" always fixed value as these directorys are generated in the impl.impl """
sys.path.insert(3, '../../main/python/services') 

import json

from PyService import PyService
from datatypes.NewInputImpl import NewInputImpl


class TestPyServive(unittest.TestCase):
    
    def test_inputTest(self):

        rawData = ''
        with open ('../resources/testData-PyService.json', 'r') as f:
            rawData = json.load(f)
        impl = NewInputImpl()
        impl.setType(rawData['newInput']['type'])
        impl.setAirTemp(rawData['newInput']['airTemp'])
        impl.setProcTemp(rawData['newInput']['procTemp'])
        impl.setRotSpe(rawData['newInput']['rotSpe'])
        impl.setTorq(rawData['newInput']['torq'])
        impl.setToolWear(rawData['newInput']['toolWear'])
        
        """Quick fix for the wrong placement of the class (positioning would not allow for loading of resources otherwise)"""
        os.chdir('../../main/python') 
        
        service = PyService()
        service.processNewInput(impl)
        """ processNewInput does not return anything not sure what to assert"""
        self.assertTrue(True)

if __name__ == "__main__":
    """ actually dont know what this does..."""
    unittest.main(argv=['first-arg-is-ignored'], exit=False)

