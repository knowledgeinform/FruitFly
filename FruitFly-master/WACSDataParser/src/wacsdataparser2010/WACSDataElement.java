package wacsdataparser2010;

/**
 *
 * @author Michael Biggins
 */
import java.util.Date;

public class WACSDataElement {
    protected String startTime = "";
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
    protected String picAlt = " ";
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
    private String[] data;


    public WACSDataElement(String time){
        startTime = time;
    }

    //Formated for importing to Excel File
    public String toString(){
        parseAll();
        return startTime + "," +  agentPositionLat + "," + agentPositionLon+ ","
                + agentPositionAlt+ "," +agentPositionBearing+ "," +
                displayPositionLat+ "," +displayPositionLon+ "," +
                displayPositionAlt+ "," +displayPositionBearing+ "," + targetLat
                + "," +targetLon + "," + windSpeed+ "," +windBearing + "," +
                largeCounts+ "," +smallCounts+ "," +
                bioLargeCounts+ "," +bioSmallCounts+ "," + align+ "," +detect +
                "," + chemDetectADMMP+ "," +chemDetectAAA + "," + chemDetectATEP + "," + chemDetectAMS + "," + chemDetectAUnk
                + "," + chemDetectBDMMP+ "," +chemDetectBAA + "," + chemDetectBTEP+ "," + chemDetectBMS + "," + chemDetectBUnk + ","
                +numDetections+ "," + cloudLat+ "," +cloudLon+ "," +cloudAlt+ "," +cloudVal+ "," +
                cloudSource+ "," +cloudTime
                + "," + picLat + "," + picLon + "," + picAlt + "," + picRoll + "," + picPitch
                + "," + picYaw + "," + picHdg + "," + picWS + "," + picWW + "," + picVN
                + "," + picVE + "," + picVD + "," + picIAS + "," + picPDOP + "," + picGPS
                + "," + picP + "," + picT
                + "," + orbitLat + "," + orbitLon + "," + orbitAlt + "," + orbitRad
                ;
    }

    //parses All valid Fields
    private void parseAll(){
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
        picAlt = data[6];
        picRoll = data[8];
        picPitch = data[10];
        picYaw = data[12];
        picHdg = data[14];
        picWS = data[16];
        picWW = data[18];
        picVN = data[20];
        picVE = data[22];
        picVD = data[24];
        picIAS = data[26];
        picPDOP = data[28];
        picGPS = data[30];
        picP = data[32];
        picT = data[34].substring(0, data[34].length()-1);
    }

    private void parseOrbit()
    {
        data = orbit.split (" ");
        orbitLat = data[2];
        orbitLon = data[5];
        orbitAlt = data[8];
        orbitRad = data[11];

    }
}
