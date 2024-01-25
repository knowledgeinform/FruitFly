/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * WACSControlPanel.java
 *
 * Created on Apr 23, 2010, 11:00:43 AM
 */

package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.cbrnPods.TestPanels.AnacondaPanel;
import edu.jhuapl.nstd.cbrnPods.TestPanels.AnacondaSpectraPanel;
import edu.jhuapl.nstd.cbrnPods.TestPanels.BladewerxPanel;
import edu.jhuapl.nstd.cbrnPods.TestPanels.BridgeportEthernetPanel;
import edu.jhuapl.nstd.cbrnPods.TestPanels.BridgeportUsbPanel;
import edu.jhuapl.nstd.cbrnPods.TestPanels.C100Panel;
import edu.jhuapl.nstd.cbrnPods.TestPanels.IbacPanel;
import edu.jhuapl.nstd.cbrnPods.TestPanels.PodPanel;
import edu.jhuapl.nstd.cbrnPods.TestPanels.THControlPanel;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterfaceTest;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.ParticleCollectorMode;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.AnacondaModeEnum;
import edu.jhuapl.nstd.swarm.WACSDisplayAgent;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorStateBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaStateBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.CBRNHeartbeatBelief;
import edu.jhuapl.nstd.swarm.belief.GammaStatisticsBelief;
import edu.jhuapl.nstd.swarm.belief.IbacActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.IbacStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.PodCommandBelief;
import edu.jhuapl.nstd.swarm.belief.ThermalCommandBelief;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.SensorDebugWindowHandler;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import javax.swing.JFrame;

/**
 *
 * @author stipeja1
 */
public class WACSControlPanel extends javax.swing.JPanel implements ActionListener
{
    protected SearchCanvas _canvas;
    protected BeliefManager _beliefManager;

    protected cbrnPodsInterface _PodInterface;

    JFrame _PodWindow;
    JFrame _AnacondaWindow;
    JFrame _BladewerxWindow;
    JFrame _BridgeportWindow;
    JFrame _C110Window;
    JFrame _IBACWindow;
    JFrame _ThermalWindow;
    JFrame _AnacondaSpectraWindow;

    //protected AnacondaStateBelief _sbelief;
    protected PodCommandBelief _pbelief;
    protected ThermalCommandBelief _tbelief;
    protected ParticleCollectorStateBelief _cbelief;
    protected ParticleCollectorActualStateBelief _cabelief;
    protected AlphaSensorStateBelief _abelief;
    protected AnacondaStateBelief _anbelief;
    protected IbacStateBelief _ibelief;
    //protected WACSShutdownBelief _wbelief;

    protected CBRNHeartbeatBelief _hb;


    private int _AnacondaCommsDelaySec;
    private int _IBACCommsDelaySec;
    private int _C100CommsDelaySec;
    private int _GammaCommsDelaySec;
    private int _AlphaCommsDelaySec;
    private int _TempCommsDelaySec;
    private int _PodCommsDelaySec;

    private long _LastAnacondaCommsSec;
    private long _LastIBACCommsSec;
    private long _LastC100CommsSec;
    private long _LastAlphaCommsSec;

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Creates new form WACSControlPanel */
    public WACSControlPanel() {
        initComponents();
    }


    public WACSControlPanel(SearchCanvas canvas, BeliefManager belMgr)
    {
        _canvas = canvas;
	_beliefManager = belMgr;

        _PodCommsDelaySec = Config.getConfig().getPropertyAsInteger("WACSControlPanel.PodCommsDelay.Sec", 20);
        _AnacondaCommsDelaySec = Config.getConfig().getPropertyAsInteger("WACSControlPanel.AnacondaCommsDelay.Sec", 20);
        _IBACCommsDelaySec = Config.getConfig().getPropertyAsInteger("WACSControlPanel.IBACCommsDelay.Sec", 20);
        _C100CommsDelaySec = Config.getConfig().getPropertyAsInteger("WACSControlPanel.C100CommsDelay.Sec", 20);
        _GammaCommsDelaySec = Config.getConfig().getPropertyAsInteger("WACSControlPanel.GammaCommsDelay.Sec", 20);
        _AlphaCommsDelaySec = Config.getConfig().getPropertyAsInteger("WACSControlPanel.AlphaCommsDelay.Sec", 30);
        _TempCommsDelaySec = Config.getConfig().getPropertyAsInteger("WACSControlPanel.TempCommsDelay.Sec", 20);

        _LastAnacondaCommsSec = 0;
        _LastIBACCommsSec = 0;
        _LastC100CommsSec = 0;
        _LastAlphaCommsSec = 0;

        _PodInterface = SensorDebugWindowHandler.getPodInterface(belMgr);;

        _PodWindow = new JFrame();
        _PodWindow.add(new PodPanel(_PodInterface, belMgr));
        _PodWindow.pack();

        _ThermalWindow = new JFrame();
        _ThermalWindow.add(new THControlPanel(_PodInterface));
        _ThermalWindow.pack();

        _AnacondaWindow = new JFrame();
        _AnacondaWindow.add(new AnacondaPanel(_PodInterface));
        _AnacondaWindow.pack();

        _AnacondaSpectraWindow = new JFrame();
        _AnacondaSpectraWindow.add(new AnacondaSpectraPanel(_PodInterface));
        _AnacondaSpectraWindow.pack();

        _BladewerxWindow = new JFrame();
        _BladewerxWindow.add(new BladewerxPanel(_PodInterface, belMgr));
        _BladewerxWindow.pack();
        
        _BridgeportWindow = new JFrame();
        
        if (Config.getConfig().getPropertyAsBoolean("cbrnPodsInterfaceTest.useBridgeportUsbInterface", false))
        {
        	_BridgeportWindow.add(new BridgeportUsbPanel(_PodInterface, belMgr));
        }
        else
        {
        	_BridgeportWindow.add(new BridgeportEthernetPanel(_PodInterface, belMgr));
        }
        
        _BridgeportWindow.pack();

        _C110Window = new JFrame();
        _C110Window.add(new C100Panel(_PodInterface));
        _C110Window.pack();

        _IBACWindow = new JFrame();
        _IBACWindow.add(new IbacPanel(_PodInterface));
        _IBACWindow.pack();

        initComponents();
    }

    public void updateLabels()
    {
        try
        {
                long dt = 0;

                long currentTimeSec = System.currentTimeMillis()/1000;

                //Alpha Actual State
                _abelief = (AlphaSensorActualStateBelief)_beliefManager.get(AlphaSensorActualStateBelief.BELIEF_NAME);
                if(_abelief!=null)
                {
                    if(m_AlphaStatus.isValid())
                    m_AlphaStatus.setText(_abelief.getStateText());
                    //_LastAlphaComms = Math.max(_LastAlphaComms, _abelief.getTimeStamp().getTime());
                }
                else
                {
                    if(m_AlphaStatus.isValid())
                        m_AlphaStatus.setText("No Data");
                }


                Color defcolor = Color.BLACK;
                Color green = new Color(0,200,0);
                Color used = new Color(255,0,0);
                Color c1 = defcolor;
                Color c2 = defcolor;
                Color c3 = defcolor;
                Color c4 = defcolor;


                //C110 Actual Commanded State
                _cabelief = (ParticleCollectorActualStateBelief)_beliefManager.get(ParticleCollectorActualStateBelief.BELIEF_NAME);
                if(_cabelief!=null)
                {
                    if(m_C110Status.isValid())
                    m_C110Status.setText(_cabelief.getStateText());

                    //_LastC100Comms = Math.max(_LastC100Comms, _cabelief.getTimeStamp().getTime());

    //                if(_cabelief.isSample1full())
    //                   m_C110CollectSample1.setEnabled(false);
    //                else
    //                   m_C110CollectSample1.setEnabled(true);
    //
    //                 if(_cabelief.isSample2full())
    //                   m_C110CollectSample2.setEnabled(false);
    //                else
    //                   m_C110CollectSample2.setEnabled(true);
    //
    //                 if(_cabelief.isSample3full())
    //                   m_C110CollectSample3.setEnabled(false);
    //                else
    //                   m_C110CollectSample3.setEnabled(true);
    //
    //                 if(_cabelief.isSample4full())
    //                   m_C110CollectSample4.setEnabled(false);
    //                else
    //                   m_C110CollectSample4.setEnabled(true);

                    c1 = _cabelief.isSample1full()?used:defcolor;
                    c2 = _cabelief.isSample2full()?used:defcolor;
                    c3 = _cabelief.isSample3full()?used:defcolor;
                    c4 = _cabelief.isSample4full()?used:defcolor;


                     m_C110CollectSample1.setForeground(c1);
                     m_C110CollectSample2.setForeground(c2);
                     m_C110CollectSample3.setForeground(c3);
                     m_C110CollectSample4.setForeground(c4);

                }
                else
                {
                    if(m_C110Status.isValid())
                        m_C110Status.setText("No Data");
                }

                //C110 Commanded State - Turn button green or disable if sample full
                _cbelief = (ParticleCollectorStateBelief)_beliefManager.get(ParticleCollectorStateBelief.BELIEF_NAME);
                if(_cbelief!=null)
                {

                    switch (_cbelief.getParticleCollectorState())
                    {
                        case  Cleaning:
                            m_C110CollectSample1.setForeground(c1);
                            m_C110CollectSample2.setForeground(c2);
                            m_C110CollectSample3.setForeground(c3);
                            m_C110CollectSample4.setForeground(c4);
                            m_C110OnOff.setForeground(defcolor);
                            m_C110Clean.setForeground(green);
                            m_C110Prime.setForeground(defcolor);
                            break;

                        case Priming:
                            m_C110CollectSample1.setForeground(c1);
                            m_C110CollectSample2.setForeground(c2);
                            m_C110CollectSample3.setForeground(c3);
                            m_C110CollectSample4.setForeground(c4);
                            m_C110OnOff.setForeground(defcolor);
                            m_C110Clean.setForeground(defcolor);
                            m_C110Prime.setForeground(green);
                            break;

                          case Collecting:
                            m_C110CollectSample1.setForeground(c1);
                            m_C110CollectSample2.setForeground(c2);
                            m_C110CollectSample3.setForeground(c3);
                            m_C110CollectSample4.setForeground(c4);
                            m_C110OnOff.setForeground(green);
                            m_C110Clean.setForeground(defcolor);
                            m_C110Prime.setForeground(defcolor);
                            break;

                        case StoringSample1:
                            if(!_cabelief.isSample1full())
                                m_C110CollectSample1.setForeground(green);
                            m_C110CollectSample2.setForeground(c2);
                            m_C110CollectSample3.setForeground(c3);
                            m_C110CollectSample4.setForeground(c4);
                            m_C110OnOff.setForeground(defcolor);
                            m_C110Clean.setForeground(defcolor);
                            m_C110Prime.setForeground(defcolor);
                            break;

                        case StoringSample2:
                            m_C110CollectSample1.setForeground(c1);
                            if(!_cabelief.isSample2full())
                                m_C110CollectSample2.setForeground(green);
                            m_C110CollectSample3.setForeground(c3);
                            m_C110CollectSample4.setForeground(c4);
                            m_C110OnOff.setForeground(defcolor);
                            m_C110Clean.setForeground(defcolor);
                            m_C110Prime.setForeground(defcolor);
                            break;

                        case StoringSample3:
                            m_C110CollectSample1.setForeground(c1);
                            m_C110CollectSample2.setForeground(c2);
                            if(!_cabelief.isSample3full())
                             m_C110CollectSample3.setForeground(green);
                            m_C110CollectSample4.setForeground(c4);
                            m_C110OnOff.setForeground(defcolor);
                            m_C110Clean.setForeground(defcolor);
                            m_C110Prime.setForeground(defcolor);
                            break;

                        case StoringSample4:
                            m_C110CollectSample1.setForeground(c1);
                            m_C110CollectSample2.setForeground(c2);
                            m_C110CollectSample3.setForeground(c3);
                            if(!_cabelief.isSample4full())
                                m_C110CollectSample4.setForeground(green);
                            m_C110OnOff.setForeground(defcolor);
                            m_C110Clean.setForeground(defcolor);
                            m_C110Prime.setForeground(defcolor);
                            break;

                        case Idle:
                            m_C110CollectSample1.setForeground(c1);
                            m_C110CollectSample2.setForeground(c2);
                            m_C110CollectSample3.setForeground(c3);
                            m_C110CollectSample4.setForeground(c4);
                            m_C110OnOff.setForeground(defcolor);
                            m_C110Clean.setForeground(defcolor);
                            m_C110Prime.setForeground(defcolor);
                    }

                }

                //IBAC  Commanded State
                 _ibelief = (IbacStateBelief)_beliefManager.get(IbacStateBelief.BELIEF_NAME);
                if(_ibelief !=null)
                {
                    if (m_IbacCommandedMode.isValid())
                    m_IbacCommandedMode.setText("Command: " + _ibelief.getStateText());
                }
                else
                {
                     if (m_IbacCommandedMode.isValid())
                    m_IbacCommandedMode.setText("Command: N/A");
                }

                //IBAC Actual State
                _ibelief = (IbacActualStateBelief)_beliefManager.get(IbacActualStateBelief.BELIEF_NAME);
                if(_ibelief !=null)
                {
                    if (m_IbacActualMode.isValid())
                    m_IbacActualMode.setText("Actual: " + _ibelief.getStateText());
                    //_LastIBACComms = Math.max(_LastIBACComms, _ibelief.getTimeStamp().getTime());

                }
                else
                {
                     if (m_IbacActualMode.isValid())
                    m_IbacActualMode.setText("Actual: N/A");
                }

                //Anaconda Commanded State
                _anbelief = (AnacondaStateBelief)_beliefManager.get(AnacondaStateBelief.BELIEF_NAME);
                if(_anbelief !=null)
                {
                    if (m_AnacondaCommandedMode.isValid())
                    m_AnacondaCommandedMode.setText("Command: " + _anbelief.getStateText());
                }
                else
                {
                     if (m_AnacondaCommandedMode.isValid())
                    m_AnacondaCommandedMode.setText("Command: N/A");
                }

                //Anaconda Actual State
                _anbelief = (AnacondaActualStateBelief)_beliefManager.get(AnacondaActualStateBelief.BELIEF_NAME);
                if(_anbelief !=null)
                {
                    if (m_AnacondaActualMode.isValid())
                    m_AnacondaActualMode.setText("Actual: " + _anbelief.getStateText());
                    //_LastAnacondaComms = Math.max(_LastAnacondaComms, _anbelief.getTimeStamp().getTime());
                }
                else
                {
                     if (m_AnacondaActualMode.isValid())
                    m_AnacondaActualMode.setText("Actual: N/A");
                }

                //Anaconda Detections Message
                AnacondaDetectionBelief det = (AnacondaDetectionBelief)_beliefManager.get(AnacondaDetectionBelief.BELIEF_NAME);

                if(det != null && m_AnacondaDetections.isValid())
                {
                   m_AnacondaDetections.setText(det.getAnacondaDetectionString());
                   //_LastAnacondaComms = Math.max(_LastAnacondaComms, det.getTimeStamp().getTime());
                }

                //Particle Detections Message
                ParticleDetectionBelief pdet = (ParticleDetectionBelief)_beliefManager.get(ParticleDetectionBelief.BELIEF_NAME);

                if(pdet != null && m_IbacDetections.isValid())
                {
                   m_IbacDetections.setText(pdet.getParticleDetectionString());
                   //_LastIBACComms = Math.max(_LastIBACComms, pdet.getTimeStamp().getTime());
                }

                GammaStatisticsBelief gbel = (GammaStatisticsBelief)_beliefManager.get(GammaStatisticsBelief.BELIEF_NAME);
                if(gbel !=null && m_GammaHB.isValid())
                {
                    dt = currentTimeSec - gbel.getTimeStamp().getTime()/1000L;
                    if(dt<=999 && dt>=-999)
                        m_GammaHB.setText(dt + "s");
                    else
                        m_GammaHB.setText("No Comms");
                    if (dt > _GammaCommsDelaySec)
                        m_GammaHB.setBackground(Color.RED);
                    else
                        m_GammaHB.setBackground(Color.GREEN);

                }


                _hb = (CBRNHeartbeatBelief)_beliefManager.get(CBRNHeartbeatBelief.BELIEF_NAME);


                //Pod Heartbeat Message
                if(_hb!=null)
                {
                    //Pod 0 Heartbeat
                    if(m_Pod0HB.isValid())
                    {
                        dt = currentTimeSec - _hb.getTimestampMs(cbrnPodsInterface.COLLECTOR_POD)/1000;
                        if(dt<=999 && dt>=-999)
                            m_Pod0HB.setText(dt + "s");
                        else
                            m_Pod0HB.setText("No Comms");
                        if (dt > _PodCommsDelaySec)
                            m_Pod0HB.setBackground(Color.RED);
                        else
                            m_Pod0HB.setBackground(Color.GREEN);

                    }
                 //Pod 1 Heartbeat
                    if(m_Pod1HB.isValid())
                    {
                        dt = currentTimeSec - _hb.getTimestampMs(cbrnPodsInterface.TRACKER_POD)/1000;
                        if(dt<=999 && dt>=-999)
                            m_Pod1HB.setText(dt + "s");
                        else
                            m_Pod1HB.setText("No Comms");
                        if (dt > _PodCommsDelaySec)
                            m_Pod1HB.setBackground(Color.RED);
                        else
                            m_Pod1HB.setBackground(Color.GREEN);

                    }


                    DecimalFormat th = new DecimalFormat("#0.00 ");

                    //Temp and Humidity Readings
                    if(m_TempHumidityCollectorPod.isValid())
                        m_TempHumidityCollectorPod.setText(th.format(_hb.getTemperature(cbrnPodsInterface.COLLECTOR_POD)*1.8+32) + (char)176   + "F / " + th.format(_hb.getHumidity(cbrnPodsInterface.COLLECTOR_POD))+"%RH");
                    if(m_TempHumidityTrackerPod.isValid())
                        m_TempHumidityTrackerPod.setText(th.format(_hb.getTemperature(cbrnPodsInterface.TRACKER_POD)*1.8+32) + (char)176 +"F / " + th.format(_hb.getHumidity(cbrnPodsInterface.TRACKER_POD))+"%RH");

                    if(_hb.getActualLogStateOn(cbrnPodsInterface.COLLECTOR_POD))
                    {
                        if(m_CollectorLoggingLED.isValid())
                            m_CollectorLoggingLED.setBackground(Color.GREEN);
                    }
                    else
                    {
                        if(m_CollectorLoggingLED.isValid())
                            m_CollectorLoggingLED.setBackground(Color.RED);
                    }

                    if(_hb.getActualLogStateOn(cbrnPodsInterface.TRACKER_POD))
                    {
                        if(m_TrackerLoggingLED.isValid())
                            m_TrackerLoggingLED.setBackground(Color.GREEN);
                    }
                    else
                    {
                        if(m_TrackerLoggingLED.isValid())
                            m_TrackerLoggingLED.setBackground(Color.RED);
                    }

                    //Heartbeat Comms Messages
                    //Collector Temp Data Update Time
                    if(m_CollectorTempHB.isValid())
                    {
                        dt = currentTimeSec - _hb.getTemperatureUpdatedSec(cbrnPodsInterface.COLLECTOR_POD);
                        if(dt<=999 && dt>=-999)
                            m_CollectorTempHB.setText("Time Since Update: " + dt + "s");
                        else
                            m_CollectorTempHB.setText("No Comms");
                        if (dt > _TempCommsDelaySec)
                            m_CollectorTempHB.setBackground(Color.RED);
                        else
                            m_CollectorTempHB.setBackground(Color.GREEN);

                    }
                    //Tracker Temp Data Update Time
                    if(m_TrackerTempHB.isValid())
                    {
                        dt = currentTimeSec - _hb.getTemperatureUpdatedSec(cbrnPodsInterface.TRACKER_POD);
                        if(dt<=999 && dt>=-999)
                            m_TrackerTempHB.setText("Time Since Update: " + dt + "s");
                        else
                            m_TrackerTempHB.setText("No Comms");
                        if (dt > _TempCommsDelaySec)
                            m_TrackerTempHB.setBackground(Color.RED);
                        else
                            m_TrackerTempHB.setBackground(Color.GREEN);

                    }
                    //Anaconda Data Update Time
                    if(m_AnacondaHB.isValid())
                    {
                        _LastAnacondaCommsSec = Math.max(_LastAnacondaCommsSec, _hb.getLastERecvSec(cbrnPodsInterface.COLLECTOR_POD));
                        dt = currentTimeSec - _LastAnacondaCommsSec;
                        if(dt<=999 && dt>=-999)
                            m_AnacondaHB.setText("Time Since Update: " + dt + "s");
                        else
                            m_AnacondaHB.setText("No Comms");

                        if (dt > _AnacondaCommsDelaySec)
                            m_AnacondaHB.setBackground(Color.RED);
                        else
                            m_AnacondaHB.setBackground(Color.GREEN);

                    }
                    //C110 Data Update Time
                    if(m_C110HB.isValid())
                    {
                        _LastC100CommsSec = Math.max(_LastC100CommsSec, _hb.getLastCRecvSec(cbrnPodsInterface.COLLECTOR_POD));
                        dt = currentTimeSec - _LastC100CommsSec;
                        if(dt<=999 && dt>=-999)
                            m_C110HB.setText("Time Since Update: " + dt + "s");
                        else
                           m_C110HB.setText("No Comms");
                        if (dt > _C100CommsDelaySec)
                            m_C110HB.setBackground(Color.RED);
                        else
                            m_C110HB.setBackground(Color.GREEN);

                    }
                    //Alpha Detector Data Update Time
                    if(m_AlphaHB.isValid())
                    {
                        _LastAlphaCommsSec = Math.max(_LastAlphaCommsSec, _hb.getLastCRecvSec(cbrnPodsInterface.TRACKER_POD));
                        dt = currentTimeSec - _LastAlphaCommsSec;

                        if(dt<=999 && dt>=-999)
                            m_AlphaHB.setText("Time Since Update: " + dt + "s");
                        else
                           m_AlphaHB.setText("No Comms");
                        if (dt > _AlphaCommsDelaySec)
                            m_AlphaHB.setBackground(Color.RED);
                        else
                            m_AlphaHB.setBackground(Color.GREEN);

                    }
                    //IBAC Data Update Time
                    if(m_IbacHB.isValid())
                    {
                        _LastIBACCommsSec = Math.max(_LastIBACCommsSec, _hb.getLastERecvSec(cbrnPodsInterface.TRACKER_POD));
                        dt = currentTimeSec - _LastIBACCommsSec;
                        if(dt<=999 && dt>=-999)
                            m_IbacHB.setText("Time Since Update: " + dt + "s");
                        else
                           m_IbacHB.setText("No Comms");
                        if (dt > _IBACCommsDelaySec)
                            m_IbacHB.setBackground(Color.RED);
                        else
                            m_IbacHB.setBackground(Color.GREEN);

                    }

                    //Heater LEDs
                    if(_hb.getHeaterToggledOn(cbrnPodsInterface.COLLECTOR_POD) == 1)
                        m_CollectorHeatLED.setBackground(Color.GREEN);
                    else
                        m_CollectorHeatLED.setBackground(new java.awt.Color(0, 80, 0));

                    if(_hb.getHeaterToggledOn(cbrnPodsInterface.TRACKER_POD) == 1)
                        m_TrackerHeatLED.setBackground(Color.GREEN);
                    else
                        m_TrackerHeatLED.setBackground(new java.awt.Color(0, 80, 0));

                    //Fan LEDs
                    if(_hb.getfanToggledOn(cbrnPodsInterface.COLLECTOR_POD) == 1)
                        m_CollectorFanLED.setBackground(Color.GREEN);
                    else
                        m_CollectorFanLED.setBackground(new java.awt.Color(0, 80, 0));

                    if(_hb.getfanToggledOn(cbrnPodsInterface.TRACKER_POD) == 1)
                        m_TrackerFanLED.setBackground(Color.GREEN);
                    else
                        m_TrackerFanLED.setBackground(new java.awt.Color(0, 80, 0));

                    //Auto LEDs
                    if(_hb.getFanManualOverride(cbrnPodsInterface.COLLECTOR_POD) || _hb.getHeaterManualOverride(cbrnPodsInterface.COLLECTOR_POD) || _hb.getServoManualOverride(cbrnPodsInterface.COLLECTOR_POD))
                        m_CollectorAutoLED.setBackground(new java.awt.Color(0, 80, 0));
                    else
                        m_CollectorAutoLED.setBackground(Color.GREEN);

                    if(_hb.getFanManualOverride(cbrnPodsInterface.TRACKER_POD) || _hb.getHeaterManualOverride(cbrnPodsInterface.TRACKER_POD) || _hb.getServoManualOverride(cbrnPodsInterface.TRACKER_POD))
                        m_TrackerAutoLED.setBackground(new java.awt.Color(0, 80, 0));
                    else
                        m_TrackerAutoLED.setBackground(Color.GREEN);

                }
        }
        catch (Exception e)
        {
            System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
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

        jInternalFrame1 = new javax.swing.JInternalFrame();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        label8 = new java.awt.Label();
        m_AnacondaIdleMode = new javax.swing.JButton();
        m_AnacondaStandbyMode = new javax.swing.JButton();
        m_AnacondaSearchMode = new javax.swing.JButton();
        m_AnacondaAirframeMode = new javax.swing.JButton();
        m_AnacondaPodMode = new javax.swing.JButton();
        m_AnacondaCommandedMode = new javax.swing.JLabel();
        m_AnacondaActualMode = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        m_AnacondaHB = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        m_IbacDetections = new javax.swing.JTextArea();
        m_IbacOn = new javax.swing.JButton();
        m_IbacOff = new javax.swing.JButton();
        m_IbacCommandedMode = new javax.swing.JLabel();
        m_IbacHB = new javax.swing.JLabel();
        m_IbacActualMode = new javax.swing.JLabel();
        m_SyncTime = new javax.swing.JButton();
        m_NewLog = new javax.swing.JButton();
        m_EndLog = new javax.swing.JButton();
        m_C110Prime = new javax.swing.JButton();
        m_C110Clean = new javax.swing.JButton();
        m_C110HB = new javax.swing.JLabel();
        m_GammaHB = new javax.swing.JLabel();
        m_AlphaHB = new javax.swing.JLabel();
        m_AlphaPumpOn = new javax.swing.JButton();
        m_AlphaPumpOff = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        m_C110CollectSample1 = new javax.swing.JButton();
        m_C110CollectSample2 = new javax.swing.JButton();
        m_C110CollectSample3 = new javax.swing.JButton();
        m_C110CollectSample4 = new javax.swing.JButton();
        m_TempHumidityCollectorPod = new javax.swing.JLabel();
        m_ThermalControl = new javax.swing.JButton();
        m_GammaControl = new javax.swing.JButton();
        m_AlphaControl = new javax.swing.JButton();
        m_ParticleCollectorControl = new javax.swing.JButton();
        m_IbacControl = new javax.swing.JButton();
        m_AnacondaControl = new javax.swing.JButton();
        m_PodControl = new javax.swing.JButton();
        m_PodOn = new javax.swing.JButton();
        m_PodOff = new javax.swing.JButton();
        m_C110Status = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        m_AnacondaDetections = new javax.swing.JTextArea();
        m_TempHumidityTrackerPod = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        m_CollectorTempHB = new javax.swing.JLabel();
        m_TrackerTempHB = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        m_CollectorFanLED = new javax.swing.JLabel();
        m_CollectorHeatLED = new javax.swing.JLabel();
        m_CollectorAutoLED = new javax.swing.JLabel();
        m_TrackerFanLED = new javax.swing.JLabel();
        m_TrackerHeatLED = new javax.swing.JLabel();
        m_TrackerAutoLED = new javax.swing.JLabel();
        m_CollectorFan = new javax.swing.JButton();
        m_CollectorHeat = new javax.swing.JButton();
        m_CollectorAuto = new javax.swing.JButton();
        m_TrackerFan = new javax.swing.JButton();
        m_TrackerHeat = new javax.swing.JButton();
        m_TrackerAuto = new javax.swing.JButton();
        m_C110OnOff = new javax.swing.JButton();
        m_AnacondaSampleNum = new javax.swing.JComboBox();
        m_CollectorLoggingLED = new javax.swing.JLabel();
        m_TrackerLoggingLED = new javax.swing.JLabel();
        m_AlphaStatus = new javax.swing.JLabel();
        m_Pod0HB = new javax.swing.JLabel();
        m_Pod1HB = new javax.swing.JLabel();
        m_C110Reset = new javax.swing.JButton();
        m_AnacondaSpectra = new javax.swing.JButton();

        jInternalFrame1.setVisible(true);

        javax.swing.GroupLayout jInternalFrame1Layout = new javax.swing.GroupLayout(jInternalFrame1.getContentPane());
        jInternalFrame1.getContentPane().setLayout(jInternalFrame1Layout);
        jInternalFrame1Layout.setHorizontalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jInternalFrame1Layout.setVerticalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        setAlignmentX(0.1F);
        setAlignmentY(0.1F);
        setAutoscrolls(true);
        setMaximumSize(new java.awt.Dimension(280, 800));
        setMinimumSize(new java.awt.Dimension(280, 800));
        setPreferredSize(new java.awt.Dimension(280, 800));
        setRequestFocusEnabled(false);

        label8.setAlignment(java.awt.Label.CENTER);
        label8.setFont(new java.awt.Font("Dialog", 1, 14));
        label8.setText("THERMAL SYSTEM");
        label8.setVisible(false);

        m_AnacondaIdleMode.setText("IDLE");
        m_AnacondaIdleMode.setAlignmentX(0.1F);
        m_AnacondaIdleMode.setAlignmentY(0.1F);
        m_AnacondaIdleMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AnacondaIdleModeActionPerformed(evt);
            }
        });

        m_AnacondaStandbyMode.setText("STANDBY");
        m_AnacondaStandbyMode.setAlignmentX(0.1F);
        m_AnacondaStandbyMode.setAlignmentY(0.1F);
        m_AnacondaStandbyMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AnacondaStandbyModeActionPerformed(evt);
            }
        });

        m_AnacondaSearchMode.setText("SEARCH");
        m_AnacondaSearchMode.setAlignmentX(0.1F);
        m_AnacondaSearchMode.setAlignmentY(0.1F);
        m_AnacondaSearchMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AnacondaSearchModeActionPerformed(evt);
            }
        });

        m_AnacondaAirframeMode.setText("AIRFRAME");
        m_AnacondaAirframeMode.setAlignmentX(0.1F);
        m_AnacondaAirframeMode.setAlignmentY(0.1F);
        m_AnacondaAirframeMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AnacondaAirframeModeActionPerformed(evt);
            }
        });

        m_AnacondaPodMode.setText("POD");
        m_AnacondaPodMode.setAlignmentX(0.1F);
        m_AnacondaPodMode.setAlignmentY(0.1F);
        m_AnacondaPodMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AnacondaPodModeActionPerformed(evt);
            }
        });

        m_AnacondaCommandedMode.setFont(new java.awt.Font("Arial", 1, 12));
        m_AnacondaCommandedMode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_AnacondaCommandedMode.setText("MODE COMMANDED:");
        m_AnacondaCommandedMode.setAlignmentX(0.1F);
        m_AnacondaCommandedMode.setAlignmentY(0.1F);

        m_AnacondaActualMode.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        m_AnacondaActualMode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_AnacondaActualMode.setText("ACTUAL MODE:");
        m_AnacondaActualMode.setAlignmentX(0.1F);
        m_AnacondaActualMode.setAlignmentY(0.1F);
        m_AnacondaActualMode.setAutoscrolls(true);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        m_AnacondaHB.setBackground(new java.awt.Color(255, 0, 0));
        m_AnacondaHB.setFont(new java.awt.Font("Arial", 0, 10));
        m_AnacondaHB.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_AnacondaHB.setText("No Communications");
        m_AnacondaHB.setAlignmentX(0.1F);
        m_AnacondaHB.setAlignmentY(0.1F);
        m_AnacondaHB.setOpaque(true);

        jScrollPane2.setAlignmentX(0.1F);
        jScrollPane2.setAlignmentY(0.1F);

        m_IbacDetections.setColumns(20);
        m_IbacDetections.setFont(new java.awt.Font("Arial", 1, 10));
        m_IbacDetections.setRows(2);
        jScrollPane2.setViewportView(m_IbacDetections);

        m_IbacOn.setText("ON");
        m_IbacOn.setAlignmentX(0.1F);
        m_IbacOn.setAlignmentY(0.1F);
        m_IbacOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_IbacOnActionPerformed(evt);
            }
        });

        m_IbacOff.setText("OFF");
        m_IbacOff.setAlignmentX(0.1F);
        m_IbacOff.setAlignmentY(0.1F);
        m_IbacOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_IbacOffActionPerformed(evt);
            }
        });

        m_IbacCommandedMode.setFont(new java.awt.Font("Arial", 1, 12));
        m_IbacCommandedMode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_IbacCommandedMode.setText("MODE COMMANDED:");
        m_IbacCommandedMode.setAlignmentX(0.1F);
        m_IbacCommandedMode.setAlignmentY(0.1F);

        m_IbacHB.setBackground(new java.awt.Color(255, 0, 0));
        m_IbacHB.setFont(new java.awt.Font("Arial", 0, 10));
        m_IbacHB.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_IbacHB.setText("No Communications");
        m_IbacHB.setAlignmentX(0.1F);
        m_IbacHB.setAlignmentY(0.1F);
        m_IbacHB.setOpaque(true);

        m_IbacActualMode.setFont(new java.awt.Font("Arial", 1, 12));
        m_IbacActualMode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_IbacActualMode.setText("ACTUAL MODE:");
        m_IbacActualMode.setAlignmentX(0.1F);
        m_IbacActualMode.setAlignmentY(0.1F);

        m_SyncTime.setText("SYNC");
        m_SyncTime.setAlignmentX(0.1F);
        m_SyncTime.setAlignmentY(0.1F);
        m_SyncTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_SyncTimeActionPerformed(evt);
            }
        });

        m_NewLog.setText("NEW LOG");
        m_NewLog.setAlignmentX(0.1F);
        m_NewLog.setAlignmentY(0.1F);
        m_NewLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_NewLogActionPerformed(evt);
            }
        });

        m_EndLog.setText("END LOG");
        m_EndLog.setAlignmentX(0.1F);
        m_EndLog.setAlignmentY(0.1F);
        m_EndLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_EndLogActionPerformed(evt);
            }
        });

        m_C110Prime.setText("PRIME");
        m_C110Prime.setAlignmentX(0.1F);
        m_C110Prime.setAlignmentY(0.1F);
        m_C110Prime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_C110PrimeActionPerformed(evt);
            }
        });

        m_C110Clean.setText("CLEAN");
        m_C110Clean.setAlignmentX(0.1F);
        m_C110Clean.setAlignmentY(0.1F);
        m_C110Clean.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_C110CleanActionPerformed(evt);
            }
        });

        m_C110HB.setBackground(new java.awt.Color(255, 0, 0));
        m_C110HB.setFont(new java.awt.Font("Arial", 0, 10));
        m_C110HB.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_C110HB.setText("No Communications");
        m_C110HB.setAlignmentX(0.1F);
        m_C110HB.setAlignmentY(0.1F);
        m_C110HB.setOpaque(true);

        m_GammaHB.setBackground(new java.awt.Color(255, 0, 0));
        m_GammaHB.setFont(new java.awt.Font("Arial", 0, 8));
        m_GammaHB.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_GammaHB.setText("No Communications");
        m_GammaHB.setOpaque(true);

        m_AlphaHB.setBackground(new java.awt.Color(255, 0, 0));
        m_AlphaHB.setFont(new java.awt.Font("Arial", 0, 8));
        m_AlphaHB.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_AlphaHB.setText("No Communications");
        m_AlphaHB.setAlignmentX(0.1F);
        m_AlphaHB.setAlignmentY(0.1F);
        m_AlphaHB.setOpaque(true);

        m_AlphaPumpOn.setText("ON");
        m_AlphaPumpOn.setAlignmentX(0.1F);
        m_AlphaPumpOn.setAlignmentY(0.1F);
        m_AlphaPumpOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AlphaPumpOnActionPerformed(evt);
            }
        });

        m_AlphaPumpOff.setText("OFF");
        m_AlphaPumpOff.setAlignmentX(0.1F);
        m_AlphaPumpOff.setAlignmentY(0.1F);
        m_AlphaPumpOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AlphaPumpOffActionPerformed(evt);
            }
        });

        jLabel1.setBackground(new java.awt.Color(128, 128, 128));
        jLabel1.setFont(new java.awt.Font("Arial", 1, 11));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Collector Pod");
        jLabel1.setOpaque(true);

        m_C110CollectSample1.setText("1");
        m_C110CollectSample1.setAlignmentX(0.1F);
        m_C110CollectSample1.setAlignmentY(0.1F);
        m_C110CollectSample1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_C110CollectSample1ActionPerformed(evt);
            }
        });

        m_C110CollectSample2.setText("2");
        m_C110CollectSample2.setAlignmentX(0.1F);
        m_C110CollectSample2.setAlignmentY(0.1F);
        m_C110CollectSample2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_C110CollectSample2ActionPerformed(evt);
            }
        });

        m_C110CollectSample3.setText("3");
        m_C110CollectSample3.setAlignmentX(0.1F);
        m_C110CollectSample3.setAlignmentY(0.1F);
        m_C110CollectSample3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_C110CollectSample3ActionPerformed(evt);
            }
        });

        m_C110CollectSample4.setText("4");
        m_C110CollectSample4.setAlignmentX(0.1F);
        m_C110CollectSample4.setAlignmentY(0.1F);
        m_C110CollectSample4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_C110CollectSample4ActionPerformed(evt);
            }
        });

        m_TempHumidityCollectorPod.setFont(new java.awt.Font("Arial", 0, 11));
        m_TempHumidityCollectorPod.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_TempHumidityCollectorPod.setText("Temp / Humidity");

        m_ThermalControl.setFont(new java.awt.Font("Arial", 1, 14));
        m_ThermalControl.setText("THERMAL SYSTEM");
        m_ThermalControl.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_ThermalControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_ThermalControlActionPerformed(evt);
            }
        });

        m_GammaControl.setFont(new java.awt.Font("Arial", 1, 14));
        m_GammaControl.setText("GAMMA SENSOR");
        m_GammaControl.setAlignmentX(0.1F);
        m_GammaControl.setAlignmentY(0.1F);
        m_GammaControl.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_GammaControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_GammaControlActionPerformed(evt);
            }
        });

        m_AlphaControl.setFont(new java.awt.Font("Arial", 1, 14));
        m_AlphaControl.setText("ALPHA SENSOR");
        m_AlphaControl.setAlignmentX(0.1F);
        m_AlphaControl.setAlignmentY(0.1F);
        m_AlphaControl.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_AlphaControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AlphaControlActionPerformed(evt);
            }
        });

        m_ParticleCollectorControl.setFont(new java.awt.Font("Arial", 1, 14));
        m_ParticleCollectorControl.setText("PARTICLE COLLECTOR");
        m_ParticleCollectorControl.setAlignmentX(0.1F);
        m_ParticleCollectorControl.setAlignmentY(0.1F);
        m_ParticleCollectorControl.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_ParticleCollectorControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_ParticleCollectorControlActionPerformed(evt);
            }
        });

        m_IbacControl.setFont(new java.awt.Font("Arial", 1, 14));
        m_IbacControl.setText("PARTICLE SENSOR");
        m_IbacControl.setAlignmentX(0.1F);
        m_IbacControl.setAlignmentY(0.1F);
        m_IbacControl.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_IbacControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_IbacControlActionPerformed(evt);
            }
        });

        m_AnacondaControl.setFont(new java.awt.Font("Arial", 1, 14));
        m_AnacondaControl.setText("ANACONDA");
        m_AnacondaControl.setAlignmentX(0.1F);
        m_AnacondaControl.setAlignmentY(0.1F);
        m_AnacondaControl.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_AnacondaControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AnacondaControlActionPerformed(evt);
            }
        });

        m_PodControl.setFont(new java.awt.Font("Arial", 1, 14));
        m_PodControl.setText("POD CONTROL");
        m_PodControl.setAlignmentX(0.1F);
        m_PodControl.setAlignmentY(0.1F);
        m_PodControl.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_PodControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_PodControlActionPerformed(evt);
            }
        });

        m_PodOn.setText("ALL ON");
        m_PodOn.setAlignmentX(0.1F);
        m_PodOn.setAlignmentY(0.1F);
        m_PodOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_PodOnActionPerformed(evt);
            }
        });

        m_PodOff.setText("ALL OFF");
        m_PodOff.setAlignmentX(0.1F);
        m_PodOff.setAlignmentY(0.1F);
        m_PodOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_PodOffActionPerformed(evt);
            }
        });

        m_C110Status.setFont(new java.awt.Font("Arial", 1, 12));
        m_C110Status.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_C110Status.setText("STATUS");
        m_C110Status.setAlignmentX(0.1F);
        m_C110Status.setAlignmentY(0.1F);

        jScrollPane3.setAlignmentX(0.1F);
        jScrollPane3.setAlignmentY(0.1F);

        m_AnacondaDetections.setColumns(20);
        m_AnacondaDetections.setFont(new java.awt.Font("Arial", 1, 10));
        m_AnacondaDetections.setRows(2);
        jScrollPane3.setViewportView(m_AnacondaDetections);

        m_TempHumidityTrackerPod.setFont(new java.awt.Font("Arial", 0, 11));
        m_TempHumidityTrackerPod.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_TempHumidityTrackerPod.setText("Temp / Humidity");

        jLabel2.setBackground(new java.awt.Color(128, 128, 128));
        jLabel2.setFont(new java.awt.Font("Arial", 1, 11));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Tracker Pod");
        jLabel2.setOpaque(true);

        m_CollectorTempHB.setBackground(new java.awt.Color(255, 0, 0));
        m_CollectorTempHB.setFont(new java.awt.Font("Arial", 0, 8));
        m_CollectorTempHB.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_CollectorTempHB.setText("No Comms");
        m_CollectorTempHB.setOpaque(true);

        m_TrackerTempHB.setBackground(new java.awt.Color(255, 0, 0));
        m_TrackerTempHB.setFont(new java.awt.Font("Arial", 0, 8));
        m_TrackerTempHB.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_TrackerTempHB.setText("No Comms");
        m_TrackerTempHB.setOpaque(true);

        m_CollectorFanLED.setBackground(new java.awt.Color(0, 80, 0));
        m_CollectorFanLED.setForeground(new java.awt.Color(0, 80, 0));
        m_CollectorFanLED.setText("     ");
        m_CollectorFanLED.setOpaque(true);

        m_CollectorHeatLED.setBackground(new java.awt.Color(0, 80, 0));
        m_CollectorHeatLED.setForeground(new java.awt.Color(0, 80, 0));
        m_CollectorHeatLED.setText("     ");
        m_CollectorHeatLED.setOpaque(true);

        m_CollectorAutoLED.setBackground(new java.awt.Color(0, 80, 0));
        m_CollectorAutoLED.setForeground(new java.awt.Color(0, 80, 0));
        m_CollectorAutoLED.setText("     ");
        m_CollectorAutoLED.setOpaque(true);

        m_TrackerFanLED.setBackground(new java.awt.Color(0, 80, 0));
        m_TrackerFanLED.setForeground(new java.awt.Color(0, 80, 0));
        m_TrackerFanLED.setText("     ");
        m_TrackerFanLED.setOpaque(true);

        m_TrackerHeatLED.setBackground(new java.awt.Color(0, 80, 0));
        m_TrackerHeatLED.setForeground(new java.awt.Color(0, 80, 0));
        m_TrackerHeatLED.setText("     ");
        m_TrackerHeatLED.setOpaque(true);

        m_TrackerAutoLED.setBackground(new java.awt.Color(0, 80, 0));
        m_TrackerAutoLED.setForeground(new java.awt.Color(0, 80, 0));
        m_TrackerAutoLED.setText("     ");
        m_TrackerAutoLED.setOpaque(true);

        m_CollectorFan.setFont(new java.awt.Font("Arial", 0, 11));
        m_CollectorFan.setText("FAN");
        m_CollectorFan.setDoubleBuffered(true);
        m_CollectorFan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_CollectorFanActionPerformed(evt);
            }
        });

        m_CollectorHeat.setFont(new java.awt.Font("Arial", 0, 11));
        m_CollectorHeat.setText("HEAT");
        m_CollectorHeat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_CollectorHeatActionPerformed(evt);
            }
        });

        m_CollectorAuto.setFont(new java.awt.Font("Arial", 0, 11));
        m_CollectorAuto.setText("AUTO");
        m_CollectorAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_CollectorAutoActionPerformed(evt);
            }
        });

        m_TrackerFan.setFont(new java.awt.Font("Arial", 0, 11));
        m_TrackerFan.setText("FAN");
        m_TrackerFan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_TrackerFanActionPerformed(evt);
            }
        });

        m_TrackerHeat.setFont(new java.awt.Font("Arial", 0, 11));
        m_TrackerHeat.setText("HEAT");
        m_TrackerHeat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_TrackerHeatActionPerformed(evt);
            }
        });

        m_TrackerAuto.setFont(new java.awt.Font("Arial", 0, 11));
        m_TrackerAuto.setText("AUTO");
        m_TrackerAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_TrackerAutoActionPerformed(evt);
            }
        });

        m_C110OnOff.setText("COLLECT");
        m_C110OnOff.setAlignmentX(0.1F);
        m_C110OnOff.setAlignmentY(0.1F);
        m_C110OnOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_C110OnOffActionPerformed(evt);
            }
        });

        m_AnacondaSampleNum.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4" }));

        m_CollectorLoggingLED.setBackground(new java.awt.Color(255, 0, 0));
        m_CollectorLoggingLED.setForeground(new java.awt.Color(255, 0, 0));
        m_CollectorLoggingLED.setText(" ");
        m_CollectorLoggingLED.setOpaque(true);

        m_TrackerLoggingLED.setBackground(new java.awt.Color(255, 0, 0));
        m_TrackerLoggingLED.setForeground(new java.awt.Color(255, 0, 0));
        m_TrackerLoggingLED.setText(" ");
        m_TrackerLoggingLED.setOpaque(true);

        m_AlphaStatus.setFont(new java.awt.Font("Arial", 0, 8));
        m_AlphaStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_AlphaStatus.setText("STATUS");
        m_AlphaStatus.setAlignmentX(0.1F);
        m_AlphaStatus.setAlignmentY(0.1F);
        m_AlphaStatus.setOpaque(true);

        m_Pod0HB.setBackground(new java.awt.Color(255, 0, 0));
        m_Pod0HB.setFont(new java.awt.Font("Tahoma", 0, 8));
        m_Pod0HB.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_Pod0HB.setText("No Comms");
        m_Pod0HB.setOpaque(true);

        m_Pod1HB.setBackground(new java.awt.Color(255, 0, 0));
        m_Pod1HB.setFont(new java.awt.Font("Tahoma", 0, 8));
        m_Pod1HB.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_Pod1HB.setText("No Comms");
        m_Pod1HB.setOpaque(true);

        m_C110Reset.setText("RESET");
        m_C110Reset.setAlignmentX(0.1F);
        m_C110Reset.setAlignmentY(0.1F);
        m_C110Reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_C110ResetActionPerformed(evt);
            }
        });

        m_AnacondaSpectra.setFont(new java.awt.Font("Arial", 1, 14));
        m_AnacondaSpectra.setText("SPECTRA");
        m_AnacondaSpectra.setAlignmentX(0.1F);
        m_AnacondaSpectra.setAlignmentY(0.1F);
        m_AnacondaSpectra.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        m_AnacondaSpectra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AnacondaSpectraActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(m_AnacondaHB, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(228, 228, 228)
                        .addComponent(label8, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addComponent(m_PodControl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(m_AnacondaPodMode)
                            .addComponent(m_AnacondaIdleMode))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(m_AnacondaStandbyMode, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                            .addComponent(m_AnacondaAirframeMode, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(m_AnacondaSampleNum, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_AnacondaSearchMode, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(m_AnacondaCommandedMode, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_AnacondaActualMode, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 141, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addGap(119, 119, 119))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(m_SyncTime)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_NewLog)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_EndLog)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                        .addComponent(m_CollectorLoggingLED, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_TrackerLoggingLED, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(m_IbacControl, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(m_IbacHB, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(m_C110Status, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(m_ParticleCollectorControl, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(m_C110HB, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(m_C110Clean, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_C110Prime, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_C110OnOff, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1))
                    .addComponent(m_AlphaControl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(m_GammaControl, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(m_ThermalControl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(m_C110CollectSample1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_C110CollectSample2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_C110CollectSample3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(5, 5, 5)
                        .addComponent(m_C110CollectSample4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_C110Reset, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(m_TempHumidityCollectorPod, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(m_CollectorHeat, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                                    .addComponent(m_CollectorFan, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                                    .addComponent(m_CollectorAuto, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(m_CollectorAutoLED, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(m_CollectorHeatLED, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(m_CollectorFanLED, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addGap(14, 14, 14))
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                            .addComponent(m_CollectorTempHB, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(m_TrackerTempHB, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(m_TempHumidityTrackerPod, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(m_TrackerAuto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(m_TrackerHeat, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                                        .addComponent(m_TrackerFan, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))
                                    .addGap(18, 18, 18)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(m_TrackerAutoLED)
                                        .addComponent(m_TrackerFanLED)
                                        .addComponent(m_TrackerHeatLED))))
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(m_AlphaHB, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                            .addComponent(m_AlphaPumpOn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(m_AlphaPumpOff, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                            .addComponent(m_AlphaStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(m_PodOn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_PodOff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_Pod0HB, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_Pod1HB, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(m_IbacCommandedMode, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                            .addComponent(m_IbacOn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(m_IbacActualMode, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                            .addComponent(m_IbacOff, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(m_AnacondaControl, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_AnacondaSpectra, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(m_GammaHB, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 214, Short.MAX_VALUE)
                    .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 66, Short.MAX_VALUE)))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 214, Short.MAX_VALUE)
                    .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 66, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(m_PodControl, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_SyncTime, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_NewLog, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_EndLog, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_TrackerLoggingLED)
                    .addComponent(m_CollectorLoggingLED))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_PodOn, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_PodOff, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_Pod1HB, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_Pod0HB, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_AnacondaControl, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_AnacondaSpectra, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_AnacondaHB)
                .addGap(1, 1, 1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_AnacondaStandbyMode, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_AnacondaSearchMode, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_AnacondaPodMode, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_AnacondaAirframeMode, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_AnacondaIdleMode, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_AnacondaSampleNum, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_AnacondaCommandedMode)
                    .addComponent(m_AnacondaActualMode))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(m_IbacControl, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_IbacHB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_IbacOff, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_IbacOn, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_IbacCommandedMode)
                    .addComponent(m_IbacActualMode, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_ParticleCollectorControl, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_C110HB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_C110Status)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_C110Clean, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_C110OnOff, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_C110Prime, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_C110CollectSample1)
                    .addComponent(m_C110CollectSample2)
                    .addComponent(m_C110Reset)
                    .addComponent(m_C110CollectSample3)
                    .addComponent(m_C110CollectSample4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_AlphaControl, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_AlphaHB)
                    .addComponent(m_AlphaStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(m_AlphaPumpOff, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_AlphaPumpOn, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_GammaControl, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_GammaHB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_ThermalControl, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_CollectorTempHB, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_TrackerTempHB, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_TempHumidityCollectorPod)
                    .addComponent(m_TempHumidityTrackerPod))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(m_CollectorFanLED)
                                .addComponent(m_CollectorFan, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(m_TrackerFan, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(m_CollectorHeatLED)
                            .addComponent(m_CollectorHeat, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_TrackerHeat, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(m_CollectorAutoLED)
                            .addComponent(m_CollectorAuto, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_TrackerAuto, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(m_TrackerFanLED)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_TrackerHeatLED)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_TrackerAutoLED)))
                .addGap(35, 35, 35)
                .addComponent(jLabel3)
                .addGap(148, 148, 148)
                .addComponent(label8, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 457, Short.MAX_VALUE)
                    .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 457, Short.MAX_VALUE)))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 457, Short.MAX_VALUE)
                    .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 457, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void m_AnacondaIdleModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AnacondaIdleModeActionPerformed
       _anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Idle);
        _beliefManager.put(_anbelief);
    }//GEN-LAST:event_m_AnacondaIdleModeActionPerformed

    private void m_AnacondaStandbyModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AnacondaStandbyModeActionPerformed
        _anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Standby);
        _beliefManager.put(_anbelief);
    }//GEN-LAST:event_m_AnacondaStandbyModeActionPerformed

    private void m_AnacondaSearchModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AnacondaSearchModeActionPerformed
        switch(m_AnacondaSampleNum.getSelectedIndex())
        {
            case 0:
                _anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Search1);
                _beliefManager.put(_anbelief);
                break;
            case 1:
                _anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Search2);
                _beliefManager.put(_anbelief);
                break;
            case 2:
                _anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Search3);
                _beliefManager.put(_anbelief);
                break;
            case 3:
                _anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Search4);
                _beliefManager.put(_anbelief);
                break;
            default:
                _anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Search1);
                _beliefManager.put(_anbelief);
        }
    }//GEN-LAST:event_m_AnacondaSearchModeActionPerformed

    private void m_AnacondaAirframeModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AnacondaAirframeModeActionPerformed
        _anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Airframe);
        _beliefManager.put(_anbelief);
    }//GEN-LAST:event_m_AnacondaAirframeModeActionPerformed

    private void m_AnacondaPodModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AnacondaPodModeActionPerformed
        _anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Pod);
        _beliefManager.put(_anbelief);
    }//GEN-LAST:event_m_AnacondaPodModeActionPerformed

    private void m_IbacOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_IbacOnActionPerformed
        _ibelief = new IbacStateBelief(WACSDisplayAgent.AGENTNAME, true);
        _beliefManager.put(_ibelief);
    }//GEN-LAST:event_m_IbacOnActionPerformed

    private void m_IbacOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_IbacOffActionPerformed
        _ibelief = new IbacStateBelief(WACSDisplayAgent.AGENTNAME, false);
        _beliefManager.put(_ibelief);
    }//GEN-LAST:event_m_IbacOffActionPerformed

    private void m_SyncTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_SyncTimeActionPerformed
        _pbelief = new PodCommandBelief(WACSDisplayAgent.AGENTNAME, cbrnPodCommand.POD_SET_RTC);
        _beliefManager.put(_pbelief);
    }//GEN-LAST:event_m_SyncTimeActionPerformed

    private void m_NewLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_NewLogActionPerformed
        _pbelief = new PodCommandBelief(WACSDisplayAgent.AGENTNAME, cbrnPodCommand.POD_LOG_NEW);
        _beliefManager.put(_pbelief);
    }//GEN-LAST:event_m_NewLogActionPerformed

    private void m_EndLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_EndLogActionPerformed
        _pbelief = new PodCommandBelief(WACSDisplayAgent.AGENTNAME, cbrnPodCommand.POD_LOG_END);
        _beliefManager.put(_pbelief);
    }//GEN-LAST:event_m_EndLogActionPerformed

    private void m_C110PrimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_C110PrimeActionPerformed
        _cbelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.Priming);
        _beliefManager.put(_cbelief);
    }//GEN-LAST:event_m_C110PrimeActionPerformed

    private void m_C110CleanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_C110CleanActionPerformed
        _cbelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.Cleaning);
        _beliefManager.put(_cbelief);
    }//GEN-LAST:event_m_C110CleanActionPerformed

    private void m_AlphaPumpOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AlphaPumpOnActionPerformed
        m_AlphaPumpOn.setForeground(new Color(0,127,0));
        m_AlphaPumpOff.setForeground(Color.BLACK);
        _abelief = new AlphaSensorStateBelief(WACSDisplayAgent.AGENTNAME, true);
        _beliefManager.put(_abelief);
    }//GEN-LAST:event_m_AlphaPumpOnActionPerformed

    private void m_AlphaPumpOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AlphaPumpOffActionPerformed
        m_AlphaPumpOn.setForeground(Color.BLACK);
        m_AlphaPumpOff.setForeground(new Color(0,127,0));
        _abelief = new AlphaSensorStateBelief(WACSDisplayAgent.AGENTNAME, false);
        _beliefManager.put(_abelief);
    }//GEN-LAST:event_m_AlphaPumpOffActionPerformed

    private void m_PodOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_PodOnActionPerformed
        
        try
        {
            _ibelief = new IbacStateBelief(WACSDisplayAgent.AGENTNAME, true);
            _beliefManager.put(_ibelief);

            Thread.sleep(500);

            switch(m_AnacondaSampleNum.getSelectedIndex())
            {
                case 0:
                    _anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Search1);
                    _beliefManager.put(_anbelief);
                    break;
                case 1:
                    _anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Search2);
                    _beliefManager.put(_anbelief);
                    break;
                case 2:
                    _anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Search3);
                    _beliefManager.put(_anbelief);
                    break;
                case 3:
                    _anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Search4);
                    _beliefManager.put(_anbelief);
                    break;
                default:
                    _anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Search1);
                    _beliefManager.put(_anbelief);
            }


            Thread.sleep(500);

            _cbelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.Collecting);
            _beliefManager.put(_cbelief);

            Thread.sleep(500);
            
            _abelief = new AlphaSensorStateBelief(WACSDisplayAgent.AGENTNAME, true);
            _beliefManager.put(_abelief);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }//GEN-LAST:event_m_PodOnActionPerformed

    private void m_PodOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_PodOffActionPerformed
        _anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Standby);
        _beliefManager.put(_anbelief);

        _ibelief = new IbacStateBelief(WACSDisplayAgent.AGENTNAME, false);
        _beliefManager.put(_ibelief);

        _cbelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.Idle);
        _beliefManager.put(_cbelief);

        _abelief = new AlphaSensorStateBelief(WACSDisplayAgent.AGENTNAME, false);
        _beliefManager.put(_abelief);
    }//GEN-LAST:event_m_PodOffActionPerformed

    private void m_CollectorFanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_CollectorFanActionPerformed
        _tbelief = (ThermalCommandBelief)_beliefManager.get(ThermalCommandBelief.BELIEF_NAME);
        if(_tbelief != null && _tbelief.getThermalCommand()== ThermalCommandBelief.ThermalCommand.FanOn)
             _tbelief = new ThermalCommandBelief(WACSDisplayAgent.AGENTNAME, ThermalCommandBelief.ThermalCommand.FanOff, cbrnPodsInterface.COLLECTOR_POD);
        else
            _tbelief = new ThermalCommandBelief(WACSDisplayAgent.AGENTNAME, ThermalCommandBelief.ThermalCommand.FanOn, cbrnPodsInterface.COLLECTOR_POD);
        _beliefManager.put(_tbelief);
    }//GEN-LAST:event_m_CollectorFanActionPerformed

    private void m_TrackerFanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_TrackerFanActionPerformed
       _tbelief = (ThermalCommandBelief)_beliefManager.get(ThermalCommandBelief.BELIEF_NAME);
        if(_tbelief != null && _tbelief.getThermalCommand()== ThermalCommandBelief.ThermalCommand.FanOn)
             _tbelief = new ThermalCommandBelief(WACSDisplayAgent.AGENTNAME, ThermalCommandBelief.ThermalCommand.FanOff, cbrnPodsInterface.TRACKER_POD);
        else
            _tbelief = new ThermalCommandBelief(WACSDisplayAgent.AGENTNAME, ThermalCommandBelief.ThermalCommand.FanOn, cbrnPodsInterface.TRACKER_POD);
        _beliefManager.put(_tbelief);
    }//GEN-LAST:event_m_TrackerFanActionPerformed

    private void m_CollectorHeatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_CollectorHeatActionPerformed
       _tbelief = (ThermalCommandBelief)_beliefManager.get(ThermalCommandBelief.BELIEF_NAME);
        if(_tbelief != null && _tbelief.getThermalCommand()== ThermalCommandBelief.ThermalCommand.HeaterOn)
             _tbelief = new ThermalCommandBelief(WACSDisplayAgent.AGENTNAME, ThermalCommandBelief.ThermalCommand.HeaterOff, cbrnPodsInterface.COLLECTOR_POD);
        else
            _tbelief = new ThermalCommandBelief(WACSDisplayAgent.AGENTNAME, ThermalCommandBelief.ThermalCommand.HeaterOn, cbrnPodsInterface.COLLECTOR_POD);
        _beliefManager.put(_tbelief);
    }//GEN-LAST:event_m_CollectorHeatActionPerformed

    private void m_TrackerHeatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_TrackerHeatActionPerformed
       _tbelief = (ThermalCommandBelief)_beliefManager.get(ThermalCommandBelief.BELIEF_NAME);
        if(_tbelief != null && _tbelief.getThermalCommand()== ThermalCommandBelief.ThermalCommand.HeaterOn)
             _tbelief = new ThermalCommandBelief(WACSDisplayAgent.AGENTNAME, ThermalCommandBelief.ThermalCommand.HeaterOff, cbrnPodsInterface.TRACKER_POD);
        else
            _tbelief = new ThermalCommandBelief(WACSDisplayAgent.AGENTNAME, ThermalCommandBelief.ThermalCommand.HeaterOn, cbrnPodsInterface.TRACKER_POD);
        _beliefManager.put(_tbelief);
    }//GEN-LAST:event_m_TrackerHeatActionPerformed

    private void m_CollectorAutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_CollectorAutoActionPerformed
       _tbelief = (ThermalCommandBelief)_beliefManager.get(ThermalCommandBelief.BELIEF_NAME);
        if(_tbelief != null && _tbelief.getThermalCommand()== ThermalCommandBelief.ThermalCommand.AutoOn)
             _tbelief = new ThermalCommandBelief(WACSDisplayAgent.AGENTNAME, ThermalCommandBelief.ThermalCommand.AutoOff, cbrnPodsInterface.COLLECTOR_POD);
        else
            _tbelief = new ThermalCommandBelief(WACSDisplayAgent.AGENTNAME, ThermalCommandBelief.ThermalCommand.AutoOn, cbrnPodsInterface.COLLECTOR_POD);
        _beliefManager.put(_tbelief);
    }//GEN-LAST:event_m_CollectorAutoActionPerformed

    private void m_TrackerAutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_TrackerAutoActionPerformed
       _tbelief = (ThermalCommandBelief)_beliefManager.get(ThermalCommandBelief.BELIEF_NAME);
        if(_tbelief != null && _tbelief.getThermalCommand()== ThermalCommandBelief.ThermalCommand.AutoOn)
             _tbelief = new ThermalCommandBelief(WACSDisplayAgent.AGENTNAME, ThermalCommandBelief.ThermalCommand.AutoOff, cbrnPodsInterface.TRACKER_POD);
        else
            _tbelief = new ThermalCommandBelief(WACSDisplayAgent.AGENTNAME, ThermalCommandBelief.ThermalCommand.AutoOn, cbrnPodsInterface.TRACKER_POD);
        _beliefManager.put(_tbelief);
    }//GEN-LAST:event_m_TrackerAutoActionPerformed

    private void m_PodControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_PodControlActionPerformed

        _PodWindow.setVisible(true);
    }//GEN-LAST:event_m_PodControlActionPerformed

    private void m_AnacondaControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AnacondaControlActionPerformed
        _AnacondaWindow.setVisible(true);
    }//GEN-LAST:event_m_AnacondaControlActionPerformed

    private void m_IbacControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_IbacControlActionPerformed
        _IBACWindow.setVisible(true);
    }//GEN-LAST:event_m_IbacControlActionPerformed

    private void m_ParticleCollectorControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_ParticleCollectorControlActionPerformed
       _C110Window.setVisible(true);
    }//GEN-LAST:event_m_ParticleCollectorControlActionPerformed

    private void m_AlphaControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AlphaControlActionPerformed
       _BladewerxWindow.setVisible(true);
    }//GEN-LAST:event_m_AlphaControlActionPerformed

    private void m_GammaControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_GammaControlActionPerformed
        _BridgeportWindow.setVisible(true);
    }//GEN-LAST:event_m_GammaControlActionPerformed

    private void m_ThermalControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_ThermalControlActionPerformed
        _ThermalWindow.setVisible(true);
    }//GEN-LAST:event_m_ThermalControlActionPerformed

    private void m_C110CollectSample1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_C110CollectSample1ActionPerformed
       if(_cabelief != null && !_cabelief.isSample1full())
       {
               _cbelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.StoringSample1);
               _beliefManager.put(_cbelief);
       }
    }//GEN-LAST:event_m_C110CollectSample1ActionPerformed

    private void m_C110CollectSample2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_C110CollectSample2ActionPerformed
        if(_cabelief != null && !_cabelief.isSample2full())
       {
            _cbelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.StoringSample2);
            _beliefManager.put(_cbelief);
        }
    }//GEN-LAST:event_m_C110CollectSample2ActionPerformed

    private void m_C110CollectSample3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_C110CollectSample3ActionPerformed
       if(_cabelief != null && !_cabelief.isSample3full())
       {
            _cbelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.StoringSample3);
            _beliefManager.put(_cbelief);
       }
    }//GEN-LAST:event_m_C110CollectSample3ActionPerformed

    private void m_C110CollectSample4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_C110CollectSample4ActionPerformed
       if(_cabelief != null && !_cabelief.isSample4full())
       {
            _cbelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.StoringSample4);
            _beliefManager.put(_cbelief);
       }
    }//GEN-LAST:event_m_C110CollectSample4ActionPerformed

    private void m_C110OnOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_C110OnOffActionPerformed
        _cbelief = (ParticleCollectorStateBelief)_beliefManager.get(ParticleCollectorStateBelief.BELIEF_NAME);
        if(_cbelief == null || _cbelief.getParticleCollectorState() == ParticleCollectorMode.Collecting)
             _cbelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.Idle);
        else
            _cbelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.Collecting);
        _beliefManager.put(_cbelief);
    }//GEN-LAST:event_m_C110OnOffActionPerformed

    private void m_C110ResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_C110ResetActionPerformed
        _cbelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.Reset);
        _beliefManager.put(_cbelief);
    }//GEN-LAST:event_m_C110ResetActionPerformed

    private void m_AnacondaSpectraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AnacondaSpectraActionPerformed
       _AnacondaSpectraWindow.setVisible(true);
       _AnacondaSpectraWindow.repaint();
    }//GEN-LAST:event_m_AnacondaSpectraActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JInternalFrame jInternalFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JTextArea jTextArea1;
    private java.awt.Label label8;
    private javax.swing.JButton m_AlphaControl;
    private javax.swing.JLabel m_AlphaHB;
    private javax.swing.JButton m_AlphaPumpOff;
    private javax.swing.JButton m_AlphaPumpOn;
    private javax.swing.JLabel m_AlphaStatus;
    private javax.swing.JLabel m_AnacondaActualMode;
    private javax.swing.JButton m_AnacondaAirframeMode;
    private javax.swing.JLabel m_AnacondaCommandedMode;
    private javax.swing.JButton m_AnacondaControl;
    private javax.swing.JTextArea m_AnacondaDetections;
    private javax.swing.JLabel m_AnacondaHB;
    private javax.swing.JButton m_AnacondaIdleMode;
    private javax.swing.JButton m_AnacondaPodMode;
    private javax.swing.JComboBox m_AnacondaSampleNum;
    private javax.swing.JButton m_AnacondaSearchMode;
    private javax.swing.JButton m_AnacondaSpectra;
    private javax.swing.JButton m_AnacondaStandbyMode;
    private javax.swing.JButton m_C110Clean;
    private javax.swing.JButton m_C110CollectSample1;
    private javax.swing.JButton m_C110CollectSample2;
    private javax.swing.JButton m_C110CollectSample3;
    private javax.swing.JButton m_C110CollectSample4;
    private javax.swing.JLabel m_C110HB;
    private javax.swing.JButton m_C110OnOff;
    private javax.swing.JButton m_C110Prime;
    private javax.swing.JButton m_C110Reset;
    private javax.swing.JLabel m_C110Status;
    private javax.swing.JButton m_CollectorAuto;
    private javax.swing.JLabel m_CollectorAutoLED;
    private javax.swing.JButton m_CollectorFan;
    private javax.swing.JLabel m_CollectorFanLED;
    private javax.swing.JButton m_CollectorHeat;
    private javax.swing.JLabel m_CollectorHeatLED;
    private javax.swing.JLabel m_CollectorLoggingLED;
    private javax.swing.JLabel m_CollectorTempHB;
    private javax.swing.JButton m_EndLog;
    private javax.swing.JButton m_GammaControl;
    private javax.swing.JLabel m_GammaHB;
    private javax.swing.JLabel m_IbacActualMode;
    private javax.swing.JLabel m_IbacCommandedMode;
    private javax.swing.JButton m_IbacControl;
    private javax.swing.JTextArea m_IbacDetections;
    private javax.swing.JLabel m_IbacHB;
    private javax.swing.JButton m_IbacOff;
    private javax.swing.JButton m_IbacOn;
    private javax.swing.JButton m_NewLog;
    private javax.swing.JButton m_ParticleCollectorControl;
    private javax.swing.JLabel m_Pod0HB;
    private javax.swing.JLabel m_Pod1HB;
    private javax.swing.JButton m_PodControl;
    private javax.swing.JButton m_PodOff;
    private javax.swing.JButton m_PodOn;
    private javax.swing.JButton m_SyncTime;
    private javax.swing.JLabel m_TempHumidityCollectorPod;
    private javax.swing.JLabel m_TempHumidityTrackerPod;
    private javax.swing.JButton m_ThermalControl;
    private javax.swing.JButton m_TrackerAuto;
    private javax.swing.JLabel m_TrackerAutoLED;
    private javax.swing.JButton m_TrackerFan;
    private javax.swing.JLabel m_TrackerFanLED;
    private javax.swing.JButton m_TrackerHeat;
    private javax.swing.JLabel m_TrackerHeatLED;
    private javax.swing.JLabel m_TrackerLoggingLED;
    private javax.swing.JLabel m_TrackerTempHB;
    // End of variables declaration//GEN-END:variables

}
