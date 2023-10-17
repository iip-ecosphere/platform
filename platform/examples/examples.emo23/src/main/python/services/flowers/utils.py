import ast
import math
import numpy as np
import cv2
import json
import base64
import sys
from PIL import Image
import io
import os
import pickle
from sklearn.model_selection import train_test_split

target_size = (128,128)

def parse_tuple(string):
    try:
        s = ast.literal_eval(str(string))
        if type(s) == tuple:
            return s
        return
    except:
        return

def truncate(f, n):
    return math.floor(f * 10 ** n) / 10 ** n

def exract_model_weights(model):
    weights = model.get_weights()
    return weights

def save_model_weights(weights, target_path):
    np.save(target_path, weights)
    
def load_pretrained_weights(weights_path):
    loaded_weights = np.load(weights_path, allow_pickle=True)
    return loaded_weights

def vertical_crop(img, top=0, bottom=0):
    height, width = img.shape[:2]
    cropped_img = img[top:height-bottom, 0:width]
    return cropped_img

def horizontal_crop(image, left=0, right=0):
    height, width = image.shape[:2]
    new_width = width - left - right
    cropped_image = image[:, left:width - right, ...]
    return cropped_image

def downsize_image(image_nparray, max_size):
    height, width = image_nparray.shape[:2]

    aspect_ratio = float(width) / float(height)

    if height > width:
        new_height = max_size
        new_width = int(new_height * aspect_ratio)
    else:
        new_width = max_size
        new_height = int(new_width / aspect_ratio)

    resized_image = cv2.resize(image_nparray, (new_width, new_height), interpolation=cv2.INTER_AREA)

    return resized_image

def preprocess_img(img,d_type):
    def vertical_crop(img, top=0, bottom=0):
        height, width = img.shape[:2]
        cropped_img = img[top:height-bottom, 0:width]
        return cropped_img
    image_size = (256,256)
    if (d_type == "side"):
        
        img = cv2.GaussianBlur(img,(23,23),0)
        t_lower = 10 # Lower Threshold
        t_upper = 25 # Upper threshold
        aperture_size = 7 # Aperture size
        L2Gradient = True # Boolean    # Applying the Canny Edge filter with L2Gradient = True
        img = cv2.Canny(img, t_lower, t_upper,aperture_size, L2gradient = L2Gradient )
        img = vertical_crop(img, top=730, bottom=800)
        resized_down = cv2.resize(img, image_size, interpolation= cv2.INTER_LINEAR)
        new_img = resized_down / 255.0
        new_img = new_img.reshape(image_size + (1,))
        X_total = np.expand_dims(new_img, 0)
        X_total = np.float32(X_total)
        return X_total
    elif(d_type == "top"):
        img = cv2.GaussianBlur(img,(31,31),0)
        t_lower = 10 # Lower Threshold
        t_upper = 25 # Upper threshold
        aperture_size = 7 # Aperture size
        L2Gradient = True # Boolean    # Applying the Canny Edge filter with L2Gradient = True
        img = cv2.Canny(img, t_lower, t_upper,aperture_size, L2gradient = L2Gradient )
        resized_down = cv2.resize(img, image_size, interpolation= cv2.INTER_LINEAR)
        new_img = resized_down / 255.0
        new_img = new_img.reshape(image_size + (1,))
        X_total = np.expand_dims(new_img, 0)
        X_total = np.float32(X_total)
        return X_total


def encode_img(source):
    with open(source, 'rb') as file:
        image_base64 = base64.b64encode(file.read()).decode('utf-8')
    return image_base64 

def decode_base64(source):

    with open(source, 'r') as file:
        data = json.load(file)

    img_base64 = data['base64']
    label = data['label']

    img_binary = base64.b64decode(img_base64)

    img = Image.open(io.BytesIO(img_binary))

    img_np = np.array(img)

    return img_np, label
    
def load_grayscale_image(file_path):
    image = Image.open(file_path).convert('L')  # Convert to grayscale
    image = image.resize(target_size)
    return np.array(image)

def resize_image(image, size=target_size):
    return image.resize(size)


class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'
