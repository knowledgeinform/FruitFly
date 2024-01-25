/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.action;

import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.AnacondaModeEnum;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.ParticleCollectorMode;
import edu.jhuapl.nstd.swarm.MissionErrorManager;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AgentModeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptActualBelief;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorStateBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaStateBelief;
import edu.jhuapl.nstd.swarm.belief.Belief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.IbacActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.IbacStateBelief;
import edu.jhuapl.nstd.swarm.belief.MissionActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.MissionCommandedStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorStateBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientRecorderCmdBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientRecorderStatusBelief;
import edu.jhuapl.nstd.swarm.belief.mode.Mode;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author humphjc1
 */
public class MissionManagerAction extends Thread implements Updateable
{
    //Belief Manager
    BeliefManager m_BeliefManager;
    
    //Time when last mission setting was received and used
    private Date m_LastCommandedMissionTime;
    
    /*
     * List of desired beliefs/settings for each mission mode.  Made it funky like this so that we can input settings for each
     * mode just once, but have everything figure it out, including the error checker
     */
    private static final ConcurrentHashMap <Integer, LinkedList <CommandConstructorDetails>> m_MissionModePairs = new ConcurrentHashMap<Integer, LinkedList <CommandConstructorDetails>>();
    private static final ConcurrentHashMap <Integer, LinkedList <CommandDesiredSettings>> m_DesiredMissionModeSettings = new ConcurrentHashMap<Integer, LinkedList <CommandDesiredSettings>>();
    
    private static final ConcurrentHashMap<Integer, LinkedList<RequiredSequentialSettingInfo>> MISSIONSETTINGS_SEQUENTIALSETTINGS = new ConcurrentHashMap<Integer, LinkedList<RequiredSequentialSettingInfo>>();
    
    public MissionManagerAction(BeliefManager belMgr)
    {
        this.setName("WACS-MissionManagerAction");
        m_BeliefManager = belMgr;
        
        this.start();
    }
    
    @Override
    public void run ()
    {
        while (true)
        {
            try
            {
                MissionCommandedStateBelief currCommandBelief = (MissionCommandedStateBelief)m_BeliefManager.get(MissionCommandedStateBelief.BELIEF_NAME);
                if (currCommandBelief != null && (m_LastCommandedMissionTime == null || currCommandBelief.getTimeStamp().after(m_LastCommandedMissionTime)))
                {
                    //Have a new mission mode to configure
                    //Look through the pre-cached commanded beliefs for this mission state, and send them out to the belief manager
                    LinkedList<CommandConstructorDetails> list = m_MissionModePairs.get(currCommandBelief.getState());
                    if (list != null)
                    {
                        for (CommandConstructorDetails beliefPair : list)
                        {
                            Belief newBelief = (Belief)beliefPair.m_CommandedBeliefConstructor.newInstance(beliefPair.m_CommandedBeliefConstructorParameters);
                            m_BeliefManager.put(newBelief);
                        }
                    }
                    else
                    {
                        //If unknown mission received, do nothing and set to manual mission.
                        MissionActualStateBelief currActualBelief = new MissionActualStateBelief(m_BeliefManager.getName(), MissionCommandedStateBelief.MANUAL_STATE);
                        m_BeliefManager.put(currActualBelief);
                    }

                    m_LastCommandedMissionTime = currCommandBelief.getTimeStamp();
                }

                //Compare required sequential commands
                MissionActualStateBelief currMissionBelief = (MissionActualStateBelief)m_BeliefManager.get (MissionActualStateBelief.BELIEF_NAME);
                if (currMissionBelief != null)
                {
                    //Get list of sequential commands for this state, then step through them to see what needs to be udpated
                    LinkedList <RequiredSequentialSettingInfo> list = MISSIONSETTINGS_SEQUENTIALSETTINGS.get(currMissionBelief.getState());
                    if (list != null && list.size() > 0)
                    {
                        for (RequiredSequentialSettingInfo info : list)
                        {
                            for (int i = 0; i < info.m_PriorBeliefValueTimeMs.length; i ++)
                            {
                                if (info.m_PriorBeliefValueTimeMs[i] < currMissionBelief.getTimeStamp().getTime())
                                {
                                    //This prior value hasn't happened yet, check to see if it is currently hit
                                    Belief currentSettingBelief = m_BeliefManager.get (info.m_ActualClass.getSimpleName());
                                    
                                    currentSettingBelief = (Belief)info.m_ActualClass.cast(currentSettingBelief);
                                    //if (currentSettingBelief != null && currentSettingBelief.equals (info.m_PriorBeliefValues))
                                    if (info.m_PriorBeliefValues[i].equals (currentSettingBelief))
                                    {
                                        //This prior value just now happened for this first time.  If it's not the last
                                        //in the sequence, just move on to the next one.  If it is the last, then 
                                        //execute the next command
                                        info.m_PriorBeliefValueTimeMs[i] = currentSettingBelief.getTimeStamp().getTime();
                                        if (i == info.m_PriorBeliefValueTimeMs.length-1)
                                        {
                                            //Execute new command
                                            Belief newBelief = (Belief)info.m_CommandedConstructor.newInstance(info.m_CommandedParams);
                                            m_BeliefManager.put(newBelief);
                                        }
                                        else
                                        {
                                            //Do nothing, check next prior value
                                        }
                                    }
                                    else
                                    {
                                        //This prior belief value hasn't been hit yet, so break this check
                                        break;
                                    }
                                }
                                else
                                {
                                    //Do nothing, This prior value has already been hit, check the next one
                                }
                            }
                        }
                    }
                }
                

            
                Thread.sleep (250);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public synchronized void update ()
    {
        MissionCommandedStateBelief currCommandBelief = (MissionCommandedStateBelief)m_BeliefManager.get(MissionCommandedStateBelief.BELIEF_NAME);
        MissionActualStateBelief currActualBelief = (MissionActualStateBelief)m_BeliefManager.get(MissionActualStateBelief.BELIEF_NAME);

        if (currCommandBelief != null && (currActualBelief == null || currCommandBelief.isNewerThan(currActualBelief)))
        {
            currActualBelief = new MissionActualStateBelief(m_BeliefManager.getName(), currCommandBelief.getState());
            m_BeliefManager.put(currActualBelief);
        }
        
    }
    
    
    private static class CommandConstructorDetails
    {
        Constructor m_CommandedBeliefConstructor;
        Object m_CommandedBeliefConstructorParameters[];
        
        public CommandConstructorDetails (Constructor constructor, Object constructorParameters[])
        {
            m_CommandedBeliefConstructor = constructor;
            m_CommandedBeliefConstructorParameters = constructorParameters;
        }
    }
    
    public static class CommandDesiredSettings
    {
        public Belief m_DesiredBelief;
        public Class m_ActualBeliefClass;
        public String m_BeliefVariable;
        
        public CommandDesiredSettings (Class actualBeliefClass, Belief desiredBeliefSettings)
        {
            this (actualBeliefClass, desiredBeliefSettings, null);
        }
        
        public CommandDesiredSettings (Class actualBeliefClass, Belief desiredBeliefSettings, String beliefVariable)
        {
            m_DesiredBelief = desiredBeliefSettings;
            m_ActualBeliefClass = actualBeliefClass;
            m_BeliefVariable = beliefVariable;
        }
    }
    
    
    public static LinkedList <CommandDesiredSettings> getMissionModeDesiredSettings (int missionMode)
    {
        return m_DesiredMissionModeSettings.get(missionMode);
    }
    
    private static void addSetting (Class commandedClass, Object commandedParams[], Class actualClass, String variableName, String errorCode, LinkedList<CommandConstructorDetails> constructorList, LinkedList<CommandDesiredSettings> desiredList) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
        MissionErrorManager.addMissionErrorType (errorCode, actualClass);
        
        Class paramClasses[] = new Class [commandedParams.length];
        for (int i = 0; i < commandedParams.length; i ++)
        {
            if (commandedParams[i] == null)
            {
                desiredList.add(new CommandDesiredSettings (actualClass, null, variableName));
                return;
            }
            paramClasses[i] = commandedParams[i].getClass();
        }
        Constructor constructor = commandedClass.getConstructor(paramClasses);
        Belief desiredBelief = (Belief)constructor.newInstance(commandedParams);
        constructorList.add(new CommandConstructorDetails (constructor, commandedParams));
        desiredList.add(new CommandDesiredSettings (actualClass, desiredBelief, variableName));
    }
    
    private static void addRequiredSequentialSetting(Class commandedClass, Object[] commandedParams, Class actualClass, Belief[] priorBeliefs, int missionMode) throws NoSuchMethodException
    {
        Class paramClasses[] = new Class [commandedParams.length];
        for (int i = 0; i < commandedParams.length; i ++)
            paramClasses[i] = commandedParams[i].getClass();
        Constructor constructor = commandedClass.getConstructor(paramClasses);
        
        RequiredSequentialSettingInfo newInfo = new RequiredSequentialSettingInfo();
        newInfo.m_ActualClass = actualClass;
        newInfo.m_CommandedConstructor = constructor;
        newInfo.m_CommandedParams = commandedParams;
        newInfo.m_PriorBeliefValues = priorBeliefs;
        newInfo.m_PriorBeliefValueTimeMs = new long [newInfo.m_PriorBeliefValues.length];
        for (int i = 0; i < newInfo.m_PriorBeliefValueTimeMs.length; i ++)
        {
            newInfo.m_PriorBeliefValueTimeMs[i] = -1;
        }
        
        LinkedList <RequiredSequentialSettingInfo> list = MISSIONSETTINGS_SEQUENTIALSETTINGS.get(missionMode);
        if (list != null)
            list.add (newInfo);
        else
        {
            list = new LinkedList <RequiredSequentialSettingInfo>();
            list.add (newInfo);
            MISSIONSETTINGS_SEQUENTIALSETTINGS.put(missionMode, list);
        }
    }
    
    private static class RequiredSequentialSettingInfo
    {
        public Constructor m_CommandedConstructor;
        public Object[] m_CommandedParams;
        public Class m_ActualClass;
        public Belief[] m_PriorBeliefValues;
        public long [] m_PriorBeliefValueTimeMs;
    }

    
    private static void defineAgentModeSetting (String modeName, LinkedList<CommandConstructorDetails> constructorList, LinkedList<CommandDesiredSettings> desiredList) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
        Class commandedClass = AgentModeCommandedBelief.class;
        Object commandedParams[] = {WACSAgent.AGENTNAME, (modeName==null?null:(new Mode(modeName)))};
        String variableName = WACSAgent.AGENTNAME;
        Class actualClass = AgentModeActualBelief.class;
        String errorCode = "WACS state mission mismatch";
        addSetting (commandedClass, commandedParams, actualClass, variableName, errorCode, constructorList, desiredList);
    }
    
    private static void defineAllowableAgentModeSetting (String modeName, String priorMode, int missionMode)
    {
        AgentModeActualBelief allowableBelief = new AgentModeActualBelief(WACSAgent.AGENTNAME, modeName);
        AgentModeActualBelief priorBelief = new AgentModeActualBelief(WACSAgent.AGENTNAME, priorMode);
        MissionErrorManager.addMissionAllowableErrorType (allowableBelief, priorBelief, AgentModeActualBelief.class, missionMode);
    }
    
    private static void defineAllowIntercept (Boolean allow, LinkedList<CommandConstructorDetails> constructorList, LinkedList<CommandDesiredSettings> desiredList) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
        Class commandedClass = AllowInterceptCommandedBelief.class;
        Object commandedParams[] = {WACSAgent.AGENTNAME, allow};
        Class actualClass = AllowInterceptActualBelief.class;
        String errorCode = "Allow intercept mission mismatch";
        addSetting (commandedClass, commandedParams, actualClass, null, errorCode, constructorList, desiredList);
    }
    
    private static void defineIrRecordingState (Boolean recOn, LinkedList<CommandConstructorDetails> constructorList, LinkedList<CommandDesiredSettings> desiredList) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
        Class commandedClass = VideoClientRecorderCmdBelief.class;
        Object commandedParams[] = {WACSAgent.AGENTNAME, recOn};
        Class actualClass = VideoClientRecorderStatusBelief.class;
        String errorCode = "Video recording state mission mismatch";
        addSetting (commandedClass, commandedParams, actualClass, null, errorCode, constructorList, desiredList);
    }
    
    private static void defineAnacondaState (AnacondaModeEnum mode, LinkedList<CommandConstructorDetails> constructorList, LinkedList<CommandDesiredSettings> desiredList) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
        Class commandedClass = AnacondaStateBelief.class;
        Object commandedParams[] = {WACSAgent.AGENTNAME, mode};
        Class actualClass = AnacondaActualStateBelief.class;
        String errorCode = "Chemical sensor state mission mismatch";
        addSetting (commandedClass, commandedParams, actualClass, null, errorCode, constructorList, desiredList);
    }
    
    private static void defineIbacState (Boolean onState, LinkedList<CommandConstructorDetails> constructorList, LinkedList<CommandDesiredSettings> desiredList) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
        Class commandedClass = IbacStateBelief.class;
        Object commandedParams[] = {WACSAgent.AGENTNAME, onState};
        Class actualClass = IbacActualStateBelief.class;
        String errorCode = "Particle sensor state mission mismatch";
        addSetting (commandedClass, commandedParams, actualClass, null, errorCode, constructorList, desiredList);
    }
    
    private static void defineC100State (ParticleCollectorMode mode, LinkedList<CommandConstructorDetails> constructorList, LinkedList<CommandDesiredSettings> desiredList) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
        Class commandedClass = ParticleCollectorStateBelief.class;
        Object commandedParams[] = {WACSAgent.AGENTNAME, mode};
        Class actualClass = ParticleCollectorActualStateBelief.class;
        String errorCode = "Particle collector state mission mismatch";
        addSetting (commandedClass, commandedParams, actualClass, null, errorCode, constructorList, desiredList);
    }
    
    private static void defineRequiredSequentialC100State (ParticleCollectorMode sequentialMode, ParticleCollectorMode priorModes[], int missionMode) throws NoSuchMethodException
    {
        ParticleCollectorActualStateBelief allowableBelief = new ParticleCollectorActualStateBelief(WACSAgent.AGENTNAME, sequentialMode, new Date());
        ParticleCollectorActualStateBelief priorBeliefs[] = new ParticleCollectorActualStateBelief [priorModes.length];
        for (int i = 0; i < priorBeliefs.length; i ++)
            priorBeliefs[i] = new ParticleCollectorActualStateBelief(WACSAgent.AGENTNAME, priorModes[i], new Date());
        MissionErrorManager.addMissionAllowableErrorType (allowableBelief, priorBeliefs[0], ParticleCollectorActualStateBelief.class, missionMode);
        
        //Store information necessary to create the sequential commands after prior modes have been seen
        Class commandedClass = ParticleCollectorStateBelief.class;
        Object commandedParams[] = {WACSAgent.AGENTNAME, sequentialMode};
        Class actualClass = ParticleCollectorActualStateBelief.class;
        addRequiredSequentialSetting (commandedClass, commandedParams, actualClass, priorBeliefs, missionMode);
    }

    private static void defineBladewerxState (Boolean onState, LinkedList<CommandConstructorDetails> constructorList, LinkedList<CommandDesiredSettings> desiredList) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
        Class commandedClass = AlphaSensorStateBelief.class;
        Object commandedParams[] = {WACSAgent.AGENTNAME, onState};
        Class actualClass = AlphaSensorActualStateBelief.class;
        String errorCode = "Alpha sensor state mission mismatch";
        addSetting (commandedClass, commandedParams, actualClass, null, errorCode, constructorList, desiredList);
    }
            
    
    private static void formPreflightStateModePairs ()
    {
        int state = MissionCommandedStateBelief.PREFLIGHT_STATE;
        LinkedList<CommandConstructorDetails> constructorList = new LinkedList<CommandConstructorDetails>();
        LinkedList<CommandDesiredSettings> desiredList = new LinkedList<CommandDesiredSettings>();
        
        try
        {
            //Put agent in loiter mode
            defineAgentModeSetting (LoiterBehavior.MODENAME, constructorList, desiredList);
            
            //Disallow intercept
            defineAllowIntercept(false, constructorList, desiredList);

            //Turn recording off
            defineIrRecordingState (false, constructorList, desiredList);

            //Anaconda idle
            defineAnacondaState (AnacondaModeEnum.Idle, constructorList, desiredList);

            //Ibac Off
            defineIbacState (false, constructorList, desiredList);

            //C100 Off
            defineC100State (ParticleCollectorMode.Idle, constructorList, desiredList);

            //Bladewerx Off
            defineBladewerxState (false, constructorList, desiredList);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        
        m_MissionModePairs.put(state, constructorList);
        m_DesiredMissionModeSettings.put(state, desiredList);
    }
    
    private static void formIngressStateModePairs ()
    {
        int state = MissionCommandedStateBelief.INGRESS_STATE;
        LinkedList<CommandConstructorDetails> constructorList = new LinkedList<CommandConstructorDetails>();
        LinkedList<CommandDesiredSettings> desiredList = new LinkedList<CommandDesiredSettings>();
        
        try
        {
            //Put agent in loiter mode
            defineAgentModeSetting (LoiterBehavior.MODENAME, constructorList, desiredList);
            
            //Disallow intercept
            defineAllowIntercept(false, constructorList, desiredList);

            //Turn recording off
            defineIrRecordingState (false, constructorList, desiredList);

            //Anaconda standby
            defineAnacondaState (AnacondaModeEnum.Standby, constructorList, desiredList);

            //Ibac On
            defineIbacState (true, constructorList, desiredList);

            //C100 Prime (off allowed afterward)
            defineC100State (ParticleCollectorMode.Priming, constructorList, desiredList);
            defineRequiredSequentialC100State (ParticleCollectorMode.Idle, new ParticleCollectorMode[]{ParticleCollectorMode.Priming,ParticleCollectorMode.Idle}, state);

            //Bladewerx on
            defineBladewerxState (true, constructorList, desiredList);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        
        m_MissionModePairs.put(state, constructorList);
        m_DesiredMissionModeSettings.put(state, desiredList);
    }
    
    private static void formSearchStateModePairs ()
    {
        for (int i = 1; i <= 4; i ++)
        {
            int state;
            if (i == 1)
                state = MissionCommandedStateBelief.SEARCH1_STATE;
            else if (i == 2)
                state = MissionCommandedStateBelief.SEARCH2_STATE;
            else if (i == 3)
                state = MissionCommandedStateBelief.SEARCH3_STATE;
            else //if (i == 4)
                state = MissionCommandedStateBelief.SEARCH4_STATE;
            
            LinkedList<CommandConstructorDetails> constructorList = new LinkedList<CommandConstructorDetails>();
            LinkedList<CommandDesiredSettings> desiredList = new LinkedList<CommandDesiredSettings>();

            try
            {
                //Put agent in loiter mode (interecept allowed afterward)
                defineAgentModeSetting (LoiterBehavior.MODENAME, constructorList, desiredList);
                defineAllowableAgentModeSetting (ParticleCloudPredictionBehavior.MODENAME, LoiterBehavior.MODENAME, state);

                //Allow intercept
                defineAllowIntercept(true, constructorList, desiredList);

                //Turn recording on
                defineIrRecordingState (true, constructorList, desiredList);

                //Anaconda search
                if (i == 1)
                    defineAnacondaState (AnacondaModeEnum.Search1, constructorList, desiredList);
                else if (i == 2)
                    defineAnacondaState (AnacondaModeEnum.Search2, constructorList, desiredList);
                else if (i == 3)
                    defineAnacondaState (AnacondaModeEnum.Search3, constructorList, desiredList);
                else if (i == 4)
                    defineAnacondaState (AnacondaModeEnum.Search4, constructorList, desiredList);

                //Ibac On
                defineIbacState (true, constructorList, desiredList);

                //C100 On
                defineC100State (ParticleCollectorMode.Collecting, constructorList, desiredList);

                //Bladewerx on
                defineBladewerxState (true, constructorList, desiredList);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(-1);
            }

            m_MissionModePairs.put(state, constructorList);
            m_DesiredMissionModeSettings.put(state, desiredList);
        }
    }
    
    private static void formEgressStateModePairs ()
    {
        for (int i = 1; i <= 4; i ++)
        {
            int state;
            if (i == 1)
                state = MissionCommandedStateBelief.EGRESS1_STATE;
            else if (i == 2)
                state = MissionCommandedStateBelief.EGRESS2_STATE;
            else if (i == 3)
                state = MissionCommandedStateBelief.EGRESS3_STATE;
            else //if (i == 4)
                state = MissionCommandedStateBelief.EGRESS4_STATE;
            
            LinkedList<CommandConstructorDetails> constructorList = new LinkedList<CommandConstructorDetails>();
            LinkedList<CommandDesiredSettings> desiredList = new LinkedList<CommandDesiredSettings>();

            try
            {
                //Put agent in loiter mode
                defineAgentModeSetting (LoiterBehavior.MODENAME, constructorList, desiredList);

                //Disallow intercept
                defineAllowIntercept(false, constructorList, desiredList);

                //Turn recording off
                defineIrRecordingState (false, constructorList, desiredList);

                //Anaconda off
                defineAnacondaState (AnacondaModeEnum.Idle, constructorList, desiredList);
                
                //Ibac Off
                defineIbacState (false, constructorList, desiredList);

                //C100 Sample (off allowed afterward)
                ParticleCollectorMode mode = ParticleCollectorMode.Idle;
                if (i == 1)
                    mode = ParticleCollectorMode.StoringSample1;
                else if (i == 2)
                    mode = ParticleCollectorMode.StoringSample2;
                else if (i == 3)
                    mode = ParticleCollectorMode.StoringSample3;
                else if (i == 4)
                    mode = ParticleCollectorMode.StoringSample4;
                defineC100State (mode, constructorList, desiredList);
                defineRequiredSequentialC100State (ParticleCollectorMode.Cleaning, new ParticleCollectorMode[]{mode,ParticleCollectorMode.Idle}, state);
                defineRequiredSequentialC100State (ParticleCollectorMode.Idle, new ParticleCollectorMode[]{ParticleCollectorMode.Cleaning,ParticleCollectorMode.Idle}, state);

                //Bladewerx off
                defineBladewerxState (false, constructorList, desiredList);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(-1);
            }

            m_MissionModePairs.put(state, constructorList);
            m_DesiredMissionModeSettings.put(state, desiredList);
        }
    }
    
    private static void formManualStateModePairs ()
    {
        int state = MissionCommandedStateBelief.MANUAL_STATE;
        LinkedList<CommandConstructorDetails> constructorList = new LinkedList<CommandConstructorDetails>();
        LinkedList<CommandDesiredSettings> desiredList = new LinkedList<CommandDesiredSettings>();
        
        try
        {
            //Put agent in loiter mode
            defineAgentModeSetting (null, constructorList, desiredList);
            
            //Disallow intercept
            defineAllowIntercept(null, constructorList, desiredList);

            //Turn recording off
            defineIrRecordingState (null, constructorList, desiredList);

            //Anaconda idle
            defineAnacondaState (null, constructorList, desiredList);

            //Ibac Off
            defineIbacState (null, constructorList, desiredList);

            //C100 Off
            defineC100State (null, constructorList, desiredList);

            //Bladewerx Off
            defineBladewerxState (null, constructorList, desiredList);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        
        m_MissionModePairs.put(state, constructorList);
        m_DesiredMissionModeSettings.put(state, desiredList);
    }
    
    static
    {
        formPreflightStateModePairs ();
        formIngressStateModePairs();
        formSearchStateModePairs();
        formEgressStateModePairs();
        formManualStateModePairs();
    }
    
}
