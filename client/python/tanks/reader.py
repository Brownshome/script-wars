#Reads the dataIn stream for tanks and actually makes it readable and shit
from ..Network import getByte, getInt, getString, getBoolean


#This assumes there have been no connection errors etc
#The very first byte is already gotten in nextTick

#Returns a dictionary of the variables
'''
isAlive
xPos
yPos
grid
enemies
shots
'''
def getData():
    isAlive = bool(getByte())
    
    xPos = getByte()
    yPos = getByte()
    
    gridWidth = getByte()
    gridHeight = getByte()
    
    grid = [[getBoolean() for x in range(gridWidth)] for y in range(gridHeight)]

    numEnemies = getByte()

    enemies = [(getByte(),getByte()) for x in range(numEnemies)]
    '''
    coding angel said this was unneeded
    enemies = []
    if numVisible > 0:
        #List of tuples of enemies' (x,y) coordinates
        #Only populated if 1 or more enemies
        enemies = [(getByte(),getByte()) for x in range(numVisible)]
    '''

    numShots = getByte()
    shots = [(getByte(),getByte(),getByte()) for x in range(numShots)]
    #For future ref, example usage:
    #d.get((1,1),-1). Is there a shot at this position? if -1 no, else its direction

    return {
        'isAlive':isAlive,
        'xPos':xPos,
        'yPos':yPos,
        'grid':grid,
        'enemies':enemies,
        'shots':shots
        }
    
