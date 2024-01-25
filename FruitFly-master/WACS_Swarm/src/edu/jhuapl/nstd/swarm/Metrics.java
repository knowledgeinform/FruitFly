//=============================== UNCLASSIFIED ==================================
//
// Copyright (c) 1999 The Johns Hopkins University/Applied Physics Laboratory
// Developed by JHU/APL.
//
// This material may be reproduced by or for the U.S. Government pursuant to
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988).
// For any other permissions please contact JHU/APL.
//
//=============================== UNCLASSIFIED ==================================
// File created: Tue Nov  9 13:10:34 1999

package edu.jhuapl.nstd.swarm;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.awt.Point;
import edu.jhuapl.jlib.collections.storage.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.jlib.math.random.*;

import edu.jhuapl.nstd.swarm.display.*;
import edu.jhuapl.nstd.swarm.action.*;
import edu.jhuapl.nstd.swarm.behavior.*;
import edu.jhuapl.nstd.swarm.comms.*;
import edu.jhuapl.nstd.swarm.util.*;
import edu.jhuapl.jlib.net.*;
import edu.jhuapl.nstd.swarm.belief.AgentBearingBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.BearingTimeName;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.Classification;
import edu.jhuapl.nstd.swarm.belief.ClassificationBelief;
import edu.jhuapl.nstd.swarm.belief.ClassificationTimeName;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;



/**
 * This is the class that implements the agent
 *
 * <P>UNCLASSIFIED
 *
 * @author Chad Hawthorne ext. 3728
 */

public class Metrics implements Updateable{

  protected BeliefManager _belMgr;
  protected Config _config = null;

  protected FileWriter _fileOut;
  protected ObjectOutputStream _objOut;
  protected PrintWriter _printWriter;

  public Metrics(BeliefManager belMgr, UpdateManager upMgr)
  {
    try
    {
        _config = Config.getConfig();
        String path = _config.getProperty("Metrics.path", "./metrics");
	if(!(path.endsWith("/") || path.endsWith("\\")))
            path = path + "/";
        boolean success = new File(path).mkdirs();
        int count = 1;
        File f = new File(path + System.getProperty("agent.name")+"." + count++ + ".metrics");
        while (f.exists())
        {
                f = new File(path + System.getProperty("agent.name")+"." + count++ + ".metrics");
        }
      _fileOut = new FileWriter(f);
      _printWriter = new PrintWriter(new BufferedWriter(_fileOut));
      //fileOut = new FileOutputStream(new File(System.getProperty("agent.name")+".metrics"));
      //objOut = new ObjectOutputStream(fileOut);

      _belMgr = belMgr;

			collectConfig(System.getProperty("agent.name"));

      upMgr.register(this, Updateable.METRICS);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

	protected void collectConfig(String agentName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("config/defaultConfig.txt")));
		write("defaultConfig:\n");
		String line = "";
		while ((line = reader.readLine()) != null) {
			write(line + "\n");
		}
		write("end defaultConfig\n");
		reader.close();

		reader = new BufferedReader(new FileReader(new File("config/" + agentName + "Config.txt")));
		write(agentName + "Config:\n");
		line = "";
		while ((line = reader.readLine()) != null) {
			write(line + "\n");
		}
		write("end " + agentName + "Config\n");
		reader.close();
	}

	protected void write(String s) {
		_printWriter.write(s);
		_printWriter.flush();
	}

  public void update() {
		try {
			write("time: " +
					new Long(TimeManagerFactory.getTimeManager().getTime()).toString());
			write("\n");
			//collect all positions
			collectAllPositions();
			collectAllBearings();
			write("end time\n");

		} catch (IOException e) {
      e.printStackTrace();
    }
  }

	protected void collectTargets() throws IOException {
		TargetActualBelief tb = (TargetActualBelief)_belMgr.get(TargetActualBelief.BELIEF_NAME);
		ClassificationBelief cb = (ClassificationBelief)_belMgr.get(ClassificationBelief.BELIEF_NAME);
		if (tb != null) {
			write("belief: targetBelief\n");
			synchronized(tb) {
				Iterator itr = tb.getAll().iterator();
				while (itr.hasNext()) {
					PositionTimeName ptn = (PositionTimeName)itr.next();
					LatLonAltPosition p = ptn.getPosition().asLatLonAltPosition();
					String name = ptn.getName();
					int classification = Classification.NEUTRAL;
					if (cb != null) {
						ClassificationTimeName ctn = cb.getClassificationTimeName(name);
						if (ctn != null) {
							classification = ctn.getClassification();
						}
					}
					write(name + "   " +
							p.getLatitude().getDoubleValue(Angle.DEGREES) + "   " +
							p.getLongitude().getDoubleValue(Angle.DEGREES));
					write("\n");
				}
			}
			write("end targetBelief\n");
		}
	}

  protected void collectAllPositions() throws IOException {
    AgentPositionBelief b = new AgentPositionBelief();
    b = (AgentPositionBelief)_belMgr.get(b.getName());

    if (b != null) {
			write("belief: agentPositionBelief\n");
      synchronized (b) {
				Collection c = b.getAll();
				Iterator i = c.iterator();
				while(i.hasNext()){
					PositionTimeName posTimeName = (PositionTimeName)i.next();
					LatLonAltPosition p = posTimeName.getPosition().asLatLonAltPosition();
					String name = posTimeName.getName();
					write(name + "   " +
							p.getLatitude().getDoubleValue(Angle.DEGREES) + "   " +
							p.getLongitude().getDoubleValue(Angle.DEGREES) + "   " +
							p.getAltitude().getDoubleValue(Length.METERS) + "   " +
							posTimeName.getHeading().getDoubleValue(Angle.DEGREES));
					write("\n");
				}
				write("end agentPositionBelief\n");
      }
    }
  }

  protected void collectAllBearings() throws IOException {
    AgentBearingBelief b = (AgentBearingBelief)_belMgr.get(AgentBearingBelief.BELIEF_NAME);

    if (b != null) {
			write("belief: agentBearingBelief\n");
      synchronized (b) {
				Collection c = b.getAll();
				Iterator i = c.iterator();
				while(i.hasNext()){
					BearingTimeName btn = (BearingTimeName)i.next();
					String name = btn.getName();
					write(name + "   " +
							btn.getCurrentBearing().getDoubleValue(Angle.DEGREES) + "   " +
							btn.getDesiredBearing().getDoubleValue(Angle.DEGREES));
					write("\n");
				}
				write("end agentBearingBelief\n");
      }
    }
  }

}



//=============================== UNCLASSIFIED ==================================
