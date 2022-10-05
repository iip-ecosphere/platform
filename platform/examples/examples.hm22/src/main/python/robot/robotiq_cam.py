#! /usr/bin/python3
import sys,os
import numpy as np
import requests
from PIL import Image
import io
import cv2
from datetime import datetime
import time

class Robotiq_Camera:
    def __init__(self, webcam=False,robot_ip_address = '192.168.1.101',\
                blur_threshold=100,\
                device=2):
        self.url = "http://"+robot_ip_address+":4242/current.jpg?type=color"
        self.blur_threshold=blur_threshold
        self.webcam = webcam
        if self.webcam:
            self.cam = cv2.VideoCapture(device)
            self.cam.set(cv2.CAP_PROP_AUTOFOCUS,0)
            self.cam.set(cv2.CAP_PROP_FRAME_WIDTH,1280) 
            self.cam.set(cv2.CAP_PROP_FRAME_HEIGHT,720)
            self.cam.set(cv2.CAP_PROP_FOCUS,570)

    def variance_of_laplacian(self,image):
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        return cv2.Laplacian(gray, cv2.CV_64F).var()
    
    def get_network_img(self):
        resp=None
        try:
            resp = requests.get(self.url).content
        except:
            resp=None
        if resp == None:
            #If the response is empty display an error image
            pilImage=Image.open("error.jpg")
        else:
            imageData = np.asarray(bytearray(resp), dtype="uint8")
            pilImage=Image.open(io.BytesIO(imageData))

        opencvImage = cv2.cvtColor(np.array(pilImage), cv2.COLOR_RGB2BGR)
        return opencvImage

    def get_webcam_img(self):
        ret, opencvImage = self.cam.read()
        return opencvImage

    def get_img(self):
        if self.webcam:
            return self.get_webcam_img()
        else:
            return self.get_network_img()

    def capture_loop(self):
        for _ in range(10):
            opencvImage = self.get_img()
            cv2.waitKey(1)
        return opencvImage


    def capture(self,prefix, path='/',check_focus=True, show=False, write=True):

        imname=path+"img"+str(prefix)+'_'+str(datetime.now())+'.jpg'
        if check_focus:
            t0 = time.time()
            while True:
                img=self.capture_loop()
                fm=self.variance_of_laplacian(img)
                t1 = time.time()
                if fm>self.blur_threshold:
                    if write:
                        cv2.imwrite(imname, img)
                        print('Image saved: ',imname)
                    if show:
                        cv2.imshow('Captured Image',img)
                    return True
                if t1-t0>10:
                    print('Can not focus!')
                    return False
        else:
            img=self.capture_loop()            
            cv2.imwrite(imname, img)
            print('Image saved: ',imname)
            if show:
                cv2.imshow('Captured Image',img)
            return True
            
        