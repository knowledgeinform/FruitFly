using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;
using System.Net.Sockets;
using System.IO;
using System.Runtime.InteropServices;

namespace RemoteGUI
{

    [StructLayout(LayoutKind.Sequential, Pack = 4)]
    struct CommandPacket
    {
        public COMMAND_ID command;
    };

    [StructLayout(LayoutKind.Sequential, Pack = 4)]
    class TimeSyncPacket
    {
        [DllImport("kernel32.dll")]
        private extern static void GetSystemTime(ref SYSTEMTIME lpSystemTime);
        public struct SYSTEMTIME
        {
            public ushort wYear;
            public ushort wMonth;
            public ushort wDayOfWeek;
            public ushort wDay;
            public ushort wHour;
            public ushort wMinute;
            public ushort wSecond;
            public ushort wMilliseconds;
        }

        public TimeSyncPacket()
        {
            //
            // Save the current system time
            //
            time = new SYSTEMTIME();
            GetSystemTime(ref time);
        }

        public SYSTEMTIME time;
    };

    static class CommandSender
    {
        enum PACKET_TYPE { COMMAND = 0, TIMESYNC };
        public static readonly IPAddress COMMAND_BROADCAST_ADDRESS = IPAddress.Parse("192.168.1.255");
        public const int COMMAND_BROADCAST_PORT = 5377;
        public const int TIMESYNC_BROADCAST_PORT = 5378;

        private static Socket mCommandSocket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
        private static IPEndPoint mRemoteEndpoint = new IPEndPoint(COMMAND_BROADCAST_ADDRESS, COMMAND_BROADCAST_PORT);
        private static IPEndPoint mRemoteTimesyncEndpoint = new IPEndPoint(COMMAND_BROADCAST_ADDRESS, TIMESYNC_BROADCAST_PORT);
        private static byte[] mOutputBuffer = new byte[128];

        public static void SendCommandPacket(CommandPacket packet)
        {
            BinaryWriter writer = new BinaryWriter(new MemoryStream(mOutputBuffer));
            writer.Write((byte)PACKET_TYPE.COMMAND);
            writer.Write(IPAddress.HostToNetworkOrder((int)packet.command));
            mCommandSocket.SendTo(mOutputBuffer, Marshal.SizeOf(Type.GetType("RemoteGUI.CommandPacket")) + 1, 0, mRemoteEndpoint);
        }

        public static void SendTimeSyncPacket()
        {
            TimeSyncPacket packet = new TimeSyncPacket();
            BinaryWriter writer = new BinaryWriter(new MemoryStream(mOutputBuffer));
            writer.Write((byte)PACKET_TYPE.TIMESYNC);
            writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wYear));
            writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wMonth));
            writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wDayOfWeek));
            writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wDay));
            writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wHour));
            writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wMinute));
            writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wSecond));
            writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wMilliseconds));
            mCommandSocket.SendTo(mOutputBuffer, Marshal.SizeOf(Type.GetType("RemoteGUI.TimeSyncPacket")) + 1, 0, mRemoteTimesyncEndpoint);
        }
    }
}
