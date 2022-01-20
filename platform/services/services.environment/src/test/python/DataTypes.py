
class Rec1:
    """ Implementation of the application data type Rec1. """

    def __init__(self):
        self.intField = None
        self.stringField = None

    def setParameters(self, parameters: dict):
        self.__dict__.update(parameters)
