using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Drawing.Imaging;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.IO;
using System.Runtime.InteropServices;
using System.Threading;

namespace RecordingVideoPlayback
{
    public partial class Form1 : Form
    {
        private int mNumFiles;
        private string[] mInputFileNames = null;
        private byte[] mImageBuffer = null;
        private byte[] mWarpBuffer = null;
        private Bitmap mBitmap;
        private Bitmap mDisplayedBitmap;
        private int mFileNum;
        private bool mIsPlaying = false;
        private bool mManualSliderChange = false;
        private int mSpeed = 1;
        private int[] mSpeedChoices = {1, 2, 3, 5, 10, 25, 50};
        private bool mInputDirectoryChanged = false;


        public delegate void UpdateSliderPosition(int position);
        public delegate void UpdateFrameImage(Bitmap frameBitmap);
        public delegate void UpdatePlayButtonText(string text);
        public delegate void UpdateTimestampLabel(string text);

        public Form1()
        {
            InitializeComponent();

            mBitmap = new Bitmap(320, 240, PixelFormat.Format8bppIndexed);
            ColorPalette palette = mBitmap.Palette;
            for (int i = 0; i < 256; ++i)
            {
                palette.Entries[i] = Color.FromArgb(i, i, i);
            }
            mBitmap.Palette = palette;
        }

        private void exitToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Application.Exit();
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            Thread playbackThread = new Thread(new ThreadStart(PlayRecording));
            playbackThread.IsBackground = true;
            playbackThread.Start();
        }

        private void openRecordingDirectoryToolStripMenuItem_Click(object sender, EventArgs e)
        {
            FolderBrowserDialog folderBrowserDialog = new FolderBrowserDialog();
            if (folderBrowserDialog.ShowDialog() == DialogResult.OK)
            {
                string inputDirectory = folderBrowserDialog.SelectedPath;

                lock (this)
                {
                    mInputFileNames = Directory.GetFiles(inputDirectory, "*.rec");
                    Array.Sort(mInputFileNames);
                    mNumFiles = mInputFileNames.Length;
                    sldFrameNum.Maximum = mNumFiles;
                    if (mNumFiles <= 0)
                    {
                        mInputFileNames = null;
                        mNumFiles = 0;
                        MessageBox.Show("Invalid directory selected");
                    }
                    else
                    {
                        mFileNum = 0;
                        Invoke(new UpdateSliderPosition(UpdateSlider), 0);
                        mIsPlaying = true;
                        Invoke(new UpdatePlayButtonText(UpdatePlayButton), "Pause");
                        btnPlayPause.Enabled = true;
                        sldFrameNum.Enabled = true;
                        mInputDirectoryChanged = true;
                    }
                }
            }
        }

        private unsafe Bitmap CopyDataToBitmap(byte[] data)
        {
            try
            {
                BitmapData bmpData = mBitmap.LockBits(new Rectangle(0, 0, mBitmap.Width, mBitmap.Height),
                                                  ImageLockMode.WriteOnly, mBitmap.PixelFormat);
                Marshal.Copy(data, 0, bmpData.Scan0, data.Length);
                mBitmap.UnlockBits(bmpData);
            }
            catch (Exception)
            { }

            return mBitmap;
        }

        private void PlayRecording()
        {
            StringBuilder fileName = new StringBuilder();

            string[] inputFileNames;

            try
            {
                while (true)
                {
                    lock (this)
                    {
                        inputFileNames = mInputFileNames;
                    }

                    if (inputFileNames != null)
                    {
                        for (int fileNum = 0; fileNum < inputFileNames.Length; ++fileNum)
                        {
                            string inputFileName = inputFileNames[fileNum];

                            using (FileStream inputStream = File.OpenRead(inputFileName))
                            {
                                long totalFileSize = inputStream.Length;
                                using (BinaryReader inputReader = new BinaryReader(inputStream))
                                {
                                    float formatVersion = inputReader.ReadSingle();
                                    int imageBufferSize = inputReader.ReadInt32();
                                    int warpBufferSize = inputReader.ReadInt32();

                                    if (mImageBuffer == null)
                                    {
                                        mImageBuffer = new byte[imageBufferSize];
                                        mWarpBuffer = new byte[warpBufferSize];
                                    }


                                    bool endOfFile = false;
                                    byte[] inputChar = new byte[1];
                                    DateTime lastFrameTime = DateTime.Now;
                                    do
                                    {
                                        TimeSpan frameTimeSpan = (DateTime.Now - lastFrameTime);
                                        int numMillisecondsBetweenFrames = 33 / mSpeed;
                                        if (frameTimeSpan.TotalMilliseconds < numMillisecondsBetweenFrames)
                                        {
                                            Thread.Sleep((int)(numMillisecondsBetweenFrames - frameTimeSpan.TotalMilliseconds));
                                        }
                                        lastFrameTime = DateTime.Now;

                                        bool isPlaying;
                                        do
                                        {
                                            lock (this)
                                            {
                                                isPlaying = mIsPlaying;
                                            }

                                            if (!isPlaying)
                                            {
                                                Thread.Sleep(100);
                                            }
                                        }
                                        while (!isPlaying);

                                        //
                                        // Read name of frame file
                                        //
                                        fileName.Remove(0, fileName.Length);

                                        int digit;
                                        try
                                        {
                                            do
                                            {
                                                digit = (byte)inputReader.ReadByte();
                                                if (digit >= 0 && digit != 255)
                                                {
                                                    fileName.Append(Encoding.ASCII.GetString(BitConverter.GetBytes(digit), 0, 1));
                                                }
                                                else
                                                {
                                                    endOfFile = true;
                                                }
                                            }
                                            while (digit != ((int)'\0') && !endOfFile);
                                        }
                                        catch (Exception)
                                        {
                                            endOfFile = true;
                                        }

                                        if (fileName.Length <= 1)
                                        {
                                            endOfFile = true;
                                        }

                                        string fileNameFinal = fileName.ToString();
                                        if (fileNameFinal.Length > 8)
                                        {
                                            Invoke(new UpdateTimestampLabel(UpdateTimestamp), fileNameFinal.Substring(9));
                                        }

                                        if (!endOfFile)
                                        {
                                            //
                                            // Read frame buffer and warp buffer data
                                            // 
                                            int numBytesRead = inputReader.Read(mImageBuffer, 0, imageBufferSize);

                                            if (numBytesRead > 0)
                                            {
                                                inputReader.Read(mWarpBuffer, 0, warpBufferSize);

                                                try
                                                {
                                                    if ((fileNameFinal.Contains("channel1") && itmChannel1.Checked) ||
                                                        (fileNameFinal.Contains("channel2") && itmChannel2.Checked))
                                                    {
                                                        Bitmap bitmap = CopyDataToBitmap(mImageBuffer);
                                                        mDisplayedBitmap = (Bitmap)bitmap.Clone();
                                                        Invoke(new UpdateFrameImage(UpdateFrame), mDisplayedBitmap);
                                                    }
                                                }
                                                catch (Exception)
                                                { }
                                            }
                                            else
                                            {
                                                endOfFile = true;
                                            }
                                        }

                                        lock (this)
                                        {
                                            if (mManualSliderChange || mInputDirectoryChanged)
                                            {
                                                endOfFile = true;
                                            }
                                        }
                                    }
                                    while (!endOfFile);
                                    
                                    int sliderPosition = fileNum;
                                    Invoke(new UpdateSliderPosition(UpdateSlider), sliderPosition); 

                                    lock (this)
                                    {
                                        if (!mManualSliderChange && !mInputDirectoryChanged)
                                        {                                            
                                            ++mFileNum;
                                        }
                                        else
                                        {
                                            fileNum = mFileNum;
                                            mManualSliderChange = false;
                                            inputFileNames = mInputFileNames;
                                        }
                                        
                                        mInputDirectoryChanged = false;
                                    }
                                }
                            }
                        }

                        lock (this)
                        {
                            mIsPlaying = false;
                            mFileNum = 0;
                            Invoke(new UpdatePlayButtonText(UpdatePlayButton), "Play");
                            Invoke(new UpdateSliderPosition(UpdateSlider), 0); 
                        }
                    }
                    else
                    {
                        Thread.Sleep(200);
                    }
                }
            }
            catch (Exception e)
            {
                MessageBox.Show(e.Message);
            }
        }

        private void btnPlayPause_Click(object sender, EventArgs e)
        {
            if (btnPlayPause.Text.Equals("Play"))
            {
                btnPlayPause.Text = "Pause";
                lock (this)
                {
                    mIsPlaying = true;
                }
            }
            else
            {
                btnPlayPause.Text = "Play";
                lock (this)
                {
                    mIsPlaying = false;
                }
            }
        }

        private void sldFrameNum_Scroll(object sender, EventArgs e)
        {
            lock (this)
            {
                mFileNum = sldFrameNum.Value;
                mManualSliderChange = true;
            }
        }

        private void UpdateSlider(int position)
        {
            if (position <= sldFrameNum.Maximum)
            {
                sldFrameNum.Value = position;
            }
        }

        private void UpdateFrame(Bitmap frameBitmap)
        {
            picFrame.Image = (Image)frameBitmap;
        }

        private void UpdatePlayButton(string text)
        {
            btnPlayPause.Text = text;
        }

        private void UpdateTimestamp(string text)
        {
            lblTimestamp.Text = text;
        }

        private void itmChannel1_Click(object sender, EventArgs e)
        {
            itmChannel1.Checked = true;
            itmChannel2.Checked = false;
        }

        private void itmChannel2_Click(object sender, EventArgs e)
        {
            itmChannel1.Checked = false;
            itmChannel2.Checked = true;
        }

        private void sldSpeed_Scroll(object sender, EventArgs e)
        {
            lock (this)
            {
                mSpeed = mSpeedChoices[sldSpeed.Value];
            }
        }        
    }
}
