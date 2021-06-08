import logging as logger
logger.basicConfig(level="DEBUG")
import yaml
from Version import Version
from Service import ServiceKind 

class YamlService:
    """Represents a service as read from a YAML descriptor. Can be used to initialize an 
    administrative service."""

    def __init__(self, id, name, version:Version, description, kind:ServiceKind, deployable):
        self.id = id
        self.name = name
        self.version = version
        self.description = description
        self.kind = kind
        self.deployable = deployable
        
    def getId(self):
        """Returns the unique id of the service.
        
        Returns:
          str
            The id of the service.
        """ 

        return self.id

    def getName(self):
        """Returns the name of the service.
        
        Returns:
          str
            The name of the service.
        """ 
        
        return self.name

    def getDescription(self):
        """Returns the description of the service.
        
        Returns:
          str
            The description of the service, may be empty.
        """
        
        return self.description

    def getVersion(self) -> Version:
        """Returns the version of the service.
        
        Returns:
          Version
            The version of the service.
        """
        
        return self.version

    def getKind(self) -> ServiceKind:
        """Returns the service kind.
        
        Returns:
          ServiceKind
            The service kind.
        """
        
        return self.kind

    def isDeployable(self):
        """Returns whether the service is deployable in distributable manner or fixed in deployment location.
        
        Returns:
          bool
            Whether the state is deployable.
        """
        
        return self.deployable

class YamlArtifact:
    """Represents an artifact as read from a YAML service descriptor."""

    def __init__(self, file):
        """Creates an instance by reading relevant information from file.
    
        Parameters:
          - file -- the file to read from
        """

        f = open(file)
        dict = yaml.load(f, Loader=yaml.FullLoader)
        self.id = dict.get("id")
        self.name = dict.get("name")
        self.version = Version(dict.get("version"))
        services = dict.get("services")
        self.services = []
        for s in services:
            id = s.get("id")
            name = s.get("name")
            version = Version(s.get("version"))
            description = s.get("description")
            kind = ServiceKind[s.get("kind")]
            deployable = s.get("deployable")
            self.services.append(YamlService(id, name, version, description, kind, deployable))
        f.close()
    
    def getId(self):
        """Returns the unique id of the artifact.
        
        Returns:
          str
            The id of the artifact.
        """ 
        
        return self.id

    def getName(self):
        """Returns the name of the artifact.
        
        Returns:
          str
            The name of the artifact.
        """ 
        
        return self.name

    def getVersion(self) -> Version:
        """Returns the version of the artifact.
        
        Returns:
          Version
            The version of the artifact.
        """
        
        return self.version

    def getServices(self):
        """Returns the services in terms of YamlService instances.
        
        Returns:
          [YamlService]
            The services contained in the artifact.
        """
        
        return self.services