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
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.util.Config;




import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

import edu.jhuapl.jlib.math.*;


public class AgentSettingsPanel extends JPanel 
	implements Updateable, ListSelectionListener,
	           ActionListener, AgentTrackerListener
{
	private SearchCanvas _canvas;

	private HashMap _dials;
	private HashMap _dialogs;

	private JButton _centerViewButton;
	private JButton _makeDetection;

	private JTable _table;
	private JToggleButton _followAgentButton;

	private DefaultTableModel _tableModel;

	private BeliefManager _beliefManager;
	private AgentTracker _tracker;

	private boolean _showWind = Config.getConfig().getPropertyAsBoolean("AgentSettingsPanel.showWind", false);

	public static Vector _columnNames;
	private JButton _setAltitudeButton;
	private JTextField _altTextField;
	private JLabel _altLabel = new JLabel("Alt:");
	private JLabel _unitsLabel = new JLabel("m");
	private int _units = SearchCanvas.METERS;

	static {
		_columnNames = new Vector();
		_columnNames.add("Agent");
		_columnNames.add("Dial");
		_columnNames.add("In Swarm");
	}
		
	public AgentSettingsPanel(SearchCanvas canvas, BeliefManager beliefManager,
			AgentTracker tracker)
	{
		super(new GridLayout(2, 1));

		_canvas = canvas;
		_beliefManager = beliefManager;
		_tracker = tracker;
		_tracker.addAgentTrackerListener(this);

		_dials = new HashMap();
		_dialogs = new HashMap();
			
		
		
		_tableModel = new SettingsTableModel(new Vector(), _columnNames);
		_table = new JTable(_tableModel);
		_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_table.getSelectionModel().addListSelectionListener(this);

		_centerViewButton = new JButton("Move view to agent");
		_centerViewButton.addActionListener(this);
		_makeDetection = new JButton("Make Detection");
		_makeDetection.addActionListener(this);
		_followAgentButton = new JToggleButton("Follow Agent");
    _followAgentButton.addActionListener(this);

		_setAltitudeButton = new JButton("Set Altitude");
		_setAltitudeButton.addActionListener(this);
		_altTextField = new JTextField("");
		_altTextField.setHorizontalAlignment(JTextField.RIGHT);
		_altTextField.setMinimumSize(new Dimension(80,20));
		_altTextField.setPreferredSize(new Dimension(80,20));
		_altTextField.addActionListener(this);

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		buttonPanel.add(_centerViewButton,gc);
		gc.gridy = 1;
		buttonPanel.add(_makeDetection,gc);
		gc.gridy = 2;
		//buttonPanel.add(_followAgentButton,gc);
		gc.gridx = 0;
		gc.gridy = 0;
		JPanel altPanel = new JPanel(new GridBagLayout());
		altPanel.add(_altLabel,gc);
		gc.gridx = 1;
		gc.gridy = 0;
		altPanel.add(_altTextField,gc);
		gc.gridx = 2;
		gc.gridy = 0;
		altPanel.add(_unitsLabel,gc);
		gc.gridx = 0;
		gc.gridy = 2;
		gc.ipady = 8;
		buttonPanel.add(altPanel, gc);
		gc.gridx = 0;
		gc.gridy = 3;
		buttonPanel.add(_setAltitudeButton,gc);
		// set up the panel
		JScrollPane jsp = new JScrollPane(_table);
		add(jsp);
		add(buttonPanel);
                
                this.setMinimumSize(new Dimension (200, 200));
                this.setPreferredSize(this.getMinimumSize());
	}

	public synchronized void setUnits(int units) {
		_units = units;
		if (units == SearchCanvas.FEET) {
			_unitsLabel.setText("ft");
		}
		else if (units == SearchCanvas.METERS) {
			_unitsLabel.setText("m");
		}
		synchronized (_dials) {
			Iterator dialItr = _dials.values().iterator();
			Iterator dialogItr = _dialogs.values().iterator();
			while (dialItr.hasNext()) {
				AgentDial dial = (AgentDial)dialItr.next();
				dial.setUnits(_units);
			}
		}
	}
	
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;

		if (_table.getSelectionModel().isSelectionEmpty()) {
			_tracker.setSelectedAgent(null, this);
			return;
		}
		int idx = _table.getSelectionModel().getMinSelectionIndex();
		String agent = (String)_tableModel.getValueAt(idx, 0);
		_tracker.setSelectedAgent(agent, this);
	}

	public void update() 
        {
            try
            {
		AgentBearingBelief bearingBel = 
			(AgentBearingBelief)_beliefManager.get(
				AgentBearingBelief.BELIEF_NAME);
		AgentPositionBelief positionBel = 
			(AgentPositionBelief)_beliefManager.get(
				AgentPositionBelief.BELIEF_NAME);
		METBelief metBel = 
			(METBelief)_beliefManager.get(
				METBelief.BELIEF_NAME);
		if (_followAgentButton.isSelected())
      centerOnAgent();


		synchronized (_dials) {
			Iterator dialItr = _dials.values().iterator();
			Iterator dialogItr = _dialogs.values().iterator();
			while (dialItr.hasNext()) {
				AgentDial dial = (AgentDial)dialItr.next();
				JDialog dialog = (JDialog)dialogItr.next();
				if (dialog.isVisible()) {
					if (bearingBel != null)
						dial.updateBelief(bearingBel, positionBel);
					if (metBel != null && _showWind)
						dial.updateBelief(metBel, positionBel);
				}
			}	
		}
		ClassificationBelief cbb = (ClassificationBelief)_canvas.beliefManager.get(ClassificationBelief.BELIEF_NAME);
		if(cbb != null)
		{
			for(int i = 0; i < _tableModel.getRowCount(); i++)
			{
				String agentName = (String)_tableModel.getValueAt(i, 0);
				ClassificationTimeName ctn = cbb.getClassificationTimeName(agentName);
				if(ctn != null)
				{
					if(ctn.getClassification() == Classification.LANDED)
					{
						_tableModel.setValueAt(new Boolean(false), i, 2);
					}
					else if(ctn.getClassification() == Classification.FRIENDLY)
					{
						_tableModel.setValueAt(new Boolean(true), i, 2);
					}
				}
			}
		}
            }
            catch (Exception e)
            {
                System.err.println ("Exception in update thread - caught and ignored");
                e.printStackTrace();
            }
	}

	public String getSelectedAgent() {
		return (String)_tracker.getSelectedAgent();
	}
	
  private void centerOnAgent() {
    String name = getSelectedAgent();
    if (name != null) {
      PositionTimeName agentInfo = _tracker.getAgentInfo(name);
      if (agentInfo != null){
    	  _canvas.setViewCenter(agentInfo.getPosition());
      }
    }
  }
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _centerViewButton) {
			String name = (String)_tracker.getSelectedAgent();
			if (name != null) {
				PositionTimeName agentInfo = _tracker.getAgentInfo(name);
				_canvas.setViewCenter(agentInfo.getPosition());
			} 
		}
                //This is never used
		/*else if (e.getSource() == _setAltitudeButton) {
			try {
				String name = (String)_tracker.getSelectedAgent();
				if (name != null) {
					double alt = Double.valueOf(_altTextField.getText());
					if (_units == SearchCanvas.METERS) {
						alt = (new Altitude(alt, Length.METERS).getDoubleValue(Length.FEET));
					}
                                        ConfigProperty prop = new ConfigProperty("user.altitude", "" + alt, name);
					_canvas.beliefManager.put(new ConfigBelief(WACSDisplayAgent.AGENTNAME, prop));
				}
			} catch (Exception c) {
				c.printStackTrace();
			}
		}*/
	}

	/** Returns -1 on failure */
	private int getTableIndex(String agentName) {
		int idx = -1;
		for (int i = 0; i < _tableModel.getRowCount(); i++) {
			String name = (String)_tableModel.getValueAt(i, 0);
			if (name.equals(agentName)) {
				idx = i;
				break;
			}
		}
		return idx;
	}



	public void trackedAgentUpdate(AgentTrackerEvent event) {
		PositionTimeName agentInfo = event.getAgentInfo();
		String agentName = null;
		if (agentInfo != null)
			agentName = agentInfo.getName();

		if (event.getType() == AgentTrackerEvent.AGENT_ADDED) {
			synchronized (_tableModel) {
				_tableModel.addRow(new Object[] {agentName, new Boolean(false)});
			}
			Logger.getLogger("GLOBAL").info("agent added");
		} else if (event.getType() == AgentTrackerEvent.AGENT_SELECTED) {
			if (agentInfo != null) {
				int idx = getTableIndex(agentName);
				if (!_table.getSelectionModel().isSelectedIndex(idx))
					_table.getSelectionModel().setSelectionInterval(idx, idx);
			} else
				_table.getSelectionModel().clearSelection();
		} else if (event.getType() == AgentTrackerEvent.AGENT_REMOVED){
			synchronized (_tableModel) {
				int delIdx = getTableIndex(agentName);
				
				if (delIdx != -1) {
					_tableModel.setValueAt(new Boolean(false), delIdx, 1);
					Vector data = new Vector(_tableModel.getDataVector());
					data.remove(delIdx);
					_tableModel = new SettingsTableModel(data, _columnNames);
					_table.setModel(_tableModel);
					synchronized (_dials) {
						_dials.remove(agentName);
						_dialogs.remove(agentName);
					}
				}
			}
		}
	}

	private class SettingsTableModel extends DefaultTableModel {

		public SettingsTableModel() {
			super();
		}

		public SettingsTableModel(Vector data, Vector columnNames) {
			super(data, columnNames);
		}
		
		public Class getColumnClass(int col) {
			if (col == 0) return String.class;
			if (col == 1) return Boolean.class;
			if (col == 2) return Boolean.class;
			return null;
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 1) 
				return true;
			else if(col == 2)
			{
				ClassificationBelief cbb = (ClassificationBelief)_canvas.beliefManager.get(ClassificationBelief.BELIEF_NAME);
				if(cbb != null)
				{
					String agentName = (String)_tableModel.getValueAt(row, 0);
					ClassificationTimeName ctn = cbb.getClassificationTimeName(agentName);
					if(ctn != null)
					{
						if(ctn.getClassification() != Classification.SENSOR_COVERED && ctn.getClassification() != Classification.SENSOR_UNCOVERED)
						{
							return true;
						}
					}
				}
			}
			return false;
		}

		public synchronized Object getValueAt(int row, int col) {
			return super.getValueAt(row, col);
		}
		
		public synchronized void setDataVector(Vector data, Vector columnNames) {
			super.setDataVector(data, columnNames);
		}

		public synchronized void setValueAt(Object val, int row, int col) {
			super.setValueAt(val, row, col);

			if (col == 1)
			{
				boolean showDial = ((Boolean)val).booleanValue();
				String name = (String)getValueAt(row, 0);
				JDialog dialog = (JDialog)_dialogs.get(name);
				if (dialog == null && showDial) {
					AgentDial dial = new AgentDial();
					dial.setSelectedAgent(name);
					dial.setUnits(_units);
	
					Frame parentFrame = _canvas.getParentFrame();
					dialog = new JDialog(parentFrame, "Agent Dial - " + name, false);
					dialog.getContentPane().add(dial);
					dialog.addWindowListener(new DialogWindowAdapter(name));
	
					synchronized (_dials) {
						_dials.put(name, dial);
						_dialogs.put(name, dialog);
					}
					dialog.pack();
					dialog.setSize(300,300);
					dialog.setVisible(true);
				} else if (dialog != null && !showDial) {
					dialog.setVisible(false);
				} else if (dialog != null && showDial) {
					dialog.setVisible(true);
				}
			}
			else if(col == 2)
			{
				String agentName = (String)getValueAt(row, 0);
				if(((Boolean)val).booleanValue())
				{
					_canvas.beliefManager.put(new ClassificationBelief(agentName, agentName, Classification.FRIENDLY));
				}
				else
				{
					_canvas.beliefManager.put(new ClassificationBelief(agentName, agentName, Classification.LANDED));
				}
			}
		}
	}


	private class DialogWindowAdapter extends WindowAdapter {
		String _name;

		public DialogWindowAdapter(String name) {
			_name = name;
		}

		public void windowClosing(WindowEvent e) {
			int idx = AgentSettingsPanel.this.getTableIndex(_name);
			AgentSettingsPanel.this._tableModel.setValueAt(
				new Boolean(false), idx, 1);
		}
	}
}
