package wacsdataparser2009;

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
    protected String chemDetect1null = " ";
    protected String chemDetect1DMMP = " ";
    protected String chemDetect2null = " ";
    protected String chemDetect2DMMP = " ";
    protected String chemDetect1AA = " ";
    protected String chemDetect2AA = " ";
    protected String chemDetect1TEPO = " ";
    protected String chemDetect2TEPO = " ";
    protected String bio = "";
    protected String cloudLat = " ";
    protected String cloudLon = " ";
    protected String cloudAlt = " ";
    protected String cloudVal = " ";
    protected String cloudSource = " ";
    protected String cloudTime = " ";
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
                numDetections+ "," +largeCounts+ "," +smallCounts+ "," +
                bioLargeCounts+ "," +bioSmallCounts+ "," + align+ "," +detect +
                "," + chemDetect1null+ "," + chemDetect1DMMP+ "," +chemDetect1AA
                + "," +chemDetect1TEPO+ "," +chemDetect2null+ "," +
                chemDetect2DMMP+ "," +chemDetect2AA+ "," +chemDetect2TEPO + ","
                + cloudLat+ "," +cloudLon+ "," +cloudAlt+ "," +cloudVal+ "," +
                cloudSource+ "," +cloudTime;
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
        windSpeed = data[2] + " " + data[3];
        windBearing = data[6] + " " + data[7];
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
        largeCounts = data[6];
        smallCounts = data[10];
        bioLargeCounts = data[15];
        bioSmallCounts = data[20];
    }

    //parses chem line
    private void parseChem(){
        data = chem.split(" ");

        for(int x = 2; x<data.length; x++){
            if(data[x].equals("#1")){
                x+=3;
                chemDetect1null = data[x];
                x+=2;
                if(data[x].equals("DMMP:")){
                    x+=1;
                    chemDetect1DMMP = data[x];
                }else if(data[x].equals("AA:")){
                    x+=1;
                    chemDetect1AA = data[x];
                }else if(data[x].equals("TEPO:")){
                    x+=1;
                    chemDetect1TEPO = data[x];
                }
            }
            if(data[x].equals("#2")){
                x+=3;
                chemDetect2null = data[x];
                if(x<data.length-3){
                    x += 2;
                    if(data[x].equals("DMMP:")){
                        x+=1;
                        chemDetect2DMMP = data[x];
                    }else if(data[x].equals("AA:")){
                        x+=1;
                        chemDetect2AA = data[x];
                    }else if(data[x].equals("TEPO:")){
                        x+=1;
                        chemDetect2TEPO = data[x];
                }
                }
            }
        }
    }
}
