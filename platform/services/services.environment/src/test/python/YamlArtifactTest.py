import unittest

# there might be a better/official way :/
import sys
sys.path.insert(1, '../../main/python')

from Service import ServiceKind
from YamlArtifact import YamlArtifact
from YamlArtifact import YamlService

class YamlArtifactTest(unittest.TestCase):
    """A simple test case for YamlArtifact and YamlService."""
    
    def test_yamlArtifact(self):
        """Tests YamlArtifact and YamlService."""

        art = YamlArtifact("../resources/deployment.yml")
        assert art.getId() == "art"
        assert art.getName() == "simpleStream.spring"
        assert not (art.getVersion() is None) and art.getVersion().toString() == "0.1.9"
        assert len(art.getServices()) == 2
        
        s = art.getServices()[0];
        assert s.getId() == "simpleStream-create"
        assert s.getName() == "create"
        assert not (s.getVersion() is None) and s.getVersion().toString() == "0.2.0"
        assert s.getDescription() == "Creates text tokens."
        assert s.getKind() == ServiceKind.SOURCE_SERVICE
        assert s.isDeployable()
        
        # we omit the second service 
    
if __name__ == '__main__':
    unittest.main()