/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.spider.messages;

/**
 *
 * @author southmk1
 */
public interface SpiderMessageType {

    public static final int FAV_HEALTH = 00;
    public static final int FAV_WIND_ESTIMATE = 01;
    public static final int SPIDER_STATE_COMMAND = 40;
    public static final int SPIDER_TEST_COMMAND = 04;
    public static final int PYLON_STATUS_DATA_BYTE = 39;
    public static final int PYLON_STATE_COMMAND = 38;
    public static final int SPIDER_CHEMICAL_DETECTION = 41;
    public static final int SPIDER_COMPRESSED_DATA = 44;
    public static final int DETECTION_VALID = 12;
    public static final int SPIDER_TEST_REPORT = 13;
    public static final int SPIDER_STATUS = 14;
    public static final int SPIDER_MET_SENSOR_REPORT = 15;
    public static final int FAV_GPS_PVT = 16;
    public static final int SEND_MET = 17;
    public static final int AIRFRAME_TEST = 18;
    public static final int ARCHIVE_MESSAGE = 20;
    public static final int FAV_STATUS = 22;
    public static final int FAV_VALID_RESPONSE = 23;
    public static final int AUTO_RESPONSE = 31;
    public static final int SPIDER_OK_POWER_DOWN = 32;
    public static final int FINDER_MISSION_PLAN = 34;
    public static final int FINDER_CONTROL_MESSAGE = 35;
    public static final int TEST_COMMS = 36;
    public static final int PIU_INIT_REQUEST = 37;
    public static final int FINDER_MISSION_WAYPOINT_SELECT = 42;
    public static final int SPIDER_DCAS_SPECTRUM_DATA = 45;
    
}
