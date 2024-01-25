package edu.jhuapl.nstd.cbrnPods.Bridgeport;

public interface BridgeportUsbMessageListener
{
    /**
     * Receive SensorDriverMessage from the Bridgeport sensor.
     *
     * @param m Received message
     */
    public void handleSensorDriverMessage(SensorDriverMessage m);
}