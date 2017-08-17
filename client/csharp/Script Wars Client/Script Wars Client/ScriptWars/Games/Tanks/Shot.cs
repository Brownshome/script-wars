using ScriptWars.Connection;

namespace ScriptWars.Games.Tanks
{
    public class Shot
    {
        public const int Speed = 3;
        
        public Coordinates Position { get; }
        public Direction Direction { get; }
        
        public Shot(Network network)
        {
            Position = new Coordinates(network);
            Direction = (Direction) network.ReadByte();
        }
    }
}
