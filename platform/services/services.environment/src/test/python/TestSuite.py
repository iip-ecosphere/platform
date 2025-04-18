import unittest

from VersionTest import VersionTest
from YamlArtifactTest import YamlArtifactTest
from ForwardingAppTest import ForwardingAppTest
from GenericSerializerTest import Rec1SerializerTest
from ServiceEnvironmentTest import CmdLineServiceEnvironmentTest

def suite():
    """The test suite for services.environment. It might be surprising that
    there are only a few tests, but most of the scripts/classes are tested 
    in an integration test with the Java side running the Python side as 
    server. However, more unit tests are of course welcome."""
    suite = unittest.TestSuite()
    suite.addTest(VersionTest())
    suite.addTest(YamlArtifactTest())
    suite.addTest(ForwardingAppTest())
    suite.addTest(Rec1SerializerTest())
    suite.addTest(CmdLineServiceEnvironmentTest())
    return suite

if __name__ == '__main__':
    exSuite = unittest.defaultTestLoader.loadTestsFromTestCase(VersionTest)
    unittest.TextTestRunner(verbosity=3).run(exSuite)

    exSuite = unittest.defaultTestLoader.loadTestsFromTestCase(YamlArtifactTest)
    unittest.TextTestRunner(verbosity=3).run(exSuite)

    exSuite = unittest.defaultTestLoader.loadTestsFromTestCase(ForwardingAppTest)
    unittest.TextTestRunner(verbosity=3).run(exSuite)

    exSuite = unittest.defaultTestLoader.loadTestsFromTestCase(Rec1SerializerTest)
    unittest.TextTestRunner(verbosity=3).run(exSuite)
    
    exSuite = unittest.defaultTestLoader.loadTestsFromTestCase(CmdLineServiceEnvironmentTest)
    unittest.TextTestRunner(verbosity=3).run(exSuite)    
    