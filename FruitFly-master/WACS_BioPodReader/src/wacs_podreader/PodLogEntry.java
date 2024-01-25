/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wacs_podreader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author southmk1
 */
public class PodLogEntry implements Comparable<PodLogEntry>{

    private Long timestamp;
    private String entry;

    private static DateFormat dateFormat = new SimpleDateFormat("y-M-d:H:m:s");

    public PodLogEntry(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public PodLogEntry(String entry) throws ParseException
    {
       //time and entry are space delimited
        timestamp = dateFormat.parse(entry.substring(0,entry.indexOf(' ')).trim()).getTime();
        this.entry = entry.substring(entry.indexOf(' ')+1).trim();
    }


    @Override
    public String toString()
    {
        return new Date(timestamp)+" "+entry;
    }

    /**
     * @return the entry
     */
    public String getEntry() {
        return entry;
    }

    
    public int compareTo(Object o) {
        if(o instanceof PodLogEntry)
        {
             return timestamp.compareTo(((PodLogEntry)o).timestamp);
        }
        if(o instanceof Long)
        {
            return timestamp.compareTo((Long)o);
        }

        throw new ClassCastException();
    }

    /**
     * @return the timestamp
     */
    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public int compareTo(PodLogEntry o) {
         return timestamp.compareTo(((PodLogEntry)o).timestamp);
    }



}
