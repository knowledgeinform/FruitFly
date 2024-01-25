
package edu.jhuapl.nstd.cbrnPods.Bridgeport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author fishmsm1
 */
public abstract class GammaCountRates
{
    
    private float m_RealTime;
    
    public void setRealTime(float realTime)
    {
        m_RealTime = realTime;
    }
    
    public float getRealTime()
    {
        return m_RealTime;
    }
    
    public abstract byte[] toLogBytes();
    public abstract String toLogString();
    public abstract void writeToStream(DataOutputStream output) throws IOException;
    public abstract void readFromStream(DataInputStream input) throws IOException;
}
