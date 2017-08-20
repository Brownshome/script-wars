using ScriptWars.Connection;

namespace ScriptWars.Games.Tanks
{
    /// <summary>
    /// A shot on the <see cref="World"/>.
    /// </summary>
    public class Shot
    {
        /// <summary>
        /// Shot movement per tick.
        /// </summary>
        public const int Speed = 3;

        /// <summary>
        /// Current tick position of the shot.
        /// </summary>
        public Coordinates Position { get; }

        /// <summary>
        /// <see cref="Direction"/> that the shot is moving in.
        /// </summary>
        public Direction Direction { get; }

        /// <summary>
        /// Read a shot from the server tick response.
        /// </summary>
        /// <param name="network"><see cref="Network"/> to use to fetch data.</param>
        internal Shot(Network network)
        {
            Position = new Coordinates(network);
            Direction = (Direction) network.ReadByte();
        }
    }
}