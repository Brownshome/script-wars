#!/usr/bin/python

from TankAPI import *
from Network import *
from Coordinates import *
from Tank import *
from random import randint

tankAPI = TankAPI(65536, "www.script-wars.com", "Python Tank");

while tankAPI.nextTick():
    if not tankAPI.isAlive:
        continue

    coord = tankAPI.currentPosition

    for d in Direction.values:
        if not tankAPI.map.isWall(d.move(coord)):
            tankAPI.move(d)
            
    for t in tankAPI.map.tankDict.values():
        ray = Direction.getDirection(t.position, coord)
        if not ray is None:
            tankAPI.shoot(ray)
            
    for s in tankAPI.map.shotDict.values():
        ray = Direction.getDirection(coord, s.position)
        if not ray is s.direction:
            tankAPI.shoot(ray.opposite())
