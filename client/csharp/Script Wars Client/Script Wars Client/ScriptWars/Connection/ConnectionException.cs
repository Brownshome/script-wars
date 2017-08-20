using System;

namespace ScriptWars.Connection
{
    public class ConnectionException : Exception
    {
        public ConnectionStatus ConnectionStatus { get; }

        public ConnectionException(ConnectionStatus connectionStatus, Exception innerException) :
            base(connectionStatus.ToString(), innerException)
        {
            ConnectionStatus = connectionStatus;
        }

        public ConnectionException(ConnectionStatus connectionStatus, string message = null) : base(
            message ?? connectionStatus.ToString())
        {
            ConnectionStatus = connectionStatus;
        }
    }
}