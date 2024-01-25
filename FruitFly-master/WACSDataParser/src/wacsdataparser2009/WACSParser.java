package wacsdataparser2009;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;


/**
 *
 * @author biggimh1
 */
public class WACSParser {

    private BufferedWriter writer;
    private BufferedReader reader;
    public WACSDataElement data = new WACSDataElement("-1");



    public WACSParser(String sourceFileName, String destination) throws IOException{
           File sourceFile = new File(sourceFileName);
           writer = new BufferedWriter(new FileWriter(destination));
           reader = new BufferedReader(new FileReader(sourceFile));

           writer.write(getFirstHeading());
           writer.newLine();
           writer.write(getSecondHeading());
           writer.newLine();

    }

    private String getSecondHeading(){
        String s = " ,Latitude,Longitude,Bearing,Degrees From North,Latitude,Longitude,Bearing,Degrees From North,Latitude,Longitude,Wind Speed,Wind Bearing,Large Counts,Small Counts,Bio Large Counts,Bio Small Counts,Align,Detect,null,DMMP,AA,TEPO,null,DMMP,AA,TEPO,Detections,Latitude,Longitude,Altitude,Value,Source,Time";
        return s;
    }

    private String getFirstHeading(){
        String s = "Time,Agent Position Belief WACSAgent, , , ,Agent Position Belief Display, , , ,Gimal Target, ,MET, ,BIO, , , ,Plume, ,Spider Chem Detector 1, , , ,Spider Chem Detector 2, , , ,Cloud Detection, , , , , , ,";
        return s;
    }

    public void parse() throws IOException{
        //get needed lines from file
        String line = "";
        while((line = reader.readLine())!= null){
            if(line.contains("time:")){
                data = new WACSDataElement(line.split(" ")[1]);
            }else if(line.equals("belief: agentPositionBelief")){
                while(!(line = reader.readLine()).contains("end")){
                    data.agentPosition = data.agentPosition + " " + line + " ";
                }
            }else if(line.equals("belief: targetBelief")){
                while(!(line = reader.readLine()).contains("end")){
                    data.target = data.target + " " + line + " ";
                }
            }else if(line.equals("metbelief:")){
                while(!(line = reader.readLine()).contains("end")){
                    data.met = data.met + " " + line + " ";
                }
            }else if(line.equals("plumedetectionbelief:")){
                while(!(line = reader.readLine()).contains("end")){
                    data.plume = data.plume + " " + line + " ";
                }
            }else if(line.equals("clouddetectionbelief:")){
                while(!(line = reader.readLine()).contains("end")){
                    data.cloud = data.cloud + " " +  line + " ";
                }
            }else if(line.equals("spiderdetectionbelief:")){
                while(!(line = reader.readLine()).contains("end")){
                    data.chem = data.chem + " " + line + " ";
                }
            }else if(line.equals("biopoddetectionbelief:")){
                while(!(line = reader.readLine()).contains("end")){
                    data.bio  = data.bio + " " + line + " ";
                }
            //parses and records the current reading
            }else if(line.equals("end time")){
                record();
            }
        }
        writer.close();
        reader.close();
    }

    private void record()throws IOException{
        //to string parses and genereates string for excel
        String d = data.toString();
        writer.write(d);
        writer.newLine();
        writer.flush();
    }
}
