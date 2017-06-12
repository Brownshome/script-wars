#Gonna try and rewrite James' network class, so I can use scriptwars in Python
#Built by Jonathan Mace
#With James Brown watching over my shoulder like the beautiful coding angel he is

import socket
import struct
#I'm going to try and just bytes() this at the end before sending
#Though might need something more fancier, like start off with a byte array or smthng
dataOut = bytearray()
dataIn = []
#I feel like I shouldn't do this?
#Sets some global variables
ID = 0
bit = 0x100
positionOfByte = 0
IP = 'www.script-wars.com'
def connect(netID, ip, name):
    global ID
    global sock
    global IP
    global dataOut
    
    #sendString(name)
    ID = netID
    IP = ip
    
    protocol = netID >> 16 & 0xff
    if protocol == 1:
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        except:
            print("Unable to connect!")
                                     
    elif protocol == 2:
        try:
            print("TCP shit. as yet, not done")
        except:
            print("Unable to connect!")
            
    else:
        print("Invalid ID")

    #Create the string shit to send
    dataOut = buildDataOut(ID, name)
    

def nextTick():
    global dataOut
    global dataIn
    sock.sendto(bytearray(dataOut), (IP, 35565))
    dataOut = bytearray()
    dataOut += (ID).to_bytes(4,'big')
    
    dataIn = sock.recv(1024)
    if dataIn == '':
        return False
    
    global position
    position = 0

    #Get the first byte and see if there are errors
    errors = {1:"DISCONNECTED: The connection was terminated by the end of the game",
              2: "FAILED_TO_KEEP_UP: The connection was terminated because the we couldn't keep up with the game tick"
              }
    headerByte = getByte()
    if headerByte in [1,2]:
        print("Error: {}".format(errors[headerByte]))
        return False
    elif headerByte == 255:
        print("Error: {}".format(getString()))
        return False
    elif headerByte == 0:
        return True
    else:
        print("I told you James I should put this in here")
        print("There's been a header byte we didn't plan for")
        print("Please call your nearest James")
        return False
    
    return dataIn != ''


#Converts a string (or number or whatever) into a list of its characters. as bytes!!
#Then adds it to data out
def sendString(name):
    global dataOut
    dataOut += (len(bytes(name, 'utf-8'))).to_bytes(2,'big')
    dataOut += bytearray(name, 'utf-8')

def sendByte(byte):
    global dataOut
    dataOut += byte.to_bytes(1,'big')

def sendInt(i):
    global dataOut
    dataOut += i.to_bytes(4,'big')

def sendFloat(f):
    global dataOut
    data += struct.pack('>f', f)

def sendData(data):
    global dataOut
    dataOut += data
    


#This adds your personal ID to the dataOut
def putHeader(ID):
    dataOut.append(ID)
    return

#Adds the ID to the start of the dataOut; something needed for every packet
def add_id(ID):
    dataOut.append(ID.to_bytes(4,'big'))

'''
#this is debugging shit. ignore.
#sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
#sock.connect((ip, 35565))
def sendstuff():
    global sock
    global IP
    sock.sendto(x, (IP, 35565))
    data = sock.recv(1024)
    print(data)
    '''



#First packet has just name
#This is first thing sent, dataOut does not yet have ID added to it
#So we need to add ID first
def buildDataOut(ID, name):
	x = bytearray(ID.to_bytes(4,'big'))
	x+= (len(bytes(name, 'utf-8'))).to_bytes(2,'big')
	x += bytearray(name, 'utf-8')
	return x



'''
 * Gets a single byte from the data. This byte is returned as an integer in the
 * range 0-255
 * @return An integer containing the read byte.
 */
'''

def getByte():
    global position
    b = dataIn[position]
    position += 1
    return b


def getInt():
    global position
    i = struct.unpack('>i', dataIn[position: position + 4])[0]
    position += 4
    return i

def getString():
    global position
    length = struct.unpack('>h', dataIn[position:position+2])[0]
    position += 2
    string = dataIn[position: position + length]
    return string.decode()

def getBoolean():
    global bit
    global positionOfByte
    global position
    if(bit == 0x100 or positionOfByte != position - 1):
        bit = 1
        positionOfByte = position
        position += 1
    	
    currentByte = dataIn[positionOfByte]
    boolean = (currentByte & bit) != 0
    bit <<= 1
    
    return boolean

def getFloat():
    global position
    i = struct.unpack('>f', dataIn[position])[0]
    position += 4
    return i

def getData():
    global dataOut
    return dataOut

def getPointer():
    global position
    return position

def movePointer(n=1):
    global position
    position += n

def hasData():
    global dataOut
    return position < len(dataOut)


'''
To write code:
while nextTick()
then James said it should do everything you expect it to

'''

