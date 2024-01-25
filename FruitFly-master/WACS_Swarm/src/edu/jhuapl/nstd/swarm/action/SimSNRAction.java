package edu.jhuapl.nstd.swarm.action;

import java.util.logging.*;

import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.util.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;




import java.util.*;

public class SimSNRAction implements Updateable {
	protected BeliefManager _belMgr;
	protected String _agentID;

	public SimSNRAction(String agentID, BeliefManager belMgr) {
		_agentID = agentID;
		_belMgr = belMgr;
	}

	protected double getSNR(Length dist) {
		double distance = dist.getDoubleValue(Length.METERS);
		double snrMax = Config.getConfig().getPropertyAsDouble("snr.max",70.0);
		double commsRange = Config.getConfig().getPropertyAsDouble("simulation.commsRange", 2000.0);
		double toReturn = snrMax -	(distance * (snrMax / commsRange));
		toReturn += (Math.random() * 3.0) * (Math.pow(-1,(int)(Math.random() * 10.0)));
		if (toReturn < 0) toReturn = 0.0;
		if (toReturn > snrMax) toReturn = snrMax;
		return toReturn;
	}

	public void update() 
        {
            try
            {
		AbsolutePosition myPosition;
		AgentPositionBelief b = (AgentPositionBelief)_belMgr.get(AgentPositionBelief.BELIEF_NAME);
		HashMap<SNR,Long> map = new HashMap<SNR,Long>();
		if (b != null) {
			myPosition = b.getPositionTimeName(_agentID).getPosition();	
			Iterator itr = b.getAll().iterator();
			//Logger.getLogger("GLOBAL").info("-----------");
			while (itr.hasNext()) {
				PositionTimeName ptn = (PositionTimeName)itr.next();
				String name = ptn.getName();
				if (name.equalsIgnoreCase(_agentID)) //skip ourselves
					continue;
				AbsolutePosition pos = ptn.getPosition();
				double dsnr = getSNR(myPosition.getRangeTo(pos));
				SNR snr = new SNR(_agentID, name, dsnr, 12);
				map.put(snr, System.currentTimeMillis());
				//Logger.getLogger("GLOBAL").info("created: " + snr + " at " + System.currentTimeMillis());
			}
			//Logger.getLogger("GLOBAL").info("-----------");
			SNRBelief snrb = new SNRBelief(_agentID, map);
			_belMgr.put(snrb);
			int rxPacket = (int)(Math.random() * 20);
			int txPacket = (int)(Math.random() * 20);
			PacketInfoBelief pib = new PacketInfoBelief(_agentID, rxPacket, txPacket);
			_belMgr.put(pib);
		}
            }
            catch (Exception e)
            {
                System.err.println ("Exception in update thread - caught and ignored");
                e.printStackTrace();
            }
	}
}
		
	
