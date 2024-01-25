package edu.jhuapl.nstd.swarm.belief;

import java.util.logging.*; 
 



import java.util.*; 
import java.util.concurrent.*; 
 
import edu.jhuapl.jlib.math.*; 
import edu.jhuapl.jlib.collections.storage.*; 
import edu.jhuapl.jlib.math.position.*; 
import edu.jhuapl.nstd.swarm.*; 
import edu.jhuapl.nstd.swarm.util.*; 

import java.io.*; 
import java.beans.PropertyChangeEvent; 
import java.beans.PropertyChangeListener; 
 
public class RegionBelief extends Belief implements BeliefExternalizable { 
	public static final String BELIEF_NAME = "RegionBelief"; 
 
	protected String _selected = null; 
	protected Date _selectionTime = null;
	protected transient boolean _manual = false;
	protected transient boolean _swap = Config.getConfig().getPropertyAsBoolean("RegionBelief.swap", false); 
 
	protected boolean _hasSelection = false;

	//sensor name to region 
	private transient ConcurrentHashMap<String, RegionTuple> _map; 
 
	public RegionBelief() { 
		this("temp"); 
	} 
 
	public RegionBelief(String agentID) { 
		super(agentID); 
		_map = new ConcurrentHashMap<String, RegionTuple>(); 
		timestamp = new Date(TimeManagerFactory.getTimeManager().getTime()); 
		propertyChange(null); 
	} 
 
	// WARNING: this only works locally, not accross the network!
	public boolean hasSelection() {
		return _hasSelection;
	}

	public synchronized void setSelected(String in) { 
		setSelected(in, false);
	} 

	public synchronized void setSelected(String in, boolean manual) {
		_hasSelection = true;
		_selected = in;
		_manual = manual;
		_selectionTime = new Date();
	}
 
	public String getSelected() { 
		return _selected; 
	} 

	public boolean isManual() {
		return _manual;
	}

	public Date getSelectionTime() {
		return _selectionTime;
	}

	public Altitude getMinimumAltitude(String name) {
		RegionTuple r = _map.get(name);
		if (r == null)
			return null;
		return getMinimumAltitude(r._region);
	}

	protected Altitude getMinimumAltitude(Region r) {
		Altitude min = Altitude.POSITIVE_INFINITY;
		for (AbsolutePosition pos : r.getPositions()) {
			Altitude alt = pos.asLatLonAltPosition().getAltitude();
			if (alt.isLowerThan(min)) {
				min = alt;
			}
		}

		if (min.equals(Altitude.POSITIVE_INFINITY))
			return null;

		return min;
	}

	public Altitude getMaximumAltitude(String name) {
		RegionTuple r = _map.get(name);
		if (r == null)
			return null;
		return getMaximumAltitude(r._region);
	}

	public Altitude getMaximumAltitude(Region r) {
		Altitude max = Altitude.NEGATIVE_INFINITY;
		for (AbsolutePosition pos : r.getPositions()) {
			Altitude alt = pos.asLatLonAltPosition().getAltitude();
			if (alt.isHigherThan(max)) {
				max = alt;
			}
		}

		if (max.equals(Altitude.NEGATIVE_INFINITY))
			return null;

		return max;
	}


	public Altitude getAltitude(String name) {
		RegionTuple r = _map.get(name);
		if (r == null)
			return null;
		// we expect each region to only have a min and max and no intermediate
		return getMinimumAltitude(r._region).plus(getMaximumAltitude(r._region).asLength()).times(0.5);
	}

	public Altitude getAltitude(String name, LatLonAltPosition relativePos) {
		RegionTuple r = _map.get(name);
		if (r == null)
			return null;
		return getAltitude(r._region, relativePos);
	}

	public static Altitude getAltitude(Region r, LatLonAltPosition relativePos) {
		// find the closest point at high alt and low alt
		// find out if you're between them or beyond
		LatLonAltPosition closestHigh = null;
		Length highRange = Length.POSITIVE_INFINITY;
		LatLonAltPosition closestLow = null;
		Length lowRange = Length.POSITIVE_INFINITY;
		for (AbsolutePosition pos : r.getPositions()) {
			LatLonAltPosition lla = pos.asLatLonAltPosition();
			Length range = null;
			if (closestLow == null || lla.getAltitude().isLowerThan(closestLow.getAltitude())
				|| (lla.getAltitude().equals(closestLow.getAltitude()) &&
					(range = lla.getRangeTo(relativePos)).isLessThan(lowRange)))
			{
				closestLow = lla;
				if (range == null)
					range = lla.getRangeTo(relativePos);
				lowRange = range;
			}

			if (closestHigh == null || lla.getAltitude().isHigherThan(closestHigh.getAltitude()) 
					|| (lla.getAltitude().equals(closestHigh.getAltitude()) &&
						(range = lla.getRangeTo(relativePos)).isLessThan(highRange))) 
			{
				closestHigh = lla;
				if (range == null)
					range = lla.getRangeTo(relativePos);
				highRange = range;
			}
		}

		Logger.getLogger("GLOBAL").info("ch: " + closestHigh.getAltitude().getDoubleValue(Length.METERS) + " cl: " + closestLow.getAltitude().getDoubleValue(Length.METERS));

		if (closestHigh.getAltitude().equals(closestLow.getAltitude())) {
			Logger.getLogger("GLOBAL").info("warning: region with no change in elevation");
			return closestHigh.getAltitude();
		}

		// h---ch\ 
		// |    |ht\  hr 
		// |    |    \ 
		// |  rr|     lla
		// |    |lt /   lr
		// l---cl/

		double rr = closestHigh.getRangeTo(closestLow).getDoubleValue(Length.METERS);
		double hr = highRange.getDoubleValue(Length.METERS);
		double lr = lowRange.getDoubleValue(Length.METERS);
		double ratio = (hr * hr + rr * rr - lr * lr) / (2.0 * hr * rr);
		if (ratio > 1.0) ratio = 1.0;
		double highTheta = Math.acos(ratio);
		ratio = (rr * rr + lr * lr - hr * hr) / (2.0 * rr * lr);
		if (ratio > 1.0) ratio = 1.0;
		double lowTheta = Math.acos(ratio);

		if (rr <= 0.0) {
			Logger.getLogger("GLOBAL").info("warning: region with no depth");
			return closestHigh.getAltitude();
		}

		

		Logger.getLogger("GLOBAL").info("rr: " + rr + " hr: " + hr + " lr: " + lr + " ht: " + highTheta + " lt: " + lowTheta);
		if (highTheta > Math.PI / 2.0) {
			return closestHigh.getAltitude();
		} else if (lowTheta > Math.PI / 2.0) {
			return closestLow.getAltitude();
		} else {
			double rrBase = hr * Math.cos(highTheta);
			Logger.getLogger("GLOBAL").info("rrBase: " + rrBase);
			Altitude alt = closestHigh.getAltitude().times((rr - rrBase) / rr).plus(
					closestLow.getAltitude().times(rrBase / rr).asLength());
			return alt;
		}
	}
 
	public RegionBelief(String agentID, HashMap<String, RegionTuple> map) { 
		super(agentID); 
		if (map == null) { 
			throw new IllegalArgumentException("Map cannot be null"); 
		} 
		_map = new ConcurrentHashMap(map); 
		timestamp = new Date(TimeManagerFactory.getTimeManager().getTime()); 
		propertyChange(null); 
	} 
 
	public void propertyChange(PropertyChangeEvent e) { 
		super.propertyChange(e); 
	} 
 
	public Region getRegion(String name) { 
		RegionTuple r = _map.get(name);
		if (r == null) return null;
		return r._region; 
	} 
 
	public long getRegionTime(String name) { 
		RegionTuple r = _map.get(name);
		if (r == null) return 0;
		return r._timestamp; 
	} 

	public ConcurrentHashMap<String, RegionTuple> getMap() { 
		return _map; 
	} 
 
	public Region getRegion(AbsolutePosition pos) { 
		for (RegionTuple reg : _map.values()) { 
			if (reg._region.contains(pos))  
				return reg._region; 
		} 
		return null; 
	} 
 
	public String getRegionName(Region reg) { 
		for (String name : _map.keySet()) { 
			if (reg.equals(_map.get(name)._region)) 
				return name; 
		} 
		return null; 
	} 
 
	//this function returns null if you should use the matrix passed in 
	//if the return in non null use that new matrix 
	public PrimitiveTypeGeocentricMatrix getSearchGoal(BeliefManager bm, String name, PrimitiveTypeGeocentricMatrix matrix) { 
		RegionTuple r = _map.get(name); 
		if (r == null) 
			return null; 

		return getSearchGoal(bm, r._region, matrix, Config.getConfig().getPropertyAsDouble("RegionBelief.delta", 00.0)); 
	} 

	public static PrimitiveTypeGeocentricMatrix getSearchGoal(BeliefManager bm, Region r, PrimitiveTypeGeocentricMatrix matrix) {
		return getSearchGoal(bm, r, matrix, Config.getConfig().getPropertyAsDouble("RegionBelief.delta", 0.0));
	}

	public static PrimitiveTypeGeocentricMatrix getSearchGoal(BeliefManager bm, Region r, PrimitiveTypeGeocentricMatrix matrix, double delta) { 
		return getSearchGoal(bm, r, matrix, delta, true);
	}

	public static PrimitiveTypeGeocentricMatrix getSearchGoal(BeliefManager bm, Region r, PrimitiveTypeGeocentricMatrix matrix, double delta, boolean replace) { 
		boolean outside = false; 
 
		if (matrix == null) {
			outside = true;
		} else {
			for (AbsolutePosition pos : r.getPositions()) { 
				if (matrix.getPoint(pos) == null) { 
					outside = true; 
					break; 
				} 
			} 
		}
 
		if (outside) { 
			return getMatrix(bm, r, delta); 
		} 
 
		NoGoBelief ngb = (NoGoBelief)bm.get(NoGoBelief.BELIEF_NAME);
		int goalCount = 0;
		for (int x=0; x<matrix.getXSize(); x++) { 
			for (int y=0; y<matrix.getYSize(); y++) { 
				if (r.intersects(matrix.getRegion(new java.awt.Point(x,y)))) { 
					if (ngb == null || !ngb.isInNoGo(matrix.getPosition(x,y))) {
						matrix.set(x,y,SearchGoalBelief.GOAL_VALUE); 
						goalCount++;
					}
					else 
						matrix.set(x,y,SearchGoalBelief.NO_GO_VALUE);
				} 
				else if (replace) { 
					matrix.set(x,y,SearchGoalBelief.NOT_GOAL_VALUE); 
				} 
			} 
		} 

		
		if (goalCount == 0)
			matrix = getMatrix(bm, r, delta);
		if (matrix.getMaximum().value <= 0.0)
			matrix = getMatrix(bm, r, delta / 2.0);
		if (matrix.getMaximum().value <= 0.0)
			matrix = getMatrix(bm, r, delta / 4.0);

		return null; 
	}
 
	public static PrimitiveTypeGeocentricMatrix getMatrix(BeliefManager bm, Region r, double delta) { 
		int gridRows = Config.getConfig().getPropertyAsInteger("agent.gridRows", 40); 
		int gridCols = Config.getConfig().getPropertyAsInteger("agent.gridCols", 40); 
 
		// we have to keep the grid square. 
		double xDelta = delta;
		double yDelta = delta;

		Length xDist = new LatLonAltPosition(r.getMinimumLatitude(), r.getWesternmostLongitude(), Altitude.ZERO). 
			getRangeTo(new LatLonAltPosition(r.getMinimumLatitude(), r.getEasternmostLongitude(), Altitude.ZERO)); 
		Length yDist = new LatLonAltPosition(r.getMaximumLatitude(), r.getWesternmostLongitude(), Altitude.ZERO). 
			getRangeTo(new LatLonAltPosition(r.getMinimumLatitude(), r.getWesternmostLongitude(), Altitude.ZERO)); 
 
		if (xDist.isGreaterThan(yDist)) 
			yDelta += xDist.minus(yDist).getDoubleValue(Length.METERS) / 2.0;
		else
			xDelta += yDist.minus(xDist).getDoubleValue(Length.METERS) / 2.0;

		RangeBearingHeightOffset offsetX = new RangeBearingHeightOffset( 
				xDist.plus(new Length(xDelta * 2.0, Length.METERS)), NavyAngle.EAST, Length.ZERO); 
		RangeBearingHeightOffset offsetY = new RangeBearingHeightOffset( 
				yDist.plus(new Length(yDelta * 2.0, Length.METERS)), NavyAngle.NORTH, Length.ZERO); 

		LatLonAltPosition swCorner = (LatLonAltPosition)((LatLonAltPosition) 
			new LatLonAltPosition(r.getMinimumLatitude(), r.getWesternmostLongitude(),Altitude.ZERO). 
			translatedBy(new RangeBearingHeightOffset(new Length(yDelta, Length.METERS), NavyAngle.SOUTH, Length.ZERO))). 
			translatedBy(new RangeBearingHeightOffset(new Length(xDelta, Length.METERS), NavyAngle.WEST, Length.ZERO)); 
		LatLonAltPosition neCorner =  
			(LatLonAltPosition)((LatLonAltPosition)swCorner.translatedBy(offsetY).translatedBy(offsetX)); 
		PrimitiveTypeGeocentricMatrix.Header header = new PrimitiveTypeGeocentricMatrix.Header( 
						swCorner, neCorner, gridRows, gridCols, 
						new Date()); 
 
		int count = 0;
		int minGoalCount = Config.getConfig().getPropertyAsInteger("regionBelief.minGoalCount", 8);
		PrimitiveTypeGeocentricMatrix matrix = new PrimitiveTypeGeocentricMatrix(header); 
		/*for (int x=0; x<matrix.getXSize(); x++) { 
			for (int y=0; y<matrix.getYSize(); y++) { 
				if (r.contains(matrix.getPosition(x,y))) { 
					matrix.set(x,y,SearchGoalBelief.GOAL_VALUE); 
					count++;
				} 
			} 
		} */

		//if (count < minGoalCount) {
		NoGoBelief ngb = (NoGoBelief)bm.get(NoGoBelief.BELIEF_NAME);
			for (int x=0; x<matrix.getXSize(); x++) {
				for (int y=0; y<matrix.getYSize(); y++) {
					if (r.intersects(matrix.getRegion(new java.awt.Point(x,y)))) {
						if (ngb == null || !ngb.isInNoGo(matrix.getPosition(x,y))) {
							matrix.set(x,y,SearchGoalBelief.GOAL_VALUE); 
						}
						else
							matrix.set(x,y,SearchGoalBelief.NO_GO_VALUE);
					}
				}
			}
		//}

		//do an intersection test if not successful enough
		return matrix; 
	} 
 
	public synchronized void addBelief(Belief b) { 
		RegionBelief regionBelief = (RegionBelief)b; 
		ConcurrentHashMap<String, RegionTuple> newMap = regionBelief.getMap(); 
 
		if (newMap == null) { 
			Logger.getLogger("GLOBAL").info("Map was null, ignoring"); 
			return; 
		} 
 
		if (_selectionTime == null || (regionBelief.getSelectionTime() != null &&
					regionBelief.getSelectionTime().compareTo(_selectionTime) > 0)) 
		{
			_hasSelection = regionBelief.hasSelection();
			_selected = regionBelief.getSelected(); 
			_selectionTime = regionBelief.getSelectionTime();
		}

		if (b.getTimeStamp().compareTo(timestamp) > 0) {
			timestamp = b.getTimeStamp(); 
			_manual = regionBelief.isManual();
			for (String key : newMap.keySet()) { 
				_map.put(key, newMap.get(key)); 
			} 
		} else {
			for (String key : newMap.keySet()) { 
				if (!_map.contains(key))
					_map.put(key, newMap.get(key)); 
			} 
		}
	} 
 
	public String getName() {  
		return BELIEF_NAME; 
	} 
 
	public byte[] serialize() throws IOException { 
		setTransmissionTime(); 
		ByteArrayOutputStream baStream = new ByteArrayOutputStream(); 
		DataOutputStream out = new DataOutputStream(baStream); 
		 
		writeExternal(out); 
		out.flush(); 
		out.close(); 
		return baStream.toByteArray(); 
	} 
 
	public static Belief deserialize(InputStream iStream, Class clas) throws IOException { 
		DataInputStream in = new DataInputStream(iStream); 
 
		RegionBelief belief = null; 
 
		try { 
			belief = (RegionBelief)clas.newInstance(); 
		} catch (Exception e) { 
			e.printStackTrace(); 
			return null; 
		} 
 
		belief.readExternal(in); 
 
		return belief; 
	} 
 
	protected int getRegionSize(Region r) { 
		return r.getPositions().length * 3 * 4; 
	} 
 
	protected synchronized int getMapSize() { 
		int count = 0; 
		for (String s : _map.keySet()) { 
			count += (2 + getRegionSize(_map.get(s)._region)); //number of regions + actual region size 
			count += (2 + s.length()); 
			count += 8;
		} 
		return count; 
	} 
 
	public String write() {
		StringBuffer buf = new StringBuffer();
		buf.append("selected: " + getSelected() + "\n");
		for (String key : _map.keySet()) {
			buf.append(key + "\n");
			RegionTuple r = _map.get(key);
			for (AbsolutePosition pos : r._region.getPositions()) {
				buf.append(pos.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES) + " " + 
						pos.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES) + 
						pos.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS) + "\n");
			}
		}
		return buf.toString();
	}

	public void readExternal(DataInput in) throws IOException { 
		try { 
			super.readExternal(in); 
			int size = (int)in.readShort(); 
			int numKeys = (int)in.readShort(); 
			byte hasSelectedByte = in.readByte();
			byte manualByte = in.readByte();
			_hasSelection = (hasSelectedByte == 0) ? false : true;
			_manual = (manualByte == 0) ? false : true;
			_selected = ByteManipulator.getString(in, _swap);
			byte[] data = new byte[8]; 
			in.readFully(data); 
			_selectionTime = new Date(ByteManipulator.getLong(data, 0, _swap));

			if (_selected.equals("")) 
				_selected = null;
 
			data = new byte[size]; 
			in.readFully(data); 
 
			_map = new ConcurrentHashMap<String, RegionTuple>(); 
			int index = 0; 
			for (int i=0; i<numKeys; i++) { 
				String key = ByteManipulator.getString(data, index, _swap); 
				index += (key.length() + 2); 
				long timestamp = ByteManipulator.getLong(data, index, _swap);
				index += 8;
				int numPoints = (int)ByteManipulator.getShort(data, index, _swap); 
				index += 2; 
 
				List<AbsolutePosition> region = new LinkedList<AbsolutePosition>(); 
				//create the region 
				for (int c=0; c<numPoints; c++) { 
					double lat = (double)ByteManipulator.getFloat(data, index, _swap); 
					index += 4; 
					double lon = (double)ByteManipulator.getFloat(data, index, _swap); 
					index += 4; 
					double alt = (double)ByteManipulator.getFloat(data, index, _swap);
					index += 4;
					region.add(new LatLonAltPosition(new Latitude(lat,Angle.DEGREES), 
								new Longitude(lon,Angle.DEGREES), 
								new Altitude(alt, Length.METERS))); 
				} 
				_map.put(key, new RegionTuple(new Region(region),timestamp)); 
			} 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
	} 
 
	public synchronized void writeExternal(DataOutput out) throws IOException { 
		super.writeExternal(out); 
		int mapSize = getMapSize(); 
		int selectedSize = 8 + 2 + ((_selected != null) ? _selected.length() : 0);
		byte[] data = new byte[2 + 2 + 1 + 1 + selectedSize + mapSize]; 
		int index = 0; 
		index = ByteManipulator.addShort(data, (short)mapSize, index, _swap); 
		index = ByteManipulator.addShort(data, (short)_map.keySet().size(), index, _swap); 
		index = ByteManipulator.addByte(data, (_hasSelection) ? (byte)1 : (byte)0, index, _swap); 
		index = ByteManipulator.addByte(data, (_manual) ? (byte)1 : (byte)0, index, _swap); 

		if (_selected == null) {
			index = ByteManipulator.addString(data, "", index, _swap);
			index = ByteManipulator.addLong(data, 0L, index, _swap);
		} else {
			index = ByteManipulator.addString(data, _selected, index, _swap);
			index = ByteManipulator.addLong(data, _selectionTime.getTime(), index, _swap);
		}
 
		for (String s : _map.keySet()) { 
			index = ByteManipulator.addString(data, s, index, _swap); 
			index = ByteManipulator.addLong(data, _map.get(s)._timestamp, index, _swap);
			index = ByteManipulator.addShort(data, (short)_map.get(s)._region.getPositions().length, index, _swap); 
			for (AbsolutePosition pos : _map.get(s)._region.getPositions()) { 
				//lat 
				index = ByteManipulator.addFloat(data, (float)pos.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES),  
						index, _swap); 
				//lon 
				index = ByteManipulator.addFloat(data, (float)pos.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES),  
						index, _swap); 
				//alt
				index = ByteManipulator.addFloat(data, (float)pos.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS),  
						index, _swap); 

			} 
		} 
 
		out.write(data); 
 
	} 
 
 
} 
