from datatypes.MipMqttOutput import MipMqttOutput
from datatypes.MipMqttOutputImpl import MipMqttOutputImpl
from datatypes.MipMqttInput import MipMqttInput
from datatypes.MipMqttInputImpl import MipMqttInputImpl
from datatypes.MipAiPythonOutput import MipAiPythonOutput
from datatypes.MipAiPythonOutputImpl import MipAiPythonOutputImpl
from interfaces.MipAiPythonServiceInterface import MipAiPythonServiceInterface

class MipAiPythonService(MipAiPythonServiceInterface):
    """Mip Ai Python Service implementing generated interface."""
    
    def __init__(self):
        """Initializes the service.""" 
        super().__init__()
        
    def processMipMqttOutput(self, data: MipMqttOutput):
        """Asynchronous data processing method. Use self.ingest(data) to pass the result back to the data stream.
    
        Parameters:
          - data -- the data to process
        """

        if data.mipcontext == 'read_idtag_data':
            resultAIOutput = MipAiPythonOutputImpl()
            resultAIOutput.aicontext = data.mipcontext
            resultAIOutput.aidate = data.mipdate
            resultAIOutput.aifrom = data.mipfrom
            resultAIOutput.aiid_tag = data.mipid_tag
            resultAIOutput.aireader = data.mipreader
            resultAIOutput.airaw_signal_clock = data.mipraw_signal_clock
            resultAIOutput.airaw_signal_data1 = data.mipraw_signal_data1
            resultAIOutput.airaw_signal_data2 = data.mipraw_signal_data2
            self.ingest(resultAIOutput) # asynchronous processing, call ingest from parent 
 
            resultInput = MipMqttInputImpl()
            resultInput.mipcontext = "iip_echosphere_id_tag_data_format" 
            resultInput.mipdate = data.mipdate
            resultInput.mipto = data.mipfrom
            resultInput.mipfrom = "IIP_Ecosphere"
            resultInput.mipbitstream_ai_clock = "0101010101010101010101"
            resultInput.mipbitstream_ai_data1 = "0101011100010101110100"
            resultInput.mipbitstream_ai_data2 = "0000000000000000000000"
            resultInput.mipreader = data.mipreader 
            self.ingest(resultInput) # asynchronous processing, call ingest from parent  

        if data.mipcontext == 'AI_read_idtag_data':
            resultAIOutput = MipAiPythonOutputImpl()
            resultAIOutput.aicontext = "AI_MIP_Respond" 
            resultAIOutput.aidate = data.mipdate
            resultAIOutput.aifrom = "IIP_Ecosphere"
            resultAIOutput.aiid_tag = data.mipid_tag
            resultAIOutput.aireader = data.mipreader
            resultAIOutput.airaw_signal_clock = data.mipraw_signal_clock
            resultAIOutput.airaw_signal_data1 = data.mipraw_signal_data1
            resultAIOutput.airaw_signal_data2 = data.mipraw_signal_data2            
            self.ingest(resultAIOutput) # asynchronous processing, call ingest from parent 

#registers itself
MipAiPythonService()