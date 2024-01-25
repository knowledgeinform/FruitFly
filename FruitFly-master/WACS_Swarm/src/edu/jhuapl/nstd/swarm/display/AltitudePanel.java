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

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.ClassificationBelief;
import edu.jhuapl.nstd.swarm.belief.CloudBelief;
import edu.jhuapl.nstd.swarm.belief.CloudPredictionBelief;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.swarm.util.Config;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JPanel;

public class AltitudePanel extends JPanel
        implements Updateable, DisplayUnitsManager.DisplayUnitsChangeListener
{

    private BeliefManager _belMgr;
    private AgentTracker _agentTracker;
    // TODO change me
    private int _timeWindow; // ms
    private int _nameAreaWidth; // px
    private int _units = SearchCanvas.METERS;

    public AltitudePanel(BeliefManager mgr, AgentTracker tracker)
    {
        _belMgr = mgr;
        _agentTracker = tracker;

        _timeWindow = Config.getConfig().getPropertyAsInteger ("AltitudePanel.TimeWindowSpan.Ms", 60000); // ms
        _nameAreaWidth = Config.getConfig().getPropertyAsInteger ("AltitudePanel.NameAreaWidth.Pixels", 120); // px


        DisplayUnitsManager.addChangeListener(this);
    }

    public void setUnits(int units)
    {
        //Logger.getLogger("GLOBAL").info("set ap to " + units);
        _units = units;
    }

    @Override
    public void unitsChanged()
    {
        if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
            setUnits (SearchCanvas.FEET);
        else //if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
            setUnits (SearchCanvas.METERS);

    }

    @Override
    public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;

        Dimension dim = getSize(null);
        if (dim.width <= 0)
        {
            return;
        }

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, dim.width, dim.height);
        g2.setColor(Color.GRAY);
        g2.drawLine(dim.width - _nameAreaWidth, 0, dim.width - _nameAreaWidth, dim.height);
        g2.drawLine(0, dim.height - 11, dim.width - _nameAreaWidth, dim.height - 11);
        g2.setColor(Color.BLACK);
        g2.drawString("" + (_timeWindow / 1000) + "s ago", 0, dim.height);
        g2.drawString("now", dim.width - _nameAreaWidth - 25, dim.height);

        if (_agentTracker.getAgentCount() <= 0)
        {
            return;
        }

        long now = System.currentTimeMillis();
        if (_agentTracker.getMinimumAltitude() == null || _agentTracker.getMaximumAltitude() == null)
        {
            return;
        }

        double minAlt_ft = _agentTracker.getMinimumAltitude().getDoubleValue(Length.FEET);
        double maxAlt_ft = _agentTracker.getMaximumAltitude().getDoubleValue(Length.FEET);
        

        CloudBelief cloudBelief = (CloudBelief) _belMgr.get(CloudBelief.BELIEF_NAME);
        if (cloudBelief != null)
        {

            Altitude groundAlt = (new Altitude(DtedGlobalMap.getDted().getAltitudeMSL(
                    cloudBelief.getEllipse().getCenter().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES),
                    cloudBelief.getEllipse().getCenter().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES)), Length.METERS));

            double cloudBottomAltitude_ft = cloudBelief.getBottomAltitudeAGL().plus(groundAlt.asLength()).getDoubleValue(Length.FEET);
            double cloudTopAltitude_ft = cloudBottomAltitude_ft + cloudBelief.getHeight().getDoubleValue(Length.FEET);

            if (cloudBottomAltitude_ft < minAlt_ft)
            {
                minAlt_ft = cloudBottomAltitude_ft;
            }

            if (cloudTopAltitude_ft > maxAlt_ft)
            {
                maxAlt_ft = cloudTopAltitude_ft;
            }
        }


        ClassificationBelief cBelief = (ClassificationBelief) _belMgr.get(ClassificationBelief.BELIEF_NAME);

        synchronized (this)
        {
            Iterator agentItr = _agentTracker.getAllAgents();
            int lastX = 0;
            int lastY = 0;
            int lastGroundY = 0;
            double lastGroundAlt = 0;
            double alt = 0.0;
            int x = 0, y = 0;

            String selectedAgent = _agentTracker.getSelectedAgent();
            AgentTracker.TimeComparator comparator = new AgentTracker.TimeComparator();
            if (selectedAgent != null)
            {
                Collection<PositionTimeName> positions = _agentTracker.getPositionHistory(
                        selectedAgent);

                for (PositionTimeName ptn : positions)
                {
                    LatLonAltPosition lla = ptn.getPosition().asLatLonAltPosition();
                    double groundAlt = (new Altitude(DtedGlobalMap.getDted().getAltitudeMSL(
                            lla.getLatitude().getDoubleValue(Angle.DEGREES),
                            lla.getLongitude().getDoubleValue(Angle.DEGREES)), Length.METERS)).getDoubleValue(Length.FEET);

                    // below ground!!!
                    if (groundAlt > maxAlt_ft)
                    {
                        maxAlt_ft = groundAlt;
                    }
                    if (groundAlt < minAlt_ft)
                    {
                        minAlt_ft = groundAlt;
                    }
                }
            }

            // convert from px to units
            double minPad = (((double) 13 / dim.height)) * (maxAlt_ft - minAlt_ft);
            double maxPad = (((double) 5 / dim.height)) * (maxAlt_ft - minAlt_ft);
            minAlt_ft -= minPad;
            maxAlt_ft += maxPad;



            int altitudeBarsXPos = dim.width - _nameAreaWidth - 30;


            //
            // Draw lines representing simulated cloud altitude
            //
            if (cloudBelief != null)
            {

                Altitude groundAlt = (new Altitude(DtedGlobalMap.getDted().getAltitudeMSL(
                        cloudBelief.getEllipse().getCenter().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES),
                        cloudBelief.getEllipse().getCenter().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES)), Length.METERS));

                Altitude cloudBottomAltitude = cloudBelief.getBottomAltitudeAGL().plus(groundAlt.asLength());
                Altitude cloudTopAltitude = cloudBottomAltitude.plus(cloudBelief.getHeight());

                int bottomY = getYpix (dim.height, cloudBottomAltitude.getDoubleValue(Length.FEET), minAlt_ft, maxAlt_ft);
                int topY = getYpix (dim.height, cloudTopAltitude.getDoubleValue(Length.FEET), minAlt_ft, maxAlt_ft);
                //int bottomY = dim.height - (int) ((cloudBottomAltitude.getDoubleValue(Length.FEET) - minAlt_ft) / (maxAlt_ft - minAlt_ft) * dim.height);
                //int topY = dim.height - (int) ((cloudTopAltitude.getDoubleValue(Length.FEET) - minAlt_ft) / (maxAlt_ft - minAlt_ft) * dim.height);

                if (bottomY > 0 || topY > 0)
                {
                    if (bottomY < 0)
                    {
                        bottomY = 0;
                    }

                    if (topY < 0)
                    {
                        topY = 0;
                    }

                    g2.setColor(new Color(Color.PINK.getRed(), Color.PINK.getGreen(), Color.PINK.getBlue(), 128));
                    g2.fillRect(altitudeBarsXPos, topY, 25, bottomY - topY);
                }

                g2.setColor(Color.PINK);
                if (_units == SearchCanvas.FEET)
                {
                    g2.drawString("plume bottom " + Integer.toString((int)cloudBottomAltitude.getDoubleValue(Length.FEET)) + "ft", dim.width - _nameAreaWidth + 5, bottomY);
                    g2.drawString("plume top " + Integer.toString((int)cloudTopAltitude.getDoubleValue(Length.FEET)) + "ft", dim.width - _nameAreaWidth + 5, topY);
                }
                else if (_units == SearchCanvas.METERS)
                {
                    g2.drawString("plume bottom " + Integer.toString((int)cloudBottomAltitude.getDoubleValue(Length.METERS)) + "m", dim.width - _nameAreaWidth + 5, bottomY);
                    g2.drawString("plume top " + Integer.toString((int)cloudTopAltitude.getDoubleValue(Length.METERS)) + "m", dim.width - _nameAreaWidth + 5, topY);
                }
            }


            //
            // Draw lines representing predicted cloud altitude
            //
            CloudPredictionBelief cloudPredictionBelief = (CloudPredictionBelief)_belMgr.get(CloudPredictionBelief.BELIEF_NAME);
            if (cloudPredictionBelief != null &&
                cloudPredictionBelief.getCloudMaxAltitudeMSL() != null)
            {

                Altitude cloudBottomAltitude = cloudPredictionBelief.getCloudMinAltitudeMSL();
                Altitude cloudTopAltitude = cloudPredictionBelief.getCloudMaxAltitudeMSL();

                int bottomY = getYpix (dim.height, cloudBottomAltitude.getDoubleValue(Length.FEET), minAlt_ft, maxAlt_ft);
                int topY = getYpix (dim.height, cloudTopAltitude.getDoubleValue(Length.FEET), minAlt_ft, maxAlt_ft);
                //int bottomY = dim.height - (int) ((cloudBottomAltitude.getDoubleValue(Length.FEET) - minAlt_ft) / (maxAlt_ft - minAlt_ft) * dim.height);
                //int topY = dim.height - (int) ((cloudTopAltitude.getDoubleValue(Length.FEET) - minAlt_ft) / (maxAlt_ft - minAlt_ft) * dim.height);
                   
                g2.setColor(new Color(Color.ORANGE.getRed(), Color.ORANGE.getGreen(), Color.ORANGE.getBlue(), 128));
                if (topY < 0)
                    g2.fillRect(altitudeBarsXPos, 1, 25, bottomY - 1);
                else
                    g2.fillRect(altitudeBarsXPos, topY, 25, bottomY - topY);

                g2.setColor(Color.ORANGE);
                if (_units == SearchCanvas.FEET)
                {
                    g2.drawString("predicted bottom " + Integer.toString((int)cloudBottomAltitude.getDoubleValue(Length.FEET)) + "ft", dim.width - _nameAreaWidth + 5, bottomY);
                    g2.drawString("predicted top " + Integer.toString((int)cloudTopAltitude.getDoubleValue(Length.FEET)) + "ft", dim.width - _nameAreaWidth + 5, topY);
                }
                else if (_units == SearchCanvas.METERS)
                {
                    g2.drawString("predicted bottom " + Integer.toString((int)cloudBottomAltitude.getDoubleValue(Length.METERS)) + "m", dim.width - _nameAreaWidth + 5, bottomY);
                    g2.drawString("predicted top " + Integer.toString((int)cloudTopAltitude.getDoubleValue(Length.METERS)) + "m", dim.width - _nameAreaWidth + 5, topY);
                }


                if (cloudPredictionBelief.getCloudPredictedCenterPositionMSL() != null)
                {
                    Altitude matrixBottomAltitude = cloudPredictionBelief.getCloudMinAltitudeMSL();
                    Altitude matrixTopAltitude = cloudPredictionBelief.getCloudMaxAltitudeMSL();

                    bottomY = getYpix (dim.height, matrixBottomAltitude.getDoubleValue(Length.FEET), minAlt_ft, maxAlt_ft);
                    topY = getYpix (dim.height, matrixTopAltitude.getDoubleValue(Length.FEET), minAlt_ft, maxAlt_ft);
                    //bottomY = dim.height - (int) ((matrixBottomAltitude.getDoubleValue(Length.FEET) - minAlt_ft) / (maxAlt_ft - minAlt_ft) * dim.height);
                    //topY = dim.height - (int) ((matrixTopAltitude.getDoubleValue(Length.FEET) - minAlt_ft) / (maxAlt_ft - minAlt_ft) * dim.height);

                    g2.setColor(new Color(Color.GRAY.getRed(), Color.GRAY.getGreen(), Color.GRAY.getBlue(), 128));
                    g2.drawRect(altitudeBarsXPos, topY, 25, bottomY - topY);

                    g2.setColor(Color.GRAY);
                    if (_units == SearchCanvas.FEET)
                    {
                        g2.drawString("matrix bottom " + Integer.toString((int)matrixBottomAltitude.getDoubleValue(Length.FEET)) + "ft", dim.width - _nameAreaWidth + 5, bottomY);
                        g2.drawString("matrix top " + Integer.toString((int)matrixTopAltitude.getDoubleValue(Length.FEET)) + "ft", dim.width - _nameAreaWidth + 5, topY);
                    }
                    else if (_units == SearchCanvas.METERS)
                    {
                        g2.drawString("matrix bottom " + Integer.toString((int)matrixBottomAltitude.getDoubleValue(Length.METERS)) + "m", dim.width - _nameAreaWidth + 5, bottomY);
                        g2.drawString("matrix top " + Integer.toString((int)matrixTopAltitude.getDoubleValue(Length.METERS)) + "m", dim.width - _nameAreaWidth + 5, topY);
                    }
                }
            }


            PositionTimeName ptn = null;
            while (agentItr.hasNext())
            {
                String agentName = (String) agentItr.next();
                boolean selected = agentName.equals(selectedAgent);

                Collection<PositionTimeName> positions =
                                             _agentTracker.getPositionHistory(agentName);
                Iterator<PositionTimeName> posItr = positions.iterator();

                boolean first = true;

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                while (posItr.hasNext())
                {
                    ptn = (PositionTimeName) posItr.next();
                    LatLonAltPosition lla = ptn.getPosition().asLatLonAltPosition();

                    alt = lla.getAltitude().getDoubleValue(Length.FEET);
                    long time = ptn.getTime().getTime();
                    double groundAlt = 0.0;
                    int groundY = 0;

                    // skip positions outside the graph. This is safe, because they're sorted
                    if (now - time > _timeWindow)
                    {
                        continue;
                    }

//                    if (selected)
//                    {
//                        groundAlt = (new Altitude(DtedGlobalMap.getDted().getAltitudeMSL(
//                                lla.getLatitude().getDoubleValue(Angle.DEGREES),
//                                lla.getLongitude().getDoubleValue(Angle.DEGREES)), Length.METERS)).getDoubleValue(Length.FEET);
//                        groundY = (dim.height - 20) - (int) ((groundAlt - minAlt_ft) / (maxAlt_ft - minAlt_ft) * (dim.height - 20) - 10);
//                    }

                    y = getYpix (dim.height, alt, minAlt_ft, maxAlt_ft);
                    x = getXpix (dim.width, now, time);

                    if (!first)
                    {
                        Color color = Color.BLUE;
                        g2.setColor(color);
                        g2.drawLine(lastX, lastY, x, y);

//                        if (selected)
//                        {
//                            g2.setColor(new Color(128, 64, 0));
//                            g2.drawLine(lastX, lastGroundY, x, groundY);
//
//                        }
                    }

                    lastX = x;
                    lastY = y;
                    lastGroundY = groundY;
                    lastGroundAlt = groundAlt;
                    first = false;
                }

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                if (ptn != null)
                {
                    g2.setColor(Color.BLACK);
                    if (_units == SearchCanvas.FEET)
                    {
                        g2.drawString(ptn.getName() + " " + (int) alt + "ft", dim.width - _nameAreaWidth + 5, y);
                    }
                    else if (_units == SearchCanvas.METERS)
                    {
                        g2.drawString(ptn.getName() + " " + (int) new Altitude(alt, Length.FEET).getDoubleValue(Length.METERS) + "m", dim.width - _nameAreaWidth + 5, y);
                    }
                }
                if (lastGroundAlt > 0)
                {
                    g2.setColor(new Color(128, 64, 0));
                    if (_units == SearchCanvas.FEET)
                    {
                        g2.drawString("Ground: " + (int) lastGroundAlt + "ft", dim.width - _nameAreaWidth + 5, lastGroundY);
                    }
                    else if (_units == SearchCanvas.METERS)
                    {
                        g2.drawString("Ground: " + (int) new Altitude(lastGroundAlt, Length.FEET).getDoubleValue(Length.METERS) + "m", dim.width - _nameAreaWidth + 5, lastGroundY);
                    }
                }
            }                

        }
    }

    private int getYpix(int dimHeight, double alt_ft, double minAlt_ft, double maxAlt_ft)
    {
        return (dimHeight - 20) - (int) ((alt_ft - minAlt_ft) / (maxAlt_ft - minAlt_ft) * (dimHeight - 20) - 10);
    }

    private int getXpix (int dimWidth, long nowMs, long timeMs)
    {
        return (dimWidth - _nameAreaWidth) - (int) (((double) (nowMs - timeMs) / _timeWindow) * (dimWidth - _nameAreaWidth));
    }

    @Override
    public synchronized void update()
    {
        try
        {
            repaint();
        }
        catch (Exception e)
        {
            System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }


}
