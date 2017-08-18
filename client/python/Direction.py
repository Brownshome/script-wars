from Coordinates import *

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
