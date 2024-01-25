package edu.jhuapl.nstd.swarm;


import edu.jhuapl.nstd.swarm.autopilot.PlaneInfo;
import edu.jhuapl.nstd.swarm.autopilot.AutoPilotInterface;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.piccolo.Pic_Telemetry;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.CBRNHeartbeatBelief;
import edu.jhuapl.nstd.swarm.belief.CircularOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.CloudDetection;
import edu.jhuapl.nstd.swarm.belief.CloudDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetection;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionMessageBelief;
import edu.jhuapl.nstd.swarm.belief.EtdStatusMessageBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeActualBelief;
import edu.jhuapl.nstd.swarm.belief.GammaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.IbacActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.IrCameraFOVBelief;
import edu.jhuapl.nstd.swarm.belief.IrExplosionAlgorithmEnabledBelief;
import edu.jhuapl.nstd.swarm.belief.LidarBelief;
import edu.jhuapl.nstd.swarm.belief.LoiterApproachPathBelief;
import edu.jhuapl.nstd.swarm.belief.METBelief;
import edu.jhuapl.nstd.swarm.belief.METTimeName;
import edu.jhuapl.nstd.swarm.belief.ModeTimeName;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.PlumeDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionActualBelief;
import edu.jhuapl.nstd.swarm.belief.RacetrackOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.RegionBelief;
import edu.jhuapl.nstd.swarm.belief.SafetyBoxBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointActualBelief;
import edu.jhuapl.nstd.swarm.belief.ZeroAirDataBelief;
import java.lang.*;
import java.io.*;
import edu.jhuapl.nstd.swarm.util.Config;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;


public class WACSMetrics extends Metrics
{

    protected AutoPilotInterface _api = null;
    protected int _planeID = -1;
    protected Date prevTimeStamp;
    protected Date prevTimeStamp_p;
    protected Date prevTimeStamp_b;
    protected Date prevTimeStamp_s;
    protected Date prevTimeStamp_a;
    protected Date prevTimeStamp_c;
    protected int prevDetections;

    protected boolean logExtendedBeliefs = false;
    protected Date prevTimeStamp_wwb;
    protected Date prevTimeStamp_alphaState;
    protected Date prevTimeStamp_ibacState;
    protected Date prevTimeStamp_c100State;
    protected Date prevTimeStamp_anacondaState;
    protected Date prevTimeStamp_hb;
    protected Date prevTimeStamp_gammaDet;
    protected Date prevTimeStamp_alphaDet;
    protected Date prevTimeStamp_exp;
    protected Date prevTimeStamp_sb;
    protected Date prevTimeStamp_za;
    protected Date prevTimeStamp_ieaeb;
    protected Date prevTimeStamp_icb;
    protected Date prevTimeStamp_etb;
    protected Date prevTimeStamp_rdb;
    protected Date prevTimeStamp_rob;
    protected Date prevTimeStamp_lap;
    protected long prevTimeStamp_cloud;
    protected long prevTimeStamp_etd;
 
  public WACSMetrics(BeliefManager belMgr, UpdateManager upMgr) {
		super(belMgr,upMgr);
                prevDetections = 0;
                prevTimeStamp = new Date(System.currentTimeMillis());

                logExtendedBeliefs = Config.getConfig().getPropertyAsBoolean("WACSMetrics.LogExtendedBeliefs", true);
	}

	public WACSMetrics(BeliefManager belMgr, UpdateManager upMgr, AutoPilotInterface api, int planeID) {
		super(belMgr,upMgr);
		_api = api;
		_planeID = planeID;
                prevDetections = 0;
                prevTimeStamp = new Date(System.currentTimeMillis());

                logExtendedBeliefs = Config.getConfig().getPropertyAsBoolean("WACSMetrics.LogExtendedBeliefs", true);
	}

  public void update()
  {
        try
        {
            write("time: " +
                            new Long(TimeManagerFactory.getTimeManager().getTime()).toString());
            write("\n");
            //if (!matrixCollected())
            //	collectMatrix();
            //collect all positions
            collectAllPositions();
            collectTargets();
            collectRegions();

            //collectLidarRegions();
            if (_api != null)
            {
                collectPlaneInfo();
            }
	    collectMetData();
            collectDetections();
            collectEtdDetections();
            collectBioTelemetry();
            collectEtdDetectionMessage();
            collectEtdStatusMessage();
            collectPlumeTelemetry();
            collectAnacondaTelemetry();
            collectOrbitTelemetry();
            collectPicTelemetry();
            
            if (logExtendedBeliefs)
            {
                collectWacsWaypointBelief();
                collectSensorStates();
                collectRNDetections();
                collectExplosion();
                collectAgentMode();
                collectSafetyBox();
                collectZeroAirdata();
                collectExplosionTime();
                collectLoiterApproachPath();
            }

			write("end time\n");

		} catch (IOException e) {
      e.printStackTrace();
    }
  }

	protected void collectMetData() {
		METBelief mb = (METBelief)_belMgr.get(METBelief.BELIEF_NAME);
		if (mb == null) return;
		METTimeName mtn = mb.getMETTimeName(_belMgr.getName());
		if (mtn == null) return;
		write("metbelief:\n");
		write("windSpeed: " + mtn.getWindSpeed() + "\n");
		write("windBearingTo: " + mtn.getWindBearing() + "\n");
		write("end metbelief\n");
	}

    protected void collectDetections()
    {
        int n = 0;
        long prevtime = 0;
        CloudDetection det;
        CloudDetectionBelief cdb = (CloudDetectionBelief)_belMgr.get(CloudDetectionBelief.BELIEF_NAME);
	if (cdb == null) return;
        if(prevTimeStamp != null && cdb.getTimeStamp().compareTo(prevTimeStamp)<=0) return;
        prevTimeStamp = cdb.getTimeStamp();
        
        synchronized(cdb.getLock())
        {
            for (CloudDetection cloudDetection : cdb.getDetections())
            {
                if (cloudDetection.getTime() > prevTimeStamp_cloud)// && cloudDetection.getValue() >= 1)
                {
                    n++;
                    write("clouddetectionbelief:\n");
                    write("numdetections: "+ cdb.getNumDetections()+"\n");

                    write("detection " + (prevDetections + n) +": \n");
                    write("   latitude : " + cloudDetection.getLat_deg() + "\n");
                    write("   longitude: " + cloudDetection.getLon_deg() + "\n");
                    write("   altitude : " + cloudDetection.getAlt_m() + "\n");
                    write("   value    : " + cloudDetection.getScaledValue() + "    source: " + cloudDetection.getSource() + "\n");
                    write("   time     : " + cloudDetection.getTime() + "\n");

                    write("end clouddetectionbelief\n");
                    
                    
                    /*System.out.println("numdetections: "+ cdb.getNumDetections()+"\n");

                    System.out.println("detection " + (prevDetections + n) +": \n");
                    System.out.println("   latitude : " + cloudDetection.getLat_deg() + "\n");
                    System.out.println("   longitude: " + cloudDetection.getLon_deg() + "\n");
                    System.out.println("   altitude : " + cloudDetection.getAlt_m() + "\n");
                    System.out.println("   value    : " + cloudDetection.getValue() + "    source: " + cloudDetection.getSource() + "\n");
                    System.out.println("   time     : " + cloudDetection.getTime() + "\n");*/

                    prevtime = cloudDetection.getTime();
                }
            }
            if(prevtime > 0)
            {
                prevDetections += n;
                prevTimeStamp_cloud =  prevtime;
            }
        }
    }
    
    protected void collectEtdDetections()
    {
        long prevtime = 0;
        EtdDetectionBelief etdBelief = (EtdDetectionBelief)_belMgr.get(EtdDetectionBelief.BELIEF_NAME);
	if (etdBelief == null) return;
        if(prevTimeStamp != null && etdBelief.getTimeStamp().compareTo(prevTimeStamp)<=0) return;
        prevTimeStamp = etdBelief.getTimeStamp();
        
        synchronized(etdBelief)
        {
            EtdDetection etdDetection = etdBelief.getEtdDetection();
            
            if (etdDetection.getTime() > prevTimeStamp_etd)
            {

                write("etddetectionbelief:\n");

                write("   time         : " + etdDetection.getTime() + "\n");
                write("   latitude     : " + etdDetection.getPosition().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES) + "\n");
                write("   longitude    : " + etdDetection.getPosition().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES) + "\n");
                write("   altitude     : " + etdDetection.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS)+ "\n");
                write("   concentration: " + etdDetection.getConcentration() + "\n");

                write("end etddetectionbelief\n");

                prevtime = etdDetection.getTime();
            }
            
            if(prevtime > 0)
            {
                prevTimeStamp_etd =  prevtime;
            }
        }
    }    
    
    protected void collectEtdDetectionMessage()
    {
        EtdDetectionMessageBelief etdBelief = (EtdDetectionMessageBelief)_belMgr.get(EtdDetectionMessageBelief.BELIEF_NAME);
		if (etdBelief == null) return;

        if(prevTimeStamp_p != null && etdBelief.getTimeStamp().compareTo(prevTimeStamp_p)<=0) return;
            prevTimeStamp_p =  etdBelief.getTimeStamp();
		write("etddetectionmessagebelief:\n");
        write(etdBelief.getDetectionMessage()+ "\n");
		write("end etddetectionmessagebelief\n");
    }
    
    protected void collectEtdStatusMessage()
    {
        EtdStatusMessageBelief etdBelief = (EtdStatusMessageBelief)_belMgr.get(EtdStatusMessageBelief.BELIEF_NAME);
		if (etdBelief == null) return;

        if(prevTimeStamp_p != null && etdBelief.getTimeStamp().compareTo(prevTimeStamp_p)<=0) return;
            prevTimeStamp_p =  etdBelief.getTimeStamp();
		write("etdstatusmessagebelief:\n");
        write(etdBelief.getStatusMessage()+ "\n");
		write("end etdstatusmessagebelief\n");
    }    

    protected void collectPlumeTelemetry()
    {
        PlumeDetectionBelief pdb = (PlumeDetectionBelief)_belMgr.get(PlumeDetectionBelief.BELIEF_NAME);
		if (pdb == null) return;

        if(prevTimeStamp_p != null && pdb.getTimeStamp().compareTo(prevTimeStamp_p)<=0) return;
            prevTimeStamp_p =  pdb.getTimeStamp();
		write("plumedetectionbelief:\n");
        write(pdb.getPlumeDetectionString()+ "\n");
		write("end plumedetectionbelief\n");

	}

    protected void collectOrbitTelemetry()
    {
        CircularOrbitBelief cob = (CircularOrbitBelief)_belMgr.get(CircularOrbitBelief.BELIEF_NAME);
        if (cob != null)
        {
            if(prevTimeStamp_c != null && cob.getTimeStamp().compareTo(prevTimeStamp_c)<=0)
            {
                //data isn't new, don't write anything
            }
            else
            {
                prevTimeStamp_c =  cob.getTimeStamp();
                write("circularorbitbelief:\n");
                write("lat: " + cob.getPosition().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES)+ "\n");
                write("lon: " + cob.getPosition().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES)+ "\n");
                write("alt: " + cob.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS)+ "\n");
                write("rad: " + cob.getRadius().getDoubleValue(Length.METERS) + "\n");
                write("end circularorbitbelief\n");
            }
        }

        RacetrackOrbitBelief rob = (RacetrackOrbitBelief)_belMgr.get(RacetrackOrbitBelief.BELIEF_NAME);
        if (rob != null)
        {
            if(prevTimeStamp_rob != null && rob.getTimeStamp().compareTo(prevTimeStamp_rob)<=0)
            {
                //data isn't new, don't write anything
            }
            else
            {
                prevTimeStamp_rob =  rob.getTimeStamp();
                write("racetrackorbitbelief:\n");
                write("lat1: " + rob.getLatitude1().getDoubleValue(Angle.DEGREES)+ "\n");
                write("lon1: " + rob.getLongitude1().getDoubleValue(Angle.DEGREES)+ "\n");
                write("altF: " + rob.getFinalAltitudeMsl().getDoubleValue(Length.METERS)+ "\n");
                write("altS: " + rob.getStandoffAltitudeMsl().getDoubleValue(Length.METERS)+ "\n");
                write("rad: " + rob.getRadius().getDoubleValue(Length.METERS) + "\n");
                write("end racetrackorbitbelief\n");
            }
        }

        RacetrackDefinitionActualBelief rdb = (RacetrackDefinitionActualBelief)_belMgr.get(RacetrackDefinitionActualBelief.BELIEF_NAME);
        if (rdb != null)
        {
            if(prevTimeStamp_rdb != null && rdb.getTimeStamp().compareTo(prevTimeStamp_rdb)<=0)
            {
                //data isn't new, don't write anything
            }
            else
            {
                prevTimeStamp_rdb =  rdb.getTimeStamp();
                write("racetrackdefinitionbelief:\n");
                write("lat1: " + rdb.getStartPosition().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES)+ "\n");
                write("lon1: " + rdb.getStartPosition().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES)+ "\n");
                write("end racetrackdefinitionbelief\n");
            }
        }
    }

    protected void collectBioTelemetry()
    {
        ParticleDetectionBelief pdb = (ParticleDetectionBelief)_belMgr.get(ParticleDetectionBelief.BELIEF_NAME);
		if (pdb == null) return;

        if(prevTimeStamp_b != null && pdb.getTimeStamp().compareTo(prevTimeStamp_b)<=0) return;
            prevTimeStamp_b =  pdb.getTimeStamp();
		write("biopoddetectionbelief:\n");
        write(pdb.getParticleDetectionString() + "\n");
		write("end biopoddetectionbelief\n");

	}

    protected void collectAnacondaTelemetry()
    {
        AnacondaDetectionBelief adb = (AnacondaDetectionBelief)_belMgr.get(AnacondaDetectionBelief.BELIEF_NAME);
		if (adb == null) return;

        if(prevTimeStamp_a != null && adb.getTimeStamp().compareTo(prevTimeStamp_a)<=0) return;
            prevTimeStamp_a =  adb.getTimeStamp();
		write("anacondadetectionbelief:\n");
        write(adb.getAnacondaDetectionString() + "\n");
		write("end anacondadetectionbelief\n");

    }

	
	protected void collectLidarRegions() {
		LidarBelief lb = (LidarBelief)_belMgr.get(LidarBelief.BELIEF_NAME);
		if (lb == null) return;
		write("lidarbelief:\n");
		write(lb.write());
		write("end lidarbelief\n");
	}

	protected void collectRegions() {
		RegionBelief rb = (RegionBelief)_belMgr.get(RegionBelief.BELIEF_NAME);
		if (rb == null) return;
		write("region_info:\n");
		write(rb.write());
		write("end region_info\n");
	}

        protected void collectPicTelemetry ()
        {
            PiccoloTelemetryBelief telem = (PiccoloTelemetryBelief)_belMgr.get(PiccoloTelemetryBelief.BELIEF_NAME);
            if (telem == null)
                return;
            Pic_Telemetry tel = telem.getPiccoloTelemetry();
            if (tel == null)
                return;

            write ("pic_telem:\n");
            write (tel.toLogString() + "\n");
            write ("end pic_telem\n");
        }

	protected void collectPlaneInfo() {
		PlaneInfo info;
		try {
			info = _api.getPlaneInfo(_planeID);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		write("plane_info:\n");
		write(info + "\n");
		write("end plane_info\n");
	}

        private void collectWacsWaypointBelief()
        {
            WACSWaypointActualBelief wwb = (WACSWaypointActualBelief)_belMgr.get(WACSWaypointActualBelief.BELIEF_NAME);
		if (wwb == null) return;

            if(prevTimeStamp_wwb != null && wwb.getTimeStamp().compareTo(prevTimeStamp_wwb)<=0)
                return;
            prevTimeStamp_wwb =  wwb.getTimeStamp();
            write("wacswaypointbelief:\n");
            write("ia:" + wwb.getIntersectAltitude().getDoubleValue(Length.FEET) + " ft, ");
            write("ir:" + wwb.getIntersectRadius().getDoubleValue(Length.METERS) + " m, ");
            write("laf:" + wwb.getFinalLoiterAltitude().getDoubleValue(Length.FEET) + " ft, ");
            write("las:" + wwb.getStandoffLoiterAltitude().getDoubleValue(Length.FEET) + " ft, ");
            write("lr:" + wwb.getLoiterRadius().getDoubleValue(Length.METERS) + " m, " + "\n");
            write("end wacswaypointbelief\n");
        }

        protected void collectSensorStates()
        {
            collectAlphaState();
            collectIbacState();
            collectC100State();
            collectAnacondaState();
            collectHeartbeat();
        }

        private void collectAlphaState()
        {
            AlphaSensorActualStateBelief alpha = (AlphaSensorActualStateBelief)_belMgr.get(AlphaSensorActualStateBelief.BELIEF_NAME);
		if (alpha == null) return;

            if(prevTimeStamp_alphaState != null && alpha.getTimeStamp().compareTo(prevTimeStamp_alphaState)<=0)
                return;
            prevTimeStamp_alphaState =  alpha.getTimeStamp();
            write("alphasensoractualstatebelief:\n");
            write(alpha.getStateText() + "\n");
            write("end alphasensoractualstatebelief\n");
        }
        private void collectIbacState()
        {
            IbacActualStateBelief ibac = (IbacActualStateBelief)_belMgr.get(IbacActualStateBelief.BELIEF_NAME);
		if (ibac == null) return;

            if(prevTimeStamp_ibacState != null && ibac.getTimeStamp().compareTo(prevTimeStamp_ibacState)<=0)
                return;
            prevTimeStamp_ibacState =  ibac.getTimeStamp();
            write("ibacactualstatebelief:\n");
            write(ibac.getStateText() + "\n");
            write("end ibacactualstatebelief\n");
        }
        private void collectC100State()
        {
            ParticleCollectorActualStateBelief c100 = (ParticleCollectorActualStateBelief)_belMgr.get(ParticleCollectorActualStateBelief.BELIEF_NAME);
		if (c100 == null) return;

            if(prevTimeStamp_c100State != null && c100.getTimeStamp().compareTo(prevTimeStamp_c100State)<=0)
                return;
            prevTimeStamp_c100State =  c100.getTimeStamp();
            write("particlecollectoractualstatebelief:\n");
            write(c100.getStateText() + "\n");
            write("end particlecollectoractualstatebelief\n");
        }
        private void collectAnacondaState()
        {
            AnacondaActualStateBelief anaconda = (AnacondaActualStateBelief)_belMgr.get(AnacondaActualStateBelief.BELIEF_NAME);
		if (anaconda == null) return;

            if(prevTimeStamp_anacondaState != null && anaconda.getTimeStamp().compareTo(prevTimeStamp_anacondaState)<=0)
                return;
            prevTimeStamp_anacondaState =  anaconda.getTimeStamp();
            write("anacondaactualstatebelief:\n");
            write(anaconda.getStateText() + "\n");
            write("end anacondaactualstatebelief\n");
        }
        private void collectHeartbeat ()
        {
            CBRNHeartbeatBelief hb = (CBRNHeartbeatBelief)_belMgr.get(CBRNHeartbeatBelief.BELIEF_NAME);
		if (hb == null) return;

            if(prevTimeStamp_hb != null && hb.getTimeStamp().compareTo(prevTimeStamp_hb)<=0)
                return;
            prevTimeStamp_hb =  hb.getTimeStamp();
            write("cbrnheartbeatbelief:\n");
            write(cbrnPodsInterface.COLLECTOR_POD + ": " + hb.getLogMessage (cbrnPodsInterface.COLLECTOR_POD) + "\n");
            write(cbrnPodsInterface.TRACKER_POD + ": " + hb.getLogMessage (cbrnPodsInterface.TRACKER_POD) + "\n");
            write("end cbrnheartbeatbelief\n");
        }


        protected void collectRNDetections()
        {
            GammaDetectionBelief gamma = (GammaDetectionBelief)_belMgr.get(GammaDetectionBelief.BELIEF_NAME);
		if (gamma == null) return;

            if(prevTimeStamp_gammaDet != null && gamma.getTimeStamp().compareTo(prevTimeStamp_gammaDet)<=0)
                return;
            prevTimeStamp_gammaDet =  gamma.getTimeStamp();
            write("gammadetectionbelief:\n");
            write(gamma.getGammaDetections() + "\n");
            write("end gammadetectionbelief\n");


            AlphaDetectionBelief alpha = (AlphaDetectionBelief)_belMgr.get(AlphaDetectionBelief.BELIEF_NAME);
		if (alpha == null) return;

            if(prevTimeStamp_alphaDet != null && alpha.getTimeStamp().compareTo(prevTimeStamp_alphaDet)<=0)
                return;
            prevTimeStamp_alphaDet =  alpha.getTimeStamp();
            write("alphadetectionbelief:\n");
            write(alpha.getAlphaDetections() + "\n");
            write("end alphadetectionbelief\n");
        }


        protected void collectExplosion()
        {
            ExplosionBelief exp = (ExplosionBelief)_belMgr.get(ExplosionBelief.BELIEF_NAME);
		if (exp == null) return;

            if(prevTimeStamp_exp != null && exp.getTimeStamp().compareTo(prevTimeStamp_exp)<=0)
                return;
            prevTimeStamp_exp =  exp.getTimeStamp();
            write("explosionbelief:\n");
            write(exp.getLocation() + "\n");
            write("end explosionbelief\n");
        }
        protected void collectAgentMode()
        {
            AgentModeActualBelief mode = (AgentModeActualBelief)_belMgr.get(AgentModeActualBelief.BELIEF_NAME);
		if (mode == null) return;

            write("agentmodebelief:\n");
            synchronized (mode)
            {
                Collection c = mode.getAll();
                Iterator i = c.iterator();
                while(i.hasNext())
                {
                    ModeTimeName timeName = (ModeTimeName)i.next();
                    String name = timeName.getName();
                    String modeText = timeName.getMode().toString();
                    write(name + "   " + modeText);
                    write("\n");
                }
            }
            write("end agentmodebelief\n");
        }
        protected void collectSafetyBox()
        {
            SafetyBoxBelief sb = (SafetyBoxBelief)_belMgr.get(SafetyBoxBelief.BELIEF_NAME);
		if (sb == null) return;

            if(prevTimeStamp_sb != null && sb.getTimeStamp().compareTo(prevTimeStamp_sb)<=0)
                return;
            prevTimeStamp_sb =  sb.getTimeStamp();
            write("safetyboxbelief:\n");
            write(sb.toLogMessage() + "\n");
            write("end safetyboxbelief\n");
        }
        protected void collectZeroAirdata()
        {
            ZeroAirDataBelief za = (ZeroAirDataBelief)_belMgr.get(ZeroAirDataBelief.BELIEF_NAME);
		if (za == null) return;

            if(prevTimeStamp_za != null && za.getTimeStamp().compareTo(prevTimeStamp_za)<=0)
                return;
            prevTimeStamp_za =  za.getTimeStamp();
            write("zeroairdatabelief:\n");
            write(za.toLogMessage() + "\n");
            write("end zeroairdatabelief\n");
        }

        protected void collectExplosionTime ()
        {
            ExplosionTimeActualBelief etb = (ExplosionTimeActualBelief)_belMgr.get(ExplosionTimeActualBelief.BELIEF_NAME);
		if (etb == null) return;

            if(prevTimeStamp_etb != null && etb.getTimeStamp().compareTo(prevTimeStamp_etb)<=0)
                return;
            prevTimeStamp_etb =  etb.getTimeStamp();
            
            write("explosiontimebelief:\n");
            write(etb.getTime_ms() + "\n");
            write("end explosiontimebelief\n");
        }

        protected void collectIrData ()
        {
            IrCameraFOVBelief icb = (IrCameraFOVBelief)_belMgr.get(IrCameraFOVBelief.BELIEF_NAME);
		if (icb == null) return;

            if(prevTimeStamp_icb != null && icb.getTimeStamp().compareTo(prevTimeStamp_icb)<=0)
                return;
            prevTimeStamp_icb =  icb.getTimeStamp();

            write("ircamerafovbelief:\n");
            write("max forward ang: " + icb.getMaxAngleFromForwardDeg() + ":");
            write("min forward ang: " + icb.getMinAngleFromForwardDeg() + ":\n");
            write("end ircamerafovbelief\n");



            IrExplosionAlgorithmEnabledBelief ieaeb = (IrExplosionAlgorithmEnabledBelief)_belMgr.get(IrExplosionAlgorithmEnabledBelief.BELIEF_NAME);
		if (ieaeb == null) return;

            if(prevTimeStamp_ieaeb != null && ieaeb.getTimeStamp().compareTo(prevTimeStamp_ieaeb)<=0)
                return;
            prevTimeStamp_ieaeb =  ieaeb.getTimeStamp();

            write("explosiontimebelief:\n");
            write("enabled:" + ieaeb.getEnabled() + "\n");
            write("time remaining:" + ieaeb.getTimeUntilExplosionMs() + "\n");
            write("end explosiontimebelief\n");
        }

        protected void collectLoiterApproachPath()
        {
            LoiterApproachPathBelief lap = (LoiterApproachPathBelief)_belMgr.get(LoiterApproachPathBelief.BELIEF_NAME);
            if (lap == null) return;

            if(prevTimeStamp_lap != null && lap.getTimeStamp().compareTo(prevTimeStamp_lap)<=0) return;
                prevTimeStamp_lap =  lap.getTimeStamp();

            write("loiterapproachpathbelief:\n");

            if (lap.getSafePosition() != null)
            {
                write("safeposition: ");
                write("lat: " + lap.getSafePosition().getLatitude().getDoubleValue(Angle.DEGREES)+ " ");
                write("lon: " + lap.getSafePosition().getLongitude().getDoubleValue(Angle.DEGREES)+ " ");
                write("alt: " + lap.getSafePosition().getAltitude().getDoubleValue(Length.METERS)+ "\n");
            }
            else
            {
                write("safeposition: null\n");
            }
            if (lap.getContactPosition() != null)
            {
                write("contactposition: ");
                write("lat: " + lap.getContactPosition().getLatitude().getDoubleValue(Angle.DEGREES)+ " ");
                write("lon: " + lap.getContactPosition().getLongitude().getDoubleValue(Angle.DEGREES)+ " ");
                write("alt: " + lap.getContactPosition().getAltitude().getDoubleValue(Length.METERS)+ "\n");
            }
            else
            {
                write("contactposition: null\n");
            }
            if (lap.getFirstRangePosition() != null)
            {
                write("firstrangeposition: ");
                write("lat: " + lap.getFirstRangePosition().getLatitude().getDoubleValue(Angle.DEGREES)+ " ");
                write("lon: " + lap.getFirstRangePosition().getLongitude().getDoubleValue(Angle.DEGREES)+ " ");
                write("alt: " + lap.getFirstRangePosition().getAltitude().getDoubleValue(Length.METERS)+ "\n");
            }
            else
            {
                write("firstrangeposition: null\n");
            }

            write("valid: " + lap.getIsPathValid() + "\n");

            write("end loiterapproachpathbelief\n");

        }

}

//=============================== UNCLASSIFIED ==================================
