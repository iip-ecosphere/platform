import socket
import logging as logger
logger.basicConfig(level="DEBUG")
import struct
import json
from JSONProvider import JSONProvider

BASYX_GET = 1
BASYX_SET = 2
BASYX_CREATE = 3
BASYX_DELETE = 4
BASYX_INVOKE = 5
BASYX_RESULT_OK = 0

class VABBaSyxTCPInterface:

    def __init__(self, builder):
        self.providerBackend = JSONProvider(builder)
        pass

    def parse(self, conn, addr) :
        frameSize = struct.unpack('i', conn.recv(4))[0]
        logger.debug("framesize " + str(frameSize))
        data = conn.recv(frameSize)
        # this is ugly, but a direct translation of VABBaSyxTCPInterface
        if data[0] == BASYX_GET:
            pathLen = struct.unpack('i', data[1:5])[0]
            path = data[5:5+pathLen].decode('UTF-8')
            logger.debug("GET path " + str(pathLen)+" "+path)
            self.basyxGet(path, conn)
        elif data[0] == BASYX_SET: 
            pathLen = struct.unpack('i', data[1:5])
            pos = 5 + pathLen
            path = data[5:pos].decode('UTF-8')
            logger.debug("SET path " + str(pathLen)+" "+path)
            jsonValueLen = struct.unpack('i', data[pos:pos+3])
            pos = pos + 3
            json = ''.join(data[pos:pos + pathLen - 1])
            self.basyxSet(path, json, conn)
        elif data[0] == BASYX_CREATE: 
            pathLen = struct.unpack('i', data[1:5])[0]
            pos = 5 + pathLen
            path = data[5:pos].decode('UTF-8')
            logger.debug("CRE path " + str(pathLen)+" "+path)
            jsonValueLen = struct.unpack('i', data[pos:pos+3])
            pos = pos + 3
            json = ''.join(data[pos:pos + pathLen - 1])
            self.basyxCreate(path, json, conn)
        elif data[0] == BASYX_DELETE: 
            pathLen = struct.unpack('i', data[1:5])[0]
            pos = 5 + pathLen
            path = data[5:pos].decode('UTF-8')
            logger.debug("DEL path " + str(pathLen)+" "+path)
            json = ''
            if pos < len(data): 
                jsonValueLen = struct.unpack('i', data[pos:pos+3])
                pos = pos + 3
                json = ''.join(data[pos:pos + pathLen - 1])
            self.basyxDelete(path, json, conn)
        elif data[0] == BASYX_INVOKE: 
            pathLen = struct.unpack('i', data[1:5])[0]
            pos = 5 + pathLen
            path = data[5:pos].decode('UTF-8')
            logger.debug("INV path " + str(pathLen)+" "+path)
            jsonValueLen = struct.unpack('i', data[pos:pos+4])
            pos = pos + 4
            json = data[pos:pos + pathLen].decode('UTF-8')
            logger.debug("INV paramJson " + json)
            self.basyxInvoke(path, json, conn)

    def basyxGet(self, path, conn):
        value = self.providerBackend.getModelPropertyValue(path)
        logger.debug("GET " + path + " -> " + str(value))
        self.sendResponseFrame(conn, value, BASYX_RESULT_OK) #no return

    def basyxSet(self, path, json, conn):
        param = json.loads(json)
        self.providerBackend.setModelPropertyValue(path, param)
        self.sendResponseFrame(conn, '', BASYX_RESULT_OK) #no return

    def basyxCreate(self, path, json, conn):
        param = json.loads(json)
        self.providerBackend.createValue(path, param)
        self.sendResponseFrame(conn, '', BASYX_RESULT_OK) #no return

    def basyxDelete(self, path, json, conn):
        param = json.loads(json)
        if len(param) > 0:
            self.providerBackend.delete(path, param)
        else:
            self.providerBackend.delete(path)
        self.sendResponseFrame(conn, '', BASYX_RESULT_OK) #no return

    def basyxInvoke(self, path, args, conn):
        result = self.providerBackend.invoke(path, args)
        self.sendResponseFrame(conn, result, BASYX_RESULT_OK)
        
    def sendResponseFrame(self, conn, value, result):
        encodedResult =  json.dumps(value).encode('UTF-8')
        bts = struct.pack('<ibi', 1 + 4 + len(encodedResult), result, len(encodedResult)) + encodedResult
        conn.sendall(bts)
        
