/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.maps;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.swarm.util.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author humphjc1
 */
public class DtedFileModifier {

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

        String newDtedFolderName;
        double mslAdditionOffset;

    private DtedFileModifier(double mslAdditionOffset, String newFolderName)
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

        newDtedFolderName = newFolderName;
        this.mslAdditionOffset = mslAdditionOffset;
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
    protected String constructFilename(int chooseLat, int chooseLon, boolean inputFolderName)
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
            String filename = "";
            if (inputFolderName)
                filename = dtedFolderName + "/" + lonDir;
            else
                filename = newDtedFolderName + "/" + lonDir;
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
            filename += ".dt2";
            return filename;
    }

    public void verifyOutputFolderFromFilename (String filename)
    {
        String outputFolderName = filename.substring(0, filename.lastIndexOf("/"));

        while (outputFolderName.endsWith ("\\") || outputFolderName.endsWith("/"))
        outputFolderName = outputFolderName.substring(0, outputFolderName.length() - 1);
        try
        {
            File outputFolder = new File (outputFolderName);
            if (!outputFolder.exists())
                outputFolder.mkdirs();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
    public boolean offsetDTED()
    {
            // Integer lat/lon values that correspond to file name;
            int chooseLat = (int)Math.floor(iAnchorLat);
            int chooseLon = (int)Math.floor(iAnchorLon);

            String filename = constructFilename(chooseLat, chooseLon, true);
            String outFilename = constructFilename (chooseLat, chooseLon, false);
            verifyOutputFolderFromFilename (outFilename);

            dted = null;
            offsetDTED(filename, outFilename);

            if (iAnchorLat + iNumLatSecs / 3600.0 > chooseLat + 1) {
                    // Also need to include DTED file to the north of the original file
                    int extendLat = (int)Math.floor(iAnchorLat + 1);
                    int extendLon = (int)Math.floor(iAnchorLon);

                    String extendFilename = constructFilename(extendLat, extendLon, true);
                    String outExtendFilename = constructFilename (extendLat, extendLon, false);
                    verifyOutputFolderFromFilename (outExtendFilename);

                    offsetDTED(extendFilename, outExtendFilename);

            }
            if (iAnchorLon + iNumLonSecs / 3600.0 > chooseLon + 1) {
                    // Also need to include DTED file to the east of the original file
                    int extendLat = (int)Math.floor(iAnchorLat);
                    int extendLon = (int)Math.floor(iAnchorLon + 1);

                    String extendFilename = constructFilename(extendLat, extendLon, true);
                    String outExtendFilename = constructFilename (extendLat, extendLon, false);
                    verifyOutputFolderFromFilename (outExtendFilename);

                    offsetDTED(extendFilename, outExtendFilename);

            }
            if ((iAnchorLat + iNumLatSecs / 3600.0 > chooseLat + 1) && (iAnchorLon + iNumLonSecs / 3600.0 > chooseLon + 1)) {
                    // Also need to include DTED file to the north-east of the original file
                    int extendLat = (int)Math.floor(iAnchorLat + 1);
                    int extendLon = (int)Math.floor(iAnchorLon + 1);

                    String extendFilename = constructFilename(extendLat, extendLon, true);
                    String outExtendFilename = constructFilename (extendLat, extendLon, false);
                    verifyOutputFolderFromFilename (outExtendFilename);

                    offsetDTED(extendFilename, outExtendFilename);
            }

            if (dted == null)
                    return false;

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
    protected void offsetDTED(String inputFilename, String outputFilename)
    {
            long timeBefore = System.currentTimeMillis();
            if (printFileStatus)
                    System.out.println("DTED file to load: " + inputFilename);

            boolean scalef = false;
            double foffset = 0.0;
            double fscale = 1.0;
            double iAnchorLatSave = iAnchorLat;
            double iAnchorLonSave = iAnchorLon;

            iNumOffsetLat = 0;
            iNumOffsetLon = 0;
            actualNumLat = 0;

            int sizeOfChar = 1;
            byte buffer[] = new byte[8000];
            int value = 0;

            DTEDObject data = null;
            data = new DTEDObject();

            if (data == null) {
                    System.out.println("DTED object not declared.");
                    return;
            }
            data.filename = inputFilename;

            try {
                    // Find size of file
                    File file = new File(inputFilename);
                    long endPos = file.length();
                    long current = 0;

                    DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

                    if (input == null) {
                            System.out.println("Error opening input file!!!!\t\t" + inputFilename);
                            return;
                    }

                    DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream (new File (outputFilename)), 1000000));

                    int bytesRead = input.read(buffer, 0, sizeOfChar * 1);
                    output.write (buffer, 0, bytesRead);

                    // While not at end of file...
                    while (current < endPos) {
                            if (buffer[0] == 'V') {
                                    // Skip a section of the DTED file that isn't important at this time
                                    //input.skipBytes(79);
                                    bytesRead = input.read(buffer, 0, 79);
                                    output.write (buffer, 0, bytesRead);
                                    current += 80;
                            } else if (buffer[0] == 'H') {
                                    // Skip a section of the DTED file that isn't important at this time
                                    //input.skipBytes(79);
                                    bytesRead = input.read(buffer, 0, 79);
                                    output.write (buffer, 0, bytesRead);
                                    current += 80;
                            } else if (buffer[0] == 'U') {
                                    //input.read(buffer, 1, sizeOfChar * 2);
                                    bytesRead = input.read(buffer, 1, sizeOfChar*2);
                                    output.write (buffer, 1, bytesRead);
                                    buffer[3] = '\0';
                                    if (buffer[1] != 'H' || buffer[2] != 'L') continue;

                                    readHeaderInfo(data, input, output);

                                    // Read 80 bytes in this section
                                    current += 80;
                            } else if (buffer[0] == 'D') {
                                    // Skip a section of the DTED file that isn't important at this time
                                    //input.skipBytes(647);
                                    bytesRead = input.read(buffer, 0, 647);
                                    output.write (buffer, 0, bytesRead);
                                    current += 648;
                            } else if (buffer[0] == 'A') {
                                    // Skip a section of the DTED file that isn't important at this time
                                    //input.skipBytes(2699);
                                    bytesRead = input.read(buffer, 0, 2699);
                                    output.write (buffer, 0, bytesRead);
                                    current += 2700;
                            } else if (buffer[0] == 'E') {
                                    // Skip a section of the DTED file that isn't important at this time
                                    //input.skipBytes(3);
                                    bytesRead = input.read(buffer, 0, 3);
                                    output.write (buffer, 0, bytesRead);
                                    current += 4;
                            } else if (buffer[0] == (byte)170) {
                                    // First time though, create the dataArray based on numLat and numLon
                                    long newCurrent = -1;
                                    //System.out.println ("current = " + current);
                                    newCurrent = readLatLonSection(data, input, output, current, endPos);

                                    // Means that readLatLonSection was aborted and newCurrent has already been updated
                                    if (newCurrent >= 0)
                                            current = newCurrent;
                                    else
                                            current += (12 + 2 * data.numLat);
                                            //current	+= (12 + 2 * actualNumLat);
                            } else {
                                    //Unknown character, something bad ???
                                    System.out.println("Error parsing DTED file - exiting...");
                            }

                            //input.read(buffer, 0, sizeOfChar * 1);
                            bytesRead = input.read(buffer, 0, sizeOfChar * 1);
                            if (bytesRead > 0)
                                output.write (buffer, 0, bytesRead);
                            ++current;
                    }

                    output.flush();
                    output.close();
            } catch (FileNotFoundException e) {
                    System.out.println("Error - the DTED file could not be found and was not loaded");
                    
            } catch (Exception e) {
                    System.out.println(e);
                    
            }
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
    private long readLatLonSection(DTEDObject data, DataInputStream input, DataOutputStream output, long current, long endPos) throws IOException
    {
            byte buffer[] = new byte[800];
            int sizeOfChar = 1;

            // Data Block Count
            int readBytes = input.read(buffer, 0, sizeOfChar * 3);
            output.write (buffer, 0, readBytes);

            // Longitude Counter
            byte lngcta[] = new byte[2];
            short lngct = 0;
            lngct = input.readShort();
            output.writeShort(lngct);

            // Latitude Counter
            byte latcta[] = new byte[2];
            short latct = 0;
            latct = input.readShort();
            output.writeShort (latct);

            int ctStart = 0;
            int ctEnd = data.numLat;

            byte elevationa[] = new byte[2];
            int ct;
            // Read in the actual elevation data
            for (ct = ctStart; ct < ctEnd; ++ct) {

                    short elevation = 0;
                    elevation = input.readShort();
                    short newElev = (short)(elevation + mslAdditionOffset);
                    output.writeShort (newElev);

            }

            readBytes = input.read(buffer, 0, 4);
            output.write (buffer, 0, readBytes);
            return -1;
    }

    /**
     * Read the header information from the input stream and save as parameters of the DTEDObject.
     *
     * @param data DTEDObject to store the header information from the input stream
     * @param input Input stream to read from
     * @exception IOException
     */
    private void readHeaderInfo(DTEDObject data, DataInputStream input, DataOutputStream output) throws IOException
    {
            /*int sizeOfChar = 1;
            int sizeOfShort = 2;
            int sizeOfInt = 4;
            int sizeOfLong = 8;

            byte buffer[] = new byte[800];
            
            int readBytes = input.read(buffer, 0, 77);
            output.write (buffer, 0, readBytes);

            //HARDCODED!!!
            data.numLat = 3601;*/









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

            int readBytes = input.read(buffer, 0, sizeOfChar * 1);
            output.write (buffer, 0, readBytes);
            // Read parameters and save...

            // Longitude Degrees
            readBytes = input.read(lngdega, 0, sizeOfChar * 3);
            output.write (lngdega, 0, readBytes);
            lngdeg = atoi(lngdega, 3);

            // Longitude Minutes
            readBytes = input.read(lngmina, 0, sizeOfChar * 2);
            output.write (lngmina, 0, readBytes);
            lngmin = atoi(lngmina, 2);

            // Longitude Seconds
            readBytes = input.read(lngseca, 0, sizeOfChar * 2);
            output.write (lngseca, 0, readBytes);
            lngsec = atoi(lngseca, 2);

            // Longitude Hemisphere
            readBytes = input.read(lnghem, 0, sizeOfChar * 1);
            output.write (lnghem, 0, readBytes);

            // Latitude Degrees
            readBytes = input.read(latdega, 0, sizeOfChar * 3);
            output.write (latdega, 0, readBytes);
            latdeg = atoi(latdega, 3);

            // Latitude Minutes
            readBytes = input.read(latmina, 0, sizeOfChar * 2);
            output.write (latmina, 0, readBytes);
            latmin = atoi(latmina, 2);

            // Latitude Seconds
            readBytes = input.read(latseca, 0, sizeOfChar * 2);
            output.write (latseca, 0, readBytes);
            latsec = atoi(latseca, 2);

            // Latitude Hemisphere
            readBytes = input.read(lathem, 0, sizeOfChar * 1);
            output.write (lathem, 0, readBytes);

            data.anchorLat = latdeg + latmin * INV60 + latsec * INV3600;
            data.anchorLon = lngdeg + lngmin * INV60 + lngsec * INV3600;

            // Longitude data interval
            readBytes = input.read(lnginta, 0, sizeOfChar * 4);
            output.write (lnginta, 0, readBytes);
            lngint = atoi(lnginta, 4);
            data.resLon = (lngint) * 0.1 / 3600;
            data.resXest = data.resLon * DEG2M * Math.cos(data.anchorLat * DEG2RAD);

            // Latitude data interval
            readBytes = input.read(latinta, 0, sizeOfChar * 4);
            output.write (latinta, 0, readBytes);
            latint = atoi(latinta, 4);
            data.resLat = (latint) * 0.1 / 3600;
            data.resYest = data.resLat * DEG2M;

            // Vertical Accuracy
            readBytes = input.read(buffer, 0, sizeOfChar * 4);
            output.write (buffer, 0, readBytes);

            // Security Code
            readBytes = input.read(buffer, 0, sizeOfChar * 3);
            output.write (buffer, 0, readBytes);

            // Unique Reference
            readBytes = input.read(buffer, 0, sizeOfChar * 12);
            output.write (buffer, 0, readBytes);

            // Number of Longitude Points
            readBytes = input.read(lngcnta, 0, sizeOfChar * 4);
            output.write (lngcnta, 0, readBytes);
            data.numLon = atoi(lngcnta, 4);

            // Number of Latitude Points
            readBytes = input.read(latcnta, 0, sizeOfChar * 4);
            output.write (latcnta, 0, readBytes);
            data.numLat = atoi(latcnta, 4);
            actualNumLat = data.numLat;

            if (lathem[0] == (byte)'S')	data.anchorLat *= -1;
            if (lnghem[0] == (byte)'W')	data.anchorLon *= -1;

            // Multiple Accuracy - don't know what this is, being ignored anyway
            readBytes = input.read(multacc, 0, sizeOfChar * 1);
            output.write (multacc, 0, readBytes);

            //readBytes = input.skipBytes(24);
            readBytes = input.read(buffer, 0, 24);
            output.write (buffer, 0, readBytes);
    }

    

    public static void main (String args[])
    {

        //int offsetM = 2023;
        int offsetM = 1484;
        DtedFileModifier mod = new DtedFileModifier(offsetM, "./dted_offset_" + offsetM);
        mod.offsetDTED();
    }
}

