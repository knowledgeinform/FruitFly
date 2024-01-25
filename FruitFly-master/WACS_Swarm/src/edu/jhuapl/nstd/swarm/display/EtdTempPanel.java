/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.CloudDetection;
import edu.jhuapl.nstd.swarm.belief.CloudDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetection;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionListBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionMessageBelief;
import edu.jhuapl.nstd.swarm.belief.EtdStatusMessageBelief;
import edu.jhuapl.nstd.swarm.belief.NmeaRawMessageBelief;
import edu.jhuapl.nstd.swarm.util.Config;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author kayjl1
 */
public class EtdTempPanel extends DynamicTimeSeriesChartPanel implements Updateable {
    private static final int MAX_ETD_PANEL_HEIGHT = 115;
    private static final int MAX_ETD_PANEL_WIDTH = 1750;
    
    private BeliefManager _belMgr;
    JLabel _etdStatusMessage;
    JScrollPane _etdScrollPane;    
    private long _latestUpdateTime;
    
    public EtdTempPanel(BeliefManager belMgr) {
        super(EtdTempPanel.class.getSimpleName());
        
        _belMgr = belMgr;
          
        setIncludeZero(false);
        
        _latestUpdateTime = 0L;
    }
    
    @Override
    protected void initChartPanel()
    {
        super.initChartPanel();
        setRestrictPositive(false);
                
        // Set the dimensions of the chart panel
        chartPanel.setPreferredSize(new Dimension(MAX_ETD_PANEL_WIDTH, MAX_ETD_PANEL_HEIGHT));

        createTimeSeries(WACSAgent.AGENTNAME);
        setSeriesColor(WACSAgent.AGENTNAME, Color.BLUE);
        getChart().getXYPlot().getRangeAxis().setLabel("Degrees Celsius");
        
        removeLegend();
        setMinimumRange(0.001);
    }    
    
    @Override
    public synchronized void updateData()
    {   
        EtdStatusMessageBelief etdStatusMessageBelief = (EtdStatusMessageBelief) _belMgr.get(EtdStatusMessageBelief.BELIEF_NAME);
        if(etdStatusMessageBelief != null) {
            synchronized(etdStatusMessageBelief) {
                long time = etdStatusMessageBelief.getTime();
                if(time>_latestUpdateTime) {
                    String statusMessage = etdStatusMessageBelief.getStatusMessage();
                    String[] splitString = statusMessage.split(":|,");
                    double laserTemp = Double.parseDouble(splitString[1].trim());
                    //double laserTemp = 0.0;
                
                    addDataPoint(WACSAgent.AGENTNAME, new Millisecond(new Date(time)), laserTemp);
                    _latestUpdateTime = etdStatusMessageBelief.getTime();
                }
            }
        }
    } 
}
