namespace RecordingVideoPlayback
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.picFrame = new System.Windows.Forms.PictureBox();
            this.menuStrip1 = new System.Windows.Forms.MenuStrip();
            this.fileToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.openRecordingDirectoryToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.exitToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.channelToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.itmChannel1 = new System.Windows.Forms.ToolStripMenuItem();
            this.itmChannel2 = new System.Windows.Forms.ToolStripMenuItem();
            this.btnPlayPause = new System.Windows.Forms.Button();
            this.sldFrameNum = new System.Windows.Forms.TrackBar();
            this.sldSpeed = new System.Windows.Forms.TrackBar();
            this.label1 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.label6 = new System.Windows.Forms.Label();
            this.label7 = new System.Windows.Forms.Label();
            this.label8 = new System.Windows.Forms.Label();
            this.lblTimestamp = new System.Windows.Forms.Label();
            ((System.ComponentModel.ISupportInitialize)(this.picFrame)).BeginInit();
            this.menuStrip1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.sldFrameNum)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.sldSpeed)).BeginInit();
            this.SuspendLayout();
            // 
            // picFrame
            // 
            this.picFrame.Location = new System.Drawing.Point(46, 51);
            this.picFrame.Name = "picFrame";
            this.picFrame.Size = new System.Drawing.Size(320, 240);
            this.picFrame.TabIndex = 0;
            this.picFrame.TabStop = false;
            // 
            // menuStrip1
            // 
            this.menuStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.fileToolStripMenuItem,
            this.channelToolStripMenuItem});
            this.menuStrip1.Location = new System.Drawing.Point(0, 0);
            this.menuStrip1.Name = "menuStrip1";
            this.menuStrip1.Size = new System.Drawing.Size(513, 24);
            this.menuStrip1.TabIndex = 1;
            this.menuStrip1.Text = "menuStrip1";
            // 
            // fileToolStripMenuItem
            // 
            this.fileToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.openRecordingDirectoryToolStripMenuItem,
            this.exitToolStripMenuItem});
            this.fileToolStripMenuItem.Name = "fileToolStripMenuItem";
            this.fileToolStripMenuItem.Size = new System.Drawing.Size(35, 20);
            this.fileToolStripMenuItem.Text = "File";
            // 
            // openRecordingDirectoryToolStripMenuItem
            // 
            this.openRecordingDirectoryToolStripMenuItem.Name = "openRecordingDirectoryToolStripMenuItem";
            this.openRecordingDirectoryToolStripMenuItem.Size = new System.Drawing.Size(209, 22);
            this.openRecordingDirectoryToolStripMenuItem.Text = "Open Recording Directory";
            this.openRecordingDirectoryToolStripMenuItem.Click += new System.EventHandler(this.openRecordingDirectoryToolStripMenuItem_Click);
            // 
            // exitToolStripMenuItem
            // 
            this.exitToolStripMenuItem.Name = "exitToolStripMenuItem";
            this.exitToolStripMenuItem.Size = new System.Drawing.Size(209, 22);
            this.exitToolStripMenuItem.Text = "Exit";
            this.exitToolStripMenuItem.Click += new System.EventHandler(this.exitToolStripMenuItem_Click);
            // 
            // channelToolStripMenuItem
            // 
            this.channelToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.itmChannel1,
            this.itmChannel2});
            this.channelToolStripMenuItem.Name = "channelToolStripMenuItem";
            this.channelToolStripMenuItem.Size = new System.Drawing.Size(58, 20);
            this.channelToolStripMenuItem.Text = "Channel";
            // 
            // itmChannel1
            // 
            this.itmChannel1.Checked = true;
            this.itmChannel1.CheckState = System.Windows.Forms.CheckState.Checked;
            this.itmChannel1.Name = "itmChannel1";
            this.itmChannel1.Size = new System.Drawing.Size(133, 22);
            this.itmChannel1.Text = "Channel 1";
            this.itmChannel1.Click += new System.EventHandler(this.itmChannel1_Click);
            // 
            // itmChannel2
            // 
            this.itmChannel2.Name = "itmChannel2";
            this.itmChannel2.Size = new System.Drawing.Size(133, 22);
            this.itmChannel2.Text = "Channel 2";
            this.itmChannel2.Click += new System.EventHandler(this.itmChannel2_Click);
            // 
            // btnPlayPause
            // 
            this.btnPlayPause.Enabled = false;
            this.btnPlayPause.Location = new System.Drawing.Point(169, 369);
            this.btnPlayPause.Name = "btnPlayPause";
            this.btnPlayPause.Size = new System.Drawing.Size(83, 27);
            this.btnPlayPause.TabIndex = 3;
            this.btnPlayPause.Text = "Play";
            this.btnPlayPause.UseVisualStyleBackColor = true;
            this.btnPlayPause.Click += new System.EventHandler(this.btnPlayPause_Click);
            // 
            // sldFrameNum
            // 
            this.sldFrameNum.Enabled = false;
            this.sldFrameNum.Location = new System.Drawing.Point(65, 311);
            this.sldFrameNum.Maximum = 100;
            this.sldFrameNum.Name = "sldFrameNum";
            this.sldFrameNum.Size = new System.Drawing.Size(277, 45);
            this.sldFrameNum.TabIndex = 4;
            this.sldFrameNum.Scroll += new System.EventHandler(this.sldFrameNum_Scroll);
            // 
            // sldSpeed
            // 
            this.sldSpeed.Location = new System.Drawing.Point(419, 72);
            this.sldSpeed.Maximum = 6;
            this.sldSpeed.Name = "sldSpeed";
            this.sldSpeed.Orientation = System.Windows.Forms.Orientation.Vertical;
            this.sldSpeed.RightToLeft = System.Windows.Forms.RightToLeft.No;
            this.sldSpeed.Size = new System.Drawing.Size(45, 184);
            this.sldSpeed.TabIndex = 5;
            this.sldSpeed.TickStyle = System.Windows.Forms.TickStyle.TopLeft;
            this.sldSpeed.Scroll += new System.EventHandler(this.sldSpeed_Scroll);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Font = new System.Drawing.Font("Vrinda", 11.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label1.Location = new System.Drawing.Point(394, 235);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(17, 18);
            this.label1.TabIndex = 6;
            this.label1.Text = "1x";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Font = new System.Drawing.Font("Vrinda", 11.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label3.Location = new System.Drawing.Point(390, 129);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(23, 18);
            this.label3.TabIndex = 8;
            this.label3.Text = "10x";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Font = new System.Drawing.Font("Vrinda", 11.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label2.Location = new System.Drawing.Point(394, 208);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(19, 18);
            this.label2.TabIndex = 9;
            this.label2.Text = "2x";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Font = new System.Drawing.Font("Vrinda", 11.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label4.Location = new System.Drawing.Point(394, 181);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(19, 18);
            this.label4.TabIndex = 10;
            this.label4.Text = "3x";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Font = new System.Drawing.Font("Vrinda", 11.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label5.Location = new System.Drawing.Point(394, 155);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(19, 18);
            this.label5.TabIndex = 11;
            this.label5.Text = "5x";
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Font = new System.Drawing.Font("Vrinda", 11.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label6.Location = new System.Drawing.Point(388, 102);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(25, 18);
            this.label6.TabIndex = 12;
            this.label6.Text = "25x";
            // 
            // label7
            // 
            this.label7.AutoSize = true;
            this.label7.Font = new System.Drawing.Font("Vrinda", 11.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label7.Location = new System.Drawing.Point(388, 76);
            this.label7.Name = "label7";
            this.label7.Size = new System.Drawing.Size(25, 18);
            this.label7.TabIndex = 13;
            this.label7.Text = "50x";
            // 
            // label8
            // 
            this.label8.AutoSize = true;
            this.label8.Font = new System.Drawing.Font("Vrinda", 11.25F);
            this.label8.Location = new System.Drawing.Point(328, 376);
            this.label8.Name = "label8";
            this.label8.Size = new System.Drawing.Size(62, 18);
            this.label8.TabIndex = 14;
            this.label8.Text = "Timestamp:";
            // 
            // lblTimestamp
            // 
            this.lblTimestamp.AutoSize = true;
            this.lblTimestamp.Font = new System.Drawing.Font("Vrinda", 11.25F);
            this.lblTimestamp.Location = new System.Drawing.Point(394, 376);
            this.lblTimestamp.Name = "lblTimestamp";
            this.lblTimestamp.Size = new System.Drawing.Size(92, 18);
            this.lblTimestamp.TabIndex = 15;
            this.lblTimestamp.Tag = "";
            this.lblTimestamp.Text = "0000000000_000";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(513, 438);
            this.Controls.Add(this.lblTimestamp);
            this.Controls.Add(this.label8);
            this.Controls.Add(this.label7);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.sldSpeed);
            this.Controls.Add(this.sldFrameNum);
            this.Controls.Add(this.btnPlayPause);
            this.Controls.Add(this.picFrame);
            this.Controls.Add(this.menuStrip1);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.MainMenuStrip = this.menuStrip1;
            this.Name = "Form1";
            this.Text = "WACS Recording Video Playback";
            this.Load += new System.EventHandler(this.Form1_Load);
            ((System.ComponentModel.ISupportInitialize)(this.picFrame)).EndInit();
            this.menuStrip1.ResumeLayout(false);
            this.menuStrip1.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.sldFrameNum)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.sldSpeed)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.PictureBox picFrame;
        private System.Windows.Forms.MenuStrip menuStrip1;
        private System.Windows.Forms.ToolStripMenuItem fileToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem openRecordingDirectoryToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem exitToolStripMenuItem;
        private System.Windows.Forms.Button btnPlayPause;
        private System.Windows.Forms.TrackBar sldFrameNum;
        private System.Windows.Forms.ToolStripMenuItem channelToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem itmChannel1;
        private System.Windows.Forms.ToolStripMenuItem itmChannel2;
        private System.Windows.Forms.TrackBar sldSpeed;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Label label7;
        private System.Windows.Forms.Label label8;
        private System.Windows.Forms.Label lblTimestamp;
    }
}

