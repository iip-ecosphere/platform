from datatypes.ImageInput import ImageInput
from datatypes.ImageInputImpl import ImageInputImpl
from datatypes.AiResult import AiResult
from datatypes.AiResultImpl import AiResultImpl
from datatypes.Command import Command
from datatypes.CommandImpl import CommandImpl
from interfaces.PythonbasedAIInterface import PythonbasedAIInterface
import random
import base64
from PIL import Image
import io
import os
import tempfile
#from keras.models import load_model
#from tensorflow import keras
#from tensorflow.keras import datasets, layers, models
import numpy as np
from tflite_runtime.interpreter import Interpreter
import sys
import cv2
import argparse
#from keras import backend as K
import glob
import time


class ExamplePythonService(PythonbasedAIInterface):
    """Example Python Service implementing generated interface."""
    
    filename = (tempfile.gettempdir() + '/00001SavedImage.jpg')
    
    testimageName = (tempfile.gettempdir() + '/1SavedImage.jpg')
    
    
    DEBUG = 0
    DEFAULT_IMAGE_WIDTH = 640
    DEFAULT_IMAGE_HEIGHT = 480
    NON_BLACK_TIRES_COLORS = ["red","green","yellow"]
    NON_BLACK_WHITEPIXEL_COUNT_THRESH = 500
    COLOR_HUE_SETTINGS = {
        "red": {
            "hMin": 170,
            "hMax": 179,
        },
        "green": {
            "hMin": 41,
            "hMax": 90,
        },
        "yellow": {
            "hMin": 25,
            "hMax": 40,
        }
    }
    STATIC_HUE_SETTINGS = {
        "sMin": 100,
        "sMax": 255,
        "vMin": 20,
        "vMax": 255
    }
    CONFIDENCE_THRESH = {
        "engraving": 0.5,
        "scratch": 0.5,
        "window": {
            1: 0.5,
            2: 0.5,
            3: 0.5
        }
    }
    RESIZE_WIDTH = 420
    RESIZE_HEIGHT = 420
    CROP_X = 80
    CROP_MARGIN_BOTTOM = 90
    CROP_MARGIN_TOP = 180
    CROP_MARGIN_RIGHT = 80
    CROP_MARGIN_LEFT = 150
    WINDOW_MODEL_PATH = 'services/WIN_420x420_mul_co.tflite'
    SCRATCH_MODEL_PATH = 'services/SC_420x420_mul_co.tflite'
    ENGRAVING_MODEL_PATH = 'services/EN_420x420.tflite'
    LABEL_DICTIONARY = {
    }
    # predictions thresholds
    SCRATCH_PREDICTION_THRETH = .5
    ENGRAVING_PREDICTION_THRETH = .5
    
    def __init__(self):
        """Initializes the service.""" 
        super().__init__()
    
    def processImageInput(self, data: ImageInput, time_file_path: str = "processing_times.txt"):
        """Asynchronous data processing method. Use self.ingest(data) to pass the result back to the data stream.
        
        Parameters:
          - data -- the data to process
        """  
        if time_file_path is not None:
            start_time = time.time()
        sys.stderr.write('>>>>>>>>>>>>>>>>>Version3.1')
        result = AiResultImpl()
        result.setImage(data.getImage())
        result.setProductId(data.getProductId())
        sys.stderr.write(">>>>>>>>>>>>>>>>> AI MODULE\n" +  str(data.getQrCodeDetected()))
        if not data.getQrCodeDetected():
            sys.stderr.write("------------------------------- AI MODULE\n")
            image_bytes = base64.b64decode(data.getImage())
            image = Image.open(io.BytesIO(image_bytes))
            """Java encodes Strings in UTF 16, meaning the base64 bytearray comes as a utf 16 encoded string into python
            making keras unable to use the bytesequence directly -> current way is to just save the file and read it again
            """
            if image_bytes is not None:
                sys.stderr.write(">>>>>>>>>>>>>>>>> IMAGE BYTES\n" +  str(image_bytes[0:15]))
                sys.stderr.write(">>>>>>>>>>>>>>>>> CWD\n" +  str(os.getcwd()))
                sys.stderr.write(">>>>>>>>>>>>>>>>> IN CWD\n" +  str(os.listdir()))
            image.save(self.filename) 
            #outputObject = self.useAI(self.filename)
            #result.setScratch(self.useAI(self.filename))
            #result.setWheelColour(self.getTireColors(self.filename))
            sys.stderr.write('>>>>><<< RUNNING AI STUFF\n')
            img = cv2.imread(self.filename)
            
            
            inspection_resut = self.inspect(img)
            """ "Engraving": False,
            "EngravingConfidence": 0,
            "OneWindowConfidence": 0,
            "TwoWindowConfidence": 0,
            "ThreeWindowConfidence": 0,
            "Scratch": False,
            "ScratchConfidence": 0,
            "WheelColour": ""
            """
            result.setWheelColour(inspection_resut["WheelColour"])
            result.setEngraving(inspection_resut["Engraving"].item())
            sys.stderr.write(">>>>>>>>>>>>VALUES_: " + str(type(inspection_resut["Engraving"])))
            result.setEngravingConfidence(inspection_resut["EngravingConfidence"].item())
            sys.stderr.write(">>>>>>>>>>>>VALUES_: " + str(type(inspection_resut["EngravingConfidence"])))
            result.setScratch(inspection_resut["Scratch"].item())
            sys.stderr.write(">>>>>>>>>>>>VALUES_: " + str(type(inspection_resut["Scratch"])))
            result.setScratchConfidence(inspection_resut["ScratchConfidence"].item())
            sys.stderr.write(">>>>>>>>>>>>VALUES_: " + str(type(inspection_resut["ScratchConfidence"])))
            result.setOneWindowConfidence(inspection_resut["OneWindowConfidence"].item())
            sys.stderr.write(">>>>>>>>>>>>VALUES_: " + str(type(inspection_resut["OneWindowConfidence"])))
            result.setTwoWindowsConfidence(inspection_resut["TwoWindowConfidence"].item())
            sys.stderr.write(">>>>>>>>>>>>VALUES_: " + str(type(inspection_resut["TwoWindowConfidence"])))
            result.setThreeWindowsConfidence(inspection_resut["ThreeWindowConfidence"].item())
            sys.stderr.write(">>>>>>>>>>>>VALUES_: " + str(type(inspection_resut["ThreeWindowConfidence"])))
            
            
            sys.stderr.write('>>>>> RESULT :'+ str(result))
        self.ingest(result) # asynchronous processing, call ingest from parent    
        if time_file_path is not None:
            t = time.time() - start_time
            f = open(time_file_path, "a")
            f.write(f"{t}\n")
            f.close()   
    
    
    def processCommand(self, data: Command):
        """Asynchronous data processing method. Use self.ingest(data) to pass the result back to the data stream."""
        
        # nothing to do for now
        sys.stderr.write("PY: " + str(data)+"\n") # do not use print
    
    
    def apply_mask(self, image,type):
        if(type == "scratch"):
            lower_black = np.array([50,50,50], dtype=np.uint8)
            upper_black = np.array([200,200,200], dtype=np.uint8)
            mask = cv2.inRange(image, lower_black, upper_black)
            result = cv2.bitwise_not(mask)
        elif (type == "window"):
            img = cv2.cvtColor(image,cv2.COLOR_BGR2GRAY)
            mask = cv2.medianBlur(img,7)
            mask = cv2.adaptiveThreshold(mask,255,cv2.ADAPTIVE_THRESH_MEAN_C,cv2.THRESH_BINARY_INV,11,2)
            mask = cv2.GaussianBlur(mask,(11,11),0)
            result = cv2.threshold(mask, 100, 255, cv2.THRESH_BINARY)[1]
        elif (type == "engrave"):
            result = cv2.Canny(image, 40, 100,5, L2gradient = True )
        return result
    
    def crop_image(self, img):
        img_height = img.shape[0]
        img_width = img.shape[1]
        cropped = img[self.CROP_MARGIN_TOP:img_height-self.CROP_MARGIN_BOTTOM,self.CROP_MARGIN_LEFT:img_width-self.CROP_MARGIN_RIGHT].copy()
        return cropped
        
    def resize_image(self, img):
        resized = cv2.resize(img, (self.RESIZE_WIDTH, self.RESIZE_HEIGHT), interpolation = cv2.INTER_AREA) 
        resized = resized / 255.0
        resized = resized.reshape((self.RESIZE_WIDTH, self.RESIZE_HEIGHT) + (1,))
        return resized
    
    def preprocess_image(self, image, type):
        img = self.apply_mask(image,type)
        #img = crop_image(img)
        img = self.resize_image(img)
        return img
    
    def get_tires_color(self, image):
        img = image
        img_shape = img.shape        
        if(img_shape[0]!=self.DEFAULT_IMAGE_HEIGHT or img_shape[1]!=self.DEFAULT_IMAGE_WIDTH):
            img = cv2.resize(img, (self.DEFAULT_IMAGE_WIDTH,self.DEFAULT_IMAGE_HEIGHT), interpolation= cv2.INTER_LINEAR)
    
        white_pixelcounts_per_color = []
        for color in self.NON_BLACK_TIRES_COLORS:
            # set the hue color settings per color 
            hMin = self.COLOR_HUE_SETTINGS[color]['hMin']
            hMax = self.COLOR_HUE_SETTINGS[color]['hMax']
            # apply static settings 
            sMin = self.STATIC_HUE_SETTINGS['sMin']
            sMax = self.STATIC_HUE_SETTINGS['sMax']
            vMin = self.STATIC_HUE_SETTINGS['vMin']
            vMax = self.STATIC_HUE_SETTINGS['vMax']
    
            # Set minimum and max HSV values to display
            lower = np.array([hMin, sMin, vMin])
            upper = np.array([hMax, sMax, vMax])
            
            hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
            mask = cv2.inRange(hsv, lower, upper)
    
            # get white pixel count
            whitePixelCount = cv2.countNonZero(mask)
            white_pixelcounts_per_color.append(whitePixelCount)
    
        if(np.sum(white_pixelcounts_per_color) <= self.NON_BLACK_WHITEPIXEL_COUNT_THRESH):
            result = "black"
            
        else:
            max_pixelcount = max(white_pixelcounts_per_color)
            max_pixelcount_index = white_pixelcounts_per_color.index(max_pixelcount)
            result = self.NON_BLACK_TIRES_COLORS[max_pixelcount_index]
    
        return result
    
    def get_window_count(self, image):
        results = {
            "OneWindowConfidence": 0,
            "TwoWindowConfidence": 0,
            "ThreeWindowConfidence": 0
        }
        img = self.preprocess_image(image, "window")
        img = img[np.newaxis,...]
        img2 = self.preprocess_image(image, "scratch")
        img2 = img2[np.newaxis,...]
        img_total = np.concatenate((img,img2), axis=3)
        #print(f"\n\n img_total shape: {img_total.shape}\n\n")
        interpreter_real = Interpreter(self.WINDOW_MODEL_PATH)
        interpreter_real.allocate_tensors()
    
        output = interpreter_real.get_output_details()[0]  # Model has single output.
        input = interpreter_real.get_input_details()[0]  # Model has single input.
        img_total = np.float32(img_total)
        interpreter_real.set_tensor(input['index'], img_total)
        interpreter_real.invoke()
        prediction = interpreter_real.get_tensor(output['index'])
        results["OneWindowConfidence"] = prediction[0][0]
        results["TwoWindowConfidence"] = prediction[0][1]
        results["ThreeWindowConfidence"] = prediction[0][2]
        return results
    
    def get_is_scratched(self, image):
        img = self.preprocess_image(image, "scratch")
        img = img[np.newaxis,...]
        interpreter_real = Interpreter(self.SCRATCH_MODEL_PATH)
        interpreter_real.allocate_tensors()
    
        output = interpreter_real.get_output_details()[0]  # Model has single output.
        input = interpreter_real.get_input_details()[0]  # Model has single input.
        img_total = np.float32(img)
        interpreter_real.set_tensor(input['index'], img_total)
        interpreter_real.invoke()
        prediction = interpreter_real.get_tensor(output['index'])
        is_scratched = prediction[0] > self.SCRATCH_PREDICTION_THRETH # assuming the first element is engraved label
        confidence = 1 - prediction[0] if is_scratched == False else prediction[0]
        return is_scratched[0], confidence[0]
    
    def get_is_engraved(self, image):
        img = self.preprocess_image(image, "engrave")
        img = img[np.newaxis,...]
        interpreter_real = Interpreter(self.ENGRAVING_MODEL_PATH)
        interpreter_real.allocate_tensors()
    
        output = interpreter_real.get_output_details()[0]  # Model has single output.
        input = interpreter_real.get_input_details()[0]  # Model has single input.
        img_total = np.float32(img)
        interpreter_real.set_tensor(input['index'], img_total)
        interpreter_real.invoke()
        prediction = interpreter_real.get_tensor(output['index'])
        is_engraved = prediction[0] > self.ENGRAVING_PREDICTION_THRETH # assuming the first element is engraved label
        confidence = 1 - prediction[0] if is_engraved == False else prediction[0]
        
        return is_engraved[0], confidence[0]
    
    #@profile
    def inspect(self, img):
        inspection_result = {
            "Engraving": False,
            "EngravingConfidence": 0,
            "OneWindowConfidence": 0,
            "TwoWindowConfidence": 0,
            "ThreeWindowConfidence": 0,
            "Scratch": False,
            "ScratchConfidence": 0,
            "WheelColour": ""
        }
        # predict wheel color
        inspection_result["WheelColour"] = self.get_tires_color(img)
        
        # predict window count
        window_confidences = self.get_window_count(img)
        inspection_result["OneWindowConfidence"] = window_confidences["OneWindowConfidence"]
        inspection_result["TwoWindowConfidence"] = window_confidences["TwoWindowConfidence"]
        inspection_result["ThreeWindowConfidence"] = window_confidences["ThreeWindowConfidence"]
        
        # # predict engraving
        in_engraved,engraving_confidence = self.get_is_engraved(img)
        inspection_result["Engraving"] = in_engraved
        inspection_result["EngravingConfidence"] = engraving_confidence
        
        # # predict scratch
        is_scratched,scratched_confidence = self.get_is_scratched(img)
        inspection_result["Scratch"] = is_scratched
        inspection_result["ScratchConfidence"] = scratched_confidence
        
        return inspection_result


    
#registers itself
ExamplePythonService()
