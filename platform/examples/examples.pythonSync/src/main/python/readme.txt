By convention, service implementations must be in module "services". 
Service-specific information is in the parent class constructor. Just call it.
Further, services must register themselves through self-instantiation in the last line.