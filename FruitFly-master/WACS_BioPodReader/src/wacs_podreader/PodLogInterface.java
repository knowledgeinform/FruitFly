/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wacs_podreader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author southmk1
 */
public class PodLogInterface {

    private static final int MAX_NUM_ENTRIES = 10000;
    private static final String[] labels = {
        "CSI",
        "CLI",
        "BCSI",
        "BCLI",
        "CSA",
        "CLA",
        "BCSA",
        "BCLA",
        "B%SA",
        "B%LA",
        "SFI",
        "SFA",
        "Alarm"
    };
    private LinkedList<PodLogEntry> traceEntries = new LinkedList<PodLogEntry>();
    private LinkedList<PodLogEntry> eventEntries = new LinkedList<PodLogEntry>();
    private LinkedList<TraceSeries> traceSeries = new LinkedList<TraceSeries>();
    private XYSeriesCollection xyDataset;

    public PodLogInterface() {
        xyDataset = new XYSeriesCollection();
        for (int i = 0; i < labels.length; i++) {
            traceSeries.add(new TraceSeries(labels[i]));
            xyDataset.addSeries(traceSeries.get(i).getXySeries());
            traceSeries.get(i).getXySeries().removeChangeListener(xyDataset);
        }

    }

    public void reset() {
        traceEntries.clear();
        eventEntries.clear();
        for (TraceSeries t : traceSeries) {
            t.reset();
        }
    }

    public void loadFile(String file) {
        reset();
        addFile(file);
    }

    public void addFile(String file) {
        BufferedReader fr;
        try {
            fr = new BufferedReader(new FileReader(file));

            String line;
            PodLogEntry ple;
            while (true) {
                line = fr.readLine();
                if (line == null) {
                    break;
                }
                try {
                    ple = new PodLogEntry(line);
                    if (ple.getEntry().contains("$trace")) {
                        traceEntries.add(ple);
                    } else {
                        eventEntries.add(ple);
                    }
                } catch (Exception ex) {
                    System.err.println("Error loading line, skipping...");
                }
            }
            Collections.sort(traceEntries);
            Collections.sort(eventEntries);

            for (PodLogEntry p : traceEntries) {
                addTraceEntry(p);
            }
            for (int i = 0; i < traceSeries.size(); i++) {
                //traceSeries.get(i).sort();
            }
        } catch (IOException ex) {
            Logger.getLogger(PodLogInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addTraceEntry(PodLogEntry podLog) {
        String[] values = podLog.getEntry().trim().split(",");
        if (values.length + 1 < traceSeries.size()) {
            return;
        }
        for (int i = 0; i < traceSeries.size(); i++) {
            try {
                Double.parseDouble(values[i + 1]);
            } catch (Exception e) {
                return;
            }
        }
        for (int i = 0; i < traceSeries.size(); i++) {
            traceSeries.get(i).addValue(podLog.getTimestamp(), Double.parseDouble(values[i + 1]));
        }
    }

    public void setFieldEnable(int i, boolean val) {
        traceSeries.get(i).setEnabled(val);
    }

    public XYSeriesCollection getDataSet(double startPosition, double stopPosition) {
        //number of windows to put in the frame
        int startIndex = (int)(startPosition*traceEntries.size());
        int stopIndex = (int)(stopPosition*traceEntries.size());

        if (stopIndex >= traceEntries.size()) {
            //unable to find timestamp in list, go to end
            stopIndex = traceEntries.size();
        }
        if (stopIndex <= startIndex) {
            stopIndex = startIndex + 1;
        }
        for (TraceSeries t : traceSeries) {
            t.updateSeries(startIndex, stopIndex);
        }

        return xyDataset;
    }

    public List<PodLogEntry> getTraceEntries(double startPosition, double stopPosition) {
        if(traceEntries.size()==0)
        {
            return traceEntries;
        }
        //number of windows to put in the frame
        int startIndex = (int)(startPosition*traceEntries.size());
        int stopIndex = (int)(stopPosition*traceEntries.size());

        if (stopIndex >= traceEntries.size()) {
            //unable to find timestamp in list, go to end
            stopIndex = traceEntries.size();
        }
        if (stopIndex <= startIndex) {
            stopIndex = startIndex + 1;
        }
        return traceEntries.subList(startIndex, stopIndex);
    }

    public List<PodLogEntry> getEventEntries(double startPosition, double stopPosition) {
        if(eventEntries.size()==0)
        {
            return eventEntries;
        }

        //get first and last timestamps for range
        int startIndex = (int)(startPosition*traceEntries.size());
        int stopIndex = (int)(stopPosition*traceEntries.size());

        if (stopIndex >= traceEntries.size()) {
            //unable to find timestamp in list, go to end
            stopIndex = traceEntries.size()-1;
        }
        if (stopIndex <= startIndex) {
            stopIndex = startIndex + 1;
        }

        PodLogEntry start = traceEntries.get(startIndex);
        PodLogEntry end = traceEntries.get(stopIndex);

        int eventStartIndex = Collections.binarySearch(eventEntries, new PodLogEntry(start.getTimestamp()));
        int eventStopIndex = Collections.binarySearch(eventEntries, new PodLogEntry(end.getTimestamp()));

        if (eventStopIndex < 0) {
            //unable to find timestamp in list, go to end
            eventStopIndex = eventEntries.size();
        }
        if (eventStartIndex < 0) {
            //unable to find timestamp in list, go to end
            eventStartIndex = 0;
        }
        if (eventStopIndex <= eventStartIndex) {
            eventStopIndex = eventStartIndex + 1;
        }

        if(eventStopIndex>eventEntries.size())
        {
           eventStopIndex=eventEntries.size();
        }

        return eventEntries.subList(eventStartIndex, eventStopIndex);
    }

    /**
     * @return the xyDataset
     */
    public XYSeriesCollection getXyDataset() {
        return xyDataset;
    }
}
