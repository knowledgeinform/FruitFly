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
import java.net.*;

import java.io.*;
import java.awt.event.*;
import javax.swing.*;



import java.util.*;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.jlib.ui.math.*;

import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.behavior.*;
import edu.jhuapl.nstd.swarm.behavior.group.*;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.belief.mode.*;
import edu.jhuapl.nstd.swarm.util.Config;


public class AvoidObstacleAction {

	/**
   * An array of vectors for no-go areas in the search space.
   */
	ArrayList _vectorsRepulse = new ArrayList();

  /**
   * The multiplication factor of a grid square that is a "no-go" grid.
   */
  final Unitless NO_GO_FACTOR = new Unitless(50.0, Unitless.UNITLESS);
  /**
   * The range that no-go grids will begin to effect the vector output.
   */
  final Length NO_GO_RANGE = new Length(300.0, Length.METERS);

	BeliefManager _belMgr = null;
  
	public AvoidObstacleAction(BeliefManager belMgr) {
		_belMgr = belMgr;
  }

	public RangeBearingHeightOffset avoidObstacle(String agentID, AbsolutePosition myPosition, 
																								NavyAngle myBearing)
	{
		NoGoBelief noGoBelief = new NoGoBelief();
		noGoBelief = (NoGoBelief)_belMgr.get(noGoBelief.getName());
		NoGoBelief dNoGoBelief = new NoGoBelief();
		dNoGoBelief = (NoGoBelief)_belMgr.get(DynamicNoGoBelief.BELIEF_NAME);
		//Logger.getLogger("GLOBAL").info("in nogo");
		if (noGoBelief == null && dNoGoBelief == null) {
			//Logger.getLogger("GLOBAL").info("noGoBelief = null");
			return null;
		}
		else if (noGoBelief == null && dNoGoBelief != null)
			noGoBelief = dNoGoBelief;
		else if (noGoBelief != null && dNoGoBelief != null) {
			noGoBelief = noGoBelief.clone();
			noGoBelief.addBelief(dNoGoBelief);
		}
		else
        {
			//Logger.getLogger("GLOBAL").info("dynamicnogo = null");
        }
		RangeBearingHeightOffset result = null;
		_vectorsRepulse.clear();
		//calculate teardrop shape surrounding the robot (global)
		java.util.List dropPoints = calculateDropPoints(myPosition, myBearing);
		Region dropRegion = new Region(dropPoints);
		Iterator dropItr = dropPoints.iterator();
		
		//get list of nogo regions
		java.util.List noGoIntersectionPoints = new LinkedList();
		AbsolutePosition closestOneObstacle = LatLonAltPosition.ORIGIN;
		AbsolutePosition closestAllObstacle = LatLonAltPosition.ORIGIN;
		
		//First, find intersections of all no-go's with teardrop
		AbsolutePosition prevDropPoint = (AbsolutePosition)dropItr.next();
		AbsolutePosition firstDropPoint = prevDropPoint;
		double minRangeAllObstacle = 999999;
		boolean threatInRange = false;
		
		while(dropItr.hasNext()){
			AbsolutePosition currDropPoint = (AbsolutePosition)dropItr.next();

			//get intersections of all nogo regions with current drop segment
			noGoIntersectionPoints = noGoBelief.
				getIntersectionPoints(prevDropPoint.asLatLonAltPosition(), 
															currDropPoint.asLatLonAltPosition(),
															agentID);
			Iterator noGoIntersectItr = noGoIntersectionPoints.iterator();

			if(Config.getConfig().getPropertyAsBoolean("debugVerbose", false)){
				searchBehaviorLogger("hasInterctions = " + noGoIntersectItr.hasNext() + "\n");		
			}
			
			//look through intersction points.  if closer than any other obstacle, mark as minRange
			while(noGoIntersectItr.hasNext()){
				AbsolutePosition intPoint = (AbsolutePosition)noGoIntersectItr.next();
				Length lenToIntersection = myPosition.getRangeTo(intPoint);
				double rangeToIntersection = Accessor.getDoubleValue(lenToIntersection, Length.METERS);

				if(Config.getConfig().getPropertyAsBoolean("debugVerbose", false)){
					searchBehaviorLogger(System.currentTimeMillis() + "  rangeToInt: " + rangeToIntersection + "\n" + 
															 "  myPosition: " + myPosition.asLatLonAltPosition().toString() + "\n" +
															 "  intPoint  : " + intPoint.asLatLonAltPosition().toString() + "\n");
				}						
			
				if(rangeToIntersection < minRangeAllObstacle){
					minRangeAllObstacle = rangeToIntersection;
					closestAllObstacle = intPoint;
					threatInRange = true;
				}
			}
		}		
		//Secondly, look at end points and closest point on segment of no-gos
		boolean insideRegion = false;
		LatLonAltPosition noGoCOG = LatLonAltPosition.ORIGIN;
		HashMap regionMap = noGoBelief.getAllRegions();
		Set keys = regionMap.keySet();
		Iterator noGoItr = keys.iterator();
		while(noGoItr.hasNext()){
			String obsName = (String)noGoItr.next();
			//skip ourselves
			if (obsName.equals(agentID))
				continue;
			Logger.getLogger("GLOBAL").info("nogo OBS NAME: " + obsName);
			Region currNoGo = 
				(Region)((NoGoTimeName)regionMap.get(obsName)).getObstacle();

			//Make sure that current robot position is not inside a nogo region
			//
			//TODO: make sure not in multiple obstacles
			if (currNoGo == null) {
				Logger.getLogger("GLOBAL").info("currnogo is null");
				continue;
			}
			if(currNoGo.contains(myPosition)){
				insideRegion = true;
				noGoCOG = currNoGo.getCenterOfGravity();
				break;
			}
			Logger.getLogger("GLOBAL").info("currnogo: " + currNoGo);
			Iterator regItr = currNoGo.iterator();
			AbsolutePosition currPos = (AbsolutePosition)regItr.next();
			AbsolutePosition firstPos = currPos;
			//check the first currPos at end (where it is part of the Nth and 0th line segment)
			while(regItr.hasNext()){
				AbsolutePosition nextPos = (AbsolutePosition)regItr.next();
				AbsolutePosition closestPos = findClosestPos(myPosition, currPos, nextPos);
				Length lenToClosest = myPosition.getRangeTo(closestPos);
				Length lenToNext = myPosition.getRangeTo(nextPos);
				double rangeToClosest = Accessor.getDoubleValue(lenToClosest, Length.METERS);
				double rangeToNext = Accessor.getDoubleValue(lenToNext, Length.METERS);						
				
				if(rangeToClosest < minRangeAllObstacle && dropRegion.contains(closestPos)){
					minRangeAllObstacle = rangeToClosest;
					closestAllObstacle = closestPos;
					threatInRange = true;
				}
				if(rangeToNext < minRangeAllObstacle && dropRegion.contains(nextPos)){
					minRangeAllObstacle = rangeToClosest;
					closestAllObstacle = closestPos;
					threatInRange = true;							
				}
				currPos = nextPos;
			}
			//last segment case
			AbsolutePosition closestPos = findClosestPos(myPosition, currPos, firstPos);
			Length lenToClosest = myPosition.getRangeTo(closestPos);
			Length lenToNext = myPosition.getRangeTo(firstPos);
			double rangeToClosest = Accessor.getDoubleValue(lenToClosest, Length.METERS);
			double rangeToNext = Accessor.getDoubleValue(lenToNext, Length.METERS);
			if(rangeToClosest < minRangeAllObstacle && dropRegion.contains(closestPos)){
				minRangeAllObstacle = rangeToClosest;
				closestAllObstacle = closestPos;
				threatInRange = true;						
			}
			if(rangeToNext < minRangeAllObstacle && dropRegion.contains(currPos)){
				minRangeAllObstacle = rangeToClosest;
				closestAllObstacle = closestPos;
				threatInRange = true;						
			}
		}
		
		if(insideRegion){
			//repulse away from center of nogo
			Logger.getLogger("GLOBAL").info("\n !!!! INSIDE REPULSE !!!!\n");
			AgentBearingBelief bearingBelief = new AgentBearingBelief(agentID, myBearing, myBearing);
			_belMgr.put(bearingBelief);
			result = noGoCOG.getRangeBearingHeightOffsetTo(myPosition);
		}
		else if(threatInRange){
			//repulse away from closest threat
			minusVector(myPosition, myBearing, closestAllObstacle, NO_GO_FACTOR, NO_GO_RANGE, 1.0);
			AgentBearingBelief bearingBelief = new AgentBearingBelief(agentID, myBearing, myBearing);
			_belMgr.put(bearingBelief);
			result = sumVectorsRepulse(myPosition);
		}
		else {
			//nothing
			result = new RangeBearingHeightOffset(Length.ZERO, NavyAngle.ZERO, Length.ZERO);
		}
		if(Config.getConfig().getPropertyAsBoolean("debugVerbose", false)){
			searchBehaviorLogger("\n\n");		
		}
		if (result.getRange().isLessThan(new Length(0.0000001, Length.METERS))) {
			return null;
		}
		return result;
	}

	/**
	 * Determines if element is within robot's threat "radius"
	 * @return -1.0 if no threat, [0,1] if threat
	 */
	private double withinTeardrop(AbsolutePosition elementPosition, AbsolutePosition myPosition, NavyAngle myBearing){
		//define the units that we will do all our math in
    Length.LengthUnit units = Length.METERS;

    //get the distance between the points
    Length dist = myPosition.getRangeTo(elementPosition);
		double range = Accessor.getDoubleValue(dist, units);

		//get the angle from robot to element in global frame
    NavyAngle myAngle = myPosition.getBearingTo(elementPosition);
	  double doubAngle = Accessor.getDoubleValue(myAngle.asAngle(), Angle.RADIANS);
		
		//get x,y distance to element
		double x = range * Math.cos(doubAngle);
		double y = range * Math.sin(doubAngle);
		
		//warp points to frame where robot is oriented on x-axis (axis where angle is measured from)
		double myHeading = Accessor.getDoubleValue(myBearing.asAngle(), Angle.RADIANS);
		double xWarp = x * Math.cos(-1.0*myHeading) - y * Math.sin(-1.0*myHeading);
		double yWarp = x * Math.sin(-1.0*myHeading) + y * Math.cos(-1.0*myHeading);

		//get angle to element in warped frame
		double diffAngle = doubAngle - myHeading;
		if(diffAngle < -1.0 * Math.PI){
			diffAngle += 2.0 * Math.PI;
		} else if(diffAngle > Math.PI){
			diffAngle -= 2.0 * Math.PI;
		}
		
		//ignore elements behind us
		if(diffAngle > Math.PI/2 || diffAngle < -1.0*Math.PI/2){
			return -1.0;
		}

		//get teardrop range & compare
		//
		//
		//THESE NEED TO CHANGE TO BE SPEED DEPENDENT (look ahead further)
		double dropRangeMax = Config.getConfig().getPropertyAsDouble("dropRangeMax", 133.0);
		double dropRangeMin = Config.getConfig().getPropertyAsDouble("dropRangeMin", 40.0);
		double dropRangeDelta = (dropRangeMax - dropRangeMin) * 2.0 / Math.PI;
		double dropRange = dropRangeMax - Math.abs(diffAngle) * dropRangeDelta;

		if(range <= dropRange){
			return (1.0 - range / dropRange);
			//return (dropRangeMax - range);
		}
		return -1.0;
	}

	/**
   * Sums all of the repulsive vectors to get a single vector.
   * @return The summation of all vectors.
   */
  private RangeBearingHeightOffset sumVectorsRepulse(AbsolutePosition myPosition) {
    Iterator i = _vectorsRepulse.iterator();
    AbsolutePosition pos = myPosition;
    while(i.hasNext()){
      RangeBearingHeightOffset offset = (RangeBearingHeightOffset)i.next();
      pos = pos.translatedBy(offset);
    }
    Length translateLength = myPosition.getRangeTo(pos);
    NavyAngle translateBearing = myPosition.getBearingTo(pos);
    return new RangeBearingHeightOffset(translateLength, translateBearing, Length.ZERO);
  }

  /**
   * Adds a negative vector to our list of vectors. This is primarily used for the no-go vector caclulation.
   * This method is pretty ugly, and should be renamed/retooled to be made more generic.
   * @param myPosition The position that the robot is at.
   * @param elementPosition The position of the grid cell that we want are using.
   * @param factor The factor we are using to create our vector.
   * @param minRange The range at which we will consider this a valid elment position. If the distance between myPosition and elementPosition is greater than this lentgh, this method will return a vector of length zero.
   */
  private void minusVector(AbsolutePosition myPosition,
			 NavyAngle myBearing,
			 AbsolutePosition elementPosition,
			 NumericalObject factor,
			 Length minRange,
			 double dropScore){

    //define the units that we will do all our math in
    Length.LengthUnit units = Length.METERS;

		//get the angle from robot to element
    NavyAngle myAngle = myPosition.getBearingTo(elementPosition);
	  double doubAngle = Accessor.getDoubleValue(myAngle.asAngle(), Angle.RADIANS);
		
		//warp points to frame where robot is oriented on x-axis (axis where angle is measured from)
		double myHeading = Accessor.getDoubleValue(myBearing.asAngle(), Angle.RADIANS);

		NavyAngle outAngle = NavyAngle.ZERO;

		//change to straight @ r=r_max, +/- 90 @ r=.5*r_max, and +/- 45 @r=0;
		double dropStPct = Config.getConfig().getPropertyAsDouble("dropStraightPercentage", 0.5);
		double dropRgPct = 1.0 - dropStPct;
		double newDropScore = 0;
		boolean changedScore = false;
		if(dropScore > dropStPct){
			newDropScore = (dropScore-dropStPct)/ dropRgPct;
		} else {
			changedScore = true;
			newDropScore = 0;
		}
		Angle scaledAngle = new Angle(0.5*Math.PI + newDropScore*0.25*Math.PI, Angle.RADIANS);
		
		if((doubAngle - myHeading) > 0){
			outAngle = myPosition.getBearingTo(elementPosition).minus(scaledAngle);
		} else {
			outAngle = myPosition.getBearingTo(elementPosition).plus(scaledAngle);
		}

		//change to straight ....
		if(changedScore){
			Angle straightAngleDiff = myBearing.minus(outAngle);
			outAngle = outAngle.plus(straightAngleDiff.times((dropStPct - dropScore)/dropStPct));
		}
		
		//use the actualFactor as the range in our offset
		Length factoredRange = new Length(1.0, units);
		RangeBearingHeightOffset offset = 
			new RangeBearingHeightOffset(factoredRange, outAngle, Length.ZERO);
		_vectorsRepulse.add(offset);

		if(changedScore){
			//Logger.getLogger("GLOBAL").info("\n" + dropScore + " REPULSING -- STRAIGHT\n");
		} else {
			//Logger.getLogger("GLOBAL").info("\n" + dropScore + " REPULSING -- normal\n");
		}
  }

	/**
	 * calculates the points of the drop shape
	 */
	private java.util.List calculateDropPoints(AbsolutePosition myPosition, NavyAngle myBearing){
		java.util.List dropPoints = new LinkedList();
		
		int numDropPts = (int)Config.getConfig().getPropertyAsDouble("numDropPts", 12);
		if(numDropPts % 2 == 1){
			numDropPts++;
		}
		
		if(Config.getConfig().getPropertyAsBoolean("debugVerbose", false)){
			searchBehaviorLogger("\n\n myBearing(rad): " + 
														Accessor.getDoubleValue(myBearing.asAngle(), Angle.RADIANS) +"\n" +
														"dropPoints (r, theta) (meters, rad): \n");
		}
		
		double dropRangeMin = Config.getConfig().getPropertyAsDouble("dropRangeMin", 40.0);	
		double dropRangeMax = Config.getConfig().getPropertyAsDouble("dropRangeMax", 300.0);	
		RangeBearingHeightOffset pointOffset;// = new RangeBearingHeightOffset(Length.ZERO, myBearing, Length.ZERO);
		for(int i=0;i<=numDropPts;i++){
			Length radius = new Length(dropRangeMin + (numDropPts/2 - Math.abs((double)(numDropPts/2 - i))) 
					* (dropRangeMax - dropRangeMin)/(numDropPts/2), Length.METERS);
			Angle angleDelta = new Angle((1.0*(i - numDropPts/2) / (numDropPts/2)) * (Math.PI / 2), Angle.RADIANS);
			pointOffset = new RangeBearingHeightOffset(radius, myBearing.plus(angleDelta), Length.ZERO);

			if(Config.getConfig().getPropertyAsBoolean("debugVerbose", false)){
				searchBehaviorLogger(Accessor.getDoubleValue(radius, Length.METERS) + " " + 
														 Accessor.getDoubleValue(myBearing.plus(angleDelta), Angle.RADIANS) + "\n");
			}

			dropPoints.add(myPosition.translatedBy(pointOffset));
		}
		return dropPoints;
	}

	/**
	 * finds the point closest to myPos along the line segment (e1,e2)
	 */
	private AbsolutePosition findClosestPos(
		AbsolutePosition myPos, AbsolutePosition e1, AbsolutePosition e2){

		AbsolutePosition closestPos = LatLonAltPosition.ORIGIN; 

		//v = vector from e1 --> e2
		Length vLen = e1.getRangeTo(e2);
		double vLength = Accessor.getDoubleValue(vLen, Length.METERS);
		NavyAngle vAng = e1.getBearingTo(e2);
		double vAngle = Accessor.getDoubleValue(vAng.asAngle(), Angle.RADIANS);
		double vX = vLength * Math.cos(vAngle);
		double vY = vLength * Math.sin(vAngle);
	
		//w = vector from e1 to myPos
		Length wLen = e1.getRangeTo(myPos);
		double wLength = Accessor.getDoubleValue(wLen, Length.METERS);
		NavyAngle wAng = e1.getBearingTo(myPos);
		double wAngle = Accessor.getDoubleValue(wAng.asAngle(), Angle.RADIANS);
		double wX = wLength * Math.cos(wAngle);
		double wY = wLength * Math.sin(wAngle);

		//c1 = dot(w,v), c2 = dot(v,v)
		double c1 = wX*vX + wY*vY;
		double c2 = vX*vX + vY*vY;

		if(c1 < 0){
			//pos is beyond e1
			closestPos = e1;
		}
		else if(c2 < c1){
			//pos is beyond e2
			closestPos = e2;
		} else {			
			//pos is between
			double cRange = (c1/c2) * vLength;
			Length cLength = new Length(cRange, Length.METERS);
			RangeBearingHeightOffset offset = 
				new RangeBearingHeightOffset(cLength, vAng, Length.ZERO);
			closestPos = e1.translatedBy(offset);
		}
		
		return closestPos;
	}

	private void searchBehaviorLogger(String s){
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter("intersectDebug.txt",true));
			out.write(s);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}


} // end class AvoidObstacleActionidObstacleAction
