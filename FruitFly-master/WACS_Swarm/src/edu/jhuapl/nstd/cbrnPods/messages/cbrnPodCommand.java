/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages;

/**
 *
 * @author humphjc1
 */
public class cbrnPodCommand extends cbrnPodBytes
{
    protected static char SYNC_BYTE = '~';
    
    
    public final static int C100_PRIME_CMD = 0x11;
    public final static int C100_COLLECT_AND_PURGE_CMD = 0x12;
    public final static int C100_COLLECT_ON_CMD = 0x13;
    public final static int C100_COLLECT_OFF_CMD = 0x14;
    public final static int C100_SAMPLE_CMD = 0x15;
    public final static int C100_CLEAN_CMD = 0x16;
    public final static int C100_STATUS_CMD = 0x17;
    public final static int C100_CONFIG_CMD = 0x18;
    public final static int C100_RESET_VIALS = 0x19;
    public final static int C100_FLOWSTATUS_CMD = 0x1A;
    public final static int C100_RAW_CMD = 0xFF;
    
    public final static int C100_CONFIG_TIMEP2 = 0x31;
    public final static int C100_CONFIG_EXTRAP2 = 0x32;
    public final static int C100_CONFIG_PRIMING1 = 0x33;
    public final static int C100_CONFIG_PRIMINGD = 0x34;
    public final static int C100_CONFIG_PRIMING2 = 0x35;
    public final static int C100_CONFIG_CL = 0x36;
    public final static int C100_CONFIG_S = 0x37;
    public final static int C100_CONFIG_RS = 0x38;
    public final static int C100_CONFIG_VALVES = 0x39;


    
    public final static int IBAC_ALARM_CMD = 0x11;
    public final static int IBAC_CLEAR_ALARM_CMD = 0x12;
    public final static int IBAC_STATUS_CMD = 0x13;
    public final static int IBAC_SLEEP_CMD = 0x14;
    public final static int IBAC_TRACE_RATE_CMD = 0x15;
    public final static int IBAC_DIAG_RATE_CMD = 0x16;
    public final static int IBAC_AIR_SAMPLE_CMD = 0x17;
    public final static int IBAC_COLLECT_CMD = 0x18;
    public final static int IBAC_AUTO_COLLECT_CMD = 0x19;
    public final static int IBAC_RAW_CMD = 0xFF;
    
    public final static int POD_SET_RTC = 0x03;
    public final static int POD_LOG_END = 0x04;
    public final static int POD_LOG_NEW = 0x05;
    
    public final static int BRIDGEPORT_STATISTICS = 0x01;
    public final static int BRIDGEPORT_HISTOGRAM = 0x02;
    public final static int BRIDGEPORT_CONFIGURATION = 0x03;
    
    public final static int SERVO_SET_PWM = 0x11;
    public final static int SERVO_INC_PWM = 0x12;
    public final static int SERVO_DEC_PWM = 0x13;
    public final static int SERVO_CONFIG_OPEN = 0x14;
    public final static int SERVO_CONFIG_CLOSED = 0x15;
    public final static int SERVO_CONFIG_TEMP = 0x16;
    public final static int SERVO_CONFIG_HUMIDITY = 0x17;
    public final static int SERVO_SET_OPEN = 0x18;
    public final static int SERVO_SET_CLOSED = 0x19;
    public final static int SERVO_AUTO_CONTROL = 0x20;

    public final static int HEATER_CONFIG_TEMP = 0x11;
    public final static int HEATER_CONFIG_HUMIDITY = 0x12;
    public final static int HEATER_SET_ON = 0x13;
    public final static int HEATER_SET_OFF = 0x14;
    public final static int HEATER_AUTO_CONTROL = 0x15;
    
    public final static int FAN_CONFIG_TEMP = 0x11;
    public final static int FAN_CONFIG_HUMIDITY = 0x12;
    public final static int FAN_SET_ON = 0x13;
    public final static int FAN_SET_OFF = 0x14;
    public final static int FAN_AUTO_CONTROL = 0x15;
    
    public final static int BLADEWERX_VERSION_CMD = 0x11;
    public final static int BLADEWERX_GET_ADC_CMD = 0x12;
    public final static int BLADEWERX_CALIBRATION_MODE_CMD = 0x13;
    public final static int BLADEWERX_SET_ADC_CMD = 0x14;
    public final static int BLADEWERX_SET_SCALE_CMD = 0x15;
    public final static int BLADEWERX_SET_THRESHOLD_CMD = 0x16;
    public final static int BLADEWERX_SET_GAIN_CMD = 0x17;
    public final static int BLADEWERX_SET_OFFSET_CMD = 0x18;
    public final static int BLADEWERX_POWER_ON_CMD = 0x19;
    public final static int BLADEWERX_POWER_OFF_CMD = 0x1A;
    public final static int BLADEWERX_RAW_CMD = 0xFF;
    
    public final static int BLADEWERX_PUMP_CONTROL_COMMAND = 0x11;
    
    public final static int ANACONDA_SET_GPS = 0x11;
    public final static int ANACONDA_MODE_IDLE = 0x12;
    public final static int ANACONDA_MODE_SEARCH = 0x13;
    public final static int ANACONDA_MODE_STANDBY = 0x14;
    public final static int ANACONDA_MODE_AIRFRAME = 0x15;
    public final static int ANACONDA_MODE_POD = 0x16;
    public final static int ANACONDA_ACTION_DELETE = 0x17;
    public final static int ANACONDA_SET_DATETIME = 0x18;
    public final static int ANACONDA_SET_SERVO_OPENLIMIT = 0x19;
    public final static int ANACONDA_SET_SERVO_CLOSEDLIMIT = 0x1A;
    public final static int ANACONDA_SET_MANIFOLD_HEATERTEMP = 0x1B;
    public final static int ANACONDA_SET_PITOT_HEATERTEMP = 0x1C;
    public final static int ANACONDA_ACTION_SAVE_SETTINGS = 0x1D;
    public final static int ANACONDA_DEBUG_OPT = 0x1E;
    public final static int ANACONDA_SEND_LCDA_G = 0x1F;
    public final static int ANACONDA_SEND_LCDA_H = 0x20;
    public final static int ANACONDA_SEND_LCDB_G = 0x21;
    public final static int ANACONDA_SEND_LCDB_H = 0x22;
    public final static int ANACONDA_STOP_LCDA_G = 0x23;
    public final static int ANACONDA_STOP_LCDA_H = 0x24;
    public final static int ANACONDA_STOP_LCDB_G = 0x25;
    public final static int ANACONDA_STOP_LCDB_H = 0x26;
    public final static int ANACONDA_RESET_SAMPLEUSAGE = 0x27;
    public final static int ANACONDA_RAW = 0xFF;
    
    public final static int ANACONDA_DEBUG_PERMIT_DEBUG = 0x3E;
    public final static int ANACONDA_DEBUG_FORBID_DEBUG = 0x3F;
    public final static int ANACONDA_DEBUG_SERVO_OPEN = 0x40;
    public final static int ANACONDA_DEBUG_SERVO_CLOSED = 0x41;
    public final static int ANACONDA_DEBUG_MANIFOLD_HEATON = 0x42;
    public final static int ANACONDA_DEBUG_MANIFOLD_HEATOFF = 0x43;
    public final static int ANACONDA_DEBUG_PITOT_HEATON = 0x44;
    public final static int ANACONDA_DEBUG_PITOT_HEATOFF = 0x45;
    public final static int ANACONDA_DEBUG_PUMPS_ON = 0x46;
    public final static int ANACONDA_DEBUG_PUMPS_OFF = 0x47;
    public final static int ANACONDA_DEBUG_VALVES1_OPEN = 0x48;
    public final static int ANACONDA_DEBUG_VALVES2_OPEN = 0x49;
    public final static int ANACONDA_DEBUG_VALVES3_OPEN = 0x4A;
    public final static int ANACONDA_DEBUG_VALVES4_OPEN = 0x4B;
    public final static int ANACONDA_DEBUG_VALVES1_CLOSED = 0x4C;
    public final static int ANACONDA_DEBUG_VALVES2_CLOSED = 0x4D;
    public final static int ANACONDA_DEBUG_VALVES3_CLOSED = 0x4E;
    public final static int ANACONDA_DEBUG_VALVES4_CLOSED = 0x4F;
    public final static int ANACONDA_DEBUG_VALVESALL_CLOSED = 0x50;
    public final static int ANACONDA_DEBUG_WRITE_TESTFILES = 0x51;
    public final static int ANACONDA_DEBUG_WRITE_TESTFILE_SYS = 0x54;
    public final static int ANACONDA_DEBUG_RESET_LCDA = 0x60;
    public final static int ANACONDA_DEBUG_RESET_LCDB = 0x61;
    public final static int ANACONDA_DEBUG_RELEASE_RESET_LCDA = 0x62;
    public final static int ANACONDA_DEBUG_RELEASE_RESET_LCDB = 0x63;


    /**
     * Type of command
     */
    protected int commandType;

    
    
    /**
     * 
     * @param sensorType Type of sensor command
     * @param length Data array length, not total byte stream length
     */
    public cbrnPodCommand(int sensorType, int commandType, int length)
    {
        this.sensorType = sensorType;
        this.commandType = commandType;
        this.syncBytes[0] = this.syncBytes[1] = this.syncBytes[2] = (char) SYNC_BYTE;
        HeaderSize = 12;
        
        resetDataLength (length);
    }

    @Override
    public byte[] toByteArray() 
    {
        byte[] byteArray = new byte[length];  
        int indx = pre_toByteArray (byteArray);
        
        post_toByteArray(byteArray, indx);
        
        return byteArray;
    }
    
    public boolean isInstanceOf(cbrnPodCommand m)
    {
        return (m.getCommandType() == this.getCommandType() && m.getSensorType() == this.getSensorType());
    }
    
    public int getCommandType ()
    {
        return commandType;
    }
    
    public String toLogString ()
    {
        return preToLogString () + postToLogString ();
    }
}
