from datatypes.MipMqttOutput import MipMqttOutput
from datatypes.MipMqttOutputImpl import MipMqttOutputImpl
from datatypes.MipMqttInput import MipMqttInput
from datatypes.MipMqttInputImpl import MipMqttInputImpl
from datatypes.MipAiPythonOutput import MipAiPythonOutput
from datatypes.MipAiPythonOutputImpl import MipAiPythonOutputImpl
from interfaces.MipAiPythonServiceInterface import MipAiPythonServiceInterface

import pickle
import tempfile
import os
import time
import traceback
from _datetime import date

class MipAiPythonService(MipAiPythonServiceInterface):
    """Mip Ai Python Service implementing generated interface."""
    
    """placeholder for the import of the Prediction AI, only relevant is the .predict() 
    function taking MipMqttOutput as arguemnt"""
    
    magneticAIInstance = None 
    var_mipraw_signal_clock = "0,0,0"
    var_mipraw_signal_data1 = "0,0,0"
    var_mipraw_signal_data2 = "0,0,0"
    var_aimip_id_tag = "0000"
    
    def __init__(self):
        """Initializes the service.""" 
        super().__init__()
        
    def processMipMqttOutput(self, data: MipMqttOutput):
        """Asynchronous data processing method. Use self.ingest(data) to pass the result back to the data stream.
    
        Parameters:
          - data -- the data to process
        """

        if data.mipcontext == 'read_idtag_data':
    
            #if not os.path.exists(fileName):
            #    os.makedirs(fileName)
            #fileToWrite = os.path.join(fileName + "/mipOutput" + str(time.time()) +  ".pickle")
            #with open (fileToWrite, 'wb') as f :
                #print("toWrite" , fileToWrite)
            #    pickle.dump(vars(data), f)
            
            predictedSequenceOr = ""
            predictedSequence = ""

            if (self.magneticAIInstance != None):

                predictedSequenceOr = self.magneticAIInstance.predict(data)
                predictedSequence = "010101" + predictedSequenceOr + "01"
            
            if (len(predictedSequenceOr) > 1):
 
                resultInput = MipMqttInputImpl()
                resultInput.mipcontext = "iip_echosphere_id_tag_data_format" 
                resultInput.mipdate = data.mipdate
                resultInput.mipto = data.mipfrom
                resultInput.mipfrom = "IIP_Ecosphere"
                resultInput.mipbitstream_ai_clock = "01010101010101010101010101"
                resultInput.mipbitstream_ai_data1 = predictedSequence
                resultInput.mipbitstream_ai_data2 = "00000000000000000000000000"
                resultInput.mipreader = data.mipreader 
                self.var_mipraw_signal_clock = data.mipraw_signal_clock
                self.var_mipraw_signal_data1 = data.mipraw_signal_data1
                self.var_mipraw_signal_data2 = data.mipraw_signal_data2
                self.var_aimip_id_tag = data.mipid_tag
                self.ingest(resultInput) # asynchronous processing, call ingest from parent  
            else:

                resultAIOutput = MipAiPythonOutputImpl()
                resultAIOutput.aicontext = data.mipcontext
                resultAIOutput.aidate = data.mipdate
                resultAIOutput.aifrom = data.mipfrom
                resultAIOutput.aiid_tag = "0000"
                resultAIOutput.aimip_id_tag = data.mipid_tag
                resultAIOutput.aireader = data.mipreader
                resultAIOutput.airaw_signal_clock = data.mipraw_signal_clock
                resultAIOutput.airaw_signal_data1 = data.mipraw_signal_data1
                resultAIOutput.airaw_signal_data2 = data.mipraw_signal_data2
                self.ingest(resultAIOutput) # asynchronous processing, call ingest from parent
        #print(self.magneticAI.predict(data));           

        if data.mipcontext == 'AI_read_id_tag_data':
            
            resultAIOutput = MipAiPythonOutputImpl()
            resultAIOutput.aicontext = data.mipcontext
            resultAIOutput.aidate = data.mipdate
            resultAIOutput.aifrom = "IIP_Ecosphere"
            resultAIOutput.aiid_tag = data.mipid_tag
            resultAIOutput.aireader = data.mipreader
            resultAIOutput.airaw_signal_clock = self.var_mipraw_signal_clock
            resultAIOutput.airaw_signal_data1 = self.var_mipraw_signal_data1
            resultAIOutput.airaw_signal_data2 = self.var_mipraw_signal_data2
            resultAIOutput.aimip_id_tag = self.var_aimip_id_tag           
            self.ingest(resultAIOutput) # asynchronous processing, call ingest from parent 
    
    def start(self):
        """Called when the server shall start.
        """
        try:

            from servicesMip.magneticAI import MagneticAI
            self.magneticAIInstance = MagneticAI()

        except Exception:
            traceback.print_exc()
            fileName = tempfile.gettempdir() + "/Log"
            if not os.path.exists(fileName):
                os.makedirs(fileName)
            fileToWrite = os.path.join(fileName + "/mipOutput.txt")
            with open (fileToWrite, 'wb') as f :
                print("toWrite" , fileToWrite)
            print("import error Magnetic not found Code")
        print("Starting Python Mip Service")
        pass


#registers itself
MipAiPythonService()