package edu.jhuapl.nstd.cbrnPods.TestPanels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Date;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;

import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.Canberra.CanberraDetectionMessageListener;
import edu.jhuapl.nstd.cbrnPods.messages.Canberra.CanberraDetectionMessage;
import edu.jhuapl.nstd.swarm.display.DynamicTimeSeriesChartPanel;

public class CanberraGammaPanel extends DynamicTimeSeriesChartPanel implements CanberraDetectionMessageListener
{
	private JLabel countLabel;
	private JLabel relativeTimeLabel;
	private JLabel filteredDoseRateLabel;
	private JLabel unfilteredDoseRateLabel;
	private JLabel missionDoseLabel;
	private JLabel peakRateLabel;
	private JLabel temperatureLabel;
	private JLabel timestampLabel;
	private JLabel countValueLabel;
	private JLabel relativeTimeValueLabel;
	private JLabel filteredDoseRateValueLabel;
	private JLabel unfilteredDoseRateValueLabel;
	private JLabel missionDoseValueLabel;
	private JLabel peakRateValueLabel;
	private JLabel temperatureValueLabel;
	private JLabel timestampValueLabel;

	/**
	 * Create the panel.
	 */
	public CanberraGammaPanel(cbrnPodsInterface pods)
	{
		super(CanberraGammaPanel.class.getSimpleName());
		
		initComponents();
		this.setVisible(true);
		
		pods.addCanberraDetectionListener(this);
	}
	
    /**
     * Builds the main XY time series plot. 
     */
    protected void initChartPanel() {
        super.initChartPanel();
        
        // Set the dimensions of the chart panel
        chartPanel.setPreferredSize(new Dimension(500, 500));
    }

	@Override
	public void handleDetectionMessage(CanberraDetectionMessage m)
	{
		final CanberraDetectionMessage msg = (CanberraDetectionMessage)m;
		
		SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
            	
            	
            	// Update the textual display
            	countValueLabel.setText("  " + Integer.toString(msg.getCount()));
				double filteredDoseRate = msg.getFilteredDoseRate();
				filteredDoseRateValueLabel.setText("  " + Double.toString(filteredDoseRate));
				double unfilteredDoseRate = msg.getUnfilteredDoseRate();
				unfilteredDoseRateValueLabel.setText("  " + Double.toString(unfilteredDoseRate));
				missionDoseValueLabel.setText("  " + Double.toString(msg.getMisssionAccumulatedDose()));
				peakRateValueLabel.setText("  " + Double.toString(msg.getPeakRate()));
				temperatureValueLabel.setText("  " + Double.toString(msg.getTemperature()));
				relativeTimeValueLabel.setText("  " + msg.getRelativeTime());
				
				long time = msg.getTimestampMs();
				timestampValueLabel.setText("  " + Long.toString(time));
				
				/********************************************************
				 * Plot the filtered and unfiltered dose rate data
				 ********************************************************/
				
				 // Add the time series data to the plot
                addDataPoint("filtered", new Second(new Date(time)), filteredDoseRate);
                
                // Add the time series data to the plot
                addDataPoint("unfiltered", new Second(new Date(time)), unfilteredDoseRate);
            }
        });
	}
	
	private void initComponents()
	{
		countLabel = new JLabel("Count");
		countLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		countLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		relativeTimeLabel = new JLabel("Relative Time (hh:mm:ss)");
		relativeTimeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		relativeTimeLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		filteredDoseRateLabel = new JLabel("Filtered Dose Rate (uG/h)");
		filteredDoseRateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		filteredDoseRateLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		unfilteredDoseRateLabel = new JLabel("Unfiltered Dose Rate (uG/h)");
		unfilteredDoseRateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		unfilteredDoseRateLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		missionDoseLabel = new JLabel("Mission Accumulated Dose (uG)");
		missionDoseLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		missionDoseLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		peakRateLabel = new JLabel("Peak Rate (cG/h)");
		peakRateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		peakRateLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		temperatureLabel = new JLabel("Temperature (C)");
		temperatureLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		temperatureLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		timestampLabel = new JLabel("Timestamp (ms)");
		timestampLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		timestampLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		countValueLabel = new JLabel("");
		countValueLabel.setBackground(new java.awt.Color(255, 255, 255));
		countValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		countValueLabel.setText(" ");
		countValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
		countValueLabel.setOpaque(true);
		
		relativeTimeValueLabel = new JLabel("");
		relativeTimeValueLabel.setBackground(new java.awt.Color(255, 255, 255));
		relativeTimeValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		relativeTimeValueLabel.setText(" ");
		relativeTimeValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
		relativeTimeValueLabel.setOpaque(true);
		
		filteredDoseRateValueLabel = new JLabel("");
		filteredDoseRateValueLabel.setBackground(new java.awt.Color(255, 255, 255));
		filteredDoseRateValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		filteredDoseRateValueLabel.setText(" ");
		filteredDoseRateValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
		filteredDoseRateValueLabel.setOpaque(true);
		
		unfilteredDoseRateValueLabel = new JLabel("");
		unfilteredDoseRateValueLabel.setBackground(new java.awt.Color(255, 255, 255));
		unfilteredDoseRateValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		unfilteredDoseRateValueLabel.setText(" ");
		unfilteredDoseRateValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
		unfilteredDoseRateValueLabel.setOpaque(true);
		
		missionDoseValueLabel = new JLabel("");
		missionDoseValueLabel.setBackground(new java.awt.Color(255, 255, 255));
		missionDoseValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		missionDoseValueLabel.setText(" ");
		missionDoseValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
		missionDoseValueLabel.setOpaque(true);
		
		peakRateValueLabel = new JLabel("");
		peakRateValueLabel.setBackground(new java.awt.Color(255, 255, 255));
		peakRateValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		peakRateValueLabel.setText(" ");
		peakRateValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
		peakRateValueLabel.setOpaque(true);
		
		temperatureValueLabel = new JLabel("");
		temperatureValueLabel.setBackground(new java.awt.Color(255, 255, 255));
		temperatureValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		temperatureValueLabel.setText(" ");
		temperatureValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
		temperatureValueLabel.setOpaque(true);
		
		timestampValueLabel = new JLabel("");
		timestampValueLabel.setBackground(new java.awt.Color(255, 255, 255));
		timestampValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		timestampValueLabel.setText(" ");
		timestampValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
		timestampValueLabel.setOpaque(true);
        
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(countLabel, Alignment.TRAILING)
						.addComponent(filteredDoseRateLabel, Alignment.TRAILING)
						.addComponent(unfilteredDoseRateLabel, Alignment.TRAILING)
						.addComponent(missionDoseLabel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(peakRateLabel, Alignment.TRAILING)
						.addComponent(temperatureLabel, Alignment.TRAILING)
						.addComponent(relativeTimeLabel, Alignment.TRAILING)
						.addComponent(timestampLabel, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 119, GroupLayout.PREFERRED_SIZE))
					.addGap(14)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(relativeTimeValueLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(temperatureValueLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(missionDoseValueLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(peakRateValueLabel, GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
						.addComponent(unfilteredDoseRateValueLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(filteredDoseRateValueLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(countValueLabel, GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
						.addComponent(timestampValueLabel, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE))
					.addGap(431))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(68)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(countLabel, Alignment.TRAILING)
						.addComponent(countValueLabel, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(filteredDoseRateLabel, Alignment.TRAILING)
						.addComponent(filteredDoseRateValueLabel, Alignment.TRAILING))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(unfilteredDoseRateLabel, Alignment.TRAILING)
						.addComponent(unfilteredDoseRateValueLabel, Alignment.TRAILING))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(missionDoseLabel, Alignment.TRAILING)
						.addComponent(missionDoseValueLabel, Alignment.TRAILING))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(peakRateValueLabel, Alignment.TRAILING)
						.addComponent(peakRateLabel, Alignment.TRAILING))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(temperatureValueLabel, Alignment.TRAILING)
						.addComponent(temperatureLabel, Alignment.TRAILING))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(relativeTimeValueLabel, Alignment.TRAILING)
						.addComponent(relativeTimeLabel, Alignment.TRAILING))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(timestampValueLabel, Alignment.TRAILING)
						.addComponent(timestampLabel, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(255, Short.MAX_VALUE))
		);
		groupLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {countLabel, relativeTimeLabel, filteredDoseRateLabel, unfilteredDoseRateLabel, peakRateLabel, temperatureLabel, timestampLabel});
		setLayout(groupLayout);
	}

    @Override
    protected void updateData()
    {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
