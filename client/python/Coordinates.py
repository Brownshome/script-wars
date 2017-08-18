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
