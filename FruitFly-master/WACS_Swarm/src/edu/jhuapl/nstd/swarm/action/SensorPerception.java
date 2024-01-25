//=============================== UNCLASSIFIED ==================================
//
// Copyright (c) 2003 The Johns Hopkins University/Applied Physics Laboratory
// Developed by JHU/APL.
//
// This material may be reproduced by or for the U.S. Government pursuant to
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988).
// For any other permissions please contact JHU/APL.
//
//=============================== UNCLASSIFIED ==================================
package edu.jhuapl.nstd.swarm.action;

import java.util.logging.*;

import java.awt.*;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.util.*;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.jlib.math.Length.*;
import java.lang.Number.*;
import java.lang.Integer.*;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * This class periodically updates the AssetServer with randomly
 * generated data.
 */
public class SensorPerception implements Updateable
{
	String agentID = "";
	NavyAngle bearingAngle;
	int updateCount;

	BeliefManager beliefManager;

	NavyAngle bearingStartAngle, bearingStepAngle;
	int bearingStepInterval;
	double positionCreepOffset, positionCreepStep;

	protected Length _sensorRadius;


	// Perception variables
	boolean perceptionBlobProcessingEnabled, perceptionVideoProcessingEnabled, perceptionBlobCenterUsingCentroid;
	int perceptionVerbosityLevel;
	String logFileDir, logFileName;
	final int INFINITY	= Integer.MAX_VALUE;
	SimpleDateFormat dateFmt	= new SimpleDateFormat("yyyyMMddHHmmss");
	String	dateString			= dateFmt.format(new Date());

	// Searchedness Table for generating sensor effectiveness pattern
	double[][] searchednessTable;

	// Formatting variables
	/*DecimalFormat df_11f3			= new DecimalFormat("###,##0.000");
	DecimalFormat df_2d				= new DecimalFormat("00");
	NumericalObjectFormat nf_7f3	= new NumericalObjectFormat("000.000");
	NumericalObjectFormat nf_10f6	= new NumericalObjectFormat("000.000000");
	NumericalObjectFormat nf_LatDMS = new NumericalObjectFormat("Lat~: 00:00:00 +", ':', "N", "S", new Angle.AngleUnit[] {Angle.MINUTES, Angle.SECONDS});
	NumericalObjectFormat nf_LonDMS = new NumericalObjectFormat("Lon~: 00:00:00 +", ':', "E", "W", new Angle.AngleUnit[] {Angle.MINUTES, Angle.SECONDS});
	*/

	public SensorPerception(BeliefManager mgr, String agentID)
	{

	 	// initialize counters, etc.
	 	updateCount		= 0;
		this.agentID = agentID;
		this.beliefManager = mgr;

		_sensorRadius = new Length(Config.getConfig().getPropertyAsDouble("SearchCanvas.sensorRadius.Meters",250), Length.METERS);

	} // end constructor

	public void update()
	{
		_sensorRadius = new Length(Config.getConfig().getPropertyAsDouble("SearchCanvas.sensorRadius.Meters",250), Length.METERS);
		updateCount++; // Count this update

		try
		{
			AbsolutePosition p;
			AgentPositionBelief b = new AgentPositionBelief();
			b = (AgentPositionBelief)beliefManager.get(b.getName());
			if (b != null)
			{

				p = b.getPositionTimeName(agentID).getPosition();

				//update my searchedness
				
				SearchBelief searchBelief = (SearchBelief)beliefManager.get(SearchBelief.BELIEF_NAME);
				if (searchBelief != null && p != null) {
					if (_sensorRadius.equals(Length.ZERO)) {
						Point point = searchBelief.getStateSpace().getPoint(p);
						if (point != null) {
							searchBelief.getStateSpace().set(point, SearchBelief.SEARCHED_VALUE);
						} else {
							searchBelief = new SearchBelief(agentID, p);
							beliefManager.put(searchBelief);
						}
					}
					else {
						//Logger.getLogger("GLOBAL").info("sensorRadius = " + _sensorRadius);
						Ellipse e = new Ellipse(_sensorRadius, _sensorRadius, p, NavyAngle.NORTH);
						PrimitiveTypeGeocentricMatrix search = searchBelief.getStateSpace();
						for (int x=0; x<search.getXSize(); x++) {
							for (int y=0; y<search.getYSize(); y++) {
								if (e.contains(search.getPosition(x,y))) {
									search.set(new Point(x,y), SearchBelief.SEARCHED_VALUE);
								}
							}
						}
					}
				}
			} // b != null
		}
                catch (Exception e)
                {
                    System.err.println ("Exception in update thread - caught and ignored");
                    e.printStackTrace();
                }
	} // end Update()



} // end class SensorPerception
