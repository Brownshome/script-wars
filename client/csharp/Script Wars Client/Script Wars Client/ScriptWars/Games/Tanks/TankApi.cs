using System;
using System.Collections.Generic;
using ScriptWars.Connection;

namespace ScriptWars.Games.Tanks
{
    public class TankApi
    {
        public Action Action { get; private set; } = Action.Nothing;
        public bool IsAlive { get; private set; }
        public int Ammo { get; private set; }
        public Direction Direction { get; private set; }
        public World Map { get; private set; }
        public Coordinates Position { get; private set; }

        public ConnectionStatus ConnectionStatus => _network.ConnectionStatus;
        public ICollection<Tank> VisibleTanks => Map.Tanks;
        public ICollection<Shot> VisibleShots => Map.Shots;

        private readonly Network _network;

        public TankApi(int id, string serverAddress, string tankName)
        {
            _network = new Network(id, serverAddress, tankName);
        }

        private void SetSendData()
        {
            _network.Send((byte)(int) Action);

            if (Action != Action.Nothing)
            {
                _network.Send((byte)(int) Direction);
            }
            
            Action = Action.Nothing;
        }

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

        public void Move(Direction direction)
        {
            Action = Action.Move;
            Direction = direction;
        }

        public void Shoot(Direction direction)
        {
            Action = Action.Shoot;
            Direction = direction;
        }

        public void DoNothing()
        {
            Action = Action.Nothing;
        }

        public void PrintAction()
        {
            var direction = Action == Action.Nothing ? "" : Direction.ToString();
            Console.WriteLine($"{Action} {direction}");
        }
    }
}
