namespace WACS_EthernetVideoClient.NET
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
            this.lblNoVideo = new System.Windows.Forms.Label();
            ((System.ComponentModel.ISupportInitialize)(this.picFrame)).BeginInit();
            this.SuspendLayout();
            // 
            // picFrame
            // 
            this.picFrame.BackColor = System.Drawing.Color.Gray;
            this.picFrame.Location = new System.Drawing.Point(0, 0);
            this.picFrame.Name = "picFrame";
            this.picFrame.Size = new System.Drawing.Size(320, 240);
            this.picFrame.TabIndex = 0;
            this.picFrame.TabStop = false;
            // 
            // lblNoVideo
            // 
            this.lblNoVideo.AutoSize = true;
            this.lblNoVideo.BackColor = System.Drawing.Color.Black;
            this.lblNoVideo.Cursor = System.Windows.Forms.Cursors.Default;
            this.lblNoVideo.Font = new System.Drawing.Font("Verdana", 11.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblNoVideo.ForeColor = System.Drawing.Color.Red;
            this.lblNoVideo.Location = new System.Drawing.Point(122, 107);
            this.lblNoVideo.Name = "lblNoVideo";
            this.lblNoVideo.Size = new System.Drawing.Size(80, 18);
            this.lblNoVideo.TabIndex = 1;
            this.lblNoVideo.Text = "No Video";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(320, 240);
            this.Controls.Add(this.lblNoVideo);
            this.Controls.Add(this.picFrame);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.Name = "Form1";
            this.Text = "Ethernet Video Client";
            ((System.ComponentModel.ISupportInitialize)(this.picFrame)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.PictureBox picFrame;
        private System.Windows.Forms.Label lblNoVideo;
    }
}

