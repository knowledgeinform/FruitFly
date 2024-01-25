package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.TimeManagerFactory;
import edu.jhuapl.nstd.swarm.util.Config;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;


/**
 *
 * @author biggimh1
 */
public class TestCircularOrbitBelief extends Belief implements BeliefExternalizable
{

    public static final String BELIEF_NAME = "TestCircularOrbitBelief";
    //Center Position is Lat, Lon and Altitude (MSL)
    protected AbsolutePosition _centerPosition;
    
    protected Altitude _racetrackFinalAltitude;
    protected boolean _isRacetrack;
    protected LatLonAltPosition gimbleTarget;
            
    protected Length _radius;
    protected boolean _isClockwise;
    protected double _minimumSafeAltitude;
    protected boolean _isNew;
    
    
    //x and Y index in the dted map
    //private int _orbitX = 0, _orbitY = 0;
    //protected AbsolutePosition _dtedMapCenter;
    //private TreeMap<Integer, TreeMap<Integer, AltitudePair>> dtedMap;

    /*
     * Used when recreating this object in a different environment
     * Class fields must be initialized in readExternal
     */
    public TestCircularOrbitBelief()
    {
        super();
    }

    /**
     * 
     * Note: minimum safe altitude is enforced through safety box function getSafeCircularOrbit(CircularOrbitBelief)
     * @param agentId
     * @param centerPosition
     * @param radius
     * @param isClockwise 
     */
    public TestCircularOrbitBelief(final String agentId, final AbsolutePosition centerPosition, final Length radius, final boolean isClockwise)
    {
        super(agentId);

        initializeBelief(centerPosition, radius, isClockwise);
        
    }
    
    public TestCircularOrbitBelief(final String agentId, final AbsolutePosition centerPosition, final Length radius, final boolean isClockwise, double mps, double windSouth, double windWest)
    {
        this (agentId, centerPosition, radius, isClockwise, mps, windSouth, windWest, new Date (System.currentTimeMillis()));
    }
            
    //TODO make this the only option for a constructor
    public TestCircularOrbitBelief(final String agentId, final AbsolutePosition centerPosition, final Length radius, final boolean isClockwise, double mps, double windSouth, double windWest, Date time)
    {
        super(agentId);

        timestamp = time;
        initializeBelief(centerPosition, radius, isClockwise);
        
        //calculate radius with wind offest

        
        double radiusInMeters = getMinimumTurnRadiusMeters(mps, windSouth, windWest);
        
        if(radius.getDoubleValue(Length.METERS) >radiusInMeters){
            _radius = radius;
        }else{
            _radius = new Length(radiusInMeters, Length.METERS);
        }
      }

    public TestCircularOrbitBelief(final String agentId, Latitude lat1, Longitude lon1, Altitude altMslFinal, Altitude altMslStandoff, Length radius, boolean isClockwise,double velocity, double windSouth, double windWest, LatLonAltPosition gimbleTarget)
    {
        this (agentId, lat1, lon1, altMslFinal, altMslStandoff, radius, isClockwise, velocity, windSouth, windWest, new Date (System.currentTimeMillis()), gimbleTarget);
    }
    
    public TestCircularOrbitBelief(final String agentId, Latitude lat1, Longitude lon1, Altitude altMslFinal, Altitude altMslStandoff, Length radius, boolean isClockwise,double velocity, double windSouth, double windWest, Date time, LatLonAltPosition gimbleTarget)
    {
        super(agentId);
        timestamp = time;
        LatLonAltPosition centerPosition = new LatLonAltPosition(lat1, lon1,altMslStandoff);
        initializeBelief(centerPosition, radius, isClockwise);
        
        double radiusInMeters = this.getMinimumTurnRadiusMeters(velocity, windSouth, windWest);
        
        if(radius.getDoubleValue(Length.METERS) >radiusInMeters){
            _radius = radius;
        }else{
            _radius = new Length(radiusInMeters, Length.METERS);
        }
        
        _isRacetrack = true;
        _racetrackFinalAltitude = altMslFinal;
        this.gimbleTarget = gimbleTarget;
        
    }
    
    public CircularOrbitBelief asCircularOrbitBelief ()
    {
        return new CircularOrbitBelief(this.agentID, getPosition(), getRadius(), getIsClockwise(), getTimeStamp());
    }
    
    public RacetrackOrbitBelief asRacetrackOrbitBelief ()
    {
        return new RacetrackOrbitBelief(getPosition().asLatLonAltPosition().getLatitude(), getPosition().asLatLonAltPosition().getLongitude(), getFinalAltitude(), getPosition().asLatLonAltPosition().getAltitude(), getRadius(), getIsClockwise(), getTimeStamp());
    }
    
    public double getMinimumTurnRadiusMeters(double velocityMps, double windSouth, double windWest){
        double bankRadians = (Config.getConfig().getPropertyAsDouble("FlightControl.maxBankAngle_deg",20)*Math.PI*2.0)/360.0;
        velocityMps = Math.sqrt(Math.pow(windSouth, 2)+Math.pow(windWest, 2))+velocityMps;
        return Math.pow(velocityMps, 2)/(9.8*Math.tan(bankRadians));
    }
    
    private void initializeBelief(AbsolutePosition centerPosition, Length radius, boolean isClockwise){
        _centerPosition = centerPosition;
        //_dtedMapCenter = new LatLonAltPosition(_centerPosition.asLatLonAltPosition().getLatitude(), _centerPosition.asLatLonAltPosition().getLongitude(), _centerPosition.asLatLonAltPosition().getAltitude());
        _radius = radius;
        _isClockwise = isClockwise;
        timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
        _minimumSafeAltitude = Double.MAX_VALUE;
        //Convert feet to meters
        
        //dtedMap = new TreeMap();
        _isNew = true;
        _isRacetrack = false;
        _racetrackFinalAltitude = Altitude.ZERO;
    }

    /**
     * Returns the unique name for this belief type.
     * @return A unique name for this belief type.
     */
    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }

    @Override
    protected void addBelief(Belief b)
    {
        TestCircularOrbitBelief belief = (TestCircularOrbitBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
            _radius = belief._radius;
            _centerPosition = belief._centerPosition;
            _isClockwise = belief._isClockwise;
            _isNew = belief._isNew;
            //_dtedMapCenter = belief._dtedMapCenter;
            _minimumSafeAltitude = belief._minimumSafeAltitude;
            _racetrackFinalAltitude = belief._racetrackFinalAltitude;
            _isRacetrack = belief._isRacetrack;
            gimbleTarget = belief.gimbleTarget;
        }
    }

      @Override
    public byte[] serialize() throws IOException
    {
        setTransmissionTime();
        ByteArrayOutputStream baStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baStream);

        writeExternal(out);
        out.flush();
        out.close();
        return baStream.toByteArray();
    }

    /*
     * Not up to Date
     */
    public static Belief deserialize(InputStream iStream, Class clas) throws IOException
    {
        DataInputStream in = new DataInputStream(iStream);

        Belief belief;

        try
        {
            belief = (Belief) clas.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);

        return belief;
    }

    /*
     * Missing field that do not overlap with COB
     */
    @Override
    public void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);

        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(new byte[75]);
        //java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(new byte[51]);
        buffer.putDouble(_radius.getDoubleValue(Length.METERS));
        buffer.putDouble(_centerPosition.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES));
        buffer.putDouble(_centerPosition.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES));
        buffer.putDouble(_centerPosition.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS));
        if (gimbleTarget != null)
        {
            buffer.putDouble(gimbleTarget.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES));
            buffer.putDouble(gimbleTarget.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES));
            buffer.putDouble(gimbleTarget.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS));
        }
        else
        {
            buffer.putDouble(-1000);
            buffer.putDouble(-1000);
            buffer.putDouble(-1000);
        }
        buffer.putDouble(_racetrackFinalAltitude.getDoubleValue(Length.METERS));
        buffer.putDouble(_minimumSafeAltitude);
        buffer.put((byte)(_isClockwise ? 1 : 0));
        buffer.put((byte)(_isNew ? 1 : 0));
        buffer.put((byte)(_isRacetrack ? 1:0));

        out.write(buffer.array());
    }
    
    /*
     * Missing field that do not overlap with COB
     */
    @Override
    public void readExternal(DataInput in) throws IOException
    {
        try
        {
            super.readExternal(in);

            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(new byte[75]);
            //java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(new byte[51]);
            in.readFully(buffer.array());

            _radius = new Length(buffer.getDouble(), Length.METERS);
            double latitude_DEG = buffer.getDouble();
            double longitude_DEG = buffer.getDouble();
            double altitude_M = buffer.getDouble();

            _centerPosition = new LatLonAltPosition(new Latitude(latitude_DEG, Angle.DEGREES),
                                                    new Longitude(longitude_DEG, Angle.DEGREES),
                                                    new Altitude(altitude_M, Length.METERS));
               
            latitude_DEG = buffer.getDouble();
            longitude_DEG = buffer.getDouble();
            altitude_M = buffer.getDouble();
            if (latitude_DEG > -180 && longitude_DEG > -180)
            {
                gimbleTarget = new LatLonAltPosition(new Latitude(latitude_DEG, Angle.DEGREES),
                                                        new Longitude(longitude_DEG, Angle.DEGREES),
                                                        new Altitude(altitude_M, Length.METERS));
            }
//
            this._racetrackFinalAltitude = new Altitude(buffer.getDouble(),Length.METERS);
            this._minimumSafeAltitude = buffer.getDouble();
            _isClockwise = (buffer.get() == 0) ? false : true;
            _isNew = (buffer.get() == 0) ? false : true;
            _isRacetrack = (buffer.get() == 0) ? false : true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public Length getRadius()
    {
        return _radius;
    }

    public void setRadius(Length radius)
    {
        _radius = radius;
    }

    
    /*public AbsolutePosition getPosition()
    {
        return _centerPosition;
    }*/
    
    public AbsolutePosition getPosition()
    {
        return _centerPosition;
    }

    public void setPosition(AbsolutePosition centerPosition)
    {
        _centerPosition = centerPosition;
    }
    
    //public void setDTEDMapCenter(AbsolutePosition center){
      //  this._dtedMapCenter = center;
    //}

    public boolean getIsClockwise()
    {
        return _isClockwise;
    }

    public void setIsClockwise(final boolean isClockwise)
    {
        _isClockwise = isClockwise;
    }
    
    /*
     * update the timestamp to show modified value
     */
    public void use(){
        _isNew = false;
        this.timestamp = new Date(System.currentTimeMillis());
    }
    
    public boolean isNew(){
        return _isNew;
    }
    
    
    public double getMinimumAltitude() {
        return this._minimumSafeAltitude;
    }
   
    public boolean isRacetrack() {
        return this._isRacetrack;
    }

    public Altitude getFinalAltitude() {
        return _racetrackFinalAltitude;
    }

    public void setMinimumSafeAltitude(double minimumAltitude) {
        _minimumSafeAltitude = minimumAltitude;
    }
    
    public Altitude getRacetrackFinalAltitude(){
        return this._racetrackFinalAltitude;
    }
    
    public LatLonAltPosition getGimbleTarget(){
        return gimbleTarget;
    }
    
    /*
    public void gridTest(){
        RangeBearingHeightOffset offsetFromCenter = new RangeBearingHeightOffset(new Length(10,Length.METERS), NavyAngle.EAST,new Length(0,Length.METERS));
        LatLonAltPosition pos = this._centerPosition.translatedBy(offsetFromCenter).asLatLonAltPosition();
        PathNode p = this.createPathNodeFromLatLonAlt(pos, null);
        
        System.out.println("Grid pos x: "+ p.dtedMapX +" y: "+ p.dtedMapY);
    }
    */
    @Override
    public String toString(){
        String string;
        double lat = this._centerPosition.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES);
        double lon = this._centerPosition.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES);
        double alt = this._centerPosition.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);
        double radius = this._radius.getDoubleValue(Length.METERS);
               
        string =
                "Lat: " + lat
                +" Lon: " + lon
                +" Alt: " + alt
                +" radius: "+ radius
                +" minSafeAlt: "+ this._minimumSafeAltitude;
                //+" dted center: "+ this._dtedMapCenter;
        
        return string;
    }
    
     /**
     * Test program for finding a safe orbit functions
     * @param args -none   
     */
    public static void main(String[] args){
        /*
        //Circular orbit belief test position
        Latitude lat = new Latitude(40.1236069,Angle.DEGREES);
        Longitude lon = new Longitude(-113.2449,Angle.DEGREES);
        Altitude alt = new Altitude(1671, Length.METERS);
        //UAV test position
        Latitude uavLat = new Latitude(40.117749,Angle.DEGREES);
        Longitude uavLon = new Longitude(-113.252171,Angle.DEGREES);
        Altitude uavAlt = new Altitude(1507, Length.METERS);
        LatLonAltPosition uavPos = new LatLonAltPosition(uavLat,uavLon,uavAlt);
        TestCircularOrbitBelief testBelief = new TestCircularOrbitBelief("Test", new LatLonAltPosition(lat,lon,alt),new Length(500,Length.METERS),false);

        double doubleLat = lat.getDoubleValue(Angle.DEGREES);
        double doubleLon = lon.getDoubleValue(Angle.DEGREES);
        double doubleAlt = alt.getDoubleValue(Length.METERS);
        
        System.out.println("Lat: "+  doubleLat+" Lon "+doubleLon+" Alt: "+doubleAlt);
        
        lat = testBelief._centerPosition.asLatLonAltPosition().getLatitude();
        lon = testBelief._centerPosition.asLatLonAltPosition().getLongitude();
        alt = testBelief._centerPosition.asLatLonAltPosition().getAltitude();
        doubleLat = lat.getDoubleValue(Angle.DEGREES);
        doubleLon = lon.getDoubleValue(Angle.DEGREES);
        doubleAlt = alt.getDoubleValue(Length.METERS);
        
        System.out.println("Lat: "+  doubleLat+" Lon "+doubleLon+" Alt: "+doubleAlt);
        */
        
        /* create path node from lat lon alt position test
         */
        /*
         Latitude lat = new Latitude(0,Angle.DEGREES);
        Longitude lon = new Longitude(0,Angle.DEGREES);
        Altitude alt = new Altitude(0, Length.METERS);
        CircularOrbitBelief testBelief = new CircularOrbitBelief("Test", new LatLonAltPosition(lat,lon,alt),new Length(1,Length.METERS),false);
        
        testBelief.test();
        *
        */
        //CircularOrbitBelief.existsFeasiblePath(new PathNode(null, null, 10,10), new PathNode(null, null, 0,0), 0);
        //testSafePathFinding();
        
        
    }
    /*
    public static void testSafePathFinding(){
        //Circular orbit belief test position
        Latitude lat = new Latitude(40.1253162074935,Angle.DEGREES);
        Longitude lon = new Longitude(-113.26052472691936,Angle.DEGREES);
        Altitude alt = new Altitude(1960.4, Length.METERS);
        //Latitude lat = new Latitude(40.148841,Angle.DEGREES);
        //Longitude lon = new Longitude(-113.301650,Angle.DEGREES);
        //Altitude alt = new Altitude(2355, Length.METERS);
        //UAV test position
        Latitude uavLat = new Latitude(40.123330277777775,Angle.DEGREES);
        Longitude uavLon = new Longitude(-113.23306861111112,Angle.DEGREES);
        Altitude uavAlt = new Altitude(1623, Length.METERS);
        LatLonAltPosition uavPos = new LatLonAltPosition(uavLat,uavLon,uavAlt);
        TestCircularOrbitBelief testBelief = new TestCircularOrbitBelief("Test", new LatLonAltPosition(lat,lon,alt),new Length(425.0,Length.METERS),false);

               
        double doubleLat = lat.getDoubleValue(Angle.DEGREES);
        double doubleLon = lon.getDoubleValue(Angle.DEGREES);
        double doubleAlt = alt.getDoubleValue(Length.METERS);
        
        System.out.println("Lat: "+  doubleLat+" Lon "+doubleLon+" Alt: "+doubleAlt);
        
        testBelief.adjustOrbitCenterToSafeLocation(uavPos, null);
        
        lat = testBelief._centerPosition.asLatLonAltPosition().getLatitude();
        lon = testBelief._centerPosition.asLatLonAltPosition().getLongitude();
        alt = testBelief._centerPosition.asLatLonAltPosition().getAltitude();
        doubleLat = lat.getDoubleValue(Angle.DEGREES);
        doubleLon = lon.getDoubleValue(Angle.DEGREES);
        doubleAlt = alt.getDoubleValue(Length.METERS);
        
        System.out.println("Lat: "+  doubleLat+" Lon "+doubleLon+" Alt: "+doubleAlt);
        
        ArrayList<LatLonAltPosition> orbitPath = testBelief.planPathToOrbit(uavPos, null, NavyAngle.WEST.getDoubleValue(Angle.DEGREES));
        
        System.out.println("Done");
    }*/
    /*
    public static void beliefManagerSanity(){
        String name = System.getProperty("agent.name");
        try {
            BeliefManagerImpl belMgr = new BeliefManagerImpl(name);
            //BeliefManagerClient client = new BeliefManagerClient(belMgr);
            Latitude lat = new Latitude(40.1236069, Angle.DEGREES);
            Longitude lon = new Longitude(-113.2449, Angle.DEGREES);
            Altitude alt = new Altitude(1671, Length.METERS);


            TestCircularOrbitBelief testBelief1 = new TestCircularOrbitBelief("Test", new LatLonAltPosition(lat, lon, alt), new Length(500, Length.METERS), false);
            belMgr.put(testBelief1);

            TestCircularOrbitBelief getBelief = (TestCircularOrbitBelief) belMgr.get(TestCircularOrbitBelief.BELIEF_NAME);
            getBelief.use();

            belMgr.put(getBelief);

            TestCircularOrbitBelief testBelief2 = new TestCircularOrbitBelief("Test", new LatLonAltPosition(Latitude.ZERO, lon, alt), new Length(500, Length.METERS), false);

            Date tb2d = testBelief2.timestamp;
            Date gbd = getBelief.timestamp;

            //belMgr.put(testBelief2);
            HashMap<String, Belief> map = belMgr.beliefMap;
            while (!getBelief._isNew) {
                belMgr.update();
                belMgr.put(testBelief2);
                for (String s : map.keySet()) {
                    TestCircularOrbitBelief mapBel = (TestCircularOrbitBelief) map.get(s);
                    System.out.println(mapBel.toString());
                }

                getBelief = (TestCircularOrbitBelief) belMgr.get(TestCircularOrbitBelief.BELIEF_NAME);
                Thread.sleep(500);
            }
            System.out.println("Done");

        } catch (IOException ex) {
            Logger.getLogger(TestCircularOrbitBelief.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
        }
    }*/

    public void setIsNew(boolean b) {
        _isNew = b;
    }
}

    
    /**
     * Coverage algorithm from http://lifc.univ-fcomte.fr/~dedu/projects/bresenham/index.html
     * @param i one endpoint of "line segment"
     * @param j other endpoint of "line segment"
     * @return max alt along line
     * 
     */
    /*
    private double getMaxAltBetween2Nodes(PathNode endpoint1, PathNode endpoint2, SafetyBox safetyBox){
        //ArrayList<PathNode> list = new ArrayList<PathNode>();
        int i;               // loop counter 
        int ystep, xstep;    // the step on y and x axis 
        int error;           // the error accumulated during the increment 
        int errorprev;       // *vision the previous value of the error variable 
        int y = endpoint1.dtedMapY, x = endpoint1.dtedMapX;  // the line points 
        int x1 = endpoint1.dtedMapX, x2=endpoint2.dtedMapX, y1=endpoint1.dtedMapY,y2=endpoint2.dtedMapY;
        int ddy, ddx;        // compulsory variables: the double values of dy and dx 
        int dx = x2 - x1;
        int dy = y2 - y1;
        
        double maxAlt = 0;
        
        // NB the last point can't be here, because of its previous point (which has to be verified) 
        if (dy < 0) {
            ystep = -1;
            dy = -dy;
        } else {
            ystep = 1;
        }
        if (dx < 0) {
            xstep = -1;
            dx = -dx;
        } else {
            xstep = 1;
        }
        ddy = 2 * dy;  // work with double values for full precision 
        ddx = 2 * dx;
        if (ddx >= ddy) {  // first octant (0 <= slope <= 1) 
            // compulsory initialization (even for errorprev, needed when dx==dy) 
            errorprev = error = dx;  // start in the middle of the square 
            for (i = 0; i < dx; i++) {  // do not use the first point (already done) 
                x += xstep;
                error += ddy;
                if (error > ddx) {  // increment y if AFTER the middle ( > ) 
                    y += ystep;
                    error -= ddx;
                    // three cases (octant == right->right-top for directions below): 
                    if (error + errorprev < ddx) // bottom square also 
                    {
                        if(findGridAltitude(x, y-ystep, safetyBox)>maxAlt)maxAlt = findGridAltitude(x, y-ystep, safetyBox);
                        //list.add(new PathNode(null, null, x,y-ystep));
                        //POINT(y - ystep, x);
                    } else if (error + errorprev > ddx) // left square also 
                    {
                        if(findGridAltitude(x-xstep,y, safetyBox)>maxAlt)maxAlt = findGridAltitude(x-xstep,y, safetyBox);
                        //list.add(new PathNode(null, null, x-xstep,y));
                        //POINT(y, x - xstep);
                    } else {  // corner: bottom and left squares also 
                        if(findGridAltitude(x,y-ystep, safetyBox)>maxAlt)maxAlt = findGridAltitude(x,y-ystep, safetyBox);
                        if(findGridAltitude(x-xstep,y, safetyBox)>maxAlt)maxAlt = findGridAltitude(x-xstep,y, safetyBox);
                        //list.add(new PathNode(null, null, x,y-ystep));
                        //list.add(new PathNode(null, null, x-xstep,y));
                        //POINT(y - ystep, x);
                        //POINT(y, x - xstep);
                    }
                }
                if(findGridAltitude(x,y, safetyBox)>maxAlt)maxAlt = findGridAltitude(x,y, safetyBox);
                //list.add(new PathNode(null, null, x,y));
                //POINT(y, x);
                errorprev = error;
            }
        } else {  // the same as above 
            errorprev = error = dy;
            for (i = 0; i < dy; i++) {
                y += ystep;
                error += ddx;
                if (error > ddy) {
                    x += xstep;
                    error -= ddy;
                    if (error + errorprev < ddy) {
                        if(findGridAltitude(x-xstep,y, safetyBox)>maxAlt)maxAlt = findGridAltitude(x-xstep,y, safetyBox);
                        //list.add(new PathNode(null, null, x-xstep,y));
                        //POINT(y, x - xstep);
                    } else if (error + errorprev > ddy) {
                        if(findGridAltitude(x,y-ystep, safetyBox)>maxAlt)maxAlt = findGridAltitude(x,y-ystep, safetyBox);
                        //list.add(new PathNode(null, null, x,y-ystep));
                        //POINT(y - ystep, x);
                    } else {
                        if(findGridAltitude(x-xstep, y, safetyBox)>maxAlt)maxAlt = findGridAltitude(x-xstep, y, safetyBox);
                        if(findGridAltitude(x,y-ystep, safetyBox)>maxAlt)maxAlt = findGridAltitude(x,y-ystep, safetyBox);
                        //list.add(new PathNode(null, null, x-xstep,y));
                        //list.add(new PathNode(null, null, x,y-ystep));
                        //POINT(y, x - xstep);
                        //POINT(y - ystep, x);
                    }
                }
                if(findGridAltitude(x,y, safetyBox)>maxAlt)maxAlt = findGridAltitude(x,y, safetyBox);
                //list.add(new PathNode(null, null, x,y));
                //POINT(y, x);
                errorprev = error;
            }
        }
        // assert ((y == y2) && (x == x2));  // the last point (y2,x2) has to be the same with the last point of the algorithm 
        /*for(PathNode n: list){
            System.out.println("x: " + n.dtedMapX + " y:"+n.dtedMapY);
        }
        
        return maxAlt;
    }
    */