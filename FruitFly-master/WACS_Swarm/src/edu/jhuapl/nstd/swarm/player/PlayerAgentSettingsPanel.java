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

package edu.jhuapl.nstd.swarm.player;

import edu.jhuapl.nstd.swarm.display.event.*;
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.belief.*;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.Frame;
import java.awt.Component;
import java.awt.*;

import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.jlib.jgeo.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;


public class PlayerAgentSettingsPanel extends JPanel 
	implements ListSelectionListener,
	           ActionListener 
{
	private PlayerCanvas _canvas;
	private String _selectedAgent = null;

	private HashMap<String,Boolean> _showAgents = new HashMap<String,Boolean>();

	private JButton _centerViewButton;

	private JTable _table;
	private JToggleButton _followAgentButton;

	private DefaultTableModel _tableModel;

	public static Vector _columnNames;
	//NJA
	private JButton _goToLatLonButton;
	private JTextField _latTextField;
	private JTextField _lonTextField;
	private JLabel _latLabel = new JLabel("Lat:");
	private JLabel _lonLabel = new JLabel("Lon:");
		
	static {
		_columnNames = new Vector();
		_columnNames.add("Agent");
		_columnNames.add("Show");
	}
		
	public PlayerAgentSettingsPanel(PlayerCanvas canvas)
	{
		super(new GridLayout(2, 1));

		_canvas = canvas;

		
		_tableModel = new SettingsTableModel(new Vector(), _columnNames, this);
		_table = new JTable(_tableModel);
		_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_table.getSelectionModel().addListSelectionListener(this);

		_centerViewButton = new JButton("Move view to agent");
		_centerViewButton.addActionListener(this);
		_followAgentButton = new JToggleButton("Follow Agent");
    _followAgentButton.addActionListener(this);

		//NJA
		_goToLatLonButton = new JButton("Go To Lat-Lon");
		_goToLatLonButton.addActionListener(this);
		_latTextField = new JTextField(Config.getConfig().getProperty("agent.startLat"));
		_latTextField.setMinimumSize(new Dimension(80,20));
		_latTextField.setPreferredSize(new Dimension(80,20));
		_latTextField.addActionListener(this);
		_lonTextField = new JTextField(Config.getConfig().getProperty("agent.startLon"));
		_lonTextField.setMinimumSize(new Dimension(80,20));
		_lonTextField.setPreferredSize(new Dimension(80,20));
		_lonTextField.addActionListener(this);

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		buttonPanel.add(_centerViewButton,gc);
		gc.gridy = 1;
		buttonPanel.add(_followAgentButton,gc);
		//NJA
		gc.gridx = 0;
		gc.gridy = 0;
		JPanel latLonPanel = new JPanel(new GridBagLayout());
		latLonPanel.add(_latLabel,gc);
		gc.gridx = 1;
		gc.gridy = 0;
		latLonPanel.add(_latTextField,gc);
		gc.gridx = 0;
		gc.gridy = 1;
		latLonPanel.add(_lonLabel,gc);
		gc.gridx = 1;
		gc.gridy = 1;
		latLonPanel.add(_lonTextField,gc);
		gc.gridx = 0;
		gc.gridy = 2;
		buttonPanel.add(latLonPanel,gc);
		
		//buttonPanel.add(latLonPanel);
		//buttonPanel.add(_latTextField);
		//buttonPanel.add(_lonLabel);
		//buttonPanel.add(_lonTextField);
		gc.gridx = 0;
		gc.gridy = 3;
		buttonPanel.add(_goToLatLonButton,gc);

		// set up the panel
		JScrollPane jsp = new JScrollPane(_table);
		add(jsp);
		add(buttonPanel);
	}
	
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;

		if (_table.getSelectionModel().isSelectionEmpty()) 
			return;
		int idx = _table.getSelectionModel().getMinSelectionIndex();
		String agent = (String)_tableModel.getValueAt(idx, 0);
		_selectedAgent = agent;
	}

	public String getSelectedAgent() {
		return _selectedAgent;
	}
	
  private void centerOnAgent() {
    String name = getSelectedAgent();
    if (name != null) {
			AbsolutePosition pos = _canvas.getPosition(name);
      if (pos != null) {
    	  _canvas.setViewCenter(pos);
      }
    }
  }

	public void redraw() {
		_canvas.jgeoRepaint();
	}

	public boolean showAgent(String name) {
		return _showAgents.get(name);
	}

	protected synchronized void setShow(String name, boolean show) {
		_showAgents.put(name,show);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _centerViewButton) {
			if (_selectedAgent != null) {
				AbsolutePosition pos = _canvas.getPosition(_selectedAgent);
				if (pos != null) {
					_canvas.setViewCenter(pos);
					_canvas.jgeoRepaint();
				}
			} 
		}
		else if (e.getSource() == _goToLatLonButton) {
			System.out.println("going to lat lon");
			if (_latTextField.getText() != null && _lonTextField.getText() != null) {
				double _goToLat=0, _goToLon=0;
				try {
					_goToLat = Double.valueOf(_latTextField.getText().trim()).doubleValue();
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
				try {
					_goToLon = Double.valueOf(_lonTextField.getText().trim()).doubleValue();
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
				AbsolutePosition goToLatLonAltPos = 
					new LatLonAltPosition(new Latitude(_goToLat, Angle.DEGREES), new Longitude(_goToLon, Angle.DEGREES), Altitude.ZERO);
				_canvas.setViewCenter(goToLatLonAltPos);
				_canvas.jgeoRepaint();
			}
		}
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


	public void addAgent(String agentName) {
		synchronized (_tableModel) {
			_tableModel.addRow(new Object[] {agentName, new Boolean(true)});
		}
		_showAgents.put(agentName,true);
	}

	public void removeAgent(String agentName) {
		synchronized (_tableModel) {
			int delIdx = getTableIndex(agentName);
			
			if (delIdx != -1) {
				_tableModel.setValueAt(new Boolean(false), delIdx, 1);
				Vector data = new Vector(_tableModel.getDataVector());
				data.remove(delIdx);
				_tableModel = new SettingsTableModel(data, _columnNames, this);
				_table.setModel(_tableModel);
			}
		}
	}

	private class SettingsTableModel extends DefaultTableModel {

		private PlayerAgentSettingsPanel _pasp = null;
		public SettingsTableModel() {
			super();
		}

		public SettingsTableModel(Vector data, Vector columnNames) {
			super(data, columnNames);
		}

		public SettingsTableModel(Vector data, Vector columnNames, PlayerAgentSettingsPanel pasp) {
			super(data, columnNames);
			_pasp = pasp;
		}
		
		public Class getColumnClass(int col) {
			if (col == 0) return String.class;
			if (col == 1) return Boolean.class;
			return null;
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 1) return true;
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

			if (col != 1) return;

			boolean showAgent = ((Boolean)val).booleanValue();
			String name = (String)getValueAt(row, 0);
			if (_pasp != null) {
				_pasp.setShow(name,showAgent);
				_pasp.redraw();
			}
		}
	}


	private class DialogWindowAdapter extends WindowAdapter {
		String _name;

		public DialogWindowAdapter(String name) {
			_name = name;
		}

		public void windowClosing(WindowEvent e) {
			int idx = PlayerAgentSettingsPanel.this.getTableIndex(_name);
			PlayerAgentSettingsPanel.this._tableModel.setValueAt(
				new Boolean(false), idx, 1);
		}
	}
}
