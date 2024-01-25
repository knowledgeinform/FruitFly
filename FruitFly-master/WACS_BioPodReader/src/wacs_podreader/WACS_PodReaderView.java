/*
 * WACS_PodReaderView.java
 */
package wacs_podreader;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.text.FieldPosition;
import java.text.ParsePosition;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.ui.ApplicationFrame;

/**
 * The application's main frame.
 */
public class WACS_PodReaderView extends FrameView {

    private PodLogInterface podLogInterface;
    private JFreeChart chart;
    private ChartPanel chartPanel;

    public WACS_PodReaderView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });



        //my code
        initializePodLog();
    }

    public void initializePodLog() {
        podLogInterface = new PodLogInterface();
        jlEventLog.setModel(new DefaultListModel());
        ((DefaultListModel) jlEventLog.getModel()).clear();
        chart = ChartFactory.createXYLineChart("iBac Data", "Time", "Value",
                podLogInterface.getXyDataset(), PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.white);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();


        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setNumberFormatOverride(
                new NumberFormat() {

                    @Override
                    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
                        return format((long) number, toAppendTo, pos);
                    }

                    @Override
                    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {

                        toAppendTo.append(new Date(number));
                        return toAppendTo;
                    }

                    @Override
                    public Number parse(String source, ParsePosition parsePosition) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                });


        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1000, 1000));
        ApplicationFrame test = new ApplicationFrame("iBac Output");
        test.setContentPane(chartPanel);

        test.pack();
        test.setVisible(true);

        cbAllActionPerformed(null);

    }

    private void setCbSelected(int index, boolean val) {
        podLogInterface.setFieldEnable(index, val);
    }

    private void refreshGraph()
    {

        podLogInterface.getDataSet(0,1.0).seriesChanged(new SeriesChangeEvent(this));
        jlEventLog.setListData(podLogInterface.getEventEntries( 0,1.0).toArray());
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = WACS_PodReaderApp.getApplication().getMainFrame();
            aboutBox = new WACS_PodReaderAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        WACS_PodReaderApp.getApplication().show(aboutBox);
    }

    private void saveTraceRange(File file, double scale, double position) {
        try {
            List<PodLogEntry> entries = podLogInterface.getTraceEntries(scale, position);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (PodLogEntry e : entries) {
                if (e.getEntry().split(",").length == 17) {
                    writer.append(e.getTimestamp() + e.getEntry().substring(e.getEntry().indexOf(',')) + "\n");
                }
            }
            writer.close();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this.getFrame(), "Error Details:\n" + ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jlEventLog = new javax.swing.JList();
        cbCSI = new javax.swing.JCheckBox();
        cbCLI = new javax.swing.JCheckBox();
        cbBCSI = new javax.swing.JCheckBox();
        cbBCLI = new javax.swing.JCheckBox();
        cbCSA = new javax.swing.JCheckBox();
        cbCLA = new javax.swing.JCheckBox();
        cbBCSA = new javax.swing.JCheckBox();
        cbBCLA = new javax.swing.JCheckBox();
        cbBpSA = new javax.swing.JCheckBox();
        cbBpLA = new javax.swing.JCheckBox();
        cbSFI = new javax.swing.JCheckBox();
        cbSFA = new javax.swing.JCheckBox();
        cbAlarm = new javax.swing.JCheckBox();
        cbAll = new javax.swing.JCheckBox();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jmiLoadLog = new javax.swing.JMenuItem();
        jSaveRange = new javax.swing.JMenuItem();
        jSaveEntire = new javax.swing.JMenuItem();
        jSaveImage = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jlEventLog.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jlEventLog.setName("jlEventLog"); // NOI18N
        jScrollPane1.setViewportView(jlEventLog);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(wacs_podreader.WACS_PodReaderApp.class).getContext().getResourceMap(WACS_PodReaderView.class);
        cbCSI.setText(resourceMap.getString("cbCSI.text")); // NOI18N
        cbCSI.setName("cbCSI"); // NOI18N
        cbCSI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStateChanged(evt);
            }
        });

        cbCLI.setText(resourceMap.getString("cbCLI.text")); // NOI18N
        cbCLI.setName("cbCLI"); // NOI18N
        cbCLI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStateChanged(evt);
            }
        });

        cbBCSI.setText(resourceMap.getString("cbBCSI.text")); // NOI18N
        cbBCSI.setName("cbBCSI"); // NOI18N
        cbBCSI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStateChanged(evt);
            }
        });

        cbBCLI.setText(resourceMap.getString("cbBCLI.text")); // NOI18N
        cbBCLI.setName("cbBCLI"); // NOI18N
        cbBCLI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStateChanged(evt);
            }
        });

        cbCSA.setText(resourceMap.getString("cbCSA.text")); // NOI18N
        cbCSA.setName("cbCSA"); // NOI18N
        cbCSA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStateChanged(evt);
            }
        });

        cbCLA.setText(resourceMap.getString("cbCLA.text")); // NOI18N
        cbCLA.setName("cbCLA"); // NOI18N
        cbCLA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStateChanged(evt);
            }
        });

        cbBCSA.setText(resourceMap.getString("cbBCSA.text")); // NOI18N
        cbBCSA.setName("cbBCSA"); // NOI18N
        cbBCSA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStateChanged(evt);
            }
        });

        cbBCLA.setText(resourceMap.getString("cbBCLA.text")); // NOI18N
        cbBCLA.setName("cbBCLA"); // NOI18N
        cbBCLA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStateChanged(evt);
            }
        });

        cbBpSA.setText(resourceMap.getString("cbBpSA.text")); // NOI18N
        cbBpSA.setName("cbBpSA"); // NOI18N
        cbBpSA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStateChanged(evt);
            }
        });

        cbBpLA.setText(resourceMap.getString("cbBpLA.text")); // NOI18N
        cbBpLA.setName("cbBpLA"); // NOI18N
        cbBpLA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStateChanged(evt);
            }
        });

        cbSFI.setText(resourceMap.getString("cbSFI.text")); // NOI18N
        cbSFI.setName("cbSFI"); // NOI18N
        cbSFI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStateChanged(evt);
            }
        });

        cbSFA.setText(resourceMap.getString("cbSFA.text")); // NOI18N
        cbSFA.setName("cbSFA"); // NOI18N
        cbSFA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStateChanged(evt);
            }
        });

        cbAlarm.setText(resourceMap.getString("cbAlarm.text")); // NOI18N
        cbAlarm.setName("cbAlarm"); // NOI18N
        cbAlarm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStateChanged(evt);
            }
        });

        cbAll.setText(resourceMap.getString("cbAll.text")); // NOI18N
        cbAll.setName("cbAll"); // NOI18N
        cbAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbCSI)
                            .addComponent(cbCLI)
                            .addComponent(cbBCLI)
                            .addComponent(cbCSA)
                            .addComponent(cbCLA)
                            .addComponent(cbBCSA)
                            .addComponent(cbBCLA)
                            .addComponent(cbBCSI))
                        .addGap(26, 26, 26)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbAll)
                            .addComponent(cbBpSA)
                            .addComponent(cbBpLA)
                            .addComponent(cbSFI)
                            .addComponent(cbSFA)
                            .addComponent(cbAlarm)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE))
                .addGap(397, 397, 397))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 132, Short.MAX_VALUE)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(cbCSI)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbCLI)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbBCSI, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbBCLI)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbCSA)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbCLA)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbBCSA)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbBCLA)
                            .addComponent(cbAll)))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(cbBpSA)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbBpLA)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbSFI)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbSFA)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbAlarm)
                        .addGap(72, 72, 72)))
                .addContainerGap())
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jmiLoadLog.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jmiLoadLog.setText(resourceMap.getString("jmiLoadLog.text")); // NOI18N
        jmiLoadLog.setName("jmiLoadLog"); // NOI18N
        jmiLoadLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiLoadLogActionPerformed(evt);
            }
        });
        fileMenu.add(jmiLoadLog);

        jSaveRange.setText(resourceMap.getString("jSaveRange.text")); // NOI18N
        jSaveRange.setName("jSaveRange"); // NOI18N
        jSaveRange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSaveRangeActionPerformed(evt);
            }
        });
        fileMenu.add(jSaveRange);

        jSaveEntire.setText(resourceMap.getString("jSaveEntire.text")); // NOI18N
        jSaveEntire.setName("jSaveEntire"); // NOI18N
        jSaveEntire.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSaveEntireActionPerformed(evt);
            }
        });
        fileMenu.add(jSaveEntire);

        jSaveImage.setText(resourceMap.getString("jSaveImage.text")); // NOI18N
        jSaveImage.setName("jSaveImage"); // NOI18N
        jSaveImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSaveImageActionPerformed(evt);
            }
        });
        fileMenu.add(jSaveImage);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(wacs_podreader.WACS_PodReaderApp.class).getContext().getActionMap(WACS_PodReaderView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 776, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 606, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jmiLoadLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiLoadLogActionPerformed


        JFileChooser jfc = new JFileChooser(new File("./"));

        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jfc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {

                return true;

            }

            @Override
            public String getDescription() {
                return "*.log,*.txt";
            }
        });
        jfc.setMultiSelectionEnabled(true);
        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            Vector<File> filesToLoad = new Vector<File>();
            LinkedList<File> filesToCheck = new LinkedList<File>();
            filesToCheck.addAll(Arrays.asList(jfc.getSelectedFiles()));
            while (filesToCheck.size() > 0) {
                File selectedFile = filesToCheck.removeFirst();
                if (selectedFile.isFile()) {
                    if (selectedFile.toString().toLowerCase().endsWith(".log") || selectedFile.toString().toLowerCase().endsWith(".txt")) {
                        //single file
                        filesToLoad.add(selectedFile);
                    }
                }
                if (selectedFile.isDirectory()) {
                    //build a list of all files
                    filesToCheck.addAll(Arrays.asList(selectedFile.listFiles()));
                }
            }

            //reset interface
            podLogInterface.reset();
            for (File logfile : filesToLoad) {
                podLogInterface.addFile(logfile.toString());
            }
            refreshGraph();
        }


    }//GEN-LAST:event_jmiLoadLogActionPerformed

    private void cbAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAllActionPerformed
        JCheckBox[] cbs = {
            cbCSI,
            cbCLI,
            cbBCSI,
            cbBCLI,
            cbCSA,
            cbCLA,
            cbBCSA,
            cbBCLA,
            cbBpSA,
            cbBpLA,
            cbSFI,
            cbSFA,
            cbAlarm};

        for (int i = 0; i < cbs.length; i++) {
            cbs[i].setSelected(cbAll.isSelected());
            setCbSelected(i, cbAll.isSelected());
        }
        refreshGraph();

    }//GEN-LAST:event_cbAllActionPerformed

    private void cbStateChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbStateChanged
        JCheckBox[] cbs = {
            cbCSI,
            cbCLI,
            cbBCSI,
            cbBCLI,
            cbCSA,
            cbCLA,
            cbBCSA,
            cbBCLA,
            cbBpSA,
            cbBpLA,
            cbSFI,
            cbSFA,
            cbAlarm};

        for (int i = 0; i < cbs.length; i++) {
            if (cbs[i].equals(evt.getSource())) {
                setCbSelected(i, cbs[i].isSelected());
            }
        }
        refreshGraph();
    }//GEN-LAST:event_cbStateChanged

    private void jSaveImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSaveImageActionPerformed
        File file = null;
        JFileChooser jfc = new JFileChooser(new File("./"));
        jfc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {

                return (f.toString().toLowerCase().contains(".png"));

            }

            @Override
            public String getDescription() {
                return "chart.png";
            }
        });
        jfc.setMultiSelectionEnabled(true);
        if (jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = jfc.getSelectedFile();
        }
        try {
            ChartUtilities.saveChartAsPNG(file, chart, 1024, 768);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this.getFrame(), "Error Details:\n" + ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
        }
}//GEN-LAST:event_jSaveImageActionPerformed

    private void jSaveEntireActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSaveEntireActionPerformed
        File file = null;
        JFileChooser jfc = new JFileChooser(new File("./"));
        jfc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {

                return (f.toString().toLowerCase().contains(".csv"));

            }

            @Override
            public String getDescription() {
                return "chart_full.csv";
            }
        });
        jfc.setMultiSelectionEnabled(true);
        if (jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = jfc.getSelectedFile();
        }
        saveTraceRange(file, 1, 0);
    }//GEN-LAST:event_jSaveEntireActionPerformed

    private void jSaveRangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSaveRangeActionPerformed
        File file = null;
        JFileChooser jfc = new JFileChooser(new File("./"));
        jfc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {

                return (f.toString().toLowerCase().contains(".csv"));

            }

            @Override
            public String getDescription() {
                return "chart_range.csv";
            }
        });
        jfc.setMultiSelectionEnabled(true);
        if (jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = jfc.getSelectedFile();
        }
        saveTraceRange(file, 0.0, 1.0);
    }//GEN-LAST:event_jSaveRangeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbAlarm;
    private javax.swing.JCheckBox cbAll;
    private javax.swing.JCheckBox cbBCLA;
    private javax.swing.JCheckBox cbBCLI;
    private javax.swing.JCheckBox cbBCSA;
    private javax.swing.JCheckBox cbBCSI;
    private javax.swing.JCheckBox cbBpLA;
    private javax.swing.JCheckBox cbBpSA;
    private javax.swing.JCheckBox cbCLA;
    private javax.swing.JCheckBox cbCLI;
    private javax.swing.JCheckBox cbCSA;
    private javax.swing.JCheckBox cbCSI;
    private javax.swing.JCheckBox cbSFA;
    private javax.swing.JCheckBox cbSFI;
    private javax.swing.JMenuItem jSaveEntire;
    private javax.swing.JMenuItem jSaveImage;
    private javax.swing.JMenuItem jSaveRange;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList jlEventLog;
    private javax.swing.JMenuItem jmiLoadLog;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
}
