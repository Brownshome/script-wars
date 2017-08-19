using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;

namespace ScriptWars.Games.Tanks
{
    /// <summary>
    /// Directions <see cref="Tank"/> and <see cref="Shot"/> can move in.
    /// </summary>
    public enum Direction
    {
        [DirectionData(0, -1)] Up,
        [DirectionData(-1, 0)] Left,
        [DirectionData(0, 1)] Down,
        [DirectionData(1, 0)] Right
    }

    /// <summary>
    /// Annotate X and Y velocities of a <see cref="Direction"/>.
    /// </summary>
    [AttributeUsage(AttributeTargets.Field)]
    internal class DirectionData : Attribute
    {
        public readonly int Dx;
        public readonly int Dy;

        public DirectionData(int dx, int dy)
        {
            Dx = dx;
            Dy = dy;
        }
    }

    public static class DirectionExtensions
    {
        /// <summary>
        /// Read an enum value's first attribute of type <typeparamref name="T"/>.
        /// </summary>
        /// <param name="value">Enum to read attribute of.</param>
        /// <typeparam name="T">Attribute type to read.</typeparam>
        /// <returns>Attribute data from that enum value.</returns>
        private static IEnumerable<T> GetAttributes<T>(Enum value) where T : Attribute
        {
            var fieldInfo = value.GetType().GetRuntimeField(value.ToString());
            return fieldInfo.GetCustomAttributes(typeof(T)).Cast<T>();
        }

        /// <summary>
        /// Get <see cref="DirectionData"/> attribute of a Direction enum value.
        /// </summary>
        /// <param name="direction">Direction to read attribute for.</param>
        /// <returns></returns>
        internal static DirectionData GetDirectionData(this Direction direction)
        {
            return GetAttributes<DirectionData>(direction).First();
        }

        /// <summary>
        /// Apply a <see cref="Direction"/> movement to an X coordinate.
        /// </summary>
        /// <param name="direction">Direction to apply.</param>
        /// <param name="x">Original coordinate.</param>
        /// <returns>Moved X coordinate.</returns>
        public static int MoveX(this Direction direction, int x)
        {
            return x + direction.GetDirectionData().Dx;
        }

        /// <summary>
        /// Apply a <see cref="Direction"/> movement to a Y coordinate.
        /// </summary>
        /// <param name="direction">Direction to apply.</param>
        /// <param name="y">Original coordinate.</param>
        /// <returns>Moved Y coordinate.</returns>
        public static int MoveY(this Direction direction, int y)
        {
            return y + direction.GetDirectionData().Dy;
        }

        /// <summary>
        /// Apply a direction movement to a <see cref="Coordinates" />.
        /// </summary>
        /// <param name="direction"></param>
        /// <param name="coordinates"></param>
        /// <returns></returns>
        public static Coordinates Move(this Direction direction, Coordinates coordinates)
        {
            return new Coordinates(direction.MoveX(coordinates.X), direction.MoveY(coordinates.Y));
        }

        /// <summary>
        /// Find the opposite of this <see cref="Direction"/>.
        /// </summary>
        /// <param name="direction">Direction to find opposite of.</param>
        /// <returns>Opposite direction.</returns>
        public static Direction Opposite(this Direction direction)
        {
            return (Direction) ((int) direction + 2 % 4);
        }

        /// <summary>
        /// Find the next clockwise <see cref="Direction"/> to <paramref name="direction"/>.
        /// </summary>
        /// <param name="direction">Direction to turn clockwise.</param>
        /// <returns>Clockwise direction.</returns>
        public static Direction Clockwise(this Direction direction)
        {
            return (Direction) ((int) direction + 3 % 4);
        }

        /// <summary>
        /// Find the next anti-clockwise <see cref="Direction"/> to <paramref name="direction"/>.
        /// </summary>
        /// <param name="direction">Direction to turn anti-clockwise.</param>
        /// <returns>Anti-clockwise direction.</returns>
        public static Direction Anticlockwise(this Direction direction)
        {
            return (Direction) ((int) direction + 1 % 4);
        }

        /// <summary>
        /// Convert an X and Y displacement vector to the corresponding <see cref="Direction"/>.
        /// </summary>
        /// <param name="x">X magnitude.</param>
        /// <param name="y">Y magnitude.</param>
        /// <returns><see cref="Direction"/> of vector, or null if (x, y) is not a cardinal direction.</returns>
        public static Direction? GetDirection(int x, int y)
        {
            if (x > 0 && y == 0) return Direction.Right;
            if (x < 0 && y == 0) return Direction.Left;
            if (x == 0 && y < 0) return Direction.Up;
            if (x == 0 && y > 0) return Direction.Down;

            return null;
        }

        /// <summary>
        /// Find the <see cref="Direction"/> to travel from <paramref name="myPosition"/> to 
        /// <paramref name="targetPosition"/>.
        /// </summary>
        /// <param name="myPosition">Original position.</param>
        /// <param name="targetPosition">Desired destination.</param>
        /// <returns>
        /// <see cref="Direction"/> from <paramref name="myPosition"/> to <paramref name="targetPosition"/>, or null if 
        /// <see cref="targetPosition"/> is not reachable via a cardinal direction.
        /// </returns>
        public static Direction? GetDirection(Coordinates myPosition, Coordinates targetPosition)
        {
            return GetDirection(targetPosition.X - myPosition.X, targetPosition.Y - myPosition.Y);
        }
    }
}