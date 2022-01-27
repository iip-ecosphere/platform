import sys
# This statement asserts only integer, not fractional part.
# It means the python version has to be at least 3.0 to be true.
assert sys.version_info[0] > 2, 'Python Version needs to be higher than 2.'

import ServiceEnvironmentImpl

ServiceEnvironmentImpl.start(sys.argv[1:])
