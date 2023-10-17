import flwr as fl
import tensorflow as tf
import pickle
#import tensorflow_model_optimization as tfmot
from keras.models import Sequential
from keras.layers import Conv2D, MaxPooling2D, Flatten, Dense
from keras.preprocessing.image import ImageDataGenerator
from services.flowers.utils import parse_tuple, preprocess_img, decode_base64
import glob
import time
import numpy as np
import os

class Client(fl.client.NumPyClient):
    
    def __init__(self, config):
             
        self.config = config
        self.compile()


    def get_parameters(self, config):
        return self.model.get_weights()

    def fit(self, parameters, config):
        self.model.set_weights(parameters)

        # wait until the image is available
        image, label, json_file = self.get_image()

        image = np.expand_dims(image, axis=0)
        label = np.array([label])

        self.image = image
        self.label = label

        epochs = int(self.config["model"]["epochs"])
        
        data_gen = ImageDataGenerator(
            rotation_range=15,
            width_shift_range=0.1,
            height_shift_range=0.1,
            zoom_range=0.1,
            horizontal_flip=True,
            fill_mode='nearest'
        )
        
        for epoch in range(epochs):
            for X_batch, y_batch in data_gen.flow(image, label, batch_size=1):
                self.model.train_on_batch(X_batch, y_batch)
                break  # As there's only one image, break after processing it once

        # delete file after training
        os.remove(json_file)

        return self.model.get_weights(), len(image), {}


    def evaluate(self, parameters, config):
        self.model.set_weights(parameters)
        loss, accuracy = self.model.evaluate(self.image, self.label)
        return loss, len(self.image), {"accuracy": float(accuracy),"loss": float(loss)}


    def set_parameters(self, parameters):
        return self.model.set_weights(parameters)  

    def get_model(self):
    	return self.model
        
    def compile(self):
        input_shape = parse_tuple(self.config["geo_classifier"]["input_shape"])
        model = Sequential()
        model.add(Conv2D(32, (3, 3), activation='relu', input_shape=input_shape))
        model.add(MaxPooling2D((2, 2)))
        model.add(Conv2D(64, (3, 3), activation='relu'))
        model.add(MaxPooling2D((2, 2)))
        model.add(Flatten())
        model.add(Dense(64, activation='relu'))
        model.add(Dense(1, activation='sigmoid'))
        lr_schedule = tf.keras.optimizers.schedules.ExponentialDecay(
            initial_learning_rate=0.001,
            decay_steps=10000,
            decay_rate=0.9)
        # Compile the model with the learning rate scheduler
        optimizer = tf.keras.optimizers.Adam(learning_rate=lr_schedule)
        model.compile(optimizer=optimizer, loss='binary_crossentropy', metrics=["accuracy"])
        self.model = model
        
        
    def get_image(self):
        
        client_id = 1
        print(f"Waiting for base64 image from the robot {client_id} ..")
        while True:
            json_files = glob.glob("services/flowers/data/*.json")
            if len(json_files) > 0:
                break
            time.sleep(10)


        json_file = json_files[0]
        print(json_file)
        # decode and reshape img
        img,label = decode_base64(json_file)

        # delete file
        img_preprocessed = preprocess_img(img,"side")

        # Return the preprocessed image and its label as a tuple
        return img_preprocessed, int(label), json_file

    
    def save(self,weights,rnd):
        self.model.set_weights(weights)
        n_clients = self.config["fl"]["clients"]
        epochs = self.config["model"]["epochs"]
        model_path = f"services/flowers/data/v6_geo_anom_classifier_{n_clients}clients_{rnd}rounds_{epochs}epochs.h5"
        self.model.save(model_path)

