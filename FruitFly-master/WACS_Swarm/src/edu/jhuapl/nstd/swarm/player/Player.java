package edu.jhuapl.nstd.swarm.player;

import java.util.logging.*;




import java.util.*;

import java.io.*;
import javax.swing.*;
 

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;

import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.behavior.*;
import edu.jhuapl.nstd.swarm.comms.BeliefManagerClient;
import edu.jhuapl.nstd.swarm.util.*;

public class Player {
	protected BufferedReader _reader = null;
	protected boolean _loaded = false;
	protected List<String> _file = null;
	protected int _position = 0;
	protected boolean _forward = true;
	protected boolean _loadedMatrix = false;
	protected PrimitiveTypeGeocentricMatrix _matrix = null;
	protected boolean _collecting = false;
	protected boolean _collectPosition = false;
	protected boolean _collectTarget = false;
    protected String _beliefName;
    protected int _fieldNum = 0;

	protected HashMap<String,Vector<PlayerPosition>> _agentPositions;
	protected HashMap<String,Vector<PlayerPosition>> _targetPositions;
    protected Vector<CloudDetection> _cloudDetections;
    protected ParticleDetectionBelief _bioDetection;
    protected PlumeDetectionBelief _plumeDetection;
    protected AnacondaDetectionBelief _anacondaDetection;
    protected int _numCloudDetections;
    protected Latitude _cloudDetectionLatitude;
    protected Longitude _cloudDetectionLongitude;
    protected Altitude _cloudDetectionAltitudeMSL;
    protected double _cloudDetectionStrength;
    protected long _cloudDetectionTimestamp_ms;
    protected short _cloudDetectionSource;
    protected int _maxHistoricalPositions;
    protected String _inputFileName;
    protected String _anacondaDetectionString = "";
    protected String _bioDetectionString = "";

    protected Latitude _circularOrbitLatitude;
    protected Longitude _circularOrbitLongitude;
    protected Altitude _circularOrbitAltitudeMSL;
    protected Length _circularOrbitRadius;
    protected CircularOrbitBelief _circularOrbitBelief;
    protected Vector<Integer> _metricEntryIndicies;
    protected int _numMetricIndicies = 0;

    BeliefManagerImpl beliefManager = null ;
    RobotUpdateManager updateManager ;
    BeliefManagerClient client ;



	public void load(File f) {
        _inputFileName = f.getAbsolutePath();
		reset();
		try {
			_file = new LinkedList<String>();
			_reader = new BufferedReader(new FileReader(f));
			String line = "";
            int i = 0;
			while ((line = _reader.readLine()) != null) {
                if (line.length() >= 5 && line.substring(0, 4).equals("time:"))
                {
                    _metricEntryIndicies.add(i);
                }
                _file.add(line);
                ++i;
			}

            _numMetricIndicies = i;

            _reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		_loaded = true;
		step();
	}

	protected synchronized void reset()
    {
		_agentPositions = new HashMap<String,Vector<PlayerPosition>>();
		_targetPositions = new HashMap<String,Vector<PlayerPosition>>();
        _cloudDetections = new Vector<CloudDetection>();
        _metricEntryIndicies = new Vector<Integer>();
        _bioDetection = null;
        _plumeDetection = null;
        _anacondaDetection = null;
        _circularOrbitBelief = null;
		_loaded = false;
		_matrix = null;
		_position = 0;
	}

	public Player() {
		_agentPositions = new HashMap<String,Vector<PlayerPosition>>();
		_targetPositions = new HashMap<String,Vector<PlayerPosition>>();
        _cloudDetections = new Vector<CloudDetection>();
        _metricEntryIndicies = new Vector<Integer>();
        _bioDetection = null;
        _plumeDetection = null;
        _anacondaDetection = null;
        _circularOrbitBelief = null;
		_loaded = false;
        _maxHistoricalPositions = Config.getConfig().getPropertyAsInteger("RecordingPlayer.maxHistoricalPositions", 1000);


        boolean useBelMgr = true;
        if (useBelMgr)
        {
            try
            {
                beliefManager = new BeliefManagerImpl("Player");
                updateManager = new RobotUpdateManager();
                updateManager.register(beliefManager, Updateable.BELIEF);
                updateManager.start();

                client = new BeliefManagerClient(beliefManager);
                }
            catch (IOException e)
            {
                e.printStackTrace();
                System.err.println ("Error creating belief manager.");
            }
        }


            //_beliefs = new LinkedList();
	}

	public synchronized HashMap<String,Vector<PlayerPosition>>  getAgentPositions() { return _agentPositions; }
	public synchronized HashMap<String,Vector<PlayerPosition>> getTargetPositions() { return _targetPositions; }
    public synchronized Vector<CloudDetection> getCloudDetections() { return _cloudDetections; }
    public synchronized ParticleDetectionBelief getBioDetection() { return _bioDetection; }
    public synchronized PlumeDetectionBelief getPlumeDetection() { return _plumeDetection; }
    public synchronized AnacondaDetectionBelief getAnacondaDetection() { return _anacondaDetection; }
    public synchronized CircularOrbitBelief getCircularOrbitBelief() { return _circularOrbitBelief; }
	public synchronized PrimitiveTypeGeocentricMatrix getMatrix() { return _matrix; }

	public static AbsolutePosition createAbsolutePosition(double lat, double lon, double altitude) {
		return new LatLonAltPosition(new Latitude(lat, Angle.DEGREES),
																 new Longitude(lon, Angle.DEGREES),
																 new Altitude(altitude, Length.METERS));
	}

	protected static PrimitiveTypeGeocentricMatrix createMatrix(double lat, double lon,
																											 int gridRows, int gridCols,
																											 double width, double height)
	{
		LatLonAltPosition southwestCorner = (createAbsolutePosition(lat,lon,0.0)).asLatLonAltPosition();

		RangeBearingHeightOffset offsetX = new RangeBearingHeightOffset(
						new Length(width, Length.METERS), NavyAngle.EAST,
						Length.ZERO);
		RangeBearingHeightOffset offsetY = new RangeBearingHeightOffset(
						new Length(height, Length.METERS), NavyAngle.NORTH,
						Length.ZERO);

		LatLonAltPosition northeastCorner = (LatLonAltPosition) southwestCorner
						.translatedBy(offsetX);
		northeastCorner = (LatLonAltPosition) northeastCorner
						.translatedBy(offsetY);

		PrimitiveTypeGeocentricMatrix.Header header = new PrimitiveTypeGeocentricMatrix.Header(
						southwestCorner, northeastCorner, gridRows, gridCols,
						new Date());
		return new PrimitiveTypeGeocentricMatrix(header);
	}

	public void setMatrix(PrimitiveTypeGeocentricMatrix in) {
		_matrix = in;
		_loadedMatrix = true;
	}

	public boolean isTarget(String name) {
		List<PlayerPosition> list = _targetPositions.get(name);
		return list != null;
	}

	protected void evaluateLine(String line) {
		try {
			if (!_collecting) {
				StringTokenizer tok = new StringTokenizer(line);

                if (tok.hasMoreTokens())
                {
                    String header = tok.nextToken();

                    if ((_forward && header.equalsIgnoreCase("belief:"))
                            || (!_forward && header.equalsIgnoreCase("end")))
                    {
                        String belief = tok.nextToken();
                        if (belief.equalsIgnoreCase("agentPositionBelief")) {
                            _collectPosition = true;
                            _collecting = true;
                        }
                        else if (belief.equalsIgnoreCase("targetBelief")) {
                            _collectTarget = true;
                            _collecting = true;
                        }
                        else {
                            _collecting = true;
                        }
                    }
                    else if (header.equalsIgnoreCase("matrix:")) {
                        //example: lat lon gridRows gridCols width height
                        //matrix: 0.0 0.0 5 5 0.0 0.0
                        if (_loadedMatrix) {
                            return;
                        }
                        double lat = Double.valueOf(tok.nextToken());
                        double lon = Double.valueOf(tok.nextToken());
                        int gridRows = Integer.valueOf(tok.nextToken());
                        int gridCols = Integer.valueOf(tok.nextToken());
                        double gridWidth = Double.valueOf(tok.nextToken());
                        double gridHeight = Double.valueOf(tok.nextToken());
                        _matrix = createMatrix(lat,lon,gridRows,gridCols,gridWidth,gridHeight);
                        Logger.getLogger("GLOBAL").info("matrix created");
                    }
                    else
                    {
                        _beliefName = header.substring(0, header.length() - 1);
                        if (_beliefName.equals("clouddetectionbelief") ||
                            _beliefName.equals("biopoddetectionbelief") ||
                            _beliefName.equals("plumedetectionbelief") ||
                            _beliefName.equals("spiderdetectionbelief") ||
                            _beliefName.equals("anacondadetectionbelief") ||
                            _beliefName.equals("circularorbitbelief"))
                        {
                            _collecting = true;
                        }
                    }
                }
                
				return;
			}
			else {
				evaluateBelief(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void doneCollecting() {
		_collectPosition = _collectTarget = _collecting = false;
        _fieldNum = 0;
	}

	protected void evaluateBelief(String line) throws IOException {
		StringTokenizer tok = new StringTokenizer(line);

        if (tok.hasMoreTokens())
        {
            String s = tok.nextToken();
            if ((_forward && s.equalsIgnoreCase("end")) ||
                    (!_forward && s.equalsIgnoreCase("belief:"))) {

                if (tok.hasMoreTokens())
                {
                    String beliefName = tok.nextToken();

                    if (beliefName.equals("anacondadetectionbelief"))
                    {
                        synchronized(this)
                        {
                            _anacondaDetection = new AnacondaDetectionBelief("unknown-agent", _anacondaDetectionString);
                            _anacondaDetectionString = "";
                            if (beliefManager != null)
                                beliefManager.put (_anacondaDetection);
                        }
                    }
                    else if (beliefName.equals("biopoddetectionbelief"))
                    {
                        synchronized(this)
                        {
                            _bioDetection = new ParticleDetectionBelief("unknown-agent", _bioDetectionString);
                            _bioDetectionString = "";
                            if (beliefManager != null)
                                beliefManager.put (_bioDetection);
                        }
                    }
                    else if (beliefName.equals("circularorbitbelief"))
                    {
                        synchronized(this)
                        {
                            AbsolutePosition orbitCenter = new LatLonAltPosition(_circularOrbitLatitude, _circularOrbitLongitude, _circularOrbitAltitudeMSL);
                            _circularOrbitBelief = new CircularOrbitBelief("unkown-agent",
                                                                           orbitCenter,
                                                                           _circularOrbitRadius,
                                                                           true);

                            if (beliefManager != null)
                                beliefManager.put (_circularOrbitBelief);
                        }
                    }
                    else if (beliefName.equals("plumedetectionbelief"))
                    {
                        synchronized(this)
                        {
                        }
                    }
                }

                doneCollecting();
                return;
            }
            if (_collectPosition) {
                //example: name lat lon heading
                String agentID = s;
                double lat = Double.valueOf(tok.nextToken());
                double lon = Double.valueOf(tok.nextToken());
                double alt = Double.valueOf(tok.nextToken());
                double heading = Double.valueOf(tok.nextToken());
                PlayerPosition p = new PlayerPosition(lat,lon,heading,Classification.FRIENDLY,alt);
                AgentPositionBelief agentPosBelief = new AgentPositionBelief (agentID, new LatLonAltPosition (new Latitude (lat, Angle.DEGREES), new Longitude(lon, Angle.DEGREES), new Altitude (alt, Length.METERS)), new NavyAngle (heading, Angle.DEGREES));
                beliefManager.put (agentPosBelief);
                
                Vector<PlayerPosition> list = _agentPositions.get(agentID);
                if (list == null)
                    list = new Vector<PlayerPosition>();
                list.add(p);
                if (list.size() > _maxHistoricalPositions)
                {
                    list.remove(0);
                }

                synchronized(this)
                {
                    _agentPositions.put(agentID,list);
                }
            }
            else if (_collectTarget) {
                //example: name lat lon heading classification
                String targetID = s;
                double lat = Double.valueOf(tok.nextToken());
                double lon = Double.valueOf(tok.nextToken());
                int classification = Classification.VEHICLE_TARGET;
                if (tok.hasMoreTokens())
                    classification = Integer.valueOf(tok.nextToken());
                PlayerPosition p = new PlayerPosition(lat,lon,0.0,classification);
                Vector<PlayerPosition> list = _targetPositions.get(targetID);
                if (list == null)
                    list = new Vector<PlayerPosition>();
                list.add(p);

                synchronized(this)
                {
                    _targetPositions.put(targetID,list);
                }
            }
            else if (_beliefName.equals("clouddetectionbelief"))
            {
                if (_fieldNum == 0)
                {
                    _numCloudDetections = Integer.valueOf(tok.nextToken());
                }
                else
                {
                    int subFieldNum = (_fieldNum - 1) % 6;

                    if (subFieldNum == 1)
                    {
                        tok.nextToken(":");
                        _cloudDetectionLatitude = new Latitude(Double.valueOf(tok.nextToken()), Angle.DEGREES);
                    }
                    else if (subFieldNum == 2)
                    {
                        _cloudDetectionLongitude = new Longitude(Double.valueOf(tok.nextToken()), Angle.DEGREES);
                    }
                    else if (subFieldNum == 3)
                    {
                        tok.nextToken(":");
                        _cloudDetectionAltitudeMSL = new Altitude(Double.valueOf(tok.nextToken()), Length.METERS);
                    }
                    else if (subFieldNum == 4)
                    {
                        tok.nextToken();
                        _cloudDetectionStrength = Double.valueOf(tok.nextToken());
                        if (tok.countTokens() == 2)
                        {
                            tok.nextToken(" ");
                            _cloudDetectionSource = Short.valueOf(tok.nextToken());
                        }
                        else
                        {
                            _cloudDetectionSource = -1;
                        }
                    }
                    else if (subFieldNum == 5)
                    {
                        tok.nextToken(":");
                        String timestampString = tok.nextToken();
                        _cloudDetectionTimestamp_ms = Long.valueOf(timestampString.trim());
                        short id = 0;

                        CloudDetection cloudDetection;
                        if (_cloudDetectionSource >= 0)
                        {
                            cloudDetection = new CloudDetection(new LatLonAltPosition(_cloudDetectionLatitude, _cloudDetectionLongitude, _cloudDetectionAltitudeMSL), _cloudDetectionStrength, _cloudDetectionSource, id, (short)_cloudDetectionStrength, _cloudDetectionTimestamp_ms);
                        }
                        else
                        {
                            cloudDetection = new CloudDetection(new LatLonAltPosition(_cloudDetectionLatitude, _cloudDetectionLongitude, _cloudDetectionAltitudeMSL), _cloudDetectionStrength, CloudDetection.SOURCE_PARTICLE, id, (short)_cloudDetectionStrength, _cloudDetectionTimestamp_ms);
                        }

                        synchronized(this)
                        {
                            _cloudDetections.add(cloudDetection);

                            CloudDetectionBelief cloudBelief = new CloudDetectionBelief (WACSAgent.AGENTNAME, cloudDetection.getDetection(), cloudDetection.getScaledValue(), cloudDetection.getSource(), cloudDetection.getId(), cloudDetection.getRawValue(), cloudDetection.getTime());
                            if (beliefManager != null)
                                beliefManager.put (cloudBelief);


                        }
                    }
                }
            }
            else if (_beliefName.equals("biopoddetectionbelief"))
            {
                _bioDetectionString += " " + line + "\n";
            }
            else if (_beliefName.equals("plumedetectionbelief"))
            {
                synchronized(this)
                {
                    line = line.replaceFirst(" ", "\n ");
                    _plumeDetection = new PlumeDetectionBelief("unkown-agent", " " + line);
                }
            }
             else if (_beliefName.equals("anacondadetectionbelief"))
            {
                _anacondaDetectionString += " " + line + "\n";
            }
            else if (_beliefName.equals("circularorbitbelief"))
            {
                if (s.equals("lat:"))
                {
                    _circularOrbitLatitude = new Latitude(Double.parseDouble(tok.nextToken()), Angle.DEGREES);
                }
                else if (s.equals("lon:"))
                {
                    _circularOrbitLongitude = new Longitude(Double.parseDouble(tok.nextToken()), Angle.DEGREES);
                }
                else if (s.equals("alt:"))
                {
                    _circularOrbitAltitudeMSL = new Altitude(Double.parseDouble(tok.nextToken()), Length.METERS);
                }
                else if (s.equals("rad:"))
                {
                    _circularOrbitRadius = new Length(Double.parseDouble(tok.nextToken()), Length.METERS);
                }
            }
        }
        ++_fieldNum;
	}

	public void setForward() {
		_forward = true;
	}

	public void setBackward() {
		_forward = false;
	}

	public boolean step() {
		if (_forward)
			return stepForward();
		else
			return stepBackward();
	}

	public void restart() {
		reset();
		_forward = true;
		step();
	}

	public boolean stepForward() {
		String line = "";
		if (_position > _file.size() - 1)
			_position = _file.size() - 1;
		while (_position < _file.size() &&
				(line = _file.get(_position++)) != null && 
				!line.equalsIgnoreCase("end time")) 
		{
			evaluateLine(line);
		}
		if (_position > _file.size() - 1)
			return false;
		return true;
	}

	public boolean stepBackward() {
		String line = "";
		if (_position < 0)
			_position = 0;
		while (_position >= 0 && 
				(line = _file.get(_position--)) != null && 
				!line.equalsIgnoreCase("end time")) 
		{
			evaluateLine(line);
		}
		if (_position < 0)
			return false;
		return true;
	}

    public void setPlaybackPosition(int position)
    {
        if (position < _numMetricIndicies)
        {
            _position = position;
        }
    }

    public int getPlaybackPosition()
    {
        return _position;
    }

    public int getNumMetricIndicies()
    {
        return _numMetricIndicies;
    }

    public void clearDetections()
    {
        _cloudDetections.clear();
    }
}

