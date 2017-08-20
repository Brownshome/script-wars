using ScriptWars.Connection;

namespace ScriptWars.Games.Tanks
{
    /// <summary>
    /// A single map position.
    /// </summary>
    public class Coordinates
    {
        public readonly int X;
        public readonly int Y;

        /// <summary>
        /// Create a coordinate from its map position.
        /// </summary>
        /// <param name="x">X coordinate.</param>
        /// <param name="y">Y coordinate.</param>
        public Coordinates(int x, int y)
        {
            X = x;
            Y = y;
        }

        /// <summary>
        /// Read a coordinate from the server tick response.
        /// </summary>
        /// <param name="network"><see cref="Network"/> to use to fetch data.</param>
        internal Coordinates(Network network) : this(network.ReadByte(), network.ReadByte())
        {
        }

        #region Equality

        protected bool Equals(Coordinates other)
        {
            return X == other.X && Y == other.Y;
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != GetType()) return false;
            return Equals((Coordinates) obj);
        }

        public override int GetHashCode()
        {
            unchecked
            {
                return X ^ (Y * 31);
            }
        }

        #endregion

        public override string ToString()
        {
            return $"({X}, {Y})";
        }
    }
}