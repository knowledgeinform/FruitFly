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
 
// File created: Tue Nov	9 13:10:34 1999 
 
package edu.jhuapl.nstd.swarm.belief;

 
import java.lang.*; 

import java.util.*; 
import edu.jhuapl.jlib.math.*; 
import edu.jhuapl.jlib.math.position.*; 
import edu.jhuapl.nstd.swarm.*; 
import edu.jhuapl.nstd.swarm.util.*; 

import java.io.*; 
import java.awt.Point; 
import java.beans.PropertyChangeEvent; 
 
/** 
 * This is the class that stores information about which cells should be searched. 
 * 
 * <P>UNCLASSIFIED 
 * @author Chad Hawthorne ext. 3728 
 */ 
 
public class CloudBeliefMatrix extends Belief implements BeliefExternalizable { 
 
	private static final long serialVersionUID = 4589867831523215946L;
	 
	private PrimitiveTypeGeocentricMatrix matrix;
    private PrimitiveTypeGeocentricMatrix cmatrix;
	/** 
	 * The value that represents a cell that should be searched. 
	 */	 
	public static final float DETECT_VALUE = 1.0f; 
	/** 
	 * The value that represents a cell that should not be searched. 
	 */	 
	public static final float NOT_DETECT_VALUE = 0.0f; 
 
 
	/** 
	 * The unique name of this belief type. 
	 */	 
	public static final String BELIEF_NAME = "CloudBeliefMatrix"; 
	 
	/** 
	 * The value to multiply the grid by every timestep to degrade the belief. 
	 */	 
	public Unitless degrade = null; 
 
	protected long _lastDetection;

 
	/** 
	 * Empty Constructor. 
	 */	 
	public CloudBeliefMatrix()
    {
			degrade = new Unitless(1.0 - Config.getConfig().getPropertyAsDouble("cloudDecay", 0.0)); 
	} 
 
	/** 
	 * Constructor that creates a matrix using Agent.createCartesianMatrix. All values are initially set to 0.0. 
	 * @param agentID The id of the agent creating this matrix. 
	 */	 
	public CloudBeliefMatrix(String agentID){ 
		super(agentID); 
		matrix = GridFactory.createCartesianMatrix();
        cmatrix = GridFactory.createCartesianMatrix();
		timestamp = new Date(System.currentTimeMillis());
		resetMatrix(); 
		degrade = new Unitless(1.0 -	
							 Config.getConfig().getPropertyAsDouble("cloudDecay", 0.0));
		Config.getConfig().addPropertyChangeListener(this); 
	} 


    public CloudBeliefMatrix(String agentID, AbsolutePosition position, double value)
    {
        this(agentID,  position,  value, 0.0);
    }
	 
	/** 
	 * This constructor is used to set the searchedness grid to reflect that some area has been searched. 
	 * @param agentID The id of the agent that created this belief. 
	 * @param position The position of the agent to apply a new "search" for. 
	 * @param searchednessTable The existing searchedness table. This will be modified to reflect a new search at the position and bearing passed in as a parameter. 
	 */	 
	public CloudBeliefMatrix(String agentID, AbsolutePosition position, double value, double color)
	{ 
		super(agentID); 
		matrix = GridFactory.createCartesianMatrix();
        cmatrix = GridFactory.createCartesianMatrix();
		resetMatrix(); 
		timestamp = new Date(System.currentTimeMillis());
		degrade = new Unitless(1.0 -	
						Config.getConfig().getPropertyAsDouble("cloudDecay", 0.0)); 
		Point p = matrix.getPoint(position); 
		if (p != null)
        {
			matrix.set(p, (float)value);
            cmatrix.set(p, (float)color);
		} 
		Config.getConfig().addPropertyChangeListener(this); 
		_lastDetection = System.currentTimeMillis();
	}

    public CloudBeliefMatrix(String agentID, AbsolutePosition position, double value,
			PrimitiveTypeGeocentricMatrix template)
    {
        this( agentID,  position,  value,template, 0.0);
    }

	public CloudBeliefMatrix(String agentID, AbsolutePosition position, double value,
			PrimitiveTypeGeocentricMatrix template, double color)

	{ 
		super(agentID); 
		
		if (template.getPoint(position) == null) {
			int gridRows = template.getYSize();
			int gridCols = template.getXSize();
	 
			LatLonAltPosition swCorner = template.getSWCorner();
			LatLonAltPosition neCorner = template.getNECorner();

			RangeBearingHeightOffset offset = swCorner.getRangeBearingHeightOffsetTo(neCorner);
			offset = new RangeBearingHeightOffset(offset.getRange().times(0.5), offset.getBearing(), offset.getHeight());
			RangeBearingHeightOffset moveTo = swCorner.translatedBy(offset).getRangeBearingHeightOffsetTo(position);

			swCorner = swCorner.translatedBy(moveTo).asLatLonAltPosition();
			neCorner = neCorner.translatedBy(moveTo).asLatLonAltPosition();

			PrimitiveTypeGeocentricMatrix.Header header = new PrimitiveTypeGeocentricMatrix.Header( 
							swCorner, neCorner, gridRows, gridCols, 
							new Date()); 
 
			matrix = new PrimitiveTypeGeocentricMatrix(header);
            cmatrix = new PrimitiveTypeGeocentricMatrix(header);
		} else {
			matrix = new PrimitiveTypeGeocentricMatrix(template.getHeader());
            cmatrix = new PrimitiveTypeGeocentricMatrix(template.getHeader());
		}

		degrade = new Unitless(1.0 -	
						Config.getConfig().getPropertyAsDouble("cloudDecay", 0.0)); 
		timestamp = new Date(System.currentTimeMillis());
		Point p = matrix.getPoint(position); 
		if (p != null){ 
			matrix.set(p, (float)value);
            cmatrix.set(p, (float)color);
		} 
		Config.getConfig().addPropertyChangeListener(this); 
		_lastDetection = System.currentTimeMillis();
	} 
	 
	public void propertyChange(PropertyChangeEvent e) { 
			degrade = new Unitless(1.0 -	
											 Config.getConfig().getPropertyAsDouble("cloudDecay", 0.0)); 
		} 
 
	/** 
	 * Returns the	matrix. 
	 * @return The matrix that has the information about what has been searched and what has been detected 
	 */	 
	public PrimitiveTypeGeocentricMatrix getStateSpace(){ 
		return matrix; 
	}
 
	public void setStateSpace (PrimitiveTypeGeocentricMatrix m) {
		timestamp = new Date();
		matrix = m;
        PrimitiveTypeGeocentricMatrix cmat = new PrimitiveTypeGeocentricMatrix(m.getHeader());
        cmat.add(cmatrix);
        cmatrix = cmat;
	 }

    public void setCloud(Point p, float value)
    {
        setCloud(p,value,0.0);
    }

	public void setCloud(Point p, float value, double color)
    {
		matrix.set(p, value);
        cmatrix.set(p, (float)color);
		timestamp = new Date();
		_lastDetection = System.currentTimeMillis();
	}

	public long getLastDetection() {
		return _lastDetection;
	}

	public synchronized Region getBoundingBox() {
		double threshold = Config.getConfig().getPropertyAsDouble("CloudBeliefMatrix.boundingBoxThreshold", 0.05);
		int east = Integer.MAX_VALUE;
		int west = -1;
		int north = Integer.MAX_VALUE;
		int south = -1;
		boolean set = false;

		for (int x=0; x<matrix.getXSize(); x++) {
			for (int y=0; y<matrix.getYSize(); y++) {
				if (matrix.get(x,y) > threshold) {
					set = true;
					if (x < east) east = x;
					if (x > west) west = x;
					if (y > south) south = y;
					if (y < north) north = y;
				}
			}
		}

		if (!set) {
			return null;
		}

		AbsolutePosition ps[] = new AbsolutePosition[4];
		AbsolutePosition neCorner = matrix.getPosition(east, north);
		AbsolutePosition seCorner = matrix.getPosition(east, south);
		AbsolutePosition swCorner = matrix.getPosition(west, south);
		AbsolutePosition nwCorner = matrix.getPosition(west, north);

		ps[0] = swCorner;
		ps[1] = seCorner;
		ps[2] = neCorner;
		ps[3] = nwCorner;

		return new Region(ps);


	}
	
	/** 
	 * adds two beliefs together. 
	 * @param b The belief to add to this beleif. 
	 */	 
	public void addBelief(Belief b){ 
		CloudBeliefMatrix cbm = (CloudBeliefMatrix)b;
		if (b.getTimeStamp().compareTo(timestamp) > 0) 
			timestamp = b.getTimeStamp(); 
		if (cbm.getLastDetection() > _lastDetection) 
			_lastDetection = cbm.getLastDetection(); 
		//Logger.getLogger("GLOBAL").info("Adding searchedness"); 
		CloudBeliefMatrix belief = (CloudBeliefMatrix)b; 
		PrimitiveTypeGeocentricMatrix m = belief.getStateSpace(); 
		matrix.add(m);
        cmatrix.add(belief.getColormatrix());
	} 
	 
	/** 
	 * Overrides the parent update to degrade the belief every update. 
	 */	 
	public void update()
        {
            try
            {
		super.update(); 
		//System.err.println("Updating cloud belief Matrix"); 
		matrix.multiplyScalar((float)degrade.getDoubleValue()); 
		//System.err.println("degrade: " + degrade);
            }
            catch (Exception e)
            {
                System.err.println ("Exception in update thread - caught and ignored");
                e.printStackTrace();
            }
	} 
 
	/** 
	 * Retuns the unique name for this belief type. 
	 * @return A unique name for this belief type. 
	 */	 
	public String getName(){ 
		return BELIEF_NAME; 
	} 
 
	private void writeObject(ObjectOutputStream out) 
		throws IOException, ClassNotFoundException { 
		out.defaultWriteObject();	 
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
		CloudBeliefMatrix belief = new CloudBeliefMatrix();

		belief.readExternal(in);

		return belief;
	}

	public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
		matrix.writeExternal(out);
        getColormatrix().writeExternal(out);

		byte[] data = new byte[8];
		int index = 0; 
		index = ByteManipulator.addLong(data, _lastDetection, index, false); 
		out.write(data); 
	}

	public void readExternal(DataInput in) throws IOException {
        super.readExternal(in); 
		matrix = new PrimitiveTypeGeocentricMatrix();
        cmatrix = new PrimitiveTypeGeocentricMatrix();
		matrix.readExternal(in);
        getColormatrix().readExternal(in);
		byte[] data = new byte[8]; 
		in.readFully(data); 
		_lastDetection = ByteManipulator.getLong(data, 0,false); 
	}
	 
	protected void resetMatrix() { 
		int xSize = matrix.getXSize(); 
		int ySize = matrix.getYSize(); 
 
		for (int x = 0; x < xSize; x++) { 
			for (int y = 0; y < ySize; y++) { 
				matrix.set(x, y, CloudBeliefMatrix.NOT_DETECT_VALUE);
                getColormatrix().set(x, y, CloudBeliefMatrix.NOT_DETECT_VALUE);
			} 
		} 
	}

    /**
     * @return the cmatrix
     */
    public PrimitiveTypeGeocentricMatrix getColormatrix() {
        return cmatrix;
    }
	 
 
} 
 
//=============================== UNCLASSIFIED ================================== 
