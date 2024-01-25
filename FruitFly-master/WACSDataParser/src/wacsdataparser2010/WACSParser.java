package wacsdataparser2010;

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
    private BufferedReader metricsReader;
    public WACSDataElement data = new WACSDataElement("-1");



    public WACSParser(String sourceFileName, String destination) throws IOException{
           File sourceFile = new File(sourceFileName);
           writer = new BufferedWriter(new FileWriter(destination));
           metricsReader = new BufferedReader(new FileReader(sourceFile));

           writer.write(getFirstHeading());
           writer.newLine();
           writer.write(getSecondHeading());
           writer.newLine();

    }

    private String getSecondHeading(){
        String s = " , ,Latitude,Longitude,Bearing,Degrees From North,Latitude,Longitude,Bearing,Degrees From North,Latitude,Longitude,Wind Speed,Wind Bearing,Large Counts,Small Counts,Bio Large Counts,Bio Small Counts,Align,Detect,DMMP,AA,TEP,MS,Unk,DMMP,AA,TEP,MS,Unk,Detections,Latitude,Longitude,Altitude,Value,Source,Time,"
                + "Latitude,Longitude,Altitude,Roll,Pitch,Yaw,True Heading,Wind South,WindWest,Vel North,VelEast,Vel Down,Indicated Air Speed,PDOP,GPS Status,Static Press,Outside Air Temp,Latitude,Longitude,Altitude,Radius";
        return s;
    }

    private String getFirstHeading(){
        String s = "Index,Time,Agent Position Belief WACSAgent, , , ,Agent Position Belief Display, , , ,Gimal Target, ,MET, ,BIO, , , ,Plume, ,Anaconda Chem Detector A, , , , ,Anaconda Chem Detector B, , , , ,Cloud Detection, , , , , , ,"
                + "Pic Telemetry, , , , , , , , , , , , , , , , ,Orbit Belieft, , , ";
        return s;
    }

    public void parse() throws IOException{
        //get needed lines from file
        String line = "";
        while((line = metricsReader.readLine())!= null){
            if(line.contains("time:")){
                data = new WACSDataElement(line.split(" ")[1]);
            }else if(line.equals("belief: agentPositionBelief")){
                while(!(line = metricsReader.readLine()).contains("end")){
                    data.agentPosition = data.agentPosition + " " + line + " ";
                }
            }else if(line.equals("belief: targetBelief")){
                while(!(line = metricsReader.readLine()).contains("end")){
                    data.target = data.target + " " + line + " ";
                }
            }else if(line.equals("metbelief:")){
                while(!(line = metricsReader.readLine()).contains("end")){
                    data.met = data.met + " " + line + " ";
                }
            }else if(line.equals("plumedetectionbelief:")){
                while(!(line = metricsReader.readLine()).contains("end")){
                    data.plume = data.plume + " " + line + " ";
                }
            }else if(line.equals("clouddetectionbelief:")){
                while(!(line = metricsReader.readLine()).contains("end")){
                    data.cloud = data.cloud + " " +  line + " ";
                }
            }else if(line.equals("anacondadetectionbelief:")){
                while(!(line = metricsReader.readLine()).contains("end")){
                    data.chem = data.chem + " " + line + " ";
                }
            }else if(line.equals("biopoddetectionbelief:")){
                while(!(line = metricsReader.readLine()).contains("end")){
                    data.bio  = data.bio + " " + line + " ";
                }
            }else if(line.equals("pic_telem:")){
                while(!(line = metricsReader.readLine()).contains("end")){
                    data.picTelem  = data.picTelem + " " + line + " ";
                }
            }else if(line.equals("circularorbitbelief:")){
                while(!(line = metricsReader.readLine()).contains("end")){
                    data.orbit  = data.orbit + " " + line + " ";
                }
            //parses and records the current reading
            }else if(line.equals("end time")){
                record();
            }
        }
        writer.close();
        metricsReader.close();
    }

    private int idx = 0;
    private void record()throws IOException{

        //to string parses and genereates string for excel
        String d = (idx++) + "," + data.toString();
        writer.write(d);
        writer.newLine();
        writer.flush();
    }
}
