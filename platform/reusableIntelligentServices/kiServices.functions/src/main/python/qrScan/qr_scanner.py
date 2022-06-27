import argparse
from pyzbar import pyzbar
import cv2
import base64
from PIL import Image
import numpy as np
import io
import sys


ap = argparse.ArgumentParser()
ap.add_argument("-i", "--image", required=True, help="the path to the car image you want to inspect")
args = vars(ap.parse_args())

def get_qr(img):
    barcodes = pyzbar.decode(img)
    qr = "" #return empty string if no code is fond (instad of none)
    if len(barcodes) > 0 : #precent exception in case of not recognized image
        qr = barcodes[0].data.decode("utf-8")
    return qr

if __name__ == "__main__":
    sys.stderr.write('>>>>>>>>>>>>>>>>>Version1.1\n')
    image_path = args['image']
    fileread = open(image_path, "rb")
    image_bytes = fileread.read()
    decoded = base64.b64decode(image_bytes) #decode the input
    image = Image.open(io.BytesIO(decoded)) # turn decoded image in PIL png type datatype
    image = np.array(image) #image to nparray (as openCV image read does not form if not from a file)
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY) #improves reliability
    gray = gray[140: 310, 210:440]
    _, gray = cv2.threshold(gray, 100, 255, cv2.THRESH_BINARY) #thresholding for turning the image black and white, first number is the value from which a pixel should be turned white
    #cv2.imshow("image", gray)
    #cv2.waitKey(0)
    qr = get_qr(gray)
    text_file = open("/tmp/qr.res", "w") # workaround; Java does not take up results
    text_file.write(qr)
    text_file.close()
    print(qr) # initial: command line passing, this is not in the service environment!