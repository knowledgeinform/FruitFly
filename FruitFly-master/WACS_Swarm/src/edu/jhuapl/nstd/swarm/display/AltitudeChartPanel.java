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
package edu.jhuapl.nstd.swarm.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jfree.data.time.Millisecond;

import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.nstd.piccolo.Pic_Telemetry;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.WACSDisplayAgent;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.CloudPredictionBelief;
import edu.jhuapl.nstd.swarm.belief.CloudBelief;
import edu.jhuapl.nstd.swarm.belief.CloudDetection;
import edu.jhuapl.nstd.swarm.belief.CloudDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import java.awt.Component;
import javax.swing.BoxLayout;
import org.jfree.chart.plot.XYPlot;

public class AltitudeChartPanel extends DynamicTimeSeriesChartPanel implements Updateable, DisplayUnitsManager.DisplayUnitsChangeListener
{

    private BeliefManager _belMgr;
    private AgentPrediction _bubblePredictor;
    private boolean _bubbleShowing;
    private boolean _bubbleSettingShow;
    private int _units;
    private static final String[] AGENT_NAMES =
    {
        WACSAgent.AGENTNAME/*, WACSDisplayAgent.AGENTNAME*/
    };
    private JLabel displayAltValueLabel, wacsagentAltValueLabel;
    private static final int MAX_ALT_PANEL_HEIGHT = 115;
    private static final int MAX_ALT_PANEL_WIDTH = 1000;
    private static final String ALT_PREDICTION = "prediction";
    private static final String ALT_SIM = "simulation";
    private static final String PLANE_PRED = "plane_prediction";
    private static final String MIN = "_min";
    private static final String MAX = "_max";
    private static final DecimalFormat format1D = new DecimalFormat("#.#");
    private Length minAltRange;
    
    public AltitudeChartPanel(BeliefManager mgr)
    {
        super(AltitudeChartPanel.class.getSimpleName());

        _belMgr = mgr;
        
        _bubblePredictor = new AgentPrediction(_belMgr, WACSAgent.AGENTNAME);
        _bubbleShowing = false;
        _bubbleSettingShow = true;

        // Register this class for unit changes.
        DisplayUnitsManager.addChangeListener(this);
        
        setIncludeZero(false);
    }

    /**
     * Builds the main XY time series plot.
     */
    @Override
    protected void initChartPanel()
    {
        super.initChartPanel();
        
        minAltRange = new Length(15, Length.FEET);

        // Set the dimensions of the chart panel
        chartPanel.setPreferredSize(new Dimension(1000, MAX_ALT_PANEL_HEIGHT));

        createTimeSeries(WACSAgent.AGENTNAME);
        setSeriesColor(WACSAgent.AGENTNAME, Color.BLUE);
        
        // Add in shading series
        createTimeSeries(ALT_PREDICTION + MAX, ALT_PREDICTION, true);
        createTimeSeries(ALT_PREDICTION + MIN, ALT_PREDICTION, true);
        shadeCollection(ALT_PREDICTION, new Color(0f, 0f, 1.0f, 0.2f));

        createTimeSeries(ALT_SIM + MAX, ALT_SIM, false);
        createTimeSeries(ALT_SIM + MIN, ALT_SIM, false);
        shadeCollection(ALT_SIM, new Color(0.4f, 0f, 0f, 0.2f));
        
        createTimeSeries(PLANE_PRED + MAX, PLANE_PRED, true);
        createTimeSeries(PLANE_PRED + MIN, PLANE_PRED, true);
        shadeCollection(PLANE_PRED, new Color(1.0f, 0f, 0f, 0.4f));
        
        removeLegend();

//        chartPanel.setMinimumDrawWidth(0);
//        chartPanel.setMinimumDrawHeight(0);
//        chartPanel.setMaximumDrawWidth(MAX_ALT_PANEL_WIDTH);
//        chartPanel.setMaximumDrawHeight(MAX_ALT_PANEL_HEIGHT);

        // Create a separate altitude text display panel
        this.add(Box.createVerticalStrut(10));
        this.add(getAltitudeTextPanel());
        this.add(Box.createVerticalStrut(10));
    }

    /**
     * Builds the text JPanel that displays the textual altitude data.
     *
     * @return
     */
    private JPanel getAltitudeTextPanel()
    {
        JPanel txtPanel = new JPanel();
        txtPanel.setBackground(Color.WHITE);
        txtPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtPanel.setAlignmentX(CENTER_ALIGNMENT);
        
        _units = -1;
        unitsChanged();
        JLabel displayAltLabel = new JLabel();
        JLabel wacsagentAltLabel = new JLabel();
        displayAltValueLabel = new JLabel();
        wacsagentAltValueLabel = new JLabel();

//        displayAltLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        displayAltLabel.setText(WACSDisplayAgent.AGENTNAME + ":");

//        wacsagentAltLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        wacsagentAltLabel.setText(WACSAgent.AGENTNAME + ":");
        
        Dimension valueLabelDim = new Dimension(20, 23);

        displayAltValueLabel.setBackground(Color.WHITE);
        displayAltValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        displayAltValueLabel.setText("N/A");
        displayAltValueLabel.setFont(new Font(displayAltValueLabel.getFont().getName(), Font.BOLD, displayAltValueLabel.getFont().getSize()));
//        displayAltValueLabel.setMinimumSize(valueLabelDim);
//        displayAltValueLabel.setPreferredSize(valueLabelDim);
//        displayAltValueLabel.setMaximumSize(valueLabelDim);

        wacsagentAltValueLabel.setBackground(Color.WHITE);
        wacsagentAltValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        wacsagentAltValueLabel.setText("N/A");
        wacsagentAltValueLabel.setFont(new Font(wacsagentAltValueLabel.getFont().getName(), Font.BOLD, wacsagentAltValueLabel.getFont().getSize()));
//        wacsagentAltValueLabel.setMinimumSize(valueLabelDim);
//        wacsagentAltValueLabel.setPreferredSize(valueLabelDim);
//        wacsagentAltValueLabel.setMaximumSize(valueLabelDim);

        txtPanel.setLayout(new BoxLayout(txtPanel, BoxLayout.X_AXIS));
        txtPanel.add(Box.createRigidArea(new Dimension(5, 0)));
//        txtPanel.add(altTextPanelTitle);
//        txtPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        txtPanel.add(wacsagentAltLabel);
        txtPanel.add(Box.createRigidArea(new Dimension(3, 0)));
        txtPanel.add(wacsagentAltValueLabel);
        txtPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        txtPanel.add(displayAltLabel);
        txtPanel.add(Box.createRigidArea(new Dimension(3, 0)));
        txtPanel.add(displayAltValueLabel);
        txtPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        
        updateDataDisplayWidth();

        return txtPanel;
    }
    
    private void updateDataDisplayWidth()
    {
        Component panel = wacsagentAltValueLabel.getParent();
        int width = 0;
        
        for (Component cmp : wacsagentAltValueLabel.getParent().getComponents())
        {
            width += cmp.getPreferredSize().getWidth();
        }
        
        Dimension size = new Dimension(width, 26);
        panel.setMinimumSize(size);
        panel.setPreferredSize(size);
        panel.setMaximumSize(size);
    }

    public void setUnits(int units)
    {
        if (_units != units)
        {
            _units = units;
            if (_units == SearchCanvas.FEET)
            {
                multiplyPoints(3.28084);
                setMinimumRange(minAltRange.getDoubleValue(Length.FEET));
                getChart().getXYPlot().getRangeAxis().setLabel("MSL Altitude (ft)");
            }
            else if (_units == SearchCanvas.METERS)
            {
                multiplyPoints(1.0/3.28084);
                setMinimumRange(minAltRange.getDoubleValue(Length.METERS));
                getChart().getXYPlot().getRangeAxis().setLabel("MSL Altitude (m)");
            }
        }
    }

    @Override
    public void unitsChanged()
    {
        if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
        {
            setUnits(SearchCanvas.FEET);
        }
        else
        {
            setUnits(SearchCanvas.METERS);
        }
    }
    
    public void showAltitudeBubble(Boolean show)
    {
        _bubbleSettingShow = show;
    }

    @Override
    protected void updateData()
    {
        try
        {
            updateChart();
        }
        catch (Exception e)
        {
            System.err.println("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }

    private void updateChart()
    {
        synchronized (this)
        {
            AgentPositionBelief posBlf = (AgentPositionBelief) _belMgr.get(AgentPositionBelief.BELIEF_NAME);
            
            // TEST CODE
//            double testAlt = 200 + Math.sin((double) System.currentTimeMillis() / 5000.0)*2 + Math.random()*0.5 + Math.sin((double) System.currentTimeMillis() / 30000.0)*5;
//            posBlf = new AgentPositionBelief(WACSAgent.AGENTNAME, new LatLonAltPosition(new Latitude(0, Angle.DEGREES), new Longitude(0, Angle.DEGREES), new Altitude(testAlt, Length.FEET)));
            // END TEST CODE

            for (String agentName : AGENT_NAMES)
            {
                if (posBlf != null)
                {
                    PositionTimeName ptn = posBlf.getPositionTimeName(agentName);
                    if (ptn != null)
                    {
                        LatLonAltPosition lla = ptn.getPosition().asLatLonAltPosition();
                        final double alt;
                        if (_units == SearchCanvas.FEET)
                            alt = lla.getAltitude().getDoubleValue(Length.FEET);
                        else
                            alt = lla.getAltitude().getDoubleValue(Length.METERS);

                        long now = System.currentTimeMillis();
                        long time = ptn.getTime().getTime();

                        // Displays the altitude based on the selected units
                        String mslText = "";
                        if (_units == SearchCanvas.FEET)
                        {
                            mslText = (format1D.format(alt) + " ft");
                        }
                        else if (_units == SearchCanvas.METERS)
                        {
                            mslText = (format1D.format(alt) + " m");
                        }

                        // Display the altitude in the text fields and updates plane icon
                        if (agentName.equals(WACSAgent.AGENTNAME))
                        {
                            Path2D.Double tri = new Path2D.Double();
                            double ratio = 35.0/50.0;
                            double size = MARKER_SIZE / 1.5;
                            double[][] pts = {{-size, -size*ratio}, {-size, size*ratio}, {size, 0}};
                            
                            // Rotate triangle with pitch
                            PiccoloTelemetryBelief picBlf = (PiccoloTelemetryBelief) _belMgr.get(PiccoloTelemetryBelief.BELIEF_NAME);
                            if (picBlf != null)
                            {
                                Pic_Telemetry telem = picBlf.getPiccoloTelemetry();
                                if (telem != null)
                                {
                                    // Rotation matrix:
                                    for (double[] pt : pts)
                                    {
                                        double x = pt[0], y = pt[1];
                                        double angle = -telem.Pitch;
                                        pt[0] = x * Math.cos(angle) - y * Math.sin(angle);
                                        pt[1] = x * Math.sin(angle) + y * Math.cos(angle);
                                    }
                                }
                            }
                            
                            tri.moveTo(pts[0][0], pts[0][1]);
                            for (int i = 0; i < pts.length; i++)
                                tri.lineTo(pts[i][0], pts[i][1]);
                            tri.closePath();
                            
                            // Updates header shape to the correct rotation
                            setHeader(WACSAgent.AGENTNAME, tri);
                            
                            wacsagentAltValueLabel.setText(mslText);
                            updateDataDisplayWidth();
                        }

                        // Add the time series data to the plot
                        addDataPoint(agentName, new Millisecond(new Date(time)), alt);
                    }
                }

                /*
            
                 //
                 //
                 //
                 double groundAlt = (new Altitude(DtedGlobalMap.getDted().getAltitudeMSL(
                 lla.getLatitude().getDoubleValue(Angle.DEGREES),
                 lla.getLongitude().getDoubleValue(Angle.DEGREES)), Length.METERS)).getDoubleValue(Length.FEET);
            
                 //
                 // Draw lines representing simulated cloud altitude
                 //
                 CloudBelief cloudBelief = (CloudBelief) _belMgr.get(CloudBelief.BELIEF_NAME);
                 if (cloudBelief != null)
                 {
            
                 Altitude cloudAlt = (new Altitude(DtedGlobalMap.getDted().getAltitudeMSL(
                 cloudBelief.getEllipse().getCenter().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES),
                 cloudBelief.getEllipse().getCenter().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES)), Length.METERS));
            
                 double cloudBottomAltitude_ft = cloudBelief.getBottomAltitudeAGL().plus(cloudAlt.asLength()).getDoubleValue(Length.FEET);
                 double cloudTopAltitude_ft = cloudBottomAltitude_ft + cloudBelief.getHeight().getDoubleValue(Length.FEET);
                 }
            
                 //
                 // Display predicted cloud altitude
                 //
                 CloudPredictionBelief cloudPredictionBelief = (CloudPredictionBelief)_belMgr.get(CloudPredictionBelief.BELIEF_NAME);
                 if (cloudPredictionBelief != null &&
                 cloudPredictionBelief.getCloudMaxAltitudeMSL() != null)
                 {
            
                 Altitude cloudBottomAltitude = cloudPredictionBelief.getCloudMinAltitudeMSL();
                 Altitude cloudTopAltitude = cloudPredictionBelief.getCloudMaxAltitudeMSL();
                 }
                 */
            }
            
                if (posBlf != null)
                {
                    PositionTimeName ptn = posBlf.getPositionTimeName(WACSDisplayAgent.AGENTNAME);
                    if (ptn != null)
                    {
                        LatLonAltPosition lla = ptn.getPosition().asLatLonAltPosition();
                        final double alt;
                        if (_units == SearchCanvas.FEET)
                            alt = lla.getAltitude().getDoubleValue(Length.FEET);
                        else
                            alt = lla.getAltitude().getDoubleValue(Length.METERS);

                        long now = System.currentTimeMillis();
                        long time = ptn.getTime().getTime();

                        // Displays the altitude based on the selected units
                        String mslText = "";
                        if (_units == SearchCanvas.FEET)
                        {
                            mslText = (format1D.format(alt) + " ft");
                        }
                        else if (_units == SearchCanvas.METERS)
                        {
                            mslText = (format1D.format(alt) + " m");
                        }
                        
                        displayAltValueLabel.setText(mslText);
                        updateDataDisplayWidth();
                    }
                }

            
            CloudPredictionBelief predBlf = (CloudPredictionBelief) _belMgr.get(CloudPredictionBelief.BELIEF_NAME);
            if (predBlf != null)
            {
                Altitude minAlt = predBlf.getCloudMinAltitudeMSL();
                Altitude maxAlt = predBlf.getCloudMaxAltitudeMSL();
                if (minAlt != null && maxAlt != null)
                {
                    double minVal = 0;
                    double maxVal = 0;
                    
                    if (_units == SearchCanvas.FEET)
                    {
                        minVal = minAlt.getDoubleValue(Length.FEET);
                        maxVal = maxAlt.getDoubleValue(Length.FEET);
                    }
                    else if (_units == SearchCanvas.METERS)
                    {
                        minVal = minAlt.getDoubleValue(Length.METERS);
                        maxVal = maxAlt.getDoubleValue(Length.METERS);
                    }

                    addDataPoint(ALT_PREDICTION + MIN, ALT_PREDICTION, new Millisecond(new Date(predBlf.getTimeStamp().getTime())), minVal);
                    addDataPoint(ALT_PREDICTION + MAX, ALT_PREDICTION, new Millisecond(new Date(predBlf.getTimeStamp().getTime())), maxVal);
                }
            }

            CloudBelief simBlf = (CloudBelief) _belMgr.get(CloudBelief.BELIEF_NAME);
            if (simBlf != null)
            {
                // !! Altitude is based off of ground alt at the WACSAgent
                // !! in hard code because there is no better solution.
                try
                {
                    Altitude groundAltBelowAgent = getGroundLevelBelow(WACSAgent.AGENTNAME);
                    Altitude minAlt = simBlf.getBottomAltitudeAGL().plus(groundAltBelowAgent.asLength());
                    if (minAlt != null)
                    {
                        Altitude maxAlt = minAlt.plus(simBlf.getHeight());
                        if (maxAlt != null)
                        {
                            double minVal = 0;
                            double maxVal = 0;

                            if (_units == SearchCanvas.FEET)
                            {
                                minVal = minAlt.getDoubleValue(Length.FEET);
                                maxVal = maxAlt.getDoubleValue(Length.FEET);
                            }
                            else if (_units == SearchCanvas.METERS)
                            {
                                minVal = minAlt.getDoubleValue(Length.METERS);
                                maxVal = maxAlt.getDoubleValue(Length.METERS);
                            }

                            addDataPoint(ALT_SIM + MIN, ALT_SIM, new Millisecond(new Date(simBlf.getTimeStamp().getTime())), minVal);
                            addDataPoint(ALT_SIM + MAX, ALT_SIM, new Millisecond(new Date(simBlf.getTimeStamp().getTime())), maxVal);
                        }
                    }
                }
                catch (Exception e)
                {
                    // Agent not loaded
                }
            }

            CloudDetectionBelief detectionBelief = (CloudDetectionBelief) _belMgr.get(CloudDetectionBelief.BELIEF_NAME);
            if (detectionBelief != null)
            {
                synchronized (detectionBelief.getLock())
                {
                    for (CloudDetection detect : detectionBelief.getDetections())
                    {
                        Color c;
                        if (detect.getSource() == CloudDetection.SOURCE_PARTICLE)
                        {
                            // Source is a particle
                            c = new Color(0.0f, 0.0f, 1.0f, 0.6f);
                        }
                        else
                        {
                            // Source is chemical
                            c = new Color(1.0f, 0.0f, 0.0f, 0.6f);
                        }

                        long detTime = detect.getTime();
                        float detAlt = detect.getAlt_m();
                        if (_units == SearchCanvas.FEET)
                            detAlt = detAlt * 3.28084f;
                        
                        addPOI(new Millisecond(new Date(detTime)), detAlt, c);
                    }
                }
            }
            
            if(_bubbleSettingShow && _bubblePredictor != null && _bubblePredictor.shouldShowBubble())
            {
                if (!_bubbleShowing)
                {
                    PositionTimeName ptn = posBlf.getPositionTimeName(WACSAgent.AGENTNAME);
                    long last = ptn.getTime().getTime();

                    double val = 0;
                    
                    if (_units == SearchCanvas.FEET)
                    {
                        val = ptn.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.FEET);
                    }
                    else if (_units == SearchCanvas.METERS)
                    {
                        val = ptn.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);
                    }

                    addDataPoint(PLANE_PRED + MIN, PLANE_PRED, new Millisecond(ptn.getTime()), val);
                    addDataPoint(PLANE_PRED + MAX, PLANE_PRED, new Millisecond(ptn.getTime()), val);
                }
                _bubbleShowing = true;
                
                Altitude minAlt = _bubblePredictor.getMinAltitude();
                Altitude maxAlt = _bubblePredictor.getMaxAltitude();

                double minVal = 0;
                double maxVal = 0;

                if (_units == SearchCanvas.FEET)
                {
                    minVal = minAlt.getDoubleValue(Length.FEET);
                    maxVal = maxAlt.getDoubleValue(Length.FEET);
                }
                else if (_units == SearchCanvas.METERS)
                {
                    minVal = minAlt.getDoubleValue(Length.METERS);
                    maxVal = maxAlt.getDoubleValue(Length.METERS);
                }

                addDataPoint(PLANE_PRED + MIN, PLANE_PRED, new Millisecond(new Date()), minVal);
                addDataPoint(PLANE_PRED + MAX, PLANE_PRED, new Millisecond(new Date()), maxVal);
            }
            else if (_bubbleShowing)
            {
                clearCollection(PLANE_PRED);
                    
                _bubbleShowing = false;
            }
        }
    }

    /**
     * Returns the ground level at the position of any agent.
     *
     * @param agentId The agent at whose position ground level will be
     * evaluated.
     * @return The ground level as an Altitude object
     */
    private Altitude getGroundLevelBelow(String agentId)
    {
        AgentPositionBelief b = (AgentPositionBelief) _belMgr.get(AgentPositionBelief.BELIEF_NAME);
        LatLonAltPosition position;
        if (b != null)
        {
            position = b.getPositionTimeName(agentId).getPosition().asLatLonAltPosition();
            return DtedGlobalMap.getDted().getJlibAltitude(position.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES), position.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES));
        }
        else
        {
            return null;
        }
    }
}
