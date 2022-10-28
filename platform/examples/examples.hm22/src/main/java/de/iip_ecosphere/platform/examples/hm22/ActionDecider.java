/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.examples.hm22;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.util.Comparator.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.services.environment.ProcessSupport;
import de.iip_ecosphere.platform.services.environment.ProcessSupport.ScriptOwner;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.AiResult;
import iip.datatypes.Command;
import iip.datatypes.CommandImpl;
import iip.datatypes.DecisionResult;
import iip.datatypes.DecisionResultImpl;
import iip.datatypes.MdzhOutput;
import iip.datatypes.PlcInput;
import iip.datatypes.PlcInputImpl;
import iip.datatypes.PlcOutput;
import iip.impl.ActionDeciderImpl;

/**
 * Implements the action decider.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ActionDecider extends ActionDeciderImpl {

    public static final long OVERALL_TIMEOUT = TimeUnit.MINUTES.toMillis(2);
    
    protected enum ScanPos {
        LEFT,
        RIGHT
    }

    // between starting to wait / we accept completed
    protected static final long TIMEDIFF_ROBOT_BUSY_SAFE = TimeUnit.MICROSECONDS.toMillis(700); 
    protected static final int PLC_ROBOT_SCAN_QR = 1;
    protected static final int PLC_ROBOT_SCAN_LEFT = 2; // legacy
    protected static final int PLC_ROBOT_SCAN_RIGHT = 3; // legacy
    protected static final int PLC_ROBOT_HAPPY = 10; // legacy
    protected static final int PLC_ROBOT_UNHAPPY = 11; // legacy
    protected static final int PLC_ROBOT_BASE = 20; // legacy
    protected static final int PLC_ROBOT_NOP = 0;
    protected static final int PLC_ROBOT_NEXT_QUIT = -2; // translate, do not send to PLC
    protected static final int PLC_ROBOT_NEXT_NONE = -1; // do not send to PLC
    protected static final int PLC_ROBOT_NEXT_HAPPY = 10; // TODO PLC check INDEX
    protected static final int PLC_ROBOT_NEXT_UNHAPPY = 11; // TODO PLC check INDEX
    protected static final int PLC_ROBOT_NEXT_BASE = 12; // TODO PLC check INDEX
    protected static final int PLC_ROBOT_NEXT_ANY = 100;
    // TODO PLC check OPCUA PC_RequestOperation#

    protected static final int ROS_ROBOT_SCAN_QR = 1;
    protected static final int ROS_ROBOT_SCAN_LEFT = 2;
    protected static final int ROS_ROBOT_SCAN_RIGHT = 3;
    protected static final int ROS_ROBOT_HAPPY = 10;
    protected static final int ROS_ROBOT_UNHAPPY = 11;
    protected static final int ROS_ROBOT_BASE = 20;

    private static ActionDecider instance;
    
    // getParameterRobotIP() now available after constructor and reconfigure was executed
    
    // getParameterUsePlc();
    private boolean usePlc = Boolean.valueOf(System.getProperty("iip.app.hm22.usePlc", "false")); 
    private boolean readyForRequest;
    private Map<String, MdzhOutput> productData = new HashMap<>();
    private List<AiResult> aiResults = new ArrayList<AiResult>();
    private Random random = new Random();
    private File storageFolder;
    private long aiResultsTimestamp;
    private long lastAiResultsTimestamp;
    private String actualAi = "PythonAi";

    private State currentState = State.STOP;
    private long lastStateChange;
    
    private ScriptOwner robotScriptOwner = new ScriptOwner("hm22-robot", "src/main/python/robot", "python-robot.zip");
    
    /**
     * Internal states for longer running operations.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected enum State {
        STOP,
        RUNNING, // sub-divided by robot movements, picture taking and result receiving
        SWITCHING_AI,
        WAIT_FOR_ROBOT
    }
    
    /**
     * Fallback constructor, also used for testing main program.
     */
    public ActionDecider() {
        super(ServiceKind.TRANSFORMATION_SERVICE);
        setCurrentState(State.STOP);
        instance = this;
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public ActionDecider(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
        instance = this;
    }

    /**
     * Called to notify that switching the AI is completed.
     * 
     * @param id the id of the new AI
     */
    static void notifyAiSwitchCompleted(String id) {
        if (null != instance) {
            // do not interfere with running, in particular upon initial "switch"
            if (instance.getCurrentState() == State.SWITCHING_AI) {
                instance.setCurrentState(State.STOP);
            }
            instance.actualAi = id;
        } else {
            System.out.println("AiSwitch completed but cannot notify ActionDecider as instance is null.");
        }
    }
    
    /**
     * Returns the current processing state.
     * 
     * @return the state
     */
    protected State getCurrentState() {
        return currentState;
    }
    
    /**
     * Returns the number of actually stored AI results.
     * 
     * @return the number
     */
    protected int getAiResultsCount() {
        return aiResults.size();
    }
    
    /**
     * Returns the storage base folder, i.e., the folder where the {@link #getStorageFolder()} shall 
     * be located in. [mocking]
     *  
     * @return the storage base folder
     */
    protected File getStorageBaseFolder() {
        return new File(System.getProperty("user.home", "."));
    }
    
    /**
     * Returns the storage folder for images, results, etc.
     * 
     * @return the storage folder
     */
    private File getStorageFolder() {
        if (null == storageFolder) {
            storageFolder = new File(getStorageBaseFolder(), "iipHm22Data");
            storageFolder.mkdirs();
        }
        return storageFolder;
    }

    /**
     * Returns a valid timestamp for persistence, either {@link #aiResultsTimestamp} or the actual system time.
     * 
     * @param fallback if the process is already gone, not positive for none
     * @return the timestamp
     */
    private long getAiTimestamp(long fallback) {
        return aiResultsTimestamp > 0 ? aiResultsTimestamp 
            : (fallback > 0 ? fallback : System.currentTimeMillis());
    }

    /**
     * Returns an AI file id.
     * 
     * @param timestamp the timestamp from {@link #getAiTimestamp(long)}
     * @return the file id, potentially including the image sequence nr
     */
    private String getAiFileId(long timestamp) {
        return aiResultsTimestamp > 0 ? timestamp + "-" + aiResults.size() : String.valueOf(timestamp);
    }

    /**
     * Repressents a persisted result, just for writing.
     * 
     * @author Holger Eichelberger, SSE
     */
    class PersistedResult {

        private String ai;
        private long timestamp;
        private int imageNr;

        /**
         * Creates an instance based on the given timestamp.
         * 
         * @param timestamp the timestamp {@link ActionDecider#getAiTimestamp(long)}
         */
        private PersistedResult(long timestamp) {
            ai = actualAi;
            this.timestamp = timestamp;
            imageNr = aiResultsTimestamp > 0 ? aiResults.size() : -1;
        }
        
        /**
         * Returns the AI service name. [jackson]
         * 
         * @return the AI service name
         */
        public String getAi() {
            return ai;
        }
        
        /**
         * Returns the timestamp of the first image. [jackson]
         * 
         * @return the timestamp
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * Returns the image number. [jackson]
         * 
         * @return the image number, negative if N/A
         */
        public int getImageNr() {
            return imageNr;
        }

    }

    /**
     * Repressents a persisted AI result, just for writing.
     * 
     * @author Holger Eichelberger, SSE
     */
    class PersistedAiResult extends PersistedResult {

        private AiResult result;
        
        /**
         * Creates an instance based on the given timestamp and result.
         * 
         * @param timestamp the timestamp {@link ActionDecider#getAiTimestamp(long)}
         * @param result the AI result to store
         */
        private PersistedAiResult(long timestamp, AiResult result) {
            super(timestamp);
            this.result = result;
        }

        /**
         * Returns the AI result. [jackson]
         * 
         * @return the AI result
         */
        public AiResult getResult() {
            return result;
        }
        
    }

    /**
     * Stores the AI result to disk.
     * 
     * @param data the data to store
     */
    protected void store(AiResult data) {
        File folder = getStorageFolder();
        long timestamp = getAiTimestamp(0);
        String id = getAiFileId(timestamp);
        
        File f = new File(folder, id + ".res");
        PersistedAiResult res = new PersistedAiResult(timestamp, data);
        storeToJson(f, res);
    }
    
    /**
     * Stores the given data to JSON.
     * 
     * @param file the file to store the data to
     * @param data the data to store
     */
    protected void storeToJson(File file, Object data) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(file, data);
        } catch (IOException e) {
            System.out.println("Cannot write " + file.getAbsolutePath() + ": " + e.getMessage());
        }
    }
    
    /**
     * Repressents a persisted AI feedback, just for writing.
     * 
     * @author Holger Eichelberger, SSE
     */
    class PersistedAiFeedback extends PersistedResult {
        
        private String feedback;

        /**
         * Creates an instance based on the given timestamp and feedback.
         * 
         * @param timestamp the timestamp {@link ActionDecider#getAiTimestamp(long)}
         * @param feedback the feedback
         */
        private PersistedAiFeedback(long timestamp, String feedback) {
            super(timestamp);
            this.feedback = feedback;
        }
        
        /**
         * Returns the feedback. [jackson]
         * 
         * @return the feedback
         */
        public String getFeedback() {
            return feedback;
        }

    }
    
    /**
     * Stores the feedback on AI.
     * 
     * @param feedback the feedback
     */
    protected void storeFeedback(String feedback) {
        File folder = getStorageFolder();
        long timestamp = getAiTimestamp(lastAiResultsTimestamp);
        String id = getAiFileId(timestamp);
        
        File f = new File(folder, id + ".fbk");
        PersistedAiFeedback res = new PersistedAiFeedback(timestamp, feedback);
        storeToJson(f, res);
    }

    @Override
    public void processAiResult(AiResult data) {
        if (State.RUNNING == currentState) {
            if (aiResults.size() == 0) {
                aiResultsTimestamp = System.currentTimeMillis();
            }
            store(data);
            aiResults.add(data);
            if (aiResults.size() == 1) {
                Command aasQueryCommand = new CommandImpl();
                aasQueryCommand.setCommand(Commands.QUERY_CAR_AAS.toString());
                aasQueryCommand.setStringParam(data.getProductId());
                sendCommand(aasQueryCommand);
                robotDoCarScanInPos(ScanPos.LEFT, 
                    () -> setCurrentState(State.WAIT_FOR_ROBOT), 
                    () -> sendCommand(Commands.SOURCE_TAKE_PICTURE));
            } else if (aiResults.size() == 2) {
                robotDoCarScanInPos(ScanPos.RIGHT, 
                    () -> setCurrentState(State.WAIT_FOR_ROBOT),
                    () -> sendCommand(Commands.SOURCE_TAKE_PICTURE));
            } else if (aiResults.size() >= 3) { // actually three, but who knows
                DecisionResult res = aggregateResults();
                String prodId = res.getProductId();
                boolean io = true; 
                int ioReason = 0;
                if (res.getScratch()) {
                    io = false; // by default, if there are scratches, the car is not ok
                    ioReason = 1;
                } else {
                    if (null != prodId && productData.containsKey(prodId)) {
                        MdzhOutput pData = productData.get(prodId);
                        System.out.println("Using AAS data for decision: " + pData);
                        io &= res.getEngraving() == pData.getPattern();
                        if (!io) {
                            ioReason = 4;
                        }
                        io &= equalsSafe(res.getWheelColour(), pData.getTiresColor());
                        if (!io) {
                            ioReason = 3;
                        }
                        io &= res.getNumWindows() == pData.getWindows();
                        if (!io) {
                            ioReason = 2;
                        }
                    } else {
                        ioReason = 5;
                    }
                }
                res.setIo(io);
                res.setIoReason(ioReason);
                ingestDecisionResult(res);
                if (io) {
                    robotDoSignalSuccess();
                } else {
                    robotDoSignalFailure();
                }
                setCurrentState(State.STOP); // we are done here
            }
        }
    }

    /**
     * Aggregate the AI results.
     * 
     * @return the aggregated output of the action decider without {@link DecisionResult#getIo()}.
     */
    private DecisionResult aggregateResults() {
        DecisionResult res = null;
        if (aiResults.size() >= 3) { // actually only three, but who knows
            res = new DecisionResultImpl();
            String prodId = null;
            for (int i = 0; null == prodId && i < aiResults.size(); i++) { // shall be in the first...
                prodId = aiResults.get(i).getProductId();
            }
            res.setProductId(prodId);
            AiResult ai1 = aiResults.get(1);
            AiResult ai2 = aiResults.get(2);
            
            res.setImage(ai1.getImage());
            
            // engraving
            if (ai1.getEngraving() == ai2.getEngraving()) {
                res.setEngravingConfidence(max(r -> r.getEngravingConfidence()));
                res.setEngraving(ai1.getEngraving());
            } else {
                if (ai1.getEngravingConfidence() > ai2.getEngravingConfidence()) {
                    res.setEngravingConfidence(ai1.getEngravingConfidence());
                    res.setEngraving(ai1.getEngraving());
                } else {
                    res.setEngravingConfidence(ai2.getEngravingConfidence());
                    res.setEngraving(ai2.getEngraving());
                }
            }
            
            // scratches
            res.setScratchConfidence(max(r -> r.getScratchConfidence()));
            res.setScratch(or(r -> r.getScratch()));

            // windows
            double[] winConf1 = windowConfs(ai1);
            double[] winConf2 = windowConfs(ai2);
            int idxWin1 = maxWindowsPerSideIdx(winConf1);
            int idxWin2 = maxWindowsPerSideIdx(winConf2);
            if (idxWin1 == idxWin2) {
                res.setNumWindows(idxWin1 + 1);
                res.setNumWindowsConfidence(Math.max(winConf1[idxWin1], winConf2[idxWin2]));
            } else {
                if (ai1.getScratchConfidence() < ai2.getScratchConfidence()) {
                    res.setNumWindows(idxWin1 + 1);
                    res.setNumWindowsConfidence(winConf1[idxWin1]);
                } else {
                    res.setNumWindows(idxWin2 + 1);
                    res.setNumWindowsConfidence(winConf2[idxWin2]);
                }
            }
            
            if (equalsSafe(ai1.getWheelColour(), ai2.getWheelColour())) {
                res.setWheelColour(ai1.getWheelColour());
            } else {
                if (random.nextDouble() >= 0.5) {
                    res.setWheelColour(ai1.getWheelColour());
                } else {
                    res.setWheelColour(ai2.getWheelColour());
                }
            }
        }
        return res;
    }
    
    /**
     * Compares two strings internally turning null values to empty strings.
     * 
     * @param s1 the first string
     * @param s2 the second string
     * @return {@code true} if both are equal, {@code false} else
     */
    private boolean equalsSafe(String s1, String s2) {
        s1 = s1 == null ? "" : s1;
        s2 = s2 == null ? "" : s2;
        return s1.equals(s2);
    }

    /**
     * Turns the window confidences in {@code res} into an array.
     *  
     * @param res the AI result
     * @return the confidence array, one window at index 0, two windows at index 1, three windows at index 2
     */
    private double[] windowConfs(AiResult res) {
        double[] windowConf = new double[3];
        windowConf[0] = res.getOneWindowConfidence();
        windowConf[1] = res.getTwoWindowsConfidence();
        windowConf[2] = res.getThreeWindowsConfidence();
        return windowConf;
    }
    
    /**
     * Returns the index with maximum confidence value.
     * 
     * @param confs the confidences, one window at index 0, two windows at index 1, three windows at index 2
     * @return the index (window number would be result +1)
     */
    private int maxWindowsPerSideIdx(double[] confs) {
        int maxIndex = IntStream.range(0, confs.length).boxed()
            .max(comparingDouble(i -> confs[i]))
            .get();  // or throw if empty list
        return maxIndex;
    }
    
    /**
     * Returns a disjunctive result over all non-QR AI results for a certain field.
     * 
     * @param pred the predicate to project values from the AI results
     * @return the disjunction
     */
    private boolean or(Predicate<AiResult> pred) {
        boolean result = false;
        for (int i = 1; !result && i < aiResults.size(); i++) { // ignore the first
            result |= pred.test(aiResults.get(i));
        }
        return result;
    }

    /**
     * Returns a conjunctive result over all non-QR AI results for a certain field.
     * 
     * @param pred the predicate to project values from the AI results
     * @return the conjunction
     */
    @SuppressWarnings("unused")
    private boolean and(Predicate<AiResult> pred) {
        boolean result = true;
        if (aiResults.size() > 1) {
            result = true;
            for (int i = 1; result && i < aiResults.size(); i++) { // ignore the first
                result &= pred.test(aiResults.get(i));
            }
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Returns an average over all non-QR AI results for a certain field.
     * 
     * @param acc the function to project values from the AI results
     * @return the average value
     */
    @SuppressWarnings("unused")
    private double avg(ToDoubleFunction<AiResult> acc) {
        double sum = 0;
        int count = 0;
        for (int i = 1; i < aiResults.size(); i++) { // ignore the first
            sum += acc.applyAsDouble(aiResults.get(i));
            count++;
        }
        return sum / count;
    }

    /**
     * Returns the maximum over all non-QR AI results for a certain field.
     * 
     * @param acc the function to project values from the AI results
     * @return the maximum value
     */
    private double max(ToDoubleFunction<AiResult> acc) {
        double max = 0;
        for (int i = 1; i < aiResults.size(); i++) { // ignore the first
            double val = acc.applyAsDouble(aiResults.get(i));
            if (i == 1) {
                max = val;
            } else {
                max = Math.max(max, val);
            }
        }
        return max;
    }
    @Override
    public void processPlcOutput(PlcOutput data) {
        readyForRequest = data.getPC_ReadyForRequest();
        if (data.getHW_StartProcess()) {
            handleCommand(Commands.REQUEST_START, null);
        } else if (data.getHW_SwitchAi()) {
            handleCommand(Commands.REQUEST_SWITCH_AI, null);
        } // TODO hw request quit? red button?
        if (State.WAIT_FOR_ROBOT == currentState) {
            if (!data.getPC_RobotBusyOperating() 
                && System.currentTimeMillis() - lastStateChange > TIMEDIFF_ROBOT_BUSY_SAFE) { // TODO PLC check
                setCurrentState(State.RUNNING);
            }
        }
    }

    @Override
    public void processMdzhOutput(MdzhOutput data) {
        productData.put(data.getProductId(), data);
    }

    @Override
    public void processCommand(Command data) {
        handleCommand(Commands.valueOfSafe(data), data.getStringParam());
    }
    
    /**
     * Handles a command, represents the state machine transitions.
     * 
     * @param cmd the command
     * @param param the parameter for the command, may be <b>null</b> or empty for none
     */
    protected void handleCommand(Commands cmd, String param) {
        System.out.println("Action Decider: Received command " + cmd + " Current state: " + currentState);
        switch (cmd) {
        case SEND_FEEDBACK_TO_AI: // can be done all the time
            storeFeedback(param);
            break;
        case REQUEST_START:
            if (currentState == State.STOP) {
                setCurrentState(State.RUNNING);
                robotDoQrScan();
                sendCommand(Commands.SOURCE_DO_QR_SCAN);
            } else {
                System.out.println("Action Decider: Cannot start process. Current state is " 
                    + currentState + ". Ignored.");
            }
            break;
        case REQUEST_SWITCH_AI: // prerequisite: not running
            if (currentState == State.STOP) {
                setCurrentState(State.SWITCHING_AI);
                sendCommand(Commands.SWITCH_AI);
            } else {
                System.out.println("Action Decider: Cannot switch AI. Some processing is going on. Ignored.");
            }
            break;
        case REQUEST_QUIT: // 
            if (currentState == State.RUNNING || currentState == State.WAIT_FOR_ROBOT) {
                robotConfirmQuit();
                setCurrentState(State.STOP);
            } else {
                System.out.println("Action Decider: Cannot send quit to robot. Process is not running. Ignored.");
            }
            break;
        case SOURCE_TAKE_PICTURE: // not here
        case SWITCH_AI: // nothing to switch here, command is sent, passed to AiServiceSelector as defined in model
        case INVALID:
        default:
            break;
        }
        if (System.currentTimeMillis() - lastStateChange > OVERALL_TIMEOUT) {
            System.out.println("DECIDER TIMEOUT: Reset to Stop");
            setCurrentState(State.STOP);
        }
    }

    /**
     * Switches from PLC to Plan B Python and back. [public for testing]
     * 
     * @param usePlc if {@code true} use the PLC, if {@code false} use the Python Plan B
     * @return the old value
     */
    public boolean usePlc(boolean usePlc) {
        boolean before = usePlc;
        this.usePlc = usePlc;
        return before;
    }
    
    /**
     * Requests the robot to do the QR scan. [public for testing]
     */
    public void robotDoQrScan() {
        if (usePlc) {
            sendRobotCommand(PLC_ROBOT_SCAN_QR, PLC_ROBOT_NEXT_NONE);
        } else {
            callRobot(ROS_ROBOT_SCAN_QR);
        }
    }
    
    /**
     * Requests the robot to do the car scan in given position. [public for testing]
     * 
     * @param pos the position to drive the robot to (1 or 2 [else])
     * @param nextPlc the operation to do immediately after sending the command if the PLC is controlling the robot
     * @param nextRos the operation to do immediately after sending the command if ROS is controlling the robot
     */
    public void robotDoCarScanInPos(ScanPos pos, Runnable nextPlc, Runnable nextRos) {
        if (usePlc) {
            if (pos == ScanPos.LEFT) {
                sendRobotCommand(PLC_ROBOT_SCAN_LEFT, PLC_ROBOT_NEXT_ANY); // we have a picture, just do the next
            } else {
                sendRobotCommand(PLC_ROBOT_SCAN_RIGHT, PLC_ROBOT_NEXT_ANY); // we have a picture, just do the next
            }
            nextPlc.run();
        } else {
            if (pos == ScanPos.LEFT) {
                callRobot(2);
            } else {
                callRobot(3);
            }
            nextRos.run();
        }
    }

    /**
     * Requests the robot to signal the success (happy movement). [public for testing]
     */
    public void robotDoSignalSuccess() {
        if (usePlc) {
            sendRobotCommand(PLC_ROBOT_HAPPY, PLC_ROBOT_NEXT_HAPPY);
        } else {
            callRobot(ROS_ROBOT_HAPPY);
        }
    }

    /**
     * Requests the robot to signal car configuration failure (sadness movement). [public for testing]
     */
    public void robotDoSignalFailure() {
        if (usePlc) {
            sendRobotCommand(PLC_ROBOT_HAPPY, PLC_ROBOT_NEXT_UNHAPPY);
        } else {
            callRobot(ROS_ROBOT_UNHAPPY);
        }
    }
    
    /**
     * Requests the robot to go to base position. [public for testing]
     */
    public void robotGoToBasePostion() {
        if (usePlc) {
            sendRobotCommand(PLC_ROBOT_BASE, PLC_ROBOT_NEXT_BASE);
        } else {
            callRobot(ROS_ROBOT_BASE);
        }
    }

    /**
     * Sends a quit signal to the robot.
     */
    public void robotConfirmQuit() {
        if (usePlc) {
            sendRobotCommand(PLC_ROBOT_NOP, PLC_ROBOT_NEXT_QUIT);
        } // as we have no information on that without PLC, no Python action here
    }
    
    /**
     * Returns whether the PLC is ready to request. [mocking]
     * 
     * @return {@code true} if the PLC is ready, {@code false} else
     */
    protected boolean readyForRequest() {
        return readyForRequest;
    }
    
    /**
     * Sends a robot command.
     * 
     * @param op the robot operation number as agreed
     * @param next the next command in pre-defined movement chain, ignored if negative
     */
    protected void sendRobotCommand(int op, int next) {
        if (readyForRequest()) {
            PlcInput pi = new PlcInputImpl();
            pi.setPC_StartOperation(false); // just to be sure
            if (next == PLC_ROBOT_NEXT_QUIT) {
                pi.setPC_Quit(true);
            } else if (next > 0) {
                pi.setPC_Command01(next); // ok, just do the next step
            } else {
                pi.setPC_RequestedOperation(op);
                pi.setPC_StartOperation(true); // TODO PLC separate?
            }
            // ignore the others for now
            ingestPlcInput(pi);
        } else {
            System.out.println("Cannot send robot command, PLC not ready");
        }
    }

    /**
     * Sends out a command.
     * 
     * @param cmd the command to be sent
     */
    protected void sendCommand(Command cmd) {
        if (!hasCommandIngestor()) {
            LoggerFactory.getLogger(Commands.class).error("Cannot send command. No ingestor present.");
        }
        ingestCommand(cmd);
    }

    /**
     * Sends out an internal application command.
     * 
     * @param cmd the command to be sent
     */
    private void sendCommand(Commands cmd) {
        if (!hasCommandIngestor()) {
            LoggerFactory.getLogger(Commands.class).error("Cannot send command. No ingestor present.");
        }
        ingestCommand(Commands.createCommand(cmd));
    }

    /**
     * Calls Tan robot script.
     * 
     * @param pos the position for the script
     */
    protected void callRobot(int pos) {
        ProcessSupport.callPython(robotScriptOwner, "move_robot.py", null, "--pos", String.valueOf(pos), "--env", 
            System.getProperty("iip.app.hm22.env", "HM22"));
    }
    
    /**
     * Changes the state and records the change timestamp.
     * 
     * @param state the new state
     */
    private void setCurrentState(State state) {
        this.currentState = state;
        if (State.STOP == state) {
            aiResults.clear();
            lastAiResultsTimestamp = aiResultsTimestamp;
            aiResultsTimestamp = 0;
        }
        this.lastStateChange = System.currentTimeMillis();
    }
    
}
