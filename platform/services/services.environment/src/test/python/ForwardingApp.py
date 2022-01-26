import sys

# Simple echo app, if there is a command line argument, echo the value of the last argument.
# If there is no command line argument, loop as long as there is data and echo that
if len(sys.argv) > 1: 
    args = sys.argv[1:]
    if len(args) > 0:
        print(args[len(args) - 1])
else:
    sys.stderr.write("wait\n")
    while True:
        s = input()
        sys.stderr.write(s+"\n")
        print(s)
