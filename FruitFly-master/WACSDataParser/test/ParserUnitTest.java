/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import wacsdataparser2009.WACSParser;
import java.io.IOException;
import junit.framework.TestCase;
import static org.junit.Assert.*;

/**
 *
 * @author biggimh1
 */
public class ParserUnitTest extends TestCase{

    public void testAll(){
        String[] data = null;
        try{
            WACSParser parser = new WACSParser("C:\\test.txt","C:\\solution.txt");
            parser.parse();
            data = parser.data.toString().split(" ");
            assertTrue(data[0].equalsIgnoreCase("1247784996406"));
            assertTrue(data[1].equalsIgnoreCase("40.103831111111106"));
            assertTrue(data[2].equalsIgnoreCase("-113.18926583333334"));
            assertTrue(data[3].equalsIgnoreCase("1298.21"));
            assertTrue(data[4].equalsIgnoreCase("316.85139028529653"));
            assertTrue(data[5].equalsIgnoreCase("40.103904724121094"));
            assertTrue(data[6].equalsIgnoreCase("-113.18888092041016"));
            assertTrue(data[7].equalsIgnoreCase("0.0"));
            assertTrue(data[8].equalsIgnoreCase("0.0"));
            assertTrue(data[9].equalsIgnoreCase("40.083376"));
            assertTrue(data[10].equalsIgnoreCase("-113.182577"));
            assertTrue(data[11].equalsIgnoreCase("1.4310084738003557"));
            assertTrue(data[13].equalsIgnoreCase("206.5590486849958"));
            assertTrue(data[15].equalsIgnoreCase("0"));
            assertTrue(data[16].equalsIgnoreCase("0"));
            assertTrue(data[17].equalsIgnoreCase("0"));
            assertTrue(data[18].equalsIgnoreCase("0"));
            assertTrue(data[19].equalsIgnoreCase("0"));
            assertTrue(data[20].equalsIgnoreCase("0.776"));
            assertTrue(data[21].equalsIgnoreCase("0"));
            /*
            assertTrue(data[22].equalsIgnoreCase(""));
            assertTrue(data[23].equalsIgnoreCase(""));
            assertTrue(data[24].equalsIgnoreCase(""));
            assertTrue(data[25].equalsIgnoreCase(""));
            assertTrue(data[26].equalsIgnoreCase(""));
             */
        }catch(IOException e){
            System.out.print("Get the file correct dopey");
        }

    }
    
}