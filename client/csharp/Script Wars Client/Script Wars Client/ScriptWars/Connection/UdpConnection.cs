using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using Syroot.BinaryData;

namespace ScriptWars.Connection
{
    internal class UdpConnection : Connection
    {
        private const int BufferSizeBytes = 1024;
        private const int SocketTimeoutMs = 1500;

        private readonly Socket _socket;

        public UdpConnection(EndPoint dnsEndPoint)
        {
            _socket = new Socket(SocketType.Dgram, ProtocolType.Udp)
            {
                SendTimeout = SocketTimeoutMs,
                ReceiveTimeout = SocketTimeoutMs
            };
            _socket.Connect(dnsEndPoint);
        }

        public override void SendData(byte[] data)
        {
            try
            {
                _socket.Send(data);
            }
            catch (Exception e)
            {
                throw new ConnectionException(ConnectionStatus.Dropped, e);
            }
        }

        public override BinaryReader WaitForData()
        {
            BinaryReader reader;

            try
            {
                var buffer = new byte[BufferSizeBytes];
                _socket.Receive(buffer);
                var bufferStream = new MemoryStream(buffer, true);
                reader = new BinaryDataReader(bufferStream)
                {
                    ByteOrder = ByteOrder.BigEndian,
                };
            }
            catch (Exception e)
            {
                throw new ConnectionException(ConnectionStatus.Dropped, e);
            }

            CheckResponseCode(reader);
            return reader;
        }
    }
}