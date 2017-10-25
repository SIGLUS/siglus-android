import base64
import sys
import os

encodedValue = sys.argv[1]
decodedFileName = sys.argv[2]
decodedValue = base64.standard_b64decode(encodedValue);
file = open(decodedFileName, "w")
file.write(decodedValue)
os.chmod(decodedFileName, 0o600)
file.close()
