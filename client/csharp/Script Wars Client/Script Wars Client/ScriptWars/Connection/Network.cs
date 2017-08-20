using System;
using System.IO;
using System.Net;
using System.Text;
using Syroot.BinaryData;

namespace ScriptWars.Connection
{
    /// <summary>
    /// Manages a network connection to the Script Wars server.
    /// </summary>
    public class Network
    {
        /// <summary>
        /// Buffer size to use for responses from the server.
        /// </summary>
        private const int MaxOutputSize = 1024;

        private const int UdpProtocol = 1;
        private const int TcpProtocol = 2;

        /// <summary>
        /// Server port to use for <see cref="UdpConnection"/>.
        /// </summary>
        private const int UdpPort = 35565;

        private readonly int _id;
        private readonly Connection _connection;

        // Track boolean squashing
        private int _boolBit = 0x100;

        private long _boolBytePosition;

        // Input and output streams
        private MemoryStream _dataOutStream;

        private BinaryWriter _dataOut;
        private BinaryReader _dataIn;

        /// <summary>
        /// Latest status of the connection to the Script Wars server.
        /// </summary>
        public ConnectionStatus ConnectionStatus { get; private set; } = ConnectionStatus.NotConnected;

        /// <summary>
        /// Whether or not the server response stream has more data to process.
        /// </summary>
        public bool HasData => _dataIn.BaseStream.Position < _dataIn.BaseStream.Length;

        /// <summary>
        /// Create network connection to Script Wars server using issued ID. 
        /// </summary>
        /// <param name="botId">ID issued by the server website.</param>
        /// <param name="serverAddress">IP or domain name of Script Wars server.</param>
        /// <param name="botName">Name of your bot.</param>
        /// <exception cref="NotImplementedException">TCP client is not available yet.</exception>
        /// <exception cref="ArgumentException">Invalid ID provided.</exception>
        public Network(int botId, string serverAddress, string botName)
        {
            _id = botId;

            InitialiseOutBuffer();
            Send(botName);

            var protocol = botId >> 16 & 0xff;
            switch (protocol)
            {
                case UdpProtocol:
                    _connection = new UdpConnection(new DnsEndPoint(serverAddress, UdpPort));
                    break;

                case TcpProtocol:
                    throw new NotImplementedException();

                default:
                    throw new ArgumentException("Invalid bot ID");
            }
        }

        private void InitialiseOutBuffer()
        {
            _dataOutStream = new MemoryStream(MaxOutputSize);
            _dataOut = new BinaryDataWriter(_dataOutStream)
            {
                ByteOrder = ByteOrder.BigEndian,
            };
            _dataOut.Write(_id);
        }

        /// <summary>
        /// Waits until all the players have made their moves and sends the data and retrieved a new set of data from 
        /// the server. This method returns false if the game is over or you have timed out.
        /// </summary>
        /// <returns>
        /// False if the client was disconnected or timed out. The exact cause can be found by checking 
        /// <see cref="ConnectionStatus"/>. Once this method returns false please attempt to re-use the connection.
        /// </returns>
        public bool NextTick()
        {
            try
            {
                ConnectionStatus = ConnectionStatus.Connected;
                _dataOutStream.SetLength(MaxOutputSize);

                var f = new FileInfo("D://hardai_cs.out");

                using (var outstream = f.OpenWrite())
                {
                    var b = _dataOutStream.ToArray();
                    outstream.Write(b, 0, b.Length);
                }

                _connection.SendData(_dataOutStream.ToArray());
                InitialiseOutBuffer();

                _dataIn = _connection.WaitForData();
            }
            catch (ConnectionException e)
            {
                Console.WriteLine(e);
                ConnectionStatus = e.ConnectionStatus;
                return false;
            }

            return true;
        }

        /// <summary>
        /// Read a 32-bit integer from the last server tick.
        /// </summary>
        /// <returns>Integer from the server response.</returns>
        public int ReadInt()
        {
            return _dataIn.ReadInt32();
        }

        /// <summary>
        /// Read a floating point number from the last server tick.
        /// </summary>
        /// <returns>Float from the server response.</returns>
        public float ReadFloat()
        {
            return _dataIn.ReadSingle();
        }

        /// <summary>
        /// Read a byte from the last server tick.
        /// </summary>
        /// <returns>Byte from response.</returns>
        public byte ReadByte()
        {
            return _dataIn.ReadByte();
        }

        /// <summary>
        /// Read a boolean from the server response. Booleans are encoded as bits inside a byte, allowing the storage 
        /// of eight booleans each byte.
        /// </summary>
        /// <returns>Next boolean from the server response.</returns>
        public bool ReadBool()
        {
            var inStream = _dataIn.BaseStream;
            var streamOriginalPosition = inStream.Position;

            if (_boolBit == 0x100 || _boolBytePosition != streamOriginalPosition - 1)
            {
                _boolBit = 1;
                _boolBytePosition = streamOriginalPosition;
                streamOriginalPosition++;
            }

            inStream.Seek(_boolBytePosition, SeekOrigin.Begin);
            int currentByte = _dataIn.ReadByte();
            var boolValue = (currentByte & _boolBit) != 0;

            _boolBit <<= 1;

            return boolValue;
        }

        /// <summary>
        /// Read a short from the last server tick.
        /// </summary>
        /// <returns>Short from response.</returns>
        public short ReadShort()
        {
            return _dataIn.ReadInt16();
        }

        /// <summary>
        /// Read a string from the last server tick.
        /// </summary>
        /// <returns>String from response.</returns>
        public string ReadString()
        {
            int length = ReadShort();
            var strBytes = _dataIn.ReadBytes(length);
            return Encoding.UTF8.GetString(strBytes);
        }

        /// <summary>
        /// Get raw data from the last server tick.
        /// </summary>
        /// <returns>Byte array of server's response.</returns>
        public byte[] GetRawData()
        {
            return ((MemoryStream) _dataIn.BaseStream).ToArray();
        }

        /// <summary>
        /// Send a 32-bit integer to the server.
        /// </summary>
        /// <param name="i">Integer to send.</param>
        public void Send(int i)
        {
            _dataOut.Write(i);
        }

        /// <summary>
        /// Send a floating point number to the server.
        /// </summary>
        /// <param name="f">Float to send.</param>
        public void Send(float f)
        {
            _dataOut.Write(f);
        }

        /// <summary>
        /// Send a byte to the server.
        /// </summary>
        /// <param name="b">Byte to send.</param>
        public void Send(byte b)
        {
            _dataOut.Write(b);
        }

        /// <summary>
        /// Send a 16-bit integer to the server.
        /// </summary>
        /// <param name="s">Short to send.</param>
        public void Send(short s)
        {
            _dataOut.Write(s);
        }

        /// <summary>
        /// Send a string to the server.
        /// </summary>
        /// <param name="s">String to send.</param>
        public void Send(string s)
        {
            var bytes = Encoding.UTF8.GetBytes(s);
            Send((short) bytes.Length);
            Send(bytes);
        }

        /// <summary>
        /// Send raw data to the server.
        /// </summary>
        /// <param name="bytes">Bytes to send.</param>
        public void Send(byte[] bytes)
        {
            _dataOut.Write(bytes);
        }
    }
}