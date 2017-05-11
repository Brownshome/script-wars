#A wrapper to help control movement

from ..Network import sendByte


def doThing(action, direction):
    
    actions = {
        'no action': 0,
        'move':1,
        'shoot':2
        }

    directions = {
        'up':0,
        'down':1,
        'left':2,
        'right':3
        }

    sendByte(actions[action])
    sendByte(directions[direction])

