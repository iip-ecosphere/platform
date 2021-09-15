import json
from http.server import BaseHTTPRequestHandler, HTTPServer
import logging as logger

from JSONProvider import JSONProvider

HOST = '127.0.0.1'  # Standard loopback interface address (localhost)

class BaSyxHTTPServer():
    def __init__(self, builder, port):
        self.port = port
        self.builder = builder

    def start(self):
        handler = HTTPHandlerFactory(self.builder)
        server = HTTPServer((HOST, self.port), handler)
        server.serve_forever()


def HTTPHandlerFactory(builder):
    class CustomHandler(BaseHTTPRequestHandler):
        def __init__(self, *args, **kwargs):
            self.providerBackend = JSONProvider(builder)
            super(CustomHandler, self).__init__(*args, **kwargs)

        def do_GET(self):
            path = self.path[1:]
            request = self.request
            logger.debug('HTTP GET path: %s', path)
            logger.debug('HTTP GET request: %s', request)

            value = self.providerBackend.getModelPropertyValue(path)
            encodedResult = json.dumps(value).encode('UTF-8')
            logger.debug('HTTP GET response:  %s ', encodedResult)

            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            self.wfile.write(encodedResult)

    return CustomHandler
