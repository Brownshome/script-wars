from Network import *
from Action import *
from Direction import *
from Coordinates import *
from World import *

class TankAPI:
    def __init__(self, netID, ip, name):
        self.network = Network(netID, ip, name)
        self.action = Action.NOTHING
        
    def setSendData(self):
        self.network.sendByte(self.action.code)
        if not self.action is Action.NOTHING:
            self.network.sendByte(self.direction.code)
            
        self.action = Action.NOTHING
        self.direction = None
    
    def nextTick(self):
        self.setSendData()
        
        if not self.network.nextTick():
            return False
            
        self.isAlive = self.network.getByte() is 1
        if self.isAlive:
            self.ammo = self.network.getByte()
            self.currentPosition = Coordinates(self.network)
            self.map = World(self.network)
        else:
            self.currentPosition = None
            self.map = None
            
        return True
        
    def move(self, direction):
        self.action = Action.MOVE
        self.direction = direction
        
    def shoot(self, direction):
        self.action = Action.SHOOT
        self.direction = direction
        
    def doNothing(self):
        self.action = Action.NOTHING
        self.direction = None
        
    def printAction(self):
        if self.action is Action.NOTHING:
            print("NOTHING")
        else:
            print('{} {}'.format(self.action, self.direction))
