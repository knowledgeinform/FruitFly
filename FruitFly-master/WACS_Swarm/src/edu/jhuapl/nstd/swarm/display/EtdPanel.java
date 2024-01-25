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
public class EtdPanel extends DynamicTimeSeriesChartPanel implements Updateable {
    private static final int MAX_ETD_PANEL_HEIGHT = 115;
    private static final int MAX_ETD_PANEL_WIDTH = 1750;
    
    private BeliefManager _belMgr;
    JLabel _etdDetectionMessage;
    JLabel _etdStatusMessage;
    JLabel _nmeaRawMessage;
    JTable _etdTable;
    JPanel _messagesPanel;
    JPanel _textPanel;
    JLabel _minConcLabel;
    JLabel _maxConcLabel;
    JPanel _minConcPanel;
    JPanel _maxConcPanel;
    JPanel _minMaxConcPanel;
    JScrollPane _etdScrollPane;
    static final String[] columnNames = {"Time", "Lat", "Lon", "Alt", "Concentration"};
    Object[][] data;
    
    JTextField _minConcTextBox;
    JTextField _maxConcTextBox;

    public float _minEtdDisplayValue;
    public float _maxEtdDisplayValue;
    
    private long _etdLastUpdateTime = 0L;
    
    private List<EtdDetection> _etdDetections;
    private long _latestUpdateTime;
    int _maxHistorySize = 15;
    Boolean _shouldFilter = false;
    
    public EtdPanel(BeliefManager belMgr) {
        super(EtdPanel.class.getSimpleName());
        
        _belMgr = belMgr;
          
        setIncludeZero(false);
        
        _etdDetections = new LinkedList<EtdDetection>();
        _latestUpdateTime = 0L;
        _maxHistorySize = Config.getConfig().getPropertyAsInteger("EtdDetectionBelief.historyLength", 15);
        _shouldFilter = Config.getConfig().getPropertyAsBoolean("Etd.shouldFilter", false);
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
        getChart().getXYPlot().getRangeAxis().setLabel("Concentration");
        
        removeLegend();
        
        _messagesPanel = new JPanel();
        BorderLayout borderLayout2 = new BorderLayout();
        _messagesPanel.setLayout(borderLayout2);
        _etdDetectionMessage = new JLabel("No Detection Message Received Yet");
        _messagesPanel.add(_etdDetectionMessage, borderLayout2.NORTH);
        _etdStatusMessage = new JLabel("No Status Message Received Yet");
        _messagesPanel.add(_etdStatusMessage, borderLayout2.CENTER);
        _nmeaRawMessage = new JLabel("No NMEA Message Received Yet");
        _messagesPanel.add(_nmeaRawMessage, borderLayout2.SOUTH);
        _messagesPanel.setMaximumSize(new Dimension(MAX_ETD_PANEL_WIDTH, 10));
        
        _minConcPanel = new JPanel();
        BorderLayout borderLayout3 = new BorderLayout();
        _minConcLabel = new JLabel("Green Value: ");
        _minConcPanel.add(_minConcLabel, borderLayout3.WEST);
        _minConcTextBox = new JTextField(Config.getConfig().getProperty("Etd.MinDisplayValue", "0"), 10);
        _minConcPanel.add(_minConcTextBox, borderLayout3.EAST);
        
        _maxConcPanel = new JPanel();
        BorderLayout borderLayout4 = new BorderLayout();
        _maxConcLabel = new JLabel("Red Value: ");
        _maxConcPanel.add(_maxConcLabel, borderLayout4.WEST);
        _maxConcTextBox = new JTextField(Config.getConfig().getProperty("Etd.MaxDisplayValue", "50"), 10);
        _maxConcPanel.add(_maxConcTextBox, borderLayout4.EAST);
        
        _minMaxConcPanel = new JPanel();
        BorderLayout borderLayout5 = new BorderLayout();
        _minMaxConcPanel.setLayout(borderLayout5);
        _minMaxConcPanel.add(_minConcPanel, borderLayout5.WEST);
        _minMaxConcPanel.add(_maxConcPanel, borderLayout5.EAST);
        
        _textPanel = new JPanel();
        BorderLayout borderLayout6 = new BorderLayout();
        _textPanel.setLayout(borderLayout6);
        _textPanel.add(_messagesPanel, borderLayout6.CENTER);
        _textPanel.add(_minMaxConcPanel, borderLayout6.SOUTH);
        _textPanel.setMaximumSize(new Dimension(MAX_ETD_PANEL_WIDTH, 15));
        
        _minEtdDisplayValue = Float.parseFloat(_minConcTextBox.getText());
        _maxEtdDisplayValue = Float.parseFloat(_maxConcTextBox.getText());

        // Create a separate altitude text display panel
        this.add(Box.createVerticalStrut(2));
        this.add(_textPanel);
        this.add(Box.createVerticalStrut(2));
        
        setMinimumRange(0.001);
    }    
    
    @Override
    public synchronized void updateData()
    {
        long currentTime = System.currentTimeMillis();
        long time = 0L;
        long deltaTime = 0L;
        double currentConcentration = 0.0;
        
        EtdDetectionListBelief etdDetectionListBelief = (EtdDetectionListBelief) _belMgr.get(EtdDetectionListBelief.BELIEF_NAME);
        if(etdDetectionListBelief != null) {
            synchronized(etdDetectionListBelief) {
                List<EtdDetection> etdDetections = etdDetectionListBelief.getDetections();
                if(_shouldFilter) {
                    etdDetections = etdDetectionListBelief.getFilteredDetections();
                }
                
                for(int i=0; i<etdDetections.size(); i++) {
                    EtdDetection etdDetection = etdDetections.get(i);
                    if(etdDetection.getTime()>_latestUpdateTime) {
                        _etdDetections.add(etdDetection);
                        _latestUpdateTime = etdDetection.getTime();
                    }
                }
            }
        }
 
        while(_etdDetections.size()>_maxHistorySize){
            _etdDetections.remove(0);
        }
        
        for(int i = 0; i<_etdDetections.size(); i++) {
            EtdDetection etd = _etdDetections.get(i);
            time = etd.getTime();
            Float concentration = etd.getConcentration();
            
            if(time>_etdLastUpdateTime) {
                addDataPoint(WACSAgent.AGENTNAME, new Millisecond(new Date(time)), concentration);
                _etdLastUpdateTime = etd.getTime();                
            }
        }
        
        EtdDetectionMessageBelief etdDetectionMessageBelief = (EtdDetectionMessageBelief) _belMgr.get(EtdDetectionMessageBelief.BELIEF_NAME);
        if (etdDetectionMessageBelief != null) {
            time = etdDetectionMessageBelief.getTime();
            deltaTime = (currentTime-time)/1000;
            _etdDetectionMessage.setText("DeltaTime: " + deltaTime + ", Time: " + time + ", " + etdDetectionMessageBelief.getDetectionMessage());
        }
        
        EtdStatusMessageBelief etdStatusMessageBelief = (EtdStatusMessageBelief) _belMgr.get(EtdStatusMessageBelief.BELIEF_NAME);
        if (etdStatusMessageBelief != null) {
            time = etdStatusMessageBelief.getTime();
            deltaTime = (currentTime-time)/1000;
            _etdStatusMessage.setText("DeltaTime: " + deltaTime + ", Time: " + time + ", " + etdStatusMessageBelief.getStatusMessage());
        }
        
        NmeaRawMessageBelief nmeaRawMessageBelief = (NmeaRawMessageBelief) _belMgr.get(NmeaRawMessageBelief.BELIEF_NAME);
        if(nmeaRawMessageBelief!=null) {
            time = nmeaRawMessageBelief.getTime();
            deltaTime = (currentTime-time)/1000;
            _nmeaRawMessage.setText("DeltaTime: " + deltaTime + ", Time: " + time + ", " + nmeaRawMessageBelief.getNmeaRawMessage());
        }
        
        try {
            _minEtdDisplayValue = Float.parseFloat(_minConcTextBox.getText());
            _maxEtdDisplayValue = Float.parseFloat(_maxConcTextBox.getText());
        } catch (NumberFormatException e) {
            System.out.println("Error: Min or max etd display value is not an integer...not changing min/max values");
        }
    } 
}
