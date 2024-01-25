/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wacs_podreader;

import java.util.Collections;
import java.util.LinkedList;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author southmk1
 */
public class TraceSeries {

    private String id;
    private boolean enabled;
    private LinkedList<Double> values;
    private LinkedList<Long> timestamps;
    private XYSeries xySeries;

    public TraceSeries(String id) {
        this.id = id;
        values = new LinkedList<Double>();
        timestamps = new LinkedList<Long>();
        xySeries = new XYSeries(this.id, false, true);
        enabled = true;
    }

    public void reset() {
        values.clear();
        timestamps.clear();
        xySeries.clear();
    }

    public void addValue(long timestamp, double value) {
        values.add(value);
        timestamps.add(timestamp);
    }

    public void sort() {
        Collections.sort(values);
        Collections.sort(timestamps);
    }

    public void updateSeries(int start, int end) {
        xySeries.clear();
        start = start < 0 ? 0 : start;
        if (end > values.size()) {
            end = values.size();
        }
        if (enabled) {
            for (int i = start; i < end; i++) {
                xySeries.add(timestamps.get(i), values.get(i));
            }
        } else {
            return;
        }
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the values
     */
    public LinkedList<Double> getValues() {
        return values;
    }

    /**
     * @param values the values to set
     */
    public void setValues(LinkedList<Double> values) {
        this.values = values;
    }

    /**
     * @return the timestamps
     */
    public LinkedList<Long> getTimestamps() {
        return timestamps;
    }

    /**
     * @param timestamps the timestamps to set
     */
    public void setTimestamps(LinkedList<Long> timestamps) {
        this.timestamps = timestamps;
    }

    /**
     * @return the xySeries
     */
    public XYSeries getXySeries() {
        return xySeries;
    }

    /**
     * @param xySeries the xySeries to set
     */
    public void setXySeries(XYSeries xySeries) {
        this.xySeries = xySeries;
    }
}
