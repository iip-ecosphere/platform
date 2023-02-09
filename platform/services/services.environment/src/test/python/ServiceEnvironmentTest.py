import unittest
import subprocess
import queue
import time
import sys
import os
import re
from subprocess import PIPE

class CmdLineServiceEnvironmentTest(unittest.TestCase):

    def test_synchronous(self):
        self.processSync("S|data")
    
    def processSync(self, test_value):
        dir = os.getcwd()
        out = subprocess.check_output(['python3', 'ServiceEnvironment.py', '--modulesPath', dir, 
            '--mode', 'console', '--data', test_value, '--sid', '1234'], cwd="../../main/python", text=True)
        
        assert len(out) > 0
        assert out.rstrip() == test_value.rstrip()

    def test_asynchronous(self):
        dir = os.getcwd()
        process = subprocess.Popen(['python3', 'ServiceEnvironment.py', '--modulesPath', dir, 
            '--mode', 'console', '--sid', '1234'], bufsize=0, stdout=PIPE, stdin=PIPE, 
            cwd="../../main/python", text=True)
        process.stdin.write('S|data\r\n')
        process.stdin.write('S|data\r\n')
        process.stdin.write('*PARAM:String|{"name":"p","value":"1234"}\r\n')
        out = process.communicate()
        
        results = re.sub(r'\n\s*\n', '\n', out[0]).splitlines()
        assert len(results) == 2 # as input
        assert results[0] == 'S|data'
        assert results[1] == 'S|data'

        process.terminate()
        process.wait()
