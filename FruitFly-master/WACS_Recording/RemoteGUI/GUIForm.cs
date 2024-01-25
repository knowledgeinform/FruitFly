using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Threading;
using System.Net;
using System.Net.Sockets;
using System.IO;
using System.Runtime.InteropServices;

namespace RemoteGUI
{
    public partial class GUIForm : Form
    {
        public const int STATUS_LISTEN_PORT = 5747;
        public const int COMMUNICATION_TIMEOUT_PERIOD_SEC = 3;
        public const int RECORDING_QUEUE_MAX_SIZE = 5000;

        private Thread mUpdateStatusThread;
        private Thread mUpdateConnectivityThread;
        private Thread mTimesyncThread;
        DateTime mLastUpdateTime = DateTime.MinValue;

        private TimeSpan mRecordingDuration = new TimeSpan(0, 0, 0);


        public GUIForm()
        {
            InitializeComponent();

            //
            // Create the thread to listen for status updates from the remote system
            //
            mUpdateStatusThread = new Thread(new ThreadStart(UpdateStatusThreadProc));
            mUpdateStatusThread.IsBackground = true;
            mUpdateStatusThread.Start();

            //
            // Create the thread to update the remote connectivity flag
            //
            mUpdateConnectivityThread = new Thread(new ThreadStart(UpdateConnectivityThreadProc));
            mUpdateConnectivityThread.IsBackground = true;
            mUpdateConnectivityThread.Start();

            //
            // Create the dedicated timesync thread
            //
            mTimesyncThread = new Thread(new ThreadStart(TimesyncThreadProc));
            mTimesyncThread.IsBackground = true;
            mTimesyncThread.Start();
        }

        public delegate void UpdateStatusCallback(StatusPacket status);
        public delegate void UpdateConnectivityCallback(bool isConnected);
        public delegate void UpdateRecordingDuration(TimeSpan duration);

        private void TimesyncThreadProc()
        {
            try
            {
                Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
                IPEndPoint remoteEndpoint = new IPEndPoint(CommandSender.COMMAND_BROADCAST_ADDRESS, 6378);                
                byte[] outputBuffer = new byte[128];

                long lastSendTime_ticks = 0;
                while (true)
                {
                    if (((DateTime.Now.Ticks - lastSendTime_ticks) / TimeSpan.TicksPerMillisecond) > 1000)
                    {
                        lastSendTime_ticks = DateTime.Now.Ticks;

                        TimeSyncPacket packet = new TimeSyncPacket();
                        ushort checksum = 0;
                        checksum += (ushort)IPAddress.HostToNetworkOrder((short)packet.time.wYear);
                        checksum += (ushort)IPAddress.HostToNetworkOrder((short)packet.time.wMonth);
                        checksum += (ushort)IPAddress.HostToNetworkOrder((short)packet.time.wDayOfWeek);
                        checksum += (ushort)IPAddress.HostToNetworkOrder((short)packet.time.wDay);
                        checksum += (ushort)IPAddress.HostToNetworkOrder((short)packet.time.wHour);
                        checksum += (ushort)IPAddress.HostToNetworkOrder((short)packet.time.wMinute);
                        checksum += (ushort)IPAddress.HostToNetworkOrder((short)packet.time.wSecond);
                        checksum += (ushort)IPAddress.HostToNetworkOrder((short)packet.time.wMilliseconds);

                        BinaryWriter writer = new BinaryWriter(new MemoryStream(outputBuffer));
                        writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wYear));
                        writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wMonth));
                        writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wDayOfWeek));
                        writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wDay));
                        writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wHour));
                        writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wMinute));
                        writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wSecond));
                        writer.Write(IPAddress.HostToNetworkOrder((short)packet.time.wMilliseconds));
                        writer.Write(IPAddress.HostToNetworkOrder((short)checksum));
                        socket.SendTo(outputBuffer, Marshal.SizeOf(Type.GetType("RemoteGUI.TimeSyncPacket")) + 2, 0, remoteEndpoint);
                    }

                    int timeSinceLastSent_ms = (int)((DateTime.Now.Ticks - lastSendTime_ticks) / TimeSpan.TicksPerMillisecond);
                    if (timeSinceLastSent_ms < 1000)
                    {
                        Thread.Sleep((int)(1000 - timeSinceLastSent_ms));
                    }
                    else
                    {
                        Thread.Sleep(50);
                    }
                }
            }
            catch (Exception e)
            {
                MessageBox.Show(e.Message);
            }
        }

        private void UpdateStatusThreadProc()
        {

            try
            {
                StatusPacket statusPacket = new StatusPacket();


                IPEndPoint localEndpoint = new IPEndPoint(IPAddress.Any, STATUS_LISTEN_PORT);
                using (Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp))
                {
                    socket.Bind(localEndpoint);
                    byte[] receiveBuffer = new byte[Marshal.SizeOf(Type.GetType("RemoteGUI.StatusPacket"))];
                    while (true)
                    {
                        int numBytesReceived = socket.Receive(receiveBuffer);
                        if (numBytesReceived == receiveBuffer.Length)
                        {
                            BinaryReader reader = new BinaryReader(new MemoryStream(receiveBuffer));
                            statusPacket.hasReceivedTimeSync = IPAddress.NetworkToHostOrder(reader.ReadInt32()) == 0 ? false : true;
                            statusPacket.timestamp = IPAddress.NetworkToHostOrder(reader.ReadInt32());
                            statusPacket.isRecording = IPAddress.NetworkToHostOrder(reader.ReadInt32()) == 0 ? false : true;
                            statusPacket.frameRate = IPAddress.NetworkToHostOrder(reader.ReadInt32());
                            statusPacket.numFramesInQueue = IPAddress.NetworkToHostOrder(reader.ReadInt32());
                            statusPacket.numFramesDropped = IPAddress.NetworkToHostOrder(reader.ReadInt32());
                            statusPacket.isAdeptPresent = IPAddress.NetworkToHostOrder(reader.ReadInt32()) == 0 ? false : true;
                            statusPacket.numWarpQueueUnderflows = IPAddress.NetworkToHostOrder(reader.ReadInt32());


                            //
                            // If the remote system's clock has deviated more than two seconds from
                            // this system's time, re-sync.
                            //
                            if (!statusPacket.hasReceivedTimeSync)
                            {
                                CommandSender.SendTimeSyncPacket();
                                lock (this)
                                {
                                    mRecordingDuration = new TimeSpan(0, 0, 0);
                                    Invoke(new UpdateRecordingDuration(UpdateDuration), mRecordingDuration);
                                }
                            }


                            lock (this)
                            {
                                if (statusPacket.isRecording)
                                {
                                    mRecordingDuration += (DateTime.Now - mLastUpdateTime);
                                    Invoke(new UpdateRecordingDuration(UpdateDuration), mRecordingDuration);
                                }
                                mLastUpdateTime = DateTime.Now;
                            }
                        }

                        try
                        {
                            //
                            // GUI updates must been in the context of the GUI thread
                            //
                            Invoke(new UpdateStatusCallback(UpdateStatus), statusPacket);
                        }
                        catch (Exception)
                        { }
                    }
                }
            }
            catch (Exception e)
            {
                MessageBox.Show(e.Message);
            }
        }

        private void UpdateConnectivityThreadProc()
        {
            try
            {
                bool isCommunicating = false;
                bool prevIsCommunicating = false;

                while (true)
                {
                    lock (this)
                    {
                        if (DateTime.Now.Subtract(mLastUpdateTime).TotalSeconds > COMMUNICATION_TIMEOUT_PERIOD_SEC)
                        {
                            isCommunicating = false;
                        }
                        else
                        {
                            isCommunicating = true;
                        }
                    }

                    if (isCommunicating != prevIsCommunicating)
                    {
                        try
                        {
                            //
                            // GUI updates must been in the context of the GUI thread
                            //
                            Invoke(new UpdateConnectivityCallback(UpdateConnectivity), isCommunicating);
                        }
                        catch (Exception)
                        { }
                        prevIsCommunicating = isCommunicating;
                    }

                    Thread.Sleep(500);
                }

            }
            catch (Exception e)
            {
                MessageBox.Show(e.Message);
            }
        }

        public void UpdateStatus(StatusPacket statusPacket)
        {
            TimeSpan epochTimeDelta = TimeSpan.FromSeconds(statusPacket.timestamp);
            DateTime packetTime = new DateTime(1970, 1, 1) + epochTimeDelta;
            //packetTime = packetTime.Add(TimeZoneInfo.Local.BaseUtcOffset);
            lblLastUpdateTime.Text = packetTime.ToString("HH:mm:ss") + " UTC";

            if (statusPacket.isRecording)
            {
                lblRecordingStatus.Text = "Recording";
                lblRecordingStatus.BackColor = Color.DeepSkyBlue;

                btnRecordingMode.Text = "Pause";
                btnRecordingMode.Image = global::RemoteGUI.Properties.Resources.pause;
            }
            else
            {
                lblRecordingStatus.Text = "Paused";
                lblRecordingStatus.BackColor = Color.Khaki;

                btnRecordingMode.Text = "Record";
                btnRecordingMode.Image = global::RemoteGUI.Properties.Resources.record;
            }

            if (statusPacket.isAdeptPresent)
            {
                lblIsAdeptPresent.Text = "Yes";
            }
            else
            {
                lblIsAdeptPresent.Text = "No";
            }

            lblQueueLength.Text = statusPacket.numFramesInQueue.ToString();        
            lblFrameRate.Text = statusPacket.frameRate.ToString();
            lblNumFramesDropped.Text = statusPacket.numFramesDropped.ToString();
            lblNumWarpUnderflows.Text = statusPacket.numWarpQueueUnderflows.ToString();

            int perecntQueueFilled = ((int)((((float)statusPacket.numFramesInQueue) / RECORDING_QUEUE_MAX_SIZE) * 100));
            if (perecntQueueFilled > 100)
            {
                perecntQueueFilled = 100;
            }
            barQueueFill.Value = perecntQueueFilled;
        }

        private void UpdateConnectivity(bool isConnected)
        {
            if (isConnected)
            {
                lblCommsStatus.Text = "Connected";
                lblCommsStatus.BackColor = Color.LightGreen;
            }
            else
            {
                lblCommsStatus.Text = "Not Connected";
                lblCommsStatus.BackColor = Color.Crimson;
            }
        }

        private void UpdateDuration(TimeSpan duration)
        {
            StringBuilder durationString = new StringBuilder();
            lblRecordingDuration.Text = String.Format("{0:00}:{1:00}:{2:00}", duration.Hours, duration.Minutes, duration.Seconds);
        }

        private void btnShowShutdown_Click(object sender, EventArgs e)
        {
            ShutdownForm frmShutdown = new ShutdownForm();
            frmShutdown.ShowDialog();
        }

        private void btnRecordingMode_Click(object sender, EventArgs e)
        {
            if (btnRecordingMode.Text.Equals("Record"))
            {
                CommandPacket commandPacket = new CommandPacket();
                commandPacket.command = COMMAND_ID.RECORD;
                CommandSender.SendCommandPacket(commandPacket);                
            }
            else
            {
                CommandPacket commandPacket = new CommandPacket();
                commandPacket.command = COMMAND_ID.PAUSE;
                CommandSender.SendCommandPacket(commandPacket);
            }
        }
    }

    [StructLayout(LayoutKind.Sequential, Pack = 4)] 
    public class StatusPacket
    {
        public bool hasReceivedTimeSync = false;
        public int timestamp = 0;
        public bool isRecording = false;
        public int frameRate = 0;
        public int numFramesInQueue = 0;
        public int numFramesDropped = 0;
        public bool isAdeptPresent = false;
        public int numWarpQueueUnderflows = 0;
    }
}
