/*
 * PTUTrackerView.java
 */
package edu.jhuapl.nstd.sti.ptutracker.app;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.piccolo.Pic_Interface;
import edu.jhuapl.nstd.piccolo.Pic_Telemetry;
import edu.jhuapl.nstd.piccolo.Pic_TelemetryListener;
import edu.jhuapl.nstd.piccolo.PiccoloTracker;
import edu.jhuapl.nstd.sti.ptutracker.PtuInterface;
import edu.jhuapl.nstd.util.GeoUtil;
import edu.jhuapl.nstd.util.XCommSerialPort;
import java.beans.PropertyChangeEvent;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * The application's main frame.
 */
public class PTUTrackerView extends FrameView implements PropertyChangeListener, Pic_TelemetryListener {

    public PTUTrackerView(SingleFrameApplication app) {
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

        addCustomComponents();
        new PTUConnectionInfo(this).setVisible(true);
        initializeFields();

    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = PTUTrackerApp.getApplication().getMainFrame();
            aboutBox = new PTUTrackerAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        PTUTrackerApp.getApplication().show(aboutBox);
    }

    public void initializeFields() {
        //initialize gps fields
        LatLonAltPosition gcs = ApplicationState.getApplicationState().getGcsPositions().get(0);
        LatLonAltPosition uav = ApplicationState.getApplicationState().getUavPositions().get(0);

        jtfGcsLat.setText(String.format("%6f", gcs.getLatitude().getDoubleValue(Angle.DEGREES)));
        jtfGcsLon.setText(String.format("%6f", gcs.getLongitude().getDoubleValue(Angle.DEGREES)));
        jtfGcsAlt.setText(String.format("%6f", gcs.getAltitude().getDoubleValue(Length.METERS)));
        jtfDeclination.setText(String.format("%2f", ApplicationState.getApplicationState().getMagneticDeclination()/10.0));
        jtfCal.setText(String.format("%2f", ApplicationState.getApplicationState().getCalibrationHeading()/10.0));


        jtfCalPan.setText(String.format("%2f", ApplicationState.getApplicationState().getCalibrationPan()/10.0));
        jtfCalTilt.setText(String.format("%2f", ApplicationState.getApplicationState().getCalibrationTilt()/10.0));



        jtfUavLat.setText(String.format("%6f", uav.getLatitude().getDoubleValue(Angle.DEGREES)));
        jtfUavLon.setText(String.format("%6f", uav.getLongitude().getDoubleValue(Angle.DEGREES)));
        jtfUavAlt.setText(String.format("%6f", uav.getAltitude().getDoubleValue(Length.METERS)));

    }
    PanPanel panPanelTrue, panPanelRaw;
    TiltPanel tiltPanelTrue, tiltPanelRaw;
    JogPanel jogPanel;
    Timer jogTimer;

    public void addCustomComponents() {
        panPanelTrue = new PanPanel();
        tiltPanelTrue = new TiltPanel();
        panPanelRaw = new PanPanel();
        tiltPanelRaw = new TiltPanel();
        jogPanel = new JogPanel();

        //Pan panel
        javax.swing.GroupLayout jpPanLayoutRaw = new javax.swing.GroupLayout(jpPanRaw);
        jpPanRaw.setLayout(jpPanLayoutRaw);
        jpPanLayoutRaw.setHorizontalGroup(
                jpPanLayoutRaw.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(panPanelRaw));
        jpPanLayoutRaw.setVerticalGroup(
                jpPanLayoutRaw.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(panPanelRaw));

        //tilt panel
        javax.swing.GroupLayout jpTiltLayoutRaw = new javax.swing.GroupLayout(jpTiltRaw);
        jpTiltRaw.setLayout(jpTiltLayoutRaw);
        jpTiltLayoutRaw.setHorizontalGroup(
                jpTiltLayoutRaw.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(tiltPanelRaw));
        jpTiltLayoutRaw.setVerticalGroup(
                jpTiltLayoutRaw.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(tiltPanelRaw));

        //Pan panel
        javax.swing.GroupLayout jpPanLayoutTrue = new javax.swing.GroupLayout(jpPanTrue);
        jpPanTrue.setLayout(jpPanLayoutTrue);
        jpPanLayoutTrue.setHorizontalGroup(
                jpPanLayoutTrue.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(panPanelTrue));
        jpPanLayoutTrue.setVerticalGroup(
                jpPanLayoutTrue.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(panPanelTrue));

        //tilt panel
        javax.swing.GroupLayout jpTiltLayoutTrue = new javax.swing.GroupLayout(jpTiltTrue);
        jpTiltTrue.setLayout(jpTiltLayoutTrue);
        jpTiltLayoutTrue.setHorizontalGroup(
                jpTiltLayoutTrue.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(tiltPanelTrue));
        jpTiltLayoutTrue.setVerticalGroup(
                jpTiltLayoutTrue.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(tiltPanelTrue));



        //jog panel
        jogPanel.setPreferredSize(jpJog.getPreferredSize());
        javax.swing.GroupLayout jpJogLayout = new javax.swing.GroupLayout(jpJog);
        jpJog.setLayout(jpJogLayout);
        jpJogLayout.setHorizontalGroup(
                jpJogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jogPanel));
        jpJogLayout.setVerticalGroup(
                jpJogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jogPanel));

        jogPanel.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });

        //set up jog timer
        jogTimer = new Timer(500, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ptu.sendSetJogSpeed(jogPanel.getXPos(), jogPanel.getYPos());
            }
        });
        jogTimer.setRepeats(true);


    }

    private void formMousePressed(java.awt.event.MouseEvent evt) {
        ptu.sendSetJogSpeed(jogPanel.getXPos(), jogPanel.getYPos());
        jogTimer.start();
    }

    private void formMouseReleased(java.awt.event.MouseEvent evt) {
        ptu.sendSetJogSpeed(0, 0);
        jogTimer.stop();
    }
    //My code
    XCommSerialPort serialPort;
    Pic_Interface piccolo;
    PtuInterface ptu;
    PiccoloTracker piccoloTracker;


    public void initMembers() {

        String com = ApplicationState.getApplicationState().getTrackerComPort();
        int baud = 38400;
        //int apNum, String port_or_ipAddr, int baud_or_port, boolean usingTCP)
        int apNum = ApplicationState.getApplicationState().getPilotId();
        String port_or_ipAddr = ApplicationState.getApplicationState().getComOrIp();
        int baud_or_port = Integer.parseInt(ApplicationState.getApplicationState().getPortOrBaud());
        boolean usingTcp = ApplicationState.getApplicationState().isIsTCP();


        if (serialPort != null) {
            serialPort.close();
        }
        if (piccolo != null) {
            piccolo.forceShutdown();
        }
        if (ptu != null) {
            ptu.shutdown();
        }
        if (piccoloTracker != null) {
            piccoloTracker.shutdown();
        }
        serialPort = new XCommSerialPort(com, baud, 8, 1, 0, 0);
        if (!serialPort.isPortGood()) {
            JOptionPane.showMessageDialog(this.getFrame(), "Error Details:\n" + "Unable to open serial port", "Exception!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        piccolo = new Pic_Interface(apNum, port_or_ipAddr, baud_or_port, usingTcp);
        new Thread(piccolo).start();

        ptu = new PtuInterface(serialPort);
        piccoloTracker = new PiccoloTracker(ptu, piccolo);

        jtglManual.setSelected(true);

        ptu.addListener(this);
        piccolo.addPicTelemetryListener(this);


        //press buttons
        jbSetCalActionPerformed(null);
        jbSetActionPerformed(null);


        updateVisible();
    }

    public void saveToState()
    {

        ApplicationState.getApplicationState().setCalibrationPan(piccoloTracker.getTrackerOffsetPan());
        ApplicationState.getApplicationState().setCalibrationTilt(piccoloTracker.getTrackerOffsetTilt());
        ApplicationState.getApplicationState().setCalibrationHeading(piccoloTracker.getCalibrationHeading());
        ApplicationState.getApplicationState().setMagneticDeclination(piccoloTracker.getMagneticDeclination());
        
        ApplicationState.saveToFile();
    }

    public void updateVisible() {

        piccoloTracker.setIsAuto(jtglAuto.isSelected());
        if (jtglAuto.isSelected()) {
            jpAuto.setEnabled(true);
            jpManual.setEnabled(false);
            jtglManual.setSelected(false);
        }
        if (jtglManual.isSelected()) {
            jpAuto.setEnabled(false);
            jpManual.setEnabled(true);
            jtglAuto.setSelected(false);
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jlPanRaw = new javax.swing.JLabel();
        jlTiltRaw = new javax.swing.JLabel();
        jpPanRaw = new javax.swing.JPanel();
        jpTiltRaw = new javax.swing.JPanel();
        jlTiltTrue = new javax.swing.JLabel();
        jlPanTrue = new javax.swing.JLabel();
        jpPanTrue = new javax.swing.JPanel();
        jpTiltTrue = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jpManual = new javax.swing.JPanel();
        jtfUavLat = new javax.swing.JTextField();
        jtfUavLon = new javax.swing.JTextField();
        jtfUavAlt = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jbSetManual = new javax.swing.JButton();
        jtglManual = new javax.swing.JToggleButton();
        jbStow = new javax.swing.JButton();
        jpJog = new javax.swing.JPanel();
        jtfManPan = new javax.swing.JTextField();
        jtfManTilt = new javax.swing.JTextField();
        jbSendManPT = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jpAuto = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jtfGcsLat = new javax.swing.JTextField();
        jtfGcsLon = new javax.swing.JTextField();
        jtfGcsAlt = new javax.swing.JTextField();
        jlUavLat = new javax.swing.JLabel();
        jlUavLon = new javax.swing.JLabel();
        jlUavAlt = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jtfDeclination = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jtfCal = new javax.swing.JTextField();
        jbSet = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jtglAuto = new javax.swing.JToggleButton();
        jPanel4 = new javax.swing.JPanel();
        jtfCalPan = new javax.swing.JTextField();
        jbCalUp = new javax.swing.JButton();
        jbCalDown = new javax.swing.JButton();
        jbCalLeft = new javax.swing.JButton();
        jbCalRight = new javax.swing.JButton();
        jbSetCal = new javax.swing.JButton();
        jtfCalibStepSize = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jtfCalTilt = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(edu.jhuapl.nstd.sti.ptutracker.app.PTUTrackerApp.class).getContext().getResourceMap(PTUTrackerView.class);
        jLabel1.setFont(resourceMap.getFont("jLabel14.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("Pan.text")); // NOI18N
        jLabel1.setName("Pan"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("jLabel14.font")); // NOI18N
        jLabel2.setText(resourceMap.getString("Tilt.text")); // NOI18N
        jLabel2.setName("Tilt"); // NOI18N

        jlPanRaw.setText(resourceMap.getString("jlPanRaw.text")); // NOI18N
        jlPanRaw.setName("jlPanRaw"); // NOI18N

        jlTiltRaw.setText(resourceMap.getString("jlTiltRaw.text")); // NOI18N
        jlTiltRaw.setName("jlTiltRaw"); // NOI18N

        jpPanRaw.setBackground(resourceMap.getColor("jpTiltRaw.background")); // NOI18N
        jpPanRaw.setForeground(resourceMap.getColor("jpTiltRaw.foreground")); // NOI18N
        jpPanRaw.setName("jpPanRaw"); // NOI18N

        javax.swing.GroupLayout jpPanRawLayout = new javax.swing.GroupLayout(jpPanRaw);
        jpPanRaw.setLayout(jpPanRawLayout);
        jpPanRawLayout.setHorizontalGroup(
            jpPanRawLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 125, Short.MAX_VALUE)
        );
        jpPanRawLayout.setVerticalGroup(
            jpPanRawLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 120, Short.MAX_VALUE)
        );

        jpTiltRaw.setBackground(resourceMap.getColor("jpTiltRaw.background")); // NOI18N
        jpTiltRaw.setForeground(resourceMap.getColor("jpTiltRaw.foreground")); // NOI18N
        jpTiltRaw.setName("jpTiltRaw"); // NOI18N

        javax.swing.GroupLayout jpTiltRawLayout = new javax.swing.GroupLayout(jpTiltRaw);
        jpTiltRaw.setLayout(jpTiltRawLayout);
        jpTiltRawLayout.setHorizontalGroup(
            jpTiltRawLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 120, Short.MAX_VALUE)
        );
        jpTiltRawLayout.setVerticalGroup(
            jpTiltRawLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 120, Short.MAX_VALUE)
        );

        jlTiltTrue.setText(resourceMap.getString("jlTiltTrue.text")); // NOI18N
        jlTiltTrue.setName("jlTiltTrue"); // NOI18N

        jlPanTrue.setText(resourceMap.getString("jlPanTrue.text")); // NOI18N
        jlPanTrue.setName("jlPanTrue"); // NOI18N

        jpPanTrue.setBackground(resourceMap.getColor("jpPanTrue.background")); // NOI18N
        jpPanTrue.setName("jpPanTrue"); // NOI18N

        javax.swing.GroupLayout jpPanTrueLayout = new javax.swing.GroupLayout(jpPanTrue);
        jpPanTrue.setLayout(jpPanTrueLayout);
        jpPanTrueLayout.setHorizontalGroup(
            jpPanTrueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 125, Short.MAX_VALUE)
        );
        jpPanTrueLayout.setVerticalGroup(
            jpPanTrueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 120, Short.MAX_VALUE)
        );

        jpTiltTrue.setBackground(resourceMap.getColor("jpPanTrue.background")); // NOI18N
        jpTiltTrue.setName("jpTiltTrue"); // NOI18N

        javax.swing.GroupLayout jpTiltTrueLayout = new javax.swing.GroupLayout(jpTiltTrue);
        jpTiltTrue.setLayout(jpTiltTrueLayout);
        jpTiltTrueLayout.setHorizontalGroup(
            jpTiltTrueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 120, Short.MAX_VALUE)
        );
        jpTiltTrueLayout.setVerticalGroup(
            jpTiltTrueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 120, Short.MAX_VALUE)
        );

        jLabel14.setFont(resourceMap.getFont("jLabel14.font")); // NOI18N
        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        jLabel15.setFont(resourceMap.getFont("jLabel14.font")); // NOI18N
        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel14)
                    .addComponent(jLabel15))
                .addGap(0, 0, 0)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jpPanTrue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlPanTrue))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jlTiltTrue)
                            .addComponent(jpTiltTrue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jpPanRaw, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlPanRaw)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jlTiltRaw)
                            .addComponent(jpTiltRaw, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jpPanRaw, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jpTiltRaw, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jlTiltRaw)
                            .addComponent(jlPanRaw))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jpPanTrue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jpTiltTrue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jlPanTrue)
                            .addComponent(jlTiltTrue)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(74, 74, 74)
                        .addComponent(jLabel14)
                        .addGap(152, 152, 152)
                        .addComponent(jLabel15)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jpManual.setName("jpManual"); // NOI18N

        jtfUavLat.setText(resourceMap.getString("jtfUavLat.text")); // NOI18N
        jtfUavLat.setName("jtfUavLat"); // NOI18N
        jtfUavLat.setPreferredSize(new java.awt.Dimension(80, 20));

        jtfUavLon.setText(resourceMap.getString("jtfUavLon.text")); // NOI18N
        jtfUavLon.setName("jtfUavLon"); // NOI18N
        jtfUavLon.setPreferredSize(new java.awt.Dimension(80, 20));

        jtfUavAlt.setText(resourceMap.getString("jtfUavAlt.text")); // NOI18N
        jtfUavAlt.setName("jtfUavAlt"); // NOI18N
        jtfUavAlt.setPreferredSize(new java.awt.Dimension(80, 20));

        jLabel8.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jLabel9.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel10.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jbSetManual.setText(resourceMap.getString("jbSetManual.text")); // NOI18N
        jbSetManual.setName("jbSetManual"); // NOI18N
        jbSetManual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSetManualActionPerformed(evt);
            }
        });

        jtglManual.setText(resourceMap.getString("jtglManual.text")); // NOI18N
        jtglManual.setName("jtglManual"); // NOI18N
        jtglManual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtglActionPerformed(evt);
            }
        });

        jbStow.setText(resourceMap.getString("jbStow.text")); // NOI18N
        jbStow.setName("jbStow"); // NOI18N
        jbStow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbStowActionPerformed(evt);
            }
        });

        jpJog.setBackground(resourceMap.getColor("jpJog.background")); // NOI18N
        jpJog.setName("jpJog"); // NOI18N
        jpJog.setPreferredSize(new java.awt.Dimension(300, 300));

        javax.swing.GroupLayout jpJogLayout = new javax.swing.GroupLayout(jpJog);
        jpJog.setLayout(jpJogLayout);
        jpJogLayout.setHorizontalGroup(
            jpJogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
        jpJogLayout.setVerticalGroup(
            jpJogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 217, Short.MAX_VALUE)
        );

        jtfManPan.setText(resourceMap.getString("jtfManPan.text")); // NOI18N
        jtfManPan.setName("jtfManPan"); // NOI18N

        jtfManTilt.setText(resourceMap.getString("jtfManTilt.text")); // NOI18N
        jtfManTilt.setName("jtfManTilt"); // NOI18N

        jbSendManPT.setText(resourceMap.getString("jbSendManPT.text")); // NOI18N
        jbSendManPT.setName("jbSendManPT"); // NOI18N
        jbSendManPT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSendManPTActionPerformed(evt);
            }
        });

        jLabel16.setFont(resourceMap.getFont("jLabel16.font")); // NOI18N
        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        jLabel17.setFont(resourceMap.getFont("jLabel16.font")); // NOI18N
        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        jLabel20.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N

        javax.swing.GroupLayout jpManualLayout = new javax.swing.GroupLayout(jpManual);
        jpManual.setLayout(jpManualLayout);
        jpManualLayout.setHorizontalGroup(
            jpManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpManualLayout.createSequentialGroup()
                .addGroup(jpManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jtglManual)
                    .addGroup(jpManualLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jpManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jpManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jtfUavLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtfUavLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtfUavAlt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jbSetManual)))
                    .addGroup(jpManualLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel20)))
                .addGap(18, 18, 18)
                .addComponent(jpJog, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addGroup(jpManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpManualLayout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addContainerGap())
                    .addGroup(jpManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jpManualLayout.createSequentialGroup()
                            .addComponent(jLabel16)
                            .addContainerGap())
                        .addGroup(jpManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jpManualLayout.createSequentialGroup()
                                .addComponent(jbSendManPT)
                                .addGap(22, 22, 22))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpManualLayout.createSequentialGroup()
                                .addGroup(jpManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jtfManPan, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                                    .addComponent(jtfManTilt, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE))
                                .addContainerGap())))))
            .addGroup(jpManualLayout.createSequentialGroup()
                .addGap(104, 104, 104)
                .addComponent(jbStow)
                .addContainerGap(509, Short.MAX_VALUE))
        );
        jpManualLayout.setVerticalGroup(
            jpManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpManualLayout.createSequentialGroup()
                .addGroup(jpManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jpJog, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                    .addGroup(jpManualLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jtglManual)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                        .addComponent(jLabel20)
                        .addGap(130, 130, 130))
                    .addGroup(jpManualLayout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jtfManPan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jtfManTilt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19)
                        .addComponent(jbSendManPT))
                    .addGroup(jpManualLayout.createSequentialGroup()
                        .addContainerGap(86, Short.MAX_VALUE)
                        .addGroup(jpManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jtfUavLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jpManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jtfUavLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jpManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jtfUavAlt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addGap(59, 59, 59)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpManualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jbStow)
                    .addComponent(jbSetManual))
                .addContainerGap())
        );

        jpAuto.setName("jpAuto"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jtfGcsLat.setText(resourceMap.getString("jtfGcsLat.text")); // NOI18N
        jtfGcsLat.setName("jtfGcsLat"); // NOI18N
        jtfGcsLat.setPreferredSize(new java.awt.Dimension(80, 20));

        jtfGcsLon.setText(resourceMap.getString("jtfGcsLon.text")); // NOI18N
        jtfGcsLon.setName("jtfGcsLon"); // NOI18N
        jtfGcsLon.setPreferredSize(new java.awt.Dimension(80, 20));

        jtfGcsAlt.setText(resourceMap.getString("jtfGcsAlt.text")); // NOI18N
        jtfGcsAlt.setName("jtfGcsAlt"); // NOI18N
        jtfGcsAlt.setPreferredSize(new java.awt.Dimension(80, 20));

        jlUavLat.setText(resourceMap.getString("jlUavLat.text")); // NOI18N
        jlUavLat.setName("jlUavLat"); // NOI18N

        jlUavLon.setText(resourceMap.getString("jlUavLon.text")); // NOI18N
        jlUavLon.setName("jlUavLon"); // NOI18N

        jlUavAlt.setText(resourceMap.getString("jlUavAlt.text")); // NOI18N
        jlUavAlt.setName("jlUavAlt"); // NOI18N

        jLabel6.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jtfDeclination.setText(resourceMap.getString("jtfDeclination.text")); // NOI18N
        jtfDeclination.setName("jtfDeclination"); // NOI18N
        jtfDeclination.setPreferredSize(new java.awt.Dimension(40, 20));

        jLabel7.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jtfCal.setText(resourceMap.getString("jtfCal.text")); // NOI18N
        jtfCal.setName("jtfCal"); // NOI18N
        jtfCal.setPreferredSize(new java.awt.Dimension(40, 20));

        jbSet.setText(resourceMap.getString("jbSet.text")); // NOI18N
        jbSet.setName("jbSet"); // NOI18N
        jbSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSetActionPerformed(evt);
            }
        });

        jLabel18.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        jLabel19.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        javax.swing.GroupLayout jpAutoLayout = new javax.swing.GroupLayout(jpAuto);
        jpAuto.setLayout(jpAutoLayout);
        jpAutoLayout.setHorizontalGroup(
            jpAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpAutoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpAutoLayout.createSequentialGroup()
                        .addGroup(jpAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jpAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jtfGcsLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtfGcsLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtfGcsAlt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19)
                    .addComponent(jlUavLat)
                    .addComponent(jlUavLon)
                    .addComponent(jlUavAlt))
                .addContainerGap(48, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpAutoLayout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(jpAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jtfDeclination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jpAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jtfCal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jbSet)
                .addContainerGap())
        );
        jpAutoLayout.setVerticalGroup(
            jpAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpAutoLayout.createSequentialGroup()
                .addGroup(jpAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(jLabel19))
                .addGroup(jpAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jpAutoLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(jpAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jtfGcsLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlUavLat)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jpAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jtfGcsLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlUavLon)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jpAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jtfGcsAlt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlUavAlt)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                        .addGroup(jpAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jpAutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jtfDeclination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtfCal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jpAutoLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jbSet)))
                .addContainerGap())
        );

        jtglAuto.setText(resourceMap.getString("jtglAuto.text")); // NOI18N
        jtglAuto.setName("jtglAuto"); // NOI18N
        jtglAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtglActionPerformed(evt);
            }
        });

        jPanel4.setName("jPanel4"); // NOI18N

        jtfCalPan.setText(resourceMap.getString("jtfCalPan.text")); // NOI18N
        jtfCalPan.setName("jtfCalPan"); // NOI18N

        jbCalUp.setIcon(resourceMap.getIcon("jbCalUp.icon")); // NOI18N
        jbCalUp.setText(resourceMap.getString("jbCalUp.text")); // NOI18N
        jbCalUp.setName("jbCalUp"); // NOI18N
        jbCalUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCalActionPerformed(evt);
            }
        });

        jbCalDown.setIcon(resourceMap.getIcon("jbCalDown.icon")); // NOI18N
        jbCalDown.setText(resourceMap.getString("jbCalDown.text")); // NOI18N
        jbCalDown.setName("jbCalDown"); // NOI18N
        jbCalDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCalActionPerformed(evt);
            }
        });

        jbCalLeft.setIcon(resourceMap.getIcon("jbCalLeft.icon")); // NOI18N
        jbCalLeft.setText(resourceMap.getString("jbCalLeft.text")); // NOI18N
        jbCalLeft.setName("jbCalLeft"); // NOI18N
        jbCalLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCalActionPerformed(evt);
            }
        });

        jbCalRight.setIcon(resourceMap.getIcon("jbCalRight.icon")); // NOI18N
        jbCalRight.setText(resourceMap.getString("jbCalRight.text")); // NOI18N
        jbCalRight.setName("jbCalRight"); // NOI18N
        jbCalRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCalActionPerformed(evt);
            }
        });

        jbSetCal.setText(resourceMap.getString("jbSetCal.text")); // NOI18N
        jbSetCal.setName("jbSetCal"); // NOI18N
        jbSetCal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSetCalActionPerformed(evt);
            }
        });

        jtfCalibStepSize.setText(resourceMap.getString("jtfCalibStepSize.text")); // NOI18N
        jtfCalibStepSize.setName("jtfCalibStepSize"); // NOI18N

        jLabel11.setFont(resourceMap.getFont("jLabel13.font")); // NOI18N
        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jLabel12.setFont(resourceMap.getFont("jLabel13.font")); // NOI18N
        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        jLabel13.setFont(resourceMap.getFont("jLabel13.font")); // NOI18N
        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jtfCalTilt.setText(resourceMap.getString("jtfCalTilt.text")); // NOI18N
        jtfCalTilt.setName("jtfCalTilt"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(77, 77, 77)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addComponent(jbCalLeft, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jbCalRight, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(48, 48, 48))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addComponent(jbCalUp, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(93, 93, 93))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jbCalDown, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(91, 91, 91)))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jbSetCal)
                    .addComponent(jtfCalibStepSize, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(jtfCalPan, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                    .addComponent(jLabel12)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jtfCalTilt, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 95, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jtfCalibStepSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jbCalUp)))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jtfCalPan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel13))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jbCalRight)
                            .addComponent(jbCalLeft))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jtfCalTilt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15)
                        .addComponent(jbSetCal))
                    .addComponent(jbCalDown))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jLabel21.setIcon(resourceMap.getIcon("jLabel21.icon")); // NOI18N
        jLabel21.setText(resourceMap.getString("jLabel21.text")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel21)
                        .addGap(18, 18, 18)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGap(177, 177, 177)
                                .addComponent(jtglAuto))
                            .addComponent(jpAuto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jpManual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jpManual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jtglAuto)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jpAuto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(120, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(edu.jhuapl.nstd.sti.ptutracker.app.PTUTrackerApp.class).getContext().getActionMap(PTUTrackerView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

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
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 1048, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 878, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

  
    private void jbSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSetActionPerformed
        //try to parse all the fields
        double lat;
        double lon;
        double alt;
        double magDecl;
        double calib;

        try {
            lat = Double.parseDouble(jtfGcsLat.getText());
            lon = Double.parseDouble(jtfGcsLon.getText());
            alt = Double.parseDouble(jtfGcsAlt.getText());
            magDecl = Double.parseDouble(jtfDeclination.getText());
            calib = Double.parseDouble(jtfCal.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this.getFrame(), "Invalid Number format:\n" + ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LatLonAltPosition newPos = new LatLonAltPosition(
                new Latitude(lat, Angle.DEGREES),
                new Longitude(lon, Angle.DEGREES),
                new Altitude(alt, Length.METERS));
        
        ApplicationState.getApplicationState().getGcsPositions().add(0, newPos);
        ApplicationState.saveToFile();

        piccoloTracker.setCalibrationHeading((int) (calib * 10.0));
        piccoloTracker.setMagneticDeclination((int) (magDecl * 10.0));

        piccoloTracker.setGcsPosition(newPos);

    }//GEN-LAST:event_jbSetActionPerformed

    private void jtglActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtglActionPerformed
        if (evt.getSource().equals(jtglManual)) {
            jtglAuto.setSelected(!jtglManual.isSelected());
        }
        if (evt.getSource().equals(jtglAuto)) {
            jtglManual.setSelected(!jtglAuto.isSelected());
        }
        updateVisible();

}//GEN-LAST:event_jtglActionPerformed

    private void jbSetManualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSetManualActionPerformed
        //try to parse all the fields
        double lat;
        double lon;
        double alt;

        try {
            lat = Double.parseDouble(jtfUavLat.getText());
            lon = Double.parseDouble(jtfUavLon.getText());
            alt = Double.parseDouble(jtfUavAlt.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this.getFrame(), "Invalid Number format:\n" + ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LatLonAltPosition newPos = new LatLonAltPosition(
                new Latitude(lat, Angle.DEGREES),
                new Longitude(lon, Angle.DEGREES),
                new Altitude(alt, Length.METERS));
        int[] pantilt = GeoUtil.calculatePointingAngles(piccoloTracker.getGcsPosition(), newPos);
        ApplicationState.getApplicationState().getUavPositions().add(0, newPos);
        ApplicationState.saveToFile();
        // add in tracking offsets (and calibration heading) (and magnetic variance)
        int panDegInt = pantilt[0];
        int tiltDegInt = pantilt[1];

        ptu.sendMoveToCoordinates(panDegInt, tiltDegInt);
        ApplicationState.saveToFile();
}//GEN-LAST:event_jbSetManualActionPerformed

    private void jbCalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCalActionPerformed
        //get cal step size
        double stepSize;
        try {
            stepSize = Double.parseDouble(jtfCalibStepSize.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this.getFrame(), "Invalid Number format:\n" + ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
            return;
        }


        if (evt.getSource().equals(jbCalUp)) {
            piccoloTracker.setTrackerOffsetTilt((int) (piccoloTracker.getTrackerOffsetTilt() - stepSize * 10.0));
        }
        if (evt.getSource().equals(jbCalDown)) {
            piccoloTracker.setTrackerOffsetTilt((int) (piccoloTracker.getTrackerOffsetTilt() + stepSize * 10.0));
        }
        if (evt.getSource().equals(jbCalLeft)) {
            piccoloTracker.setTrackerOffsetPan((int) (piccoloTracker.getTrackerOffsetPan() - stepSize * 10.0));
        }
        if (evt.getSource().equals(jbCalRight)) {
            piccoloTracker.setTrackerOffsetPan((int) (piccoloTracker.getTrackerOffsetPan() + stepSize * 10.0));
        }

        piccoloTracker.sendMoveToCoordinates(piccoloTracker.getPanDegrees(), piccoloTracker.getTiltDegrees());
        saveToState();
        updateCalFields();
}//GEN-LAST:event_jbCalActionPerformed

    private void jbSetCalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSetCalActionPerformed
        //get cal step size
        double panDegrees;
        double tiltDegrees;
        try {
            panDegrees = Double.parseDouble(jtfCalPan.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this.getFrame(), "Invalid Number format:\n" + ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            tiltDegrees = Double.parseDouble(jtfCalTilt.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this.getFrame(), "Invalid Number format:\n" + ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        piccoloTracker.setTrackerOffsetTilt((int) (tiltDegrees * 10.0));
        piccoloTracker.setTrackerOffsetPan((int) (panDegrees * 10.0));
        piccoloTracker.sendMoveToCoordinates(piccoloTracker.getPanDegrees(), piccoloTracker.getTiltDegrees());
        
        updateDisplay();
        updateCalFields();
        saveToState();
    }//GEN-LAST:event_jbSetCalActionPerformed

    private void jbStowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbStowActionPerformed
        //this needs to be in tracker reference
        ptu.sendMoveToCoordinates(0, 0);
        updateDisplay();
    }//GEN-LAST:event_jbStowActionPerformed

    private void jbSendManPTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSendManPTActionPerformed
        //get cal step size
        double panDegrees;
        double tiltDegrees;
        try {
            panDegrees = Double.parseDouble(jtfManPan.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this.getFrame(), "Invalid Number format:\n" + ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            tiltDegrees = Double.parseDouble(jtfManTilt.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this.getFrame(), "Invalid Number format:\n" + ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        piccoloTracker.sendMoveToCoordinates((int)(panDegrees*10.0),(int)(tiltDegrees*10.0));
    }//GEN-LAST:event_jbSendManPTActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JButton jbCalDown;
    private javax.swing.JButton jbCalLeft;
    private javax.swing.JButton jbCalRight;
    private javax.swing.JButton jbCalUp;
    private javax.swing.JButton jbSendManPT;
    private javax.swing.JButton jbSet;
    private javax.swing.JButton jbSetCal;
    private javax.swing.JButton jbSetManual;
    private javax.swing.JButton jbStow;
    private javax.swing.JLabel jlPanRaw;
    private javax.swing.JLabel jlPanTrue;
    private javax.swing.JLabel jlTiltRaw;
    private javax.swing.JLabel jlTiltTrue;
    private javax.swing.JLabel jlUavAlt;
    private javax.swing.JLabel jlUavLat;
    private javax.swing.JLabel jlUavLon;
    private javax.swing.JPanel jpAuto;
    private javax.swing.JPanel jpJog;
    private javax.swing.JPanel jpManual;
    private javax.swing.JPanel jpPanRaw;
    private javax.swing.JPanel jpPanTrue;
    private javax.swing.JPanel jpTiltRaw;
    private javax.swing.JPanel jpTiltTrue;
    private javax.swing.JTextField jtfCal;
    private javax.swing.JTextField jtfCalPan;
    private javax.swing.JTextField jtfCalTilt;
    private javax.swing.JTextField jtfCalibStepSize;
    private javax.swing.JTextField jtfDeclination;
    private javax.swing.JTextField jtfGcsAlt;
    private javax.swing.JTextField jtfGcsLat;
    private javax.swing.JTextField jtfGcsLon;
    private javax.swing.JTextField jtfManPan;
    private javax.swing.JTextField jtfManTilt;
    private javax.swing.JTextField jtfUavAlt;
    private javax.swing.JTextField jtfUavLat;
    private javax.swing.JTextField jtfUavLon;
    private javax.swing.JToggleButton jtglAuto;
    private javax.swing.JToggleButton jtglManual;
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

    public void propertyChange(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                updateDisplay();
            }
        });


    }

    public void updateDisplay() {
        jlPanRaw.setText(String.format("%06f",ptu.getPanDegrees() / 10.0));
        jlTiltRaw.setText(String.format("%06f",ptu.getTiltDegrees() / 10.0));
        jlPanTrue.setText(String.format("%06f",piccoloTracker.getPanDegrees() / 10.0));
        jlTiltTrue.setText(String.format("%06f",piccoloTracker.getTiltDegrees() / 10.0));

        panPanelTrue.setPanDegrees(piccoloTracker.getPanDegrees());
        tiltPanelTrue.setTiltDegrees(piccoloTracker.getTiltDegrees());
        panPanelRaw.setPanDegrees(ptu.getPanDegrees());
        tiltPanelRaw.setTiltDegrees(ptu.getTiltDegrees());

        jlUavLat.setText(String.format("%06f",piccoloTracker.getUavPosition().getLatitude().getDoubleValue(Angle.DEGREES)));
        jlUavLon.setText(String.format("%06f",piccoloTracker.getUavPosition().getLongitude().getDoubleValue(Angle.DEGREES)));
        jlUavAlt.setText(String.format("%06f",piccoloTracker.getUavPosition().getAltitude().getDoubleValue(Length.METERS)));


    }

    public void updateCalFields() {
        jtfCalPan.setText("" + piccoloTracker.getTrackerOffsetPan() / 10.0);
        jtfCalTilt.setText("" + piccoloTracker.getTrackerOffsetTilt() / 10.0);
    }

    public void handlePic_Telemetry(Pic_Telemetry telem) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                updateDisplay();
            }
        });

    }
}
