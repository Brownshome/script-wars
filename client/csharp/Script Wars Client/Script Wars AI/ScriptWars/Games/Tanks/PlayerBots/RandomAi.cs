using System;

namespace ScriptWars.Games.Tanks.PlayerBots
{
    /// <summary>
    /// Example Script Wars tanks AI using the C# client.
    /// </summary>
    public class RandomAi
    {
        public static void Main(string[] args)
        {
            int id;

            // Try parse ID from args
            if (!(args.Length > 0 && int.TryParse(args[0], out id)))
            {
                // Try get ID from console input
                bool parsed;

                do
                {
                    Console.WriteLine("Enter ID: ");
                    parsed = int.TryParse(Console.ReadLine(), out id);
                } while (!parsed);
            }

            var ai = new RandomAi(id);
            ai.Run();
        }
        
        private readonly TankApi _api;
        private readonly Random _random = new Random();

        private RandomAi(int id)
        {
            _api = new TankApi(id, "www.script-wars.com", "C# Random AI");
        }

        private void Run()
        {
            var directionCount = Enum.GetValues(typeof(Direction)).Length;
            
            while (_api.NextTick())
            {
                if (!_api.IsAlive)
                {
                    // Dead this tick
                    continue;
                }

                // Pick a random non-wall and non-tank direction to move in
                Direction direction;
                Coordinates newPosition;
                do
                {
                    direction = (Direction) _random.Next(directionCount);
                    newPosition = direction.Move(_api.Position);
                } while (_api.Map.IsWall(newPosition) || _api.Map.GetTank(newPosition) != null);

                _api.Move(direction);

                // Try shoot at someone if there's ammo
                if (_api.Ammo > 0)
                {
                    foreach (var tank in _api.VisibleTanks)
                    {
                        var targetDirection = DirectionExtensions.GetDirection(_api.Position, tank.Position);

                        // Check for cardinal direction shot, shoot if we can
                        if (targetDirection.HasValue)
                        {
                            _api.Shoot(targetDirection.Value);
                        }
                    }
                }

                _api.PrintAction();
            }
        }
    }
}