package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaLCDReportMessage.AnacondaDataPair;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.belief.AnacondaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.util.Config;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeEventType;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.Millisecond;
import org.jfree.ui.RectangleEdge;

/**
 *
 * @author fishmsm1
 */
public class AnacondaChartPanel extends DynamicTimeSeriesChartPanel implements Updateable, ActionListener
{

    BeliefManager _belMgr;
    JCheckBox bLcda;
    JCheckBox bLcdb;
    private static final String LCDA = "LCDA";
    private static final String LCDB = "LCDB";
//    private static final String SUM = "both";

    public AnacondaChartPanel(BeliefManager mgr)
    {
        super(AnacondaChartPanel.class.getSimpleName());

        _belMgr = mgr;
        
        setAxisLabel("Bars");

        LegendItemSource source = new LegendItemSource()
        {
            public LegendItemCollection getLegendItems()
            {
                HashSet<String> items = new HashSet<String>();

                LegendItemCollection old = getChart().getPlot().getLegendItems();
                LegendItemCollection lic = new LegendItemCollection();
                Iterator<LegendItem> iter = old.iterator();
                while (iter.hasNext())
                {
                        LegendItem item = iter.next();
                        if (!items.contains(item.getLabel()))
                        {
                            items.add(item.getLabel());
                            LegendItem newItem = new LegendItem(item.getLabel(), item.getDescription(), item.getToolTipText(), item.getURLText(), new Rectangle(10, 10), item.getFillPaint());
                            
                            lic.add(newItem);
                        }
                }
                
                return lic;
            }
        };
        getChart().removeLegend();
        LegendTitle legend = new LegendTitle(source);
        legend.setPosition(RectangleEdge.BOTTOM);
        legend.setBorder(1, 1, 1, 1);
        getChart().addLegend(legend);
    }
    
    @Override
    protected void initChartPanel()
    {
        super.initChartPanel();
        
        XYPlot pl = (XYPlot) getChart().getPlot();
        pl.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        createTimeSeriesCollection(LCDA);
        createTimeSeriesCollection(LCDB);
        BasicStroke stroke = new BasicStroke(
            LINE_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
            1.0f, new float[] {6.0f, 6.0f}, 0.0f
        );
        setTimeSeriesCollectionStroke(LCDB, stroke);
//        createTimeSeriesCollection(SUM);
        
        changeVisibility(null, LCDA, true); // Turns off all series in the LCDA collection
        changeVisibility(null, LCDB, true); // Turns off all series in the LCDB collection
//        changeVisibility(null, SUM, true); // Turns on all series in SUM collection
        
        this.add(Box.createVerticalStrut(10));
        this.add(getButtonPanel());
        this.add(Box.createVerticalStrut(10));
    }

    private JPanel getButtonPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        bLcda = new JCheckBox(LCDA);
        bLcda.setVerticalTextPosition(AbstractButton.CENTER);
        bLcda.setHorizontalTextPosition(AbstractButton.LEFT);
        bLcda.setActionCommand(LCDA);
        bLcda.addActionListener(this);
        bLcda.setSelected(true);
        bLcda.setBackground(Color.WHITE);

        bLcdb = new JCheckBox(LCDB + " (dashed)");
        bLcdb.setVerticalTextPosition(AbstractButton.CENTER);
        bLcdb.setHorizontalTextPosition(AbstractButton.LEFT);
        bLcdb.setActionCommand(LCDB);
        bLcdb.addActionListener(this);
        bLcdb.setSelected(true);
        bLcdb.setBackground(Color.WHITE);

        panel.add(Box.createHorizontalStrut(10));
        panel.add(bLcda);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(bLcdb);
        panel.add(Box.createHorizontalStrut(10));
        
        int panWidth = 0;
        for (Component cmp : panel.getComponents())
        {
            panWidth += cmp.getPreferredSize().getWidth();
        }
        Dimension size = new Dimension(panWidth, 26);
        panel.setMinimumSize(size);
        panel.setPreferredSize(size);
        panel.setMaximumSize(size);
        
        return panel;
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        super.actionPerformed(e);

        if (bLcda.isSelected() && bLcdb.isSelected())
        {
            changeVisibility(null, LCDA, true); // Turns off all series in the LCDA collection
            changeVisibility(null, LCDB, true); // Turns off all series in the LCDB collection
//            changeVisibility(null, SUM, true); // Turns on all series in SUM collection
        }
        else if (bLcda.isSelected())
        {
            changeVisibility(null, LCDA, true);
            changeVisibility(null, LCDB, false);
//            changeVisibility(null, SUM, false); 
        }
        else if (bLcdb.isSelected())
        {
            changeVisibility(null, LCDA, false);
            changeVisibility(null, LCDB, true);
//            changeVisibility(null, SUM, false);
        }
        else
        {
            changeVisibility(null, LCDA, false);
            changeVisibility(null, LCDB, false);
//            changeVisibility(null, SUM, false); 
        }
    }

    @Override
    protected void updateData()
    {
        AnacondaDetectionBelief blf = (AnacondaDetectionBelief) _belMgr.get(AnacondaDetectionBelief.BELIEF_NAME);
        
        // TEST CODE
//        AnacondaDataPair pair1 =  new AnacondaDataPair();
//        pair1.agentID = 1;
//        pair1.bars = 5;
//        
//        AnacondaDataPair pair2 =  new AnacondaDataPair();
//        pair2.agentID = 7;
//        pair2.bars = 2;
//        
//        AnacondaDataPair pair3 =  new AnacondaDataPair();
//        pair3.agentID = 1;
//        pair3.bars = 2;
//        
//        AnacondaDataPair pair4 =  new AnacondaDataPair();
//        pair4.agentID = 8;
//        pair4.bars = 3;
//        AnacondaDataPair[] lcda0 = {pair1, pair2};
//        AnacondaDataPair[] lcdb0 = {pair3, pair4};
//        
//        blf = new AnacondaDetectionBelief("wacsagent", lcda0, lcdb0);
        // END TEST CODE
        
        if (blf != null)
        {
            AnacondaDataPair[] lcda = blf.getLcda();
            AnacondaDataPair[] lcdb = blf.getLcdb();
//            HashMap<String, Integer> totals = new HashMap<String, Integer>();
            
            if (lcda == null || lcda.length == 0)
            {
                for (int i = 0; i < datasetMap.get(LCDA).dataset.getSeriesCount(); i++)
                {
                    addDataPoint((String)datasetMap.get(LCDA).dataset.getSeriesKey(i), LCDA, new Millisecond(new Date(blf.getTimeStamp().getTime())), 0);
                }
            }
            else
            {
                for (AnacondaDataPair pair : lcda)
                {
                    String agentName = Config.getConfig().getProperty("podAction.anaconda.agent" + pair.agentID, Integer.toString(pair.agentID));
                    addDataPoint(agentName, LCDA, new Millisecond(new Date(blf.getTimeStamp().getTime())), pair.bars);
                    matchColor(agentName, LCDA, agentName, LCDB);
    //                totals.put(agentName, pair.bars);
                }
            }
            
            if (lcdb == null || lcdb.length == 0)
            {
                for (int i = 0; i < datasetMap.get(LCDB).dataset.getSeriesCount(); i++)
                {
                    addDataPoint((String)datasetMap.get(LCDB).dataset.getSeriesKey(i), LCDB, new Millisecond(new Date(blf.getTimeStamp().getTime())), 0);
                }
            }
            else
            {
                for (AnacondaDataPair pair : lcdb)
                {
                    String agentName = Config.getConfig().getProperty("podAction.anaconda.agent" + pair.agentID, Integer.toString(pair.agentID));
                    addDataPoint(agentName, LCDB, new Millisecond(new Date(blf.getTimeStamp().getTime())), pair.bars);
                    matchColor(agentName, LCDB, agentName, LCDA);

    //                if (totals.containsKey(agentName))
    //                    totals.put(agentName, totals.get(agentName) + pair.bars);
    //                else
    //                    totals.put(agentName, pair.bars);
                }
            }
            
//            for (String agent : totals.keySet())
//            {
//                addDataPoint(agent, SUM, new Millisecond(new Date()), totals.get(agent));
//            }
        }
    }
}
