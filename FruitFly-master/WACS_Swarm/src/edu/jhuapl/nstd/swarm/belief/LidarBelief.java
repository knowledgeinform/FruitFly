package edu.jhuapl.nstd.swarm.belief;

import java.util.logging.*; 
 



import java.util.*; 
import java.util.concurrent.*; 
 
import java.awt.Point; 
import edu.jhuapl.jlib.math.*; 
import edu.jhuapl.jlib.collections.storage.*; 
import edu.jhuapl.jlib.math.position.*; 
import edu.jhuapl.nstd.swarm.*; 
import edu.jhuapl.nstd.swarm.util.*; 

import java.io.*; 
import java.beans.PropertyChangeEvent; 
import java.beans.PropertyChangeListener; 
 
public class LidarBelief extends Belief implements BeliefExternalizable { 

	public static final String SENSOR_TYPE = "LIDAR_BEAM";

	protected long _decayTime;

	public static final String BELIEF_NAME = "LidarBelief"; 
 
	protected transient boolean _swap = Config.getConfig().getPropertyAsBoolean("LidarBelief.swap", false); 
 
	private transient List<RegionTuple> _regions; 

	private transient boolean _selected = false;
 
	public LidarBelief() { 
		this("temp", new LinkedList<RegionTuple>()); 
	} 
 
	public LidarBelief(String agentID, Region r) { 
		super(agentID); 
		_regions = new LinkedList<RegionTuple>();
		timestamp = new Date(TimeManagerFactory.getTimeManager().getTime()); 
		_regions.add(new RegionTuple(r, timestamp.getTime()));
		propertyChange(null); 
	} 
 
	public LidarBelief(String agentID, List<RegionTuple> regions) { 
		super(agentID); 
		if (regions == null) { 
			throw new IllegalArgumentException("Map cannot be null"); 
		} 
		_regions = new LinkedList(regions); 
		timestamp = new Date(TimeManagerFactory.getTimeManager().getTime()); 
		propertyChange(null); 
	} 
 
	public void propertyChange(PropertyChangeEvent e) { 
		super.propertyChange(e); 
	} 
 
	public List<RegionTuple> getRegions() { 
		return _regions;
	} 

	public double getAltitude() {
		long now = System.currentTimeMillis();
		double toReturn = 0.0;
		double weights = 0.0;
		for (RegionTuple rt : _regions) {
			double weight =  0.5 + ((double)(now - rt._timestamp)) / (_decayTime * 2);
			weights += weight;
			toReturn += rt.getAltitude() * weight;
		}
		toReturn /= weights;
		return toReturn;
	}

	public Altitude getAltitude(LatLonAltPosition relativePos) {
		Region closestRegion = null;
		Length closestRange = null;
		for (RegionTuple rt : _regions) {
			for (AbsolutePosition pos : rt._region.getPositions()) {
				LatLonAltPosition lla = pos.asLatLonAltPosition();

				Length range = lla.getRangeTo(relativePos);
				if (closestRange == null || range.isLessThan(closestRange)) {
					closestRange = range;
					closestRegion = rt._region;
				}
			}
		}

		if (closestRegion == null)
			return null;

		return RegionBelief.getAltitude(closestRegion, relativePos);
	}
 
 
	//this function returns null if you should use the matrix passed in 
	//if the return in non null use that new matrix 
	public PrimitiveTypeGeocentricMatrix getSearchGoal(BeliefManager bm, PrimitiveTypeGeocentricMatrix matrix) { 
 
		if (_regions == null || _regions.isEmpty()) {
			return null;
		}
		boolean outside = false; 
 
		for (RegionTuple r : _regions) {
			for (AbsolutePosition pos : r._region.getPositions()) { 
				if (matrix.getPoint(pos) == null) { 
					outside = true; 
					break; 
				} 
			} 
		}
 
		if (outside) { 
			return getMatrix(bm); 
		} 
 
		for (int x=0; x<matrix.getXSize(); x++) { 
			for (int y=0; y<matrix.getYSize(); y++) { 
				matrix.set(x,y,SearchGoalBelief.NOT_GOAL_VALUE); 
			}
		}

		gridRegions(bm, matrix);
	/*
		NoGoBelief ngb = (NoGoBelief)bm.get(NoGoBelief.BELIEF_NAME);
		for (int x=0; x<matrix.getXSize(); x++) { 
			for (int y=0; y<matrix.getYSize(); y++) { 
				if (matrix.get(x, y) != SearchGoalBelief.NOT_GOAL_VALUE)
					continue;

				Region matrixRegion = matrix.getRegion(new java.awt.Point(x,y));
				for (RegionTuple r : _regions) {
					if (r._region.intersects(matrixRegion)) { 
						if (ngb == null || !ngb.isInNoGo(matrix.getPosition(x,y))) {
							matrix.set(x,y,SearchGoalBelief.GOAL_VALUE); 
						}
						else 
							matrix.set(x,y,SearchGoalBelief.NO_GO_VALUE);
						break;
					} 
				} 
			} 
		}
		*/
		return null; 
	} 

	private void gridRegions(BeliefManager bm, PrimitiveTypeGeocentricMatrix matrix) {
		NoGoBelief ngb = (NoGoBelief)bm.get(NoGoBelief.BELIEF_NAME);
		for (RegionTuple r : _regions) {
			Region bounds = r._region.getMinimumBoundingRectangle();
			Point topLeft = matrix.getPoint(
					new LatLonAltPosition(
						bounds.getMaximumLatitude(),
						bounds.getWesternmostLongitude(),
						Altitude.ZERO));
			Point lowerRight = matrix.getPoint(
					new LatLonAltPosition(
						bounds.getMinimumLatitude(),
						bounds.getEasternmostLongitude(),
						Altitude.ZERO));
			for (int x = topLeft.x; x <= lowerRight.x; x++) {
				for (int y = lowerRight.y; y <= topLeft.y; y++) {
					if (matrix.get(x, y) != SearchGoalBelief.NOT_GOAL_VALUE)
						continue;

					Region matrixRegion = matrix.getRegion(new java.awt.Point(x,y));
					if (r._region.intersects(matrixRegion)) { 
						if (ngb == null || !ngb.isInNoGo(matrix.getPosition(x,y))) {
							matrix.set(x,y,SearchGoalBelief.GOAL_VALUE); 
						}
						else 
							matrix.set(x,y,SearchGoalBelief.NO_GO_VALUE);
						break;
					} 
				}
			}
		}
	}

	private LatLonAltPosition getPosition(Latitude lat, Longitude lon, double delta) {
		return (LatLonAltPosition)((LatLonAltPosition) 
			new LatLonAltPosition(lat, lon,Altitude.ZERO). 
			translatedBy(new RangeBearingHeightOffset(new Length(delta, Length.METERS), NavyAngle.SOUTH, Length.ZERO))). 
			translatedBy(new RangeBearingHeightOffset(new Length(delta, Length.METERS), NavyAngle.WEST, Length.ZERO)); 
	}

	private LatLonAltPosition getPositionNorthEast(Latitude lat, Longitude lon, double delta) {
		return (LatLonAltPosition)((LatLonAltPosition) 
			new LatLonAltPosition(lat, lon,Altitude.ZERO). 
			translatedBy(new RangeBearingHeightOffset(new Length(delta, Length.METERS), NavyAngle.NORTH, Length.ZERO))). 
			translatedBy(new RangeBearingHeightOffset(new Length(delta, Length.METERS), NavyAngle.EAST, Length.ZERO)); 
	}
 
	public PrimitiveTypeGeocentricMatrix getMatrix(BeliefManager bm) { 
 
		if (_regions == null || _regions.isEmpty()) {
			return null;
		}
		double delta = Config.getConfig().getPropertyAsDouble("RegionBelief.delta", 00.0); 
		int gridRows = Config.getConfig().getPropertyAsInteger("agent.gridRows", 40); 
		int gridCols = Config.getConfig().getPropertyAsInteger("agent.gridCols", 40); 
 
		LatLonAltPosition swCorner = null;
		LatLonAltPosition neCorner = null;

		Region reg = getBoundingBox();

				// we have to keep the grid square. 
		double xDelta = delta;
		double yDelta = delta;

		Length xDist = new LatLonAltPosition(reg.getMinimumLatitude(), reg.getWesternmostLongitude(), Altitude.ZERO). 
			getRangeTo(new LatLonAltPosition(reg.getMinimumLatitude(), reg.getEasternmostLongitude(), Altitude.ZERO)); 
		Length yDist = new LatLonAltPosition(reg.getMaximumLatitude(), reg.getWesternmostLongitude(), Altitude.ZERO). 
			getRangeTo(new LatLonAltPosition(reg.getMinimumLatitude(), reg.getWesternmostLongitude(), Altitude.ZERO)); 
 
		if (xDist.isGreaterThan(yDist)) 
			yDelta += xDist.minus(yDist).getDoubleValue(Length.METERS) / 2.0;
		else
			xDelta += yDist.minus(xDist).getDoubleValue(Length.METERS) / 2.0;

		RangeBearingHeightOffset offsetX = new RangeBearingHeightOffset( 
				xDist.plus(new Length(xDelta * 2.0, Length.METERS)), NavyAngle.EAST, Length.ZERO); 
		RangeBearingHeightOffset offsetY = new RangeBearingHeightOffset( 
				yDist.plus(new Length(yDelta * 2.0, Length.METERS)), NavyAngle.NORTH, Length.ZERO); 

		swCorner = (LatLonAltPosition)((LatLonAltPosition) 
			new LatLonAltPosition(reg.getMinimumLatitude(), reg.getWesternmostLongitude(),Altitude.ZERO). 
			translatedBy(new RangeBearingHeightOffset(new Length(yDelta, Length.METERS), NavyAngle.SOUTH, Length.ZERO))). 
			translatedBy(new RangeBearingHeightOffset(new Length(xDelta, Length.METERS), NavyAngle.WEST, Length.ZERO)); 
		neCorner =  
			(LatLonAltPosition)((LatLonAltPosition)swCorner.translatedBy(offsetY).translatedBy(offsetX)); 


		//swCorner = getPosition(reg.getMinimumLatitude(), reg.getWesternmostLongitude(), delta);
		//neCorner = getPositionNorthEast(reg.getMaximumLatitude(), reg.getEasternmostLongitude(), delta);
		PrimitiveTypeGeocentricMatrix.Header header = new PrimitiveTypeGeocentricMatrix.Header( 
						swCorner, neCorner, gridRows, gridCols, 
						new Date()); 

		PrimitiveTypeGeocentricMatrix matrix = new PrimitiveTypeGeocentricMatrix(header); 

		gridRegions(bm, matrix);
		return matrix; 
	} 

	public Region getBoundingBox() {
		Latitude north = null;
		Latitude south = null;
		Longitude east = null;
		Longitude west = null;

		for (RegionTuple rt : _regions) {
			Region r = rt._region;
			Latitude rNorth = r.getMaximumLatitude();
			Latitude rSouth = r.getMinimumLatitude();
			Longitude rEast = r.getEasternmostLongitude();
			Longitude rWest = r.getWesternmostLongitude();

			if (north == null) {
				north = rNorth;
			}
			if (south == null) {
				south = rSouth;
			}
			if (west == null) {
				west = rWest;
			}
			if (east == null) {
				east = rEast;
			}

			if (rNorth.isNorthOf(north)) {
				north = rNorth;
			}
			if (rSouth.isSouthOf(south)) {
				south = rSouth;
			}
			if (rEast.isEastOf(east)) {
				east = rEast;
			}
			if (rWest.isWestOf(west)) {
				west = rWest;
			}


		}

		LatLonAltPosition swCorner = getPosition(south, west, 0.0);
		LatLonAltPosition neCorner = getPosition(north, east, 0.0);
		LatLonAltPosition seCorner = getPosition(south, east, 0.0);
		LatLonAltPosition nwCorner = getPosition(north, west, 0.0);

		List<AbsolutePosition> posList = new LinkedList<AbsolutePosition>();
		posList.add(swCorner);
		posList.add(seCorner);
		posList.add(neCorner);
		posList.add(nwCorner);

		return new Region(posList);

	}

	public synchronized void update() 
        {
            try
            {
		super.update();
		_decayTime = Config.getConfig().getPropertyAsLong("LidarBelief.decaytime", 1000);
		long now = System.currentTimeMillis();

		Iterator<RegionTuple> itr = _regions.iterator();
		while (itr.hasNext()) {
			RegionTuple rt = itr.next();
			if ((now - rt._timestamp) > _decayTime) {
				itr.remove();
			}
		}
            }
            catch (Exception e)
            {
                System.err.println ("Exception in update thread - caught and ignored");
                e.printStackTrace();
            }

	}
 
	public synchronized void addBelief(Belief b) { 
		LidarBelief lidarBelief = (LidarBelief)b; 
		List<RegionTuple> regions = lidarBelief.getRegions();
 
		if (regions == null) { 
			Logger.getLogger("GLOBAL").info("List was null, ignoring"); 
			return; 
		} 
 
		if (b.getTimeStamp().compareTo(timestamp) > 0) {
			timestamp = b.getTimeStamp(); 
 
			for (RegionTuple r : regions) { 
				if (!_regions.contains(r)) {
					_regions.add(r);
				}
			} 
		} else {
			for (RegionTuple r : regions) { 
				if (!_regions.contains(r)) {
					_regions.add(r);
				}
			} 
		}
	} 

	public synchronized void setSelected(boolean in) {
		_selected = in;
	}

	public boolean isSelected() {
		return _selected;
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
 
		LidarBelief belief = null; 
 
		try { 
			belief = (LidarBelief)clas.newInstance(); 
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
 
	protected synchronized int getListSize() { 
		int count = 0; 
		for (RegionTuple r : _regions) { 
			count += (8 + 2 + getRegionSize(r._region)); //timestamp + number of regions + actual region size 
		} 
		return count; 
	} 
 
	public String write() {
		StringBuffer buf = new StringBuffer();
		buf.append("selected: " + isSelected() + "\n");
		for (RegionTuple rt : _regions) {
			buf.append("region:\n" + rt.write() + "\n");
		}
		return buf.toString();
	}

	public void readExternal(DataInput in) throws IOException { 
		try { 
			super.readExternal(in); 
			int size = (int)in.readShort(); 
			int numRegions = (int)in.readShort(); 
 
			int isSelected = (int)in.readByte();
			_selected = (isSelected == 1) ? true : false;

			byte[] data = new byte[size]; 
			in.readFully(data); 
 
			_regions = new LinkedList<RegionTuple>();
			int index = 0; 
			for (int i=0; i<numRegions; i++) { 
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
				_regions.add(new RegionTuple(new Region(region), timestamp));

			} 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
	} 
 
	public synchronized void writeExternal(DataOutput out) throws IOException { 
		super.writeExternal(out); 
		int listSize = getListSize(); 
		byte[] data = new byte[2 + 2 + 1 + listSize]; 
		int index = 0; 
		index = ByteManipulator.addShort(data, (short)listSize, index, _swap); 
		index = ByteManipulator.addShort(data, (short)_regions.size(), index, _swap); 
		index = ByteManipulator.addByte(data, (_selected) ? (byte)1 : (byte) 0, index, _swap);
 
		for (RegionTuple r : _regions) { 
			index = ByteManipulator.addLong(data, r._timestamp, index, _swap);
			index = ByteManipulator.addShort(data, (short)r._region.getPositions().length, index, _swap); 
			for (AbsolutePosition pos : r._region.getPositions()) { 
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
