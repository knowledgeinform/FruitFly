package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.CanberraDetectionBelief;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.data.time.Millisecond;

public class CanberraChartPanel extends DynamicTimeSeriesChartPanel implements Updateable
{
    private BeliefManager _belMgr;
    private JLabel lblRate;
    
    /**
     * Create the panel.
     */
    public CanberraChartPanel(BeliefManager mgr)
    {
        super(CanberraChartPanel.class.getSimpleName());

        this.setVisible(true);
        
        setAxisLabel("Radiation Rate (\u00B5G/h)");
        removeLegend();
        
        _belMgr = mgr;
    }

    /**
     * Builds the main XY time series plot.
     */
    @Override
    protected void initChartPanel()
    {
        super.initChartPanel();
        
        createTimeSeries("dose rate");
        setSeriesColor("dose rate", Color.MAGENTA);
        
        TickUnits unitSource = new TickUnits();
        for (int i = 0; i < 5; i++)
        {
            unitSource.add(new NumberTickUnit(0.01 * Math.pow(10, i)));
            unitSource.add(new NumberTickUnit(0.02 * Math.pow(10, i)));
            unitSource.add(new NumberTickUnit(0.03 * Math.pow(10, i)));
            unitSource.add(new NumberTickUnit(0.04 * Math.pow(10, i)));
            unitSource.add(new NumberTickUnit(0.05 * Math.pow(10, i)));
        }
        yaxis.setStandardTickUnits(unitSource);
        
        this.add(Box.createVerticalStrut(10));
        this.add(getDataDisplay());
        this.add(Box.createVerticalStrut(10));
    }
    
    private JPanel getDataDisplay()
    {
        JPanel pan = new JPanel();
        pan.setLayout(new BoxLayout(pan, BoxLayout.X_AXIS));
        pan.setAlignmentX(CENTER_ALIGNMENT);
        pan.setBackground(Color.WHITE);
        pan.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        pan.add(Box.createHorizontalStrut(10));
        pan.add(new JLabel("Current Gamma Rate: "));
        lblRate = new JLabel("N/A");
        lblRate.setFont(new Font(lblRate.getFont().getName(), Font.BOLD, lblRate.getFont().getSize()));
        pan.add(lblRate);
        pan.add(new JLabel(" \u00B5G/h"));
        pan.add(Box.createHorizontalStrut(10));
        
        updateDataDisplayWidth();

        return pan;
    }
    
    private void updateDataDisplayWidth()
    {
        Component panel = lblRate.getParent();
        int width = 0;
        
        for (Component cmp : lblRate.getParent().getComponents())
        {
            width += cmp.getPreferredSize().getWidth();
        }
        
        Dimension size = new Dimension(width, 26);
        panel.setMinimumSize(size);
        panel.setPreferredSize(size);
        panel.setMaximumSize(size);
    }

    @Override
    protected void updateData()
    {
        CanberraDetectionBelief cdb = (CanberraDetectionBelief) _belMgr.get(CanberraDetectionBelief.BELIEF_NAME);
        
        // TEST CODE
//        cdb = new CanberraDetectionBelief();
//        cdb.setFilteredDoseRate(0.015);
        // END TEST CODE
        
        if (cdb != null)
        {
            addDataPoint("dose rate", new Millisecond(new Date(cdb.getTimeStamp().getTime())), cdb.getFilteredDoseRate());
            lblRate.setText(cdb.getFilteredDoseRate() + "");
            updateDataDisplayWidth();
        }
    }
}
