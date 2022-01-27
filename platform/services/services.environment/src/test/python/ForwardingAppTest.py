import unittest
import subprocess
import queue
import time
import sys
from subprocess import PIPE
from IOThread import WriteThread, ReadThread


class ForwardingAppTest(unittest.TestCase):
    """Tests ForwardingApp"""

    def test_forwardingapp(self):
        # Does the app echos received values?
        test_value = 'test\n'
        process = subprocess.Popen(['python3', 'ForwardingApp.py'], bufsize=0, stdout=PIPE, stdin=PIPE)

        source_queue = queue.Queue()
        target_queue = queue.Queue()

        # input thread
        writer = WriteThread(process.stdin, source_queue)
        writer.setDaemon(True)
        writer.start()
        # output thread
        reader = ReadThread(process.stdout, target_queue)
        reader.setDaemon(True)
        reader.start()

        # populate queue
        number_of_sended_values = 3
        for i in range(number_of_sended_values):
            source_queue.put(test_value)
        source_queue.put('')

        time.sleep(2)  # expect some output from reader thread

        source_queue.join()  # wait until all items in source_queue are processed


        received_values_list = reader.get_target()
        number_of_received_values = len(received_values_list)
        received_value = received_values_list.pop(0).decode('utf-8')
        print("Sent/received values: " + repr(test_value) + " " + repr(received_value))
        print("# sent/received: " + repr(number_of_sended_values) + " " + repr(number_of_received_values))

        # Senden value equals received value
        assert received_value.rstrip() == test_value.rstrip()

        # Repeated sending and receiving
        assert number_of_sended_values == number_of_received_values

        process.terminate()
        process.wait()
        reader.stop()
        writer.stop()


if __name__ == '__main__':
    unittest.main()
