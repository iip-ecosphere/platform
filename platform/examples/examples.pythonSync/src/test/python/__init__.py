import logging as logger
logger.basicConfig(level="DEBUG")

# there might be a better/official way :/
import sys
sys.path.insert(1, '../../main/python')
sys.path.insert(1, '../../../gen/py/ApplicationInterfaces/src/main/python')
sys.path.insert(1, 'iip')

from services.ExamplePythonSyncService import ExamplePythonSyncService
from Starter import start

if __name__ == '__main__':
    myService = ExamplePythonSyncService()
    start([myService])
