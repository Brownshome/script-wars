from Tank import *
from Shot import *
from Coordinates import *

class World:
    def __init__(self, network):
        self.width = network.getByte()
        self.height = network.getByte()
        
        self.map = [[network.getBoolean() for x in range(self.width)] for y in range(self.height)]
        
        numberOfTanks = network.getByte()
        tankList = [Tank(network) for x in range(numberOfTanks)]
        self.tankDict = dict([(t.position, t) for t in tankList])
        
        numberOfShots = network.getByte()
        shotList = [Shot(network) for x in range(numberOfShots)]
        self.shotDict = dict([(s.position, s) for s in shotList])
        
        amountOfAmmo = network.getByte()
        self.ammoPickups = set([Coordinates(network) for x in range(amountOfAmmo)])

    def isWall(self, coord):
        return self.map[coord.y][coord.x]
