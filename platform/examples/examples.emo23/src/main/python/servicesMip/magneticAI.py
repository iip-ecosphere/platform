import numpy as np
import pickle
import pandas as pd
import logging
logging.getLogger('tensorflow').setLevel(logging.FATAL) # suppress tf logs during prediction ;)
import tensorflow as tf
# additional libraries
from scipy.signal import find_peaks,savgol_filter
import ast
import os

class MagneticAI:
    def __init__(self):
        # load model
        print(">>>>>>>>>>>>>>>>>>Loading!?")
        currentDir = os.path.dirname(os.path.realpath(__file__))
        model_path = currentDir + "/mipFiles/magnetic_model.h5"
        scaler_path = currentDir + "/mipFiles/scaler.pickle"
        print(model_path)
        self.model = tf.keras.models.load_model(model_path)
        print(scaler_path)
        # load scaler
        with open(scaler_path, 'rb') as f:
            self.scaler = pickle.load(f)
        print(">>>>>>>>>>>>>>>>>>Loaded")

    def preprocess(self, data):
        # make dataframe
        clock_stream = data._mipraw_signal_clock
        raw_clock_stream = data._mipraw_signal_data1
        
        tag_id = data._mipid_tag
        clock_data = ast.literal_eval(str(clock_stream))
        
        raw_data = ast.literal_eval(str(raw_clock_stream))
        clock_data = np.array(clock_data)
        raw_data = np.array(raw_data)
        dataframe = pd.DataFrame({'C': clock_data, 'D': raw_data})
        ## find peaks and valleys in clock signal
        peaks,valleys = self.get_peaks_and_valleys(dataframe)
        
        ## get the sequences based on peaks and valleys
        sequences = []
        
        if (len(valleys)==13):
            print("Working on signal")
            digital_df = pd.DataFrame({
                'D':dataframe['D'],
            })
            clock_df = pd.DataFrame({
                'C':dataframe['C'],
            })

            # get sliding window sequences
            digital_sequences = self.get_sequences(digital_df,peaks,valleys)
            clock_sequences = self.get_sequences(clock_df,peaks,valleys)
            i=0
            for sequence in digital_sequences:                
                extracted_seq= np.array(sequence.values)
                extracted_seq = self.scaler.transform(extracted_seq)
                sequences.append(extracted_seq)
                i+=1

        else:
            print("NOT Working on signal")
            print(f"Bad signal, please rescan! -> {tag_id}")
        
        return sequences
    
    def predict(self, data):
        try:
            sequences = self.preprocess(data)
            sequences = np.array(sequences)
            binary_str = ""
            predictions = self.model.predict(sequences)
            for prediction in predictions:
                if prediction >=0.5:
                    binary_str += str(1)
                else:
                    binary_str += str(0)
        except:
            binary_str ="0"
        return binary_str
        
    def get_sequences(self,df,peaks,valleys):
        # concat valleys and peaks indices in order
        p_v_indices = []
        for v, p in zip(valleys, peaks):
            p_v_indices.append(v)
            p_v_indices.append(p)
        p_v_indices.append(valleys[-1])
        sliding_window_size = 3
        sequences_indices = self.sliding_window_with_overlap_indices(p_v_indices,sliding_window_size)
        sequences_indices = sequences_indices[5:] # we disregard the first 6 sequences as they are constant 010101
        sequences = []
        for start, end in sequences_indices:
            if start >= 5 and end <= len(df):
                mid_point = (start+end)//2
                sequence = df.iloc[mid_point-10:mid_point+10]
                sequences.append(sequence)
            else:
                print(f"Skipping invalid indices: start={start}, end={end}")

        return sequences
    
    def get_peaks_and_valleys(self,df):
        clock_signal = df['C'].values
        window_length = 71  
        polyorder = 3  
        smoothed_clock_signal = savgol_filter(clock_signal, window_length, polyorder)
        dy = np.diff(smoothed_clock_signal) # compute the first difference (derivative approximation)
        sharpness_threshold = 0.002   # sharpness threshold
        valleys, _ = find_peaks(-smoothed_clock_signal, height=-300, width=20) # find all valleys first        
        valleys = [valley for valley in valleys if dy[valley - 1] < -sharpness_threshold and dy[valley] > sharpness_threshold]  # filter out non-sharp valleys
        valleys = valleys[:13]
        peaks = [(valleys[i] + valleys[i + 1]) // 2 for i in range(len(valleys) - 1)] 
        return peaks,valleys
    
    def sliding_window_with_overlap_indices(self,arr, window_size):
        result = []
        for i in range(0, len(arr) - window_size + 1):
            start_index = arr[i]
            stop_index = arr[i + window_size - 1]
            result.append((start_index, stop_index))

        return np.array(result)