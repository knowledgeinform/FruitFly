
package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.belief.BladewerxStatisticsBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.CountItem;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jfree.data.time.Millisecond;

/**
 * Displays a history of the Alpha count rate
 * 
 * @author fishmsm1
 */
public class AlphaChartPanel extends DynamicTimeSeriesChartPanel implements Updateable, ActionListener
{
    private BeliefManager _belMgr;
    private JComboBox boxUnits;
    private JLabel lblRate;
    private JLabel lblUnits;
    private boolean inMinutes;
    
    private final static String AXIS_LABEL = "Radiation Rate";
    
    public AlphaChartPanel(BeliefManager mgr)
    {
        super(AlphaChartPanel.class.getSimpleName());
        
        _belMgr = mgr;
        
        inMinutes = false;
        
        setAxisLabel(AXIS_LABEL + " (count/s)");
        
        removeLegend();
    }
    
    @Override
    protected void initChartPanel()
    {
        super.initChartPanel();
        
        createTimeSeries("count rate");
        setSeriesColor("count rate", Color.ORANGE);
        
//        chartHolder.add(Box.createRigidArea(new Dimension(10, 0)));
//        chartHolder.add(getOptionPanel());
//        chartHolder.add(Box.createRigidArea(new Dimension(10, 0)));
        
        JPanel pan = new JPanel();
        pan.setLayout(new BoxLayout(pan, BoxLayout.X_AXIS));
        pan.setBackground(Color.WHITE);
        
        pan.add(Box.createRigidArea(new Dimension(10, 10)));
        pan.add(getDataDisplay());
//        pan.add(Box.createHorizontalGlue());
//        pan.add(getOptionPanel());
        pan.add(Box.createRigidArea(new Dimension(10, 10)));
        
        this.add(Box.createVerticalStrut(10));
        this.add(pan);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    private JPanel getOptionPanel()
    {
        JPanel pan = new JPanel();
        pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
        pan.setBackground(Color.WHITE);
        //pan.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        Dimension dims = new Dimension(160, 22);
        pan.setMaximumSize(dims);
        pan.setMinimumSize(dims);
        pan.setPreferredSize(dims);
        
        JPanel jp1 = new JPanel();
        jp1.setLayout(new BoxLayout(jp1, BoxLayout.X_AXIS));
        jp1.setBackground(Color.WHITE);
        
        jp1.add(new JLabel("Units: "));
        jp1.add(Box.createRigidArea(new Dimension(10, 0)));
        
        boxUnits = new JComboBox();
        boxUnits.addItem("Per Second");
        boxUnits.addItem("Per Minute");
        boxUnits.setActionCommand("units");
        boxUnits.addActionListener(this);
        boxUnits.setMaximumSize(new Dimension(60, 20));
        jp1.add(boxUnits);
        jp1.add(Box.createRigidArea(new Dimension(20, 0)));
        
        pan.add(jp1);
        
        return pan;
    }
    
    private JPanel getDataDisplay()
    {
        JPanel pan = new JPanel();
        pan.setLayout(new BoxLayout(pan, BoxLayout.X_AXIS));
        pan.setAlignmentX(CENTER_ALIGNMENT);
        pan.setBackground(Color.WHITE);
        pan.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        pan.add(Box.createRigidArea(new Dimension(10, 0)));
        pan.add(new JLabel("Current Alpha Rate: "));
        lblRate = new JLabel("N/A");
        lblRate.setFont(new Font(lblRate.getFont().getName(), Font.BOLD, lblRate.getFont().getSize()));
        pan.add(lblRate);
        lblUnits = new JLabel(" count/s");
        pan.add(lblUnits);
        pan.add(Box.createRigidArea(new Dimension(10, 0)));
        
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
        // TEST CODE
//        BladewerxStatisticsBelief test = new BladewerxStatisticsBelief((int)(15 + Math.random() * 10), 1, 1, 5.0);
//        _belMgr.put(test);
        // END TEST CODE
        
        BladewerxStatisticsBelief blf = (BladewerxStatisticsBelief) _belMgr.get(BladewerxStatisticsBelief.BELIEF_NAME);
        
        if (blf != null)
        {
            double rate = blf.getAlphaRate();
            if (inMinutes)
            {
                rate = rate * 60;
            }
            
            addDataPoint("count rate", new Millisecond(new Date(blf.getTimeStamp().getTime())), rate);
            lblRate.setText(String.format("%.2f", rate));
            updateDataDisplayWidth();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        super.actionPerformed(e);
        
        if (e.getActionCommand().equals("units"))
        {
            if (boxUnits.getSelectedIndex() == 0 && inMinutes)
            {
                multiplyPoints("count rate", null, 1.0/60.0);
                setAxisLabel(AXIS_LABEL + " (count/s)");
                lblUnits.setText(" count/s");
                updateDataDisplayWidth();
                inMinutes = false;
            }
            else if (boxUnits.getSelectedIndex() == 1 && !inMinutes)
            {
                multiplyPoints("count rate", null, 60.0);
                setAxisLabel(AXIS_LABEL + " (count/min)");
                lblUnits.setText(" count/min");
                updateDataDisplayWidth();
                inMinutes = true;
            }
        }
    }
    
}
