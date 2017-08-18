#Gonna try and rewrite James' network class, so I can use scriptwars in Python
#Built by Jonathan Mace
#With James Brown watching over my shoulder like the beautiful coding angel he is

import socket
import struct

class Network:
    def __init__(self, netID, ip, name):
        self.bit = 0x100
        self.positionOfByte = 0
        
        self.dataOut = bytearray()
        
        #sendString(name)
        self.ID = netID
        self.IP = ip
    
        protocol = netID >> 16 & 0xff
        if protocol == 1:
            try:
                self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
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
        self.dataOut = self.buildDataOut(self.ID, name)
        
    
    def nextTick(self):
        self.sock.sendto(bytearray(self.dataOut), (self.IP, 35565))
        self.dataOut = bytearray()
        self.dataOut += (self.ID).to_bytes(4,'big')
        
        self.dataIn = self.sock.recv(1024)
        if self.dataIn == '':
            return False
        
        self.position = 0
    
        #Get the first byte and see if there are errors
        errors = {1:"DISCONNECTED: The connection was terminated by the end of the game",
                  2: "FAILED_TO_KEEP_UP: The connection was terminated because the we couldn't keep up with the game tick"
                  }
        headerByte = self.getByte()
        if headerByte in [1,2]:
            print("Error: {}".format(errors[headerByte]))
            return False
        elif headerByte == 255:
            print("Error: {}".format(self.getString()))
            return False
        elif headerByte == 0:
            return True
        else:
            print("I told you James I should put this in here")
            print("There's been a header byte we didn't plan for")
            print("Please call your nearest James")
            return False
        
        return self.dataIn != ''
    
    
    #Converts a string (or number or whatever) into a list of its characters. as bytes!!
    #Then adds it to data out
    def sendString(self, name):
        self.dataOut += (len(bytes(name, 'utf-8'))).to_bytes(2,'big')
        self.dataOut += bytearray(name, 'utf-8')
    
    def sendByte(self, byte):
        self.dataOut += byte.to_bytes(1,'big')
    
    def sendInt(self, i):
        self.dataOut += i.to_bytes(4,'big')
    
    def sendFloat(self, f):
        self.data += struct.pack('>f', f)
    
    def sendData(self, data):
        self.dataOut += data
    
    #This adds your personal ID to the dataOut
    def putHeader(self, ID):
        self.dataOut.append(ID)
    
    #Adds the ID to the start of the dataOut; something needed for every packet
    def add_id(self, ID):
        self.dataOut.append(ID.to_bytes(4,'big'))
    
    #First packet has just name
    #This is first thing sent, dataOut does not yet have ID added to it
    #So we need to add ID first
    def buildDataOut(self, ID, name):
    	x = bytearray(ID.to_bytes(4,'big'))
    	x += (len(bytes(name, 'utf-8'))).to_bytes(2,'big')
    	x += bytearray(name, 'utf-8')
    	return x
    
    '''
     * Gets a single byte from the data. This byte is returned as an integer in the
     * range 0-255
     * @return An integer containing the read byte.
     */
    '''
    
    def getByte(self):
        b = self.dataIn[self.position]
        self.position += 1
        return b
    
    
    def getInt(self):
        i = struct.unpack('>i', self.dataIn[self.position: self.position + 4])[0]
        self.position += 4
        return i
    
    def getString(self):
        length = struct.unpack('>h', self.dataIn[self.position:self.position+2])[0]
        self.position += 2
        string = self.dataIn[self.position: self.position + length]
        return string.decode()
    
    def getBoolean(self):
        if(self.bit == 0x100 or self.positionOfByte != self.position - 1):
            self.bit = 1
            self.positionOfByte = self.position
            self.position += 1
        	
        currentByte = self.dataIn[self.positionOfByte]
        boolean = (currentByte & self.bit) != 0
        self.bit <<= 1
        
        return boolean
    
    def getFloat(self):
        i = struct.unpack('>f', self.dataIn[self.position])[0]
        self.position += 4
        return i
    
    def getData(self):
        return self.dataOut
    
    def getPointer(self):
        return self.position
    
    def movePointer(self, n=1):
        self.position += n
    
    def hasData():
        return self.position < len(self.dataOut)
