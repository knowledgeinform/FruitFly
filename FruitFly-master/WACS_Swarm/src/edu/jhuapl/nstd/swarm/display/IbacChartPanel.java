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
import edu.jhuapl.nstd.swarm.belief.ParticleDetectionBelief;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;


/**
 *
 * @author fishmsm1
 */
public class IbacChartPanel extends DynamicTimeSeriesChartPanel implements ActionListener, Updateable
{

    private BeliefManager _belMgr;
    private HashMap<String, Boolean> plots;
    private JLabel lblLarge, lblSmall, lblLargeBio, lblSum, lblSmallBio, lblPerBio;
    private double maxPercent;
    private NumberAxis m_PercentBioAxis;
    private JPanel dataDisplay;
    private ArrayList<JPanel> labels;
    private static final String LARGE = "large";
    private static final String SMALL = "small";
    private static final String SUM = "sum";
    private static final String LARGE_BIO = "large bio";
    private static final String SMALL_BIO = "small bio";
    private static final String PERCENT_BIO = "percent bio";

    public IbacChartPanel(BeliefManager mgr)
    {
        super(IbacChartPanel.class.getSimpleName());
        
        XYPlot pl = (XYPlot) getChart().getPlot();
        pl.getRangeAxis().setLabel("Particle Count");

        _belMgr = mgr;
        plots = new HashMap<String, Boolean>();
    }

    @Override
    protected void initChartPanel()
    {
        super.initChartPanel();
        
        createTimeSeries(LARGE);
        changeVisibility(LARGE, false);
        createTimeSeries(SMALL);
        changeVisibility(SMALL, false);
        createTimeSeries(SUM);
        createTimeSeries(LARGE_BIO);
        changeVisibility(LARGE_BIO, false);
        createTimeSeries(SMALL_BIO);
        changeVisibility(SMALL_BIO, false);
        
        maxPercent = 0.3;
        createTimeSeries(PERCENT_BIO);
        NumberAxis axis = new NumberAxis("Bio Percentage");
        axis.setNumberFormatOverride(NumberFormat.getPercentInstance());
        axis.setUpperBound(maxPercent);
        axis.setLowerBound(0);
        axis.setLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        addAxis(PERCENT_BIO, axis);
        changeVisibility(PERCENT_BIO, false);
        m_PercentBioAxis = axis;
        
        removeLegend();

        chartHolder.add(Box.createRigidArea(new Dimension(10, 0)));
        chartHolder.add(getButtonPanel());
        chartHolder.add(Box.createRigidArea(new Dimension(10, 0)));
        
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(getDataDisplay());
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.setMinimumSize(new Dimension(278, 244));
        
        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e)
            {
//                System.out.println(e.getComponent().getWidth() + " x " + e.getComponent().getHeight());
                updateSize(e.getComponent().getWidth());
            }

            @Override
            public void componentMoved(ComponentEvent e) {
//                updateSize(e.getComponent().getWidth());
            }

            @Override
            public void componentShown(ComponentEvent e) {
//                updateSize(e.getComponent().getWidth());
            }

            @Override
            public void componentHidden(ComponentEvent e) {
//                updateSize(e.getComponent().getWidth());
            }
            
            private void updateSize(int width)
            {   
                boolean shrinking;
                int setSize = ((Container) dataDisplay.getComponent(0)).getComponentCount() - 1;
                if (width < dataDisplay.getPreferredSize().getWidth())
                {
                    setSize = setSize - 1;
                    shrinking = true;
                }
                else
                {
                    setSize = setSize + 1;
                    shrinking = false;
                }
                int setCount = (int) Math.ceil((double) labels.size() / setSize);
                
                if (((Container) dataDisplay.getComponent(0)).getComponentCount() - 1 != setSize)
                {
                    while ((shrinking && (width < dataDisplay.getPreferredSize().getWidth()) || (!shrinking && width > setSize * getLargestItemWidth())) && labels.size() > 0)
                    {
                        if (setSize < 2)
                        {
                            return;
                        }

                        //System.out.println("WINDOW RESIZED TO " + width);

                        int pos = 0;

                        dataDisplay.invalidate();
                        dataDisplay.removeAll();

                        for (int i = 0; i < setCount; i++)
                        {
                            JPanel pan = new JPanel();
                            pan.setBackground(Color.WHITE);
                            pan.setLayout(new BoxLayout(pan, BoxLayout.X_AXIS));

                            pan.add(Box.createHorizontalStrut(10));

                            for (int j = 0; j < setSize; j++)
                            {
                                if (pos < labels.size())
                                {
                                    pan.add(labels.get(pos));
                                }

                                pos++;
                            }
                            pan.invalidate();
                            dataDisplay.add(pan);
                        }

                        if (shrinking)
                        {
                            setSize = setSize - 1;
                        }
                        else
                        {
                            setSize = setSize + 1;
                        }
                        setCount = (int) Math.ceil((double) labels.size() / setSize);

                        updateDataDisplaySize();

                        dataDisplay.invalidate();
                        dataDisplay.revalidate();
                        dataDisplay.getParent().invalidate();
                        dataDisplay.getParent().getParent().invalidate();
                    }
                }
            }
        });
    }

    private JPanel getButtonPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JCheckBox bLarge = new JCheckBox("L");
        panel.add(configureButton(bLarge, LARGE));

        JCheckBox bSmall = new JCheckBox("S");
        panel.add(configureButton(bSmall, SMALL));

        JCheckBox bSum = new JCheckBox("TOT");
        panel.add(configureButton(bSum, SUM));
        bSum.setSelected(true);

        JCheckBox bLargeBio = new JCheckBox("LB");
        panel.add(configureButton(bLargeBio, LARGE_BIO));

        JCheckBox bSmallBio = new JCheckBox("SB");
        panel.add(configureButton(bSmallBio, SMALL_BIO));

        JCheckBox bPercentBio = new JCheckBox("%B");
        panel.add(configureButton(bPercentBio, PERCENT_BIO));

        return panel;
    }
    
    private JPanel configureButton(JCheckBox btn, String id)
    {
        LegendBox box = new LegendBox(getSeriesColor(id));
        
        btn.setVerticalTextPosition(AbstractButton.CENTER);
        btn.setHorizontalTextPosition(AbstractButton.RIGHT);
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setActionCommand(id);
        btn.addActionListener(this);
        btn.setBackground(Color.WHITE);
        
        JPanel pan = new JPanel();
        pan.setLayout(new BoxLayout(pan, BoxLayout.X_AXIS));
        pan.setBackground(Color.WHITE);
        pan.add(btn);
        pan.add(box);
        
        pan.setAlignmentX(LEFT_ALIGNMENT);
        
        return pan;
    }
    
    private JPanel getDataDisplay()
    {
        labels = new ArrayList<JPanel>();
        
        dataDisplay = new JPanel();
        dataDisplay.setLayout(new BoxLayout(dataDisplay, BoxLayout.Y_AXIS));
        dataDisplay.setAlignmentX(CENTER_ALIGNMENT);
        dataDisplay.setBackground(Color.WHITE);
        dataDisplay.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JPanel pan = new JPanel();
        pan.setLayout(new BoxLayout(pan, BoxLayout.X_AXIS));
        pan.setAlignmentX(CENTER_ALIGNMENT);
        pan.setBackground(Color.WHITE);
        
        pan.add(Box.createHorizontalStrut(10));
        
        JPanel panLarge = new JPanel();
        panLarge.setLayout(new BoxLayout(panLarge, BoxLayout.X_AXIS));
        panLarge.setAlignmentX(CENTER_ALIGNMENT);
        panLarge.setBackground(Color.WHITE);
        panLarge.add(new JLabel("Large: "));
        lblLarge = new JLabel("N/A");
        lblLarge.setFont(new Font(lblLarge.getFont().getName(), Font.BOLD, lblLarge.getFont().getSize()));
        panLarge.add(lblLarge);
        panLarge.add(new JLabel(" counts"));
        panLarge.add(Box.createRigidArea(new Dimension(10, 0)));
        pan.add(panLarge);
        labels.add(panLarge);
        
        JPanel panSmall = new JPanel();
        panSmall.setLayout(new BoxLayout(panSmall, BoxLayout.X_AXIS));
        panSmall.setAlignmentX(CENTER_ALIGNMENT);
        panSmall.setBackground(Color.WHITE);
        panSmall.add(new JLabel("Small: "));
        lblSmall = new JLabel("N/A");
        lblSmall.setFont(new Font(lblSmall.getFont().getName(), Font.BOLD, lblSmall.getFont().getSize()));
        panSmall.add(lblSmall);
        panSmall.add(new JLabel(" counts"));
        panSmall.add(Box.createRigidArea(new Dimension(10, 0)));
        pan.add(panSmall);
        labels.add(panSmall);
        
        JPanel panSum = new JPanel();
        panSum.setLayout(new BoxLayout(panSum, BoxLayout.X_AXIS));
        panSum.setAlignmentX(CENTER_ALIGNMENT);
        panSum.setBackground(Color.WHITE);
        panSum.add(new JLabel("Sum: "));
        lblSum = new JLabel("N/A");
        lblSum.setFont(new Font(lblSum.getFont().getName(), Font.BOLD, lblSum.getFont().getSize()));
        panSum.add(lblSum);
        panSum.add(new JLabel(" counts"));
        panSum.add(Box.createRigidArea(new Dimension(10, 0)));
        pan.add(panSum);
        labels.add(panSum);
        
        JPanel panLargeBio = new JPanel();
        panLargeBio.setLayout(new BoxLayout(panLargeBio, BoxLayout.X_AXIS));
        panLargeBio.setAlignmentX(CENTER_ALIGNMENT);
        panLargeBio.setBackground(Color.WHITE);
        panLargeBio.add(new JLabel("Large Bio: "));
        lblLargeBio = new JLabel("N/A");
        lblLargeBio.setFont(new Font(lblLargeBio.getFont().getName(), Font.BOLD, lblLargeBio.getFont().getSize()));
        panLargeBio.add(lblLargeBio);
        panLargeBio.add(new JLabel(" counts"));
        panLargeBio.add(Box.createRigidArea(new Dimension(10, 0)));
        pan.add(panLargeBio);
        labels.add(panLargeBio);
        
        JPanel panSmallBio = new JPanel();
        panSmallBio.setLayout(new BoxLayout(panSmallBio, BoxLayout.X_AXIS));
        panSmallBio.setAlignmentX(CENTER_ALIGNMENT);
        panSmallBio.setBackground(Color.WHITE);
        panSmallBio.add(new JLabel("Small Bio: "));
        lblSmallBio = new JLabel("N/A");
        lblSmallBio.setFont(new Font(lblSmallBio.getFont().getName(), Font.BOLD, lblSmallBio.getFont().getSize()));
        panSmallBio.add(lblSmallBio);
        panSmallBio.add(new JLabel(" counts"));
        panSmallBio.add(Box.createRigidArea(new Dimension(10, 0)));
        pan.add(panSmallBio);
        labels.add(panSmallBio);
        
        
        JPanel panPerBio = new JPanel();
        panPerBio.setLayout(new BoxLayout(panPerBio, BoxLayout.X_AXIS));
        panPerBio.setAlignmentX(CENTER_ALIGNMENT);
        panPerBio.setBackground(Color.WHITE);
        panPerBio.add(new JLabel("Percent Bio: "));
        lblPerBio = new JLabel("N/A");
        lblPerBio.setFont(new Font(lblPerBio.getFont().getName(), Font.BOLD, lblPerBio.getFont().getSize()));
        panPerBio.add(lblPerBio);
        panPerBio.add(new JLabel("%"));
        panPerBio.add(Box.createRigidArea(new Dimension(10, 0)));
        pan.add(panPerBio);
        labels.add(panPerBio);
        
        dataDisplay.add(pan);
        
        updateDataDisplaySize();

        return dataDisplay;
    }        
    
    private void updateDataDisplaySize()
    {
        int maxWidth = 0;
        int height = 12;
        
        for (Component line : dataDisplay.getComponents())
        {
            int width = 0;

            for (Component cmp : ((Container) line).getComponents())
            {
                width += cmp.getPreferredSize().getWidth();
            }
            
            if (width > maxWidth)
                maxWidth = width;
            
            height += line.getPreferredSize().getHeight();
        }
        
        Dimension size = new Dimension(maxWidth, height);
        dataDisplay.setMinimumSize(size);
        dataDisplay.setPreferredSize(size);
        dataDisplay.setMaximumSize(size);
       
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        super.actionPerformed(e);
        
        if (e.getSource() instanceof JToggleButton)
        {
            JToggleButton btn = (JToggleButton) e.getSource();

            boolean state = btn.isSelected();
            if (e.getActionCommand().equals(LARGE))
            {
                changeVisibility(LARGE, state);
            }
            else if (e.getActionCommand().equals(SMALL))
            {
                changeVisibility(SMALL, state);
            }
            else if (e.getActionCommand().equals(SUM))
            {
                changeVisibility(SUM, state);
            }
            else if (e.getActionCommand().equals(LARGE_BIO))
            {
                changeVisibility(LARGE_BIO, state);
            }
            else if (e.getActionCommand().equals(SMALL_BIO))
            {
                changeVisibility(SMALL_BIO, state);
            }
            else if (e.getActionCommand().equals(PERCENT_BIO))
            {
                changeVisibility(PERCENT_BIO, state);
            }
            else
            {
                throw new UnsupportedOperationException("Action not supported");
            }
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

    private void updateChart()
    {
        synchronized (this)
        {
            ParticleDetectionBelief blf = (ParticleDetectionBelief) _belMgr.get(ParticleDetectionBelief.BELIEF_NAME);
            if (blf != null)
            {
                int large = blf.getLCI();
                int small = blf.getSCI();
                int sum = large + small;
                int large_bio = blf.getBLCI();
                int small_bio = blf.getBSCI();
                double per_bio;
                if (large + small > 0)
                    per_bio = blf.getBioPercent();
                else
                    per_bio = 0;
                per_bio /= 100.0;
                
                addDataPoint(LARGE, new Millisecond(new Date(blf.getTimeStamp().getTime())), large);
                lblLarge.setText(Integer.toString(large));
                addDataPoint(SMALL, new Millisecond(new Date(blf.getTimeStamp().getTime())), small);
                lblSmall.setText(Integer.toString(small));
                addDataPoint(SUM, new Millisecond(new Date(blf.getTimeStamp().getTime())), sum);
                lblSum.setText(Integer.toString(sum));
                addDataPoint(LARGE_BIO, new Millisecond(new Date(blf.getTimeStamp().getTime())), large_bio);
                lblLargeBio.setText(Integer.toString(large_bio));
                addDataPoint(SMALL_BIO, new Millisecond(new Date(blf.getTimeStamp().getTime())), small_bio);
                lblSmallBio.setText(Integer.toString(small_bio));
                addDataPoint(PERCENT_BIO, new Millisecond(new Date(blf.getTimeStamp().getTime())), per_bio);
                lblPerBio.setText((int)(per_bio) + "");
                
                updateDataDisplaySize();
                
                if(per_bio > maxPercent)
                {
                    maxPercent = per_bio;
                    m_PercentBioAxis.setUpperBound(maxPercent + 0.02);
                }
            }
           
            // TEST CODE
//            long time = System.currentTimeMillis();
//            int large = (int)(10026 + Math.random() * 200);
//            int small = (int)(500 + Math.random() * 100);
//            int sum = small + large;
//            int large_bio = (int)(50 + Math.random() * 50);
//            int small_bio = (int)(100 + Math.random() * 50);
//            double per_bio = 0.05 + Math.random() * 0.05;
//            
//            addDataPoint(LARGE, new Millisecond(new Date(time)), large);
//            lblLarge.setText(Integer.toString(large));
//            addDataPoint(SMALL, new Millisecond(new Date(time)), small);
//            lblSmall.setText(Integer.toString(small));
//            addDataPoint(SUM, new Millisecond(new Date(time)), sum);
//            lblSum.setText(Integer.toString(sum));
//            addDataPoint(LARGE_BIO, new Millisecond(new Date(time)), large_bio);
//            lblLargeBio.setText(Integer.toString(large_bio));
//            addDataPoint(SMALL_BIO, new Millisecond(new Date(time)), small_bio);
//            lblSmallBio.setText(Integer.toString(small_bio));
//            addDataPoint(PERCENT_BIO, new Millisecond(new Date(time)), per_bio);
//            lblPerBio.setText((int)(per_bio * 100) + "");
            // END TEST CODE
        }
    }
    
    private int getLargestItemWidth()
    {
        int width = 0;
        
        for (JPanel item : labels)
        {
            if (item.getPreferredSize().getWidth() > width)
                width = (int)item.getPreferredSize().getWidth();
        }
        
        return width;
    }
}
class LegendBox extends JComponent {
    
    private Color boxColor = Color.BLACK;
    private Dimension size = new Dimension(20, 20);
    
    public LegendBox() {this.setBackground(Color.WHITE);}
    public LegendBox(Color c)
    {
        super();
        
        boxColor = c;
        this.setBackground(Color.WHITE);
    }
    
    public void setColor(Color c)
    {
        boxColor = c;
    }
    
    public Color getColor()
    {
        return boxColor;
    }

    @Override
    public Dimension getMinimumSize() {
        return size;
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    @Override
    public Dimension getMaximumSize() {
        return size;
    }

    @Override
    public void paintComponent(Graphics g) {
        int margin = 5;
        Dimension dim = getSize();
        super.paintComponent(g);
        g.setColor(boxColor);
        g.fillRect(margin, margin, dim.width - margin * 2, dim.height - margin * 2);
    }
}
