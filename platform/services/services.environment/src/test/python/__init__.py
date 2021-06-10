import logging as logger
logger.basicConfig(level="DEBUG")

# there might be a better/official way :/
import sys
sys.path.insert(1, '../../main/python')

from MyService import MyService
from Starter import start

if __name__ == '__main__':
    myService = MyService()
    start([myService])
