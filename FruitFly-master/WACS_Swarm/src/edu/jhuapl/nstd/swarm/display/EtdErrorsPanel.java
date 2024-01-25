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
import edu.jhuapl.nstd.swarm.belief.EtdDetection;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionMessageBelief;
import edu.jhuapl.nstd.swarm.belief.EtdErrorMessageBelief;
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
public class EtdErrorsPanel extends JPanel implements Updateable {
    private BeliefManager _belMgr;
    JTable _etdTable;
    JScrollPane _etdScrollPane;
    static final String[] columnNames = {"DeltaTime", "Time", "Error Message"};
    Object[][] data;
    private LinkedList<String> _errors;
    private LinkedList<Long> _errorTimes;
    private long _latestUpdateTime = 0L;
    private int _tableSize = 15;
    
    public EtdErrorsPanel(BeliefManager belMgr) {      
        _belMgr = belMgr;
        
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);
        
        _tableSize = Config.getConfig().getPropertyAsInteger("EtdErrorsPanel.historyLength", 15);    
        data = new Object[_tableSize][3];
        _etdTable = new JTable(data, columnNames);
        _etdScrollPane = new JScrollPane(_etdTable);
        add(_etdScrollPane);
        
        _errorTimes = new LinkedList<Long>();
        _errors = new LinkedList<String>();
    }
    
    @Override
    public synchronized void update()
    {
        long currentTime = System.currentTimeMillis();
        double currentConcentration = 0.0;
        
        EtdErrorMessageBelief newErrorMessageBelief = (EtdErrorMessageBelief) _belMgr.get(EtdErrorMessageBelief.BELIEF_NAME);
        if (newErrorMessageBelief != null) {
            synchronized(newErrorMessageBelief) {
                if(newErrorMessageBelief.getTime()>_latestUpdateTime) {
                    _errors.add(newErrorMessageBelief.getErrorMessage());
                    _errorTimes.add(newErrorMessageBelief.getTime());
                    _latestUpdateTime = newErrorMessageBelief.getTime();
                }
            }
            
            while(_errors.size() > _tableSize) {
                _errors.remove();
                _errorTimes.remove();
            }
            
            int row = 0;
            for(int i=_errors.size()-1; i>=0; i--) { //put the last item from list (most recent detection) in the top row of the table
                String errorMessage = _errors.get(i);
                long time = _errorTimes.get(i);
                long deltaTime = (currentTime-time)/1000;
                
                _etdTable.setValueAt(deltaTime, row, 0);
                _etdTable.setValueAt(time, row, 1);
                _etdTable.setValueAt(errorMessage, row, 2);

                row++;
            }
        }
    }
    
}
