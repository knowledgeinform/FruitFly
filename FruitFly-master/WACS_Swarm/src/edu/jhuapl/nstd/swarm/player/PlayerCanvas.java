package edu.jhuapl.nstd.swarm.player;

import java.util.logging.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;



import java.util.*;

import java.io.*;
import java.net.*;


import edu.jhuapl.nstd.swarm.util.Config;


import edu.jhuapl.jlib.jgeo.*;
import edu.jhuapl.jlib.jgeo.event.*;
import edu.jhuapl.jlib.jgeo.action.*;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;

import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.display.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PlayerCanvas extends JGeoCanvas implements JGeoMouseListener,
			 JGeoMouseMotionListener,
			 ActionListener,
             ChangeListener
{
	public class OpenAction extends AbstractAction {
		protected Player _player = null;
		protected PlayerCanvas _canvas = null;
		public OpenAction(String text, Player player, PlayerCanvas canvas) {
			super(text);
			_player = player;
			_canvas = canvas;
		}

		public void actionPerformed(ActionEvent e) {
			Logger.getLogger("GLOBAL").info("open");
			File f = null;
			JFileChooser chooser = new JFileChooser(".");
			chooser.setMultiSelectionEnabled(false);
			int option = chooser.showOpenDialog(null);
			if (option == JFileChooser.APPROVE_OPTION) {
				f = chooser.getSelectedFile();
				_player.load(f);
				_canvas.jgeoRepaint();
				return;
			}
			else {
				return;
			}
		}
	}

	private static final int MAP_LEVEL = 0;
	private static final int MATRIX_LEVEL = 1;
	private static final int POSITION_LEVEL = 3;
    private static final int MIN_REPAINT_TIME = 50;
	public static final long NORMAL_SPEED = 300;
	protected long _playSpeed = NORMAL_SPEED;
	protected Action _open;
	protected Action _panUp;
  protected Action _panDown;
  protected Action _panLeft;
  protected Action _panRight;
  protected Action _exitAction;
  protected Action _zoomInAction;
  protected Action _zoomOutAction;
	protected JToggleButton _playButton = new JToggleButton("PLAY", false);
	protected JToggleButton _pauseButton = new JToggleButton("PAUSE", false);
	protected JButton _fasterButton = new JButton("FASTER");
	protected JButton _slowerButton = new JButton("SLOWER");
	protected JButton _restartButton = new JButton("RESTART");
	protected JToggleButton _forwardButton = new JToggleButton("FORWARD", true);
	protected JToggleButton _backwardButton = new JToggleButton("BACKWARD", false);
    protected JSlider _progressSlider = new JSlider();
	protected LinkedList<String> _agentList = new LinkedList<String>();

	protected Player _player;
	protected PlayThread _playThread;

	protected PlayerAgentSettingsPanel _playerAgentSettingPanel = null;
    protected WACSPanel _wacsPanel = null;


    protected JRadioButton _namesButton = new JRadioButton("Show Names");
    protected JRadioButton _showHistory = new JRadioButton("Show Position History");

	protected Vector _images = new Vector();

	protected JFrame _parentFrame;

    protected boolean _showPositionHistory = false;
    protected long _lastRepaintTime = 0;


	public PlayerCanvas(int numLevels, Length range,
			int width, int height, int projectionType,
			LatLonAltPosition center, JFrame owner,
			Player player)
	{
		super(numLevels,
              range,
              width,
              height,
              projectionType,
              new LatLonAltPosition(new Latitude(Config.getConfig().getPropertyAsDouble("agent.startLat", center.getLatitude().getDoubleValue(Angle.DEGREES)), Angle.DEGREES),
                                    new Longitude(Config.getConfig().getPropertyAsDouble("agent.startLon", center.getLongitude().getDoubleValue(Angle.DEGREES)), Angle.DEGREES),
                                    center.getAltitude()));

		_parentFrame = owner;
		_player = player;
		_namesButton.addActionListener(this);
		_showHistory.addActionListener(this);
		_playButton.addActionListener(this);
		_pauseButton.addActionListener(this);
		_slowerButton.addActionListener(this);
		_restartButton.addActionListener(this);
		_fasterButton.addActionListener(this);
		_forwardButton.addActionListener(this);
		_backwardButton.addActionListener(this);
        _progressSlider.addChangeListener(this);
        _progressSlider.setValue(0);

		setPreferredSize(new Dimension(width,height));
		setBackground(Color.black);
		setLayout(null);
		_open = new OpenAction("Open...", player, this);
    _panUp = new PanUpAction(this);
    _panDown = new PanDownAction(this);
    _panLeft = new PanLeftAction(this);
    _panRight = new PanRightAction(this);
	  
    _zoomInAction = new ZoomInAction(this);
    _zoomOutAction = new ZoomOutAction(this);
    _exitAction = new ExitAction();
	  
    addJGeoMouseListener(this);
    addJGeoMouseMotionListener(this);

		URL url;
		try {
			File imageDir = new File("./images");
			System.err.println("getting files");
			String[] files = imageDir.list();
			LatLonAltPosition position;
			Length imgWidth;
			BufferedImage image;
			Length imgHeight;
			
			for (int i=0;i<files.length;i++){
				System.err.println("File: " + files[i]);
				if ((new File("./images/" + files[i])).isDirectory())
					continue;
				position = getImagePosition(files[i]);
				imgWidth = getImageWidth(files[i]);
				imgHeight = getImageHeight(files[i]);
				if (position != null){
					url = getClass().getClassLoader().getResource(files[i]);
					image = ImageIO.read(url);
					_images.add(new ImageData(imgHeight, imgWidth, position, image));
				}
			}
			setViewCenter(new LatLonAltPosition(new Latitude(Config.getConfig().getPropertyAsDouble("agent.startLat", center.getLatitude().getDoubleValue(Angle.DEGREES)), Angle.DEGREES),
                          new Longitude(Config.getConfig().getPropertyAsDouble("agent.startLon", center.getLongitude().getDoubleValue(Angle.DEGREES)), Angle.DEGREES),
                          center.getAltitude()));
			Logger.getLogger("GLOBAL").info("center: " + center);
    } catch(Exception ex){
      ex.printStackTrace();
    }

		this.setHorizontalRange(new Length(1000,Length.METERS));
	}

	protected LatLonAltPosition getImagePosition(String filename){
    try {
			StringTokenizer tokenizer = new StringTokenizer(filename, "_");
			tokenizer.nextToken();
			tokenizer.nextToken();
			String lat = tokenizer.nextToken();
			String lon = tokenizer.nextToken();
			System.err.println("lat: " + lat);
			System.err.println("lon: " + lon);
			Double latDouble = new Double(lat);
			Double lonDouble = new Double(lon);

			Latitude jLat = new Latitude(latDouble.doubleValue(), Angle.DEGREES);
			Longitude jLon = new Longitude(lonDouble.doubleValue(), Angle.DEGREES);
			return new LatLonAltPosition(jLat, jLon, Altitude.ZERO);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    	
	}
    
  protected Length getImageWidth(String filename){
  	try{
    	StringTokenizer tokenizer = new StringTokenizer(filename, "_");
			String imgWidth = tokenizer.nextToken();
			Double widthDouble = new Double(imgWidth);
			return new Length(widthDouble.doubleValue(), Length.METERS);
   	}	catch (Exception e){
    		return null;
    	}
  }
    
  protected Length getImageHeight(String filename ){
   	try{
     	StringTokenizer tokenizer = new StringTokenizer(filename, "_");
     	tokenizer.nextToken();
   		String imgHeight = tokenizer.nextToken();
   		Double heightDouble = new Double(imgHeight);
   		return new Length(heightDouble.doubleValue(), Length.METERS);
   	}	catch (Exception e){
     	return null;
   	}
  }

	public JFrame getParentFrame() {
		return _parentFrame;
	}
			
	public JPanel getDisplaySettingPanel() {
		JPanel panel = new JPanel(new GridLayout(2,1));
		panel.add(_namesButton);
		panel.add(_showHistory);
		panel.setVisible(true);
		return panel;
	}

	public PlayerAgentSettingsPanel getPlayerAgentSettingsPanel() {
		_playerAgentSettingPanel = new PlayerAgentSettingsPanel(this);
		return _playerAgentSettingPanel;
	}

    public WACSPanel getWACSPanel()
    {
        if (_wacsPanel == null)
        {
            _wacsPanel = new WACSPanel(_player);
        }
        return _wacsPanel;
    }

  public void jgeoUpdateComponent(JGeoGraphics graphics) {
    jgeoPaintComponent(graphics);
  }

	public AbsolutePosition getPosition(String name) {
		Vector<PlayerPosition> posList = (_player.getAgentPositions()).get(name);
		if (posList == null) {
			posList = (_player.getTargetPositions()).get(name);
		}
		return posList.get(posList.size()-1).asAbsolutePosition();
	}

	protected void drawAgents(JGeoGraphics jg) throws Exception {      
		jg.setLevel(POSITION_LEVEL);
		jg.clear();

		//draw agents
		HashMap<String,Vector<PlayerPosition>> positions = _player.getAgentPositions();
		Set<String> ids = positions.keySet();
		Iterator<String> itr = ids.iterator();
		while (itr.hasNext()) {			
			String name = itr.next();
			if (!_agentList.contains(name)) {
				_agentList.add(name);
				_playerAgentSettingPanel.addAgent(name);
			}
			if (!_playerAgentSettingPanel.showAgent(name))
				continue;
			if (_player.isTarget(name)) {
				jg.setColor(Color.red);
			}
			Vector<PlayerPosition> posList = positions.get(name);


            if (posList.size() > 0 && _showPositionHistory)
            {
                AbsolutePosition[] historicalPositions = new AbsolutePosition[posList.size() - 1];

                //
                // Draw position history if enabled
                //
                for (int i = 0; i < (posList.size() - 1); ++i)
                {
                    historicalPositions[i] = posList.get(i).asAbsolutePosition();
                }


                jg.setColor(Color.YELLOW);
                jg.drawPolyline(historicalPositions);
            }

            //
            // Draw current agent position
            //
			PlayerPosition pos = posList.get(posList.size()-1);
			NavyAngle currHeading = new NavyAngle(pos.getHeading(), Angle.DEGREES);
			AbsolutePosition p = pos.asAbsolutePosition();
			RangeBearingHeightOffset top = new RangeBearingHeightOffset(new Length(80.0,Length.METERS),
						currHeading,
						Length.ZERO);
			RangeBearingHeightOffset right = new RangeBearingHeightOffset(new Length(20.0,Length.METERS),
						currHeading.plus(Angle.RIGHT_ANGLE),
						Length.ZERO);
			RangeBearingHeightOffset left = new RangeBearingHeightOffset(new Length(20.0,Length.METERS),
						currHeading.plus(Angle.NEGATIVE_RIGHT_ANGLE),
						Length.ZERO);
			AbsolutePosition[] triangle = new AbsolutePosition[3];
			triangle[0] = p.translatedBy(top);
			triangle[1] = p.translatedBy(right);
			triangle[2] = p.translatedBy(left);
			
			if (_namesButton.isSelected()) {
				jg.drawString(" " + name, p);
			}
            jg.setColor(Color.MAGENTA);
			jg.fillPolygon(triangle);
			//jg.fillOval(p, new Length(50, Length.METERS), new Length(50, Length.METERS));
		}
	}

	protected void drawTargets(JGeoGraphics jg) throws Exception {
		jg.setColor(Color.white);
		HashMap<String,Vector<PlayerPosition>> positions = _player.getTargetPositions();
		Set<String> ids = positions.keySet();
		Iterator<String> itr = ids.iterator();
		while (itr.hasNext()) {
			String name = itr.next();
			if (!_agentList.contains(name)) {
				_agentList.add(name);
				_playerAgentSettingPanel.addAgent(name);
			}
			if (!_playerAgentSettingPanel.showAgent(name))
				continue;
			Vector<PlayerPosition> posList = positions.get(name);
			PlayerPosition pos = posList.get(posList.size()-1);
			int classification = pos.getClassification();
			AbsolutePosition p = pos.asAbsolutePosition();

			if (classification == Classification.VEHICLE_TARGET)
				jg.setColor(Color.WHITE);
			
			if (_namesButton.isSelected()) {
				jg.drawString(" " + name, p);
			}
			jg.fillOval(p, new Length(35,Length.METERS), new Length(35,Length.METERS));
		}
	}

    protected void drawCloudDetections(JGeoGraphics jg) throws Exception
    {
        Vector<CloudDetection> cloudDetections = _player.getCloudDetections();

        for (CloudDetection cloudDetection : cloudDetections)
        {
            if (cloudDetection.getSource() == CloudDetection.SOURCE_PARTICLE)
            {
                jg.setColor(new Color(0.0f, 0.0f,1.0f,0.5f));
            }
            else
            {
                jg.setColor(new Color(1.0f, 0.0f,0.0f,0.5f));
            }
            jg.fillOval(cloudDetection.getDetection(), new Length(25,Length.METERS), new Length(25,Length.METERS));
        }        
    }

    protected void drawCircularOrbitBelief(JGeoGraphics jg) throws Exception
    {
        CircularOrbitBelief circularOrbitBelief = _player.getCircularOrbitBelief();

        if (circularOrbitBelief != null)
        {
            Length ellipseSize = circularOrbitBelief.getRadius().times(2);
            Ellipse ellipse = new Ellipse(ellipseSize, ellipseSize, circularOrbitBelief.getPosition(), NavyAngle.NORTH);
           
            Color oldColor = jg.getColor();
            jg.setColor(Color.PINK);
            jg.drawEllipse(ellipse);      
            jg.setColor(oldColor);
        }
    }

	protected void drawRect(JGeoGraphics jg) {
		jg.setColor(Color.green);
		//AbsolutePosition p = new LatLonAltPosition(new Latitude(37.0371466, Angle.DEGREES),
			//																				 new Longitude(-76.2473078, Angle.DEGREES),
				//																			 Altitude.ZERO);
		AbsolutePosition p = new LatLonAltPosition(new Latitude(37.0244813, Angle.DEGREES),
																							 new Longitude(-76.26977712, Angle.DEGREES),
																							 Altitude.ZERO);
		jg.fillRect(p, new Length(1100, Length.METERS), new Length(900, Length.METERS));
	}

	protected void drawMatrix(JGeoGraphics jg) {
		PrimitiveTypeGeocentricMatrix matrix = _player.getMatrix();
		if (matrix == null)
			return;
		int xSize = matrix.getXSize();
		int ySize = matrix.getYSize();

		Region r;	
		float n; 
		float min = matrix.getMinimum().value;
		float max = matrix.getMaximum().value;
		float diff = min - max;
		//Logger.getLogger("GLOBAL").info("Max: " + max + "	 Min: " + min);

	
		for (int x=0;x<xSize;x++) {
			for (int y=0;y<ySize;y++) {
				r = matrix.getRegion(new Point(x, y));
				n = matrix.get(x, y); 

				float myDiff = min - n;
				float dividedDiff = myDiff / diff;

				//d is a value between 0 and 1
				float d = Math.abs(myDiff);

				//Logger.getLogger("GLOBAL").info("d is: " + d);
				jg.setColor(new Color(0f, d, 0f, 0.4f));

				if (d < .1) {
					jg.drawRegion(r);
				}
				else {
					jg.fillRegion(r);
					jg.setColor(Color.BLACK);
					jg.drawRegion(r);
				}
			}
		}
	}

	/*protected void drawMatrix(JGeoGraphics jg) {
		PrimitiveTypeGeocentricMatrix matrix = _player.getMatrix();
		if (matrix == null)
			return;
		jg.setLevel(MATRIX_LEVEL);
		int xSize = matrix.getXSize();
		int ySize = matrix.getYSize();
		Stroke oldStroke = jg.getStroke();

		Region r;	
		float n; 

		ArrayList<Region> regions = new ArrayList<Region>();
		ArrayList<Float> colors = new ArrayList<Float>();

		for (int x=0; x<xSize; x++) {
			for (int y=0; y<ySize; y++) {
				r = matrix.getRegion(new Point(x,y));
				n = matrix.get(x,y);
				float d = n;

				if (d != 0) {
					regions.add(0,r);
					colors.add(0,new Float(d));
				}
			}
		}

		for (int c=0; c<regions.size(); c++) {
			r = regions.get(c);
			n = (colors.get(c)).floatValue();
			if (n == 1.0) {
				jg.setColor(Color.green);
			}
			else if (n == -1.0) {
				jg.setColor(Color.red);
			}
			else {
				jg.setColor(Color.black);
			}
			jg.setStroke(new BasicStroke(2));
			jg.drawRegion(r);
		}
		jg.setStroke(oldStroke);
	}*/


	protected void drawMap(JGeoGraphics jg) throws Exception {
		boolean redrawMapLevel = jg.viewChanged();

		if (redrawMapLevel) {
			jg.setLevel(MAP_LEVEL);
			jg.clear();
			jg.setColor(Color.blue);
			jg.drawOcean();

			Iterator itr = _images.iterator();
			while (itr.hasNext()) {
				ImageData data = (ImageData)itr.next();
				jg.drawImage(data.getPosition(), data.getWidth(), data.getHeight(), data.getImage());
			}
		}
	}

	public void jgeoPaintComponent(JGeoGraphics jg) {
		try
        {
            if ((System.currentTimeMillis() - _lastRepaintTime) > MIN_REPAINT_TIME)
            {
                _progressSlider.setMinimum(0);
                _progressSlider.setMaximum(_player.getNumMetricIndicies());
                _progressSlider.setValue(_player.getPlaybackPosition());

                drawMap(jg);
                //drawMatrix(jg);
                //drawRect(jg);
                drawAgents(jg);
                drawTargets(jg);
                drawCloudDetections(jg);
                drawCircularOrbitBelief(jg);
                _wacsPanel.UpdateText();

                _lastRepaintTime = System.currentTimeMillis();
            }
            else
            {
                Thread.sleep(MIN_REPAINT_TIME / 10);
            }
		}
        catch (Exception e)
        {
			e.printStackTrace();
		}
	}

	public JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();
	 
		toolBar.add(_restartButton);
		toolBar.add(_backwardButton);
		toolBar.add(_slowerButton);
		toolBar.add(_playButton);
		toolBar.add(_pauseButton);
		toolBar.add(_fasterButton);
		toolBar.add(_forwardButton);
		toolBar.addSeparator();
		toolBar.add(_panUp);
		toolBar.add(_panDown);
		toolBar.add(_panLeft);
		toolBar.add(_panRight);
		toolBar.add(_zoomInAction);
		toolBar.add(_zoomOutAction); 
		toolBar.add(_exitAction);
        toolBar.add(_progressSlider);

		return toolBar;
	}

	public JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		 
		JMenu file = new JMenu("File");
		file.add(_open);
		JMenuItem item = new JMenuItem("Load Search Goal...");
		item.addActionListener(this);
		item.setActionCommand("loadSearchGoal");
		file.add(item);
		file.add(_exitAction);
		menuBar.add(file);

		return menuBar;
	}
		
	protected void play() {
		_playButton.setSelected(true);
		_pauseButton.setSelected(false);
		_playThread = new PlayThread(_player, this, _playSpeed);
		_playThread.start();
	}

	protected void faster() {
		_playThread.faster();
		if (_playThread != null)
			_playSpeed = _playThread.getSpeed();
	}

	protected void restart() {
		pause();
		_player.restart();
		this.jgeoRepaint();
	}

	protected void slower() {
		_playThread.slower();
		if (_playThread != null)
			_playSpeed = _playThread.getSpeed();
	}

	protected void pause() {
		_playButton.setSelected(false);
		_pauseButton.setSelected(true);
		_playThread.pause();
	}

	protected void forward() {
		_forwardButton.setSelected(true);
		_backwardButton.setSelected(false);
		_player.setForward();
	}

	protected void backward() {
		_forwardButton.setSelected(false);
		_backwardButton.setSelected(true);
		_player.setBackward();
	}

	public void donePlaying() {
		_playButton.setSelected(false);
		_pauseButton.setSelected(true);
	}

	protected void loadSearchGoal() {
		try {
			File f;
			JFileChooser chooser = new JFileChooser(".");
			chooser.setMultiSelectionEnabled(false);
			int option = chooser.showOpenDialog(this);
			if (option == JFileChooser.APPROVE_OPTION) {
				f = chooser.getSelectedFile();
			}
			else {
				return;
			}
			ObjectInputStream in = new ObjectInputStream(
					new FileInputStream(f));
			_player.setMatrix((PrimitiveTypeGeocentricMatrix)in.readObject());
			Logger.getLogger("GLOBAL").info("loaded");
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

  public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _playButton) 
			play();
		else if (e.getSource() == _pauseButton)
			pause();
		else if (e.getSource() == _fasterButton)
			faster();
		else if (e.getSource() == _slowerButton)
			slower();
		else if (e.getSource() == _restartButton)
			restart();
		else if (e.getSource() == _forwardButton)
			forward();
		else if (e.getSource() == _backwardButton)
			backward();
		else if (e.getSource() == _namesButton)
			this.jgeoRepaint();
        else if (e.getSource() == _showHistory)
        {
            _showPositionHistory = _showHistory.isSelected();
        }

		if (e.getActionCommand().equalsIgnoreCase("loadSearchGoal")) {
			loadSearchGoal();
		}
	}

    public void stateChanged(ChangeEvent e)
    {
        if (e.getSource() == _progressSlider)
        {
            _player.setPlaybackPosition(_progressSlider.getValue());
        }
    }

	public void mousePressed(JGeoMouseEvent event) {}
	public void mouseMoved(JGeoMouseEvent event){}
	public void mouseDragged(JGeoMouseEvent event){}
	public void mouseReleased(JGeoMouseEvent event) {}
	public void mouseClicked(JGeoMouseEvent event) {
		setViewCenter(event.getLatLonAltPosition());
		this.jgeoRepaint();
	}
	public void mouseEntered(JGeoMouseEvent event) {}
	public void mouseExited(JGeoMouseEvent event) {}

}

