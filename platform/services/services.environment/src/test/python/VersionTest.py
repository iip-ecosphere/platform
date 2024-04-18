import unittest

# there might be a better/official way :/
import sys
sys.path.insert(1, '../../main/python')

from Version import Version

class VersionTest(unittest.TestCase):
    """A simple test case for Version."""
    
    def test_Version(self):
        """Tests Version."""

        v = Version("1")
        assert v.getSegmentCount() == 1
        assert v.toString() == "1"

        v = Version("1.22.3")
        assert v.getSegmentCount() == 3
        assert v.toString() == "1.22.3"
    
if __name__ == '__main__':
    unittest.main()