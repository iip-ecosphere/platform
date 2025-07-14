import json
import logging as logger

class RequestProcessor:

    def processArguments(self, args):
        """Pre-processes the invocation arguments.
        
        Returns:
          any
            The preprocessed arguments.
        """ 

        return args
        
    def processResult(self, result):
        """Processes the invocation result.
        
        Returns:
          any
            The processed result.
        """ 
        
        return json.dumps(result).encode('UTF-8')
