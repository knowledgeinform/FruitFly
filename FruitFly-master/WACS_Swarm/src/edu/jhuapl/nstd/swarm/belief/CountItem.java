
package edu.jhuapl.nstd.swarm.belief;

/**
 * Data type to hold counts and the channel they belong to.
 * 
 * @author fishmsm1
 */
public class CountItem
{
    public Long timestamp;
    public Integer channel;
    public Integer num;
    
    public CountItem(long time, int bin, int n)
    {
        timestamp = time;
        channel = bin;
        num = n;
    }
    
    public CountItem(long time, int bin)
    {
        this(time, bin, 1);
    }
}