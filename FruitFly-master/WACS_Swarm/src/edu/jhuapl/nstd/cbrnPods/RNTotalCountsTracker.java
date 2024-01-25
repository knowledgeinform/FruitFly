/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods;

import java.util.ArrayList;

/**
 * Tracks total counts and alerts whether a significant increase or decrease in total counts has occured
 *
 * @author humphjc1
 */
public class RNTotalCountsTracker
{
    /**
     * Maximum number of total count values to consider in statistics testing
     */
    int maxTotalCountsToAvg ;

    /**
     * How many standard deviations away from the current total count average must a total count value be to trigger an alarm
     */
    int totalCountsStdDevsLimit ;

    /**
     * Minimum total counts for an alarm.  Reduces false alarms with very low data
     */
    int totalCountsAlarmThreshold ;

    /**
     * List of total count values to consider when testing if total counts are changing significantly
     */
    ArrayList <Integer> totalCountsToAvg = new ArrayList <Integer>();

    /**
     * Create tracker object
     *
     * @param totalCountsToAvg Maximum number of total count values to consider in statistics testing
     * @param stdDevsLimit How many standard deviations away from the current total count average must a total count value be to trigger an alarm
     * @param countsAlarmThreshold Minimum total counts for an alarm.  Reduces false alarms with very low data
     */
    public RNTotalCountsTracker (int totalCountsToAvg, int stdDevsLimit, int countsAlarmThreshold)
    {
        maxTotalCountsToAvg = totalCountsToAvg;
        totalCountsStdDevsLimit = stdDevsLimit;
        totalCountsAlarmThreshold = countsAlarmThreshold;
    }

    /**
     * Compare total counts value to past data to determine if counts are changing significantly.  Add the new data to the list
     * to consider for next time
     *
     * @param totalCounts New total counts value to compare to historical data
     * @return Alert message if value is outside of expected range, blank String if no alert.
     */
    public String getCountsAlertMessage (long totalCounts)
    {
        String msg = "";

        if (totalCountsToAvg.size() > 1)
        {
            //Compute average
            int sum = 0;
            for (int i = 0; i < totalCountsToAvg.size(); i ++)
                sum += totalCountsToAvg.get(i);
            double avg = ((double)sum)/totalCountsToAvg.size();

            //Compute standard deviation
            double squaresSum = 0;
            for (int i = 0; i < totalCountsToAvg.size(); i ++)
                squaresSum += Math.pow(totalCountsToAvg.get(i)-avg, 2);
            double stddev = Math.sqrt(squaresSum/totalCountsToAvg.size());

            //If above avg+n*stddev,
            if (totalCounts > avg + stddev*totalCountsStdDevsLimit && totalCounts > totalCountsAlarmThreshold)
                msg = "COUNTS RISING: avg=" + (int)avg + ",stddev=" + (int)stddev;
            //If below avg-n*stddev,
            if (totalCounts < avg - stddev*totalCountsStdDevsLimit && totalCounts > totalCountsAlarmThreshold)
                msg = "COUNTS DROPPING: avg=" + (int)avg + ",stddev=" + (int)stddev;
        }

        //Trim list to store only recent data
        while (totalCountsToAvg.size() >= maxTotalCountsToAvg)
            totalCountsToAvg.remove(0);
        totalCountsToAvg.add(new Integer((int)totalCounts));

        return msg;
    }
}
