/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.spider.messages;

/**
 *
 * @author southmk1
 */
public class MetResponse extends SpiderMessage {
/**
 * 4.6.5 Message Type 15, SPIDER MET Sensor Report

This message is sent from the SPIDER to report MET sensor data.
Message Length:		TBD  6 bytes
Message Repetition Rate:	1 per second after the FAV enters the met spiral maneuver and continues until FINDER enters the egress waypoint.
Message Structure:		as below
			Pressure (2 bytes)
			Temperature (2 bytes)
			Humidity (2 bytes)

	Scaling of values:

Pressure 1 word = 10*Pressure in mbar. Resolution 0.1 mbar, Unsigned word.

Temperature 1 word = 100 * Temperature in Kelvin. Resolution 0.01 deg. Unsigned word.

Humidity 1 word = 10 * humidity %. Resolution 0.1 %. Unsigned word.

(all words lo:hi byte sequence).

 */
    private int pressure;
    private int temperature;
    private int humidity;

    public MetResponse() {
        super(SpiderMessageType.SPIDER_MET_SENSOR_REPORT, HeaderSize + ChecksumSize + 6);
    }
    @Override
    public void parseSpiderMessage(SpiderMessage m) {
        super.parseSpiderMessage(m);
        int idx=0;
        pressure = (m.readDataShort(idx));
        idx+=2;
        temperature = (m.readDataShort(idx));
        idx+=2;
        humidity = (m.readDataShort(idx));
        idx+=2;
    }

    /**
     * @return the pressure
     */
    public int getPressure() {
        return pressure;
    }

    /**
     * @param pressure the pressure to set
     */
    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    /**
     * @return the temperature
     */
    public int getTemperature() {
        return temperature;
    }

    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    /**
     * @return the humidity
     */
    public int getHumidity() {
        return humidity;
    }

    /**
     * @param humidity the humidity to set
     */
    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }
}
