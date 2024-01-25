package edu.jhuapl.nstd.swarm.action;

import java.util.logging.*;

import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.util.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;




import java.util.*;
import java.net.*;

public class SNRAction implements Runnable {
	protected BeliefManager _belMgr;
	protected String _agentID;
	protected HashMap<SNR,Long> _map;
	protected boolean _begin = false;

	public SNRAction(String agentID, BeliefManager belMgr) {
		_agentID = agentID;
		_belMgr = belMgr;
		_map = null;
	}

	public void run() {
		String address = Config.getConfig().getProperty("SNR.ipaddress");
		int port = Config.getConfig().getPropertyAsInteger("SNR.port");
		int messageLength = 20;
		byte[] message = new byte[messageLength];
		try {
			Socket sock = new Socket(address, port);
			Logger.getLogger("GLOBAL").info("SNRAction: connection made");
			String msgString;
			while (true) {
				sock.getInputStream().read(message);
				msgString = new String(message);
				processMessage(msgString,message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void processMessage(String msgString, byte[] message)
    {
		if (msgString.startsWith("begin")) {
			_begin = true;
			_map = new HashMap<SNR,Long>();
			return;
		}
		else if (msgString.startsWith("end")) {
			SNRBelief snrb = new SNRBelief(_agentID, _map);
			_belMgr.put(snrb);
			return;
		}
		else if (!_begin) {
			int index = 0;
			int id = ByteManipulator.getInt(message,index,false);
			index += 4;
			float snr = ByteManipulator.getFloat(message,index,false);
			index += 4;
			int mtm = ByteManipulator.getInt(message,index,false);
			index += 4;
			int active = ByteManipulator.getInt(message,index,false);
			index += 4;
			int pulse = ByteManipulator.getInt(message,index,false);
			String addr = new String(Integer.toHexString(id).toString());
			String name = Config.getConfig().getProperty(addr);
			//String name = nb.getMap().get(addr);
			if (name == null) {
				Logger.getLogger("GLOBAL").info("NO NAME ASSOCIATED WITH " + addr + "!!");
				return;
			}
			_map.put(new SNR(_agentID, name, (double)snr, mtm), System.currentTimeMillis());
			Logger.getLogger("GLOBAL").info("name: " + name + " " + snr);
		}
		else {
			int index = 0;
			int recvPkts = ByteManipulator.getInt(message,index,false);
			index += 4;
			int sendPkts = ByteManipulator.getInt(message,index,false);
			_begin = false;
			Logger.getLogger("GLOBAL").info("recv = " + recvPkts);
			PacketInfoBelief pib = new PacketInfoBelief(_agentID, recvPkts, sendPkts);
			_belMgr.put(pib);
		}
	}
}
