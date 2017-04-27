#Gonna try and rewrite James' network class, so I can use scriptwars in Python
import socket
#I'm going to try and just bytes() this at the end before sending
#Though might need something more fancier, like start off with a byte array or smthng
dataOut = []
dataIn = []
#I feel like I shouldn't do this?
global ID = -1
#ip = 52.65.69.217
def connect(netID, ip, name):
    if ID !=1:
        print("More than one connection is not allowed! whoopsies")
        return;
    
    ID = netID
    
    dataOut = []
    dataOut.append(netID)
    sendString(name)

    protocol = netID >> 16 & 0xff
    if protocol == 1:
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            sock.connect((ip, 35565))
        except:
            print("Unable to connect!")
                                     
    elif protocol == 2:
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.connect((ip, 35565))
        except:
            print("Unable to connect!")
            
    else:
        print("Invalid ID")

def nextTick():
    #send dataOut
    sock.send(bytearray(dataOut))
    

    #wait for dataIn

    return dataIn != null


#Converts a string into a list of its characters. as bytes!!
#Then adds it to data out
def sendString(name):
    stringBytes = [b for b in bytearray(name, 'utf-8')]
    dataOut.append(len(name))
    dataOut.extend(stringBytes)
    
    
