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

// Created by Steven Marshall under S7102XXXSTI

package edu.jhuapl.nstd.swarm.display;

import java.util.logging.*;

import edu.jhuapl.nstd.swarm.display.event.*;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.util.*;

import javax.swing.*;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.Iterator;

import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;


import java.awt.geom.*;

/**
 * A Compass dial that shows the agents desired direction as an offset from
 * their current direction. North is used if these are the same (a simulated
 * agent.
 */
public class AgentDial extends JPanel implements AgentTrackerListener {
	private String _selectedAgent;
	
	/** Current size of the painted region */
	private Rectangle _currentSize;
	
	private NavyAngle _currentBearing;
	private NavyAngle _desiredBearing;
	private AbsolutePosition _currentPosition;
	private Speed _windSpeed;

	/** A result buffer for transforms */
	private Point2D.Double[] _result;

	/** The compass needle proper */
	private Point2D.Double[] _needle;

	/** The red needle adornment */
	private Point2D.Double[] _needleTri;

	/** The triangle pointing to either north or the current bearing, always at
	 * 90 degrees on a trig system. North is used when the current and desired
	 * are the same (a simulated agent w/o kinematics) */
	private Point2D.Double[] _north;

	/** The points defining the compass circle. */
	private Point2D.Double[] _circlePts;
	
	private boolean _needsRepaint = false;

	/** Rotate the needle into place */
	private AffineTransform _rotate;

	/** Scale to the window size */
	private AffineTransform _scale;

	/** Translate the (0, 0) origin to the center of the window */
	private AffineTransform _translateOrigin;

	/** Flip to a left handed coordinate system */
	private AffineTransform _flip;

	/** Translate negative y values to positive */
	private AffineTransform _translateYValues;

	/** All size based transforms together (rorate is computed every draw) */
	private AffineTransform _combinedTransform;

	/** The extreme dimension of the painted region, which is always square */
	private static final double REGION_SIZE = 100.0;

	private int _units = SearchCanvas.METERS;

	public AgentDial() {	
		super();

		_flip = new AffineTransform(1, 0, 0, -1, 0, 0);
		_combinedTransform = new AffineTransform();

		_result = new Point2D.Double[4];

		// the needle is in normal right hand coords, with a rotation point at
		// the origin. The region size is the extreme dimension of the painting
		// area
		_needle = new Point2D.Double[4];
		_needle[0] = new Point2D.Double(0.0, -15.0);
		_needle[1] = new Point2D.Double(10.0, 0.0);
		_needle[2] = new Point2D.Double(0.0, 100.0);
		_needle[3] = new Point2D.Double(-10.0, 0.0);

		_needleTri = new Point2D.Double[3];
		_needleTri[0] = new Point2D.Double(3.0, 45.0);
		_needleTri[1] = new Point2D.Double(0.0, 80.0);
		_needleTri[2] = new Point2D.Double(-3.0, 45.0);

		_north = new Point2D.Double[3];
		_north[0] = new Point2D.Double(0.0, 90.0);
		_north[1] = new Point2D.Double(5.0, 100.0);
		_north[2] = new Point2D.Double(-5.0, 100.0);

		_circlePts = new Point2D.Double[2];
		_circlePts[0] = new Point2D.Double(-REGION_SIZE, REGION_SIZE);
		_circlePts[1] = new Point2D.Double(REGION_SIZE, -REGION_SIZE);
	}

	public void trackedAgentUpdate(AgentTrackerEvent e) {
		if (e.getType() == AgentTrackerEvent.AGENT_SELECTED) {
			_selectedAgent = e.getAgentInfo().getName();
		}
	}

	public void setSelectedAgent(String name) {
		_selectedAgent = name;
	}


	public void updateBelief(METBelief metBelief, AgentPositionBelief positionBelief) {
		if (isVisible() == false)
			return;

		// Grab the new belief for the selected agent 
		synchronized (metBelief) {
			Iterator itr = metBelief.getAll().iterator();
			while(itr.hasNext()) {
				METTimeName met = (METTimeName)itr.next();
				String name = met.getName();
				if (name.equals(_selectedAgent)) {
					_currentBearing = met.getWindBearing();
					_desiredBearing = met.getWindBearing();
					_windSpeed = met.getWindSpeed();
					_needsRepaint = true;
					break;
				}
			}
		}

		updateBelief(positionBelief);

		if (_needsRepaint)
			repaint();
	}

	public void setUnits(int units) {
		_units = units;
	}

	/** Causes the new bearing for our selected agent to be drawn */
	public void updateBelief(AgentBearingBelief bearingBelief, AgentPositionBelief positionBelief) {
		if (isVisible() == false)
			return;

		// Grab the new belief for the selected agent 
		synchronized (bearingBelief) {
			Iterator itr = bearingBelief.getAll().iterator();
			while(itr.hasNext()) {
				BearingTimeName bearing = (BearingTimeName)itr.next();
				String name = bearing.getName();
				if (name.equals(_selectedAgent)) {
					_currentBearing = bearing.getCurrentBearing();
					_desiredBearing = bearing.getDesiredBearing();
					_needsRepaint = true;
					break;
				}
			}
		}

		updateBelief(positionBelief);

		if (_needsRepaint)
			repaint();
	}

	protected void updateBelief(AgentPositionBelief positionBelief) {
		synchronized (positionBelief) {
			Iterator itr = positionBelief.getAll().iterator();
			while(itr.hasNext()) {
				PositionTimeName position = (PositionTimeName)itr.next();
				String name = position.getName();
				if (name.equals(_selectedAgent)) {
					_currentPosition = position.getPosition();
					_needsRepaint = true;
					break;
				}
			}
		}
	}



	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;

		// only do this stuff when the size of the window changes
		Rectangle rect = getBounds(null);
		if (_currentSize == null || !_currentSize.equals(rect)) {
			_currentSize = rect;

			// our region is square, so use the smallest size for scaling
			int smallestSize = _currentSize.height;
			if (_currentSize.width < _currentSize.height) {
				smallestSize = _currentSize.width;
			} 

			// the center of the region in swing coordinates
			Point2D.Double center = new Point2D.Double(
				(double)_currentSize.x + (double)_currentSize.width / 2.0,
				(double)_currentSize.y + (double)_currentSize.height / 2.0);

			// scale of our geometry coordinates to swing scale
			double scale = ((double)smallestSize - 5.0) /
				(REGION_SIZE * 2.0);

			_scale = AffineTransform.getScaleInstance(scale, scale);
			_translateOrigin = AffineTransform.getTranslateInstance(
				center.x, center.y);
			_translateYValues = AffineTransform.getTranslateInstance(
				0, (double)_currentSize.height);

			_combinedTransform.setToIdentity();
			_combinedTransform.preConcatenate(_scale);
			_combinedTransform.preConcatenate(_translateOrigin);
			_combinedTransform.preConcatenate(_flip);
			_combinedTransform.preConcatenate(_translateYValues);
		}

		// fill the background gray
		g2.setColor(new Color(225,225,225));
		g2.fill(_currentSize);
		
		// turn on anti-aliasing
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		drawDial(g2);
		
		_needsRepaint = false;
	}


	private void drawDial(Graphics2D g2) {
		// create the circle
		_combinedTransform.transform(_circlePts, 0, _result, 0, _circlePts.length);
		Ellipse2D.Double circle = new Ellipse2D.Double(
			_result[0].x, _result[0].y, _result[1].x - _result[0].x, 
			_result[1].y - _result[0].y);

		// fill behind it
		g2.setColor(Color.WHITE);
		g2.fill(circle);

		// draw the north pointer
		Polygon poly = createPolygon(_north, _combinedTransform);
		g2.setColor(Color.BLUE);
		g2.fillPolygon(poly);
		
		// draw the circle on top
		Stroke defaultStroke = g2.getStroke();
		g2.setStroke(new BasicStroke(5));
		g2.setColor(Color.BLACK);
		g2.draw(circle);
		g2.setStroke(defaultStroke);
		
		// determine the bearing angle to draw
		if (_currentBearing == null)
			return;
			
		Angle desiredAngle;

		// find the difference to north if the desired and current are the same,
		// otherwise the current angle will be plotted as straight ahead, 
		// offsetting our desired from there.
		if (_currentBearing.equals(_desiredBearing)) {
			desiredAngle = 
				NavyAngle.NORTH.counterClockwiseAngleTo(_desiredBearing);
		} else {
			desiredAngle = 
				_currentBearing.counterClockwiseAngleTo(_desiredBearing);
		}

		double desired = Accessor.getDoubleValue(desiredAngle, Angle.RADIANS);

		// create the rotation transform
		AffineTransform rotate = AffineTransform.getRotateInstance(desired);

		rotate.preConcatenate(_combinedTransform);

		// draw the compass needle
		poly = createPolygon(_needle, rotate);
		g2.setColor(Color.BLACK);
		g2.fillPolygon(poly);
		poly = createPolygon(_needleTri, rotate);
		g2.setColor(new Color(180,0,0));
		g2.fillPolygon(poly);

		int printAngle = (int)(360.0 - (desired * (180.0 / Math.PI)));
		if (printAngle == 360) printAngle = 0;

		int smallestSize = _currentSize.height;
		if (_currentSize.width < _currentSize.height) {
			smallestSize = _currentSize.width;
		} 
		
		double scale = (double)smallestSize / 150.0;

		int yoffset = (int)(((double)(_currentSize.height - smallestSize) / 2.0) / scale);
		int xoffset = (int)(((double)(_currentSize.width - smallestSize) / 2.0) / scale);
		
		g2.setColor(Color.BLACK);
		g2.scale(scale, scale);
		g2.drawString(Integer.toString((int)printAngle) + "\u00b0", 
			xoffset + 2, yoffset + 10);

		if (_currentPosition != null) {
			Altitude alt = _currentPosition.asLatLonAltPosition().getAltitude();
			Altitude ground = DtedGlobalMap.getDted().getJlibAltitude(_currentPosition.asLatLonAltPosition());
			alt = alt.minus(ground.asLength());
			if (_units == SearchCanvas.METERS) {
				g2.drawString(Integer.toString(
						(int)(Accessor.getDoubleValue(
							alt, 
							Length.METERS))) + "m", 
					xoffset + 2, yoffset + 148);
			}
			else if (_units == SearchCanvas.FEET) {
				g2.drawString(Integer.toString(
						(int)(Accessor.getDoubleValue(
							alt,
							Length.FEET))) + "ft", 
					xoffset + 2, yoffset + 148);
			}
		}
		if (_windSpeed != null) {
			if (_units == SearchCanvas.METERS) {
				g2.drawString(Integer.toString(
						(int)(_windSpeed.getDoubleValue(Speed.METERS_PER_SECOND))) + "m/s", 
					xoffset + 120, yoffset + 148);
			}
			else if (_units == SearchCanvas.FEET) {
				Logger.getLogger("GLOBAL").info("chris trying to draw feet per second");
				g2.drawString(Integer.toString(
						(int)(_windSpeed.getDoubleValue(Speed.FEET_PER_SECOND))) + "f/s", 
					xoffset + 120, yoffset + 148);
			}
		}
	}

	/**
	 * Transform the points to swing coordinates and create a polygon out of them
	 */
	private Polygon createPolygon(Point2D.Double[] pts, AffineTransform transform) {
		transform.transform(pts, 0, _result, 0, pts.length);
		
		Polygon poly = new Polygon();

		for (int i = 0; i < pts.length; i++) {
			poly.addPoint((int)_result[i].x, (int)_result[i].y);
		}

		return poly;
	}
}
