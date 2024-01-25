package edu.jhuapl.nstd.swarm.player;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import edu.jhuapl.jlib.jgeo.*;
import edu.jhuapl.jlib.ui.overlay.*;

import java.awt.BorderLayout;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;

import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.util.*;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.belief.mode.*;
import edu.jhuapl.nstd.swarm.behavior.*;
import edu.jhuapl.nstd.swarm.behavior.group.*;

public class PlayerDisplay {

  //number of levels in the canvas
  public static final int NUM_LEVELS = 8;
    protected Player _player;
	protected PlayerCanvas _canvas;
	protected PlayerAgentSettingsPanel _playerAgentSettings;
    protected WACSPanel _wacsPanel;
	protected JTabbedPane _tabPane = new JTabbedPane();

	protected PlayerCanvas createCanvas(JFrame owner) {
		Length viewRange = new Length(2000.0, Length.MILES);
		LatLonAltPosition viewCenter = new LatLonAltPosition(
				new Latitude(39.236424020160925, Angle.DEGREES),
				new Longitude(-76.97699134695846, Angle.DEGREES),
				Altitude.ZERO);

        _player = new Player();
		return new PlayerCanvas(NUM_LEVELS,viewRange,600,600,JGeoCanvas.ORTHOGRAPHIC_PROJECTION,
				viewCenter,owner,_player);
	}

	public PlayerDisplay() {
		try {
			JFrame f = new JFrame("Player Display");
			_canvas = createCanvas(f);
      MousePositionReadoutLabel mprl = new MousePositionReadoutLabel();
      _canvas.addJGeoMouseListener(mprl);
      _canvas.addJGeoMouseMotionListener(mprl);
			_playerAgentSettings = _canvas.getPlayerAgentSettingsPanel();
            _wacsPanel =  _canvas.getWACSPanel();

			JPanel displaySettings = _canvas.getDisplaySettingPanel();
			_tabPane.addTab("Agent Settings", _playerAgentSettings);
			_tabPane.addTab("Display Settings", displaySettings);
            _tabPane.addTab("WACS Panel", _wacsPanel);
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					_tabPane,_canvas);
			splitPane.setOneTouchExpandable(true);
			splitPane.setDividerLocation(150);

			JToolBar toolbar = _canvas.createToolBar();
			JMenuBar menuBar = _canvas.createMenuBar();

			f.setJMenuBar(menuBar);

			f.getContentPane().add(splitPane, BorderLayout.CENTER);
			f.getContentPane().add(toolbar, BorderLayout.NORTH);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.pack();
			f.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		PlayerDisplay display = new PlayerDisplay();
	}
}
			


