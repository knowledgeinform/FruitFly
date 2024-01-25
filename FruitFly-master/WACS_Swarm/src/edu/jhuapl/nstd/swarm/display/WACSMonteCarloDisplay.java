package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.jlib.jgeo.JGeoCanvas;
import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.util.Config;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JSplitPane;

public class WACSMonteCarloDisplay implements Updateable
{
    protected BeliefManager m_beliefManager;
    protected AgentTracker m_agentTracker;
    protected AltitudeChartPanel m_altitudePanel;
    protected SearchCanvas m_searchCanvas;

    public WACSMonteCarloDisplay(BeliefManager beliefManager)
    {
        m_beliefManager = beliefManager;

        JFrame frame = new JFrame("Monte Carlo Display");
        frame.addWindowListener(new WindowAdapter()
                                {
                                    @Override
                                    public void windowClosing(WindowEvent e)
                                    {
                                        System.exit(0);
                                    }
                                });

        m_agentTracker = new AgentTracker(beliefManager);
        m_altitudePanel = new AltitudeChartPanel(beliefManager);
        m_altitudePanel.setPreferredSize(new Dimension(600, 125));
        m_altitudePanel.setMinimumSize(new Dimension(400, 100));
        m_searchCanvas = createCanvas(beliefManager, frame);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                            m_searchCanvas, m_altitudePanel);
        splitPane.setResizeWeight(1.0);
        splitPane.setOneTouchExpandable(true);

        frame.getContentPane().add(splitPane);

        frame.pack();
        frame.setVisible(true);

        Latitude plumeStartLatitude = new Latitude(Config.getConfig().getPropertyAsDouble("MonteCarlo.plumeStartLatitude_deg"), Angle.DEGREES);
        Longitude plumeStartLongitude = new Longitude(Config.getConfig().getPropertyAsDouble("MonteCarlo.plumeStartLongitude_deg"), Angle.DEGREES);
        LatLonAltPosition plumeStartLocation = new LatLonAltPosition(plumeStartLatitude, plumeStartLongitude, Altitude.ZERO);
        m_searchCanvas.setViewCenter(plumeStartLocation);

        frame.setSize(frame.getWidth(), frame.getHeight() + 1);
    }

    protected SearchCanvas createCanvas(BeliefManager belMgr, JFrame owner)
    {
        Latitude plumeStartLatitude = new Latitude(Config.getConfig().getPropertyAsDouble("MonteCarlo.plumeStartLatitude_deg"), Angle.DEGREES);
        Longitude plumeStartLongitude = new Longitude(Config.getConfig().getPropertyAsDouble("MonteCarlo.plumeStartLongitude_deg"), Angle.DEGREES);
        LatLonAltPosition plumeStartLocation = new LatLonAltPosition(plumeStartLatitude, plumeStartLongitude, Altitude.ZERO);
        LatLonAltPosition viewCenter = plumeStartLocation;
        Length viewRange = new Length(2000.0, Length.MILES);
        return new SearchCanvas(5,
                                viewRange,
                                1100,
                                600,
                                JGeoCanvas.ORTHOGRAPHIC_PROJECTION,
                                viewCenter,
                                belMgr,
                                null,
                                owner,
                                m_agentTracker);
    }

    @Override
    public synchronized void update()
    {
        try
        {
            m_agentTracker.update();
            m_searchCanvas.update();
            m_altitudePanel.update();
        }
        catch (Exception e)
        {
            System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }
}