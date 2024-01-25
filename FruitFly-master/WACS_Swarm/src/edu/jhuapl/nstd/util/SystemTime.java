package edu.jhuapl.nstd.util;

/**
 *
 * @author buckar1
 */
public class SystemTime
{
    //
    // Since java doesn't allow us to set system time directly, we have to
    // use a JNI dll.
    //
    public static native void JNISetLocalTime(String timeString);
    public static native String JNIGetSystemTime();

    static
    {
        try
        {
            System.loadLibrary("JNI_SystemTime");
        }
        catch (UnsatisfiedLinkError e)
        {
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getMessage());
        }
    }
}
