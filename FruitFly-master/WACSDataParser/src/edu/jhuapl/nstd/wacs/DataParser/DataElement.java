/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.wacs.DataParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class DataElement
{
    protected String startTime = "";
    protected String startTimeL = "";
    protected String agentPosition = "";
    protected String agentPositionLat = " ";
    protected String agentPositionLon = " ";
    protected String agentPositionAlt = " ";
    protected String agentPositionBearing = " ";
    protected String displayPositionLat = " ";
    protected String displayPositionLon = " ";
    protected String displayPositionAlt = " ";
    protected String displayPositionBearing = " ";
    protected String target = "";
    protected String targetLat = " ";
    protected String targetLon = " ";
    protected String met = "";
    protected String windSpeed = " ";
    protected String windBearing = " ";
    protected String cloud = "";
    protected int numDetections = 0;
    protected String largeCounts = " ";
    protected String smallCounts = " ";
    protected String bioLargeCounts = " ";
    protected String bioSmallCounts = " ";
    protected String chemDetections1 = " ";
    protected String chemDetections2 = " ";
    protected String plume = "";
    protected String align = " ";
    protected String detect = " ";
    protected String chem = "";
    protected String picTelem = "";
    protected String orbit = "";
    protected String racetrack = "";
    protected String chemDetectADMMP = " ";
    protected String chemDetectBDMMP = " ";
    protected String chemDetectAAA = " ";
    protected String chemDetectBAA = " ";
    protected String chemDetectATEP = " ";
    protected String chemDetectBTEP = " ";
    protected String chemDetectAMS = " ";
    protected String chemDetectBMS = " ";
    protected String chemDetectAUnk = " ";
    protected String chemDetectBUnk = " ";
    protected String bio = "";
    protected String cloudLat = " ";
    protected String cloudLon = " ";
    protected String cloudAlt = " ";
    protected String cloudVal = " ";
    protected String cloudSource = " ";
    protected String cloudTime = " ";
    protected String picLat = " ";
    protected String picLon = " ";
    protected String picAltWGS84 = " ";
    protected String picAltMSL = " ";
    protected String picAgl = " ";
    protected String picAglValid = " ";
    protected String picRoll = " ";
    protected String picPitch = " ";
    protected String picYaw = " ";
    protected String picHdg = " ";
    protected String picWS = " ";
    protected String picWW = " ";
    protected String picVN = " ";
    protected String picVE = " ";
    protected String picVD = " ";
    protected String picIAS = " ";
    protected String picPDOP = " ";
    protected String picGPS = " ";
    protected String picP = " ";
    protected String picT = " ";
    protected String orbitLat = " ";
    protected String orbitLon = " ";
    protected String orbitAlt = " ";
    protected String orbitRad = " ";
    protected String bladewerxRaw = " ";
    protected String bladewerxResults = " ";
    protected String bladewerxTotalCounts = " ";
    protected String bladewerxGOF1 = " ";
    protected String bladewerxChannel1 = " ";
    protected String bladewerxCounts1 = " ";
    protected String bladewerxVariance1 = " ";
    protected String bladewerxGOF2 = " ";
    protected String bladewerxChannel2 = " ";
    protected String bladewerxCounts2 = " ";
    protected String bladewerxVariance2 = " ";
    protected String bladewerxGOF3 = " ";
    protected String bladewerxChannel3 = " ";
    protected String bladewerxCounts3 = " ";
    protected String bladewerxVariance3 = " ";
    protected String bladewerxGOF4 = " ";
    protected String bladewerxChannel4 = " ";
    protected String bladewerxCounts4 = " ";
    protected String bladewerxVariance4 = " ";
    protected String bladewerxGOF5 = " ";
    protected String bladewerxChannel5 = " ";
    protected String bladewerxCounts5 = " ";
    protected String bladewerxVariance5 = " ";
    protected String bridgeportResults = " ";
    protected String bridgeportRaw = " ";
    protected String bridgeportBkgnd = " ";
    protected String bridgeportIsotope = " ";
    protected String bridgeportIsotopeName = " ";
    protected String bridgeportNumEvents = " ";
    protected String bridgeportEventRate = " ";

    protected String explosionTimeRaw = " ";
    protected String explosionTimeMs = " ";
    protected String explosionPosRaw = " ";
    protected String explosionLatDecDeg = " ";
    protected String explosionLonDecDeg = " ";
    protected String explosionAltMslFt = " ";
    protected String wacsagentModeRaw = " ";
    protected String wacsagentMode = " ";
    protected String racetrackOrbitRaw = " ";
    protected String racetrackOrbitLat1DecDeg = " ";
    protected String racetrackOrbitLon1DecDeg = " ";
    protected String racetrackOrbitLat2DecDeg = " ";
    protected String racetrackOrbitLon2DecDeg = " ";
    protected String racetrackOrbitFinalAltMslM = " ";
    protected String racetrackOrbitStandoffAltMslM = " ";
    protected String racetrackOrbitRadiusM = " ";
    protected String alphaStateRaw = " ";
    protected String alphaSensorState = " ";
    protected String ibacStateRaw = " ";
    protected String ibacSensorState = " ";
    protected String c100StateRaw = " ";
    protected String c100SensorState = " ";
    protected String anacondaStateRaw = " ";
    protected String anacondaSensorState = " ";

    private String[] data;

    private String firstHeading;
    private String secondHeading;
    private String dataLine;


    public DataElement (String newTime)
    {
        startTime = newTime;
    }

    public void writeToFile (BufferedWriter outputWriter, int writeIndex) throws IOException, HumpsException
    {
        parseAll ();

        if (writeIndex == 0)
        {
            constructHeaders ();
            outputWriter.write(firstHeading);
            outputWriter.newLine();
            outputWriter.write(secondHeading);
            outputWriter.newLine();
        }

        constructLine (writeIndex);
        outputWriter.write (dataLine);
        outputWriter.newLine ();
    }
    
    private void constructLine (int index)
    {
        dataLine = index + "," + 
                startTime + "," + startTimeL + "," + 
                explosionTimeMs + "," +
                agentPositionLat + "," + agentPositionLon + "," + agentPositionAlt + "," + agentPositionBearing + "," +
                displayPositionLat+ "," +displayPositionLon + "," + displayPositionAlt + "," + displayPositionBearing + "," + 
                targetLat + "," + targetLon + "," + 
                windSpeed + "," + windBearing + "," +
                largeCounts + "," + smallCounts + "," + bioLargeCounts + "," + bioSmallCounts + "," + 
                align + "," + detect + "," + 
                explosionLatDecDeg + "," + explosionLonDecDeg + "," + explosionAltMslFt + "," +
                chemDetectADMMP+ "," +chemDetectAAA + "," + chemDetectATEP + "," + chemDetectAMS + "," + chemDetectAUnk + "," + chemDetectBDMMP+ "," +chemDetectBAA + "," + chemDetectBTEP+ "," + chemDetectBMS + "," + chemDetectBUnk + "," +
                numDetections + "," + cloudLat + "," + cloudLon + "," + cloudAlt + "," + cloudVal + "," + cloudSource + "," + cloudTime + "," + 
                picLat + "," + picLon + "," + picAltWGS84 + "," + picAltMSL + "," + picAgl + "," + picAglValid + "," + picRoll + "," + picPitch + "," + picYaw + "," + picHdg + "," + picWS + "," + picWW + "," + picVN + "," + picVE + "," + picVD + "," + picIAS + "," + picPDOP + "," + picGPS + "," + picP + "," + picT + "," +
                orbitLat + "," + orbitLon + "," + orbitAlt + "," + orbitRad + "," +
                racetrackOrbitLat1DecDeg + "," + racetrackOrbitLon1DecDeg + "," + racetrackOrbitLat2DecDeg + "," + racetrackOrbitLon2DecDeg + "," +
                racetrackOrbitFinalAltMslM + "," + racetrackOrbitStandoffAltMslM + "," + racetrackOrbitRadiusM + "," +
                wacsagentMode + "," +
                bladewerxTotalCounts + "," +
                bladewerxGOF1 + "," + bladewerxChannel1 + "," + bladewerxCounts1 + "," + bladewerxVariance1 + "," + bladewerxGOF2 + "," + bladewerxChannel2 + "," + bladewerxCounts2 + "," + bladewerxVariance2 + "," +
                bladewerxGOF3 + "," + bladewerxChannel3 + "," + bladewerxCounts3 + "," + bladewerxVariance3 + "," + bladewerxGOF4 + "," + bladewerxChannel4 + "," + bladewerxCounts4 + "," + bladewerxVariance4 + "," +
                bladewerxGOF5 + "," + bladewerxChannel5 + "," + bladewerxCounts5 + "," + bladewerxVariance5 + "," +
                bridgeportNumEvents + "," + bridgeportEventRate + "," + 
                bridgeportBkgnd + "," + bridgeportIsotope + "," + bridgeportIsotopeName + "," +
                alphaSensorState + "," + ibacSensorState + "," + c100SensorState + "," + anacondaSensorState + ",";
    }
    
    private void constructHeaders ()
    {
        firstHeading = "Index," + 
                "Time,Time," +
                "Exp Time," +
                "Agent Position Belief WACSAgent, , , ," +
                "Agent Position Belief Display, , , ," + 
                "Gimbal Target, ," +
                "MET, ," + 
                "BIO, , , ," + 
                "Plume, ," +
                "Explosion, , ," +
                "Anaconda Chem Detector A, , , , ," +
                "Anaconda Chem Detector B, , , , ," + 
                "Cloud Detection, , , , , , ," + 
                "Pic Telemetry, , , , , , , , , , , , , , , , , , , ," +
                "Orbit Belief, , , ," +
                "Racetrack Belief, , , , , , ," +
                "WACS Mode," +
                "Bladewerx Spectra," +
                "Bladewerx No Isotope, , , ," +
                "Bladewerx Isotope 1, , , ," +
                "Bladewerx Isotope 2, , , ," +
                "Bladewerx Isotope 3, , , ," +
                "Bladewerx Isotope 4, , , ," +
                "Bridgeport Spectra, ," +
                "Bridgeport Classification, , , " +
                "Sensor States, , , ,";
        secondHeading = " ," +
                " , ," +
                " ," + 
                "Latitude,Longitude,Altitude,Bearing," +
                "Latitude,Longitude,Altitude,Bearing," +
                "Latitude,Longitude," + 
                "Wind Speed,Wind Bearing To," + 
                "Large Counts,Small Counts,Bio Large Counts,Bio Small Counts," + 
                "Align,Detect," +
                "Latitude,Longitude,Altitude," +
                "DMMP,AA,TEP,MS,Unk,DMMP,AA,TEP,MS,Unk," +
                "Detections,Latitude,Longitude,Altitude,Value,Source,Time," + 
                "Latitude,Longitude,AltitudeWGS84,AltitudeMSL,AGL,AGL Valid,Roll,Pitch,Yaw,True Heading,Wind South,WindWest,Vel North,VelEast,Vel Down,Indicated Air Speed,PDOP,GPS Status,Static Press,Outside Air Temp," +
                "Latitude,Longitude,Altitude,Radius," +
                "Latitude1,Longitude1,Latitude2,Longitude2," + 
                "Final Altitude,Standoff Altitude,Radius," +
                " ," +
                "Total Counts," +
                "GOF,Channel,Counts,Variance,GOF,Channel,Counts,Variance,GOF,Channel,Counts,Variance,GOF,Channel,Counts,Variance,GOF,Channel,Counts,Variance," +
                "Num Events, Event Rate," +
                "Background Conc, Isotope Conc, Isotope Name," +
                "Alpha,Ibac,C100,Anaconda";
    }

    //parses All valid Fields
    private void parseAll() throws HumpsException
    {
        if(startTime.length()>2) parseTime();
        if(agentPosition.length()>2) parseAgent();
        if(met.length()>2) parseMet();
        if(cloud.length()>2) parseCloud();
        if(target.length()>2) parseTarget();
        if(plume.length()>2) parsePlume();
        if(bio.length()>2) parseBio();
        if(chem.length()>2) parseChem();
        if(picTelem.length()>2) parsePicTelem();
        if(orbit.length()>2) parseOrbit();
        if(racetrack.length()>2) parseRacetrack();
        if(bladewerxRaw.length()>2) parseBladewerxRaw();
        if(bladewerxResults.length()>2) parseBladewerxResults();
        if(bridgeportRaw.length()>2) parseBridgeportRaw();
        if(bridgeportResults.length()>2) parseBridgeportResults();

        if(explosionTimeRaw.length()>2) parseExplosionTime();
        if(explosionPosRaw.length()>2) parseExplosionPosition();
        if(wacsagentModeRaw.length()>2) parseWacsagentMode();
        if(racetrackOrbitRaw.length()>2) parseRacetrackOrbit();
        if(alphaStateRaw.length()>2) parseAlphaState();
        if(ibacStateRaw.length()>2) parseIbacState();
        if(c100StateRaw.length()>2) parseC100State();
        if(anacondaStateRaw.length()>2) parseAnacondaState();
    }

    //parses Agent Line
    private void parseAgent(){
        data = agentPosition.split(" ");
        agentPositionLat = data[4];
        agentPositionLon = data[7];
        agentPositionAlt = data[10];
        agentPositionBearing = data[13];
        if(data.length>14){
            displayPositionLat = data[18];
            displayPositionLon = data[21];
            displayPositionAlt = data[24];
            displayPositionBearing = data[27];
        }
    }

    //parses Start Time Line
    private void parseTime(){
        long x = Long.parseLong(startTime);
        Date date = new Date(x);
        startTimeL = startTime;
        startTime = date.toString();
    }

    //parses target line
    private void parseTarget(){
        data = target.split(" ");
        targetLat = data[4];
        targetLon = data[7];
    }

    //parses Met line
    private void parseMet(){
        data = met.split(" ");
        /*windSpeed = data[2] + " " + data[3];
        windBearing = data[6] + " " + data[7];*/
        windSpeed = data[2];
        windBearing = data[6];
    }

    //parses cloud line
    private void parseCloud(){
        data = cloud.split(" ");
        numDetections = Integer.parseInt(data[2]);
        cloudLat = data[13];
        cloudLon = data[19];
        cloudAlt = data[26];
        cloudVal = data[36];
        long x;
        if(data[41].equals("time")){
            x = Long.parseLong(data[47]);
        }else{
            cloudSource = data[41];
            x = Long.parseLong(data[52]);
        }
        Date d = new Date(x);
        cloudTime = d.toString();
    }

    //parses plume line
    private void parsePlume(){
        data = plume.split(" ");
        align = data[1].substring(6,data[1].length());
        detect = data[3];

        if(detect.contains(",")){
            detect = detect.replaceFirst(",", "");
        }
    }

    //parses bio line
    private void parseBio(){
        data = bio.split(" " );
        largeCounts = data[2];
        smallCounts = data[6];
        bioLargeCounts = data[10];
        bioSmallCounts = data[13];
    }

    //parses chem line
    private void parseChem(){
        data = chem.split(" ");

        int idx = 2;
        while (!data[idx].equals(""))
        {
            //We have a detection for LCDA
            String[] pair = data[idx].split(":");

            if (pair[0].equalsIgnoreCase("DMMP"))
                chemDetectADMMP = pair[1];
            else if(pair[0].equalsIgnoreCase("AA"))
                chemDetectAAA = pair[1];
            else if (pair[0].equalsIgnoreCase("TEP"))
                chemDetectATEP = pair[1];
            else if (pair[0].equalsIgnoreCase("MS"))
                chemDetectAMS = pair[1];
            else
                chemDetectAUnk = pair[1];

            idx ++;
        }
        idx += 3;

        while (idx < data.length)
        {
            //We have a detection for LCDB
            String[] pair = data[idx].split(":");

            if (pair[0].equalsIgnoreCase("DMMP"))
                chemDetectBDMMP = pair[1];
            else if (pair[0].equalsIgnoreCase("AA"))
                chemDetectBAA = pair[1];
            else if (pair[0].equalsIgnoreCase("TEP"))
                chemDetectBTEP = pair[1];
            else if (pair[0].equalsIgnoreCase("MS"))
                chemDetectBMS = pair[1];
            else
                chemDetectBUnk = pair[1];

            idx ++;
        }
    }

    private void parsePicTelem()
    {
        data = picTelem.split(" " );
        picLat = data[2];
        picLon = data[4];
        picAltWGS84 = data[6];
        picAltMSL = data[8];
        picAgl = data[10];
        picAglValid = (data[12].equals("Y")?"1":"0");
        picRoll = data[14];
        picPitch = data[16];
        picYaw = data[18];
        picHdg = data[20];
        picWS = data[22];
        picWW = data[24];
        picVN = data[26];
        picVE = data[28];
        picVD = data[30];
        picIAS = data[32];
        picPDOP = data[34];
        picGPS = data[36];
        picP = data[38];
        picT = data[40].substring(0, data[40].length()-1);
    }

    private void parseOrbit()
    {
        data = orbit.split (" ");
        orbitLat = data[2];
        orbitLon = data[5];
        orbitAlt = data[8];
        orbitRad = data[11];

    }
    
    private void parseRacetrack()
    {
        data = racetrack.split(" ");
        racetrackOrbitLat1DecDeg = data[2];
        racetrackOrbitLon1DecDeg = data[5];
        racetrackOrbitLat2DecDeg = data[8];
        racetrackOrbitLon2DecDeg = data[11];
        racetrackOrbitFinalAltMslM = data[14];
        racetrackOrbitStandoffAltMslM = data[17];
        racetrackOrbitRadiusM = data[20];
    }

    private void parseBladewerxResults() throws HumpsException
    {
        data = bladewerxResults.split ("\t");

        try
        {
            bladewerxGOF1 = data[2];
            bladewerxChannel1 = data[20];
            bladewerxCounts1 = data[22];
            bladewerxVariance1 = data[23];
            bladewerxGOF2 = data[284];
            bladewerxChannel2 = data[302];
            bladewerxCounts2 = data[304];
            bladewerxVariance2 = data[305];
            bladewerxGOF3 = data[566];
            bladewerxChannel3 = data[584];
            bladewerxCounts3 = data[586];
            bladewerxVariance3 = data[587];
            bladewerxGOF4 = data[848];
            bladewerxChannel4 = data[866];
            bladewerxCounts4 = data[868];
            bladewerxVariance4 = data[869];
            bladewerxGOF5 = data[1130];
            bladewerxChannel5 = data[1148];
            bladewerxCounts5 = data[1150];
            bladewerxVariance5 = data[1151];
        }
        catch (Exception e)
        {
            throw new HumpsException ("Bladewerx Isotopes may be separated between files!");
        }
    }

    private void parseBladewerxRaw()
    {
        data = bladewerxRaw.split ("\t");

        int counts = 0;
        for (int i = 6; i < 262; i ++)
            counts += Integer.parseInt(data[i]);

        bladewerxTotalCounts = "" + counts;
    }

    private void parseBridgeportResults()
    {
        data = bridgeportResults.split ("\t");

        bridgeportBkgnd = "0";
        bridgeportIsotope = "0";
        bridgeportIsotopeName = " ";

        if (data[1035].equals ("background-      bkg"))
        {
            bridgeportBkgnd = data[1036];
            bridgeportIsotopeName = data[1035];
        }
        else
        {
            bridgeportIsotope = data[1036];
            bridgeportIsotopeName = data[1035];
        }
    }

    private void parseBridgeportRaw()
    {
        data = bridgeportRaw.split ("\t");

        bridgeportNumEvents = data[2];
        bridgeportEventRate = data[6];
    }


    private void parseExplosionTime()
    {
        data = explosionTimeRaw.split (" ");

        explosionTimeMs = data[2];
    }

    private void parseExplosionPosition()
    {
        data = explosionPosRaw.split(" " );

        explosionLatDecDeg = data[2];
        explosionLatDecDeg = explosionLatDecDeg.substring(1);
        explosionLonDecDeg = data[4];
        explosionAltMslFt = data[6];
    }

    private void parseWacsagentMode()
    {
        data = wacsagentModeRaw.split (" ");

        for (int i = 0; i < data.length - 3; i ++)
        {
            if (data[i].equals("wacsagent"))
            {
                wacsagentMode = data[i+3];
                break;
            }
        }
    }

    private void parseRacetrackOrbit()
    {
        data = racetrackOrbitRaw.split (" ");

        racetrackOrbitLat1DecDeg = data[3];
        racetrackOrbitLon1DecDeg = data[6];
        racetrackOrbitLat2DecDeg = data[9];
        racetrackOrbitLon2DecDeg = data[12];
        racetrackOrbitFinalAltMslM = data[15];
        racetrackOrbitStandoffAltMslM = data[18];
        racetrackOrbitRadiusM = data[21];

    }

    private void parseAlphaState()
    {
        data = alphaStateRaw.split ("\t");

        alphaSensorState = data[0];
    }

    private void parseIbacState()
    {
        data = ibacStateRaw.split ("\t");

        ibacSensorState = data[0];
    }

    private void parseC100State()
    {
        data = c100StateRaw.split ("\t");

        c100SensorState = data[0];
    }

    private void parseAnacondaState()
    {
        data = anacondaStateRaw.split ("\t");

        anacondaSensorState = data[0];
    }
}
