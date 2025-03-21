import iip.Registry
from Version import Version
from Service import ServiceState
from Service import ServiceKind
from datatypes.ImageInput import ImageInput
from datatypes.ImageInputImpl import ImageInputImpl
from datatypes.Command import Command
from datatypes.CommandImpl import CommandImpl
from datatypes.AiResult import AiResult
from datatypes.AiResultImpl import AiResultImpl
from interfaces.PythonbasedAIInterface import PythonbasedAIInterface

import sys
import os
import json
from urllib.request import url2pathname
from urllib.parse import urlparse
sys.path.append(os.getcwd() + '/../')
import base64 
from PIL import Image
import io
import numpy as np
from services.flowers.utils import decode_base64, preprocess_img,resize_image
# Disable tf logs before importing tensorflow IMPORTANT 
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'
import tflite_runtime.interpreter as tf
import pickle
import random
from io import BytesIO
import time
import cv2
import traceback
"""Do not utilise global code here. Place the needed function in the start / end methods.
   Add methods needed to be run on the start of a service in the start or end methods NOT in init()"""



class PythonbasedAI(PythonbasedAIInterface):
    """Template service implementation for PythonbasedAI
       Generated by: EASy-Producer."""
    
    def __init__(self):
        """Initializes the service.""" 
        super().__init__()
        self.counter = 0
        self.model_path = ""
        self.model_path_top = ""
        self.model_id = ""
        self.currentTime = -1
        self.model_path = "servicesTF/data/best-side.tflite"
        self.model_path_no_car = "servicesTF/data/fl-no-car-model.tflite"
        if (self.getRobotId() == 1):
            self.model_path_top = "servicesTF/data/best-top.tflite" #"servicesTF/data/client1_no_scratch.tflite" 
            self.model_id = "initial"
        else:
            self.model_path_top = "servicesTF/data/best-top.tflite"
            self.model_id = "initial"

    
    def processImageInput(self, data: ImageInput):
        """Asynchronous data processing method. Use self.ingest(data) to pass the result back to the data stream
           Possible output types: AiResultImpl
    
        Parameters:
          - data -- the data to process
        """
        print("IN AI STATIC")
        #img = data.getImage()
        print("GOT THE IMAGE !!")
        urlarray = data.getImageUri().split(';')
        print("URLS: " , urlarray[0], urlarray[1])
        print("PRE DECODING")
        img = None
        """Done as there is a error with laoding images which is hard to reproduce"""
        try:
                
            if (data.getImage() == ""):
                print("File PATH")
                img = self.openImageFile(urlarray[1])
            else:
                print("URL PATH")
                image_bytes = base64.b64decode(data.getImage())
                img = Image.open(io.BytesIO(image_bytes))
                """ The copy for the no car model, needed as the preprocessing is different """
                imgNoCar = img 
                img = cv2.cvtColor(np.array(img), cv2.COLOR_BGR2RGB)
                #noDone = False 
                #counter = 0
                #while not noDone and (counter < 10):
                #    try: 
                #        print("First ATTEMPT")
                #        #img = openImageRequest(data)
                #        noDone = True
                #    except Exception as e:
                #        counter = counter + 1
                #        print(e)
                #counter = 0
                #noDone = False
        except EOFError:
            print("Error on reading a file via pillow (exact cause unknown)")
            traceback.print_stack()
        #prepare the needed objects with default values, if there is processing they will be overwritten
        result = AiResultImpl()
        errorConfidence = [0, 0, 0, 0, 0]
        if img is not None:
            
            #img.save("testingImage.png")
            img_np = np.array(img)
            print("ARRAYED" , str(np.shape(img_np)))
            #img_np = img_np
            
            print("AFTER DECODING")
            
            if sys.platform == "win32":
                print("WINDOWS RUN: ")
            else:
                predictionNoCar = 0 
                if data.getSide() == "left":
                    interpreter = tf.Interpreter(model_path=self.model_path_no_car)
                    interpreter.allocate_tensors()
                    image = resize_image(imgNoCar) #taking the opened image!
                    image = np.array(image) / 255.0 #running with the opened image
        
                    image = np.expand_dims(image, axis=0)
                    image = np.expand_dims(image, axis=3)  
        
                    image = np.array(image, dtype=np.float32)  
        
                    input_details = interpreter.get_input_details()
                    output_details = interpreter.get_output_details()
                
                    # test model on input data
                    interpreter.set_tensor(input_details[0]['index'], image)
                    interpreter.invoke()
        
                    # predictions
                    predictionNoCar = interpreter.get_tensor(output_details[0]['index'])[0][0]
                #errorconfidence = [0,0,0, prediction,0]
                    
                position = "side"
                if (data.getSide() == "top"):
                    print("SHALL PROCESS TOP: " , data.getSide())
                    position = "top" #It just cannot be side as the usage is If else        
                    interpreter = tf.Interpreter(model_path=self.model_path_top)
                else: 
                    print("SHALL PROCESS SIDE: " , data.getSide())
                    position = "side"        
                    interpreter = tf.Interpreter(model_path=self.model_path)
                    
                # preprocess image
                preprocessed_img = preprocess_img(img_np,position)
                #image = np.array([preprocessed_img], dtype=np.float32)
                print("Preprocessed" , str(np.shape(preprocessed_img)))
                print("Models" , self.model_path_top, " Side: " , self.model_path)
                interpreter.allocate_tensors()
                
                input_details = interpreter.get_input_details()
                output_details = interpreter.get_output_details()
                print("RUNNING PREDICTION")
                interpreter.set_tensor(input_details[0]['index'], preprocessed_img)
                interpreter.invoke()
                
                print("GETTING OUTPUT OF TFlite")
                
                if (position == "top"):
                    probability = interpreter.get_tensor(output_details[0]['index'])[0] # output: [normal, scratch, shatter]
                    errorConfidence = [probability[1], probability[2], 0, predictionNoCar, probability[0]]
                else :
                    probability = interpreter.get_tensor(output_details[0]['index'])[0][0]
                    errorConfidence = [0, 0, probability, predictionNoCar, (1 - probability)]
            #result.errorConfidence = [0, 1, 0, 0, 0]
                    
        result.aiId = self.getAiId()
        result.error = ['Shatter', 'Scratch', 'Geometry', 'Car missing', 'Normal']
        result.errorConfidence = errorConfidence
        result.imageUri = urlarray[0]
        result.robotId = data.robotId
        result.modelId = self.model_id
        self.ingest(result)
        print("PY-Static: " + str(result.errorConfidence))
    
    def getGeometryThreshold(self, probability):
        if probability > 0.5:
            predicted_class = 1
            confidence = probability
        else:
            predicted_class = 0
            confidence = 1 - probability
    """        
    def openImageRequest(self, data: ImageInput):
        response = requests.get(data.getImage())
        return Image.open(BytesIO(response.content))
    """        
    def openImageFile(self, data):
        return Image.open(url2pathname(urlparse(data).path))
            
        
    def predict_tflite(model,image):
        image = np.expand_dims(image, axis=0)
        image = np.expand_dims(image, axis=3)  
    
        image = np.array(image, dtype=np.float32)  
    
        # load model
        interpreter = tf.lite.Interpreter(model_path=model)
        interpreter.allocate_tensors()
    
        # get input and output tensors
        input_details = interpreter.get_input_details()
        output_details = interpreter.get_output_details()
    
        # test model on input data
        interpreter.set_tensor(input_details[0]['index'], image)
        interpreter.invoke()
    
        # predictions
        prediction = interpreter.get_tensor(output_details[0]['index'])[0][0]
        return prediction

    
    def processCommand(self, data: Command):
        """Asynchronous data processing method. Use self.ingest(data) to pass the result back to the data stream
           Possible output types: AiResultImpl
    
        Parameters:
          - data -- the data to process
        """
        if ((time.time_ns() - self.currentTime) > 200000000) :
            self.currentTime = time.time_ns()
            print(self.currentTime)
            if (data.getCommand() == "SEND_MODEL_CHANGE_TO_AI"):
                #to do exchange models!
                
                print("RECEIVED COMMMAND FOR SWITCHING MODELS robotId " + str(self.getRobotId()))
                if (self.getRobotId() != 1 and self.model_path_top != "servicesTF/data/fl-top-model.tflite"):
                    self.model_path_top="servicesTF/data/fl-top-model.tflite"
                    self.model_id = "learned"
                    print("SWITCHING TO " + str(self.model_path_top))
                elif (self.getRobotId() != 1 and self.model_path_top =="servicesTF/data/fl-top-model.tflite"):
                    self.model_path_top="servicesTF/data/client2_no_shatter.tflite"
                    self.model_id = "initial"
                    print("SWITCHING TO " + str(self.model_path_top))
                
            #create result instance and call self.ingest(data)
        print("PY-Static: " + str(data))

#registers itself
PythonbasedAI()
