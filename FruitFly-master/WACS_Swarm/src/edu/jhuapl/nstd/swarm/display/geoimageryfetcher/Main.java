package edu.jhuapl.nstd.swarm.display.geoimageryfetcher;

public class Main
{
    public static void main(String[] args)
    {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GeoImageryTestFrame().setVisible(true);
            }
        });
    }
}
