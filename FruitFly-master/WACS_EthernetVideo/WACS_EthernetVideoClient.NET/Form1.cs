using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Threading;
using System.Net.Sockets;
using System.Net;
using System.IO;
using System.Windows.Media.Imaging;
using System.Configuration;

namespace WACS_EthernetVideoClient.NET
{
    public partial class Form1 : Form
    {
        private String mRemoteIPString = ConfigurationManager.AppSettings["RemoteIPAddress"];
        private int mRemotePort = Int32.Parse(ConfigurationManager.AppSettings["RemoteIPPort"]);
        private int mReceivedFrameWaitTime_ms = Int32.Parse(ConfigurationManager.AppSettings["ReceivedFrameWaitTime_ms"]);
        private int mLostFrameWaitTime_ms = Int32.Parse(ConfigurationManager.AppSettings["LostFrameWaitTime_ms"]);
        private int mLocalPort = 3998;

        private bool mFrameRequested = false;
        private Object mFrameRequestLock = new Object();

        private long mLastFrameReceivedTime_ms = 0;
        private Object mConnectivityLock = new Object();

        public delegate void UpdateFrameImageDelegate(Bitmap frameBitmap);
        public delegate void SetNoVideoLabelVisibleDelegate(bool visible);

        public Form1()
        {
            InitializeComponent();

            Thread receiveThread = new Thread(new ThreadStart(ReceiveThread));
            receiveThread.IsBackground = true;
            receiveThread.Start();

            Thread connectivityCheckThread = new Thread(new ThreadStart(ConnectivityCheckThread));
            connectivityCheckThread.IsBackground = true;
            connectivityCheckThread.Start();

            Thread requestorThread = new Thread(new ThreadStart(RequestorThread));
            requestorThread.IsBackground = true;
            requestorThread.Start();
        }

        private void UpdateFrameImage(Image frameImage)
        {
            picFrame.Image = frameImage;
        }

        private void SetNoVideoLabelVisible(bool visible)
        {
            lblNoVideo.Visible = visible;
        }

        private void ConnectivityCheckThread()
        {
            long lastFrameReceivedTime_ms;

            while (true)
            {
                lock (mFrameRequestLock)
                {
                    lastFrameReceivedTime_ms = mLastFrameReceivedTime_ms;
                }

                try
                {
                    if (((DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond) - lastFrameReceivedTime_ms) > 3000)
                    {
                        Invoke(new SetNoVideoLabelVisibleDelegate(SetNoVideoLabelVisible), true);
                    }
                    else
                    {
                        Invoke(new SetNoVideoLabelVisibleDelegate(SetNoVideoLabelVisible), false);
                    }
                }
                catch (Exception)
                {
                }

                Thread.Sleep(250);
            }
        }

        private void RequestorThread()
        {
            byte[] localIPAddress = null;
            IPHostEntry hostEntry;
            hostEntry = Dns.GetHostEntry(Dns.GetHostName());
            foreach (IPAddress ip in hostEntry.AddressList)
            {
                if (ip.AddressFamily.ToString() == "InterNetwork")
                {
                    if (localIPAddress == null || ip.ToString().Substring(0, 5).Equals(mRemoteIPString.Substring(0, 5)))
                    {
                        localIPAddress = ip.GetAddressBytes();
                    }
                }
            }
            
            
            MemoryStream memoryStream = new MemoryStream();
            BinaryWriter binaryWriter = new BinaryWriter(memoryStream);
            binaryWriter.Write(localIPAddress);
            binaryWriter.Write((byte)((mLocalPort & 0xFF00) >> 8));
            binaryWriter.Write((byte)(mLocalPort & 0xFF));            
            memoryStream.Seek(0, SeekOrigin.Begin);
            byte[] frameRequestBytes = memoryStream.ToArray();

            UdpClient udpClient = new UdpClient();
            udpClient.Connect(mRemoteIPString, mRemotePort);

            bool frameRequested;
            do
            {
                lock (mFrameRequestLock)
                {
                    frameRequested = mFrameRequested;
                }

                if (!frameRequested)
                {
                    Thread.Sleep(250);
                }
            }
            while (!frameRequested);

            long timeOfLastRequest_ms = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            while (true)
            {
                lock (mFrameRequestLock)
                {
                    frameRequested = mFrameRequested;
                    mFrameRequested = false; //Reset flag
                }

                if (frameRequested || (((DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond) - timeOfLastRequest_ms) > mLostFrameWaitTime_ms))
                {
                    udpClient.Send(frameRequestBytes, frameRequestBytes.Length);
                    timeOfLastRequest_ms = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
                }

                Thread.Sleep(50);
            }
        }

        private void ReceiveThread()
        {
            UdpClient udpClient = new UdpClient(mLocalPort);
            
            IPEndPoint senderEndPoint = new IPEndPoint(IPAddress.Any, 0);

            while (true)
            {
                lock (mFrameRequestLock)
                {
                    mFrameRequested = true;
                }

                byte[] incomingBytes = udpClient.Receive(ref senderEndPoint);

                lock (mFrameRequestLock)
                {
                    mLastFrameReceivedTime_ms = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
                }

                MemoryStream incomingStream = new MemoryStream(incomingBytes);
                try
                {
                    Invoke(new UpdateFrameImageDelegate(UpdateFrameImage), Image.FromStream(incomingStream));
                }
                catch (Exception)
                {
                }

                Thread.Sleep(mReceivedFrameWaitTime_ms);
            }
        }
    }
}
