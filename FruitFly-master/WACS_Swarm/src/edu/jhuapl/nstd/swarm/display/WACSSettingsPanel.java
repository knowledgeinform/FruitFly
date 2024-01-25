/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * WACSSettingsPanel.java
 *
 * Created on May 12, 2010, 9:50:31 AM
 */

package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.WACSDisplayAgent;
import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior;
import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior.TRACKING_TYPE;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AgentModeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptActualBelief;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.ConfigProperty;
import edu.jhuapl.nstd.swarm.belief.EnableAutopilotControlActualBelief;
import edu.jhuapl.nstd.swarm.belief.EnableAutopilotControlCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.GimbalDeployBelief;
import edu.jhuapl.nstd.swarm.belief.IrExplosionAlgorithmEnabledBelief;
import edu.jhuapl.nstd.swarm.belief.METBelief;
import edu.jhuapl.nstd.swarm.belief.METPositionBelief;
import edu.jhuapl.nstd.swarm.belief.METTimeName;
import edu.jhuapl.nstd.swarm.belief.ParticleCloudTrackingTypeActualBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCloudTrackingTypeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.PlumeDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.TASETelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.TargetCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.WACSShutdownBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointActualBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.WindEstimateSourceCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.ZeroAirDataBelief;
import edu.jhuapl.nstd.swarm.belief.mode.Mode;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.swarm.util.Config;
import java.awt.Color;
import java.awt.MouseInfo;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author stipeja1
 */
public class WACSSettingsPanel extends javax.swing.JPanel
{
         protected SearchCanvas _canvas;
    protected BeliefManager _beliefManager;

    protected GimbalDeployBelief _gbelief;
    protected WACSShutdownBelief _wbelief;

    protected double _priorLat;
    protected double _priorLon;
    protected double _priorAlt;

    protected double _priorWindSpeed = -1;
    protected double _priorWindDir = -1;
    protected double _priorFinalLoiterAlt = -1;
    protected double _priorStandoffLoiterAlt = -1;
    protected double _priorLoiterRad = -1;
    protected double _priorInterceptAlt = -1;
    protected double _priorInterceptRad = -1;

    protected int _units = SearchCanvas.METERS;

    protected long  _GimbalCommsDelay;
    protected long  _APCommsDelay;

    protected String m_StandoffLoiterAltLastChosenUnits;
    protected String m_FinalLoiterAltLastChosenUnits;
    protected String m_LoiterRadiusLastChosenUnits;
    protected String m_InterceptAltLastChosenUnits;
    protected String m_InterceptRadiusLastChosenUnits;
    protected String m_GimbalAltLastChosenUnits;
    protected String m_WindSpeedLastChosenUnits;
    
    protected ExplosionTimeDialog m_TimeDialog = null;
    protected GimbalJogOptionsPanel m_GimbalJogOptionsPanel = null;
    
    protected final DecimalFormat m_DecFormat1 = new DecimalFormat ("#.0");
    protected final DecimalFormat m_DecFormat6 = new DecimalFormat ("#.000000");
    

    /** Creates new form WACSSettingsPanel */
    public WACSSettingsPanel()
    {
        initComponents();
    }

    public WACSSettingsPanel(SearchCanvas canvas, BeliefManager belMgr)
    {
        initComponents();
        _canvas = canvas;
	_beliefManager = belMgr;


        _APCommsDelay = Config.getConfig().getPropertyAsInteger("WACSSettingsPanel.APCommsDelay.Sec", 10);
        _GimbalCommsDelay = Config.getConfig().getPropertyAsInteger("WACSSettingsPanel.GimbalCommsDelay.Sec", 10);

        //m_EnableAutopilot.setSelected(false);
        //m_UseGuiWindRadio.setSelected(false);
        //m_UseExternalAutopilotWindRadio.setSelected(false);
        AllowInterceptActualBelief allowInterceptBlf = (AllowInterceptActualBelief)_beliefManager.get(AllowInterceptActualBelief.BELIEF_NAME);
        boolean allowIntercept = (allowInterceptBlf != null && allowInterceptBlf.getAllow());
        m_AllowIntercept.setSelected(allowIntercept);

        m_StandbyAltUnitsInput.setModel (new DefaultComboBoxModel (new Object[] {"m", "ft"}));
        m_StandbyAltUnitsInput.setSelectedIndex(1);
        m_LoiterAltUnitsInput.setModel (new DefaultComboBoxModel (new Object[] {"m", "ft"}));
        m_LoiterAltUnitsInput.setSelectedIndex(1);
        m_LoiterRadiusUnitsInput.setModel (new DefaultComboBoxModel (new Object[] {"m", "ft"}));
        m_LoiterRadiusUnitsInput.setSelectedIndex(0);
        m_InterceptAltUnitsInput.setModel (new DefaultComboBoxModel (new Object[] {"m", "ft"}));
        m_InterceptAltUnitsInput.setSelectedIndex(1);
        m_InterceptRadiusUnitsInput.setModel (new DefaultComboBoxModel (new Object[] {"m", "ft"}));
        m_InterceptRadiusUnitsInput.setSelectedIndex(0);
        m_GimbalAltUnitsInput.setModel (new DefaultComboBoxModel (new Object[] {"m", "ft"}));
        m_GimbalAltUnitsInput.setSelectedIndex(1);
        m_WindSpeedUnitsInput.setModel (new DefaultComboBoxModel (new Object[] {"m/s", "mph", "kts"}));
        m_WindSpeedUnitsInput.setSelectedIndex(0);

        ParticleCloudTrackingTypeCommandedBelief cloudTrackingBelief = new ParticleCloudTrackingTypeCommandedBelief(TRACKING_TYPE.MIXTURE, 100);
        if (cloudTrackingBelief != null)
            _beliefManager.put(cloudTrackingBelief);
    }


    public void updateLabels()
    {
        try
        {
            long dt = 0;
            long currentTime = System.currentTimeMillis()/1000;

            PlumeDetectionBelief pdb = new PlumeDetectionBelief();
            pdb = (PlumeDetectionBelief)_beliefManager.get(pdb.getName());

            if(pdb!=null)
            {
                if(m_PlumeDetections.isValid())
                    m_PlumeDetections.setText(pdb.getPlumeDetectionString());
            }

            AgentModeActualBelief agModeBlf = (AgentModeActualBelief) _beliefManager.get(AgentModeActualBelief.BELIEF_NAME);
            if (agModeBlf != null)
            {
                Mode wacsagentmode = agModeBlf.getMode(WACSAgent.AGENTNAME);
                boolean autopilotControlEnabled = false;
                EnableAutopilotControlActualBelief actualControl = (EnableAutopilotControlActualBelief)_beliefManager.get (EnableAutopilotControlActualBelief.BELIEF_NAME);
                if (actualControl != null && actualControl.getAllow())
                    autopilotControlEnabled = true;
                
                if (!autopilotControlEnabled && wacsagentmode != null && (wacsagentmode.getName().equals (LoiterBehavior.MODENAME) || wacsagentmode.getName().equals (ParticleCloudPredictionBehavior.MODENAME)))
                {
                    m_EnableAutopilot.setBackground(Color.RED);
                }
                else
                {
                    m_EnableAutopilot.setBackground(m_UseGuiWindRadio.getBackground());
                }
            }
            
            TASETelemetryBelief ttb = (TASETelemetryBelief)_beliefManager.get(TASETelemetryBelief.BELIEF_NAME);

            if(ttb!=null)
            {
                    dt = currentTime - ttb.getTimeStamp().getTime()/1000;
                    if(dt<=999)
                        m_GimbalComms.setText("Time Since Update: " + dt + "s");
                    else
                        m_GimbalComms.setText("No Comms");
                    if (dt > _GimbalCommsDelay)
                        m_GimbalComms.setBackground(Color.RED);
                    else
                        m_GimbalComms.setBackground(Color.GREEN);
            }



            METPositionBelief mpb = (METPositionBelief)_beliefManager.get(METPositionBelief.BELIEF_NAME);
            if(mpb!=null)
            {
                        
                    dt = currentTime - mpb.getTimeStamp().getTime()/1000;
                    if(dt<=999)
                        m_AutopilotComms.setText("Time Since Update: " + dt + "s");
                    else
                        m_AutopilotComms.setText("No Comms");
                    if (dt > _APCommsDelay)
                        m_AutopilotComms.setBackground(Color.RED);
                    else
                        m_AutopilotComms.setBackground(Color.GREEN);
            }

            String tmp = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
            TargetActualBelief targets = (TargetActualBelief)_beliefManager.get(TargetActualBelief.BELIEF_NAME);

            if(targets != null)
            {
                PositionTimeName ptn = targets.getPositionTimeName(tmp);
                if(ptn !=null)
                {
                    double lt, ln, at;
                    LatLonAltPosition lla = ptn.getPosition().asLatLonAltPosition();
                    at = lla.getAltitude().getDoubleValue(m_GimbalAltUnitsInput.getSelectedItem().toString().equals("ft")?Length.FEET:Length.METERS);
                    lt = lla.getLatitude().getDoubleValue(Angle.DEGREES);
                    ln = lla.getLongitude().getDoubleValue(Angle.DEGREES);

                    if(at != _priorAlt)
                    {
                        m_GimbalAlt.setText("" + m_DecFormat1.format(at));
                        _priorAlt = at;
                    }

                    if(lt != _priorLat)
                    {
                        m_GimbalLat.setText("" + m_DecFormat6.format(lt));
                        _priorLat = lt;
                    }

                    if(ln != _priorLon)
                    {
                        m_GimbalLon.setText("" + m_DecFormat6.format(ln));
                        _priorLon = ln;
                    }
                }
            }

            METBelief metbel = (METBelief)_beliefManager.get(METBelief.BELIEF_NAME);
            if(metbel != null)
            {

                METTimeName mtn = metbel.getMETTimeName(WACSAgent.AGENTNAME);
                if(mtn !=null)
                {
                    double ws, wd;
                    wd = mtn.getWindBearing().getDoubleValue(Angle.DEGREES);

                    double wsMPerS = mtn.getWindSpeed().getDoubleValue(Speed.METERS_PER_SECOND);

                    String units = m_WindSpeedUnitsInput.getSelectedItem().toString();
                    if (units.equals ("kts"))
                        ws = wsMPerS/0.5144444444;
                    else if(units.equals("mph"))
                        ws = wsMPerS/0.44704;
                    else //if(units.equals("m/s"))
                        ws = wsMPerS;

                    if(wd != _priorWindDir)
                    {
                        m_WindDirection.setText("" + m_DecFormat1.format(wd));
                        _priorWindDir = wd;
                    }

                    if(ws != _priorWindSpeed)
                    {
                        m_WindSpeed.setText("" + m_DecFormat1.format(ws));
                        _priorWindSpeed = ws;
                    }
                }
            }

            WACSWaypointActualBelief wwb = (WACSWaypointActualBelief)_beliefManager.get(WACSWaypointActualBelief.BELIEF_NAME);
            if(wwb != null)
            {
                    double laf, las, lr, ia, ir;
                    laf = wwb.getFinalLoiterAltitude().getDoubleValue(m_LoiterAltUnitsInput.getSelectedItem().toString().equals("ft")?Length.FEET:Length.METERS);
                    las = wwb.getStandoffLoiterAltitude().getDoubleValue(m_StandbyAltUnitsInput.getSelectedItem().toString().equals("ft")?Length.FEET:Length.METERS);
                    lr = wwb.getLoiterRadius().getDoubleValue(m_LoiterRadiusUnitsInput.getSelectedItem().toString().equals("ft")?Length.FEET:Length.METERS);
                    ia = wwb.getIntersectAltitude().getDoubleValue(m_InterceptAltUnitsInput.getSelectedItem().toString().equals("ft")?Length.FEET:Length.METERS);
                    ir = wwb.getIntersectRadius().getDoubleValue(m_InterceptRadiusUnitsInput.getSelectedItem().toString().equals("ft")?Length.FEET:Length.METERS);

                    if(laf != _priorFinalLoiterAlt)
                    {
                        m_LoiterAlt.setText("" + m_DecFormat1.format(laf));
                        _priorFinalLoiterAlt = laf;
                    }

                    if(las != _priorStandoffLoiterAlt)
                    {
                        m_StandbyAlt.setText("" + m_DecFormat1.format(las));
                        _priorStandoffLoiterAlt = las;
                    }

                    if(lr != _priorLoiterRad)
                    {
                        m_LoiterRad.setText("" + m_DecFormat1.format(lr));
                        _priorLoiterRad = lr;
                    }

                    if(ia != _priorInterceptAlt)
                    {
                        m_InterceptAlt.setText("" + m_DecFormat1.format(ia));
                        _priorInterceptAlt = ia;
                    }

                    if(ir != _priorInterceptRad)
                    {
                        m_InterceptRad.setText("" + m_DecFormat1.format(ir));
                        _priorInterceptRad = ir;
                    }
            }


            AgentModeActualBelief modeBlf = (AgentModeActualBelief)_beliefManager.get(AgentModeActualBelief.BELIEF_NAME);
            if (modeBlf != null)
            {
                if (modeBlf.getMode(WACSAgent.AGENTNAME) != null)
                {
                    if (modeBlf.getMode(WACSAgent.AGENTNAME).getName().equals(LoiterBehavior.MODENAME))
                    {
                        IrExplosionAlgorithmEnabledBelief irBlf = (IrExplosionAlgorithmEnabledBelief)_beliefManager.get(IrExplosionAlgorithmEnabledBelief.BELIEF_NAME);
                        if (irBlf != null)
                        {
                            if (irBlf.getEnabled())
                            {
                                m_AllowIntercept.setBackground(Color.GREEN);
                            }
                            else
                            {
                                m_AllowIntercept.setBackground(Color.RED);
                            }
                        }
                    }
                    else
                    {
                        m_AllowIntercept.setBackground(m_UseGuiWindRadio.getBackground());
                    }
                }
            }
            

        }
        catch (Exception e)
        {
            System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }

    protected void setGimbalTargetBelief(double lat, double lon, double alt)
    {
        String tmp = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");

        Latitude la = new Latitude(lat, Angle.DEGREES);
        Longitude lo = new Longitude(lon, Angle.DEGREES);
        Altitude al = new Altitude(alt, Length.METERS);

        LatLonAltPosition lla = new LatLonAltPosition(la,lo,al);
        _beliefManager.put(new TargetCommandedBelief(WACSDisplayAgent.AGENTNAME,
							 lla,
							 Length.ZERO,
							 tmp));
    }
    
    private void setWacsWaypointSettings ()
    {
        Altitude altLoiterFinal = new Altitude (Double.parseDouble(m_LoiterAlt.getText()),m_LoiterAltUnitsInput.getSelectedItem().toString().equals("ft")?Length.FEET:Length.METERS);
        Altitude altLoiterStandoff = new Altitude (Double.parseDouble(m_StandbyAlt.getText()),m_StandbyAltUnitsInput.getSelectedItem().toString().equals("ft")?Length.FEET:Length.METERS);
        Length loiterLen = new Length (Double.parseDouble(m_LoiterRad.getText()),m_LoiterRadiusUnitsInput.getSelectedItem().toString().equals("ft")?Length.FEET:Length.METERS);
        Altitude interceptAlt = new Altitude (Double.parseDouble(m_InterceptAlt.getText()),m_InterceptAltUnitsInput.getSelectedItem().toString().equals("ft")?Length.FEET:Length.METERS);
        Length interceptLen = new Length (Double.parseDouble(m_InterceptRad.getText()),m_InterceptRadiusUnitsInput.getSelectedItem().toString().equals("ft")?Length.FEET:Length.METERS);
        WACSWaypointCommandedBelief wwb = new WACSWaypointCommandedBelief(WACSDisplayAgent.AGENTNAME, interceptAlt, interceptLen, altLoiterFinal, altLoiterStandoff, loiterLen);
        _beliefManager.put(wwb);
    }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        m_Autopilot = new javax.swing.JButton();
        m_AutopilotComms = new javax.swing.JLabel();
        m_EnableAutopilot = new javax.swing.JCheckBox();
        m_Gimbal = new javax.swing.JButton();
        m_GimbalComms = new javax.swing.JLabel();
        m_Loiter = new javax.swing.JButton();
        m_Intercept = new javax.swing.JButton();
        m_LoiterAlt = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        m_LoiterRad = new javax.swing.JTextField();
        m_LoiterSet = new javax.swing.JButton();
        m_InterceptRad = new javax.swing.JTextField();
        m_InterceptSet = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        m_InterceptAlt = new javax.swing.JTextField();
        m_GimbalLon = new javax.swing.JTextField();
        m_SetGimbalLook = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        m_GimbalLat = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        m_GimbalAlt = new javax.swing.JTextField();
        m_AnacondaControl5 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        m_PlumeDetections = new javax.swing.JTextArea();
        m_WindDirection = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        m_WindSpeed = new javax.swing.JTextField();
        m_SetWind = new javax.swing.JButton();
        m_Gimbal1 = new javax.swing.JButton();
        m_ZeroAirData = new javax.swing.JButton();
        m_AllowIntercept = new javax.swing.JCheckBox();
        m_LoiterAltUnitsInput = new javax.swing.JComboBox();
        m_LoiterRadiusUnitsInput = new javax.swing.JComboBox();
        m_InterceptAltUnitsInput = new javax.swing.JComboBox();
        m_InterceptRadiusUnitsInput = new javax.swing.JComboBox();
        m_GimbalAltUnitsInput = new javax.swing.JComboBox();
        m_WindSpeedUnitsInput = new javax.swing.JComboBox();
        m_InterceptTimeButton = new javax.swing.JButton();
        m_StandbyAlt = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        m_StandbyAltUnitsInput = new javax.swing.JComboBox();
        m_UseGuiWindRadio = new javax.swing.JRadioButton();
        m_UseExternalAutopilotWindRadio = new javax.swing.JRadioButton();
        m_UseInternalPiccoloWind = new javax.swing.JRadioButton();
        m_EditTrackingType = new javax.swing.JButton();

        setDoubleBuffered(false);

        m_Autopilot.setFont(new java.awt.Font("Arial", 1, 14));
        m_Autopilot.setText("AUTOPILOT");
        m_Autopilot.setAlignmentX(0.1F);
        m_Autopilot.setAlignmentY(0.1F);
        m_Autopilot.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_Autopilot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AutopilotActionPerformed(evt);
            }
        });

        m_AutopilotComms.setBackground(new java.awt.Color(255, 0, 0));
        m_AutopilotComms.setFont(new java.awt.Font("Arial", 0, 10));
        m_AutopilotComms.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_AutopilotComms.setText("No Communications");
        m_AutopilotComms.setAlignmentX(0.1F);
        m_AutopilotComms.setAlignmentY(0.1F);
        m_AutopilotComms.setOpaque(true);

        m_EnableAutopilot.setText("Enable Control Via Autopilot");
        m_EnableAutopilot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_EnableAutopilotActionPerformed(evt);
            }
        });

        m_Gimbal.setFont(new java.awt.Font("Arial", 1, 14));
        m_Gimbal.setText("GIMBAL");
        m_Gimbal.setAlignmentX(0.1F);
        m_Gimbal.setAlignmentY(0.1F);
        m_Gimbal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_Gimbal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_GimbalActionPerformed(evt);
            }
        });

        m_GimbalComms.setBackground(new java.awt.Color(255, 0, 0));
        m_GimbalComms.setFont(new java.awt.Font("Arial", 0, 10));
        m_GimbalComms.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_GimbalComms.setText("No Communications");
        m_GimbalComms.setAlignmentX(0.1F);
        m_GimbalComms.setAlignmentY(0.1F);
        m_GimbalComms.setOpaque(true);

        m_Loiter.setFont(new java.awt.Font("Arial", 1, 14));
        m_Loiter.setText("LOITER");
        m_Loiter.setAlignmentX(0.1F);
        m_Loiter.setAlignmentY(0.1F);
        m_Loiter.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_Loiter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_LoiterActionPerformed(evt);
            }
        });

        m_Intercept.setFont(new java.awt.Font("Arial", 1, 14));
        m_Intercept.setText("INTERCEPT");
        m_Intercept.setAlignmentX(0.1F);
        m_Intercept.setAlignmentY(0.1F);
        m_Intercept.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_Intercept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_InterceptActionPerformed(evt);
            }
        });

        m_LoiterAlt.setText("1000.0");
        m_LoiterAlt.setPreferredSize(new java.awt.Dimension(42, 20));
        m_LoiterAlt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_LoiterAltActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText("Loiter Alt (AGL)");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel2.setText("Loiter Radius");

        m_LoiterRad.setText("1000.0");

        m_LoiterSet.setText("SET");
        m_LoiterSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_LoiterSetActionPerformed(evt);
            }
        });

        m_InterceptRad.setText("300.0");

        m_InterceptSet.setText("SET");
        m_InterceptSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_InterceptSetActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel3.setText("Intercept Alt(AGL)");

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel4.setText("Intercept Radius");

        m_InterceptAlt.setText("300.0");

        m_GimbalLon.setText("-76.0000");

        m_SetGimbalLook.setText("Set Gimbal Look Position");
        m_SetGimbalLook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_SetGimbalLookActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel5.setText("Gimbal Latitude");

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel6.setText("Gimbal Longitude");

        m_GimbalLat.setText("39.0000");
        m_GimbalLat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_GimbalLatActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel7.setText("Gimbal Altitude (MSL)");

        m_GimbalAlt.setText("1000.0");
        m_GimbalAlt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_GimbalAltActionPerformed(evt);
            }
        });

        m_AnacondaControl5.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_AnacondaControl5.setText("PLUME DETECTIONS");
        m_AnacondaControl5.setAlignmentX(0.1F);
        m_AnacondaControl5.setAlignmentY(0.1F);
        m_AnacondaControl5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_AnacondaControl5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AnacondaControl5ActionPerformed(evt);
            }
        });

        jScrollPane3.setAlignmentX(0.1F);
        jScrollPane3.setAlignmentY(0.1F);

        m_PlumeDetections.setColumns(20);
        m_PlumeDetections.setFont(new java.awt.Font("Arial", 0, 12));
        m_PlumeDetections.setRows(1);
        jScrollPane3.setViewportView(m_PlumeDetections);

        m_WindDirection.setText("0.0");

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel12.setText("Wind Speed ");

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel13.setText("Wind To (deg)");

        m_WindSpeed.setText("0.0");
        m_WindSpeed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_WindSpeedActionPerformed(evt);
            }
        });

        m_SetWind.setText("SET WIND");
        m_SetWind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_SetWindActionPerformed(evt);
            }
        });

        m_Gimbal1.setFont(new java.awt.Font("Arial", 1, 14));
        m_Gimbal1.setText("WIND");
        m_Gimbal1.setAlignmentX(0.1F);
        m_Gimbal1.setAlignmentY(0.1F);
        m_Gimbal1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_Gimbal1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_Gimbal1ActionPerformed(evt);
            }
        });

        m_ZeroAirData.setFont(new java.awt.Font("Arial", 1, 10));
        m_ZeroAirData.setText("Zero Air Data");
        m_ZeroAirData.setAlignmentX(0.1F);
        m_ZeroAirData.setAlignmentY(0.1F);
        m_ZeroAirData.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_ZeroAirData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_ZeroAirDataActionPerformed(evt);
            }
        });

        m_AllowIntercept.setText("Allow");
        m_AllowIntercept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AllowInterceptActionPerformed(evt);
            }
        });

        m_LoiterAltUnitsInput.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        m_LoiterAltUnitsInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_LoiterAltUnitsInputActionPerformed(evt);
            }
        });

        m_LoiterRadiusUnitsInput.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        m_LoiterRadiusUnitsInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_LoiterRadiusUnitsInputActionPerformed(evt);
            }
        });

        m_InterceptAltUnitsInput.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        m_InterceptAltUnitsInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_InterceptAltUnitsInputActionPerformed(evt);
            }
        });

        m_InterceptRadiusUnitsInput.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        m_InterceptRadiusUnitsInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_InterceptRadiusUnitsInputActionPerformed(evt);
            }
        });

        m_GimbalAltUnitsInput.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        m_GimbalAltUnitsInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_GimbalAltUnitsInputActionPerformed(evt);
            }
        });

        m_WindSpeedUnitsInput.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        m_WindSpeedUnitsInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_WindSpeedUnitsInputActionPerformed(evt);
            }
        });

        m_InterceptTimeButton.setFont(new java.awt.Font("Arial", 1, 14));
        m_InterceptTimeButton.setText("TIME");
        m_InterceptTimeButton.setAlignmentX(0.1F);
        m_InterceptTimeButton.setAlignmentY(0.1F);
        m_InterceptTimeButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_InterceptTimeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_InterceptTimeButtonActionPerformed(evt);
            }
        });

        m_StandbyAlt.setText("1000.0");
        m_StandbyAlt.setPreferredSize(new java.awt.Dimension(42, 20));
        m_StandbyAlt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_StandbyAltActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel8.setText("Standby Alt (AGL)");

        m_StandbyAltUnitsInput.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        m_StandbyAltUnitsInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_StandbyAltUnitsInputActionPerformed(evt);
            }
        });

        buttonGroup1.add(m_UseGuiWindRadio);
        m_UseGuiWindRadio.setText("Use GUI Wind");
        m_UseGuiWindRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_UseGuiWindRadioActionPerformed(evt);
            }
        });

        buttonGroup1.add(m_UseExternalAutopilotWindRadio);
        m_UseExternalAutopilotWindRadio.setText("Use External Autopilot Wind");
        m_UseExternalAutopilotWindRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_UseExternalAutopilotWindRadioActionPerformed(evt);
            }
        });

        buttonGroup1.add(m_UseInternalPiccoloWind);
        m_UseInternalPiccoloWind.setSelected(true);
        m_UseInternalPiccoloWind.setText("Use Internal Piccolo Wind");
        m_UseInternalPiccoloWind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_UseInternalPiccoloWindActionPerformed(evt);
            }
        });

        m_EditTrackingType.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_EditTrackingType.setText("EDIT");
        m_EditTrackingType.setAlignmentX(0.1F);
        m_EditTrackingType.setAlignmentY(0.1F);
        m_EditTrackingType.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_EditTrackingType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_EditTrackingTypeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(m_UseExternalAutopilotWindRadio)
                        .addContainerGap())
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(m_UseGuiWindRadio)
                            .addGap(54, 54, 54)
                            .addComponent(m_SetWind, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(42, 42, 42))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(m_UseInternalPiccoloWind)
                                .addContainerGap())
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                                        .addComponent(m_Loiter, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(m_AnacondaControl5, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(m_EditTrackingType, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)))
                                    .addGap(32, 32, 32))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel8)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(m_StandbyAlt, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(m_StandbyAltUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap())
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(m_Autopilot, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel1)
                                                .addComponent(jLabel2))
                                            .addGap(18, 18, 18)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(m_LoiterRad, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(m_LoiterRadiusUnitsInput, 0, 0, Short.MAX_VALUE))
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(m_LoiterAlt, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(m_LoiterAltUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                                            .addComponent(m_LoiterSet))
                                        .addGroup(layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(m_GimbalAlt, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(m_GimbalAltUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addComponent(m_GimbalLon, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(m_GimbalLat, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(m_SetGimbalLook, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                                        .addComponent(m_GimbalComms, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                                        .addComponent(m_Gimbal, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                                        .addComponent(m_AutopilotComms, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(m_EnableAutopilot)
                                            .addGap(18, 18, 18)
                                            .addComponent(m_ZeroAirData, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(jLabel3)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(m_InterceptRad, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_InterceptAlt, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(m_InterceptRadiusUnitsInput, 0, 0, Short.MAX_VALUE)
                                                        .addComponent(m_InterceptAltUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addComponent(jLabel4))
                                            .addGap(18, 18, 18)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(m_InterceptSet, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(m_InterceptTimeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(m_AllowIntercept))))
                                        .addComponent(m_Intercept, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(32, 32, 32))
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(m_Gimbal1, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(m_WindSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(m_WindSpeedUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addComponent(m_WindDirection, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGap(32, 32, 32)))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_AnacondaControl5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_EditTrackingType, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_Loiter, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(m_StandbyAlt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_StandbyAltUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(m_LoiterAlt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_LoiterAltUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(m_LoiterRad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_LoiterRadiusUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_LoiterSet))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_Intercept, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_InterceptTimeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(m_InterceptAlt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_InterceptAltUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_AllowIntercept))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(m_InterceptRad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_InterceptRadiusUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_InterceptSet))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_Autopilot, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_AutopilotComms)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_EnableAutopilot)
                    .addComponent(m_ZeroAirData, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_Gimbal, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_GimbalComms)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(m_GimbalLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_GimbalLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(m_GimbalAlt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_GimbalAltUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_SetGimbalLook, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(m_Gimbal1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(m_WindSpeedUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_WindSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(m_WindDirection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_UseInternalPiccoloWind)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_UseGuiWindRadio)
                    .addComponent(m_SetWind))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_UseExternalAutopilotWindRadio)
                .addContainerGap(167, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void m_AutopilotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AutopilotActionPerformed

}//GEN-LAST:event_m_AutopilotActionPerformed

    private void m_GimbalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_GimbalActionPerformed
        // TODO add your handling code here:
        if (m_GimbalJogOptionsPanel == null)
            m_GimbalJogOptionsPanel = new GimbalJogOptionsPanel(_beliefManager);

        m_GimbalJogOptionsPanel.setLocation(MouseInfo.getPointerInfo().getLocation());
        m_GimbalJogOptionsPanel.setVisible (true);

    }//GEN-LAST:event_m_GimbalActionPerformed

    private void m_LoiterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_LoiterActionPerformed
        
        ExplosionTimeCommandedBelief expBlf = (ExplosionTimeCommandedBelief)_beliefManager.get (ExplosionTimeCommandedBelief.BELIEF_NAME);
        if (expBlf == null && Config.getConfig().getPropertyAsBoolean("WacsSettingsDefaults.UseExplosionTimer", true))
        {
            m_InterceptTimeButtonActionPerformed (null);
            new Thread (){
                public void run ()
                {
                    while (true)
                    {
                        ExplosionTimeCommandedBelief expBlf = (ExplosionTimeCommandedBelief)_beliefManager.get (ExplosionTimeCommandedBelief.BELIEF_NAME);
                        if (expBlf != null)
                            break;

                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(WACSSettingsPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    AgentModeCommandedBelief loitermode = new AgentModeCommandedBelief(WACSAgent.AGENTNAME, new Mode(LoiterBehavior.MODENAME));
                    _beliefManager.put(loitermode);

                }

            }.start();
        }
        else
        {
            AgentModeCommandedBelief loitermode = new AgentModeCommandedBelief(WACSAgent.AGENTNAME, new Mode(LoiterBehavior.MODENAME));
            _beliefManager.put(loitermode);
        }
    }//GEN-LAST:event_m_LoiterActionPerformed

    private void m_InterceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_InterceptActionPerformed
        AllowInterceptActualBelief allowInterceptBlf = (AllowInterceptActualBelief)_beliefManager.get(AllowInterceptActualBelief.BELIEF_NAME);
        if (allowInterceptBlf != null && allowInterceptBlf.getAllow())
        {
            String gimbalTargetName = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
            TargetActualBelief targets = (TargetActualBelief)_beliefManager.get(TargetActualBelief.BELIEF_NAME);
            if(targets != null)
            {
                PositionTimeName positionTimeName = targets.getPositionTimeName(gimbalTargetName);

                if(positionTimeName != null)
                {
                    _beliefManager.put(new ExplosionBelief(positionTimeName.getPosition(), System.currentTimeMillis()));
                    _beliefManager.put(new AgentModeCommandedBelief(WACSAgent.AGENTNAME, new Mode(ParticleCloudPredictionBehavior.MODENAME)));
                }
            }
        }
    }//GEN-LAST:event_m_InterceptActionPerformed

    private void m_LoiterAltActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_LoiterAltActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_LoiterAltActionPerformed

    private void m_GimbalLatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_GimbalLatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_GimbalLatActionPerformed

    private void m_GimbalAltActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_GimbalAltActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_GimbalAltActionPerformed

    private void m_WindSpeedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_WindSpeedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_WindSpeedActionPerformed

    private void m_Gimbal1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_Gimbal1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_Gimbal1ActionPerformed

    private void m_LoiterSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_LoiterSetActionPerformed
       setWacsWaypointSettings ();
    }//GEN-LAST:event_m_LoiterSetActionPerformed

    private void m_SetGimbalLookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_SetGimbalLookActionPerformed

        double alt = Double.parseDouble(m_GimbalAlt.getText());
        Double altM = null;
        if (m_GimbalAltUnitsInput.getSelectedItem().toString().equals("ft"))
            altM = new Double (alt*0.3048);
        else //if(m_GimbalAltUnitsInput.getSelectedItem().toString().equals("m"))
            altM = new Double (alt);

        setGimbalTargetBelief(Double.parseDouble(m_GimbalLat.getText()), Double.parseDouble(m_GimbalLon.getText()),altM);
    }//GEN-LAST:event_m_SetGimbalLookActionPerformed

    private void m_SetWindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_SetWindActionPerformed
        
        String units = m_WindSpeedUnitsInput.getSelectedItem().toString();
        double value = Double.parseDouble(m_WindSpeed.getText());
        double speedMPerS = 0;
        if (units.equals ("kts"))
            speedMPerS = value*0.5144444444;
        else if(units.equals("mph"))
            speedMPerS = value*0.44704;
        else //if(units.equals("m/s"))
            speedMPerS = value;
        
        Speed speed = new Speed (speedMPerS,Speed.METERS_PER_SECOND);
        METTimeName mt = new METTimeName(WACSAgent.AGENTNAME, new NavyAngle(Double.parseDouble(m_WindDirection.getText()), Angle.DEGREES), speed,  new Date());
            METBelief mBel = new METBelief(WACSDisplayAgent.AGENTNAME, mt);
            _beliefManager.put(mBel);
             mBel = new METBelief(WACSAgent.AGENTNAME, mt);
            _beliefManager.put(mBel);
    }//GEN-LAST:event_m_SetWindActionPerformed

    private void m_InterceptSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_InterceptSetActionPerformed
       setWacsWaypointSettings();
    }//GEN-LAST:event_m_InterceptSetActionPerformed

    private void m_EnableAutopilotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_EnableAutopilotActionPerformed
            
        EnableAutopilotControlCommandedBelief enableBelief;
        if(m_EnableAutopilot.isSelected())
            enableBelief = new EnableAutopilotControlCommandedBelief (WACSAgent.AGENTNAME, true);
        else
            enableBelief = new EnableAutopilotControlCommandedBelief (WACSAgent.AGENTNAME, false);
        
        _beliefManager.put(enableBelief);
    }//GEN-LAST:event_m_EnableAutopilotActionPerformed

    private void m_AnacondaControl5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AnacondaControl5ActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_m_AnacondaControl5ActionPerformed

    private void m_ZeroAirDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_ZeroAirDataActionPerformed
        
        //String retVal = JOptionPane.showInputDialog(this.getParent().getParent(), new String ("Set Autopilot Altitude (ASL m)"), new String ("Attention"), JOptionPane.QUESTION_MESSAGE);
        //String retVal = JOptionPane.showInputDialog(m_ZeroAirData, new String ("Set Autopilot Altitude (ASL m)"), new String ("Attention"), JOptionPane.QUESTION_MESSAGE);
       
        double agentLat, agentLon;
        double defPressPa = Config.getConfig().getPropertyAsDouble("WACSSettingsPanel.ZeroAirData.BasePressurePa", 29.92*3386.4);  //inHg to Pa
        PiccoloTelemetryBelief picBlf = (PiccoloTelemetryBelief)_beliefManager.get (PiccoloTelemetryBelief.BELIEF_NAME);
        if (picBlf == null || picBlf.getPiccoloTelemetry() == null)
        {
            agentLat = Config.getConfig().getPropertyAsDouble("agent.startLat", 0);
            agentLon = Config.getConfig().getPropertyAsDouble("agent.startLon", 0);
        }
        else
        {
            agentLat = picBlf.getPiccoloTelemetry().Lat;
            agentLon = picBlf.getPiccoloTelemetry().Lon;
            //defPressPa = picBlf.getPiccoloTelemetry().StaticPressPa;
        }


        double defAltMSLm = DtedGlobalMap.getDted().getAltitudeMSL(agentLat, agentLon);

        if (Config.getConfig().getPropertyAsBoolean("WACSSettingsPanel.ZeroAirData.PromptDialog", true))
        {
            ZeroAirDataFrame newFrame = new ZeroAirDataFrame(_beliefManager, defAltMSLm, defPressPa);
            newFrame.setVisible(true);
            //ZeroAirDataPanel.getZeroAirData(defAltMSLm, defPressPa, _beliefManager);
        }
        else
            _beliefManager.put(new ZeroAirDataBelief(new Double (defAltMSLm), new Double (defPressPa)));
        
    }//GEN-LAST:event_m_ZeroAirDataActionPerformed

    private void m_AllowInterceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AllowInterceptActionPerformed
        
        AllowInterceptCommandedBelief commandAllowBlf;
                
        if(m_AllowIntercept.isSelected())
        {
            commandAllowBlf = new AllowInterceptCommandedBelief (WACSDisplayAgent.AGENTNAME, true);
        }
        else
        {
            commandAllowBlf = new AllowInterceptCommandedBelief (WACSDisplayAgent.AGENTNAME, false);
        }
         _beliefManager.put(commandAllowBlf);
    }//GEN-LAST:event_m_AllowInterceptActionPerformed

    private void m_InterceptAltUnitsInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_InterceptAltUnitsInputActionPerformed
        // TODO add your handling code here:
        m_InterceptAltLastChosenUnits = changeLengthUnits (m_InterceptAlt, m_InterceptAltUnitsInput, m_InterceptAltLastChosenUnits, "intercept altitude");
    }//GEN-LAST:event_m_InterceptAltUnitsInputActionPerformed

    private void m_LoiterAltUnitsInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_LoiterAltUnitsInputActionPerformed
        // TODO add your handling code here:
        m_FinalLoiterAltLastChosenUnits = changeLengthUnits (m_LoiterAlt, m_LoiterAltUnitsInput, m_FinalLoiterAltLastChosenUnits, "loiter altitude");
    }//GEN-LAST:event_m_LoiterAltUnitsInputActionPerformed

    private void m_LoiterRadiusUnitsInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_LoiterRadiusUnitsInputActionPerformed
        // TODO add your handling code here:
        m_LoiterRadiusLastChosenUnits = changeLengthUnits (m_LoiterRad, m_LoiterRadiusUnitsInput, m_LoiterRadiusLastChosenUnits, "loiter radius");
    }//GEN-LAST:event_m_LoiterRadiusUnitsInputActionPerformed

    private void m_InterceptRadiusUnitsInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_InterceptRadiusUnitsInputActionPerformed
        // TODO add your handling code here:
        m_InterceptRadiusLastChosenUnits = changeLengthUnits (m_InterceptRad, m_InterceptRadiusUnitsInput, m_InterceptRadiusLastChosenUnits, "intercept radius");
    }//GEN-LAST:event_m_InterceptRadiusUnitsInputActionPerformed

    private void m_GimbalAltUnitsInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_GimbalAltUnitsInputActionPerformed
        // TODO add your handling code here:
        m_GimbalAltLastChosenUnits = changeLengthUnits (m_GimbalAlt, m_GimbalAltUnitsInput, m_GimbalAltLastChosenUnits, "gimbal altitude");
    }//GEN-LAST:event_m_GimbalAltUnitsInputActionPerformed

    private void m_WindSpeedUnitsInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_WindSpeedUnitsInputActionPerformed
        // TODO add your handling code here:
        m_WindSpeedLastChosenUnits = changeSpeedUnits (m_WindSpeed, m_WindSpeedUnitsInput, m_WindSpeedLastChosenUnits, "wind speed");
    }//GEN-LAST:event_m_WindSpeedUnitsInputActionPerformed

    private void m_InterceptTimeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_InterceptTimeButtonActionPerformed
        // TODO add your handling code here:
        if (m_TimeDialog == null)
        {
            m_TimeDialog = new ExplosionTimeDialog(_beliefManager);
            m_TimeDialog.setLocation(MouseInfo.getPointerInfo().getLocation());
        }

        m_TimeDialog.setVisible(true);
    }//GEN-LAST:event_m_InterceptTimeButtonActionPerformed

    private void m_StandbyAltActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_StandbyAltActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_StandbyAltActionPerformed

    private void m_StandbyAltUnitsInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_StandbyAltUnitsInputActionPerformed
        // TODO add your handling code here:
        m_StandoffLoiterAltLastChosenUnits = changeLengthUnits (m_StandbyAlt, m_StandbyAltUnitsInput, m_StandoffLoiterAltLastChosenUnits, "standoff altitude");
    }//GEN-LAST:event_m_StandbyAltUnitsInputActionPerformed

    private void m_UseGuiWindRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_UseGuiWindRadioActionPerformed
        // TODO add your handling code here:
        updateWindOption ();
        
    }//GEN-LAST:event_m_UseGuiWindRadioActionPerformed

    private void m_UseExternalAutopilotWindRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_UseExternalAutopilotWindRadioActionPerformed
        // TODO add your handling code here:
        updateWindOption();
    }//GEN-LAST:event_m_UseExternalAutopilotWindRadioActionPerformed

    private void m_UseInternalPiccoloWindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_UseInternalPiccoloWindActionPerformed
        // TODO add your handling code here:
        updateWindOption();
    }//GEN-LAST:event_m_UseInternalPiccoloWindActionPerformed

private void m_EditTrackingTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_EditTrackingTypeActionPerformed
// TODO add your handling code here:
    
    String currentTypeName = "MIXTURE";
    ParticleCloudTrackingTypeActualBelief currBelief = (ParticleCloudTrackingTypeActualBelief)_beliefManager.get(ParticleCloudTrackingTypeActualBelief.BELIEF_NAME);
    if (currBelief != null)
    {
        if (currBelief.getTrackingType() == TRACKING_TYPE.MIXTURE)
            currentTypeName = "MIXTURE";
        else if (currBelief.getTrackingType() == TRACKING_TYPE.PARTICLE)
            currentTypeName = "PARTICLE";
        else if (currBelief.getTrackingType() == TRACKING_TYPE.CHEMICAL)
            currentTypeName = "CHEMICAL";
        
    }
    
    Object options[] = {"MIXTURE", "PARTICLES", "CHEMICALS"};
    int opt = JOptionPane.showOptionDialog(this, "What detection types should be tracked (Current: " + currentTypeName + ")?", "Choose Tracking Type", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    
    ParticleCloudTrackingTypeCommandedBelief belief = null;
    if (opt == 0)
        belief = new ParticleCloudTrackingTypeCommandedBelief(TRACKING_TYPE.MIXTURE);
    else if (opt == 1)
        belief = new ParticleCloudTrackingTypeCommandedBelief(TRACKING_TYPE.PARTICLE);
    else if (opt == 2)
        belief = new ParticleCloudTrackingTypeCommandedBelief(TRACKING_TYPE.CHEMICAL);
    
    if (belief != null)
        _beliefManager.put(belief);
}//GEN-LAST:event_m_EditTrackingTypeActionPerformed


    private void updateWindOption()
    {
        try
        {
            WindEstimateSourceCommandedBelief cmdBlf = null;
            if (m_UseInternalPiccoloWind.isSelected())
                cmdBlf = new WindEstimateSourceCommandedBelief(WindEstimateSourceCommandedBelief.WINDSOURCE_WACSAUTOPILOT);
            else if (m_UseExternalAutopilotWindRadio.isSelected())
                cmdBlf = new WindEstimateSourceCommandedBelief(WindEstimateSourceCommandedBelief.WINDSOURCE_UAVAUTOPILOT);
            else if (m_UseGuiWindRadio.isSelected())
                cmdBlf = new WindEstimateSourceCommandedBelief(WindEstimateSourceCommandedBelief.WINDSOURCE_WACSGROUNDSTATION);

            _beliefManager.put(cmdBlf);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private String changeLengthUnits (final JTextField inputField, JComboBox unitsField, String lastUnits, final String fieldType)
    {
        final String oldText = inputField.getText();
        final String oldUnits = lastUnits;
        final String newUnits = unitsField.getSelectedItem().toString();
        final javax.swing.JPanel parent = this;

        if (oldText != null && !oldText.equals (""))
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                     try
                    {
                        double oldValue = Double.parseDouble(oldText);

                        if (newUnits.equals ("m") && oldUnits != null && oldUnits.equals ("ft"))
                        {
                            double newValue = oldValue*0.3048;
                            inputField.setText (m_DecFormat1.format(newValue) + "");
                            inputField.setCaretPosition(0);
                        }
                        else if(newUnits.equals("ft") && oldUnits != null && oldUnits.equals("m"))
                        {
                            double newValue = oldValue/0.3048;
                            inputField.setText (m_DecFormat1.format(newValue) + "");
                            inputField.setCaretPosition(0);
                        }

                    }
                    catch (Exception e)
                    {
                        JOptionPane.showMessageDialog(parent, "Error parsing " + fieldType + ": " + oldText);
                        e.printStackTrace ();
                    }
                }
            });
        }

        lastUnits = newUnits;
        return lastUnits;
    }

    private String changeSpeedUnits (final JTextField inputField, JComboBox unitsField, String lastUnits, final String fieldType)
    {
        final String oldText = inputField.getText();
        final String oldUnits = lastUnits;
        final String newUnits = unitsField.getSelectedItem().toString();
        final javax.swing.JPanel parent = this;

        if (oldText != null && !oldText.equals (""))
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        double oldValue = Double.parseDouble(oldText);

                        if (newUnits.equals ("m/s") && oldUnits != null && oldUnits.equals ("mph"))
                        {
                            double newValue = oldValue*0.44704;
                            inputField.setText (m_DecFormat1.format(newValue) + "");
                            inputField.setCaretPosition(0);
                        }
                        else if(newUnits.equals("m/s") && oldUnits != null && oldUnits.equals("kts"))
                        {
                            double newValue = oldValue*0.5144444444;
                            inputField.setText (m_DecFormat1.format(newValue) + "");
                            inputField.setCaretPosition(0);
                        }

                        else if(newUnits.equals("mph") && oldUnits != null && oldUnits.equals("m/s"))
                        {
                            double newValue = oldValue/0.44704;
                            inputField.setText (m_DecFormat1.format(newValue) + "");
                            inputField.setCaretPosition(0);
                        }
                        else if(newUnits.equals("mph") && oldUnits != null && oldUnits.equals("kts"))
                        {
                            double newValue = oldValue*1.15077945;
                            inputField.setText (m_DecFormat1.format(newValue) + "");
                            inputField.setCaretPosition(0);
                        }

                        else if(newUnits.equals("kts") && oldUnits != null && oldUnits.equals("m/s"))
                        {
                            double newValue = oldValue/0.5144444444;
                            inputField.setText (m_DecFormat1.format(newValue) + "");
                            inputField.setCaretPosition(0);
                        }
                        else if(newUnits.equals("kts") && oldUnits != null && oldUnits.equals("mph"))
                        {
                            double newValue = oldValue/1.15077945;
                            inputField.setText (m_DecFormat1.format(newValue) + "");
                            inputField.setCaretPosition(0);
                        }

                    }
                    catch (Exception e)
                    {
                        JOptionPane.showMessageDialog(parent, "Error parsing " + fieldType + ": " + oldText);
                        e.printStackTrace ();
                    }
                }
            });
        }

        lastUnits = newUnits;
        return lastUnits;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JCheckBox m_AllowIntercept;
    private javax.swing.JButton m_AnacondaControl5;
    private javax.swing.JButton m_Autopilot;
    private javax.swing.JLabel m_AutopilotComms;
    private javax.swing.JButton m_EditTrackingType;
    private javax.swing.JCheckBox m_EnableAutopilot;
    private javax.swing.JButton m_Gimbal;
    private javax.swing.JButton m_Gimbal1;
    private javax.swing.JTextField m_GimbalAlt;
    private javax.swing.JComboBox m_GimbalAltUnitsInput;
    private javax.swing.JLabel m_GimbalComms;
    private javax.swing.JTextField m_GimbalLat;
    private javax.swing.JTextField m_GimbalLon;
    private javax.swing.JButton m_Intercept;
    private javax.swing.JTextField m_InterceptAlt;
    private javax.swing.JComboBox m_InterceptAltUnitsInput;
    private javax.swing.JTextField m_InterceptRad;
    private javax.swing.JComboBox m_InterceptRadiusUnitsInput;
    private javax.swing.JButton m_InterceptSet;
    private javax.swing.JButton m_InterceptTimeButton;
    private javax.swing.JButton m_Loiter;
    private javax.swing.JTextField m_LoiterAlt;
    private javax.swing.JComboBox m_LoiterAltUnitsInput;
    private javax.swing.JTextField m_LoiterRad;
    private javax.swing.JComboBox m_LoiterRadiusUnitsInput;
    private javax.swing.JButton m_LoiterSet;
    private javax.swing.JTextArea m_PlumeDetections;
    private javax.swing.JButton m_SetGimbalLook;
    private javax.swing.JButton m_SetWind;
    private javax.swing.JTextField m_StandbyAlt;
    private javax.swing.JComboBox m_StandbyAltUnitsInput;
    private javax.swing.JRadioButton m_UseExternalAutopilotWindRadio;
    private javax.swing.JRadioButton m_UseGuiWindRadio;
    private javax.swing.JRadioButton m_UseInternalPiccoloWind;
    private javax.swing.JTextField m_WindDirection;
    private javax.swing.JTextField m_WindSpeed;
    private javax.swing.JComboBox m_WindSpeedUnitsInput;
    private javax.swing.JButton m_ZeroAirData;
    // End of variables declaration//GEN-END:variables

    
   
}
