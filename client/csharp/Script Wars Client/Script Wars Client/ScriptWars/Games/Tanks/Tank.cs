using ScriptWars.Connection;

namespace ScriptWars.Games.Tanks
{
    public class Tank
    {
        public Coordinates Position { get; }
        public int ClientId { get; }
        
        public Tank(Network network)
        {
            Position = new Coordinates(network);
            ClientId = network.ReadByte();
        }
    }
}