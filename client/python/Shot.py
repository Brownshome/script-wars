from Direction import *
from Coordinates import *

class Shot:
    def __init__(self, network):
        self.position = Coordinates(network)
        self.direction = Direction.values[network.getByte()]
