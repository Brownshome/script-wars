using System;
using System.Collections.Generic;
using ScriptWars.Connection;

namespace ScriptWars.Games.Tanks
{
    /// <summary>
    /// API to interface with the Tanks game on Script Wars.
    /// </summary>
    public class TankApi
    {
        /// <summary>
        /// Current tick's selected <see cref="Action"/>.
        /// </summary>
        public Action Action { get; private set; } = Action.Nothing;

        /// <summary>
        /// Whether or not this client is alive for the current tick.
        /// </summary>
        public bool IsAlive { get; private set; }

        /// <summary>
        /// Available ammunition at start of the current tick.
        /// </summary>
        public int Ammo { get; private set; }

        /// <summary>
        /// Direction of current tick's Move or Shoot <see cref="Action"/>.
        /// </summary>
        public Direction Direction { get; private set; }

        /// <summary>
        /// <see cref="World"/> data as at the current tick.
        /// </summary>
        public World Map { get; private set; }

        /// <summary>
        /// Current position at the start of the current tick.
        /// </summary>
        public Coordinates Position { get; private set; }

        /// <summary>
        /// Current tick connection status.
        /// </summary>
        public ConnectionStatus ConnectionStatus => _network.ConnectionStatus;

        /// <summary>
        /// All tanks in line-of-sight to the client for this tick.
        /// </summary>
        public ICollection<Tank> VisibleTanks => Map.Tanks;

        /// <summary>
        /// All shots in flight this tick.
        /// </summary>
        public ICollection<Shot> VisibleShots => Map.Shots;

        private readonly Network _network;

        /// <summary>
        /// Establish a connection to a Script Wars tanks server.
        /// </summary>
        /// <param name="id">Bot ID issued by the server.</param>
        /// <param name="serverAddress">IP or domain of the server.</param>
        /// <param name="tankName">Tank vanity name.</param>
        public TankApi(int id, string serverAddress, string tankName)
        {
            _network = new Network(id, serverAddress, tankName);
        }

        private void SetSendData()
        {
            _network.Send((byte) (int) Action);

            if (Action != Action.Nothing)
            {
                _network.Send((byte) (int) Direction);
            }

            Action = Action.Nothing;
        }

        /// <summary>
        /// Wait for server tick and update client-side game state.
        /// </summary>
        /// <returns>Whether or not the connection and game are still active.</returns>
        public bool NextTick()
        {
            SetSendData();

            if (!_network.NextTick())
            {
                return false;
            }

            IsAlive = _network.ReadByte() == 1;

            if (IsAlive)
            {
                DoNothing();
                Ammo = _network.ReadByte();
                Position = new Coordinates(_network);
                Map = new World(_network);
            }
            else
            {
                Position = null;
                Map = null;
            }

            return true;
        }

        /// <summary>
        /// Issue a move <see cref="Action"/> for this tick.
        /// </summary>
        /// <param name="direction"><see cref="Direction"/> to move in.</param>
        public void Move(Direction direction)
        {
            Action = Action.Move;
            Direction = direction;
        }

        /// <summary>
        /// Issue a shoot <see cref="Action"/> for this tick.
        /// </summary>
        /// <param name="direction"><see cref="Direction"/> to shoot in.</param>
        public void Shoot(Direction direction)
        {
            Action = Action.Shoot;
            Direction = direction;
        }

        /// <summary>
        /// Do nothing this tick.
        /// </summary>
        public void DoNothing()
        {
            Action = Action.Nothing;
        }

        /// <summary>
        /// Print the current action for this tick.
        /// </summary>
        public void PrintAction()
        {
            var direction = Action == Action.Nothing ? "" : Direction.ToString();
            Console.WriteLine($"{Action} {direction}");
        }
    }
}