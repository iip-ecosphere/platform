from http.server import BaseHTTPRequestHandler, HTTPServer
import logging

HOST = '127.0.0.1'  # Standard loopback interface address (localhost)


class BaSyxHTTPServer(HTTPServer):

    def __init__(self, port):
        super().__init__((HOST, port), BaSyxHTTPRequestHandler)

    def start(self):
        self.serve_forever()


class BaSyxHTTPRequestHandler(BaseHTTPRequestHandler):

    # TODO maybe remove later
    def _set_response(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_GET(self):
        path = self.path
        logging.info("GET request,\nPath: %s\nHeaders:\n%s\n", path, str(self.headers))

        # TODO remove later
        self._set_response()
        self.wfile.write("GET request for {}".format(self.path).encode('utf-8'))

    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        path = self.path
        data = self.rfile.read(content_length).decode('utf-8')
        logging.info("POST request,\nPath: %s\nHeaders:\n%s\n\nBody:\n%s\n",
                     path, str(self.headers), data)




