class Rec1:
    def __init__(self):
        self.intField = None
        self.stringField = None

    def setParameters(self, parameters: dict):
        self.__dict__.update(parameters)

    """
        def __repr__(self):
        s = f'Rec1({ repr(self.intField) }, { repr(self.stringField) })'
        return s

    """

