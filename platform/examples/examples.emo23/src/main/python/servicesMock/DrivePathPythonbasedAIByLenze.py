import iip.Registry
from Version import Version
from Service import ServiceState
from Service import ServiceKind
from datatypes.LenzeDriveMeasurement import LenzeDriveMeasurement
from datatypes.LenzeDriveMeasurementImpl import LenzeDriveMeasurementImpl
from datatypes.AggregatedPlcEnergyMeasurement import AggregatedPlcEnergyMeasurement
from datatypes.AggregatedPlcEnergyMeasurementImpl import AggregatedPlcEnergyMeasurementImpl
from datatypes.DriveAiResult import DriveAiResult
from datatypes.DriveAiResultImpl import DriveAiResultImpl
from interfaces.DrivePathPythonbasedAIByLenzeInterface import DrivePathPythonbasedAIByLenzeInterface

import random

class DrivePathPythonbasedAIbyLenze(DrivePathPythonbasedAIByLenzeInterface):
    """Template service implementation for DrivepathPythonbasedAIbyLenze
       Generated by: EASy-Producer."""
       
    lastEnergy = None
    
    def __init__(self):
        """Initializes the service.""" 
        super().__init__()
    
    
    def processLenzeDriveMeasurement(self, data: LenzeDriveMeasurement):
        """Asynchronous data processing method. Use self.ingest(data) to pass the result back to the data stream.
    
        Parameters:
          - data -- the data to process
        """
        
        dataString = data.getPROCESS().getChannels()[3].getData().strip("[]") # Channel 4 (Torque)
        result = DriveAiResultImpl()
        result.io = True
        result.error = ['slippage', 'erosion', 'standstill']
        result.errorConfidence = [random.random(), random.random(), random.random()]
        result.aiId = self.getAiId()
        result.drive = data
        if self.lastEnergy is None:
            en = AggregatedPlcEnergyMeasurementImpl()
            en.channels = []
            result.energy = en
        else:
            result.energy = self.lastEnergy
        self.ingest(result)
        
    def processAggregatedPlcEnergyMeasurement(self, data: AggregatedPlcEnergyMeasurement):
        """Asynchronous data processing method. Use self.ingest(data) to pass the result back to the data stream.
    
        Parameters:
          - data -- the data to process
        """
        self.lastEnergy = data
    

#registers itself
DrivePathPythonbasedAIbyLenze()
