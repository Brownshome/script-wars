using ScriptWars.Connection;

namespace ScriptWars.Games.Tanks
{
    /// <summary>
    /// A tank on the <see cref="World"/>.
    /// </summary>
    public class Tank
    {
        /// <summary>
        /// Current tick position of the tank.
        /// </summary>
        public Coordinates Position { get; }

        /// <summary>
        /// ID of the tank owner.
        /// </summary>
        public int ClientId { get; }

        /// <summary>
        /// Read a tank from the server tick response.
        /// </summary>
        /// <param name="network"><see cref="Network"/> to use to fetch data.</param>
        internal Tank(Network network)
        {
            Position = new Coordinates(network);
            ClientId = network.ReadByte();
        }
    }
}