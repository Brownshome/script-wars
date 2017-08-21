from Network import *

class Action:
    def __init__(self, code):
        self.code = code

Action.NOTHING = Action(0)
Action.MOVE = Action(1)
Action.SHOOT = Action(2)

class Coordinates:
    def __init__(self, network=None):
        if network is not None:
            self.x = network.getByte()
            self.y = network.getByte()
        
    def __hash__(self):
        return self.x ^ self.y
        
    def __eq__(self, other):
        return (self.x, self.y) == (other.x, other.y)
        
    def __ne__(self, other):
        return not(self == other)

class Direction:
    def __init__(self, dx, dy, code):
        self.dx = dx
        self.dy = dy
        self.code = code

    def move(self, coord):
        c = Coordinates()
        c.x = coord.x + self.dx
        c.y = coord.y + self.dy
        return c

    def opposite(self):
        return Direction.values[(self.code + 2) % 4]

    def clockwise(self):
        return Direction.values[(self.code + 3) % 4]

    def anitClockwise(self):
        return Direction.values[(self.code + 1) % 4]

    def getDirection(dx, dy):
        if(x > 0 and y == 0):
            return Direction.RIGHT
        if(x < 0 and y == 0):
            return Direction.LEFT
        if(x == 0 and y < 0):
            return Direction.UP
        if(x == 0 and y > 0):
            return Direction.DOWN

        return None;

    def getDirection(toPos, fromPos):
        return getDirection(toPos.x - fromPos.x, toPos.y - fromPos.y)

Direction.UP = Direction(0, -1, 0)
Direction.LEFT = Direction(-1, 0, 1)        
Direction.DOWN = Direction(0, 1, 2)
Direction.RIGHT = Direction(1, 0, 3)

Direction.values = [Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT]

class Shot:
    def __init__(self, network):
        self.position = Coordinates(network)
        self.direction = Direction.values[network.getByte()]

class Tank:
    def __init__(self, network):
        self.position = Coordinates(network)
        self.clientID = network.getByte()

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
