from Coordinates import *

class Tank:
    def __init__(self, network):
        self.position = Coordinates(network)
        self.clientID = network.getByte()
