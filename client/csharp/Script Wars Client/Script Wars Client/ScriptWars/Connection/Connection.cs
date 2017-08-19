using System.IO;
using System.Text;

namespace ScriptWars.Connection
{
    /// <summary>
    /// Base connection to Script Wars server.
    /// </summary>
    /// <author></author>
    internal abstract class Connection
    {
        private const int SuccessResponseCode = 0;
        private const int ErrorResponseCode = 1;
        private const int FailedToKeepUpResponseCode = 2;
        private const int ErrorInBufferResponseCode = 255;

        /// <summary>
        /// Send data to the connected Script Wars server.
        /// </summary>
        /// <param name="data">Data to send.</param>
        public abstract void SendData(byte[] data);

        /// <summary>
        /// Receive data from the connected Script Wars server.
        /// </summary>
        /// <returns>Reader for response.</returns>
        public abstract BinaryReader WaitForData();

        /// <summary>
        /// Test the response code sent by the server in the first byte.
        /// </summary>
        /// <param name="responseReader">Reader for response stream from server.</param>
        /// <exception cref="ConnectionException">Thrown when connection error has occurred.</exception>
        protected void CheckResponseCode(BinaryReader responseReader)
        {
            var responseCode = responseReader.ReadByte();

            switch (responseCode)
            {
                case ErrorResponseCode:
                    throw new ConnectionException(ConnectionStatus.Disconnected);

                case FailedToKeepUpResponseCode:
                    throw new ConnectionException(ConnectionStatus.FailedToKeepUp);

                case ErrorInBufferResponseCode:
                    var length = responseReader.ReadInt16();
                    var messageBytes = responseReader.ReadBytes(length);
                    throw new ConnectionException(ConnectionStatus.Error, Encoding.UTF8.GetString(messageBytes));

                case SuccessResponseCode:
                    break;

                default:
                    throw new ConnectionException(ConnectionStatus.Error, "Unknown response code " + responseCode);
            }
        }
    }
}