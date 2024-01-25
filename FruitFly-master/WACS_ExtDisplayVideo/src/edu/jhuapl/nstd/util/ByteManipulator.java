package edu.jhuapl.nstd.util;

import java.io.DataInput;
import java.io.IOException;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;

public class ByteManipulator {

	protected static void swap(byte[] orig, int start, int length) {
		int mid = length / 2;
		byte tmp;
		for (int i=0; i<mid; i++) {
			tmp = orig[start+i];
			orig[start+i] = orig[start+length-1-i];
			orig[start+length-1-i] = tmp;
		}
	}

	public static int addString(byte[] array, String toAdd, int index, boolean swap) {
		byte[] bytes = toAdd.getBytes();

		index = addShort(array, (short)toAdd.length(), index, swap);
		for (int i = 0; i < bytes.length; i++) {
			array[index + i] = bytes[i];
		}
		return index + bytes.length;
	}

	public static String getString(byte[] array, int index, boolean swap) {
		int size = getShort(array, index, swap);
		index += 2;
		return new String(array, index, size);
	}

	/** 
	 * A special string reader for data input, we need this to be able 
	 * to easily switch up our string representation if we need to. This allows
	 * us to not have to create buffers for reading in the strings that are
	 * larger than neccessary.
	 */
	public static String getString(DataInput in, boolean swap) throws IOException {
		byte[] lenBytes = new byte[2];
		in.readFully(lenBytes, 0, 2);
		short strLen = ByteManipulator.getShort(lenBytes, 0, false);

		byte[] strBytes = new byte[strLen];
		in.readFully(strBytes);
		return new String(strBytes);		
	}

	public static int addLatLonAltPosition(
			byte[] array, AbsolutePosition pos, int index, boolean swap)
	{
		LatLonAltPosition lla = pos.asLatLonAltPosition();
		float lat = (float)lla.getLatitude().getDoubleValue(Angle.DEGREES);
		float lon = (float)lla.getLongitude().getDoubleValue(Angle.DEGREES);
		float alt = (float)lla.getAltitude().getDoubleValue(Length.METERS);

		index = ByteManipulator.addFloat(array, lat, index, false);
		index = ByteManipulator.addFloat(array, lon, index, false);
		index = ByteManipulator.addFloat(array, alt, index, false);

		return index;
	}

	public static LatLonAltPosition getLatLonAltPosition(
			byte[] array, int index, boolean swap) 
	{
		float lat = ByteManipulator.getFloat(array, index, false);
		index += 4;
		float lon = ByteManipulator.getFloat(array, index, false);
		index += 4;
		float alt = ByteManipulator.getFloat(array, index, false);

		LatLonAltPosition pos = new LatLonAltPosition(
				new Latitude(lat, Angle.DEGREES),
				new Longitude(lon, Angle.DEGREES),
				new Altitude(alt, Length.METERS));
		return pos;
	}

	public static int addInt(byte[] array, int toAdd, int index, boolean swap) {
		array[index] = (byte)((toAdd >>> 24) & 0xFF);
		array[index+1] = (byte)((toAdd >>> 16) & 0xFF);
		array[index+2] = (byte)((toAdd >>> 8) & 0xFF);
		array[index+3] = (byte)((toAdd >>> 0) & 0xFF);

		if (swap) 
			swap(array, index, 4);
		return index += 4;
	}

	public static int addShort(byte[] array, short toAdd, int index, boolean swap) {
		array[index] = (byte)((toAdd >>> 8) & 0xFF);
		array[index+1] = (byte)((toAdd >>> 0) & 0xFF);

		if (swap) 
			swap(array, index, 2);
		return index += 2;
	}

	public static int addByte(byte[] array, int toAdd, int index, boolean swap) {
		array[index] = (byte)((toAdd >>> 0) & 0xFF);
		return index +=1;
	}

	public static int addSignedByte(byte[] array, byte toAdd, int index, boolean swap) {
		array[index] = toAdd;
		return index += 1;
	}

	public static int addLong(byte[] array, long toAdd, int index, boolean swap) {
		array[index] = (byte)((toAdd >>> 56) & 0xFF);
		array[index+1] = (byte)((toAdd >>> 48) & 0xFF);
		array[index+2] = (byte)((toAdd >>> 40) & 0xFF);
		array[index+3] = (byte)((toAdd >>> 32) & 0xFF);
		array[index+4] = (byte)((toAdd >>> 24) & 0xFF);
		array[index+5] = (byte)((toAdd >>> 16) & 0xFF);
		array[index+6] = (byte)((toAdd >>> 8) & 0xFF);
		array[index+7] = (byte)((toAdd >>> 0) & 0xFF);

		if (swap) 
			swap(array, index, 8);
		return index += 8;
	}

	public static int addFloat(byte[] array, float toAdd, int index, boolean swap) {
		return addInt(array, Float.floatToIntBits(toAdd), index, swap);
	}

	public static int addDouble(byte[] array, double toAdd, int index, boolean swap) {
		return addLong(array, Double.doubleToLongBits(toAdd), index, swap);
	}

	public static byte getByte(byte[] array, int index, boolean swap) {
		return array[index];
	}

	public static short getShort(byte[] array, int index, boolean swap) {
		short toReturn;
		if (swap) 
			toReturn = (short)(((array[index+1] & 0xFF) << 8) + ((array[index] & 0xFF) << 0));
		else
			toReturn = (short)(((array[index] & 0xFF) << 8) + ((array[index+1] & 0xFF) << 0));
		return toReturn;
	}

	public static int getInt(byte[] array, int index, boolean swap) {
		int toReturn;
		if (swap) 
			toReturn = (((array[index+3] & 0xFF) << 24) 
								+ ((array[index+2] & 0xFF) << 16)
								+ ((array[index+1] & 0xFF) << 8)
								+ ((array[index] & 0xFF) << 0));
		else
			toReturn = (((array[index] & 0xFF) << 24) 
								+ ((array[index+1] & 0xFF) << 16)
								+ ((array[index+2] & 0xFF) << 8)
								+ ((array[index+3] & 0xFF) << 0));
		return toReturn;
	}

	public static long getLong(byte[] array, int index, boolean swap) {
		long toReturn;
		if (swap) 
			toReturn = (((long)(((array[index+7] & 0xFF) << 24) 
								+ ((array[index+6] & 0xFF) << 16)
								+ ((array[index+5] & 0xFF) << 8)
								+ ((array[index+4] & 0xFF) << 0))) << 32)
								+ ((((array[index+3] & 0xFF) << 24)
								+ ((array[index+2] & 0xFF) << 16)
								+ ((array[index+1] & 0xFF) << 8)
								+ ((array[index] & 0xFF) << 0)) & 0xFFFFFFFFL);

		else
			toReturn = (((long)(((array[index] & 0xFF) << 24) 
								+ ((array[index+1] & 0xFF) << 16)
								+ ((array[index+2] & 0xFF) << 8)
								+ ((array[index+3] & 0xFF) << 0))) << 32)
								+ ((((array[index+4] & 0xFF) << 24)
								+ ((array[index+5] & 0xFF) << 16)
								+ ((array[index+6] & 0xFF) << 8)
								+ ((array[index+7] & 0xFF) << 0)) & 0xFFFFFFFFL);
		return toReturn;
	}

	public static float getFloat(byte[] array, int index, boolean swap) {
		return Float.intBitsToFloat(getInt(array,index,swap));
	}

	public static double getDouble(byte[] array, int index, boolean swap) {
		return Double.longBitsToDouble(getLong(array,index,swap));
	}


		
	public static String byteArrayToHexString(byte in[], int length) {
        byte ch = 0x00;
        int i = 0; 
        if (in == null || in.length <= 0)
            return null;
        
        String pseudo[] = {"0", "1", "2",
                "3", "4", "5", "6", "7", "8",
                "9", "A", "B", "C", "D", "E",
        "F"};
        StringBuffer out = new StringBuffer(in.length * 2);
        
        while (i < length) {
            ch = (byte) (in[i] & 0xF0); // Strip off
            
            ch = (byte) (ch >>> 4);
            // shift the bits down
            ch = (byte) (ch & 0x0F);    
            //must do this is high order bit is on!
            out.append(pseudo[ (int) ch]); // convert the
           
            ch = (byte) (in[i] & 0x0F); // Strip off
            
            out.append(pseudo[ (int) ch]); // convert the
           
            i++;
        }
        String rslt = new String(out);
        return rslt;
    }    

	 public static String byteArrayToCharString(byte in[], int length) {
		byte ch = 0x00;
		int i = 0; 
        if (in == null || in.length <= 0)
            return null;
        
        StringBuffer out = new StringBuffer(in.length);
        
        while (i < length) {
            ch = in[i++];

				if (ch >= 32 && ch < 127) 
					out.append((char)ch);
				else
					out.append('.');
        }
        return out.toString();
	 }



}



