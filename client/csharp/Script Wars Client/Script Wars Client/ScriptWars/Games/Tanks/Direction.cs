using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;

namespace ScriptWars.Games.Tanks
{
    public enum Direction
    {
        [DirectionData(0, -1)] Up,
        [DirectionData(-1, 0)] Left,
        [DirectionData(0, 1)] Down,
        [DirectionData(1, 0)] Right
    }

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
        private static IEnumerable<T> GetAttributes<T>(Enum value) where T : Attribute
        {
            var fieldInfo = value.GetType().GetRuntimeField(value.ToString());
            return fieldInfo.GetCustomAttributes(typeof(T)).Cast<T>();
        }
        
        internal static DirectionData GetDirectionData(this Direction direction)
        {
            return GetAttributes<DirectionData>(direction).First();
        }
        
        public static int MoveX(this Direction direction, int x)
        {
            return x + direction.GetDirectionData().Dx;
        }

        public static int MoveY(this Direction direction, int y)
        {
            return y + direction.GetDirectionData().Dy;
        }

        public static Coordinates Move(this Direction direction, Coordinates coordinates)
        {
            return new Coordinates(direction.MoveX(coordinates.X), direction.MoveY(coordinates.Y));
        }

        public static Direction Opposite(this Direction direction)
        {
            return (Direction) ((int)direction + 2 % 4);
        }

        public static Direction Clockwise(this Direction direction)
        {
            return (Direction) ((int)direction + 3 % 4);
        }
        
        public static Direction Anticlockwise(this Direction direction)
        {
            return (Direction) ((int)direction + 1 % 4);
        }

        public static Direction? GetDirection(int x, int y)
        {
            if(x > 0 && y == 0) return Direction.Right;
            if(x < 0 && y == 0) return Direction.Left;
            if(x == 0 && y < 0) return Direction.Up;
            if(x == 0 && y > 0) return Direction.Down;

            return null;
        }

        public static Direction? GetDirection(Coordinates targetPosition, Coordinates myPosition)
        {
            return GetDirection(targetPosition.X - myPosition.X, targetPosition.Y - myPosition.Y);
        }
    }
}