import flwr as fl

class Client(fl.client.NumPyClient):
    
    def get_parameters(self, config):
        """flower method needed for training"""

    def fit(self, parameters, config):
        """method where the trainnig procedure is mentioned"""

    def evaluate(self, parameters, config):
        """Method to evalute the training results"""

    def set_parameters(self, parameters):
        """Needed to set the weights to the most recent version"""    

    def get_model(self):
    	"""return your model here"""
