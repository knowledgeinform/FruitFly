/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

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
import edu.jhuapl.nstd.swarm.util.Config;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.jfree.data.time.Millisecond;

/**
 *
 * @author kayjl1
 */
public class EtdHistoryPanel extends JPanel implements Updateable {
    private BeliefManager _belMgr;
    JTable _etdTable;
    JPanel _rawEtdPanel;
    JScrollPane _etdScrollPane;
    static final String[] columnNames = {"DeltaTime", "Time", "Lat", "Lon", "Alt", "Concentration"};
    Object[][] data;
    List<EtdDetection> _etdDetections;
    long _latestUpdateTime;
    int _maxHistorySize = 15;
    Boolean _shouldFilter = false;
    
    public EtdHistoryPanel(BeliefManager belMgr) {      
        _belMgr = belMgr;
        
        
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);
        
        int tableSize = Config.getConfig().getPropertyAsInteger("EtdDetectionBelief.historyLength", 15);    
        data = new Object[tableSize][6];
        _etdTable = new JTable(data, columnNames);
        _etdScrollPane = new JScrollPane(_etdTable);
        add(_etdScrollPane);
        
        _etdDetections = new LinkedList<EtdDetection>();
        _latestUpdateTime = 0L;
        
        _maxHistorySize = Config.getConfig().getPropertyAsInteger("EtdDetectionBelief.historyLength", 15);
        _shouldFilter = Config.getConfig().getPropertyAsBoolean("Etd.shouldFilter", false);
    }
    
    @Override
    public synchronized void update()
    {
        long currentTime = System.currentTimeMillis();
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
        
        int row = 0;
        for(int i=_etdDetections.size()-1; i>=0; i--) { //put the last item from list (most recent detection) in the top row of the table
            EtdDetection etd = _etdDetections.get(i);
            long time = etd.getTime();
            long deltaTime = (currentTime-time)/1000;
            LatLonAltPosition pos = etd.getPosition().asLatLonAltPosition();
            Float concentration = etd.getConcentration();

            _etdTable.setValueAt(deltaTime, row, 0);
            _etdTable.setValueAt(time, row, 1);
            _etdTable.setValueAt(pos.getLatitude(), row, 2);
            _etdTable.setValueAt(pos.getLongitude(), row, 3);
            _etdTable.setValueAt(pos.getAltitude(), row, 4);
            _etdTable.setValueAt(concentration, row, 5);

            row++;
        }
    }
}
