using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using ScriptWars.Connection;

namespace ScriptWars.Games.Tanks
{
    public class World
    {
        public bool[,] Map { get; }
        public ICollection<Coordinates> AmmoLocations { get; }
        public ICollection<Tank> Tanks { get; }
        public ICollection<Shot> Shots { get; }

        private readonly IDictionary<Coordinates, Tank> _tankMap = new Dictionary<Coordinates, Tank>();

        public World(Network network)
        {
            int width = network.ReadByte();
            int height = network.ReadByte();

            Map = new bool[width, height];

            for (var i = 0; i < Map.GetLength(0); i++)
            for (var j = 0; j < Map.GetLength(1); j++)
                Map[i, j] = network.ReadBool();

            int tankCount = network.ReadByte();
            for (var i = 0; i < tankCount; i++)
            {
                var tank = new Tank(network);
                _tankMap.Add(tank.Position, tank);
            }
            Tanks = new ReadOnlyCollection<Tank>(_tankMap.Values.ToList());

            int shotCount = network.ReadByte();
            var shots = new List<Shot>();
            for (var i = 0; i < shotCount; i++)
            {
                var shot = new Shot(network);
                shots.Add(shot);
            }
            Shots = new ReadOnlyCollection<Shot>(shots);

            int ammoCount = network.ReadByte();
            var ammoLocations = new List<Coordinates>();
            for (var i = 0; i < ammoCount; i++)
            {
                var ammo = new Coordinates(network);
                ammoLocations.Add(ammo);
            }
            AmmoLocations = new ReadOnlyCollection<Coordinates>(ammoLocations);
        }

        public Tank GetTank(Coordinates position)
        {
            return _tankMap.TryGetValue(position, out var tank) ? tank : null;
        }

        public bool IsWall(Coordinates position)
        {
            return Map[position.X, position.Y];
        }
    }
}
