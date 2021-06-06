import logging as logger
logger.basicConfig(level="DEBUG")

from Service import ServiceState
from Service import ServiceKind
from VabIipOperationsBuilder import composeResult

def getQName(name, service):
    """Maps the given operation/property name for the given service to a qualified name.
    
    Parameters:
      name -- the name of the property/operation 
      service -- the service (context) the property/operation belongs to
    Returns:
      str
        the (qualified) name
    """ 
    
    return name
    #TODO return service.getId() + "_" + name

def mapService(builder, service):
    """Maps know functions of a service to functions and registers them appropriately with builder.
    
    Parameters:
      - builder the service builder (VabIipOperationsBuilder)
      - service the service to map (instance of Service)
    """

    def getName():
        return composeResult(service.getName(), None)
    builder.defineProperty(getQName("name", service), getName, None)
    
    def getVersion():
        return composeResult(service.getVersion(), None)
    builder.defineProperty(getQName("version", service), getVersion, None)
    
    def getDescription():
        return composeResult(service.getDescription(), None)
    builder.defineProperty(getQName("description", service), getDescription, None)
    
    def getState():
        return composeResult(service.getState().name, None) #translate to string
    builder.defineProperty(getQName("state", service), getState, None)

    def passivate(params):
        service.passivate() # ignore params
    builder.defineOperation(getQName("passivate", service), passivate)

    def activate(params):
        service.activate() # ignore params
    builder.defineOperation(getQName("activate", service), activate)

    def setState(params):
        try:
            service.setState(ServiceState[params[0]])
            return composeResult(True, None)
        except ValueError as e:
            return composeResult(None, e.message)
    builder.defineOperation(getQName("setState", service), setState)

