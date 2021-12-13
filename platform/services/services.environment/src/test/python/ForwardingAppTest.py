import unittest
import subprocess
from subprocess import PIPE

class ForwardingAppTest(unittest.TestCase):
    """Tests if send value equals received value."""

    def test_forwardingapp(self):
        test_value = 'test'
        # Sending test value to ForwardingApp
        process = subprocess.Popen(['python3', 'ForwardingApp.py'], stdout=PIPE, stdin=PIPE, stderr=PIPE)
        test_value_as_byte = test_value.encode()
        output_as_byte = process.communicate(input=test_value_as_byte)[0]
        # Converting received byte value to string
        output = output_as_byte.decode('utf-8').rstrip() # removing newline
        assert output == test_value


if __name__ == '__main__':
    unittest.main()
