package edu.jhuapl.nstd.swarm.maps;

import java.lang.String;
import java.lang.System;
import java.io.*;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.swarm.util.*;

enum mapType {LINEOFSIGHT, LANDABILITY};


public class DtedGlobalMap {

	/**
	 * Print timing for analyzeDTED and extractMap function calls
	 */
	static boolean printTiming = false;

	/**
	 * Print status of file loads/saves to standard output
	 */
	static boolean printFileStatus = true;

	/**
	 * Print 1's and 0's grid for local map to standard output
	 */
	static boolean printLocalMap = false;

	/**
	 * Folder location of DTED file heirarchy.  Should contain longitude indexed folders, which contain latitude indexed files
	 */
	protected String dtedFolderName;
        
        /**
         * File extension of dted files.  Includes 'dot'.
         */
        protected String dtedFileExtension;

	/**
	 * Anchor position latitude for the DTED data as specified in the config file.  The actual 
	 * anchor position latitude used will likely be a little more encompassing
	 */
	protected double iAnchorLat;

	/**
	 * Anchor position longitude for the DTED data as specified in the config file.  The actual 
	 * anchor position longitude used will likely be a little more encompassing
	 */
	protected double iAnchorLon;

	/**
	 * Number of longitude seconds to extend (positive direction, E) from the anchor point
	 * to load the DTED data, as specified in the config file.  The actual value will likely be
	 * a little more encompassing
	 */
	protected int iNumLonSecs;

	/**
	 * Number of latitude seconds to extend (positive direction, N) from the anchor point
	 * to load the DTED data, as specified in the config file.  The actual value will likely be
	 * a little more encompassing
	 */
	protected int iNumLatSecs;

	/**
	 * Number of latitude points that should actually be read for each longitude value
	 */
	protected int actualNumLat;

	/**
	 * Number of latitude points to skip in the DTED file to reach the interest region
	 */
	protected int iNumOffsetLat;

	/**
	 * Number of longitude points to skip in the DTED file to reach the interest region
	 */
	protected int iNumOffsetLon;

	/**
	 * Counter-clockwise indices (of loaded DTED map) that the algorithm is using in the spiral search.
	 * Name is actually backward, but I don't care to change it.
	 */
	protected int cwI, cwJ;

	/**
	 * Clockwise indices (of loaded DTED map) that the algorithm is using in the spiral search.
	 * Name is actually backward, but I don't care to change it.
	 */
	protected int ccwI, ccwJ;

	/** 
	 * Indices (of loaded DTED map) that the current source point would be at.  Not an integer value,
	 * so generally falls between discrete DTED points
	 */
	protected double srcI, srcJ;

	/**
	 * Enumeration value to record which side of the spiraling box is being processed.  Needed because some calculations
	 * change based on which direction the spiral is going.
	 */
	enum direct { LEFT, BOTTOM, RIGHT, TOP};

	/**
	 * Stores which side of the spiraling box is being processed.  See definition of 'direct' above
	 */
	protected direct whichSide;

	/**
	 * DTED object loaded from file
	 */
	public DTEDObject dted = null;

	/**
	 * 1/60
	 */
	static final protected double INV60 = 0.01666666667;

	/**
	 * 1/3600
	 */
	static final protected double INV3600 = 0.00027777777778;

	/**
	 * Ratio of meters/degree at the equator.  Used to calculate meter-resolution from degree-resolution
	 */
	static final protected double DEG2M = 40075.16 * 1000 / 360.0;

	/**
	 * pi
	 */
	static final protected double M_PI = 3.14159265358979323;

	/**
	 * Converion from degrees to radians
	 */
	static final protected double DEG2RAD = M_PI / 180.0;

	/**
	 * Config data from file
	 */
	Config c = null;

	/**
	 * True if normal values have been calculated already
	 */
	boolean normsSet = false;
    protected static DtedGlobalMap _dted = null;
    private final static Object lockObject = new Object();

    public static DtedGlobalMap getDted()
    {
        if (_dted == null)
        {
            synchronized (lockObject)
            {
                //Do this check twice so we don't waste the energy to sync once the dted object has been loaded.
                if (_dted == null)
                {
                    _dted = new DtedGlobalMap();
                    _dted.analyzeDTED();
                }
            }
        }

        
        return _dted;
    }



    private DtedGlobalMap()
    {

        c = Config.getConfig();

        // Anchor point for loading DTED object - must encompass entire possible working area.
        iAnchorLat = (c.getPropertyAsDouble("DtedGlobalMap.dtedAnchorLatD", 0.0));
        iAnchorLon = (c.getPropertyAsDouble("DtedGlobalMap.dtedAnchorLonD", 0.0));
        // Number of lat/lon seconds to extend the loaded area from the anchor point, in the positive lat/lon directions
        iNumLatSecs = (c.getPropertyAsInteger("DtedGlobalMap.dtedLatSecsCountD", 0));
        iNumLonSecs = (c.getPropertyAsInteger("DtedGlobalMap.dtedLonSecsCountD", 0));

        // Directory that contains all DTED files.  This folder should contain a number of folders indexed
        // by longitude and each of those folders should contain the actual DTED files indexed by latitude
        dtedFolderName = new String(c.getProperty("DtedGlobalMap.dtedFolder", "C:\\DTED"));
        dtedFileExtension = c.getProperty ("DtedGlobalMap.dtedFileExtension", "dt2");
        if (!dtedFileExtension.startsWith("."))
            dtedFileExtension = "." + dtedFileExtension;
    }



    public Altitude getJlibAltitude(LatLonAltPosition pos)
    {
        return new Altitude(getAltitudeMSL(pos.getLatitude().getDoubleValue(Angle.DEGREES),
        pos.getLongitude().getDoubleValue(Angle.DEGREES)),
        Length.METERS);
    }

    public Altitude getJlibAltitude(double lat, double lon)
    {
        return new Altitude(getAltitudeMSL(lat,lon), Length.METERS);
    }

    public double getAltitudeMSL(double latDeg, double lonDeg)
    {
        int dtedI = 0, dtedJ = 0;

        // Index values of the cell of the DTED map that contains the specified lat/lon from the region of interest map
        dtedI = (int)Math.floor((latDeg - dted.anchorLat) / dted.resLat);
        dtedJ = (int)Math.floor((lonDeg - dted.anchorLon) / dted.resLon);
        if (dtedI < 0 || dtedI >= dted.numLat || dtedJ < 0 || dtedJ >= dted.numLon)
        {
            return -1.0;
        }
        else
          return dted.dataArray[dtedI][dtedJ].z;
    }


    /**
     * Allocates memory for the dataArray in the DTEDObject, must have numLat and numLon already set
     *
     * @param data DTEDObject to have its dataArray established
     * @exception OutOfMemoryError
     * @return none
     */
    public void regenDataArray(DTEDObject data) throws OutOfMemoryError
    {
            data.dataArray = new DTEDPoint[data.numLat][data.numLon];
            for (int i = 0; i < data.numLat; i++) {
                    for (int j = 0; j < data.numLon; j++) {
                            data.dataArray[i][j] = new DTEDPoint();
                    }
            }
    }
    /**
     * Option to use a previously created DTEDObject as the DTED map instead of regenerating one.  Should be used if
     * both a LineOfSightGlobalMap (derived) and LandabilityGlobalMap (derived) are used.  Pass the DTEDObject from one
     * instance into the other instance using this function to reduce memory waste.
     *
     * @param newDted DTEDObject to store as the DTED data map
     * @return boolean true if successfully stored, false otherwise
     */
    public boolean analyzeDTED(DTEDObject newDted)
    {
            return analyzeDTED(newDted, false);
    }

    /**
     * Option to use a previously created DTEDObject as the DTED map instead of regenerating one.  Should be used if
     * both a LineOfSightGlobalMap (derived) and LandabilityGlobalMap (derived) are used.  Pass the DTEDObject from one
     * instance into the other instance using this function to reduce memory waste.
     *
     * @param newDted DTEDObject to store as the DTED data map
     * @param computeNormals True if normal values should be computed for the newDted object, false otherwise
     * @return boolean true if successfully stored, false otherwise
     */
    public boolean analyzeDTED(DTEDObject newDted, boolean computeNormals)
    {
            if (newDted != null) {
                    dted = newDted;
                    if (computeNormals)
                            return doNorms();
                    return true;
            }
            return false;
    }

    /**
     * Construct the DTED filename to open based on the specified anchor Lat/Lon points.  The input arguments
     * must be integers and relate to the south-west corner of the DTED file.
     *
     * @param chooseLat Integer latitude degree values for the DTED file to be opened
     * @param chooseLon Integer longitude degree values for the DTED file to be opened
     * @return String filename and path to the DTED file to be opened
     *
     */
    protected String constructFilename(int chooseLat, int chooseLon)
    {
            char latDir, lonDir;
            if (chooseLat < 0) {
                    latDir = 's';
                    chooseLat = Math.abs(chooseLat);
            } else
                    latDir = 'n';

            if (chooseLon < 0) {
                    lonDir = 'w';
                    chooseLon = Math.abs(chooseLon);
            } else
                    lonDir = 'e';

            // An example full filename (including directory structure would look like:
            //		dtedFolderName\w077\n37.dt2
            // Use the current location and form a likeness...
            String filename = dtedFolderName + "/" + lonDir;
            if (chooseLon > 100)
                    filename += chooseLon;
            else if (chooseLon > 10)
                    filename += "0" + chooseLon;
            else
                    filename += "00" + chooseLon;

            filename += "/" + latDir;
            if (chooseLat > 10)
                    filename += chooseLat;
            else
                    filename += "0" + chooseLat;
            
            //filename += ".dt2";
            filename += dtedFileExtension;
            return filename;
    }


    /**
     * Main function call to load a DTED file into memory.  This function will use parameters that were
     * loaded from the config file and determine which file is needed to load the region of interest.  Up to 4
     * DTED files will be loaded, starting with the bottom-left (south-west) DTED file and expanding north-east
     * as necessary to load the entire region.  If more than 4 DTED files are needed, the entire region will not
     * be loaded.
     *
     * @param none
     * @return boolean status of load
     */
    public boolean analyzeDTED()
    {
            return analyzeDTED(false);
    }

    /**
     * Main function call to load a DTED file into memory.  This function will use parameters that were
     * loaded from the config file and determine which file is needed to load the region of interest.  Up to 4
     * DTED files will be loaded, starting with the bottom-left (south-west) DTED file and expanding north-east
     * as necessary to load the entire region.  If more than 4 DTED files are needed, the entire region will not
     * be loaded.
     *
     * @param none
     * @param computeNormals True if normals of the DTED data should be computed, false otherwise
     * @return boolean status of load
     */
    public boolean analyzeDTED(boolean computeNormals)
    {
            // Integer lat/lon values that correspond to file name;
            int chooseLat = (int)Math.floor(iAnchorLat);
            int chooseLon = (int)Math.floor(iAnchorLon);

            String filename = constructFilename(chooseLat, chooseLon);

            dted = null;
            dted = analyzeDTED(filename, false, null);

            if (dted != null && iAnchorLat + iNumLatSecs / 3600.0 > chooseLat + 1) {
                    // Also need to include DTED file to the north of the original file
                    int extendLat = (int)Math.floor(iAnchorLat + 1);
                    int extendLon = (int)Math.floor(iAnchorLon);

                    String extendFilename = constructFilename(extendLat, extendLon);

                    dted = analyzeDTED(extendFilename, true, dted);

            }
            if (dted != null && iAnchorLon + iNumLonSecs / 3600.0 > chooseLon + 1) {
                    // Also need to include DTED file to the east of the original file
                    int extendLat = (int)Math.floor(iAnchorLat);
                    int extendLon = (int)Math.floor(iAnchorLon + 1);

                    String extendFilename = constructFilename(extendLat, extendLon);

                    dted = analyzeDTED(extendFilename, true, dted);

            }
            if (dted != null && (iAnchorLat + iNumLatSecs / 3600.0 > chooseLat + 1) && (iAnchorLon + iNumLonSecs / 3600.0 > chooseLon + 1)) {
                    // Also need to include DTED file to the north-east of the original file
                    int extendLat = (int)Math.floor(iAnchorLat + 1);
                    int extendLon = (int)Math.floor(iAnchorLon + 1);

                    String extendFilename = constructFilename(extendLat, extendLon);

                    dted = analyzeDTED(extendFilename, true, dted);
            }

            if (dted == null)
                    return false;

            if (computeNormals)
                    return doNorms();

            return(dted != null);
    }

    /**
     * protected method that actually loads the DTED data using the specified parameters
     *
     * @param filename full filepath location of DTED file to load
     * @param extendDTED Used if more than one DTED file is being loaded.  If extendDTED is false, the original
     * DTED file (or only DTED file, if that's the case), the header info from that DTED file is read and saved
     * (anchor points, resolution, etc).  If true, only the actual DTED points are read and the counters are updated.
     * @param currDTED If extendDTED is false, this should specify the previously loaded DTEDObject
     * @return DTEDObject DTED object created from file
     */
    protected DTEDObject analyzeDTED(String filename, boolean extendDTED, DTEDObject currDTED)
    {
            long timeBefore = System.currentTimeMillis();
            if (printFileStatus)
                    System.out.println("DTED file to load: " + filename);

            boolean scalef = false;
            double foffset = 0.0;
            double fscale = 1.0;
            double iAnchorLatSave = iAnchorLat;
            double iAnchorLonSave = iAnchorLon;

            iNumOffsetLat = 0;
            iNumOffsetLon = 0;
            actualNumLat = 0;

            int sizeOfChar = 1;
            byte buffer[] = new byte[800];
            int value = 0;

            DTEDObject data = null;
            data = new DTEDObject();

            if (data == null) {
                    System.out.println("DTED object not declared.");
                    return null;
            }
            data.filename = filename;

            try {
                    // Find size of file
                    File file = new File(filename);
                    long endPos = file.length();
                    long current = 0;

                    DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

                    if (input == null) {
                            System.out.println("Error opening input file!!!!\t\t" + filename);
                            return null;
                    }

                    input.read(buffer, 0, sizeOfChar * 1);

                    // While not at end of file...
                    while (current < endPos) {
                            if (buffer[0] == 'V') {
                                    // Skip a section of the DTED file that isn't important at this time
                                    input.skipBytes(79);
                                    current += 80;
                            } else if (buffer[0] == 'H') {
                                    // Skip a section of the DTED file that isn't important at this time
                                    input.skipBytes(79);
                                    current += 80;
                            } else if (buffer[0] == 'U') {
                                    input.read(buffer, 1, sizeOfChar * 2);
                                    buffer[3] = '\0';
                                    if (buffer[1] != 'H' || buffer[2] != 'L') continue;

                                    readHeaderInfo(data, input);
                                    adjustDTEDObject(data, extendDTED, currDTED);

                                    // Read 80 bytes in this section
                                    current += 80;
                            } else if (buffer[0] == 'D') {
                                    // Skip a section of the DTED file that isn't important at this time
                                    input.skipBytes(647);
                                    current += 648;
                            } else if (buffer[0] == 'A') {
                                    // Skip a section of the DTED file that isn't important at this time
                                    input.skipBytes(2699);
                                    current += 2700;
                            } else if (buffer[0] == 'E') {
                                    // Skip a section of the DTED file that isn't important at this time
                                    input.skipBytes(3);
                                    current += 4;
                            } else if (buffer[0] == (byte)170) {
                                    // First time though, create the dataArray based on numLat and numLon
                                    if (data.dataArray == null) {
                                            try {
                                                    regenDataArray(data);
                                            } catch (OutOfMemoryError e) {
                                                    System.out.println("Error - Creating the DTED object exceeded the allowable memory for this program.  Reduce the size of the DTED load region or increase available memory.\n");
                                                    return null;
                                            }
                                    }

                                    long newCurrent = -1;
                                    newCurrent = readLatLonSection(data, input, current, endPos);

                                    // Means that readLatLonSection was aborted and newCurrent has already been updated
                                    if (newCurrent >= 0)
                                            current = newCurrent;
                                    else
                                            //current += (12 + 2 * data.numLat);
                                            current	+= (12 + 2 * actualNumLat);
                            } else {
                                    //Unknown character, something bad ???
                                    System.out.println("Error parsing DTED file - exiting...");
                                    return null;
                            }

                            input.read(buffer, 0, sizeOfChar * 1);
                            ++current;
                    }
            } catch (FileNotFoundException e) {
                    System.out.println("Error - the DTED file could not be found and was not loaded");
                    return null;
            } catch (Exception e) {
                    System.out.println(e);
                    e.printStackTrace();
                    return null;
            }

            if (extendDTED) {
                    if (Math.abs(data.resLon - currDTED.resLon) > 0.00001 || Math.abs(data.resLat - currDTED.resLat) > 0.00001) {
                            System.out.println("Error - can not merge DTED files with different resolutions");
                            return null;
                    }

                    data = mergeDTED(currDTED, data);
            }

            iAnchorLat = iAnchorLatSave;
            iAnchorLon = iAnchorLonSave;

            if (printFileStatus)
                    System.out.println("DTED file successfully loaded.");
            long timeAfter = System.currentTimeMillis();
            if (printTiming)
                    System.out.println("Load DTED file took: " + (timeAfter - timeBefore) / 1000.0 + " s");
            return data;
    }

    /**
     * Simulates the C++ atoi function call, but is distincly modified to fit this code and is not an exact replica.  Converts
     * a byte array to an integer value and returns the result
     *
     * @param buf byte array to be parsed
     * @param num number of bytes in buf to read
     * @return int resulting integer value from buf
     */
    private int atoi(byte buf[], int num)
    {

            String text = new String(buf, 0, num);
            int value = Integer.parseInt(text);
            return value;
    }

    /**
     * Calculates the normal values of the dted data using a criss-cross method.
     *
     * @param maxLandableAngle Maximum angle from vertical to normal vector to flag a point as landable
     * @return boolean True if succesfuly calculations, false otherwise
     */
    protected boolean doNorms()
    {
            double maxLandableAngle;

            // Visibility distance limits for base and helo sources, specified in meters
            maxLandableAngle = c.getPropertyAsDouble("belief.landability.maximumLandabilityAngleD", 10.0);
            normsSet = true;

            if (dted == null) {
                    System.out.println("Error - null DTEDObject used in doNorms function");
                    return false;
            }
            dted.calcNorms();

            for (int i = 0; i < dted.numLat; i++) {
                    for (int j = 0; j < dted.numLon; j++) {
                            dted.dataArray[i][j].landable = (dted.dataArray[i][j].normAngD <= maxLandableAngle);
                    }
            }

            return true;
    }

    /**
     * This method writes a portable bitmap file that can be opened in GIMP simply to see
     * the local map that was created.  No need to actually call this unless debugging.
     * This version prints the entire DTED map, although not all of it may have been processed
     *
     * @param filename to save bitmap file to
     * @return none
     */
    public void logger(String filename, mapType mapTypeChoice)
    {
            try {
                    Writer output = null;
                    File file = new File(filename);
                    output = new BufferedWriter(new FileWriter(file));

                    // Header for a portable bitmap file, P1 on first line
                    // and number of cols/rows on next line
                    output.write("P1\n");
                    output.write(dted.numLon + " " + dted.numLat + "\n");

                    // 1 for true, 0 for false
                    for (int i = dted.numLat - 1; i >= 0; i--) {
                            for (int j = 0; j < dted.numLon; j++) {
                                    if (mapTypeChoice == mapType.LINEOFSIGHT)
                                            output.write(dted.dataArray[i][j].visible ? "1" : "0");
                                    else if (mapTypeChoice == mapType.LANDABILITY)
                                            output.write(dted.dataArray[i][j].landable ? "1" : "0");
                            }
                            output.write("\n");
                    }

                    if (printFileStatus)
                            System.out.println("Bitmap file written to: " + file.getAbsoluteFile());
                    output.close();

            } catch (Exception e) {
                    System.out.println("Error writing bitmap file");
            }
    }

    /**
     * Merges two DTEDObjects together.  data will be merged to the saved currDTED object, then currDTED will be returned
     * currDTED must be the South-West-most DTED region, so data must be North, East, or North-East of data
     *
     * @param currDTED DTEDObject with all previous regions
     * @param data DTEDObject to add to currDTED
     * @return DTEDObject merged DTEDObject
     */
    private DTEDObject mergeDTED (DTEDObject currDTED, DTEDObject data)
    {

            // Merge the newly created data with the old currDTED
            double oldEndLat = currDTED.anchorLat + (currDTED.numLat - 1) * currDTED.resLat;
            double oldEndLon = currDTED.anchorLon + (currDTED.numLon - 1) * currDTED.resLon;
            double newEndLat = data.anchorLat + (data.numLat - 1) * data.resLat;
            double newEndLon = data.anchorLon + (data.numLon - 1) * data.resLon;

            if ((newEndLat > oldEndLat + currDTED.resLat / 2.0) || (newEndLon > oldEndLon + currDTED.resLon / 2.0)) {
                    // Extend in both dimensions, technically
                    int newNumLat = (int)Math.round((Math.max(newEndLat, oldEndLat) - currDTED.anchorLat) / currDTED.resLat) + 1;
                    int newNumLon = (int)Math.round((Math.max(newEndLon, oldEndLon) - currDTED.anchorLon) / currDTED.resLon) + 1;
                    DTEDPoint newDataArray[][] = new DTEDPoint[newNumLat][newNumLon];
                    for (int lat = 0; lat < currDTED.numLat; lat++) {
                            for (int lon = 0; lon < currDTED.numLon; lon++) {
                                    newDataArray[lat][lon] = currDTED.dataArray[lat][lon];
                            }
                    }
                    currDTED.dataArray = null;
                    currDTED.dataArray = newDataArray;
                    currDTED.numLat = newNumLat;
                    currDTED.numLon = newNumLon;
            }

            double latStartD = (data.anchorLat - currDTED.anchorLat) / currDTED.resLat;
            double lonStartD = (data.anchorLon - currDTED.anchorLon) / currDTED.resLon;
            int latStart = 0, lonStart = 0;
            if (latStartD - Math.floor(latStartD) < Math.ceil(latStartD) - latStartD)
                    latStart = (int)Math.floor(latStartD);
            else
                    latStart = (int)Math.ceil(latStartD);
            if (lonStartD - Math.floor(lonStartD) < Math.ceil(lonStartD) - lonStartD)
                    lonStart = (int)Math.floor(lonStartD);
            else
                    lonStart = (int)Math.ceil(lonStartD);

            double xAdd = 0;
            for (int newLat = latStart, oldLat = 0; oldLat < data.numLat && newLat < currDTED.numLat; newLat++, oldLat++) {
                    for (int newLon = lonStart, oldLon = 0; oldLon < data.numLon && newLon < currDTED.numLon; newLon++, oldLon++) {
                            if (oldLon == 0 && newLon > 0) {
                                    xAdd = currDTED.dataArray[newLat][newLon - 1].x - data.dataArray[oldLat][oldLon].x + currDTED.resXest;
                            }
                            currDTED.dataArray[newLat][newLon] = data.dataArray[oldLat][oldLon];

                            // The following line will break the definition of UTM values.  To simplify calculations, a UTM
                            // zone is extended and Easting values are adjusted for continuous values.
                            currDTED.dataArray[newLat][newLon].x += xAdd;
                    }
            }
            return currDTED;
    }

    /**
     * Reads a segment of DTED elevation data from the input stream and saves it in the DTEDObject appropriately.  Updates file counters
     * as necessary when reading through file
     *
     * @param data DTEDObject to store elevation data into
     * @param input input stream to read from
     * @param current Current position in input stream reading from
     * @param endPos Last position in input stream
     * @param iNumOffsetLat Number of latitude indices to skip before starting to read from stream
     * @param iNumOffsetLon Number of longitude indices to skip before starting to read from stream
     * @param actualNumLat Actual number of latitude points to read from stream
     * @return boolean True if entire segment was read, false otherwise
     * @exception IOException
     */
    private long readLatLonSection(DTEDObject data, DataInputStream input, long current, long endPos) throws IOException
    {
            byte buffer[] = new byte[800];
            int sizeOfChar = 1;

            // Data Block Count
            input.read(buffer, 0, sizeOfChar * 3);

            // Longitude Counter
            byte lngcta[] = new byte[2];
            short lngct = 0;
            lngct = input.readShort();

            // If we are outside of the specified longitude load region, skip this data and keep moving...
            if (iNumLatSecs > 0 && iNumLonSecs > 0 && (lngct < iNumOffsetLon)) {
                    input.skipBytes(2 * (actualNumLat) + 6);
                    current += 2 * (actualNumLat) + 12;
                    return current;
            } else if (lngct >= iNumOffsetLon + data.numLon) {
                    current = endPos;
                    return current;
            }

            // Latitude Counter
            byte latcta[] = new byte[2];
            short latct = 0;
            latct = input.readShort();

            int ctStart = 0;
            int ctEnd = data.numLat;

            // If a load region was specified, update the latitude limits and
            // skip through the file up to the first latitude limit
            if (iNumLatSecs > 0 && iNumLonSecs > 0) {
                    ctStart = iNumOffsetLat;
                    ctEnd = ctStart + data.numLat;
                    input.skipBytes(2 * (ctStart));
            }

            byte elevationa[] = new byte[2];
            int ct;
            // Read in the actual elevation data
            for (ct = ctStart; ct < ctEnd; ++ct) {

                    short elevation = 0;
                    elevation = input.readShort();

                    if (elevation < 0) {
                            elevation = (short)(-32768 - elevation);
                    }

                    data.dataArray[ct - ctStart][lngct - iNumOffsetLon].setLatLonZ(data.anchorLat + data.resLat * (ct - ctStart),
                                                                                                                                               data.anchorLon + data.resLon * (lngct - iNumOffsetLon),
                                                                                                                                               (double)elevation);
            }

            // If we didn't get all the way through the latitude data for this longitude, skip over
            // the rest of the latitude points
            if (ct != actualNumLat)
                    input.skipBytes(2 * (actualNumLat - ct));

            input.skipBytes(4);
            return -1;
    }

    /**
     * Read the header information from the input stream and save as parameters of the DTEDObject.
     *
     * @param data DTEDObject to store the header information from the input stream
     * @param input Input stream to read from
     * @exception IOException
     */
    private void readHeaderInfo(DTEDObject data, DataInputStream input) throws IOException
    {
            int sizeOfChar = 1;
            int sizeOfShort = 2;
            int sizeOfInt = 4;
            int sizeOfLong = 8;

            byte buffer[] = new byte[800];
            byte lngdega[] = new byte[4]; lngdega[3] = '\0';
            byte lngmina[] = new byte[3]; lngmina[2] = '\0';
            byte lngseca[] = new byte[3]; lngseca[2] = '\0';
            byte lnghem[] = new byte[1];
            byte latdega[] = new byte[4]; latdega[3] = '\0';
            byte latmina[] = new byte[3]; latmina[2] = '\0';
            byte latseca[] = new byte[3]; latseca[2] = '\0';
            byte lathem[] = new byte[1];
            byte lnginta[] = new byte[5]; lnginta[4] = '\0';
            byte latinta[] = new byte[5]; latinta[4] = '\0';
            byte lngcnta[] = new byte[5]; lngcnta[4] = '\0';
            byte latcnta[] = new byte[5]; latcnta[4] = '\0';
            byte vertacc[] = new byte[4];
            byte security[] = new byte[3];
            byte ref[] = new byte[12];
            byte widtha[] = new byte[5]; widtha[4] = '\0';
            byte heighta[] = new byte[5]; heighta[4] = '\0';
            byte multacc[] = new byte[1];

            int lngint;
            int latint;
            int lngdeg, lngmin, lngsec;
            int latdeg, latmin, latsec;

            input.read(buffer, 0, sizeOfChar * 1);
            // Read parameters and save...

            // Longitude Degrees
            input.read(lngdega, 0, sizeOfChar * 3);
            lngdeg = atoi(lngdega, 3);

            // Longitude Minutes
            input.read(lngmina, 0, sizeOfChar * 2);
            lngmin = atoi(lngmina, 2);

            // Longitude Seconds
            input.read(lngseca, 0, sizeOfChar * 2);
            lngsec = atoi(lngseca, 2);

            // Longitude Hemisphere
            input.read(lnghem, 0, sizeOfChar * 1);

            // Latitude Degrees
            input.read(latdega, 0, sizeOfChar * 3);
            latdeg = atoi(latdega, 3);

            // Latitude Minutes
            input.read(latmina, 0, sizeOfChar * 2);
            latmin = atoi(latmina, 2);

            // Latitude Seconds
            input.read(latseca, 0, sizeOfChar * 2);
            latsec = atoi(latseca, 2);

            // Latitude Hemisphere
            input.read(lathem, 0, sizeOfChar * 1);

            data.anchorLat = latdeg + latmin * INV60 + latsec * INV3600;
            data.anchorLon = lngdeg + lngmin * INV60 + lngsec * INV3600;

            // Longitude data interval
            input.read(lnginta, 0, sizeOfChar * 4);
            lngint = atoi(lnginta, 4);
            data.resLon = (lngint) * 0.1 / 3600;
            data.resXest = data.resLon * DEG2M * Math.cos(data.anchorLat * DEG2RAD);

            // Latitude data interval
            input.read(latinta, 0, sizeOfChar * 4);
            latint = atoi(latinta, 4);
            data.resLat = (latint) * 0.1 / 3600;
            data.resYest = data.resLat * DEG2M;

            // Vertical Accuracy
            input.read(buffer, 0, sizeOfChar * 4);

            // Security Code
            input.read(buffer, 0, sizeOfChar * 3);

            // Unique Reference
            input.read(buffer, 0, sizeOfChar * 12);

            // Number of Longitude Points
            input.read(lngcnta, 0, sizeOfChar * 4);
            data.numLon = atoi(lngcnta, 4);

            // Number of Latitude Points
            input.read(latcnta, 0, sizeOfChar * 4);
            data.numLat = atoi(latcnta, 4);
            actualNumLat = data.numLat;

            if (lathem[0] == (byte)'S')	data.anchorLat *= -1;
            if (lnghem[0] == (byte)'W')	data.anchorLon *= -1;

            // Multiple Accuracy - don't know what this is, being ignored anyway
            input.read(multacc, 0, sizeOfChar * 1);
            input.skipBytes(24);
    }

    /**
     * Parses data that was saved into the DTEDObject and adjusts them as necessary to most efficiently load
     * the interest region and ignore surrounding areas.  If the DTEDObject
     * should be added to another, previously created DTEDObect, some parameters will be modified as necessary
     *
     * @param data DTEDObject to store the header information from the input stream
     * @param extendDTED boolean flag, true if this DTEDObject will be added to an old DTEDObject
     * @param currDTED if extendDTED, the DTEDObject that will be added to
     */
    private void adjustDTEDObject (DTEDObject data, boolean extendDTED, DTEDObject currDTED)
    {
            /* Up to this point, data was simply read from the DTED file.  Now, determine
             * how much of the file should actually be read based on the user's specified
             * load region.  A conservative region will be loaded and this section will determine
             * what limits to use when loading the data.  In short:
             *
             * iNumOffsetLat will specify how many latitude values to skip in the DTED file
             * iNumOffsetLon will specify how many longitude values to skip in the DTED file
             * data.AnchorLat and data.AnchorLon will be set to encompass the entire load region
             * data.NumLat and data.NumLon will be set to encompass the entire load region
             *
             * If iNumLatSecs and iNumLonSecs are both zero (indicating a load region
             * wasn't specified), these changes will be pointless and the whole DTED file
             * will be loaded.
             */

            // Offset number for latitude
            iNumOffsetLat = (int)Math.floor((iAnchorLat - data.anchorLat) / data.resLat);
            if (iNumOffsetLat < 0)
                    iNumOffsetLat = 0;
            // Anchor latitude specified by offset number
            iAnchorLat = iNumOffsetLat * data.resLat + data.anchorLat;

            // Offset number for longitude
            iNumOffsetLon = (int)Math.floor((iAnchorLon - data.anchorLon) / data.resLon);
            if (iNumOffsetLon < 0)
                    iNumOffsetLon = 0;
            // Anchor longitude specified by offset number
            iAnchorLon = iNumOffsetLon * data.resLon + data.anchorLon;

            // Set flag if specified anchor points are within the DTED file
            boolean iUseAnchorLat = false, iUseAnchorLon = false;
            if (iNumLatSecs > 0 && iNumLonSecs > 0 && data.anchorLat < iAnchorLat)
                    iUseAnchorLat = true;
            if (iNumLatSecs > 0 && iNumLonSecs > 0 && data.anchorLon < iAnchorLon)
                    iUseAnchorLon = true;

            // Ending lat/lon values as specified by the load region
            double iEndLat = (extendDTED ? currDTED.anchorLat : iAnchorLat) + (iNumLatSecs) / 3600.0;
            double iEndLon = (extendDTED ? currDTED.anchorLon : iAnchorLon) + (iNumLonSecs) / 3600.0;
            // Ending lat/lon values as specified by DTED file
            double endLat = data.anchorLat + data.resLat * (data.numLat - 1);
            double endLon = data.anchorLon + data.resLon * (data.numLon - 1);

            // Set flag if specified ending points are within DTED file
            boolean iUseEndLat = false, iUseEndLon = false;
            if (iNumLatSecs > 0 && iNumLonSecs > 0 && iEndLat < endLat)
                    iUseEndLat = true;
            if (iNumLatSecs > 0 && iNumLonSecs > 0 && iEndLon < endLon)
                    iUseEndLon = true;

            // If using chosen anchor points, update anchor point information
            if (iUseAnchorLat)
                    data.anchorLat = iAnchorLat;
            if (iUseAnchorLon)
                    data.anchorLon = iAnchorLon;

            // Update number of lat/lon points to use based on which end limits are being used
            if (iUseEndLat)
                    data.numLat = 1 + (int)Math.abs(Math.round((iEndLat - (data.anchorLat)) / data.resLat));
            else if (iNumLatSecs > 0 && iNumLonSecs > 0)
                    data.numLat = 1 + (int)Math.abs(Math.round((endLat - (data.anchorLat)) / data.resLat));
            if (iUseEndLon)
                    data.numLon = 1 + (int)Math.abs(Math.round((iEndLon - (data.anchorLon)) / data.resLon));
            else if (iNumLatSecs > 0 && iNumLonSecs > 0)
                    data.numLon = 1 + (int)Math.abs(Math.round((endLon - (data.anchorLon)) / data.resLon));
    }
}
