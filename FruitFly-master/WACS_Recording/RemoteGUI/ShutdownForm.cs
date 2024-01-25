using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace RemoteGUI
{
    public partial class ShutdownForm : Form
    {
        public ShutdownForm()
        {
            InitializeComponent();
        }

        private void chkShutdown_CheckedChanged(object sender, EventArgs e)
        {
            btnShutdown.Enabled = chkShutdown.Checked;
        }

        private void btnShutdown_Click(object sender, EventArgs e)
        {
            CommandPacket commandPacket = new CommandPacket();
            commandPacket.command = COMMAND_ID.SHUTDOWN;
            CommandSender.SendCommandPacket(commandPacket);   
        }
    }
}
