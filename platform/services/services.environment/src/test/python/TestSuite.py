import unittest

from YamlArtifactTest import YamlArtifactTest

def suite():
    """The test suite for services.environment. It might be surprising that
    there are only a few tests, but most of the scripts/classes are tested 
    in an integration test with the Java side running the Python side as 
    server. However, more unit tests are of course welcome."""
    suite = unittest.TestSuite()
    suite.addTest(YamlArtifactTest())
    return suite

if __name__ == '__main__':
    suite = unittest.defaultTestLoader.loadTestsFromTestCase(YamlArtifactTest)
    unittest.TextTestRunner(verbosity=3).run(suite)