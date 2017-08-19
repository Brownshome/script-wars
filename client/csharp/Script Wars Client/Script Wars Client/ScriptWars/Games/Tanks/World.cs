using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using ScriptWars.Connection;

namespace ScriptWars.Games.Tanks
{
    /// <summary>
    /// The world map.
    /// </summary>
    public class World
    {
        /// <summary>
        /// The width of the map in squares.
        /// </summary>
        public int Width => _map.GetLength(0);

        /// <summary>
        /// The height of the map in squares.
        /// </summary>
        public int Height => _map.GetLength(1);

        /// <summary>
        /// Positions of all ammo packs on the map.
        /// </summary>
        public ICollection<Coordinates> AmmoLocations { get; }

        /// <summary>
        /// All <see cref="Tank"/> visible to the client.
        /// </summary>
        public ICollection<Tank> Tanks { get; }

        /// <summary>
        /// All <see cref="Shot"/> visible to the client.
        /// </summary>
        public ICollection<Shot> Shots { get; }

        private readonly bool[,] _map;
        private readonly IDictionary<Coordinates, Tank> _tankMap = new Dictionary<Coordinates, Tank>();

        /// <summary>
        /// Read the game world from the server tick response.
        /// </summary>
        /// <param name="network"><see cref="Network"/> to use to fetch data.</param>
        public World(Network network)
        {
            // Map dimensions
            int width = network.ReadByte();
            int height = network.ReadByte();

            _map = new bool[width, height];

            // Wall positions
            for (var i = 0; i < _map.GetLength(0); i++)
            for (var j = 0; j < _map.GetLength(1); j++)
                _map[i, j] = network.ReadBool();

            // Tanks
            int tankCount = network.ReadByte();
            for (var i = 0; i < tankCount; i++)
            {
                var tank = new Tank(network);
                _tankMap.Add(tank.Position, tank);
            }
            Tanks = new ReadOnlyCollection<Tank>(_tankMap.Values.ToList());

            // Shots
            int shotCount = network.ReadByte();
            var shots = new List<Shot>();
            for (var i = 0; i < shotCount; i++)
            {
                var shot = new Shot(network);
                shots.Add(shot);
            }
            Shots = new ReadOnlyCollection<Shot>(shots);

            // Ammo
            int ammoCount = network.ReadByte();
            var ammoLocations = new List<Coordinates>();
            for (var i = 0; i < ammoCount; i++)
            {
                var ammo = new Coordinates(network);
                ammoLocations.Add(ammo);
            }
            AmmoLocations = new ReadOnlyCollection<Coordinates>(ammoLocations);
        }

        /// <summary>
        /// Get the <see cref="Tank"/> at a particular position.
        /// </summary>
        /// <param name="position">Position to look for a <see cref="Tank"/> at.</param>
        /// <returns><see cref="Tank"/> data or null if there is no tank at <paramref name="position"/>.</returns>
        public Tank GetTank(Coordinates position)
        {
            return _tankMap.TryGetValue(position, out var tank) ? tank : null;
        }

        /// <summary>
        /// Check if a particular position is a wall or open ground.
        /// </summary>
        /// <param name="position">Position to check for wall block.</param>
        /// <returns>Whether or not <paramref name="position"/> holds a wall.</returns>
        public bool IsWall(Coordinates position)
        {
            return _map[position.X, position.Y];
        }
    }
}