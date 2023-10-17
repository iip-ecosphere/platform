import flwr as fl
from services.flowers.Client import Client
from argparse import ArgumentParser
import configparser
from services.flowers.utils import bcolors, truncate, load_pretrained_weights
import numpy as np
from flwr.common.typing import Parameters
from flwr.common import ndarrays_to_parameters



class Strategy(fl.server.strategy.FedAvg):
    
    def __init__(self, min_evaluate_clients, min_available_clients, min_fit_clients, on_evaluate_config_fn, on_fit_config_fn):
        super().__init__(min_evaluate_clients=min_evaluate_clients
                         ,min_available_clients =  min_available_clients
                         ,min_fit_clients = min_fit_clients
                         ,on_evaluate_config_fn =on_evaluate_config_fn
                         ,on_fit_config_fn = on_fit_config_fn)
        
        self.config = configparser.ConfigParser()
        self.config.read('services/flowers/config.ini')
        self.ACCELERATION_WEIGHTS_PATH = 'services/flowers/pretrained_weights/200_rounds_weights.npy'
    
    def aggregate_evaluate(self,rnd: int,results,failures,):    
        """Aggregate evaluation losses using weighted average."""
            
        # Weigh accuracy of each client by number of examples used
        accuracies = [r.metrics["accuracy"] * r.num_examples for _, r in results]
        losses = [r.metrics["loss"] *  r.num_examples for _, r in results]
        examples = [r.num_examples for _, r in results]

        # Aggregate and print custom metric
        aggregated_accuracy = sum(accuracies) / (sum(examples) + 1) #just to prevent div. by 0
        str_aggr_accuracy = truncate(aggregated_accuracy*100,2)
        print(bcolors.OKGREEN + f"ROUND {rnd} ACCURACY: {str_aggr_accuracy}"+ bcolors.ENDC)
        return super().aggregate_evaluate(rnd, results, failures)

    def save_model(self, aggregated_weights,rnd):

        model = Client(None, self.config)

        weights = fl.common.parameters_to_ndarrays(aggregated_weights[0])
        model.save(weights,rnd)

    def aggregate_fit(self,rnd: int,results,failures):
        rounds_per_save = int(self.config["fl"]["rounds_per_save"])
        aggregated_weights = super().aggregate_fit(rnd, results, failures)
        if(aggregated_weights is not None and  rnd > 0 and rnd%rounds_per_save==0):
            self.save_model(aggregated_weights,rnd)
            
        if (int(self.config["fl"]["acceleration"]) == 1 and rnd == int(self.config["fl"]["acceleration_round"])):
            loaded_weights = load_pretrained_weights(self.ACCELERATION_WEIGHTS_PATH)
            loaded_weights_list = list(loaded_weights)
            parameters = ndarrays_to_parameters(loaded_weights_list)
            aggregated_weights = parameters,{}

        return aggregated_weights
