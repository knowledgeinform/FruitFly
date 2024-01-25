namespace RemoteGUI
{
    partial class ShutdownForm
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
            this.chkShutdown = new System.Windows.Forms.CheckBox();
            this.btnShutdown = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Font = new System.Drawing.Font("Sylfaen", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label1.Location = new System.Drawing.Point(22, 32);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(356, 22);
            this.label1.TabIndex = 0;
            this.label1.Text = "You are about to shutdown the remote system!";
            // 
            // chkShutdown
            // 
            this.chkShutdown.AutoSize = true;
            this.chkShutdown.Location = new System.Drawing.Point(107, 97);
            this.chkShutdown.Name = "chkShutdown";
            this.chkShutdown.Size = new System.Drawing.Size(169, 17);
            this.chkShutdown.TabIndex = 1;
            this.chkShutdown.Text = "I really do want to shut it down";
            this.chkShutdown.UseVisualStyleBackColor = true;
            this.chkShutdown.CheckedChanged += new System.EventHandler(this.chkShutdown_CheckedChanged);
            // 
            // btnShutdown
            // 
            this.btnShutdown.Enabled = false;
            this.btnShutdown.Location = new System.Drawing.Point(140, 169);
            this.btnShutdown.Name = "btnShutdown";
            this.btnShutdown.Size = new System.Drawing.Size(93, 40);
            this.btnShutdown.TabIndex = 2;
            this.btnShutdown.Text = "Shutdown System";
            this.btnShutdown.UseVisualStyleBackColor = true;
            this.btnShutdown.Click += new System.EventHandler(this.btnShutdown_Click);
            // 
            // ShutdownForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(401, 244);
            this.Controls.Add(this.btnShutdown);
            this.Controls.Add(this.chkShutdown);
            this.Controls.Add(this.label1);
            this.Name = "ShutdownForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "Shutdown";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.CheckBox chkShutdown;
        private System.Windows.Forms.Button btnShutdown;
    }
}