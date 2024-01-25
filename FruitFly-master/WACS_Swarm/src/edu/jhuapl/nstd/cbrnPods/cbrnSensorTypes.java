/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods;

/**
 * Indicies corresponding to sensor types and other pod components
 *
 * @author humphjc1
 */
public class cbrnSensorTypes
{
    /**
     * Directly to the rabbit board, not a specific sensor
     */
    public static final int RABBIT_BOARD = 0x00;

    /**
     * IBAC Particle Counter
     */
    public static final int SENSOR_IBAC = 0x01;

    /**
     * Bridgeport Gamma Detector
     */
    public static final int SENSOR_BRIDGEPORT = 0x02;

    /**
     * Bladewerx Alpha Detector
     */
    public static final int SENSOR_BLADEWERX = 0x03;

    /**
     * Pump for Bladewerx Alpha Detector
     */
    public static final int SENSOR_BLADEWERX_PUMP = 0x04;

    /**
     * C100 Particle Collector
     */
    public static final int SENSOR_C100 = 0x05;

    /**
     * Anaconda Chemical Detector and Collector
     */
    public static final int SENSOR_ANACONDA = 0x06;

    /**
     * Temperature and Humidity sensor
     */
    public static final int SENSOR_TEMPERATURE = 0x07;

    /**
     * Pod Cooling Fan
     */
    public static final int SENSOR_FAN = 0x08;

    /**
     * Pod Heater
     */
    public static final int SENSOR_HEATER = 0x09;

    /**
     * Pod Cooling Servo
     */
    public static final int SENSOR_SERVO = 0x0A;
    
    /**
     * Canberra Gamma Detector
     */
    public static final int SENSOR_CANBERRA = 0x0B;
            
}
