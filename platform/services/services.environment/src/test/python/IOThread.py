import sys
import threading

class WriteThread(threading.Thread):
    def __init__(self, p_in, source_queue):
        threading.Thread.__init__(self)
        self.pipe = p_in
        self.source_queue = source_queue

    def run(self):
        while True:
            source = self.source_queue.get()
            #print ("writing to process: ", repr(source))
            self.pipe.write(source.encode())
            self.pipe.flush()
            self.source_queue.task_done()

class ReadThread(threading.Thread):
    def __init__(self, p_out, target_queue):
        threading.Thread.__init__(self)
        self.pipe = p_out
        self.target_queue = target_queue

    def run(self):
        while True:
            line = self.pipe.readline() # blocking read
            if line == '':
                break
            #print ("reader read: ", line.rstrip())
            self.target_queue.put(line)

    def get_target(self):
        return list(self.target_queue.queue)



