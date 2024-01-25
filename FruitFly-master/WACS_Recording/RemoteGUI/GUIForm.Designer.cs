namespace RemoteGUI
{
    partial class GUIForm
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
            this.label1 = new System.Windows.Forms.Label();
            this.lblCommsStatus = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.lblFrameRate = new System.Windows.Forms.Label();
            this.barQueueFill = new System.Windows.Forms.ProgressBar();
            this.label2 = new System.Windows.Forms.Label();
            this.lblNumFramesDropped = new System.Windows.Forms.Label();
            this.label6 = new System.Windows.Forms.Label();
            this.lblQueueLength = new System.Windows.Forms.Label();
            this.lblRecordingStatus = new System.Windows.Forms.Label();
            this.label7 = new System.Windows.Forms.Label();
            this.lblLastUpdateTime = new System.Windows.Forms.Label();
            this.label8 = new System.Windows.Forms.Label();
            this.btnShowShutdown = new System.Windows.Forms.Button();
            this.btnRecordingMode = new System.Windows.Forms.Button();
            this.lblIsAdeptPresent = new System.Windows.Forms.Label();
            this.label9 = new System.Windows.Forms.Label();
            this.lblNumWarpUnderflows = new System.Windows.Forms.Label();
            this.label10 = new System.Windows.Forms.Label();
            this.lblRecordingDuration = new System.Windows.Forms.Label();
            this.label11 = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Font = new System.Drawing.Font("Sylfaen", 17.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label1.Location = new System.Drawing.Point(211, 84);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(159, 29);
            this.label1.TabIndex = 0;
            this.label1.Text = "Comms Status:";
            // 
            // lblCommsStatus
            // 
            this.lblCommsStatus.AutoSize = true;
            this.lblCommsStatus.BackColor = System.Drawing.Color.Crimson;
            this.lblCommsStatus.Font = new System.Drawing.Font("Sylfaen", 17.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblCommsStatus.Location = new System.Drawing.Point(375, 84);
            this.lblCommsStatus.Name = "lblCommsStatus";
            this.lblCommsStatus.Size = new System.Drawing.Size(161, 29);
            this.lblCommsStatus.TabIndex = 1;
            this.lblCommsStatus.Text = "Not Connected";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Font = new System.Drawing.Font("Microsoft Sans Serif", 27.75F, ((System.Drawing.FontStyle)((System.Drawing.FontStyle.Bold | System.Drawing.FontStyle.Italic))), System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label3.Location = new System.Drawing.Point(49, 19);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(663, 42);
            this.label3.TabIndex = 2;
            this.label3.Text = "WACS Video Recording Remote GUI";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Font = new System.Drawing.Font("Sylfaen", 14.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label4.Location = new System.Drawing.Point(21, 386);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(154, 25);
            this.label4.TabIndex = 3;
            this.label4.Text = "Frame Rate (FPS):";
            // 
            // lblFrameRate
            // 
            this.lblFrameRate.AutoSize = true;
            this.lblFrameRate.Font = new System.Drawing.Font("Sylfaen", 14.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblFrameRate.Location = new System.Drawing.Point(172, 386);
            this.lblFrameRate.Name = "lblFrameRate";
            this.lblFrameRate.Size = new System.Drawing.Size(22, 25);
            this.lblFrameRate.TabIndex = 4;
            this.lblFrameRate.Text = "0";
            // 
            // barQueueFill
            // 
            this.barQueueFill.BackColor = System.Drawing.SystemColors.Control;
            this.barQueueFill.Location = new System.Drawing.Point(251, 340);
            this.barQueueFill.Name = "barQueueFill";
            this.barQueueFill.Size = new System.Drawing.Size(251, 30);
            this.barQueueFill.Style = System.Windows.Forms.ProgressBarStyle.Continuous;
            this.barQueueFill.TabIndex = 5;
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Font = new System.Drawing.Font("Sylfaen", 14.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label2.Location = new System.Drawing.Point(246, 310);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(132, 25);
            this.label2.TabIndex = 6;
            this.label2.Text = "Queue Length:";
            // 
            // lblNumFramesDropped
            // 
            this.lblNumFramesDropped.AutoSize = true;
            this.lblNumFramesDropped.Font = new System.Drawing.Font("Sylfaen", 14.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblNumFramesDropped.Location = new System.Drawing.Point(434, 386);
            this.lblNumFramesDropped.Name = "lblNumFramesDropped";
            this.lblNumFramesDropped.Size = new System.Drawing.Size(22, 25);
            this.lblNumFramesDropped.TabIndex = 8;
            this.lblNumFramesDropped.Text = "0";
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Font = new System.Drawing.Font("Sylfaen", 14.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label6.Location = new System.Drawing.Point(246, 386);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(195, 25);
            this.label6.TabIndex = 7;
            this.label6.Text = "Num Frames Dropped:";
            // 
            // lblQueueLength
            // 
            this.lblQueueLength.AutoSize = true;
            this.lblQueueLength.Font = new System.Drawing.Font("Sylfaen", 14.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblQueueLength.Location = new System.Drawing.Point(372, 310);
            this.lblQueueLength.Name = "lblQueueLength";
            this.lblQueueLength.Size = new System.Drawing.Size(22, 25);
            this.lblQueueLength.TabIndex = 9;
            this.lblQueueLength.Text = "0";
            // 
            // lblRecordingStatus
            // 
            this.lblRecordingStatus.AutoSize = true;
            this.lblRecordingStatus.BackColor = System.Drawing.Color.Khaki;
            this.lblRecordingStatus.Font = new System.Drawing.Font("Sylfaen", 17.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblRecordingStatus.Location = new System.Drawing.Point(375, 130);
            this.lblRecordingStatus.Name = "lblRecordingStatus";
            this.lblRecordingStatus.Size = new System.Drawing.Size(82, 29);
            this.lblRecordingStatus.TabIndex = 11;
            this.lblRecordingStatus.Text = "Paused";
            // 
            // label7
            // 
            this.label7.AutoSize = true;
            this.label7.Font = new System.Drawing.Font("Sylfaen", 17.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label7.Location = new System.Drawing.Point(189, 130);
            this.label7.Name = "label7";
            this.label7.Size = new System.Drawing.Size(183, 29);
            this.label7.TabIndex = 10;
            this.label7.Text = "Recording Status:";
            // 
            // lblLastUpdateTime
            // 
            this.lblLastUpdateTime.AutoSize = true;
            this.lblLastUpdateTime.BackColor = System.Drawing.SystemColors.Control;
            this.lblLastUpdateTime.Font = new System.Drawing.Font("Sylfaen", 17.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblLastUpdateTime.Location = new System.Drawing.Point(373, 208);
            this.lblLastUpdateTime.Name = "lblLastUpdateTime";
            this.lblLastUpdateTime.Size = new System.Drawing.Size(53, 29);
            this.lblLastUpdateTime.TabIndex = 15;
            this.lblLastUpdateTime.Text = "N/A";
            // 
            // label8
            // 
            this.label8.AutoSize = true;
            this.label8.Font = new System.Drawing.Font("Sylfaen", 17.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label8.Location = new System.Drawing.Point(177, 208);
            this.label8.Name = "label8";
            this.label8.Size = new System.Drawing.Size(193, 29);
            this.label8.TabIndex = 14;
            this.label8.Text = "Last Update Time:";
            // 
            // btnShowShutdown
            // 
            this.btnShowShutdown.Font = new System.Drawing.Font("Sylfaen", 11.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnShowShutdown.Image = global::RemoteGUI.Properties.Resources.power;
            this.btnShowShutdown.Location = new System.Drawing.Point(685, 435);
            this.btnShowShutdown.Name = "btnShowShutdown";
            this.btnShowShutdown.Size = new System.Drawing.Size(47, 46);
            this.btnShowShutdown.TabIndex = 13;
            this.btnShowShutdown.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
            this.btnShowShutdown.UseVisualStyleBackColor = false;
            this.btnShowShutdown.Click += new System.EventHandler(this.btnShowShutdown_Click);
            // 
            // btnRecordingMode
            // 
            this.btnRecordingMode.Font = new System.Drawing.Font("Sylfaen", 11.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnRecordingMode.Image = global::RemoteGUI.Properties.Resources.record;
            this.btnRecordingMode.ImageAlign = System.Drawing.ContentAlignment.TopLeft;
            this.btnRecordingMode.Location = new System.Drawing.Point(326, 435);
            this.btnRecordingMode.Name = "btnRecordingMode";
            this.btnRecordingMode.Size = new System.Drawing.Size(91, 33);
            this.btnRecordingMode.TabIndex = 12;
            this.btnRecordingMode.Text = "Record";
            this.btnRecordingMode.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
            this.btnRecordingMode.UseVisualStyleBackColor = true;
            this.btnRecordingMode.Click += new System.EventHandler(this.btnRecordingMode_Click);
            // 
            // lblIsAdeptPresent
            // 
            this.lblIsAdeptPresent.AutoSize = true;
            this.lblIsAdeptPresent.BackColor = System.Drawing.SystemColors.Control;
            this.lblIsAdeptPresent.Font = new System.Drawing.Font("Sylfaen", 17.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblIsAdeptPresent.Location = new System.Drawing.Point(375, 170);
            this.lblIsAdeptPresent.Name = "lblIsAdeptPresent";
            this.lblIsAdeptPresent.Size = new System.Drawing.Size(42, 29);
            this.lblIsAdeptPresent.TabIndex = 17;
            this.lblIsAdeptPresent.Text = "No";
            // 
            // label9
            // 
            this.label9.AutoSize = true;
            this.label9.Font = new System.Drawing.Font("Sylfaen", 17.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label9.Location = new System.Drawing.Point(214, 170);
            this.label9.Name = "label9";
            this.label9.Size = new System.Drawing.Size(158, 29);
            this.label9.TabIndex = 16;
            this.label9.Text = "Adept Present:";
            // 
            // lblNumWarpUnderflows
            // 
            this.lblNumWarpUnderflows.AutoSize = true;
            this.lblNumWarpUnderflows.Font = new System.Drawing.Font("Sylfaen", 14.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblNumWarpUnderflows.Location = new System.Drawing.Point(699, 386);
            this.lblNumWarpUnderflows.Name = "lblNumWarpUnderflows";
            this.lblNumWarpUnderflows.Size = new System.Drawing.Size(22, 25);
            this.lblNumWarpUnderflows.TabIndex = 19;
            this.lblNumWarpUnderflows.Text = "0";
            // 
            // label10
            // 
            this.label10.AutoSize = true;
            this.label10.Font = new System.Drawing.Font("Sylfaen", 14.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label10.Location = new System.Drawing.Point(501, 386);
            this.label10.Name = "label10";
            this.label10.Size = new System.Drawing.Size(208, 25);
            this.label10.TabIndex = 18;
            this.label10.Text = "Num Warp Underflows:";
            // 
            // lblRecordingDuration
            // 
            this.lblRecordingDuration.AutoSize = true;
            this.lblRecordingDuration.BackColor = System.Drawing.SystemColors.Control;
            this.lblRecordingDuration.Font = new System.Drawing.Font("Sylfaen", 17.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblRecordingDuration.Location = new System.Drawing.Point(373, 246);
            this.lblRecordingDuration.Name = "lblRecordingDuration";
            this.lblRecordingDuration.Size = new System.Drawing.Size(97, 29);
            this.lblRecordingDuration.TabIndex = 21;
            this.lblRecordingDuration.Text = "00:00:00";
            // 
            // label11
            // 
            this.label11.AutoSize = true;
            this.label11.Font = new System.Drawing.Font("Sylfaen", 17.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label11.Location = new System.Drawing.Point(157, 246);
            this.label11.Name = "label11";
            this.label11.Size = new System.Drawing.Size(213, 29);
            this.label11.TabIndex = 20;
            this.label11.Text = "Recording Duration:";
            // 
            // GUIForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(744, 493);
            this.Controls.Add(this.lblRecordingDuration);
            this.Controls.Add(this.label11);
            this.Controls.Add(this.lblNumWarpUnderflows);
            this.Controls.Add(this.label10);
            this.Controls.Add(this.lblIsAdeptPresent);
            this.Controls.Add(this.label9);
            this.Controls.Add(this.lblLastUpdateTime);
            this.Controls.Add(this.label8);
            this.Controls.Add(this.btnShowShutdown);
            this.Controls.Add(this.btnRecordingMode);
            this.Controls.Add(this.lblRecordingStatus);
            this.Controls.Add(this.label7);
            this.Controls.Add(this.lblQueueLength);
            this.Controls.Add(this.lblNumFramesDropped);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.barQueueFill);
            this.Controls.Add(this.lblFrameRate);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.lblCommsStatus);
            this.Controls.Add(this.label1);
            this.Name = "GUIForm";
            this.Text = "WACS Video Recording Remote GUI";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label lblCommsStatus;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label lblFrameRate;
        private System.Windows.Forms.ProgressBar barQueueFill;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label lblNumFramesDropped;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Label lblQueueLength;
        private System.Windows.Forms.Label lblRecordingStatus;
        private System.Windows.Forms.Label label7;
        private System.Windows.Forms.Button btnRecordingMode;
        private System.Windows.Forms.Button btnShowShutdown;
        private System.Windows.Forms.Label lblLastUpdateTime;
        private System.Windows.Forms.Label label8;
        private System.Windows.Forms.Label lblIsAdeptPresent;
        private System.Windows.Forms.Label label9;
        private System.Windows.Forms.Label lblNumWarpUnderflows;
        private System.Windows.Forms.Label label10;
        private System.Windows.Forms.Label lblRecordingDuration;
        private System.Windows.Forms.Label label11;


    }
}

