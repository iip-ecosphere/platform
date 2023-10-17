import flwr as fl
from services.flowers.Client import Client

class Strategy(fl.server.strategy.FedAvg):

    def __init__(self):
        self.super()
