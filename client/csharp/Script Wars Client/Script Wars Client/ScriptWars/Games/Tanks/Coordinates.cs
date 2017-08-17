using ScriptWars.Connection;

namespace ScriptWars.Games.Tanks
{
    public class Coordinates
    {
        public readonly int X;
        public readonly int Y;

        public Coordinates(int x, int y)
        {
            X = y;
            Y = y;
        }

        public Coordinates(Network network) : this(network.ReadByte(), network.ReadByte())
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