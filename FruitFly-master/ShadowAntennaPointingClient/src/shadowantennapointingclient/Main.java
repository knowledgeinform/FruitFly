package shadowantennapointingclient;

public class Main
{
    public static void main(String[] args)
    {
        ShadowTelemetryListener shadowTelemetryListener = new ShadowTelemetryListener();
        shadowTelemetryListener.start();

        ShadowTelemetryListener.TelemetryMessage telemetryMessage = new ShadowTelemetryListener.TelemetryMessage();
        while (true)
        {            
            shadowTelemetryListener.copyLatestTelemetryMessage(telemetryMessage);

            System.out.println("Latitude: " + Math.toDegrees(telemetryMessage.latitude_rad));
            System.out.println("Longitude: " + Math.toDegrees(telemetryMessage.longitude_rad));
            System.out.println("Altitude(m): " + telemetryMessage.gpsAltitude_m);
            System.out.println();

            try
            {
                Thread.sleep(500);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
