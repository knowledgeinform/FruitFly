/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import edu.jhuapl.nstd.math.CoordConversions.ConvertMGRSStringToLatLonAlt;
import edu.jhuapl.nstd.math.CoordConversions.PositionStringFormatter;
import edu.jhuapl.nstd.swarm.MissionErrorManager;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.WACSDisplayAgent;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.Classification;
import edu.jhuapl.nstd.swarm.belief.ClassificationBelief;
import edu.jhuapl.nstd.swarm.belief.ConfigProperty;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionActualBelief;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.TargetCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointActualBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointBaseBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointCommandedBelief;
import edu.jhuapl.nstd.swarm.util.Config;
import java.awt.Color;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.util.Date;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import edu.jhuapl.nstd.swarm.belief.METBelief;
import edu.jhuapl.nstd.swarm.belief.METTimeName;
import edu.jhuapl.nstd.swarm.belief.WindEstimateSourceCommandedBelief;


/**
 *
 * @author humphjc1
 */
public class AdvancedSettingsPanel extends javax.swing.JPanel implements Updateable, DisplayUnitsManager.DisplayUnitsChangeListener, MissionErrorManager.CommandedBeliefErrorListener
{
    private final static int POSITIONUNITS_DECDEGREES = 1;
    private final static int POSITIONUNITS_DEGMIN = 2;
    private final static int POSITIONUNITS_DEGMINSEC = 3;
    private final static int POSITIONUNITS_MGRS = 4;
    
    
    private BeliefManager m_BeliefManager;
    
    private Date m_LastWwbUpdatedTime;
    private Date m_LastTbUpdatedTime;
    private Date m_LastRdbUpdatedTime;
    private boolean m_ForceUpdate;
    
    private DecimalFormat m_DecFormat1 = new DecimalFormat ("0.#");
    private DecimalFormat m_DecFormat2 = new DecimalFormat ("0.##");
    private DecimalFormat m_DecFormat4 = new DecimalFormat ("0.####");
    private DecimalFormat m_DecFormat6 = new DecimalFormat ("0.######");
    
    private int m_StrikeLatitudeUnits;
    private int m_StrikeLongitudeUnits;
    private int m_LoiterLatitudeUnits;
    private int m_LoiterLongitudeUnits;

    /**
     * Creates new form AdvancedSettingsPanel
     */
    public AdvancedSettingsPanel(BeliefManager belMgr) 
    {
        initComponents();
        m_BeliefManager = belMgr;
        
        DisplayUnitsManager.addChangeListener(this);
        MissionErrorManager.getInstance().registerErrorListener(this);
        
        strikeLatitudeUnitsChanged(null);
        strikeLongitudeUnitsChanged(null);
        loiterLatitudeUnitsChanged(null);
        loiterLongitudeUnitsChanged(null);
        jToggleButton1ActionPerformed (null);
    }
    
    @Override
    public void unitsChanged ()
    {
        m_ForceUpdate = true;
    }
    
    private void updateWindOption()
    {
        try
        {
            WindEstimateSourceCommandedBelief cmdBlf = null;
            if (jToggleButton1.isSelected())
                cmdBlf = new WindEstimateSourceCommandedBelief(WindEstimateSourceCommandedBelief.WINDSOURCE_WACSAUTOPILOT);
            else if (jToggleButton2.isSelected())
                cmdBlf = new WindEstimateSourceCommandedBelief(WindEstimateSourceCommandedBelief.WINDSOURCE_UAVAUTOPILOT);
            else if (jToggleButton3.isSelected())
                cmdBlf = new WindEstimateSourceCommandedBelief(WindEstimateSourceCommandedBelief.WINDSOURCE_WACSGROUNDSTATION);
            
            m_BeliefManager.put(cmdBlf);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public void handleCommandedBeliefError(MissionErrorManager.ErrorCodeBase errorCode, int alarmLevel) 
    {
        if (errorCode == MissionErrorManager.COMMANDACTUALSYNC_WACSWAYPOINT_ERRORCODE)
        {
            alarmLabelBackground(jLabel3, alarmLevel);
            alarmLabelBackground(jLabel4, alarmLevel);
            alarmLabelBackground(jLabel5, alarmLevel);
            alarmLabelBackground(jLabel7, alarmLevel);
            alarmLabelBackground(jLabel9, alarmLevel);
        }
        else if (errorCode == MissionErrorManager.COMMANDACTUALSYNC_TARGETBELIEF_ERRORCODE)
        {
            alarmLabelBackground(jLabel11, alarmLevel);
            alarmLabelBackground(jLabel16, alarmLevel);
            alarmLabelBackground(jLabel21, alarmLevel);
        }
        else if (errorCode == MissionErrorManager.COMMANDACTUALSYNC_RACETRACKDEFINITION_ERRORCODE)
        {
            alarmLabelBackground(jLabel23, alarmLevel);
            alarmLabelBackground(jLabel32, alarmLevel);
        }
        else if (errorCode == MissionErrorManager.COMMANDACTUALSYNC_WINDSOURCE_ERRORCODE)
        {
            alarmLabelBackground(jLabel24, alarmLevel);
            alarmLabelBackground(jLabel27, alarmLevel);
        }
        
    }
    
    public void alarmLabelBackground (JComponent label, int alarmLevel)
    {
        if (alarmLevel == MissionErrorManager.ALARMLEVEL_ERROR)
            alarmLabelBackground (label, Color.RED);
        else if (alarmLevel == MissionErrorManager.ALARMLEVEL_WARNING)
            alarmLabelBackground (label, Color.ORANGE);
        else
        {
            //alarmLabelBackground (label, null);
            if (label instanceof JLabel && alarmLevel == MissionErrorManager.ALARMLEVEL_NOALARM)
            {
                alarmLabelBackground (label, Color.GREEN);
            }
            else
            {
                alarmLabelBackground (label, null);
            }
        }
    }
    
    public void alarmLabelBackground (JComponent label, Color backgroundColor)
    {
        if (backgroundColor != null && (!label.getBackground().equals (backgroundColor) || !label.isOpaque()))
        {    
            label.setBackground(backgroundColor);
            label.setOpaque(true);
            label.repaint();
        }
        else if (backgroundColor == null && label.isOpaque())
        {
            label.setOpaque(false);
            label.repaint();
        }
    }
    
    @Override
    public void update ()
    {
        WACSWaypointActualBelief wwb = (WACSWaypointActualBelief)m_BeliefManager.get(WACSWaypointActualBelief.BELIEF_NAME);
        if (wwb != null && (m_ForceUpdate || m_LastWwbUpdatedTime == null || wwb.getTimeStamp().after(m_LastWwbUpdatedTime)))
        {
            String loiterStandoffAltitudeText;
            if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                loiterStandoffAltitudeText = m_DecFormat1.format(wwb.getStandoffLoiterAltitude().getDoubleValue(Length.FEET)) + " ft AGL";
            else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
                loiterStandoffAltitudeText = m_DecFormat1.format(wwb.getStandoffLoiterAltitude().getDoubleValue(Length.METERS)) + " m AGL";
            jLabel3.setText (loiterStandoffAltitudeText);
            
            String loiterFinalAltitudeText;
            if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                loiterFinalAltitudeText = m_DecFormat1.format(wwb.getFinalLoiterAltitude().getDoubleValue(Length.FEET)) + " ft AGL";
            else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
                loiterFinalAltitudeText = m_DecFormat1.format(wwb.getFinalLoiterAltitude().getDoubleValue(Length.METERS)) + " m AGL";
            jLabel4.setText (loiterFinalAltitudeText);
            
            String loiterRadiusText;
            if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.RANGE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                loiterRadiusText = m_DecFormat1.format(wwb.getLoiterRadius().getDoubleValue(Length.FEET)) + " ft";
            else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.RANGE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
                loiterRadiusText = m_DecFormat1.format(wwb.getLoiterRadius().getDoubleValue(Length.METERS)) + " m";
            jLabel5.setText (loiterRadiusText);
            
            String interceptAltitudeText;
            if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                interceptAltitudeText = m_DecFormat1.format(wwb.getIntersectAltitude().getDoubleValue(Length.FEET)) + " ft AGL";
            else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
                interceptAltitudeText = m_DecFormat1.format(wwb.getIntersectAltitude().getDoubleValue(Length.METERS)) + " m AGL";
            jLabel7.setText (interceptAltitudeText);

            String interceptRadiusText;
            if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.RANGE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                interceptRadiusText = m_DecFormat1.format(wwb.getIntersectRadius().getDoubleValue(Length.FEET)) + " ft";
            else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.RANGE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
                interceptRadiusText = m_DecFormat1.format(wwb.getIntersectRadius().getDoubleValue(Length.METERS)) + " m";
            jLabel9.setText (interceptRadiusText);
            
            m_LastWwbUpdatedTime = wwb.getTimeStamp();
        }
        
        TargetActualBelief tb = (TargetActualBelief)m_BeliefManager.get(TargetActualBelief.BELIEF_NAME);
        if (tb != null)
        {
            String targetName = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
            PositionTimeName ptn = tb.getPositionTimeName(targetName);
            if (ptn != null && ( m_ForceUpdate || m_LastTbUpdatedTime == null || ptn.getTime().after(m_LastTbUpdatedTime)))
            {
                double strikeLatDecDeg = ptn.getPosition().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES);
                double strikeLonDecDeg = ptn.getPosition().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES);
                Altitude strikeAlt = ptn.getPosition().asLatLonAltPosition().getAltitude();
                
                if (m_StrikeLatitudeUnits == POSITIONUNITS_MGRS || m_StrikeLongitudeUnits == POSITIONUNITS_MGRS)
                {
                    String text = PositionStringFormatter.formatLatLonAsMGRS(strikeLatDecDeg, strikeLonDecDeg);
                    jLabel11.setText (text);
                }
                else
                {
                    updatePositionLabel (jLabel11, strikeLatDecDeg, m_StrikeLatitudeUnits, "N", "S");
                    updatePositionLabel (jLabel16, strikeLonDecDeg, m_StrikeLongitudeUnits, "E", "W");
                }
                
                String strikeAltText;
                if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                    strikeAltText = m_DecFormat1.format(strikeAlt.getDoubleValue(Length.FEET)) + " ft";
                else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
                    strikeAltText = m_DecFormat1.format(strikeAlt.getDoubleValue(Length.METERS)) + " m";
                jLabel21.setText (strikeAltText);
                
                
                m_LastTbUpdatedTime = ptn.getTime();
            }
        }
        
        RacetrackDefinitionActualBelief rdb = (RacetrackDefinitionActualBelief)m_BeliefManager.get(RacetrackDefinitionActualBelief.BELIEF_NAME);
        if (rdb != null && ( m_ForceUpdate || m_LastRdbUpdatedTime == null || rdb.getTimeStamp().after(m_LastRdbUpdatedTime)))
        {
            double loiterLatDecDeg = rdb.getStartPosition().getLatitude().getDoubleValue(Angle.DEGREES);
            double loiterLonDecDeg = rdb.getStartPosition().getLongitude().getDoubleValue(Angle.DEGREES);

            if (m_LoiterLatitudeUnits == POSITIONUNITS_MGRS || m_LoiterLongitudeUnits == POSITIONUNITS_MGRS)
            {
                String text = PositionStringFormatter.formatLatLonAsMGRS(loiterLatDecDeg, loiterLonDecDeg);
                jLabel23.setText (text);
            }
            else
            {        
                updatePositionLabel (jLabel23, loiterLatDecDeg, m_LoiterLatitudeUnits, "N", "S");
                updatePositionLabel (jLabel32, loiterLonDecDeg, m_LoiterLongitudeUnits, "E", "W");
            }

            m_LastRdbUpdatedTime = rdb.getTimeStamp();
        }
        
        METBelief currMetBelief = (METBelief)m_BeliefManager.get(METBelief.BELIEF_NAME);
        if (currMetBelief != null)
        {
            METTimeName metTN = currMetBelief.getMETTimeName(WACSAgent.AGENTNAME);
            if (metTN != null)
            {
                NavyAngle windBearingTo = metTN.getWindBearing();
                Speed windSpeed = metTN.getWindSpeed();

                String speedText = "";
                int speedUnits = DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.WINDSPEED_UNITS);
                if (speedUnits == DisplayUnitsManager.SPEED_KNOTS)
                    speedText = m_DecFormat1.format(windSpeed.getDoubleValue(Speed.KNOTS)) + " kts";
                else if (speedUnits == DisplayUnitsManager.SPEED_METERSPERSEC)
                    speedText = m_DecFormat1.format(windSpeed.getDoubleValue(Speed.METERS_PER_SECOND)) + " mps";
                else //if (speedUnits == DisplayUnitsManager.SPEED_MILESPERHOUR)
                    speedText = m_DecFormat1.format((windSpeed.getDoubleValue(Speed.FEET_PER_SECOND)*3600.0/5280)) + " mph";

                double windBearingFromDeg = windBearingTo.plus(Angle.HALF_CIRCLE).getDoubleValue(Angle.DEGREES);
                
                jLabel24.setText (speedText);
                jLabel27.setText (((int)windBearingFromDeg) + " deg");
            }
        }
        
        m_ForceUpdate = false;
    }
    
    private void updatePositionLabel (JLabel label, double positionDecDeg, int positionUnits, String positiveChar, String negativeChar)
    {
        if (positionUnits == POSITIONUNITS_DECDEGREES)
        {
            label.setText (PositionStringFormatter.formatLatORLonAsDecDeg(positionDecDeg, positiveChar, negativeChar));
            
        }
        else if (positionUnits == POSITIONUNITS_DEGMIN)
        {
            label.setText (PositionStringFormatter.formatLatORLonAsDegMin(positionDecDeg, positiveChar, negativeChar));
        }
        else //if (positionUnits == POSITIONUNITS_DEGMINSEC)
        {
            label.setText (PositionStringFormatter.formatLatORLonAsDegMinSec(positionDecDeg, positiveChar, negativeChar));
        }
    }
    
    private void clearWwbInputs ()
    {
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        jTextField5.setText("");
        jTextField6.setText("");
    }

    private int getPositionUnitsFromString (String selectedUnits)
    {
        if (selectedUnits.equals ("DD"))
        {
            return POSITIONUNITS_DECDEGREES;
        }
        else if (selectedUnits.equals ("DM"))
        {
            return POSITIONUNITS_DEGMIN;
        }
        else if (selectedUnits.equals ("MGRS"))
        {
            return POSITIONUNITS_MGRS;
        }
        else //if (selectedUnits.equals ("DMS"))
        {
            return POSITIONUNITS_DEGMINSEC;
        }
    }
    
    private void updateInputBoxes (int units, JTextField textField1, JLabel label1, JTextField textField2, JLabel label2, JTextField textField3, JLabel label3)
    {
        if (units == POSITIONUNITS_DECDEGREES)
        {
            textField1.setVisible(true);
            label1.setText("D.D");
            label1.setVisible(true);
            
            textField2.setVisible(false);
            textField2.setText("");
            label2.setVisible(false);
            textField3.setVisible(false);
            textField3.setText("");
            label3.setVisible(false);
        }
        else if (units == POSITIONUNITS_DEGMIN)
        {
            textField1.setVisible(true);
            label1.setText("D");
            label1.setVisible(true);
            textField2.setVisible(true);
            label2.setText("M.M");
            label2.setVisible(true);
            
            textField3.setVisible(false);
            textField3.setText("");
            label3.setVisible(false);
        }
        else if (units == POSITIONUNITS_DEGMINSEC)
        {
            textField1.setVisible(true);
            label1.setText("D");
            textField2.setVisible(true);
            label2.setText("M");
            label2.setVisible(true);
            textField3.setVisible(true);
            label3.setText("S.S");
            label3.setVisible(true);
        }
    }
    
    private Altitude parseAltitudeAgl (JTextField altTextField, JComboBox altUnitsInputBox, JComboBox altReferenceInputBox, Altitude defaultAltitude) throws Exception
    {
        Altitude altitudeAgl = null;
        String altString = altTextField.getText();
        String altUnitsString = altUnitsInputBox.getSelectedItem().toString();
        String altRefString = altReferenceInputBox.getSelectedItem().toString();
        
        if (altString == null || altString.isEmpty())
        {
            altitudeAgl = defaultAltitude;
        }
        else
        {
            double valUnkUnits = Double.parseDouble(altString);
            if (altUnitsString.equals ("ft"))
                altitudeAgl = new Altitude (valUnkUnits, Length.FEET);
            else if (altUnitsString.equals ("m"))
                altitudeAgl = new Altitude (valUnkUnits, Length.METERS);
        }

        if (altitudeAgl == null)
            throw new Exception ("alt is null");
        if (!altRefString.equals ("AGL"))
            throw new Exception ("alt is not AGL");
        return altitudeAgl;
    }
    
    private Altitude parseAltitudeMsl (JTextField altTextField, JComboBox altUnitsInputBox, JComboBox altReferenceInputBox, Altitude defaultAltitude) throws Exception
    {
        Altitude altitudeMsl = null;
        String altString = altTextField.getText();
        String altUnitsString = altUnitsInputBox.getSelectedItem().toString();
        String altRefString = altReferenceInputBox.getSelectedItem().toString();
        
        if (altString == null || altString.isEmpty())
        {
            altitudeMsl = defaultAltitude;
        }
        else
        {
            double valUnkUnits = Double.parseDouble(altString);
            if (altUnitsString.equals ("ft"))
                altitudeMsl = new Altitude (valUnkUnits, Length.FEET);
            else if (altUnitsString.equals ("m"))
                altitudeMsl = new Altitude (valUnkUnits, Length.METERS);
        }

        if (altitudeMsl == null)
            throw new Exception ("alt is null");
        if (!altRefString.equals ("MSL"))
            throw new Exception ("alt is not MSL");
        return altitudeMsl;
    }
    
    private Length parseLength (JTextField lengthTextField, JComboBox lengthUnitsInputBox, Length defaultLength) throws Exception 
    {
        Length length = null;
        String lengthString = lengthTextField.getText();
        String lengthUnitsString = lengthUnitsInputBox.getSelectedItem().toString();
        
        if (lengthString == null || lengthString.isEmpty())
        {
            length = defaultLength;
        }
        else
        {
            double valUnkUnits = Double.parseDouble(lengthString);
            if (lengthUnitsString.equals ("ft"))
                length = new Length (valUnkUnits, Length.FEET);
            else if (lengthUnitsString.equals ("m"))
                length = new Length (valUnkUnits, Length.METERS);
        }

        if (length == null)
            throw new Exception ("length is null");
        return length;
    }
    
    private double parseInputPosition (JTextField degInputField, JTextField minInputField, JTextField secInputField, boolean positiveSign, int positionUnits)
    {
        double decDeg = Double.parseDouble(degInputField.getText());;
        
        if (positionUnits == POSITIONUNITS_DEGMIN || positionUnits == POSITIONUNITS_DEGMINSEC)
            decDeg += Double.parseDouble(minInputField.getText())/60.0;
        if (positionUnits == POSITIONUNITS_DEGMINSEC)
            decDeg += Double.parseDouble(secInputField.getText())/3600.0;

        if (positiveSign)
            return decDeg;
        else
            return -decDeg;
    }
        
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_WindOptButtonGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox();
        jComboBox2 = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jComboBox3 = new javax.swing.JComboBox();
        jComboBox4 = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jComboBox6 = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jComboBox5 = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jComboBox7 = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jComboBox8 = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel11 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jComboBox9 = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        jComboBox10 = new javax.swing.JComboBox();
        jTextField8 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jComboBox11 = new javax.swing.JComboBox();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jTextField12 = new javax.swing.JTextField();
        jComboBox12 = new javax.swing.JComboBox();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jTextField13 = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jComboBox13 = new javax.swing.JComboBox();
        jComboBox14 = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel23 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jTextField15 = new javax.swing.JTextField();
        jComboBox17 = new javax.swing.JComboBox();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jComboBox18 = new javax.swing.JComboBox();
        jTextField16 = new javax.swing.JTextField();
        jTextField17 = new javax.swing.JTextField();
        jTextField18 = new javax.swing.JTextField();
        jComboBox19 = new javax.swing.JComboBox();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jTextField19 = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        jTextField20 = new javax.swing.JTextField();
        jComboBox20 = new javax.swing.JComboBox();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton3 = new javax.swing.JToggleButton();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jTextField14 = new javax.swing.JTextField();
        jTextField21 = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        jComboBox15 = new javax.swing.JComboBox();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Loiter Standby Altitude:");

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ft", "m" }));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AGL" }));

        jLabel3.setText("Unknown");

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ft", "m" }));

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AGL" }));

        jLabel4.setText("Unknown");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText("Loiter Final Altitude:");

        jLabel5.setText("Unknown");

        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jComboBox6.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ft", "m" }));

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel6.setText("Loiter Orbit Radius:");

        jComboBox5.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AGL" }));

        jLabel7.setText("Unknown");

        jTextField5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField5ActionPerformed(evt);
            }
        });

        jComboBox7.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ft", "m" }));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel8.setText("Intercept Min Altitude:");

        jTextField6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField6ActionPerformed(evt);
            }
        });

        jLabel9.setText("Unknown");

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel10.setText("Intercept Orbit Radius:");

        jComboBox8.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ft", "m" }));

        jLabel11.setText("Unknown");

        jTextField7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField7ActionPerformed(evt);
            }
        });

        jComboBox9.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DD", "DM", "DMS", "MGRS" }));
        jComboBox9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                strikeLatitudeUnitsChanged(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel12.setText("Strike Latitude:");

        jComboBox10.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "N", "S" }));

        jTextField8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField8ActionPerformed(evt);
            }
        });

        jLabel13.setText("D");

        jLabel14.setText("M");

        jTextField9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField9ActionPerformed(evt);
            }
        });

        jLabel15.setText("S.S");

        jLabel16.setText("Unknown");

        jTextField10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField10ActionPerformed(evt);
            }
        });

        jComboBox11.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DD", "DM", "DMS", "MGRS" }));
        jComboBox11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                strikeLongitudeUnitsChanged(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setText("Strike Longitude:");

        jLabel18.setText("S.S");

        jTextField11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField11ActionPerformed(evt);
            }
        });

        jTextField12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField12ActionPerformed(evt);
            }
        });

        jComboBox12.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "E", "W" }));

        jLabel19.setText("M");

        jLabel20.setText("D");

        jLabel21.setText("Unknown");

        jTextField13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField13ActionPerformed(evt);
            }
        });

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel22.setText("Strike Altitude:");

        jComboBox13.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ft", "m" }));

        jComboBox14.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "MSL" }));

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton1.setText("SET");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton2.setText("CLEAR");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton3.setText("SET");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton4.setText("CLEAR");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel23.setText("Unknown");

        jButton5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton5.setText("SET");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton6.setText("CLEAR");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel25.setText("Loiter Standby Longitude:");

        jLabel26.setText("S.S");

        jTextField15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField15ActionPerformed(evt);
            }
        });

        jComboBox17.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DD", "DM", "DMS", "MGRS" }));
        jComboBox17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loiterLongitudeUnitsChanged(evt);
            }
        });

        jLabel28.setText("D");

        jLabel29.setText("M");

        jComboBox18.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "E", "W" }));

        jTextField16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField16ActionPerformed(evt);
            }
        });

        jTextField17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField17ActionPerformed(evt);
            }
        });

        jTextField18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField18ActionPerformed(evt);
            }
        });

        jComboBox19.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DD", "DM", "DMS", "MGRS" }));
        jComboBox19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loiterLatitudeUnitsChanged(evt);
            }
        });

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel30.setText("Loiter Standby Latitude:");
        jLabel30.setMaximumSize(new java.awt.Dimension(145, 14));
        jLabel30.setMinimumSize(new java.awt.Dimension(145, 14));
        jLabel30.setPreferredSize(new java.awt.Dimension(145, 14));

        jLabel31.setText("S.S");

        jLabel32.setText("Unknown");

        jTextField20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField20ActionPerformed(evt);
            }
        });

        jComboBox20.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "N", "S" }));

        jLabel33.setText("M");

        jLabel34.setText("D");

        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel35.setText("Wind:");
        jLabel35.setMaximumSize(new java.awt.Dimension(145, 14));
        jLabel35.setMinimumSize(new java.awt.Dimension(145, 14));
        jLabel35.setPreferredSize(new java.awt.Dimension(145, 14));

        jLabel24.setText("Unknown");
        jLabel24.setOpaque(true);

        jLabel36.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel36.setText("From:");
        jLabel36.setMaximumSize(new java.awt.Dimension(145, 14));
        jLabel36.setMinimumSize(new java.awt.Dimension(145, 14));
        jLabel36.setPreferredSize(new java.awt.Dimension(145, 14));

        jLabel27.setText("Unknown");
        jLabel27.setOpaque(true);

        m_WindOptButtonGroup.add(jToggleButton1);
        jToggleButton1.setSelected(true);
        jToggleButton1.setText("Use WACS Wind Estimate");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        m_WindOptButtonGroup.add(jToggleButton2);
        jToggleButton2.setText("Use UAS Wind Estimate");
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });

        m_WindOptButtonGroup.add(jToggleButton3);
        jToggleButton3.setText("Override Wind Estimate");
        jToggleButton3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton3ItemStateChanged(evt);
            }
        });
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton3ActionPerformed(evt);
            }
        });
        jToggleButton3.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jToggleButton3PropertyChange(evt);
            }
        });

        jLabel37.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel37.setText("Wind Speed:");
        jLabel37.setEnabled(false);

        jLabel38.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel38.setText("Wind From:");
        jLabel38.setEnabled(false);

        jTextField14.setEnabled(false);

        jTextField21.setEnabled(false);

        jLabel39.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel39.setText("deg");
        jLabel39.setEnabled(false);

        jComboBox15.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "m/s", "mph", "kts" }));
        jComboBox15.setEnabled(false);

        jButton8.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton8.setText("SET");
        jButton8.setEnabled(false);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton9.setText("CLEAR");
        jButton9.setEnabled(false);
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 9, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 161, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 9, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 120, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel25)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel32, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jTextField18)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel34)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextField20)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel33)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextField19)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jComboBox20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jComboBox19, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jTextField15)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel28)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextField16)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel29)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextField17)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jComboBox18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jComboBox17, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(13, 13, 13))
                    .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToggleButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToggleButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTextField2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTextField3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTextField5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jTextField4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jTextField6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jComboBox8, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(61, 61, 61))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jTextField13)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jComboBox13, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jComboBox14, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jTextField7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel13)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextField8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel14)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextField9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel15)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jComboBox10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jComboBox9, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jTextField10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel20)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextField12)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel19)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextField11)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel18)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jComboBox12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jComboBox11, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jTextField14)
                                            .addComponent(jTextField21))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jComboBox15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel39, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))))))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13)
                            .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14)
                            .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15)
                            .addComponent(jComboBox10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel20)
                            .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel19)
                            .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18)
                            .addComponent(jComboBox12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel22)
                            .addComponent(jLabel21))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton3)
                            .addComponent(jButton4)))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel23))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel34)
                            .addComponent(jTextField20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel33)
                            .addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel31))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel25)
                            .addComponent(jLabel32))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel28)
                            .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel29)
                            .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel26))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton5)
                            .addComponent(jButton6)))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24)
                    .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel37)
                    .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel38)
                    .addComponent(jTextField21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel39))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton8)
                    .addComponent(jButton9))
                .addContainerGap(30, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField5ActionPerformed

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void jTextField7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField7ActionPerformed

    private void jTextField8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField8ActionPerformed

    private void jTextField9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField9ActionPerformed

    private void jTextField10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField10ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField10ActionPerformed

    private void jTextField11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField11ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField11ActionPerformed

    private void jTextField12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField12ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField12ActionPerformed

    private void jTextField13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField13ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField13ActionPerformed

    private void jTextField15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField15ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField15ActionPerformed

    private void jTextField16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField16ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField16ActionPerformed

    private void jTextField17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField17ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField17ActionPerformed

    private void jTextField18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField18ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField18ActionPerformed

    private void jTextField20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField20ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField20ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        // TODO add your handling code here:
        updateWindOption();
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    private void jToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton3ActionPerformed
        // TODO add your handling code here:
        updateWindOption();
        boolean enabled = jToggleButton3.isSelected();
        
        jLabel37.setEnabled (enabled);
        jLabel38.setEnabled (enabled);
        jLabel39.setEnabled (enabled);
        jTextField14.setEnabled (enabled);
        jTextField21.setEnabled (enabled);
        jComboBox15.setEnabled (enabled);
        jButton8.setEnabled (enabled);
        jButton9.setEnabled (enabled);
    }//GEN-LAST:event_jToggleButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        clearWwbInputs ();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        WACSWaypointActualBelief actualWwbBelief = (WACSWaypointActualBelief)m_BeliefManager.get(WACSWaypointActualBelief.BELIEF_NAME);
        WACSWaypointCommandedBelief lastWwbCommandBelief = (WACSWaypointCommandedBelief)m_BeliefManager.get(WACSWaypointCommandedBelief.BELIEF_NAME);
        
        WACSWaypointBaseBelief defaultWwbBelief = null;
        if (actualWwbBelief == null && lastWwbCommandBelief != null)
            defaultWwbBelief = lastWwbCommandBelief;
        else if (lastWwbCommandBelief == null && actualWwbBelief != null)
            defaultWwbBelief = actualWwbBelief;
        else if (lastWwbCommandBelief != null && actualWwbBelief != null)
        {
            if (lastWwbCommandBelief.isNewerThan(actualWwbBelief))
                defaultWwbBelief = lastWwbCommandBelief;
            else
                defaultWwbBelief = actualWwbBelief;
        }
        
        //Parse inputs, use defaultWwbBelief if no input given.
        
        Altitude loiterStandoffAlt;
        try
        {
            loiterStandoffAlt = parseAltitudeAgl (jTextField2, jComboBox1, jComboBox2, (defaultWwbBelief==null?null:defaultWwbBelief.getStandoffLoiterAltitude()));
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Error in standoff loiter altitude", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Altitude loiterFinalAlt;
        try
        {
            loiterFinalAlt = parseAltitudeAgl (jTextField3, jComboBox3, jComboBox4, (defaultWwbBelief==null?null:defaultWwbBelief.getFinalLoiterAltitude()));
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Error in final loiter altitude", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Length loiterRadius;
        try
        {
            loiterRadius = parseLength (jTextField4, jComboBox6, (defaultWwbBelief==null?null:defaultWwbBelief.getLoiterRadius()));
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Error in loiter radius", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Altitude interceptAlt;
        try
        {
            interceptAlt = parseAltitudeAgl (jTextField5, jComboBox7, jComboBox5, (defaultWwbBelief==null?null:defaultWwbBelief.getIntersectAltitude()));
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Error in intercept altitude", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Length interceptRadius;
        try
        {
            interceptRadius = parseLength (jTextField6, jComboBox8, (defaultWwbBelief==null?null:defaultWwbBelief.getIntersectRadius()));
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Error in intercept radius", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        WACSWaypointCommandedBelief newBelief = new WACSWaypointCommandedBelief (WACSDisplayAgent.AGENTNAME, interceptAlt, interceptRadius, loiterFinalAlt, loiterStandoffAlt, loiterRadius);
        m_BeliefManager.put(newBelief);
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void strikeLatitudeUnitsChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strikeLatitudeUnitsChanged
        // Units (decimal degrees, DMS, etc) changed for input of strike latitude
        String selectedUnits = jComboBox9.getSelectedItem().toString();
        m_StrikeLatitudeUnits = getPositionUnitsFromString (selectedUnits);
        if (m_StrikeLatitudeUnits == POSITIONUNITS_MGRS)
        {
            if (m_StrikeLongitudeUnits != POSITIONUNITS_MGRS)
            {
                jComboBox11.setSelectedItem(selectedUnits);
                strikeLongitudeUnitsChanged (null);
            }
            jTextField7.setVisible(true);
            
            jLabel12.setText ("Strike Position:");
            jLabel17.setVisible(false);
            jLabel16.setVisible (false);
            jLabel13.setVisible (false);
            jTextField8.setVisible (false);
            jLabel14.setVisible (false);
            jTextField9.setVisible (false);
            jLabel15.setVisible (false);
            jComboBox10.setVisible(false);
            jTextField10.setVisible(false);
            jLabel20.setVisible (false);
            jTextField12.setVisible (false);
            jLabel19.setVisible (false);
            jTextField11.setVisible (false);
            jLabel18.setVisible (false);
            jComboBox12.setVisible(false);
            jComboBox11.setVisible(false);
        }
        else
        {
            if (m_StrikeLongitudeUnits == POSITIONUNITS_MGRS)
            {
                jComboBox11.setSelectedItem(selectedUnits);
                strikeLongitudeUnitsChanged (null);
            }
            
            jLabel12.setText ("Strike Latitude:");
            jLabel17.setVisible(true);
            jLabel16.setVisible (true);
            jComboBox11.setVisible(true);
            jComboBox12.setVisible(true);
            jComboBox10.setVisible(true);
            updateInputBoxes (m_StrikeLatitudeUnits, jTextField7, jLabel13, jTextField8, jLabel14, jTextField9, jLabel15);
        }
        m_ForceUpdate = true;
    }//GEN-LAST:event_strikeLatitudeUnitsChanged

    private void strikeLongitudeUnitsChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strikeLongitudeUnitsChanged
        // Units (decimal degrees, DMS, etc) changed for input of strike longitude
        String selectedUnits = jComboBox11.getSelectedItem().toString();
        m_StrikeLongitudeUnits = getPositionUnitsFromString (selectedUnits);
        if (m_StrikeLongitudeUnits == POSITIONUNITS_MGRS)
        {
            if (m_StrikeLatitudeUnits != POSITIONUNITS_MGRS)
            {
                jComboBox9.setSelectedItem(selectedUnits);
                strikeLatitudeUnitsChanged (null);
            }
            jTextField7.setVisible(true);
            
            jLabel12.setText ("Strike Position:");
            jLabel17.setVisible(false);
            jLabel16.setVisible (false);
            jLabel13.setVisible (false);
            jTextField8.setVisible (false);
            jLabel14.setVisible (false);
            jTextField9.setVisible (false);
            jLabel15.setVisible (false);
            jComboBox10.setVisible(false);
            jTextField10.setVisible(false);
            jLabel20.setVisible (false);
            jTextField12.setVisible (false);
            jLabel19.setVisible (false);
            jTextField11.setVisible (false);
            jLabel18.setVisible (false);
            jComboBox12.setVisible(false);
            jComboBox11.setVisible(false);
        }
        else
        {
            if (m_StrikeLatitudeUnits == POSITIONUNITS_MGRS)
            {
                jComboBox9.setSelectedItem(selectedUnits);
                strikeLatitudeUnitsChanged (null);
            }
            
            jLabel12.setText ("Strike Latitude:");
            jLabel17.setVisible(true);
            jLabel16.setVisible (true);
            jComboBox11.setVisible(true);
            jComboBox12.setVisible(true);
            jComboBox10.setVisible(true);
            updateInputBoxes (m_StrikeLongitudeUnits, jTextField10, jLabel20, jTextField12, jLabel19, jTextField11, jLabel18);
        }
        m_ForceUpdate = true;
    }//GEN-LAST:event_strikeLongitudeUnitsChanged

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // Clear strike position input boxes
        jTextField7.setText("");
        jTextField8.setText("");
        jTextField9.setText("");
        jTextField10.setText("");
        jTextField12.setText("");
        jTextField11.setText("");
        jTextField13.setText("");
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // Parse strike position input settings and send new belief
        Latitude strikeLat = null;
        Longitude strikeLon = null;
        Altitude strikeAltMsl = null;
        String tmp = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
        int clas = Config.getConfig().getPropertyAsInteger("WACSAgent.gimbalTargetClassification", Classification.ASSET);

        TargetActualBelief extgBelief = (TargetActualBelief)m_BeliefManager.get(TargetActualBelief.BELIEF_NAME);
        if (extgBelief != null)
        {
            PositionTimeName ptn = extgBelief.getPositionTimeName(tmp);
            if (ptn != null)
            {
                LatLonAltPosition defPosition = ptn.getPosition().asLatLonAltPosition();
                strikeLat = defPosition.getLatitude();
                strikeLon = defPosition.getLongitude();
                strikeAltMsl = defPosition.getAltitude();
            }
        }
        
        if (m_StrikeLatitudeUnits == POSITIONUNITS_MGRS || m_StrikeLongitudeUnits == POSITIONUNITS_MGRS)
        {
            try
            {
                String mgrsText = jTextField7.getText();
                LatLonAltPosition lla = ConvertMGRSStringToLatLonAlt.convert(mgrsText);
                strikeLat = lla.getLatitude();
                strikeLon = lla.getLongitude();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, "Error - invalid strike position entered!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        else
        {
            try
            {
                double strikeLatDecDeg = parseInputPosition (jTextField7, jTextField8, jTextField9, jComboBox10.getSelectedIndex()==0, m_StrikeLatitudeUnits);
                strikeLat = new Latitude (strikeLatDecDeg, Angle.DEGREES);
            }
            catch (Exception e)
            {
            }

            try
            {
                double strikeLonDecDeg = parseInputPosition (jTextField10, jTextField12, jTextField11, jComboBox12.getSelectedIndex()==0, m_StrikeLongitudeUnits);
                strikeLon = new Longitude (strikeLonDecDeg, Angle.DEGREES);
            }
            catch (Exception e)
            {
            }
        }
        
        try
        {
            strikeAltMsl = parseAltitudeMsl (jTextField13, jComboBox13, jComboBox14, strikeAltMsl);
        }
        catch (Exception e)
        {
        }
        
        
        if (strikeLat == null)
        {
            JOptionPane.showMessageDialog(this, "Error - invalid strike latitude entered!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        else if (strikeLon == null)
        {
            JOptionPane.showMessageDialog(this, "Error - invalid strike longitude entered!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        else if (strikeAltMsl == null)
        {
            JOptionPane.showMessageDialog(this, "Error - invalid strike altitude entered!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
            
        //Publish new belief
        LatLonAltPosition pos = new LatLonAltPosition(strikeLat, 
                                                        strikeLon,
                                                        strikeAltMsl);
        m_BeliefManager.put(new TargetCommandedBelief(m_BeliefManager.getName(),
                pos,
                Length.ZERO,
                tmp));
        m_BeliefManager.put(new ClassificationBelief(m_BeliefManager.getName(),
                tmp, clas));

    }//GEN-LAST:event_jButton3ActionPerformed

    private void loiterLatitudeUnitsChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loiterLatitudeUnitsChanged
        // Units (decimal degrees, DMS, etc) changed for input of loiter latitude
        String selectedUnits = jComboBox19.getSelectedItem().toString();
        m_LoiterLatitudeUnits = getPositionUnitsFromString (selectedUnits);
        if (m_LoiterLatitudeUnits == POSITIONUNITS_MGRS)
        {
            if (m_LoiterLongitudeUnits != POSITIONUNITS_MGRS)
            {
                jComboBox17.setSelectedItem(selectedUnits);
                loiterLongitudeUnitsChanged (null);
            }
            jTextField18.setVisible(true);
            
            jLabel30.setText ("Loiter Standby Position:");
            jLabel25.setVisible(false);
            jLabel32.setVisible (false);
            jLabel34.setVisible (false);
            jTextField20.setVisible (false);
            jLabel33.setVisible (false);
            jTextField19.setVisible (false);
            jLabel31.setVisible (false);
            jComboBox20.setVisible(false);
            jTextField15.setVisible(false);
            jLabel28.setVisible (false);
            jTextField16.setVisible (false);
            jLabel29.setVisible (false);
            jTextField17.setVisible (false);
            jLabel26.setVisible (false);
            jComboBox18.setVisible(false);
            jComboBox17.setVisible(false);
        }
        else
        {
            if (m_LoiterLongitudeUnits == POSITIONUNITS_MGRS)
            {
                jComboBox17.setSelectedItem(selectedUnits);
                loiterLongitudeUnitsChanged (null);
            }
            
            jLabel30.setText ("Loiter Standby Latitude:");
            jLabel25.setVisible(true);
            jLabel32.setVisible (true); 
            jComboBox17.setVisible(true);
            jComboBox20.setVisible(true);
            jComboBox18.setVisible(true);
            updateInputBoxes (m_LoiterLatitudeUnits, jTextField18, jLabel34, jTextField20, jLabel33, jTextField19, jLabel31);
        }
        m_ForceUpdate = true;
    }//GEN-LAST:event_loiterLatitudeUnitsChanged

    private void loiterLongitudeUnitsChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loiterLongitudeUnitsChanged
        // Units (decimal degrees, DMS, etc) changed for input of loiter longitude
        String selectedUnits = jComboBox17.getSelectedItem().toString();
        m_LoiterLongitudeUnits = getPositionUnitsFromString (selectedUnits);
        if (m_LoiterLongitudeUnits == POSITIONUNITS_MGRS)
        {
            if (m_LoiterLatitudeUnits != POSITIONUNITS_MGRS)
            {
                jComboBox19.setSelectedItem(selectedUnits);
                loiterLatitudeUnitsChanged (null);
            }
            jTextField18.setVisible(true);
            
            jLabel30.setText ("Loiter Standby Position:");
            jLabel25.setVisible(false);
            jLabel32.setVisible (false);
            jLabel34.setVisible (false);
            jTextField20.setVisible (false);
            jLabel33.setVisible (false);
            jTextField19.setVisible (false);
            jLabel31.setVisible (false);
            jComboBox20.setVisible(false);
            jTextField15.setVisible(false);
            jLabel28.setVisible (false);
            jTextField16.setVisible (false);
            jLabel29.setVisible (false);
            jTextField17.setVisible (false);
            jLabel26.setVisible (false);
            jComboBox18.setVisible(false);
            jComboBox17.setVisible(false);
        }
        else
        {
            if (m_LoiterLatitudeUnits == POSITIONUNITS_MGRS)
            {
                jComboBox19.setSelectedItem(selectedUnits);
                loiterLatitudeUnitsChanged (null);
            }
            
            jLabel30.setText ("Loiter Standby Latitude:");
            jLabel25.setVisible(true);
            jLabel32.setVisible (true); 
            jComboBox17.setVisible(true);
            jComboBox20.setVisible(true);
            jComboBox18.setVisible(true);
            updateInputBoxes (m_LoiterLongitudeUnits, jTextField15, jLabel28, jTextField16, jLabel29, jTextField17, jLabel26);
        }
        m_ForceUpdate = true;
    }//GEN-LAST:event_loiterLongitudeUnitsChanged

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // Clear loiter position input boxes
        jTextField18.setText("");
        jTextField20.setText("");
        jTextField19.setText("");
        jTextField15.setText("");
        jTextField16.setText("");
        jTextField17.setText("");
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // Parse loiter position input settings and send new belief
        Latitude loiterLat = null;
        Longitude loiterLon = null;
        
        RacetrackDefinitionActualBelief extgBelief = (RacetrackDefinitionActualBelief)m_BeliefManager.get(RacetrackDefinitionActualBelief.BELIEF_NAME);
        if (extgBelief != null)
        {
            LatLonAltPosition defPosition = extgBelief.getStartPosition();
            loiterLat = defPosition.getLatitude();
            loiterLon = defPosition.getLongitude();
        }
        
        if (m_LoiterLatitudeUnits == POSITIONUNITS_MGRS || m_LoiterLongitudeUnits == POSITIONUNITS_MGRS)
        {
            try
            {
                String mgrsText = jTextField18.getText();
                LatLonAltPosition lla = ConvertMGRSStringToLatLonAlt.convert(mgrsText);
                loiterLat = lla.getLatitude();
                loiterLon = lla.getLongitude();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, "Error - invalid loiter position entered!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        else
        {
            try
            {
                double loiterLatDecDeg = parseInputPosition (jTextField18, jTextField20, jTextField19, jComboBox20.getSelectedIndex()==0, m_LoiterLatitudeUnits);
                loiterLat = new Latitude (loiterLatDecDeg, Angle.DEGREES);
            }
            catch (Exception e)
            {
            }

            try
            {
                double loiterLonDecDeg = parseInputPosition (jTextField15, jTextField16, jTextField17, jComboBox18.getSelectedIndex()==0, m_LoiterLongitudeUnits);
                loiterLon = new Longitude (loiterLonDecDeg, Angle.DEGREES);
            }
            catch (Exception e)
            {
            }
        }
        
        if (loiterLat == null)
        {
            JOptionPane.showMessageDialog(this, "Error - invalid loiter latitude entered!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        else if (loiterLon == null)
        {
            JOptionPane.showMessageDialog(this, "Error - invalid loiter longitude entered!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
            
        //Publish new belief
        LatLonAltPosition pos = new LatLonAltPosition(loiterLat, 
                                                        loiterLon,
                                                        Altitude.ZERO);
        m_BeliefManager.put(new RacetrackDefinitionCommandedBelief (pos));
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        //Clear window manual override boxes
        jTextField14.setText ("");
        jTextField21.setText ("");
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        // TODO add your handling code here:
        updateWindOption();
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        //Send new GUI wind override
        // TODO: clean up
        
        try
        {
            String units = jComboBox15.getSelectedItem().toString();
            double value = Double.parseDouble(jTextField14.getText());
            double speedMPerS = 0;
            if (units.equals ("kts"))
                speedMPerS = value*0.5144444444;
            else if(units.equals("mph"))
                speedMPerS = value*0.44704;
            else //if(units.equals("m/s"))
                speedMPerS = value;

            Speed speed = new Speed (speedMPerS,Speed.METERS_PER_SECOND);
            METTimeName mt = new METTimeName(WACSAgent.AGENTNAME, new NavyAngle(Double.parseDouble(jTextField21.getText()), Angle.DEGREES).plus(Angle.HALF_CIRCLE), speed,  new Date());
            METBelief mBel = new METBelief(WACSDisplayAgent.AGENTNAME, mt);
            m_BeliefManager.put(mBel);
             mBel = new METBelief(WACSAgent.AGENTNAME, mt);
            m_BeliefManager.put(mBel);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog (this, "Error - bad values for wind estimate!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jToggleButton3PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jToggleButton3PropertyChange
        
    }//GEN-LAST:event_jToggleButton3PropertyChange

    private void jToggleButton3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton3ItemStateChanged
        // TODO add your handling code here:
        jToggleButton3ActionPerformed (null);
    }//GEN-LAST:event_jToggleButton3ItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox10;
    private javax.swing.JComboBox jComboBox11;
    private javax.swing.JComboBox jComboBox12;
    private javax.swing.JComboBox jComboBox13;
    private javax.swing.JComboBox jComboBox14;
    private javax.swing.JComboBox jComboBox15;
    private javax.swing.JComboBox jComboBox17;
    private javax.swing.JComboBox jComboBox18;
    private javax.swing.JComboBox jComboBox19;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox20;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JComboBox jComboBox5;
    private javax.swing.JComboBox jComboBox6;
    private javax.swing.JComboBox jComboBox7;
    private javax.swing.JComboBox jComboBox8;
    private javax.swing.JComboBox jComboBox9;
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
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField13;
    private javax.swing.JTextField jTextField14;
    private javax.swing.JTextField jTextField15;
    private javax.swing.JTextField jTextField16;
    private javax.swing.JTextField jTextField17;
    private javax.swing.JTextField jTextField18;
    private javax.swing.JTextField jTextField19;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField20;
    private javax.swing.JTextField jTextField21;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.ButtonGroup m_WindOptButtonGroup;
    // End of variables declaration//GEN-END:variables

}
