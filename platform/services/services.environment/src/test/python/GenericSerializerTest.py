import unittest
import json
from GenericSerializer import GenericSerializer
from DataTypes import Rec1

class GenericSerializerTest(unittest.TestCase):

    def test_Rec1Serializer(self):

        rec1 = Rec1()
        parameters = {"intField" : 1, "stringField": "a"}
        rec1.setParameters(parameters)

        rec1_serializer = GenericSerializer(Rec1)
        rec1_serialized = rec1_serializer.writeTo(rec1)

        # Is returned object bytes?
        assert type(rec1_serialized) == bytes

        # Are values of parameters the same?
        # Converting the returned bytes object to a dictionary.
        rec1_serialized_dict = json.loads(rec1_serialized.decode('utf-8'))
        assert rec1_serialized_dict['intField'] == parameters['intField']
        assert rec1_serialized_dict['stringField'] == parameters['stringField']

