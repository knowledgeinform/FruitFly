//=============================== UNCLASS ==================================
// 
// Copyright (c) 2001 The Johns Hopkins University/Applied Physics Laboratory
// Developed by JHU/APL.
//
// This material may be reproduced by or for the U.S. Government pursuant to
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988).
// For any other permissions please contact JHU/APL.
//
//=============================== UNCLASS ==================================

package edu.jhuapl.nstd.swarm.display;


import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import edu.jhuapl.jlib.jgeo.JGeoCanvas;
import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.cbrnPods.RNHistogramDisplayGraphPanel;
import edu.jhuapl.nstd.swarm.ModeMap;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.WACSDisplayAgent;
import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.behavior.group.BehaviorGroup;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.ModeWeights;
import edu.jhuapl.nstd.swarm.belief.SafetyBoxBelief;
import edu.jhuapl.nstd.swarm.belief.mode.Mode;
import edu.jhuapl.nstd.swarm.util.Config;

public class SearchDisplay implements Updateable
{



  //number of levels in the canvas
  public static final int NUM_LEVELS = 3;
  protected BeliefManager beliefManager;
  protected SearchCanvas canvas;
  protected AgentTracker agentTracker;
  protected JTabbedPane tabPane = new JTabbedPane();
  protected JTabbedPane settingsTabPane = new JTabbedPane();

  //protected AltitudePanel altitudePanel;
  protected AltitudeChartPanel altitudePanel;
  protected RNHistogramDisplayGraphPanel GammaPanel;
  protected RNHistogramDisplayGraphPanel AlphaPanel;
  protected SafetyBoxPanel _safetyBoxPanel = null;

  public    JSplitPane _HistSplitPane;
  public    JSplitPane _MainSplitPane;
  public    JSplitPane _SplitPane;
  public    JSplitPane _SplitPane2;

  private long _safetyBoxTimestamp = 0;
  private JFrame m_MainFrame = null;
  
  	
  public SearchDisplay(BeliefManager beliefManager)
  {
     try
     {
          this.beliefManager = beliefManager;

          m_MainFrame = new JFrame("WACS Ground Control Station");

          agentTracker = new AgentTracker(beliefManager);
          //altitudePanel = new AltitudePanel(beliefManager, agentTracker);
          altitudePanel = new AltitudeChartPanel(beliefManager);
          canvas = createCanvas(beliefManager, m_MainFrame);
          GammaPanel = new RNHistogramDisplayGraphPanel();
          GammaPanel.selectLogScale(true);
          AlphaPanel = new RNHistogramDisplayGraphPanel();
          Dimension d = new Dimension(300,300);
          GammaPanel.setMinimumSize(d);
          AlphaPanel.setMinimumSize(d);
          GammaPanel.setTitle("Gamma Count Histogram");
          AlphaPanel.setTitle("Alpha Count Histogram");

          JPanel searchSettings = canvas.getSearchSettingPanel();
          JPanel displaySettings = canvas.getDisplaySettingPanel();
          JPanel agentSettings = canvas.getAgentSettingsPanel();
          WACSSettingsPanel wacspanel = null;
          WACSControlPanel wacscontrolpanel = null;          

            wacspanel = new WACSSettingsPanel(canvas, beliefManager);
            wacscontrolpanel = new WACSControlPanel(canvas, beliefManager);
            _safetyBoxPanel = new SafetyBoxPanel(canvas, beliefManager);

            canvas.setWACSPanel(wacspanel);
            canvas.setWACSControlPanel(wacscontrolpanel);
          
          //Make our control Pane a tabbed pane
          tabPane.setPreferredSize(new Dimension(270, 800));

            JScrollPane jsp1 = new JScrollPane(wacscontrolpanel);
            tabPane.addTab("Sensors", jsp1);

            JScrollPane jsp2 = new JScrollPane(wacspanel);
            tabPane.addTab("UAV", jsp2);

            JScrollPane jsp3 = new JScrollPane(_safetyBoxPanel);
            tabPane.addTab("Waypoint Box", _safetyBoxPanel);
          

          settingsTabPane.addTab("Commands", searchSettings);
          settingsTabPane.addTab("Display ", displaySettings);
          settingsTabPane.addTab("Agent Info", agentSettings);



          tabPane.addTab("Swarm" , settingsTabPane);




          _HistSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, GammaPanel, AlphaPanel);
          _HistSplitPane.setResizeWeight(0.5);
          _HistSplitPane.setDividerLocation(0.5);

           _SplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, altitudePanel,_HistSplitPane);
           _SplitPane.setDividerLocation(0.375);
           _SplitPane.setResizeWeight(0.0);

          _MainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, canvas, _SplitPane);
          _MainSplitPane.setResizeWeight(1.0);

          Dimension minimumSize = new Dimension(0, 0);
          altitudePanel.setMinimumSize(minimumSize);
          _HistSplitPane.setMinimumSize(minimumSize);
//          altitudePanel.setPreferredSize(new Dimension(600, 150));
//          _HistSplitPane.setPreferredSize(new Dimension(600, 300));
//          _SplitPane.setPreferredSize(new Dimension(600, 450));
//          _SplitPane.setMinimumSize(new Dimension(300, 300));
          //create the split pane
          _SplitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPane, _MainSplitPane);
          _SplitPane2.setDividerLocation(320);
          _SplitPane2.setResizeWeight(0.0);


          m_MainFrame.getContentPane().add(_SplitPane2, BorderLayout.CENTER);


          JToolBar toolbar = canvas.createToolBar(false);
          JToolBar toolbar2 = canvas.createTelemetryBar(false);
          JSplitPane toolbars = new JSplitPane(JSplitPane.VERTICAL_SPLIT, toolbar, toolbar2);
          toolbars.setDividerSize(1);

          JMenuBar menubar = canvas.createMenuBar();


          //f.getContentPane().add(toolbar, BorderLayout.NORTH);
          m_MainFrame.getContentPane().add(toolbars, BorderLayout.NORTH);

          m_MainFrame.setJMenuBar(menubar);

          m_MainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          m_MainFrame.setSize(1440, 900);
          m_MainFrame.pack();
          m_MainFrame.setVisible(true);
          
            m_MainFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
            m_MainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    exitGracefully();
                }
            });

    } catch (Exception ex){
      System.err.println("Cannot Create Model");
      ex.printStackTrace();
    }
  }

    private void exitGracefully ()
    {
        int opt = JOptionPane.showConfirmDialog(m_MainFrame, "Are you sure you want to exit?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (opt == JOptionPane.NO_OPTION)
            return;

        m_MainFrame.dispose();
        System.exit(0);
    }

  protected SearchCanvas createCanvas(BeliefManager belMgr, JFrame owner) 
  {
      double startLat = Config.getConfig().getPropertyAsDouble("agent.startLat");
      double startLon = Config.getConfig().getPropertyAsDouble("agent.startLon");
        ModeMap m = createDefaultModeMap(WACSDisplayAgent.AGENTNAME, belMgr);

      boolean useImageryFetcher = Config.getConfig().getPropertyAsBoolean("SearchDisplay.UseImageryFetcher", false);
      LatLonAltPosition viewCenter = null;
      Length viewRange = null;
      if (useImageryFetcher)
      {
        viewCenter = new LatLonAltPosition (new Latitude (startLat, Angle.DEGREES), new Longitude (startLon, Angle.DEGREES), new Altitude (0, Length.METERS));
        viewRange = new Length(16.0, Length.MILES);
        return new SearchCanvas(NUM_LEVELS, viewRange, 1024, 768, viewCenter, belMgr, m, owner, agentTracker,this);
      }
        else
      {
        viewCenter = LatLonAltPosition.ORIGIN;
        viewRange = new Length(2000.0, Length.MILES);
        return new SearchCanvas(NUM_LEVELS, viewRange, 1024, 768, JGeoCanvas.ORTHOGRAPHIC_PROJECTION, viewCenter, belMgr, m, owner, agentTracker,this);
        }
      
  }
  
  protected ModeMap createDefaultModeMap(String id, BeliefManager belMgr){
      //Create the Mode Map
      ModeMap modeMap = new ModeMap(id);
      
      //Create the Behavior Group that will hold all of our behaviors
      BehaviorGroup behaviorGroup = new BehaviorGroup(belMgr, modeMap, id);
      
      //Set the weights for the behaviors
      ModeWeights searchMode = new ModeWeights(new Mode("search"), behaviorGroup);
      
      ModeWeights stopMode = new ModeWeights(new Mode("idle"), behaviorGroup);

      ModeWeights interceptMode = new ModeWeights(new Mode("intercept"), behaviorGroup);
      
      ModeWeights loiterMode = new ModeWeights(new Mode(LoiterBehavior.MODENAME), behaviorGroup);
      
      ModeWeights reacquireMode = new ModeWeights(new Mode("reacquire"), behaviorGroup);
     
      //add all of the modes and weights to the mode map
      modeMap.addMode(searchMode);
      modeMap.addMode(stopMode);
      modeMap.addMode(interceptMode);
      modeMap.addMode(loiterMode);
      modeMap.addMode(reacquireMode);
      
      return modeMap;
  }

  @Override
  public synchronized void update()
  {
      try
      {
          //
          // Update the safety box if it has changed
          //
          SafetyBoxBelief safetyBoxBelief = (SafetyBoxBelief)beliefManager.get(SafetyBoxBelief.BELIEF_NAME);
          if (safetyBoxBelief != null &&
              (safetyBoxBelief.getTimeStamp().getTime() > _safetyBoxTimestamp || _safetyBoxPanel.unitsHaveChanged()))
          {
              canvas.setSafetyBox(new Latitude(safetyBoxBelief.getLatitude1_deg(), Angle.DEGREES),
                                  new Latitude(safetyBoxBelief.getLatitude2_deg(), Angle.DEGREES),
                                  new Longitude(safetyBoxBelief.getLongitude1_deg(), Angle.DEGREES),
                                  new Longitude(safetyBoxBelief.getLongitude2_deg(), Angle.DEGREES));

              _safetyBoxPanel.setSafetyBoxBelief(safetyBoxBelief);

              _safetyBoxTimestamp = safetyBoxBelief.getTimeStamp().getTime();
          }
          canvas.update();
          altitudePanel.update();
          agentTracker.update();
      }
      catch(Exception e)
      {
          System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
      }
  }

} // end class SearchDisplay
    
