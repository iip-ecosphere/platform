import json
from RequestProcessor import RequestProcessor
import logging as logger

class BaSyx2RequestProcessor(RequestProcessor):

    def processArguments(self, args):
        """Pre-processes the invocation arguments.
        
        Returns:
          any
            The preprocessed arguments.
        """ 
        
        tmp = []
        for a in json.loads(args):
           prop = a["value"]
           propVal = prop["value"] # we could use "type" but python does not care
           tmp.append(propVal)
        return json.dumps(tmp)
        
    def processResult(self, result):
        """Processes the invocation result.
        
        Returns:
          any
            The processed result.
        """ 
        tmp = json.dumps(result)
        tmp = [{"value" : {"modelType": "Property", "value": tmp }}]
        return json.dumps(tmp)
