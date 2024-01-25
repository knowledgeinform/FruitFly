//=============================== UNCLASSIFIED ==================================
//
// Copyright (c) 1999 The Johns Hopkins University/Applied Physics Laboratory
// Developed by JHU/APL.
//
// This material may be reproduced by or for the U.S. Government pursuant to
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988).
// For any other permissions please contact JHU/APL.
//
//=============================== UNCLASSIFIED ==================================
package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.GammaStatisticsBelief;
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
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;

/**
 * Displays the gamma rate put out by the bridgeport sensor versus time.
 * 
 * @author fishmsm1
 */
public class BridgeportChartPanel extends DynamicTimeSeriesChartPanel implements Updateable
{
    private BeliefManager _belMgr;
    private JLabel lblValue;
    
    public BridgeportChartPanel(BeliefManager mgr)
    {
        super(BridgeportChartPanel.class.getSimpleName());

        _belMgr = mgr;
        
        XYPlot pl = (XYPlot) getChart().getPlot();
        pl.getRangeAxis().setLabel("Radiation Rate (count/s)");
        
        removeLegend();
    }
    
    @Override
    protected void initChartPanel()
    {
        super.initChartPanel();
        
        createTimeSeries("gamma count rate");
        setSeriesColor("gamma count rate", Color.RED);
        
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(getDataDisplay());
        this.add(Box.createHorizontalGlue());
        this.add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    private JPanel getDataDisplay()
    {
        JPanel pan = new JPanel();
        pan.setLayout(new BoxLayout(pan, BoxLayout.X_AXIS));
        pan.setAlignmentX(CENTER_ALIGNMENT);
        pan.setBackground(Color.WHITE);
        pan.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        pan.add(Box.createRigidArea(new Dimension(10, 0)));
        pan.add(new JLabel("Current Gamma Rate: "));
        lblValue = new JLabel("N/A");
        lblValue.setFont(new Font(lblValue.getFont().getName(), Font.BOLD, lblValue.getFont().getSize()));
        pan.add(lblValue);
        pan.add(new JLabel(" count/s"));
        pan.add(Box.createRigidArea(new Dimension(10, 0)));
        
        updateDataDisplayWidth();
        
        return pan;
    }
    
    private void updateDataDisplayWidth()
    {
        Component panel = lblValue.getParent();
        int width = 0;
        
        for (Component cmp : lblValue.getParent().getComponents())
        {
            width += cmp.getPreferredSize().getWidth();
        }
        
        Dimension size = new Dimension(width, 26);
        panel.setMinimumSize(size);
        panel.setPreferredSize(size);
        panel.setMaximumSize(size);
    }
    
    private void updateLabel(Float newValue)
    {
        if (lblValue != null)
        {
            lblValue.setText(String.format("%.2f", newValue));
            updateDataDisplayWidth();
        }
    }
    
    @Override
    protected void updateData()
    {
        try
        {
            updateChart();
        }
        catch (Exception e)
        {
            System.err.println("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }
    
//    double x = 0; // TEST VARIABLE
    private void updateChart()
    {
        synchronized (this)
        {
            // TEST CODE
//            GammaStatisticsBelief blf1 = new GammaStatisticsBelief("1", new Date());
//            if (x % 5 == 0)
//            {
//            if (x > 100)
//                x = 0;
//            if (x < 40 || x > 71)
//                blf1.setEventRate((float)Math.random() + 2);
//            else if (x < 55)
//                blf1.setEventRate((float)Math.random()*2 + (float)Math.exp((x - 40)/10.0));
//            else if (x < 57)
//                blf1.setEventRate((float)Math.random()*2 + (float)Math.exp(15.0/10.0));
//            else
//                blf1.setEventRate((float)Math.random()*2 + (float)Math.exp((71 - x)/10.0));
//            
//            _belMgr.put(blf1);
//            }
//            x++;
            // END TEST CODE
            
            GammaStatisticsBelief blf = (GammaStatisticsBelief) _belMgr.get(GammaStatisticsBelief.BELIEF_NAME);
            
            if (blf != null)
            {
                float rate = blf.getEventRate();
                Millisecond timestamp = new Millisecond(new Date(blf.getTimeStamp().getTime()));
                addDataPoint("gamma count rate", timestamp, rate);
                updateLabel(rate);
            }
        }
    }
}
